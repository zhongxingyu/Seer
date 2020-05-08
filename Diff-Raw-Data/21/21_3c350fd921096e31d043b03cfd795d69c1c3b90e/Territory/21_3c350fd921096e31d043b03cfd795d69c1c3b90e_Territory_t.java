 package warborn.model;
 
 import java.util.ArrayList;
 
 public class Territory {
 	
 	private ArrayList <Territory> connections;
 	private Player owner;
 	private int nbrOfUnits = 0, id;
 	private String name;
 	
	public Territory(String name, int id){
 		this.id = id;
		connections = new ArrayList<Territory>();
 		owner = null;
 		this.name = name;
 	}
 	
 	public String getName(){
 		return name;
 	}
 
 	private Object getId() {
 		return id;
 	}
 	
 	public Player getOwner(){
 		return owner;
 	}
 	
 	public void setOwner(Player newOwner){
 		this.owner = newOwner;
 	}
 	
 	public int getNbrOfUnits(){
 		return this.nbrOfUnits;
 	}
 	
 	public void setNbrOfUnits(int nbrOfUnits){
 		this.nbrOfUnits = nbrOfUnits;
 	}
 	
 	public boolean hasConnection(Territory compareTo){
 		if(connections == null){
 			return false;
 		}
 		for(int i = 0; i < connections.size(); i++){
 			if(connections.get(i).equals(compareTo)){
 				return true;
 			}
 		}
 		return false;
 	}
 	
	public void addConnection(Territory toAdd){
		connections.add(toAdd);
 	}
 	
 
 	@Override
 	public boolean equals(Object rhs){
 		if(rhs == null || !(rhs.getClass() == this.getClass())){
 			return false;
 		}
 		Territory toCompare = (Territory)rhs;
 		return this.getId() == toCompare.getId();
 	}
 	
 	
 }
