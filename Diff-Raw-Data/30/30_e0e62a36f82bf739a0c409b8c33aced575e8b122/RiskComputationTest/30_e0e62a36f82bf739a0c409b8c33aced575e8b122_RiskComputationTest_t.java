 /*
  *  Copyright (C) 2007-2012 GeoSolutions S.A.S.
  *  http://www.geo-solutions.it
  *
  *  GPLv3 + Classpath exception
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package it.geosolutions.geobatch.destination.vulnerability;
 
 import it.geosolutions.geobatch.destination.common.OutputObject;
 import it.geosolutions.geobatch.destination.commons.DestinationOnlineTestCase;
 import it.geosolutions.geobatch.flow.event.ProgressListenerForwarder;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import org.geotools.jdbc.JDBCDataStoreFactory;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * @author DamianoG
  *
  */
 public class RiskComputationTest extends DestinationOnlineTestCase{
 
     @Before
     public void before() throws Exception{
         //Setup the origin table... this table schema will be copied and replicated into the test table
         setOriginTable("siig_geo_ln_arco_1");
         // The test table, where the tests run
         testTable = "siig_geo_ln_arcotest_1";
         super.before();
     }
     
     @Test
     public void testRiskComputationProcess() throws IOException {
     	int aggregationLevel = 2;
 		RiskComputation riskComputation = new RiskComputation(aggregationLevel,  new ProgressListenerForwarder(null));
     	
         Map<String, Serializable> datastoreParams = new HashMap<String, Serializable>();
         datastoreParams.put(JDBCDataStoreFactory.DBTYPE.key, "postgis");
         datastoreParams.put(JDBCDataStoreFactory.HOST.key, getFixture().getProperty("pg_host"));
         datastoreParams.put(JDBCDataStoreFactory.PORT.key, getFixture().getProperty("pg_port"));
         datastoreParams.put(JDBCDataStoreFactory.SCHEMA.key, getFixture().getProperty("pg_schema"));
         datastoreParams.put(JDBCDataStoreFactory.DATABASE.key, getFixture().getProperty("pg_database"));
         datastoreParams.put(JDBCDataStoreFactory.USER.key, getFixture().getProperty("pg_user"));
         datastoreParams.put(JDBCDataStoreFactory.PASSWD.key, getFixture().getProperty("pg_password"));
         
        riskComputation.prefetchRiskAtLevel(datastoreParams, 3, aggregationLevel, 1, 26, 100, "1,2,3,4,5,6,7,8,9,10", "1,2,3,4,5,6,7,8,9,10,11", "0,1", "1,2,3,4,5", "fp_scen_centrale", 1, "UPDATE");
     }
     
     @Override
     protected void loadFeature(OutputObject objOut) throws IOException{
     }
     
     @Override
     protected String getFixtureId() {
         return "destination";
     }
     
     @Override
     protected Properties createExampleFixture() {
         Properties ret = new Properties();
         for (Map.Entry entry : getExamplePostgisProps().entrySet()) {
             ret.setProperty(entry.getKey().toString(), entry.getValue().toString());
         }
         return ret;
     }
 
 }
