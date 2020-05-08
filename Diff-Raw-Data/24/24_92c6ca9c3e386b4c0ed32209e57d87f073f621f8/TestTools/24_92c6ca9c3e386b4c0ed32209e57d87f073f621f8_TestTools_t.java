 package no.ntnu.ai.classifiers.dtc.test;
 
import static org.junit.Assert.*;
 
 import java.util.List;
 

 import no.ntnu.ai.classifiers.dtc.DTCTools;
 import no.ntnu.ai.data.DataElement;
 import no.ntnu.ai.file.parser.FileParser;
 import no.ntnu.ai.filter.GlassFilter;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class TestTools {
 
	private static List<DataElement<Integer, Integer>> data2;
 	
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		FileParser parser = new FileParser();
		parser.initialize("training sets/test.txt");	
 		GlassFilter filter = new GlassFilter();
		data2 = filter.convert(parser.getData());
 	}
 
 	@Test
 	public void testEntropy() {
 		DTCTools<Integer, Integer> tool = new DTCTools<Integer, Integer>();
		assertEquals(0.954, tool.entropy(data2), 0.0009);
 	}
 
 }
