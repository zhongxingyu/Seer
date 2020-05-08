 // cinnamon - the Open Enterprise CMS project
 // Copyright (C) 2007-2009 Horner GmbH (http://www.horner-project.eu)
 // 
 // This library is free software; you can redistribute it and/or
 // modify it under the terms of the GNU Lesser General Public
 // License as published by the Free Software Foundation; either
 // version 2.1 of the License, or (at your option) any later version.
 // 
 // This library is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 // Lesser General Public License for more details.
 // 
 // You should have received a copy of the GNU Lesser General Public
 // License along with this library; if not, write to the Free Software
 // Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 // (or visit: http://www.gnu.org/licenses/lgpl.html)
 
 package server;
 
 import org.apache.commons.compress.archivers.ArchiveOutputStream;
 import org.apache.commons.compress.archivers.ArchiveStreamFactory;
 import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
 import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
 import org.apache.commons.compress.utils.IOUtils;
 import org.dom4j.Document;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 import org.dom4j.Node;
 import org.hibernate.Hibernate;
 import org.hibernate.annotations.Type;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import server.dao.*;
 import server.data.ObjectSystemData;
 import server.data.Validator;
 import server.data.ZippedFolder;
 import server.exceptions.CinnamonException;
 import server.global.Conf;
 import server.global.ConfThreadLocal;
 import server.global.Constants;
 import server.helpers.MetasetService;
 import server.helpers.ObjectTreeCopier;
 import server.index.IndexAction;
 import server.index.IndexJob;
 import server.index.Indexable;
 import server.index.LuceneBridge;
 import server.interfaces.IMetasetJoin;
 import server.interfaces.IMetasetOwner;
 import server.interfaces.XmlConvertable;
 import utils.HibernateSession;
 import utils.ParamParser;
 
 import javax.persistence.*;
 import java.io.*;
 import java.util.*;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 @Entity
 @Table(name = "folders",
 		uniqueConstraints = {@UniqueConstraint(columnNames={"name", "parent_id"})}
 )
 public class Folder
 	implements Serializable, Ownable, Indexable, XmlConvertable, IMetasetOwner {
 
 	private static final long	serialVersionUID	= 1L;
 	static final DAOFactory daoFactory = DAOFactory.instance(DAOFactory.HIBERNATE);
 
 	@Transient
 	private transient Logger log = LoggerFactory.getLogger(this.getClass());
 
 	@Id @GeneratedValue //(strategy=GenerationType.SEQUENCE)
 	@Column(name = "id")
 	private long id;
 
 	@Column(name = "name",
 			length = Constants.NAME_LENGTH,
 			nullable = false)
 	private String name;
 
 	@Column(name = "metadata",
 			length = Constants.METADATA_SIZE,
 			nullable = false
 			)
 	@Type(type="text")
 	private String metadata = "<meta/>";
 
 	@ManyToOne
 	@JoinColumn(name = "owner_id",
 	    nullable = false)
     private User owner;
 
 	@ManyToOne
 	@JoinColumn(name = "parent_id",
 			nullable = true)
 	private Folder parent;
 
 	@OneToMany(mappedBy = "parent")
 	@OrderBy("name")
 	private Set<Folder> children = new LinkedHashSet<Folder>();
 
 	@ManyToOne
 	@JoinColumn(name = "type_id",
 			nullable = false)
 	private FolderType type;
 
 	@ManyToOne
 	@JoinColumn(name = "acl_id",
 			nullable = false)
 	private Acl acl;
 
     @OneToMany(
             mappedBy = "folder",
             cascade = {CascadeType.PERSIST, CascadeType.REMOVE}
     )
     private Set<FolderMetaset> folderMetasets = new HashSet<FolderMetaset>();
 
 	@Version
 	@Column(name="obj_version")
 	@SuppressWarnings("unused")
 	private Long obj_version = 0L;
 
 	public Folder(){
 
 	}
 
     public Folder(Folder that){
         name = that.name;
         owner = that.owner;
         parent = that.parent;
         type = that.type;
         acl = that.acl;
         metadata = that.metadata;
     }
 
 	public Folder(String name, String metadata, Acl acl, Folder parent, User owner, FolderType type){
 		this.name = name;
         if(metadata != null){
 		    setMetadata(metadata);
         }
 		this.acl = acl;
 		this.parent = parent;
 		this.owner = owner;
 		this.type = type;
 	}
 
 	public Folder(Map<String, String> cmd){
 		EntityManager em = HibernateSession.getLocalEntityManager();
 		// TODO: change constructor to receive valid EM
 		FolderDAO folderDao = daoFactory.getFolderDAO(em);
 
 		log.debug("Entering Folder-Constructor");
 
 		this.name = cmd.get("name");
 		this.setMetadata(cmd.get("metadata"));
 		Long parentId= ParamParser.parseLong(cmd.get("parentid"), "error.param.parent_id");
 
 		log.debug("trying to find parent folder with id "+parentId);
 
 
 		if(parentId == 0){
 			this.parent = folderDao.findRootFolder();
 			log.debug("got root folder:  "+parent);
 		}
 		else{
 			Folder myParent = folderDao.get(parentId);
 			if(myParent == null ){ // parent not found
 				throw new CinnamonException("error.parent_folder.not_found");
 			}
 			else{
 				this.parent = myParent;
 			}
 		}
 
 		// typeid is optional
 		if(! cmd.containsKey("typeid")){
 			cmd.put("typeid", "0");
 		}
 		Long folderTypeId = ParamParser.parseLong(cmd.get("typeid"), "error.param.type_id");
 		FolderTypeDAO ftDao = daoFactory.getFolderTypeDAO(em);
 		if(folderTypeId == 0){
 			this.type = ftDao.findByName(Constants.FOLDER_TYPE_DEFAULT);
 		}
 		else{
 			this.type = ftDao.get(folderTypeId);
 		}
 
 		log.debug("check if folder already exists.");
 
     	// check whether folder exists in parent folder
 	    List<Folder> folders = folderDao.findAllByNameAndParentID(name, parent);
     	if(! folders.isEmpty()){         
     		throw new CinnamonException("error.folder.exists");
     	}
 
     	log.debug("set acl for this folder");
     	// Set ACL for this folder:
     	if(cmd.containsKey("aclid")){
     		Long acl_id = ParamParser.parseLong(cmd.get("aclid"), "error.param.acl_id");
     		AclDAO aclDao = daoFactory.getAclDAO(em);
     		this.acl = aclDao.get(acl_id);
     	}
     	else{
     		// a folder normally inherits the parent folder's acl.
     		this.acl = parent.acl;
     	}
 
 		Long ownerId = ParamParser.parseLong(cmd.get("ownerid"), "error.param.owner_id");
 		server.dao.UserDAO userDao = daoFactory.getUserDAO(em);
 		User myOwner = userDao.get(ownerId);
 		if(myOwner == null){
 			throw new CinnamonException("error.user.not_found");
 		}
 		owner = myOwner;
 	}
 
     public Folder(Map<String,String> cmd, User owner){
     	this(cmd);
     	this.owner = owner;
     }
 
 
 	public long getId() {
 		return id;
 	}
 
     /**
      * @return the compiled metadata of this element (all metasets collected under one meta root element).
      */
     public String getMetadata() {
         // for compatibility: return non-empty metadata, otherwise try to compile metasets
 //       if(metadata.length() > 8 && getFolderMetasets().size() == 0){
 //            return metadata;
 //        }
         Document doc = DocumentHelper.createDocument();
         Element root = doc.addElement("meta");
         for(Metaset m : fetchMetasets()){
             root.add(Metaset.asElement("metaset", m));
         }
         return doc.asXML();
     }
 
     /**
      * Set the Metadata on this folder. Tries to parse the submitted string
      * and throws an exception if it is not valid XML.<br/>
      * Note: this parses the meta-xml into metasets and stores them as such.
      * @param metadata the custom metadata
      */
     public void setMetadata(String metadata) {
         setMetadata(metadata, WritePolicy.BRANCH);
     }
 
     /**
      * Set the Metadata on this item. Tries to parse the submitted string
      * and throws an exception if it is not valid XML.<br/>
      * Note: this parses the meta-xml into metasets and stores them as such.
      * This method will unlink existing metasets if they are missing from the metadata,
      * that is, you cannot submit partial metadata to setMetadata. You must set <em>all</em>
      * metadata if you use this method. (see addMetaset to add an individual metaset) 
      * @param metadata the custom metadata
      * @param writePolicy the WritePolicy - how to treat existing metasets.
      */
     public void setMetadata(String metadata, WritePolicy writePolicy) {
         EntityManager em = HibernateSession.getLocalEntityManager();
         if (metadata == null || metadata.trim().length() == 0) {
             this.metadata = "<meta/>";
             for(FolderMetaset fm : getFolderMetasets()){
                 em.remove(fm);
             }
         }
         else {
             MetasetService metasetService = new MetasetService();
             Document doc = ParamParser.parseXmlToDocument(metadata, "error.param.metadata");
             List<Node> sets = doc.selectNodes("//metaset");
             if(sets.size() == 0){
                 this.metadata = metadata;
                 if(folderMetasets.size() > 0){
                     // delete obsolete metasets:
                     for(Metaset m : fetchMetasets()){
                         new MetasetService().unlinkMetaset(this, m);
                     }
                 }
                 return;
             }
 
             Set<MetasetType> currentMetasetMap = new HashSet<MetasetType>();
             for(Metaset metaset : fetchMetasets()){
                 // create a set of the currently existing metasets.
                 currentMetasetMap.add(metaset.getType());
             }
             
             for(Node metasetNode : sets){
                 String content = metasetNode.detach().asXML();
                 String metasetTypeName = metasetNode.selectSingleNode("@type").getText();
                 log.debug("metasetType: "+metasetTypeName);
                 MetasetTypeDAO mtDao = daoFactory.getMetasetTypeDAO(em);
                 MetasetType metasetType = mtDao.findByName(metasetTypeName);
                 if(metasetType == null){
                     throw new CinnamonException("error.unknown.metasetType",metasetTypeName);
                 }
                 metasetService.createOrUpdateMetaset(this,metasetType, content, writePolicy);
                 currentMetasetMap.remove(metasetType);
             }
             for(MetasetType metasetType : currentMetasetMap){
                 // any metaset that was not found in the metadata parameter will be deleted.                    
                 metasetService.unlinkMetaset(this, this.fetchMetaset(metasetType.getName())); // somewhat convoluted.
             }
         }
         // remove legacy metadata
         this.metadata = "<meta />";
     }
 
 	public Folder getParent() {
 		return parent;
 	}
 
 	public void setParent(Folder parent) {
 		this.parent = parent;
 	}
 
 	public void setParent(Long parentId){
 		EntityManager em = HibernateSession.getLocalEntityManager();
 		log.debug("trying to find parent folder with id "+parentId);
 
 		if (parentId == 0) {
 		    FolderDAO folderDao = daoFactory.getFolderDAO(em);
 			parent = folderDao.findRootFolder();
 		}
 		else{
 			Folder myParent = em.find(Folder.class, parentId);
 			if(myParent == null){
 				throw new CinnamonException("error.parent_folder.not_found");
             }
 			parent = myParent;
 		}
 	}
 
 	public void setAcl(Long aclId){
 		EntityManager em = HibernateSession.getLocalEntityManager();
 		AclDAO aclDAO = daoFactory.getAclDAO(em);
 		acl = (aclId == 0) ? aclDAO.findByName(Constants.ACL_DEFAULT) : aclDAO.get(aclId);
 
 		if (acl == null) {
 			throw new CinnamonException("error.acl.not_found");
 		}
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	@SuppressWarnings("unused")
 	private void setId(long id) {
 		this.id = id;
 	}
 
 	public Acl getAcl() {
 		return acl;
 	}
 
 	public void setAcl(Acl acl) {
 		this.acl = acl;
 	}
 
 	static public boolean folderExists(long id){
 		EntityManager em = HibernateSession.getLocalEntityManager();
 		Folder f = em.find(Folder.class, id);
 		return f != null;
 	}
 
 	static public boolean folderExists(Folder folder){
 		EntityManager em = HibernateSession.getLocalEntityManager();
 
 		if (folder == null) {
 			throw new CinnamonException("error.folder.not_found");
 		}
 		long id = folder.getId();
 		Folder f = em.find(Folder.class, id);
 
 		return f != null;
 	}
 
 	public boolean is_rootfolder(){
 		if(getParent() == null){
 			return false;
 		}
 		return getParent().getId() == getId();
 	}
 
 	/**
 	 * @return Folder	The workflow.Server will store its Objects in this Folder.
 	 * @param emx an EntityManager, configured for the current repository.
 	 */
 	// TODO: use DAO
 	static public Folder findWorkflowFolder(EntityManager emx){
 		Logger log = LoggerFactory.getLogger("server.Folder");
 		FolderDAO folderDao = daoFactory.getFolderDAO(emx);
 		Folder root = folderDao.findRootFolder();
 		log.debug("Root-Folder-Id is: "+root.getId());
 
 		Folder f = null;
 		try {
 			Query q = emx.createQuery("select f from Folder f where f.name=:name and f.parent=:parent");
 			q.setParameter("name", Constants.WORKFLOW_FOLDER_NAME);
 			q.setParameter("parent", root);
 			f = (Folder) q.getSingleResult();
 		} catch (Exception e) {
 			log.warn("Could not find workflow-Folder"+e.getMessage());
 		}
 		return f;
 	}
 
 	/**
 	 *
 	 * @return Folder for workflow objects.
 	 */
 	static public Folder findWorkflowFolder(){
 		EntityManager em = HibernateSession.getLocalEntityManager();
 		return findWorkflowFolder(em);
 	}
 
 	public void setChildren(Set<Folder> children) {
 		this.children = children;
 	}
 
 	public Set<Folder> getChildren() {
 		return children;
 	}
 
 	/**
 	 * @return the owner
 	 */
 	public User getOwner() {
 		return owner;
 	}
 
 	/**
 	 * @param owner the owner to set
 	 */
 	public void setOwner(User owner) {
 		this.owner = owner;
 	}
 
 	/**
 	 * Add the folder as XML Element "folder" to the parameter Element.
 	 * @param root the element to which the folder element is appended as a child
      * @return the element which was added to the root param.
 	 */
 	public Element toXmlElement(Element root){
 		Element folder = root.addElement("folder");
 		folder.addElement("id").addText(String.valueOf(getId()) );
 		folder.addElement("name").addText( getName());
 		folder.add(User.asElement("owner", getOwner()));
 		folder.addElement("aclId").addText(String.valueOf(getAcl().getId()));
 		folder.addElement("typeId").addText(String.valueOf(type.getId()));
 		if(getParent() != null){
 			folder.addElement("parentId").addText(String.valueOf(getParent().getId()));
 		}
 		else{
 			folder.addElement("parentId");
 		}
         FolderDAO fDao = daoFactory.getFolderDAO(HibernateSession.getLocalEntityManager());
         if(fDao.getSubfolders(this).isEmpty()){
             folder.addElement("hasChildren").addText("false");
         }
         else{
             folder.addElement("hasChildren").addText("true");
         }
         return folder;
 	}
 
 	/**
 	 * Create a list of folders right up to one step below the root folder (so the
 	 * root folder is not returned).
 	 * @return List of parent folders
 	 */
 	List<Folder> getAncestors(){
 	    List<Folder> folderList = new ArrayList<Folder>();
 	    Folder folder = this;
 		while(folder.getParent() != null && folder.getParent().id != folder.id){
     		folderList.add(folder.getParent());
     		folder = folder.getParent();
     	}
 	    return folderList;
 	}
 
 	@Override
 	public String getContent(String repository) {
 		/*
 		 * Folder.getContent could return a list of children and objects.
 		 * But what would be the purpose of this in a search context?
 		 * Is there a use case for "give me all folders which contain foo"
 		 * instead of searching for all objects/folders named "foo"?
 		 */
 		return "<content/>";
 	}
 
     @Override
     public byte[] getContentAsBytes(String repository){
         return "<content/>".getBytes();
     }
 
 	@Override
 	public String getSystemMetadata() {
 		Document doc = DocumentHelper.createDocument();
 		Element root = doc.addElement("sysMeta");
 		String className = Hibernate.getClass(this).getName();
 		root.addAttribute("javaClass", className);
 		root.addAttribute("hibernateId", String.valueOf(getId()));
 		root.addAttribute("id", uniqueId());
 		toXmlElement(root);
 		return doc.asXML();
 	}
 
 	/**
 	 * Turn a collection of folders into an XML document. Any exceptions encountered during
 	 * serialization are turned into error-Elements which contain the exception's message.
 	 * @param results
 	 * @return Document
 	 */
 	static public Document generateQueryFolderResultDocument(Collection<Folder> results)
 		{
 		Document doc = DocumentHelper.createDocument();
 		Element root = doc.addElement("folders");
 		Logger log = LoggerFactory.getLogger(ObjectSystemData.class);
 
 		for (Folder	folder : results) {
     		Long id= folder.getId();
     		log.debug("working on object: "+id);
     		try {
     			folder.toXmlElement(root);
     		}
     		catch(CinnamonException ex) {
     			log.error("Error serializing folder: " + id + " - " + ex.getMessage());
     			Element error = DocumentHelper.createElement("error").addText(ex.getLocalizedMessage());
     			error.addElement("id").addText(id.toString());
     			root.add(error);
     		}
     	}
 		return doc;
 	}
 
 	/**
 	 *
 	 * @param recursive set to true if subfolders must be included in the list of OSDs.
 	 * @return a List of OSDs in this folder
 	 */
 	public List<ObjectSystemData> getOSDList(Boolean recursive){
 		EntityManager em = HibernateSession.getLocalEntityManager();
 		FolderDAO folderDao = daoFactory.getFolderDAO(em);
 
 		return folderDao.getFolderContent(this, recursive);
 	}
 
 	/**
 	 * @return the type
 	 */
 	public FolderType getType() {
 		return type;
 	}
 
 	/**
 	 * @param type the type to set
 	 */
 	public void setType(FolderType type) {
 		this.type = type;
 	}
 
 	@Override
 	/*
 	 * Implements Comparable interface based on id.
 	 * This is used to generate search results with a consistent ordering (where the
 	 * results are not ordered by any other parameter).
 	 *
 	 */
 	public int compareTo(XmlConvertable o) {
 		if(getId() > o.getId()){
 			return 1;
 		}
 		else if(getId() < o.getId()){
 			return -1;
 		}
 		return 0;
 	}
 
     /**
      * Copy a source folder to a target folder. Copies recursively.
      * @param target the target folder in which the copy is created
      * @param croakOnError if true, do not continue past an error.
      * @param versions must be one of 'all', 'branch', 'head' - determines which OSDs inside the folder tree to copy.
      * @param user the user who issued the copyFolder command. This user is the new owner of the copy and he is also
      * used for permission checks.
      * @return a CopyResult object containing information about new folders and objects as well as error messages.
      */
     public CopyResult copyFolder(Folder target, Boolean croakOnError, String versions, User user) {
         /*
          * validate read permissions on source folder
          * validate write permissions on target folder
          * create folder in target folder
          * create a CopyResult object
          * this folder has sub folders? Copy them. Add their CopyResult to the existing one.
          * this folder has OSDs? Copy them.
          * 
          * 
          */
         CopyResult copyResult = new CopyResult();
         Validator validator = new Validator(user);
         try{
             // we need permission to browse this folder.
             validator.validateGetFolder(this);
         }
         catch (CinnamonException ce){
             return copyResult.addFailure(this, ce);
         }
 
         try{
             // we need the permission to create a folder inside the target folder.
             validator.validateCreateFolder(target);
 
         }
         catch (CinnamonException ce){
             return copyResult.addFailure(target, ce);
         }
 
         Folder copy = new Folder(this);
         copy.owner = user;
         copy.parent = target;
 
         EntityManager em = HibernateSession.getLocalEntityManager();
         FolderDAO fDao = daoFactory.getFolderDAO(em);
         fDao.makePersistent(copy);
         copyResult.addFolder(copy);
 
         // copy child folders
         List<Folder> children = fDao.getSubfolders(this);
         for(Folder child : children){
             CopyResult cr = child.copyFolder(copy, croakOnError, versions, user);
             copyResult.addCopyResult(cr);
             if(copyResult.foundFailure() && croakOnError){
                 return copyResult;
             }
         }
 
         ObjectSystemDataDAO oDao = daoFactory.getObjectSystemDataDAO(em);
         // copy content
         Collection<ObjectSystemData> folderContent = oDao.findAllByParent(this);
         log.debug("folderContent contains "+folderContent.size()+" objects.");
 
         if(versions.equals("all")){
             log.debug("copy all versions");
             // copy all versions
             ObjectTreeCopier otc = new ObjectTreeCopier( user, copy, validator, true);
             copyResult.addCopyResult( copyAllVersions(folderContent, otc, croakOnError));
         }
         else if(versions.equals("branch")){
             log.debug("copy newest branch objects");
             Set<ObjectSystemData> branches = new HashSet<ObjectSystemData>();
             for(ObjectSystemData osd: folderContent){
                 branches.add( oDao.findLatestBranch(osd) );
             }
             copyResult.addCopyResult( createNewVersionCopies(branches, copy, validator, user, croakOnError));
         }
         else{
             log.debug("copy head of object tree");
             // the default: copy head
             Set<ObjectSystemData> headSet = new HashSet<ObjectSystemData>();
             for(ObjectSystemData head : folderContent){
                 ObjectSystemData latestHead = oDao.findLatestHead(head);
                 log.debug("latestHead found for "+head.getId());
                 headSet.add( latestHead );
             }
             copyResult.addCopyResult( createNewVersionCopies(headSet, copy, validator, user, croakOnError));
         }
         log.debug("new folders: "+copyResult.newFolderCount());
         log.debug("new objects: "+copyResult.newObjectCount());
         return copyResult;
     }
 
     /**
      * Copy all versions of the objects found in a folder. This will create the complete object tree of
      * the objects, so if an object has ancestors or descendants in other folders, those will be copied, too.
      * @param folderContent the content of the folder which should be copied completely.
      * @param otc a ObjectTreeCopier which is configured with a validator and correct activeUser.
      * @param croakOnError if true, stop in case of an error and return a CopyResult which contains the events so far.
      * @return a copyResult containing a collection of all failed and successful attempts at copying the
      * folder's contents.
      */
     CopyResult copyAllVersions(Collection<ObjectSystemData> folderContent, ObjectTreeCopier otc, Boolean croakOnError){
         ObjectSystemDataDAO oDao = daoFactory.getObjectSystemDataDAO(HibernateSession.getLocalEntityManager());
         CopyResult copyResult = new CopyResult();
 
         ConcurrentLinkedQueue<ObjectSystemData> conQueue = new ConcurrentLinkedQueue<ObjectSystemData>();
         conQueue.addAll(folderContent);
         log.debug("starting to copy "+conQueue.size()+" objects");
 
         for (ObjectSystemData source : conQueue) {
 //            otc.resetCopyResult();
             try {
                 // create a full copy of the whole object tree:
                 otc.createFullCopy(source);
                 copyResult.addCopyResult(otc.getCopyResult());
             }
             catch (Exception ex) {
                 log.debug("objectTreeCopy failed for id " + source.getId(), ex);
                 // copy failed - now we have to cleanup and remove the already created copies:
                 ObjectSystemData brokenCopy = otc.getCopyCache().get(source);
                 if (brokenCopy != null) {
                     // we should nuke all other objects with the same root,
                     // as they won't be amendable to a copy operation either.
                     for (ObjectSystemData osd : conQueue) {
                         if (osd.getRoot().equals(brokenCopy.getRoot())) {
                             conQueue.remove(osd);
                         }
                     }
 
                     // recursively delete the broken object tree.
                     oDao.delete(brokenCopy.getRoot(), true, true);
                 }
 
                 log.debug("cleanup complete.");
                 copyResult.addFailure(source, new CinnamonException(ex));
                 if(croakOnError){
                     return copyResult;
                 }
             }
         }
         return copyResult;
     }
 
     CopyResult createNewVersionCopies(Collection<ObjectSystemData> sources, Folder target, Validator validator, User user, Boolean croakOnError) {
         CopyResult copyResult = new CopyResult();
         ObjectSystemDataDAO oDao = daoFactory.getObjectSystemDataDAO(HibernateSession.getLocalEntityManager());
 
         for (ObjectSystemData osd : sources) {
             log.debug("trying to copy "+osd.getId());
             // check permissions:
             try {
                 validator.validateCopy(osd, target);
             }
             catch (Exception ex) {
                 copyResult.addFailure(osd, new CinnamonException(ex));
                 if(croakOnError){
                     return copyResult;
                 }
             }
 
             ObjectSystemData newCopy = new ObjectSystemData(osd, user);
             newCopy.setVersion("1");
             newCopy.setRoot(newCopy);
             newCopy.setParent(target);
             oDao.makePersistent(newCopy);
             osd.copyContent(newCopy);
             copyResult.addObject(newCopy);
         }
         return copyResult;
     }
 
     /**
      * Compute this folder's path, which is the combination of all parent folder's names,
      * for example: /system/workflows/templates.
      * @return the path of this folder.
      */
     public String fetchPath(){
         /*
          * getParentFolders returns: "c/b/a" for folders /a/b/c
         */
         FolderDAO folderDao = daoFactory.getFolderDAO(HibernateSession.getLocalEntityManager());
         List<Folder> folders = folderDao.getParentFolders(this);
         StringBuilder path = new StringBuilder();
         Collections.reverse(folders);
         folders.add(this);
         for(Folder f : folders){
             path.append('/');
             path.append(f.getName());
         }
         return path.toString();
     }
 
     /**
      * Create a zipped folder containing those OSDs and subfolders (recursively) which the
      * validator allows. <br/>
      * Zip file encoding compatibility is difficult to achieve.<br/>
      * Using Cp437 as encoding will generate zip archives which can be unpacked with MS Windows XP
      * system utilities and also with the Linux unzip tool v6.0 (although the unzip tool will list them
      * as corrupted filenames with "?" in place for the special characters, it should unpack them
      * correctly). In tests, 7zip was unable to unpack those archives without messing up the filenames
      * - it requires UTF8 as encoding, as far as I can tell.<br/>
      * see: http://commons.apache.org/compress/zip.html#encoding<br/>
      * to manually test this, use: https://github.com/dewarim/GrailsBasedTesting
      * @param folderDao data access object for Folder objects
      * @param latestHead if set to true, only add objects with latestHead=true, if set to false include only
      *                   objects with latestHead=false, if set to null: include everything regardless of
      *                   latestHead status.
      * @param latestBranch if set to true, only add objects with latestBranch=true, if set to false include only
      *                   objects with latestBranch=false, if set to null: include everything regardless of
      *                     latestBranch status.
      * @param validator a Validator object which should be configured for the current user to check if access
      *                  to objects and folders inside the given folder is allowed. The content of this folder
      *                  will be filtered before it is added to the archive.
      * @return the zip archive of the given folder
      */
     public ZippedFolder createZippedFolder(FolderDAO folderDao,
                                    Boolean latestHead, Boolean latestBranch,
                                    Validator validator){
         String repositoryName = HibernateSession.getLocalRepositoryName();
         final File sysTempDir = new File(System.getProperty("java.io.tmpdir"));
         File tempFolder = new File(sysTempDir, UUID.randomUUID().toString());
         if (!tempFolder.mkdirs()) {
             throw new CinnamonException(("error.create.tempFolder.fail"));
         }       
         
         List<Folder> folders = new ArrayList<Folder>();
         folders.add(this);
         folders.addAll(folderDao.getSubfolders(this, true));
         folders = validator.filterUnbrowsableFolders(folders);
         log.debug("# of folders found: "+folders.size());
         // create zip archive:
         ZippedFolder zippedFolder;
         try {
             File zipFile = File.createTempFile("cinnamonArchive", "zip");
             zippedFolder = new ZippedFolder(zipFile, this);
             
             final OutputStream out = new FileOutputStream(zipFile);
             ZipArchiveOutputStream zos = (ZipArchiveOutputStream) new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.ZIP, out);
             String encoding = ConfThreadLocal.getConf().getField("zipFileEncoding", "Cp437");
 
             log.debug("current file.encoding: "+System.getProperty("file.encoding"));
             log.debug("current Encoding for ZipArchive: "+zos.getEncoding()+"; will now set: "+encoding);
             zos.setEncoding(encoding);
             zos.setFallbackToUTF8(true);
             zos.setCreateUnicodeExtraFields(ZipArchiveOutputStream.UnicodeExtraFieldPolicy.ALWAYS);
 
             for (Folder folder : folders) {
                
                 String path =  folder.fetchPath().replace(fetchPath(), name); // do not include the parent folders up to root.
                 log.debug("zipFolderPath: "+path);
                 File currentFolder = new File(tempFolder, path);
                 if (!currentFolder.mkdirs()) {
                     // log.warn("failed  to create folder for: "+currentFolder.getAbsolutePath());
                 }
                 List<ObjectSystemData> osds = validator.filterUnbrowsableObjects(
                         folderDao.getFolderContent(folder, false, latestHead, latestBranch));
                 if(osds.size() > 0){
                     zippedFolder.addToFolders(folder); // do not add empty folders as they are excluded automatically.
                 }                
                 for (ObjectSystemData osd : osds) {                    
                     if (osd.getContentSize() == null) {
                         continue;
                     }
                     zippedFolder.addToObjects(osd);
                     File outFile = osd.createFilenameFromName(currentFolder);
                     // the name in the archive should be the path without the temp folder part prepended.
                     String zipEntryPath = outFile.getAbsolutePath().replace(tempFolder.getAbsolutePath(), "");
                     if(zipEntryPath.startsWith(File.separator)){
                         zipEntryPath = zipEntryPath.substring(1);
                     }
                     log.debug("zipEntryPath: "+zipEntryPath);
 
                     zipEntryPath = zipEntryPath.replaceAll("\\\\","/");
                     zos.putArchiveEntry(new ZipArchiveEntry(zipEntryPath));
                     IOUtils.copy(new FileInputStream(osd.getFullContentPath(repositoryName)), zos);
                     zos.closeArchiveEntry();
                 }
             }
             zos.close();
         } catch (Exception e) {
             log.debug("Failed to create zipFolder:", e);
             throw new CinnamonException("error.zipFolder.fail", e.getLocalizedMessage());
         }
         return zippedFolder;
     }
 
     public Boolean hasXmlContent(){
         return false;
     }
 
     public Set<FolderMetaset> getFolderMetasets() {
         return folderMetasets;
     }
 
     public void setFolderMetasets(Set<FolderMetaset> folderMetasets) {
         this.folderMetasets = folderMetasets;
     }
 
     public Set<Metaset> fetchMetasets(){
         Set<Metaset> metasets = new HashSet<Metaset>(getFolderMetasets().size());
         for(FolderMetaset fm : getFolderMetasets()){
             metasets.add(fm.getMetaset());
         }
         return metasets;
     }
 
     /**
      * Fetch a metaset by its given name. Returns null in case there is no such metaset
      * associated with this object.
      * @param name the name of the metaset
      * @return the metaset or null
      */
     public Metaset fetchMetaset(String name){
         Metaset metaset = null;
         for(Metaset m: fetchMetasets()){
             if(m.getType().getName().equals(name)){
                 metaset = m;
                 break;
             }
         }
         return metaset;
     }
 
     public IMetasetJoin fetchMetasetJoin(MetasetType type){
         EntityManager em = HibernateSession.getLocalEntityManager();
         Query q = em.createQuery("select o from FolderMetaset o where o.metaset.type=:metasetType and o.folder=:folder");
         q.setParameter("metasetType", type);
         q.setParameter("folder", this);
         return (IMetasetJoin) q.getSingleResult();
     }
 
     public void addMetaset(Metaset metaset){
         FolderMetaset om = new FolderMetaset(this, metaset);
         EntityManager em = HibernateSession.getLocalEntityManager();
         em.persist(om);
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof Folder)) return false;
 
         Folder folder = (Folder) o;
 
         if (acl != null ? !acl.equals(folder.acl) : folder.acl != null) return false;
         if (metadata != null ? !metadata.equals(folder.metadata) : folder.metadata != null) return false;
         if (name != null ? !name.equals(folder.name) : folder.name != null) return false;
         if (owner != null ? !owner.equals(folder.owner) : folder.owner != null) return false;
         if (parent != null ? !parent.equals(folder.parent) : folder.parent != null) return false;
         if (type != null ? !type.equals(folder.type) : folder.type != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = name != null ? name.hashCode() : 0;
         result = 31 * result + (metadata != null ? metadata.hashCode() : 0);
         result = 31 * result + (owner != null ? owner.hashCode() : 0);
         return result;
     }
 
     public void updateIndex(){
         EntityManager em = HibernateSession.getLocalEntityManager();
         IndexJobDAO jobDAO = daoFactory.getIndexJobDAO(em);
         IndexJob indexJob = new IndexJob(this);
         jobDAO.makePersistent(indexJob);
     }
     
     @PostUpdate
     public void updateIndexOnCommit(){
         LocalRepository.addIndexable(this, IndexAction.UPDATE);
     } 
     
     @PostPersist
     public void addToIndexOnCommit(){
         LocalRepository.addIndexable(this, IndexAction.ADD);
     }
 
     @PostRemove
     public void removeFromIndex(){
         LocalRepository.addIndexable(this, IndexAction.REMOVE);
     }
   
     @Override
     public Long myId(){
         return id;
     }
     
     @Override
     public Indexable reload(){
         EntityManager em = HibernateSession.getLocalEntityManager();
        FolderDAO fDao = daoFactory.getFolderDAO(em);
        return fDao.get(this.getId());
     }
 
     @Override
     public String uniqueId() {
         String className = Hibernate.getClass(this).getName();
         return className + "@" + getId();
     }
 }
