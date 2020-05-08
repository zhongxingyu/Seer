 package cz.cuni.mff.odcleanstore.configuration;
 
 import static org.junit.Assert.assertEquals;
 
 import cz.cuni.mff.odcleanstore.configuration.ConfigGroup.EnumDbConnectionType;
 import cz.cuni.mff.odcleanstore.connection.JDBCConnectionCredentials;
 import cz.cuni.mff.odcleanstore.connection.SparqlEndpointConnectionCredentials;
 
 import org.mockito.Mockito;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Properties;
 
 /**
  * Base class for config tests.
  * @author Jan Michelfeit
  */
 public class ConfigTestBase {
     protected void mockSparqlEndpointsConnectionCredentials(
             Properties properties, EnumDbConnectionType dbType, boolean credentials) {
         
        Mockito.when(properties.getProperty(dbType.getConfigPrefix() + "sparql_endpoint_url")).thenReturn(
                 "http://www.sparql.cz/" + dbType.name());
         if (credentials) {
            Mockito.when(properties.getProperty(dbType.getConfigPrefix() + "sparql_endpoint_username")).thenReturn("username");
            Mockito.when(properties.getProperty(dbType.getConfigPrefix() + "sparql_endpoint_password")).thenReturn("password");
         }
     }
 
     protected void checkSparqlEndpointsConnectionCredentials(
             SparqlEndpointConnectionCredentials sparqlCredentials, EnumDbConnectionType dbType, boolean credentials) 
             throws MalformedURLException {
         
         assertEquals(new URL("http://www.sparql.cz/" + dbType.name()), sparqlCredentials.getUrl());
         if (credentials) {
             assertEquals("username", sparqlCredentials.getUsername());            
             assertEquals("password", sparqlCredentials.getPassword());            
         }
     }
 
     protected void mockJDBCConnectionCredentials(Properties properties, EnumDbConnectionType dbType) {
        Mockito.when(properties.getProperty(dbType.getConfigPrefix() + "jdbc_connection_string"))
                 .thenReturn("jdbc:virtuoso://localhost:1112");
 
        Mockito.when(properties.getProperty(dbType.getConfigPrefix() + "jdbc_username")).thenReturn("dba");
 
        Mockito.when(properties.getProperty(dbType.getConfigPrefix() + "jdbc_password")).thenReturn("dba");
     }
 
     protected void checkJDBCConnectionCredentials(JDBCConnectionCredentials credentials, EnumDbConnectionType dbType)
             throws MalformedURLException {
         assertEquals("jdbc:virtuoso://localhost:1112", credentials.getConnectionString());
         assertEquals("dba", credentials.getUsername());
         assertEquals("dba", credentials.getPassword());
     }
 }
