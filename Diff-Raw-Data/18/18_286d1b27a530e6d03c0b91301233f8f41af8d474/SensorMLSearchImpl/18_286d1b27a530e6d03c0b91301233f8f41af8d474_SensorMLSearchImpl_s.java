 package net.prep.solr.sensorml.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.management.Query;
 
 import org.apache.solr.client.solrj.SolrQuery;
 import org.apache.solr.client.solrj.SolrServer;
 import org.apache.solr.client.solrj.impl.HttpSolrServer;
 import org.apache.solr.client.solrj.response.QueryResponse;
 import org.apache.solr.common.SolrDocumentList;
 import org.apache.solr.common.SolrInputDocument;
 
 import net.prep.solr.sensorml.Constants;
 import net.prep.solr.sensorml.SensorMLSearch;
 
 public class SensorMLSearchImpl implements SensorMLSearch {
 
 	public List<Object> getForQuery(String query) throws Exception {
 		// TODO Auto-generated method stub
 		SolrServer server = new HttpSolrServer(Constants.URL);
 		SolrQuery solrquery = new SolrQuery();
		solrquery.setQuery("*");
 		QueryResponse response = server.query(solrquery);
 		SolrDocumentList list = response.getResults();
 		
 		long n = list.getNumFound();
 		List<Object> result_list = new ArrayList<Object>();
 		for(int i=0;i<n;i++)
 			result_list.add(list.get(i));
 		return result_list;
 
 	}
 
 }
