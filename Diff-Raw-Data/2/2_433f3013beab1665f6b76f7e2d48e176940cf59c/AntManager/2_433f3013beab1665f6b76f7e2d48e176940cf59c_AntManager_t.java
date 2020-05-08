 package org.makumba.parade.model.managers;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 import java.util.logging.Logger;
 
 import org.apache.tools.ant.BuildEvent;
 import org.apache.tools.ant.DefaultLogger;
 import org.apache.tools.ant.Main;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.ProjectHelper;
 import org.apache.tools.ant.Target;
 import org.makumba.parade.model.AntTarget;
 import org.makumba.parade.model.Row;
 import org.makumba.parade.model.interfaces.ParadeManager;
 import org.makumba.parade.model.interfaces.RowRefresher;
 import org.makumba.parade.tools.Execute;
 import org.makumba.parade.tools.HtmlUtils;
 import org.makumba.parade.tools.ParadeLogger;
 
 public class AntManager implements RowRefresher, ParadeManager {
 
     static Logger logger = ParadeLogger.getParadeLogger(AntManager.class.getName());
 
     public void hardRefresh(Row row) {
         logger.fine("Refreshing row information for row " + row.getRowname());
 
         File buildFile = setBuildFile(row);
         if (buildFile == null || !buildFile.exists()) {
             logger.warning("AntManager: no build file found for row " + row.getRowname() + " at path "
                     + row.getRowpath());
         } else {
             Project p = getInitProject(buildFile, row);
             if (p != null) {
                 setTargets(row, p);
             }
         }
 
     }
 
     private java.io.File setBuildFile(Row row) {
         File dir = new File(row.getRowpath());
 
         String buildFilePath = row.getBuildfile();
         File buildFile = null;
 
        if (buildFilePath == "" || buildFilePath == null || !(new File(buildFilePath).exists())) {
             buildFile = setBuildFilePath(row, dir);
         } else {
             buildFile = new java.io.File(buildFilePath);
         }
 
         return buildFile;
 
     }
 
     private java.io.File setBuildFilePath(Row row, File dir) {
         File buildFile;
         buildFile = new java.io.File(dir.getPath() + File.separator + "build.xml");
         if (!buildFile.exists()) {
             row.setBuildfile("No build file found");
         } else {
             try {
                 row.setBuildfile(buildFile.getCanonicalPath());
             } catch (IOException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }
         return buildFile;
     }
 
     private synchronized Project getInitProject(File buildFile, Row row) {
         Project project = null;
 
         if (row.getLastmodified() == null || buildFile.lastModified() > row.getLastmodified().longValue()) {
             row.setLastmodified(new Long(buildFile.lastModified()));
             project = getProject(buildFile, row);
 
         }
         return project;
     }
 
     private synchronized Project getProject(File buildFile, Row row) {
         Project project = new Project();
 
         try {
             project.init();
             ProjectHelper.getProjectHelper().parse(project, buildFile);
         } catch (Throwable t) {
             ParadeLogger.getParadeLogger("org.makumba.parade.ant").log(
                     java.util.logging.Level.WARNING, "project config error", t);
             return null;
         }
 
         try {
             project.setUserProperty("ant.file", buildFile.getCanonicalPath());
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         return project;
     }
     
     private class TargetComparator implements Comparator<AntTarget> {
 
         public int compare(AntTarget arg0, AntTarget arg1) {
             return arg0.getTarget().compareTo(arg1.getTarget());
         }
         
     }
 
     private void setTargets(Row row, Project p) {
         Project project = p;
         Enumeration ptargets = project.getTargets().elements();
 
         List<AntTarget> targets = row.getTargets();
         while (ptargets.hasMoreElements()) {
             Target currentTarget = (Target) ptargets.nextElement();
             if (currentTarget.getDescription() != null && currentTarget.getDescription() != "") {
                 targets.add(new AntTarget("#" + currentTarget.getName()));
             } else {
                 targets.add(new AntTarget(currentTarget.getName()));
             }
         }
         Collections.sort(row.getTargets(), new TargetComparator());
     }
 
     public void newRow(String name, Row r, Map<String, String> m) {
         // TODO Auto-generated method stub
 
     }
 
     public String executeAntCommand(Row r, String command) {
         java.lang.Runtime rt = java.lang.Runtime.getRuntime();
         rt.gc();
         long memSize = rt.totalMemory() - rt.freeMemory();
 
         String buildFilePath = r.getBuildfile();
         if (buildFilePath.equals("No build file found"))
             return ("No build file found");
         java.io.File buildFile = new java.io.File(buildFilePath);
 
         OutputStream result = new ByteArrayOutputStream();
         PrintWriter out = new PrintWriter(result);
 
         out.println("heap size: " + memSize);
         out.println("Buildfile: " + buildFile.getName());
         Vector<String> v = new Vector<String>();
         v.addElement("ant");
         v.addElement(command);
 
         logger.fine("Attempting to execute ANT command " + command + " with a java heap of " + memSize);
 
         Execute.exec(v, buildFile.getParentFile(), out);
 
         rt.gc();
         long memSize1 = rt.totalMemory() - rt.freeMemory();
         logger.fine("Finished to execute ANT command " + command + " with a java heap of " + memSize1);
 
         out.println("heap size: " + memSize1);
         out.println("heap grew with: " + (memSize1 - memSize));
         out.flush();
 
         return HtmlUtils.text2html(result.toString(), "", "<br>");
 
     }
 
     public String executeProjectAntCommand(Row r, String command) throws IOException {
         java.lang.Runtime rt = java.lang.Runtime.getRuntime();
         rt.gc();
         long memSize = rt.totalMemory() - rt.freeMemory();
 
         String buildFilePath = r.getBuildfile();
         if (buildFilePath.equals("No build file found"))
             return ("No build file found");
         java.io.File buildFile = new java.io.File(buildFilePath);
 
         Project project = getProject(buildFile, r);
         DefaultLogger lg = new DefaultLogger();
         lg.setEmacsMode(true);
         lg.setMessageOutputLevel(Project.MSG_INFO);
         project.addBuildListener(lg);
 
         OutputStream result = new ByteArrayOutputStream();
         PrintStream out = new PrintStream(result);
 
         out.println("heap size: " + memSize);
         out.println(Main.getAntVersion());
         out.println("Buildfile: " + buildFile.getName());
         Vector<String> v = new Vector<String>();
         v.addElement(command);
 
         logger.fine("Attempting to execute ANT command " + command + " with a java heap of " + memSize);
 
         lg.setOutputPrintStream(out);
         lg.setErrorPrintStream(out);
 
         lg.buildStarted(null);
         Throwable error = null;
         try {
 
             project.executeTargets(v);
         } catch (Throwable t) {
             error = t;
         }
         BuildEvent be = new BuildEvent(project);
         be.setException(error);
         lg.buildFinished(be);
 
         out.flush();
 
         rt.gc();
         long memSize1 = rt.totalMemory() - rt.freeMemory();
         logger.fine("Finished to execute ANT command " + command + " with a java heap of " + memSize1);
 
         out.println("heap size: " + memSize1);
         out.println("heap grew with: " + (memSize1 - memSize));
 
         return HtmlUtils.text2html(result.toString(), "", "<br>");
     }
 
     public void softRefresh(Row row) {
         hardRefresh(row);
     }
 }
