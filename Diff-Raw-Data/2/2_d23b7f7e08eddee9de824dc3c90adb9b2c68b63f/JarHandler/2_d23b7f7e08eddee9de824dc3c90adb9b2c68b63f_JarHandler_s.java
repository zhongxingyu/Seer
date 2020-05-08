 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.abf.librarysupport;
 
 import org.apache.log4j.Logger;
 import org.apache.tools.ant.module.api.support.ActionUtils;
 
 import org.openide.execution.ExecutorTask;
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.FileUtil;
 import org.openide.modules.InstalledFileLocator;
 import org.openide.xml.XMLUtil;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 
 import de.cismet.cids.abf.librarysupport.project.util.DeployInformation;
 
 /**
  * DOCUMENT ME!
  *
  * @author   mscholl
  * @version  1.5
  */
 public final class JarHandler {
 
     //~ Static fields/initializers ---------------------------------------------
 
     public static final String ANT_TARGET_UNJAR = "extractJar";                      // NOI18N
     public static final String ANT_TARGET_DELETE_DIR = "deleteDir";                  // NOI18N
     public static final String ANT_TARGET_DELETE_FILE = "deleteFile";                // NOI18N
     public static final String ANT_TARGET_DEPLOY_JAR = "deployJar";                  // NOI18N
     public static final String ANT_TARGET_DEPLOY_ALL_JARS = "deployAllJars";         // NOI18N
     public static final String ANT_TARGET_DEPLOY_CHANGED_JARS = "deployChangedJars"; // NOI18N
     public static final String ANT_TARGET_MOVE_JAR = "moveJar";                      // NOI18N
     public static final String ANT_TARGET_SIGN_SERVICE_JAR = "signServiceJar";       // NOI18N
     public static final String ANT_TARGET_CREATE_JAR = "createJar";                  // NOI18N
     public static final String ANT_TARGET_SIGN_JAR = "signJar";                      // NOI18N
 
     private static final String TMP_DIR_ADDITION = "_abf_tmp_";     // NOI18N
     private static final String TMP_BUILD_FILE = "__tmp_build.xml"; // NOI18N
 
     private static final transient Logger LOG = Logger.getLogger(JarHandler.class);
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new JarHandler object.
      */
     private JarHandler() {
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @param   buildXML      DOCUMENT ME!
      * @param   jarToExtract  DOCUMENT ME!
      * @param   target        DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  IOException  DOCUMENT ME!
      */
     public static FileObject extractJar(final FileObject buildXML,
             final FileObject jarToExtract,
             final FileObject target) throws IOException {
         final FileObject extractDir;
         if ((target != null) && target.isValid() && target.isFolder()) {
             extractDir = target;
         } else {
             extractDir = jarToExtract.getParent()
                         .createFolder(jarToExtract.getName() + TMP_DIR_ADDITION + System.currentTimeMillis());
         }
         final Properties p = new Properties();
         if (jarToExtract != null) {
             p.put("jar.sourcefile", FileUtil.toFile(jarToExtract) // NOI18N
                 .getAbsolutePath());
         }
         p.put("jar.tmpdir",                   // NOI18N
             FileUtil.toFile(extractDir).getAbsolutePath());
         final ExecutorTask task = ActionUtils.runTarget(
                 buildXML,
                 new String[] { ANT_TARGET_UNJAR },
                 p);
         task.waitFinished();
         if (task.result() != 0) {
             LOG.error("could not extract jar '" // NOI18N
                         + jarToExtract.getName()
                         + "' to temporary directory '" // NOI18N
                         + extractDir.getPath()
                         + "', cleaning up");  // NOI18N
             if ((target == null) || !target.isValid() || !target.isFolder()) {
                 removeDir(buildXML, extractDir);
             }
             throw new IOException("could not extract jar '" // NOI18N
                         + jarToExtract.getName()
                         + "' to temporary directory '" // NOI18N
                         + extractDir.getPath()
                         + "', cleaning up");  // NOI18N
         }
         return extractDir;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   buildXML     DOCUMENT ME!
      * @param   dirToRemove  DOCUMENT ME!
      *
      * @throws  IOException  DOCUMENT ME!
      */
     public static void removeDir(final FileObject buildXML, final FileObject dirToRemove) throws IOException {
         final Properties p = new Properties();
         p.put("jar.tmpdir", FileUtil.toFile(dirToRemove).getAbsolutePath());
         final ExecutorTask task = ActionUtils.runTarget(buildXML, new String[] { ANT_TARGET_DELETE_DIR }, p);
         task.waitFinished();
         if (task.result() != 0) {
             final String message = "removal of dir '" + dirToRemove.getPath() + "' failed"; // NOI18N
             LOG.error(message);
             throw new IOException(message);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   buildXML  DOCUMENT ME!
      * @param   workDir   DOCUMENT ME!
      *
      * @throws  IOException  DOCUMENT ME!
      */
     public static void cleanup(final FileObject buildXML, final FileObject workDir) throws IOException {
         for (final FileObject f : workDir.getChildren()) {
             if (f.getName().contains(TMP_DIR_ADDITION)) {
                 removeDir(buildXML, f);
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   info  DOCUMENT ME!
      *
      * @throws  IOException  DOCUMENT ME!
      */
     public static void deployJar(final DeployInformation info) throws IOException {
         deployAllJars(Arrays.asList(new DeployInformation[] { info }), ANT_TARGET_DEPLOY_JAR);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   buildDoc  DOCUMENT ME!
      * @param   paths     DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private static String createDeleteFileTask(final Document buildDoc, final String[] paths) {
         final Element target = buildDoc.createElement("target");     // NOI18N
         target.setAttribute("name", ANT_TARGET_DELETE_FILE);         // NOI18N
         buildDoc.getDocumentElement().appendChild(target);
         for (final String path : paths) {
             final Element delete = buildDoc.createElement("delete"); // NOI18N
             delete.setAttribute("file", path);                       // NOI18N
             delete.setAttribute("failonerror", "false");             // NOI18N
             target.appendChild(delete);
         }
 
         return ANT_TARGET_DELETE_FILE;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   buildDoc  DOCUMENT ME!
      * @param   infos     DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private static String createMoveTask(final Document buildDoc, final DeployInformation[] infos) {
         final Element target = buildDoc.createElement("target");                   // NOI18N
         target.setAttribute("name", ANT_TARGET_MOVE_JAR);                          // NOI18N
         buildDoc.getDocumentElement().appendChild(target);
         for (final DeployInformation info : infos) {
             final Element move = buildDoc.createElement("move");                   // NOI18N
             move.setAttribute("file", info.getDestFilePath());                     // NOI18N
             move.setAttribute("tofile", info.getDestFilePath() + "_sendToCismet"); // NOI18N
             target.appendChild(move);
         }
 
         return ANT_TARGET_MOVE_JAR;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   buildDoc  DOCUMENT ME!
      * @param   infos     DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private static String createSignServiceTask(final Document buildDoc, final DeployInformation[] infos) {
         // NOTE: this approach is not very nice as the dependecies have to be watched and the code adapted accordingly
         final InstalledFileLocator locator = InstalledFileLocator.getDefault();
         final Set<File> clerksterCP = new HashSet<File>(6);
         clerksterCP.add(locator.locate(
                 "modules/ext/commons-codec/commons-codec-1.6.jar",
                 "de.cismet.cids.abf.librarysupport/3",
                 false));
         clerksterCP.add(locator.locate(
                 "modules/ext/commons-logging/commons-logging-1.0.4.jar",
                 "de.cismet.cids.abf.librarysupport/3",
                 false));
         clerksterCP.add(locator.locate(
                 "modules/ext/commons-io/commons-io-2.2.jar",
                 "de.cismet.cids.abf.librarysupport/3",
                 false));
         clerksterCP.add(locator.locate(
                 "modules/ext/commons-httpclient/commons-httpclient-3.1.jar",
                 "de.cismet.cids.abf.librarysupport/3",
                 false));
         clerksterCP.add(locator.locate(
                 "modules/ext/log4j/log4j-1.2.17.jar",
                 "de.cismet.cids.abf.librarysupport/3",
                 false));
         clerksterCP.add(locator.locate(
                "modules/ext/de.cismet.clerkster/clerkster-client-1.0-SNAPSHOT.jar",
                 "de.cismet.cids.abf.librarysupport/3",
                 false));
         clerksterCP.add(locator.locate(
                 "modules/de-cismet-cids-abf-librarysupport.jar",
                 "de.cismet.cids.abf.librarysupport/3",
                 false));
         assert clerksterCP.size() == 7;
 
         final Element taskdef = buildDoc.createElement("taskdef");                                  // NOI18N
         taskdef.setAttribute("name", "clerksterClient");                                            // NOI18N
         taskdef.setAttribute("classname", "de.cismet.cids.abf.librarysupport.ClerksterClientTask"); // NOI18N
         buildDoc.getDocumentElement().appendChild(taskdef);
         final Element cp = buildDoc.createElement("classpath");                                     // NOI18N
         for (final File file : clerksterCP) {
             final Element pe = buildDoc.createElement("pathelement");                               // NOI18N
             pe.setAttribute("location", file.getAbsolutePath());                                    // NOI18N
             cp.appendChild(pe);
         }
         taskdef.appendChild(cp);
 
         final Element target = buildDoc.createElement("target");                              // NOI18N
         target.setAttribute("name", ANT_TARGET_SIGN_SERVICE_JAR);                             // NOI18N
         buildDoc.getDocumentElement().appendChild(target);
         for (final DeployInformation info : infos) {
             final Element clerksterClient = buildDoc.createElement("clerksterClient");        // NOI18N
             clerksterClient.setAttribute("url", info.getSignServiceUrl());                    // NOI18N
             clerksterClient.setAttribute("username", info.getSignServiceUser());
             clerksterClient.setAttribute("password", "${jar.sign.upload.password}");          // NOI18N
             clerksterClient.setAttribute("infile", info.getDestFilePath() + "_sendToCismet"); // NOI18N
             clerksterClient.setAttribute("outfile", info.getDestFilePath());                  // NOI18N
             clerksterClient.setAttribute("failonerror", "true");                              // NOI18N
             clerksterClient.setAttribute("loglevel", info.getSignServiceLoglevel());          // NOI18N
             target.appendChild(clerksterClient);
         }
 
         return ANT_TARGET_SIGN_SERVICE_JAR;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   buildDoc  DOCUMENT ME!
      * @param   infos     DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private static String createJarTask(final Document buildDoc, final DeployInformation[] infos) {
         final Element target = buildDoc.createElement("target");                                 // NOI18N
         target.setAttribute("name", ANT_TARGET_CREATE_JAR);                                      // NOI18N
         buildDoc.getDocumentElement().appendChild(target);
         for (final DeployInformation info : infos) {
             final Element jar = buildDoc.createElement("jar");                                   // NOI18N
             jar.setAttribute("destfile", info.getDestFilePath());                                // NOI18N
             jar.setAttribute("basedir", FileUtil.toFile(info.getSourceDir()).getAbsolutePath()); // NOI18N
             jar.setAttribute("manifest", FileUtil.toFile(info.getManifest()).getAbsolutePath()); // NOI18N
 
             target.appendChild(jar);
         }
 
         return ANT_TARGET_CREATE_JAR;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   buildDoc  DOCUMENT ME!
      * @param   infos     DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private static String createSignJarTask(final Document buildDoc, final DeployInformation[] infos) {
         final Element target = buildDoc.createElement("target");                                  // NOI18N
         target.setAttribute("name", ANT_TARGET_SIGN_JAR);                                         // NOI18N
         buildDoc.getDocumentElement().appendChild(target);
         for (final DeployInformation info : infos) {
             final Element sign = buildDoc.createElement("signjar");                               // NOI18N
             sign.setAttribute("jar", info.getDestFilePath());                                     // NOI18N
             sign.setAttribute("alias", info.getAlias());                                          // NOI18N
             sign.setAttribute("storepass", "${jar.sign.storepass}");                              // NOI18N
             sign.setAttribute("keystore", FileUtil.toFile(info.getKeystore()).getAbsolutePath()); // NOI18N
 
             target.appendChild(sign);
         }
 
         return ANT_TARGET_SIGN_JAR;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   buildDoc  DOCUMENT ME!
      * @param   info      DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  IOException  DOCUMENT ME!
      */
     private static FileObject writeDoc(final Document buildDoc, final DeployInformation info) throws IOException {
         final File outFile = new File(FileUtil.toFile(info.getBuildXML().getParent()), TMP_BUILD_FILE);
         if (outFile.exists() && !outFile.delete()) {
             LOG.error("outfile could not be deleted"); // NOI18N
             throw new IOException("outfile '"          // NOI18N
                         + outFile.getAbsolutePath()
                         + "' could not be deleted");   // NOI18N
         }
         final BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outFile));
         try {
             XMLUtil.write(buildDoc, bos, "UTF-8");     // NOI18N
 
             return FileUtil.toFileObject(outFile);
         } finally {
             bos.close();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   infos       DOCUMENT ME!
      * @param   targetName  DOCUMENT ME!
      *
      * @throws  IOException  DOCUMENT ME!
      */
     public static void deployAllJars(final List<DeployInformation> infos, final String targetName) throws IOException {
         if (infos.size() < 1) {
             return;
         }
         final Document buildDoc = XMLUtil.createDocument(
                 "project", // NOI18N
                 null,
                 null,
                 null);
         final Element target = buildDoc.createElement("target"); // NOI18N
         target.setAttribute("name", targetName); // NOI18N
         final StringBuilder deps = new StringBuilder();
 
         buildDoc.getDocumentElement().appendChild(target);
 
         final DeployInformation[] dis = infos.toArray(new DeployInformation[infos.size()]);
         deps.append(createJarTask(buildDoc, dis));
         deps.append(", ").append(createSignJarTask(buildDoc, dis)); // NOI18N
 
         if (dis[0].isUseSignService()) {
             deps.append(", ").append(createMoveTask(buildDoc, dis));        // NOI18N
             deps.append(", ").append(createSignServiceTask(buildDoc, dis)); // NOI18N
             final String[] paths = new String[dis.length];
             for (int i = 0; i < dis.length; ++i) {
                 paths[i] = dis[i].getDestFilePath() + "_sendToCismet";      // NOI18N
             }
             deps.append(", ").append(createDeleteFileTask(buildDoc, paths));
         }
 
         target.setAttribute("depends", deps.toString()); // NOI18N
 
         final FileObject outFile = writeDoc(buildDoc, dis[0]);
 
         final Properties p = new Properties();
         if (dis[0].getStorepass() == null) {
             p.put("jar.sign.storepass", "");                                                // NOI18N
         } else {
             p.put("jar.sign.storepass", String.valueOf(dis[0].getStorepass()));             // NOI18N
         }
         if (dis[0].getSignServicePass() == null) {
             p.put("jar.sign.upload.password", "");                                          // NOI18N
         } else {
             p.put("jar.sign.upload.password", String.valueOf(dis[0].getSignServicePass())); // NOI18N
         }
 
         final ExecutorTask task = ActionUtils.runTarget(outFile, new String[] { targetName }, p);
         task.waitFinished();
         if (task.result() != 0) {
             LOG.error("deploy jar failed: " + dis[0].getDestFilePath());             // NOI18N
             throw new IOException("deploy jar failed: " + dis[0].getDestFilePath()); // NOI18N
         }
     }
 }
