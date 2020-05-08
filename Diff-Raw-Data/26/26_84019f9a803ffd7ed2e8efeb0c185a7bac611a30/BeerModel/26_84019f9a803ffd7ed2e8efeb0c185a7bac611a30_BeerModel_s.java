 package ch.hsr.bieridee.models;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.NotFoundException;
 import org.neo4j.graphdb.Relationship;
 
 import ch.hsr.bieridee.config.NodeType;
 import ch.hsr.bieridee.config.RelType;
 import ch.hsr.bieridee.domain.Beer;
 import ch.hsr.bieridee.domain.Beertype;
 import ch.hsr.bieridee.domain.Brewery;
 import ch.hsr.bieridee.domain.Tag;
 import ch.hsr.bieridee.exceptions.WrongNodeTypeException;
 import ch.hsr.bieridee.utils.DBUtil;
 import ch.hsr.bieridee.utils.DomainConverter;
 import ch.hsr.bieridee.utils.NodeProperty;
 import ch.hsr.bieridee.utils.NodeUtil;
 
 /**
  * Model to work and persist the beer object.
  */
 public class BeerModel extends AbstractModel {
 
 	private Beer domainObject;
 	private Node node;
 	private double averageRating;
 
 	/**
 	 * Creates a BeerModel for the desired beer.
 	 * 
 	 * @param beerId
 	 *            The node id of the desired beer
 	 * @throws WrongNodeTypeException
 	 *             Thrown when the given id does not reference a beer node
 	 * @throws NotFoundException
 	 *             Thrown when the given ide does not reference an existing node
 	 */
 	public BeerModel(long beerId) throws WrongNodeTypeException, NotFoundException {
 		this(DBUtil.getNodeById(beerId));
 	}
 
 	/**
 	 * @param node
 	 *            Node containing Properties of the Beer.
 	 * @throws WrongNodeTypeException
 	 *             Thrown when the given node is not a beer node
 	 * @throws NotFoundException
 	 *             Thrown when the given node is not existing
 	 */
 	public BeerModel(Node node) throws WrongNodeTypeException, NotFoundException {
 
 		NodeUtil.checkNode(node, NodeType.BEER);
 
 		this.node = node;
 		final long id = node.getId();
 		final String name = (String) this.node.getProperty(NodeProperty.Beer.NAME);
 		final String brand = (String) this.node.getProperty(NodeProperty.Beer.BRAND);
 		final String image = (String) this.node.getProperty(NodeProperty.Beer.IMAGE);
 		this.averageRating = (Double) this.node.getProperty(NodeProperty.Beer.AVERAGE_RATING, 0.0);
 		final List<Tag> tags = new LinkedList<Tag>();
 
 		for (Relationship r : this.node.getRelationships(RelType.HAS_TAG)) {
 			final Node nodeTag = r.getEndNode();
 			final Tag domainTag = new Tag((String) nodeTag.getProperty(NodeProperty.Beer.NAME));
 			tags.add(domainTag);
 		}
 
 		final Relationship beertypeRel = this.node.getSingleRelationship(RelType.HAS_BEERTYPE, Direction.OUTGOING);
 		final Node beertypeNode = beertypeRel.getEndNode();
 		final BeertypeModel beertypeModel = new BeertypeModel(beertypeNode);
 		final Beertype type = beertypeModel.getDomainObject();
 
 		final Relationship breweryRel = this.node.getSingleRelationship(RelType.BREWN_BY, Direction.OUTGOING);
 		final Node breweryNode = breweryRel.getEndNode();
 		final BreweryModel breweryModel = new BreweryModel(breweryNode);
 		final Brewery brewery = breweryModel.getDomainObject();
 
 		this.domainObject = new Beer(id, name, brand, image, tags, type, brewery);
 	}
 
 	public Node getNode() {
 		return this.node;
 	}
 
 	public Beer getDomainObject() {
 		return this.domainObject;
 	}
 
 	public long getId() {
 		return this.domainObject.getId();
 	}
 
 	public Beertype getBeertype() {
 		return this.domainObject.getBeertype();
 	}
 
 	public String getBrand() {
 		return this.domainObject.getBrand();
 	}
 
 	public Brewery getBrewery() {
 		return this.domainObject.getBrewery();
 	}
 
 	public String getName() {
 		return this.domainObject.getName();
 	}
 
 	public String getImage() {
 		return this.domainObject.getPicture();
 	}
 
 	public List<Tag> getTags() {
 		return this.domainObject.getTags();
 	}
	
 	public double getAverageRating() {
 		return averageRating;
 	}
 
 	// SUPPRESS CHECKSTYLE: setter
 	public void setBeertype(Beertype beertype) {
 		// TODO
 	}
 
 	// SUPPRESS CHECKSTYLE: setter
 	public void setBrand(String brand) {
 		this.domainObject.setBrand(brand);
 		this.node.setProperty(NodeProperty.Beer.BRAND, brand);
 	}
 
 	// SUPPRESS CHECKSTYLE: setter
 	public void setBrewery(Brewery brewery) {
 		// TODO
 	}
 
 	// SUPPRESS CHECKSTYLE: setter
 	public void setName(String name) {
 		this.domainObject.setName(name);
 		this.node.setProperty(NodeProperty.Beer.NAME, name);
 	}
 
 	// SUPPRESS CHECKSTYLE: setter
 	public void setImage(String path) {
 		this.domainObject.setPicture(path);
 		this.node.setProperty(NodeProperty.Beer.IMAGE, path);
 	}
 
 	/**
 	 * Links this Beer to the desired TagModel.
 	 * 
 	 * @param t
 	 *            TagModel to add.
 	 */
 	public void addTag(TagModel t) {
 		final Iterable<Relationship> existingTagRelations = this.node.getRelationships(RelType.HAS_TAG);
 		for (Relationship relationship : existingTagRelations) {
 			if (relationship.getEndNode().getProperty(NodeProperty.Tag.NAME).equals(t.getName())) {
 				return;
 			}
 		}
 		DBUtil.createRelationship(this.node, RelType.HAS_TAG, t.getNode());
 	}
 
 	// SUPPRESS CHECKSTYLE: setter
 	public void setTags(List<TagModel> tags) {
 		final List<Tag> tagDomainList = DomainConverter.extractDomainObjectFromModel(tags);
 		this.domainObject.setTags(tagDomainList);
 
 		for (TagModel t : tags) {
 			this.addTag(t);
 		}
 	}
 
 	@Override
 	public int hashCode() {
 		return this.node.hashCode();
 	}
 
 	@Override
 	public boolean equals(Object o) {
 		if (!(o instanceof BeerModel)) {
 			return false;
 		}
 		return this.node.getId() == ((BeerModel) o).getId();
 	}
 }
