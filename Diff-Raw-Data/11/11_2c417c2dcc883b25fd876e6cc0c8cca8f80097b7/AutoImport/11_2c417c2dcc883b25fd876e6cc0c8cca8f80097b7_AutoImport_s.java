 package uk.ac.ox.oucs.vle;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLConnection;
 
 import org.apache.commons.io.filefilter.DirectoryFileFilter;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.archive.api.ArchiveService;
 import org.sakaiproject.authz.api.SecurityAdvisor;
 import org.sakaiproject.authz.api.SecurityService;
 import org.sakaiproject.authz.api.SecurityAdvisor.SecurityAdvice;
 import org.sakaiproject.component.api.ServerConfigurationService;
 import org.sakaiproject.component.cover.ComponentManager;
 import org.sakaiproject.exception.IdUnusedException;
 import org.sakaiproject.exception.PermissionException;
 import org.sakaiproject.site.api.Site;
 import org.sakaiproject.site.api.SiteService;
 import org.sakaiproject.tool.api.Session;
 import org.sakaiproject.tool.api.SessionManager;
 
 /**
  * This class attempts to download some files and import the contents.
  * 
  * The configurations is a set of URLs which can be downloaded into the archive space, unpacked
  * and then imported. This is useful for populating clean databases with content. It's not designed for
  * updating existing DBs.
  * @author buckett
  *
  */
 public class AutoImport {
 
 	private final static Log LOG = LogFactory.getLog(AutoImport.class);
 
 	private ServerConfigurationService serverConfigurationService;
 	
 	private ArchiveService archiveService;
 	
 	private SiteService siteService;
 	
 	private SessionManager sessionManager;
 	
 	private Thread thread;
 	
 	public void setServerConfigurationService(
 			ServerConfigurationService serverConfigurationService) {
 		this.serverConfigurationService = serverConfigurationService;
 	}
 
 	public void setArchiveService(ArchiveService archiveService) {
 		this.archiveService = archiveService;
 	}
 
 	public void setSiteService(SiteService siteService) {
 		this.siteService = siteService;
 	}
 
 	public void setSessionManager(SessionManager sessionManager) {
 		this.sessionManager = sessionManager;
 	}
 
 	public void init() {
 		thread = new Thread(new ImportContent());
 		thread.setDaemon(true); // Don't bother the cleanup.
 		thread.start();
 	}
 
 	/**
 	 * Import the content. We do this in a thread so we don't slow down the startup.
 	 * @author buckett
 	 *
 	 */
 	public class ImportContent implements Runnable {
 
 		public void run() {
 			// The component manager also needs to have fully started up before we start this.
 			
 			String[] archives = serverConfigurationService.getStrings("import.archives");
 			String sakaiHome = serverConfigurationService.getSakaiHomePath();
 			String archiveHome = sakaiHome+"archive";
 
 			// Download and expand all the folders, we don't need Sakai yet.
 			for (String archive : archives) {
 				LOG.info("Attempting to import: "+ archive);
 				InputStream inputStream = null;
 				try {
 					URL url = new URL(archive);
 					URLConnection connection = url.openConnection();
 					connection.setRequestProperty("User-Agent", "Sakai Content Importer");
 					connection.setConnectTimeout(30);
 					connection.setReadTimeout(30);
 					// Now make the connection.
 					connection.connect();
 					inputStream = connection.getInputStream();
					ZipUtils.expandZip(inputStream, archiveHome);
 				} catch (IOException ioe) {
 					LOG.warn("Problem with "+ archive+ " "+ ioe.getMessage());
 				} finally {
 					if (inputStream != null) {
 						try {
 							inputStream.close();
 						} catch (IOException ioe){} //Ignore
 					}
 				}
 			}
			
 
 			// No idea why this is deprecated. To me this is better than depending on Spring.
 			// Don't import stuff until the component manager is started.
 			ComponentManager.waitTillConfigured();
 			
 			// Find all the folders and load them.
 			File archiveDirectory = new File(archiveHome);
 			for (File dir: archiveDirectory.listFiles((FileFilter)DirectoryFileFilter.DIRECTORY)) {
 				String siteId = dir.getName();
 				if (siteId.endsWith("-archive")) {
 					siteId = siteId.substring(0, siteId.length() - "-archive".length());
 				}
 				if (siteService.siteExists(siteId)) {
 					LOG.info("Site already exists, not importing: "+ siteId);
 				} else {
 					LOG.info("Attempting to import: "+ siteId);
 					Session currentSession = sessionManager.getCurrentSession();
 					String oldId = currentSession.getUserId();
 					String oldEid = currentSession.getUserEid();
 					try {
 						
 							
 						currentSession.setUserId("admin");
 						currentSession.setUserEid("admin");
 						archiveService.merge(dir.getName(), siteId, null);
 					} catch (Exception e) {
 						LOG.warn("Failed to import "+ dir.getAbsolutePath()+ " to "+ siteId+ " "+ e.getMessage());
 					} finally {
 						currentSession.setUserId(oldId);
 						currentSession.setUserEid(oldEid);
 					}
 				}
 			}
 			LOG.info("Import finished");
 			thread = null; // Cleanup
 		}
 		
 	}
 
 
 }
