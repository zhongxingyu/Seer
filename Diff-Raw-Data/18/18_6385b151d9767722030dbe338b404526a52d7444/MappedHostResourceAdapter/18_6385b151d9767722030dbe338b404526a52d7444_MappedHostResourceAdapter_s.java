 package com.ettrema.davproxy.adapter;
 
 import com.bradmcevoy.http.*;
 import com.bradmcevoy.http.exceptions.BadRequestException;
 import com.bradmcevoy.http.exceptions.ConflictException;
 import com.bradmcevoy.http.exceptions.NotAuthorizedException;
 import com.bradmcevoy.http.exceptions.NotFoundException;
 import com.ettrema.davproxy.content.FolderHtmlContentGenerator;
 import com.ettrema.httpclient.File;
 import com.ettrema.httpclient.Folder;
 import com.ettrema.httpclient.Host;
 import com.ettrema.httpclient.HttpException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Represents a remote DAV host which has been mapped onto the DAV proxy
  *
  * @author brad
  */
 public class MappedHostResourceAdapter extends AbstractRemoteAdapter implements CollectionResource, MakeCollectionableResource, PutableResource, GetableResource, PropFindableResource, DigestResource {
 
     private final com.ettrema.httpclient.Host remoteHost;
     private final FolderHtmlContentGenerator contentGenerator;
     private final RemoteManager remoteManager;
     private final String name;
     private final String hostName;
 
     public MappedHostResourceAdapter(String name, Host host, com.bradmcevoy.http.SecurityManager securityManager, String hostName, FolderHtmlContentGenerator contentGenerator, RemoteManager remoteManager) {
         super(host, securityManager, hostName);
         this.contentGenerator = contentGenerator;
         this.remoteManager = remoteManager;
         this.remoteHost = host;
         this.name = name;
         this.hostName = hostName;
     }
 
     @Override
     public String getName() {
         return name;
     }
     
     @Override
     public CollectionResource createCollection(String newName) throws NotAuthorizedException, ConflictException, BadRequestException {
         Folder newRemoteFolder;
         try {
             newRemoteFolder = remoteHost.createFolder(newName);
         } catch (IOException ex) {
             throw new RuntimeException(ex);
         } catch (NotFoundException ex) {
             throw new RuntimeException(ex);
         } catch (HttpException ex) {
             throw new RuntimeException(ex);
         }
         return new FolderResourceAdapter(newRemoteFolder, getSecurityManager(), newName, contentGenerator, remoteManager);
     }
 
     @Override
     public com.bradmcevoy.http.Resource child(String childName) throws NotAuthorizedException, BadRequestException {
         for( Resource r : getChildren() ) {
             if( r.getName().equals(childName)) {
                 return r;
             }
         }
         return null;
     }
 
     @Override
     public List<? extends com.bradmcevoy.http.Resource> getChildren() throws NotAuthorizedException, BadRequestException {
         try {
             return remoteManager.getChildren(hostName, remoteHost);
         } catch (IOException ex) {
             throw new RuntimeException(ex);
         } catch (HttpException ex) {
             throw new RuntimeException(ex);
         }
     }
 
     @Override
     public com.bradmcevoy.http.Resource createNew(String newName, InputStream inputStream, Long length, String contentType) throws IOException, ConflictException, NotAuthorizedException, BadRequestException {
         try {
            File newFile = remoteHost.upload(name, inputStream, length, null);
             return new FileResourceAdapter(newFile, getSecurityManager(), hostName, remoteManager);
         } catch (HttpException ex) {
             throw new RuntimeException(ex);
         } catch (NotFoundException ex) {
             throw new RuntimeException(ex);
         }
     }
 
     @Override
     public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {
         String uri = HttpManager.request().getAbsolutePath();
         contentGenerator.generateContent(this, out, uri);
     }
 
     @Override
     public Long getMaxAgeSeconds(Auth auth) {
         return null;
     }
 
     @Override
     public String getContentType(String accepts) {
         return "text/html";
     }
 
     @Override
     public Long getContentLength() {
         return null;
     }
 
     @Override
     public Date getCreateDate() {
         return null;
     }
 }
 
