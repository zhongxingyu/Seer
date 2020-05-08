 /*
  * Created on 7 Aug 2006
  */
 package uk.org.ponder.springutil;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.config.BeanPostProcessor;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 
 import uk.org.ponder.arrayutil.MapUtil;
 import uk.org.ponder.saxalizer.AccessMethod;
 import uk.org.ponder.saxalizer.MethodAnalyser;
 import uk.org.ponder.saxalizer.SAXalizerMappingContext;
 
 /**
  * Does the work of collecting and focusing all the distributed property
  * deliveries onto the target list-valued bean property.
  * 
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  * 
  */
 
 public class TLABPostProcessor implements BeanPostProcessor,
     ApplicationContextAware {
 
   private Map targetMap = new HashMap();
 
   private SAXalizerMappingContext mappingContext;
 
   public void setMappingContext(SAXalizerMappingContext mappingContext) {
     this.mappingContext = mappingContext;
   }
 
   public void init() {
     if (mappingContext == null) {
       mappingContext = SAXalizerMappingContext.instance();
     }
   }
   
   public void setApplicationContext(ApplicationContext applicationContext) {
     // We do this here so that fewer will have to come after us!
     String[] viewbeans = applicationContext.getBeanNamesForType(
         TargetListAggregatingBean.class, false, false);
     for (int i = 0; i < viewbeans.length; ++i) {
       TargetListAggregatingBean tlab = (TargetListAggregatingBean) applicationContext
           .getBean(viewbeans[i]);
       MapUtil.putMultiMap(targetMap, tlab.getTargetBean(), tlab);
     }
   }
 
   public Object postProcessAfterInitialization(Object bean, String beanName) {
     return bean;
   }
 
   public Object postProcessBeforeInitialization(Object bean, String beanName)
       throws BeansException {
     // Perhaps in Ruby, Perl, or Haskell, this method body is just 4 lines!
     List tlabs = (List) targetMap.get(beanName);
     if (tlabs == null) return bean;
     Map listprops = new HashMap(); // map of property name to list
     for (int i = 0; i < tlabs.size(); ++i) {
       TargetListAggregatingBean tlab = (TargetListAggregatingBean) tlabs.get(i);
       Object value = tlab.getValue();
       if (tlab.getUnwrapLists() && value instanceof List) {
         List values = (List) value;
         for (int j = 0; j < values.size(); ++j) {
           MapUtil.putMultiMap(listprops, tlab.getTargetProperty(), values
               .get(j));
         }
       }
       else {
         MapUtil.putMultiMap(listprops, tlab.getTargetProperty(), value);
       }
     }
     for (Iterator propit = listprops.keySet().iterator(); propit.hasNext();) {
       String propname = (String) propit.next();
       Object value = listprops.get(propname);
       MethodAnalyser ma = mappingContext.getAnalyser(bean.getClass());
       AccessMethod sam = ma.getAccessMethod(propname);
      if (sam == null || !sam.canSet()) {
        throw new IllegalArgumentException("TLAB target bean " + beanName + " does not have any writeable property named " + propname);
      }
       sam.setChildObject(bean, value);
     }
     return bean;
   }
 
 }
