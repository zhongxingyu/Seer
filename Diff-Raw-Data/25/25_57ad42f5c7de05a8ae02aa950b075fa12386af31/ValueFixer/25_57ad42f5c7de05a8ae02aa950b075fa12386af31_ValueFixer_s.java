 /*
  * Created on Nov 1, 2005
  */
 package uk.org.ponder.rsf.componentprocessor;
 
 import uk.org.ponder.beanutil.BeanLocator;
 import uk.org.ponder.beanutil.BeanModelAlterer;
 import uk.org.ponder.beanutil.BeanResolver;
 import uk.org.ponder.beanutil.BeanUtil;
 import uk.org.ponder.conversion.LeafObjectParser;
 import uk.org.ponder.rsf.components.UIBound;
 import uk.org.ponder.rsf.components.UIBoundList;
 import uk.org.ponder.rsf.components.UIBoundObject;
 import uk.org.ponder.rsf.components.UIBoundString;
 import uk.org.ponder.rsf.components.UIComponent;
import uk.org.ponder.rsf.components.UIInput;
 import uk.org.ponder.rsf.components.UIParameter;
 import uk.org.ponder.rsf.request.FossilizedConverter;
 import uk.org.ponder.rsf.request.RequestSubmittedValueCache;
 import uk.org.ponder.rsf.request.SubmittedValueEntry;
 import uk.org.ponder.rsf.uitype.UITypes;
 import uk.org.ponder.util.UniversalRuntimeException;
 
 /**
  * Fetches values from the request bean model that are referenced via EL value
  * bindings, if such have not already been set. Will also compute the fossilized
  * binding for this component (not a completely cohesively coupled set of
  * functions, but we are accumulating quite a lot of little processors).
  * 
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  */
 
 public class ValueFixer implements ComponentProcessor {
   private BeanLocator beanlocator;
   private BeanModelAlterer alterer;
   private RequestSubmittedValueCache rsvc;
 
   public void setBeanLocator(BeanLocator beanlocator) {
     this.beanlocator = beanlocator;
   }
 
   public void setModelAlterer(BeanModelAlterer alterer) {
     this.alterer = alterer;
   }
 
   public void setRequestRSVC(RequestSubmittedValueCache rsvc) {
     this.rsvc = rsvc;
   }
 
   // This dependency is here so we can free FC from instance wiring cycle on
   // RenderSystem. A slight loss of efficiency since this component may never
   // be rendered - we might think about "lazy processors" at some point...
   private FossilizedConverter fossilizedconverter;
 
   public void setFossilizedConverter(FossilizedConverter fossilizedconverter) {
     this.fossilizedconverter = fossilizedconverter;
   }
 
   public void processComponent(UIComponent toprocesso) {
     if (toprocesso instanceof UIBound) {
       UIBound toprocess = (UIBound) toprocesso;
       // If there is a value in the SVE, return it to the control.
       SubmittedValueEntry sve = rsvc.byID(toprocess.getFullID());
       if (sve != null) {
         toprocess.updateValue(sve.newvalue);
       }
       else if (toprocess.valuebinding != null
           && (toprocess.acquireValue() == null || UITypes
               .isPlaceholder(toprocess.acquireValue()))) {
         // a bound component ALWAYS contains a value of the correct type.
         Object oldvalue = toprocess.acquireValue();
         String stripbinding = toprocess.valuebinding.value;
         BeanResolver resolver = getResolver(toprocess);
 
         Object flatvalue = alterer.getFlattenedValue(stripbinding, beanlocator,
             oldvalue.getClass(), resolver);
         if (flatvalue != null) {
           toprocess.updateValue(flatvalue);
         }
       }
 
       // TODO: Think carefully whether we want these "encoded" bindings to
       // EVER appear in the component tree. Tradeoffs - we would need to create
       // more
       // classes that renderer could recognise to compute bindings, and increase
       // its
       // knowledge about the rest of RSF.
       if (toprocess.fossilize && toprocess.fossilizedbinding == null) {
         UIParameter fossilized = fossilizedconverter
             .computeFossilizedBinding(toprocess);
         toprocess.fossilizedbinding = fossilized;
       }
     }
   }
 
   private BeanResolver getResolver(UIBound toprocess) {
     if (toprocess instanceof UIBoundString) {
       UIBoundObject boundrenderer = ((UIBoundString) toprocess).renderer;
       if (boundrenderer == null) {
         return null;
       }
       final Object renderer = boundrenderer.getValue();
      
       if (renderer instanceof BeanResolver) {
         return (BeanResolver) renderer;
       }
       else if (renderer instanceof LeafObjectParser) {
         return new BeanResolver() {
           public String resolveBean(Object bean) {
             return ((LeafObjectParser) renderer).render(bean);
           }
         };
       }
       else
         throw UniversalRuntimeException.accumulate(
             new IllegalArgumentException(), "Renderer object for "
                 + toprocess.getFullID() + " of unrecognised "
                 + renderer.getClass()
                 + " (expected BeanResolver or LeafObjectParser)");
     }
     else if (!(toprocess instanceof UIBoundList))
       return null;
     String resolverel = ((UIBoundList) toprocess).fieldresolver;
     if (resolverel == null)
       return null;
     resolverel = BeanUtil.stripEL(resolverel);
     return (BeanResolver) alterer.getBeanValue(resolverel, beanlocator);
   }
 
 }
