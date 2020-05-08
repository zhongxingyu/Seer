 package de.uni_leipzig.simba.limeswebservice.server;
 
 import java.util.HashMap;
 
 import org.junit.Test;
 
 public class LimesServiceImplTest {
 
 	@Test
 	public void testGetMapping() {
 		HashMap<String, Object> src = new HashMap();
 		src.put("endpoint", "http://live.dbpedia.org/sparql");
 		src.put("var", "?src");
 		src.put("graph", "http://dbpedia.org");
 		src.put("id", "dbpedia");
 		src.put("class", "http://dbpedia.org/ontology/Architect");
 		HashMap<String, String> prefix = new HashMap();
 		prefix.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
 		src.put("prefix", prefix);
 		HashMap<String, String> props = new HashMap();
 		props.put("rdfs:label", "lowercase");
 		src.put("properties", props);
 		
 		HashMap<String, Object> dest = new HashMap();
 		dest.put("endpoint", "http://live.dbpedia.org/sparql");
 		dest.put("var", "?dest");
 		dest.put("graph", "http://dbpedia.org");
 		dest.put("id", "dbpedia");
	//	dest.put("class", "http://dbpedia.org/ontology/Architect");
 		HashMap<String, String> prefix2 = new HashMap();
 		prefix2.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
 		dest.put("prefix", prefix2);
 		HashMap<String, String> props2 = new HashMap();
 		props2.put("rdfs:label", "lowercase");
 		dest.put("properties", props2);
 		
 		HashMap<String, Object> metric = new HashMap();
 		metric.put("metric", "trigram(src.rdfs:label, dest.rdfs:label)");
 		metric.put("accthreshold", 0.9d);
 		metric.put("verthreshold", 0.8d);
 		
 		LimesServiceImpl impl = new LimesServiceImpl();
 		impl.getMapping(src, dest, metric);
 		
 	}
 }
 //http://dbpedia.org/ontology/Person
 //rdfs:label with prefix rdfs - http://www.w3.org/2000/01/rdf-schema#
