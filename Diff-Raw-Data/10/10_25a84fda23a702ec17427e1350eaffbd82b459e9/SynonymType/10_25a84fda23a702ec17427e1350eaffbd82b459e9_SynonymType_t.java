 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package symbolTable.namespace;
 
 import errorHandling.BaseClassCVQual;
 import errorHandling.VoidDeclaration;
 import symbolTable.types.PrimitiveType;
 import symbolTable.types.Type;
 import symbolTable.types.UserDefinedType;
 
 /**
  *
  * @author kostas
  */
 public class SynonymType implements NamedType {
     
     String name;
     
     Type synonym;
     
     DefinesNamespace belongsTo;
     
     String enum_or_typedef;
     
     String fileName;
     
     int line, pos;
 
     public SynonymType(String name, Type synonym, DefinesNamespace belongsTo){
         this.name = name;
         this.synonym = synonym;
         this.belongsTo = belongsTo;
         this.enum_or_typedef = "typedef";
     }
     
     public SynonymType(String name, DefinesNamespace belongsTo){
         this.name = name;
         this.belongsTo = belongsTo;
         this.synonym = new PrimitiveType("int", true, false);
         this.enum_or_typedef = "enum";
     }
     
     public Type getSynonym(){
         return this.synonym;
     }
     
     public void setSynonym(Type t){
         this.synonym = t;
     }
     
     public void setFileName(String fileName){
         this.fileName = fileName;
     }
     
     @Override
     public String getFileName(){
         return this.fileName;
     }
     
     public void setLineAndPos(int line, int pos){
         this.line = line;
         this.pos = pos;
     }
 
     
     @Override
     public boolean equals(Object obj){
         if(obj == null) return false;
         if(obj instanceof SynonymType){
             SynonymType s = (SynonymType) obj;
             if(this.belongsTo != s.belongsTo) return false;
             return this.name.equals(s.name);
         }
         else return false;
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 11 * hash + (this.name != null ? this.name.hashCode() : 0);
         hash = 11 * hash + (this.belongsTo != null ? this.belongsTo.hashCode() : 0);
         return hash;
     }
     
     @Override
     public String toString(){
         if(this.enum_or_typedef.equals("enum") == true){
            String parent = this.belongsTo.getStringName(new StringBuilder()).toString();
             return this.enum_or_typedef + " " + parent + (parent.equals("") ? "" : "::") + this.name;
         }
         else{
            String parent = this.belongsTo.getStringName(new StringBuilder()).toString();
             String t_name = " " + parent + (parent.equals("") ? "" : "::") + this.name;
             return "typedef " + this.synonym.toString(t_name);
         }
      }
     
     /*
      * NamedType interface methods
      */
     @Override
     public int getLine() {
         return this.line;
     }
 
     @Override
     public int getPosition() {
         return this.pos;
     }
 
     @Override
    public String getFullName() {
        String parent = this.belongsTo.getStringName(new StringBuilder()).toString();
         return parent + (parent.equals("") ? "" : "::") + this.name;
     }
 
     @Override
     public String getTag() {
         return this.enum_or_typedef;
     }
 
     @Override
     public CpmClass isClassName() throws BaseClassCVQual {
         CpmClass rv = null;
         if(this.synonym instanceof UserDefinedType){
             UserDefinedType ut = (UserDefinedType)this.synonym;
             if((rv = ut.getNamedType().isClassName()) != null){
                 if (ut.isConst() == true || ut.isVolatile() == true){
                     throw new BaseClassCVQual(this);
                 }
             }
         }
         return rv;
     }
     
     @Override
     public DefinesNamespace getParentNamespace(){
         return this.belongsTo;
     }
     
     @Override
     public boolean isComplete(CpmClass current) throws VoidDeclaration{
         return this.synonym.isComplete(current);
     }
     
 }
