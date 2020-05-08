 package IntermediateCodeGeneration.SymbolTable.Type;
 
 import IntermediateCodeGeneration.SymbolTable.ClassEntry;
 import IntermediateCodeGeneration.SymbolTable.SymbolTable;
import static IntermediateCodeGeneration.SymbolTable.Type.Type.isPrimitiveType;
 
 /**
  * Representacion de los tipos de datos definidos por el programador
  *
  * @author Ramiro Agis
  * @author Victoria Martinez de la Cruz
  */
 public class ClassType extends Type {
 
     private SymbolTable symbolTable;
 
     public ClassType(String typeName, SymbolTable symbolTable) {
         super(typeName);
         this.symbolTable = symbolTable;
     }
 
     @Override
     public boolean checkConformity(Type type) {
        // el parametro tiene que conformar al que reccibe 
        // parametro subtypo
        // this supertypo
         boolean conforms = false;
 
         if (isPrimitiveType(type)) {
             // El unico tipo primitivo que puede conformar con un tipo clase
             // es null
             if (type.getTypeName().equals("null")) {
                 return true;
             }
             // Un tipo primitivo no puede conformar con uno de tipo clase
             return false;
         }
        if (isPrimitiveType(this)) {
            // El unico tipo primitivo que puede conformar con un tipo clase
            // es null
            if (this.getTypeName().equals("null")) {
                return true;
            }
            // Un tipo primitivo no puede conformar con uno de tipo clase
            return false;
        }
 
         ClassEntry subtypeClass = symbolTable.getClassEntry(type.getTypeName());
         ClassEntry supertypeClass = symbolTable.getClassEntry(this.getTypeName());
         String subtypeName = subtypeClass.getName();
       
         String supertypeName = supertypeClass.getName();
 
         if (subtypeName.equals("Object") && (supertypeName.equals("Object"))) {
             return true;
         }
 
         while (!conforms && !subtypeName.equals("Object")) {
             // If the types are the same then it conforms.
             if (subtypeName.equals(supertypeName)) {
                 conforms = true;
             } else {
                 // If the types aren't the same, we check with the subtype's parent.
                 subtypeClass = symbolTable.getClassEntry(subtypeClass.getParent());
                 subtypeName = subtypeClass.getName();
             }
         }
         return conforms;
     }
 }
