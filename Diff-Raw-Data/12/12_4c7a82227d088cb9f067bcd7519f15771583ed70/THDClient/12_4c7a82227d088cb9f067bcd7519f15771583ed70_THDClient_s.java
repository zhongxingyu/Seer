 package fr.eurecom.nerd.proxy;
 
 import java.lang.reflect.Type;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.ws.rs.client.Client;
 import javax.ws.rs.client.ClientBuilder;
 import javax.ws.rs.client.Entity;
 import javax.ws.rs.client.WebTarget;
 import javax.ws.rs.core.Form;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 
 import fr.eurecom.nerd.db.table.DocumentType;
 import fr.eurecom.nerd.db.table.TDocument;
 import fr.eurecom.nerd.db.table.TEntity;
 import fr.eurecom.nerd.exceptions.ClientException;
 import fr.eurecom.nerd.logging.LogFactory;
 import fr.eurecom.nerd.ontology.OntoFactory;
 import fr.eurecom.nerd.ontology.OntologyType;
 import fr.eurecom.nerd.srt.SRTMapper;
 
 public class THDClient implements IClient {
 	  private static String SOURCE = Extractor.getName(ExtractorType.THD);
 	    private static String ENDPOINT = "http://ner.vse.cz/thd/api/v1/extraction";
 
 	    public List<TEntity> extract( TDocument document, 
 	                                  String key, 
 	                                  OntologyType otype) 
 	    throws ClientException 
 	    {
 	    	

	    	System.out.println("Key: "+key);
 	    	
 	        LogFactory.logger.info(SOURCE + " is going to extract entities from a document");
 	        String json = post(key, document.getText(), document.getLanguage());
 	        
 	        List<TEntity> result = parse (json, document.getText(), otype);
 
 	        
 	        //if the document is a TIMEDTEXTTYPE then it map the corresponding time interval
 	        if(document.getType().equals(DocumentType.TIMEDTEXTTYPE)) {
 	            SRTMapper srt = new SRTMapper();
 	            result = srt.run(document, result);
 	        }
 
 	        LogFactory.logger.info(SOURCE + " has found #entities=" + result.size());
 	        return result;
 	    }
 	    
 	    private List<TEntity> parse(String json, String text, OntologyType otype) 
 	    {
 	        List<TEntity> result = new LinkedList<TEntity>();
 	        Gson gson = new Gson();
 	        
 	        try {
 
 	            JSONArray  arrayEntities = new JSONArray(json);
 	            //JSONObject response = o.getJSONObject("response");
 	            //String entityjson = response.getJSONArray("entities").toString();
 
 	            //System.out.println(entityjson);
 	            //Type listType = new TypeToken<LinkedList<TextRazorEntity>>() {}.getType();
 	            //List<TextRazorEntity>  entities = gson.fromJson(THDEntity, listType); 
 	            for(int i = 0; i < arrayEntities.length(); i++) 
 	            {
 	            	JSONObject entityJSON =  (JSONObject) arrayEntities.get(i);
 		            Type entityType = new TypeToken<THDEntity>() {}.getType();
 
 		            THDEntity e = gson.fromJson(entityJSON.toString(), entityType);
 	            
 	                Integer startChar = e.getStartOffset();
 	                Integer endChar = e.getEndOffset();
 	                
 	               //LABEL
 	                String label = text.substring(startChar, endChar);
 	                
 	                // we relax a bit this constraint
 	                //if( ! label.equalsIgnoreCase(e.getMatchedText()) )
 	                //    continue;
 
 	               //URL (The most specific one by the moment) May be prioritize those from DBPedia?
 	                String uri = null;
 	                if (e.getTypes() != null){
 		                if (e.getTypes().size()>0){
 		                	uri = e.getTypes().get(e.getTypes().size()-1).entityURI;
 		                }
 	                }	                
 	                
 	                
 	                //EXTRACTOR TYPE (the most specific one)
 	                String extractorType = "null";
 	                if (e.getTypes() != null){
 		                if (e.getTypes().size()>0){
 		                	extractorType = e.getTypes().get(e.getTypes().size()-1).typeURI;
 		                }
 	                }
 	                
 	                //FIXME
 	                //String nerdType (STARTING TO LOOK FOR THE MOST GENERAL ONE, the first one in hierarchy)
 	                String nerdTypeThing = "http://nerd.eurecom.fr/ontology#Thing";
 	                String nerdType = nerdTypeThing;
 	                if (e.getTypes() != null){
 	                	int cont = 0;
 		                while (cont < e.getTypes().size() && nerdType.equals(nerdTypeThing)) {
 		                	
 		                	String currentType = e.getTypes().get(cont).typeURI;
 		                	
 		                	if (currentType == null) currentType = e.getTypes().get(cont).entityURI;
 		                	if (currentType != null){
 			                	//Remove namespace / baseURI
 			                	currentType = currentType.split("/")[currentType.split("/").length-1];
 			                	nerdType = OntoFactory.mapper.getNerdType(otype, label, SOURCE, currentType).toString();
 		                	}
 				            cont ++;
 		                }
 	                }
 	                
 	                
 	                
 	                
 	                //The one from the more specific one.
 	                Double confidence = 0.0;
 	                if (e.getTypes() != null){
 		            	if (e.getTypes().size()>0){
 		            		if (e.getTypes().get(e.getTypes().size()-1).getConfidence() != null){
 		            			confidence = e.getTypes().get(e.getTypes().size()-1).getConfidence().getValue();
 		            		}
 		            	}
 	                }
 	                
 	                TEntity entity = new TEntity(
 	                                             label,
 	                                             extractorType,
 	                                             uri,
 	                                             nerdType,
 	                                             startChar,
 	                                             endChar,
 	                                             confidence,
 	                                             SOURCE
 	                                            );
 	                
 	                //System.out.println(entity.toString());
 	                result.add(entity);
 	                
 	            }
 	            
 	        } catch (JSONException e) {
 	            e.printStackTrace();
 	        }  
 	        return result;
 	    }
 
 	    private String post(String key, String text, String language) 
 	    throws ClientException 
 	    {
 	        
	    	


 

	        
	        
 	        //curl -v "http://ner.vse.cz/thd/api/v1/extraction?apikey=a515172367fc402fa38685d0a3482a7d&format=json&provenance=thd&priority_entity_linking=true&entity_type=all" -d "The Charles Bridge is a famous historic bridge that crosses the Vltava river in Prague, Czech Republic." -H "Accept: application/xml"
 	        Client client = ClientBuilder.newClient();     
 	       
 	        //client.addFilter(new LoggingFilter(System.out));
 	        
 	        WebTarget target = client.target(ENDPOINT);
 	        
 	        target = target.queryParam("apikey", key);
 	        target = target.queryParam("format", "json");
 	        target = target.queryParam("provenance", "thd,dbpedia");
 	        target = target.queryParam("knowledge_base", "linkedHypernymsDataset");
 	        target = target.queryParam("lang", language);
 	        target = target.queryParam("priority_entity_linking", "true");
 	        target = target.queryParam("entity_type", "ne");
 
 
 
 	        
 	        
 	        
 	        Response response = target.request(MediaType.APPLICATION_JSON)
 	        							.post(Entity.entity(
 	                            		text, 
 	                                   MediaType.APPLICATION_XML_TYPE)
 	                             );
 	        
 	        
 	        
 	        if( response.getStatus() != 200 )
 	            throw new ClientException("Extractor: " + SOURCE + " is temporary not available.");
 	        
 	        
 	        String json = response.readEntity(String.class);
	        System.out.println("ESTOOO " + json);
 	        return json;
 
 	    }
 	    
 	    
 	    
 	
 	    
 	    class THDEntity{
 	    	
 	    	private Integer startOffset;
 	    	private Integer endOffset;
 	    	private String underlyingString;
 	    	private String entityType;
 
 	        private List<THDType> types;
 
 			public Integer getStartOffset() {
 				return startOffset;
 			}
 
 			public void setStartOffset(Integer startOffset) {
 				this.startOffset = startOffset;
 			}
 
 			public Integer getEndOffset() {
 				return endOffset;
 			}
 
 			public void setEndOffset(Integer endOffset) {
 				this.endOffset = endOffset;
 			}
 
 			public String getUnderlyingString() {
 				return underlyingString;
 			}
 
 			public void setUnderlyingString(String underlyingString) {
 				this.underlyingString = underlyingString;
 			}
 
 			public String getEntityType() {
 				return entityType;
 			}
 
 			public void setEntityType(String entityType) {
 				this.entityType = entityType;
 			}
 
 			public List<THDType> getTypes() {
 				return types;
 			}
 
 			public void setTypes(List<THDType> types) {
 				this.types = types;
 			}
 
 	    	
 	    }
 	    
 	    
 	    class THDType{
 	    	
 	    	private String entityURI;
 	    	private String typeURI;
 	    	private String entityLabel;
 	    	private String provenance;
 	    	private THDConfidence confidence;
 			public String getEntityURI() {
 				return entityURI;
 			}
 			public void setEntityURI(String entityURI) {
 				this.entityURI = entityURI;
 			}
 			public String getTypeURI() {
 				return typeURI;
 			}
 			public void setTypeURI(String typeURI) {
 				this.typeURI = typeURI;
 			}
 			public String getEntityLabel() {
 				return entityLabel;
 			}
 			public void setEntityLabel(String entityLabel) {
 				this.entityLabel = entityLabel;
 			}
 			public String getProvenance() {
 				return provenance;
 			}
 			public void setProvenance(String provenance) {
 				this.provenance = provenance;
 			}
 			public THDConfidence getConfidence() {
 				return confidence;
 			}
 			public void setConfidence(THDConfidence confidence) {
 				this.confidence = confidence;
 			}
 
 
 	    }
 	    
 	    class THDConfidence{
 	    	
 
 			private String type;
 	    	private String bounds;
 	        private Double value;
 	        
 	    	public String getType() {
 				return type;
 			}
 			public void setType(String type) {
 				this.type = type;
 			}
 			public String getBounds() {
 				return bounds;
 			}
 			public void setBounds(String bounds) {
 				this.bounds = bounds;
 			}
 			public Double getValue() {
 				return value;
 			}
 			public void setValue(Double value) {
 				this.value = value;
 			}
 	    	
 	    }
 }
