 import java.util.ArrayList;
 
 public class FunctionSymbolTableEntry
 {
         private String stlID, javaID;
         private ArrayList<String> paramTypes;
        private String returnType;
        
        public FunctionSymbolTableEntry(String id, String javaID, String returnType, ArrayList<String> parameterTypes)
         {
                 stlID = id;
                 this.javaID = javaID;
                this.setReturnType(returnType);
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
		/**
		 * @return the returnType
		 */
		public String getReturnType() {
			return returnType;
		}
		/**
		 * @param returnType the returnType to set
		 */
		public void setReturnType(String returnType) {
			this.returnType = returnType;
		}
 }
