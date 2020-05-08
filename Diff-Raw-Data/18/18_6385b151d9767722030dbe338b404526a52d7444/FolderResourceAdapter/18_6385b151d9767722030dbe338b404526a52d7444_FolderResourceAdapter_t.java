 package com.ettrema.davproxy.adapter;
 
 import com.bradmcevoy.http.*;
 import com.bradmcevoy.http.exceptions.BadRequestException;
 import com.bradmcevoy.http.exceptions.ConflictException;
 import com.bradmcevoy.http.exceptions.NotAuthorizedException;
 import com.bradmcevoy.http.exceptions.NotFoundException;
 import com.ettrema.davproxy.content.FolderHtmlContentGenerator;
import com.ettrema.httpclient.File;
 import com.ettrema.httpclient.Folder;
 import com.ettrema.httpclient.HttpException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 /**
  *
  * @author brad
  */
 public class FolderResourceAdapter extends AbstractRemoteAdapter implements FolderResource {
 
     private final com.ettrema.httpclient.Folder folder;
 
     private final FolderHtmlContentGenerator contentGenerator;
     
     private final RemoteManager remoteManager;
     
     public FolderResourceAdapter(Folder folder, com.bradmcevoy.http.SecurityManager securityManager, String hostName, FolderHtmlContentGenerator contentGenerator, RemoteManager remoteManager) {
         super(folder, securityManager, hostName);
         this.folder = folder;
         this.contentGenerator = contentGenerator;
         this.remoteManager = remoteManager;
     }
 
     @Override
     public CollectionResource createCollection(String newName) throws NotAuthorizedException, ConflictException, BadRequestException {
         Folder newRemoteFolder;
         try {
             newRemoteFolder = folder.createFolder(newName);
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
             return remoteManager.getChildren(getHostName(), folder);
         } catch (IOException ex) {
             throw new RuntimeException(ex);
         } catch (HttpException ex) {
             throw new RuntimeException(ex);
         }
     }
 
 
     @Override
     public com.bradmcevoy.http.Resource createNew(String newName, InputStream inputStream, Long length, String contentType) throws IOException, ConflictException, NotAuthorizedException, BadRequestException {
        try {
            File newFile = folder.upload(newName, inputStream, length, null);
            return new FileResourceAdapter(newFile, getSecurityManager(), getHostName(), remoteManager);
        } catch (HttpException ex) {
            throw new RuntimeException(ex);
        } catch (NotFoundException ex) {
            throw new RuntimeException(ex);
        }
     }
 
 
     @Override
     public void copyTo(CollectionResource toCollection, String destName) throws NotAuthorizedException, BadRequestException, ConflictException {
         FolderResourceAdapter destFolderAdapter = (FolderResourceAdapter) toCollection;
         Folder destRemoteFolder = destFolderAdapter.getRemoteFolder();
         remoteManager.copyTo(folder, destName, destRemoteFolder);
     }
 
     @Override
     public void moveTo(CollectionResource toCollection, String destName) throws ConflictException, NotAuthorizedException, BadRequestException {
         FolderResourceAdapter destFolderAdapter = (FolderResourceAdapter) toCollection;
         Folder destRemoteFolder = destFolderAdapter.getRemoteFolder();
         remoteManager.moveTo(folder, destName, destRemoteFolder);
     }
     
     @Override
     public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException {
         String uri = HttpManager.request().getAbsolutePath();
         contentGenerator.generateContent(this, out, uri);
     }
 
     @Override
     public Long getMaxAgeSeconds(Auth auth) {
         return null;
     }
 
     @Override
     public String getContentType(String accepts) {
         return null;
     }
 
     @Override
     public Long getContentLength() {
         return null;
     }
 
     @Override
     public Date getCreateDate() {
         return folder.getCreatedDate();
     }
     
     @Override
     public void delete() throws NotAuthorizedException, ConflictException, BadRequestException {
         try {
             folder.delete();
         } catch (NotFoundException ex) {
             return; // ok, not there to delete
         } catch (IOException ex) {
             throw new RuntimeException(ex);
         } catch (HttpException ex) {
             throw new RuntimeException(ex);
         }
     }
 
     public Folder getRemoteFolder() {
         return folder;
     }
     
     
 }
