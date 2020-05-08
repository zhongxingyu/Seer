 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.abf.client;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.log4j.Logger;
 
 import org.dom4j.Document;
 import org.dom4j.Element;
 import org.dom4j.Node;
 import org.dom4j.io.SAXReader;
 import org.dom4j.io.XMLWriter;
 
 import org.netbeans.api.project.FileOwnerQuery;
 import org.netbeans.api.project.Project;
 
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.FileUtil;
 import org.openide.loaders.DataObject;
import org.openide.util.RequestProcessor;
 import org.openide.windows.IOProvider;
 import org.openide.windows.InputOutput;
 import org.openide.windows.OutputWriter;
 
 import java.awt.EventQueue;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
import java.io.PrintStream;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.concurrent.Executor;
 
 import de.cismet.cids.abf.librarysupport.JarHandler;
 import de.cismet.cids.abf.librarysupport.project.customizer.PropertyProvider;
 import de.cismet.cids.abf.librarysupport.project.util.DeployInformation;
 import de.cismet.cids.abf.utilities.ProgressIndicatingExecutor;
 
 import de.cismet.tools.PasswordEncrypter;
import org.dom4j.io.OutputFormat;
 
 /**
  * DOCUMENT ME!
  *
  * @version  $Revision$, $Date$
  */
 public final class CreateSecurityJarAction implements ActionListener {
 
     //~ Static fields/initializers ---------------------------------------------
 
     /** LOGGER. */
     private static final transient Logger LOG = Logger.getLogger(CreateSecurityJarAction.class);
     private static final Executor EXECUTOR = new ProgressIndicatingExecutor(
             "Create security jar",
             "create-security-jar-dispatcher",
             1);
 
     //~ Instance fields --------------------------------------------------------
 
     private final List<DataObject> context;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new CreateSecurityJarAction object.
      *
      * @param  context  DOCUMENT ME!
      */
     public CreateSecurityJarAction(final List<DataObject> context) {
         this.context = context;
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public void actionPerformed(final ActionEvent ev) {
         EXECUTOR.execute(new Runnable() {
 
                 @Override
                 public void run() {
                     final InputOutput io = IOProvider.getDefault().getIO("Create security jar", false);
                     io.setFocusTaken(true);
                     final Map<DataObject, Info> infos = new HashMap<DataObject, Info>();
                     for (final DataObject dataObject : context) {
                         final Project project = FileOwnerQuery.getOwner(dataObject.getPrimaryFile());
                         FileObject workingFolder = null;
 
                         try {
                             if (project == null) {
                                 throw new IllegalStateException("project not available for jnlp: " + dataObject);
                             } else {
                                 final Info info = getInfo(project, infos.values());
                                 if (info == null) {
                                     workingFolder = prepareWorkingFolder(project);
 
                                     final Properties projectProps = project.getLookup().lookup(Properties.class);
                                     if (projectProps == null) {
                                         throw new IllegalStateException(
                                             "project properties not availabe for project: "
                                                     + project);
                                     } else {
                                         final String ksPath = projectProps.getProperty(
                                                 PropertyProvider.KEY_GENERAL_KEYSTORE);
                                         final String ksPw = projectProps.getProperty(
                                                 PropertyProvider.KEY_GENERAL_KEYSTORE_PW);
 
                                         if ((ksPath == null) || ksPath.isEmpty() || (ksPw == null) || ksPw.isEmpty()) {
                                             throw new IllegalStateException(
                                                 "project properties do not contain keystore path and pw properties"); // NOI18N
                                         } else {
                                             final Info i = new Info();
                                             i.project = project;
                                             i.workingFolder = workingFolder;
                                             i.ksPath = ksPath;
                                             i.ksPw = ksPw;
                                             i.transformed = false;
 
                                             infos.put(dataObject, i);
                                         }
                                     }
                                 } else {
                                     infos.put(dataObject, info);
                                 }
                             }
                         } catch (final Exception e) {
                             final String message = "cannot process: " + dataObject.getPrimaryFile().getNameExt();
                             LOG.warn(message, e);
                             io.getErr().println(message);
                             io.getErr().println(e.getMessage());
 
                             if (workingFolder != null) {
                                 try {
                                     workingFolder.delete();
                                 } catch (IOException ex) {
                                     LOG.warn("cannot delete working folder: " + workingFolder, ex);
                                 }
                             }
                         }
                     }
 
                     try {
                         transformJnlps(io, infos);
                         buildSecurityJars(io, infos);
                     } finally {
                         cleanup(infos);
                     }
                 }
             });
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  infos  DOCUMENT ME!
      */
     private void cleanup(final Map<DataObject, Info> infos) {
         for (final Info info : infos.values()) {
             try {
                 info.workingFolder.delete();
             } catch (final IOException e) {
                 LOG.warn("cannot delete working folder: " + info.workingFolder, e);
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  io     DOCUMENT ME!
      * @param  infos  DOCUMENT ME!
      */
     private void transformJnlps(final InputOutput io, final Map<DataObject, Info> infos) {
         for (final DataObject dataObject : infos.keySet()) {
             final SAXReader reader = new SAXReader();
             XMLWriter writer = null;
 
             try {
                 final FileObject fo = dataObject.getPrimaryFile();
 
                 dispatchMessage(io.getOut(), "Transforming '" + fo.getNameExt() + "' ...", false);
 
                 final String secHref = "client/" + infos.get(dataObject).project.getProjectDirectory().getName() + "/"
                             + fo.getName() + "_security.jar";
                 final Document d = reader.read(fo.getInputStream());
                 final List jarNodes = d.selectNodes("//jnlp/resources/jar"); // NOI18N
 
                 Element secNode = null;
                 for (final Iterator it = jarNodes.iterator(); it.hasNext() && (secNode == null);) {
                     final Node jarNode = (Node)it.next();
                     final String href = jarNode.valueOf("@href"); // NOI18N
                     if ((href != null) && href.equals(secHref)) { // NOI18N
                         secNode = (Element)jarNode;
                     }
                 }
 
                 if (secNode == null) {
                     final Element resources = (Element)d.selectSingleNode("//jnlp/resources"); // NOI18N
                     secNode = resources.addElement("jar");                                     // NOI18N
                     secNode.addAttribute("href", secHref);                                     // NOI18N
                 }
 
                 secNode.addAttribute("main", "true"); // NOI18N
 
                 writer = new XMLWriter(OutputFormat.createPrettyPrint());
                 writer.setOutputStream(fo.getOutputStream());
                 writer.write(d);
 
                 infos.get(dataObject).transformed = true;
 
                 dispatchMessage(io.getOut(), "DONE", true);
             } catch (final Exception ex) {
                 final String message = "cannot transform jnlp";
                 LOG.error(message, ex);
                 dispatchMessage(io.getErr(), message, true);
             } finally {
                if(writer != null) {
                     try {
                         writer.close();
                     } catch (final IOException e) {
                         // noop
                     }
                 }
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  out      DOCUMENT ME!
      * @param  message  DOCUMENT ME!
      * @param  newline  DOCUMENT ME!
      */
     private void dispatchMessage(final OutputWriter out, final String message, final boolean newline) {
         EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     if (newline) {
                         out.println(message);
                     } else {
                         out.print(message);
                     }
                 }
             });
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  io     DOCUMENT ME!
      * @param  infos  DOCUMENT ME!
      */
     private void buildSecurityJars(final InputOutput io, final Map<DataObject, Info> infos) {
         for (final DataObject dataObject : infos.keySet()) {
             final Info info = infos.get(dataObject);
             if (info.transformed) {
                 final FileObject fo = dataObject.getPrimaryFile();
 
                 dispatchMessage(io.getOut(), "Building security jar for '" + fo.getNameExt() + "' ...", false);
 
                 final FileObject src = info.workingFolder.getFileObject("src");
                 try {
                     fo.copy(src.getFileObject("JNLP-INF"), "APPLICATION", "JNLP"); // NOI18N
                     final FileObject buildxml = info.workingFolder.getFileObject("build.xml");
                     final DeployInformation di = new DeployInformation(
                             buildxml,
                             src,
                             ".."
                                     + File.separator
                                     + fo.getName()
                                     + "_security.jar",
                             FileUtil.toFileObject(new File(info.ksPath)),
                             "cismet",
                             String.valueOf(PasswordEncrypter.decrypt(info.ksPw.toCharArray(), true)),
                             src.getFileObject("META-INF").getFileObject("MANIFEST.MF"));
                     JarHandler.deployJar(di);
 
                     dispatchMessage(io.getOut(), "DONE", true);
                 } catch (final IOException ex) {
                     final String message = "cannot build security jar";
                     LOG.error(message, ex);
                     dispatchMessage(io.getErr(), message, true);
                 }
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   project  DOCUMENT ME!
      * @param   infos    DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private Info getInfo(final Project project, final Collection<Info> infos) {
         for (final Info info : infos) {
             if (info.project.equals(project)) {
                 return info;
             }
         }
 
         return null;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   project  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  IOException  DOCUMENT ME!
      */
     private FileObject prepareWorkingFolder(final Project project) throws IOException {
         FileObject fo = project.getProjectDirectory().getFileObject("work");
         if (fo != null) {
             fo.delete();
         }
         fo = project.getProjectDirectory().createFolder("work");
         final FileObject src = fo.createFolder("src");
         final FileObject mi = src.createFolder("META-INF");
         final FileObject mf = mi.createData("MANIFEST.MF");
         src.createFolder("JNLP-INF");
 
         final InputStream mis = CreateSecurityJarAction.class.getResourceAsStream("MANIFEST.MF");
         final OutputStream mos = mf.getOutputStream();
         final InputStream bis = CreateSecurityJarAction.class.getResourceAsStream("build.xml");
         final OutputStream bos = fo.createData("build.xml").getOutputStream();
 
         assert mis != null;
         assert bis != null;
         try {
             IOUtils.copy(mis, mos);
             IOUtils.copy(bis, bos);
         } finally {
             IOUtils.closeQuietly(mis);
             IOUtils.closeQuietly(mos);
             IOUtils.closeQuietly(bis);
             IOUtils.closeQuietly(bos);
         }
 
         return fo;
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private static final class Info {
 
         //~ Instance fields ----------------------------------------------------
 
         Project project;
         FileObject workingFolder;
         String ksPath;
         String ksPw;
         boolean transformed;
     }
 }
