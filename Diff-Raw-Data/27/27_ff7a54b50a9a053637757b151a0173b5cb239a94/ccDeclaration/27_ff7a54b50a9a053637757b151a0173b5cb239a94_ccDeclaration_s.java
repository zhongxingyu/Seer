package oop;
 
 import xtc.tree.GNode;
 import xtc.tree.Node;
 
 public class ccDeclaration {
 	private String modifiers="";
 	private String types="";
 	private String name="";
 	private String declaration="";
 	private String value="";
 	
 	public ccDeclaration(GNode n) {
 		modifiers = extract((GNode)n.get(0));
 		types = extract((GNode)n.get(1));
		if(types == "boolean")
			types = "bool";
 		treatDeclarator((GNode) ((GNode) n.get(2)).get(0));
 //		System.out.println(this.publish());					//To be added to ccBlock
 	}
 	
 	/*
 	 * treatDeclarator is a special method to deal with declarator, an aspect of every declaration. To my knowledge, declarator contains 3
 	 * nodes: the declared's name, a middle value (which is still a slight mystery) and a value for the declared. Value could be a full statement
 	 * so a ccStatement object is declared, only to be published and forgotten. Should a new object be declared (like so?) or is that a poor technique?
 	 */
 
 	private void treatDeclarator(GNode n) {
 		name=(String)n.get(0);
 		if(n.get(2)!=null)
 			value = new ccStatement((GNode)n.get(2)).publish();
 		if(n.get(1)==null&&value!="")
 			declaration="=";
 
 	}
 	
 	public String publish(){
 		return modifiers+" "+types+" "+name+" "+declaration+" "+value+";";
 	}
 
 	public String extract(GNode n){
 		for(Object s: n){
 			if (s instanceof String){
 				return (String)s;
 			}
 			else if(s instanceof GNode){
 				return extract((GNode)s);
 			}
 		}
 		return "";
 	}
 }
