 package org.iucn.sis.server.restlets.publication;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.hibernate.Session;
 import org.iucn.sis.server.api.application.SIS;
 import org.iucn.sis.server.api.filters.AssessmentFilterHelper;
 import org.iucn.sis.server.api.io.AssessmentIO;
 import org.iucn.sis.server.api.io.PublicationIO;
 import org.iucn.sis.server.api.io.WorkingSetIO;
 import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
 import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
 import org.iucn.sis.server.extensions.integrity.IntegrityValidator;
 import org.iucn.sis.shared.api.debug.Debug;
 import org.iucn.sis.shared.api.models.Assessment;
 import org.iucn.sis.shared.api.models.AssessmentIntegrityValidation;
 import org.iucn.sis.shared.api.models.PublicationData;
 import org.iucn.sis.shared.api.models.PublicationTarget;
 import org.iucn.sis.shared.api.models.Taxon;
 import org.iucn.sis.shared.api.models.User;
 import org.iucn.sis.shared.api.models.WorkingSet;
 import org.restlet.Context;
 import org.restlet.data.MediaType;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.data.Status;
 import org.restlet.ext.xml.DomRepresentation;
 import org.restlet.representation.Representation;
 import org.restlet.representation.StringRepresentation;
 import org.restlet.resource.ResourceException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 
 import com.solertium.util.BaseDocumentUtils;
 import com.solertium.util.NodeCollection;
 
 public class PublicationRestlet extends BaseServiceRestlet {
 
 	public PublicationRestlet(Context context) {
 		super(context);
 	}
 
 	@Override
 	public void definePaths() {
 		paths.add("/publication/data");
 		paths.add("/publication/submit/{type}/{id}");
 	}
 	
 	@Override
 	public void handlePut(Representation entity, Request request, Response response, Session session) throws ResourceException {
 		final PublicationIO io = new PublicationIO(session);
 		
 		final String type = (String)request.getAttributes().get("type");
 		final Integer id = toInt((String)request.getAttributes().get("id"));
 		final User user = getUser(request, session);
 		final boolean doValidation = "true".equals(SIS.get().getSettings(null).getProperty("org.iucn.sis.server.publication.validate", "false"));
 		final boolean allowWarning = "true".equals(request.getResourceRef().getQueryAsForm().getFirstValue("allow_warnings", "false"));
 		
 		if ("assessment".equals(type)) {
 			Assessment assessment = new AssessmentIO(session).getAssessment(id);
 			if (assessment == null)
 				throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
 			
 			if (doValidation){
 				int status;
 				try {
 					status = IntegrityValidator.validate_background(session, SIS.get().getVFS(), 
						SIS.get().getExecutionContext(), assessment.getId());
 				} catch (Exception e) {
 					Debug.println("Failed attempting to run validation: {0}\n{1]", e.getMessage(), e);
 					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e.getMessage(), e);
 				}
 				
 				if (status == AssessmentIntegrityValidation.FAILURE) {
 					response.setEntity(getSubmissionErrorEntity("FAILURE: Validation failed on this assessment."));
 					throw new ResourceException(Status.CLIENT_ERROR_CONFLICT, "Validation failed.");
 				}
 				else if (status == AssessmentIntegrityValidation.WARNING && !allowWarning) {
 					response.setEntity(getSubmissionErrorEntity("WARNING: Validation failed with warnings on this assessment."));
 					throw new ResourceException(Status.CLIENT_ERROR_CONFLICT, "Validation failed with warnings.");
 				}
 			}
 			
 			io.submit(assessment, null, user);
 			
 			response.setEntity(assessment.getPublicationData().toXML(), MediaType.TEXT_XML);
 		}
 		else if ("workingSet".equals(type)) {
 			WorkingSet ws = new WorkingSetIO(session).readWorkingSet(id);
 			if (ws == null)
 				throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
 			
 			String group = ws.getName();
 			
 			AssessmentFilterHelper helper = new AssessmentFilterHelper(session, ws.getFilter());
 			for (Taxon taxon : ws.getTaxon()) {
 				for (Assessment assessment : helper.getAssessments(taxon.getId())) {
 					if (doValidation) {
 						int status;
 						try {
 							status = IntegrityValidator.validate_background(session, SIS.get().getVFS(), 
									SIS.get().getExecutionContext(), assessment.getId());
 						} catch (Exception e) {
 							Debug.println("Failed attempting to run validation: {0}\n{1]", e.getMessage(), e);
 							throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e.getMessage(), e);
 						}
 					
 						if (status == AssessmentIntegrityValidation.FAILURE) {
 							String message = "FAILURE: Validation failed on " + assessment.getAssessmentType().getDisplayName() + " assessment for " + assessment.getSpeciesName() + ".";
 							response.setEntity(getSubmissionErrorEntity(message));
 							throw new ResourceException(Status.CLIENT_ERROR_CONFLICT, message);
 						}
 						else if (status == AssessmentIntegrityValidation.WARNING) {
 							String message = "WARNING: Validation failed on " + assessment.getAssessmentType().getDisplayName() + " assessment for " + assessment.getSpeciesName() + ".";
 							response.setEntity(getSubmissionErrorEntity(message));
 							throw new ResourceException(Status.CLIENT_ERROR_CONFLICT, message);
 						}
 					}
 					io.submit(assessment, group, user);
 				}
 			}
 		}
 		else
 			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid type specified.");
 			
 		response.setStatus(Status.SUCCESS_OK);
 	}
 	
 	private Representation getSubmissionErrorEntity(String message) {
 		return new DomRepresentation(MediaType.TEXT_XML, BaseDocumentUtils.impl.createErrorDocument(message));
 	}
 	
 	@Override
 	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
 		PublicationIO io = new PublicationIO(session);
 		
 		final StringBuilder out = new StringBuilder();
 		out.append("<root>");
 			
 		try {
 			for (PublicationData data : io.listPublicationData())
 				out.append(data.toXML());
 		} catch (PersistentException e) {
 			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
 		}
 			
 		out.append("</root>");
 			
 		return new StringRepresentation(out.toString(), MediaType.TEXT_XML);
 	}
 	
 	@Override
 	public void handlePost(Representation entity, Request request, Response response, Session session) throws ResourceException {
 		final Document document = getEntityAsDocument(entity);
 		
 		final NodeCollection nodes = new NodeCollection(document.getDocumentElement().getChildNodes());
 		
 		String status = null, notes = null;
 		Integer goal = null, approved = null, priority = null;
 		List<Integer> ids = new ArrayList<Integer>();
 		for (Node node : nodes) {
 			if ("status".equals(node.getNodeName()))
 				status = node.getTextContent();
 			else if ("goal".equals(node.getNodeName()))
 				goal = toInt(node.getTextContent());
 			else if ("approved".equals(node.getNodeName()))
 				approved = toInt(node.getTextContent());
 			else if ("priority".equals(node.getNodeName()))
 				priority = toInt(node.getTextContent());
 			else if ("notes".equals(node.getNodeName()))
 				notes = node.getTextContent();
 			else if ("data".equals(node.getNodeName()))
 				ids.add(toInt(node.getTextContent()));	
 		}
 		
 		if (ids.isEmpty())
 			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please supply assessment publication data to change.");
 		
 		PublicationData source = new PublicationData();
 		if (status != null) {
 			Assessment fauxAssessment = new Assessment();
 			fauxAssessment.setType(status);
 			source.setAssessment(fauxAssessment);
 		}
 		if (goal != null) {
 			PublicationTarget target = new PublicationTarget();
 			target.setId(goal);
 			source.setTargetGoal(target);
 		}
 		if (approved != null) {
 			PublicationTarget target = new PublicationTarget();
 			target.setId(approved);
 			source.setTargetApproved(target);
 		}
 		if (priority != null) 
 			source.setPriority(priority);
 		if (notes != null)
 			source.setNotes(notes);
 		
 		final PublicationIO io = new PublicationIO(session);
 		
 		try {
 			io.update(source, ids);
 		} catch (PersistentException e) {
 			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
 		}
 	}
 	
 	private Integer toInt(String value) throws ResourceException {
 		try {
 			return Integer.valueOf(value);
 		} catch (NumberFormatException e) {
 			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
 		} catch (NullPointerException e) {
 			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
 		}
 	}
 
 }
