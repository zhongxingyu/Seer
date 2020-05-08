 package com.loquatic.realtor.solr;
 
 import java.io.IOException;
 
 import junit.framework.Assert;
 
 import org.apache.solr.client.solrj.SolrServer;
 import org.apache.solr.client.solrj.SolrServerException;
 import org.apache.solr.client.solrj.response.SolrPingResponse;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 @RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "file:src/main/webapp/WEB-INF/spring/*-context.xml" })
 public class ServerConfigTest {
 
 	@Autowired
 	private ServerConfig server;
 	
 	@Test
 	public void testServerConnection() throws SolrServerException, IOException{
 		Assert.assertNotNull(server);
 		SolrServer solr = server.getServer();
 		Assert.assertNotNull(solr);
 		SolrPingResponse resp = solr.ping();
 		Assert.assertEquals(0, resp.getStatus());
 	}
 }
