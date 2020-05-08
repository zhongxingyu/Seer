 package hudson.plugins.sctmexecutor.publisher.xunit;
 
 import com.thalesgroup.dtkit.junit.model.JUnitModel;
import com.thalesgroup.dtkit.metrics.model.InputMetricXSL;
import com.thalesgroup.dtkit.metrics.model.InputType;
import com.thalesgroup.dtkit.metrics.model.OutputMetric;
 
 public final class SCTMInputMetric extends InputMetricXSL {
   private static final long serialVersionUID = -38720636581583520L;
 
   @Override
   public String getXslName() {
     return "sctm-to-junit.xsl"; //$NON-NLS-1$
   }
 
   @Override
   public String getToolName() {
     return "SilkCentral TestManager";
   }
 
   @Override
   public String getToolVersion() {
     return " >2008R2";
   }
 
   @Override
   public InputType getToolType() {
     return InputType.TEST;
   }
 
   @Override
   public OutputMetric getOutputFormatType() {
     return JUnitModel.OUTPUT_JUNIT_1_0;
   }
 }
