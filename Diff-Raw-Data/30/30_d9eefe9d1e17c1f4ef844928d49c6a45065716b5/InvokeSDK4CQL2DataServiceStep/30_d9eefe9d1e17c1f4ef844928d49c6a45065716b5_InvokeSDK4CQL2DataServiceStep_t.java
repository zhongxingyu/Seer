 package org.cagrid.data.styles.cacore4.test.steps;
 
 import gov.nih.nci.cagrid.data.client.DataServiceClient;
 import gov.nih.nci.cagrid.testing.system.deployment.ServiceContainer;
 import gov.nih.nci.cagrid.testing.system.haste.Step;
 
 import java.io.InputStream;
 import java.io.InputStreamReader;
import java.io.StringWriter;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.axis.types.URI;
 import org.apache.axis.types.URI.MalformedURIException;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.cagrid.cql.utilities.CQL2SerializationUtil;
 import org.cagrid.cql.utilities.iterator.CQL2QueryResultsIterator;
 import org.cagrid.cql2.CQLQuery;
 import org.cagrid.cql2.results.CQLQueryResults;
 import org.cagrid.cql2.results.TargetAttribute;
 import org.cagrid.data.test.creation.DataTestCaseInfo;
 
 /** 
  *  InvokeSDK4CQL2DataServiceStep
  *  Step invokes the SDK4 Data service with CQL 2
  *  and verifies results
  * 
  * @author David Ervin
  * 
  * @created Feb 1, 2008 9:02:20 AM
  * @version $Id: InvokeSDK4DataServiceStep.java,v 1.5 2009-01-05 17:54:41 dervin Exp $ 
  */
 public class InvokeSDK4CQL2DataServiceStep extends Step {
     public static final String TEST_RESOURCES_DIR = "/resources/";
     public static final String TEST_QUERIES_DIR = TEST_RESOURCES_DIR + "testQueries/cql2/";
     public static final String TEST_RESULTS_DIR = TEST_RESOURCES_DIR + "testGoldResults/cql2/";
     
     private static Log LOG = LogFactory.getLog(InvokeSDK4CQL2DataServiceStep.class);
     
     private ServiceContainer container;
     private DataTestCaseInfo testInfo;
 
     public InvokeSDK4CQL2DataServiceStep(ServiceContainer container, DataTestCaseInfo testInfo) {
         this.container = container;
         this.testInfo = testInfo;
     }
 
 
     public void runStep() throws Throwable {
         // valid queries
         testAllDisplaysInstanceofLCDMonitor();
         testCountHumans();
         testMaxIdFromHumans();
         testMinIdFromHumans();
         testNamedAttributeFromHumans();
         
         // invalid queries
         testInvalidWrongInheritanceDirectionInstanceof();
     }
     
     
     private void testAllDisplaysInstanceofLCDMonitor() {
         LOG.debug("testAllDisplaysInstanceofLCDMonitor");
         CQLQuery query = loadQuery("allDisplaysInstanceofLCDMonitor.xml");
         CQLQueryResults goldResults = loadQueryResults("goldAllDisplaysInstanceofLCDMonitor.xml");
         invokeValidQueryValidResults(query, goldResults);
     }
     
     
     private void testInvalidWrongInheritanceDirectionInstanceof() {
         LOG.debug("testInvalidWrongInheritanceDirectionInstanceof");
         CQLQuery query = loadQuery("invalid_wrongInheritanceDirectionInstanceof.xml");
         invokeInvalidQuery(query);
     }
     
     
     private void testCountHumans() {
         LOG.debug("testCountHumans");
         CQLQuery query = loadQuery("countHumans.xml");
         CQLQueryResults results = loadQueryResults("goldCountHumans.xml");
         invokeValidQueryValidResults(query, results);
     }
     
     
     private void testMaxIdFromHumans() {
         LOG.debug("testMaxIdFromHumans");
         CQLQuery query = loadQuery("maxIdFromHumans.xml");
         CQLQueryResults results = loadQueryResults("goldMaxIdFromHumans.xml");
         invokeValidQueryValidResults(query, results);
     }
     
     
     private void testMinIdFromHumans() {
         LOG.debug("testMinIdFromHumans");
         CQLQuery query = loadQuery("minIdFromHumans.xml");
         CQLQueryResults results = loadQueryResults("goldMinIdFromHumans.xml");
         invokeValidQueryValidResults(query, results);
     }
     
     
     private void testNamedAttributeFromHumans() {
         LOG.debug("testNamedAttributeFromHumans");
         CQLQuery query = loadQuery("namedAttributeFromHumans.xml");
         CQLQueryResults results = loadQueryResults("goldNamedAttributeFromHumans.xml");
         invokeValidQueryValidResults(query, results);
     }
     
     
     private CQLQuery loadQuery(String filename) {
         String fullFilename = TEST_QUERIES_DIR + filename;
        System.out.println("Loading query from " + fullFilename);
         CQLQuery query = null;
         try {
             InputStream queryInputStream = InvokeSDK4CQL2DataServiceStep.class.getResourceAsStream(fullFilename);
             InputStreamReader reader = new InputStreamReader(queryInputStream);
             query = CQL2SerializationUtil.deserializeCql2Query(reader);
             reader.close();
             queryInputStream.close();
         } catch (Exception ex) {
             ex.printStackTrace();
             fail("Error deserializing query (" + fullFilename + "): " + ex.getMessage());
         }
         assertNotNull("Query was null", query);
         return query;
     }
     
     
     private CQLQueryResults loadQueryResults(String filename)  {
         String fullFilename = TEST_RESULTS_DIR + filename;
        System.out.println("Loading query results from " + fullFilename);
         CQLQueryResults results = null;
         try {
             InputStream resultInputStream = InvokeSDK4CQL2DataServiceStep.class.getResourceAsStream(fullFilename);
             InputStreamReader reader = new InputStreamReader(resultInputStream);
             results = CQL2SerializationUtil.deserializeCql2QueryResults(reader);
             reader.close();
             resultInputStream.close();
         } catch (Exception ex) {
             ex.printStackTrace();
             fail("Error deserializing query results (" + fullFilename + "): " + ex.getMessage());
         }
         assertNotNull("Results were null", results);
         return results;
     }
     
     
     /**
      * Executes a query, which is expected to be valid and compares the
      * results to gold, which is expected to be valid
      * @param query
      *      The query to execute
      * @param goldResults
      *      The gold results set
      */
     private void invokeValidQueryValidResults(CQLQuery query, CQLQueryResults goldResults) {
         DataServiceClient client = getServiceClient();
         CQLQueryResults queryResults = null;
         try {
             if (query.getCQLTargetObject() != null) {
                 System.out.println("Targeting " + query.getCQLTargetObject().getClassName());
             } else {
                 System.out.println("No target!");
             }
             queryResults = client.executeQuery(query);
             // If this fails, we need to still be able to exit the jvm
         } catch (Exception ex) {
             ex.printStackTrace();
             fail("Query failed to execute: " + ex.getMessage());
         }
         compareResults(goldResults, queryResults);
     }
     
     
     /**
      * Executes a query, which is expected to be invalid and fail
      * @param query
      *      The expected invalid query
      */
     private void invokeInvalidQuery(CQLQuery query) {
         DataServiceClient client = getServiceClient();
         try {
             client.executeQuery(query);
             fail("Query returned results, should have failed");
         } catch (Exception ex) {
             // expected
         }
     }
     
     
     private DataServiceClient getServiceClient() {
         DataServiceClient client = null;
         try {
             client = new DataServiceClient(getServiceUrl()); 
         } catch (Exception ex) {
             ex.printStackTrace();
             fail("Error creating data service client: " + ex.getMessage());
         }
         return client;
     }
     
     
     private String getServiceUrl() {
         String url = null;
         try {
             URI baseUri = container.getContainerBaseURI();
             url = baseUri.toString() + "cagrid/" + testInfo.getName();
         } catch (MalformedURIException ex) {
             ex.printStackTrace();
             fail("Error generating service url: " + ex.getMessage());
         }
         LOG.debug("Data service url: " + url);
         return url;
     }
     
     
     private void compareResults(CQLQueryResults gold, CQLQueryResults test) {
         List<Object> goldObjects = new ArrayList<Object>();
         List<Object> testObjects = new ArrayList<Object>();
         
        if (LOG.isDebugEnabled()) {
            try {
                StringWriter writer = new StringWriter();
                writer.write("Expected results:\n");
                CQL2SerializationUtil.serializeCql2QueryResults(gold, writer);
                writer.write("\nTest results:\n");
                CQL2SerializationUtil.serializeCql2QueryResults(test, writer);
                LOG.debug(writer.getBuffer().toString());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
         boolean goldIsAttributes = false;
         CQL2QueryResultsIterator goldIter = new CQL2QueryResultsIterator(gold, getClientConfigStream());
         while (goldIter.hasNext()) {
             Object o = goldIter.next();
             if (o instanceof TargetAttribute[]) {
                 goldIsAttributes = true;
             }
             goldObjects.add(o);
         }
         
         boolean testIsAttributes = false;
         CQL2QueryResultsIterator testIter = new CQL2QueryResultsIterator(test, getClientConfigStream());
         while (testIter.hasNext()) {
             Object o = testIter.next();
             if (o instanceof TargetAttribute[]) {
                 testIsAttributes = true;
             }
             testObjects.add(o);
         }
         
         assertEquals("Number of results differed from expected", goldObjects.size(), testObjects.size());
         assertEquals("Test results as attributes differed from expected", goldIsAttributes, testIsAttributes);
         
         if (goldIsAttributes) {
             List<TargetAttribute[]> goldAttributes = recastList(goldObjects);
             List<TargetAttribute[]> testAttributes = recastList(testObjects);
             compareTargetAttributes(goldAttributes, testAttributes);
         } else {
             compareObjects(goldObjects, testObjects);
         }
     }
     
     
     private void compareObjects(List<Object> gold, List<Object> test) {
         if (!gold.containsAll(test)) {
             // fail, but why?
             List<Object> tempGold = new ArrayList<Object>();
             tempGold.addAll(gold);
             tempGold.removeAll(test);
             StringBuffer errors = new StringBuffer();
             errors.append("The following objects were expected but not found\n");
             dumpGetters(tempGold, errors);
             List<Object> tempTest = new ArrayList<Object>();
             tempTest.addAll(test);
             tempTest.removeAll(gold);
             errors.append("\n\nThe following objects were found, but not expected\n");
             dumpGetters(tempTest, errors);
             fail(errors.toString());
         }
     }
     
     
     private void dumpGetters(Collection<Object> objs, StringBuffer buff) {
         for (Object o : objs) {
             buff.append(o.getClass().getName()).append("\n");
             Method[] methods = o.getClass().getMethods();
             for (Method m : methods) {
                 if (m.getName().startsWith("get") && m.getParameterTypes().length == 0) {
                     try {
                         Object value = m.invoke(o, new Object[0]);
                         buff.append(m.getName());
                         buff.append(" --> ");
                         buff.append(String.valueOf(value));
                         buff.append("\n");
                     } catch (Exception ex) {
                         ex.printStackTrace();
                     }
                 }
             }
         }
     }
     
     
     /**
      * assumes sizes of both lists are equal
      */
     private void compareTargetAttributes(List<TargetAttribute[]> gold, List<TargetAttribute[]> test) {
         // Must find each array of gold attributes (in any order) 
         // in the test attributes list
         
         // sorts attributes for consistency of comparison
         Comparator<TargetAttribute> attributeSorter = new Comparator<TargetAttribute>() {
             public int compare(TargetAttribute o1, TargetAttribute o2) {
                 String att1String = o1.getName() + "=" + o1.getValue();
                 String att2String = o2.getName() + "=" + o2.getValue();
                 return att1String.compareTo(att2String);
             }
         };
         
         // walk the gold attribute arrays
         for (TargetAttribute[] goldAttributes : gold) {
             // sort that array
             Arrays.sort(goldAttributes, attributeSorter);
             
             // find a matching array of attributes in the test set
             Iterator<TargetAttribute[]> testIter = test.iterator();
             while (testIter.hasNext()) {
                 TargetAttribute[] testAttributes = testIter.next();
                 
                 // sort them out
                 Arrays.sort(testAttributes, attributeSorter);
                 
                // verify the same number of attributes.  This should be true for every array
                 assertEquals("Number of attributes differed from expected", goldAttributes.length, testAttributes.length);
                 
                 // check that the current goldAttribute[] matches the test[]
                 boolean matching = true;
                 for (int i = 0; i < goldAttributes.length && matching; i++) {
                     assertEquals("Unexpected attribute name in test results", 
                         goldAttributes[i].getName(), testAttributes[i].getName());
                     String goldValue = goldAttributes[i].getValue();
                     String testValue = testAttributes[i].getValue();
                     matching = String.valueOf(goldValue).equals(String.valueOf(testValue));
                 }
                 if (matching) {
                     // found a matching TargetAttribute[] in test for one in gold.
                     // remove it from the test set so anything left in there is
                     // not a valid result when this process completes
                     testIter.remove();
                 }
             }
         }
         
         if (test.size() != 0) {
             StringBuffer errors = new StringBuffer();
             errors.append("The following attribute arrays were not expected in the test results:");
             for (TargetAttribute[] atts : test) {
                 errors.append("---------\n");
                 for (TargetAttribute ta : atts) {
                     errors.append("Attribute: ").append(ta.getName()).append("\t\tValue: ")
                         .append(ta.getValue()).append("\n");
                 }
             }
             fail(errors.toString());
         }
     }
     
     
     private <T> List<T> recastList(List<?> set) {
         List<T> returnme = new ArrayList<T>();
         for (Object o : set) {
             returnme.add((T) o);
         }
         return returnme;
     }
     
     
     private InputStream getClientConfigStream() {
         InputStream is = null;
         String resourceName = TEST_RESOURCES_DIR + "wsdd/client-config.wsdd";
         try {
             is = InvokeSDK4CQL2DataServiceStep.class.getResourceAsStream(resourceName);
         } catch (Exception ex) {
             ex.printStackTrace();
             fail("Error obtaining client config input stream: " + ex.getMessage());
         }
         return is;
     }
 }
