 package semantic;
 
import java.util.HashMap;

 import syntax.Value;
 
public class Block implements Cloneable{
 	Block father;
 	Map<Value> memory;
 
 	public Block() {
 		father = null;
 		memory = new Map<Value>();
 	}
 
 	public void setFather(Block f) {
 		father = f;
 	}
 
 	public Block getFather() {
 		return father;
 	}
 
 	public Block(Block f) {
 		father = f;
 		memory = new Map<Value>();
 	}
 
 	public Value getValue(String id) {
 		return memory.query(id);
 	}
 
 	public void onion(String id, Value v) {
 		memory.onion(id, v);
 	}
	
	public String toString(){
		return memory.map.toString();
	}
	
	public Block clone(){
		Block nb = new Block();
		nb.memory.map = (HashMap<String, Value>) this.memory.map.clone();
		return nb;
	}
 
 }
