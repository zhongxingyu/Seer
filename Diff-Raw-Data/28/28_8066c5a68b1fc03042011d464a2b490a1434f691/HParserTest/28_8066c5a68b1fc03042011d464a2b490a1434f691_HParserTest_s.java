 /*
     Copyright 2008 San Jose State University
     
     This file is part of the Blackberry Cinequest client.
 
     The Blackberry Cinequest client is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     The Blackberry Cinequest client is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with the Blackberry Cinequest client.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package edu.sjsu.cinequest.comm;
 
 import java.util.Vector;
 
 import junit.framework.TestCase;
 import edu.sjsu.cinequest.comm.cinequestitem.Film;
 import edu.sjsu.cinequest.comm.cinequestitem.ProgramItem;
 import edu.sjsu.cinequest.javase.JavaSEPlatform;
 
 public class HParserTest extends TestCase
 {
     public static final int BOLD = 2;
     public static final int ITALIC = 1;
     public static final int LARGE = 4;
     private QueryManager mgr;
     
     protected void setUp() throws Exception
     {
         Platform.setInstance(new JavaSEPlatform());
         mgr = new QueryManager();
     }
 
     public void testSimpleString()
     {
         String input = "simple string";
         HParser parser = new HParser();
         parser.parse(input);
         assertEquals(input, parser.getResultString());
         int[] offsets = parser.getOffsets();
         assertEquals(2, offsets.length);
         assertEquals(0, offsets[0]);
         assertEquals(input.length(), offsets[1]);
         byte[] attrs = parser.getAttributes();
         assertEquals(offsets.length - 1, attrs.length);
         assertEquals(0, attrs[0]);
     }
 
     public void testOneBoldInMiddle()
     {
         String input = "one <b>bold</b> test";
         HParser parser = new HParser();
         parser.parse(input);
         String expectedResult = "one bold test";
         assertEquals(expectedResult, parser.getResultString());
         int[] offsets = parser.getOffsets();
         assertEquals(4, offsets.length);
         assertEquals(0, offsets[0]);
         assertEquals(4, offsets[1]);
         assertEquals(8, offsets[2]);
         assertEquals(expectedResult.length(), offsets[offsets.length - 1]);
         byte[] attrs = parser.getAttributes();
         assertEquals(offsets.length - 1, attrs.length);
         assertEquals(0, attrs[0]);
         assertEquals(BOLD, attrs[1]);
         assertEquals(0, attrs[2]);
     }
 
     public void testOneItalicAtEnd()
     {
         String input = "one <i>test</i>";
         HParser parser = new HParser();
         parser.parse(input);
         String expectedResult = "one test";
         assertEquals(expectedResult, parser.getResultString());
         int[] offsets = parser.getOffsets();
         assertEquals(3, offsets.length);
         assertEquals(0, offsets[0]);
         assertEquals(4, offsets[1]);
         assertEquals(expectedResult.length(), offsets[offsets.length - 1]);
         byte[] attrs = parser.getAttributes();
         assertEquals(offsets.length - 1, attrs.length);
         assertEquals(0, attrs[0]);
         assertEquals(ITALIC, attrs[1]);
     }
 
     public void testOneLargeAtStart()
     {
         String input = "<h1>one</h1> test";
         HParser parser = new HParser();
         parser.parse(input);
         String expectedResult = "one test";
         assertEquals(expectedResult, parser.getResultString());
         int[] offsets = parser.getOffsets();
         assertEquals(3, offsets.length);
         assertEquals(0, offsets[0]);
         assertEquals(3, offsets[1]);
         assertEquals(expectedResult.length(), offsets[offsets.length - 1]);
         byte[] attrs = parser.getAttributes();
         assertEquals(offsets.length - 1, attrs.length);
         assertEquals(LARGE, attrs[0]);
         assertEquals(0, attrs[1]);
     }
 
     public void testBoldAndItalic()
     {
         String input = "this is <b>bold <i>and</i> italic</b>.";
         HParser parser = new HParser();
         parser.parse(input);
         String expectedResult = "this is bold and italic.";
         assertEquals(expectedResult, parser.getResultString());
         int[] offsets = parser.getOffsets();
         assertEquals(6, offsets.length);
         assertEquals(0, offsets[0]);
         assertEquals(8, offsets[1]);
         assertEquals(13, offsets[2]);
         assertEquals(16, offsets[3]);
         assertEquals(23, offsets[4]);
         assertEquals(expectedResult.length(), offsets[offsets.length - 1]);
         byte[] attrs = parser.getAttributes();
         assertEquals(offsets.length - 1, attrs.length);
         assertEquals(0, attrs[0]);
         assertEquals(BOLD, attrs[1]);
         assertEquals(BOLD + ITALIC, attrs[2]);
         assertEquals(BOLD, attrs[3]);
         assertEquals(0, attrs[4]);
     }
 
     public void testBadInput()
     {
         String input = "this is <bad";
         HParser parser = new HParser();
         parser.parse(input);
         assertEquals(input, parser.getResultString());
         int[] offsets = parser.getOffsets();
         assertEquals(2, offsets.length);
         assertEquals(0, offsets[0]);
         assertEquals(input.length(), offsets[offsets.length - 1]);
         byte[] attrs = parser.getAttributes();
         assertEquals(offsets.length - 1, attrs.length);
         assertEquals(0, attrs[0]);
     }
 
     public void testNothing()
     {
         HParser testParser = new HParser();
         try
         {
             testParser.parse("base case, no tags");
         }
         catch (Exception e)
         {
             assertNull(e);
         }
     }
 
     public void testItalic()
     {
         HParser testParser = new HParser();
         try
         {
             testParser.parse("plain <i> italic</i> plain");
             byte[] attributes = testParser.getAttributes();
             byte[] correctAttributes =
                 { (byte) 0, (byte) 1, (byte) 0 };
             for (int i = 0; i < attributes.length; i++)
             {
                 assertEquals(attributes[i], correctAttributes[i]);
             }
         }
         catch (Exception e)
         {
             assertNull(e);
         }
     }
 
     public void testBold()
     {
         HParser testParser = new HParser();
         try
         {
             testParser.parse("plain <b> bold</b> plain");
             byte[] attributes = testParser.getAttributes();
             byte[] correctAttributes =
                 { (byte) 0, (byte) 2, (byte) 0 };
             for (int i = 0; i < attributes.length; i++)
             {
                 assertEquals(attributes[i], correctAttributes[i]);
             }
         }
         catch (Exception e)
         {
             assertNull(e);
         }
     }
 
     public void testTagsOnOff()
     {
         HParser testParser = new HParser();
         try
         {
             testParser
                     .parse("plain <i> italic<b> bold/italic <h1> big/b/i </i> big/b </b> big </h1> plain");
             byte[] attributes = testParser.getAttributes();
             byte[] correctAttributes =
                 { (byte) 0, (byte) 1, (byte) 3, (byte) 7, (byte) 6, (byte) 4,
                         (byte) 0 };
             for (int i = 0; i < attributes.length; i++)
             {
                 assertEquals(attributes[i], correctAttributes[i]);
             }
         }
         catch (Exception e)
         {
             assertNull(e);
         }
     }
 
     public void testOffsets()
     {
         HParser testParser = new HParser();
         try
         {
             testParser.parse("0<>34<>7");
             int[] attributes = testParser.getOffsets();
             int[] correctOffsets =
                 { 0, 4 };
             for (int i = 0; i < attributes.length; i++)
             {
                 assertEquals(attributes[i], correctOffsets[i]);
             }
         }
         catch (Exception e)
         {
             assertNull(e);
         }
     }
 
     public void testEmphasis() {
         HParser testParser = new HParser();
         try
         {
         	testParser.parse("plain <em> bold</em> plain");
         	byte[] attributes = testParser.getAttributes();
         	byte[] correctAttributes = {
         			(byte)0,
         			(byte)HParser.ITALIC,
         			(byte)0
         	};
         	for(int i = 0; i < attributes.length; i++) {
         		assertEquals(attributes[i], correctAttributes[i]);
         	}
         }
         catch (Exception e)
         {
         	assertNull(e);
         }
     }
     
     public void testLarge() {
         HParser testParser = new HParser();
         try
         {
         	testParser.parse("plain <h1> large</h1> plain");
         	byte[] attributes = testParser.getAttributes();
         	byte[] correctAttributes = {
         			(byte)0,
         			(byte)4,
         			(byte)0
         };
         for(int i = 0; i < attributes.length; i++) {
         	assertEquals(attributes[i], correctAttributes[i]);
         }
     
         testParser.parse("plain <h2> large</h2> plain");
         attributes = testParser.getAttributes();
         for(int i = 0; i < attributes.length; i++) {
         	assertEquals(attributes[i], correctAttributes[i]);
         }
     
         testParser.parse("plain <h3> large</h3> plain");
         attributes = testParser.getAttributes();
         for(int i = 0; i < attributes.length; i++) {
         	assertEquals(attributes[i], correctAttributes[i]);
         }
     
         testParser.parse("plain <h4> large</h4> plain");
         attributes = testParser.getAttributes();
         for(int i = 0; i < attributes.length; i++) {
         	assertEquals(attributes[i], correctAttributes[i]);
         }
         }
         catch (Exception e)
         {
         	assertNull(e);
         }
     }
 
     public void testTagStripping()
     {
         HParser testParser = new HParser();
         try
         {
             testParser.parse("plain <h1> large</h1> plain");
             byte[] attributes = testParser.getAttributes();
             byte[] correctAttributes =
                 { (byte) 0, (byte) 4, (byte) 0 };
             for (int i = 0; i < attributes.length; i++)
             {
                 assertEquals(attributes[i], correctAttributes[i]);
             }
         }
         catch (Exception e)
         {
             assertNull(e);
         }
     }
 
     public void testScanImages()
     {
         HParser testParser = new HParser();
         testParser.parse("<img src=\"imgs/logos/Castellano2008.gif\" alt=\"Castellano\"> <img src=\"smiley.gif\"/>");
         Vector images = testParser.getImageURLs();
         assertEquals(images.elementAt(0), "imgs/logos/Castellano2008.gif");
         assertEquals(images.elementAt(1), "smiley.gif");
     }
 
     public void testH2()
     {
         String input = "<h2>title</h2> test";
         HParser parser = new HParser();
         parser.parse(input);
         String expectedResult = "title test";
         assertEquals(expectedResult, parser.getResultString());
         int[] offsets = parser.getOffsets();
         assertEquals(3, offsets.length);
         assertEquals(expectedResult.length(), offsets[offsets.length - 1]);
         byte[] attrs = parser.getAttributes();
         assertEquals(offsets.length - 1, attrs.length);
         assertEquals(LARGE, attrs[0]);
     }
 
     public void testEM()
     {
         String input = "<em>bold</em> test";
         HParser parser = new HParser();
         parser.parse(input);
         String expectedResult = "bold test";
         assertEquals(expectedResult, parser.getResultString());
         int[] offsets = parser.getOffsets();
         assertEquals(3, offsets.length);
         assertEquals(expectedResult.length(), offsets[offsets.length - 1]);
         byte[] attrs = parser.getAttributes();
         assertEquals(offsets.length - 1, attrs.length);
         assertEquals(ITALIC, attrs[0]);
     }
     
     public void testBR()
     {
         String input = "line1<br/>line2<br>line3";
         HParser parser = new HParser();
         parser.parse(input);
         String expectedResult = "line1\nline2\nline3";
         assertEquals(expectedResult, parser.getResultString());
         int[] offsets = parser.getOffsets();
         assertEquals(2, offsets.length);
         assertEquals(expectedResult.length(), offsets[offsets.length - 1]);
         byte[] attrs = parser.getAttributes();
         assertEquals(offsets.length - 1, attrs.length);
         assertEquals(0, attrs[0]);
     }
     
     public void testBRandTag()
     {
         String input = "line1<br/>line2 <b>and bold</b><br/>line3";
         HParser parser = new HParser();
         parser.parse(input);
         String expectedResult = "line1\nline2 and bold\nline3";
         assertEquals(expectedResult, parser.getResultString());
         int[] offsets = parser.getOffsets();
         assertEquals(4, offsets.length);
         assertEquals(0, offsets[0]);
         assertEquals(12, offsets[1]);
         assertEquals(20, offsets[2]);        
         assertEquals(expectedResult.length(), offsets[offsets.length - 1]);
         byte[] attrs = parser.getAttributes();
         assertEquals(offsets.length - 1, attrs.length);
         assertEquals(0, attrs[0]);
         assertEquals(HParser.BOLD, attrs[1]);
         assertEquals(0, attrs[2]);
     }
     
     public void testUnknownTag()
     {
         String input = "line1<foo>line2</foo>line3";
         HParser parser = new HParser();
         parser.parse(input);
         String expectedResult = "line1line2line3";
         assertEquals(expectedResult, parser.getResultString());
         int[] offsets = parser.getOffsets();
         assertEquals(2, offsets.length);
         assertEquals(expectedResult.length(), offsets[offsets.length - 1]);
         byte[] attrs = parser.getAttributes();
         assertEquals(offsets.length - 1, attrs.length);
         assertEquals(0, attrs[0]);
     }    
 
     
     public void testBeaufort()
     {
         String input = "<b>Nominated for the Academy Award for Best Foreign Language Feature</b><br>  In a very special collaboration with the Silicon Valley Jewish Film Festival and the Consulate General of Israel to the Pacific Northwest please join Cinequest as we celebrate Israels 60th Anniversary for a special evening with author and screenwriter Ron Leshem and a very special screening of <i>Beaufort</i>. Winner of the Silver Bear for best director at the 2007 Berlin Film Festival and Israels official nomination for the Foreign Language Film category at the Oscars director Joseph Cedar employs an effective minimalist style to his intense subtle study of war based on the celebrated novel by Leshem. Serving as an examination of the futility of war and with more recent Israeli Lebanese history clearly in mind Beaufort is a haunting and beautiful portrayal of not only tragedy but of the strength and fragility of the human condition. (Mr. Leshems visit is made possible through the Consulate General of Israel to the Pacific Northwest).";
         HParser parser = new HParser();
         parser.parse(input);
         int[] offsets = parser.getOffsets();
         for (int i = 0; i < offsets.length - 1; i++)
             assertTrue(offsets[i] < offsets[i + 1]);
         assertTrue(offsets[offsets.length - 1] == parser.getResultString().length());
     }
     
     public void testEndsInJunk()
     {
     	String input = "<p>Fred<b>Wilma</b>Barney</p>";
     	HParser parser = new HParser();
         parser.parse(input);
         int[] offsets = parser.getOffsets();
         assertTrue(offsets[offsets.length - 1] == parser.getResultString().length());
     }
     
     public void testShorts()
     {
     	String input = "<p>Not your average student films.<br><br>Students may be the most passionate of all filmmakers, filled with raw ideas that manifest themselves through intensely personal subjects and unique approaches to filmmaking. The Student Shorts program showcases this passion at its best, with inspired films in familiar genres - from a charming animation (Bloom) to an array of documentaries (Mexican Cuisine, The Other Side, Sick as a Dog); from a hilarious comedy (Elder Anderson) to unsettling dramas (Rest Stop, Say Goodnight, Teardrop); and three wildly different coming-of-age stories (First Match, The Homecoming Queen, Nani) giving us a glimpse at some of the many challenges that young people face today.<br><br>– Nathan Zanon</p><ul><li>Rest Stop - De Anza College - dir. Nader Carun</li>		<li>Nani - AFI - dir. Justin Tipping</li><li>The Other Side - Academy of Art University - dir. Sangmi Eom</li><li>Elder Anderson - San Jose State - dir. Daniel Maggio</li><li>Teardrop - University of Television and Film, Munich - dir. Damian John Harper</li> <li>Mexican Cuisine - Academy of Art University - dir. Francisco Guijarro</li><li>Say Goodnight - UCLA - dir. Brad LaBriola	</li><li>Bloom - San Jose State University - dir. Brian Kistler</li>	<li>Sick as a Dog - Stanford University - dir. Rebekah Meredith</li><li>First Match - Columbia University - dir. Olivia Newman</li><li>The Homecoming Queen - Columbia University - dir. Rammy Lee Park</li></ul>";
         HParser parser = new HParser();
         parser.parse(input);
         int[] offsets = parser.getOffsets();
         for (int i = 0; i < offsets.length - 1; i++)
             assertTrue(offsets[i] < offsets[i + 1]);
         assertTrue(offsets[offsets.length - 1] == parser.getResultString().length());
     }
    
     public void testWriter()
     {
         TestCallback callback = new TestCallback();
        mgr.getProgramItem(1482, callback);
         ProgramItem item = (ProgramItem) callback.getResult();
         String input = item.getDescription();
         HParser parser = new HParser();
         parser.parse(input);
         assertTrue(parser.getResultString() != null);
     }
    
     public void testFilms()
     {
         TestCallback callback = new TestCallback();
        mgr.getProgramItem(1482, callback);
         ProgramItem item = (ProgramItem) callback.getResult();
         String input = item.getDescription();
         HParser parser = new HParser();
         parser.parse(input);
         assertTrue(parser.getResultString() != null);
         Vector films = item.getFilms();
         for (int i = 0; i < films.size(); i++)
         {
             Film film = (Film) films.elementAt(i);
             input = film.getDescription();
             parser = new HParser();
             parser.parse(input);
             assertTrue(parser.getResultString() != null);            
         }
     }
 }
