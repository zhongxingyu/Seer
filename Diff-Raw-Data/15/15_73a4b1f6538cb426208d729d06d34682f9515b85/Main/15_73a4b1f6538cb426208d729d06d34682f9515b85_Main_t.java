 package edu.rivfader;
 
 /**
  * This is the central class which ties everything together and contains the
  * main method.
  * @author harald
  */
 public class Main {
     /**
     * contains the database name to operate on.
     */
    private String databaseName;
    /**
      * initializes the core to use the database.
     * @param pDatabaseName a path to a database directory
      */
    public Main(final String pDatabaseName) {
        databaseName = pDatabaseName;
     }
 
     /**
      * executes the input string.
      * @param input a valid simpleSQL-Query.
      */
    public String run(final String input) {
        databaseName = databaseName + "x"; // To get rid of that warning :)
         return null;
     }
 }
