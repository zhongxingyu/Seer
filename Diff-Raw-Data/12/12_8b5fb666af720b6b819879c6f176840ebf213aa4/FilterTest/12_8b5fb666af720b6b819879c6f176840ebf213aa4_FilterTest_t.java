 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.neo.services.testing.filters;
 
 import java.io.Serializable;
 
 import org.amanzi.neo.services.DatasetService;
 import org.amanzi.neo.services.NeoServiceFactory;
 import org.amanzi.neo.services.enums.NodeTypes;
 import org.amanzi.neo.services.filters.ExpressionType;
 import org.amanzi.neo.services.filters.Filter;
 import org.amanzi.neo.services.filters.FilterType;
 import org.amanzi.neo.services.filters.exceptions.FilterTypeException;
 import org.amanzi.neo.services.filters.exceptions.NotComparebleException;
 import org.amanzi.neo.services.filters.exceptions.NotComparebleRuntimeException;
 import org.amanzi.neo.services.filters.exceptions.NullValueException;
 import org.amanzi.testing.AbstractAWETest;
 import org.apache.log4j.Logger;
 import org.junit.AfterClass;
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Transaction;
 
 /**
  * TODO Purpose of 
  * <p>
  *
  * </p>
  * @author Kondratenko_V
  * @since 1.0.0
  */
 public class FilterTest extends AbstractAWETest {
     
     private static Logger LOGGER = Logger.getLogger(FilterTest.class);
 
     private static long startTimestamp;
 
     private static DatasetService datasetService;
 
   
     @BeforeClass
     public static void setUpBeforeClass() throws Exception {
         startTimestamp = System.currentTimeMillis();
         LOGGER.info("Set up AFP Filter Test");
 
         try {
             initializeDb();
             initPreferences();
             
             datasetService = NeoServiceFactory.getInstance().getDatasetService();
             //afpService = AfpService.getService();
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     /**
      * @throws java.lang.Exception
      */
     @AfterClass
     public static void tearDownAfterClass() throws Exception {
         stopDb();
         clearDb();
 
         long duration = System.currentTimeMillis() - startTimestamp;
         int milliseconds = (int)(duration % 1000);
         int seconds = (int)(duration / 1000 % 60);
         int minutes = (int)(duration / 1000 / 60 % 60);
         int hours = (int)(duration / 1000 / 60 / 60 % 24);
         LOGGER.info("Test finished. Test time - " + hours + " hours " + minutes + " minutes " + seconds + " seconds "
                 + milliseconds + " milliseconds");
     }
     /**
      * @throws java.lang.Exception
      */
   
     /*
      * check nodes with the same property value
      */
     @Test
     public void SameValuesTest() {
         LOGGER.info("< checkSameValuesThest begin >");
         Transaction tx = graphDatabaseService.beginTx();
         try {
 
             Node firstNode = graphDatabaseService.createNode();
             firstNode.setProperty("Name", "Value1");
             datasetService.getNodeType(firstNode);
             Node secondNode = graphDatabaseService.createNode();
             secondNode.setProperty("Name", "Value1");
             Filter afpFilter=new Filter(FilterType.EQUALS,ExpressionType.AND);
             LOGGER.info("--firstNode property: "+"'Name'; value: "+firstNode.getProperty("Name"));
             LOGGER.info("--secondNode property: "+"'Name'; value: "+secondNode.getProperty("Name"));
             LOGGER.info("----FilterType is 'EQUALS' ExpressionType is 'AND'");
             //afpFilter.addFilter(new Filter(FilterType.EQUALS,ExpressionType.AND));
             afpFilter.setExpression(null, "Name", secondNode.getProperty("Name").toString());
             Assert.assertTrue("Values are not EQUALS ",afpFilter.check(firstNode));
             
             tx.success();
         } catch (Exception e) {
             e.printStackTrace();
            
         }finally{
             tx.finish();
             LOGGER.info("< checkSameValuesThest end >");
         }
        
   
     }
     /*
      * check node with the width incorrect Expression value
      * FilterType EQUALS
      */
     @Test
     public void DifferentValuesTest() {
         LOGGER.info("< checkDifferentValuesTest begin >");
         Transaction tx = graphDatabaseService.beginTx();
         try {
 
             Node firstNode = graphDatabaseService.createNode();
             firstNode.setProperty("Name", "Value");
             datasetService.getNodeType(firstNode);
             Node secondNode = graphDatabaseService.createNode();
             secondNode.setProperty("Name", "Value 1 6 kk");
   
             Filter afpFilter=new Filter(FilterType.EQUALS,ExpressionType.AND);
             LOGGER.info("--firstNode property: "+"'Name';value: "+firstNode.getProperty("Name"));
             LOGGER.info("--secondNode property: "+"'Name';value: "+secondNode.getProperty("Name"));
             LOGGER.info("----FilterType is 'EQUALS' ExpressionType is 'AND'");
             //afpFilter.addFilter(new Filter(FilterType.EQUALS,ExpressionType.AND));
             afpFilter.setExpression(null, "Name", secondNode.getProperty("Name").toString());
             Assert.assertFalse("Values are EQUALS or expression is find ",afpFilter.check(firstNode));
             tx.success();
             
         } catch (Exception e) {
             e.printStackTrace();
            
         }finally{
             tx.finish();
             LOGGER.info("< checkDifferentValuesTest end >");
         }
      }
     
     /*
      * with 'Like' filterType and RegExp
      */
     @Test
     public void FilterTypeRE() {
         LOGGER.info("< checkFilterTypeRE begin >");
         Transaction tx = graphDatabaseService.beginTx();
         try {
 
             Node firstNode = graphDatabaseService.createNode();
             firstNode.setProperty("Name", "Value1");
             datasetService.getNodeType(firstNode);
             Node secondNode = graphDatabaseService.createNode();
             secondNode.setProperty("Name", "Value");
             Filter afpFilter=new Filter(FilterType.LIKE,ExpressionType.AND);
             afpFilter.setExpression(null, "Name", ".*"+secondNode.getProperty("Name")+".*");
             LOGGER.info("--firstNode property: "+"'Name'; value: "+firstNode.getProperty("Name"));
             LOGGER.info("--secondNode property: "+"'Name'; value: "+secondNode.getProperty("Name"));
             LOGGER.info("----FilterType is 'LIKE' ExpressionType is 'AND'");
             Assert.assertTrue("Need regExp or values doesn't LIKE",afpFilter.check(firstNode));
             tx.success();
             
         } catch (Exception e) {
             e.printStackTrace();
            
         }finally{
             tx.finish();
             LOGGER.info("< checkFilterTypeRE end >");
         }
      }
     
     
     /*
      * check with 'MORE' filterType , result false
      */
     @Test
     public void CheckMOREFilterFalse(){
     	LOGGER.info("< checkMOREFilterFalseTest begin >");
         Transaction tx = graphDatabaseService.beginTx();
         try {
 
             Node node = graphDatabaseService.createNode();
             node.setProperty("intValue", 5);
             
             Filter afpFilter=new Filter(FilterType.MORE);
             LOGGER.info("--Node property: "+"'intValue';value: "+node.getProperty("intValue"));
            
             LOGGER.info("----FilterType is 'MORE' ");
            
             afpFilter.setExpression(null, "intValue", 6);
             Assert.assertFalse("propertyValue is MORE then value ",afpFilter.check(node));
             tx.success();
             
         } catch (Exception e) {        
             LOGGER.error(e.toString());
            
         }finally{
             tx.finish();
             LOGGER.info("< checkMOREFilterFalseTest end >");
         }
     }
     
     /*
      * check with 'MORE' filterType , result true
      */
     @Test
     public void CheckMOREFilterTrue(){
     	LOGGER.info("< checkMOREFilterTrueTest begin >");
         Transaction tx = graphDatabaseService.beginTx();
         try {
 
             Node node = graphDatabaseService.createNode();
             node.setProperty("intValue", 6);
             Filter afpFilter=new Filter(FilterType.MORE);
             
             LOGGER.info("--Node property: "+"'intValue';value: "+node.getProperty("intValue"));
            
             LOGGER.info("----FilterType is 'MORE' ");
            
             afpFilter.setExpression(null, "intValue", 5);
             Assert.assertTrue("propertyValue isn't MORE then value ",afpFilter.check(node));
             tx.success();
             
         } catch (Exception e) {        
             LOGGER.error(e.toString());
            
         }finally{
             tx.finish();
             LOGGER.info("< checkMOREFilterTrueTest end >");
         }
     }
     
     /*
      * check with 'LESS' filterType , result false
      */
     @Test
     public void CheckLESSFilterFalse(){
     	LOGGER.info("< checkLESSFilterFalseTest begin >");
         Transaction tx = graphDatabaseService.beginTx();
         try {
 
             Node node = graphDatabaseService.createNode();
             node.setProperty("intValue", 6);
 
             Filter afpFilter=new Filter(FilterType.LESS);
             LOGGER.info("--Node property: "+"'intValue';value: "+node.getProperty("intValue"));
            
             LOGGER.info("----FilterType is 'LESS' ");
            
             afpFilter.setExpression(null, "intValue", 5);
             Assert.assertFalse("propertyValue is LESS then value ",afpFilter.check(node));
             tx.success();
             
         } catch (Exception e) {        
             LOGGER.error(e.toString());
            
         }finally{
             tx.finish();
             LOGGER.info("< checkLESSFilterFalseTest end >");
         }
     }
     
     /*
      * check with 'LESS' filterType , result true
      */
     @Test
     public void CheckLESSFilterTrue(){
     	LOGGER.info("< checkLESSFilterTrueTest begin >");
         Transaction tx = graphDatabaseService.beginTx();
         try {
 
             Node node = graphDatabaseService.createNode();
             node.setProperty("intValue", 5);
          
             Filter afpFilter=new Filter(FilterType.LESS);
             LOGGER.info("--Node property: "+"'intValue';value: "+node.getProperty("intValue"));
            
             LOGGER.info("----FilterType is 'LESS' ");
            
             afpFilter.setExpression(null, "intValue", 6);
             Assert.assertTrue("propertyValue isn't LESS then value ",afpFilter.check(node));
             tx.success();
             
         } catch (Exception e) {        
             LOGGER.error(e.toString());
            
         }finally{
             tx.finish();
             LOGGER.info("< checkLESSFilterTrueTest end >");
         }
     }
     
     /*
      * check with 'LESS' filterType with NullValueException
      */
     @Test
     public void CheckNullValueException(){
     	LOGGER.info("< checkNullValueExceptionTest begin >");
         Transaction tx = graphDatabaseService.beginTx();
         try {
 
             Node node = graphDatabaseService.createNode();
             node.setProperty("intValue", 5);
          
             Filter afpFilter=new Filter(FilterType.LESS);
             LOGGER.info("--Node property: "+"'intValue';value: "+node.getProperty("intValue"));
            
             LOGGER.info("----FilterType is 'LESS' ");
            
             afpFilter.setExpression(null, "intValue", null);
             Exception exc = null;
             try{
             	afpFilter.check(node);
             }
             
             catch (Exception e){
             	exc = e;
             }
             finally{
             	Assert.assertTrue("method check(Node node) don't catch NullValueException",exc instanceof NullValueException );
             }
             tx.success();
             
         } catch (Exception e) {        
             LOGGER.error(e.toString());
             
            
         }finally{
             tx.finish();            
             LOGGER.info("< checkNullValueExceptionTest end >");
         }
     }
     
     
     /*
      * check with 'LESS' filterType with NotComparebleException
      */
     @Test
     public void CheckNotComparebleException(){
     	LOGGER.info("< checkNotComparebleExceptionTest begin >");
         Transaction tx = graphDatabaseService.beginTx();
         try {
 
             Node node = graphDatabaseService.createNode();
             Object obj = new Object();
             node.setProperty("intValue",obj );
                
             Filter afpFilter=new Filter(FilterType.LESS);
             LOGGER.info("--Node property: "+"'intValue';value: "+node.getProperty("intValue", "null"));
            
             LOGGER.info("----FilterType is 'LESS' ");
            
             afpFilter.setExpression(null, "intValue", 6);
             Exception exc = null;
             try{
             	afpFilter.check(node);
             }
             catch (Exception e){
             	exc = e;          	
             }
             finally{
             	Assert.assertTrue("method check(Node node) don't catch NotComparebleException",exc instanceof NotComparebleException );
             }
             tx.success();
             
         } catch (Exception e) {        
             LOGGER.error(e.toString());
             
            
         }finally{
             tx.finish();            
             LOGGER.info("< checkNotComparebleExceptionTest end >");
         }
     }
     /*
      * check with 'MORE_OR_EQUALS' filterType , result true
      */
     @Test
     public void CheckMORE_OR_EQUALSFilterTrue(){
     	LOGGER.info("< checkMORE_OR_EQUALSFilterTrueTest begin >");
         Transaction tx = graphDatabaseService.beginTx();
         try {
 
             Node node = graphDatabaseService.createNode();
             node.setProperty("intValue", 5);
          
             Filter afpFilter=new Filter(FilterType.MORE_OR_EQUALS);
             LOGGER.info("--Node property: "+"'intValue';value: "+node.getProperty("intValue"));
            
             LOGGER.info("----FilterType is 'MORE_OR_EQUALS' ");
            
             afpFilter.setExpression(null, "intValue", 5);
             Assert.assertTrue("propertyValue isn't MORE_OR_EQUALS then value ",afpFilter.check(node));
             tx.success();
             
         } catch (Exception e) {        
             LOGGER.error(e.toString());
            
         }finally{
             tx.finish();
             LOGGER.info("< checkMORE_OR_EQUALSFilterTrueTest end >");
         }
     }
     
     /*
      * check with 'EMPTY' filterType , result true
      */
     @Test
     public void CheckEMPTYFilterTrue(){
     	LOGGER.info("< checkEMPTYFilterTrueTest begin >");
         Transaction tx = graphDatabaseService.beginTx();
         try {
 
             Node node = graphDatabaseService.createNode();
             node.setProperty("property", 5);
          
             Filter afpFilter=new Filter(FilterType.EMPTY);
             LOGGER.info("--Node property: "+"'property';value: "+node.getProperty("property"));
            
             LOGGER.info("----FilterType is 'EMPTY' ");
            
             afpFilter.setExpression(null, "anotherProperty");
             Assert.assertTrue(" 'EMPTY' filter is wron ",afpFilter.check(node));
             tx.success();
             
         } catch (Exception e) {        
             LOGGER.error(e.toString());
            
         }finally{
             tx.finish();
             LOGGER.info("< checkEMPTYFilterTrueTest end >");
         }
     }
     
     /*
      * check with 'EMPTY' filterType , result false
      */
     @Test
     public void CheckEMPTYFilterFalse(){
     	LOGGER.info("< checkEMPTYFilterFalseTest begin >");
         Transaction tx = graphDatabaseService.beginTx();
         try {
 
             Node node = graphDatabaseService.createNode();
             node.setProperty("property", 5);
          
             Filter afpFilter=new Filter(FilterType.EMPTY);
             LOGGER.info("--Node property: "+"'property';value: "+node.getProperty("property"));
            
             LOGGER.info("----FilterType is 'EMPTY' ");
            
             afpFilter.setExpression(null, "property");
             Assert.assertFalse(" 'EMPTY' filter is wron ",afpFilter.check(node));
             tx.success();
             
         } catch (Exception e) {        
             LOGGER.error(e.toString());
            
         }finally{
             tx.finish();
             LOGGER.info("< checkEMPTYFilterFalseTest end >");
         }
     }
     
     /*
      * check with 'NOT_EMPTY' filterType , result false
      */
     @Test
     public void CheckNOT_EMPTYFilterFalse(){
     	LOGGER.info("< checkNOT_EMPTYFilterFalseTest begin >");
         Transaction tx = graphDatabaseService.beginTx();
         try {
 
             Node node = graphDatabaseService.createNode();
             node.setProperty("property", 5);
          
             Filter afpFilter=new Filter(FilterType.NOT_EMPTY);
             LOGGER.info("--Node property: "+"'property';value: "+node.getProperty("property"));
            
             LOGGER.info("----FilterType is 'NOT_EMPTY' ");
            
             afpFilter.setExpression(null, "anotherProperty");
             Assert.assertFalse(" 'NOT_EMPTY' filter is wron ",afpFilter.check(node));
             tx.success();
             
         } catch (Exception e) {        
             LOGGER.error(e.toString());
            
         }finally{
             tx.finish();
             LOGGER.info("< checkNOT_EMPTYFilterFalseTest end >");
         }
     }
     
     /*
      * check with 'NOT_EMPTY' filterType , result true
      */
     @Test
     public void CheckNOT_EMPTYFilterTrue(){
     	LOGGER.info("< checkNOT_EMPTYFilterTrueTest begin >");
         Transaction tx = graphDatabaseService.beginTx();
         try {
 
             Node node = graphDatabaseService.createNode();
             node.setProperty("property", 5);
          
             Filter afpFilter=new Filter(FilterType.NOT_EMPTY);
             LOGGER.info("--Node property: "+"'property';value: "+node.getProperty("property"));
            
             LOGGER.info("----FilterType is 'NOT_EMPTY' ");
            
             afpFilter.setExpression(null, "property");
             Assert.assertTrue(" 'NOT_EMPTY' filter is wron ",afpFilter.check(node));
             tx.success();
             
         } catch (Exception e) {        
             LOGGER.error(e.toString());
            
         }finally{
             tx.finish();
             LOGGER.info("< checkNOT_EMPTYFilterTrueTest end >");
         }
     }
     
     /*
      * setExpression with FilterTypeException
      */
     @Test
     public void CheckFilterTypeException(){
     	LOGGER.info("< checkFilterTypeExceptionTest begin >");
     	Transaction tx = graphDatabaseService.beginTx();
     	try {
 
     		Node node = graphDatabaseService.createNode();
     		node.setProperty("property", 5);
 
     		Filter afpFilter=new Filter(FilterType.MORE);
     		LOGGER.info("--Node property: "+"'property';value: "+node.getProperty("property"));
 
     		LOGGER.info("----FilterType is 'MORE' ");
     		Exception exc = null;
     		try{
     			afpFilter.setExpression(null, "property");
     		}
     		catch (Exception e){
     			exc = e;
     		}
     		finally{
     			Assert.assertTrue("method setExpression() don't catch FilterTypeException", exc instanceof FilterTypeException);
     		}
     		tx.success();
 
     	} catch (Exception e) {        
     		LOGGER.error(e.toString());
 
     	}finally{
     		tx.finish();
     		LOGGER.info("< checkFilterTypeExceptionTest end >");
     	}
     }
     
     /*
      * setExpression with NotComparebleRuntimeException
      */
     @Test
     public void CheckNotComparebleRuntimeException(){
     	LOGGER.info("< checkNotComparebleRuntimeExceptionTest begin >");
     	Transaction tx = graphDatabaseService.beginTx();
     	try {
 
     		Node node = graphDatabaseService.createNode();
     		node.setProperty("property", 5);
 
     		Filter afpFilter=new Filter(FilterType.MORE);
     		LOGGER.info("--Node property: "+"'property';value: "+node.getProperty("property"));
 
     		LOGGER.info("----FilterType is 'MORE' ");
     		Exception exc = null;
    		
    		final class NotComparebleClass implements Serializable{	
    		}
    		
     		try{
    			NotComparebleClass object = new NotComparebleClass();
    			afpFilter.setExpression(null, "property", object);
     		}
     		catch (Exception e){
     			exc = e;
     		}
     		finally{
     			Assert.assertTrue("method setExpression() don't catch NotComparebleRuntimeException ", exc instanceof NotComparebleRuntimeException);
     		}
     		tx.success();
 
     	} catch (Exception e) {        
     		LOGGER.error(e.toString());
 
     	}finally{
     		tx.finish();
     		LOGGER.info("< checkNotComparebleRuntimeExceptionTest end >");
     	}
     }
     /*
      * check with 'MORE_OR_EQUALS' filterType , result false
      */
     @Test
     public void CheckMORE_OR_EQUALSFilterFalse(){
     	LOGGER.info("< checkMORE_OR_EQUALSFilterFalseTest begin >");
         Transaction tx = graphDatabaseService.beginTx();
         try {
 
             Node node = graphDatabaseService.createNode();
             node.setProperty("intValue", 5);
          
             Filter afpFilter=new Filter(FilterType.MORE_OR_EQUALS);
             LOGGER.info("--Node property: "+"'intValue';value: "+node.getProperty("intValue"));
            
             LOGGER.info("----FilterType is 'MORE_OR_EQUALS' ");
            
             afpFilter.setExpression(null, "intValue", 6);
             Assert.assertFalse("propertyValue is MORE_OR_EQUALS then value ",afpFilter.check(node));
             tx.success();
             
         } catch (Exception e) {        
             LOGGER.error(e.toString());
            
         }finally{
             tx.finish();
             LOGGER.info("< checkMORE_OR_EQUALSFilterFalseTest end >");
         }
     }
     
     
     /*
      * check with 'Like' filterType and without RegExp
      */
     @Test
     public void FilterTypeWithoutRE() {
         LOGGER.info("< checkFilterTypeWithoutRE begin >");
         Transaction tx = graphDatabaseService.beginTx();
         try {
 
             Node firstNode = graphDatabaseService.createNode();
             firstNode.setProperty("Name", "Value 1 2 4");
             datasetService.getNodeType(firstNode);
             Node secondNode = graphDatabaseService.createNode();
             secondNode.setProperty("Name", "Value");
             Filter afpFilter=new Filter(FilterType.LIKE,ExpressionType.AND);
             afpFilter.setExpression(null, "Name", secondNode.getProperty("Name").toString());
             LOGGER.info("--firstNode property: "+"'Name'; value: "+firstNode.getProperty("Name"));
             LOGGER.info("--secondNode property: "+"'Name'; value: "+secondNode.getProperty("Name"));
             LOGGER.info("----FilterType is 'LIKE' ExpressionType is 'AND'");
             Assert.assertFalse("has RegExp or values really LIKE",afpFilter.check(firstNode));
             tx.success();
             
         } catch (Exception e) {
             e.printStackTrace();
            
         }finally{
             tx.finish();
             LOGGER.info("< checkFilterTypeWithoutRE end >");
         }
      }
     
     /*
      *check afpFilter with 'EQUALS' filterType and 'OR' expressionType
      *addAfpFilter with 'EQUALS' filterType and with 'AND'  ExpressionType
      *with same 'Rest' property
      */
     @Test
     public void AdditionFilterEQ() {
         LOGGER.info("< checkAdditionFilterEQ begin >");
         Transaction tx = graphDatabaseService.beginTx();
         try {
             Node firstNode = graphDatabaseService.createNode();
             firstNode.setProperty("Name", 44);
             firstNode.setProperty("Rest", "value1");
             Node secondNode = graphDatabaseService.createNode();
             secondNode.setProperty("Name", 33);
             secondNode.setProperty("Rest", "value1");
             Filter afpFilter=new Filter(FilterType.EQUALS,ExpressionType.OR);
             Filter addAfpFilter=new Filter(FilterType.EQUALS);
            
             afpFilter.setExpression(null, "Name", ".*"+secondNode.getProperty("Name")+".*");
             addAfpFilter.setExpression(null,"Rest", secondNode.getProperty("Rest").toString());
            
             afpFilter.addFilter(addAfpFilter);
             LOGGER.info("--firstNode property: "+"'Name'; value: "+firstNode.getProperty("Name"));
             LOGGER.info("--secondNode property: "+"'Name'; value: "+secondNode.getProperty("Name"));
             LOGGER.info("---- afpFilter FilterType is 'EQUALS' ExpressionType is 'OR'");
             LOGGER.info("---- addAfpFilter FilterType is 'EQUALS' ExpressionType is 'AND'");
             
             Assert.assertTrue("need the same 'Rest' or 'Name' property value ",afpFilter.check(firstNode));
             tx.success();
          } catch (Exception e) {
             e.printStackTrace();
          }finally{
             tx.finish();
             LOGGER.info("< checkAdditionFilterEQ end >");
         }
      }
     
     /*
      * check with 'LESS_OR_EQUALS' filterType , result false
      */
     @Test
     public void CheckLESS_OR_EQUALSFilterFalse(){
     	LOGGER.info("< checkLESS_OR_EQUALSFilterFalseTest begin >");
         Transaction tx = graphDatabaseService.beginTx();
         try {
 
             Node node = graphDatabaseService.createNode();
             node.setProperty("intValue", 6);
          
             Filter afpFilter=new Filter(FilterType.LESS_OR_EQUALS);
             LOGGER.info("--Node property: "+"'intValue';value: "+node.getProperty("intValue"));
            
             LOGGER.info("----FilterType is 'LESS_OR_EQUALS' ");
            
             afpFilter.setExpression(null, "intValue", 5);
             Assert.assertFalse("propertyValue is LESS_OR_EQUALS then value ",afpFilter.check(node));
             tx.success();
             
         } catch (Exception e) {        
             LOGGER.error(e.toString());
            
         }finally{
             tx.finish();
             LOGGER.info("< checkLESS_OR_EQUALSFilterFalseTest end >");
         }
     }
     
     /*
      * check with 'LESS_OR_EQUALS' filterType , result true
      */
     @Test
     public void CheckLESS_OR_EQUALSFilterTrue(){
     	LOGGER.info("< checkLESS_OR_EQUALSFilterTrueTest begin >");
         Transaction tx = graphDatabaseService.beginTx();
         try {
 
             Node node = graphDatabaseService.createNode();
             node.setProperty("intValue", 5);
          
             Filter afpFilter=new Filter(FilterType.LESS_OR_EQUALS);
             LOGGER.info("--Node property: "+"'intValue';value: "+node.getProperty("intValue"));
            
             LOGGER.info("----FilterType is 'LESS_OR_EQUALS' ");
            
            afpFilter.setExpression(null, "intValue", 5);
             Assert.assertTrue("propertyValue isn't LESS_OR_EQUALS then value ",afpFilter.check(node));
             tx.success();
             
         } catch (Exception e) {        
             LOGGER.error(e.toString());
            
         }finally{
             tx.finish();
             LOGGER.info("< checkLESS_OR_EQUALSFilterTrueTest end >");
         }
     }
     
     
     /*
      *check afpFilter with 'LIKE' filterType and 'AND' expressionType
      *addAfpFilter with 'LIKE' filterType and with 'AND'  ExpressionType
      */
     @Test
     public void AdditionFilterLike() {
         LOGGER.info("< checkAdditionFilterLike begin >");
         Transaction tx = graphDatabaseService.beginTx();
         try {
             Node firstNode = graphDatabaseService.createNode();
             firstNode.setProperty("Name", "name 1");
             firstNode.setProperty("Rest", "value 4");
             Node secondNode = graphDatabaseService.createNode();
             secondNode.setProperty("Name", "name");
             secondNode.setProperty("Rest", "value");
             Filter afpFilter=new Filter(FilterType.LIKE,ExpressionType.AND);
             Filter addAfpFilter=new Filter(FilterType.LIKE);
            
             afpFilter.setExpression(null, "Name", ".*"+secondNode.getProperty("Name")+".*");
             addAfpFilter.setExpression(null,"Rest", ".*"+secondNode.getProperty("Rest")+".*");
             afpFilter.addFilter(addAfpFilter);
              
             LOGGER.info("--firstNode property: "+"'Name'; value: "+firstNode.getProperty("Name"));
             LOGGER.info("--secondNode property: "+"'Name'; value: "+secondNode.getProperty("Name"));
             LOGGER.info("---- afpFilter FilterType is 'LIKE' ExpressionType is 'AND'");
             LOGGER.info("---- addAfpFilter FilterType is 'LIKE' ExpressionType is 'AND'");
             
             Assert.assertTrue("values doesn't 'LIKE' each others",afpFilter.check(firstNode));
             tx.success();
          } catch (Exception e) {
             e.printStackTrace();
          }finally{
             tx.finish();
             LOGGER.info("< checkAdditionFilterLike end >");
         }
      }
     
     /*
      *check afpFilter with 'EQUALS' filterType and 'AND' expressionType
      *addAfpFilter with 'LIKE' filterType and with 'AND'  ExpressionType
      */
     @Test
     public void AdditionFilterEQL() {
         LOGGER.info("< checkAdditionFilterEQL begin >");
         Transaction tx = graphDatabaseService.beginTx();
         try {
             Node firstNode = graphDatabaseService.createNode();
             firstNode.setProperty("Name", "name 1");
             firstNode.setProperty("Rest", "value 4");
             Node secondNode = graphDatabaseService.createNode();
             secondNode.setProperty("Name", "name 1");
             secondNode.setProperty("Rest", "value");
             Filter afpFilter=new Filter(FilterType.EQUALS,ExpressionType.AND);
             Filter addAfpFilter=new Filter(FilterType.LIKE);
            
             afpFilter.setExpression(null, "Name", secondNode.getProperty("Name").toString());
             addAfpFilter.setExpression(null,"Rest", ".*"+secondNode.getProperty("Rest")+".*");
             afpFilter.addFilter(addAfpFilter);
              
             LOGGER.info("--firstNode property: "+"'Name'; value: "+firstNode.getProperty("Name"));
             LOGGER.info("--secondNode property: "+"'Name'; value: "+secondNode.getProperty("Name"));
             LOGGER.info("---- afpFilter FilterType is 'EQUALS' ExpressionType is 'AND'");
             LOGGER.info("---- addAfpFilter FilterType is 'LIKE' ExpressionType is 'AND'");
             
             Assert.assertTrue("'Name' property is not EQUALS or 'Rest' property is not LIKE to Expression",afpFilter.check(firstNode));
             tx.success();
          } catch (Exception e) {
             e.printStackTrace();
          }finally{
             tx.finish();
             LOGGER.info("< checkAdditionFilterEQL end >");
         }
      }
     
     /*
      *check afpFilter with 'LIKE' filterType and 'AND' expressionType 
      *addAfpFilter with 'EQUALS' filterType and with 'AND'  ExpressionType
     */
     @Test
     public void AdditionFilterLEQ() {
         LOGGER.info("< checkAdditionFilterLEQ begin >");
         Transaction tx = graphDatabaseService.beginTx();
         try {
             Node firstNode = graphDatabaseService.createNode();
             firstNode.setProperty("Name", "name 1");
             firstNode.setProperty("Rest", "value 4");
            
             Node secondNode = graphDatabaseService.createNode();
             secondNode.setProperty("Name", "name");
             secondNode.setProperty("Rest", "value 4");
             
             Filter afpFilter=new Filter(FilterType.LIKE,ExpressionType.AND);
             Filter addAfpFilter=new Filter(FilterType.EQUALS);
            
             afpFilter.setExpression(null, "Name", ".*"+secondNode.getProperty("Name")+".*");
             addAfpFilter.setExpression(null,"Rest", secondNode.getProperty("Rest").toString());
             afpFilter.addFilter(addAfpFilter);
              
             LOGGER.info("--firstNode property: "+"'Name'; value: "+firstNode.getProperty("Name"));
             LOGGER.info("--secondNode property: "+"'Name'; value: "+secondNode.getProperty("Name"));
             LOGGER.info("---- afpFilter FilterType is 'LIKE' ExpressionType is 'AND'");
             LOGGER.info("---- addAfpFilter FilterType is 'EQUALS' ExpressionType is 'AND'");
             
             Assert.assertTrue("'Name' property is not LIKE or 'Rest' property not EQUALS to Expression",afpFilter.check(firstNode));
             tx.success();
          } catch (Exception e) {
             e.printStackTrace();
          }finally{
             tx.finish();
             LOGGER.info("< checkAdditionFilterLEQ end >");
         }
      }
     
     /*
      *check if node doesn't consist propertyName
      *
     */
     @Test
     public void NullName() {
         LOGGER.info("< checkAdditionFilterLEQ begin >");
         Transaction tx = graphDatabaseService.beginTx();
         try {
             Node firstNode = graphDatabaseService.createNode();
             firstNode.setProperty("Rest", "value 4");
 
             Node secondNode = graphDatabaseService.createNode();
             secondNode.setProperty("Name", "name");
             secondNode.setProperty("Rest", "value 4");
             Filter afpFilter = new Filter(FilterType.LIKE);
             afpFilter.setExpression(null, "Name", ".*" + secondNode.getProperty("Name") + ".*");
 
             LOGGER.info("--firstNode doesn't consist 'Name' property: ");
             LOGGER.info("--secondNode property: " + "'Name'; value: " + secondNode.getProperty("Name"));
             LOGGER.info("---- afpFilter FilterType is 'LIKE' ExpressionType is 'AND'");
             Assert.assertFalse("'Name' property is found",afpFilter.check(firstNode));
         }catch (org.neo4j.graphdb.NotFoundException e) {
             e.printStackTrace();
             //Assert.fail("can't find propertyName in a node ");
         } 
         catch (Exception e) {
             e.printStackTrace();
         } finally {
             tx.finish();
             LOGGER.info("< checkNullName end >");
         }
     }
 //    
 //    /*
 //     *check if node property value is  null
 //     *
 //    */
 //    @Test
 //    public void checkNullValue() {
 //        LOGGER.info("< checkAdditionFilterLEQ begin >");
 //        Transaction tx = graphDatabaseService.beginTx();
 //        try {
 //            Node firstNode = graphDatabaseService.createNode();
 //            firstNode.setProperty("Name", null);
 //            firstNode.setProperty("Rest", "value 4");
 //
 //            Node secondNode = graphDatabaseService.createNode();
 //            secondNode.setProperty("Name", "name");
 //            secondNode.setProperty("Rest", "value 4");
 //            Filter afpFilter = new Filter(FilterType.LIKE);
 //            afpFilter.setExpression(null, "Name", ".*" + secondNode.getProperty("Name") + ".*");
 //
 //            LOGGER.info("--firstNode property: " + "'Name'; value: " + firstNode.getProperty("Name"));
 //            LOGGER.info("--secondNode property: " + "'Name'; value: " + secondNode.getProperty("Name"));
 //            LOGGER.info("---- afpFilter FilterType is 'LIKE' ExpressionType is 'AND'");
 //            LOGGER.info("---- addAfpFilter FilterType is 'LIKE' ExpressionType is 'AND'");
 //            Assert.assertTrue("'Name' property is not LIKE or 'Rest' property not EQUALS to Expression",afpFilter.check(firstNode));
 //        }catch (java.lang.IllegalArgumentException e) {
 //            e.printStackTrace();
 //            Assert.fail("Value in filtering Node is null");
 //        } 
 //        catch (Exception e) {
 //            e.printStackTrace();
 //        } finally {
 //            tx.finish();
 //            LOGGER.info("< checkAdditionFilterLEQ end >");
 //        }
 //    }
     
     /*
      *check if propertyName in expression  is  null
      *FilterType 'LIKE'
     */
     @Test
     public void NullPropertyNameLike() {
         LOGGER.info("< checkPropertyNameNull begin >");
         Transaction tx = graphDatabaseService.beginTx();
         try {
             Node firstNode = graphDatabaseService.createNode();
             firstNode.setProperty("Name", "Name11");
             firstNode.setProperty("Rest", "value 4");
 
             Node secondNode = graphDatabaseService.createNode();
             secondNode.setProperty("Name", "name");
             secondNode.setProperty("Rest", "value 4");
             Filter afpFilter = new Filter(FilterType.LIKE);
             afpFilter.setExpression(null,null, ".*" + secondNode.getProperty("Name") + ".*");
 
             LOGGER.info("--firstNode property: " + "'Name'; value: " + firstNode.getProperty("Name"));
             LOGGER.info("--secondNode property: " + "'Name'; value: " + secondNode.getProperty("Name"));
             LOGGER.info("---- afpFilter FilterType is 'LIKE' ExpressionType is 'AND'");
             LOGGER.info("---- afpFilter Expression: null,null,'name'");
             Assert.assertFalse("Expression propertyName is null",afpFilter.check(firstNode));
         }catch (NullPointerException e){
             e.printStackTrace();
             //Assert.fail("Null pointer Exception in propertyName");
         }
         catch (Exception e) {
             e.printStackTrace();
         } finally {
             tx.finish();
             LOGGER.info("< checkNullPropertyNameLike end >");
         }
     }
     
     /*
      *check if propertyName in expression  is  null
      *FilterType 'EQUALS'
     */
     @Test
     public void NullPropertyNameEQUALS() {
         LOGGER.info("< checkPropertyNameNull begin >");
         Transaction tx = graphDatabaseService.beginTx();
         try {
             Node firstNode = graphDatabaseService.createNode();
             firstNode.setProperty("Name", "Name11");
             firstNode.setProperty("Rest", "value 4");
 
             Node secondNode = graphDatabaseService.createNode();
             secondNode.setProperty("Name", "name");
             secondNode.setProperty("Rest", "value 4");
             Filter afpFilter = new Filter(FilterType.EQUALS);
             afpFilter.setExpression(null,null,secondNode.getProperty("Name").toString());
 
             LOGGER.info("--firstNode property: " + "'Name'; value: " + firstNode.getProperty("Name"));
             LOGGER.info("--secondNode property: " + "'Name'; value: " + secondNode.getProperty("Name"));
             LOGGER.info("---- afpFilter FilterType is 'LIKE' ExpressionType is 'AND'");
             LOGGER.info("---- afpFilter Expression: null,null,'name'");
             Assert.assertFalse("Expression propertyName is null",afpFilter.check(firstNode));
         }catch (NullPointerException e){
             e.printStackTrace();
             //Assert.fail("Null pointer Exception in propertyName");
         }
         catch (Exception e) {
             e.printStackTrace();
         } finally {
             tx.finish();
             LOGGER.info("< checkNullPropertyNameEQUALS end >");
         }
     }
     
     /*
      *check if propertyName in expression  is  null
      *FilterType ERQUALS
     */
     @Test
     public void NullPropertyValueEQ() {
         LOGGER.info("< checkPropertyNameNull begin >");
         Transaction tx = graphDatabaseService.beginTx();
         try {
             Node firstNode = graphDatabaseService.createNode();
             firstNode.setProperty("Name", "Name11");
             firstNode.setProperty("Rest", "value 4");
 
             Node secondNode = graphDatabaseService.createNode();
             secondNode.setProperty("Name", "name");
             secondNode.setProperty("Rest", "value 4");
             Filter afpFilter = new Filter(FilterType.EQUALS);
             afpFilter.setExpression(null,"Name", null);
 
             LOGGER.info("--firstNode property: " + "'Name'; value: " + firstNode.getProperty("Name"));
             LOGGER.info("--secondNode property: " + "'Name'; value: " + secondNode.getProperty("Name"));
             LOGGER.info("---- afpFilter FilterType is 'LIKE' ExpressionType is 'AND'");
             LOGGER.info("---- afpFilter Expression: null,'Name',null");
             Assert.assertFalse("Expression propertyName is not null",afpFilter.check(firstNode));
         }catch (NullPointerException e){
             e.printStackTrace();
             //Assert.fail("Null pointer Exception in propertyValue");
         }
         catch (Exception e) {
             e.printStackTrace();
         } finally {
             tx.finish();
             LOGGER.info("< checkNullPropertyValueEQ end >");
         }
     }
     /*
      *check if expression propertyName is  null
      *FilterType LIKE
     */
     @Test
     public void NullPropertyValueLike() {
         LOGGER.info("< checkPropertyNameNull begin >");
         Transaction tx = graphDatabaseService.beginTx();
         try {
             Node firstNode = graphDatabaseService.createNode();
             firstNode.setProperty("Name", "Name11");
             firstNode.setProperty("Rest", "value 4");
 
             Node secondNode = graphDatabaseService.createNode();
             secondNode.setProperty("Name", "name");
             secondNode.setProperty("Rest", "value 4");
             Filter afpFilter = new Filter(FilterType.LIKE);
             afpFilter.setExpression(null,"Name", null);
 
             LOGGER.info("--firstNode property: " + "'Name'; value: " + firstNode.getProperty("Name"));
             LOGGER.info("--secondNode property: " + "'Name'; value: " + secondNode.getProperty("Name"));
             LOGGER.info("---- afpFilter FilterType is 'LIKE' ExpressionType is 'AND'");
             LOGGER.info("---- afpFilter Expression: null,'Name',null");
             Assert.assertFalse("Expression propertyValue is not null",afpFilter.check(firstNode));
         }catch (NullPointerException e){
             e.printStackTrace();
         }
         catch (Exception e) {
             e.printStackTrace();
         } finally {
             tx.finish();
             LOGGER.info("< checkNullPropertyValue end >");
         }
     }
     /*
      *check if expression propertyName and value null
      *FilterType LIKE
     */
     @Test
     public void NullBOTHLike() {
         LOGGER.info("< checkNullBOTHLike begin >");
         Transaction tx = graphDatabaseService.beginTx();
         try {
             Node firstNode = graphDatabaseService.createNode();
             firstNode.setProperty("Name", "Name11");
             firstNode.setProperty("Rest", "value 4");
 
             Node secondNode = graphDatabaseService.createNode();
             secondNode.setProperty("Name", "name");
             secondNode.setProperty("Rest", "value 4");
             Filter afpFilter = new Filter(FilterType.LIKE);
             afpFilter.setExpression(null,null, null);
 
             LOGGER.info("--firstNode property: " + "'Name'; value: " + firstNode.getProperty("Name"));
             LOGGER.info("--secondNode property: " + "'Name'; value: " + secondNode.getProperty("Name"));
             LOGGER.info("---- afpFilter FilterType is 'LIKE' ExpressionType is 'AND'");
             LOGGER.info("---- afpFilter Expression: null,null,null");
             Assert.assertFalse("Expression BOTH parameters is not null",afpFilter.check(firstNode));
         }catch (NullPointerException e){
             e.printStackTrace();
             //Assert.fail("Null pointer Exception in propertyValue or propertyName");
         }
         catch (Exception e) {
             e.printStackTrace();
         } finally {
             tx.finish();
             LOGGER.info("< checkNullBOTHLike end >");
         }
     }
     /*
      * check if expression propertyName and value null
      * FilterType EQUALS
     */
     @Test
     public void NullBOTHEQ() {
         LOGGER.info("< checkNullBOTHLike begin >");
         Transaction tx = graphDatabaseService.beginTx();
         try {
             Node firstNode = graphDatabaseService.createNode();
             firstNode.setProperty("Name", "Name11");
             firstNode.setProperty("Rest", "value 4");
 
             Node secondNode = graphDatabaseService.createNode();
             secondNode.setProperty("Name", "name");
             secondNode.setProperty("Rest", "value 4");
             Filter afpFilter = new Filter(FilterType.EQUALS);
             afpFilter.setExpression(null,null, null);
 
             LOGGER.info("--firstNode property: " + "'Name'; value: " + firstNode.getProperty("Name"));
             LOGGER.info("--secondNode property: " + "'Name'; value: " + secondNode.getProperty("Name"));
             LOGGER.info("---- afpFilter FilterType is 'LIKE' ExpressionType is 'AND'");
             LOGGER.info("---- addAfpFilter FilterType is 'LIKE' ExpressionType is 'AND'");
             LOGGER.info("---- afpFilter Expression: null,null,null");
             Assert.assertFalse("Expression BOTH parameters is not null",afpFilter.check(firstNode));
         }catch (NullPointerException e){
             e.printStackTrace();
             //Assert.fail("Null pointer Exception in propertyValue or propertyName");
         }
         catch (Exception e) {
             e.printStackTrace();
     
         } finally {
             tx.finish();
             LOGGER.info("< checkNullBOTHLike end >");
         }
     }
     
     /*
      * check nodeType 
      * FilterType EQUALS
     */
     @Test
     public void SameNodeType() {
         LOGGER.info("< checkSameNodeType begin >");
         Transaction tx = graphDatabaseService.beginTx();
         try {
             Node firstNode = graphDatabaseService.createNode();
             datasetService.setNodeType(firstNode, NodeTypes.CITY);
             //System.out.println(datasetService.getNodeType(firstNode));
             firstNode.setProperty("Name", "Name11");
             firstNode.setProperty("Rest", "value 4");
 
             Node secondNode = graphDatabaseService.createNode();
             secondNode.setProperty("Name", "name");
             secondNode.setProperty("Rest", "value 4");
             Filter afpFilter = new Filter(FilterType.EQUALS,ExpressionType.AND);
             afpFilter.setExpression(NodeTypes.CITY,"Rest", "value 4");
 
             LOGGER.info("--firstNode nodeType:'CITY'; property: " + "'Rest'; value: " + firstNode.getProperty("Rest"));
             LOGGER.info("---- afpFilter FilterType is 'EQUALS' ExpressionType is 'AND'");
             LOGGER.info("---- afpFilter Expression: CITY,'Rest','value 4'");
             Assert.assertTrue("Expression NodeType Parameter and node type are not the same",afpFilter.check(firstNode));
         }catch (NullPointerException e){
             e.printStackTrace();
             //Assert.fail("Null pointer Exception in propertyValue or propertyName");
         }
         catch (Exception e) {
             e.printStackTrace();
     
         } finally {
             tx.finish();
             LOGGER.info("< checkSameNodeType end >");
         }
     }
     
     /*
      * check nodeType 
      * FilterType EQUALS
     */
     @Test
     public void DiffNodeType() {
         LOGGER.info("< checkNullBOTHLike begin >");
         Transaction tx = graphDatabaseService.beginTx();
         try {
             Node firstNode = graphDatabaseService.createNode();
             datasetService.setNodeType(firstNode, NodeTypes.CITY);
             //System.out.println(datasetService.getNodeType(firstNode));
             firstNode.setProperty("Name", "Name11");
             firstNode.setProperty("Rest", "value 4");
 
             Node secondNode = graphDatabaseService.createNode();
             secondNode.setProperty("Name", "name");
             secondNode.setProperty("Rest", "value 4");
             Filter afpFilter = new Filter(FilterType.EQUALS,ExpressionType.AND);
             //Filter addafpFilter = new Filter(FilterType.LIKE);
            // addafpFilter.setExpression(NodeTypes.CITY, "Name", ".*Name.*");
             afpFilter.setExpression(NodeTypes.BSC,"Rest", "value 4");
            // afpFilter.addFilter(addafpFilter);
 
 
             LOGGER.info("--firstNode nodeType:'CITY'; property: " + "'Rest'; value: " + firstNode.getProperty("Rest"));
             LOGGER.info("---- afpFilter FilterType is 'EQUALS' ExpressionType is 'AND'");
             LOGGER.info("---- afpFilter Expression: BSC,'Rest','value 4'");
             Assert.assertFalse("Expression NodeType Parameter and node type are the same",afpFilter.check(firstNode));
         }catch (NullPointerException e){
             e.printStackTrace();
             //Assert.fail("Null pointer Exception in propertyValue or propertyName");
         }
         catch (Exception e) {
             e.printStackTrace();
     
         } finally {
             tx.finish();
             LOGGER.info("< checkDiffNodeType end >");
         }
     }
 }
