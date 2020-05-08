 package it.uniroma3.dia.alfred.mpi;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class OutputParserTest {
 	private static final String CONF_FILE = "examples/configuration.properties";
 	private static final String DOMAIN_FILE = "examples/domains.properties";
 	private static final String OUTMPI_PATH = "examples/output_mpi/";
 	private static final String TEST_PATH = "examples/testOutputParser";
 	private static final String ATTORI_FILE = "attori.json";
 	private static final String MOVIES_FILE = "movies.json";
 	
 	private static final String EXPECTED_ATTORI_RES = "{\"xpaths\":{\"Name\":[\"//*[@itemprop='name']/text()\",\"/HTML/BODY[1]/DIV[3]/DIV[1]/DIV[4]/DIV[3]/DIV[2]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/DIV[1]/DIV[1]/H1[1]/text()[1]\"],\"AltName\":[\"//*[@itemprop='name']/text()\",\"//H1[@class='header'][1]/text()[1]\"]}}";
 	private static final String EXPECTED_MOVIE_RES = "{\"xpaths\":{\"Rating\":[\"//*[@itemprop='ratingValue']/text()\",\"/HTML/BODY[1]/DIV[3]/DIV[1]/DIV[6]/DIV[3]/DIV[2]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/DIV[3]/DIV[1]/text()[1]\"],\"Title\":[\"/HTML/BODY[1]/DIV[3]/DIV[1]/DIV[6]/DIV[3]/DIV[2]/DIV[1]/DIV[1]/TABLE[1]/TBODY[1]/TR[1]/TD[2]/H1[1]/text()[1]\",\"//*[@itemprop='name']/text()\"]}}";
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		setup();
 		OutputParser.parse(
 				ConfiguratorParser.readConfig(CONF_FILE, DOMAIN_FILE),
 				OUTMPI_PATH,
 				TEST_PATH
 			);
 	}
 	
 	private static void setup() {
		if (new File(TEST_PATH + "/" + ATTORI_FILE).exists()){
			new File(TEST_PATH + "/" + ATTORI_FILE).delete();
 		}
		if (new File(TEST_PATH + "/" + MOVIES_FILE).exists()){
			new File(TEST_PATH + "/" + MOVIES_FILE).delete();
 		}
 	}
 
 	@Test
 	public void testParse() {
 		assertTrue(new File(TEST_PATH).exists());
 		assertTrue(new File(TEST_PATH).isDirectory());
 		assertNotNull(new File(TEST_PATH).listFiles());
 		assertTrue(new File(TEST_PATH + "/" + ATTORI_FILE).exists());
 		assertTrue(new File(TEST_PATH + "/" + MOVIES_FILE).exists());
 		
 		BufferedReader reader = null;
 		
 		try {
 			reader = new BufferedReader(new FileReader(TEST_PATH + "/" + ATTORI_FILE));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			fail();
 		}
 		
 		String line = null;
 		
 		try {
 			line = reader.readLine();
 		} catch (IOException e) {
 			e.printStackTrace();
 			fail();
 		}
 		
 		assertNotNull(line);
 		assertEquals(EXPECTED_ATTORI_RES, line);
 		
 		try {
 			line = reader.readLine();
 		} catch (IOException e) {
 			e.printStackTrace();
 			fail();
 		}
 		
 		assertNull(line);
 		
 		try {
 			reader = new BufferedReader(new FileReader(TEST_PATH + "/" + MOVIES_FILE));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			fail();
 		}
 		
 		try {
 			line = reader.readLine();
 		} catch (IOException e) {
 			e.printStackTrace();
 			fail();
 		}
 		
 		assertNotNull(line);
 		assertEquals(EXPECTED_MOVIE_RES, line);
 		
 		try {
 			line = reader.readLine();
 		} catch (IOException e) {
 			e.printStackTrace();
 			fail();
 		}
 		
 		assertNull(line);
 	}
 }
