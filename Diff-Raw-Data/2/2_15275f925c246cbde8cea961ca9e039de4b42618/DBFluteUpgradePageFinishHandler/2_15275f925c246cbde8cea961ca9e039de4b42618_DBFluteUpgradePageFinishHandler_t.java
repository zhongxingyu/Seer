 package org.seasar.dbflute.emecha.eclipse.plugin.wizards.version;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Properties;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.seasar.dbflute.emecha.eclipse.plugin.core.exception.EmExceptionHandler;
 import org.seasar.dbflute.emecha.eclipse.plugin.core.prototype.EmPrototypeEntry;
 import org.seasar.dbflute.emecha.eclipse.plugin.core.util.io.EmFileUtil;
 import org.seasar.dbflute.emecha.eclipse.plugin.core.util.net.EmURLUtil;
 import org.seasar.dbflute.emecha.eclipse.plugin.core.wrapper.resource.EmContainer;
 import org.seasar.dbflute.emecha.eclipse.plugin.core.wrapper.resource.EmFile;
 import org.seasar.dbflute.emecha.eclipse.plugin.core.wrapper.resource.EmWorkspaceRoot;
 import org.seasar.dbflute.emecha.eclipse.plugin.core.wrapper.runtime.EmProgressMonitor;
 
 /**
  * @author jflute
  * @since 0.2.3 (2009/01/31 Saturday)
  */
 public class DBFluteUpgradePageFinishHandler {
 
     // ===================================================================================
     //                                                                           Attribute
     //                                                                           =========
     protected DBFluteUpgradePageResult versionUpPageResult;
     protected IResource topLevelResource;
 
     // ===================================================================================
     //                                                                         Constructor
     //                                                                         ===========
     public DBFluteUpgradePageFinishHandler(DBFluteUpgradePageResult newWizardPageResult, IResource topLevelResource) {
         this.versionUpPageResult = newWizardPageResult;
         this.topLevelResource = topLevelResource;
     }
 
     // ===================================================================================
     //                                                                     Finish Handling
     //                                                                     ===============
     public void handleFinish(IProgressMonitor monitor) throws CoreException {
         // Top Level Definition
         final EmProgressMonitor progressMonitor = new EmProgressMonitor(monitor);
         final EmWorkspaceRoot workspaceRoot = EmWorkspaceRoot.create();
         final EmContainer container = workspaceRoot.findContainer(versionUpPageResult.getOutputDirectory());
 
         // Create directory of dbflute_[project].
         File locationPureFile = container.getLocationPureFile();
         File[] dbfluteDirs = locationPureFile.listFiles(new FileFilter() {
             public boolean accept(File file) {
                 return file.isDirectory() && file.getName().startsWith("dbflute_");
             }
         });
         if (dbfluteDirs == null) {
             String msg = "Not found DBFlute Client at the project: " + locationPureFile.getName();
             throw new IllegalStateException(msg);
         }
         List<File> buildPropertyList = new ArrayList<File>();
         for (File dbfluteDir : dbfluteDirs) {
             File[] listFiles = dbfluteDir.listFiles(new FileFilter() {
                 public boolean accept(File file) {
                     if (!file.isFile()) {
                         return false;
                     }
                     String name = file.getName();
                     return name.startsWith("build") && name.endsWith(".properties");
                 }
             });
             if (listFiles != null && listFiles.length > 0) {
                 buildPropertyList.add(listFiles[0]);
             }
         }
         if (buildPropertyList.isEmpty()) {
             String msg = "The build.properties was NOT found:";
             msg = msg + " dbfluteDirs=" + Arrays.asList(dbfluteDirs);
             throw new IllegalStateException(msg);
         }
 
         List<String> projectList = new ArrayList<String>();
         for (File buildProperty : buildPropertyList) {
             Properties properties = new Properties();
             try {
                 properties.load(new FileInputStream(buildProperty));
             } catch (FileNotFoundException e) {
                 throw new IllegalStateException(e);
             } catch (IOException e) {
                 throw new IllegalStateException(e);
             }
             String property = properties.getProperty("torque.project");
             if (property == null) {
                 String msg = "The property 'torque.project' was NOT found:";
                 msg = msg + " buildProperty=" + buildProperty;
                 msg = msg + " properties=" + properties;
                 throw new IllegalStateException(msg);
             }
             projectList.add(property.trim());
         }
         if (projectList.isEmpty()) {
            String msg = "The project name was NOT found:";
             msg = msg + " buildPropertyList=" + buildPropertyList;
             throw new IllegalStateException(msg);
         }
         final String projectBat = "_project.bat";
         final String projectSh = "_project.sh";
         for (String project : projectList) {
             versionUpPageResult.setProject(project);
 
             final EmPrototypeEntry prototypeEntry = EmPrototypeEntry.create();
             final List<URL> entryList = prototypeEntry.findDBFluteClientEntries();
             for (URL url : entryList) {
                 final String path = buildOutputPath(url.getPath());
 
                 if (!url.getFile().contains(projectBat) && !url.getFile().contains(projectSh)) {
                     continue;
                 }
 
                 // - - - - - - - - - - - - -
                 // Here is for Project File!
                 // - - - - - - - - - - - - -
 
                 boolean filterFileText = false;
                 if (isAvailableFilteringFileText(path)) {
                     filterFileText = true;
                 }
 
                 final InputStream openStream = EmURLUtil.openStream(url);
                 BufferedReader reader;
                 try {
                     reader = new BufferedReader(new InputStreamReader(openStream, "UTF-8"));
                 } catch (UnsupportedEncodingException e) {
                     String msg = "The encoding is unsupported: UTF-8";
                     EmExceptionHandler.throwAsPluginException(msg, e);
                     return;
                 }
                 final StringBuilder sb = new StringBuilder();
                 while (true) {
                     try {
                         String line = reader.readLine();
                         if (line == null) {
                             break;
                         }
                         if (filterFileText) {
                             line = filterFileText(line);
                         }
                         sb.append(line).append(getLineSeparator());
                     } catch (IOException e) {
                         String msg = "";
                         EmExceptionHandler.throwAsPluginException(msg, e);
                         return;
                     }
                 }
                 final EmFile outputFile = container.createFile(path);
                 try {
                     final byte[] bytes = sb.toString().getBytes("UTF-8");
                     outputFile.delete(true, progressMonitor);
                     outputFile.create(new ByteArrayInputStream(bytes), true, progressMonitor);
                 } catch (UnsupportedEncodingException e) {
                     String msg = "The encoding is unsupported: UTF-8";
                     EmExceptionHandler.throwAsPluginException(msg, e);
                     return;
                 }
             }
         }
 
         refreshResources(monitor);
         monitor.worked(1);
     }
 
     protected void refreshResources(IProgressMonitor monitor) {
         if (topLevelResource != null) {
             try {
                 topLevelResource.refreshLocal(IResource.DEPTH_INFINITE, monitor);
             } catch (CoreException ignored) {
                 String msg = "IResource.refreshLocal() threw the exception!";
                 EmExceptionHandler.show(msg, ignored);
             }
         }
     }
 
     protected boolean isAvailableFilteringFileText(final String path) {
         if (path.contains(buildBuildPropertiesName(versionUpPageResult))) {
             return true;
         }
         if (path.contains(".dfprop")) {
             return true;
         }
         if (path.contains("_project.bat") || path.contains("_project.sh")) {
             return true;
         }
         return false;
     }
 
     protected boolean isDirectory(String path) {
         return path.endsWith("/");
     }
 
     protected String extractDirectoryPath(String path) {
         if (path.endsWith("/")) {
             return path.substring(0, path.lastIndexOf("/"));
         }
         return path;
     }
 
     protected String getLineSeparator() {
         return "\n"; // Not from EmSystemUtil.getLineSeparator()! Because it is possible of creating at windows and use at linux.
     }
 
     // ===================================================================================
     //                                                                     Building Helper
     //                                                                     ===============
     protected String buildOutputPath(String path) {
         final String fileSeparator = EmPrototypeEntry.FILE_SEPARATOR;
         path = EmFileUtil.removeFirstFileSeparatorIfNeeds(path, fileSeparator);
 
         final String dbfluteClientDirectoryName = buildDBFluteClientDirectoryName(versionUpPageResult);
         final String dbfluteClientPath = EmPrototypeEntry.buildDBFluteClientPath();
         if (path.contains(dbfluteClientPath)) {
             path = replace(path, dbfluteClientPath, dbfluteClientDirectoryName);
         }
         path = EmFileUtil.removeFirstFileSeparatorIfNeeds(path, fileSeparator);
 
         final String projectNameMark = EmPrototypeEntry.PROJECT_NAME_MARK;
         if (path.contains(projectNameMark)) {
             path = replace(path, projectNameMark, versionUpPageResult.getProject());
         }
 
         return path;
     }
 
     protected String buildDBFluteClientDirectoryName(DBFluteUpgradePageResult result) {
         return "dbflute_" + result.getProject();
     }
 
     protected String buildBuildPropertiesName(DBFluteUpgradePageResult result) {
         return "build-" + result.getProject() + ".properties";
     }
 
     // ===================================================================================
     //                                                                    Filtering Helper
     //                                                                    ================
     protected String filterFileText(String line) {
         if (line.startsWith("#")) {// Exclude comment line.
             return line;
         }
         return versionUpPageResult.filterLineByResult(line, "${", "}");
     }
 
     // ===================================================================================
     //                                                                      General Helper
     //                                                                      ==============
     protected String replace(String text, String fromText, String toText) {
         if (text == null) {
             return null;
         }
         if (fromText == null || toText == null) {
             String msg = "The fromText and toText should not be null:";
             msg = msg + " fromText=" + fromText + " toText=" + toText;
             throw new IllegalArgumentException(msg);
         }
         StringBuffer buf = new StringBuffer(100);
         int pos = 0;
         int pos2 = 0;
         while (true) {
             pos = text.indexOf(fromText, pos2);
             if (pos == 0) {
                 buf.append(toText);
                 pos2 = fromText.length();
             } else if (pos > 0) {
                 buf.append(text.substring(pos2, pos));
                 buf.append(toText);
                 pos2 = pos + fromText.length();
             } else {
                 buf.append(text.substring(pos2));
                 break;
             }
         }
         return buf.toString();
     }
 
     protected void debug(String log) {
         if (true) {
             System.out.println(log);
         }
     }
 }
