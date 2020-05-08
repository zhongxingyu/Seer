 package org.iucn.sis.server.restlets.assessments;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.hibernate.Hibernate;
 import org.hibernate.Session;
 import org.iucn.sis.server.api.filters.AssessmentFilterHelper;
 import org.iucn.sis.server.api.io.AssessmentIO;
 import org.iucn.sis.server.api.io.TaxonIO;
 import org.iucn.sis.server.api.io.AssessmentIO.AssessmentIOWriteResult;
 import org.iucn.sis.server.api.persistance.FieldDAO;
 import org.iucn.sis.server.api.persistance.PrimitiveFieldDAO;
 import org.iucn.sis.server.api.persistance.SISPersistentManager;
 import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
 import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
 import org.iucn.sis.server.api.utils.RegionConflictException;
 import org.iucn.sis.server.utils.AssessmentPersistence;
 import org.iucn.sis.shared.api.assessments.AssessmentDeepCopyFilter;
 import org.iucn.sis.shared.api.assessments.AssessmentFetchRequest;
 import org.iucn.sis.shared.api.assessments.PublishedAssessmentsComparator;
 import org.iucn.sis.shared.api.debug.Debug;
 import org.iucn.sis.shared.api.models.Assessment;
 import org.iucn.sis.shared.api.models.AssessmentFilter;
 import org.iucn.sis.shared.api.models.AssessmentType;
 import org.iucn.sis.shared.api.models.Edit;
 import org.iucn.sis.shared.api.models.Field;
 import org.iucn.sis.shared.api.models.PrimitiveField;
 import org.iucn.sis.shared.api.models.Reference;
 import org.iucn.sis.shared.api.models.Region;
 import org.iucn.sis.shared.api.models.Taxon;
 import org.iucn.sis.shared.api.models.User;
 import org.iucn.sis.shared.api.utils.CanonicalNames;
 import org.restlet.Context;
 import org.restlet.data.MediaType;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.data.Status;
 import org.restlet.representation.Representation;
 import org.restlet.representation.StringRepresentation;
 import org.restlet.resource.ResourceException;
 
 import com.solertium.lwxml.shared.NativeDocument;
 import com.solertium.lwxml.shared.NativeNodeList;
 import com.solertium.util.events.ComplexListener;
 
 public class AssessmentRestlet extends BaseServiceRestlet {
 
 	public AssessmentRestlet(Context context) {
 		super(context);
 	}
 
 	@Override
 	public void definePaths() {
 		paths.add("/assessments");
 		paths.add("/assessments/{type}");
 		paths.add("/assessments/{type}/{id}");
 	}
 	
 	@Override
 	public void handlePost(Representation entity, Request request, Response response, Session session) throws ResourceException {
 		String action = request.getResourceRef().getQueryAsForm().getFirstValue("action");
 		if (action == null) {
 			postAssessment(entity, request, response, getUser(request, session), session);
 		} else if (action.equalsIgnoreCase("fetch")) {
 			String mode = request.getResourceRef().getQueryAsForm().getFirstValue("mode", "FULL");
 			getAssessments(entity, request, response, getIdentifier(request), (String)request.getAttributes().get("type"), mode, session);
 		} else if (action.equalsIgnoreCase("batch")) {
 			batchCreate(entity, request, response, getUser(request, session), session);
 		}
 	}
 
 	@Override
 	public void handleDelete(Request request, Response response, Session session) throws ResourceException {
 		final Integer assessmentID = getAssessmentID(request);
 		final AssessmentIO assessmentIO = new AssessmentIO(session);
 		Assessment assessment = assessmentIO.getNonCachedAssessment(assessmentID);
 		if (assessment != null) {
 			AssessmentIOWriteResult deleted = assessmentIO.trashAssessment(assessment, getUser(request, session));
 			if (deleted.status.isSuccess()) {
 				response.setStatus(Status.SUCCESS_OK);
 			} else
 				throw new ResourceException(Status.CLIENT_ERROR_LOCKED);
 		} else {
 			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
 			response.setEntity("Assessment with id " + assessmentID + " was not found.", MediaType.TEXT_PLAIN);
 		}
 	}
 	
 	private Integer getAssessmentID(Request request) throws ResourceException {
 		try {
 			return Integer.valueOf((String) request.getAttributes().get("id"));
 		} catch (NumberFormatException e) {
 			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please supply a valid ID");
 		}
 	}
 
 	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
 		Integer id = getAssessmentID(request);
 		final AssessmentIO assessmentIO = new AssessmentIO(session);
 		Assessment assessment = assessmentIO.getAssessment(id);
 		if (assessment != null) {
 			response.setStatus(Status.SUCCESS_OK);
 			return new StringRepresentation(assessment.toXML(), MediaType.TEXT_XML);
 		} else {
 			response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
 			return new StringRepresentation("No Assessment found with id " + id, MediaType.TEXT_PLAIN);
 		}
 	}
 
 	/**
 	 * Format of a proper get request: &lt;uid&gt;(assessmentUID)&lt;/uid&gt;
 	 * 
 	 * or
 	 * 
 	 * &lt;taxon&gt;(taxonID)&lt;/taxon&gt;
 	 * 
 	 * If the type parameter is null for the latter format, all .
 	 * @param type
 	 * @param session TODO
 	 * @param getEntity
 	 * 
 	 * @return
 	 */
 	
 	private void getAssessments(Representation entity, Request request, Response response, String user, String type, String mode, Session session) throws ResourceException {
 		NativeDocument ndoc = getEntityAsNativeDocument(entity);
 		AssessmentFetchRequest afq = AssessmentFetchRequest.fromXML(ndoc.getDocumentElement());
 		
 		Set<Assessment> fetched = new HashSet<Assessment>();
 		
 		final AssessmentIO assessmentIO = new AssessmentIO(session);
 		
 		for (Integer id : afq.getAssessmentUIDs()) {
 			Assessment assessment = assessmentIO.getAssessment(id);
			if (assessment != null) {
 				fetched.add(assessment);
				/*fetched.addAll(assessmentIO.readAssessmentsForTaxon(
						assessment.getSpeciesID()));*/
			}
 		}
 
 		for (Integer taxonID : afq.getTaxonIDs()) {
 			if (type == null) {
 				fetched.addAll(assessmentIO.readAssessmentsForTaxon(taxonID));
 			} else if (type.equalsIgnoreCase(AssessmentType.DRAFT_ASSESSMENT_TYPE)) {
 				fetched.addAll(assessmentIO.readDraftAssessmentsForTaxon(taxonID));
 			} else if (type.equalsIgnoreCase(AssessmentType.PUBLISHED_ASSESSMENT_TYPE)) {
 				fetched.addAll(assessmentIO.readPublishedAssessmentsForTaxon(taxonID));
 			}
 		}
 		
 		StringBuilder ret = new StringBuilder("<assessments>");
 		for (Assessment asm : fetched) {
 			filterFields(asm, mode);
 			ret.append(asm.toXML());
			//ret.append(assessmentIO.getAssessmentXML(asm.getId()));
 		}
 		ret.append("</assessments>");
 
 		response.setStatus(Status.SUCCESS_OK);
 		response.setEntity(ret.toString(), MediaType.TEXT_XML);
 	}
 	
 	private void filterFields(Assessment assessment, String mode) {
 		Edit lastEdit = assessment.getLastEdit();
 		if (lastEdit != null) {
 			HashSet<Edit> editSet = new HashSet<Edit>();
 			editSet.add(lastEdit);
 			assessment.setEdit(editSet);
 		}
 		
 		if ("PARTIAL".equalsIgnoreCase(mode)) {
 			List<String> allowed = new ArrayList<String>();
 			allowed.add(CanonicalNames.RedListAssessmentDate);
 			allowed.add(CanonicalNames.RedListCriteria);
 			allowed.add(CanonicalNames.RegionInformation);
 			
 			Set<Field> fields = new HashSet<Field>();
 			for (String name : new String[] {CanonicalNames.RedListAssessmentDate, CanonicalNames.RedListCriteria, CanonicalNames.RegionInformation}) {
 				Field field = assessment.getField(name);
 				if (field != null)
 					fields.add(field);
 			}
 			
 			assessment.setField(fields);
 		}
 	}
 
	private void postAssessment(Representation entity, Request request, Response response, User username, final Session session) throws ResourceException {
 		session.clear();
 		
 		NativeDocument doc = getEntityAsNativeDocument(entity);
 		final AssessmentIO assessmentIO = new AssessmentIO(session);
 		try {
 			Assessment source = Assessment.fromXML(doc);
 			
 			// ONLY ALLOW POSTING OF ASSESSMENTS THAT ALREADY EXIST;
 			if (source.getId() != 0) {
 				final Assessment target = assessmentIO.getAssessment(source.getId());
 				
 				final Map<Integer, Reference> targetRefs = new HashMap<Integer, Reference>();
 				for (Reference reference : target.getReference())
 					targetRefs.put(reference.getId(), reference);
 				
 				for (Reference sourceRef : source.getReference()) {
 					if (sourceRef.getId() == 0)
 						continue;
 					
 					Reference targetRef = targetRefs.remove(sourceRef.getId());
 					if (targetRef == null) {
 						Reference ref = SISPersistentManager.instance().getObject(session, Reference.class, sourceRef.getId());
 						if (ref != null)
 							target.getReference().add(ref);
 					}
 				}
 				
 				target.getReference().removeAll(targetRefs.values());
 				
 				target.toXML();
 				
 				final AssessmentPersistence saver = new AssessmentPersistence(session);
 				saver.setDeleteFieldListener(new ComplexListener<Field>() {
 					public void handleEvent(Field field) {
 						try {
 							FieldDAO.deleteAndDissociate(field, session);
 						} catch (PersistentException e) {
 							Debug.println(e);
 						}
 					}
 				});
 				saver.setDeletePrimitiveFieldListener(new ComplexListener<PrimitiveField>() {
 					public void handleEvent(PrimitiveField field) {
 						try {
 							PrimitiveFieldDAO.deleteAndDissociate(field, session);
 						} catch (PersistentException e) {
 							Debug.println(e);
 						}
 					}
 				});
 				saver.sink(source, target);
 				
 				Hibernate.initialize(target.getEdit());
 				
 				//This may or may not need to happen for hibernate reasons...
 				target.toXML();
 				
 				/*
 				 * If this happens, then some field that should not have been 
 				 * removed got removed, and I'd rather fail here than continue 
 				 * processing; lest we risk losing data, notes, or references.
 				 * 
 				 * TODO: add this back; removing the constraint for now until 
 				 * this is further tested with the client 
 				 */
 				/*if (source.getField().size() != target.getField().size())
 					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Server error: fields not persisted correctly.");*/
 				
 				AssessmentIOWriteResult result = 
 					assessmentIO.writeAssessment(target, getUser(request, session), true);
 				
 				if (!result.status.isSuccess())
 					throw new ResourceException(result.status, "AssessmentIOWrite threw exception when saving.");
 				
 				session.flush();
 				
 				response.setStatus(result.status);
 				response.setEntity(target.toXML(), MediaType.TEXT_XML);
 				
 				if (result.edit == null)
 					Debug.println("Error: No edit associated with this change. Not backing up changes.");
 				else
 					saver.saveChanges(target, result.edit);
				/*AssessmentIOWriteResult result = saveAssessment(assessment, username);
				if (result.status.isSuccess()) {
					response.setEntity(assessment.toXML(), MediaType.TEXT_XML);
					response.setStatus(result.status);
				} else {
					throw new ResourceException(Status.CLIENT_ERROR_EXPECTATION_FAILED);
				}*/
 			} else {
 				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
 			}
 
		} catch (Exception e) {
 			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
 	}
 
 	private void batchCreate(Representation entity, Request request, Response response, User user, Session session) throws ResourceException {
 		NativeDocument doc = getEntityAsNativeDocument(entity);
 		StringBuffer successfulIDs = new StringBuffer();
 		StringBuffer extantIDs = new StringBuffer();
 		StringBuffer unsuccessfulIDs = new StringBuffer();
 		
 		AssessmentFilter filter = AssessmentFilter.fromXML(doc.getDocumentElement().getElementsByTagName(
 				AssessmentFilter.ROOT_TAG).elementAt(0));
 
 		NativeNodeList nodes = doc.getDocumentElement().getElementsByTagName("taxon");
 		boolean useTemplate = Boolean.parseBoolean(doc.getDocumentElement().getElementsByTagName("useTemplate")
 				.elementAt(0).getTextContent());
 		Debug.println("Using template? {0}", useTemplate);
 		
 		TaxonIO taxonIO = new TaxonIO(session);
 		
 		AssessmentIO io = new AssessmentIO(session);
 
 		for (int i = 0; i < nodes.getLength(); i++) {
 			Taxon taxon = taxonIO.getTaxon(Integer.valueOf(nodes.elementAt(i).getTextContent()));
 			Assessment curAssessment = doCreateAssessmentForBatch(user, filter, useTemplate, taxon, session);
 			try {
 				AssessmentIOWriteResult result = io.saveNewAssessment(curAssessment, user);
 				if (result.status.isSuccess())
 					successfulIDs.append(curAssessment.getSpeciesName() + (i == nodes.getLength() - 1 ? "" : ", "));
 				else
 					unsuccessfulIDs.append(curAssessment.getSpeciesName() + (i == nodes.getLength() - 1 ? "" : ", "));
 			} catch (RegionConflictException e) {
 				extantIDs.append(curAssessment.getSpeciesName() + (i == nodes.getLength() - 1 ? "" : ", "));
 			}
 		}
 
 		StringBuilder ret = new StringBuilder();
 		if (unsuccessfulIDs.length() > 0)
 			ret.append("<div>Unable to create an assessment for the following species: " + unsuccessfulIDs
 					+ "</div>\r\n");
 		if (extantIDs.length() > 0)
 			ret.append("<div>The following species already have draft assessments with the specific locality: "
 					+ extantIDs + "</div>\r\n");
 		if (successfulIDs.length() > 0)
 			ret.append("<div>Successfully created an assessment for the following species: " + successfulIDs
 					+ "</div>\r\n");
 		response.setStatus(Status.SUCCESS_OK);
 		response.setEntity(ret.toString(), MediaType.TEXT_HTML);
 		
 	}
 
 	private Assessment doCreateAssessmentForBatch(User user, AssessmentFilter filter, boolean useTemplate, Taxon taxon, Session session) {
 		AssessmentFilter draftFilter = filter.deepCopy();
 		draftFilter.setDraft(false);
 		draftFilter.setRecentPublished(true);
 		draftFilter.setAllPublished(false);
 
 		AssessmentFilterHelper helper = new AssessmentFilterHelper(session, draftFilter);
 
 		Assessment assessment = null;
 
 		if (useTemplate) {
 			List<Assessment> assessments = helper.getAssessments(taxon.getId());
 			if (assessments.isEmpty()) {
 				draftFilter.getRegions().clear();
 				draftFilter.getRegions().add(Region.getGlobalRegion());
 				assessments = helper.getAssessments(taxon.getId());
 			}
 			if (assessments.isEmpty()) {
 				Debug.println("No template exists for species {0}", taxon.getFullName());
 				assessment = new Assessment(); // No template exists...
 			} else {
 				Collections.sort(assessments, new PublishedAssessmentsComparator(false));
 				assessment = assessments.get(0).deepCopy(new AssessmentDeepCopyFilter());
 			}
 		} else
 			assessment = new Assessment();
 
 		assessment.setRegions(filter.getRegions(), filter.getRegions().contains("-1"));
 		assessment.setType(AssessmentType.DRAFT_ASSESSMENT_TYPE);
 		assessment.setTaxon(taxon);
 		
 		return assessment;
 	}
 	
 	@Override
 	public void handlePut(Representation entity, Request request, Response response, Session session) throws ResourceException {
 		NativeDocument doc = getEntityAsNativeDocument(entity);
 		try {
 			Assessment assessment = Assessment.fromXML(doc);
 			
 			final Map<String, List<Integer>> references = new HashMap<String, List<Integer>>();
 			for (Field field : assessment.getField()) {
 				if (field.getReference() != null && !field.getReference().isEmpty()) {
 					List<Integer> refs = new ArrayList<Integer>();
 					for (Reference reference : field.getReference())
 						refs.add(reference.getId());
 					references.put(field.getName(), refs);
 					field.setReference(new HashSet<Reference>());
 				}
 			}
 			
 			AssessmentIO io = new AssessmentIO(session);
 			AssessmentIOWriteResult result = io.saveNewAssessment(assessment, getUser(request, session));
 			if (result.status.isSuccess()) {
 				for (Map.Entry<String, List<Integer>> entry : references.entrySet()) {
 					Field field = assessment.getField(entry.getKey());
 					if (field != null && field.getId() > 0) {
 						HashSet<Reference> refs = new HashSet<Reference>();
 						for (Integer id : entry.getValue()) 
 							refs.add((Reference)session.get(Reference.class, id));
 						field.setReference(refs);
 						
 						session.update(field);
 					}
 				}
 				
 				response.setEntity(assessment.getId()+"", MediaType.TEXT_PLAIN);
 				response.setStatus(Status.SUCCESS_OK);
 			} else {
 				throw new ResourceException(Status.CLIENT_ERROR_EXPECTATION_FAILED);
 			}
 		} catch (RegionConflictException e) {
 			throw new ResourceException(Status.CLIENT_ERROR_CONFLICT, e);
 		} catch (ResourceException e) {
 			throw e;
 		} catch (Exception e) {
 			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
 		}
 	}
 	
 }
