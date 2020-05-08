 // cinnamon - the Open Enterprise CMS project
 // Copyright (C) 2007 Dr.-Ing. Boris Horner
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
 
 package server.data;
 
 import org.dom4j.Document;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 import org.dom4j.Node;
 import org.hibernate.annotations.Type;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import server.*;
 import server.dao.*;
 import server.exceptions.CinnamonConfigurationException;
 import server.exceptions.CinnamonException;
 import server.global.Conf;
 import server.global.ConfThreadLocal;
 import server.global.Constants;
 import server.global.PermissionName;
 import server.helpers.MetasetService;
 import server.helpers.ObjectTreeCopier;
 import server.i18n.Language;
 import server.index.Indexable;
 import server.interfaces.IMetasetJoin;
 import server.interfaces.IMetasetOwner;
 import server.interfaces.XmlConvertable;
 import server.lifecycle.LifeCycleState;
 import server.references.Link;
 import server.references.LinkService;
 import utils.FileKeeper;
 import utils.HibernateSession;
 import utils.ParamParser;
 
 import javax.persistence.*;
 import java.io.File;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.*;
 
 @Entity
 @Table(name = "objects")
 public class ObjectSystemData
         implements Serializable, Ownable, Indexable, XmlConvertable, IMetasetOwner {
     private static final long serialVersionUID = 1L;
     public static final String defaultXmlFormatList = "xml|xhtml|dita|ditamap";
 
     static final DAOFactory daoFactory = DAOFactory.instance(DAOFactory.HIBERNATE);
     @Transient
     transient Logger log = LoggerFactory.getLogger(this.getClass());
 
     @Column(name = "contentpath",
             length = 255,// length 255 should be enough, as we use a 128 Bit UUID for the file/folder structure.
             nullable = true
     )
     private String contentPath = null;
 
     @Column(name = "contentsize",
             nullable = true)
     private Long contentSize = null;
 
     @Column(name = "name",
             length = Constants.NAME_LENGTH,
             nullable = false)
     private String name = "";
 
     @Column(name = "index_ok",
             nullable = true)
     private Boolean indexOk = true; // a new object should always be valid and indexed.
 
     @OneToOne
     @JoinColumn(name = "pre_id",
             nullable = true)
     private ObjectSystemData predecessor;
 
     @ManyToOne
     @JoinColumn(name = "root_id",
             nullable = true)
     // IMPLEMENTATION: workflow-objects set root to null.
     // TODO: fix workflow: root_id should be now NOT NULL.
     // TODO: fix OSD creation.
     private ObjectSystemData root;
 
     @Id
     @GeneratedValue
     @Column(name = "id")
     private long id = 0;
 
     @ManyToOne
     @JoinColumn(name = "creator_id",
             nullable = false)
     private User creator;
 
     @ManyToOne
     @JoinColumn(name = "locked_by",
             nullable = true)
     private User locked_by;
 
     @ManyToOne
     @JoinColumn(name = "modifier_id",
             nullable = false)
     private User modifier;
 
     @ManyToOne
     @JoinColumn(name = "owner_id",
             nullable = true)
     private User owner;
 
     @Temporal(TemporalType.TIMESTAMP)
     @Column(name = "created",
             nullable = false)
     private Date created = new Date();
 
     @Temporal(TemporalType.TIMESTAMP)
     @Column(name = "indexed",
             nullable = true)
     private Date indexed = null;
 
     @Temporal(TemporalType.TIMESTAMP)
     @Column(name = "modified",
             nullable = false)
     private Date modified = new Date();
 
     @ManyToOne
     @JoinColumn(name = "lang_id",
             nullable = false)
     private Language language;
 
     @ManyToOne
     @JoinColumn(name = "acl_id",
             nullable = false)
     private Acl acl;
 
     @ManyToOne
     @JoinColumn(name = "parent_id",
             nullable = false)
     private Folder parent;
 
     //private ObjectSystemData pre_;
     @ManyToOne
     @JoinColumn(name = "format_id",
             nullable = true)
     private server.Format format;
 
     @ManyToOne
     @JoinColumn(name = "type_id",
             nullable = false)
     private ObjectType type;
 
     @Column(name = "appname",
             length = 255)
     private String appName = "";
 
     @Column(name = "metadata",
             length = Constants.METADATA_SIZE,
             nullable = false
     )
     @Type(type = "text")
     private String metadata = "<meta/>";
 
     @Column(name = "procstate",
             length = 128)
     private String procstate;
 
     @Column(name = "latesthead",
             nullable = false)
     private Boolean latestHead;
 
     @Column(name = "latestbranch",
             nullable = false)
     private Boolean latestBranch = true;
 
     @Column(name = "version",
             length = 128,
             nullable = false)
     private String version;
 
     @Version
     @Column(name = "obj_version")
     @SuppressWarnings("unused")
     private Long obj_version = 0L;
 
     @ManyToOne
     @JoinColumn(name = "lifecycle_state_id",
             nullable = true)
     private LifeCycleState state;
 
     @OneToMany(
             mappedBy = "osd",
             cascade = {CascadeType.PERSIST, CascadeType.REMOVE}
     )
     @OrderBy("id")
     private Set<OsdMetaset> osdMetasets = new HashSet<OsdMetaset>();
 
     public ObjectSystemData() {
 
     }
 
     /**
      * Create an OSD with all default settings and version 1.
      * Caller must only supply three things:
      * 1. the name;
      * 2. the user who is going to be owner, modifier and creator;
      * 3) the target folder wherein to create the object.
      * Everything else is set to default and can be modified after instantiation.
      *
      * @param name         the name of the object.
      * @param user         the user who is to be the creator, owner and modifier.
      * @param parentFolder the folder where the object will be created. The object will
      *                     inherit this folder's ACL by default.
      */
     public ObjectSystemData(String name, User user, Folder parentFolder) {
         EntityManager em = HibernateSession.getLocalEntityManager();
         this.name = name;
         log.debug("set version label = 1");
         version = "1";
         latestHead = true;
 
         log.debug("set root");
         setRoot(this);
 
         log.debug("set default objectType");
         ObjectTypeDAO otDao = daoFactory.getObjectTypeDAO(em);
         type = otDao.findByName(Constants.OBJTYPE_DEFAULT);
 
         log.debug("set parentfolder");
         parent = parentFolder;
 
         log.debug("set acl to parent-folder's acl");
         setAcl(getParent().getAcl());
         procstate = Constants.PS_LABEL_CREATED;
 
         log.debug("set language to 'und' (undetermined)");
         LanguageDAO langDao = daoFactory.getLanguageDAO(em);
         Language lang = langDao.findByIsoCode("und");
         setLanguage(lang);
 
         log.debug("set owner, modifier, creator to " + user.getName());
         setOwner(user);
         setModifier(user);
         setCreator(user);
     }
 
     public ObjectSystemData(Map<String, Object> cmd, User user, boolean initFromPredecessor) {
         EntityManager em = HibernateSession.getLocalEntityManager();
         ObjectSystemDataDAO osdDao = daoFactory.getObjectSystemDataDAO(em);
 
         log.debug("predecessor-init");
         if (cmd.containsKey("preid")) {
             Long preId = ParamParser.parseLong((String) cmd.get("preid"), "error.param.pre_id");
             ObjectSystemData pred = osdDao.get(preId);
             if (pred == null) {
                 throw new CinnamonException("error.predecessor.not_found");
             }
 
             if (initFromPredecessor) {
                 name = pred.getName();
                 parent = pred.getParent();
                 metadata = pred.getMetadata();
                 appName = pred.getAppName();
                 language = pred.getLanguage();
                 if (pred.getState() != null) {
                     state = pred.getState().getLifeCycleStateForCopy();
                 }
             }
             this.predecessor = pred;
             /*
                 * because sys is now the latest obj in the branch,
                 * its predecessor has to loose the latestBranch flag.
                 */
             predecessor.setLatestBranch(false);
             predecessor.setLatestHead(false);
 //			log.debug("flushing after setting predecessor.branches");
 
         }
 
         log.debug("set version label");
         version = createNewVersionLabel();
         log.debug("new version: " + version);
         latestHead = !version.contains(".");
 
         log.debug("set root");
         if (predecessor == null) {
             setRoot(this);
         } else {
             setRoot(predecessor.getRoot());
         }
 
         log.debug("set name,appname,metadata");
         if (cmd.containsKey("name")) {
             setName((String) cmd.get("name"));
         }
         if (cmd.containsKey("appname")) {
             setAppName((String) cmd.get("appname"));
         }
 
         /*
            * Set ObjectType:
            * 1. by objtype_id
            * 2. by objtype string
            * 3. by predecessor
            * 4. default_object_type.
            */
         log.debug("set objectType");
         ObjectTypeDAO otDao = daoFactory.getObjectTypeDAO(em);
         if (cmd.containsKey("objtype_id")) {
 
             Long otId = ParamParser.parseLong((String) cmd.get("objtype_id"), "error.param.objtype_id");
             this.type = otDao.get(otId);
         } else if (cmd.containsKey("objtype")) {
             ObjectType objectType = otDao.findByName((String) cmd.get("objtype"));
             if (objectType == null) {
                 throw new CinnamonException("error.param.objtype");
             } else {
                 this.type = objectType;
             }
         } else if (predecessor != null) {
             this.type = predecessor.getType();
         } else {
             this.type = otDao.findByName(Constants.OBJTYPE_DEFAULT);
         }
 
         log.debug("set parentfolder");
         if (cmd.containsKey("parentid")) {
             Long parent_id = Long.parseLong((String) cmd.get("parentid"));
             if (parent_id != 0) {
                 FolderDAO folderDAO = daoFactory.getFolderDAO(em);
                 parent = folderDAO.get(parent_id);
                 if (parent != null) {
                     setParent(parent);
                 } else {
                     throw new CinnamonException("error.parent_folder.not_found");
                 }
 
             } else { // parent_id == 0
                 FolderDAO folderDao = daoFactory.getFolderDAO(em);
                 Folder rootFolder = folderDao.findRootFolder();
                 setParent(rootFolder);
             }
         } else if (parent == null) {
             // note: parent may be set from predecessor.
             throw new CinnamonException("error.parent_folder.not_found");
         }
 
         log.debug("set format");
         FormatDAO formatDao = daoFactory.getFormatDAO(em);
         if (cmd.containsKey("format_id")) {
             Long formatId = ParamParser.parseLong((String) cmd.get("format_id"), "error.param.format_id");
             Format format = formatDao.get(formatId);
             setFormat(format);
         } else if (cmd.containsKey("format")) {
             Format format = formatDao.findByName((String) cmd.get("format"));
             setFormat(format);
         }
 
 
         log.debug("set acl");
         if (cmd.containsKey("acl_id")) {
             AclDAO aclDao = daoFactory.getAclDAO(em);
             Long aclId = ParamParser.parseLong((String) cmd.get("acl_id"), "error.param.acl_id");
             Acl acl = aclDao.get(aclId);
             setAcl(acl);
         } else {
             // if no sepcific acl is given, use the parent folder's acl
             log.debug("set acl to parent-folder's acl");
             setAcl(getParent().getAcl());
         }
 
 
         procstate = Constants.PS_LABEL_CREATED;
 
         /*
            * Set language to language_id or to 'und' if language is null.
            */
         log.debug("set language");
         LanguageDAO langDao = daoFactory.getLanguageDAO(em);
         if (cmd.containsKey("language_id")) {
             Long langId = ParamParser.parseLong((String) cmd.get("language_id"),
                     "error.param.language_id");
             Language lang = langDao.get(langId);
             if (lang == null) {
                 throw new CinnamonException("error.param.language_id");
             }
             setLanguage(lang);
         } else if (getLanguage() == null) {
             Language lang = langDao.findByIsoCode("und");
             setLanguage(lang);
         }
 
         log.debug("set owner, modifier, creator to " + user.getName());
         setOwner(user);
         setModifier(user);
         setCreator(user);
 
         if (cmd.containsKey("metadata")) { // must come last, because MetasetService may persist this object.
             setMetadata((String) cmd.get("metadata"));
         }
     }
 
     /**
      * Create a new OSD based upon "that". The lifecycle state (if not null) is set to the default lifecycle state.
      * Root, predecessor, format and locked_by are set to null,
      * version is also 0. You MUST set those to the correct values.
      *
      * @param that the source OSD
      * @param user the active user who will be set as creator / modifier.
      */
     public ObjectSystemData(ObjectSystemData that, User user) {
         acl = that.getAcl();
         appName = that.getAppName();
         created = Calendar.getInstance().getTime();
         creator = user;
         owner = user;
         modified = Calendar.getInstance().getTime();
         modifier = user;
 //        format = that.getFormat();
         language = that.getLanguage();
         latestHead = that.getLatestHead();
         latestBranch = that.getLatestBranch();
         locked_by = null;
         metadata = that.getMetadata();
         name = that.getName();
         parent = that.getParent();
         predecessor = null;
         procstate = that.getProcstate();
         type = that.getType();
         version = "0";
 
         if (that.getState() != null) {
             state = that.getState().getLifeCycleStateForCopy();
         }
     }
 
     public String getContentPath() {
         return contentPath;
     }
 
     /**
      * Set the contentPath directly (and <em>NOT</em> the contentSize). Only for internal use.
      *
      * @param contentPath relative path to this object's content
      */
     private void setContentPath(String contentPath) {
         this.contentPath = contentPath;
     }
 
     /**
      * Set the contentPath <em>and the contentSize</em>, if the former is a valid String which can
      * be mapped to a valid File.
      *
      * @param contentPath relative path to this object's content. The full path is computed from $cinnamon_data
      *                    as set in cinnamon_config.xml) and $repository_name.
      * @param repository  name of the this OSDs repository..
      */
     public void setContentPath(String contentPath, String repository) {
         this.contentPath = contentPath;
         if (contentPath != null) {
             this.contentSize = contentPath.length() > 0 ? (new File(getFullContentPath(repository))).length() : 0;
         } else {
             this.contentSize = null;
         }
     }
 
     public Acl getAcl() {
         return acl;
     }
 
     public void setAcl(Acl acl) {
         this.acl = acl;
     }
 
     public String getAppName() {
         return appName;
     }
 
     public void setAppName(String appName) {
         this.appName = appName;
     }
 
     public Long getContentSize() {
         return contentSize;
     }
 
     public void setContentSize(Long contentSize) {
         this.contentSize = contentSize;
     }
 
     public Format getFormat() {
         return format;
     }
 
     public void setFormat(Format format) {
         this.format = format;
     }
 
     public long getId() {
         return id;
     }
 
     @SuppressWarnings(value = {"unused"})
     private void setId(long id) {
         this.id = id;
     }
 
     /**
      * @return the compiled metadata of this element (all metasets collected under one meta root element).
      */
     public String getMetadata() {
         // for compatibility: return non-empty metadata, otherwise try to compile metasets
         if (metadata.length() > 8 && getOsdMetasets().size() == 0) {
             return metadata;
         }
         Document doc = DocumentHelper.createDocument();
         Element root = doc.addElement("meta");
         for (Metaset m : fetchMetasets()) {
             root.add(Metaset.asElement("metaset", m));
         }
         return doc.asXML();
     }
 
     /**
      * @return the compiled metadata of this element (all metasets collected under one meta root element).
      */
     public String getMetadata(List<String> metasetNames) {
         // for compatibility: return non-empty metadata, otherwise try to compile metasets
 //        if(metadata.length() > 8 && getOsdMetasets().size() == 0){
 //            return metadata;
 //        }
         Document doc = DocumentHelper.createDocument();
         Element root = doc.addElement("meta");
         for (Metaset m : fetchMetasets()) {
             root.add(Metaset.asElement("metaset", m));
         }
         return metadata;
     }
 
     /**
      * Set the Metadata on this object. Tries to parse the submitted string
      * and throws an exception if it is not valid XML.<br/>
      * Note: this parses the meta-xml into metasets and stores them as such.
      *
      * @param metadata the custom metadata
      */
     public void setMetadata(String metadata) {
         setMetadata(metadata, WritePolicy.BRANCH);
     }
 
     /**
      * Set the Metadata on this object. Tries to parse the submitted string
      * and throws an exception if it is not valid XML.<br/>
      * Note: this parses the meta-xml into metasets and stores them as such.
      * This method will unlink existing metasets if they are missing from the metadata,
      * that is, you cannot submit partial metadata to setMetadata. You must set <em>all</em>
      * metadata if you use this method. (see addMetaset to add an individual metaset) 
      *
      * @param metadata    the custom metadata
      * @param writePolicy the write policy - what to do if other items already reference a metaset.
      */
     public void setMetadata(String metadata, WritePolicy writePolicy) {
         try {
             MetasetService metasetService = new MetasetService();
             
             EntityManager em = HibernateSession.getLocalEntityManager();
             if (metadata == null || metadata.trim().length() == 0) {
                 this.metadata = "<meta/>";
                 for (OsdMetaset om : getOsdMetasets()) {
                     em.remove(om);
                 }
             } else {
                 Document doc = ParamParser.parseXmlToDocument(metadata, "error.param.metadata");
                 List<Node> sets = doc.selectNodes("//metaset");
 //            log.debug("found "+sets.size()+" metaset nodes in:\n"+metadata);
                 if (sets.size() == 0) {
                     this.metadata = metadata;
                     if (osdMetasets.size() > 0) {
                         // delete obsolete metasets:
                         for (Metaset m : fetchMetasets()) {
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
                 
                 for (Node metasetNode : sets) {
                     String content = metasetNode.detach().asXML();
                     String metasetTypeName = metasetNode.selectSingleNode("@type").getText();
                     log.debug("metasetType: " + metasetTypeName);
                     MetasetTypeDAO mtDao = daoFactory.getMetasetTypeDAO(em);
                     MetasetType metasetType = mtDao.findByName(metasetTypeName);                    
                     if (metasetType == null) {
                         throw new CinnamonException("error.unknown.metasetType", metasetTypeName);
                     }
                     metasetService.createOrUpdateMetaset(this, metasetType, content, writePolicy);
                     currentMetasetMap.remove(metasetType);
                 }                
                 for(MetasetType metasetType : currentMetasetMap){
                     // any metaset that was not found in the metadata parameter will be deleted.                    
                     metasetService.unlinkMetaset(this, this.fetchMetaset(metasetType.getName())); // somewhat convoluted.
                 }
             }
         } catch (Exception e) {
             log.debug("failed to add metadata:", e);
             throw new RuntimeException(e);
         }
         // remove legacy metadata:
         this.metadata = "<meta />";
     }
 
     public IMetasetJoin fetchMetasetJoin(MetasetType type) {
         EntityManager em = HibernateSession.getLocalEntityManager();
         Query q = em.createQuery("select o from OsdMetaset o where o.metaset.type=:metasetType and o.osd=:osd");
         q.setParameter("metasetType", type);
         q.setParameter("osd", this);
         List<OsdMetaset> metasetList = q.getResultList();
         log.debug("query for: " + type.getName() + " / osd: " + getId() + " returned #objects: " + metasetList.size());
         if (metasetList.size() == 0) {
             return null;
         } else if (metasetList.size() > 1) {
 //            for(OsdMetaset om : metasetList){
 //                log.debug("found OsdMetaset: "+om.toString());
 //            }
             throw new CinnamonConfigurationException("Found two metasets of the same type in osd #" + getId());
         } else {
             return metasetList.get(0);
         }
     }
 
     public void addMetaset(Metaset metaset) {
         // make sure that we do not add a second metaset of the same type:
         MetasetType metasetType = metaset.getType();
         IMetasetJoin metasetJoin = fetchMetasetJoin(metasetType);
         if (metasetJoin != null) {
             log.debug("found existing metasetJoin: " + metasetJoin.getId());
             throw new CinnamonException("you tried to add a second metaset of type " + metasetType.getName() + " to " + getId());
         }
 
         OsdMetaset om = new OsdMetaset(this, metaset);
         EntityManager em = HibernateSession.getLocalEntityManager();
         // the correct way would be to use a DAO, but then we are on the way to use Grails
         // GORM anyway, so this is a temporary solution:
         log.debug("persist metaset");
         em.persist(om);
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public Folder getParent() {
         return parent;
     }
 
     public void setParent(Folder parent) {
         this.parent = parent;
     }
 
     public ObjectSystemData getPredecessor() {
         return predecessor;
     }
 
     public void setPredecessor(ObjectSystemData previous) {
         this.predecessor = previous;
     }
 
     /**
      * @return the first version of this object.
      */
     public ObjectSystemData getRoot() {
         return root;
     }
 
     public void setRoot(ObjectSystemData root) {
         this.root = root;
     }
 
     public String getName() {
         return name;
     }
 
     public ObjectType getType() {
         return type;
     }
 
     public void setType(ObjectType type) {
         this.type = type;
     }
 
     public User getCreator() {
         return creator;
     }
 
     public void setCreator(User user) {
         this.creator = user;
     }
 
     public Date getCreated() {
         return created;
     }
 
     public void setCreated(Date created) {
         this.created = created;
     }
 
     public Date getIndexed() {
         return indexed;
     }
 
     public void setIndexed(Date indexed) {
         this.indexed = indexed;
     }
 
     public Date getModified() {
         return modified;
     }
 
     public void setModified(Date modified) {
         this.modified = modified;
     }
 
     public Boolean getLatestBranch() {
         return latestBranch;
     }
 
     public void setLatestBranch(Boolean latestBranch) {
         this.latestBranch = latestBranch;
     }
 
     public Boolean getLatestHead() {
         return latestHead;
     }
 
     public void setLatestHead(Boolean latestHead) {
         this.latestHead = latestHead;
     }
 
     public User getModifier() {
         return modifier;
     }
 
     public void setModifier(User modifier) {
         this.modifier = modifier;
     }
 
     public User getOwner() {
         return owner;
     }
 
     public void setOwner(User owner) {
         this.owner = owner;
     }
 
     public String getProcstate() {
         return procstate;
     }
 
     public void setProcstate(String procstate) {
         this.procstate = procstate;
     }
 
     public String getVersion() {
         return version;
     }
 
     public void setVersion(String version) {
         this.version = version;
     }
 
     public User getLocked_by() {
         return locked_by;
     }
 
     public void setLocked_by(User locked_by) {
         this.locked_by = locked_by;
     }
 
     /**
      * createClone: create a copy with created and modified set to current date, <br>
      * and without an Id (which should be set by the persistence layer when inserting into  the db).
      *
      * @return the cloned OSD, not yet persisted to the database. Note: this clone lacks the relations
      *         of the original. If you need the relations, you should use original.copyRelations(clone).
      */
     public ObjectSystemData createClone() {
         ObjectSystemData twin = new ObjectSystemData();
 
         twin.setAcl(getAcl());
         twin.setAppName(getAppName());
         twin.setContentPath(getContentPath());
         twin.setContentSize(getContentSize());
         twin.setCreated(Calendar.getInstance().getTime());
         twin.setCreator(getCreator());
         twin.setFormat(getFormat());
         //		twin.setIndexed(null); // must be "now()", or IndexServer will do bad things.
         twin.setLanguage(getLanguage());
         twin.setLatestBranch(getLatestBranch());
         twin.setLatestHead(getLatestHead());
         twin.setLocked_by(getLocked_by());
         twin.setModified(Calendar.getInstance().getTime());
         twin.setModifier(getModifier());
         twin.setName(getName());
         twin.setParent(getParent());
         twin.setPredecessor(getPredecessor());
         twin.setProcstate(getProcstate());
         twin.setRoot(getRoot());
         twin.setType(getType());
         twin.setVersion(getVersion());
         if (getState() != null) {
             twin.setState(getState().getLifeCycleStateForCopy());
         }
         twin.setOwner(getOwner()); // will be set later to the correct owner by CmdInterpreter.copy(), if necessary.
         twin.setMetadata(getMetadata()); // must come last, because it may persist the object via addMetaset.
         return twin;
     }
 
     /**
      * @return the language
      */
     public Language getLanguage() {
         return language;
     }
 
     /**
      * @param language the language to set
      */
     public void setLanguage(Language language) {
         this.language = language;
     }
 
     // this is now done via Lifecycles by directly setting procstate with constants in the WorkflowAPI.
 //    /**
 //     * Set the objects procstate status to _done or _todo.
 //     */
 //    public void setFinished(boolean done) {
 //        log.debug("setting procstate to finished (_done).");
 //        if (done) {
 //            setProcstate("_done");
 //        }
 //        else {
 //            setProcstate("_todo");
 //            /*
 //                * note that setReadyForValidation also may set the state to _todo.
 //                * This is okay, since an unfinished task is always todo,
 //                * (unless ready for validation...)
 //                */
 //        }
 //    }
 
     public void updateAccess(User user) {
         setModifier(user);
         setModified(Calendar.getInstance().getTime());
     }
 
 //    @Transient
 //    public boolean isFinished() { // TODO: is this really needed by some future workflow class?
 //        return getProcstate().equals("_done");
 //    }
 
 //    @Transient           // TODO: is this really needed by some future workflow class?
 //    public void setReadyForValidation(boolean ready) {
 //        if (ready) {
 //            setProcstate("_validate_task");
 //        }
 //        else {
 //            setProcstate("_todo");
 //        }
 //    }
 
 //    @Transient             // TODO: is this really needed by some future workflow class?
 //    public boolean isReadyForValidation() {
 //        return getProcstate().equals("_validate_task");
 //    }
 
     public Document toXML() {
         Document doc = DocumentHelper.createDocument();
         doc.add(convertToElement());
 
         return doc;
     }
 
     /**
      * Delete the file content of this Data Object.
      *
      * @param repository the name of the repository that contains the file.
      */
     public void deleteContent(String repository) {
         ContentStore.deleteObjectFile(this);
         setContentSize(null);
         setContentPath(null);
         setFormat(null);
     }
 
     public void setContentPathAndFormat(String contentPath, String formatName, String repositoryName) {
         EntityManager em = HibernateSession.getLocalEntityManager();
         FormatDAO formatDAO = daoFactory.getFormatDAO(em);
         Format newFormat = formatDAO.findByName(formatName);
         setFormat(newFormat);
         setContentPathAndFormat(contentPath, newFormat, repositoryName);
     }
 
     public void setContentPathAndFormat(String contentPath, Format newFormat, String repositoryName) {
         setContentPath(contentPath, repositoryName); // side effect: will set contentSize, if valid.
         setFormat(newFormat);
     }
 
     /**
      * Turn a collection of data objects into an XML document. Any exceptions encountered during
      * serialization are turned into error-Elements which contain the exception's message.
      *
      * @param results
      * @return Document
      */
     static public Document generateQueryObjectResultDocument(Collection<ObjectSystemData> results) {
         return generateQueryObjectResultDocument(results, false);
     }
 
     /**
      * Turn a collection of data objects into an XML document. Any exceptions encountered during
      * serialization are turned into error-Elements which contain the exception's message.
      *
      * @param results      the source collection of results to be used to generate the XML document.
      * @param withMetadata if true, include object custom metadata in the output (which can get quite large).
      * @return Document
      */
     static public Document generateQueryObjectResultDocument(Collection<ObjectSystemData> results,
                                                              Boolean withMetadata) {
         Document doc = DocumentHelper.createDocument();
         Element root = doc.addElement("objects");
         Logger log = LoggerFactory.getLogger(ObjectSystemData.class);
 
         for (ObjectSystemData osd : results) {
             Long id = osd.getId();
             log.debug("working on object: " + id);
             Element data;
             try {
                 data = osd.convertToElement();
                 if (withMetadata) {
                     data.add(ParamParser.parseXml(osd.getMetadata(), null));
                 }
                 root.add(data);               
             } catch (CinnamonException ex) {
                 /*
                      * Note: any exceptions encountered here are probably serious bugs,
                      * which could be caused by corrupted data or faulty serialization
                      * routines.
                      * So, let's report them as errors instead of debug messages.
                      */
                 log.error("Error serializing object: " + id + " - " + ex.getMessage());
                 Element error = DocumentHelper.createElement("error").addText(ex.getLocalizedMessage());
                 error.addElement("id").addText(id.toString());
                 root.add(error);
             }            
         }
         return doc;
     }
 
     public Element toXmlElement(Element root) {
         Element e = convertToElement();
         root.add(e);
         return e;
     }
 
     /**
      * Serialize the OSD to a dom4j-Element by name of "object".
      * User data (in lockedBy, owner, creator, modifier), format and object type are added as
      * full elements themselves. All other object references will be included as id, for example:
      *  <pre>
      * {@code
      *  <user><id>453</id><name>foo</name>...</user>
      *  <aclId>1234</aclId>
      * }
      * </pre>
      * @return the serialized OSD as dom4j-Element
      */
     public Element convertToElement() {
         Element data = DocumentHelper.createElement("object");
         data.addElement("id").addText(String.valueOf(getId()));
         data.addElement("name").addText(getName());
         data.addElement("version").addText(getVersion());
         data.addElement("created").addText(ParamParser.dateToIsoString(getCreated()));
         data.addElement("modified").addText(ParamParser.dateToIsoString(getModified()));
         data.addElement("procstate").addText(getProcstate());
         data.addElement("aclId").addText(String.valueOf(getAcl().getId()));
 
         data.addElement("appName").addText(getAppName());
         data.addElement("latestHead").addText(getLatestHead().toString());
         data.addElement("latestBranch").addText(getLatestBranch().toString());
 
         log.debug("UserAsElementSection");
         data.add(User.asElement("lockedBy", getLocked_by()));
         data.add(User.asElement("owner", getOwner()));
         data.add(User.asElement("creator", getCreator()));
         data.add(User.asElement("modifier", getModifier()));
 
         log.debug("FormatAsElement");
         data.add(Format.asElement("format", getFormat()));
         log.debug("ObjectTypeAsElement");
         data.add(ObjectType.asElement("objectType", getType()));
 
         log.debug("nullChecks");
         if (getContentSize() != null) {
             data.addElement("contentsize").addText(String.valueOf(getContentSize()));
         } else {
             data.addElement("contentsize");
         }
 
         if (getParent() != null) {
             data.addElement("parentId").addText(String.valueOf(getParent().getId()));
         } else {
             data.addElement("parentId");
         }
 
         if (getPredecessor() != null) {
             data.addElement("predecessorId").addText(String.valueOf(getPredecessor().getId()));
         } else {
             data.addElement("predecessorId");
         }
 
         if (getRoot() != null) {
             data.addElement("rootId").addText(String.valueOf(getRoot().getId()));
         } else {
             data.addElement("rootId"); // TODO: prevent rootId==null on the db level.
         }
 
         log.debug("languageSection");
         Element lang = data.addElement("language");
         lang.addElement("id").addText(String.valueOf(getLanguage().getId()));
         lang.addElement("isoCode").addText(getLanguage().getIsoCode());
 
         log.debug("lifecycleSection");
         if (getState() == null) {
             data.addElement("lifeCycleState");
         } else {
             data.addElement("lifeCycleState").addText(String.valueOf(state.getId()));
         }
 
         return data;
     }
 
     /**
      * Read the content of the OSD and return it as XML for indexing.
      *
      * @param repository The repository where the indexable object is located.
      * @return A string containing the XML version of this object's content.
      * @see server.index.Indexable
      */
     public String getContent(String repository) {
         return getContent(repository, null);
     }
 
     /**
      * @param repository the name of the repository where this object is stored.
      * @param encoding   the encoding of the content. May be null.
      * @return a string containing the content, in XML format if possible. If the contentSize is null,
      *         an empty content-element is returned.
      * @see ObjectSystemData#getContent(String)
      */
     public String getContent(String repository, String encoding) {
         if (contentSize == null) {
             return "<content/>";
         }
         String fileContent;
         try {
             fileContent = utils.ContentReader.readFileAsString(
                     getFullContentPath(repository), encoding
             );
         } catch (Exception e) {
             throw new CinnamonException(e);
         }
         return fileContent;
     }
 
     public byte[] getContentAsBytes(String repository) {
         byte[] fileContent;
         try {
             String path = getFullContentPath(repository);
             if (path == null) {
                 return "<empty />".getBytes();
             }
             log.debug("path to file: " + path);
             fileContent = utils.ContentReader.readFileAsBytes(path);
         } catch (Exception e) {
             throw new CinnamonException(e);
         }
         return fileContent;
     }
 
     @Override
     public String getSystemMetadata() {
         log.debug("getsystemMeta");
         Document doc = DocumentHelper.createDocument();
         Element root = doc.addElement("sysMeta");
         String className = getClass().getName();
         root.addAttribute("javaClass", className);
         root.addAttribute("hibernateId", String.valueOf(getId()));
         root.addAttribute("id", className + "@" + getId()); // for a given repository, it's unique.
         log.debug("convertToElement");
         root.add(convertToElement());
         return doc.asXML();
     }
 
     /**
      * @param repositoryName the name of the repository where the object is stored.
      * @return the complete path to this OSD's content - or null, if no content exists.
      */
     public String getFullContentPath(String repositoryName) {
         Conf conf = ConfThreadLocal.getConf();
         if (contentPath == null) {
 //            log.debug("ContentPath is null");
             return null;
         }
         String fullContentPath = conf.getDataRoot() + File.separator +
                 repositoryName + File.separator + getContentPath();
 //        log.debug("fullContentPath: "+fullContentPath);
         return fullContentPath;
     }
 
     /**
      * @return the indexStatus
      */
     public Boolean getIndexOk() {
         return indexOk;
     }
 
     /**
      * Set the index status, true means indexing was completed successfully, false means
      * indexing has failed. If indexOk is null, this object has not been indexed yet (or
      * it is scheduled to be indexed as soon as possible, provided the IndexServer is running,
      * which can be configured by setting {@code <startIndexServer>true</startIndexServer>}
      * in the cinnamon_config.xml).
      *
      * @param indexOk the index status
      */
     public void setIndexOk(Boolean indexOk) {
         this.indexOk = indexOk;
     }
 
     public void copyContent(String repositoryName, ObjectSystemData copy) {
         String conPath = getContentPath();
         if (conPath != null && conPath.length() > 0) {
             String fullContentPath = getFullContentPath(repositoryName);
 
             log.debug("ContentPath: " + fullContentPath +
                     " and Size is: " + getContentSize());
             try {
                 String targetPath = ContentStore.copyToContentStore(fullContentPath, repositoryName);
                 log.debug("targetPath = " + targetPath);
                 if (targetPath.length() > 0) {
                     copy.setContentPath(targetPath, repositoryName);
                 }
                 copy.setFormat(getFormat());
             } catch (IOException ex) {
                 throw new CinnamonException(ex);
             }
         }
     }
 
     public void copyContent(ObjectSystemData copy) {
         String repositoryName = HibernateSession.getLocalRepositoryName();
         copyContent(repositoryName, copy);
     }
 
     @SuppressWarnings("unchecked")
     public String createNewVersionLabel() {
 
         if (getPredecessor() == null) {
             return "1";
         }
 
         String predecessorVersion = getPredecessor().getVersion();
         String[] branches = predecessorVersion.split("\\.");
         String lastSegment = branches[branches.length - 1];
         String[] lastBranch = lastSegment.split("-");
 
         String lastDescendantVersion;
         try {
             log.debug("query for predecessor " + getPredecessor().getId());
             EntityManager em = HibernateSession.getLocalEntityManager();
             Query q = em.createNamedQuery("findOsdsByPredecessorOrderByIdDesc");
             q.setParameter("predecessor", getPredecessor());
             List<ObjectSystemData> versions = q.getResultList();
             ObjectSystemData lastDescendant;
             if (versions.size() == 0) {
                 throw new NoResultException();
             } else {
                 lastDescendant = versions.get(0);
             }
             lastDescendantVersion = lastDescendant.getVersion();
         } catch (NoResultException e) {
             // no object with same predecessor
             log.debug("no result for last-descendant-query");
             String buffer = lastBranch.length == 2 ? lastBranch[1] : lastBranch[0];
             String stem = predecessorVersion.substring(0, predecessorVersion.length() - buffer.length());
             buffer = String.valueOf(Integer.parseInt(buffer) + 1);
             return stem + buffer;
         }
         log.debug("lastDescendant: " + lastDescendantVersion);
         String[] lastDescBranches = lastDescendantVersion.split("\\.");
         if (branches.length == lastDescBranches.length) {
             // last descendant is the only one so far: create first branch
             return predecessorVersion + ".1-1";
         }
         String buffer = lastDescBranches[lastDescBranches.length - 1].split("-")[0];
         buffer = String.valueOf(Integer.parseInt(buffer) + 1);
         return predecessorVersion + "." + buffer + "-1";
     }
 
 
     public LifeCycleState getState() {
         return state;
     }
 
     public void setState(LifeCycleState state) {
         this.state = state;
     }
 
     @Override
     /*
       * Implements Comparable interface based on id.
       * This is used to generate search results with a consistent ordering (where the
       * results are not ordered by any other parameter).
       */
     public int compareTo(XmlConvertable o) {
         if (getId() > o.getId()) {
             return 1;
         } else if (getId() < o.getId()) {
             return -1;
         }
         return 0;
     }
 
 
     static public String fetchVersionPredicate(String versions) {
         String versionPred;
         if (versions == null || versions.length() == 0 || versions.equals("head")) {
             versionPred = " and latesthead=true";
         } else if (versions.equals("all")) {
             versionPred = "";
         } else if (versions.equals("branch")) {
             versionPred = " and latestbranch=true";
         } else {
             throw new CinnamonException("error.param.version");
         }
         return versionPred;
     }
 
     /**
      * Copy a root object and all of its descendants.
      *
      * @param source       the source object
      * @param targetFolder the folder in which the copy will be created.
      * @param activeUser   the user who will be owner / modifier of the copied objects.
      * @return the root object of the new objectTree
      */
     CopyResult copyObjectTree(ObjectSystemData source, Folder targetFolder, User activeUser) {
         CopyResult cr = new CopyResult();
         EntityManager em = HibernateSession.getLocalEntityManager();
 //		RelationDAO relDao = daoFactory.getRelationDAO(em);
         ObjectSystemDataDAO osdDao = daoFactory.getObjectSystemDataDAO(em);
 
         List<ObjectSystemData> allVersions = osdDao.findAllVersions(source);
 //		List<ObjectSystemData> newTree = new ArrayList<ObjectSystemData>();
 
         // create copies of all versions:
         ObjectTreeCopier objectTreeCopier = new ObjectTreeCopier(activeUser, targetFolder);
         ObjectSystemData currentOsd = null;
         try {
             log.debug("create  full copies of all versions");
 
             for (ObjectSystemData osd : allVersions) {
                 currentOsd = osd;
                 log.debug("create full copy of: " + osd.getId());
                 ObjectSystemData copy = objectTreeCopier.createFullCopy(osd);
                 objectTreeCopier.getCopyCache().put(osd, copy);
                 log.debug("copy relations");
                 copyRelations(copy);
                 log.debug("copy content");
                 copyContent(copy);
                 cr.addObject(copy);
             }
         } catch (Exception ex) {
             /*
              * If an exception occurs, we must terminate the whole tree,
              * as in most cases we could only get a stunted version (with missing branches or
              * missing content etc.)
              */
             for (ObjectSystemData osd : objectTreeCopier.getCopyCache().keySet()) {
                 osd.deleteContent(HibernateSession.getLocalRepositoryName());
                 osdDao.delete(osd);
             }
             cr = new CopyResult();
             cr.addFailure(currentOsd, new CinnamonException("Failed to copy tree of OSD.", ex));
         }
 
         return cr;
     }
 
     /**
      * Copy relations of an object if the relationType demands it.
      *
      * @param target for which the new relations will be created.
      */
     public void copyRelations(ObjectSystemData target) {
         EntityManager em = HibernateSession.getLocalEntityManager();
         RelationDAO relationDao = daoFactory.getRelationDAO(em);
         List<Relation> relations = relationDao.findAllByLeftOrRight(this, this);
         for (Relation rel : relations) {
             /*
              * The relation will only be copied if the cloneOn{left,right}Copy flag is set on the
              * {left,right} part of the osd which is copied.
              * Example: html to image relation should (normally) have the clone flag set on the
              * left, but not on the right part of the relation. If the image is copied, the copy
              * will not have a relation to the html file. If the html file is copied, the copy
              * should have a relation to the image.
              */
             RelationType relationType = rel.getType();
             if (relationType.getCloneOnLeftCopy() && rel.getLeft().equals(this)) {
                 Relation relCopy = relationDao.findOrCreateRelation(rel.getType(), target, rel.getRight(), rel.getMetadata());
                 log.debug("created new Relation: " + relCopy);
             }
             if (relationType.getCloneOnRightCopy() && rel.getRight().equals(this)) {
                 Relation relCopy = relationDao.findOrCreateRelation(rel.getType(), rel.getLeft(), target, rel.getMetadata());
                 log.debug("created new Relation: " + relCopy);
             }
         }
     }
 
     /**
      * Get a new filename for the given osd.
      * First, it filters potentially harmful characters from the object's name. Then,
      * if a file with osd.name already exists, it will try
      * to create increasingly specific filenames, by using the version and then the id to differentiate this
      * filename from other existing files. This method is intended for use when yours is the only thread actually
      * working in this folder. It is <em>not</em> to be used for generic folders like java.io.tmpdir to construct
      * "unique" filenames (where you may encounter security problems due to race conditions).
      * If all fails, create a filename $name_$version_$id_$uuid.$format where uuid is a unique hex string.
      *
      * @param path the path where the new File will be created.
      * @return a filename which is unique inside this path.
      */
     public File createFilenameFromName(File path) {
         String extension = "";
         if (format != null) {
             // the format *should* be set, but you never know. Perhaps someone created a 0-byte lock file without format.
             extension = "." + format.getExtension();
         }
         name = name.replaceAll("[^\\w]", "_");
         File file = new File(path, name + extension);
         if (file.exists()) {
             name = name + "_" + getVersion();
             file = new File(path, name + extension);
             if (file.exists()) {
                 name = name + "_" + getId();
                 file = new File(path, name + extension);
                 if (file.exists()) {
                     // "Wir können auch anders."
                     return new File(path, name + "_" + UUID.randomUUID().toString() + extension);
                 }
             }
         }
         return file;
     }
 
     /**
      * Check if the content can be considered to be XML. This is used by the LuceneBridge to
      * determine if it's worth parsing the content with an XML-Parser. Previously we would
      * try to parse everything, but this is a waste of time and memory (especially when reading
      * huge files only to have the parser croak on seeing that it's a zip archive etc)
      * A somewhat crude heuristic (format.contenttype.endsWith("xml")) is used to detect xml content
      * at the moment.
      *
      * @return true if the content is probably xml, false if not.
      */
     public Boolean hasXmlContent() {
         if (format == null) {
             return false;
         }
         return getFormat().getExtension().toLowerCase().matches(fetchXmlFormatList());
     }
 
     public Set<OsdMetaset> getOsdMetasets() {
         return osdMetasets;
     }
 
     public void setOsdMetasets(Set<OsdMetaset> osdMetasets) {
         this.osdMetasets = osdMetasets;
     }
 
     public Set<Metaset> fetchMetasets() {
         Set<Metaset> metasets = new HashSet<Metaset>(getOsdMetasets().size());
         for (OsdMetaset om : getOsdMetasets()) {
             metasets.add(om.getMetaset());
         }
 //        log.debug("found "+metasets.size()+" metasets");
         return metasets;
     }
 
     /**
      * Fetch a metaset by its given name. Returns null in case there is no such metaset
      * associated with this object.
      *
      * @param name the name of the metaset
      * @return the metaset or null
      */
     public Metaset fetchMetaset(String name) {
         Metaset metaset = null;
         for (Metaset m : fetchMetasets()) {
 //            log.debug("check "+name+" vs. "+m.getType().getName());
             if (m.getType().getName().equals(name)) {
                 metaset = m;
                 break;
             }
         }
         return metaset;
     }
 
 
     /**
      * You can add a config entry to define formats that can be parsed by XML/XPath based IndexItems.
      * For example, DITA files are XML, but have their own extension.
      * The default xmlFormatList is: "xml|xhtml|dita|ditamap"
      *
      * @return a String that may be used as a regex to filter for invalid format extensions<br/>
      *         Example: may return "dita|xml|foo"
      */
     String fetchXmlFormatList() {
         EntityManager em = HibernateSession.getLocalEntityManager();
         ConfigEntryDAO ceDao = daoFactory.getConfigEntryDAO(em);
         ConfigEntry formatList = ceDao.findByName("xml.format.list");
         if (formatList == null) {
             log.debug("Did not find xml.format.list config entry, returning defaultXmlFormatList.");
             return defaultXmlFormatList;
         }
         Node formatListNode = ParamParser.parseXmlToDocument(formatList.getConfig()).selectSingleNode("//format");
         if (formatListNode == null) {
             log.debug("Did not find format node in xml.format.list config entry, returning defaultXmlFormatlist.");
             return defaultXmlFormatList;
         }
         log.debug("Found formatList: " + formatListNode.getText());
         return formatListNode.getText();
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof ObjectSystemData)) return false;
 
         ObjectSystemData that = (ObjectSystemData) o;
 
         if (acl != null ? !acl.equals(that.acl) : that.acl != null) return false;
         if (appName != null ? !appName.equals(that.appName) : that.appName != null) return false;
         if (contentPath != null ? !contentPath.equals(that.contentPath) : that.contentPath != null) return false;
         if (contentSize != null ? !contentSize.equals(that.contentSize) : that.contentSize != null) return false;
         if (created != null ? !created.equals(that.created) : that.created != null) return false;
         if (creator != null ? !creator.equals(that.creator) : that.creator != null) return false;
         if (format != null ? !format.equals(that.format) : that.format != null) return false;
         if (language != null ? !language.equals(that.language) : that.language != null) return false;
         if (latestBranch != null ? !latestBranch.equals(that.latestBranch) : that.latestBranch != null)
             return false;
         if (latestHead != null ? !latestHead.equals(that.latestHead) : that.latestHead != null) return false;
         if (locked_by != null ? !locked_by.equals(that.locked_by) : that.locked_by != null) return false;
         if (metadata != null ? !metadata.equals(that.metadata) : that.metadata != null) return false;
         if (modified != null ? !modified.equals(that.modified) : that.modified != null) return false;
         if (modifier != null ? !modifier.equals(that.modifier) : that.modifier != null) return false;
         if (name != null ? !name.equals(that.name) : that.name != null) return false;
         if (owner != null ? !owner.equals(that.owner) : that.owner != null) return false;
         if (parent != null ? !parent.equals(that.parent) : that.parent != null) return false;
         if (predecessor != null ? !predecessor.equals(that.predecessor) : that.predecessor != null) return false;
         if (procstate != null ? !procstate.equals(that.procstate) : that.procstate != null) return false;
         if (root != null ? !root.equals(that.root) : that.root != null) return false;
         if (state != null ? !state.equals(that.state) : that.state != null) return false;
         if (type != null ? !type.equals(that.type) : that.type != null) return false;
         if (version != null ? !version.equals(that.version) : that.version != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = name != null ? name.hashCode() : 0;
         result = 31 * result + (created != null ? created.hashCode() : 0);
         result = 31 * result + (version != null ? version.hashCode() : 0);
         return result;
     }
 
     public static void fixLatestHeadAndBranch(ObjectSystemData target, List<ObjectSystemData> children) {
         ObjectSystemData predecessor = target.getPredecessor();
         Boolean hasChildren = children.size() > 0;
         if (hasChildren) {
             // if target has children: it is not latestBranch.
             target.setLatestBranch(false);
             // target *may* be latestHead if the previous head has been deleted 
             // and only child branches remain.
             Boolean isHead = true;
             for (ObjectSystemData child : children) {
                 if (child.getVersion().matches("^\\d+$")) {
                     isHead = false;
                     break;
                 }
             }
             target.setLatestHead(isHead);
         }
         else {
             // target no children: it is latestBranch
             target.setLatestBranch(true);
             if (target.getVersion().matches("^\\d+$")) {
                 target.setLatestHead(true);
                 if (predecessor != null && predecessor.getLatestHead()) {
                     predecessor.setLatestHead(false);
                 }
             }
         }
 
         // the predecessor cannot be latest branch, that has to be this (or a descendant) node.
         if (predecessor != null && predecessor.getLatestBranch()) {
             predecessor.setLatestBranch(false);
         }
     }
 }
