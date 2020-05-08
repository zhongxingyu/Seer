 package it.polimi.deib.streams.oracle.io;
 
 import static org.junit.Assert.*;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 
 import it.polimi.deib.streams.oracle.Utility;
 import it.polimi.deib.streams.oracle.result.OutputStreamResult;
 
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class JSonConverterTest {
 	private static final Logger logger = LoggerFactory.getLogger(JSonConverterTest.class);
 
 	@Test public void shouldEncodeResults(){
 		OutputStreamResult oracleResult = new OutputStreamResult();
 		oracleResult.addRelation(Utility.importRelation("timestampedrelation18.properties", logger));
 		oracleResult.addRelation(Utility.importRelation("timestampedrelation19.properties", logger));
 		JsonConverter converter = new JsonConverter();
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		converter.encodeJson(baos, oracleResult);
 		System.out.println(baos.toString());
 		assertEquals(baos.toString(), "{\"relations\":[{\"head\":{\"vars\":[\"sensor\"]},\"timestamp\":18000,\"results\":{\"bindings\":[{\"timestamp\":18000,\"binding\":{\"sensor\":{\"type\":\"uri\",\"value\":\"http://knoesis.wright.edu/ssw/System_C1166\"}}},{\"timestamp\":18000,\"binding\":{\"sensor\":{\"type\":\"uri\",\"value\":\"http://knoesis.wright.edu/ssw/System_C0837\"}}},{\"timestamp\":18000,\"binding\":{\"sensor\":{\"type\":\"uri\",\"value\":\"http://knoesis.wright.edu/ssw/System_C1192\"}}},{\"timestamp\":18000,\"binding\":{\"sensor\":{\"type\":\"uri\",\"value\":\"http://knoesis.wright.edu/ssw/System_C0834\"}}},{\"timestamp\":18000,\"binding\":{\"sensor\":{\"type\":\"uri\",\"value\":\"http://knoesis.wright.edu/ssw/System_C1193\"}}}]}},{\"head\":{\"vars\":[\"sensor\"]},\"timestamp\":19000,\"results\":{\"bindings\":[{\"timestamp\":19000,\"binding\":{\"sensor\":{\"type\":\"uri\",\"value\":\"http://knoesis.wright.edu/ssw/System_C0835\"}}},{\"timestamp\":19000,\"binding\":{\"sensor\":{\"type\":\"uri\",\"value\":\"http://knoesis.wright.edu/ssw/System_C1192\"}}},{\"timestamp\":19000,\"binding\":{\"sensor\":{\"type\":\"uri\",\"value\":\"http://knoesis.wright.edu/ssw/System_C0834\"}}},{\"timestamp\":19000,\"binding\":{\"sensor\":{\"type\":\"uri\",\"value\":\"http://knoesis.wright.edu/ssw/System_C1167\"}}}]}}]}");
 	}
 	
 	@Test public void shouldDecodeResults(){
 		JsonConverter converter = new JsonConverter();
 		
 		String s = "{\"relations\":[{\"head\":{\"vars\":[\"sensor\"]},\"timestamp\":18000,\"results\":{\"bindings\":[{\"timestamp\":18000,\"binding\":{\"sensor\":{\"type\":\"uri\",\"value\":\"http://knoesis.wright.edu/ssw/System_C1166\"}}},{\"timestamp\":18000,\"binding\":{\"sensor\":{\"type\":\"uri\",\"value\":\"http://knoesis.wright.edu/ssw/System_C0837\"}}},{\"timestamp\":18000,\"binding\":{\"sensor\":{\"type\":\"uri\",\"value\":\"http://knoesis.wright.edu/ssw/System_C1192\"}}},{\"timestamp\":18000,\"binding\":{\"sensor\":{\"type\":\"uri\",\"value\":\"http://knoesis.wright.edu/ssw/System_C0834\"}}},{\"timestamp\":18000,\"binding\":{\"sensor\":{\"type\":\"uri\",\"value\":\"http://knoesis.wright.edu/ssw/System_C1193\"}}}]}},{\"head\":{\"vars\":[\"sensor\"]},\"timestamp\":19000,\"results\":{\"bindings\":[{\"timestamp\":19000,\"binding\":{\"sensor\":{\"type\":\"uri\",\"value\":\"http://knoesis.wright.edu/ssw/System_C0835\"}}},{\"timestamp\":19000,\"binding\":{\"sensor\":{\"type\":\"uri\",\"value\":\"http://knoesis.wright.edu/ssw/System_C1192\"}}},{\"timestamp\":19000,\"binding\":{\"sensor\":{\"type\":\"uri\",\"value\":\"http://knoesis.wright.edu/ssw/System_C0834\"}}},{\"timestamp\":19000,\"binding\":{\"sensor\":{\"type\":\"uri\",\"value\":\"http://knoesis.wright.edu/ssw/System_C1167\"}}}]}}]}";
 		InputStream is = new ByteArrayInputStream(s.getBytes());
 		OutputStreamResult result = converter.decodeJson(is);
 		
 		OutputStreamResult oracleResult = new OutputStreamResult();
 		oracleResult.addRelation(Utility.importRelation("timestampedrelation18.properties", logger));
 		oracleResult.addRelation(Utility.importRelation("timestampedrelation19.properties", logger));
 
 		assertEquals(result, oracleResult);
 	}
	
	@Test public void shouldDecodeResultsinFile(){
        JsonConverter converter = new JsonConverter();				
		OutputStreamResult result = converter.decodeJson(
				getClass().getClassLoader().getResourceAsStream("testresult.json"));
		
	}
 }
