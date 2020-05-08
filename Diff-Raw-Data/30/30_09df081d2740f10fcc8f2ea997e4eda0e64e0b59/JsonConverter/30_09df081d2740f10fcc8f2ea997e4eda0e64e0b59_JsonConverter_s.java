 package it.polimi.deib.streams.oracle.io;
 
 import it.polimi.deib.streams.oracle.io.serializer.LiteralSerializer;
 import it.polimi.deib.streams.oracle.io.serializer.OutputStreamResultSerializer;
 import it.polimi.deib.streams.oracle.io.serializer.TimestampedRelationElementSerializer;
 import it.polimi.deib.streams.oracle.io.serializer.TimestampedRelationSerializer;
 import it.polimi.deib.streams.oracle.io.serializer.URISerializer;
 import it.polimi.deib.streams.oracle.result.OutputStreamResult;
 import it.polimi.deib.streams.oracle.result.TimestampedRelation;
 import it.polimi.deib.streams.oracle.result.TimestampedRelationElement;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Iterator;
 
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.JsonParser;
 import org.codehaus.jackson.JsonProcessingException;
 import org.codehaus.jackson.Version;
 import org.codehaus.jackson.map.DeserializationContext;
 import org.codehaus.jackson.map.JsonDeserializer;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.map.module.SimpleModule;
 import org.openrdf.model.Literal;
 import org.openrdf.model.URI;
 import org.openrdf.model.ValueFactory;
 import org.openrdf.model.impl.ValueFactoryImpl;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class JsonConverter {
 	private ObjectMapper mapper;
 	private static final Logger logger=LoggerFactory.getLogger(JsonConverter.class); 
 	
 	public JsonConverter(){
 		mapper = new ObjectMapper();
 
 		SimpleModule oracleModule = new SimpleModule("OracleModule", new Version(1, 0, 0, null));
 		oracleModule.addSerializer(new URISerializer());
 		oracleModule.addSerializer(new LiteralSerializer());
 		oracleModule.addSerializer(new TimestampedRelationElementSerializer());
 		oracleModule.addSerializer(new TimestampedRelationSerializer());
 		oracleModule.addSerializer(new OutputStreamResultSerializer());
 
 		oracleModule.addDeserializer(OutputStreamResult.class, new JsonDeserializer<OutputStreamResult>(){
 			@Override
 			public OutputStreamResult deserialize(JsonParser jp, DeserializationContext ctxt)
 					throws IOException, JsonProcessingException {
 
 				OutputStreamResult ret = new OutputStreamResult();
 				
 				jp.nextValue();
 
 				JsonNode relations = jp.readValueAsTree();
 				//get the timestamped relations
 				Iterator<JsonNode> rit = relations.iterator();
 				while(rit.hasNext()){
 					TimestampedRelation tr = new TimestampedRelation();
 					JsonNode n = rit.next();
 					tr.setComputationTimestamp(n.get("timestamp").asLong());
 					JsonNode results = n.get("results").get("bindings");
 					JsonNode varsNode = n.get("head").get("vars");
 					String[] vars = new String[varsNode.size()]; 
 					for(int i = 0; i < varsNode.size(); i++)
 						vars[i]=varsNode.get(i).asText();
 
 					Iterator<JsonNode> bit = results.iterator();
 					ValueFactory vf = new ValueFactoryImpl();
 					while(bit.hasNext()){
 						TimestampedRelationElement tre = new TimestampedRelationElement();
 
 						JsonNode element = bit.next();
 						tre.setTimestamp(element.get("timestamp").asLong());
 						for(String var : vars){
 							JsonNode value = element.get("binding").get(var);
 							if(value.get("type").asText().equals("uri")){
 								URI uri = vf.createURI(value.get("value").asText()); 
 										//new URIImpl(value.get("value").asText());
 								tre.add(var, uri);
 							}
 							else if(value.get("type").asText().equals("literal")){
 								Literal literal;
								if(value.get("xml:lang")==null)
 									literal = vf.createLiteral(value.get("xml:lang").asText());
 								else
 									literal = vf.createLiteral(value.get("value").asText());
 								tre.add(var, literal);
 							}
 							else throw new RuntimeException("not a uri or literal");
 						}
 						tr.addElement(tre);
 					}
 					ret.addRelation(tr);
 				}
 				return ret;
 			}
 		});
 		mapper.registerModule(oracleModule);
 	}
 	
 	OutputStreamResult decodeJson(InputStream is){
 		try {
 			OutputStreamResult osr;
 			osr = mapper.readValue(is, OutputStreamResult.class);
 			logger.debug("Parsed stream: {}", osr);
 			return osr;
 		} catch (IOException e) {
 			logger.error("Error while decoding the Json", e);
 		}
 		return null;
 	}
 	
	void encodeJson(OutputStream os, OutputStreamResult res){
 		try {
 			mapper.writeValue(os, res);
 		} catch (IOException e) {
 			logger.error("Error while decoding the Json", e);
 		}
 	}
 }
