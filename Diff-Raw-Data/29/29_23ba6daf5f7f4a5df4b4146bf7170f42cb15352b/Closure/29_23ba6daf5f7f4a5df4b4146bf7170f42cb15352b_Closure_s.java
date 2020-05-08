 
 public class Closure extends Node{
 	private Node args, code;
 	private ReferenceFrame ref;
 	
 	public Closure(Node args, Node code, ReferenceFrame ref) {
 		this.args = args;
 		this.ref = ref;
		this.code = code;	
 	}
 	
 	public Node getCode() {
 		return code;
 	}
 	
 	public Node getArgs() {
 		return args;
 	}
 	
 	public ReferenceFrame getRef() {
 		return ref;
 	}
 }
