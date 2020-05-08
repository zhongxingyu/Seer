 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.awe.report.model;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.amanzi.awe.report.ReportPlugin;
 import org.amanzi.awe.report.model.events.IReportModelListener;
 import org.amanzi.awe.report.util.ReportUtils;
 import org.amanzi.neo.core.NeoCorePlugin;
 import org.amanzi.neo.core.database.nodes.ReportNode;
 import org.amanzi.neo.core.database.nodes.RubyProjectNode;
 import org.amanzi.neo.core.database.services.AweProjectService;
 import org.amanzi.scripting.jruby.EclipseLoadService;
 import org.amanzi.scripting.jruby.ScriptUtils;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.ui.PlatformUI;
 import org.jruby.Ruby;
 import org.jruby.RubyInstanceConfig;
 import org.jruby.internal.runtime.ValueAccessor;
 import org.jruby.javasupport.JavaEmbedUtils;
 import org.jruby.runtime.builtin.IRubyObject;
 import org.jruby.runtime.load.LoadService;
 
 /**
  * TODO Purpose of
  * <p>
  * </p>
  * 
  * @author Pechko E.
  * @since 1.0.0
  */
 public class ReportModel {
     /**
      * Path to report script
      */
     private static final String REPORT_SCRIPT = "ruby/report.rb";
     private static final String REPORT_MODEL_RUBY_NAME = "report_model";
     /*
      * Ruby Runtime
      */
     private Ruby runtime;
     private String scriptInput;
     private Report report;
     private List<IReportModelListener> listeners = new ArrayList<IReportModelListener>(0);
 
     public ReportModel(String rubyProjectName) {
         try {
             initializeJRubyInterpreter();
         } catch (IOException e) {
             // TODO Handle IOException
             throw (RuntimeException)new RuntimeException().initCause(e);
         }
     }
 
     public void initializeJRubyInterpreter() throws IOException {
         RubyInstanceConfig config = null;
         config = new RubyInstanceConfig() {
             {
                 setJRubyHome(ScriptUtils.getJRubyHome()); // this helps online
                 // help work
                 setObjectSpaceEnabled(true); // useful for code completion
                 // inside the IRB
                 setLoadServiceCreator(new LoadServiceCreator() {
                     public LoadService create(Ruby runtime) {
                         return new EclipseLoadService(runtime);
                     }
                 });
                 setLoader(ReportPlugin.getDefault().getClass().getClassLoader());
             }
         };
 
         runtime = Ruby.newInstance(config);
         runtime.getLoadService().init(ScriptUtils.makeLoadPath(new String[] {}));
 
         String path = "";
         URL scriptURL = null;
         try {
             scriptURL = FileLocator.toFileURL(ReportPlugin.getDefault().getBundle().getEntry(REPORT_SCRIPT));
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         path = scriptURL.getPath();
 
         scriptInput = ReportUtils.readScript(path);
 
         HashMap<String, Object> globals = new HashMap<String, Object>();
         globals.put(REPORT_MODEL_RUBY_NAME, this);
         makeRubyGlobals(runtime, globals);
 
         Job initJob= new Job("Initializing Ruby runtime"){
 
             @Override
             protected IStatus run(IProgressMonitor monitor) {
                 System.out.println("[DEBUG]Initializing Ruby runtime...");
                 runtime.evalScriptlet(scriptInput);
                 System.out.println("[DEBUG]finished");
                 return Status.OK_STATUS;
             }
             
         };
         initJob.schedule();
        try {
            initJob.join();
        } catch (InterruptedException e) {
            // TODO Handle InterruptedException
            e.printStackTrace();
        }
     }
 
     /**
      * Utility method that creates a Ruby Global Variables from Java Objects
      * 
      * @param rubyRuntime Ruby Environment
      * @param globals Map with Names of Variables and Java Objects
      */
 
     private void makeRubyGlobals(Ruby rubyRuntime, HashMap<String, Object> globals) {
         for (String name : globals.keySet()) {
             IRubyObject rubyObject = JavaEmbedUtils.javaToRuby(rubyRuntime, globals.get(name));
             rubyRuntime.getGlobalVariables().define("$" + name, new ValueAccessor(rubyObject));
         }
     }
 
     public void updateModel(String script) {
         this.scriptInput = script;
         Job updateModelJob=new Job("Updating report..."){
 
             @Override
             protected IStatus run(IProgressMonitor monitor) {
                 final IRubyObject res = runtime.evalScriptlet(scriptInput);
                 return Status.OK_STATUS;
             }
             
         };
         updateModelJob.schedule();
         try {
             updateModelJob.join();
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Method intended to be called from Ruby
      * 
      * @param part part created by ruby model builder
      */
 
     public void createPart(final IReportPart part) {
         PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
 
             @Override
             public void run() {
                 if (report != null) {
                     report.addPart(part);
                     report.firePartAdded(part, scriptInput);
                 }
             }
         });
     }
 
     /**
      * @return Returns the report.
      */
     public Report getReport() {
         return report;
     }
 
     /**
      * @param report The report to set.
      */
     public void setReport(Report report) {
         this.report = report;
     }
 
     /**
      * @param listener
      */
     public void addReportListener(IReportModelListener listener) {
         listeners.add(listener);
     }
 
     /**
      * @param listener
      */
 
     public void removeReportListener(IReportModelListener listener) {
         listeners.remove(listener);
     }
 
    
 
     public void updateReport(Report newReport) {
         if (report != null) {
             report.removeAllReportListeners();
         }
         this.report = newReport;
         for (IReportModelListener l : listeners) {
             this.report.addReportListener(l);
         }
     }
    
 }
