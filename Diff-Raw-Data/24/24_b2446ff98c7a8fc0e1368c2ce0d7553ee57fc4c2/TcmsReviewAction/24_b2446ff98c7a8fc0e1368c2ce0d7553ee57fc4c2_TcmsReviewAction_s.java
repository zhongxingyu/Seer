 /*
  * Copyright (C) 2012 Red Hat, Inc.     
  * 
  * This copyrighted material is made available to anyone wishing to use, 
  * modify, copy, or redistribute it subject to the terms and conditions of the 
  * GNU General Public License v.2.
  * 
  * Authors: Adam Saleh (asaleh at redhat dot com)
  *          Jan Rusnacko (jrusnack at redhat dot com)
  */
 package NitrateIntegration;
 
 import NitrateIntegration.CommandWrapper.CommandWrapper;
 import com.redhat.nitrate.TcmsConnection;
 import com.redhat.nitrate.TcmsException;
 import com.redhat.nitrate.command.Build;
 import com.redhat.nitrate.command.TestRun;
 import hudson.matrix.AxisList;
 import hudson.model.AbstractBuild;
 import hudson.model.Action;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.ServletException;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 
 /**
  *
  * @author asaleh
  * @author jrusnack
  */
 public class TcmsReviewAction implements Action {
 
     public AbstractBuild<?, ?> build;
     public TcmsReport report;
     public TcmsReviewActionSettings settings;
     public TcmsReviewActionEnvironment environmentCheck;
     /**
      * Stores mapping from old properties/Jenkins axes(names and values) to new
      * names and values possibly changed by user
      */
     private TcmsGatherer gatherer;
         
     public TcmsReviewAction(AbstractBuild build, String serverUrl,
             String plan,
             String product,
             String product_v,
             String category,
             String priority,
             String manager,
             String env,
             String testPath) {
 
         this.build = build;
         
         this.report = new TcmsReport();
         this.settings = new TcmsReviewActionSettings(serverUrl, plan, product, product_v, category, priority, manager, env, testPath);
         this.environmentCheck = new TcmsReviewActionEnvironment();
         
         gatherer = new TcmsGatherer(this.settings.getProperties(), this.settings.getEnvironment());
         
     }
     
     /**
      * Constructor for multiconf project (contains AxisList)
      */
     public TcmsReviewAction(AbstractBuild build, AxisList axisList, String serverUrl,
             String plan,
             String product,
             String product_v,
             String category,
             String priority,
             String manager,
             String env,
             String testPath) {
         this(build, serverUrl, plan, product, product_v, category, priority, manager, env, testPath);
     }
 
     public TcmsReviewActionSettings getSettings() {
         return settings;
     }
 
     public TcmsReviewActionEnvironment getEnvironment() {
         return environmentCheck;
     }
 
     public String getIconFileName() {
         return Definitions.__ICON_FILE_NAME;
     }
 
     public String getDisplayName() {
         return Definitions.__DISPLAY_NAME;
     }
 
     public String getUrlName() {
         return Definitions.__URL_NAME;
     }
 
     public String getPrefix() {
         return Definitions.__PREFIX;
     }
 
     public TcmsGatherer getGatherer() {
         return gatherer;
     }
 
     public AbstractBuild getBuild() {
         return build;
     }
 
     // FIXME: javadoc
     public void doGather(StaplerRequest req, StaplerResponse rsp) throws IOException {
         settings.clearUpdateException();
         gatherer.clear();
         
         try {
             
             if (req.getParameter("Submit").equals("Gather report from test-files")) {
                 settings.updateCredentialsFromRequest(req);                
             } 
 
             settings.getConnectionAndUpdate();
             settings.getEnvironment().reloadEnvId();
             report.checkEnvironmentMapping(settings.getEnvironment());
             settings.getProperties().reload();
 
             gatherer.setProperties(settings.getProperties());
             gatherer.setEnvironment(settings.getEnvironment());
 
             for (TcmsReport.TestRunResults r : report.getTestRuns_withAppliedVariableTransformations()) {
                 gatherer.gather(r.results, build, r.build, r.variables);
             }
 
         } catch (TcmsException ex) {
             Logger.getLogger(TcmsReviewAction.class.getName()).log(Level.SEVERE, null, ex);
             settings.setUpdateException(ex.getMessage());
        } catch (IOException ex) {
             Logger.getLogger(TcmsReviewAction.class.getName()).log(Level.SEVERE, null, ex);
             settings.setUpdateException( ex.toString());
         } finally {
             rsp.sendRedirect("../" + Definitions.__URL_NAME);
             return;
         }
 
     }
 
     // FIXME: javadoc
     public void doUpdateSettings(StaplerRequest req, StaplerResponse rsp) throws IOException {
        settings.doUpdateSettings(req, rsp, report);
     }    
     
     public void doCheckSubmit(StaplerRequest req, StaplerResponse rsp) throws IOException  {
        environmentCheck.doCheckSubmit(req, rsp, report, settings);
     }
 
     // refactor
     public void doReportSubmit(StaplerRequest req, StaplerResponse rsp) throws ServletException,
             IOException, InterruptedException, TcmsException {
 
         if (req.getParameter("Submit").equals("update")) {
             // update build name
             Build.create buildCreate = (Build.create) gatherer.getCommandList("Build.create").getFirst().current;
             buildCreate.name = req.getParameter("buildName");
 
             // update testRun summary
             TestRun.create testRunCreate = (TestRun.create) gatherer.getCommandList("TestRun.create").getFirst().current;
             testRunCreate.summary = req.getParameter("testRunSummary");
 
             rsp.sendRedirect("../" + Definitions.__URL_NAME);
         } else {
             try {
                 // parse 
                 String input;
                 for (CommandWrapper c : gatherer) {
                     String a = new Integer(c.hashCode()).toString();
                     input = req.getParameter(a);
                     if (input != null) {
                         c.setExecutable(true);
                         c.setChecked(true);
                     } else {
                         c.setExecutable(false);
                         c.setChecked(false);
                     }
                 }                
                 
                 TcmsConnection connection = null;
                 connection = settings.getConnection();
                 upload(gatherer,connection);
 
             } catch (TcmsException ex) {
                 Logger.getLogger(TcmsPublisher.class.getName()).log(Level.SEVERE, null, ex);
             }
             rsp.sendRedirect("../" + Definitions.__URL_NAME);
         }
 
     }
 
     public static void upload(TcmsGatherer gathered, TcmsConnection connection) {
         boolean at_least_one;
         boolean at_least_one_not_duplicate;
         do {
             at_least_one = false;
             at_least_one_not_duplicate = false;
             for (CommandWrapper command : gathered) {
                 if (command.isExecutable()) {
                     if (command.resolved()) { //If dependecnies are satisfied
                         if (command.completed() == false) { // not to run command again
                             if (command.performed() == false) { // this command had satisfied dependecies but failed for some reason, so dont loop o it
                                 boolean tmp = command.perform(connection);
                                 if (tmp) {
                                     at_least_one = true;
                                 }
                                 if (command.duplicate() == false) {
                                     at_least_one_not_duplicate = true;
                                 }
                             }
                         }
                     } else { // dependencies we not met
                         command.setUnmetDependencies();
                     }
                 }
 
             }
         } while (at_least_one && at_least_one_not_duplicate);
     }
     
 }
