 package semantic;
 
 import syntax.Value;
 
public class Block {
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
 
 }
