 package org.joni.test.meta.server;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.security.cert.X509Certificate;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.StringTokenizer;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 
 import org.glite.security.util.DNHandler;
 import org.infinispan.Cache;
 import org.infinispan.manager.DefaultCacheManager;
 import org.joni.test.meta.ACLHandler;
 import org.joni.test.meta.MetaDataAPI;
 import org.joni.test.meta.MetaFile;
 import org.joni.test.meta.UserInfo;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.caucho.hessian.server.HessianServlet;
 import com.eaio.uuid.UUID;
 
 
 
 public class MetaService extends HessianServlet implements MetaDataAPI {
 
     @SuppressWarnings("unused")
     private static Logger logger = LoggerFactory.getLogger(MetaService.class);
     private static FileWriter writer;
     {
         try{
             writer = new FileWriter("testLog.txt");
         } catch (IOException e) {
             System.exit(1);
             e.printStackTrace();
         }
     }
     private static final long serialVersionUID = 1L;
     private static DefaultCacheManager cacheManager = null;
     private static Cache<UUID, MetaFile> cache = null;
     private static Cache<String, UserInfo> users = null;
     private static String _superUser = null;
     /** used to pass along the user certificate from servlet handling to the actual methods doing the work */
     private static ThreadLocal<X509Certificate[]> certStore = new ThreadLocal<X509Certificate[]>();
     public static final String CACHE_CONFIG_FILE_OPT = "cacheConfigFile";
     public static final String SUPER_USER_OPT = "superuser";
 
     public MetaService(String configFile) throws IOException {
 
         File testFile = new File(configFile);
         if (!testFile.exists()) {
             throw new FileNotFoundException("Configuration file \"" + configFile + "\" not found.");
         }
         if (testFile.isDirectory()) {
             throw new FileNotFoundException("The file \"" + configFile + "\" given as a configuration file is a directory!");
         }
 
         Properties props = new Properties();
         props.load(new FileReader(configFile));
         String cacheConfig = props.getProperty(CACHE_CONFIG_FILE_OPT);
         String superUser = props.getProperty(SUPER_USER_OPT);
         if(superUser == null){
             throw new IOException("No superuser setting found in the configuration file.");
         }
         _superUser = superUser;
         testFile = new File(cacheConfig);
         if (!testFile.exists()) {
             throw new FileNotFoundException("Storage configuration file \"" + cacheConfig + "\" not found.");
         }
         if (testFile.isDirectory()) {
             throw new FileNotFoundException("The file \"" + cacheConfig + "\" given as a storage configuration file is a directory!");
         }
         cacheManager = new DefaultCacheManager(cacheConfig);
         cache = cacheManager.getCache("meta");
         users = cacheManager.getCache("users");
     }
 
     public void service(ServletRequest request, ServletResponse response)
             throws IOException, ServletException{
         // Interpret the client's certificate.
         X509Certificate[] cert = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
         certStore.set(cert);
         super.service(request,response);
     }
 
     private String getUser() {
         // Interpret the client's certificate.
         X509Certificate[] cert = certStore.get();
         String user = DNHandler.getSubject(cert[0]).getRFCDNv2();
 
         return user;
     }
 
     @SuppressWarnings("unused")
     private boolean checkPermissionForNewRoot() {
         return _superUser.equals(getUser());
     }
 
     public void putFile(MetaFile newFile) throws IOException {
 
         if (newFile == null) {
             throw new NullPointerException("Cannot put a file that is null");
         }
         UUID id = newFile.getId();
         if (cache.get(id) != null) {
             throw new IOException("File already exists");
         }
         UUID parentId = newFile.getParent();
         if (parentId == null) {
             // override, so that root can add other users roots
             if(getUser().equals(_superUser)){
                 cache.put(id, newFile);
                 return;
             }
             UserInfo info = users.get(getUser());
             if(info == null){
                 throw new IOException("User " + getUser() + " does not exist in the system.");
             }
             List<UUID> roots = info.getRoots();
             roots.add(id);
             info.setRoots(roots);
             users.put(info.getName(), info);
             cache.put(id, newFile);
             return;
         }
         MetaFile parent = cache.get(parentId);
         if (parent == null) {
             // revert the put
             throw new FileNotFoundException("Parent file not found");
         }
         if (!ACLHandler.hasWriteAccess(getUser(), parent.getACL())) {
             throw new IOException("Access denied to directory: " + parent.getName() + " file: " + newFile.getName());
         }
         cache.put(id, newFile);
         parent.addFile(newFile);
         cache.put(parent.getId(), parent);
 
     }
 
     public void updateFile(MetaFile updatedFile) throws IOException {
 
         if (updatedFile == null) {
             throw new NullPointerException("Cannot update a file that is null");
         }
         // TODO:make threadsafe?
         // TODO:if only change is directory contents or stripe or key locations, should not need write permission to
         // parent dir.
 
         UUID id = updatedFile.getId();
         MetaFile oldFile = cache.get(id);
         if (oldFile == null) {
             throw new IOException("File does not exists");
         }
         // updating root
         if (updatedFile.getParent() == null) {
             if (oldFile.getParent() != null) {
                 throw new IOException("Cannot make any file or directory as a new root.");
             }
             if (!ACLHandler.hasWriteAccess(getUser(), oldFile.getACL())) {
                 throw new IOException("Access denied to directory: " + oldFile.getName());
             }
             cache.put(id, updatedFile);
             return;
         }
 
         if (!updatedFile.isDirectory() && !updatedFile.getParent().equals(oldFile.getParent())) {
             throw new IOException(
                     "The parent directory of old version and new version do not match. To move file, delete it from old place and put it to the new place.");
         }
 
         UUID parentId = updatedFile.getParent();
         MetaFile parent = cache.get(parentId);
         if (parent == null) {
             // parent deleted?
             throw new FileNotFoundException("Parent file not found.");
         }
         if (!ACLHandler.hasWriteAccess(getUser(), parent.getACL())) {
             throw new IOException("No write access to directory: " + parent.getName());
         }
         cache.put(id, updatedFile);
 
     }
 
     public void deleteFile(UUID id) throws IOException {
 
         if (id == null) {
             throw new NullPointerException("Cannot delete a file that is null");
         }
 
         MetaFile oldFile = cache.get(id);
         if (oldFile == null) {
             throw new FileNotFoundException("File does not exists");
         }
         UUID parentId = oldFile.getParent();
         if (parentId == null) {
             throw new IOException("Cannot delete root directory.");
         }
 
         MetaFile parent = cache.get(parentId);
         if (parent == null) {
             // revert the put
             throw new FileNotFoundException("Parent file not found");
         }
         if (!ACLHandler.hasWriteAccess(getUser(), parent.getACL())) {
             throw new IOException("Access denied to directory: " + parent.getName());
         }
         parent.removeFile(id);
         oldFile = cache.remove(id);
         // extra check
         if (oldFile == null) {
             throw new IOException("File was already removed during the operation.");
         }
 
     }
 
     public MetaFile getFileByPath(String path) throws IOException {
     	// Lets find the metafile by searching from the root
     	//System.out.println("GetFileByPath, finding path: " + path);
     	
     	if(path==null) {
     		throw new NullPointerException("Null path parameter not allowed.");
     	}else if(path.length() <= 0) {
     		throw new IOException("Empty path parameter not allowed.");
     	}
     	
     	// Get user
     	UserInfo info = users.get(getUser());
     	if(info == null) {
     		throw new IOException("User " + getUser() + " does not exist in the system.");
     	}
     	
     	// Get root directories
     	List<UUID> roots = info.getRoots();
     	if(roots == null) {
     		throw new IOException("Root not found (null).");
     	}else if(roots.size() <= 0) {
     		throw new IOException("Root not found (size zero).");
     	}
 
     	// Go through the root directories until match is found
     	MetaFile returnme = null;
     	String token = null;
     	String PATH_SEPARATOR = "/";
     	StringTokenizer st = new StringTokenizer(path, PATH_SEPARATOR);
     	//System.out.println("st: '" + path + "' and st.size =" + st.countTokens());
     	// Resolve first the root
     	if(st.hasMoreTokens()) {
     		token = st.nextToken();
     		for (UUID uuid: roots) {
     			MetaFile file = cache.get(uuid);
     			if (file.getName().equals(token)) {
     				returnme = file;
     				break;
     			}
     		}
     	}
     	if(returnme == null) {
     		//throw new FileNotFoundException("Root '" + token + "' not found");
     		return null;
     	}
         if (!ACLHandler.hasReadAccess(getUser(), returnme.getACL())) {
             throw new IOException("Access denied: '" + path + "'.");
         }
     	
     	// Find similarly rest of the path
     	while (st.hasMoreTokens()) {
             if (!ACLHandler.hasReadAccess(getUser(), returnme.getACL())) {
                 throw new IOException("Access denied: '" + path + "'.");
             }
     		List<UUID> uidlist = returnme.listFiles();
     		if (uidlist == null) {
     			throw new FileNotFoundException("Path not found: '" + path + "'.");
     		}
     		token = st.nextToken();
     		returnme = null;
     		for (UUID uuid : uidlist) {
     			MetaFile file = cache.get(uuid);
     			if (file.getName().equals(token)) {
     				returnme = file;
     				continue;
     			}
 			}
     		if(returnme == null) {
     			throw new FileNotFoundException("Path '" + token + "' not found of path '" + path + "'");
     		}
     	}
 	   	
     	return returnme;        
     }
     
     public MetaFile getFile(UUID id) throws IOException {
         if (id == null) {
             throw new NullPointerException("Cannot find a file that is null");
         }
         MetaFile file = cache.get(id);
         if (file == null) {
             throw new FileNotFoundException("File does not exists");
         }
         UUID parentId = file.getParent();
         if (parentId != null) {
             MetaFile parent = cache.get(parentId);
             if (parent == null) {
                 // revert the put
                 throw new FileNotFoundException("Parent file not found");
             }
             // TODO: requesting random files, you gain knowledge of the parent dir, should prevent...
            try {
                if (!ACLHandler.hasReadAccess(getUser(), parent.getACL())) {
                    throw new IOException("Access denied to directory: " + parent.getName());
                }
            } catch (IllegalArgumentException e){
                if (getUser() == null){
                    throw new IllegalArgumentException("When accessing: " + parent.getName() + " user was null, which should not happen.");
                }
                if (parent.getACL() == null){
                    throw new IllegalArgumentException("When accessing: " + parent.getName() + " the ACL was null, which should not happen.");
                }
                throw e;
             }
         } else {
             // root directory, return only if has rights
             if (!ACLHandler.hasReadAccess(getUser(), file.getACL())) {
                 throw new IOException("Access denied.");
             }
         }
         // has access to parent, return restricted file
         if (!ACLHandler.hasReadAccess(getUser(), file.getACL())) {
             return file.restricted();
         }
         return file;
     }
 
     public List<MetaFile> getListFile(UUID id) throws IOException {
         if (id == null) {
             throw new NullPointerException("Cannot find a file that is null");
         }
         MetaFile current = cache.get(id);
         if (current == null) {
             throw new FileNotFoundException("File not found"); 
         }
         UUID parentId = current.getParent();
         if (parentId == null) {
             // file is root and no read access, throw exception.
             if (!ACLHandler.hasReadAccess(getUser(), current.getACL()) && !getUser().equals(_superUser)) {
                 throw new IOException("Access denied.");
             }
         } else {
 
             // check parent access control
             MetaFile parent = cache.get(parentId);
             if (parent == null) {
                 throw new FileNotFoundException("Inconsistent file system, parent directory not found.");
             }
             if (!ACLHandler.hasReadAccess(getUser(), parent.getACL()) && !getUser().equals(_superUser)) {
                 throw new IOException("Access denied.");
             }
         }
 
         List<MetaFile> files = new ArrayList<MetaFile>();
         if (!ACLHandler.hasReadAccess(getUser(), current.getACL()) && !getUser().equals(_superUser)) {
             files.add(current.restricted());
             return files;
         }
 
         files.add(current);
         if (!current.isDirectory()) {
             return files;
         }
         List<UUID> subFiles = current.listFiles();
         if (subFiles == null) {
             return files;
         }
         for (UUID subId : subFiles) {
             MetaFile subFile = cache.get(subId);
             if (subFile == null) {
                 throw new FileNotFoundException("Reference to nonexistent file found");
             }
             if (!ACLHandler.hasReadAccess(getUser(), subFile.getACL()) && !getUser().equals(_superUser)) {
                 MetaFile limitedFile = subFile.restricted();
                 files.add(limitedFile);
             } else {
                 files.add(subFile);
             }
         }
 
         return files;
     }
 
     @Override
     public UserInfo getUserInfo() {
         try {
             writer.write("user is: " + getUser());
             writer.write("\nreturning: " + users.get(getUser()) + "\n");
             writer.flush();
             return users.get(getUser());
         } catch (IOException e) {
             System.exit(1);
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         return null;
     }
 
     @Override
     public void addUser(UserInfo userInfo) throws IOException {
         if (getUser().equals(_superUser) || userInfo.getName().equals(getUser())) {
             if(users.get(userInfo.getName()) != null){
                 throw new IOException("User " + userInfo.getName() + " already exists!");
             }
             users.put(userInfo.getName(), userInfo);
         }
     }
 
     @Override
     public void updateUserInfo(UserInfo userInfo) throws IOException {
         if (userInfo == null) {
             throw new NullPointerException("User info can't be null.");
         }
         if (userInfo.getName() == null) {
             throw new NullPointerException("User name can't be null.");
         }
         UserInfo oldUser = users.get(userInfo.getName());
         if (oldUser == null) {
             throw new NullPointerException("Can't update nonexistent user.");
         }
         if (getUser().equals(oldUser.getName())) {
             users.put(userInfo.getName(), userInfo);
         } else {
             if(getUser().equals(_superUser)){
                 users.put(userInfo.getName(), userInfo);
             } else {
                 throw new IOException ("Access denied.");
             }
         }
     }
 
     @Override
     public String getVersion() {
         return "0.5.0";
     }
 
     @Override
     public UserInfo getOtherUserInfo(String name) {
         if (getUser().equals(_superUser) || getUser().equals(name)) {
             UserInfo info = users.get(name);
             return info;
         }
         return null;
     }
 
 }
