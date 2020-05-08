 package com.reckonlabs.reckoner.contentservices.service;
 
 import java.lang.Boolean;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.data.domain.PageRequest;
 import org.springframework.data.domain.Sort;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import com.reckonlabs.reckoner.contentservices.cache.ReckoningCache;
 import com.reckonlabs.reckoner.contentservices.repo.ReckoningRepo;
 import com.reckonlabs.reckoner.contentservices.repo.ReckoningRepoCustom;
 import com.reckonlabs.reckoner.domain.ApprovalStatusEnum;
 import com.reckonlabs.reckoner.domain.message.Message;
 import com.reckonlabs.reckoner.domain.message.MessageEnum;
 import com.reckonlabs.reckoner.domain.message.ServiceResponse;
 import com.reckonlabs.reckoner.domain.message.ReckoningServiceList;
 import com.reckonlabs.reckoner.domain.notes.Comment;
 import com.reckonlabs.reckoner.domain.reckoning.Answer;
 import com.reckonlabs.reckoner.domain.reckoning.Reckoning;
 import com.reckonlabs.reckoner.domain.reckoning.ReckoningTypeEnum;
 import com.reckonlabs.reckoner.domain.user.User;
 import com.reckonlabs.reckoner.domain.utility.DateUtility;
 import com.reckonlabs.reckoner.domain.utility.DBUpdateException;
 import com.reckonlabs.reckoner.domain.utility.ListPagingUtility;
 
 @Component
 public class ReckoningServiceImpl implements ReckoningService {
 	
 	@Autowired
 	ReckoningRepo reckoningRepo;
 	
 	@Autowired
 	ReckoningRepoCustom reckoningRepoCustom;
 	
 	@Autowired
 	ReckoningCache reckoningCache;
 	
 	@Autowired
 	UserService userService;
 	
 	private static final Logger log = LoggerFactory
 			.getLogger(ReckoningServiceImpl.class);
 	
 	@Override
 	public ServiceResponse postReckoning (Reckoning reckoning, String sessionId) {
 		
 		try {
 			// Compute the answer index for each provided answer.
 			int answerIndex = 0;
 			for (Answer answer : reckoning.getAnswers()) {
 				answer.setIndex(answerIndex);
 				answerIndex ++;
 			}
 			
 			// Clean up the reckoning for fields that can't be set for a new posting.
 			reckoning.setId(null);
 			reckoning.setApproved(false);
 			reckoning.setRejected(false);
 			reckoning.setSubmissionDate(DateUtility.now());
 			reckoning.setPostingDate(null);
 			reckoning.setClosingDate(null);
 			reckoning.setHighlighted(false);
 			reckoning.setFlags(null);
 			reckoning.setFavorites(null);
 			reckoning.setComments(null);
 			if (reckoning.getInterval() == null || reckoning.getInterval() == 0) {
 				reckoning.setInterval(10080);
 			}
 			
 			// Set the random select number to be a double between 0 and 1.  This is used to
 			// enable random Reckoning selection.
 			
 			// Clean up the tags.
 			reckoning.setTags(formatTags(reckoning.getTags()));
 			
 			reckoning.setRandomSelect(new Random().nextDouble());
 			reckoningRepoCustom.insertNewReckoning(reckoning);
 			
 			reckoningCache.removeCachedUserReckoningSummaries(reckoning.getSubmitterId());
 		} catch (Exception e) {
 			log.error("General exception when inserting a new reckoning: " + e.getMessage());
 			log.debug("Stack Trace:", e);			
 			return (new ServiceResponse(new Message(MessageEnum.R01_DEFAULT), false));	
 		}
 		
 		return new ServiceResponse();
 	}
 	
 	@Override
 	public ServiceResponse updateReckoning (Reckoning reckoning, boolean merge, String sessionId) {
 		
 		try {
 			// Clean up the tags (if any).
 			reckoning.setTags(formatTags(reckoning.getTags()));
 			
 			if (merge) {
 				if (reckoningRepoCustom.confirmReckoningExists(reckoning.getId())) {					
 					reckoningRepoCustom.mergeReckoning(reckoning);
 				}
 				else {
 					return (new ServiceResponse(new Message(MessageEnum.R106_POST_RECKONING), false));
 				}
 			} else {
 				reckoningRepoCustom.updateReckoning(reckoning);
 			}
 			
 			reckoningCache.removeCachedReckoning(reckoning.getId());
 		} catch (Exception e) {
 			log.error("General exception when updating a reckoning: " + e.getMessage());
 			log.debug("Stack Trace:", e);			
 			return (new ServiceResponse(new Message(MessageEnum.R01_DEFAULT), false));	
 		}
 		
 		return new ServiceResponse();
 	}
 	
 	@Override
 	public ServiceResponse approveReckoning (String id, String sessionId) {
 		
 		try {
 			List<Reckoning> approvedReckoning = reckoningRepo.findById(id);
 			
 			if (approvedReckoning != null && approvedReckoning.size() > 0) {
 				reckoningRepoCustom.approveReckoning(id, userService.getUserBySessionId(sessionId).getUser().getId(), DateUtility.now(), 
 						new Date(DateUtility.now().getTime() + approvedReckoning.get(0).getInterval() * 60000));
 				reckoningCache.removeCachedReckoning(id);
 				reckoningCache.removeCachedUserReckoningSummaries(approvedReckoning.get(0).getSubmitterId());
 			} else {
 				log.info("Request to approve non-existent reckoning: " + id);
 				return (new ServiceResponse(new Message(MessageEnum.R300_APPROVE_RECKONING), false));					
 			}
 			
 		} catch (DBUpdateException dbE) {
 			log.error("Database exception when accepting a reckoning: " + dbE.getMessage());
 			log.debug("Stack Trace:", dbE);			
 			return (new ServiceResponse(new Message(MessageEnum.R01_DEFAULT), false));				
 		}
 		  catch (Exception e) {
 			log.error("General exception when accepting a reckoning: " + e.getMessage());
 			log.debug("Stack Trace:", e);			
 			return (new ServiceResponse(new Message(MessageEnum.R01_DEFAULT), false));	
 		}
 		
 		return new ServiceResponse();
 	}
 	
 	@Override
 	public ServiceResponse rejectReckoning (String id, String sessionId) {
 		
 		try {
 			List<Reckoning> rejectedReckoning = reckoningRepo.findById(id);
 			
 			if (rejectedReckoning != null && rejectedReckoning.size() > 0) {
 				reckoningRepoCustom.rejectReckoning(id, userService.getUserBySessionId(sessionId).getUser().getId());
 				reckoningCache.removeCachedReckoning(id);
 				reckoningCache.removeCachedUserReckoningSummaries(rejectedReckoning.get(0).getSubmitterId());
 			} else {
 				log.info("Request to reject non-existent reckoning: " + id);
 				return (new ServiceResponse(new Message(MessageEnum.R300_APPROVE_RECKONING), false));					
 			}
 			
 		} catch (DBUpdateException dbE) {
 			log.error("Database exception when rejecting a reckoning: " + dbE.getMessage());
 			log.debug("Stack Trace:", dbE);			
 			return (new ServiceResponse(new Message(MessageEnum.R01_DEFAULT), false));				
 		}
 		  catch (Exception e) {
 			log.error("General exception when rejecting a reckoning: " + e.getMessage());
 			log.debug("Stack Trace:", e);			
 			return (new ServiceResponse(new Message(MessageEnum.R01_DEFAULT), false));	
 		}
 		
 		return new ServiceResponse();
 	}
 	
 	@Override
 	public ReckoningServiceList getReckoning (String id, String sessionId) {
 		return getReckoning (id, false, false, sessionId);
 	}
 	
 	@Override
 	public ReckoningServiceList getReckoning (String id, boolean includeUnaccepted, boolean pageVisit, String sessionId) {		
 		List<Reckoning> reckoningList = null;
 		try {
 			// Check the caches to see if the Reckoning has already been pulled.  If so, there you go.
 			reckoningList = reckoningCache.getCachedReckoning(id);
 			
 			// If this is a 'page visit', (i.e. a unique display of this Reckoning on an end client), increment
 			// the views value for this Reckoning in the DB.  Also, update the value returned from the cache.
 			if (pageVisit) {
 				reckoningRepoCustom.incrementReckoningViews(id);
 				if (reckoningList != null && !reckoningList.isEmpty()) {
 					reckoningList.get(0).incrementViews();
 					reckoningCache.setCachedReckoning(reckoningList, id);
 				}
 			}
 			
 			// If not, pull it (excluding rejected reckonings as specified).
 			if (reckoningList == null) {
 				if (includeUnaccepted) {
 					reckoningList = reckoningRepo.findById(id);
 				} else {
 					reckoningList = reckoningRepo.findByIdAndApproved(id, true);
 				}
 				
 				if (reckoningList != null && !reckoningList.isEmpty()) {
 					// Iterate through the comments to add the pertinent user information necessary to render them.
 					if (reckoningList.get(0).getComments() != null) {
 						for (Comment comment : reckoningList.get(0).getComments()) {
 							comment.setUser(userService.getUserByUserId(comment.getPosterId(), true).getUser());
 						}
 					}
 					// Get the summary for the Commentary user
 					if (reckoningList.get(0).getCommentaryUserId() != null) {
 						reckoningList.get(0).setCommentaryUser(userService.getUserByUserId
 								(reckoningList.get(0).getCommentaryUserId(), true).getUser());
 					}
 					// Get the summary for the Posting user
 					if (reckoningList.get(0).getSubmitterId() != null) {
 						reckoningList.get(0).setPostingUser(userService.getUserByUserId
 								(reckoningList.get(0).getSubmitterId(), true).getUser());
 					}					
 					
 					reckoningCache.setCachedReckoning(reckoningList, id);
 				}
 			}
 		} catch (Exception e) {
 			log.error("General exception when retrieving a reckoning: " + e.getMessage());
 			log.debug("Stack Trace:", e);	
 			return new ReckoningServiceList(null, new Message(MessageEnum.R01_DEFAULT), false);
 		}	
 		
 		return new ReckoningServiceList(reckoningList, new Message(), true);
 	}
 	
 	@Override
 	public ReckoningServiceList getApprovalQueue (Integer page, Integer size, Boolean latestFirst, String sessionId) {
 		List<Reckoning> approvalQueue = null;
 		
 		try {
 			if (page != null && size != null) {
 				if (latestFirst == null) {
 					latestFirst = false;
 				}
 				if (latestFirst.booleanValue()) {
 					approvalQueue = reckoningRepo.findByApprovedAndRejected(false, false, 
 							new PageRequest(page, size, Sort.Direction.DESC, "submissionDate")).getContent();
 				} else {
 					approvalQueue = reckoningRepo.findByApprovedAndRejected(false, false, 
 							new PageRequest(page, size, Sort.Direction.ASC, "submissionDate")).getContent();					
 				}
 			} else {
 				approvalQueue = reckoningRepo.findByApprovedAndRejected(false, false);
 			}
 		} catch (Exception e) {
 			log.error("General exception when getting the approval queue: " + e.getMessage());
 			log.debug("Stack Trace:", e);	
 			return new ReckoningServiceList(null, new Message(MessageEnum.R01_DEFAULT), false);
 		}
 		
 		return new ReckoningServiceList(approvalQueue, new Message(), true);
 	}
 	
 	@Override
 	// NOTE:  This is separate from the standard Reckoning Summary query because of the use of caching, as well
 	// as a different pagination policy.
 	public ReckoningServiceList getReckoningSummariesByUser(String submitterId, Integer page, Integer size, String sessionId) {
 		List<Reckoning> reckonings = new LinkedList<Reckoning>();
 		long count = 0;
 		try {
 			reckonings = reckoningCache.getCachedUserReckoningSummaries(submitterId);
 			if (reckonings == null) {
 				reckonings = reckoningRepoCustom.getReckoningSummaries(ReckoningTypeEnum.OPEN_AND_CLOSED, 
 						null, null, null, null, null, null, null, 
 						submitterId, ApprovalStatusEnum.APPROVED_AND_PENDING, 
 						"submissionDate", null, null, null, null);
 				
 				// Pull the user profile associated with the submitter Id and attach it to each Reckoning.
 				User user = userService.getUserByUserId(submitterId, true).getUser();
 				for (Reckoning reckoning : reckonings) {
 					reckoning.setPostingUser(user);
 				}
 				
 				reckoningCache.setCachedUserReckoningSummaries(submitterId, reckonings);
 			}
 			
 			count = reckonings.size();
 			reckonings = (List<Reckoning>) ListPagingUtility.pageList(reckonings, page, size);
 		} catch (Exception e) {
 			log.error("General exception when getting reckoning summaries by user: " + e.getMessage());
 			log.debug("Stack Trace:", e);
 			return new ReckoningServiceList(null, new Message(MessageEnum.R01_DEFAULT), false);
 		}
 		
 		return new ReckoningServiceList(reckonings, count, new Message(), true);
 	}
 
 	@Override
 	public ReckoningServiceList getReckoningSummaries(ReckoningTypeEnum reckoningType, 
 			Date postedAfter, Date postedBefore,
 			Date closedAfter, Date closedBefore,
 			List<String> includeTags, List<String> excludeTags,
 			Boolean highlighted,
 			String submitterId,
 			ApprovalStatusEnum approvalStatus,
 			String sortBy, Boolean ascending,
 			Integer page, Integer size, Boolean randomize,
 			String sessionId) {
 		List<Reckoning> reckonings = null;
 		Long count = null;
 
 		try {
 			includeTags = formatTags(includeTags);
 			excludeTags = formatTags(excludeTags);
 			
 			reckonings = reckoningRepoCustom.getReckoningSummaries(reckoningType, postedBefore, postedAfter, closedBefore, 
 					closedAfter, includeTags, excludeTags, highlighted, submitterId, approvalStatus, sortBy, ascending, page, size, randomize);
 			
 			for (Reckoning reckoning : reckonings) {
 				reckoning.setPostingUser(userService.getUserByUserId
 								(reckoning.getSubmitterId(), true).getUser());
 			}
 			
 			count = reckoningRepoCustom.getReckoningCount(reckoningType, postedBefore, postedAfter, 
 					closedBefore, closedAfter, includeTags, excludeTags, highlighted, submitterId, approvalStatus);			
 		}
 		catch (Exception e) {
 			log.error("General exception when getting reckoning summaries: " + e.getMessage());
 			log.debug("Stack Trace:", e);
 			return new ReckoningServiceList(null, new Message(MessageEnum.R01_DEFAULT), false);
 		}
 		
 		return new ReckoningServiceList(reckonings, count, new Message(), true);
 	}
 	
 	@Override
 	public ReckoningServiceList getReckoningCount(ReckoningTypeEnum reckoningType, 
 			Date postedAfter, Date postedBefore,
 			Date closedAfter, Date closedBefore,
 			List<String> includeTags, List<String> excludeTags,
 			Boolean highlighted,
 			String submitterId,
 			ApprovalStatusEnum approvalStatus) {
 		Long count = null;
 		
 		try {
 			count = reckoningRepoCustom.getReckoningCount(reckoningType, postedBefore, postedAfter, 
 					closedBefore, closedAfter, includeTags, excludeTags, highlighted, submitterId, approvalStatus);
 			
 		} catch (Exception e) {
 			log.error("General exception when getting reckoning count: " + e.getMessage());
 			log.debug("Stack Trace:", e);
 			return new ReckoningServiceList(null, new Message(MessageEnum.R01_DEFAULT), false);
 		}
 		
 		return new ReckoningServiceList(null, count, new Message(), true);
 	}
 	
 	@Override
 	public ReckoningServiceList getRelatedReckoningSummaries(
 			ReckoningTypeEnum reckoningType, String reckoningId, Integer size) {
 		List<Reckoning> reckonings = new LinkedList<Reckoning>();
 		
 		try {		
 			List<Reckoning> baseReckonings = getReckoning(reckoningId, null).getReckonings();
 			if (baseReckonings != null && !baseReckonings.isEmpty()) {
 				Reckoning sourceReckoning = baseReckonings.get(0);
 				
 				// Use the tags to determine related reckonings.
 				if (sourceReckoning.getTags() != null && !sourceReckoning.getTags().isEmpty()) {
 					int cycleSize = sourceReckoning.getTags().size();
 					int reckoningsPerTag = size / cycleSize;
 					
 					if (reckoningsPerTag == 0) {
 						reckoningsPerTag = 1;
 						cycleSize = size.intValue();
 					}
 					
 					for (int i = 0; i < cycleSize; i ++) {
 						List<String> tagList = new ArrayList<String>(1);
 						tagList.add(sourceReckoning.getTags().get(i));
 						
						List<Reckoning> suggestedReckonings = reckoningRepoCustom.getReckoningSummaries(reckoningType, null, null, null, 
								null, tagList, null, null, null, null, null, null, null, reckoningsPerTag, true);
						
						// Add all suggestions that are NOT the Reckoning we started with.
						for (Reckoning suggested : suggestedReckonings) {
							if (!suggested.getId().equals(reckoningId)) {
								reckonings.add(suggested);
							}
						}			
 					}					
 				}
				
				// If we haven't gotten four related reckonings, use random reckonings to fill out the rest.
				if (size - reckonings.size() > 0) {
					reckonings.addAll(reckoningRepoCustom.getReckoningSummaries(reckoningType, null, null, null, 
						null, null, null, null, null, null, null, null, null, (size - reckonings.size()), true));
				}
 			}
 		}
 		catch (Exception e) {
 			log.error("General exception when getting reckoning summaries: " + e.getMessage());
 			log.debug("Stack Trace:", e);
 			return new ReckoningServiceList(null, new Message(MessageEnum.R01_DEFAULT), false);
 		}
 		
 		return new ReckoningServiceList(reckonings, new Message(), true);
 	}
 
 	@Override
 	public ReckoningServiceList getRandomReckoning(
 			ReckoningTypeEnum reckoningType) {
 		List<Reckoning> reckonings = null;		
 		try {
 			reckonings = reckoningRepoCustom.getRandomReckoningSummary(reckoningType);
 		}
 		catch (Exception e) {
 			log.error("General exception when getting random reckoning: " + e.getMessage());
 			log.debug("Stack Trace:", e);
 			return new ReckoningServiceList(null, new Message(MessageEnum.R01_DEFAULT), false);
 		}
 		
 		return new ReckoningServiceList(reckonings, new Message(), true);
 	}	
 	
 	private static List<String> formatTags(List<String> tags) {
 		List<String> formattedTags = null;
 		
 		if (tags != null) {
 			formattedTags = new LinkedList<String> ();
 			for (String tag : tags) {
 				formattedTags.add(tag.trim().toLowerCase());
 			}
 		}
 		
 		return formattedTags;
 	}
 }
