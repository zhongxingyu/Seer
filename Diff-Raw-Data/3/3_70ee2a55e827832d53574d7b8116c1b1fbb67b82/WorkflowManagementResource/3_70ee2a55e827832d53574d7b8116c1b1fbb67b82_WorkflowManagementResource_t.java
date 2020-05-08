 package org.iucn.sis.server.extensions.workflow;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.iucn.sis.server.api.application.SIS;
 import org.iucn.sis.server.extensions.integrity.IntegrityValidator;
 import org.iucn.sis.shared.api.models.Assessment;
 import org.iucn.sis.shared.api.models.AssessmentType;
 import org.iucn.sis.shared.api.models.WorkingSet;
 import org.iucn.sis.shared.api.workflow.WorkflowStatus;
 import org.iucn.sis.shared.api.workflow.WorkflowUserInfo;
 import org.restlet.Context;
 import org.restlet.data.MediaType;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.data.Status;
 import org.restlet.ext.xml.DomRepresentation;
 import org.restlet.representation.Representation;
 import org.restlet.representation.Variant;
 import org.restlet.resource.ResourceException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 
 import com.solertium.db.CanonicalColumnName;
 import com.solertium.db.DBException;
 import com.solertium.db.query.QConstraint;
 import com.solertium.db.query.SelectQuery;
 import com.solertium.db.restlet.DBResource;
 import com.solertium.util.BaseDocumentUtils;
 import com.solertium.util.NodeCollection;
 
 public class WorkflowManagementResource extends DBResource {
 	
 	private final WorkflowManager manager;
 	private final String workingSet;
 
 	public WorkflowManagementResource(Context context, Request request, Response response) {
 		super(context, request, response);
 		setModifiable(true);
 		manager = new WorkflowManager();
 		workingSet = (String)request.getAttributes().get("working-set");
 	}
 	
 	public Representation represent(Variant variant) throws ResourceException {
 		final SelectQuery query = new SelectQuery();
 		query.select(WorkflowConstants.WORKFLOW_TABLE, "*");
 		query.constrain(new CanonicalColumnName(WorkflowConstants.WORKFLOW_TABLE, "workingsetid"), 
 			QConstraint.CT_EQUALS, workingSet	
 		);
 		
 		return getRowsAsRepresentation(query);
 	}
 	
 	/**
 	 * <root>
 	 * 	<user>
 	 * 		<name>...</email>
 	 * 		<email>...</email>
 	 *  </user>
 	 * 	<status>...</status>
 	 * 	<comment>...</comment>
 	 *  <scope>...</scope>
 	 *  <notify>
 	 *  	<name>...</name>
 	 *  	<email>...</email>
 	 *  </notify>
 	 * </root>
 	 */
 	public void acceptRepresentation(Representation entity) throws ResourceException {
 		final Document document;
 		try {
 			document = new DomRepresentation(entity).getDocument();
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new ResourceException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
 		}
 		
 		WorkflowUserInfo user = null;
 		String status = null, commentText = null, scope = null;
 		final List<WorkflowUserInfo> notify = new ArrayList<WorkflowUserInfo>();
 		
 		final NodeCollection nodes = new NodeCollection(document.getDocumentElement().getChildNodes());
 		for (Node node : nodes) {
 			if ("user".equals(node.getNodeName()))
 				user = parseUserInfo(node);
 			else if ("status".equals(node.getNodeName()))
 				status = node.getTextContent();
 			else if ("comment".equals(node.getNodeName()))
 				commentText = node.getTextContent();
 			else if ("scope".equals(node.getNodeName()))
 				scope = node.getTextContent();
 			else if ("notify".equals(node.getNodeName())) {
 				WorkflowUserInfo info = parseUserInfo(node);
 				if (info != null)
 					notify.add(info);
 			}
 		}
 		
 		final WorkflowStatus proposed = WorkflowStatus.getStatus(status);
 		final WorkflowComment comment = new WorkflowComment(user, commentText, scope);
 		
 		if (proposed == null) {
 			try {
 				manager.addComment(workingSet, comment);
 			} catch (WorkflowManagerException e) {
 				die(e);
 				return;
 			}		
 		}
 		else {
 			/*
 			 * 1. Run integrity check
 			 */
 //			final VFS vfs = ServerApplication.getStaticVFS();
 			final WorkingSet  data = SIS.get().getWorkingSetIO().readWorkingSet(Integer.valueOf(workingSet));
 			
 			final Collection<Assessment> assessments = 
 				WorkflowManager.getAllAssessments(data);
 			
 			System.out.println("There are " + assessments.size() + " assessments in this working set...");
 			
 			if (assessments.isEmpty()) {
 				die(new ResourceException(Status.CLIENT_ERROR_EXPECTATION_FAILED, "There are no assessments available in this working set."));
 				return;
 			}
 			
 			boolean success = true;
 			for (Assessment assessment : assessments) {
 				try {
 					success &= IntegrityValidator.
 						validate_background(ServerApplication.
 							getApplication(getContext()).getVFS(), 
							ec, assessment.getId()
 						);
 				} catch (DBException e) {
 					e.printStackTrace();
 					continue;
 				}
 			}
 			
 			success |= "integrity".equals(comment.getComment());
 			
 			if (!success) {
 				die(new ResourceException(Status.CLIENT_ERROR_EXPECTATION_FAILED, "Integrity validation failed."));
 				return;
 			}
 			
 			try {
 				manager.changeStatus(Integer.parseInt(workingSet), user, proposed, comment, notify);
 			} catch (WorkflowManagerException e) {
 				die(e);
 				return;
 			} catch (Exception e) {
 				die(new WorkflowManagerException("Unexpected exception, please try again later.", Status.SERVER_ERROR_INTERNAL, e));
 				return;
 			}
 		}
 		
 		getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
 	}
 	
 	private WorkflowUserInfo parseUserInfo(Node parent) {
 		String id = null, name = null, email = null;
 		final NodeCollection children = new NodeCollection(parent.getChildNodes());
 		for (Node child : children) {
 			if ("id".equals(child.getNodeName()))
 				id = child.getTextContent();
 			if ("name".equals(child.getNodeName()))
 				name = child.getTextContent();
 			else if ("email".equals(child.getNodeName()))
 				email = child.getTextContent();
 		}
 		if (id != null && name != null && email != null)
 			return new WorkflowUserInfo(id, name, email);
 		else
 			return null;
 	}
 	
 	private void die(ResourceException e) {
 		getResponse().setStatus(e.getStatus());
 		getResponse().setEntity(new DomRepresentation(MediaType.TEXT_XML, 
 			BaseDocumentUtils.impl.createErrorDocument(e.getMessage())));
 	}
 
 }
