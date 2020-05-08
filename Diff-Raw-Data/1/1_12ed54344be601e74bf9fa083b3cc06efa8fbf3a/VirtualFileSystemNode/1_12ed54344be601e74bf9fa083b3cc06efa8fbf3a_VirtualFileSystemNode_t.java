 package org.wyona.yarep.impl.repo.vfs;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 
 import org.apache.commons.io.FileUtils;
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
  */
 public class VirtualFileSystemNode extends AbstractNode {
     private static Category log = Category.getInstance(VirtualFileSystemNode.class);
 
     protected static final String META_FILE_NAME = "meta";
     protected static final String REVISIONS_BASE_DIR = "revisions";
     protected static final String META_DIR_SUFFIX = ".yarep";
     
     //protected FileSystemRepository repository;
     protected File contentDir;
     protected File contentFile;
     protected File metaDir;
     protected File metaFile;
     
     protected RevisionDirectoryFilter revisionDirectoryFilter = new RevisionDirectoryFilter();
     
     
     /**
      * Constructor
      * @throws RepositoryException
      */
     public VirtualFileSystemNode(VirtualFileSystemRepository repository, String path, String uuid) throws RepositoryException {
         super(repository, path, uuid);
         init();
     }
     
     /**
      * Constructor
      * @throws RepositoryException
      */
     protected VirtualFileSystemNode(VirtualFileSystemRepository repository, String path, String uuid, boolean doInit) throws RepositoryException {
         super(repository, path, uuid);
         
         if (doInit) {
             init();
         }
     }
     
     protected void init() throws RepositoryException {
         
         this.contentDir = getRepository().getContentDir();
         this.contentFile = new File(this.contentDir, this.uuid);
         this.metaDir = new File(this.contentDir, uuid + META_DIR_SUFFIX);
         this.metaFile = new File(this.metaDir, META_FILE_NAME);
         
         if (log.isDebugEnabled()) {
             log.debug("VirtualFileSystemNode: path=" + path + " uuid=" + uuid);
             log.debug("contentDir=" + contentDir);
             log.debug("contentFile=" + contentFile);
             log.debug("metaDir=" + metaDir);
             log.debug("metaFile=" + metaFile);
         }
         
         if (!metaFile.exists()) {
             createMetaFile();
         }
         readProperties();
         readRevisions();
     }
     
     protected void createMetaFile() throws RepositoryException {
         log.debug("creating new meta file in dir: " + metaDir);
         if (!metaDir.exists()) {
             metaDir.mkdir();
         }
         this.properties = new HashMap();
         if (this.contentFile.isDirectory()) {
             this.setProperty(PROPERTY_TYPE, NodeType.TYPENAME_COLLECTION);
         } else {
             this.setProperty(PROPERTY_TYPE, NodeType.TYPENAME_RESOURCE);
             //this.setProperty(PROPERTY_SIZE, this.contentFile.length());
             //this.setProperty(PROPERTY_LAST_MODIFIED, this.contentFile.lastModified());
         }
     }
     
     /**
      * Read properties from meta file
      */
     protected void readProperties() throws RepositoryException {
         try {
             log.debug("Reading meta file: " + this.metaFile);
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
     
     /**
      * FIXME: this implementation does not work correctly when a string property contains a line-break. 
      * @throws RepositoryException
      */
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
      * @see org.wyona.yarep.core.Node#getNodes()
      */
     public Node[] getNodes() throws RepositoryException {
         Path[] childPaths = getRepository().getMap().getChildren(new Path(this.path));
         
         Node[] childNodes = new Node[childPaths.length];
         for (int i=0; i<childPaths.length; i++) {
             childNodes[i] = this.repository.getNode(childPaths[i].toString());
         }
         return childNodes;
     }
     
     /**
      * @see org.wyona.yarep.core.Node#addNode(java.lang.String, int)
      */
     public Node addNode(String name, int type) throws RepositoryException {
         String newPath = getPath() + "/" + name;
         log.debug("adding node: " + newPath);
         if (this.repository.existsNode(newPath)) {
             throw new RepositoryException("Node exists already: " + newPath);
         }
         UID uid = getRepository().getMap().create(new Path(newPath));
         // create file:
         File file = new File(this.contentDir, uid.toString());
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
     
     /**
      * @see org.wyona.yarep.core.Node#removeProperty(java.lang.String)
      */
     public void removeProperty(String name) throws RepositoryException {
         this.properties.remove(name);
         saveProperties();
     }
     
     /**
      * @see org.wyona.yarep.core.Node#setProperty(org.wyona.yarep.core.Property)
      */
     public void setProperty(Property property) throws RepositoryException {
         this.properties.put(property.getName(), property);
         saveProperties();
     }
 
     /**
      * @see org.wyona.yarep.core.Node#getInputStream()
      */
     public InputStream getInputStream() throws RepositoryException {
         try {
             if (isCollection()) {
                 if (getRepository().getAlternative() != null) {
                     File alternativeFile = new File(contentFile, getRepository().getAlternative());
                     if (alternativeFile.isFile()) {
                         return new FileInputStream(alternativeFile);
                     } else {
                         log.warn("Is Collection (" + contentFile + ") and no alternative File exists (" + alternativeFile + ")");
                         return new java.io.StringBufferInputStream(getDirectoryListing(contentFile, getRepository().getDirListingMimeType()));
                     }
                 } else {
                     log.warn("Is Collection: " + contentFile);
                     return new java.io.StringBufferInputStream(getDirectoryListing(contentFile, getRepository().getDirListingMimeType()));
                 }
             } else {
                 return new FileInputStream(contentFile);
             }
         } catch (FileNotFoundException e) {
             throw new RepositoryException(e.getMessage(), e);
         }
         //return getProperty(PROPERTY_CONTENT).getInputStream();
     }
     
     /**
      * @see org.wyona.yarep.core.Node#getOutputStream()
      */
     public OutputStream getOutputStream() throws RepositoryException {
         try {
             return new FileOutputStream(this.contentFile);
             //return new VirtualFileSystemOutputStream(this, this.contentFile);
         } catch (FileNotFoundException e) {
             throw new RepositoryException(e.getMessage(), e);
         }
         //return getProperty(PROPERTY_CONTENT).getOutputStream();
     }
     
     /**
      * @see org.wyona.yarep.core.Node#checkin()
      */
     public Revision checkin() throws NodeStateException, RepositoryException {
         return checkin("");
     }
     
     /**
      * @see org.wyona.yarep.core.Node#checkin()
      */
     public Revision checkin(String comment) throws NodeStateException, RepositoryException {
         if (!isCheckedOut()) {
             throw new NodeStateException("Node " + path + " is not checked out.");
         }
         Revision revision = createRevision(comment);
         
         setProperty(PROPERTY_IS_CHECKED_OUT, false);
         
         return revision;
     }
     
     /**
      * @see org.wyona.yarep.core.Node#checkout(java.lang.String)
      */
     public void checkout(String userID) throws NodeStateException, RepositoryException {
         // TODO: this should be somehow synchronized
         if (isCheckedOut()) {
             throw new NodeStateException("Node " + path + " is already checked out by: " + getCheckoutUserID());
         }
         
         setProperty(PROPERTY_IS_CHECKED_OUT, true);
         setProperty(PROPERTY_CHECKOUT_USER_ID, userID);
         setProperty(PROPERTY_CHECKOUT_DATE, new Date());
 
         if (getRevisions().length == 0) {
             // create a backup revision
             createRevision("initial revision");
         }
     }
     
     protected Revision createRevision(String comment) throws RepositoryException {
         try {
             File revisionsBaseDir = new File(this.metaDir, REVISIONS_BASE_DIR);
             String revisionName = String.valueOf(System.currentTimeMillis());
             File revisionDir = new File(revisionsBaseDir, revisionName);
             
             File destContentFile = new File(revisionDir, VirtualFileSystemRevision.CONTENT_FILE_NAME);
             FileUtils.copyFile(this.contentFile, destContentFile);
         
             File destMetaFile = new File(revisionDir, META_FILE_NAME);
             FileUtils.copyFile(this.metaFile, destMetaFile);
         
             Revision revision = new VirtualFileSystemRevision(this, revisionName);
             revision.setProperty(PROPERTY_IS_CHECKED_OUT, false);
             ((VirtualFileSystemRevision)revision).setCreationDate(new Date());
             ((VirtualFileSystemRevision)revision).setCreator(getCheckoutUserID());
             ((VirtualFileSystemRevision)revision).setComment(comment);
             this.revisions.put(revisionName, revision);
             return revision;
         } catch (IOException e) {
             log.error(e.getMessage(), e);
             throw new RepositoryException(e.getMessage(), e);
         }
     }
     
     protected void readRevisions() throws RepositoryException {
         File revisionsBaseDir = new File(this.metaDir, REVISIONS_BASE_DIR);
         
         File[] revisionDirs = revisionsBaseDir.listFiles(this.revisionDirectoryFilter);
         
         this.revisions = new LinkedHashMap();
         
         if (revisionDirs != null) {
             Arrays.sort(revisionDirs);
             for (int i=0; i<revisionDirs.length; i++) {
                 String revisionName = revisionDirs[i].getName();
                 Revision revision = new VirtualFileSystemRevision(this, revisionName);
                 this.revisions.put(revisionName, revision);
             }
         }
     }
     
     /**
      * @see org.wyona.yarep.core.Node#restore(java.lang.String)
      */
     public void restore(String revisionName) throws NoSuchRevisionException, RepositoryException {
         try {
             File revisionsBaseDir = new File(this.metaDir, REVISIONS_BASE_DIR);
             File revisionDir = new File(revisionsBaseDir, revisionName);
             
             File srcContentFile = new File(revisionDir, VirtualFileSystemRevision.CONTENT_FILE_NAME);
             FileUtils.copyFile(srcContentFile, this.contentFile);
         
             File srcMetaFile = new File(revisionDir, META_FILE_NAME);
             FileUtils.copyFile(srcMetaFile, this.metaFile);
             
             setProperty(AbstractNode.PROPERTY_LAST_MODIFIED, this.contentFile.lastModified());
         } catch (IOException e) {
             log.error(e.getMessage(), e);
             throw new RepositoryException(e.getMessage(), e);
         }
     }
     
     protected class RevisionDirectoryFilter implements FileFilter {
         public boolean accept(File pathname) {
             if (pathname.getName().matches("[0-9]+") && pathname.isDirectory()) {
                 return true;
             } else {
                 return false;
             }
         }
     }
     
     /**
      * Changing a property should update the last modified date.
      * FIXME: This implementation does not change the last modified date if a property changes.
      * @see org.wyona.yarep.impl.AbstractNode#getLastModified()
      */
     public long getLastModified() throws RepositoryException {
         return this.contentFile.lastModified();
     }
     
     /**
      * @see org.wyona.yarep.impl.AbstractNode#getSize()
      */
     public long getSize() throws RepositoryException {
         return this.contentFile.length();
     }
     
 
     protected VirtualFileSystemRepository getRepository() {
         return (VirtualFileSystemRepository)this.repository;
     }
 
     /**
      * @see org.wyona.yarep.core.Node#delete()
      */
     public void delete() throws RepositoryException {
         deleteRec(this);
     }
        
     protected void deleteRec(Node node) throws RepositoryException {
         Node[] children = node.getNodes();
         for (int i=0; i<children.length; i++) {
             deleteRec(children[i]);
         }
         boolean success = getRepository().getMap().delete(new Path(getPath()));
         try {
             FileUtils.deleteDirectory(this.contentFile);
             FileUtils.deleteDirectory(this.metaDir);
         } catch (IOException e) {
             throw new RepositoryException("Could not delete node: " + node.getPath() + ": " + 
                     e.toString(), e);
         }
     }
 
     /**
      *
      */
     public int getType() throws RepositoryException {
         if (getRepository().getMap().isCollection(new Path(path))) {
             return NodeType.COLLECTION;
         } else if (getRepository().getMap().isResource(new Path(path))) {
             return NodeType.RESOURCE;
         } else {
             return -1;
         }
     }
 
     /**
      * Get directory listing
      */
     public String getDirectoryListing(File file, String mimeType) {
         StringBuffer dirListing = new StringBuffer("<?xml version=\"1.0\"?>");
         if(mimeType.equals("application/xhtml+xml")) {
             dirListing.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
             dirListing.append("<head>");
             dirListing.append("<title>"+path+"</title>");
             dirListing.append("</head>");
             dirListing.append("<body>");
             dirListing.append("<ul>");
             String[] children = file.list();
             for (int i = 0; i < children.length; i++) {
                 File child = new File(file, children[i]);
                 if (child.isFile()) {
                     dirListing.append("<li>File: <a href=\"" + children[i] + "\">" + children[i] + "</a></li>");
                 } else if (child.isDirectory()) {
                     dirListing.append("<li>Directory: <a href=\"" + children[i] + "/\">" + children[i] + "/</a></li>");
                 } else {
                     dirListing.append("<li>Child: <a href=\"" + children[i] + "\">" + children[i] + "</a></li>");
                 }
             }
             dirListing.append("</ul>");
             dirListing.append("</body>");
             dirListing.append("</html>");
         } else if (mimeType.equals("application/xml")) {
             dirListing.append("<directory xmlns=\"http://www.wyona.org/yarep/1.0\" path=\""+path+"\" fs-path=\""+file+"\">");
             String[] children = file.list();
             for (int i = 0; i < children.length; i++) {
                 File child = new File(file, children[i]);
                 if (child.isFile()) {
                     dirListing.append("<file name=\"" + children[i] + "\"/>");
                 } else if (child.isDirectory()) {
                     dirListing.append("<directory name=\"" + children[i] + "\"/>");
                 } else {
                     dirListing.append("<child name=\"" + children[i] + "\"/>");
                 }
             }
             dirListing.append("</directory>");
         } else {
             dirListing.append("<no-such-mime-type-supported>" + mimeType + "</no-such-mime-type-supported>");
         }
         return dirListing.toString();
     }
 
     /**
      * @see org.wyona.yarep.core.Node#getRevisions()
      */
     public Revision[] getRevisions() throws RepositoryException {
         Revision[] revisions = new Revision[1];
         revisions[0] = new VirtualFileSystemRevision(this, "this");
         return revisions;
     }
 }
