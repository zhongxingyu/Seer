 package atl.space.components.collision;
 
 import java.util.List;
 
 import atl.space.components.Component;
 import atl.space.entities.Entity;
 
 public class RectObstructionComponent extends ObstructionComponent {
 	public float xSize;
 	public float ySize;
 	public float zSize;
 	//extensions will modify these values through stepping so that this class's hasCollided does not need modifying
 	
 
 	public RectObstructionComponent() {
 		super("obstruction");
 		xSize = 0;
 		ySize = 0;
 		zSize = 0;
 	}
 	
 	public RectObstructionComponent(RectObstructionComponent roc){
 		super(roc.id);
 		xSize = roc.xSize;
 		ySize = roc.ySize;
 		zSize = roc.zSize;
 	}
 	
 	public Component clone(){
 		return new RectObstructionComponent(this);
 	}
 	
 	public boolean isInHitbox(Vector3f position){
 		return isWithinSize(position.x, 1) && isWithinSize(position.y, 2) && isWithinSize(position.z, 3);
 		//TODO check if works?
 	}
 	
 	
 	//TODO replace owner.position with getCenter
 	protected Vector3f getCenter(){
 		return owner.position;
 	}
 	
 	/*
 	*	dim:
 	*	x = 1, y = 2, z = 3
 	*
 	*/
 	private boolean isWithinSize(float coord, int dim){
 		float tempRelative;
 		switch(dim){
 			case 1:
 				tempRelative = coord - getCenter().x;
 				return (tempRelative < xSize/2) && (tempRelative > -xSize/2);
 			case 2:
 				tempRelative = coord - getCenter().y;
 				return (tempRelative < ySize/2) && (tempRelative > -ySize/2);
 			case 3:
 				tempRelative = coord - getCenter().z;
 				return (tempRelative < zSize/2) && (tempRelative > -zSize/2); 
 			default:
				return null;
 		}	
 	}
 	
 	@Override
 	public boolean hasCollided(Entity e, int delta, List<Entity> entities) {
 		//TODO make this less stupid and use isInHitbox if possible
 		//Take advantage of getLastPos and getNextPos or whatever from MovementComponent, if applicable
 		//basically everything here's gotta go. scrap getStepped, might not need coords either
 		Coord minX, maxX, minY, maxY, minZ, maxZ;
 		Entity steppedThis = owner.getStepped(delta, entities);
 		Entity steppedOther = e.getStepped(delta, entities);
 		RectObstructionComponent otherComp = (RectObstructionComponent) steppedOther
 				.getComponent("obstruction");
 		RectObstructionComponent thisComp = (RectObstructionComponent) steppedThis
 				.getComponent("obstruction");
 
 		if (owner.position.x >= e.position.x) {
 			minX = new Coord(steppedOther.position.x, otherComp.xSize);
 			maxX = new Coord(steppedThis.position.x, thisComp.xSize);
 		} else {
 			maxX = new Coord(steppedOther.position.x, otherComp.xSize);
 			minX = new Coord(steppedThis.position.x, thisComp.xSize);
 		}
 		if (owner.position.y >= e.position.y) {
 			minY = new Coord(steppedOther.position.y, otherComp.ySize);
 			maxY = new Coord(steppedThis.position.y, thisComp.ySize);
 		} else {
 			maxY = new Coord(steppedOther.position.y, otherComp.ySize);
 			minY = new Coord(steppedThis.position.y, thisComp.ySize);
 		}
 		if (owner.position.z >= e.position.z) {
 			minZ = new Coord(steppedOther.position.z, otherComp.zSize);
 			maxZ = new Coord(steppedThis.position.z, thisComp.zSize);
 		} else {
 			maxZ = new Coord(steppedOther.position.z, otherComp.zSize);
 			minZ = new Coord(steppedThis.position.z, thisComp.zSize);
 		}
 		if ((minX.value + minX.obsSize / 2 >= maxX.value - maxX.obsSize / 2)
 				&& (minY.value + minY.obsSize / 2 >= maxY.value - maxY.obsSize
 						/ 2)
 				&& (minZ.value + minZ.obsSize / 2 >= maxZ.value - maxZ.obsSize
 						/ 2)) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	@Override
 	public boolean isTouching(Entity e) {
 		// TODO Make this work XD Basically checks if hitboxes intersect, same thingish as hasCollided but static timeframe
 		return false;
 	}
 
 	@Override
 	public void update(int delta, List<Entity> entities) {
 		// do nothing?
 	}
 	
 	protected class Coord {
 		public double value;
 		public double obsSize;
 
 		public Coord(double v, double s) {
 			value = v;
 			obsSize = s;
 		}
 	}
 
 }
