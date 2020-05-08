 import java.util.ArrayList;
 
 public class Vertex{
 	
 	String name;
 	public ArrayList<String> dependancies;
 	public boolean visited;
 	int timeStamp; 
 	int indegree;
 
 	public Vertex(String name){
 		dependancies = new ArrayList<String>();
 		this.name = name;
 		this.timeStamp = 0;
 		this.indegree = 0;
 		this.visited = false;
 	}
 
 	public String getName(){
 		return this.name;
 	}
 
 	public void addAdj(String adj){
 
 		dependancies.add(adj);
 	}
 
 	@Override
 	public String toString(){
 	
 		return(name +" : "+ dependancies +"\n");
 
 	}
 
 	public boolean isBasic(){
 		return (dependancies.size() == 0);
 	}
 
 	public void setTime( int time ){
 		timeStamp = time;
 	}
 }
