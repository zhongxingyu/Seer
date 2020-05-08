 package org.melati.test;
 
 import org.melati.poem.*;
 
 public class Regression {
 
   public static final String dbName = "melatiregression";
 
   public static void main(String[] args) throws Exception {
    if (Runtime.exec("destroydb " + dbName).waitFor() != 0 ||
	Runtime.exec("createdb " + dbName).waitFor() != 0)
      exit(1);
 
     final Database database = new PoemDatabase();
 
     database.connect(new org.melati.poem.postgresql.jdbc2.Driver(),
		     "jdbc:postgresql:" + melatiregression, "postgres", "*");
 
     // to test:
 
     // creation
     // deletion
     // attempt to re-create
     // 
 
     // rollback
     // blocking
     // deadlock recovery
   }
 }
