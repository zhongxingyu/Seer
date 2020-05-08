 package controllers;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map.Entry;
 
 import models.Card;
 
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.node.ObjectNode;
 
 import play.db.ebean.Transactional;
 import play.libs.Json;
 import play.mvc.BodyParser;
 import play.mvc.Controller;
 import play.mvc.Result;
 import flexjson.JSONSerializer;
 
 /**
  * This controller is supposed to expose the Cards REST API.
  * 
  * @author Tiago Garcia
  * @see http://github.com/tiagorg
  */
 public class Cards extends Controller {
 
 	private static final String PARENT_ID = "parentId";
 	private static final JSONSerializer cardSerializer = new JSONSerializer()
 			.include("children").include("children.children");
 
 	/**
 	 * Retrieves all projects and return OK (200) with the cards as JSON.
 	 * 
 	 * @return the result
 	 */
 	public static Result getAllProjects() {
 		List<Card> projects = Card.allProjects();
 		for (Card project : projects) {
 			List<Card> stories = project.getChildren();
 			for (int i = 0; i < stories.size(); i++) {
 				stories.set(i, Card.byId(stories.get(i).getId()));
 			}
 		}
 		return ok(cardSerializer.serialize(projects));
 	}
 
 	/**
 	 * Searches for a card and return OK (200) with the card as JSON if
 	 * successful, or NOT_FOUND (404) otherwise.
 	 * 
 	 * @param id
 	 *           the card id
 	 * @return the result
 	 */
 	public static Result get(Long id) {
 		Result result = null;
 
 		Card card = Card.byId(id);
 		if (card != null) {
 			result = ok(cardSerializer.serialize(card));
 		} else {
 			result = notFound();
 		}
 		return result;
 	}
 
 	/**
 	 * Creates a card from the request body and return OK (200) if successful, or
 	 * BAD_REQUEST (400) otherwise.
 	 * 
 	 * @return the result
 	 */
 	@BodyParser.Of(BodyParser.Json.class)
 	@Transactional
 	public static Result create() {
 		ObjectMapper mapper = new ObjectMapper();
 		Result result = null;
 		Card card = null;
 
 		try {
 			ObjectNode objectNode = (ObjectNode) request().body().asJson();
 
 			// Removing empty fields
 			Iterator<Entry<String, JsonNode>> fieldIterator = objectNode
 					.getFields();
 			while (fieldIterator.hasNext()) {
 				Entry<String, JsonNode> fieldEntry = fieldIterator.next();
 				if (fieldEntry.getValue().asText().isEmpty()) {
 					objectNode.remove(fieldEntry.getKey());
 				}
 			}
 
 			if (objectNode.has(PARENT_ID)) {
 				Long parentId = objectNode.get(PARENT_ID).asLong();
 				Card parentCard = Card.byId(parentId);
 				objectNode.remove(PARENT_ID);
 
 				card = mapper.readValue(objectNode, Card.class);
 				card.setParent(parentCard);
 			} else {
 				card = mapper.readValue(objectNode, Card.class);
 			}
 
 			Card.create(card);
 
 			result = ok(cardSerializer.serialize(card));
 		} catch (Exception e) {
 			result = badRequest();
 		}
 		return result;
 	}
 
 	/**
 	 * Updates a card from the request body and return OK (200) with the card as
 	 * JSON if successful, NO_CONTENT (204) if there was no change and NOT_FOUND
 	 * (404) otherwise.
 	 * 
 	 * @param id
 	 *           the card id
 	 * @return the return
 	 */
 	@BodyParser.Of(BodyParser.Json.class)
 	@Transactional
 	public static Result update(Long id) {
 		ObjectMapper mapper = new ObjectMapper();
 		Result result = null;
 
 		try {
 			JsonNode jsonNode = request().body().asJson();
 			Card cardFromPersistence = Card.byId(id);
 			Card cardFromJson = mapper.readValue(jsonNode, Card.class);
 
 			if (cardFromPersistence.getStatus().equals(cardFromJson.getStatus()) == false) {
 				cardFromPersistence.setStatus(cardFromJson.getStatus());
 				Card.update(cardFromPersistence);
				cardFromPersistence.setParent(null);
 				result = ok(cardSerializer.serialize(cardFromPersistence));
 			} else {
 				result = noContent();
 			}
 		} catch (Exception e) {
 			result = badRequest();
 		}
 		return result;
 	}
 
 	/**
 	 * Deletes a card and return OK (200) if successful, or NOT_FOUND (404)
 	 * otherwise.
 	 * 
 	 * @param id
 	 *           the card id
 	 * @return the return
 	 */
 	@Transactional
 	public static Result delete(Long id) {
 		Result result = null;
 
 		try {
 			Card.delete(id);
 			result = ok(Json.toJson(id));
 		} catch (Exception e) {
 			result = badRequest();
 		}
 		return result;
 	}
 
 }
