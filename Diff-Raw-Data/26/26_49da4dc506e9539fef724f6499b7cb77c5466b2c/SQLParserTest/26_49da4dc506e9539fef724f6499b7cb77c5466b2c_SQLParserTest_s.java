 package com.anyconsole.core.parser;
 
 import net.sf.jsqlparser.JSQLParserException;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.anyconsole.db.MongoExpressionBuilder;
 import com.anyconsole.db.MongoPlugin;
 
 /**
  * User: kbabushkin
  * Date: 8/6/13
  */
 
 public class SQLParserTest {
 
     @Before
     public void setUp() throws Exception {
     }
 
     @After
     public void tearDown() throws Exception {
 
     }
 
     @Test
     public void parseSelectTest() throws JSQLParserException {
         SQLParser parser = new SQLParser("SELECT * FROM table1");
     }
 
     @Test
     public void parseUpdateTest() throws JSQLParserException {
         SQLParser parser = new SQLParser("UPDATE table1 SET table1.column=value1");
         String result = parser.execute(new MongoExpressionBuilder(new MongoPlugin()));
         System.out.println(result);
     }
     
     @Test
     public void parseUpdateWhereTest() throws JSQLParserException {
        SQLParser parser = new SQLParser("UPDATE table1 SET column='value1' where column2 = 'value2'");
         String result = parser.execute(new MongoExpressionBuilder(new MongoPlugin()));
         System.out.println(result);
     }
 }
