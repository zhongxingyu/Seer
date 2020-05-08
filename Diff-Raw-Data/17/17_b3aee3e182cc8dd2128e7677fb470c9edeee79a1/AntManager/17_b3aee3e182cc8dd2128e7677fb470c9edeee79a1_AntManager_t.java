 package org.makumba.parade.model.managers;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import org.apache.log4j.Logger;
 import org.apache.tools.ant.BuildEvent;
 import org.apache.tools.ant.DefaultLogger;
 import org.apache.tools.ant.Main;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.ProjectHelper;
 import org.apache.tools.ant.Target;
 import org.makumba.parade.model.Row;
 import org.makumba.parade.model.RowAnt;
 import org.makumba.parade.model.interfaces.ParadeManager;
 import org.makumba.parade.model.interfaces.RowRefresher;
 import org.makumba.parade.tools.Execute;
 import org.makumba.parade.tools.HtmlUtils;
 
 public class AntManager implements RowRefresher, ParadeManager {
 
     static Logger logger = Logger.getLogger(AntManager.class.getName());
 
     public void hardRefresh(Row row) {
         logger.debug("Refreshing row information for row " + row.getRowname());
 
         RowAnt antdata = new RowAnt();
         antdata.setDataType("ant");
 
         File buildFile = setBuildFile(row, antdata);
         if (buildFile == null || !buildFile.exists()) {
            logger.warn("AntManager: no build file found for row " + row.getRowname() + " at path " + row.getRowpath());
         } else {
             Project p = getInitProject(buildFile, row, antdata);
            if (p != null) {
                 setTargets(antdata, p);
             }
         }
 
         row.addManagerData(antdata);
 
     }
 
     private java.io.File setBuildFile(Row row, RowAnt data) {
         File dir = new File(row.getRowpath());
 
         String buildFilePath = data.getBuildfile();
         File buildFile = null;
 
         if (buildFilePath == "") {
             buildFile = setBuildFilePath(row, data, dir);
         }
 
         return buildFile;
 
     }
 
     private java.io.File setBuildFilePath(Row row, RowAnt data, File dir) {
         File buildFile;
         buildFile = new java.io.File(dir + File.separator + "build.xml");
         if (!buildFile.exists()) {
             data.setBuildfile("No build file found");
         } else {
             try {
                 data.setBuildfile(buildFile.getCanonicalPath());
             } catch (IOException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }
         return buildFile;
     }
 
     private synchronized Project getInitProject(File buildFile, Row row, RowAnt data) {
         Project project = null;
 
         if (data.getLastmodified() == null || buildFile.lastModified() > data.getLastmodified().longValue()) {
             data.setLastmodified(new Long(buildFile.lastModified()));
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
             java.util.logging.Logger.getLogger("org.makumba.parade.ant").log(java.util.logging.Level.WARNING,
                     "project config error", t);
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
 
     private void setTargets(RowAnt antdata, Project p) {
         Project project = p;
         Enumeration ptargets = project.getTargets().elements();
 
         List<String> targets = antdata.getTargets();
         while (ptargets.hasMoreElements()) {
             Target currentTarget = (Target) ptargets.nextElement();
             if (currentTarget.getDescription() != null && currentTarget.getDescription() != "") {
                 targets.add("#" + currentTarget.getName());
             } else {
                 targets.add(currentTarget.getName());
             }
         }
        Collections.sort(antdata.getTargets());
     }
 
     public void newRow(String name, Row r, Map<String, String> m) {
         // TODO Auto-generated method stub
 
     }
 
     public String executeAntCommand(Row r, String command) {
         java.lang.Runtime rt = java.lang.Runtime.getRuntime();
         rt.gc();
         long memSize = rt.totalMemory() - rt.freeMemory();
 
         RowAnt antData = (RowAnt) r.getRowdata().get("ant");
         String buildFilePath = antData.getBuildfile();
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
 
         logger.debug("Attempting to execute ANT command " + command + " with a java heap of " + memSize);
 
         Execute.exec(v, buildFile.getParentFile(), out);
 
         rt.gc();
         long memSize1 = rt.totalMemory() - rt.freeMemory();
         logger.debug("Finished to execute ANT command " + command + " with a java heap of " + memSize1);
 
         out.println("heap size: " + memSize1);
         out.println("heap grew with: " + (memSize1 - memSize));
         out.flush();
 
         return HtmlUtils.text2html(result.toString(), "", "<br>");
 
     }
 
     public String executeProjectAntCommand(Row r, String command) throws IOException {
         java.lang.Runtime rt = java.lang.Runtime.getRuntime();
         rt.gc();
         long memSize = rt.totalMemory() - rt.freeMemory();
 
         RowAnt antData = (RowAnt) r.getRowdata().get("ant");
         String buildFilePath = antData.getBuildfile();
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
 
         logger.debug("Attempting to execute ANT command " + command + " with a java heap of " + memSize);
 
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
         logger.debug("Finished to execute ANT command " + command + " with a java heap of " + memSize1);
 
         out.println("heap size: " + memSize1);
         out.println("heap grew with: " + (memSize1 - memSize));
 
         return HtmlUtils.text2html(result.toString(), "", "<br>");
     }
 
     public void softRefresh(Row row) {
        hardRefresh(row);
     }
 }
