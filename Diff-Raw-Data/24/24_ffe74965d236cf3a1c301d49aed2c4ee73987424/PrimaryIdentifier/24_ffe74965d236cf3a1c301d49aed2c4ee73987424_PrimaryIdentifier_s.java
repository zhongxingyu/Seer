 /**
  * Identifier
  */
 package translator;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import xtc.tree.GNode;
 import xtc.tree.Visitor;
 
 public class PrimaryIdentifier extends Expression implements Translatable {
 
   public String identifier;
 
   public PrimaryIdentifier(GNode n) {
     identifier = n.getString(0);
   }
   
   public String getCC(int indent, String className, List<Variable> variables) {
    return identifier;
   }
   
 }
