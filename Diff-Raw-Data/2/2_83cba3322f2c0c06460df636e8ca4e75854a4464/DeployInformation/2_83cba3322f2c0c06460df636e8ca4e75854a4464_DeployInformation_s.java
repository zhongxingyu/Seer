 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.abf.librarysupport.project.util;
 
 import org.apache.log4j.Logger;
 
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.FileUtil;
 import org.openide.nodes.Node;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 
 import java.util.Arrays;
 
 import de.cismet.cids.abf.librarysupport.project.customizer.PropertyProvider;
 import de.cismet.cids.abf.librarysupport.project.nodes.cookies.LibrarySupportContextCookie;
 import de.cismet.cids.abf.librarysupport.project.nodes.cookies.ManifestProviderCookie;
 import de.cismet.cids.abf.librarysupport.project.nodes.cookies.SourceContextCookie;
 
 import de.cismet.tools.PasswordEncrypter;
 
 /**
  * DOCUMENT ME!
  *
  * @author   mscholl
  * @version  1.7
  */
 public final class DeployInformation {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final transient Logger LOG = Logger.getLogger(DeployInformation.class);
 
     //~ Instance fields --------------------------------------------------------
 
     private FileObject buildXML;
     private FileObject sourceDir;
     private FileObject keystore;
     private FileObject manifest;
     private String destFilePath;
     private String alias;
     private char[] storepass;
     private boolean useSignService;
     private String signServiceUrl;
     private String signServiceUser;
     private char[] signServicePass;
     private String signServiceLoglevel;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new DeployInformation object.
      *
      * @param  buildXML         DOCUMENT ME!
      * @param  sourceDir        DOCUMENT ME!
      * @param  keystore         DOCUMENT ME!
      * @param  manifest         DOCUMENT ME!
      * @param  destFilePath     DOCUMENT ME!
      * @param  alias            DOCUMENT ME!
      * @param  storepass        DOCUMENT ME!
      * @param  useSignService   DOCUMENT ME!
      * @param  signServiceUrl   DOCUMENT ME!
      * @param  signServiceUser  DOCUMENT ME!
      * @param  signServicePass  DOCUMENT ME!
      */
     public DeployInformation(final FileObject buildXML,
             final FileObject sourceDir,
             final FileObject keystore,
             final FileObject manifest,
             final String destFilePath,
             final String alias,
             final char[] storepass,
             final boolean useSignService,
             final String signServiceUrl,
             final String signServiceUser,
             final char[] signServicePass) {
         this(
             buildXML,
             sourceDir,
             keystore,
             manifest,
             destFilePath,
             alias,
             storepass,
             useSignService,
             signServiceUrl,
             signServiceUser,
             signServicePass,
             null);
     }
    
     /**
      * Creates a new DeployInformation object.
      *
      * @param  buildXML             DOCUMENT ME!
      * @param  sourceDir            DOCUMENT ME!
      * @param  keystore             DOCUMENT ME!
      * @param  manifest             DOCUMENT ME!
      * @param  destFilePath         DOCUMENT ME!
      * @param  alias                DOCUMENT ME!
      * @param  storepass            DOCUMENT ME!
      * @param  useSignService       DOCUMENT ME!
      * @param  signServiceUrl       DOCUMENT ME!
      * @param  signServiceUser      DOCUMENT ME!
      * @param  signServicePass      DOCUMENT ME!
      * @param  signServiceLoglevel  DOCUMENT ME!
      */
     public DeployInformation(final FileObject buildXML,
             final FileObject sourceDir,
             final FileObject keystore,
             final FileObject manifest,
             final String destFilePath,
             final String alias,
             final char[] storepass,
             final boolean useSignService,
             final String signServiceUrl,
             final String signServiceUser,
             final char[] signServicePass,
             final String signServiceLoglevel) {
         this.buildXML = buildXML;
         this.sourceDir = sourceDir;
         this.keystore = keystore;
         this.manifest = manifest;
         this.destFilePath = destFilePath;
         this.alias = alias;
         this.storepass = Arrays.copyOf(storepass, storepass.length);
         this.useSignService = useSignService;
         this.signServiceUrl = signServiceUrl;
         this.signServiceUser = signServiceUser;
         this.signServicePass = Arrays.copyOf(signServicePass, signServicePass.length);
         this.signServiceLoglevel = signServiceLoglevel;
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public FileObject getBuildXML() {
         return buildXML;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  buildXML  DOCUMENT ME!
      */
     public void setBuildXML(final FileObject buildXML) {
         this.buildXML = buildXML;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public FileObject getSourceDir() {
         return sourceDir;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  sourceDir  DOCUMENT ME!
      */
     public void setSourceDir(final FileObject sourceDir) {
         this.sourceDir = sourceDir;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public FileObject getKeystore() {
         return keystore;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  keystore  DOCUMENT ME!
      */
     public void setKeystore(final FileObject keystore) {
         this.keystore = keystore;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public FileObject getManifest() {
         return manifest;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  manifest  DOCUMENT ME!
      */
     public void setManifest(final FileObject manifest) {
         this.manifest = manifest;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getDestFilePath() {
         return destFilePath;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  destFilePath  DOCUMENT ME!
      */
     public void setDestFilePath(final String destFilePath) {
         this.destFilePath = destFilePath;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getAlias() {
         return alias;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  alias  DOCUMENT ME!
      */
     public void setAlias(final String alias) {
         this.alias = alias;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public char[] getStorepass() {
         return storepass;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  storepass  DOCUMENT ME!
      */
     public void setStorepass(final char[] storepass) {
         this.storepass = storepass;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean isUseSignService() {
         return useSignService;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  useSignService  DOCUMENT ME!
      */
     public void setUseSignService(final boolean useSignService) {
         this.useSignService = useSignService;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getSignServiceUrl() {
         return signServiceUrl;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  signServiceUrl  DOCUMENT ME!
      */
     public void setSignServiceUrl(final String signServiceUrl) {
         this.signServiceUrl = signServiceUrl;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getSignServiceUser() {
         return signServiceUser;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  signServiceUser  DOCUMENT ME!
      */
     public void setSignServiceUser(final String signServiceUser) {
         this.signServiceUser = signServiceUser;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public char[] getSignServicePass() {
         return signServicePass;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  signServicePass  DOCUMENT ME!
      */
     public void setSignServicePass(final char[] signServicePass) {
         this.signServicePass = signServicePass;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getSignServiceLoglevel() {
         return signServiceLoglevel;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  signServiceLoglevel  DOCUMENT ME!
      */
     public void setSignServiceLoglevel(final String signServiceLoglevel) {
         this.signServiceLoglevel = signServiceLoglevel;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   n  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static DeployInformation getDeployInformation(final Node n) {
         try {
             final SourceContextCookie sourceCookie = n.getCookie(SourceContextCookie.class);
             final LibrarySupportContextCookie libCC = n.getCookie(LibrarySupportContextCookie.class);
             final ManifestProviderCookie manifestProvider = n.getCookie(ManifestProviderCookie.class);
             final FileObject manifest;
             if (manifestProvider == null) {
                 manifest = libCC.getLibrarySupportContext().getDefaultManifest();
             } else {
                 manifest = manifestProvider.getManifest();
             }
             final FileObject srcFile = sourceCookie.getSourceObject();
             final File destFilePath = FileUtil.toFile(srcFile.getParent().getParent().getParent());
             final PropertyProvider provider = PropertyProvider.getInstance(
                     libCC.getLibrarySupportContext().getProjectProperties());
             final FileObject keystore = FileUtil.toFileObject(new File(
                         provider.get(PropertyProvider.KEY_GENERAL_KEYSTORE)));
             final String keystoreAlias = provider.get(PropertyProvider.KEY_KEYSTORE_ALIAS);
 
             final char[] passwd = PasswordEncrypter.decrypt(provider.get(PropertyProvider.KEY_GENERAL_KEYSTORE_PW)
                             .toCharArray(),
                     true);
 
             final String destFile = destFilePath.getAbsolutePath()
                         + System.getProperty("file.separator") // NOI18N
                         + srcFile.getName() + ".jar";          // NOI18N
 
             final boolean useSignService = PropertyProvider.STRATEGY_USE_SIGN_SERVICE.equals(provider.get(
                         PropertyProvider.KEY_DEPLOYMENT_STRATEGY));
             final String signServiceUrl = provider.get(PropertyProvider.KEY_SIGN_SERVICE_URL);
             final String signServiceUser = provider.get(PropertyProvider.KEY_SIGN_SERVICE_USERNAME);
             final char[] signServicePass = PasswordEncrypter.decrypt(provider.get(
                         PropertyProvider.KEY_SIGN_SERVICE_PASSWORD).toCharArray(),
                     true);
             final String signServiceLoglevel = provider.get(PropertyProvider.KEY_SIGN_SERVICE_LOG_LEVEL);
 
             final DeployInformation info = new DeployInformation(
                     libCC.getLibrarySupportContext().getBuildXML(),
                     srcFile,
                     keystore,
                     manifest,
                     destFile,
                     keystoreAlias,
                     passwd,
                     useSignService,
                     signServiceUrl,
                     signServiceUser,
                     signServicePass,
                     signServiceLoglevel);
 
             PasswordEncrypter.wipe(passwd);
             PasswordEncrypter.wipe(signServicePass);
 
             return info;
         } catch (final FileNotFoundException ex) {
             LOG.error("could not create deploy information", ex); // NOI18N
             return null;
         }
     }
 }
