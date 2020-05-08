 package org.atosorigin.ctOO;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.xml.sax.SAXException;
 
 //@org.junit.TestSuite
 public class UnitTest
 {
     private WordsParser parser;
 	
     private URL foreignTerms;
 	{
 //		foreignTerms = UnitTest.class.getResource("/terminologie.xml");
 		foreignTerms = UnitTest.class.getResource("/tiny.xml");
 	}
 	
 	@Before
 	public void before() throws IOException, SAXException, ParserConfigurationException
 	{
 		try
 		{
 			InputStream in=new BufferedInputStream(foreignTerms.openStream());
 			parser=WordsParser.createFromXML(in);
 			in.close();
 		} catch (Exception e)
 		{
 			e.printStackTrace();
 			
 		}
 	}
 		
 	@org.junit.Test
 	public void testTiretMerge()
 	{
 		Result[] r;
 
 //		r=parser.find("abc-def ghi jkl-mno");Assert.assertEquals(1,r.length);
 //		r=parser.find("abcdef ghi jkl-mno");Assert.assertEquals(1,r.length);
 //		r=parser.find("abc-def ghi jklmno");Assert.assertEquals(1,r.length);
 //		r=parser.find("abcdef ghi jklmno");Assert.assertEquals(1,r.length);
 //		r=parser.find("abc def ghi jkl mno");Assert.assertEquals(1,r.length);
 		
 		// Vérifie gestion du tiret
 		r=parser.find("e mail e-mail email");
 		Assert.assertEquals(3,r.length);
 		r=parser.find("time-division switching");
 		Assert.assertEquals(2,r.length);
 		r=parser.find("timedivision switching");
 		Assert.assertEquals(1,r.length);
 		r=parser.find("time division switching");
 		Assert.assertEquals(2,r.length);
 	}
 	
 	@org.junit.Test
 	public void testOrder()
 	{
 		Result[] r;
 
 		// Vérifie le classement des résultats
 		r=parser.find("cell library");
 		Assert.assertEquals(2,r.length);
 		Assert.assertTrue(r[0].len>r[1].len);
 	}
 	
 	@org.junit.Test
 	public void testPlurial()
 	{
 		Result[] r;
 		
 		// Vérifie la gestion des pluriels
 		r=parser.find("databases");
 		Assert.assertEquals(1,r.length);
 		Assert.assertEquals(0,r[0].start);
 		Assert.assertEquals(9,r[0].len);
 	}
 	@org.junit.Test
 	public void testTiret()
 	{
 		Result[] r;
 
 		// Vérifie les tirets
 		r=parser.find("back-up");
 		Assert.assertEquals(1, r.length);
 		Assert.assertEquals(0,r[0].start);
 		Assert.assertEquals(7,r[0].len);
 	}
 	@org.junit.Test
 	public void testDouble()
 	{
 		Result[] r;
 
 		// Vérifie les doublons
 		r=parser.find("database database");
 		Assert.assertEquals(2,r.length);
 		Assert.assertEquals(0,r[0].start);
 		Assert.assertEquals(9,r[1].start);
 		Assert.assertEquals(2,parser.find("bit et bit").length);
 	}
 
	@org.junit.Test
	public void testPrefixeNumerique()
	{
		Result[] r;

		// Vérifie les doublons
		r=parser.find("3.2database");
		Assert.assertEquals(1,r.length);
		Assert.assertEquals(0,r[0].start);
		Assert.assertEquals(9,r[1].start);
		Assert.assertEquals(2,parser.find("bit et bit").length);
	}

 }
