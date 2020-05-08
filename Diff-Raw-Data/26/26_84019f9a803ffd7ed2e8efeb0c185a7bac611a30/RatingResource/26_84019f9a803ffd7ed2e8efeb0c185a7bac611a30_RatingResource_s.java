 package ch.hsr.bieridee.resourcehandler;
 
 import java.io.IOException;
 
 import org.apache.commons.lang.NotImplementedException;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.neo4j.graphdb.NotFoundException;
 import org.neo4j.server.rest.web.NodeNotFoundException;
 import org.restlet.representation.Representation;
 import org.restlet.representation.StringRepresentation;
 import org.restlet.resource.ServerResource;
 
 import ch.hsr.bieridee.config.Res;
 import ch.hsr.bieridee.exceptions.WrongNodeTypeException;
 import ch.hsr.bieridee.models.BeerModel;
 import ch.hsr.bieridee.models.RatingModel;
 import ch.hsr.bieridee.models.UserModel;
 import ch.hsr.bieridee.resourcehandler.interfaces.IStoreResource;
 import ch.hsr.bieridee.utils.Cypher;
 import ch.hsr.bieridee.utils.Cypherqueries;
 import ch.hsr.bieridee.utils.DBUtil;
 import ch.hsr.bieridee.utils.NodeProperty;
 
 /**
  * Server resource to provide access to users.
  */
 public class RatingResource extends ServerResource implements IStoreResource {
 
 	private String username;
 	private long beerId;
 
 	@Override
 	public void doInit() {
 		this.username = (String) getRequestAttributes().get(Res.USER_REQ_ATTR);
 		this.beerId = Long.parseLong((String) (getRequestAttributes().get(Res.BEER_REQ_ATTR)));
 	}
 
 	@Override
 	public Representation retrieve() throws WrongNodeTypeException, NodeNotFoundException {
 		return new StringRepresentation("not implemented");
 
 	}
 
 	@Override
 	public void store(Representation rating) throws JSONException, IOException, NotFoundException, WrongNodeTypeException {
 
 		final JSONObject ratingJSON = new JSONObject(rating.getText());
 		final int ratingValue = ratingJSON.getInt(NodeProperty.Rating.RATING);
 		final BeerModel beerModel = new BeerModel(this.beerId);
 		final UserModel userModel = new UserModel(this.username);
 
 		RatingModel.create(ratingValue, beerModel, userModel);
		
		DBUtil.setProperty(beerModel.getNode(), NodeProperty.Beer.AVERAGE_RATING, Cypher.executeAndGetDouble(Cypherqueries.GET_AVERAGE_RATING_OF_BEER, "AverageRating", beerModel.getId()+"", Long.toString(this.beerId)));

 	}
 
 	@Override
 	public void remove(Representation rep) {
 		throw new NotImplementedException(); // TODO
 	}
	
 }
