 package projectrts.model.entities;
 
 import projectrts.model.constants.P;
 import projectrts.model.pathfinding.Node;
 import projectrts.model.pathfinding.World;
 import projectrts.model.utils.Position;
 
 /**
  * Abstract class for the common parts of the different entities
  * @author Filip Brynfors, Modified by Markus Ekstrm, Jakob Svensson, Bjorn Persson Mattsson
  *
  */
 public abstract class AbstractEntity implements IEntity {
 
 	private Position position;
 
 	private String name;
 	private int entityID;
 	private World world;
 	private float size;
 	private float speed;
 
 	/**
 	 * When subclassing, invoke this to initialize the entity.
 	 * @param spawnPos The initial position of the entity.
 	 */
 	protected void initialize(Position spawnPos) {
 		this.entityID = EntityManager.getInstance().requestNewEntityID();
 		
 		this.world = World.getInstance();
 
 		this.position = spawnPos.copy();
 	}
 	
 	protected void setSize(float size){
 		this.size=size*P.INSTANCE.getUnitLength();
 		occupyNodes(world.getNodeAt(this.getPosition()));
 	}
 	protected void setSpeed(float speed){
 		this.speed=speed*P.INSTANCE.getUnitLength();
 	}
 	protected void setName(String name) {
 		this.name = name;
 	}
 	
 	@Override
 	public float getSize() {
 		return size;
 	}
 	
 	@Override
 	public float getSpeed() {
 		return speed;
 	}
 
 	@Override
 	public String getName() {
 		return name;
 	}
 	
 	@Override
 	public Position getPosition() {
 		return position;
 	}
 	
 	public int getEntityID()
 	{
 		return entityID;
 	}
 	
 	/**
 	 * Sets the position of the entity
 	 * @param pos the new position
 	 */
 	public void setPosition(Position pos){
 		position = pos.copy();
 	}
 	
 	private void occupyNodes(Node newNode)
 	{
 		world.setNodesOccupied(newNode, getSize(), getEntityID());
 	}
 
 	/*
 	private void enterNewNode(Node newNode)
 	{
		// Plankton: Can this be done better?
 		if (occupiedNode == null)
 		{
 			world.setNodesOccupied(newNode, getSize(), getEntityID());
 			occupiedNode = newNode;
 		}
 		if (!occupiedNode.equals(newNode))
 		{
 			world.setNodesOccupied(occupiedNode, getSize(), 0);
 			world.setNodesOccupied(newNode, getSize(), getEntityID());
 			occupiedNode = newNode;
 		}
 	}
 	*/
 	
 	/**
 	 * Updates the unit.
 	 * @param tpf Time per frame
 	 */
 	public abstract void update(float tpf);
 }
