 package zinara.ast;
 
 public class Declaration {
     private Type type;
     private String identifier;
     private Object value;
     private boolean variable;
     
     public Declaration(Type t, String id, Object v, boolean var) {
 	this.type = t; this.identifier = id; this.value = v; this.variable = var;
     }
 
     public void setType(Type t) { this.type = t; }
     public void setVariable(boolean var) { this.variable = var; }
 }
