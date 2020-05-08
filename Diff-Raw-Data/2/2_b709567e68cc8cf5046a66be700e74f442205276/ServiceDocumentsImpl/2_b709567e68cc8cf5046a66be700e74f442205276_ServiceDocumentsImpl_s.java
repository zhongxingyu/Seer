 package com.silverpeas.mobile.server.services;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import com.silverpeas.mobile.shared.dto.documents.AttachmentDTO;
 import com.silverpeas.mobile.shared.dto.documents.PublicationDTO;
 import com.silverpeas.mobile.shared.dto.documents.TopicDTO;
 import com.silverpeas.mobile.shared.exceptions.AuthenticationException;
 import com.silverpeas.mobile.shared.exceptions.DocumentsException;
 import com.silverpeas.mobile.shared.services.ServiceDocuments;
 import com.stratelia.webactiv.kmelia.control.ejb.KmeliaBm;
 import com.stratelia.webactiv.kmelia.control.ejb.KmeliaBmHome;
 import com.stratelia.webactiv.util.EJBUtilitaire;
 import com.stratelia.webactiv.util.JNDINames;
 import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
 import com.stratelia.webactiv.util.node.control.NodeBm;
 import com.stratelia.webactiv.util.node.control.NodeBmHome;
 import com.stratelia.webactiv.util.node.model.NodeDetail;
 import com.stratelia.webactiv.util.node.model.NodePK;
 import com.stratelia.webactiv.util.publication.control.PublicationBm;
 import com.stratelia.webactiv.util.publication.control.PublicationBmHome;
 import com.stratelia.webactiv.util.publication.model.PublicationDetail;
 import com.stratelia.webactiv.util.publication.model.PublicationPK;
 
 /**
  * Service de gestion des GED.
  * @author svuillet
  */
 public class ServiceDocumentsImpl extends AbstractAuthenticateService implements ServiceDocuments {
 
 	private final static Logger LOGGER = Logger.getLogger(ServiceDocumentsImpl.class);
 	private static final long serialVersionUID = 1L;
 	private KmeliaBm kmeliaBm;
 	private PublicationBm pubBm;
 	private NodeBm nodeBm;
 	
 	
 	/**
 	 * Retourne tous les topics de premier niveau d'un topic.
 	 */
 	@Override
 	public List<TopicDTO> getTopics(String instanceId, String rootTopicId) throws DocumentsException, AuthenticationException {
 		checkUserInSession();
 		List<TopicDTO> topicsList = new ArrayList<TopicDTO>();
 		
 		try {
 			if (rootTopicId == null || rootTopicId.isEmpty()) {
 				rootTopicId = "0";			
 			}
 			NodePK pk = new NodePK(rootTopicId, instanceId);
 			NodeDetail rootNode = getNodeBm().getDetail(pk);			
 			ArrayList<NodeDetail> nodes = getNodeBm().getSubTreeByLevel(pk, rootNode.getLevel() + 1);
 			for (NodeDetail nodeDetail : nodes) {
 				
 				if (rootTopicId.equals(nodeDetail.getFatherPK().getId())) {				
 					TopicDTO topic = new TopicDTO();
 					topic.setId(String.valueOf(nodeDetail.getId()));
 					topic.setName(nodeDetail.getName());				
 					int childrenNumber = getNodeBm().getChildrenNumber(new NodePK(String.valueOf(nodeDetail.getId()), instanceId));
 					topic.setTerminal(childrenNumber == 0);
 					topicsList.add(topic);
 				}
 			}			
 		} catch (Exception e) {
 			LOGGER.error("getTopics", e);
 			throw new DocumentsException(e.getMessage());
 		}
 		return topicsList;
 	}
 	
 	/**
 	 * Retourne les publications d'un topic (au niveau 1).
 	 */
 	@Override
 	public List<PublicationDTO> getPublications(String instanceId, String topicId) throws DocumentsException, AuthenticationException {
 		checkUserInSession();
 		ArrayList<PublicationDTO> pubs = new ArrayList<PublicationDTO>();
 	
         try {
         	if (topicId == null || topicId.isEmpty()) {
         		topicId = "0";			
 			}
         	NodePK nodePK = new NodePK(topicId, instanceId);
         	PublicationPK pubPK = new PublicationPK("useless", instanceId);
     		String status = "Valid";
     		ArrayList<String> nodeIds = new ArrayList<String>();
     		nodeIds.add(nodePK.getId());   		
 			List<PublicationDetail> publications = (List<PublicationDetail>) getPubBm().getDetailsByFatherIdsAndStatus(nodeIds, pubPK, "pubname", status);			
 			for (PublicationDetail publicationDetail : publications) {
 				PublicationDTO dto = new PublicationDTO();
 				dto.setId(publicationDetail.getId());
 				dto.setName(publicationDetail.getName());
 				pubs.add(dto);				
 			}
 			
 		} catch (Exception e) {
 			LOGGER.error("getPublications", e);
 			throw new DocumentsException(e.getMessage());
 		}
 		
 		return pubs;
 	}
 	
 	private KmeliaBm getKmeliaBm() throws Exception {
 		if (kmeliaBm == null) {
 			KmeliaBmHome home = EJBUtilitaire.getEJBObjectRef(JNDINames.KMELIABM_EJBHOME, KmeliaBmHome.class);
 			kmeliaBm  = home.create();		
 		}
 		return kmeliaBm;
 	}
 	
 	private PublicationBm getPubBm() throws Exception {
 		if (pubBm == null) {			 
 			PublicationBmHome home = EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME, PublicationBmHome.class);
 			pubBm = home.create();
 		}
 		return pubBm;
 	}
 	
 	private NodeBm getNodeBm() throws Exception {
 		if (nodeBm == null) {
 			NodeBmHome home = EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeBmHome.class);
 			nodeBm = home.create(); 
 		}
 		return nodeBm;
 	}
 
 	@Override
 	public PublicationDTO getPublication(String pubId) throws DocumentsException, AuthenticationException {
 		checkUserInSession();				
 		try {
 			PublicationDetail pub = getPubBm().getDetail(new PublicationPK(pubId));
 			PublicationDTO dto = new PublicationDTO();
 			dto.setId(pub.getId());
 			dto.setName(pub.getName());
 			dto.setAuteur(pub.getAuthor());
 			dto.setVersion(pub.getVersion());
 			dto.setDescription(pub.getDescription());
 						
 			ArrayList<AttachmentDTO> attachments = new ArrayList<AttachmentDTO>();
 			for (AttachmentDetail attachment : pub.getAttachments()) {
 				AttachmentDTO attach = new AttachmentDTO();						
 				attach.setTitle(attachment.getTitle());
				if (attachment.getTitle().isEmpty()) {
 					attach.setTitle(attachment.getLogicalName());
 				}			
 				attach.setUrl(attachment.getOnlineURL());
 				attach.setType(attachment.getType());
 				attachments.add(attach);
 				attach.setAuthor(attachment.getAuthor());
 				attach.setOrderNum(attachment.getOrderNum());
 				attach.setSize(attachment.getSize());
 				attach.setCreationDate(attachment.getCreationDate());		
 			}
 			dto.setAttachments(attachments);		
 			
 			return dto;
 		} catch (Exception e) {
 			LOGGER.error("getPublication", e);
 			throw new DocumentsException(e.getMessage());
 		}	
 	}
 }
