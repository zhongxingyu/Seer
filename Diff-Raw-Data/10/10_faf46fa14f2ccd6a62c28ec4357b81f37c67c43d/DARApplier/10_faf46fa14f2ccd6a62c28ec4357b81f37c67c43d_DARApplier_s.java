 /*
  * Created on Nov 22, 2004
  */
 package uk.org.ponder.mapping;
 
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.Map;
 
 import uk.org.ponder.beanutil.BeanModelAlterer;
 import uk.org.ponder.beanutil.BeanResolver;
 import uk.org.ponder.beanutil.BeanUtil;
 import uk.org.ponder.beanutil.PathUtil;
 import uk.org.ponder.beanutil.PropertyAccessor;
 import uk.org.ponder.beanutil.WriteableBeanLocator;
 import uk.org.ponder.conversion.ConvertUtil;
 import uk.org.ponder.conversion.VectorCapableParser;
 import uk.org.ponder.errorutil.CoreMessages;
 import uk.org.ponder.errorutil.PropertyException;
 import uk.org.ponder.errorutil.TargettedMessage;
 import uk.org.ponder.errorutil.TargettedMessageList;
 import uk.org.ponder.reflect.ReflectUtils;
 import uk.org.ponder.reflect.ReflectiveCache;
 import uk.org.ponder.saxalizer.MethodAnalyser;
 import uk.org.ponder.saxalizer.SAXAccessMethod;
 import uk.org.ponder.saxalizer.SAXalXMLProvider;
 import uk.org.ponder.saxalizer.SAXalizerMappingContext;
 import uk.org.ponder.stringutil.StringList;
 import uk.org.ponder.util.Denumeration;
 import uk.org.ponder.util.EnumerationConverter;
 import uk.org.ponder.util.Logger;
 import uk.org.ponder.util.UniversalRuntimeException;
 
 /**
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  * 
  */
 public class DARApplier implements BeanModelAlterer {
   private SAXalXMLProvider xmlprovider;
   private SAXalizerMappingContext mappingcontext;
   private VectorCapableParser vcp;
   private ReflectiveCache reflectivecache;
 
   public void setSAXalXMLProvider(SAXalXMLProvider saxal) {
     xmlprovider = saxal;
   }
 
   public void setMappingContext(SAXalizerMappingContext mappingcontext) {
     this.mappingcontext = mappingcontext;
     vcp = new VectorCapableParser();
     vcp.setScalarParser(mappingcontext.saxleafparser);
   }
   
   public SAXalizerMappingContext getMappingContext() {
     return mappingcontext;
   }
 
   public void setReflectiveCache(ReflectiveCache reflectivecache) {
     this.reflectivecache = reflectivecache;
   }
 
   public Object getFlattenedValue(String fullpath, Object root,
       Class targetclass, BeanResolver resolver) {
     Object toconvert = getBeanValue(fullpath, root);
     if (toconvert == null)
       return null;
     if (targetclass == String.class || targetclass == Boolean.class) {
       String rendered = mappingcontext.saxleafparser.render(toconvert);
       return targetclass == String.class ? rendered
           : mappingcontext.saxleafparser.parse(Boolean.class, rendered);
     }
     else {
       // It MUST be String[], AND the value must be a container.
       // this had BETTER be a Container otherwise we will fail to update the
       // value
       // in setBeanValue below.
       Collection collection = (Collection) toconvert;
       String[] target = new String[collection.size()];
       vcp.render(collection, target, resolver);
       return target;
     }
   }
 
   public Object getBeanValue(String fullpath, Object rbl) {
     Object togo = BeanUtil.navigate(rbl, fullpath, mappingcontext);
     return togo;
   }
 
   // a convenience method to have the effect of a "set" ValueBinding,
   // constructs a mini-DAR just for setting. Errors will be accumulated
   // into ThreadErrorState
   // NB there are two calls in the workspace, both from PostHandler.applyValues.
   // Should we really try to do away with the ThreadErrorState?
   // Yes, we should! Since it has now become an invisible load-time issue.
   // We are currently making lots of things lazy just so they don't
   // precipitate an "early" getting of this error state that may be empty.
   // Also people may have different error contexts for different targets.
   public void setBeanValue(String fullpath, Object root, Object value,
       TargettedMessageList messages) {
     DataAlterationRequest dar = new DataAlterationRequest(fullpath, value);
     // messages.pushNestedPath(headpath);
     // try {
     applyAlteration(root, dar, messages);
     // }
     // finally {
     // messages.popNestedPath();
     // }
   }
 
   public Object invokeBeanMethod(String fullpath, Object rbl) {
     String totail = PathUtil.getToTailPath(fullpath);
     String method = PathUtil.getTailPath(fullpath);
     try {
       Object bean = BeanUtil.navigate(rbl, totail, mappingcontext);
       return reflectivecache.invokeMethod(bean, method);
     }
     catch (Throwable t) { // Need to grab "NoSuchMethodError"
       throw UniversalRuntimeException.accumulate(t, "Error invoking method "
           + method + " in bean at path " + totail);
     }
   }
 
   private void applyAlteration(Object rootobj, DataAlterationRequest dar,
       TargettedMessageList messages) {
    Logger.log.info("Applying DAR " + dar.type + " to path " + dar.path + ": "
         + dar.data);
     String totail = PathUtil.getToTailPath(dar.path);
     // TODO: Pause at any DARReceiver we discover in the model and instead
     // queue the requests there.
     Object moveobj = BeanUtil.navigate(rootobj, totail, mappingcontext);
     Object convert = dar.data;
     String tail = PathUtil.getTailPath(dar.path);
     PropertyAccessor pa = MethodAnalyser.getPropertyAccessor(moveobj,
         mappingcontext);
     Class leaftype = pa.getPropertyType(tail);
     if (dar.type.equals(DataAlterationRequest.ADD)) {
       // If we got a list of Strings in from
       // the UI, they may be "cryptic" leaf types without proper packaging. This
       // implies we MUST know the element type of the collection.
       // For now we must assume collection is of leaf types.
       if (pa.isMultiple(tail)) {
         Object lastobj = pa.getProperty(moveobj, tail);
 
         SAXAccessMethod sam = mappingcontext.getAnalyser(moveobj.getClass())
             .getAccessMethod(tail);
         if (convert instanceof String) {
           // deference to Spring "auto-convert from comma-separated list"
           convert = StringList.fromString((String) convert);
         }
         if (lastobj == null) {
           lastobj = ReflectUtils.instantiateContainer(leaftype,
               EnumerationConverter.getEnumerableSize(convert), reflectivecache);
           pa.setProperty(moveobj, tail, lastobj);
         }
         if (VectorCapableParser.isLOSType(convert)) {
           if (lastobj instanceof Collection) {
             ((Collection) lastobj).clear();
           }
           vcp.parse(convert, lastobj, sam.getAccessedType());
         }
         else { // must be a single item, or else a collection
           Denumeration den = EnumerationConverter.getDenumeration(lastobj);
         
           if (EnumerationConverter.isEnumerable(convert.getClass())) {
             for (Enumeration enumm = EnumerationConverter.getEnumeration(convert);
             enumm.hasMoreElements();) {
               den.add(enumm.nextElement());
             }
           }
           else {
             den.add(convert);
           }
         }
       }
       else { // property is a scalar type, possibly composite.
         if (convert instanceof String[]) {
           convert = ((String[]) convert)[0];
         }
         // Step 1 - attempt to convert the dar value if it is still a String,
         // using our now knowledge of the target leaf type.
         if (convert instanceof String) {
           String string = (String) convert;
           try {
             if (messages != null)
               messages.pushNestedPath(totail);
             convert = ConvertUtil.parse(string, xmlprovider, leaftype);
           }
           finally {
             if (messages != null)
               messages.popNestedPath();
           }
         }
         // this case also deals with Maps and WBLs.
         pa.setProperty(moveobj, tail, convert);
       }
     }
     // at this point, moveobj contains the object BEFORE the final path
     // section.
 
     else if (dar.type.equals(DataAlterationRequest.DELETE)) {
       try {
         boolean removed = false;
         // if we have data, we can try to remove it by value
         if (convert != null) {
           Object lastobj = pa.getProperty(moveobj, tail);
           if (lastobj instanceof Collection) {
             removed = ((Collection) lastobj).remove(convert);
           }
           else if (lastobj instanceof Map) {
             removed = ((Map) moveobj).remove(convert) != null;
           }
         }
         else { // there is no data, so it must be a Map, concrete or
           // WriteableBeanLocator.
           if (moveobj instanceof Map) {
             removed = ((Map) moveobj).remove(tail) != null;
           }
           else if (moveobj instanceof WriteableBeanLocator) {
             removed = ((WriteableBeanLocator) moveobj).remove(tail);
           }
           else {
             pa.setProperty(moveobj, tail, null);
           }
         }
         if (!removed) {
           throw UniversalRuntimeException.accumulate(new PropertyException());
         }
       }
       catch (Exception e) {
         if (messages != null) {
           TargettedMessage message = new TargettedMessage(
               CoreMessages.MISSING_DATA_ERROR, dar.path);
           messages.addMessage(message);
         }
         Logger.log.warn("Couldn't remove object " + convert + " from path "
             + dar.path);
       }
     }
   }
 
   /**
    * Apply the alterations mentioned in the enclosed DARList to the supplied
    * bean. Note that this method assumes that the TargettedMessageList is
    * already navigated to the root path referred to by the bean, and that the
    * DARList mentions paths relative to that bean.
    * 
    * @param rootobj The object to which alterations are to be applied
    * @param toapply The list of alterations
    * @param messages The list to which error messages accreted during
    *          application are to be appended. This is probably the same as that
    *          in the ThreadErrorState, but is supplied as an argument to reduce
    *          costs of ThreadLocal gets.
    */
   public void applyAlterations(Object rootobj, DARList toapply,
       TargettedMessageList messages) {
     for (int i = 0; i < toapply.size(); ++i) {
       DataAlterationRequest dar = toapply.DARAt(i);
       applyAlteration(rootobj, dar, messages);
     }
   }
 
 }
