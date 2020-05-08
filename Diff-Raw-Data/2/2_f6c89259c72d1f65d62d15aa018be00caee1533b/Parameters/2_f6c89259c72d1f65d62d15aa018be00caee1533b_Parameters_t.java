 package parser;
 
 /**
 * <b>Parser - Parameters</b><br>
  * Provide a simple container to the
  * parameters of a function stored in
  * the parser's simble table.
  */
 public class Parameters
 {
     // Auxiliar attributes
     String[] types;
     
     /**
      * Default constructor.<br>
      * @param types Array with strings indicating
      *              the types of the parameters of
      *              a given function
      */
     public Parameters(String[] types)
     {
         this.types = types;
     }
     
     /**
      * Getter for a type.
      * @param  pos Position
      * @return Parameter type of a given position.
      *         If invalid, returns null
      */
     public String getType(int pos)
     {
         if(pos < 0 || pos >= types.length) return null;
         return types[pos];
     }
     
     /**
      * Getter for the number of parameters.
      * @return Number of parameters
      */
     public int getSize()
     {
         return types.length;
     }
 }
