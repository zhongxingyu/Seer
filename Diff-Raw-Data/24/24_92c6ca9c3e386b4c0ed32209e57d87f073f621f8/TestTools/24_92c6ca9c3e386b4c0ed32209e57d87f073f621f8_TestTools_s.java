 package no.ntnu.ai.classifiers.dtc.test;
 
import static org.junit.Assert.fail;
 
 import java.util.List;
 
 import no.ntnu.ai.classifiers.dtc.DTCTools;
 import no.ntnu.ai.data.DataElement;
 import no.ntnu.ai.file.parser.FileParser;
 import no.ntnu.ai.filter.GlassFilter;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class TestTools {
 
	private static List<DataElement<Integer, Integer>> data;
 	
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		FileParser parser = new FileParser();
		parser.initialize("training sets/glass.txt");
 		GlassFilter filter = new GlassFilter();
		data = filter.convert(parser.getData());
 	}
 
 	@Test
 	public void testEntropy() {
 		DTCTools<Integer, Integer> tool = new DTCTools<Integer, Integer>();
		System.out.println(tool.entropy(data));
		fail("Not yet implemented");
 	}
 
 }
