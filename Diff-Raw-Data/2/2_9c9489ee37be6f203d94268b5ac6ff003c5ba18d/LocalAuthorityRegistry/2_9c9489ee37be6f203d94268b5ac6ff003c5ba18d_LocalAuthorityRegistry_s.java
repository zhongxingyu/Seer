 package fr.cg95.cvq.service.authority.impl;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Transparency;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.lang.reflect.Method;
 import java.math.BigInteger;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.imageio.ImageIO;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.hibernate.SessionFactory;
 import org.joda.time.DateMidnight;
 import org.joda.time.Interval;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.beans.factory.BeanFactoryAware;
 import org.springframework.beans.factory.ListableBeanFactory;
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.beans.factory.config.TypedStringValue;
 import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.context.support.GenericApplicationContext;
 import org.springframework.core.io.Resource;
 import org.springframework.orm.hibernate3.LocalSessionFactoryBean;
 
 import fr.cg95.cvq.business.authority.Agent;
 import fr.cg95.cvq.business.authority.LocalAuthority;
 import fr.cg95.cvq.business.authority.LocalAuthorityResource;
 import fr.cg95.cvq.business.authority.SiteProfile;
 import fr.cg95.cvq.business.authority.SiteRoles;
 import fr.cg95.cvq.business.authority.LocalAuthorityResource.Type;
 import fr.cg95.cvq.business.authority.LocalAuthorityResource.Version;
 import fr.cg95.cvq.business.users.Address;
 import fr.cg95.cvq.business.users.Adult;
 import fr.cg95.cvq.business.users.FamilyStatusType;
 import fr.cg95.cvq.business.users.RoleType;
 import fr.cg95.cvq.business.users.TitleType;
 import fr.cg95.cvq.dao.authority.ILocalAuthorityDAO;
 import fr.cg95.cvq.dao.hibernate.HibernateUtil;
 import fr.cg95.cvq.exception.CvqConfigurationException;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.exception.CvqObjectNotFoundException;
 import fr.cg95.cvq.security.SecurityContext;
 import fr.cg95.cvq.security.annotation.Context;
 import fr.cg95.cvq.security.annotation.ContextPrivilege;
 import fr.cg95.cvq.security.annotation.ContextType;
 import fr.cg95.cvq.service.authority.IAgentService;
 import fr.cg95.cvq.service.authority.ILocalAuthorityLifecycleAware;
 import fr.cg95.cvq.service.authority.ILocalAuthorityRegistry;
 import fr.cg95.cvq.service.authority.LocalAuthorityConfigurationBean;
 import fr.cg95.cvq.service.users.IHomeFolderService;
 import fr.cg95.cvq.util.development.BusinessObjectsFactory;
 
 /**
  * Implementation of the local authority registry.
  *
  * @author bor@zenexity.fr
  */
 public class LocalAuthorityRegistry
     implements ILocalAuthorityRegistry, ApplicationContextAware, BeanFactoryAware {
 
     private static Logger logger = Logger.getLogger(LocalAuthorityRegistry.class);
 
     /**
      * The name of the Local authority used in development mode
      */
     private static final String DEVELOPMENT_LOCAL_AUTHORITY = "blainville";
     private static final String DEVELOPMENT_ADMIN_NAME = "admin";
     private static final String DEVELOPMENT_AGENT_NAME = "agent";
     private static final String DEVELOPMENT_MANAGER_NAME = "manager";
 
     /** Used to store the currently deployed local authorities. */
     private Map<String, LocalAuthorityConfigurationBean> configurationBeansMap = 
         new HashMap<String, LocalAuthorityConfigurationBean>();
 
     /** Mapping between URL server names and local authorities */
     private Map<String, LocalAuthority> serverNameMappings =
         new HashMap<String, LocalAuthority>();
 
     /** The parent application context in which the application runs. */
     private ApplicationContext parentApplicationContext;
 
     /** Keep a map of all services interested in local authorities lifecycle */
     protected Collection<ILocalAuthorityLifecycleAware> allListenerServices;
 
     private ILocalAuthorityDAO localAuthorityDAO;
     private IAgentService agentService;
     private IHomeFolderService homeFolderService;
 
     private ListableBeanFactory beanFactory;
 
     private Boolean performDbUpdates;
 
     private String referentialBase;
     private String assetsBase;
     private String[] includedLocalAuthorities;
     private String localAuthoritiesListFilename;
 
     private SessionFactory templateSessionFactory;
 
     public void init() {
         Map<String, ILocalAuthorityLifecycleAware> services =
             beanFactory.getBeansOfType(ILocalAuthorityLifecycleAware.class, true, true);
         if (!services.isEmpty()) {
             allListenerServices = services.values();
         }
     }
 
     @Override
     public LocalAuthority getLocalAuthorityByServerName(String serverName) {
         return serverNameMappings.get(serverName);
     }
 
     @Override
     @Context(types = {ContextType.ADMIN}, privilege = ContextPrivilege.WRITE)
     public void addLocalAuthorityServerName(String serverName) {
         LocalAuthority localAuthority = SecurityContext.getCurrentSite();
         localAuthority.getServerNames().add(serverName);
         localAuthorityDAO.update(localAuthority);
         registerLocalAuthorityServerName(serverName);
     }
 
     @Override
     @Context(types = {ContextType.ADMIN}, privilege = ContextPrivilege.WRITE)
     public void registerLocalAuthorityServerName(String serverName) {
         serverNameMappings.put(serverName, SecurityContext.getCurrentSite());
     }
 
     @Override
     @Context(types = {ContextType.ADMIN}, privilege = ContextPrivilege.WRITE)
     public void removeLocalAuthorityServerName(String serverName) {
         LocalAuthority localAuthority = SecurityContext.getCurrentSite();
         localAuthority.getServerNames().remove(serverName);
         localAuthorityDAO.update(localAuthority);
         unregisterLocalAuthorityServerName(serverName);
     }
 
     @Override
     @Context(types = {ContextType.ADMIN}, privilege = ContextPrivilege.WRITE)
     public void unregisterLocalAuthorityServerName(String serverName) {
         serverNameMappings.put(serverName, null);
     }
 
     @Override
     @Context(types = {ContextType.ADMIN}, privilege = ContextPrivilege.WRITE)
     public void setLocalAuthorityServerNames(TreeSet<String> serverNames)
         throws CvqException {
         // this method iterates twice on serverNames but it is needed
         // to check serverNames availability before erasing previous ones,
         // and avoid concurrent modification exceptions, although it can probably be enhanced
         for (String serverName : serverNames) {
             if (!isAvailableLocalAuthorityServerName(serverName)) {
                 throw new CvqException("localAuthority.error.existingUrl", new String[]{serverName});
             }
         }
         LocalAuthority localAuthority = SecurityContext.getCurrentSite();
         for (String serverName : localAuthority.getServerNames()) {
                 unregisterLocalAuthorityServerName(serverName);
         }
         localAuthority.getServerNames().clear();
         for (String serverName : serverNames) {
             addLocalAuthorityServerName(serverName);
         }
     }
 
     @Override
     @Context(types = {ContextType.ADMIN}, privilege = ContextPrivilege.READ)
     public boolean isAvailableLocalAuthorityServerName(String serverName) {
         return (serverNameMappings.get(serverName) == null 
                 || serverNameMappings.get(serverName).getId().equals(SecurityContext.getCurrentSite().getId()));
     }
 
     @Override
     public LocalAuthorityConfigurationBean getLocalAuthorityBeanByName(final String name) {
         if (name == null)
             return null;
 
         return configurationBeansMap.get(name.toLowerCase());
     }
 
     @Override
     public LocalAuthority getLocalAuthorityByName(String name) {
         return localAuthorityDAO.findByName(name);
     }
 
     @Override
     public Set<String> getAllLocalAuthoritiesNames() {
         return configurationBeansMap.keySet();
     }
 
     private LocalAuthorityResource getLocalAuthorityResource(String id)
         throws CvqException {
         LocalAuthorityResource resource = LocalAuthorityResource.localAuthorityResources.get(id);
         if (resource == null) {
             logger.error("getLocalAuthorityResource() couldn't find local authority resource");
             throw new CvqException("localAuthority.resource.error.invalid");
         }
         return resource;
     }
 
     @Override
     public File getLocalAuthorityResourceFile(String id)
         throws CvqException {
         return getLocalAuthorityResourceFile(id, getLocalAuthorityResource(id).canFallback());
     }
 
     @Override
     public File getLocalAuthorityResourceFile(String id, Version version)
         throws CvqException {
         return getLocalAuthorityResourceFile(id, version,
             getLocalAuthorityResource(id).canFallback());
     }
 
     private File getLocalAuthorityResourceFile(String id, boolean fallbackToDefault)
         throws CvqException {
         return getLocalAuthorityResourceFile(id,
             LocalAuthorityResource.Version.CURRENT, fallbackToDefault);
     }
 
     private File getLocalAuthorityResourceFile(String id, Version version, boolean fallbackToDefault)
         throws CvqException {
         LocalAuthorityResource resource = getLocalAuthorityResource(id);
         return getAssetsFile(resource.getType(),
             resource.getFilename() + version.getExtension(), fallbackToDefault);
     }
 
     @Override
     public File getLocalAuthorityResourceFile(Type type, String filename, Version version,
             boolean fallbackToDefault) throws CvqException {
         return getAssetsFile(type, filename + version.getExtension(), fallbackToDefault);
     }
 
     private File getAssetsFile(final Type type, final String filename,
         final boolean fallbackToDefault) {
         StringBuffer filePath = new StringBuffer().append(assetsBase)
             .append(SecurityContext.getCurrentSite().getName().toLowerCase())
             .append("/").append(type.getFolder()).append("/").append(filename)
             .append(type.getExtension());
         logger.debug("getAssetsFile() searching file : " + filePath.toString());
         File resourceFile = new File(filePath.toString());
         if (!resourceFile.exists() && fallbackToDefault) {
             logger.warn("getAssetsFile() did not find " + filePath.toString()
                 + ", trying default");
             return getReferentialResource(type, filename);
         }
         return resourceFile;
     }
 
     @Override
     public File getReferentialResource(final Type type, final String filename) {
         StringBuffer filePath = new StringBuffer().append(referentialBase)
             .append(type.getFolder()).append('/').append(filename)
             .append(type.getExtension());
         logger.debug("getReferentialResource() searching file : " + filePath.toString());
         File resourceFile = new File(filePath.toString());
         if (!resourceFile.exists()) {
             logger.warn("getReferentialResource() did not find resource file : " + filename
                     + " of type " + type);
             return null;
         }
         return resourceFile;
     }
 
     @Override
     public File getLocalAuthorityResourceFile(final Type type, final String filename,
         final boolean fallbackToDefault) {
         return getAssetsFile(type, filename, fallbackToDefault);
     }
 
     @Override
     public File getLocalAuthorityResourceFileForLocalAuthority(final String localAuthorityName,
             final Type type, final String filename, final boolean fallbackToDefault) {
         StringBuffer filePath = new StringBuffer().append(assetsBase)
             .append(localAuthorityName.toLowerCase())
             .append("/").append(type.getFolder()).append("/").append(filename)
             .append(type.getExtension());
         logger.debug("getAssetsFile() searching file : " + filePath.toString());
         File resourceFile = new File(filePath.toString());
         if (!resourceFile.exists() && fallbackToDefault) {
             logger.warn("getAssetsFile() did not find " + filePath.toString()
                     + ", trying default");
             return getReferentialResource(type, filename);
         }
         return resourceFile;
     }
     
     @Override
     public File getRequestXmlResource(Long id) {
         return new File(getRequestXmlPath(id));
     }
 
     private String getRequestXmlPath(Long id) {
         return String.format("%1$s/%2$s/%3$s/%4$s.xml", 
                 this.getAssetsBase(),
                 SecurityContext.getCurrentConfigurationBean().getName(),
                 Type.REQUEST_XML.getFolder(),
                 id);
     }
 
     @Override
     public String getBufferedLocalAuthorityResource(final Type type,
         final String filename, final boolean fallbackToDefault) {
         return getFileContent(
             getLocalAuthorityResourceFile(type, filename, fallbackToDefault));
     }
 
     @Override
     public Map<String,String> getBufferedCurrentLocalAuthorityRequestHelpMap(
         final String requestLabel) {
         
         StringBuffer requestTypePath = new StringBuffer().append(assetsBase)
             .append(SecurityContext.getCurrentSite().getName().toLowerCase())
             .append("/").append(Type.HTML.getFolder()).append("/request/")
             .append(requestLabel);
             
         File requestTypeDir = new File(requestTypePath.toString());
         
         File[] helpFiles = requestTypeDir.listFiles(new FilenameFilter() { 
             public boolean accept(File dir, String n) {
                 if(n.endsWith(".html"))
                     return true;
                 else
                     return false;
             }
         });
 
         HashMap<String,String> helpMap = new HashMap<String,String>();
         if (helpFiles != null)
             for (File helpFile : helpFiles)
                  helpMap.put(helpFile.getName().replace(".html", ""), getFileContent(helpFile));
         
         return helpMap ;
     }
 
     @Override
     public List<String> getLocalAuthorityRules(String requestTypeLabel) {
         StringBuffer requestTypePath = new StringBuffer().append(assetsBase)
             .append(SecurityContext.getCurrentSite().getName().toLowerCase())
             .append("/").append(Type.PDF.getFolder()).append("/").append(requestTypeLabel);
         File requestTypeDir = new File(requestTypePath.toString());
         if (!requestTypeDir.exists()) {
             return Collections.emptyList();
         }
         List<String> result = new ArrayList<String>();
         for (String filename : requestTypeDir.list()) {
             result.add(filename.substring(0, filename.lastIndexOf(".pdf")));
         }
         return result;
     }
 
     private String getFileContent(File resourceFile) {
         
         if (resourceFile == null || !resourceFile.exists())
             return null;
         
         FileReader fileReader = null;
         String result = null;
         try {
             fileReader = new FileReader(resourceFile);
             char[] data = new char[(int) resourceFile.length()];
             fileReader.read(data);
             result = new String(data);
         } catch (FileNotFoundException e) {
             // unlikely to happen since we already checked just above
         } catch (IOException ioe) {
             logger.error("getBufferedCurrentLocalAuthorityResource() error while reading file " 
                     + resourceFile);
             logger.error("getBufferedCurrentLocalAuthorityResource() error : " + ioe);
             return null;
         } finally {
             if (fileReader != null)
                 try {
                     fileReader.close();
                 } catch (IOException e) {
                     logger.error("getBufferedCurrentLocalAuthorityResource() failed to close file reader stream !");
                     e.printStackTrace();
                 }
         }
         return result;
     }
 
     @Override
     @Context(types = {ContextType.ADMIN}, privilege = ContextPrivilege.WRITE)
     public void saveLocalAuthorityResource(Type type, String filename,
         byte[] data) throws CvqException {
         if (data == null) {
             logger.warn("saveLocalAuthorityResource() received empty data to save");
             return;
         }
         File assetsFile = getAssetsFile(type, filename, false);
         try {
             if (!assetsFile.exists())
                 assetsFile.createNewFile();
             FileOutputStream fos = new FileOutputStream(assetsFile);
             fos.write(data);
             fos.flush();
             fos.close();
         } catch (FileNotFoundException e) {
             logger.error("saveLocalAuthorityResource() failel !" + e.getMessage());
             throw new CvqException(e.getMessage());
         } catch (IOException ioe) {
             logger.error("saveLocalAuthorityResource() failel !" + ioe.getMessage());
             throw new CvqException(ioe.getMessage());
         }
     }
 
     @Override
     @Context(types = {ContextType.ADMIN}, privilege = ContextPrivilege.WRITE)
     public void saveLocalAuthorityResource(String id, byte[] data)
         throws CvqException {
         if (data == null) {
             logger.warn("saveLocalAuthorityResource() received empty data to save");
             return;
         }
         File assetsFile = getLocalAuthorityResourceFile(id, false);
         try {
             if (!assetsFile.exists())
                 assetsFile.createNewFile();
             FileOutputStream fos = new FileOutputStream(assetsFile);
             fos.write(data);
             fos.flush();
             fos.close();
         } catch (FileNotFoundException e) {
             logger.error("saveLocalAuthorityResource() failel !" + e.getMessage());
             throw new CvqException(e.getMessage());
         } catch (IOException ioe) {
             logger.error("saveLocalAuthorityResource() failel !" + ioe.getMessage());
             throw new CvqException(ioe.getMessage());
         }
     }
 
     @Override
     @Context(types = {ContextType.ADMIN}, privilege = ContextPrivilege.WRITE)
     public void replaceLocalAuthorityResource(String id, byte[] data)
         throws CvqException {
         if (data == null) {
             logger.warn("replaceCurrentLocalAuthorityResource() received empty data to save");
             return;
         }
         if (hasLocalAuthorityResource(id, LocalAuthorityResource.Version.CURRENT)) {
             renameLocalAuthorityResource(id, LocalAuthorityResource.Version.CURRENT, LocalAuthorityResource.Version.TEMP);
         }
         try {
             saveLocalAuthorityResource(id, data);
         } catch (CvqException e) {
             if (hasLocalAuthorityResource(id, LocalAuthorityResource.Version.TEMP)) {
                 renameLocalAuthorityResource(id, LocalAuthorityResource.Version.TEMP, LocalAuthorityResource.Version.CURRENT);
             }
             throw e;
         }
         if (hasLocalAuthorityResource(id, LocalAuthorityResource.Version.TEMP)) {
             renameLocalAuthorityResource(id, LocalAuthorityResource.Version.TEMP, LocalAuthorityResource.Version.OLD);
         }
         // JSB : hack for PDF generation
         if (LocalAuthorityResource.LOGO_PDF.getId().equals(id)
             || LocalAuthorityResource.FOOTER_PDF.getId().equals(id)) {
             generateJPEG(id);
         }
     }
 
     @Override
     @Context(types = {ContextType.ADMIN}, privilege = ContextPrivilege.WRITE)
     public void rollbackLocalAuthorityResource(String id)
         throws CvqException {
         renameLocalAuthorityResource(id, LocalAuthorityResource.Version.OLD, LocalAuthorityResource.Version.TEMP);
         renameLocalAuthorityResource(id, LocalAuthorityResource.Version.CURRENT, LocalAuthorityResource.Version.OLD);
         renameLocalAuthorityResource(id, LocalAuthorityResource.Version.TEMP, LocalAuthorityResource.Version.CURRENT);
         // JSB : hack for PDF generation
         if (LocalAuthorityResource.LOGO_PDF.getId().equals(id)
             || LocalAuthorityResource.FOOTER_PDF.getId().equals(id)) {
             generateJPEG(id);
         }
     }
 
     @Override
     public boolean hasLocalAuthorityResource(String id, Version version)
         throws CvqException {
         File resourceFile = getLocalAuthorityResourceFile(id, version, false);
         return resourceFile != null && resourceFile.exists();
     }
 
     @Override
     @Context(types = {ContextType.ADMIN}, privilege = ContextPrivilege.WRITE)
     public void renameLocalAuthorityResource(String id, Version oldVersion, Version newVersion)
         throws CvqException {
         LocalAuthorityResource resource = getLocalAuthorityResource(id);
         File file = getLocalAuthorityResourceFile(id, oldVersion, false);
         if (!file.exists()) {
             throw new CvqException("File "+ file.getPath() + " does not exist !");
         }
         if (!file.renameTo(new File(file.getParent() + "/" + resource.getFilename()
             + newVersion.getExtension() + resource.getType().getExtension())))
             throw new CvqException("Can't rename "
                     + resource.getId() + " from version "
                     + oldVersion + " to "
                     + newVersion);
     }
 
     @Override
     @Context(types = {ContextType.ADMIN}, privilege = ContextPrivilege.WRITE)
     public void renameLocalAuthorityResource(Type type, String oldFilename, String newFilename)
         throws CvqException {
         if (oldFilename.equals(newFilename))
             return;
         File file = getLocalAuthorityResourceFile(type, oldFilename, false);
         if (!file.exists()) {
             throw new CvqException("File "+ file.getPath() + " does not exist !");
         }
         if (!file.renameTo(new File(file.getParent() + "/" + newFilename + type.getExtension())))
             throw new CvqException("Can't rename resource type "
                     + type + " from "
                     + oldFilename + " to "
                     + newFilename);
     }
 
     @Override
     @Context(types = {ContextType.ADMIN}, privilege = ContextPrivilege.WRITE)
     public void removeLocalAuthorityResource(Type type, String filename) {
         File assetsFile = getAssetsFile(type, filename, false);
         if (!assetsFile.exists())
             return;
         if (!assetsFile.delete())
             logger.warn("removeLocalAuthorityResource() can't delete " + getAssetsBase() + filename );
     }
 
     @Override
     @Context(types = {ContextType.ADMIN}, privilege = ContextPrivilege.WRITE)
     public void removeLocalAuthorityResource(String id)
         throws CvqException {
         File file;
         for (Version version : LocalAuthorityResource.Version.values()) {
             file = getLocalAuthorityResourceFile(id, version, false);
             if (!file.exists())
                 continue;
             if (!file.delete())
                 logger.warn("removeLocalAuthorityResource() can't delete " + file.getPath() + file.getName());
         }
     }
 
     @Override
     public void generateJPEGFiles() {
         String[] ids = {
             LocalAuthorityResource.LOGO_PDF.getId(),
             LocalAuthorityResource.FOOTER_PDF.getId()
         };
         for (String id : ids) {
             try {
                 File png = getLocalAuthorityResourceFile(id, false);
                 if (png.exists()) {
                     File jpeg = new File(StringUtils.removeEnd(png.getPath(), "png").concat("jpg"));
                     if (!jpeg.exists()) {
                         generateJPEG(id);
                     }
                 }
             } catch (CvqException e) {
                 logger.error("registerLocalAuthorities() unable to generate JPEG for " + id);
             }
         }
     }
 
     /**
      * Hack to regenerate the JPEG version of a LocalAuthorityResource image for PDF files
      */
     private void generateJPEG(String id) {
         File png;
         try {
             png = getLocalAuthorityResourceFile(id, false);
         } catch (CvqException e) {
             logger.warn("generateJPEG() could not get PNG file for " + id);
             return;
         }
         if (png == null || !png.exists()) {
             logger.warn("generateJPEG() PNG file doesn't exist for " + id);
             return;
         }
         File jpeg = new File(StringUtils.removeEnd(png.getPath(), "png").concat("jpg"));
         try {
             jpeg.createNewFile();
             BufferedImage image = ImageIO.read(png);
             if (image.getColorModel().getTransparency() != Transparency.OPAQUE) {
                 int w = image.getWidth();
                 int h = image.getHeight();
                 BufferedImage image2 = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                 Graphics2D g = image2.createGraphics();
                 g.setColor(Color.WHITE);
                 g.fillRect(0,0,w,h);
                 g.drawRenderedImage(image, null);
                 g.dispose();
                 image = image2;
             }
             ImageIO.write(image, "jpg", jpeg);
         } catch (IOException e) {
             logger.warn("generateJPEG() failed to generate JPEG file for " + id);
         }
     }
 
     @Override
     public void registerLocalAuthorities(Resource[] localAuthoritiesFiles) {
 
         GenericApplicationContext gac = new GenericApplicationContext(parentApplicationContext);
         XmlBeanDefinitionReader xmlBeanDefinitionReader = new XmlBeanDefinitionReader(gac);
         if (includedLocalAuthorities == null) {
             xmlBeanDefinitionReader.loadBeanDefinitions(localAuthoritiesFiles);
         } else {
             for (int i = 0; i < localAuthoritiesFiles.length; i++) {
                 String localAuthorityName =
                     localAuthoritiesFiles[i].getFilename().replaceFirst("localAuthority-", "")
                     .replaceFirst(".xml", "");
                 logger.debug("registerLocalAuthorities() extracted " + localAuthorityName);
                 for (int j = 0; j < includedLocalAuthorities.length; j++) {
                     if (includedLocalAuthorities[j].equals(localAuthorityName)) {
                         logger.debug("registerLocalAuthorities() loading " + localAuthorityName);
                         xmlBeanDefinitionReader.loadBeanDefinitions(localAuthoritiesFiles[i]);
                         if (DEVELOPMENT_LOCAL_AUTHORITY.equals(localAuthorityName)
                             && performDbUpdates) {
                             HibernateUtil.setSessionFactory(templateSessionFactory);
                             BeanDefinition definition =
                                 gac.getBeanDefinition("pgDataSource_blainville");
                             try {
                                 String dbName =
                                     new URI(parseJDBCProperty(definition, "jdbcUrl").substring(5))
                                         .getPath().substring(1);
                                 if (BigInteger.ZERO.equals(HibernateUtil.getSession()
                                     .createSQLQuery("select count(*) from pg_catalog.pg_database where datname = :dbName")
                                         .setString("dbName", dbName).uniqueResult())) {
                                     try {
                                         HibernateUtil.getSession().connection().createStatement()
                                             .execute("create database \"" + dbName + '\"');
                                     } catch (SQLException e) {
                                         logger.error("could not create database " + dbName);
                                         e.printStackTrace();
                                     }
                                     gac.getBean("&sessionFactory_blainville",
                                         LocalSessionFactoryBean.class).createDatabaseSchema();
                                 }
                             } catch (URISyntaxException e) {
                                 logger.error("got an exception while trying to create dev database");
                                 e.printStackTrace();
                             }
                         }
                         break;
                     }
                 }
             }
         }
         Map<String, LocalAuthorityConfigurationBean> beansMap =
             gac.getBeansOfType(LocalAuthorityConfigurationBean.class, true, true);
         if (beansMap.isEmpty()) {
             logger.warn("registerLocalAuthorities() no local authority configuration bean found !");
             return;
         }
 
         for (LocalAuthorityConfigurationBean lacb : beansMap.values()) {
             logger.debug("registerLocalAuthorities() adding [" + lacb.getName()
                          + "] to the list of known local authorities (" + lacb + ")");
             configurationBeansMap.put(lacb.getName().toLowerCase(), lacb);
             if (performDbUpdates.booleanValue())
                 callback(lacb.getName(), this, "instantiateLocalAuthority",
                     new String[]{lacb.getName()});
             callback(lacb.getName(), this, "registerLocalAuthority",
                 new String[]{lacb.getName()});
         }
         generateLocalAuthoritiesList();
     }
 
     @SuppressWarnings("unused")
     private void registerLocalAuthority(String localAuthorityName) {
         // notify listener services of the new local authority
         for (ILocalAuthorityLifecycleAware service : allListenerServices) {
             service.addLocalAuthority(localAuthorityName.toLowerCase());
         }
         
         LocalAuthority localAuthority = localAuthorityDAO.findByName(localAuthorityName);
         for (String serverName : localAuthority.getServerNames()) {
             registerLocalAuthorityServerName(serverName);
         }
     }
 
     @SuppressWarnings("unused")
     private void instantiateLocalAuthority(String localAuthorityName)
         throws CvqConfigurationException {
 
         LocalAuthorityConfigurationBean lacb =
             configurationBeansMap.get(localAuthorityName);
         
         LocalAuthority localAuthority = localAuthorityDAO.findByName(lacb.getName());
         // create local authority entry in DB if it does not exist yet
         if (localAuthority == null) {
             logger.debug("instantiateLocalAuthority() creating " + localAuthorityName);
             try {
                 localAuthority = 
                     new LocalAuthority(lacb.getName().toLowerCase(), lacb.getName().toLowerCase());
                 if (lacb.getDefaultServerName() != null)
                     localAuthority.getServerNames().add(lacb.getDefaultServerName());
                 localAuthorityDAO.create(localAuthority);
                 HibernateUtil.getSession().flush();
                 if (DEVELOPMENT_LOCAL_AUTHORITY.equals(localAuthorityName)) {
                     SecurityContext.setCurrentSite(DEVELOPMENT_LOCAL_AUTHORITY,
                         SecurityContext.ADMIN_CONTEXT);
                     createAgent(DEVELOPMENT_ADMIN_NAME, SiteProfile.ADMIN);
                     createAgent(DEVELOPMENT_AGENT_NAME, SiteProfile.AGENT);
                     createAgent(DEVELOPMENT_MANAGER_NAME, SiteProfile.AGENT);
                     localAuthorityDAO.create(
                         BusinessObjectsFactory.gimmeSchool("École Jean Jaurès"));
                     localAuthorityDAO.create(BusinessObjectsFactory
                         .gimmeRecreationCenter("Centre de loisirs Louise Michel"));
                     SecurityContext.setCurrentSite(DEVELOPMENT_LOCAL_AUTHORITY,
                         SecurityContext.FRONT_OFFICE_CONTEXT);
                     Address address = BusinessObjectsFactory.gimmeAdress(
                         "12", "Rue d'Aligre", "Paris", "75012");
                     Adult homeFolderResponsible =
                         BusinessObjectsFactory.gimmeAdult(TitleType.MISTER, "Dupont", "Jean",
                             address, FamilyStatusType.SINGLE);
                     homeFolderService.addHomeFolderRole(homeFolderResponsible,
                         RoleType.HOME_FOLDER_RESPONSIBLE);
                     List<Adult> adults = new ArrayList<Adult>(2);
                     adults.add(homeFolderResponsible);
                     adults.add(BusinessObjectsFactory.gimmeAdult(TitleType.MISTER,
                         "Durand", "Jacques", address, FamilyStatusType.SINGLE));
                     homeFolderResponsible.setPassword("aaaaaaaa");
                    homeFolderService.create(adults, null, new Address());
                 }
                 // set current site to be able to generateJPEGFiles (which uses getCurrentSite) ...
                 SecurityContext.setCurrentSite(localAuthorityName, SecurityContext.ADMIN_CONTEXT);
             } catch (Exception e) {
                 throw new CvqConfigurationException("unable to create local authority " 
                         + localAuthorityName + " (" + e.getMessage() + ")");
             }
         }
         // an existing local authority with no registered server name, bootstrap one
         // from configured default or assign a default one (migration case only)
         if (localAuthority.getServerNames() == null 
                 || localAuthority.getServerNames().isEmpty()) {
             localAuthority.setServerNames(new TreeSet<String>());
             String serverName = null;
             if (lacb.getDefaultServerName() != null)
                 serverName = lacb.getDefaultServerName();
             else
                 serverName = "vosdemarches.ville-" + lacb.getName() + ".fr";
             logger.debug("instantiateLocalAuthority() initializing with server name " 
                     + serverName);
             localAuthority.getServerNames().add(serverName);
             registerLocalAuthorityServerName(serverName);
         } 
         File resourceDir;
         for (Type type : Type.values()) {
             resourceDir = new File(assetsBase + localAuthorityName + "/"
                 + type.getFolder());
             if (!resourceDir.exists())
                 resourceDir.mkdir();
         }
         
         generateJPEGFiles();
     }
 
     @Override
     public void callback(String localAuthority, Object object,
         String callbackMethodName, Object[] args) {
         
         LocalAuthorityConfigurationBean lacb =
             configurationBeansMap.get(localAuthority);
         SessionFactory sessionFactory = lacb.getSessionFactory();
         HibernateUtil.setSessionFactory(sessionFactory);
         try {
             SecurityContext.setCurrentSite(lacb.getName(), SecurityContext.ADMIN_CONTEXT);
         } catch (CvqObjectNotFoundException confe) {
             // unlikely to happen
         }
 
         try {
             
             HibernateUtil.beginTransaction();
 
             if (args == null || args.length == 0) {
                 Method method = 
                     object.getClass().getDeclaredMethod(callbackMethodName);
                 method.invoke(object);
             } else {
                 Object[] methArgs = new Object[args.length];
                 Class<?>[] methDefArgs = new Class[args.length];
                 for (int i=0; i < args.length; i++) {
                     methArgs[i] = args[i];
                     methDefArgs[i] = String.class;
                 }
                 Method method = 
                     object.getClass().getDeclaredMethod(callbackMethodName, methDefArgs);
                 method.invoke(object, methArgs);
             }
 
             HibernateUtil.commitTransaction();
 
         } catch (Exception e) {
             logger.error("callback() got an exception, rollbacking");
             e.printStackTrace();
             HibernateUtil.rollbackTransaction();
         } finally {
             HibernateUtil.closeSession();
             SecurityContext.resetCurrentSite();
         }
     }
 
     @Override
     public void browseAndCallback(Object object, String callbackMethodName,
         Object[] methodArgs) {
         for (String localAuthorityName : configurationBeansMap.keySet()) {
             logger.debug("browseAndCallback() calling " + callbackMethodName
                 + " for " + localAuthorityName);
             callback(localAuthorityName, object, callbackMethodName, methodArgs);
         }
     }
 
     @Override
     public void generateLocalAuthoritiesList() {
         Set<String> allLocalAuthoriesNames = getAllLocalAuthoritiesNames();
 
         try {
             String filename = getAssetsBase() + localAuthoritiesListFilename;
             logger.debug("generateLocalAuthoritiesList() writing list in " + filename);
             FileOutputStream fos = new FileOutputStream(filename);
             for (String localAuth : allLocalAuthoriesNames) {
                 fos.write(localAuth.getBytes());
                 fos.write('\n');
             }
             fos.close();
         } catch (Exception e) {
             logger.error("generateLocalAuthoritiesList() got exception while writing local authorities list");
             e.printStackTrace();
         }
     }
 
     public void setApplicationContext(final ApplicationContext applicationContext) {
         this.parentApplicationContext = applicationContext;
     }
 
     public void setBeanFactory(final BeanFactory beanFactory) throws BeansException {
         this.beanFactory = (ListableBeanFactory) beanFactory;
     }
 
     public void setReferentialBase(final String referentialBase) {
         if (!referentialBase.endsWith("/"))
             this.referentialBase = referentialBase + "/";
         else
             this.referentialBase = referentialBase;
     }
 
     @Override
     public List<String> getLocalAuthorityResourceFileNames(Type type)
         throws CvqException {
         return getLocalAuthorityResourceFileNames(type, ".*");
     }
 
     @Override
     public List<String> getLocalAuthorityResourceFileNames(Type type, final String pattern)
         throws CvqException {
         if (pattern == null)
             throw new CvqException("localresources.mask_cannt_be_null");
         StringBuffer path = new StringBuffer(assetsBase).append("/")
             .append(SecurityContext.getCurrentSite().getName().toLowerCase())
             .append("/").append(type.getFolder()).append("/");
         FilenameFilter filter = new FilenameFilter() {
             public boolean accept(File dir, String name) {
                 return name.toLowerCase().matches(pattern);
             }
         };
         File[] files = new File(path.toString()).listFiles(filter);
         List<String> result = new ArrayList<String>(files.length);
         for (File file : files) {
             result.add(StringUtils.removeEnd(file.getName(), type.getExtension()));
         }
         return result;
     }
 
     @Override
     public String getReferentialBase() {
         return this.referentialBase;
     }
 
     @Override
     public String getAssetsBase() {
         return assetsBase;
     }
 
     public void setAssetsBase(String assetsBase) {
         if (!assetsBase.endsWith("/")) 
             this.assetsBase = assetsBase + "/";
         else
             this.assetsBase = assetsBase;
     }
 
     public void setLocalAuthorityDAO(ILocalAuthorityDAO localAuthorityDAO) {
         this.localAuthorityDAO = localAuthorityDAO;
     }
 
     public void setPerformDbUpdates(Boolean performDbUpdates) {
         if (performDbUpdates != null)
             this.performDbUpdates = performDbUpdates;
         else
             this.performDbUpdates = Boolean.FALSE;
     }
 
     public void setIncludes(final String includes) {
         if (!includes.equals("") && !includes.equals("**"))
             includedLocalAuthorities = includes.split(",");
     }
 
     public void setLocalAuthoritiesListFilename(final String localAuthoritiesListFilename) {
         this.localAuthoritiesListFilename = localAuthoritiesListFilename;
     }
 
     @Override
     public boolean isPaymentEnabled() {
         DateMidnight start = null;
         DateMidnight end = null;
         if (SecurityContext.getCurrentSite().getPaymentDeactivationStartDate() != null) {
             start = new DateMidnight(SecurityContext.getCurrentSite().getPaymentDeactivationStartDate());
         }
         if (SecurityContext.getCurrentSite().getPaymentDeactivationEndDate() != null) {
             end = new DateMidnight(SecurityContext.getCurrentSite().getPaymentDeactivationEndDate()).plusDays(1);
         }
         if (start == null && end == null) return true;
         if (start != null && end == null) return start.isAfterNow();
         if (start == null && end != null) return end.isBeforeNow();
         return !new Interval(start, end).containsNow();
     }
 
     /**
      * Utility method which parses a property value as String in a BeanDefinition,
      * for on-the-fly development database creation.
      */
     private String parseJDBCProperty(BeanDefinition definition, String property) {
         return
             ((TypedStringValue)definition.getPropertyValues().getPropertyValue(property).getValue())
                 .getValue();
     }
 
     private Agent createAgent(String login, SiteProfile siteProfile)
         throws CvqException {
         Agent agent = new Agent();
         agent.setActive(Boolean.TRUE);
         agent.setLogin(login);
         agent.setFirstName(login);
         agent.setLastName(DEVELOPMENT_LOCAL_AUTHORITY);
         SiteRoles siteRoles = new SiteRoles(siteProfile, agent);
         Set<SiteRoles> siteRolesSet = new HashSet<SiteRoles>(1);
         siteRolesSet.add(siteRoles);
         agent.setSitesRoles(siteRolesSet);
         agentService.create(agent);
         return agent;
     }
 
     public void setTemplateSessionFactory(SessionFactory templateSessionFactory) {
         this.templateSessionFactory = templateSessionFactory;
     }
 
     public void setAgentService(IAgentService agentService) {
         this.agentService = agentService;
     }
 
     public void setHomeFolderService(IHomeFolderService homeFolderService) {
         this.homeFolderService = homeFolderService;
     }
 }
