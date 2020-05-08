 package org.young.db;
 
 import org.junit.Test;
 
 
 public class DBTest {
     //~ Static fields/initializers *********************************************
 
     private static final int TEST_COUNT = 5;
 
     //~ Methods ****************************************************************
 
     @Test
     public void testGenerateUniqueKey() {
         for (int i = 0; i < (DB.STEP_FACTOR + TEST_COUNT); i++) {
         	
             long uniqueKey = DB.generateUniqueKey();
             System.out.println(uniqueKey);
             
         }
     }
    
    @Test
    public void testGenerateUniqueKeyWithLength() {
    	
    	System.out.println(DB.generateUniqueKey(32));
    }
 }
