 package jp.ac.osaka_u.ist.sel.metricstool.rfc;
 
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.HashSet;
 import java.util.Set;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.accessor.ClassInfoAccessor;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.metric.MetricAlreadyRegisteredException;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetMethodInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.plugin.AbstractPlugin;
 import jp.ac.osaka_u.ist.sel.metricstool.main.util.LANGUAGE;
 import jp.ac.osaka_u.ist.sel.metricstool.main.util.LanguageUtil;
 import jp.ac.osaka_u.ist.sel.metricstool.main.util.METRIC_TYPE;
 
 
 /**
  * RFCvvOCNX.
  * 
  * @author rniitani
  */
 public class RfcPlugin extends AbstractPlugin {
     /**
      * ڍא萔
      */
     private final static String DETAIL_DESCRIPTION;
 
     /**
      * gNXvJnD
      */
     @Override
     protected void execute() {
         // NXANZT擾
         final ClassInfoAccessor classAccessor = this.getClassInfoAccessor();
 
         // i񍐗p
         int measuredClassCount = 0;
         final int maxClassCount = classAccessor.getClassCount();
 
         //SNXɂ
         for (final TargetClassInfo targetClass : classAccessor) {
             // ̐ RFC
             final Set<MethodInfo> rfcMethods = new HashSet<MethodInfo>();
 
             // ݂̃NXŒ`Ă郁\bh
             final Set<TargetMethodInfo> localMethods = targetClass.getDefinedMethods();
             rfcMethods.addAll(localMethods);
 
             // localMethods ŌĂ΂Ă郁\bh
             for (final TargetMethodInfo m : localMethods) {
                 rfcMethods.addAll(m.getCallees());
             }
 
             try {
                 this.registMetric(targetClass, rfcMethods.size());
             } catch (final MetricAlreadyRegisteredException e) {
                 this.err.println(e);
             }
 
             //1NXƂ%Ői
             this.reportProgress(++measuredClassCount * 100 / maxClassCount);
         }
     }
 
     /**
      * ̃vOC̊ȈՐ1sŕԂ
      * @return ȈՐ
      */
     @Override
     protected String getDescription() {
         return "Measuring the RFC metric.";
     }
 
     /**
      * ̃vOC̏ڍאԂ
      * @return@ڍא
      */
     @Override
     protected String getDetailDescription() {
         return DETAIL_DESCRIPTION;
     }
 
     /**
      * ̃vOCgNXvł錾ԂD
      * 
      * vΏۂ̑S̒ŃIuWFNgwł̂̔zԂD
      * 
      * @return IuWFNgw̔z
      * @see jp.ac.osaka_u.ist.sel.metricstool.main.util.LANGUAGE
      */
     @Override
     protected LANGUAGE[] getMeasurableLanguages() {
         return LanguageUtil.getObjectOrientedLanguages();
     }
 
     /**
      * gNXԂD
      * 
      * @return gNX
      */
     @Override
     protected String getMetricName() {
         return "RFC";
     }
 
     /**
      * ̃vOCv郁gNX̃^CvԂD
      * 
      * @return gNX^Cv
      * @see jp.ac.osaka_u.ist.sel.metricstool.main.util.METRIC_TYPE
      */
     @Override
     protected METRIC_TYPE getMetricType() {
         return METRIC_TYPE.CLASS_METRIC;
     }
 
     /**
      * ̃vOCNXɊւ𗘗p邩ǂԂ\bhD
      * trueԂD
      * 
      * @return trueD
      */
     @Override
     protected boolean useClassInfo() {
         return true;
     }
 
     /**
      * ̃vOC\bhɊւ𗘗p邩ǂԂ\bhD
      * trueԂD
      * 
      * @return trueD
      */
     @Override
     protected boolean useMethodInfo() {
         return true;
     }
 
     /**
      * ̃vOC\bhɊւ𗘗p邩ǂԂ\bh.
      * trueԂD
      * 
      * @return trueD
      */
     @Override
     protected boolean useMethodLocalInfo() {
        return true;
     }
 
     static {
         // DETAIL_DESCRIPTION 
         {
             StringWriter buffer = new StringWriter();
             PrintWriter writer = new PrintWriter(buffer);
 
             writer.println("This plugin measures the RFC (Response for a Class) metric.");
             writer.println();
             writer.println("RFC = number of local methods in a class");
             writer.println("    + number of remote methods called by local methods");
             writer.println();
             writer.println("A given remote method is counted by once.");
             writer.println();
             writer.flush();
 
             DETAIL_DESCRIPTION = buffer.toString();
         }
     }
 
 }
