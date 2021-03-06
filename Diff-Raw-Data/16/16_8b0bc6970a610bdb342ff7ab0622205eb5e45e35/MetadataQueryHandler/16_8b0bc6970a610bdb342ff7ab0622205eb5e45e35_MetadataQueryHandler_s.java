 package com.buddycloud.channeldirectory.handler.metadata;
 
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import org.apache.solr.client.solrj.SolrQuery;
 import org.apache.solr.client.solrj.SolrServer;
 import org.apache.solr.client.solrj.SolrServerException;
 import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
 import org.apache.solr.client.solrj.response.QueryResponse;
 import org.apache.solr.common.SolrDocument;
 import org.apache.solr.common.SolrDocumentList;
 import org.dom4j.Element;
 import org.xmpp.packet.IQ;
 
 import com.buddycloud.channeldirectory.handler.ChannelQueryHandler;
 import com.buddycloud.channeldirectory.handler.response.ChannelData;
 import com.buddycloud.channeldirectory.handler.response.Geolocation;
 import com.buddycloud.channeldirectory.utils.XMPPUtils;
 
 /**
  * Handles queries for content metadata.
  * A query should contain a metadata query string, so
  * this handle can return channels related to this search.
  *  
  */
 public class MetadataQueryHandler extends ChannelQueryHandler {
 
 	private static final String SOLR_CHANNELCORE_PROP = "solr.channelcore";
 
 	public MetadataQueryHandler(Properties properties) {
		super("urn:oslo:metadatasearch", properties);
 	}
 
 	@Override
 	public IQ handle(IQ iq) {
 		
 		Element queryElement = iq.getElement().element("query");
 		Element searchElement = queryElement.element("search");
 		
 		if (searchElement == null) {
 			return XMPPUtils.error(iq, "Query does not contain search element.", 
 					getLogger());
 		}
 		
 		String search = searchElement.getText();
 		
 		if (search == null || search.isEmpty()) {
 			return XMPPUtils.error(iq, "Search content cannot be empty.", 
 					getLogger());
 		}
 		
 		List<ChannelData> nearbyObjects;
 		try {
 			nearbyObjects = findObjectsByMetadata(search);
 		} catch (Exception e) {
 			return XMPPUtils.error(iq, "Search could not be performed, service is unavailable.", 
 					getLogger());
 		}
 		
 		return createIQResponse(iq, nearbyObjects);
 	}
 
 	private List<ChannelData> findObjectsByMetadata(String search) throws MalformedURLException, SolrServerException {
 		SolrServer solrServer = getSolrServer();
 		SolrQuery solrQuery = new SolrQuery(search);
 		QueryResponse queryResponse = solrServer.query(solrQuery);
 		
 		return convertResponse(queryResponse);
 	}
 	
 	private static List<ChannelData> convertResponse(QueryResponse queryResponse) {
 		List<ChannelData> channels = new ArrayList<ChannelData>();
 		SolrDocumentList results = queryResponse.getResults();
 		
 		for (SolrDocument solrDocument : results) {
 			channels.add(convertDocument(solrDocument));
 		}
 		
 		return channels;
 	}
 
 	private static ChannelData convertDocument(SolrDocument solrDocument) {
 		ChannelData channelData = new ChannelData();
 		String latLonStr = (String) solrDocument.getFieldValue("geoloc");
 		if (latLonStr != null) {
 			String[] latLonSplit = latLonStr.split(",");
 			channelData.setGeolocation(new Geolocation(
 					Double.parseDouble(latLonSplit[0]), 
 					Double.parseDouble(latLonSplit[1])));
 		}
 		
 		channelData.setId((String) solrDocument.getFieldValue("jid"));
 		channelData.setTitle((String) solrDocument.getFieldValue("title"));
 		return channelData;
 	}
 
 	private SolrServer getSolrServer() throws MalformedURLException {
 		String solrChannelUrl = (String) getProperties()
 				.get(SOLR_CHANNELCORE_PROP);
 		SolrServer server = new CommonsHttpSolrServer(solrChannelUrl);
 		return server;
 	}
 }
