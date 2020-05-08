 package org.wyona.yarep.impl;
 
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 
 import org.apache.log4j.Category;
 import org.wyona.commons.io.PathUtil;
 import org.wyona.yarep.core.NoSuchNodeException;
 import org.wyona.yarep.core.NoSuchPropertyException;
 import org.wyona.yarep.core.NoSuchRevisionException;
 import org.wyona.yarep.core.Node;
 import org.wyona.yarep.core.NodeStateException;
 import org.wyona.yarep.core.NodeType;
 import org.wyona.yarep.core.Property;
 import org.wyona.yarep.core.PropertyType;
 import org.wyona.yarep.core.Repository;
 import org.wyona.yarep.core.RepositoryException;
 import org.wyona.yarep.core.Revision;
 
 /**
  * This class represents a repository node and implements some basic functionality which may be 
  * shared among different implementations.
  */
 public abstract class AbstractNode implements Node {
     private static Category log = Category.getInstance(AbstractNode.class);
 
     protected Repository repository;
     protected String path;
     protected String name;
     protected String uuid;
     protected HashMap properties;
     protected LinkedHashMap revisions;
     
     // system properties:
     public static final String PROPERTY_TYPE = "yarep_type";
     //public static final String PROPERTY_CONTENT = "yarep_content";
     public static final String PROPERTY_SIZE = "yarep_size";
     public static final String PROPERTY_LAST_MODIFIED = "yarep_lastModifed";
     public static final String PROPERTY_MIME_TYPE = "yarep_mimeType";
     public static final String PROPERTY_ENCODING = "yarep_encoding";
     public static final String PROPERTY_IS_CHECKED_OUT = "yarep_isCheckedOut";
     public static final String PROPERTY_CHECKOUT_USER_ID = "yarep_checkoutUserID";
     public static final String PROPERTY_CHECKOUT_DATE = "yarep_checkoutDate";
     public static final String PROPERTY_CHECKIN_DATE = "yarep_checkinDate";
     
     
     /**
      * Constructor
      * @throws RepositoryException
      */
     public AbstractNode(Repository repository, String path, String uuid) throws RepositoryException {
         this.repository = repository;
         this.path = path;
         this.name = PathUtil.getName(path);
         this.uuid = uuid;
 
         // TODO: Make sure that no backslashes are being used
         if (path.indexOf("\\") >= 0) {
            RepositoryException e = new RepositoryException();
             log.error(e.getMessage(), e);
             throw e;
         }
     }
     
     /**
      * @see org.wyona.yarep.core.Node#getName()
      */
     public String getName() throws RepositoryException {
         return this.name;
     }
     
     /**
      * @see org.wyona.yarep.core.Node#getParent()
      */
     public Node getParent() throws RepositoryException {
         if (getPath().equals("") || getPath().equals("/")) return null;
         String parentPath = PathUtil.getParent(path);
         return this.repository.getNode(parentPath);
     }
     
     /**
      * @see org.wyona.yarep.core.Node#getPath()
      */
     public String getPath() throws RepositoryException {
         return this.path;
     }
     
     /**
      * @see org.wyona.yarep.core.Node#getUUID()
      */
     public String getUUID() throws RepositoryException {
         return this.uuid;
     }
     
     /**
      * @see org.wyona.yarep.core.Node#getType()
      */
     public int getType() throws RepositoryException {
         return NodeType.getType(getProperty(PROPERTY_TYPE).getString());
     }
     
     /**
      * @see org.wyona.yarep.core.Node#isResource()
      */
     public boolean isResource() throws RepositoryException {
         return getType() == NodeType.RESOURCE; 
     }
     
     /**
      * @see org.wyona.yarep.core.Node#isCollection()
      */
     public boolean isCollection() throws RepositoryException {
         //log.debug("Node Type: " + getType() + ", Path: " + getPath());
         return getType() == NodeType.COLLECTION; 
     }
     
     /**
      * @see org.wyona.yarep.core.Node#getNode(java.lang.String)
      */
     public Node getNode(String name) throws NoSuchNodeException, RepositoryException {
         String childPath = getPath() + "/" + name;
         return this.repository.getNode(childPath);
     }
     
     /**
      * @see org.wyona.yarep.core.Node#hasNode(java.lang.String)
      */
     public boolean hasNode(String name) throws RepositoryException {
         String childPath = getPath() + "/" + name;
         return this.repository.existsNode(childPath);
     }
     
     /**
      * @see org.wyona.yarep.core.Node#getProperty(java.lang.String)
      */
     public Property getProperty(String name) throws RepositoryException {
         return (Property)this.properties.get(name);
     }
     
     /**
      * @see org.wyona.yarep.core.Node#getProperties()
      */
     public Property[] getProperties() throws RepositoryException {
         return (Property[])this.properties.values().toArray(new Property[this.properties.size()]);
     }
     
     /**
      * @see org.wyona.yarep.core.Node#hasProperty(java.lang.String)
      */
     public boolean hasProperty(String name) throws RepositoryException {
         return this.properties.containsKey(name);
     }
     
     //public boolean hasProperties() throws RepositoryException;
     
     /**
      * @see org.wyona.yarep.core.Node#setProperty(java.lang.String, boolean)
      */
     public Property setProperty(String name, boolean value) throws RepositoryException {
         Property property = new DefaultProperty(name, PropertyType.BOOLEAN, this);
         property.setValue(value);
         setProperty(property);
         return property;
     }
     
     /**
      * @see org.wyona.yarep.core.Node#setProperty(java.lang.String, java.util.Date)
      */
     public Property setProperty(String name, Date value) throws RepositoryException {
         Property property = new DefaultProperty(name, PropertyType.DATE, this);
         property.setValue(value);
         setProperty(property);
         return property;
     }
     
     /**
      * @see org.wyona.yarep.core.Node#setProperty(java.lang.String, double)
      */
     public Property setProperty(String name, double value) throws RepositoryException {
         Property property = new DefaultProperty(name, PropertyType.DOUBLE, this);
         property.setValue(value);
         setProperty(property);
         return property;
     }
     
     //public Property setProperty(String name, InputStream value) throws RepositoryException;
     
     /**
      * @see org.wyona.yarep.core.Node#setProperty(java.lang.String, long)
      */
     public Property setProperty(String name, long value) throws RepositoryException {
         Property property = new DefaultProperty(name, PropertyType.LONG, this);
         property.setValue(value);
         setProperty(property);
         return property;
     }
     
     /**
      * @see org.wyona.yarep.core.Node#setProperty(java.lang.String, java.lang.String)
      */
     public Property setProperty(String name, String value) throws RepositoryException {
         Property property = new DefaultProperty(name, PropertyType.STRING, this);
         property.setValue(value);
         setProperty(property);
         return property;
     }
     
     
     /**
      * @see org.wyona.yarep.core.Node#isCheckedOut()
      */
     public boolean isCheckedOut() throws RepositoryException {
         if (!hasProperty(PROPERTY_IS_CHECKED_OUT)) {
             return false;
         }
         return getProperty(PROPERTY_IS_CHECKED_OUT).getBoolean();
     }
     
     /**
      * @see org.wyona.yarep.core.Node#getCheckoutUserID()
      */
     public String getCheckoutUserID() throws NodeStateException, RepositoryException {
         if (!isCheckedOut()) {
             throw new NodeStateException("Node is not checked out: " + getPath());
         }
         return getProperty(PROPERTY_CHECKOUT_USER_ID).getString();
     }
     
     /**
      * @see org.wyona.yarep.core.Node#getCheckoutDate()
      */
     public Date getCheckoutDate() throws NodeStateException, RepositoryException {
         if (!isCheckedOut()) {
             throw new NodeStateException("Node is not checked out: " + getPath());
         }
         return getProperty(PROPERTY_CHECKOUT_DATE).getDate();
     }
     
     /**
      * @see org.wyona.yarep.core.Node#getCheckinDate()
      */
     public Date getCheckinDate() throws NodeStateException, RepositoryException {
         if (isCheckedOut()) {
             throw new NodeStateException("Node is not checked in: " + getPath());
         }
         return getProperty(PROPERTY_CHECKIN_DATE).getDate();
     }
     
     /**
      * @see org.wyona.yarep.core.Node#getRevisions()
      */
     public Revision[] getRevisions() throws RepositoryException {
         Collection values =  this.revisions.values();
         return (Revision[])values.toArray(new Revision[values.size()]);
     }
     
     /**
      * @see org.wyona.yarep.core.Node#getRevision(java.lang.String)
      */
     public Revision getRevision(String revisionName) throws NoSuchRevisionException, RepositoryException {
         if (!this.revisions.containsKey(revisionName)) {
             throw new NoSuchRevisionException("Node " + getPath() + " has no revision with name: " + revisionName);
         }
         return (Revision)this.revisions.get(revisionName);
     }
     
     /**
      * @see org.wyona.yarep.core.Node#getRevisionByTag(java.lang.String)
      */
     public Revision getRevisionByTag(String tag) throws NoSuchRevisionException, RepositoryException {
         Iterator iter = this.revisions.values().iterator();
         
         while (iter.hasNext()) {
             Revision revision = (Revision)iter.next();
             if (revision.hasTag() && revision.getTag().equals(tag)) {
                 return revision;
             }
         }
         // revision not found:
         throw new NoSuchRevisionException("Node " + getPath() + " has no revision with tag: " + tag);
     }
     
     /**
      * @see org.wyona.yarep.core.Node#hasRevisionWithTag(java.lang.String)
      */
     public boolean hasRevisionWithTag(String tag) throws RepositoryException {
         Iterator iter = this.revisions.values().iterator();
         
         while (iter.hasNext()) {
             Revision revision = (Revision)iter.next();
             if (revision.hasTag() && revision.getTag().equals(tag)) {
                 return true;
             }
         }
         // revision not found:
         return false;
     }
     
     /**
      * @see org.wyona.yarep.core.Node#getLastModified()
      */
     public long getLastModified() throws RepositoryException {
         Property lastModified = getProperty(PROPERTY_LAST_MODIFIED);
         if (lastModified != null) {
             return lastModified.getLong();
         } else {
             return 0;
         }
     }
     
     /**
      * @see org.wyona.yarep.core.Node#getSize()
      */
     public long getSize() throws RepositoryException {
         Property size = getProperty(PROPERTY_SIZE);
         if (size != null) {
             return size.getLong();
         } else {
             return 0;
         }
     }
     
     /**
      * @see org.wyona.yarep.core.Node#getMimeType()
      */
     public String getMimeType() throws RepositoryException {
         Property mimeType = getProperty(PROPERTY_MIME_TYPE);
         if (mimeType != null) {
             return mimeType.getString();
         } else {
             return null;
         }
     }
     
     /**
      * @see org.wyona.yarep.core.Node#setMimeType(java.lang.String)
      */
     public void setMimeType(String mimeType) throws RepositoryException {
         setProperty(PROPERTY_MIME_TYPE, mimeType);
     }
     
     /**
      * @see org.wyona.yarep.core.Node#getEncoding()
      */
     public String getEncoding() throws RepositoryException {
         Property encoding = getProperty(PROPERTY_ENCODING);
         if (encoding != null) {
             return encoding.getString();
         } else {
             return null;
         }
     }
     
     /**
      * @see org.wyona.yarep.core.Node#setEncoding(java.lang.String)
      */
     public void setEncoding(String encoding) throws RepositoryException {
         setProperty(PROPERTY_ENCODING, encoding);
     }
         
 }
