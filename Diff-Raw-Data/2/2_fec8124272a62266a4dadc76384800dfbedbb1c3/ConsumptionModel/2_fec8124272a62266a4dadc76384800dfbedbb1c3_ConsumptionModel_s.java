 package ch.hsr.bieridee.models;
 
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.NotFoundException;
 
 import ch.hsr.bieridee.config.NodeProperty;
 import ch.hsr.bieridee.config.NodeType;
 import ch.hsr.bieridee.domain.Consumption;
 import ch.hsr.bieridee.exceptions.WrongNodeTypeException;
 import ch.hsr.bieridee.utils.DBUtil;
 
 /**
  * Model to work with and persist the User object.
  */
 public class ConsumptionModel extends AbstractActionModel {
 
 	private Consumption domainObject;
 
 	/**
 	 * Creates an empty RatingModel, needed to create a new Rating.
 	 * 
 	 */
 	private ConsumptionModel(BeerModel beerModel, UserModel userModel) {
 		super(NodeType.CONSUMPTION, beerModel, userModel);
 		this.domainObject = new Consumption(new Date(System.currentTimeMillis()), beerModel.getDomainObject(), userModel.getDomainObject());
 		this.setDate(this.domainObject.getDate());
 	}
 
 	/**
 	 * Creates a UserModel, consisting from a User domain object and the corresponding Node.
 	 * 
 	 * @param node
 	 *            Ratingnode
 	 * @throws NotFoundException
 	 *             Thrown if the given node can not been found
 	 * @throws WrongNodeTypeException
 	 *             Thrown if the given node is not of type user
 	 */
 	public ConsumptionModel(Node node) throws NotFoundException, WrongNodeTypeException {
 		super(NodeType.CONSUMPTION, node);
 		this.domainObject = new Consumption(this.date, this.beerModel.getDomainObject(), this.userModel.getDomainObject());
 	}
 
 	public Consumption getDomainObject() {
 		return this.domainObject;
 	}
 
 	// SUPPRESS CHECKSTYLE: setter
 	public void setDate(Date d) {
 		this.domainObject.setDate(d);
 		DBUtil.setProperty(this.node, NodeProperty.Rating.TIMESTAMP, d.getTime());
 	}
 
 	/**
 	 * Creates a new user and returns a new UserModel for it.
 	 * 
 	 * @param beerModel
 	 *            The BeerModel.
 	 * @param userModel
 	 *            The UserModel.
 	 * @return The UserModel containing the new user node and the user domain object
 	 */
 	public static ConsumptionModel create(BeerModel beerModel, UserModel userModel) {
 		return new ConsumptionModel(beerModel, userModel);
 	}
 
 	/**
 	 * Gets all consumptions for the given beer.
 	 * 
 	 * @param beerId
 	 *            The beer to be filterd with
 	 * @return A list of ConsumptionModels for the given beer
 	 * @throws NotFoundException
 	 *             Thrown if a node could not be found
 	 * @throws WrongNodeTypeException
 	 *             Thronw if a node has the wrong type
 	 */
 	public static List<ConsumptionModel> getAll(long beerId) throws NotFoundException, WrongNodeTypeException {
 		return createModelsFromNodes(DBUtil.getConsumptionsByBeer(beerId));
 	}
 
 	/**
	 * Gets all consumptions of a specific user for thas he given beer.
 	 * 
 	 * @param beerId
 	 *            The beer to be filterd with
 	 * @param username
 	 *            The drinker
 	 * @return List of ConsumptionModels for the user and the beer
 	 * @throws NotFoundException
 	 *             Thrown if a node could not be found
 	 * @throws WrongNodeTypeException
 	 *             Thronw if a node has the wrong type
 	 */
 	public static List<ConsumptionModel> getAll(long beerId, String username) throws NotFoundException, WrongNodeTypeException {
 		return createModelsFromNodes(DBUtil.getConsumptionsForUserByBeer(username, beerId));
 	}
 
 	private static List<ConsumptionModel> createModelsFromNodes(List<Node> nodes) throws NotFoundException, WrongNodeTypeException {
 		final List<ConsumptionModel> models = new LinkedList<ConsumptionModel>();
 		for (Node node : nodes) {
 			models.add(new ConsumptionModel(node));
 		}
 		return models;
 	}
 
 	@Override
 	public String toString() {
 		return this.domainObject.toString();
 	}
 
 }
