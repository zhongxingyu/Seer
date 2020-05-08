 /**
  * 
  */
 package work;
 
 
 import java.net.URL;
 
 import junit.framework.TestCase;
 
 import org.junit.Test;
 
 import work.GmExpPozTO.SchemaParser;
 
 /**
  * @author Tomasz.Switek
  *
  */
 public class ExpWzorcreFkSchemaParserTest extends TestCase {
 
 	// GMEXPPOZTO TO SCHEMA
 	@Test
 	public void testPath()
 	{
		String filName1 = "D:\\repo\\Implementacja\\trunk\\amms\\server\\src\\main\\resources\\pl\\asseco\\amms\\services\\apteka\\schema\\data\\ExpFkQueryData.xml";
		String filName2 = "pl/asseco/amms/services/apteka/schema/data/ExpFkQueryData.xml";
 		
 	
 		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
     	URL url = classLoader.getResource(filName1);
     	url = classLoader.getResource(filName2);
 		
     	assertNotNull(url);
 	}
 	
 	@Test
 	public void testConvertDataToSchemaTest()
 	{
 		GmExpPozTO expPoz = new GmExpPozTO();
 		String testSchema = "@~K~#-#~M~SW@NETTO;ALL_RW;@1;^1;@~M~#4444#~SW@~#000#@0@~K~M~#999#~SW@";
 		SchemaParser parser = new SchemaParser();
 		
 		parser.convertSchemaToData(expPoz, testSchema);
 		
 		String newSchema = parser.convertDataToSchema(expPoz);
 		
 		assertEquals(testSchema, newSchema);
 	}	
 
 	// SCHEMA TO GMEXPPOZTO 
 	@Test
 	public void testParserOpisTest()
 	{
 		GmExpPozTO expPoz = new GmExpPozTO();
 		String testSchema = "@~O~#666#~SW@VAT;0;5;DARY_NIE;@84;^0;@@~K~F~#777#@0@";
 		SchemaParser parser = new SchemaParser();
 		
 		parser.convertSchemaToData(expPoz, testSchema);
 	
 		assertNotNull(expPoz.getOpisList());
 		assertTrue(expPoz.getOpisList().size() == 3);
 
 		DictItem diItem = expPoz.getOpisList().get(0);
 		
 		assertEquals(diItem.getCode(), "K");
 
 		diItem = expPoz.getOpisList().get(1);
 		
 		assertEquals(diItem.getCode(), "F");
 
 		diItem = expPoz.getOpisList().get(2);
 	
 		assertEquals(diItem.getCode(), "#");
 		assertEquals(diItem.getDaneDod1(), "777");
 	}	
 	@Test
 	public void testParserExpTypMMTest()
 	{
 		GmExpPozTO expPoz = new GmExpPozTO();
 		String testSchema = "@~O~#666#~SW@VAT;0;5;DARY_NIE;@84;^0;@@~K@0@";
 		SchemaParser parser = new SchemaParser();
 		
 		parser.convertSchemaToData(expPoz, testSchema);
 	
 		assertNotNull(expPoz.getExpTypDokMM());
 		assertEquals(expPoz.getExpTypDokMM(), "0");
 	
 		testSchema = "@~O~#666#~SW@VAT;0;5;DARY_NIE;@84;^0;@@~K@@";
 
 		expPoz = new GmExpPozTO();
 		parser.convertSchemaToData(expPoz, testSchema);
 	
 		assertNull(expPoz.getExpTypDokMM());
 		
 		testSchema = "@~K~#-#~M~SW@NETTO;ALL_RW;@1;^1;@~M~#4444#~SW@~#000#@@~K~M~#999#~SW@";
 		
 		expPoz = new GmExpPozTO();
 		parser.convertSchemaToData(expPoz, testSchema);
 	
 		assertNull(expPoz.getExpTypDokMM());
 	}
 	@Test
 	public void testParserExpRodzajTest()
 	{
 		GmExpPozTO expPoz = new GmExpPozTO();
 		String testSchema = "@~O~#666#~SW@VAT;0;5;DARY_NIE;@84;^0;@@~K@0@";
 		SchemaParser parser = new SchemaParser();
 		
 		parser.convertSchemaToData(expPoz, testSchema);
 	
 		assertNotNull(expPoz.getExpRodzaj());
 		assertEquals(expPoz.getExpRodzaj(), "DARY_NIE");
 	
 	}
 	@Test
 	public void testParserExpWartTest()
 	{
 		GmExpPozTO expPoz = new GmExpPozTO();
 		String testSchema = "@~O~#666#~SW@VAT;0;5;DARY_NIE;@84;^0;@@~K@0@";
 		SchemaParser parser = new SchemaParser();
 		
 		parser.convertSchemaToData(expPoz, testSchema);
 	
 		assertNotNull(expPoz.getExpWartosc());
 		assertEquals(expPoz.getExpWartosc(), "VAT");
 		
 		assertNotNull(expPoz.getWartVatList());
 		assertTrue(expPoz.getWartVatList().size() == 2);
 		
 		DictItem diItem = expPoz.getWartVatList().get(0);
 			
 		assertEquals(diItem.getCode(), "00");
 	
 		diItem = expPoz.getWartVatList().get(1);
 		
 		assertEquals(diItem.getCode(), "05");
 		
 		testSchema = "@~O~#666#~SW@BRUTTO;DARY_NIE;@84;^0;@@~K@0@";
 		expPoz = new GmExpPozTO();
 		parser = new SchemaParser();
 		
 		parser.convertSchemaToData(expPoz, testSchema);
 	
 		assertNotNull(expPoz.getExpWartosc());
 		assertEquals(expPoz.getExpWartosc(), "BRUTTO");
 		
 	} 
 	@Test
 	public void testParserStronaKontaTest()
 	{
 		GmExpPozTO expPoz = new GmExpPozTO();
 		String testSchema = "@~O~#666#~SW@VAT;0;5;DARY_NIE;@84;^0;@@~K@0@";
 		SchemaParser parser = new SchemaParser();
 		
 		parser.convertSchemaToData(expPoz, testSchema);
 	
 		assertNotNull(expPoz.getExpStronaKonta());
 		assertEquals(expPoz.getExpStronaKonta(), "SW");
 	} 
 	@Test
 	public void testParserSymbolKontaTest()
 	{
 		GmExpPozTO expPoz = new GmExpPozTO();
 		String testSchema = "@~O~#666#~SW@VAT;0;5;DARY_NIE;@84;^0;@@~K@0@";
 		SchemaParser parser = new SchemaParser();
 		
 		parser.convertSchemaToData(expPoz, testSchema);
 	
 		assertNotNull(expPoz.getSymbolKontaList());
 		assertTrue(expPoz.getSymbolKontaList().size() == 2);
 
 		DictItem diItem = expPoz.getSymbolKontaList().get(0);
 		
 		assertEquals(diItem.getCode(), "O");
 
 		diItem = expPoz.getSymbolKontaList().get(1);
 	
 		assertEquals(diItem.getCode(), "#");
 		assertEquals(diItem.getDaneDod1(), "666");
 
 		testSchema = "@~O~##~SW@VAT;0;5;DARY_NIE;@84;^0;@@~K@0@";
 		expPoz = new GmExpPozTO();
 		
 		parser.convertSchemaToData(expPoz, testSchema);
 
 		diItem = expPoz.getSymbolKontaList().get(1);
 		
 		assertEquals(diItem.getCode(), "#");
 		assertEquals(diItem.getDaneDod1(), "");
 		
 	}
 	@Test
 	public void testParserMagTest()
 	{
 		GmExpPozTO expPoz = new GmExpPozTO();
 		String testSchema = "@~O~#666#~SW@VAT;0;5;DARY_NIE;@1;58;83;54;^3;4;13;27;28;29;2;@@~K@0@";
 		SchemaParser parser = new SchemaParser();
 		
 		parser.convertSchemaToData(expPoz, testSchema);
 	
 		assertNotNull(expPoz.getMagList());
 		assertTrue(expPoz.getMagList().size() == 4);
 
 		DictItem diItem = expPoz.getMagList().get(0);
 		assertEquals(diItem.getCode(), "1");
 
 		diItem = expPoz.getMagList().get(1);
 		assertEquals(diItem.getCode(), "58");
 
 		diItem = expPoz.getMagList().get(2);
 		assertEquals(diItem.getCode(), "83");
 
 		diItem = expPoz.getMagList().get(3);
 		assertEquals(diItem.getCode(), "54");
 	} 
 	@Test
 	public void testParserDokRodzTest()
 	{
 		GmExpPozTO expPoz = new GmExpPozTO();
 		String testSchema = "@~O~#666#~SW@VAT;0;5;DARY_NIE;@1;58;83;54;^3;4;13;27;28;29;2;@@~K@0@";
 		SchemaParser parser = new SchemaParser();
 		
 		parser.convertSchemaToData(expPoz, testSchema);
 	
 		assertNotNull(expPoz.getDokRodzList());
 		assertTrue(expPoz.getDokRodzList().size() == 7);
 
 		DictItem diItem = expPoz.getDokRodzList().get(0);
 		assertEquals(diItem.getCode(), "3");
 
 		diItem = expPoz.getDokRodzList().get(1);
 		assertEquals(diItem.getCode(), "4");
 
 		diItem = expPoz.getDokRodzList().get(2);
 		assertEquals(diItem.getCode(), "13");
 
 		diItem = expPoz.getDokRodzList().get(3);
 		assertEquals(diItem.getCode(), "27");
 	
 		diItem = expPoz.getDokRodzList().get(4);
 		assertEquals(diItem.getCode(), "28");
 	
 		diItem = expPoz.getDokRodzList().get(5);
 		assertEquals(diItem.getCode(), "29");
 	
 		diItem = expPoz.getDokRodzList().get(6);
 		assertEquals(diItem.getCode(), "2");
 	} 
 
 }
