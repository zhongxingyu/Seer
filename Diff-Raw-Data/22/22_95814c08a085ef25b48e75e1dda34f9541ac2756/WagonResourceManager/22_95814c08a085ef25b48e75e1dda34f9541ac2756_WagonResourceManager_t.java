 /**
  *  Copyright (C) 2009 Progress Software, Inc. All rights reserved.
  *  http://fusesource.com
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package org.fusesource.meshkeeper.distribution.repository.wagon;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.maven.wagon.ResourceDoesNotExistException;
 import org.apache.maven.wagon.TransferFailedException;
 import org.apache.maven.wagon.Wagon;
 import org.apache.maven.wagon.authorization.AuthorizationException;
 import org.apache.maven.wagon.repository.Repository;
 import org.apache.maven.wagon.shared.http.AbstractHttpClientWagon;
 import org.apache.maven.wagon.shared.http.HttpConfiguration;
 import org.apache.maven.wagon.shared.http.HttpMethodConfiguration;
 import org.fusesource.meshkeeper.AuthenticationInfo;
 import org.fusesource.meshkeeper.MeshArtifact;
 import org.fusesource.meshkeeper.MeshRepository;
 import org.fusesource.meshkeeper.distribution.repository.AbstractRepositoryClient;
 import org.fusesource.meshkeeper.util.internal.FileSupport;
 
 /**
  * RepositoryManager
  * <p>
  * Description:
  * </p>
  * 
  * @author cmacnaug
  * @version 1.0
  */
 public class WagonResourceManager extends AbstractRepositoryClient {
 
     private static Log log = LogFactory.getLog(AbstractRepositoryClient.class);
 
     private static final HashMap<String, Class<? extends Wagon>> wagonProviders = new HashMap<String, Class<? extends Wagon>>();
 
     private HashMap<String, Wagon> connectedRepos = new HashMap<String, Wagon>();
 
     static {
         registerWagonClass("file", "org.apache.maven.wagon.providers.file.FileWagon");
         registerWagonClass("ftp", "org.apache.maven.wagon.providers.ftp.FtpWagon");
         registerWagonClass("http", "org.apache.maven.wagon.providers.http.HttpWagon");
         registerWagonClass("dav", "org.apache.maven.wagon.providers.webdav.WebDavWagon");
         registerWagonClass("scp", "org.apache.maven.wagon.providers.ssh.jsch.ScpWagon");
     }
 
     @SuppressWarnings("unchecked")
     private static void registerWagonClass(String protocol, String classname) {
         try {
             Class<? extends Wagon> clazz = (Class<? extends Wagon>) Thread.currentThread().getContextClassLoader().loadClass(classname);
             wagonProviders.put(protocol, clazz);
         } catch (Exception e) {
             log.warn("Error loading provider class for " + protocol, e);
         }
 
     }
 
     /**
      * Factory method for creating a resource.
      * 
      * @return An empty resource.
      */
     public MeshArtifact createArtifact() {
         return new WagonResource();
     }
 
     public void setLocalRepoDir(String localRepoDir) throws Exception {
         File dir = new File(localRepoDir);
         if (!dir.exists()) {
             dir.mkdirs();
         }
         registerRepository(createRepository(LOCAL_REPOSITORY_ID, dir.toURI().toString(), true, null));
     }
 
     public File getLocalRepoDirectory() {
         MeshRepository repo;
         try {
             repo = getRepository(LOCAL_REPOSITORY_ID);
             if (repo != null) {
                 return repo.getBaseDirectory();
             }
         } catch (Exception e) {
         }
 
         return null;
     }
 
     public void setCentralRepoUri(String url, AuthenticationInfo authInfo) throws Exception {
         connectWagon(createRepository(CENTRAL_REPOSITORY_ID, url, false, authInfo));
     }
 
     public MeshArtifact downloadArtifact(MeshArtifact resource, String repositoryId) throws Exception {
         Wagon target = connectWagon(repositoryId);
 
         Wagon source = null;
         long timestamp = 0;
         if (target.resourceExists(resource.getRepositoryPath())) {
             timestamp = new File(target.getRepository().getBasedir() + File.separator + resource.getRepositoryPath()).lastModified();
         }
 
         synchronized (this) {
             source = connectedRepos.get(resource.getRepositoryId());
             if (source == null) {
                 source = connectWagon(resource.getRepositoryId());
             }
         }
 
        log.debug("Downloading " + resource.getRepositoryPath() + " from " + source.getRepository().getUrl());
         if (source != null && source.resourceExists(resource.getRepositoryPath())) {
 
             String sourceDir = resource.getRepositoryPath();
             if (resource.getType() == MeshArtifact.DIRECTORY) {
                 if (!sourceDir.endsWith("/")) {
                     sourceDir = sourceDir + "/";
                 }
             } else {
                 sourceDir = new File(target.getRepository().getBasedir(), resource.getRepositoryPath()).getParent();
             }
 
             try {
                 if (resource.getType() == MeshArtifact.DIRECTORY) {
                    
                    downloadDirectory(source, new File(target.getRepository().getBasedir()), sourceDir, timestamp);
                 } else {
                     source.getIfNewer(resource.getRepositoryPath(), new File(target.getRepository().getBasedir(), resource.getRepositoryPath()), timestamp);
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             }
         } else if (timestamp == 0) {
             throw new Exception("Resource not found: " + resource.getRepositoryPath());
         }
 
         resource.setLocalPath(target.getRepository().getBasedir() + File.separator + resource.getRepositoryPath());
         return resource;
     }
 
     /**
      * @param resource
      * @param data
      * @throws IOException
      */
     public void deployFile(MeshArtifact resource, byte[] data) throws Exception {
         // TODO Auto-generated method stub
         File f = File.createTempFile("tmp", "dat");
         FileOutputStream fw = new FileOutputStream(f);
         fw.write(data);
         fw.flush();
         fw.close();
         try {
             deployResource(resource, f);
         } finally {
             f.delete();
         }
     }
 
     public void deployDirectory(MeshArtifact resource, File d) throws Exception {
         deployResource(resource, d);
     }
 
     private void deployResource(MeshArtifact resource, File f) throws Exception {
         Wagon w = null;
         synchronized (this) {
             w = connectedRepos.get(resource.getRepositoryId());
             if (w == null) {
                 w = connectWagon(resource.getRepositoryId());
             }
         }
         w.put(f, resource.getRepositoryPath());
     }
 
     private Wagon connectWagon(String repositoryId) throws Exception {
         MeshRepository repo = getRepository(repositoryId);
         if (repo == null) {
             throw new Exception("MeshRepository with id " + repositoryId + " was not found.");
         }
         return connectWagon(repo);
     }
 
     private Wagon connectWagon(MeshRepository meshRepo) throws Exception {
         Wagon w = connectedRepos.get(meshRepo.getRepositoryId());
         if (w != null) {
             return w;
         }
         Repository repo = new Repository(meshRepo.getRepositoryId(), meshRepo.getRepositoryUri());
         Class<? extends Wagon> wagonClass = wagonProviders.get(repo.getProtocol());
         if (wagonClass == null) {
             throw new Exception("Unsupported repository protocol" + repo.getProtocol());
         }
         w = wagonClass.newInstance();
 
         if (w instanceof AbstractHttpClientWagon) {
             // Override the default http configuration since it erroneously sets
             // Accept Encoding: gzip, then barfs when it doesn't check for it.
             HttpConfiguration hc = new HttpConfiguration();
             HttpMethodConfiguration hmc = new HttpMethodConfiguration();
             hmc.setUseDefaultHeaders(false);
             hmc.addHeader("Cache-control", "no-cache");
             hmc.addHeader("Cache-store", "no-store");
             hmc.addHeader("Pragma", "no-cache");
             hmc.addHeader("Expires", "0");
             hc.setAll(hmc);
             ((AbstractHttpClientWagon) w).setHttpConfiguration(hc);
         }
 
         w.connect(repo, convertAuthInfo(meshRepo.getAuthenticationInfo()));
         connectedRepos.put(repo.getName(), w);
         return w;
     }
 
     @SuppressWarnings("unchecked")
    private static final void downloadDirectory(Wagon source, File targetDir, String path, long timestamp) throws Exception {
         Iterator<String> i = (Iterator<String>) source.getFileList(path).iterator();
        
         if (!i.hasNext()) {
             File target = new File(targetDir, path);
             target.mkdirs();
         } else {
             while (i.hasNext()) {
                 String file = i.next();
                 if (file.endsWith("/")) {
                    downloadDirectory(source, targetDir, path + file, timestamp);
                 } else {
                    downloadFile(source, targetDir, path + file, timestamp);
                 }
             }
         }
     }
 
    private static final void downloadFile(Wagon source, File targetDir, String name, long timestamp) throws IOException, TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
         File target = new File(targetDir, name);
        source.getIfNewer(name, new File(targetDir, name), timestamp);
         // Empty files may not get created, so make sure that they are created
         // here.
         if (!target.exists()) {
             target.createNewFile();
         }
     }
 
     public void purgeLocalRepo() throws IOException {
         File local = getLocalRepoDirectory();
         if (local != null) {
             FileSupport.recursiveDelete(local);
         }
     }
 
     /**
      * Closes all repository connections.
      * 
      * @throws Exception
      */
     public void start() {
         // No-op:
     }
 
     /**
      * Closes all repository connections.
      * 
      * @throws Exception
      */
     public void destroy() {
         for (Wagon w : connectedRepos.values()) {
             try {
                 w.disconnect();
             } catch (Throwable t) {
                 t.printStackTrace();
             }
         }
         connectedRepos.clear();
     }
 
     private static org.apache.maven.wagon.authentication.AuthenticationInfo convertAuthInfo(AuthenticationInfo info) {
         if (info == null) {
             return null;
         } else {
             org.apache.maven.wagon.authentication.AuthenticationInfo rc = new org.apache.maven.wagon.authentication.AuthenticationInfo();
             rc.setPassphrase(info.getPassphrase());
             rc.setPassword(info.getPassword());
             rc.setPrivateKey(info.getPrivateKey());
             rc.setUserName(info.getUserName());
             return rc;
         }
     }
 
 }
