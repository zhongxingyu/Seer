 package org.jenkinsci.plugins.vs_code_metrics;
 
 import hudson.model.AbstractBuild;
 import hudson.model.ModelObject;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.Map;
 
 import org.jenkinsci.plugins.vs_code_metrics.bean.AbstractBean;
 import org.jenkinsci.plugins.vs_code_metrics.util.CodeMetricsUtil;
 import org.jenkinsci.plugins.vs_code_metrics.util.Constants;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 
 public abstract class AbstractReport implements Serializable, ModelObject {
 
     private static final long serialVersionUID = 1L;
 
     private AbstractBuild<?,?> build;
     private String name;
     private AbstractBean<?> result;
     private boolean depthOfInheritance = true;
     private boolean childUrlLink = true;
 
     /**
      *
      * @param build
      * @param name
      * @param result
      */
     protected AbstractReport(AbstractBuild<?,?> build, String name, AbstractBean<?> result) {
         this.build = build;
         this.name = name;
         this.result = result;
     }
 
     public AbstractBuild<?, ?> getBuild() {
         return build;
     }
 
     public String getName() {
         return name;
     }
 
     public String getDisplayName() {
         return name;
     }
 
     public boolean isDepthOfInheritance() {
         return depthOfInheritance;
     }
 
     public void setDepthOfInheritance(boolean depthOfInheritance) {
         this.depthOfInheritance = depthOfInheritance;
     }
     public boolean isChildUrlLink() {
         return childUrlLink;
     }
 
     public void setChildUrlLink(boolean childUrlLink) {
         this.childUrlLink = childUrlLink;
     }
 
     public Object getDynamic(final String token, final StaplerRequest req, final StaplerResponse rsp) {
         return getReport(token);
     }
 
     public void doGraph(final StaplerRequest req, final StaplerResponse rsp) throws IOException {
        String[] buildTokens = CodeMetricsUtil.getBuildActionTokens(req.getOriginalRequestURI(), req.getContextPath());
         AbstractGraph graph = new MaintainabilityIndexGraph(build, buildTokens, build.getTimestamp(), Constants.REPORT_GRAPH_WIDTH, Constants.REPORT_GRAPH_HEIGHT);
         graph.doPng(req, rsp);
     }
 
     public void doCycGraph(final StaplerRequest req, final StaplerResponse rsp) throws IOException {
        String[] buildTokens = CodeMetricsUtil.getBuildActionTokens(req.getOriginalRequestURI(), req.getContextPath());
         AbstractGraph graph = new CyclomaticComplexityGraph(build, buildTokens, build.getTimestamp(), Constants.REPORT_GRAPH_WIDTH, Constants.REPORT_GRAPH_HEIGHT);
         graph.doPng(req, rsp);
     }
 
     public Object getResult() {
         return result;
     }
 
     public boolean hasChildren() {
         return ((result != null) && (result.getChildren().size() > 0));
     }
 
     public Map<String, ?> getChildren() {
         return result.getChildren();
     }
 
     public abstract Object getReport(String token);
 
 }
