package com.votr;

 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.utils.URIUtils;
 import org.apache.http.entity.ByteArrayEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicHeader;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 
 import com.amazonaws.auth.AWSCredentials;
 import com.amazonaws.auth.PropertiesCredentials;
 import com.amazonaws.services.dynamodb.AmazonDynamoDBClient;
 import com.amazonaws.services.dynamodb.model.AttributeValue;
 import com.amazonaws.services.dynamodb.model.ScanRequest;
 import com.amazonaws.services.dynamodb.model.ScanResult;
 
 
 public class indexUploader {
 	private final HttpClient client = new DefaultHttpClient();
 	private final AmazonDynamoDBClient dynamoDB = createClient();
 	
 	public static void main(String[] args){
 		indexUploader uploader = new indexUploader();
 		//uploader.loadTable();
 		
 		uploader.search();
 		
 	}
 	
 	public void search() {
 		try {
 			//URI uri = URIUtils.createURI("http", "search-test-xnrvb2xw2rc2iq76porynyunqy.us-east-1.cloudsearch.amazonaws.com", 80, "/2011-02-01/search",
 			//		URLEncoder.encode("bq=(-voter_id:'*')","UTF-8"), null);
 		// todo:add facets
 		// &facet=zipcode_count,city_count,choice_count,state_count,tags_count
 			//System.out.println(uri);
 			
 			String uri = "http://search-test-xnrvb2xw2rc2iq76porynyunqy.us-east-1.cloudsearch.amazonaws.com/2011-02-01/search?" +
 					"bq=(not%20voter_id:'1')" +
 					"&facet=zipcode_count,city_count,choice_count,state_count,tags_count";
 			
 			HttpGet get = new HttpGet(uri);
 			//HttpGet get = new HttpGet(uri.toString().replaceAll("\\+", "%20"));
 
 			/*
 			get.addHeader(new BasicHeader("Host", "doc-test-xnrvb2xw2rc2iq76porynyunqy.us-east-1.cloudsearch.amazonaws.com"));
 			get.addHeader(new BasicHeader("Accept", "application/json"));
 			
 			try {
 				get.setURI(new URI("http://search-test-xnrvb2xw2rc2iq76porynyunqy.us-east-1.cloudsearch.amazonaws.com/2011-02-01/search/?q=star+wars"));
 			} catch (URISyntaxException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 			*/
 	
 		    ObjectMapper mapper = new ObjectMapper();
 		    String json ="";
 
 	        HttpResponse response = client.execute(get);
 	        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
 	        String line = "";
 		    while ((line = rd.readLine()) != null) {
 		          System.out.println("line= " + line);
 		          json = line;
 		    }
 		    
 		    
 		    // todo: convert JSON object to Vote object
 		    JsonNode node = mapper.readTree(json);
 		    System.out.println(node.findValues("tags_count").get(0));
 		        
 		    } catch (Exception e) {
 		    	e.printStackTrace();
 		    }
 		
 	}
 	
 	public void loadTable() {
 		ScanRequest scanRequest = new ScanRequest().withTableName("Votes");
 		ScanResult result = dynamoDB.scan(scanRequest);
 		
 		List<Vote> votes = new ArrayList<Vote>();
 		
 		for (Map<String, AttributeValue> item : result.getItems()){
 			Vote v = new Vote(item.get("voter_id").getS(), 
 					item.get("poll_id").getS(),
 					(item.get("choice") == null) ? null : item.get("choice").getN(), 
 					(item.get("city") == null) ? null : item.get("city").getS(), 
 					(item.get("state") == null) ? null : item.get("state").getS(), 
 					(item.get("zipcode") == null) ? null : item.get("zipcode").getS(), 
 					(item.get("tags") == null) ? null : item.get("tags").getSS());
 			votes.add(v);
 			System.out.println("new: "+ item);
 		}
 		
 		indexVote(votes, (int) System.currentTimeMillis());
 
 	}
 	
     private AmazonDynamoDBClient createClient() {
         AWSCredentials credentials;
 		try {
 			credentials = new PropertiesCredentials(
 			        LoadDynamoDb.class.getResourceAsStream("AwsCredentials.properties"));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			throw new RuntimeException(e);
 		}
 
         AmazonDynamoDBClient dynamoDB = new AmazonDynamoDBClient(credentials);
         dynamoDB.setEndpoint("https://dynamodb.us-west-1.amazonaws.com");
         
         return dynamoDB;
     }
 	
 	public void indexVote(List<Vote> votes, int version) {
 		
 		HttpPost post = new HttpPost("http://doc-test-xnrvb2xw2rc2iq76porynyunqy.us-east-1.cloudsearch.amazonaws.com");
 		post.addHeader(new BasicHeader("Content-Type", "application/json"));
 		post.addHeader(new BasicHeader("Host", "doc-test-xnrvb2xw2rc2iq76porynyunqy.us-east-1.cloudsearch.amazonaws.com"));
 		post.addHeader(new BasicHeader("Accept", "application/json"));
 
 		
 	    try {
 			post.setURI(new URI("http://doc-test-xnrvb2xw2rc2iq76porynyunqy.us-east-1.cloudsearch.amazonaws.com/2011-02-01/documents/batch"));
 
 	    	ObjectMapper mapper = new ObjectMapper();
 	    	List<Object> docs = new ArrayList<Object>();
 	    	
 		    for (Vote vote : votes) {
 		    	String doc_id = (new Integer(Math.abs(vote.voter_id.hashCode()))).toString() + (new Integer(Math.abs(vote.poll_id.hashCode()))).toString();
 		    	Map<String,Object> doc = new HashMap<String,Object>();
 		    	doc.put("type", "add");
 		    	doc.put("id", doc_id);
 		    	doc.put("version", version);
 		    	doc.put("lang", "en");
 	
 		    	Map<String,Object> fields = new HashMap<String,Object>();
 		    	fields.put("voter_id", vote.voter_id);
 		    	fields.put("poll_id", vote.poll_id);
 		    	fields.put("choice", (vote.choice == null) ? "" : vote.choice);
 		    	fields.put("choice_count", (vote.choice == null) ? "" : vote.choice);
 		    	fields.put("city", (vote.city == null) ? "" : vote.city);
 		    	fields.put("city_count", (vote.city == null) ? "" :vote.city);
 		    	fields.put("state", (vote.state == null) ? "" : vote.state);
 		    	fields.put("state_count", (vote.state == null) ? "" : vote.state);
 		    	fields.put("zipcode", (vote.zipcode == null) ? "" : vote.zipcode);
 		    	fields.put("zipcode_count", (vote.zipcode == null) ? "" : vote.zipcode);
 		    	fields.put("tags", (vote.tags == null) ? "" : vote.tags);
 		    	fields.put("tags_count", (vote.tags == null) ? "" : vote.tags);
 		    	
 		    	doc.put("fields", fields);
 		    	docs.add(doc);
 	
 		    }
 	    	
 	    	byte[] json = mapper.writeValueAsBytes(docs);
 	    	
 	    	System.out.println(new String(json));
 	    	
 	    	post.setEntity(new ByteArrayEntity(json));
 
 	        HttpResponse response = client.execute(post);
 	        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
 	        String line = "";
 	        while ((line = rd.readLine()) != null) {
 	          System.out.println(line);
 	        }
 	        
 
 	      } catch (Exception e) {
 	        throw new RuntimeException(e);
 	      }
 	}
 }
