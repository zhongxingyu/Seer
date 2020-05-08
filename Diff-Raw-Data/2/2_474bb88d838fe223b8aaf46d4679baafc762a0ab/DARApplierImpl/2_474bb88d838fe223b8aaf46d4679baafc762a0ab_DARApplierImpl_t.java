 /*
  * Created on 13 Feb 2008
  */
 package uk.org.ponder.mapping.support;
 
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.Map;
 
 import uk.org.ponder.beanutil.WriteableBeanLocator;
 import uk.org.ponder.conversion.GeneralConverter;
 import uk.org.ponder.conversion.VectorCapableParser;
 import uk.org.ponder.errorutil.CoreMessages;
 import uk.org.ponder.errorutil.PropertyException;
 import uk.org.ponder.iterationutil.Denumeration;
 import uk.org.ponder.iterationutil.EnumerationConverter;
 import uk.org.ponder.iterationutil.SingleEnumeration;
 import uk.org.ponder.messageutil.TargettedMessage;
 import uk.org.ponder.reflect.ReflectUtils;
 import uk.org.ponder.reflect.ReflectiveCache;
 import uk.org.ponder.saxalizer.AccessMethod;
 import uk.org.ponder.saxalizer.SAXalizerMappingContext;
 import uk.org.ponder.stringutil.StringList;
 import uk.org.ponder.util.Logger;
 import uk.org.ponder.util.UniversalRuntimeException;
 
 public class DARApplierImpl {
   private SAXalizerMappingContext mappingcontext;
   private ReflectiveCache reflectivecache;
   private VectorCapableParser vcp;
   private GeneralConverter generalConverter;
   private boolean springmode;
 
   public void setGeneralConverter(GeneralConverter generalConverter) {
     this.generalConverter = generalConverter;
   }
 
   public void setMappingContext(SAXalizerMappingContext mappingcontext) {
     this.mappingcontext = mappingcontext;
   }
 
   public void setReflectiveCache(ReflectiveCache reflectivecache) {
     this.reflectivecache = reflectivecache;
   }
 
   public void setVectorCapableParser(VectorCapableParser vcp) {
     this.vcp = vcp;
   }
 
   public void setSpringMode(boolean springmode) {
     this.springmode = springmode;
   }
 
   public void processAddition(DARApplyEnvironment d) {
     boolean isleaf = mappingcontext.generalLeafParser.isLeafType(d.leaftype);
     // If we got a list of Strings in from the UI, they may be
     // "cryptic" leaf types without proper packaging.
     // This implies we MUST know the element type of the collection.
     // For now we must assume collection is of leaf types.
     if (d.pa.isMultiple(d.moveobj, d.tail) || 
         d.convert != null
         && EnumerationConverter.isEnumerable(d.convert.getClass())
         && !isleaf) {
       Object lastobj = d.pa.getProperty(d.moveobj, d.tail);
 
       AccessMethod sam = mappingcontext.getAnalyser(d.moveobj.getClass())
           .getAccessMethod(d.tail);
       Class declared = sam.getDeclaredType();
       Class container = declared == Object.class && lastobj != null ? lastobj.getClass()
           : declared;
       if (d.convert instanceof String && springmode) {
         // deference to Spring "auto-convert from comma-separated list"
         // NB this is currently disused, RSACBeanLocator does not use
         // DARApplier yet.
         d.convert = StringList.fromString((String) d.convert);
       }
 
       if (d.convert instanceof String && d.dar.applyconversions) {
         String string = (String) d.convert;
         d.convert = generalConverter.parse(string, container, d.dar.encoding);
       }
       boolean resetrequired = false;
 
       if (container == Object.class) {
         lastobj = d.convert;
         resetrequired = true;
       }
       else {
         int incomingsize = EnumerationConverter.getEnumerableSize(d.convert);
 
         // reinstantiate the existing object if it is not there or an array of the wrong size
         if (lastobj == null || lastobj.getClass().isArray()
             && EnumerationConverter.getEnumerableSize(lastobj) != incomingsize) {
 
           lastobj = ReflectUtils.instantiateContainer(container, incomingsize,
               reflectivecache);
           resetrequired = true;
         }
         if (VectorCapableParser.isLOSType(d.convert)) {
           if (lastobj instanceof Collection) {
             ((Collection) lastobj).clear();
           }
           // TODO: for JDK collections, "leaftype" will be equal to the
           // collection type unless we have got type info from elsewhere.
           // for now, use arrays.
           vcp.parse(d.convert, lastobj, d.leaftype);
 
         }
         else { // must be a single item, or else a collection
           Denumeration den = EnumerationConverter.getDenumeration(lastobj,
               reflectivecache);
           // TODO: use CompletableDenumeration here to support extensible arrays.
           if (EnumerationConverter.isEnumerable(d.convert.getClass())) {
             for (Enumeration enumm = EnumerationConverter.getEnumeration(d.convert); enumm
                 .hasMoreElements();) {
               den.add(enumm.nextElement());
             }
           }
           else {
             den.add(d.convert);
           }
         }
       } // end if actual vector conversion was required
       if (resetrequired) {
         d.pa.setProperty(d.moveobj, d.tail, lastobj);
       }
 
     }
     else { // property is a scalar type, possibly composite.
       if (d.convert instanceof String[]) {
         d.convert = ((String[]) d.convert)[0];
       }
       // Attempt to convert the dar value if it is still a String,
       // using our now knowledge of the target leaf type.
       // (case of guard invocation, for example)
       if (d.convert instanceof String && d.dar.applyconversions) {
         String string = (String) d.convert;
         // but try no further conversion if no encoding is specified and we have no type
         // info
         if (!(d.leaftype == Object.class && d.dar.encoding == null)) {
           d.convert = generalConverter.parse(string, d.leaftype, d.dar.encoding);
         }
       }
       // this case also deals with Maps and WBLs.
       d.pa.setProperty(d.moveobj, d.tail, d.convert);
     }
   }
 
   public void processDeletion(DARApplyEnvironment d) {
     try {
       boolean failedremove = false;
       Object removetarget = null;
       // if we have data, we can try to remove it by value
       if (d.convert == null) {
         removetarget = d.moveobj;
         d.convert = d.tail;
       }
       else {
         removetarget = d.pa.getProperty(d.moveobj, d.tail);
       }
 
       // this decision is not quite right for "Map" but we have no way
       // to declare the type of the container.
       if (removetarget instanceof WriteableBeanLocator || removetarget instanceof Map) {
         d.leaftype = String.class;
       }
       Enumeration values = null;
       if (EnumerationConverter.isEnumerable(d.convert.getClass())) {
         values = EnumerationConverter.getEnumeration(d.convert);
       }
       else {
         values = new SingleEnumeration(d.convert);
       }
 
       while (values.hasMoreElements()) {
 
         Object toremove = values.nextElement();
         // copied code from "ADD" branch. Regularise this conversion at
         // some point.
         if (d.dar.applyconversions) {
           if (toremove instanceof String) {
             String string = (String) toremove;
             toremove = generalConverter.parse(string, d.leaftype, d.dar.encoding);
           }
           else if (d.leaftype == String.class) {
             toremove = generalConverter.render(toremove, d.dar.encoding);
           }
         }
         if (removetarget instanceof WriteableBeanLocator) {
           if (!((WriteableBeanLocator) removetarget).remove((String) toremove)) {
             failedremove = true;
           }
         }
         else if (removetarget instanceof Collection) {
           if (!((Collection) removetarget).remove(toremove)) {
             failedremove = true;
           }
         }
         else if (removetarget instanceof Map) {
           if (((Map) removetarget).remove(toremove) == null) {
             failedremove = true;
           }
         }
         else {
           d.pa.setProperty(removetarget, (String) d.convert, null);
         }
       }
 
       if (failedremove) {
         throw UniversalRuntimeException.accumulate(new PropertyException());
       }
     }
     catch (Exception e) {
       if (d.darenv != null) {
         TargettedMessage message = new TargettedMessage(CoreMessages.MISSING_DATA_ERROR,
             d.dar.path);
         d.darenv.messages.addMessage(message);
       }
       Logger.log.warn("Couldn't remove object " + d.convert + " from path " + d.dar.path,
           e);
     }
 
   }
 }
