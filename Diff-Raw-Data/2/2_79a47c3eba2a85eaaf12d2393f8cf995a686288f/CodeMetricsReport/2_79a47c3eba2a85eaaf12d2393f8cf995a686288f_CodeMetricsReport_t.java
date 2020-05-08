 package org.jenkinsci.plugins.vs_code_metrics;
 
 import hudson.model.AbstractBuild;
 
 import org.jenkinsci.plugins.vs_code_metrics.bean.CodeMetrics;
 import org.jenkinsci.plugins.vs_code_metrics.bean.Module;
 
 public final class CodeMetricsReport extends AbstractReport {
 
     /**
      *
      * @param build
      * @param result
      */
     public CodeMetricsReport(AbstractBuild<?, ?> build, CodeMetrics result) {
        super(build, Messages.CodeMetricsReport_DisplayName(), result);
     }
 
     @Override
     public Object getReport(String token) {
         if ((getChildren() != null) && getChildren().containsKey(token))
             return new ModuleReport(getBuild(), (Module)getChildren().get(token));
         else
             return this;
     }
 }
