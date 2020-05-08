 package de.MrX13415.Massband;
 
 import java.util.ArrayList;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.util.Vector;
 
 public class PlayerVars{
 
 	public static final int MODE_LENGTH = 0;
 	public static final int MODE_SURFACE = 1;
 	
 	private Player player;
 	private int mode = MODE_LENGTH;
 	
 	private double lenght = 0;
 	
 	private double dimensionLength = 0;
 	private double dimensionWith = 0;
 	private double dimensionHieght = 0;
 	
 	private int blockCount = 0;
 	
 	private ArrayList<Vector> wayPoints = new ArrayList<Vector>();
 	
 	private boolean ignoreHeight = true;	//Y-axe
 	
 	public PlayerVars(Player player){
 		this.player = player;
 		removeAllWayPoints();
 	}
 	
 	public void addPoint(int x, int y, int z) {
 		wayPoints.add(new Vector(x, y, z));
 	}
 	
 	public Vector getVector(int index) {
 		return wayPoints.get(index);
 	}
 	
 	public int getWayPointListSize(){
 		return wayPoints.size();
 	}	
 	
 	public void removeAllWayPoints() {
 		wayPoints.clear();
 	}
 	
 	public Player getPlayer() {
 		return player;
 	}
 	
 	public void setLenght(double lenght) {
 		this.lenght = lenght;
 	}
 	
 	public double getLenght() {
 		return lenght;
 	}
 	
 	public int getBlockCount() {
 		return blockCount;
 	}
 	
 	public double getDimensionWith() {
 		return dimensionWith;
 	}
 	
 	public double getDimensionLength() {
 		return dimensionLength;
 	}
 	
 	public double getDimensionHieght() {
 		return dimensionHieght;
 	}
 	
 	public int getMode() {
 		return mode;
 	}
 	
 	public void setMode(int mode) {
 		this.mode = mode;
 	}
 	
 	public double computingVectors(){
 		if (getWayPointListSize() >= 2) {
 			lenght = 0;
 						
 			for (int vectorIndex = 0; vectorIndex < getWayPointListSize() - 1; vectorIndex++) {
 				Vector firstV = getVector(vectorIndex);
 				Vector nextV = getVector(vectorIndex + 1);
 				
 				if (ignoreHeight){
 					firstV.setY(0);
 					nextV.setY(0);
 				}
 				
 				lenght += firstV.distance(nextV);
 			}
 			lenght += 1;	//add last point
 		}
 		return lenght;
 	}
 	
 	public void calculateDiminsion(){
 		if (ignoreHeight){
 			wayPoints.get(0).setY(0);
 			wayPoints.get(1).setY(0);
 		}
 		
 		//calculate dimensions
 		dimensionWith = Math.abs(wayPoints.get(0).getX() - wayPoints.get(1).getX()) + 1;		
 		dimensionLength = Math.abs(wayPoints.get(0).getZ() - wayPoints.get(1).getZ()) + 1;
 		dimensionHieght = Math.abs(wayPoints.get(0).getY() - wayPoints.get(1).getY()) + 1;
 
 	}
 	
 	public int countBlocks(World world){
 		int yStart = (int) Math.min(wayPoints.get(0).getY(), wayPoints.get(1).getY());
 		int yEnd = (int) Math.max(wayPoints.get(0).getY(), wayPoints.get(1).getY());
 		
 		int zStart = (int) Math.min(wayPoints.get(0).getZ(), wayPoints.get(1).getZ());
 		int zEnd = (int) Math.max(wayPoints.get(0).getZ(), wayPoints.get(1).getZ());
 	
 		int xStart = (int) Math.min(wayPoints.get(0).getX(), wayPoints.get(1).getX());
 		int xEnd = (int) Math.max(wayPoints.get(0).getX(), wayPoints.get(1).getX());
 
 		int blockCount = 0;
 		
 		for (int yIndex = yStart; yIndex <= yEnd; yIndex++) {
 			for (int zIndex = zStart; zIndex <= zEnd; zIndex++) {
 				for (int xIndex = xStart; xIndex <= xEnd; xIndex++) {
 					//get Block
 					Block b = Massband.server.getWorld(world.getName()).getBlockAt(xIndex, yIndex, zIndex);
 					//count blocks exept air ...
 					if (b.getTypeId()!= 0) blockCount++;
 				}
 			}	
 		}
 		
 		return this.blockCount = blockCount;
 	}
 	
 	/**ignors the Y axe
 	 * 
 	 * @param bool
 	 */
 	public void setignoreHeight(boolean bool) {
 		ignoreHeight = bool;
 	}
 	
 	public boolean getignoreHeight() {
 		return ignoreHeight;
 	}
 }
