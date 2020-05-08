 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.javavfs.impl.nativefs;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URI;
 import java.security.Principal;
 import java.util.Date;
 import org.javavfs.Directory;
 import org.javavfs.FileSystemSession;
 import org.javavfs.Node;
 import org.javavfs.Path;
 
 /**
  *
  * @author mzk
  */
 public abstract class NativeNode implements Node{
 
     public NativeNode(NativeFileSystemSession session, java.io.File file) {
         this.session=session;
         this.file=file;
     }
     
     protected NativeFileSystemSession session;
     protected java.io.File file;
 
     protected Principal getPrincipal(){
         return session.getPrincipal();
     }
     private void deleteNonRecursive(File file) throws IOException{
         session.getFileSystem().getSecurity().checkWrite(getPrincipal(), this);
         
         boolean deleted = file.delete();
         if(!deleted && file.isDirectory())
             throw new IOException("Could not delete directory. Maybe because it is not empty.[path="+file.toString()+"]");
         else if(!deleted)
             throw new IOException("Could not delete file. [path="+file.toString()+"]");
     }
     
     private void deleteRecursive(File file) throws IOException{
         if(!file.isDirectory())
             throw new IOException("A file cannot be deleted recursively.");
         
         if(file.exists()){
             File[] files = file.listFiles();
             for(File currentFile:files){
                 if(currentFile.isDirectory())
                     deleteRecursive(currentFile);
                 else
                     deleteNonRecursive(currentFile);
             }
             deleteNonRecursive(file);
         }
     }
     
     public boolean isRoot(){
         try{
             return session.getRoot().equals(this);
         } catch(FileNotFoundException ex){
             
         }
         return false;
     }
     
     public void delete(boolean recursive) throws IOException {
         if(isRoot())
             throw new IOException("Cannot delete root folder.");
         
         if(recursive)
             deleteRecursive(file);
         else
             deleteNonRecursive(file);
         
     }
 
     public void moveTo(Directory newParent) throws IOException {
         session.getFileSystem().getSecurity().checkWrite(getPrincipal(), this);
         session.getFileSystem().getSecurity().checkWrite(getPrincipal(), newParent);
         
         java.io.File newParentFile = new java.io.File(newParent.toUri());
         java.io.File newDestination = new java.io.File(newParentFile,file.getName());
         boolean moved = file.renameTo(newDestination);
         if(!moved)
             throw new IOException("Could not move the "+(file.isDirectory()?"directory":"file")+".");
     }
 
     public void moveTo(Directory newParent, String newName) throws IOException {
         session.getFileSystem().getSecurity().checkWrite(getPrincipal(), this);
         session.getFileSystem().getSecurity().checkWrite(getPrincipal(), newParent);
         
         java.io.File newParentFile = new java.io.File(newParent.toUri());
         java.io.File newDestination = new java.io.File(newParentFile,newName);
         boolean moved = file.renameTo(newDestination);
         if(!moved)
             throw new IOException("Could not move the "+(file.isDirectory()?"directory":"file")+".");
     }
 
     public void setName(String name) throws IOException {
         session.getFileSystem().getSecurity().checkWrite(getPrincipal(), this);
         
         java.io.File newDestination = new java.io.File(file.getParentFile(),name);
         boolean moved = file.renameTo(newDestination);
         if(!moved)
             throw new IOException("Could not rename the "+(file.isDirectory()?"directory":"file")+".");
     }
 
     public String getName() {
         return file.getName();
     }
 
     public Directory getParent() throws FileNotFoundException {
         if(isRoot())
             return null;
         return new NativeDirectory(session, file.getParentFile());
     }
 
     public FileSystemSession getFileSystem() {
         return session;
     }
     
  public boolean isDirectory() {
         return file.isDirectory();
     }
 
     public boolean isHidden() {
         return file.isHidden();
     }
 
     public Date getLastModified() {
         return new Date(file.lastModified());
     }
 
     public void setLastModified(Date date) {
         session.getFileSystem().getSecurity().checkWrite(getPrincipal(), this);
         file.setLastModified(date.getTime());
     }
 
     public int compareTo(Node node) {
         return this.getName().compareTo(node.getName());
     }
 
     public void delete() throws IOException {
         delete(false);
     }
 
     public URI toUri() {
         return file.toURI();
     }
 
     public int compareTo(Object o) {
         if(o instanceof Node)
             return compareTo((Node)o);
         return 0;
     }
 
     public boolean equals(Node node) {
         return this.file.equals(((NativeNode)node).file);
     }
     
     public boolean equals(Object object){
         if(object instanceof NativeNode){
             return equals((NativeNode)object);
         }
         return false;
     }
 
     public Path getPath() {
         Path path = new Path();
         
         Node node = this;
         while(!node.isDirectory() || !((Directory)node).isRoot()){
             path.addLevel(0, node.getName());
             try{
                 node=getParent();
             } catch(FileNotFoundException ex){
                 //if this happens, the parent directy has been removed.
                 //Actually - this file does not exist anymote.
                 throw new RuntimeException("Cannot retrieve parent directory.");
             }
         }
         return path;
     }
 
     public boolean canRead() {
         return session.getFileSystem().getSecurity().canRead(getPrincipal(), this) && file.canRead();
     }
 
     public boolean canWrite() {
         return session.getFileSystem().getSecurity().canWrite(getPrincipal(), this) && file.canWrite();
     }
 
     public String getBaseName() {
         String name = file.getName();
         if(name.contains("."))
            return name.substring(0,name.lastIndexOf("."));
         else
             return name;
     }
 
     public String getSuffix() {
         String name = file.getName();
         if(name.contains("."))
             return name.substring(name.lastIndexOf(".")+1);
         else
             return null;
     }
 
     
 
 }
