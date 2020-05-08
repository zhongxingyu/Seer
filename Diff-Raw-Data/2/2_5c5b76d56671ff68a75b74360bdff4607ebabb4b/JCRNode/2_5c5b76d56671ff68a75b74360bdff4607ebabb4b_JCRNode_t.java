 package org.wyona.yarep.impl.repo.jcr;
 
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.Reader;
 import java.io.Writer;
 import java.util.Date;
 
 import org.wyona.yarep.core.Node;
 import org.wyona.yarep.core.NoSuchNodeException;
 import org.wyona.yarep.core.NoSuchRevisionException;
 import org.wyona.yarep.core.NodeStateException;
 import org.wyona.yarep.core.NodeType;
 import org.wyona.yarep.core.Property;
 import org.wyona.yarep.core.PropertyType;
 import org.wyona.yarep.core.RepositoryException;
 import org.wyona.yarep.core.Revision;
 import org.wyona.yarep.impl.AbstractNode;
 import org.wyona.yarep.impl.DefaultProperty;
 
 import org.apache.log4j.Category;
 
 /**
  * This class represents a repository node.
  * A repository node may be either a collection ("directory") or a resource ("file").
  * If it is a resource, it has an associated data content, which may be accessed by using 
  * getInputStream()/getOutputStream() or getReader()/getWriter().
  * To store textual data, the reader/writer methods should be used instead of the stream
  * methods to allow the implementation to handle textual data differently from binary data.
  * 
  * @see org.wyona.yarep.core.Repository
  */
 public class JCRNode implements Node {
 
     private static Category log = Category.getInstance(JCRNode.class);
 
     private javax.jcr.Node jcrNode;
     private javax.jcr.Session session;
 
     public static String BINARY_CONTENT_PROP_NAME = "binary-content";
 
     /**
      *
      */
     public JCRNode(javax.jcr.Node node, javax.jcr.Session session) {
         this.jcrNode = node;
         this.session = session;
     }
 
     /**
      * Gets the name of this node, which is the last part of the path.
      * @return name
      * @throws RepositoryException repository error
      */
     public String getName() throws RepositoryException {
         try {
             return jcrNode.getName();
         } catch (javax.jcr.RepositoryException e) {
             throw new RepositoryException(e.getMessage(), e);
         }
     }
     
     /**
      * Gets the parent node of this node.
      * @return parent node or null if this is the root node
      * @throws RepositoryException repository error
      */
     public Node getParent() throws RepositoryException {
         log.error("Not implemented yet!");
         return null;
     }
     
     /**
      * Deletes this node and all subnodes.
      * The root node cannot be deleted.
      * @throws RepositoryException if this node is the root node or if a repository error occurs.
      */
     public void delete() throws RepositoryException {
         log.error("Not implemented yet!");
     }
     
     /**
      * Gets the complete repository path of this node.
      * @return path
      * @throws RepositoryException repository error
      */
     public String getPath() throws RepositoryException {
         try {
             return jcrNode.getPath();
         } catch (javax.jcr.RepositoryException e) {
             throw new RepositoryException(e.getMessage(), e);
         }
     }
     
     /**
      * Gets the UUID of this node.
      * @return uuid
      * @throws RepositoryException repository error
      */
     public String getUUID() throws RepositoryException {
         try {
             return jcrNode.getUUID();
         } catch (javax.jcr.RepositoryException e) {
             throw new RepositoryException(e.getMessage(), e);
         }
     }
 
     /**
      * Gets the type of this node (collection or resource).
      * @return type
      * @throws RepositoryException repository error
      * @see org.wyona.yarep.core.Node#getType()
      */
     public int getType() throws RepositoryException {
         log.error("Not implemented yet!");
         return -1;
         //jcrNode.getDefinition().getDeclaringNodeType()
     }
     
     /**
      * Indicates whether this node is of type "resource".
      * @return true if type is resource
      * @throws RepositoryException repository error
      * @see org.wyona.yarep.core.Node#isResource()
      */
     public boolean isResource() throws RepositoryException {
         log.error("Implementation not finished yet!");
         // TODO: Do not use the property BINARY_CONTENT_PROP_NAME ...
         try {
             if (jcrNode.hasProperty(BINARY_CONTENT_PROP_NAME)) {
                 return true;
             } else {
                 return false;
             }
         } catch (Exception e) {
             throw new RepositoryException(e.getMessage(), e);
         }
     }
     
     /**
      *  Indicates whether this node is of type "collection".
      * @return true if type is collection
      * @throws RepositoryException repository error
      * @see org.wyona.yarep.core.Node#isCollection()
      */
     public boolean isCollection() throws RepositoryException {
         log.error("Not implemented yet!");
         return true;
         //return getType() == NodeType.COLLECTION; 
     }
     
     /**
      * Indicates whether the content of this node is binary or textual.
      * Useful only if this node is a resource.
      * @return true if the content of this node is binary
      * @throws RepositoryException repository error
      */
     //public boolean isBinary() throws RepositoryException;
     
     /**
      * Creates a new node and adds it as a direct child to this node.
      * @param name name of the child node
      * @param type node type of the child node
      * @return the new child node
      * @throws RepositoryException if this node is not a collection or if a repository error occurs
      */
     public Node addNode(String name, int type) throws RepositoryException {
         try {
             javax.jcr.Node newNode = this.jcrNode.addNode(name);
             this.session.save();
             return new JCRNode(newNode, session);
         } catch (Exception e) {
             throw new RepositoryException(e.getMessage(), e);
         }
     }
 
     /**
      * Gets the child node with the given name. Must be a direct child.
      * @param name name of the child node
      * @return child node
      * @throws NoSuchNodeException if no child node with this name exists.
      * @throws RepositoryException if node is not a collection or if a repository error occurs
      */
     public Node getNode(String name) throws NoSuchNodeException, RepositoryException {
         log.error("Not implemented yet!");
         return null;
     }
     
     /**
      * Gets all child nodes.
      * @return child nodes or empty array if there are no child nodes.
      * @throws RepositoryException if node is not a collection or if a repository error occurs
      */
     public Node[] getNodes() throws RepositoryException {
         log.error("Not implemented yet!");
         return null;
     }
     
     /**
      * Indicates whether this node has a direct child node with the given name.
      * @param name
      * @return true if child node exists with the given id, false otherwise
      * @throws RepositoryException if node is not a collection or if a repository error occurs
      */
     public boolean hasNode(String name) throws RepositoryException {
         try {
             return jcrNode.hasNode(name);
         } catch (Exception e) {
             throw new RepositoryException(e);
         }
     }
     
     /**
      * Gets the property with the given name.
      * @param name
      * @return property or null if the property does not exist
      * @throws RepositoryException repository error
      */
     public Property getProperty(String name) throws RepositoryException {
         try {
             javax.jcr.Property jcrProp = this.jcrNode.getProperty(name);
             int type = jcrProp.getDefinition().getRequiredType();
 
             Property p = null;
             if (type == javax.jcr.PropertyType.STRING) {
                 p = new DefaultProperty(name, PropertyType.STRING, this);
                 p.setValue(this.jcrNode.getProperty(name).getValue().getString());
             } else if (type == javax.jcr.PropertyType.UNDEFINED) {
                 log.warn("PropertyType is UNDEFINED. Trying to convert to String ...");
                 p = new DefaultProperty(name, PropertyType.STRING, this);
                 p.setValue(this.jcrNode.getProperty(name).getValue().getString());
             } else {
                 log.error("PropertyType not implemented yet: " + type);
                 log.error("javax.jcr.PropertyType.UNDEFINED: " + javax.jcr.PropertyType.UNDEFINED);
                 log.error("javax.jcr.PropertyType.STRING: " + javax.jcr.PropertyType.STRING);
             }
             return p;
         } catch (Exception e) {
             throw new RepositoryException(e.getMessage(), e);
         }
     }
     
     /**
      * Get all properties of this node
      * @return array of properties of this node or empty array if there are no properties.
      * @throws RepositoryException other error
      */
     public Property[] getProperties() throws RepositoryException {
         log.error("Not implemented yet!");
         return null;
     }
     
     /**
      * Indicates whether this node has a property with the given name.
      * @param name
      * @return true if a property exists with the given name, false otherwise
      * @throws RepositoryException repository error
      */
     public boolean hasProperty(String name) throws RepositoryException {
         log.error("Not implemented yet!");
         return false;
     }
     
     //public boolean hasProperties() throws RepositoryException;
     
     /**
      * Removes the property with the given name.
      * Does nothing if no property with the given name exists.
      * @param name
      * @throws RepositoryException repository error
      */
     public void removeProperty(String name) throws RepositoryException {
         log.error("Not implemented yet!");
     }
     
     /**
      * Sets a property of type boolean or creates it if it does not exist yet.
      * @param name
      * @param value
      * @return the set property
      * @throws RepositoryException repository error
      */
     public Property setProperty(String name, boolean value) throws RepositoryException {
         log.error("Not implemented yet!");
         return null;
     }
     
     /**
      * Sets a property of type date or creates it if it does not exist yet.
      * @param name
      * @param value
      * @return the set property
      * @throws RepositoryException repository error
      */
     public Property setProperty(String name, Date value) throws RepositoryException {
         log.error("Not implemented yet!");
         return null;
     }
     
     /**
      * Sets a property of type double or creates it if it does not exist yet.
      * @param name
      * @param value
      * @return the set property
      * @throws RepositoryException repository error
      */
     public Property setProperty(String name, double value) throws RepositoryException {
         log.error("Not implemented yet!");
         return null;
     }
     
     //public Property setProperty(String name, InputStream value) throws RepositoryException;
     
     /**
      * Sets a property of type long or creates it if it does not exist yet.
      * @param name
      * @param value
      * @return the set property
      * @throws RepositoryException repository error
      */
     public Property setProperty(String name, long value) throws RepositoryException {
         log.error("Not implemented yet!");
         return null;
     }
     
     /**
      * Sets a property of type string or creates it if it does not exist yet.
      * @param name
      * @param value
      * @return the set property
      * @throws RepositoryException repository error
      */
     public Property setProperty(String name, String value) throws RepositoryException {
         try {
             this.jcrNode.setProperty(name, value);
             //this.jcrNode.setProperty(name, value, javax.jcr.PropertyType.STRING);
             session.save();
             Property p = new DefaultProperty(name, PropertyType.STRING, this);
             p.setValue(value);
             return p;
         } catch (Exception e) {
             throw new RepositoryException(e.getMessage(), e);
         }
     }
 
     /**
      * Sets a property or creates it if it does not exist yet.
      * @param property
      * @throws RepositoryException repository error
      */
     public void setProperty(Property property) throws RepositoryException {
         try {
             if (property.getType() == PropertyType.STRING) {
                 this.jcrNode.setProperty(property.getName(), property.getString());
                 session.save();
             } else {
                 log.error("Not implemented yet!");
             }
         } catch (Exception e) {
             throw new RepositoryException(e.getMessage(), e);
         }
     }
 
     //public Property getDefaultProperty() throws RepositoryException;
     
     /**
      * Gets an input stream of the binary data content of this node.
      * Useful only for nodes of type resource.
      * @return input stream
      * @throws RepositoryException repository error
      */
     public InputStream getInputStream() throws RepositoryException {
         try {
             //return jcrNode.getProperty(BINARY_CONTENT_PROP_NAME).getStream();
             return jcrNode.getNode("jcr:content").getProperty("jcr:data").getStream();
         } catch (Exception e) {
             throw new RepositoryException(e.getMessage(), e);
         }
     }
     
     //public void setInputStream(InputStream inputStream) throws RepositoryException;
     
     /**
      * Gets an output stream of the binary data content of this node.
      * Useful only for nodes of type resource.
      * Don't forget to close the stream because some implementations may
      * require that.
      * @return output stream
      * @throws RepositoryException repository error
      */
     public OutputStream getOutputStream() throws RepositoryException {
         return new JCROutputStream(this);
     }
     
     /**
      * Gets a reader of the data content of this node. Use this method if the
      * node contains character data.
      * Useful only for nodes of type resource.
      * @return reader
      * @throws RepositoryException repository error
      */
     //public Reader getReader() throws RepositoryException;
     
     /**
      * Gets a writer of the data content of this node. Use this method if the
      * node contains character data.
      * Useful only for nodes of type resource.
      * Don't forget to close the writer because some implementations may
      * require that.
      * @return writer
      * @throws RepositoryException repository error
      */
     //public Writer getWriter() throws RepositoryException;
     
     /**
      * Puts this node into checked-in state and creates a new revision.
      * @return the new revision
      * @throws NodeStateException if node is not in checked out state
      * @throws RepositoryException repository error
      */
     public Revision checkin() throws NodeStateException, RepositoryException {
         log.error("Not implemented yet!");
         return null;
     }
     
     /**
      * Puts this node into checked-in state and creates a new revision.
      * @param comment a comment to add to the new revision.
      * @return the new revision
      * @throws NodeStateException if node is not in checked out state
      * @throws RepositoryException repository error
      */
     public Revision checkin(String comment) throws NodeStateException, RepositoryException {
         log.error("Not implemented yet!");
         return null;
     }
     
     /**
      * Puts this node into checked-out state.
      * @throws NodeStateException if node is in checked out state already
      * @throws RepositoryException repository error
      */
     public void checkout(String userID) throws NodeStateException, RepositoryException {
         log.error("Not implemented yet!");
     }
     
     /**
      * Cancels a checkout, i.e. performs a checkin without creating a new revision.
      * @throws NodeStateException
      * @throws NodeStateException if node is not in checked out state
      * @throws RepositoryException
      */
     public void cancelCheckout() throws NodeStateException, RepositoryException {
         log.error("Not implemented yet!");
     }
     
     /**
      * Indicates whether this node is checked out.
      * @return true if checked out, false otherwise
      * @throws RepositoryException repository error
      */
     public boolean isCheckedOut() throws RepositoryException {
         log.error("Not implemented yet!");
         return false;
     }
     
     /**
      * Gets the userID which was supplied when calling checkout(userID).
      * @return userID
      * @throws NodeStateException if node is not checked out.
      * @throws RepositoryException
      */
     public String getCheckoutUserID() throws NodeStateException, RepositoryException {
         log.error("Not implemented yet!");
         return null;
     }
     
     /**
      * Gets the date when this node was checked out.
      * @return checkout date
      * @throws NodeStateException if node is not checked out.
      * @throws RepositoryException
      */
     public Date getCheckoutDate() throws NodeStateException, RepositoryException {
         log.error("Not implemented yet!");
         return null;
     }
     
     /**
      * Gets the date when this node was checked in.
      * @return checkin date
      * @throws NodeStateException if node is not checked in.
      * @throws RepositoryException
      */
     public Date getCheckinDate() throws NodeStateException, RepositoryException {
         log.error("Not implemented yet!");
         return null;
     }
     
     /**
      * Gets all revisions of this node.
      * Oldest revision at the first array position, newest at the last position.
      * @return array of revisions, or empty array if there are no revisions
      * @throws RepositoryException
      */
     public Revision[] getRevisions() throws RepositoryException {
         log.error("Not implemented yet!");
         return null;
     }
     
     /**
      * Gets the revision with the given name.
      * @param revisionName
      * @return revision
      * @throws NoSuchRevisionException if the revision does not exist
      * @throws RepositoryException
      */
     public Revision getRevision(String revisionName) throws NoSuchRevisionException, RepositoryException {
         log.error("Not implemented yet!");
         return null;
     }
     
     /**
      * Gets the revision with the given tag.
      * If multiple revisions have the same tag, the oldest one will be returned.
      * @param tag
      * @return revision
      * @throws NoSuchRevisionException if the revision does not exist
      * @throws RepositoryException
      */
     public Revision getRevisionByTag(String tag) throws NoSuchRevisionException, RepositoryException {
         log.error("Not implemented yet!");
         return null;
     }
     
     /**
      * Indicates whether this node has a revision with the given tag.
      * If multiple revisions have the same tag, the oldest one will be returned.
      * @param tag
      * @return true if a revision with the given tag exists, false otherwise
      * @throws RepositoryException
      */
     public boolean hasRevisionWithTag(String tag) throws RepositoryException {
         log.error("Not implemented yet!");
         return false;
     }
     
     /**
      * Restores the revision with the given name.
      * @param revisionName
      * @throws NoSuchRevisionException if the revision does not exist
      * @throws RepositoryException
      */
     public void restore(String revisionName) throws NoSuchRevisionException, RepositoryException {
         log.error("Not implemented yet!");
     }
     
     /**
      * Gets the last modified date of this node in ms.
      * Changing a property should update the last modified date.
      * @return last modified date in ms
      * @throws RepositoryException
      */
     public long getLastModified() throws RepositoryException {
         try {
             return getJCRResourceNode().getProperty("jcr:lastModified").getDate().getTimeInMillis();
         } catch (Exception e) {
             throw new RepositoryException(e);
         }
     }
     
     /**
      * Gets the size of the data content of this node if this node is of type resource.
      * @return size in bytes
      * @throws RepositoryException
      */
     public long getSize() throws RepositoryException {
         log.error("Not implemented yet!");
         return -1;
     }
     
     /**
      * Gets the mimetype of the data content of this node if this node is of type resource.
      * @return mimetype
      * @throws RepositoryException
      */
     public String getMimeType() throws RepositoryException {
 	// TODO: check on jcr:content/@jcr:mimeType
         try {
             if (jcrNode.hasProperty("mimeType")) {
                return jcrNode.getProperty("mimeType").getString();
             }
         } catch (Exception e) {
             throw new RepositoryException(e);
         }
         return null;
     }
     
     /**
      * Sets the mimetype of the data content of this node if this node is of type resource.
      * @param mimeType
      * @throws RepositoryException
      */
     public void setMimeType(String mimeType) throws RepositoryException {
         // TODO: Use a namespace, e.g. yarep:mimeType
 	// TODO: check on jcr:content/@jcr:mimeType
         try {
             jcrNode.setProperty("mimeType", mimeType);
             session.save();
         } catch (Exception e) {
             throw new RepositoryException(e);
         }
     }
     
     /**
      * Gets the encoding of the data content of this node if this node is of type resource.
      * @return encoding
      * @throws RepositoryException
      */
     public String getEncoding() throws RepositoryException {
         log.error("Not implemented yet!");
         return null;
     }
     
     /**
      * Sets the encoding of the data content of this node if this node is of type resource.
      * @param encoding
      * @throws RepositoryException
      */
     public void setEncoding(String encoding) throws RepositoryException {
         log.error("Not implemented yet!");
     }
 
     /**
      *
      */
     public javax.jcr.Node getJCRNode() {
         return jcrNode;
     }
 
     /**
      *
      */
     public javax.jcr.Node getJCRResourceNode() throws Exception {
         return jcrNode.getNode("jcr:content");
     }
 
     /**
      *
      */
     public javax.jcr.Session getJCRSession() {
         return session;
     }
 }
