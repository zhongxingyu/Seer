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
 
 import org.dom4j.Attribute;
 import org.dom4j.Document;
 import org.dom4j.Element;
 import org.dom4j.io.OutputFormat;
 import org.dom4j.io.SAXReader;
 import org.dom4j.io.XMLWriter;
 
 import org.netbeans.api.project.FileOwnerQuery;
 import org.netbeans.api.project.Project;
 
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.FileUtil;
 import org.openide.loaders.DataObject;
 import org.openide.util.NbBundle;
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
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Executor;
 
 import de.cismet.cids.abf.librarysupport.JarHandler;
 import de.cismet.cids.abf.librarysupport.project.customizer.PropertyProvider;
 import de.cismet.cids.abf.librarysupport.project.util.DeployInformation;
 import de.cismet.cids.abf.utilities.ProgressIndicatingExecutor;
 
 import de.cismet.tools.PasswordEncrypter;
 
 /**
  * DOCUMENT ME!
  *
  * @version  1.0
  */
 public final class CreateSecurityJarAction implements ActionListener {
 
     //~ Static fields/initializers ---------------------------------------------
 
     /** LOGGER. */
     private static final transient Logger LOG = Logger.getLogger(CreateSecurityJarAction.class);
     private static final Executor EXECUTOR = new ProgressIndicatingExecutor(
             "Create security jar",            // NOI18N
             "create-security-jar-dispatcher", // NOI18N
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
                     final InputOutput io = IOProvider.getDefault()
                                 .getIO(
                                     NbBundle.getMessage(
                                         CreateSecurityJarAction.class,
                                         "CreateSecurityJarAction.actionPerformed(ActionEvent).io.title"), // NOI18N
                                     false);
                     io.setFocusTaken(true);
                     final Map<DataObject, Info> infos = new HashMap<DataObject, Info>();
                     for (final DataObject dataObject : context) {
                         final Project p = FileOwnerQuery.getOwner(dataObject.getPrimaryFile());
                         if (!(p instanceof ClientProject)) {
                             throw new IllegalStateException("this action can only be used for client projects");
                         }
                         final ClientProject project = (ClientProject)p;
 
                         FileObject workingFolder = null;
 
                         try {
                             final Info info = getInfo(project, infos.values());
                             if (info == null) {
                                 workingFolder = prepareWorkingFolder(project);
 
                                 final PropertyProvider provider = PropertyProvider.getInstance(
                                         project.getProjectProperties());
                                 final String ksPath = provider.get(PropertyProvider.KEY_GENERAL_KEYSTORE);
                                 final String ksPw = provider.get(PropertyProvider.KEY_GENERAL_KEYSTORE_PW);
 
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
                                     i.alias = provider.get(PropertyProvider.KEY_KEYSTORE_ALIAS);
                                     i.useSignService = PropertyProvider.STRATEGY_USE_SIGN_SERVICE.equals(
                                             provider.get(PropertyProvider.KEY_DEPLOYMENT_STRATEGY));
                                     i.signServiceUrl = provider.get(PropertyProvider.KEY_SIGN_SERVICE_URL);
                                     i.signServiceUser = provider.get(PropertyProvider.KEY_SIGN_SERVICE_USERNAME);
 
                                     final String sspw = provider.get(PropertyProvider.KEY_SIGN_SERVICE_PASSWORD);
                                     if (sspw == null) {
                                         i.signServicePass = null;
                                     } else {
                                         i.signServicePass = PasswordEncrypter.decrypt(sspw.toCharArray(), true);
                                     }
 
                                     i.signServiceLoglevel = provider.get(PropertyProvider.KEY_SIGN_SERVICE_LOG_LEVEL);
 
                                     infos.put(dataObject, i);
                                 }
                             } else {
                                 infos.put(dataObject, info);
                             }
                         } catch (final Exception e) {
                             final String message = NbBundle.getMessage(
                                     CreateSecurityJarAction.class,
                                     "CreateSecurityJarAction.actionPerformed(ActionEvent).message.processError", // NOI18N
                                     dataObject.getPrimaryFile().getNameExt());
                             LOG.warn(message, e);
                             io.getErr().println(message);
                             io.getErr().println(e.getMessage());
 
                             if (workingFolder != null) {
                                 try {
                                     workingFolder.delete();
                                 } catch (IOException ex) {
                                     LOG.warn("cannot delete working folder: " + workingFolder, ex); // NOI18N
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
                 LOG.warn("cannot delete working folder: " + info.workingFolder, e); // NOI18N
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   info  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private boolean isLegacyStructure(final Info info) {
         return "client".equals(info.project.getProjectDirectory().getName()); // NOI18N
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
 
                 dispatchMessage(io.getOut(),
                     NbBundle.getMessage(
                         CreateSecurityJarAction.class,
                         "CreateSecurityJarAction.transformJnlps(InputOutput,Map<DataObject,Info>).message.start", // NOI18N
                         fo.getNameExt()),
                     false);
 
                 final StringBuilder secHref = new StringBuilder("client/");                                                          // NOI18N
                 if (isLegacyStructure(infos.get(dataObject))) {
                     dispatchMessage(io.getErr(), "", true);                                                                          // NOI18N
                     dispatchMessage(io.getErr(),
                         NbBundle.getMessage(
                             CreateSecurityJarAction.class,
                             "CreateSecurityJarAction.transformJnlps(InputOutput,Map<DataObject,Info>).message.warnLegacyStructure"), // NOI18N
                         true);
                 } else {
                     secHref.append(infos.get(dataObject).project.getProjectDirectory().getName());
                     secHref.append("/");                                                                                             // NOI18N
                 }
                 secHref.append(fo.getName());
                 secHref.append("_security.jar");                                                                                     // NOI18N
 
                 final Document d = reader.read(fo.getInputStream());
                 final List jarNodes = d.selectNodes("//jnlp/resources/jar"); // NOI18N
 
                 Element secNode = null;
                 for (final Iterator it = jarNodes.iterator(); it.hasNext();) {
                     final Element jarNode = (Element)it.next();
                     final String href = jarNode.valueOf("@href"); // NOI18N
                     if ((secNode == null) && (href != null) && href.equals(secHref)) {
                         secNode = jarNode;
                     }
 
                     // remove any other main attribute
                     final Attribute main = jarNode.attribute("main"); // NOI18N
                     if (main != null) {
                         jarNode.remove(main);
                     }
                 }
 
                 if (secNode == null) {
                     final Element resources = (Element)d.selectSingleNode("//jnlp/resources"); // NOI18N
                     secNode = resources.addElement("jar");                                     // NOI18N
                     secNode.addAttribute("href", secHref.toString());                          // NOI18N
                 }
 
                 secNode.addAttribute("main", "true"); // NOI18N
 
                 writer = new XMLWriter(OutputFormat.createPrettyPrint());
                 writer.setOutputStream(fo.getOutputStream());
                 writer.write(d);
 
                 infos.get(dataObject).transformed = true;
 
                 dispatchMessage(io.getOut(),
                     NbBundle.getMessage(
                         CreateSecurityJarAction.class,
                         "CreateSecurityJarAction.transformJnlps(InputOutput,Map<DataObject,Info>).message.finished"), // NOI18N
                     true);
             } catch (final Exception ex) {
                 final String message = NbBundle.getMessage(
                         CreateSecurityJarAction.class,
                         "CreateSecurityJarAction.transformJnlps(InputOutput,Map<DataObject,Info>).message.transformError"); // NOI18N
                 LOG.error(message, ex);
                 dispatchMessage(io.getErr(), message, true);
             } finally {
                 if (writer != null) {
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
 
                 dispatchMessage(io.getOut(),
                     NbBundle.getMessage(
                         CreateSecurityJarAction.class,
                         "CreateSecurityJarAction.buildSecurityJars(InputOutput,Map<DataObject,Info>).message.buildingJar", // NOI18N
                         fo.getNameExt()),
                     false);
 
                 final FileObject src = info.workingFolder.getFileObject("src");                // NOI18N
                 final FileObject jnlpDir = src.getFileObject("JNLP-INF");                      // NOI18N
                 final FileObject jnlp = jnlpDir.getFileObject("APPLICATION.JNLP");             // NOI18N
                 try {
                     if (jnlp != null) {
                         jnlp.delete();
                     }
                     fo.copy(jnlpDir, "APPLICATION", "JNLP");                                   // NOI18N
                     final FileObject buildxml = info.workingFolder.getFileObject("build.xml"); // NOI18N
                     final File outfile = new File(info.workingFolder.getPath()
                                     + File.separator
                                     + ".."                                                     // NOI18N
                                     + File.separator
                                     + fo.getName()
                                     + "_security.jar");                                        // NOI18N
                     final DeployInformation di = new DeployInformation(
                             buildxml,
                             src,
                             FileUtil.toFileObject(new File(info.ksPath)),
                             src.getFileObject("META-INF").getFileObject("MANIFEST.MF"),        // NOI18N
                             outfile.getAbsolutePath(),
                             (info.alias == null) ? "cismet" : info.alias,                      // NOI18N
                             PasswordEncrypter.decrypt(info.ksPw.toCharArray(), true),
                             info.useSignService,
                             info.signServiceUrl,
                             info.signServiceUser,
                             info.signServicePass,
                             info.signServiceLoglevel);
                     JarHandler.deployJar(di);
 
                     dispatchMessage(io.getOut(),
                         NbBundle.getMessage(
                             CreateSecurityJarAction.class,
                             "CreateSecurityJarAction.buildSecurityJars(InputOutput,Map<DataObject,Info>).message.finished"), // NOI18N
                         true);
                } catch (final IOException ex) {
                     final String message = NbBundle.getMessage(
                             CreateSecurityJarAction.class,
                             "CreateSecurityJarAction.buildSecurityJars(InputOutput,Map<DataObject,Info>).message.buildError"); // NOI18N
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
         final String wfName = "work_" + System.currentTimeMillis();          // NOI18N
         FileObject fo = project.getProjectDirectory().getFileObject(wfName); // NOI18N
         if (fo != null) {
             fo.delete();
         }
         fo = project.getProjectDirectory().createFolder(wfName);
         final FileObject src = fo.createFolder("src");                       // NOI18N
         final FileObject mi = src.createFolder("META-INF");                  // NOI18N
         final FileObject mf = mi.createData("MANIFEST.MF");                  // NOI18N
         src.createFolder("JNLP-INF");                                        // NOI18N
 
         final InputStream mis = CreateSecurityJarAction.class.getResourceAsStream("MANIFEST.MF"); // NOI18N
         final OutputStream mos = mf.getOutputStream();
         final InputStream bis = CreateSecurityJarAction.class.getResourceAsStream("build.xml");   // NOI18N
         final OutputStream bos = fo.createData("build.xml").getOutputStream();                    // NOI18N
 
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
         String alias;
         boolean useSignService;
         String signServiceUrl;
         String signServiceUser;
         char[] signServicePass;
         String signServiceLoglevel;
     }
 }
