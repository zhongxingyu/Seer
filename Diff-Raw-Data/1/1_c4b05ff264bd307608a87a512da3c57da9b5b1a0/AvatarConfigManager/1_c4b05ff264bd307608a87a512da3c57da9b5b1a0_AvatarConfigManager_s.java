 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * Sun designates this particular file as subject to the "Classpath" 
  * exception as provided by Sun in the License file that accompanied 
  * this code.
  */
 package org.jdesktop.wonderland.modules.avatarbase.client;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.Semaphore;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.xml.bind.JAXBException;
 import org.jdesktop.wonderland.client.ClientContext;
 import org.jdesktop.wonderland.client.cell.view.ViewCell;
 import org.jdesktop.wonderland.client.comms.SessionStatusListener;
 import org.jdesktop.wonderland.client.comms.WonderlandSession;
 import org.jdesktop.wonderland.client.comms.WonderlandSession.Status;
 import org.jdesktop.wonderland.client.login.ServerSessionManager;
 import org.jdesktop.wonderland.common.ThreadManager;
 import org.jdesktop.wonderland.modules.avatarbase.client.AvatarConfigManager.Job.JobType;
 import org.jdesktop.wonderland.modules.avatarbase.client.cell.AvatarConfigComponent;
 import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.WlAvatarCharacter;
 import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepository;
 import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepositoryRegistry;
 import org.jdesktop.wonderland.modules.contentrepo.common.ContentCollection;
 import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode;
 import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode.Type;
 import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;
 import org.jdesktop.wonderland.modules.contentrepo.common.ContentResource;
 
 /**
  * Manages the various avatar configurations a user may have.
  *
  * The local avatar (ie on local disk) are the ones presented to the user
  * for selection.
  *
  * Each avatar file is versioned in the filename using the _[number] postfix
  *
  * When the user connects to a server the system will ensure that the latest
  * versions of avatars are uploaded to the server. It will also download any
  * new files from the server.
  *
  * @author paulby
  */
 public class AvatarConfigManager {
 
     private final HashMap<ServerSessionManager, ServerSyncThread> avatarConfigServers = new HashMap();
     private ContentCollection localAvatarsDir;
 
     private static final String extension=".xml";
 
     private final HashMap<String, AvatarConfigFile> localAvatars = new HashMap();
 
     private final ArrayList<AvatarManagerListener> listeners = new ArrayList();
 
     private static AvatarConfigManager avatarConfigManager=null;
 
     private AvatarConfigSettings configSettings = new AvatarConfigSettings();
 
     private final static String CONFIG_FILENAME = ".AvatarSettings";
     private ContentCollection localContent = null;
 
     private static final Logger logger = Logger.getLogger(AvatarConfigManager.class.getName());
 
     AvatarConfigManager() {
         logger.setLevel(Level.ALL);
         localContent = ContentRepositoryRegistry.getInstance().getLocalRepository();
         try {
             localAvatarsDir = (ContentCollection) localContent.getChild("avatars");
             if (localAvatarsDir==null) {
                 localAvatarsDir = (ContentCollection) localContent.createChild("avatars", Type.COLLECTION);
             }
 
             List<ContentNode> avatarList = localAvatarsDir.getChildren();
             for(ContentNode a : avatarList) {
                 if (a instanceof ContentResource) {
                     AvatarConfigFile acf = new AvatarConfigFile((ContentResource) a);
                     localAvatars.put(acf.avatarName, acf);
 //                    System.err.println(acf.avatarName+"  "+acf.resource.getURL());
                 }
             }
         } catch (ContentRepositoryException ex) {
             Logger.getLogger(AvatarConfigManager.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         loadConfigSettings();
         logger.info("DEFAULT AVATAR "+configSettings.getDefaultAvatarConfig());
     }
 
     /**
      * Get the url for the named avatar config, returns null if no such config
      * exists.
      *
      * @param name
      * @return
      */
     public URL getNamedAvatarURL(String name) {
         AvatarConfigFile c = localAvatars.get(name);
 
         if (c==null)
             return null;
 
         try {
             return c.resource.getURL();
         } catch (ContentRepositoryException ex) {
             Logger.getLogger(AvatarConfigManager.class.getName()).log(Level.SEVERE, null, ex);
         }
         return null;
     }
 
     public URL getNamedAvatarServerURL(String name, ServerSessionManager session) {
         ServerSyncThread syncThread = avatarConfigServers.get(session);
         if (syncThread==null) {
             throw new RuntimeException("No SyncThread for server session");
         }
         return syncThread.getNamedAvatarServerURL(name);
     }
 
     /**
      * Return the server URL for the default avatar for the specified server.
      * This call blocks until the config file is available on the server
      * 
      * @param session
      * @return
      */
     public URL getDefaultAvatarServerURL(ServerSessionManager session) {
         String defaultAvatarName = configSettings.getDefaultAvatarConfig();
         logger.info("LOOKING FOR DEFAULT "+defaultAvatarName);
         if (defaultAvatarName==null)
             return null;
 
         ServerSyncThread s = avatarConfigServers.get(session);
         if (s==null) {
             logger.info("-----> NO SERVER SYNC THREAD ");
             return null;
         }
         return s.getNamedAvatarServerURL(defaultAvatarName);
     }
 
     public void setDefaultAvatarName(String defaultAvatar) {
         configSettings.setDefaultAvatarConfig(defaultAvatar);
         saveConfigSettings();
     }
 
     public String getDefaultAvatarName() {
         return configSettings.getDefaultAvatarConfig();
     }
 
     public static AvatarConfigManager getAvatarConigManager() {
         if (avatarConfigManager==null)
             avatarConfigManager = new AvatarConfigManager();
         return avatarConfigManager;
     }
 
     public void addAvatarManagerListener(AvatarManagerListener listener) {
         synchronized(listeners) {
             listeners.add(listener);
         }
     }
 
     public void setViewCell(final ViewCell newViewCell) {
         // Sometimes setViewCell is called before addServer so wait until the
         // server is in the avatarConfigServers map.
         Thread t = new Thread() {
             public void run() {
                 boolean ready = false;
                 
                 while(!ready) {
                     synchronized(avatarConfigServers) {
                         if (avatarConfigServers.containsKey(newViewCell.getCellCache().getSession().getSessionManager()))
                             ready=true;
                     }
 
                     if (!ready) {
                         try {
                             Thread.sleep(100);
                         } catch (InterruptedException ex) {
                             Logger.getLogger(AvatarConfigManager.class.getName()).log(Level.SEVERE, null, ex);
                         }
                     }
                 }
                 // Load the users default avatar
                 AvatarConfigComponent configComponent = newViewCell.getComponent(AvatarConfigComponent.class);
                 URL selectedURL = AvatarConfigManager.getAvatarConigManager().getDefaultAvatarServerURL(newViewCell.getCellCache().getSession().getSessionManager());
                 logger.info("APPLY "+selectedURL);
                 if (selectedURL!=null) {
                     configComponent.requestConfigChange(selectedURL);
                 }
             }
         };
         t.start();
     }
 
     private void saveConfigSettings() {
         try {
             ContentResource f = (ContentResource) localContent.getChild(CONFIG_FILENAME);
             if (f==null)
                 f = (ContentResource) localContent.createChild(CONFIG_FILENAME, Type.RESOURCE);
 
             ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(byteStream);
             
             out.writeObject(configSettings);
             out.close();
             f.put(byteStream.toByteArray());
         } catch (Exception ex) {
             Logger.getLogger(AvatarConfigManager.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     private void loadConfigSettings() {
         try {
             ContentResource f = (ContentResource) localContent.getChild(CONFIG_FILENAME);
             if (f!=null) {
                 ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(f.getInputStream()));
                 configSettings = (AvatarConfigSettings) in.readObject();
                 in.close();
             }
         } catch (Exception ex) {
             Logger.getLogger(AvatarConfigManager.class.getName()).log(Level.WARNING, "Unable to load avatar config settings", ex);
             configSettings = new AvatarConfigSettings();
         }
 
     }
 
     private void notifyListeners(boolean added, String name) {
         for(AvatarManagerListener l : listeners) {
             if (added)
                 l.avatarAdded(name);
             else
                 l.avatarRemoved(name);
         }
     }
 
     private int getAvatarVersion(String filename) {
         int underscore = filename.lastIndexOf('_');
         int ext = filename.lastIndexOf('.');
 
         if (underscore==-1 || ext==-1)
             return -1;
 
         String verStr = filename.substring(underscore+1, ext);
 
         try {
             return Integer.parseInt(verStr);
         } catch(NumberFormatException e) {
             return -1;
         }
     }
 
     public void addServer(ServerSessionManager session) {
         if (avatarConfigServers.containsKey(session))
             return;
 
         try {
             ServerSyncThread t = new ServerSyncThread(session);
             avatarConfigServers.put(session, t);
             t.scheduleSync();
         } catch (ContentRepositoryException ex) {
             Logger.getLogger(AvatarConfigManager.class.getName()).log(Level.SEVERE, null, ex);
         }
 
 
     }
 
     public Iterable<String> getAvatars() {
         synchronized(localAvatars) {
             LinkedList<String> ret = new LinkedList();
             for(AvatarConfigFile f : localAvatars.values())
                 ret.add(f.avatarName);
             return ret;
         }
     }
 
     public void deleteAvatar(String avatarName) {
         synchronized(localAvatars) {
             AvatarConfigFile f = localAvatars.get(avatarName);
             if (f==null) {
                 Logger.getAnonymousLogger().warning("Unable to delete avatar, does not exist "+avatarName);
                 return;
             }
 
             localAvatars.remove(avatarName);
             notifyListeners(false, avatarName);
             try {
                 logger.info("REMOVING LOCAL");
                 localAvatarsDir.removeChild(f.getFilename());
 
                 logger.info("REMOVE FROM SERVERS");
                 // This is not quite correct as it will not remove older versions of a file
                 synchronized(avatarConfigServers) {
                     for(ServerSyncThread c : avatarConfigServers.values()) {
                         c.scheduleDelete(f.getFilename());
                     }
                 }
             } catch (ContentRepositoryException ex) {
                 Logger.getLogger(AvatarConfigManager.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 
     /**
      * Save the supplied avatar into the repository with the specified name
      * 
      * @param avatarName
      * @param avatar
      * @throws org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException
      * @throws java.io.IOException
      */
     public void saveAvatar(String avatarName, WlAvatarCharacter avatar) throws ContentRepositoryException, IOException {
         AvatarConfigFile existing = localAvatars.get(avatarName);
         boolean newFile = false;
         if (existing==null) {
             existing = new AvatarConfigFile(avatarName, 1);
             newFile = true;
         } else {
             // Increase version number
             existing.incrementVersion();
         }
 
         ContentResource file = (ContentResource) localAvatarsDir.createChild(existing.getFilename(), Type.RESOURCE);
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         try {
             avatar.saveConfiguration(out);
         } catch (JAXBException ex) {
             Logger.getLogger(AvatarConfigManager.class.getName()).log(Level.SEVERE, null, ex);
             throw new IOException(ex.getMessage());
         }
         out.close();
         file.put(out.toByteArray());
         existing.setResource(file);
 
         if (newFile) {
             synchronized(localAvatars) {
                 localAvatars.put(existing.avatarName, existing);
                 notifyListeners(true, existing.avatarName);
             }
         }
 
         // Copy file to server
         synchronized(avatarConfigServers) {
             for(ServerSyncThread t : avatarConfigServers.values()) {
                 t.scheduleUpload(existing);
                 if (!newFile)
                     t.scheduleDelete(existing.getPreviousVersionFilename());
             }
         }
     }
 
     /**
      * Check if an avatar with the specified name exists
      */
     public boolean exists(String avatarName) {
         synchronized(localAvatars) {
             return localAvatars.containsKey(avatarName);
         }
     }
 
     /**
      * Return the directory which contains the avatar co
      * @return
      */
     public static File getAvatarConfigDir() {
         File userDir = ClientContext.getUserDirectory();
         File avatarDir = new File(userDir, "avatars");
         if (!avatarDir.exists())
             avatarDir.mkdir();
         return avatarDir;
     }
 
     /**
      * Returns the default avatar config file. The file may
      * or may not exist.
      * @return
      */
     public static File getDefaultAvatarConfigFile() {
         return new File(getAvatarConfigDir(), "avatar_config_1.xml");
     }
 
     class AvatarConfigFile {
         ContentResource resource;
         int version;
         String avatarName;      // Stripped filename
 
         public AvatarConfigFile(ContentResource resource) {
             this.resource = resource;
             version = getAvatarVersion(resource.getName());
             int i = resource.getName().lastIndexOf('_');
             if (i==-1)
                 avatarName = resource.getName();
             else
                 avatarName = resource.getName().substring(0, resource.getName().lastIndexOf('_'));
         }
 
         public AvatarConfigFile(String avatarName, int version) {
             this.avatarName = avatarName;
             this.version = version;
             this.resource = null;
         }
 
         public String toString() {
             try {
                 return avatarName + " : " + version + "  " + (resource == null ? "null" : resource.getURL().toExternalForm());
             } catch (ContentRepositoryException ex) {
                 return avatarName + " : " + version + "  nullURL";
             }
         }
 
         public void setResource(ContentResource resource) {
             this.resource = resource;
         }
 
         /**
          * Return the filename of the config, without the path
          * @return
          */
         public String getFilename() {
             return avatarName+"_"+version+extension;
         }
 
         public String getPreviousVersionFilename() {
             return avatarName+"_"+(version-1)+extension;
         }
 
         public void incrementVersion() {
             version++;
         }
     }
 
     public interface AvatarManagerListener {
         public void avatarAdded(String name);
 
         public void avatarRemoved(String name);
     }
 
     class ServerSyncThread extends Thread {
 
         private LinkedBlockingQueue<Job> jobQueue = new LinkedBlockingQueue();
 
         private ContentRepository repository;
         private ContentCollection avatarsDir;
         private HashMap<String, AvatarConfigFile> serverAvatars = new HashMap();
         private boolean connected = true;
 
         public ServerSyncThread(ServerSessionManager session) throws ContentRepositoryException {
             super(ThreadManager.getThreadGroup(), "AvatarServerSyncThread");
             logger.info("SERVER SYNC "+this);
             repository = ContentRepositoryRegistry.getInstance().getRepository(session);
             ContentCollection userDir = repository.getUserRoot(true);
             avatarsDir = (ContentCollection) userDir.getChild("avatars");
             if (avatarsDir == null) {
                 avatarsDir = (ContentCollection) userDir.createChild("avatars", Type.COLLECTION);
             }
             synchronized (avatarConfigServers) {
                 avatarConfigServers.put(session, this);
             }
             session.getPrimarySession().addSessionStatusListener(new SessionStatusListener() {
 
                 public void sessionStatusChanged(WonderlandSession session, Status status) {
                     if (status == Status.DISCONNECTED) {
                         synchronized (avatarConfigServers) {
                             avatarConfigServers.remove(session);
                             connected = false;
                         }
                     }
                 }
             });
             this.start();
         }
 
         public void run() {
             while(connected) {
                 try {
                     Job job = jobQueue.take();
                     switch(job.getJobType()) {
                         case SYNC :
                             syncImpl();
                             break;
                         case DELETE :
                             deleteImpl(job);
                             break;
                         case UPLOAD:
                             uploadFileImpl(job);
                             break;
                         case GETURL :
                             getURLImpl(job);
                             break;
                     }
                 } catch (InterruptedException ex) {
                     Logger.getLogger(AvatarConfigManager.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         }
 
         public void scheduleSync() {
             jobQueue.add(Job.newSyncJob());
         }
 
         public void scheduleDelete(String filename) {
             jobQueue.add(Job.newDeleteJob(filename));
         }
 
         public void scheduleUpload(AvatarConfigFile upload) {
             System.err.println("Scheduling update "+upload);
             jobQueue.add(Job.newUploadJob(upload));
         }
 
         private URL getNamedAvatarServerURL(String name) {
             Job job = Job.newGetURLJob(name);
             jobQueue.add(job);
 
             return job.getURL();
         }
 
         private void deleteImpl(Job job) {
             avatarConfigServers.remove(job.filename);
         }
 
         private void getURLImpl(Job job) {
             System.err.println("List size "+serverAvatars.size()+"  "+job.filename);
             for(AvatarConfigFile f : serverAvatars.values())
                 System.err.println(f);
             
             AvatarConfigFile r = serverAvatars.get(job.filename);
             if (r==null) {
                 System.err.println(this);
                 Logger.getLogger(AvatarConfigManager.class.getName()).log(Level.SEVERE, "No record of avatar on server "+job.filename);
                 job.returnURL(null);
             }
             try {
                 job.returnURL(r.resource.getURL());
             } catch (ContentRepositoryException ex) {
                 Logger.getLogger(AvatarConfigManager.class.getName()).log(Level.WARNING, "Unable to find avatar "+job.filename, ex);
             }
         }
 
 
         private void syncImpl() {
             ArrayList<AvatarConfigFile> uploadList = new ArrayList();
             ArrayList<AvatarConfigFile> downloadList = new ArrayList();
             
             try {
                 List<ContentNode> avatarList = avatarsDir.getChildren();
                 for(ContentNode a : avatarList) {
                     if (a instanceof ContentResource) {
                         AvatarConfigFile serverAvatar =  new AvatarConfigFile((ContentResource)a);
                         AvatarConfigFile previous = serverAvatars.put(serverAvatar.avatarName, serverAvatar);
                         if (previous!=null && previous.version>serverAvatar.version) {
                             serverAvatars.put(previous.avatarName, previous);
                             System.err.println("REMOVING OLD AVATAR CONFIG "+serverAvatar.getFilename());
                             avatarsDir.removeChild(serverAvatar.getFilename());
                         }
                     }
                 }
 
                 HashMap<String, AvatarConfigFile> tmpServerAvatars =  (HashMap<String, AvatarConfigFile>) serverAvatars.clone();
 
                 synchronized(localAvatars) {
                     for(AvatarConfigFile a : localAvatars.values()) {
                         AvatarConfigFile serverVersion = tmpServerAvatars.get(a.avatarName);
                         logger.fine("Comparing "+serverVersion+"   "+a);
                         if (serverVersion==null || serverVersion.version<a.version) {
                             uploadList.add(a);
 //                            System.err.println("Uploading "+a);
                             tmpServerAvatars.remove(a.avatarName);
                         } else if (serverVersion.version>a.version) {
                             downloadList.add(a);
 //                            System.err.println("Downloading "+a);
                             tmpServerAvatars.remove(a.avatarName);
                         } else if (serverVersion.version == a.version) {
                             tmpServerAvatars.remove(a.avatarName);
                         }
                     }
                 }
 
                 // Avatars left in the serverAvatars set are only on the server, so add them to download list
                 for(AvatarConfigFile a : tmpServerAvatars.values()) {
 //                    System.err.println("Downloading "+a);
                     downloadList.add(a);
                 }
 
 
                 // Do the actual upload
                 for(AvatarConfigFile a : uploadList) {
                     try {
                         uploadFileImpl(a);
                         serverAvatars.put(a.avatarName, a);
                     } catch (IOException ex) {
                         Logger.getLogger(AvatarConfigManager.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 }
 
                 // Now do the actual downloads
                 for(AvatarConfigFile a : downloadList) {
                     try {
                         ContentResource localFile = (ContentResource)localAvatarsDir.createChild(a.resource.getName(), Type.RESOURCE);
                         localFile.put(new BufferedInputStream(a.resource.getURL().openStream()));
                     } catch (IOException ex) {
                         Logger.getLogger(AvatarConfigManager.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 }
 
                 // Put all the files we downloaded into the localAvatars hash map
                 synchronized(localAvatars) {
                     for(AvatarConfigFile a : downloadList) {
                         localAvatars.put(a.avatarName, a);
                         notifyListeners(true, a.avatarName);
                     }
                 }
 
 
             } catch (ContentRepositoryException ex) {
                 Logger.getLogger(AvatarConfigManager.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
 
         private void uploadFileImpl(Job job) {
             try {
                 uploadFileImpl(job.avatarConfigFile);
             } catch (IOException ex) {
                 Logger.getLogger(AvatarConfigManager.class.getName()).log(Level.SEVERE, null, ex);
             } catch (ContentRepositoryException ex) {
                 Logger.getLogger(AvatarConfigManager.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
 
         private void uploadFileImpl(AvatarConfigFile upload) throws IOException, ContentRepositoryException {
             ContentResource serverFile = (ContentResource)avatarsDir.createChild(upload.resource.getName(), Type.RESOURCE);
             serverFile.put(new BufferedInputStream(upload.resource.getURL().openStream()));
             System.err.println("UPLOADED "+upload);
             serverAvatars.put(upload.avatarName, upload);
         }
 
     }
 
     static class Job {
         private URL url;
         public enum JobType { SYNC, DELETE, UPLOAD, GETURL };
 
         private JobType jobType;
         private String filename;
         private AvatarConfigFile avatarConfigFile;
 
         private Semaphore jobDone;
 
         private Job(JobType jobType, String fileToDelete, AvatarConfigFile uploadFile) {
             this.jobType = jobType;
             this.filename = fileToDelete;
             this.avatarConfigFile = uploadFile;
 
             if (jobType==JobType.GETURL)
                 jobDone = new Semaphore(0);
         }
 
         public static Job newSyncJob() {
             return new Job(JobType.SYNC, null, null);
         }
 
         public static Job newDeleteJob(String filename) {
             return new Job(JobType.DELETE, filename, null);
         }
 
         public static Job newUploadJob(AvatarConfigFile upload) {
             return new Job(JobType.UPLOAD, null, upload);
         }
 
         public static Job newGetURLJob(String filename) {
             return new Job(JobType.GETURL, filename, null);
         }
 
         public JobType getJobType() {
             return jobType;
         }
 
         public void returnURL(URL url) {
             System.err.println("JOB got "+url);
             this.url = url;
             jobDone.release();
         }
 
         public URL getURL() {
             try {
                 jobDone.acquire();
             } catch (InterruptedException ex) {
                 Logger.getLogger(AvatarConfigManager.class.getName()).log(Level.SEVERE, null, ex);
             }
             return url;
         }
     }
 }
