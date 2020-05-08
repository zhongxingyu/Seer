 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.requests;
 
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.LinearRing;
 import com.vividsolutions.jts.geom.Polygon;
 import com.vividsolutions.jts.geom.PrecisionModel;
 import junit.framework.Test;
 import junit.framework.TestSuite;
 import org.geotools.feature.Feature;

 
 
 /**
  * Tests the Insert request handling.
  *
  * @author Chris Holmes, TOPP
 * @version $Id: InsertSuite.java,v 1.7 2003/09/19 19:06:25 cholmesny Exp $
  */
 public class InsertSuite extends TransactionSuite {
     // Initializes the logger. Uncomment to see log messages.
     //static {
     //org.vfny.geoserver.config.Log4JFormatter.init("org.vfny.geoserver", 
     //java.util.logging.Level.FINER);
     //}
 
     /**
      * Constructor with super.
      *
      * @param testName The name of the test.
      */
     public InsertSuite(String testName) {
         super(testName);
     }
 
     public static Test suite() {
         TestSuite suite = new TestSuite("Insert tests");
         suite.addTestSuite(InsertSuite.class);
 
         return suite;
     }
 
     /* These tests need a geom_test feature type with an info.xml and
      * an info.xml added to rail to properly work. */
     public void testXml1() throws Exception {
         // make base comparison objects        
         InsertRequest insert = new InsertRequest();
 
         insert.setHandle("insert 1");
         insert.addFeature(testFeature);
 
         TransactionRequest baseRequest = new TransactionRequest();
         baseRequest.addSubRequest(insert);
         baseRequest.setHandle("my insert");
 
         // run test       
        assertTrue(runXmlTest(baseRequest, "insert1", true));
     }
 
     public void testXml2() throws Exception {
         // make base comparison objects        
         InsertRequest insert = new InsertRequest();
         insert.setHandle("insert 2");
         insert.addFeature(testFeature);
 
         TransactionRequest baseRequest = new TransactionRequest();
         baseRequest.addSubRequest(insert);
 
         Coordinate[] points = {
             new Coordinate(5, 5), new Coordinate(5, 15), new Coordinate(15, 15),
             new Coordinate(15, 5), new Coordinate(5, 5)
         };
         PrecisionModel precModel = new PrecisionModel();
         int srid = 2035;
         LinearRing shell = new LinearRing(points, precModel, srid);
         Polygon the_geom = new Polygon(shell, precModel, srid);
 
         Integer featureId = new Integer(23);
         String name = "polygon2";
         Object[] attributes = { featureId, the_geom, name };
 
         //try{
         Feature feature2 = schema.create(attributes, String.valueOf(featureId));
 
         insert.addFeature(feature2);
         baseRequest.setHandle("my second insert");
 
         // run test       
        assertTrue(runXmlTest(baseRequest, "insert2", true));
    }
 
     /*
        public void testDiffFeatures() throws Exception{
            TransactionRequest baseRequest = new TransactionRequest();
            try {
                runXmlTest(baseRequest, "insert3", true);
            } catch (WfsTransactionException e) {
                LOGGER.fine("caught exception: " + e.getMessage());
                assertTrue(e.getMessage().equals("Problem adding features: features"
                                               + " do not match- added typeName: "
                                               + "rail, set typeName: geom_test"));
            }
        }
      */
 }
