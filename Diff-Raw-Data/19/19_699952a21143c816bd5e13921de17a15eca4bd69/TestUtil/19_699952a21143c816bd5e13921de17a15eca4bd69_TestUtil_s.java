 package org.rulez.magwas.test;
 
import org.rulez.magwas.worldmodel.Util;
 
 public class TestUtil {
 
 	public static String NormalizeXmlString(String in) {//FIXME testcases for stylesheet and xml header
 		return in
 				.replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>", "")
 				.replaceAll("\n", "")
 				.replace("<?xml-stylesheet type=\"text/xsl\" href=\"stylesheet.xsl\"?>", "");
 	}
 	
 	
 }
