 import java.util.ArrayList;
 
 public class FunctionSymbolTableEntry
 {
         private String stlID, javaID;
         private ArrayList<String> paramTypes;
        public FunctionSymbolTableEntry(String id, String javaID, ArrayList<String> parameterTypes)
         {
                 stlID = id;
                 this.javaID = javaID;
                 paramTypes = parameterTypes;
         }
         public String getID()
         {
                 return stlID;
         }
         public String getJavaID()
         {
                 return javaID;
         }
         public ArrayList<String> getParamTypes()
         {
                 return paramTypes;
         }
 }
