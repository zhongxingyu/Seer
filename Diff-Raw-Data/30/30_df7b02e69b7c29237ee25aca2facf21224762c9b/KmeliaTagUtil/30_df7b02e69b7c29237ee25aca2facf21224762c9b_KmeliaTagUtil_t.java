 package com.silverpeas.tags.kmelia;
 
 import com.silverpeas.comment.model.CommentPK;
 import com.silverpeas.comment.model.CommentedPublicationInfo;
 import java.rmi.NoSuchObjectException;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import com.silverpeas.comment.service.CommentService;
 import com.silverpeas.comment.service.CommentServiceFactory;
import com.silverpeas.form.importExport.XMLField;
 import com.silverpeas.notation.ejb.NotationBm;
 import com.silverpeas.notation.model.Notation;
 import com.silverpeas.notation.model.NotationPK;
 import com.silverpeas.tags.ComponentTagUtil;
 import com.silverpeas.tags.util.EJBDynaProxy;
 import com.silverpeas.tags.util.SiteTagUtil;
 import com.silverpeas.tags.util.VisibilityException;
 import com.silverpeas.util.ForeignPK;
 import com.silverpeas.util.StringUtil;
 import com.stratelia.silverpeas.silvertrace.SilverTrace;
 import com.stratelia.silverpeas.wysiwyg.WysiwygException;
 import com.stratelia.silverpeas.wysiwyg.control.WysiwygController;
 import com.stratelia.webactiv.beans.admin.ComponentInst;
 import com.stratelia.webactiv.beans.admin.SpaceInst;
 import com.stratelia.webactiv.kmelia.control.ejb.KmeliaBm;
 import com.stratelia.webactiv.kmelia.model.FullPublication;
 import com.stratelia.webactiv.kmelia.model.KmeliaRuntimeException;
 import com.stratelia.webactiv.kmelia.model.UserCompletePublication;
 import com.stratelia.webactiv.searchEngine.control.ejb.SearchEngineBm;
 import com.stratelia.webactiv.searchEngine.model.MatchingIndexEntry;
 import com.stratelia.webactiv.searchEngine.model.QueryDescription;
 import com.stratelia.webactiv.util.FileServerUtils;
 import com.stratelia.webactiv.util.JNDINames;
 import com.stratelia.webactiv.util.attachment.control.AttachmentController;
 import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
 import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
 import com.stratelia.webactiv.util.attachment.model.AttachmentDetailI18N;
 import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
 import com.stratelia.webactiv.util.node.control.NodeBm;
 import com.stratelia.webactiv.util.node.model.NodeDetail;
 import com.stratelia.webactiv.util.node.model.NodeI18NDetail;
 import com.stratelia.webactiv.util.node.model.NodePK;
 import com.stratelia.webactiv.util.publication.control.PublicationBm;
 import com.stratelia.webactiv.util.publication.info.model.InfoDetail;
 import com.stratelia.webactiv.util.publication.info.model.InfoImageDetail;
 import com.stratelia.webactiv.util.publication.info.model.InfoTextDetail;
 import com.stratelia.webactiv.util.publication.info.model.ModelDetail;
 import com.stratelia.webactiv.util.publication.model.CompletePublication;
 import com.stratelia.webactiv.util.publication.model.PublicationDetail;
 import com.stratelia.webactiv.util.publication.model.PublicationI18N;
 import com.stratelia.webactiv.util.publication.model.PublicationPK;
 
 public class KmeliaTagUtil extends ComponentTagUtil {
 
   private static final String BEGIN_EDITO = "@";
   private String componentId = null;
   private String spaceId = null;
   private String visibilityFilter = null;
   private KmeliaBm kscEjb = null;
   private PublicationBm publicationBm = null;
   private NodeBm nodeBm = null;
   private SearchEngineBm searchEngineBm = null;
   private CommentService commentService = null;
   private NotationBm notationBm = null;
 
   public KmeliaTagUtil(String spaceId, String componentId, String userId) {
     super(componentId, userId);
 
     this.spaceId = spaceId;
     this.componentId = componentId;
   }
 
   public KmeliaTagUtil(String componentId, String userId) {
     super(componentId, userId);
 
     this.componentId = componentId;
   }
 
   public KmeliaTagUtil(String spaceId, String componentId, String userId, boolean checkAuthorization) {
     super(componentId, userId, checkAuthorization);
 
     this.spaceId = spaceId;
     this.componentId = componentId;
   }
 
   private void initEJB() {
     SilverTrace.info("kmelia", "KMeliaTagUtil.initEJB()", "root.MSG_GEN_ENTER_METHOD",
         "componentId = " + componentId);
 
     kscEjb = null;
     try {
       getKmeliaBm();
     } catch (Exception e) {
       throw new KmeliaRuntimeException("KmeliaTagUtil.initEJB()", SilverpeasRuntimeException.ERROR,
           "root.EX_CANT_GET_REMOTE_OBJECT", e);
     }
   }
 
   private void initPublicationEJB() {
     SilverTrace.info("kmelia", "KmeliaTagUtil.initPublicationEJB()", "root.MSG_GEN_ENTER_METHOD");
     publicationBm = null;
     try {
       getPublicationBm();
     } catch (Exception e) {
       throw new KmeliaRuntimeException("KmeliaTagUtil.initPublicationEJB()",
           SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
     }
   }
 
   private KmeliaBm getKmeliaBm() {
     SilverTrace.info("kmelia", "KMeliaTagUtil.getKmeliaBm()", "root.MSG_GEN_ENTER_METHOD");
     if (kscEjb == null) {
       try {
         SilverTrace.info("kmelia", "KMeliaTagUtil.getKmeliaBm()", "root.MSG_GEN_PARAM_VALUE",
             "Try to access to EJBHome " + JNDINames.KMELIABM_EJBHOME);
         kscEjb = (KmeliaBm) EJBDynaProxy.createProxy(JNDINames.KMELIABM_EJBHOME, KmeliaBm.class);
       } catch (Exception e) {
         throw new KmeliaRuntimeException("KmeliaTagUtil.getKmeliaBm()",
             SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
       }
     }
     return kscEjb;
   }
 
   private SearchEngineBm getSearchEngineBm() {
     if (searchEngineBm == null) {
       try {
         searchEngineBm = (SearchEngineBm) EJBDynaProxy.createProxy(JNDINames.SEARCHBM_EJBHOME,
             SearchEngineBm.class);
       } catch (Exception e) {
         throw new KmeliaRuntimeException("KmeliaTagUtil.getSearchEngineBm",
             SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
       }
     }
     return searchEngineBm;
   }
 
   private CommentService getCommentService() {
     if (commentService == null) {
       CommentServiceFactory serviceFactory = CommentServiceFactory.getFactory();
       commentService = serviceFactory.getCommentService();
     }
     return commentService;
   }
 
   private NotationBm getNotationBm() {
     if (notationBm == null) {
       try {
         notationBm = (NotationBm) EJBDynaProxy.createProxy(
             JNDINames.NOTATIONBM_EJBHOME, NotationBm.class);
       } catch (Exception e) {
         throw new KmeliaRuntimeException("KmeliaTagUtil.getNotationBm",
             SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
       }
     }
     return notationBm;
   }
 
   public void setVisibilityFilter(String visibilityFilter) {
     this.visibilityFilter = visibilityFilter;
   }
 
   public String getVisibilityFilter() {
     return visibilityFilter;
   }
 
   private String getSpaceId() {
     return this.spaceId;
   }
 
   private String getComponentId() {
     return this.componentId;
   }
 
   private String getSiteLanguage() {
     return SiteTagUtil.getLanguage();
   }
 
   public SpaceInst getSpaceInst() throws Exception {
     //SpaceInst spaceInst = getAdminBm().getSpaceInstById(this.spaceId);
     SpaceInst spaceInst = null;
     ComponentInst componentInst = getComponentInst();
     if (componentInst != null) {
       String fatherSpaceId = componentInst.getDomainFatherId();
       spaceInst = getAdmin().getSpaceInst(fatherSpaceId);
     }
     return spaceInst;
   }
 
   public ComponentInst getComponentInst() throws Exception {
     //ComponentInst compoInst = getAdminBm().getComponentInst(this.componentId);
     ComponentInst compoInst = getAdmin().getComponentInst(this.componentId);
     return compoInst;
   }
 
   public String getComponentLabel() throws Exception {
     return getComponentInst().getLabel();
   }
 
   /**************************************************************************************/
   /* KMelia - Gestion des publications                                                  */
   /**************************************************************************************/
   private boolean checkPublicationStatus(String pubId) throws RemoteException, VisibilityException {
     PublicationDetail pubDetail = getPublicationDetail(pubId);
     return checkPublicationStatus(pubDetail);
   }
 
   private boolean checkPublicationStatus(PublicationDetail pubDetail) throws VisibilityException {
     String pubStatus = pubDetail.getStatus();
     if (SiteTagUtil.isProdMode()) {
       if (!"Valid".equalsIgnoreCase(pubDetail.getStatus())) {
         throw new VisibilityException();
       }
     } else if (SiteTagUtil.isRecetteMode()) {
       if (!"Valid".equalsIgnoreCase(pubStatus) && !"ToValidate".equalsIgnoreCase(pubStatus)) {
         throw new VisibilityException();
       }
     } else {
       //Site's mode is dev : all publications are visible
     }
     return true;
   }
 
   /**
    * Get filtered publications
    * @param publicationDetails
    * @return Collection
    */
   private Collection<PublicationDetail> filterPublications(Collection<PublicationDetail> publicationDetails) {
     List<PublicationDetail> filteredPublications = new ArrayList<PublicationDetail>();
     for(PublicationDetail pubDetail : publicationDetails) {
       try {
         if (visibilityFilter != null) {
           if (pubDetail.getName().startsWith(visibilityFilter)) {
             continue;
           }
         }
         checkPublicationStatus(pubDetail);
         checkPublicationLocation(pubDetail);
         filteredPublications.add( getTranslatedPublication(pubDetail, null));
       } catch (VisibilityException ae) {
         //this publication cannot be display according its status and site's mode
       } catch (RemoteException ae) {
         //this publication cannot be display according its status and site's mode
       }
     }
     return filteredPublications;
   }
 
   private void checkPublicationLocation(String pubId) throws RemoteException, VisibilityException {
     PublicationDetail pubDetail = getPublicationDetail(pubId);
     checkPublicationLocation(pubDetail);
   }
 
   private void checkPublicationLocation(PublicationDetail pubDetail) throws RemoteException,
       VisibilityException {
 
     List fathers = (List) getKmeliaBm().getPublicationFathers(pubDetail.getPK());
 
     if (fathers == null || fathers.size() == 0 || (fathers.size() == 1 && ((NodePK) fathers.get(0)).
         getId().equals("1"))) {
       throw new VisibilityException();
     }
   }
 
   public Integer getPublicationCommentsCount(String pubId) throws RemoteException,
       VisibilityException {
     Integer commentsCount = new Integer(0);
     PublicationPK publicationKey = new PublicationPK(pubId, this.getComponentId());
     int count = getCommentService().getCommentsCountOnPublication(publicationKey);
     if (count > 0) {
       commentsCount = new Integer(count);
     }
     return commentsCount;
   }
 
   /**
    * Get publicationDetail
    * @param pubId
    * @return PublicationDetail
    * @throws RemoteException
    * @throws VisibilityException
    */
   public PublicationDetail getPublicationDetail(String pubId) throws RemoteException,
       VisibilityException {
     return getPublicationDetail(pubId, null);
   }
 
   /**
    * Get publicationDetail
    * @param pubId
    * @param language
    * @return PublicationDetail
    * @throws RemoteException
    * @throws VisibilityException
    */
   public PublicationDetail getPublicationDetail(String pubId, String language) throws
       RemoteException, VisibilityException {
     try {
       SilverTrace.info("kmelia", "KMeliaTagUtil.getPublicationDetail()", "root.MSG_GEN_ENTER_METHOD",
           "pubId = " + pubId);
       PublicationDetail pubDetail = getPublicationBm().getDetail(getPublicationPK(pubId));
 
       //check status according to site's mode
       checkPublicationStatus(pubDetail);
       checkPublicationLocation(pubDetail);
 
       pubDetail = getTranslatedPublication(pubDetail, language);
 
       return pubDetail;
     } catch (NoSuchObjectException nsoe) {
       //initEJB();
       initPublicationEJB();
       return null;
 //			return getPublicationDetail(pubId);
     }
   }
 
   public Collection getLinkedPublications(String pubId) throws RemoteException, VisibilityException {
     try {
       SilverTrace.info("kmelia", "KMeliaTagUtil.getLinkedPublications()",
           "root.MSG_GEN_ENTER_METHOD", "pubId = " + pubId);
       CompletePublication pubComplete = getPublicationBm().getCompletePublication(getPublicationPK(
           pubId));
       List<ForeignPK> targets = pubComplete.getLinkList();
       SilverTrace.info("kmelia", "KMeliaTagUtil.getLinkedPublications()",
           "root.MSG_GEN_ENTER_METHOD", "nb linked publications = " + targets.size());
       List<PublicationPK> targetPKs = new ArrayList<PublicationPK>();
       for(ForeignPK foreignPk : targets) {
         targetPKs.add(new PublicationPK(foreignPk.getId(), foreignPk.getInstanceId()));
       }
       return filterPublications(getPublicationBm().getPublications(targetPKs));
     } catch (NoSuchObjectException nsoe) {
       initPublicationEJB();
       //			initEJB();
       return getLinkedPublications(pubId);
     }
   }
 
   public Collection getPublicationsOnSameSubject(String pubId) throws RemoteException,
       VisibilityException {
     try {
       SilverTrace.info("kmelia", "KMeliaTagUtil.getPublicationsOnSameSubject()",
           "root.MSG_GEN_ENTER_METHOD", "pubId = " + pubId);
 
       //get name and keywords of the publication to launch a request.
       PublicationDetail pubDetail = getPublicationBm().getDetail(getPublicationPK(pubId));
 
       //launch the search
       QueryDescription query = new QueryDescription(pubDetail.getName() + " " + pubDetail.
           getKeywords());
       query.setSearchingUser(getUserId());
       query.addSpaceComponentPair(getSpaceId(), getComponentId());
       getSearchEngineBm().search(query);
       MatchingIndexEntry[] result = getSearchEngineBm().getRange(0, getSearchEngineBm().getResultLength());
 
 
       //get each publication according to result's list
       MatchingIndexEntry mie = null;
       List<PublicationPK> pubPKs = new ArrayList<PublicationPK>();
       for (int r = 0; r < result.length; r++) {
         mie = result[r];
         if (mie != null && !mie.getObjectId().equals(pubId)) {
           if ("Publication".equals(mie.getObjectType())) {
             pubPKs.add(getPublicationPK(mie.getObjectId()));
           }
         }
       }
       return filterPublications(getPublicationBm().getPublications(pubPKs));
     } catch (NoSuchObjectException nsoe) {
       initEJB();
       return getPublicationsOnSameSubject(pubId);
     }
   }
 
   public InfoDetail getInfoDetail(String pubId) throws RemoteException, VisibilityException {
     SilverTrace.info("kmelia", "KMeliaTagUtil.getInfoDetail()", "root.MSG_GEN_ENTER_METHOD",
         "pubId = " + pubId);
     try {
       checkPublicationStatus(pubId);
       checkPublicationLocation(pubId);
 
       return getPublicationBm().getInfoDetail(getPublicationPK(pubId));
     } catch (NoSuchObjectException nsoe) {
       initEJB();
       return getInfoDetail(pubId);
     }
   }
 
   public UserCompletePublication getUserCompletePublication(String pubId) throws RemoteException,
       VisibilityException {
     SilverTrace.info("kmelia", "KMeliaTagUtil.getUserCompletePublication()",
         "root.MSG_GEN_ENTER_METHOD", "pubId = " + pubId);
     try {
       UserCompletePublication ucPublication = getKmeliaBm().getUserCompletePublication(getPublicationPK(
           pubId), getUserId());
 
       PublicationDetail pub = ucPublication.getPublication().getPublicationDetail();
       checkPublicationStatus(pub);
       checkPublicationLocation(pub);
 
       return ucPublication;
     } catch (NoSuchObjectException nsoe) {
       initEJB();
       return getUserCompletePublication(pubId);
     }
   }
 
   public CompletePublication getCompletePublication(String pubId) throws RemoteException,
       VisibilityException {
     SilverTrace.info("kmelia", "KMeliaTagUtil.getCompletePublication()", "root.MSG_GEN_ENTER_METHOD",
         "pubId = " + pubId);
     try {
       CompletePublication cPublication = getPublicationBm().getCompletePublication(getPublicationPK(
           pubId));
 
       PublicationDetail pub = cPublication.getPublicationDetail();
       checkPublicationStatus(pub);
       checkPublicationLocation(pub);
 
       return cPublication;
     } catch (NoSuchObjectException nsoe) {
       initEJB();
       return getCompletePublication(pubId);
     }
   }
 
   public FullPublication getFullPublication(String pubId) throws RemoteException,
       VisibilityException {
     SilverTrace.info("kmelia", "KMeliaTagUtil.getFullPublication()", "root.MSG_GEN_ENTER_METHOD",
         "pubId = " + pubId);
     try {
       FullPublication fullPublication = getKmeliaBm().getFullPublication(new PublicationPK(pubId,
           componentId));
 
       PublicationDetail pub = fullPublication.getPublication().getPublicationDetail();
       checkPublicationStatus(pub);
       checkPublicationLocation(pub);
 
       return fullPublication;
     } catch (NoSuchObjectException nsoe) {
       initEJB();
       return getFullPublication(pubId);
     }
   }
 
   public Collection getAllPublications() throws RemoteException {
     SilverTrace.info("kmelia", "KMeliaTagUtil.getAllPublications()", "root.MSG_GEN_ENTER_METHOD");
     try {
       return filterPublications(getPublicationBm().getAllPublications(new PublicationPK("useless",
           componentId), "P.pubCreationDate desc"));
     } catch (NoSuchObjectException nsoe) {
       initEJB();
       return getAllPublications();
     }
   }
 
   /**
    * The parameter must be split to get 3 parameters. The delimiter is a ,
    */
   public Collection getPublicationsByTopic(String topicIdAndColumnNameAndSort) throws
       RemoteException {
     try {
       SilverTrace.info("kmelia", "KMeliaTagUtil.getPublicationsByTopic()",
           "root.MSG_GEN_ENTER_METHOD",
           "topicIdAndColumnNameAndSort = " + topicIdAndColumnNameAndSort);
       return getPublications(topicIdAndColumnNameAndSort, false);
     } catch (NoSuchObjectException nsoe) {
       initEJB();
       return getPublicationsByTopic(topicIdAndColumnNameAndSort);
     }
   }
 
   public Collection getPublicationsBySubTree(String topicIdAndColumnNameAndSort) throws
       RemoteException {
     try {
       SilverTrace.info("kmelia", "KMeliaTagUtil.getPublicationsBySubTree()",
           "root.MSG_GEN_ENTER_METHOD",
           "topicIdAndColumnNameAndSort = " + topicIdAndColumnNameAndSort);
 
       return getPublications(topicIdAndColumnNameAndSort, true);
     } catch (NoSuchObjectException nsoe) {
       initEJB();
       return getPublicationsBySubTree(topicIdAndColumnNameAndSort);
     }
   }
 
   /**
    * The parameter must be split to get 3 parameters. The delimiter is a ,
    */
   private Collection getPublications(String topicIdAndColumnNameAndSort, boolean recursive) throws
       RemoteException {
     try {
       SilverTrace.info("kmelia", "KMeliaTagUtil.getPublications()", "root.MSG_GEN_ENTER_METHOD",
           "topicIdAndColumnNameAndSort = " + topicIdAndColumnNameAndSort);
 
       StringTokenizer tokenizer = new StringTokenizer(topicIdAndColumnNameAndSort, ",");
       int i = 1;
       String param = "";
       String topicId = "";
       String columnName = null;
       String sort = null;
       while (tokenizer.hasMoreTokens()) {
         param = tokenizer.nextToken();
         if (i == 1) {
           topicId = param;
         } else if (i == 2) {
           columnName = param;
         } else if (i == 3) {
           sort = param;
         }
         i++;
       }
 
       SilverTrace.info("kmelia", "KMeliaTagUtil.getPublications()", "root.MSG_GEN_PARAM_VALUE",
           "topicId = " + topicId);
       SilverTrace.info("kmelia", "KMeliaTagUtil.getPublications()", "root.MSG_GEN_PARAM_VALUE",
           "sort = " + sort);
 
       String sortString = getSortString(columnName, sort);
 
       //NodePK			nodePK			= new NodePK(topicId, spaceId, componentId);
       NodePK nodePK = getNodePK(topicId);
       List publications = null;
       ArrayList nodeIds = new ArrayList();
       if (recursive) {
         //get all nodes of the subtree where root is topicId
         ArrayList nodes = getNodeBm().getSubTree(nodePK);
 
         NodeDetail nodeDetail = null;
         for (int n = 0; n < nodes.size(); n++) {
           nodeDetail = (NodeDetail) nodes.get(n);
           if (NodeDetail.STATUS_VISIBLE.equals(nodeDetail.getStatus())) {
             nodeIds.add(nodeDetail.getNodePK().getId());
           }
         }
       } else {
         //get only publications linked to the topic identified by topicId
         nodeIds.add(nodePK.getId());
       }
 
       PublicationPK pubPK = new PublicationPK("useless", componentId);
       if (SiteTagUtil.isDevMode()) {
         //all publications are shown
         //we do nothing
         publications = (List) getPublicationBm().getDetailsByFatherIds(nodeIds, pubPK, sortString);
       } else if (SiteTagUtil.isRecetteMode()) {
         //all publications are shown except the ones which are in draft
         ArrayList statusList = new ArrayList();
         statusList.add("Valid");
         statusList.add("ToValidate");
         publications = (List) getPublicationBm().getDetailsByFatherIdsAndStatusList(nodeIds, pubPK,
             sortString, statusList);
       } else {
         //Production mode is enabled. Only validated publications are shown
         String status = "Valid";
         publications = (List) getPublicationBm().getDetailsByFatherIdsAndStatus(nodeIds, pubPK,
             sortString, status);
       }
 
       /*
        * publication is ignored if pub name starts with visibility filter
        */
       if (visibilityFilter != null) {
         Iterator it = publications.iterator();
         while (it.hasNext()) {
           PublicationDetail pubDetail = (PublicationDetail) it.next();
           if (pubDetail.getName().startsWith(visibilityFilter)) {
             it.remove();
           }
         }
       }
 
 
       if (!StringUtil.isDefined(getSiteLanguage())) {
         return publications;
       }
 
       if (publications != null && StringUtil.isDefined(getSiteLanguage())) {
         PublicationDetail publi = null;
         for (int p = 0; p < publications.size(); p++) {
           publi = (PublicationDetail) publications.get(p);
 
           publi.setName(publi.getName(getSiteLanguage()));
           publi.setDescription(publi.getDescription(getSiteLanguage()));
           publi.setKeywords(publi.getKeywords(getSiteLanguage()));
         }
       }
       return publications;
     } catch (NoSuchObjectException nsoe) {
       initEJB();
       return getPublications(topicIdAndColumnNameAndSort, recursive);
     }
   }
 
   private String getSortString(String columnName, String sort) {
     if (columnName != null) {
       //first letter must be in upper case
       String firstLetter = columnName.substring(0, 1);
       columnName = firstLetter.toUpperCase() + columnName.substring(1, columnName.length());
     }
 
     SilverTrace.info("kmelia", "KMeliaTagUtil.getSortString()", "root.MSG_GEN_PARAM_VALUE",
         "columnName = " + columnName);
     String sortString = null;
     if (columnName != null) {
       if (columnName.equals("Order")) {
         sortString = "F.pub" + columnName;
       } else {
         sortString = "P.pub" + columnName;
       }
       if (sort != null) {
         sortString += " ";
         sortString += sort;
       }
     }
     SilverTrace.info("kmelia", "KMeliaTagUtil.getSortString()", "root.MSG_GEN_PARAM_VALUE",
         "sortString = " + sortString);
     return sortString;
   }
 
   public String getPublicationHTMLContent(String pubId) throws RemoteException, VisibilityException,
       WysiwygException {
 
     //check publication
     checkPublicationStatus(pubId);
     checkPublicationLocation(pubId);
 
     String htmlContent = "";
     String wysiwygContent = getWysiwyg(pubId);
     if (wysiwygContent == null) {
       //the publication have no content of type wysiwyg
       //The content is maybe stored in a database model
       //CompletePublication completePublication = getKmeliaBm().getCompletePublication(pubId);
       CompletePublication completePublication = getPublicationBm().getCompletePublication(getPublicationPK(
           pubId));
       InfoDetail infoDetail = completePublication.getInfoDetail();
       ModelDetail modelDetail = completePublication.getModelDetail();
       if (infoDetail != null && modelDetail != null) {
         //the publication have some content
         //we merge the content with the model's displayer template
         htmlContent = getModelHtmlContent(modelDetail, infoDetail);
       }
     } else {
       htmlContent = wysiwygContent;
     }
     return parseHtmlContent(htmlContent);
   }
 
   public Collection getPublicationPath(String pubId) throws RemoteException {
     Collection path = new ArrayList();
     try {
       SilverTrace.info("kmelia", "KMeliaTagUtil.getPathList()", "root.MSG_GEN_ENTER_METHOD",
           "pubId = " + pubId);
       ArrayList paths = (ArrayList) getKmeliaBm().getPathList(getPublicationPK(pubId));
       if (paths.size() > 0) {
         //get only the first path
         List pathInReverse = (List) paths.get(0);
         //reverse the path from root to leaf
         for (int i = pathInReverse.size() - 1; i >= 0; i--) {
           path.add(pathInReverse.get(i));
         }
       }
     } catch (NoSuchObjectException nsoe) {
       initEJB();
       return getPublicationPath(pubId);
     }
     return path;
   }
 
   public NodeDetail getTopic(String topicId) throws RemoteException {
     SilverTrace.info("kmelia", "KMeliaTagUtil.getTopic()", "root.MSG_GEN_ENTER_METHOD",
         "topicId = " + topicId);
     try {
       NodeDetail topic = getNodeBm().getDetail(getNodePK(topicId));
       topic = getTranslatedNode(topic, null);
       if (SiteTagUtil.isDevMode()) {
         //Web site is in developpement mode
         //We get all sub topics (visibles and invisibles)
         topic.setChildrenDetails(getTranslatedSubTopics(topic, false));
       } else {
         //Web site is in 'recette' or 'production' mode
         //We get only visible subtopics
         topic.setChildrenDetails(getTranslatedSubTopics(topic, true));
       }
       return topic;
     } catch (NoSuchObjectException nsoe) {
       initEJB();
       return getTopic(topicId);
     }
   }
 
   /**
    * Used to get the most commented publication
    * @param condition number,formName,topicId for
    * for example 10,fiche_produit,30 or -1,fiche_article,22
    * @return Collection of PublicationDetail
    */
   public Collection getMostCommentedPublication(String condition) {
     StringTokenizer tokenizer = new StringTokenizer(condition, ",");
     String number = tokenizer.nextToken();
     int numberPublication = Integer.parseInt(number);
     String formName = tokenizer.nextToken();
     String topicId = tokenizer.nextToken();
     List comments = new ArrayList();
     try {
       if (topicId.equals("-1")) {
         ArrayList commentsCountSorted = new ArrayList();
         commentsCountSorted.addAll(this.getCommentService().getAllMostCommentedPublicationsInfo());
         Iterator iter = commentsCountSorted.iterator();
         while (iter.hasNext() && comments.size() < numberPublication) {
           CommentedPublicationInfo commentInfo = (CommentedPublicationInfo) iter.next();
           PublicationDetail publication = this.getPublicationDetail(commentInfo.getPublicationId());
           if (publication.getInfoId().equals(formName)) {
             comments.add(commentInfo);
           }
         }
         return comments;
       } else {
         Collection publications = getPublications(topicId, true);
         Iterator iter = publications.iterator();
         List commentsPks = new ArrayList();
         PublicationDetail publication;
         PublicationPK publicationPK;
         while (iter.hasNext()) {
           publication = (PublicationDetail) iter.next();
           if (publication.getInfoId().equals(formName)) {
             publicationPK = publication.getPK();
             commentsPks.add(
                 new CommentPK(publicationPK.getId(), null, publicationPK.getInstanceId()));
           }
         }
         if (!commentsPks.isEmpty()) {
           comments = getCommentService().getMostCommentedPublicationsInfo(commentsPks);
           if (comments.size() > numberPublication) {
             comments = comments.subList(0, numberPublication);
           }
         }
       }
     } catch (Exception e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
     return comments;
   }
 
   public Collection getMostPopularPublicationsNotations(String topicIdAndNotationsCount) throws
       RemoteException {
     StringTokenizer st = new StringTokenizer(topicIdAndNotationsCount, ",");
     String topicId = st.nextToken();
     if (st.hasMoreTokens()) {
       int notationsCount = Integer.parseInt(st.nextToken());
       if (notationsCount > 0) {
         Collection publications = getPublications(topicId, true);
         Iterator iter = publications.iterator();
         ArrayList notationsPks = new ArrayList();
         PublicationDetail publication;
         PublicationPK publicationPK;
         while (iter.hasNext()) {
           publication = (PublicationDetail) iter.next();
           publicationPK = publication.getPK();
           notationsPks.add(new NotationPK(publicationPK.getId(),
               publicationPK.getComponentName(), Notation.TYPE_PUBLICATION));
         }
 
         if (!notationsPks.isEmpty()) {
           return getNotationBm().getBestNotations(notationsPks, notationsCount);
         }
       }
     }
     return new ArrayList();
   }
 
   public PublicationDetail getTopicEdito(String topicId) throws RemoteException {
     SilverTrace.info("kmelia", "KMeliaTagUtil.getTopicEdito()", "root.MSG_GEN_ENTER_METHOD",
         "topicId = " + topicId);
     try {
       //get all publications of topicId
       String topicIdAndColumnNameAndSort = topicId + ",CreationDate,Desc";
       List publications = (List) getPublicationsByTopic(topicIdAndColumnNameAndSort);
       PublicationDetail publi, publiEdito = null;
       for (int p = 0; p < publications.size(); p++) {
         publi = (PublicationDetail) publications.get(p);
         if (publi.getName().substring(0, BEGIN_EDITO.length()).equals(BEGIN_EDITO)) {
           publiEdito = publi;
           break;
         }
       }
 
       return publiEdito;
     } catch (NoSuchObjectException nsoe) {
       initEJB();
       return getTopicEdito(topicId);
     }
   }
 
   private List getTranslatedSubTopics(NodeDetail node, boolean showOnlyVisibles) {
     List result = new ArrayList();
 
     List subTopics = (List) node.getChildrenDetails();
     if (subTopics != null && subTopics.size() > 0) {
       NodeDetail subTopic = null;
       for (int i = 0; i < subTopics.size(); i++) {
         subTopic = (NodeDetail) subTopics.get(i);
         subTopic = getTranslatedNode(subTopic, null);
         if (showOnlyVisibles) {
           if (NodeDetail.STATUS_VISIBLE.equals(subTopic.getStatus())) {
             result.add(subTopic);
           }
         } else {
           result.add(subTopic);
         }
       }
     }
     return result;
   }
 
   private boolean checkTopicStatus(String nodeId) throws RemoteException, VisibilityException {
     NodeDetail nodeDetail = getTopic(nodeId);
     return checkTopicStatus(nodeDetail);
   }
 
   private boolean checkTopicStatus(NodeDetail nodeDetail) throws VisibilityException {
     String status = nodeDetail.getStatus();
     if (!SiteTagUtil.isDevMode()) {
       if (NodeDetail.STATUS_INVISIBLE.equalsIgnoreCase(status)) {
         throw new VisibilityException();
       }
     }
     return true;
   }
 
   public Collection getTopicPath(String topicId) throws RemoteException {
     SilverTrace.info("kmelia", "KMeliaTagUtil.getTopicPath()", "root.MSG_GEN_ENTER_METHOD",
         "topicId = " + topicId);
     Collection path = new ArrayList();
     try {
       List pathInReverse = (List) getNodeBm().getPath(getNodePK(topicId));
       //reverse the path from root to leaf
       for (int i = pathInReverse.size() - 1; i >= 0; i--) {
         path.add(pathInReverse.get(i));
       }
     } catch (NoSuchObjectException nsoe) {
       initEJB();
       return getTopicPath(topicId);
     }
     return path;
   }
 
   public Collection getTreeView(String topicId) throws RemoteException {
     SilverTrace.info("kmelia", "KMeliaTagUtil.getTreeView()", "root.MSG_GEN_ENTER_METHOD",
         "topicId = " + topicId);
     List tree = new ArrayList();
     try {
       tree = (ArrayList) getNodeBm().getSubTree(getNodePK(topicId));
 
       //if topicId is the root, remove "basket" and "declassified zone"
       if ("0".equals(topicId)) {
         tree = removeSpecificNodes(tree);
       }
 
       if (SiteTagUtil.isDevMode()) {
         //Web site is in developpement mode
         //We get all topics (visibles and invisibles)
         if (StringUtil.isDefined(SiteTagUtil.getLanguage())) {
           NodeDetail node = null;
           for (int n = 0; n < tree.size(); n++) {
             node = (NodeDetail) tree.get(n);
             getTranslatedNode(node, SiteTagUtil.getLanguage());
           }
         } else {
           return tree;
         }
       } else {
         //Web site is in 'recette' or 'production' mode
         //We get only visible topics
         tree = (ArrayList) getVisibleTreeView(tree);
       }
     } catch (NoSuchObjectException nsoe) {
       initEJB();
       return getTreeView(topicId);
     }
     return tree;
   }
 
   private List removeSpecificNodes(List tree) {
     NodeDetail node = null;
     String id = null;
     List nodes = new ArrayList();
     for (int i = 0; i < tree.size(); i++) {
       node = (NodeDetail) tree.get(i);
       id = node.getNodePK().getId();
       if (!"1".equals(id) && !"2".equals(id)) {
         nodes.add(node);
       }
     }
     return nodes;
   }
 
   private Collection getVisibleTreeView(List tree) throws RemoteException {
     SilverTrace.info("kmelia", "KMeliaTagUtil.getVisibleTreeView()", "root.MSG_GEN_ENTER_METHOD");
 
     NodeDetail node = null;
     ArrayList visibleNodes = new ArrayList();
     ArrayList invisibleNodes = new ArrayList();
     for (int i = 0; i < tree.size(); i++) {
       node = (NodeDetail) tree.get(i);
       node = getTranslatedNode(node, null);
       if (NodeDetail.STATUS_INVISIBLE.equals(node.getStatus())) {
         if (i == 0) {
           return visibleNodes;
         } else {
           //the node is invisible. We do not add it to the result treeview
           invisibleNodes.add(node.getNodePK().getId());
         }
       } else {
         if (invisibleNodes.contains(node.getFatherPK().getId())) {
           //the father is invisible. Even if the node is Visible, we do not add it to the result treeview.
           invisibleNodes.add(node.getNodePK().getId());
         } else {
           node.setChildrenDetails(getTranslatedSubTopics(node, true));
           visibleNodes.add(node);
         }
       }
     }
     return visibleNodes;
   }
 
   public String getTopicHTMLContent(String topicId) throws RemoteException, VisibilityException,
       WysiwygException {
 
     //check topic status
     checkTopicStatus(topicId);
 
     String wysiwygContent = getWysiwyg("Node_" + topicId);
     if (wysiwygContent == null) {
       wysiwygContent = "";
     }
     return parseHtmlContent(wysiwygContent);
   }
 
   public int getSilverObjectId(String pubId) throws RemoteException {
     SilverTrace.info("kmelia", "KmeliaTagUtil.getSilverObjectId()", "root.MSG_GEN_ENTER_METHOD",
         "pubId = " + pubId);
     int silverObjectId = -1;
     try {
       silverObjectId = getKmeliaBm().getSilverObjectId(getPublicationPK(pubId));
     } catch (NoSuchObjectException nsoe) {
       initEJB();
       return getSilverObjectId(pubId);
     }
     return silverObjectId;
   }
   
 
   /**
    * renvoit l'URL de la vignette de la publication, ou chaine vide s'il n'y a pas de vignette
    * @param pubId
    * @return String not null
    */
   public String getVignetteURL(String pubId) throws RemoteException, VisibilityException {
     SilverTrace.info("kmelia", "KmeliaTagUtil.getVignetteURL()", "root.MSG_GEN_ENTER_METHOD",
         "pubId = " + pubId);
     String imageURL = "";
     PublicationDetail pubDetail = getPublicationDetail(pubId);
   	if(pubDetail.getImage() != null) {
   		if(pubDetail.getImage().startsWith("/")) {//image provenant de la photothèque
   			imageURL = pubDetail.getImage();
   		} else {//image téléchargée
   			imageURL = "vignette?ComponentId="+pubDetail.getInstanceId()+"&SourceFile="+pubDetail.getImage()+"&MimeType=image/jpeg&Directory=images";
   		}
   	}
     return imageURL;
   }
 
   /**************************************************************************************/
   /* KMelia - Gestion des validations                                                   */
   /**************************************************************************************/
   public Collection getPublicationsToValidate() throws RemoteException {
     try {
       SilverTrace.info("kmelia", "KmeliaTagUtil.getPublicationsToValidate()",
           "root.MSG_GEN_ENTER_METHOD");
       return getPublicationBm().getPublicationsByStatus("ToValidate", getPublicationPK("useless"));
     } catch (NoSuchObjectException nsoe) {
       initEJB();
       return getPublicationsToValidate();
     }
   }
 
   /**************************************************************************************/
   /* Gestion des fichiers joints		                                                  */
   /**************************************************************************************/
   public Collection getAttachments(String pubId) throws RemoteException, VisibilityException {
     SilverTrace.info("kmelia", "KmeliaTagUtil.getAttachments()", "root.MSG_GEN_ENTER_METHOD",
         "pubId = " + pubId);
     try {
       //check publication
       checkPublicationStatus(pubId);
       checkPublicationLocation(pubId);
 
       List attachments = (List) getKmeliaBm().getAttachments(getPublicationPK(pubId));
 
       if (!StringUtil.isDefined(SiteTagUtil.getLanguage())) {
         return attachments;
       }
 
       AttachmentDetail attachment = null;
       for (int a = 0; a < attachments.size(); a++) {
         attachment = (AttachmentDetail) attachments.get(a);
         AttachmentDetailI18N translation = (AttachmentDetailI18N) attachment.getTranslation(SiteTagUtil.
             getLanguage());
         if (translation != null) {
           attachment.setLogicalName(translation.getLogicalName());
           attachment.setPhysicalName(translation.getPhysicalName());
           attachment.setType(translation.getType());
           attachment.setTitle(translation.getTitle());
           attachment.setSize(translation.getSize());
           attachment.setInfo(translation.getInfo());
         }
       }
       return attachments;
 
     } catch (NoSuchObjectException nsoe) {
       initEJB();
       return getAttachments(pubId);
     }
   }
 
   /**
    * Get an attachmentDetail
    * @param attachmentId
    * @return AttachmentDetail
    */
   public AttachmentDetail getAttachment(String attachmentId) throws RemoteException,
       VisibilityException {
     SilverTrace.info("kmelia", "KmeliaTagUtil.getAttachment()", "root.MSG_GEN_ENTER_METHOD",
         "attachmentId = " + attachmentId);
     AttachmentDetail attachment = AttachmentController.searchAttachmentByPK(new AttachmentPK(
         attachmentId));
     return attachment;
   }
 
   public String getWysiwyg(String pubId) throws RemoteException, VisibilityException,
       WysiwygException {
     SilverTrace.info("kmelia", "KmeliaTagUtil.getWysiwyg()", "root.MSG_GEN_ENTER_METHOD",
         "pubId = " + pubId);
     try {
       if (!pubId.startsWith("Node")) {
         //check publication
         checkPublicationStatus(pubId);
         checkPublicationLocation(pubId);
 
       }
       String wysiwyg = WysiwygController.load(componentId, pubId, getSiteLanguage());
 
       return parseHtmlContent(wysiwyg);
     } catch (NoSuchObjectException nsoe) {
       initEJB();
       return getWysiwyg(pubId);
     }
   }
 
   private String getModelHtmlContent(ModelDetail model, InfoDetail infos) {
     String toParse = model.getHtmlDisplayer();
     Iterator textIterator = infos.getInfoTextList().iterator();
     Iterator imageIterator = infos.getInfoImageList().iterator();
     StringBuilder htmlContent = new StringBuilder();
 
     int posit = toParse.indexOf("%WA");
     while (posit != -1) {
       if (posit > 0) {
         htmlContent.append(toParse.substring(0, posit));
         toParse = toParse.substring(posit);
       }
       if (toParse.startsWith("%WATXTDATA%")) {
         if (textIterator.hasNext()) {
           InfoTextDetail textDetail = (InfoTextDetail) textIterator.next();
           htmlContent.append(encode(textDetail.getContent()));
         }
         toParse = toParse.substring(11);
       } else if (toParse.startsWith("%WAIMGDATA%")) {
         if (imageIterator.hasNext()) {
           InfoImageDetail imageDetail = (InfoImageDetail) imageIterator.next();
           String logicalName = imageDetail.getLogicalName();
           String physicalName = imageDetail.getPhysicalName();
           String mimeType = imageDetail.getType();
           String type = logicalName.substring(logicalName.lastIndexOf(".") + 1, logicalName.length());
 
           if (type.equalsIgnoreCase("gif") || type.equalsIgnoreCase("jpg") || type.equalsIgnoreCase(
               "jpe") || type.equalsIgnoreCase("jpeg") || type.equalsIgnoreCase("png")) {
             String url = FileServerUtils.getUrl(imageDetail.getPK().getSpace(), imageDetail.getPK().
                 getComponentName(), logicalName, physicalName, mimeType, "images");
             url = "http://fakeServer:fakePort" + url;
             htmlContent.append("<IMG BORDER=\"0\" SRC=\"" + url + "\">");
           }
         }
         toParse = toParse.substring(11);
       }
 
       // et on recommence
       posit = toParse.indexOf("%WA");
     }
     return htmlContent.toString();
   }
 
   private String encode(String javastring) {
     StringBuffer res = new StringBuffer();
     if (javastring == null) {
       return res.toString();
     }
     for (int i = 0; i < javastring.length(); i++) {
       switch (javastring.charAt(i)) {
         case '\n':
           res.append("<br>");
           break;
         case '\t':
           res.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
           break;
         default:
           res.append(javastring.charAt(i));
       }
     }
     return res.toString();
   }
 
   /**
    *
    * @throws VisibilityException
    * @throws RemoteException
    */
   public String getParsedFieldValue(String pubIdAndFieldName) throws RemoteException,
       VisibilityException {
     StringTokenizer tokenizer = new StringTokenizer(pubIdAndFieldName, ",");
 
     String pubId = "";
     String fieldName = "";
 
     int i = 1;
     while (tokenizer.hasMoreTokens()) {
       String param = tokenizer.nextToken();
       if (i == 1) {
         pubId = param;
       } else if (i == 2) {
         fieldName = param;
       }
       i++;
     }
 
     PublicationDetail detail = getPublicationDetail(pubId);
     String fieldValue = detail.getFieldValue(fieldName);
     if(fieldValue == null) {
       fieldValue = "";
     }
    
    //Particular case of Image Field (in the XML forms) 
    String webContext = SiteTagUtil.getServerContext();
    List<XMLField> xmlFields = detail.getXmlFields();
    XMLField xmlField = null;
    for (int x = 0; x < xmlFields.size(); x++) {
      xmlField = xmlFields.get(x);
      if (fieldName.equals(xmlField.getName())) {
        if (xmlField.getValue() != null && (xmlField.getValue().startsWith("image_") || xmlField.getValue().startsWith("file_"))) {
          //prefix the value with the webContext
          fieldValue = webContext + fieldValue;
          break;
        }
      }
    }
     return parseHtmlContent(fieldValue);
   }
 
   /**
    * There's a problem with images. Links to images are like http://server_name:port/silverpeas/FileServer/image.jpg?...
    * The servlet FileServer is used. This works in the traditional context of silverpeas.
    * But, it does not works in the taglibs context because the FileServer is securised.
    * In taglibs context, the servlet WebFileServer must be used.
   * The string http://server_name:port/silverpeas/FileServer must be replaced by http://webServer_name:port/webContext/attached_file
    * The web context is provided by the tag Site.
    * @param htmlContent not null
    */
   public String parseHtmlContent(String htmlContent) {
     if (htmlContent != null && htmlContent.length() > 0) {
       String content = htmlContent;
       String webContext = SiteTagUtil.getServerContext() + SiteTagUtil.getFileServerName() + "/";
       content = convertToWebUrl(content, "/FileServer/", webContext);
       content = convertToWebUrl(content, "/GalleryInWysiwyg/", webContext);
       content = convertRestToWebUrl(content, "/attached_file/", webContext);
 
       int finPath = 0;
       int debutPath = 0;
       StringBuilder newWysiwygText = new StringBuilder();
       String link = null;
       while (content.indexOf("href=\"", finPath) > -1) {
         debutPath = content.indexOf("href=\"", finPath);
         debutPath += 6;
 
         newWysiwygText.append(content.substring(finPath, debutPath));
 
         finPath = content.indexOf("\"", debutPath);
         link = content.substring(debutPath, finPath);
 
         int d = link.indexOf("../../");
         if (d != -1) {
           //C'est un lien relatif
           d += 6;
           newWysiwygText.append("/silverpeas/");
           newWysiwygText.append(link.substring(d, link.length()));
         } else {
           newWysiwygText.append(link);
         }
       }
       newWysiwygText.append(content.substring(finPath, content.length()));
       return newWysiwygText.toString().replaceAll("&amp;", "&");
     }
     return htmlContent;
   }
 
   private PublicationPK getPublicationPK(String pubId) {
     return new PublicationPK(pubId, getComponentId());
   }
 
   private NodePK getNodePK(String nodeId) {
     return new NodePK(nodeId, getComponentId());
   }
 
  /**
   * Replace the String "/xxx/servletMapping par "attachmentUrl
   * Used in the Wysiwyg field of the XML Forms
   * @param content : the String to replace, for example the String /silverpeas/attached_file/componentId/kmelia24/attachmentId/19578/lang/fr/name/ESAT_Rhone-Alpes_synthese.jpg
   * @param servletMapping : for example the String /attached_file/
   * @param attachmentUrl :  the webContext, for example the String /webContext/attached_file/
   * For example replace the String /silverpeas/attached_file/componentId/kmelia24/attachmentId/19578/lang/fr/name/ESAT_Rhone-Alpes_synthese.jpg
   * @return the String replaced, in the example, return /webContext/attached_file/componentId/kmelia24/attachmentId/19578/lang/fr/name/ESAT_Rhone-Alpes_synthese.jpg 
   */
   public String convertRestToWebUrl(String content, String servletMapping, String attachmentUrl) {
    return content.replaceAll("\"/[^/]*" + servletMapping , '"' + attachmentUrl);
   }
 
   public String convertToWebUrl(String content, String servletMapping, String webContext) {
     String htmlContent = content;
     while (htmlContent.indexOf(servletMapping) > -1) {
       int place = htmlContent.indexOf(servletMapping);
       String debut = htmlContent.substring(0, place);
       int srcPlace = debut.lastIndexOf('\"');
       debut = debut.substring(0, srcPlace + 1);
       String suite = htmlContent.substring(place + servletMapping.length(), htmlContent.length());
       int srcEndPlace = suite.indexOf('\"', srcPlace + 1);
       String url = "";
       if (srcEndPlace > 0) {
         url = suite.substring(0, srcEndPlace);
         url = url.replaceAll("&amp;", "&");
       } else {
         srcEndPlace = 0;
 
       }
       htmlContent = debut + webContext + url + suite.substring(srcEndPlace);
     }
     return htmlContent;
   }
 
   /**
    * Get translated Publication in current site lang or lang as parameter
    * @param pubDetail
    * @return PublicationDetail
    */
   private PublicationDetail getTranslatedPublication(PublicationDetail pubDetail, String language) {
     String lang = null;
     if (StringUtil.isDefined(language)) {
       lang = language;
     } else if (StringUtil.isDefined(getSiteLanguage())) {
       lang = getSiteLanguage();
     }
     if (StringUtil.isDefined(lang)) {
       PublicationI18N pubDetaili18n = (PublicationI18N) pubDetail.getTranslation(getSiteLanguage());
       if (pubDetaili18n != null) {
         pubDetail.setName(pubDetaili18n.getName());
         pubDetail.setDescription(pubDetaili18n.getDescription());
       }
     }
     return pubDetail;
   }
 
   /**
    * Get translated Node in current site lang or lang if parameter
    * @param nodeDetail
    * @param lang
    * @return NodeDetail
    */
   private NodeDetail getTranslatedNode(NodeDetail nodeDetail, String language) {
     String lang = null;
     if (StringUtil.isDefined(language)) {
       lang = language;
     } else if (StringUtil.isDefined(getSiteLanguage())) {
       lang = getSiteLanguage();
     }
     if (StringUtil.isDefined(lang)) {
       NodeI18NDetail nodeDetaili18n = (NodeI18NDetail) nodeDetail.getTranslation(getSiteLanguage());
       if (nodeDetaili18n != null) {
         nodeDetail.setName(nodeDetaili18n.getName());
         nodeDetail.setDescription(nodeDetaili18n.getDescription());
       }
     }
     return nodeDetail;
   }
 
   private PublicationBm getPublicationBm() {
     if (publicationBm == null) {
       try {
         publicationBm = (PublicationBm) EJBDynaProxy.createProxy(JNDINames.PUBLICATIONBM_EJBHOME,
             PublicationBm.class);
       } catch (Exception e) {
         throw new KmeliaRuntimeException("KmeliaTagUtil.getPublicationBm",
             SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
       }
     }
     return publicationBm;
   }
 
   private NodeBm getNodeBm() {
     if (nodeBm == null) {
       try {
         nodeBm = (NodeBm) EJBDynaProxy.createProxy(JNDINames.NODEBM_EJBHOME, NodeBm.class);
       } catch (Exception e) {
         throw new KmeliaRuntimeException("KmeliaTagUtil.getNodeBm", SilverpeasRuntimeException.ERROR,
             "root.EX_CANT_GET_REMOTE_OBJECT", e);
       }
     }
     return nodeBm;
   }
 }
