 package structures;
 
 import java.util.LinkedList;
 
 public class Vertex {
 	
 	private int num = 0;
 	private static int n = 0;
 	private String line;
 	private VertexType type;
 	private LinkedList<Vertex>dominators;
 
 	public Vertex(String line, VertexType type) {
 		super();
 		num = n++ ;
 		this.line = line;
 		this.type = type;
 	}
 	
 	public Vertex() {
 		num = n++;
 		this.line = "join";
 		this.type = VertexType.JOIN;
 	}
 	
 	public String getLine() {
 		return line;
 	}
 	
 	public void setLine(String line) {
 		this.line = line;
 	}
 	
 	public Boolean isCond() {
 		return this.type==VertexType.COND;
 	}
 	
 	public Boolean isCondWhile() {
 		return this.type==VertexType.CONDW;
 	}
 	
 	public void setType(VertexType t) {
 		this.type = t;
 	}
 	
 	public Boolean isDo() {
 		return this.type==VertexType.DO;
 	}
 	
 	public int getNum(){
 		return num;
 	}
 	
 	
 	public LinkedList<Vertex> getDominators() {
 		return dominators;
 	}
 
 	public void setDominators(LinkedList<Vertex> dominators) {
 		this.dominators = dominators;
 	}
 	
 	public void addDominators(Vertex v){
		if(!dominators.contains(v))
			dominators.add(v);
 	}
 
 	@Override
 	public String toString() {
 		return num+". "+line;
 	}
 
 	public boolean isBegin() {
 		return type == VertexType.BEGIN;
 	}
 	
 	public boolean isEnd(){
 		return type == VertexType.END;
 	}
 	
 
 }
