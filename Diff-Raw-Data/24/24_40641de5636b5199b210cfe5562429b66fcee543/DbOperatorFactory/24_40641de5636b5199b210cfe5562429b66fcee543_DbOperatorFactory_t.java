 package net.madz.db.core;
 
 /**
  * This factory generates DBSchemaResolvers and DBSchemaConstrutors of different
  * databases.
  * 
  * @author tracy
  * 
  */
 public interface DbOperatorFactory {
 
     /**
      * @param databaseName
      * @param isCopy
      * @return
      */
     // TODO [Jan 22, 2013][barry] isCopy means unclearly
     AbsSchemaMetaDataParser createSchemaParser(String databaseName, boolean isCopy);
 
     // TODO [Jan 22, 2013][barry] More comments
    /***
     * 
     * @param databaseName
     * @return
     */
     AbsDatabaseGenerator createDatabaseGenerator(String databaseName);
 }
