 package de.zib.gndms.logic.action;
 
 import de.zib.gndms.kit.config.ConfigProvider;
 import de.zib.gndms.kit.config.MapConfig;
 import static de.zib.gndms.kit.config.ParameterTools.*;
 import de.zib.gndms.kit.config.MandatoryOptionMissingException;
 import org.testng.Assert;
 import static org.testng.Assert.assertEquals;
 import org.testng.annotations.Test;
 
 import java.text.ParseException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.List;
 
 
 /**
  * Tests for ConfigurationAction.ParameterTools
  *
  * @author Stefan Plantikow <plantikow@zib.de>
  * @version $Id$
  *
  *          User: stepn Date: 14.08.2008 Time: 12:50:50
  */
 @SuppressWarnings({
         "FeatureEnvy", "OverlyLongMethod", "InstanceMethodNamingConvention",
         "HardcodedFileSeparator" })
 public class ParameterToolsTest {
     Map<String, String> paraMap = new HashMap<String, String>(8);
 
     @Test(groups = {"factory"})
     public void toStrTest() {
         paraMap.clear();
         assertEquals(asString(paraMap, null), "");
 
         paraMap.put("test1", "val1");
         assertEquals(asString(paraMap, null), "test1: 'val1'");
 
         paraMap.put("test2", "val2");
         final String strVal = asString(paraMap, null);
         Assert.assertTrue(
                 "test1: 'val1'; test2: 'val2'".equals(strVal)
                           || "test2: 'val2'; test1: 'val1'".equals(strVal));
     }
 
 
 
     @Test(groups = {"factory"})
     public void positiveParseTest() throws ParameterParseException {
         paraMap.clear();
 
         parseParameters(paraMap, "", null);
 
         paraMap.clear();
         parseParameters(paraMap, "foo: bar", null);
         assertEquals(paraMap.get("foo"), "bar");
 
         paraMap.clear();
         parseParameters(paraMap, " foo:bar", null);
         assertEquals(paraMap.get("foo"), "bar");
 
         paraMap.clear();
         parseParameters(paraMap, "foo: bar;", null);
         assertEquals(paraMap.get("foo"), "bar");
 
         paraMap.clear();
         parseParameters(paraMap, "  foo: bar  ", null);
         assertEquals(paraMap.get("foo"), "bar");
 
         paraMap.clear();
         parseParameters(paraMap, "foo: bar ;", null);
         assertEquals(paraMap.get("foo"), "bar");
 
         paraMap.clear();
         parseParameters(paraMap, "  foo: bar ; ", null);
         assertEquals(paraMap.get("foo"), "bar");
 
         paraMap.clear();
         parseParameters(paraMap, "  foo: 'bar'", null);
         assertEquals(paraMap.get("foo"), "bar");
 
         paraMap.clear();
         parseParameters(paraMap, "blorp: blurp;  foo: bar ; ", null);
         assertEquals(paraMap.get("foo"), "bar");
         assertEquals(paraMap.get("blorp"), "blurp");
 
         paraMap.clear();
         parseParameters(paraMap, "blorp: blurp;  foo: bar", null);
         assertEquals(paraMap.get("foo"), "bar");
         assertEquals(paraMap.get("blorp"), "blurp");
 
         paraMap.clear();
         parseParameters(paraMap, "blorp:'blurp ' ;  foo: bar", null);
         assertEquals(paraMap.get("foo"), "bar");
         assertEquals(paraMap.get("blorp"), "blurp ");
 
         paraMap.clear();
         parseParameters(paraMap, "blorp:blurp;  foo:' bar';", null);
         assertEquals(paraMap.get("foo"), " bar");
         assertEquals(paraMap.get("blorp"), "blurp");
     }
 
 
     @SuppressWarnings({ "HardcodedFileSeparator" })
     @Test(groups = {"factory"})
     public void positiveParseEscapeTest() throws ParameterParseException {
         paraMap.clear();
         parseParameters(paraMap, "", null);
         assert paraMap.isEmpty();
 
         paraMap.clear();
         parseParameters(paraMap, "foo: ''", null);
         assertEquals(paraMap.get("foo"), "");
 
         paraMap.clear();
         parseParameters(paraMap, "foo= ':'", null);
         assertEquals(paraMap.get("foo"), ":");
 
         paraMap.clear();
         parseParameters(paraMap, "foo:", null);
         assertEquals(paraMap.get("foo"), "");
 
         paraMap.clear();
         parseParameters(paraMap, "foo: 'bar'", null);
         assertEquals(paraMap.get("foo"), "bar");
 
         paraMap.clear();
         parseParameters(paraMap, "foo: \"bar\"", null);
         assertEquals(paraMap.get("foo"), "bar");
 
         paraMap.clear();
         parseParameters(paraMap, "foo: b\\ar", null);
         assertEquals(paraMap.get("foo"), "bar");
 
         paraMap.clear();
         parseParameters(paraMap, "foo: b\\ar\\;;", null);
         assertEquals(paraMap.get("foo"), "bar;");
 
         paraMap.clear();
         parseParameters(paraMap, "foo:'b\\ar';", null);
         assertEquals(paraMap.get("foo"), "bar");
 
         paraMap.clear();
         parseParameters(paraMap, "foo:'b\\;r';", null);
         assertEquals(paraMap.get("foo"), "b;r");
 
         paraMap.clear();
         parseParameters(paraMap, "foo:'b\\;r\\'';", null);
         assertEquals(paraMap.get("foo"), "b;r'");
 
 
         paraMap.clear();
         parseParameters(paraMap, "foo:'bar\\\\';", null);
         assertEquals(paraMap.get("foo"), "bar\\");
 
         paraMap.clear();
         parseParameters(paraMap, "foo; bar: baz;", null);
         assertEquals(paraMap.get("foo"), "true");
 
         paraMap.clear();
         parseParameters(paraMap, "!foo; bar: baz;", null);
         assertEquals(paraMap.get("foo"), "false");
 
         paraMap.clear();
         parseParameters(paraMap, "foo", null);
         assertEquals(paraMap.get("foo"), "true");
 
         paraMap.clear();
         parseParameters(paraMap, "!foo", null);
         assertEquals(paraMap.get("foo"), "false", null);
 
         paraMap.clear();
         parseParameters(paraMap, "-foo", null);
         assertEquals(paraMap.get("foo"), "false", null);
 
         paraMap.clear();
         parseParameters(paraMap, "+foo", null);
         assertEquals(paraMap.get("foo"), "true", null);
     }
 
 	@SuppressWarnings({ "HardcodedFileSeparator" })
 	@Test(groups = {"factory"})
 	public void parseDynArrayTest()
 		  throws ParameterParseException, MandatoryOptionMissingException, ParseException {
 	   paraMap.clear();
 	   parseParameters(paraMap, "key: '[1, 2, 3]'", null);
 	   ConfigProvider config = new MapConfig(paraMap);
 	   config = config.getDynArrayOption("key");
 	   assertEquals(config.getIntOption("count"), 3);
 	   assertEquals(config.getIntOption("0"), 1);
 	   assertEquals(config.getIntOption("1"), 2);
 	   assertEquals(config.getIntOption("2"), 3);
 	}
 
 	@Test(groups = {"factory"})
 	public void parseStringArrayTest(){
	    paraMap.clear();
 	    List<String> config=parseStringArray("[1 ,2 ,3\\,4]");
         assertEquals(config.size(), 3);
        assertEquals(config.get(0),"1");
        assertEquals(config.get(1)," 2");
         assertEquals(config.get(2),"3,4");
        // assertEquals(config.get(3),"4");
     }
 
     @Test(groups = {"factory"},
           expectedExceptions = { ParameterParseException.class})
     public void negativeWsInKeyTest1() throws ParameterParseException {
         paraMap.clear();
         parseParameters(paraMap, "foo : ", null);
     }
 
     @Test(groups = {"factory"},
           expectedExceptions = { ParameterParseException.class})
     public void negativeWsInKeyTest2() throws ParameterParseException {
         paraMap.clear();
         parseParameters(paraMap, "fo o:", null);
     }
 
     @Test(groups = {"factory"},
           expectedExceptions = { ParameterParseException.class})
     public void negativeUncompleteTest1() throws ParameterParseException {
         paraMap.clear();
         parseParameters(paraMap, "foo: 'bar", null);
     }
 
     @Test(groups = {"factory"},
           expectedExceptions = { ParameterParseException.class})
     public void negativeEmptyKeyTest1() throws ParameterParseException {
         paraMap.clear();
         parseParameters(paraMap, ":", null);
     }
 
     @Test(groups = {"factory"},
           expectedExceptions = { ParameterParseException.class})
     public void negativeEmptyKeyTest2() throws ParameterParseException {
         paraMap.clear();
         parseParameters(paraMap, ":''", null);
     }
 
     @Test(groups = {"factory"},
           expectedExceptions = { ParameterParseException.class})
     public void negativeUnfinishedEscapeTest1() throws ParameterParseException {
         paraMap.clear();
         parseParameters(paraMap, ":'\\", null);
     }
 
     @Test(groups = {"factory"},
           expectedExceptions = { ParameterParseException.class})
     public void negativeUnfinishedEscapeTest2() throws ParameterParseException {
         paraMap.clear();
         parseParameters(paraMap, ": \\", null);
     }
 
 
     @Test(groups = {"factory"},
           expectedExceptions = { ParameterParseException.class})
     public void negativeDuplicateKey() throws ParameterParseException {
         paraMap.clear();
         parseParameters(paraMap, "foo:bar; foo:", null);
     }
 }
