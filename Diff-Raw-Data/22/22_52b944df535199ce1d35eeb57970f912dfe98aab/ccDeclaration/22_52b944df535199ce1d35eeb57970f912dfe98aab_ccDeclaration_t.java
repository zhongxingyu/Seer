 package oop;
 
 import oop.ccStatement;
 import xtc.tree.GNode;
 import xtc.tree.Node;
 
 public class ccDeclaration {
 	private String modifiers="";
 	private String types="";
 	private String name="";
 	private String declaration="";
 	private String value="";
 	private ccBlock block;
 	
 	public ccDeclaration(GNode n, ccBlock b) {
 		modifiers = extract((GNode)n.get(0));
 		modifiers = modifiers.replace("final", "const");
 		types = new ccStatement(n.getGeneric(1),block).publish();
 		types = new ccHelper().convertType(types);
 		block = b;
 		treatDeclarator((GNode) ((GNode) n.get(2)).get(0));
 	}
 	
 	/*
 	 * treatDeclarator is a special method to deal with declarator, an aspect of every declaration. To my knowledge, declarator contains 3
 	 * nodes: the declared's name, a middle value (which is still a slight mystery) and a value for the declared. Value could be a full statement
 	 * so a ccStatement object is declared, only to be published and forgotten. Should a new object be declared (like so?) or is that a poor technique?
 	 */
 
 	private void treatDeclarator(GNode n) {
 		name=n.getString(0);
 		if(n.get(2)!=null){
 			if(block!=null){
 				value = new ccStatement((GNode)n.get(2), block).publish();
 			}
 			else{
 				value = new ccStatement((GNode)n.get(2), block).publish();
 			}
 		}
 		if(n.getNode(2)!=null&&n.getNode(2).hasName("StringLiteral")){
 			value = "new __String("+n.getNode(2).getString(0)+")";
 		}
		if((n.get(1)==null||n.getNode(1).hasName("Dimensions"))&&value!="")
 			declaration="=";
 
 	}
	public void changeTypeTo(String newType){
		types = newType;
	}
 	public String publish(){
 		return modifiers+types+ " "+name+" "+declaration+" "+value+";";
 	}
 
 	public String publishShort(){
 		return name+" "+declaration+" "+value+";\n";
 	}
 	public String getModifiers(){
 		return modifiers;
 	}
 	public boolean declaresToValue(){
 		return declaration.contains("=");
 	}
 	public String getTypes(){
 		return types;
 	}
 	public String extract(GNode n){
 		String modifiers = "";
 		for(Object s: n){
 			if (s instanceof String){
 				modifiers+=(String)s+" ";
 			}
 			else if(s instanceof GNode){
 				modifiers+= extract((GNode)s);
 			}
 		}
 		return modifiers;
 	}
 }
