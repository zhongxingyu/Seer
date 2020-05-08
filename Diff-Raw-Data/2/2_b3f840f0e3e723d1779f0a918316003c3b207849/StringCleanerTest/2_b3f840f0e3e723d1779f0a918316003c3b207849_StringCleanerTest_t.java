 package com.appspot.anaki808built.utils.helpers;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Test;
 
 /**
  * @author andi Jul 22, 2013
  */
 public final class StringCleanerTest {
 
 	/**
 	 * 
 	 */
 	@Test
 	public void makePrettyURL() {
 		final CharSequence input = "The swift fox jumped over the lazy Brown dog";
 		final String expected = "the-swift-fox-jumped-over-the-lazy-brown-dog";
 		final String result = StringCleaner.makePrettyURL(input);
 		assertEquals(expected, result);
 		assertEquals("el-qucik-zorro-salto-sobre-el-perro-perezoso-marron", StringCleaner.makePrettyURL("El qucik zorro saltó sobre el perro perezoso marrón"));
 		assertEquals("the-qucik-refur-stokk-yfir-latur-brunn-hundur", StringCleaner.makePrettyURL("The qucik refur stökk yfir latur brúnn hundur"));
 		assertEquals("qucik-lapsa-uzleca-par-slinks-bruns-suns", StringCleaner.makePrettyURL("Qucik lapsa uzlēca pār slinks brūns suns"));
 		assertEquals("qucik-lape-soktelejo-per-tingus-ruda-suni", StringCleaner.makePrettyURL("Qucik lapė šoktelėjo per tingus rudą šunį"));
 		assertEquals("o-qucik-raposa-saltou-sobre-o-cao-preguicoso-marrom", StringCleaner.makePrettyURL("O qucik raposa saltou sobre o cão preguiçoso marrom"));
 		assertEquals("", StringCleaner.makePrettyURL(null));
 		assertEquals("", StringCleaner.makePrettyURL("    "));
 		assertEquals("-something-to-test-", StringCleaner.makePrettyURL("\\\"&gt;Something to &#64;&#165;&yen; test/<>"));
 	}
 
 	/**
 	 * 
 	 */
	@Test(timeout = 220L)
 	public void unescapeHTMLEncoding() {
 		final CharSequence toTest = "&amp;&&lt;&gt;&#64;&#165;&yen;";
 		final CharSequence result = StringCleaner.unescapeHTMLEncoding(toTest);
 		assertEquals("&&<>@¥¥", result);
 		assertEquals("", StringCleaner.unescapeHTMLEncoding(null));
 		assertEquals("", StringCleaner.unescapeHTMLEncoding("   "));
 
 	}
 }
