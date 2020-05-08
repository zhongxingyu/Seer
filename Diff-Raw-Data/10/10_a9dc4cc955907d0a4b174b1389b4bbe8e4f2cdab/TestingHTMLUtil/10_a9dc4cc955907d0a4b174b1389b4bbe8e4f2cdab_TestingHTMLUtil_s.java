 package br.org.mosaic.tests;
 
 import junit.framework.Assert;
 import junit.framework.TestCase;
 import br.org.mosaic.HTMLUtil;
 import br.org.mosaic.properties.StyleName;
 import br.org.mosaic.properties.StyleProperty;
 
 public class TestingHTMLUtil extends TestCase {
 	public void testCreateCoord() {
 		Assert.assertEquals("1,2,3,4,5,6,0", HTMLUtil.createCoords(1, 2, 3, 4, 5, 6, 0));
 	}
 
 	public void testCreateLevel_0() {
 		Assert.assertEquals("", HTMLUtil.createLevel(0));
 	}
 
 	public void testCreateLevel_1() {
 		Assert.assertEquals("\n\t", HTMLUtil.createLevel(1));
 	}
 
 	public void testCreateLevel_10() {
 		Assert.assertEquals("\n\t\t\t\t\t\t\t\t\t\t", HTMLUtil.createLevel(10));
 	}
 
 	public void testLTrim_0() {
 		Assert.assertEquals("A", HTMLUtil.ltrim("A"));
 	}
 
 	public void testLTrim_1() {
 		Assert.assertEquals("A", HTMLUtil.ltrim(" A"));
 	}
 
 	public void testLTrim_1_without_r_trim() {
 		Assert.assertEquals("A ", HTMLUtil.ltrim(" A "));
 	}
 
 	public void testCreateLevel_minus_1() {
 		Assert.assertEquals("", HTMLUtil.createLevel(-1));
 	}
 
 	public void testCreatingSeveralStyleProperty() {
 		final StyleProperty p = new StyleProperty(StyleProperty.style(StyleName.WIDTH, "100%"), StyleProperty.style(StyleName.HEIGHT, "75%"));
 		Assert.assertEquals("style='width:100%;height:75%;'", p.toString());
 	}
 
 	public void testTrimmingInStyleFormat() {
 		final StyleProperty p = new StyleProperty(StyleProperty.style(StyleName.WIDTH, "                       100%       "), StyleProperty.style(StyleName.HEIGHT, "75%          "), StyleProperty
 				.style(StyleName.BORDER, "             2px solid #000"));
 		Assert.assertEquals("style='width:100%;height:75%;border:2px solid #000;'", p.toString());
 	}
 }
