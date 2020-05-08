 package com.joelj.jcss.parse;
 
 import com.joelj.DelayedAssert;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 import java.util.Arrays;
 
 /**
  * User: joeljohnson
  * Date: 5/15/12
  * Time: 8:52 PM
  */
 public class CssUtilsTest {
 	@Test
 	public void testSimpleSelectors() {
 		DelayedAssert.assertEquals(CssUtils.convertCssToXpath("long word"), "//long//word");
 		DelayedAssert.assertEquals(CssUtils.convertCssToXpath("E F"), "//E//F");
 		DelayedAssert.assertEquals(CssUtils.convertCssToXpath("E  F"), "//E//F");
 		DelayedAssert.assertEquals(CssUtils.convertCssToXpath("E\tF"), "//E//F");
 
 		DelayedAssert.assertEquals(CssUtils.convertCssToXpath("E > F"), "//E/F");
 		DelayedAssert.assertEquals(CssUtils.convertCssToXpath("E  >  F"), "//E/F");
 		DelayedAssert.assertEquals(CssUtils.convertCssToXpath("E\t>\tF"), "//E/F");
 
 		DelayedAssert.assertEquals(CssUtils.convertCssToXpath("E + F"), "//E/following-sibling::*[1]/self::F");
 		DelayedAssert.assertEquals(CssUtils.convertCssToXpath("E  +  F"), "//E/following-sibling::*[1]/self::F");
 		DelayedAssert.assertEquals(CssUtils.convertCssToXpath("E\t+\tF"), "//E/following-sibling::*[1]/self::F");
 
 		DelayedAssert.assertDelayed();
 	}
 
 	@Test
 	public void testComplexSelectors() {
 		DelayedAssert.assertEquals(CssUtils.convertCssToXpath("E F G"), "//E//F//G");
 		DelayedAssert.assertEquals(CssUtils.convertCssToXpath("E>F>G"), "//E/F/G");
 		DelayedAssert.assertEquals(CssUtils.convertCssToXpath("E+F+G"), "//E/following-sibling::*[1]/self::F/following-sibling::*[1]/self::G");
 
 		DelayedAssert.assertEquals(CssUtils.convertCssToXpath("E F>G+H"), "//E//F/G/following-sibling::*[1]/self::H");
 
 		DelayedAssert.assertDelayed();
 	}
 
 	@Test
 	public void testSelectorsWithSpecialAttributes() {
 		DelayedAssert.assertEquals(CssUtils.convertCssToXpath("E#myid F#myid2"), "//E[@id=\"myid\"]//F[@id=\"myid2\"]");
 		DelayedAssert.assertEquals(CssUtils.convertCssToXpath("E#myid>F#myid2"), "//E[@id=\"myid\"]/F[@id=\"myid2\"]");
 		DelayedAssert.assertEquals(CssUtils.convertCssToXpath("E#myid+F#myid2"), "//E[@id=\"myid\"]/following-sibling::*[1]/self::F[@id=\"myid2\"]");
 
 		DelayedAssert.assertEquals(CssUtils.convertCssToXpath("E.myclass F#myid2"), "//E[contains(concat(\" \",@class,\" \"),concat(\" \",\"myclass\",\" \"))]//F[@id=\"myid2\"]");
 		DelayedAssert.assertEquals(CssUtils.convertCssToXpath("E.myclass>F#myid2"), "//E[contains(concat(\" \",@class,\" \"),concat(\" \",\"myclass\",\" \"))]/F[@id=\"myid2\"]");
 		DelayedAssert.assertEquals(CssUtils.convertCssToXpath("E.myclass+F#myid2"), "//E[contains(concat(\" \",@class,\" \"),concat(\" \",\"myclass\",\" \"))]/following-sibling::*[1]/self::F[@id=\"myid2\"]");
 
 		DelayedAssert.assertDelayed();
 	}
 
 	@Test
 	public void testSelectorsWithAttributes() {
 		DelayedAssert.assertEquals(CssUtils.convertCssToXpath("E[attr=\"value\"] F[attr]"), "//E[@attr=\"value\"]//F[@attr]");
 
 		DelayedAssert.assertDelayed();
 	}
 
 	@Test
 	public void testSection() {
 		DelayedAssert.assertEquals(CssUtils.convertCssSectionToXpath("*"), "*");
 		DelayedAssert.assertEquals(CssUtils.convertCssSectionToXpath("E"), "E");
		DelayedAssert.assertEquals(CssUtils.convertCssSectionToXpath(""), "*");
		DelayedAssert.assertEquals(CssUtils.convertCssSectionToXpath("[atr]"), "*[@atr]");
 		DelayedAssert.assertEquals(CssUtils.convertCssSectionToXpath("E[atr]"), "E[@atr]");
 		DelayedAssert.assertEquals(CssUtils.convertCssSectionToXpath("E[atr=\"value\"]"), "E[@atr=\"value\"]");
 		DelayedAssert.assertEquals(CssUtils.convertCssSectionToXpath("E[atr~=\"value\"]"), "E[contains(concat(\" \",@atr,\" \"),concat(\" \",\"value\",\" \"))]");
 		DelayedAssert.assertEquals(CssUtils.convertCssSectionToXpath("E.value"), "E[contains(concat(\" \",@class,\" \"),concat(\" \",\"value\",\" \"))]");
 		DelayedAssert.assertEquals(CssUtils.convertCssSectionToXpath("E#value"), "E[@id=\"value\"]");
 		DelayedAssert.assertEquals(CssUtils.convertCssSectionToXpath("E:first-child"), "*[1]/self::E");
 
 		DelayedAssert.assertEquals(CssUtils.convertCssSectionToXpath("E[atr=\"value\"][atr2=\"value2\"]"), "E[@atr=\"value\" and @atr2=\"value2\"]");
 		DelayedAssert.assertEquals(CssUtils.convertCssSectionToXpath("E.aclass#anID"), "E[contains(concat(\" \",@class,\" \"),concat(\" \",\"aclass\",\" \")) and @id=\"anID\"]");
 
 //		Assert.assertEqualsDelayed(CssUtils.convertCssSectionToXpath("E[atr|=\"value\"]"), "E[@lang=\"en\" or starts-with(@lang,concat(\"en\",\"-\"))]");
 //		Assert.assertEqualsDelayed(CssUtils.convertCssSectionToXpath("E:lang(c)"),         "E[ @xml:lang = \"c\" or starts-with( @xml:lang, concat( \"c\", \"-\" ) ) ]");
 
 		DelayedAssert.assertDelayed();
 	}
 
 	@Test
 	public void testSectionErrors() {
 		try {
 			DelayedAssert.assertEquals(CssUtils.convertCssSectionToXpath("E:fail"), "*[1]/self::E");
 			Assert.fail("Shouldn't allow unsupported selector :fail");
 		} catch(CssParseException ignore) {
 		}
 
 		try {
 			CssUtils.convertCssSectionToXpath("E[attr=\"value\"");
 			Assert.fail("Should be throwing an exception for the missing ']'");
 		} catch (CssParseException ignore) {
 		}
 	}
 
 	@Test
 	public void testAttribute() {
 		DelayedAssert.assertEquals(CssUtils.convertCssAttributeSectionToXpath("[atr]"), "[@atr]");
 		DelayedAssert.assertEquals(CssUtils.convertCssAttributeSectionToXpath("[atr=\"value\"]"), "[@atr=\"value\"]");
 		DelayedAssert.assertEquals(CssUtils.convertCssAttributeSectionToXpath("[atr=\"val'ue\"]"), "[@atr=\"val'ue\"]");
 		DelayedAssert.assertEquals(CssUtils.convertCssAttributeSectionToXpath("[atr=\"val]ue\"]"), "[@atr=\"val]ue\"]");
 		DelayedAssert.assertEquals(CssUtils.convertCssAttributeSectionToXpath("[atr=\"val[]ue\"]"), "[@atr=\"val[]ue\"]");
 		DelayedAssert.assertEquals(CssUtils.convertCssAttributeSectionToXpath("[atr~=\"value\"]"), "[contains(concat(\" \",@atr,\" \"),concat(\" \",\"value\",\" \"))]");
 
 		DelayedAssert.assertDelayed();
 	}
 
 	@Test
 	public void testIndexOfNotQuoted() {
 		DelayedAssert.assertEquals(CssUtils.indexOfNotQuoted("~", '~'), 0);
 		DelayedAssert.assertEquals(CssUtils.indexOfNotQuoted("~", '~', 1), -1);
 		DelayedAssert.assertEquals(CssUtils.indexOfNotQuoted("1234 ~ 890", '~'), 5);
 		DelayedAssert.assertEquals(CssUtils.indexOfNotQuoted("1234 ~ 890", '~', 6), -1);
 		DelayedAssert.assertEquals(CssUtils.indexOfNotQuoted("1234 ~ 890", '~', 5), 5);
 
 		DelayedAssert.assertEquals(CssUtils.indexOfNotQuoted("", '~'), -1);
 		DelayedAssert.assertEquals(CssUtils.indexOfNotQuoted("asdf", '~'), -1);
 		DelayedAssert.assertEquals(CssUtils.indexOfNotQuoted("as'~'df", '~'), -1);
 		DelayedAssert.assertEquals(CssUtils.indexOfNotQuoted("as\"~\"df", '~'), -1);
 		DelayedAssert.assertEquals(CssUtils.indexOfNotQuoted("\"~\"", '~'), -1);
 		DelayedAssert.assertEquals(CssUtils.indexOfNotQuoted("'~'", '~'), -1);
 
 		DelayedAssert.assertDelayed();
 	}
 
 	@Test
 	public void testGetAttributeName() {
 		DelayedAssert.assertEquals(CssUtils.getAttributeName("[name]"), "name");
 		DelayedAssert.assertEquals(CssUtils.getAttributeName("[name=\"value\"]"), "name");
 		DelayedAssert.assertEquals(CssUtils.getAttributeName("[name=\"va[]lue\"]"), "name");
 		DelayedAssert.assertEquals(CssUtils.getAttributeName("[name~=\"value\"]"), "name");
 
 		DelayedAssert.assertDelayed();
 	}
 
 	@Test
 	public void testGetAttributeValue() {
 		DelayedAssert.assertEquals(CssUtils.getAttributeValue("[name]"), null);
 		DelayedAssert.assertEquals(CssUtils.getAttributeValue("[name=\"value\"]"), "value");
 		DelayedAssert.assertEquals(CssUtils.getAttributeValue("[name=\"va[]lue\"]"), "va[]lue");
 
 		try {
 			CssUtils.getAttributeValue("[name=va[]lue]");
 			Assert.fail("Expecting CssParseException for not having any quotes");
 		} catch(CssParseException ignore) {
 		}
 
 		try {
 			CssUtils.getAttributeValue("[name=\"value]");
 			Assert.fail("Expecting CssParseException for not having close quote");
 		} catch(CssParseException ignore) {
 		}
 
 		DelayedAssert.assertDelayed();
 	}
 
 	@Test
 	public void testReplaceClassAndIDSelectors() {
 		DelayedAssert.assertEquals(CssUtils.replaceClassAndIDSelectors(".someClass"), "[class~=\"someClass\"]");
 		DelayedAssert.assertEquals(CssUtils.replaceClassAndIDSelectors(".someClass.anotherClass"), "[class~=\"someClass\"][class~=\"anotherClass\"]");
 		DelayedAssert.assertEquals(CssUtils.replaceClassAndIDSelectors(".some1"), "[class~=\"some1\"]");
 
 
 		DelayedAssert.assertEquals(CssUtils.replaceClassAndIDSelectors("#someID"), "[id=\"someID\"]");
 
 		DelayedAssert.assertDelayed();
 	}
 
 	@Test
 	public void testSplit() {
 		DelayedAssert.assertEquals(CssUtils.split("F>E"), Arrays.asList("F", ">", "E"));
 		DelayedAssert.assertEquals(CssUtils.split("long>word"), Arrays.asList("long", ">", "word"));
 		DelayedAssert.assertEquals(CssUtils.split("E>F>G"), Arrays.asList("E", ">", "F", ">", "G"));
 
 		DelayedAssert.assertEquals(CssUtils.split("F+E"), Arrays.asList("F", "+", "E"));
 		DelayedAssert.assertEquals(CssUtils.split("long+word"), Arrays.asList("long", "+", "word"));
 		DelayedAssert.assertEquals(CssUtils.split("E+F+G"), Arrays.asList("E", "+", "F", "+", "G"));
 
 		DelayedAssert.assertEquals(CssUtils.split("F E"), Arrays.asList("F", " ", "E"));
 		DelayedAssert.assertEquals(CssUtils.split("long word"), Arrays.asList("long", " ", "word"));
 		DelayedAssert.assertEquals(CssUtils.split("E F G"), Arrays.asList("E", " ", "F", " ", "G"));
 
 		DelayedAssert.assertEquals(CssUtils.split("E>F+G H"), Arrays.asList("E", ">", "F", "+", "G", " ", "H"));
 		DelayedAssert.assertEquals(CssUtils.split("E>F G+H"), Arrays.asList("E", ">", "F", " ", "G", "+", "H"));
 
 		DelayedAssert.assertEquals(CssUtils.split("E+F G>H"), Arrays.asList("E", "+", "F", " ", "G", ">", "H"));
 		DelayedAssert.assertEquals(CssUtils.split("E+F>G H"), Arrays.asList("E", "+", "F", ">", "G", " ", "H"));
 
 		DelayedAssert.assertEquals(CssUtils.split("E F+G>H"), Arrays.asList("E", " ", "F", "+", "G", ">", "H"));
 		DelayedAssert.assertEquals(CssUtils.split("E F>G+H"), Arrays.asList("E", " ", "F", ">", "G", "+", "H"));
 
 		DelayedAssert.assertDelayed();
 	}
 }
