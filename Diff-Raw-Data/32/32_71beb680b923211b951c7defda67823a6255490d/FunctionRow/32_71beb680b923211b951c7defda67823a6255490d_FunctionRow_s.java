 package compiler.parser.symbol;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class FunctionRow extends Row implements ModuleRow {
 
     private List<Attribute> attributes;
     private String branchLbl;
    
     public FunctionRow(String lexeme, Classification classification, Type returnType, List<Attribute> attributes, String branchLbl) {
         super(lexeme, classification);
         setType(returnType);
         this.attributes = new ArrayList<Attribute>();
         this.attributes.addAll(attributes);
         this.branchLbl = branchLbl;
     }
 
     @Override
     public String getBranchLabel() {
         return branchLbl;
     }
 
     public void addAttribute(Attribute attribute) {
         attributes.add(attribute);
     }
 
     public Type getReturnType() {
         return getType();
     }
 
     public void setReturnType(Type returnType) {
         setType(returnType);
     }
 
     public List<Attribute> getAttributes()
     {
         return attributes;
     }
 
     @Override
     public void print() {
         System.out.print("Function: " + getLexeme() + "\tBranch Label: " + getBranchLabel() + "\tformal attributes: {");
         boolean first = true;
         for (Attribute a : attributes)
         {
             if(!first)
             {
                 System.out.print(", "); 
             }
             else
             {
                 first = false;
             }
             a.print();
         }
         System.out.println("}\treturn type: " + getReturnType());
     }
 }
