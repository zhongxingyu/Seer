 package com.exalead.codesearch.connectors.svn;
 
 import com.exalead.codesearch.connectors.ConnectorTests;
 import com.exalead.codesearch.connectors.helpers.DummyPAPI;
 
 public class SVNConnectorTests extends ConnectorTests {
 
     public void testSimple() throws Exception {
         SVNConnector connector = new SVNConnector(new SVNConnectorConfig().withRepositoryConfig(new RepositoryConfig[] {
                new RepositoryConfig().withName("test").withUrl(getRepositoryFile("svn"))
         }));
         connector.scan(new DummyPAPI(), "full", null);
     }
     
 }
