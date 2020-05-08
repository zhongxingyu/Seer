 package org.wyona.yarep.impl.repo.vfs;
 
 import java.io.File;
 import java.util.Date;
 
 import org.apache.log4j.Category;
 import org.wyona.yarep.core.NoSuchRevisionException;
 import org.wyona.yarep.core.Node;
 import org.wyona.yarep.core.NodeStateException;
 import org.wyona.yarep.core.Property;
 import org.wyona.yarep.core.RepositoryException;
 import org.wyona.yarep.core.Revision;
 
 /**
  */
 public class VirtualFileSystemRevision extends VirtualFileSystemNode implements Revision {
 
     private static Category log = Category.getInstance(VirtualFileSystemRevision.class);
     
     public static final String PROPERTY_REVISION_CREATION_DATE = "yarep_revisionCreationDate";
     public static final String PROPERTY_REVISION_CREATOR = "yarep_revisionCreator";
     public static final String PROPERTY_REVISION_TAG = "yarep_revisionTag";
     public static final String PROPERTY_REVISION_COMMENT = "yarep_revisionComment";
 
     public static final String CONTENT_FILE_NAME = "content";
 
     protected Node node;
     protected String revisionName;
     
     /**
      * Constructor
      * @throws RepositoryException
      */
     public VirtualFileSystemRevision(VirtualFileSystemNode node, String revisionName) throws RepositoryException {
         super(node.getRepository(), node.getPath(), node.getUUID(), false);
        this.node = node;
 
         this.revisionName = revisionName;
         this.contentDir = new File(((VirtualFileSystemRepository)repository).getContentDir(), this.uuid + META_DIR_SUFFIX + File.separator + REVISIONS_BASE_DIR + File.separator + this.revisionName);
         this.contentFile = new File(this.contentDir, CONTENT_FILE_NAME);
 
 /*
         this.metaDir = this.contentDir;
         this.metaFile = new File(this.metaDir, META_FILE_NAME);
 */
     
         if (log.isDebugEnabled()) {
             log.debug("VirtualFileSystemRevision: path=" + path + " uuid=" + uuid + " revisionName=" + revisionName);
             log.debug("contentDir=" + contentDir);
             log.debug("contentFile=" + contentFile);
 /*
             log.debug("metaDir=" + metaDir);
             log.debug("metaFile=" + metaFile);
 */
         }
 
 /*
         if (!metaFile.exists()) {
             throw new RepositoryException("Meta file " + metaFile + " does not exist.");
         }
         readProperties();
 */
     }
     
     /**
      * @see org.wyona.yarep.impl.repo.fs.VirtualFileSystemNode#addNode(java.lang.String, int)
      */
     public Node addNode(String name, int type) throws RepositoryException {
         throw new RepositoryException("cannot call this method on a revision");
     }
     
     /**
      * @see org.wyona.yarep.impl.repo.fs.VirtualFileSystemNode#checkin()
      */
     public Revision checkin() throws NodeStateException, RepositoryException {
         throw new RepositoryException("cannot call this method on a revision");
     }
     
     /**
      * @see org.wyona.yarep.impl.repo.fs.VirtualFileSystemNode#checkout(java.lang.String)
      */
     public void checkout(String userID) throws NodeStateException, RepositoryException {
         throw new RepositoryException("cannot call this method on a revision");
     }
     
     protected Revision createRevision() throws RepositoryException {
         throw new RepositoryException("cannot call this method on a revision");
     }
     
     /**
      * @see org.wyona.yarep.impl.repo.fs.VirtualFileSystemNode#restore(java.lang.String)
      */
     public void restore(String revisionName) throws NoSuchRevisionException, RepositoryException {
         throw new RepositoryException("cannot call this method on a revision");
     }
     
     /**
      * @see org.wyona.yarep.core.Revision#getCreationDate()
      */
     public Date getCreationDate() throws RepositoryException {
        return new Date(node.getLastModified());
     }
 
     /**
      * Sets the creation date of this revision.
      * @param date
      * @throws RepositoryException
      */
     public void setCreationDate(Date date) throws RepositoryException {
         setProperty(PROPERTY_REVISION_CREATION_DATE, date);
     }
 
     /**
      * @see org.wyona.yarep.core.Revision#getCreator()
      */
     public String getCreator() throws RepositoryException {
        return "unknown";
     }
 
     /**
      * Sets the creator of this revision.
      * @param creator user id
      * @throws RepositoryException
      */
     public void setCreator(String creator) throws RepositoryException {
         setProperty(PROPERTY_REVISION_CREATOR, creator);
     }
 
     /**
      * @see org.wyona.yarep.core.Revision#getComment()
      */
     public String getComment() throws RepositoryException {
        return "no comment";
     }
 
     /**
      * Sets the comment about this revision.
      * @param comment
      * @throws RepositoryException
      */
     public void setComment(String comment) throws RepositoryException {
         setProperty(PROPERTY_REVISION_COMMENT, comment);
     }
 
     /**
      * @see org.wyona.yarep.core.Revision#getTag()
      */
     public String getTag() throws RepositoryException {
         Property tag = getProperty(PROPERTY_REVISION_TAG);
         if (tag == null) return null;
         return tag.getString();
     }
 
     /**
      * @see org.wyona.yarep.core.Revision#setTag(java.lang.String)
      */
     public void setTag(String tag) throws RepositoryException {
         setProperty(PROPERTY_REVISION_TAG, tag);
     }    
        
     /**
      * @see org.wyona.yarep.core.Revision#hasTag()
      */
     public boolean hasTag() throws RepositoryException {
         return hasProperty(PROPERTY_REVISION_TAG);
     }    
        
     /**
      * @see org.wyona.yarep.core.Revision#getRevisionName()
      */
     public String getRevisionName() throws RepositoryException {
         return this.revisionName;
     }
 
     /**
      *
      */
     public String toString() {
         String s = "";
         try {
            s = s + getName() + ", " + getRevisionName() + ", " + getComment() + ", " + getCreator() + ", " + getCreationDate();
         } catch (Exception e) {
             log.error(e.getMessage(), e);
             s = s + e.getMessage();
         }
         return s;
     }
 }
