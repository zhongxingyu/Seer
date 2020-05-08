 package org.robbins.flashcards.webservices;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import javax.inject.Inject;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Response;
 
 import org.apache.commons.lang3.StringUtils;
 import org.robbins.flashcards.model.FlashCard;
 import org.robbins.flashcards.model.Tag;
 import org.robbins.flashcards.service.FlashCardService;
 import org.robbins.flashcards.service.TagService;
 import org.robbins.flashcards.service.base.GenericJpaService;
 import org.robbins.flashcards.webservices.base.AbstractGenericResource;
 import org.robbins.flashcards.webservices.exceptions.GenericWebServiceException;
 import org.springframework.data.domain.PageRequest;
 import org.springframework.stereotype.Component;
 
 import com.wordnik.swagger.annotations.ApiOperation;
 
 @Path("/v1/flashcards/")
 @Component("flashCardsResource")
//@Api(value="/v1/flashcards", description = "Operations about FlashCards")
 @Produces("application/json")
 public class FlashCardsResource extends AbstractGenericResource<FlashCard, Long> {
 
 	@Inject
 	private FlashCardService flashcardService;
 	
 	@Inject
 	private TagService tagService;
 
 	protected GenericJpaService<FlashCard, Long> getService() {
 		return flashcardService;
 	}
 
 	@GET
 	@Path("/search")
 	@ApiOperation(value = "Search for FlashCards", responseClass = "org.robbins.flashcards.model.FlashCard")
 	public FlashCard[] search(	@QueryParam("page") Integer page,
 								@DefaultValue("25") @QueryParam("size") Integer size,
 								@QueryParam("question") String question,
 								@QueryParam("tags") String tags) {
 
 		return searchFlashCards(page, size, question, tags);
 	}
 
 	@GET
 	@Path("/search/count")
 	@ApiOperation(value = "Get a count of FlashCards", responseClass = "java.lang.Long")
 	public Long searchCount(@QueryParam("question") String question,
 							@QueryParam("tags") String tags) {
 
 		return Long.valueOf(searchFlashCards(null, null, question, tags).length);
 	}
 	
 	private FlashCard[] searchFlashCards(	Integer page,
 											Integer size,
 											String question,
 											String tags) {
 		
 		// find by Question
 		if (!StringUtils.isBlank(question)) {
 			// are we using pagination?
 			if (page != null) {
 				List<FlashCard> results = flashcardService.findByQuestionLike(question, new PageRequest(page, size));
 				return results.toArray(new FlashCard[results.size()]);
 			} else {
 				List<FlashCard> results = flashcardService.findByQuestionLike(question);
 				return results.toArray(new FlashCard[results.size()]);				
 		}	}
 		// find by Tags
 		if (!StringUtils.isBlank(tags)) {
 			StringTokenizer st = new StringTokenizer(tags, ",");
 			Set<Tag> tagsList = new HashSet<Tag>();
 			while (st.hasMoreTokens()) {
 				tagsList.add(new Tag(Long.parseLong(st.nextToken())));
 			}
 			// are we using Pagination?
 			if (page != null) {
 				List<FlashCard> results = flashcardService.findByTagsIn(tagsList, new PageRequest(page, size)); 
 				return results.toArray(new FlashCard[results.size()]);
 			} else {
 				List<FlashCard> results = flashcardService.findByTagsIn(tagsList); 
 				return results.toArray(new FlashCard[results.size()]);
 			}
 		}
 		else {
 			throw new GenericWebServiceException(Response.Status.BAD_REQUEST,
 					"Invalid 'search' parameters");
 		}
 	}
 	
 	@Override
 	public Response put(@PathParam("id") Long id, FlashCard entity) {
 
 		// some client apps don't know the Created By and Created Date, so make sure we set it 
 		if (entity.getCreatedBy() == null) {
 			FlashCard orig = flashcardService.findOne(id);
 			entity.setCreatedBy(orig.getCreatedBy());
 			entity.setCreatedDate(orig.getCreatedDate());
 		}
 
 		entity.setTags(configureTags(entity.getTags()));
 		
 		return super.put(id,  entity);
 	}
 	
 	@Override
 	public FlashCard post(FlashCard entity) {
 		entity.setTags(configureTags(entity.getTags()));
 		
 		return super.post(entity);
 	}
 	
 	private Set<Tag> configureTags(Set<Tag> tags) {
 
 		Set<Tag> results = new HashSet<Tag>();
 		for (Tag tag : tags) {
 			// if we don't have the id of the Tag
 			if (tag.getId() == null || tag.getId() == 0) {
 				// try to get the existing Tag
 				Tag existingTag = tagService.findByName(tag.getName());
 				
 				// does the Tag exist?
 				if (existingTag != null) {
 					results.add(existingTag);
 				} else {
 					// tag doesn't exist, re-add the Tag as-as
 					results.add(tag);
 				}
 			}
 			else {
 				results.add(tag);
 			}
 		}
 		return results;
 	}
 }
