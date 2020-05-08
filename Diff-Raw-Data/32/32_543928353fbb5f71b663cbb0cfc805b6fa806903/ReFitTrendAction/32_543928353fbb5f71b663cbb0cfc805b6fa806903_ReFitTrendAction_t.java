 package com.googlecode.refit.jenkins;
 
 import hudson.model.Action;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
import hudson.util.ChartUtil;
 import hudson.util.DataSetBuilder;
 
 import java.io.IOException;
 
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 
 public class ReFitTrendAction implements Action {
     
     private AbstractProject<?, ?> project;
 
 
 
     public ReFitTrendAction(AbstractProject<?, ?> project) {
         this.project = project;
     }
 
     @Override
     public String getIconFileName() {
         return null;
     }
 
     @Override
     public String getDisplayName() {
         return "Fit Test Result Trend";
     }
 
     @Override
     public String getUrlName() {
         return "refitTrend";
     }
 
     public boolean buildDataExists() {
        return project.getLastSuccessfulBuild() != null;
     }
     
     public void doGraph( StaplerRequest req, StaplerResponse rsp ) throws IOException {
        if (ChartUtil.awtProblemCause != null) {
            // not available. send out error message
            rsp.sendRedirect2(req.getContextPath() + "/images/headless.png");
            return;
        }
 
        getGraph().doPng(req, rsp);
     }
     
     public void doMap( StaplerRequest req, StaplerResponse rsp ) throws IOException {
        if (ChartUtil.awtProblemCause != null) {
            // not available. send out error message
            rsp.sendRedirect2(req.getContextPath() + "/images/headless.png");
            return;
        }
        getGraph().doMap(req, rsp);
     }
     
     
     
   private ReFitGraph getGraph() {
        DataSetBuilder<String, ChartLabel> data = new DataSetBuilder<String, ChartLabel>();
        
        for (AbstractBuild<?, ?> build = project.getLastSuccessfulBuild(); 
            build != null; 
            build = build.getPreviousBuild() ) {
            
            ReFitBuildAction action = build.getAction(ReFitBuildAction.class);
            if (action == null) 
                continue;
            
            ReFitTestResult result = action.getTestResult();
            if (result == null)
                continue;
            
            data.add(action.getNumRight(), "2Passed", new ChartLabel(result));
            data.add(action.getNumWrong(), "1Failed", new ChartLabel(result));
            data.add(action.getNumIgnored(), "0Skipped", new ChartLabel(result));
        }
        
        
        return new ReFitGraph(data.build());
   }    
 }
