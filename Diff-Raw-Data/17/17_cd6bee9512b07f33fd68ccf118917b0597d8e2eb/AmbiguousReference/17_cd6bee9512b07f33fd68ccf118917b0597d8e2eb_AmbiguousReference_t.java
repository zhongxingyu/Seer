 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package errorHandling;
 
 import java.util.ArrayList;
 import java.util.List;
 import symbolTable.namespace.DefinesNamespace;
 import symbolTable.namespace.MemberElementInfo;
 import symbolTable.namespace.Namespace;
 import symbolTable.namespace.TypeDefinition;
 import symbolTable.types.Type;
 
 /**
  *
  * @author kostas
  */
 public class AmbiguousReference extends ErrorMessage {
     
     private List<String> lines_errors = new ArrayList<String>();

     private String referenced_type;

     private DefinesNamespace last_valid;

     private String referenced_err;

     private String final_err;

     boolean isPending = false;
     
     public AmbiguousReference(List<TypeDefinition> candidatesTypes, List<Namespace> candidateNameSpaces, List<? extends MemberElementInfo<? extends Type>> candidateFields, String referenced_name){
         super("");
         this.referenced_type = referenced_name;
         boolean typesEmpty, namespacesEmpty = false;
         
         
         if((typesEmpty = candidatesTypes.isEmpty()) == false){
             TypeDefinition firstClass = candidatesTypes.get(0);
             lines_errors.add(firstClass.getFileName() + " line " + firstClass.getLine() + ":" + firstClass.getPosition()
                                     + " error: candidates are: " + firstClass.toString() + "\n");
             for(int i = 1 ; i < candidatesTypes.size() ; ++i){
                 TypeDefinition _class = candidatesTypes.get(i);
                 lines_errors.add(_class.getFileName() + " line " + _class.getLine() + ":" + _class.getPosition()
                                                     + " error:                 " + _class.toString() + "\n");
             }
         }
         
         if(typesEmpty && (namespacesEmpty = candidateNameSpaces != null ? candidateNameSpaces.isEmpty() : true) == false){
             Namespace firstNamespace = candidateNameSpaces.get(0);
             this.lines_errors.add(firstNamespace.getFileName() + " line " + firstNamespace.getLine() + ":" + firstNamespace.getPos()
                                   + " error: candidates are: " + firstNamespace + "\n");
         }
 
         for(int i = (typesEmpty && !namespacesEmpty) ? 1 : 0 ; i < candidateNameSpaces.size() ; ++i){
             Namespace namespace = candidateNameSpaces.get(i);
             this.lines_errors.add(namespace.getFileName() + " line " + namespace.getLine() + ":" + namespace.getPos()
                                  + " error:                 " + namespace + "\n");
         }
         
         if(typesEmpty && namespacesEmpty){
             MemberElementInfo<? extends Type> firstField = candidateFields.get(0);
             this.lines_errors.add(firstField.getFileName() + " line " + firstField.getLine() + ":" + firstField.getPos()
                                   + " error: candidates are: " + firstField.getElement().toString(referenced_name) + "\n");
         }
         
         for(int i = (typesEmpty && namespacesEmpty) ? 1 : 0 ; i < candidateFields.size() ; ++i){
             MemberElementInfo<? extends Type> memberInf = candidateFields.get(i);
             lines_errors.add(memberInf.getFileName() + " line " + memberInf.getLine() + ":" + memberInf.getPos()
                              + " error:                 " + memberInf.getElement().toString(referenced_name) + "\n");
         }
     }
     
     public void setLastValid(DefinesNamespace last_valid){
         this.last_valid = last_valid;
     }
     
     @Override
     public String getMessage(){
         StringBuilder rv = new StringBuilder();
         
         for(String line : lines_errors){
             rv.append(line);
         }
         
         return rv.toString();
     }
     
     public String getRefError(){
         return this.referenced_err;
     }
     
     public String getLastLine(){
         return this.final_err;
     }
     
     public void referenceTypeError(int line, int pos){
         this.referenced_err = "line " + line + ":" + pos + " error: reference to '" + this.referenced_type +"' is ambiguous";
     }
     
     public void finalizeErrorMessage(int line, int pos){
         String msg = "line " + line + ":" + pos + " error: '" + this.referenced_type + "'";
         if(this.last_valid != null){
             msg += "in '" + this.last_valid +"'";
         }
         msg += " does not name a type";
         this.final_err = msg;
     }
     
     public void finalizeErrorMessage(){
         String msg = "error: '" + this.referenced_type + "'";
         if(this.last_valid != null){
             msg += "in '" + this.last_valid +"'";
         }
         msg += " does not name a type";
         this.final_err = msg;
     }
     
     public void setIsPending(){
         this.isPending = true;
     }
     
     public boolean isPending(){
         return this.isPending;
     }
     
     
 }
