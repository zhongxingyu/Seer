 
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Random;
 
 public class Agent {
 
 	public String name;
 	public int id;
 	public double opinion;
 	public double mu;
 	public ArrayList<Agent> neighbors;
 	public Group group;
 	public ArrayList<Agent> external;
 	public HashSet<Group> exGroups;
 	Random gen = new Random();
 	private double migrationProbTotal = 0; //Denominator for the probability of group changes
 	
 	public Agent(int id, String name, double opinion, double mu) {
 		super();
 		this.id = id;
 		this.name = name;
 		this.opinion = opinion;
 		this.neighbors = new ArrayList<Agent>();
 		this.mu = mu;
 		this.exGroups = new HashSet<Group>();
 		this.external = new ArrayList<Agent>();
 	}
 	
 	public int getId() {
 		return id;
 	}
 
 	public double getOpinion() {
 		return opinion;
 	}
 	
 	public void setOpinion(Double opinion) {
 		this.opinion = this.opinion + this.mu * (opinion - this.opinion);
 		if(this.opinion > 1)this.opinion = 1;
 		else if(this.opinion<0)this.opinion = 0;
 	}
 	
 	public void redOpinion(Double opinion){
 		this.opinion = this.opinion - this.mu * (opinion - this.opinion);
 		if(this.opinion < 0){
 			this.opinion = 0;
 		}
 		else if(this.opinion > 1){
 			this.opinion = 1;
 		}
 	}
 	
 	public String getName() {
 		return name;
 	}
 
 	public ArrayList<Agent> getNeighbors() {
 		return neighbors;
 	}
 	
 	public ArrayList<Agent> getExternalNeighbors() {
 		return external;
 	}
 	
 	public int getNumExternalNeighbors() {
 		return external.size();
 	}
 	
 	public boolean hasNeighbor(Agent a) {
 		//return neighbors.contains(a);
 		if(this.group.getAgents().contains(a)){
 			return true;
 		}
 		else if(this.external.contains(a)){
 			return true;
 		}
 		else {return false;}
 	}
 	
 	public boolean hasExternalNeighbor(Agent a) {
 		return external.contains(a);
 	}
 
 	public void setNeighbors(ArrayList<Agent> arrayList) {
 		this.neighbors = new ArrayList<Agent>(arrayList);
 		//this.neighbors.clear();
 	//	this.neighbors.addAll(neighbors);
 	}
 	
 	public void addNeighbor(Agent neighbor) {
 		if(neighbor != this){
 			this.neighbors.add(neighbor);
 			if(neighbor.getGroup() != this.getGroup()){
 				this.external.add(neighbor);
 				if(!exGroups.contains(neighbor.getGroup())){
 					exGroups.add(neighbor.getGroup());
 				}
 			}
 		}
 	}
 	
 	public void removeNeighbor(Agent neighbor) {
 		this.neighbors.remove(neighbor);
 		if(this.getGroup() != neighbor.getGroup()){
 			this.external.remove(neighbor);
 			//calcEx();
 		}
 	}
 	
 	public void setGroup(Group group){
 		this.group = group;
 	}
 	public Group getGroup(){
 		return this.group;
 	}
 	public void resetExternal(){
 		this.external.clear();
 		calcEx();
 	}
 	public void calcEx(){ //private function to calculate external groups we are connected to
 		exGroups.clear();
 		for(Agent a : this.external){
 			if(!this.exGroups.contains(a.getGroup())){
 				this.exGroups.add(a.getGroup());
 			}
 		}
 	}
 	private void calcProb(){
 		this.migrationProbTotal = 0;
 		for(Group a : this.exGroups){
 			this.migrationProbTotal += 1 - Math.abs(this.opinion - a.getavg());
 		}
 	}
 	public double prob(){
 		this.calcProb();
 		return this.migrationProbTotal;
 	}
 	public Agent nSelection(){
 		int numneigh = this.group.getAgents().size() - 1 + this.external.size();
 		int select = gen.nextInt(numneigh+1);
 		if(select > this.group.getAgents().size() - 1){
 			return this.external.get(gen.nextInt(this.external.size()));
 		}
 		else if(this.group.getAgents().size() == 1){
 			return this.external.get(gen.nextInt(this.external.size()));
 		}
 		else{
			if(select >= this.group.getAgents().size()){
				select =- this.group.getAgents().size();
 			}
 			if(this.group.getAgents().get(select) == this){
 				return this.nSelection();
 			}
 			return this.group.getAgents().get(select);
 		}
 	}
 	
 }
