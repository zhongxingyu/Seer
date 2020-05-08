 package jp.ac.osaka_u.ist.sel.metricstool.dit;
 
 
import java.util.List;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfo;
import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.plugin.AbstractClassMetricPlugin;
 
 
 /**
  * DIT(CKgNX1)vZ郁gNX
  * <p>
  * SẴIuWFNgwɑΉ
  * 
  * @author y-higo
  * 
  */
 public class DITPlugin extends AbstractClassMetricPlugin {
 
     /**
      * ŗ^ꂽNXDITvZ
      * 
      * @param targetClass DITvZΏۃNX
      * @return vZ
      */
     @Override
     protected Number measureClassMetric(TargetClassInfo targetClass) {
 
         ClassInfo classInfo = targetClass;
         for (int depth = 1;; depth++) {
 
            final List<ClassTypeInfo> superClasses = classInfo.getSuperClasses();
             if (0 == superClasses.size()) {
                 return depth;
             }
            classInfo = superClasses.get(0).getReferencedClass();
         }
     }
 
     /**
      * ̃vOC̊ȈՐPsŕԂ
      * 
      * @return ȈՐ
      */
     @Override
     protected String getDescription() {
         return "Measuring the DIT metric.";
     }
 
     /**
      * ̃vOCv郁gNX̖OԂ
      * 
      * @return DIT
      */
     @Override
     protected String getMetricName() {
         return "DIT";
     }
 
     /**
      * ̃vOCtB[hɊւ𗘗p邩ǂԂ\bhD falseԂD
      * 
      * @return falseD
      */
     @Override
     protected boolean useFieldInfo() {
         return false;
     }
 
     /**
      * ̃vOC\bhɊւ𗘗p邩ǂԂ\bhD falseԂD
      * 
      * @return falseD
      */
     @Override
     protected boolean useMethodInfo() {
         return false;
     }
 }
