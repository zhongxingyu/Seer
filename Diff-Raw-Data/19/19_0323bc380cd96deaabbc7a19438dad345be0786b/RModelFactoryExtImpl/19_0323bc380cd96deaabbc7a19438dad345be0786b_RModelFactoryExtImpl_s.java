 /*******************************************************************************
  * Copyright (c) 2010 Ericsson
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * Contributors:
  *   Alvaro Sanchez-Leon - Initial Implementation
  *******************************************************************************/
 package org.eclipse.mylyn.reviews.r4e.core.model.serial.impl;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.EMap;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.mylyn.reviews.r4e.core.Activator;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EAnomaly;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EAnomalyTextPosition;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EAnomalyType;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EComment;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EContent;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EDelta;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EFileContext;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EFileVersion;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EFormalReview;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EID;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EIDComponent;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EItem;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EParticipant;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReview;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewGroup;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewPhase;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewPhaseInfo;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewState;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewType;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4ETextContent;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4ETextPosition;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EUser;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EUserReviews;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EUserRole;
 import org.eclipse.mylyn.reviews.r4e.core.model.RModelFactory;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.Persistence;
 import org.eclipse.mylyn.reviews.r4e.core.rfs.ReviewsRFSProxy;
 import org.eclipse.mylyn.reviews.r4e.core.rfs.spi.ReviewsFileStorageException;
 import org.eclipse.mylyn.reviews.r4e.core.utils.filePermission.UserPermission;
 
 /**
  * @author lmcalvs
  *
  */
 public class RModelFactoryExtImpl extends Common implements Persistence.RModelFactoryExt {
 
 	// ------------------------------------------------------------------------
 	// Fields
 	// ------------------------------------------------------------------------
 
 	R4EWriter	fWriter	= SerializeFactory.getWriter();
 	R4EReader	fReader	= SerializeFactory.getReader();
 
 	// ------------------------------------------------------------------------
 	// GROUP Resource Methods
 	// ------------------------------------------------------------------------
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.mylyn.reviews.r4e.core.model.serial.Persistence.GroupResFactory#createR4EReviewGroup(org.eclipse.
 	 * emf.common.util.URI, java.lang.String)
 	 */
 	public R4EReviewGroup createR4EReviewGroup(URI aFolderPath, String aGroupName) throws ResourceHandlingException {
 
 		URI fileGroupURI = fWriter.createResourceURI(aGroupName, aFolderPath, ResourceType.GROUP);
 		// create a new ResourceSet and resource for the given group
 		Resource resource = fWriter.createResourceSetWithResource(fileGroupURI);
 		R4EReviewGroup group = RModelFactory.eINSTANCE.createR4EReviewGroup();
 		resource.getContents().add(group);
 		// Update the resource
 		group.setName(aGroupName);
 		group.setFolder(group.eResource().getURI().trimSegments(1).devicePath().toString());
 		fWriter.saveResource(resource);
 
 		// Make sure a local review repository exist in this location
 		File groupFolder = new File(aFolderPath.devicePath());
 		try {
 			checkOrCreateRepo(groupFolder);
 		} catch (ReviewsFileStorageException e) {
 			throw new ResourceHandlingException(e);
 		}
 
 		return group;
 	}
 
 	/**
 	 * @param aFolderPath
 	 * @throws ReviewsFileStorageException
 	 */
 	private void checkOrCreateRepo(File aDir) throws ReviewsFileStorageException {
 		boolean valid = ReviewsRFSProxy.isValidRepo(aDir);
 		if (!valid) {
 			// No valid review repository exist, time to create it
 			ReviewsRFSProxy revRepo = new ReviewsRFSProxy(aDir, true);
 			revRepo.close();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.mylyn.reviews.r4e.core.model.serial.Persistence.GroupResFactory#openR4EReviewGroup(org.eclipse.emf
 	 * .common.util.URI)
 	 */
 	public R4EReviewGroup openR4EReviewGroup(URI aResourcePath) throws ResourceHandlingException {
 		R4EReviewGroup group = fReader.deserializeTopElement(aResourcePath, R4EReviewGroup.class);
 
 		// Load resources from all participants
 		URI folder = getFolderPath(aResourcePath);
 
 		List<URI> usrGroupFiles = fReader.selectUsrReviewGroupRes(folder);
 		for (URI uri : usrGroupFiles) {
 			loadUsrReviews(group, uri);
 		}
 
 		// Build the mapping references to anomaly types
 		EList<R4EAnomalyType> anomTypes = group.getAvailableAnomalyTypes();
 		for (Iterator<R4EAnomalyType> iterator = anomTypes.iterator(); iterator.hasNext();) {
 			R4EAnomalyType r4eAnomalyType = iterator.next();
 			group.getAnomalyTypeKeyToReference().put(r4eAnomalyType.getType(), r4eAnomalyType);
 		}
 
 		// update the transient value of folder
 		group.setFolder(group.eResource().getURI().trimSegments(1).devicePath().toString());
 		// Make sure a local review repository exist in this location
 		File groupFolder = new File(folder.devicePath());
 		try {
 			checkOrCreateRepo(groupFolder);
 		} catch (ReviewsFileStorageException e) {
 			throw new ResourceHandlingException(e);
 		}
 
 		return group;
 	}
 
 	/**
 	 * @param group
 	 * @param uri
 	 * @throws ResourceHandlingException
 	 */
 	private void loadUsrReviews(R4EReviewGroup group, URI uri) throws ResourceHandlingException {
 		ResourceSet resSet = group.eResource().getResourceSet();
 		R4EUserReviews usrReviews = fReader.deserializeTopElement(uri, resSet, R4EUserReviews.class);
 		if (usrReviews == null) {
 			return;
 		}
 
 		// Associate the usrRewviews to the group ResrourceSet
 		associateToResourceSet(group, usrReviews);
 
 		// keep reference to all userReviews within group
 		group.getUserReviews().put(usrReviews.getName(), usrReviews);
 		EList<String> reviewNames = usrReviews.getCreatedReviews();
 		// Add enabled reviews to overall review map
 		for (String revName : reviewNames) {
 			R4EReview review = usrReviews.getInvitedToMap().get(revName);
 			if (review != null) {
 				group.getReviewsMap().put(revName, review);
 				group.getReviews().add(review);
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.mylyn.reviews.r4e.core.model.serial.Persistence.GroupResFactory#closeR4EReviewGroup(org.eclipse.mylyn
 	 * .reviews.r4e.core.model.R4EReviewGroup)
 	 */
 	public String closeR4EReviewGroup(R4EReviewGroup aReviewGroup) {
 		StringBuilder sb = new StringBuilder();
 
 		// Obtain all resources
 		Resource resource = aReviewGroup.eResource();
 		if (resource == null) {
 			sb.append("Attempting to close a review group with no associated resource");
 			Activator.fTracer.traceDebug(sb.toString());
 			return sb.toString();
 		}
 
 		ResourceSet resSet = resource.getResourceSet();
 		if (resSet == null) {
 			sb.append("Attempting to close a review group with no associated resource set");
 			Activator.fTracer.traceDebug(sb.toString());
 			return sb.toString();
 		}
 
 		EList<Resource> resList = resSet.getResources();
 
 		// unload then all
 		for (Resource res : resList) {
 			res.unload();
 		}
 		return null;
 	}
 
 	// ------------------------------------------------------------------------
 	// REVIEW Resource Methods
 	// ------------------------------------------------------------------------
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.mylyn.reviews.r4e.core.model.serial.Persistence.ReviewResFactory#createR4EReview(org.eclipse.mylyn
 	 * .reviews.r4e.core.model.R4EReviewGroup, java.lang.String, java.lang.String)
 	 */
 	public R4EReview createR4EReview(R4EReviewGroup aReviewGroup, String aReviewName, String aCreatedByUser)
 			throws ResourceHandlingException {
 		// validate
 		if (aReviewGroup == null || aReviewName == null || aCreatedByUser == null) {
 			return null;
 		}
 
 		R4EReview review = RModelFactory.eINSTANCE.createR4EReview();
 
 		reviewInit(aReviewGroup, aReviewName, aCreatedByUser, review);
 		return review;
 	}
 
 	/**
 	 * @param aReviewGroup
 	 * @param aReviewName
 	 * @param aCreatedByUser
 	 * @param review
 	 * @throws ResourceHandlingException
 	 */
 	private void reviewInit(R4EReviewGroup aReviewGroup, String aReviewName, String aCreatedByUser, R4EReview review)
 			throws ResourceHandlingException {
 		// Initialize block
 		Resource groupResource = createReviewInputCheck(aReviewGroup, aReviewName);
 		ResourceSet resSet = groupResource.getResourceSet();
 		URI groupFilePath = groupResource.getURI();
 		groupFilePath = fWriter.getFolderPath(groupFilePath); /* To directory */
 
 		// CREATE REVIEW - and associate it to a resource
 		review.setName(aReviewName);
 		URI reviewURI = fWriter.createResourceURI(aReviewName, groupFilePath, ResourceType.REVIEW);
 		Resource reviewResource = resSet.createResource(reviewURI);
 		reviewResource.getContents().add(review);
 
 		// UPDATE TRANSIENT REFERENCES WITH GROUP, USER GROUP, REVIEW AND PARTICIPANT
 		aReviewGroup.getReviewsMap().put(aReviewName, review);
 		aReviewGroup.getReviews().add(review);
 
 		// CREATE PARTICIPANT resource and save it under the review folder
 		// create the participant role
 		List<R4EUserRole> role = new ArrayList<R4EUserRole>();
 		role.add(R4EUserRole.R4E_ROLE_ORGANIZER);
 		R4EParticipant participant = (R4EParticipant) createR4EUser(review, aCreatedByUser, role, true);
 
 		// Update pending associations to Review
 		Date now = new Date(new Date().getTime());
 		R4EReviewState state = RModelFactory.eINSTANCE.createR4EReviewState();
 		state.setState(R4EReviewPhase.R4E_REVIEW_PHASE_STARTED);
 		review.setCreatedBy(participant);
 		review.setStartDate(now);
 		review.getUsersMap().put(participant.getId(), participant);
 		review.setState(state);
 		review.setType(R4EReviewType.R4E_REVIEW_TYPE_BASIC);
 
 		// SAVE REVIEW
 		fWriter.saveResource(reviewResource);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.mylyn.reviews.r4e.core.model.serial.Persistence.ReviewResFactory#createR4EFormalReview(org.eclipse
 	 * .mylyn.reviews.r4e.core.model.R4EReviewGroup, java.lang.String, java.lang.String)
 	 */
 	public R4EFormalReview createR4EFormalReview(R4EReviewGroup aReviewGroup, String aReviewName, String aCreatedByUser)
 			throws ResourceHandlingException {
 		// validate
 		if (aReviewGroup == null || aReviewName == null || aCreatedByUser == null) {
 			return null;
 		}
 
 		R4EFormalReview review = RModelFactory.eINSTANCE.createR4EFormalReview();
 		reviewInit(aReviewGroup, aReviewName, aCreatedByUser, review);
 
 		return review;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.mylyn.reviews.r4e.core.model.serial.Persistence.ReviewResFactory#createR4EReviewPhaseInfo(org.eclipse
 	 * .mylyn.reviews.r4e.core.model.R4EFormalReview)
 	 */
 	public R4EReviewPhaseInfo createR4EReviewPhaseInfo(R4EFormalReview aReview) throws ResourceHandlingException {
 		// validate
 		if (aReview == null) {
 			return null;
 		}
 
 		R4EReviewPhaseInfo phase = RModelFactory.eINSTANCE.createR4EReviewPhaseInfo();
 		aReview.getPhases().add(phase);
 
 		// Initial save of the new element
 		aReview.eResource().getContents().add(phase);
 		fWriter.saveResource(phase.eResource());
 
 		return phase;
 	}
 
 	/**
 	 * @param aReviewGroup
 	 * @param aReviewName
 	 * @return
 	 * @throws ResourceHandlingException
 	 */
 	private Resource createReviewInputCheck(R4EReviewGroup aReviewGroup, String aReviewName)
 			throws ResourceHandlingException {
 		StringBuilder sb = new StringBuilder("ResourceSet not found in the review group provided");
 		// group resource
 		Resource groupResource = aReviewGroup.eResource();
 		if (groupResource == null) {
 			throw new ResourceHandlingException(sb.toString());
 		}
 		sb.setLength(0); // clear
 		sb.append("A review with this name already exists in Group: " + aReviewGroup.getName());
 		R4EReview review = aReviewGroup.getReviewsMap().get(aReviewName);
 		if (review != null) {
 			throw new ResourceHandlingException(sb.toString());
 		}
 		return groupResource;
 	}
 
 	/**
 	 * @param review
 	 * @param participant
 	 * @return
 	 */
 	private Resource createUserReviewsResource(R4EReview review, R4EUser participant) {
 		R4EReviewGroup aReviewGroup = (R4EReviewGroup) review.eContainer();
 		String aCreatedByUser = participant.getId();
 		ResourceSet resSet = review.eResource().getResourceSet();
 		URI groupFilePath = fWriter.getFolderPath(aReviewGroup.eResource().getURI());
 
 		// CREATE USER GROUP RESOURCE if not already created
 		R4EUserReviews uReviews = aReviewGroup.getUserReviews().get(participant.getId());
 		Resource ureviewsRes = null;
 		if (uReviews == null) {
 			uReviews = RModelFactory.eINSTANCE.createR4EUserReviews();
 			URI uri = fWriter.createResourceURI(aCreatedByUser, groupFilePath, ResourceType.USER_GROUP);
 			ureviewsRes = resSet.createResource(uri);
 			ureviewsRes.getContents().add(uReviews);
 			uReviews.setName(participant.getId());
 			uReviews.setGroup(aReviewGroup);
 			aReviewGroup.getUserReviews().put(participant.getId(), uReviews);
 		} else {
 			ureviewsRes = uReviews.eResource();
 		}
 
 		uReviews.setGroup(aReviewGroup);
 		EList<String> reviewList = uReviews.getCreatedReviews();
 		if (!(reviewList.contains(review.getName())) && participant.isReviewCreatedByMe()) {
 			reviewList.add(review.getName());
 		}
 
 		uReviews.getInvitedToMap().put(review.getName(), review);
 		return ureviewsRes;
 	}
 
 	public R4EReview openR4EReview(R4EReviewGroup aReviewGroup, String aReviewName) throws ResourceHandlingException {
 		if (aReviewGroup == null) {
 			return null;
 		}
 
 		R4EReview review = aReviewGroup.getReviewsMap().get(aReviewName);
 		if (review == null) {
 			StringBuilder sb = new StringBuilder("Not able to find Review: " + aReviewName + "\tin group: "
 					+ aReviewGroup);
 			throw new ResourceHandlingException(sb.toString());
 		}
 
		// refresh transient references
		// aReviewGroup.getReviewsMap().put(aReviewName, review);
		aReviewGroup.getReviews().add(review);
 
 		URI folder = getFolderPath(review.eResource().getURI());
 		// Load resources from all participants
 		List<URI> usrFiles = fReader.selectUsrCommentsRes(folder);
 		for (URI uri : usrFiles) {
 			loadUsrData(review, uri);
 		}
 
 		return review;
 	}
 
 	private void associateToResourceSet(EObject base, EObject recent) {
 		// Link the participant to the same ResourceSet as the review
 		ResourceSet resSet = base.eResource().getResourceSet();
 		Resource resourcePresent = resSet.getResource(recent.eResource().getURI(), false);
 		if (resourcePresent == null) {
 			// Add the resource if not present
 			resSet.getResources().add(recent.eResource());
 		}
 	}
 
 	/**
 	 * Loads the user itself and the related comments and Items to the review context
 	 * 
 	 * @param review
 	 * @param uri
 	 * @throws ResourceHandlingException
 	 */
 	private void loadUsrData(R4EReview review, URI uri) throws ResourceHandlingException {
 		ResourceSet resSet = review.eResource().getResourceSet();
 		R4EParticipant participant = fReader.deserializeTopElement(uri, resSet, R4EParticipant.class);
 		if (participant == null) {
 			return;
 		}
 
 		associateToResourceSet(review, participant);
 
 		review.getUsersMap().put(participant.getId(), participant);
 		// update refs to comments and particpant
 		EList<R4EComment> comments = participant.getAddedComments();
 		if (comments != null) {
 			for (R4EComment comment : comments) {
 				review.getIdsMap().put(comment.getId(), comment);
 				if (comment instanceof R4EAnomaly) {
 					review.getTopics().add((R4EAnomaly) comment);
 				} else {
 					R4EAnomaly anomalyRef = comment.getAnomaly();
 					// Make sure it is associated to the same resource set
 					associateToResourceSet(review, anomalyRef);
 					anomalyRef.getComments().add(comment);
 				}
 			}
 		}
 
 		// update refs to items
 		EList<R4EItem> items = participant.getAddedItems();
 		if (items != null && items.size() > 0) {
 			R4EItem anItem = items.get(0);
 
 			// associate item to resource set
 			associateToResourceSet(review, anItem);
 
 			// Get: Items
 			for (R4EItem item : items) {
 				EMap<R4EID, R4EIDComponent> idsMap = review.getIdsMap();
 				idsMap.put(item.getId(), item);
 				review.getReviewItems().add(item);
 
 				// Get: file contexts
 				EList<R4EFileContext> fileCtxt = item.getFileContextList();
 				for (Iterator<R4EFileContext> iterator = fileCtxt.iterator(); iterator.hasNext();) {
 					R4EFileContext r4eFileContext = iterator.next();
 					idsMap.put(r4eFileContext.getId(), r4eFileContext);
 					// Get: Deltas
 					EList<R4EDelta> deltas = r4eFileContext.getDeltas();
 					for (Iterator<R4EDelta> iterator2 = deltas.iterator(); iterator2.hasNext();) {
 						R4EDelta delta = iterator2.next();
 						idsMap.put(delta.getId(), delta);
 					}
 				}
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.mylyn.reviews.r4e.core.model.serial.Persistence.ReviewResFactory#closeR4EReview(org.eclipse.mylyn
 	 * .reviews.r4e.core.model.R4EReview)
 	 */
 	public String closeR4EReview(R4EReview aReview) {
 		// Get all participants
 		Resource resource = null;
 		Collection<R4EUser> participants = aReview.getUsersMap().values();
 		if (participants != null) {
 			// Release resources associated to each participant
 			for (Iterator<R4EUser> iterator = participants.iterator(); iterator.hasNext();) {
 				R4EParticipant participant = (R4EParticipant) iterator.next();
 				resource = participant.eResource();
 				// participant + its comments
 				if (resource != null) {
 					resource.unload();
 				}
 				EList<R4EItem> items = participant.getAddedItems();
 				if (items != null && items.size() > 0) {
 					// items per participant
 					for (Iterator<R4EItem> iterator2 = items.iterator(); iterator2.hasNext();) {
 						R4EItem r4eItem = iterator2.next();
 						resource = r4eItem.eResource();
 						if (resource != null) {
 							resource.unload();
 						}
 					}
 				}
 			}
 		}
 
 		// finally dispose the resource at the review level
 		resource = aReview.eResource();
 		if (resource != null) {
 			resource.unload();
 		}
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.mylyn.reviews.r4e.core.model.serial.Persistence.ReviewResFactory#deleteR4EReview(org.eclipse.mylyn
 	 * .reviews.r4e.core.model.R4EReview, boolean)
 	 */
 	public String deleteR4EReview(R4EReview aReview, boolean aDeleteOnDisk) throws ResourceHandlingException {
 		StringBuilder sb = new StringBuilder();
 		if (aReview == null) {
 			sb.append("can not delete a null review");
 			return sb.toString();
 		}
 
 		sb.setLength(0);
 		Resource resource = aReview.eResource();
 		if (resource == null) {
 			sb.append("No able to mark delete a review not associated to a resource");
 			return sb.toString();
 		}
 
 		String reviewName = aReview.getName();
 		R4EReviewGroup group = (R4EReviewGroup) aReview.eContainer();
 		group.getReviewsMap().remove(reviewName);
 
 		// // TODO: This action would require to update each user reviews file, where permissions may be an issue. For
 		// the
 		// // moment only mark the review as disabled and unload resources
 		// EMap<String, R4EUserReviews> usrReviews = group.getUserReviews();
 		// if (usrReviews != null && usrReviews.size() > 0) {
 		// Collection<R4EUserReviews> reviews = usrReviews.values();
 		// for (Iterator<R4EUserReviews> iterator = reviews.iterator(); iterator.hasNext();) {
 		// R4EUserReviews r4eUserReviews = (R4EUserReviews) iterator.next();
 		//
 		// // created list
 		// EList<String> createdList = r4eUserReviews.getCreatedReviews();
 		// if (createdList != null && createdList.size() > 0) {
 		// createdList.remove(reviewName);
 		// }
 		//
 		// // invited list
 		// r4eUserReviews.getInvitedToMap().remove(reviewName);
 		// }
 		// }
 
 		// Update ReviewGroup
 		group.getReviews().remove(aReview);
 
 		// Mark the review as disabled
 		aReview.setEnabled(false);
 
 		// Save the status, this may cause
 		fWriter.saveResource(resource);
 
 		// unload resources
 		closeR4EReview(aReview);
 
 		return null;
 	}
 
 	// ------------------------------------------------------------------------
 	// ITEMS Resource Methods
 	// ------------------------------------------------------------------------
 	/*
 	 * 
 	 * /* (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.mylyn.reviews.r4e.core.model.serial.Persistence.UserItemResFactory#createR4EItem(org.eclipse.mylyn
 	 * .reviews.r4e.core.model.R4EParticipant)
 	 */
 	public R4EItem createR4EItem(R4EParticipant aParticipant) throws ResourceHandlingException {
 		if (aParticipant == null) {
 			return null;
 		}
 
 		Resource usrResource = aParticipant.eResource();
 		if (usrResource == null) {
 			StringBuilder sb = new StringBuilder("No resource found for given Participant: " + aParticipant.getId());
 			throw new ResourceHandlingException(sb.toString());
 		}
 
 		// Prepare before update
 		R4EReview review = aParticipant.getReviewInstance();
 		R4EItem item = RModelFactory.eINSTANCE.createR4EItem();
 		R4EID itemID = RModelFactory.eINSTANCE.createR4EID();
 		itemID.setSequenceID(aParticipant.getSequenceIDCounterNext());
 		itemID.setUserID(aParticipant.getId());
 		ResourceSet resSet = usrResource.getResourceSet();
 
 		// update ITEM
 		item.setAddedBy(aParticipant);
 		item.setAddedById(aParticipant.getId());
 		item.setReview(review);
 		item.setId(itemID);
 
 		// update derived references to the review
 		review.getIdsMap().put(itemID, item);
 		review.getReviewItems().add(item);
 
 		// Verify if an item already exists to append to the same resource
 		int addedItems = aParticipant.getAddedItems().size();
 		Resource itemResource = null;
 		if (addedItems > 0) {
 			// Resolve items resource
 			R4EItem firstItem = aParticipant.getAddedItems().get(0);
 			itemResource = firstItem.eResource();
 			if (itemResource == null) {
 				Activator.fTracer.traceError("Item found not associated to a Resource, addedBy: "
 						+ firstItem.getAddedById() + ", Description: " + firstItem.getDescription());
 			}
 		}
 
 		if (itemResource == null) {
 			// crate item resource
 			URI usrURI = usrResource.getURI();
 			URI reviewFolderURI = fWriter.getFolderPath(usrURI);
 			// create a uri for the new participant, the user is serialized within the comments resource
 			URI itemURI = fWriter.createResourceURI(aParticipant.getId(), reviewFolderURI, ResourceType.USER_ITEM);
 			// create a Resource for the Participant
 			itemResource = resSet.createResource(itemURI);
 		}
 
 		// update Participant
 		aParticipant.getAddedItems().add(item);
 		// update resource
 		itemResource.getContents().add(item);
 
 		// Save persistence changes affecting the Participant and the actual items
 		fWriter.saveResource(itemResource);
 		fWriter.saveResource(usrResource);
 
 		return item;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.mylyn.reviews.r4e.core.model.serial.Persistence.UserItemResFactory#deleteR4EItem(org.eclipse.mylyn
 	 * .reviews.r4e.core.model.R4EItem, boolean)
 	 */
 	public void deleteR4EItem(R4EItem aItem, boolean aDeleteOnDisk) throws ResourceHandlingException {
 		if (aItem == null) {
 			return;
 		}
 
 		R4EUser user = (R4EUser) aItem.eContainer();
 		if (user == null) {
 			return;
 		}
 
 		// TODO: To consider before implementing actual resource removal
 		// Removing other elements may have references to this item (e.g reviewed items)
 		// Removing all references may present reading and writing conflicts and file permissions issues, since the
 		// references may be across different users.
 		// removing items may cause reshuffle of the references between items, and cause incorrect references indexes on
 		// EMF
 		// Disable the item itself
 		aItem.setEnabled(false);
 		// Disable all related deltas
 		EList<R4EFileContext> fileContextList = aItem.getFileContextList();
 		if (fileContextList != null && fileContextList.size() > 0) {
 			for (Iterator<R4EFileContext> iterator = fileContextList.iterator(); iterator.hasNext();) {
 				R4EFileContext fileContext = iterator.next();
 				EList<R4EDelta> deltas = fileContext.getDeltas();
 				if (deltas != null && deltas.size() > 0) {
 					for (Iterator<R4EDelta> iterator2 = deltas.iterator(); iterator2.hasNext();) {
 						R4EDelta delta = iterator2.next();
 						delta.setEnabled(false);
 					}
 				}
 			}
 		}
 
 		// Save the change of status
 		Resource resource = aItem.eResource();
 		if (resource != null) {
 			fWriter.saveResource(resource);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.mylyn.reviews.r4e.core.model.serial.Persistence.UserItemResFactory#createR4EFileContext(org.eclipse
 	 * .mylyn.reviews.r4e.core.model.R4EItem)
 	 */
 	public R4EFileContext createR4EFileContext(R4EItem item) throws ResourceHandlingException {
 		R4EFileContext fileContext = null;
 		if (!(isAssociatedToResource(item))) {
 			StringBuilder sb = new StringBuilder("Can not create FileContext from an Item not associated to a Resource");
 			throw new ResourceHandlingException(sb.toString());
 		}
 
 		fileContext = RModelFactory.eINSTANCE.createR4EFileContext();
 		item.getFileContextList().add(fileContext);
 		Resource resource = item.eResource();
 		resource.getContents().add(fileContext);
 
 		R4EUser user = (R4EUser) item.eContainer();
 		// Create an R4EID for the context
 		R4EID contextID = RModelFactoryExt.eINSTANCE.createR4EID();
 		contextID.setSequenceID(user.getSequenceIDCounterNext());
 		contextID.setUserID(user.getId());
 
 		// Associate new fileContext with ID
 		fileContext.setId(contextID);
 		// Register ID to idMap at the review level
 		user.getReviewInstance().getIdsMap().put(contextID, fileContext);
 
 		fWriter.saveResource(resource);
 
 		return fileContext;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.mylyn.reviews.r4e.core.model.serial.Persistence.UserItemResFactory#createR4EBaseFileVersion(org.eclipse
 	 * .mylyn.reviews.r4e.core.model.R4EFileContext)
 	 */
 	public R4EFileVersion createR4EBaseFileVersion(R4EFileContext context) throws ResourceHandlingException {
 		R4EFileVersion fileVersion = createR4EFileVersion(context);
 		context.setBase(fileVersion);
 		fWriter.saveResource(fileVersion.eResource());
 		return fileVersion;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.mylyn.reviews.r4e.core.model.serial.Persistence.UserItemResFactory#createR4ETargetFileVersion(org
 	 * .eclipse.mylyn.reviews.r4e.core.model.R4EFileContext)
 	 */
 	public R4EFileVersion createR4ETargetFileVersion(R4EFileContext context) throws ResourceHandlingException {
 		R4EFileVersion fileVersion = createR4EFileVersion(context);
 		context.setTarget(fileVersion);
 		fWriter.saveResource(fileVersion.eResource());
 		return fileVersion;
 	}
 
 	/**
 	 * @param context
 	 * @return
 	 * @throws ResourceHandlingException
 	 */
 	private R4EFileVersion createR4EFileVersion(R4EFileContext context) throws ResourceHandlingException {
 		R4EFileVersion fileVersion = null;
 		if (!(isAssociatedToResource(context))) {
 			StringBuilder sb = new StringBuilder(
 					"Can not create FileVersion from an context not associated to a Resource");
 			throw new ResourceHandlingException(sb.toString());
 		}
 
 		fileVersion = RModelFactoryExt.eINSTANCE.createR4EFileVersion();
 		context.eResource().getContents().add(fileVersion);
 
 		return fileVersion;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.mylyn.reviews.r4e.core.model.serial.Persistence.UserItemResFactory#createR4EDelta(org.eclipse.mylyn
 	 * .reviews.r4e.core.model.R4EFileContext)
 	 */
 	public R4EDelta createR4EDelta(R4EFileContext context) throws ResourceHandlingException {
 		R4EDelta delta = null;
 		// Validate
 		if (!(isAssociatedToResource(context))) {
 			StringBuilder sb = new StringBuilder("Can not create Delta from a context not associated to a Resource");
 			throw new ResourceHandlingException(sb.toString());
 		}
 
 		// input is expected to be fully created (all references updated)
 		R4EItem item = (R4EItem) context.eContainer();
 		R4EUser user = (R4EUser) item.eContainer();
 
 		if (user == null) {
 			return null;
 		}
 
 		// Crate delta
 		delta = RModelFactoryExt.eINSTANCE.createR4EDelta();
 
 		// Create an R4EID
 		R4EID deltaID = RModelFactoryExt.eINSTANCE.createR4EID();
 		deltaID.setSequenceID(user.getSequenceIDCounterNext());
 		deltaID.setUserID(user.getId());
 
 		// Associate new delta to ID
 		delta.setId(deltaID);
 		// Register ID to idMap at the review level
 		user.getReviewInstance().getIdsMap().put(deltaID, delta);
 		// Associate delta to the context resource
 		context.getDeltas().add(delta);
 		// Save the resource
 		context.eResource().getContents().add(delta);
 		fWriter.saveResource(delta.eResource());
 
 		return delta;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.mylyn.reviews.r4e.core.model.serial.Persistence.UserItemResFactory#deleteR4EDelta(org.eclipse.mylyn
 	 * .reviews.r4e.core.model.R4EDelta)
 	 */
 	public void deleteR4EDelta(R4EDelta delta) throws ResourceHandlingException {
 		if (!(isAssociatedToResource(delta))) {
 			StringBuilder sb = new StringBuilder(
 					"Can not udpate the State of a Delta if it's not associated to a Resource");
 			throw new ResourceHandlingException(sb.toString());
 		}
 
 		delta.setEnabled(false);
 		fWriter.saveResource(delta.eResource());
 
 		return;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.mylyn.reviews.r4e.core.model.serial.Persistence.UserItemResFactory#createR4EBaseTextContent(org.eclipse
 	 * .mylyn.reviews.r4e.core.model.R4EDelta)
 	 */
 	public R4ETextContent createR4EBaseTextContent(R4EDelta delta) throws ResourceHandlingException {
 		return createR4ETextContent(delta, true);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.mylyn.reviews.r4e.core.model.serial.Persistence.UserItemResFactory#createR4ETargetTextContent(org
 	 * .eclipse.mylyn.reviews.r4e.core.model.R4EDelta)
 	 */
 	public R4ETextContent createR4ETargetTextContent(R4EDelta delta) throws ResourceHandlingException {
 		return createR4ETextContent(delta, false);
 	}
 
 	/**
 	 * @param delta
 	 * @param base
 	 * @return
 	 * @throws ResourceHandlingException
 	 */
 	private R4ETextContent createR4ETextContent(R4EDelta delta, boolean base) throws ResourceHandlingException {
 		if (!(isAssociatedToResource(delta))) {
 			StringBuilder sb = new StringBuilder(
 					"Can not create TextContent from a Delta/Selection not associated to a Resource");
 			throw new ResourceHandlingException(sb.toString());
 		}
 
 		R4ETextContent txtContent = RModelFactoryExt.eINSTANCE.createR4ETextContent();
 		if (base) {
 			delta.setBase(txtContent);
 		} else {
 			delta.setTarget(txtContent);
 		}
 		// Associate to Resource and save
 		delta.eResource().getContents().add(txtContent);
 		fWriter.saveResource(txtContent.eResource());
 		return txtContent;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.mylyn.reviews.r4e.core.model.serial.Persistence.UserItemResFactory#createR4ETextPosition(org.eclipse
 	 * .mylyn.reviews.r4e.core.model.R4ETextContent)
 	 */
 	public R4ETextPosition createR4ETextPosition(R4ETextContent content) throws ResourceHandlingException {
 		R4ETextPosition textPosition = null;
 		if (!(isAssociatedToResource(content))) {
 			StringBuilder sb = new StringBuilder(
 					"Can not create a TextLocation from a Content not associated to a Resource");
 			throw new ResourceHandlingException(sb.toString());
 		}
 
 		textPosition = RModelFactoryExt.eINSTANCE.createR4ETextPosition();
 		content.setLocation(textPosition);
 
 		// Associate to resource and save
 		content.eResource().getContents().add(textPosition);
 		fWriter.saveResource(textPosition.eResource());
 
 		return textPosition;
 	}
 
 	// ------------------------------------------------------------------------
 	// COMMENTS Resource Methods
 	// ------------------------------------------------------------------------
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.mylyn.reviews.r4e.core.model.serial.Persistence.UserCommentResFactory#createR4EParticipant(org.eclipse
 	 * .mylyn.reviews.r4e.core.model.R4EReview, java.lang.String, java.util.List)
 	 */
 	public R4EParticipant createR4EParticipant(R4EReview aReview, String aParticipantId, List<R4EUserRole> aRoles)
 			throws ResourceHandlingException {
 		if (aRoles == null || aRoles.size() == 0) {
 			StringBuilder sb = new StringBuilder("Creating a participant requires to define at least one role");
 			throw new ResourceHandlingException(sb.toString());
 		}
 		return (R4EParticipant) createR4EUser(aReview, aParticipantId, aRoles, false);
 	}
 
 	/**
 	 * @param aReview
 	 * @param userId
 	 * @return
 	 * @throws ResourceHandlingException
 	 */
 	public R4EUser createR4EUser(R4EReview aReview, String userId) throws ResourceHandlingException {
 		return createR4EUser(aReview, userId, null, false);
 	}
 
 	/**
 	 * @param aReview
 	 * @param aUserId
 	 * @param aRoles
 	 * @param newReview
 	 *            - A new review is triggering the creation of this new user
 	 * @return
 	 * @throws ResourceHandlingException
 	 */
 	private R4EUser createR4EUser(R4EReview aReview, String aUserId, List<R4EUserRole> aRoles, boolean newReview)
 			throws ResourceHandlingException {
 		// Validate input
 		if (aReview == null || aUserId == null) {
 			return null;
 		}
 
 		R4EUser participant = aReview.getUsersMap().get(aUserId);
 		StringBuilder sb = new StringBuilder();
 		if (participant != null) {
 			// the participant is already present
 			if (participant.eResource() == null) {
 				sb.append("A participant with the same Id already exist in unknown state: " + aUserId);
 				sb.append("/nClose and reopen review, to reset the state of the elements");
 				// participant exists but is not associated to a resource, e.g. unknown state of references
 				throw new ResourceHandlingException(sb.toString());
 			} else {
 				// user already exists
 				// update roles (in case there are new ones)
 				if (aRoles != null && participant instanceof R4EParticipant) {
 					R4EParticipant dParticipant = (R4EParticipant) participant;
 					EList<R4EUserRole> eRoles = dParticipant.getRoles();
 					for (Iterator<R4EUserRole> iterator = aRoles.iterator(); iterator.hasNext();) {
 						R4EUserRole role = iterator.next();
 						if (!(eRoles.contains(role))) {
 							eRoles.add(role);
 						}
 					}
 				}
 
 				// check if the user reviews lists are upto date
 				Resource userReviewsRes = createUserReviewsResource(aReview, participant);
 				fWriter.saveResource(userReviewsRes);
 
 				return participant;
 			}
 		}
 
 		sb.setLength(0);
 		sb.append("ResourceSet not found to create participant: " + aUserId);
 
 		// obtain ResourceSet from the Review
 		Resource containerResource = aReview.eResource();
 		if (containerResource == null) {
 			throw new ResourceHandlingException(sb.toString());
 		}
 		ResourceSet resSet = containerResource.getResourceSet();
 
 		if (aRoles != null) {
 			// create participant and build references
 			participant = createR4EParticipantInstance(aReview, aUserId, aRoles);
 		} else {
 			participant = createR4EUserInstance(aReview, aUserId);
 		}
 
 		// A new review is triggering the creation of this participant
 		if (newReview) {
 			participant.setReviewCreatedByMe(true);
 		}
 		participant.getGroupPaths().add(containerResource.getURI().toString());
 
 		// find the review file uri to create the resource for the new participant
 		URI folderPath = containerResource.getURI();
 		// convert to folder
 		folderPath = fWriter.getFolderPath(folderPath);
 		// define the participants file URI
 		URI participantURI = fWriter.createResourceURI(aUserId, folderPath, ResourceType.USER_COMMENT);
 		// create a Resource for the Participant
 		Resource participantResource = resSet.createResource(participantURI);
 		// associate the participant with the resource
 		participantResource.getContents().add(participant);
 
 		// Serialize the participant resources
 		Resource userReviewsRes = createUserReviewsResource(aReview, participant);
 		fWriter.saveResource(participantResource);
 		fWriter.saveResource(userReviewsRes);
 
 		return participant;
 	}
 
 	/**
 	 * Create a brand new participant e.g. no associated comments
 	 * 
 	 * @param aReview
 	 * @param aParticipantId
 	 * @param aRoles
 	 * @return
 	 */
 	private R4EParticipant createR4EParticipantInstance(R4EReview aReview, String aParticipantId,
 			List<R4EUserRole> aRoles) {
 		// participant and id
 		R4EParticipant participant = RModelFactory.eINSTANCE.createR4EParticipant();
 		updateUsrReferences(participant, aParticipantId, aReview);
 
 		// roles
 		if (aRoles != null && aRoles.size() > 0) {
 			for (Iterator<R4EUserRole> iterator = aRoles.iterator(); iterator.hasNext();) {
 				R4EUserRole r4eUserRole = iterator.next();
 				participant.getRoles().add(r4eUserRole);
 			}
 		}
 
 		return participant;
 	}
 
 	/**
 	 * Create a new R4E User
 	 * 
 	 * @param aReview
 	 * @param aUserId
 	 * @return
 	 */
 	private R4EUser createR4EUserInstance(R4EReview aReview, String aUserId) {
 		// participant and id
 		R4EUser user = RModelFactory.eINSTANCE.createR4EUser();
 		updateUsrReferences(user, aUserId, aReview);
 		return user;
 	}
 
 	private void updateUsrReferences(R4EUser aUser, String aUsrId, R4EReview aReview) {
 		aUser.setId(aUsrId);
 		aUser.setReviewInstance(aReview);
 		// add to list of reviews
 		if (aReview != null) {
 			aReview.getUsersMap().put(aUsrId, aUser);
 		}
 
 		return;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.mylyn.reviews.r4e.core.model.serial.Persistence.UserCommentResFactory#createR4EAnomaly(org.eclipse
 	 * .mylyn.reviews.r4e.core.model.R4EParticipant)
 	 */
 	public R4EAnomaly createR4EAnomaly(R4EParticipant aAnomalyCreator) throws ResourceHandlingException {
 		// not able to obtain a unique id with an invalid participant or Review
 		if (aAnomalyCreator == null) {
 			return null;
 		}
 
 		R4EReview review = aAnomalyCreator.getReviewInstance();
 		R4EAnomaly anomaly = RModelFactory.eINSTANCE.createR4EAnomaly();
 
 		// set the transient eOpposite reference
 		if (review != null) {
 			review.getTopics().add(anomaly);
 		}
 
 		// Comments references
 		updCommonCommentRefs(aAnomalyCreator, anomaly);
 
 		// Save resources
 		Resource participantRes = aAnomalyCreator.eResource();
 		participantRes.getContents().add(anomaly);
 		fWriter.saveResource(participantRes);
 
 		return anomaly;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.mylyn.reviews.r4e.core.model.serial.Persistence.UserCommentResFactory#createR4EComment(org.eclipse
 	 * .mylyn.reviews.r4e.core.model.R4EParticipant, org.eclipse.mylyn.reviews.r4e.core.model.R4EAnomaly)
 	 */
 	public R4EComment createR4EComment(R4EParticipant aParticipant, R4EAnomaly aContainerAnomaly)
 			throws ResourceHandlingException {
 		if (aContainerAnomaly == null) {
 			return null;
 		}
 
 		R4EComment comment = RModelFactory.eINSTANCE.createR4EComment();
 		aContainerAnomaly.getComments().add(comment);
 
 		updCommonCommentRefs(aParticipant, comment);
 
 		// Register the associated anomaly
 		comment.setAnomaly(aContainerAnomaly);
 
 		// Save resource, participant and anomalies go in the same resource
 		Resource participantRes = aParticipant.eResource();
 		participantRes.getContents().add(comment);
 		fWriter.saveResource(participantRes);
 
 		return comment;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.mylyn.reviews.r4e.core.model.serial.Persistence.UserCommentResFactory#createR4ETextContent(org.eclipse
 	 * .mylyn.reviews.r4e.core.model.R4EAnomaly)
 	 */
 	public R4ETextContent createR4ETextContent(R4EAnomaly anomaly) throws ResourceHandlingException {
 		if (!(isAssociatedToResource(anomaly))) {
 			StringBuilder sb = new StringBuilder(
 					"Can not create TextContent from an Anomaly not associated to a Resource");
 			throw new ResourceHandlingException(sb.toString());
 		}
 
 		R4ETextContent txtContent = RModelFactoryExt.eINSTANCE.createR4ETextContent();
 		anomaly.getLocation().add(txtContent);
 
 		// Associate to Resource and save
 		anomaly.eResource().getContents().add(txtContent);
 		fWriter.saveResource(txtContent.eResource());
 		return txtContent;
 	}
 
 	public R4EAnomalyTextPosition createR4EAnomalyTextPosition(R4EContent content) throws ResourceHandlingException {
 		R4EAnomalyTextPosition textPosition = null;
 		if (!(isAssociatedToResource(content))) {
 			StringBuilder sb = new StringBuilder(
 					"Can not create an R4EAnomalyTextPosition from a Content not associated to a Resource");
 			throw new ResourceHandlingException(sb.toString());
 		}
 
 		textPosition = RModelFactoryExt.eINSTANCE.createR4EAnomalyTextPosition();
 		content.setLocation(textPosition);
 
 		// Associate to resource and save
 		content.eResource().getContents().add(textPosition);
 		fWriter.saveResource(textPosition.eResource());
 
 		return textPosition;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.mylyn.reviews.r4e.core.model.serial.Persistence.UserCommentResFactory#createR4EFileVersion(org.eclipse
 	 * .mylyn.reviews.r4e.core.model.R4EAnomalyTextPosition)
 	 */
 	public R4EFileVersion createR4EFileVersion(R4EAnomalyTextPosition txtPosition) throws ResourceHandlingException {
 		R4EFileVersion fileVersion = null;
 		if (!(isAssociatedToResource(txtPosition))) {
 			StringBuilder sb = new StringBuilder(
 					"Can not create FileVersion from an txtPosition not associated to a Resource");
 			throw new ResourceHandlingException(sb.toString());
 		}
 
 		fileVersion = RModelFactoryExt.eINSTANCE.createR4EFileVersion();
 		txtPosition.eResource().getContents().add(fileVersion);
 
 		txtPosition.setFile(fileVersion);
 		fWriter.saveResource(fileVersion.eResource());
 		return fileVersion;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.mylyn.reviews.r4e.core.model.serial.Persistence.UserCommentResFactory#deleteR4EComment(org.eclipse
 	 * .mylyn.reviews.r4e.core.model.R4EComment, boolean)
 	 */
 	public void deleteR4EComment(R4EComment aComment, boolean aDeleteOnDisk) throws ResourceHandlingException {
 		if (aComment == null) {
 			return;
 		}
 
 		aComment.setEnabled(false);
 		Resource resource = aComment.eResource();
 		if (resource == null) {
 			StringBuilder sb = new StringBuilder("Not able to delete a comment with no associated resource");
 			throw new ResourceHandlingException(sb.toString());
 		}
 
 		fWriter.saveResource(resource);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.mylyn.reviews.r4e.core.model.serial.Persistence.UserCommentResFactory#deleteR4EAnomaly(org.eclipse
 	 * .mylyn.reviews.r4e.core.model.R4EAnomaly, boolean)
 	 */
 	public void deleteR4EAnomaly(R4EAnomaly aAnomaly, boolean aDeleteOnDisk) throws ResourceHandlingException {
 		deleteR4EComment(aAnomaly, aDeleteOnDisk);
 	}
 
 	/**
 	 * @param aCommentCreator
 	 * @param aComment
 	 */
 	private void updCommonCommentRefs(R4EParticipant aCommentCreator, R4EComment aComment) {
 		Date createdOn = new Date();
 		aCommentCreator.getAddedComments().add(aComment);
 		aComment.setUser(aCommentCreator);
 		aComment.setCreatedOn(createdOn);
 
 		// Assign the unique id to the comment
 		R4EID id = RModelFactory.eINSTANCE.createR4EID();
 		id.setUserID(aCommentCreator.getId());
 		id.setSequenceID(aCommentCreator.getSequenceIDCounterNext());
 		aComment.setId(id);
 
 		// update references from review
 		aCommentCreator.getReviewInstance().getIdsMap().put(id, aComment);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.mylyn.reviews.r4e.core.model.serial.Persistence#testWritePermissions(org.eclipse.emf.common.util.URI)
 	 */
 	public boolean testWritePermissions(URI aLocation) throws ResourceHandlingException {
 		if (aLocation == null) {
 			StringBuilder sb = new StringBuilder("Illegal null argument recieved");
 			throw new ResourceHandlingException(sb.toString());
 		}
 
 		String path = aLocation.toString();
 		File folder = new File(path);
 		if ((!folder.isDirectory()) || (!folder.exists())) {
 			StringBuilder sb = new StringBuilder("Illegal argument recieved i.e. URI is not a directory");
 			throw new ResourceHandlingException(sb.toString());
 		}
 
 		// Perform actual write test
 		return UserPermission.canWrite(path + File.separator + "test.txt");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.mylyn.reviews.r4e.core.model.serial.Persistence#PollDirUpdates(org.eclipse.emf.ecore.EObject)
 	 */
 	public List<Resource> pollDirUpdates(EObject atElementLoc) {
 		// TODO Implement me
 		return null;
 	}
 
 	/**
 	 * @param eobject
 	 * @return
 	 */
 	private boolean isAssociatedToResource(EObject eobject) {
 		if (eobject == null) {
 			return false;
 		}
 		Resource resource = eobject.eResource();
 		if (resource == null) {
 			return false;
 		}
 
 		// The associated resource is not null
 		return true;
 	}
 
 }
