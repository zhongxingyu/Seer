 package org.wyona.yarep.impl.repo.fs;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import org.apache.log4j.Category;
 import org.wyona.yarep.core.NoSuchRevisionException;
 import org.wyona.yarep.core.Node;
 import org.wyona.yarep.core.NodeStateException;
 import org.wyona.yarep.core.NodeType;
 import org.wyona.yarep.core.Path;
 import org.wyona.yarep.core.Property;
 import org.wyona.yarep.core.PropertyType;
 import org.wyona.yarep.core.RepositoryException;
 import org.wyona.yarep.core.Revision;
 import org.wyona.yarep.core.UID;
 import org.wyona.yarep.impl.AbstractNode;
 import org.wyona.yarep.impl.DefaultProperty;
 
 /**
  * This class represents a repository node.
  * A repository node may be either a collection ("directory") or a resource ("file").
  * If it is a resource, it has a binary default property, which may be accessed by using 
  * getInputStream() and getOutputStream().
  */
 public class FileSystemNode extends AbstractNode {
     private static Category log = Category.getInstance(FileSystemNode.class);
 
     //protected FileSystemRepository repository;
     protected File metaFile;
     
     
     /**
      * Constructor
      * @throws RepositoryException
      */
     public FileSystemNode(FileSystemRepository repository, String path, String uuid) throws RepositoryException {
         super(repository, path, uuid);
         
         this.metaFile = new File(repository.getContentDir(), uuid + ".yarep" + File.separator + "meta");
         if (!metaFile.exists()) {
             createMetaFile();
         }
         readProperties();
     }
     
     protected void createMetaFile() throws RepositoryException {
         File metaDir = new File(getContentDir(), uuid + ".yarep");
         log.debug("creating new meta file in dir: " + metaDir);
         if (!metaDir.exists()) {
             metaDir.mkdir();
         }
         this.properties = new HashMap();
         File contentFile = new File(getContentDir(), uuid);
         if (contentFile.isDirectory()) {
             this.setProperty(PROPERTY_TYPE, NodeType.TYPENAME_COLLECTION);
         } else {
             this.setProperty(PROPERTY_TYPE, NodeType.TYPENAME_RESOURCE);
             this.setProperty(PROPERTY_SIZE, contentFile.length());
             this.setProperty(PROPERTY_LAST_MODIFIED, contentFile.lastModified());
         }
     }
     
     protected void readProperties() throws RepositoryException {
         try {
             log.debug("reading meta file: " + this.metaFile);
             this.properties = new HashMap();
             BufferedReader reader = new BufferedReader(new FileReader(this.metaFile));
             String line;
             while ((line = reader.readLine()) != null) {
                 line = line.trim();
                 String name;
                 String typeName;
                 String value;
                 try {
                     name = line.substring(0, line.indexOf("<")).trim();
                     typeName = line.substring(line.indexOf("<")+1, line.indexOf(">")).trim();
                     value = line.substring(line.indexOf(":")+1).trim();
                 } catch (StringIndexOutOfBoundsException e) {
                     throw new RepositoryException("Error while parsing meta file: " + this.metaFile 
                             + " at line " + line);
                 }
                 Property property = new DefaultProperty(name, PropertyType.getType(typeName), this);
                 property.setValueFromString(value);
                 this.properties.put(name, property);
             }
             reader.close();
         } catch (IOException e) {
             throw new RepositoryException("Error while reading meta file: " + metaFile + ": " 
                     + e.getMessage());
         }
     }
     
     protected void saveProperties() throws RepositoryException {
         try {
             log.debug("writing meta file: " + this.metaFile);
             PrintWriter writer = new PrintWriter(new FileOutputStream(this.metaFile));
             Iterator iterator = this.properties.values().iterator();
             while (iterator.hasNext()) {
                 Property property = (Property)iterator.next();
                 writer.println(property.getName() + "<" + PropertyType.getTypeName(property.getType()) + 
                         ">:" + property.getValueAsString());
             }
             writer.flush();
             writer.close();
         } catch (IOException e) {
             throw new RepositoryException("Error while reading meta file: " + metaFile + ": " 
                     + e.getMessage());
         }
     }
     
     /**
      * Gets all child nodes.
      * @return child nodes or empty array if there are no child nodes.
      * @throws RepositoryException repository error
      */
     public Node[] getNodes() throws RepositoryException {
         Path[] childPaths = ((FileSystemRepository)this.repository).getMap().getChildren(new Path(this.path));
         Node[] childNodes = new Node[childPaths.length];
         for (int i=0; i<childPaths.length; i++) {
            childNodes[i] = this.repository.getNode(childPaths.toString());
         }
         return childNodes;
     }
     
     /**
      * Creates a new node and adds it as a child to this node.
      * @param name of the child node 
      * @return the new child node
      * @throws RepositoryException repository error
      */
     public Node addNode(String name, int type) throws RepositoryException {
         String newPath = getPath() + "/" + name;
         log.debug("adding node: " + newPath);
         if (this.repository.existsNode(newPath)) {
             throw new RepositoryException("Node exists already: " + newPath);
         }
         UID uid = ((FileSystemRepository)this.repository).getMap().create(new Path(newPath));
         // create file:
         File file = new File(getContentDir(), uid.toString());
         try {
             if (type == NodeType.COLLECTION) {
                 file.mkdir();
             } else if (type == NodeType.RESOURCE) {
                 file.createNewFile();
             } else {
                 throw new RepositoryException("Unknown node type: " + type);
             }
             return this.repository.getNode(newPath);
         } catch (IOException e) {
             throw new RepositoryException("Could not access file " + file, e);
         }
     }
     
     public void setProperty(Property property) throws RepositoryException {
         this.properties.put(property.getName(), property);
         saveProperties();
     }
 
     /**
      * Gets an input stream of the binary default property.
      * Useful only for nodes of type resource.
      * @return
      * @throws RepositoryException repository error
      */
     public InputStream getInputStream() throws RepositoryException {
         File file = new File(getContentDir(), this.uuid);
         try {
             return new FileInputStream(file);
         } catch (FileNotFoundException e) {
             throw new RepositoryException(e.getMessage(), e);
         }
         //return getProperty(PROPERTY_CONTENT).getInputStream();
     }
     
     /**
      * Gets an output stream of the binary default property.
      * Useful only for nodes of type resource.
      * @return
      * @throws RepositoryException repository error
      */
     public OutputStream getOutputStream() throws RepositoryException {
         File file = new File(getContentDir(), this.uuid);
         try {
             return new FileSystemOutputStream(this, file);
         } catch (FileNotFoundException e) {
             throw new RepositoryException(e.getMessage(), e);
         }
         //return getProperty(PROPERTY_CONTENT).getOutputStream();
     }
     
     /**
      * Checks in this node and creates a new revision.
      * @return
      * @throws NodeStateException if node is not in checked out state
      * @throws RepositoryException repository error
      */
     public Revision checkin() throws NodeStateException, RepositoryException {
         // TODO: not implemented yet
         log.warn("Not implemented yet.");
         return null;
     }
     
     /**
      * Checks out this node.
      * @throws NodeStateException if node is checked out by a different user
      * @throws RepositoryException repository error
      */
     public void checkout(String userID) throws NodeStateException, RepositoryException {
         // TODO: not implemented yet
         log.warn("Not implemented yet.");
     }
     
     /**
      * Restores the revision with the given name.
      * @param revisionName
      * @throws NoSuchRevisionException if the revision does not exist
      * @throws RepositoryException
      */
     public void restore(String revisionName) throws NoSuchRevisionException, RepositoryException {
         // TODO: not implemented yet
         log.warn("Not implemented yet.");
     }
     
     protected File getContentDir() {
         return ((FileSystemRepository)repository).getContentDir();
     }
     
        
 }
