 package dk.apaq.vfs.impl.ram;
 
 import dk.apaq.vfs.Directory;
 import dk.apaq.vfs.FileSystem;
 import dk.apaq.vfs.Node;
 import dk.apaq.vfs.Path;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URI;
 import java.util.Date;
 
 /**
  *
  */
 public class RamNode implements Node {
 
     protected RamDirectory parent;
     private String name;
     private Date lastModified = new Date();
 
     public RamNode(RamDirectory parent, String name) {
         this.parent = parent;
         this.name = name;
     }
     
     public void moveTo(Directory newParent) throws IOException {
         RamDirectory todir = (RamDirectory) newParent;
         parent.getChildrenUnwrapped().remove(this);
         todir.getChildrenUnwrapped().add(this);
         parent = todir;
     }
 
     public void moveTo(Directory newParent, String newName) throws IOException {
         moveTo(newParent);
         name = newName;
     }
 
     public void setName(String name) throws IOException {
         this.name=name;
     }
 
     public String getName() {
         return name;
     }
 
     public String getBaseName() {
         if (name.contains(".")) {
             return name.substring(0, name.lastIndexOf("."));
         } else {
             return name;
         }
     }
 
     public String getSuffix() {
         if (name.contains(".")) {
             return name.substring(name.lastIndexOf(".") + 1);
         } else {
             return null;
         }
     }
 
     public Directory getParent() throws FileNotFoundException {
         return parent;
     }
 
     public FileSystem getFileSystem() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public boolean isDirectory() {
         return this instanceof RamDirectory;
     }
 
     public boolean isFile() {
         return this instanceof RamFile;
     }
 
     public boolean isHidden() {
         return false;
     }
 
     public Date getLastModified() {
         return lastModified;
     }
 
     public void setLastModified(Date date) {
         this.lastModified = date;
     }
 
     public int compareTo(Node node) {
         return this.getName().compareTo(node.getName());
     }
 
     public void delete() throws IOException {
         if(isDirectory()) {
             ((Directory)this).delete(false);
         } else {
             parent.getChildrenUnwrapped().remove(this);
         }
     }
 
 
     public URI toUri() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public boolean equals(Node node) {
         return this == node;
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
         return true;
     }
 
     public boolean canWrite() {
         return false;
     }
 
     public int compareTo(Object o) {
         return compareTo((Node)o);
     }
 }
