 package uk.co.sromo.blister;
 
 import org.apache.log4j.lf5.util.StreamUtils;
 import org.junit.Assert;
 import org.junit.Test;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 /**
  * Created by IntelliJ IDEA.
  * User: daniel
  * Date: 14-Aug-2010
  * Time: 19:48:40
  * To change this template use File | Settings | File Templates.
  */
 public class TestBPItems {
 
     public final static String ASCII_STRING_1 = "Quite Interesting";
     public final static String ASCII_STRING_2 = "RatherDull";
 
     public final static String UNICODE_STRING_1 = "Ỡuitὲ InƬerestіng";
     public final static String UNICODE_STRING_2 = "ṜaŦherḒull";
 
     @Test
     public void TestBPStringTypeIdentification() {
         BPString s1 = BPString.get(ASCII_STRING_1);
         Assert.assertEquals("String wasn't ASCII", BPString.EncodingType.ASCII, s1.getEncodingType());
         BPString s2 = BPString.get(UNICODE_STRING_1);
         Assert.assertEquals("String wasn't Unicode", BPString.EncodingType.UTF16, s2.getEncodingType());
     }
 
     @Test
     public void TestReadingBinaryUnicode() throws IOException, BinaryPlistException {
         InputStream stream = TestBPItems.class.getResourceAsStream("/BinaryUnicode.plist");
         byte[] bytes = StreamUtils.getBytes(stream);
         BPItem root = BinaryPlist.decode(bytes);
         Assert.assertEquals("Not a dictioary", BPItem.Type.Dict, root.getType());
 
         BPDict newDict = (BPDict) root;
 
         Assert.assertEquals("New ascii lookup failed", ASCII_STRING_1, newDict.get(ASCII_STRING_2, "FAIL"));
         Assert.assertEquals("New unicode lookup failed", UNICODE_STRING_1, newDict.get(UNICODE_STRING_2, "FAIL"));
     }
 
     @Test
     public void TestPrimitiveTypesInDictionary() throws BinaryPlistException {
         BPDict dict = new BPDict()
                 .with(ASCII_STRING_2, ASCII_STRING_1)
                 .with(UNICODE_STRING_2, UNICODE_STRING_1);
         Assert.assertEquals("Ascii lookup failed", ASCII_STRING_1, dict.get(ASCII_STRING_2, "FAIL"));
         Assert.assertEquals("Unicode lookup failed", UNICODE_STRING_1, dict.get(UNICODE_STRING_2, "FAIL"));
 
         byte[] bytes = BinaryPlist.encode(dict);
 
         BPItem root = BinaryPlist.decode(bytes);
         Assert.assertEquals("Not a dictionary", BPItem.Type.Dict, root.getType());
 
         BPDict newDict = (BPDict) root;
 
         Assert.assertEquals("New ascii lookup failed", ASCII_STRING_1, newDict.get(ASCII_STRING_2, "FAIL"));
         Assert.assertEquals("New unicode lookup failed", UNICODE_STRING_1, newDict.get(UNICODE_STRING_2, "FAIL"));
     }
 
     @Test
     public void TestRoundTripping() throws BinaryPlistException {
         BPDict dict = new BPDict()
                 .with("key1", "value1")
                 .with("key2", 14)
                 .with("key3", true)
                 .with("key4", new BPArray()
                     .with("another value")
                     .with(56)
                     .with(false))
                 .with("key5", "finished");
         byte[] bytes = BinaryPlist.encode(dict);
 
         BPItem root = BinaryPlist.decode(bytes);
         Assert.assertEquals("Not a dictionary", BPItem.Type.Dict, root.getType());
         BPDict newDict = (BPDict) root;
 
         Assert.assertEquals(dict.get("key1", ""), newDict.get("key1", "FAIL"));
         Assert.assertEquals(dict.get("key2", 0), newDict.get("key2", -1));
         Assert.assertEquals(dict.get("key3", true), newDict.get("key3", false));
         Assert.assertEquals(dict.get("key5", ""), newDict.get("key5", "FAIL"));
         BPItem obj1 = dict.get("key4");
         BPItem obj2 = newDict.get("key4");
         Assert.assertEquals(BPItem.Type.Array, obj1.getType());
         Assert.assertEquals(BPItem.Type.Array, obj2.getType());
         BPArray array1 = (BPArray) obj1;
         BPArray array2 = (BPArray) obj2;
 
         Assert.assertEquals(array1.get(0), array2.get(0));
         Assert.assertEquals(array1.get(1), array2.get(1));
         Assert.assertEquals(array1.get(2), array2.get(2));
     }
 
     @Test
     public void TestEnumeratedConstants() throws BinaryPlistException {
         BPDict dict = new BPDict()
                 .with("key1", Numerals.FIRST)
                 .with("key2", Numerals.SECOND)
                 .with("key3", Numerals.THIRD);
         byte[] bytes = BinaryPlist.encode(dict);
 
         BPItem root = BinaryPlist.decode(bytes);
         Assert.assertEquals("Not a dictionary", BPItem.Type.Dict, root.getType());
         BPDict newDict = (BPDict) root;
 
         Assert.assertEquals(dict.get("key1", Numerals.DIFFERENT_FAIL), newDict.get("key1", Numerals.FAIL));
         Assert.assertEquals(dict.get("key2", Numerals.DIFFERENT_FAIL), newDict.get("key2", Numerals.FAIL));
         Assert.assertEquals(dict.get("key3", Numerals.DIFFERENT_FAIL), newDict.get("key3", Numerals.FAIL));
         Assert.assertEquals(Numerals.FAIL, newDict.get("key4", Numerals.FAIL));
 
     }
 
     @Test
     public void TestEnumeratedKeys() throws BinaryPlistException {
         BPDict dict = new BPDict()
                 .with(Keys.ALPHA, "value1")
                 .with(Keys.BETA, 14)
                 .with(Keys.GAMMA, true)
                 .with(Keys.DELTA, Numerals.FIRST)
                 .with("key5", "finished");
         byte[] bytes = BinaryPlist.encode(dict);
 
         BPItem root = BinaryPlist.decode(bytes);
         Assert.assertEquals("Not a dictionary", BPItem.Type.Dict, root.getType());
         BPDict newDict = (BPDict) root;
 
         Assert.assertEquals(dict.get(Keys.ALPHA, ""), newDict.get(Keys.ALPHA, "FAIL"));
         Assert.assertEquals(dict.get(Keys.BETA, 0), newDict.get(Keys.BETA, -1));
         Assert.assertEquals(dict.get(Keys.GAMMA, true), newDict.get(Keys.GAMMA, false));
         Assert.assertEquals(dict.get(Keys.DELTA, Numerals.DIFFERENT_FAIL), newDict.get(Keys.DELTA, Numerals.FAIL));
         Assert.assertEquals(dict.get("key5", ""), newDict.get("key5", "FAIL"));
     }
 
     @Test
     public void TestStringEquivalentKeys() throws BinaryPlistException {
         BPDict dict = new BPDict()
                 .with(Keys.ALPHA, "value1")
                 .with("BETA", 14)
                 .with(Keys.GAMMA, true)
                 .with("DELTA", Numerals.FIRST)
                 .with("key5", "finished");
         byte[] bytes = BinaryPlist.encode(dict);
 
         BPItem root = BinaryPlist.decode(bytes);
         Assert.assertEquals("Not a dictionary", BPItem.Type.Dict, root.getType());
         BPDict newDict = (BPDict) root;
 
         Assert.assertEquals(dict.get(Keys.ALPHA, ""), newDict.get("ALPHA", "FAIL"));
         Assert.assertEquals(dict.get(Keys.BETA, 0), newDict.get(Keys.BETA, -1));
         Assert.assertEquals(dict.get(Keys.GAMMA, true), newDict.get(BPString.get("GAMMA"), false));
         Assert.assertEquals(dict.get(Keys.DELTA, Numerals.DIFFERENT_FAIL), newDict.get(Keys.DELTA, Numerals.FAIL));
         Assert.assertEquals(dict.get("key5", ""), newDict.get("key5", "FAIL"));
     }
 
 }
