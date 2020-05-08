 /**
  * A pie to be used in an implementation of the game Bammi.
  * By clicking on the graphical representation of a pie, the
  * game will call the addSlice-method of the corresponding pie.
  */
 class Pie{
 
 	private GUI home;
 	private java.util.HashSet<Pie> neighbors; //adjacent pies
 	private int maxSlices; //Neighbors.length
 	private int currentSlices;
 	private Player owner; //Pointer
 	private boolean exploding;
 
 	public Pie(GUI home){
 		this.home = home;  //Pointer
 		neighbors = new java.util.HashSet<Pie>();
 		maxSlices = 0;
 		currentSlices = 0;
 		owner = null;
 		exploding = false;
 	}
 
 	public void addSlice(Player player){
 		if(owner == null || owner == player){
 			if(currentSlices == maxSlices){
 				add(player);
 				ExplosionManager.go();
 			}
 			else{
 				add(player);
 			}
 			ifNext();
 		}
 	}
 	
 	private void add(Player player){
 		if(owner == null){
 			player.addPie();
 			Game.removeUnownedPie();
 			owner = player;
 		}
 		else if(owner != player){ //triggered by explosion
 			owner.removePie();
 			owner.checkLoss();
 			player.addPie();
 			owner = player;
 		}
 		currentSlices++;
 		if(currentSlices > maxSlices) lightFuse(player);
 		home.repaint();
 	}
 	
 	private void ifNext(){
 		if(ExplosionManager.isEmpty()){
 			Game.nextPlayer();
 		}
 	}
 	
 	public void addNeighbor(Pie p){
 		if(neighbors.add(p)) maxSlices++;
 	}
 	
 	public void removeNeighbor(Pie p){
 		if(neighbors.remove(p)) maxSlices--;
 	}
 	
 	public Player getOwner(){
 		return owner;
 	}
 	
 	public int maxSlices(){
 		return maxSlices;
 	}
 	
 	public int currentSlices(){
 		if(exploding){
 			return maxSlices;
 		}
 		return currentSlices;
 	}
 	
 	public boolean exploding(){
 		return exploding;
 	}
 
 	private void lightFuse(Player player){
 		currentSlices = 1;
 		owner = player;
 		exploding = true;
 		ExplosionManager.add(this);
 	}
 
 	public void explode(Player player){
 		for(Pie neighbor :neighbors){
 			neighbor.add(player);
 		}
 		home.repaint();
 		exploding = false;
 		ExplosionManager.go();
 		ifNext();
 	}
 }
