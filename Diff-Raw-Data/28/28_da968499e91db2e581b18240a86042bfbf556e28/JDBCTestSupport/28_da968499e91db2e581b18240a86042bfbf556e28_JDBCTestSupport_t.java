 /*
  *    GeoTools - The Open Source Java GIS Toolkit
  *    http://geotools.org
  *
  *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
  *
  *    This library is free software; you can redistribute it and/or
  *    modify it under the terms of the GNU Lesser General Public
  *    License as published by the Free Software Foundation;
  *    version 2.1 of the License.
  *
  *    This library is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *    Lesser General Public License for more details.
  */
 package org.geotools.jdbc;
 
 import java.sql.Connection;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.sql.DataSource;
 
 import junit.framework.TestCase;
 import junit.framework.TestResult;
 
 import org.geotools.feature.LenientFeatureFactoryImpl;
 import org.geotools.feature.type.FeatureTypeFactoryImpl;
 import org.geotools.filter.FilterFactoryImpl;
 
 import com.vividsolutions.jts.geom.GeometryFactory;
 
 
 /**
  * Test support class for jdbc test cases.
  * <p>
  * This test class fires up a live instance of an h2 database to provide a
  * live database to work with.
  * </p>
  *
  * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
  *
  */
 public abstract class JDBCTestSupport extends TestCase {
     /**
      * map of test setup class to boolean which tracks which 
      * setups can obtain a connection and which cannot
      */
     static Map dataSourceAvailable = new HashMap();
     
     static {
         //turn up logging
         /*
         java.util.logging.ConsoleHandler handler = new java.util.logging.ConsoleHandler();
         handler.setLevel(java.util.logging.Level.FINE);
         
         org.geotools.util.logging.Logging.getLogger("org.geotools.data.jdbc").setLevel(java.util.logging.Level.FINE);
         org.geotools.util.logging.Logging.getLogger("org.geotools.data.jdbc").addHandler(handler);
         
         org.geotools.util.logging.Logging.getLogger("org.geotools.jdbc").setLevel(java.util.logging.Level.FINE);
         org.geotools.util.logging.Logging.getLogger("org.geotools.jdbc").addHandler(handler);
         */ 
     }
 
     protected JDBCTestSetup setup;
     protected JDBCDataStore dataStore;
     protected SQLDialect dialect;
     
     /**
      * Override to check if a database connection can be obtained, if not
      * tests are ignored.
      */
     public void run(TestResult result) {
         JDBCTestSetup setup = createTestSetup();
         
         //check if the data source is available for this setup
         Boolean available = 
             (Boolean) dataSourceAvailable.get( setup.getClass() );
         if ( available == null || available.booleanValue() ) {
             //test the connection
             try {
                 DataSource dataSource = setup.createDataSource();
                 Connection cx = dataSource.getConnection();
                 cx.close();
             } catch (Throwable t) {
                 System.out.println("Skipping tests " + getClass().getName() + " since data souce is not available: " + t.getMessage());
                 dataSourceAvailable.put( setup.getClass(), Boolean.FALSE );
                 return;
             }
             
             super.run(result);
         }
     }
     
     protected String tname( String raw ) {
         return setup.typeName( raw );
     }
     
     protected String aname( String raw ) {
         return setup.attributeName( raw );
     }
 
     protected void setUp() throws Exception {
         super.setUp();
 
         //create the test harness
         if (setup == null) {
             setup = createTestSetup();
         }
 
         setup.setUp();
 
         //initialize the database
         setup.initializeDatabase();
 
         //initialize the data
         setup.setUpData();
 
         //create the dataStore
         //TODO: replace this with call to datastore factory
         dataStore = new JDBCDataStore();
         dataStore.setSQLDialect(setup.createSQLDialect(dataStore));
         dataStore.setNamespaceURI("http://www.geotools.org/test");
         dataStore.setDataSource(setup.getDataSource());
         dataStore.setDatabaseSchema("geotools");
         dataStore.setFilterFactory(new FilterFactoryImpl());
         dataStore.setGeometryFactory(new GeometryFactory());
         dataStore.setFeatureFactory(new LenientFeatureFactoryImpl());
         dataStore.setFeatureTypeFactory(new FeatureTypeFactoryImpl());
 
         setup.setUpDataStore(dataStore);
         
         dialect = dataStore.getSQLDialect();
     }
 
     protected abstract JDBCTestSetup createTestSetup();
 
     protected void tearDown() throws Exception {
         dataStore.dispose();
        setup.tearDown();
        super.tearDown();
     }
 }
