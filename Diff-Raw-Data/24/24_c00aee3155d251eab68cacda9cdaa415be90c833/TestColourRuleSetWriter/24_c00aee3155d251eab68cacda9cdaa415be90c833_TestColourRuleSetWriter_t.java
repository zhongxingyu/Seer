 package com.hexcore.cas.model.test;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Test;
 
 import com.hexcore.cas.model.ColourRule;
 import com.hexcore.cas.model.ColourRuleSet;
 import com.hexcore.cas.model.ColourRuleSetWriter;
 import com.hexcore.cas.ui.toolkit.Colour;
 
 public class TestColourRuleSetWriter
 {
 	@Test
 	public void test()
 	{
         ColourRuleSet colourRules = new ColourRuleSet(4);
         ColourRule    colourRule;
 
         colourRule = new ColourRule();
         colourRule.addRange(new ColourRule.Range(0.0, 1.0, new Colour(1.0f, 0.0f, 0.0f)));
         colourRules.setColourRule(0, colourRule);
         
         colourRule = new ColourRule();
         colourRule.useClosestRange = true;
         colourRule.addRange(new ColourRule.Range(0.0, 10.0, new Colour(0.0f, 0.25f, 0.5f, 0.5f)));
         colourRule.addRange(new ColourRule.Range(10.0, 20.0, new Colour(0.0f, 0.8f, 0.5f)));
         colourRules.setColourRule(1, colourRule);
         
         colourRule = new ColourRule();
         colourRule.addRange(new ColourRule.Range(0.0, 15.1, new Colour(0.0f, 0.5f, 0.8f), new Colour(0.0f, 0.25f, 0.5f)));
         colourRules.setColourRule(2, colourRule);    
         
         colourRule = new ColourRule();
         colourRule.addRange(new ColourRule.Range(0.0, 8.0, new Colour(0.5f, 0.25f, 0.0f), new Colour(0.0f, 0.8f, 0.5f)));
         colourRule.addRange(new ColourRule.Range(8.0, 16.0, new Colour(0.0f, 0.8f, 0.5f), new Colour(0.4f, 1.0f, 0.8f)));
         colourRules.setColourRule(3, colourRule);  
         
         List<String> properties = new ArrayList<String>();
         properties.add("type");
         properties.add("land");
         properties.add("water");
         properties.add("temp");
         
         ColourRuleSetWriter writer = new ColourRuleSetWriter();
         String result = writer.write(colourRules, "test", properties);
         
        assertEquals("colourset test\n" + 
         		"{\n" + 
         		"	property type\n" + 
         		"	{\n" + 
         		"		0.0 - 1.0 : rgb(1.0, 0.0, 0.0);\n" + 
         		"	}\n" + 
         		"	property land\n" + 
         		"	{\n" + 
         		"		0.0 - 10.0 : rgba(0.0, 0.25, 0.5, 0.5);\n" + 
         		"		10.0 - 20.0 : rgb(0.0, 0.8, 0.5);\n" + 
         		"	}\n" + 
         		"	property water\n" + 
         		"	{\n" + 
        		"		0.0 - 15.1 : rgb(0.0, 0.5, 0.8) rgb(0.0, 0.25, 0.5);\n" + 
         		"	}\n" + 
         		"	property temp\n" + 
         		"	{\n" + 
        		"		0.0 - 8.0 : rgb(0.5, 0.25, 0.0) rgb(0.0, 0.8, 0.5);\n" + 
        		"		8.0 - 16.0 : rgb(0.0, 0.8, 0.5) rgb(0.4, 1.0, 0.8);\n" + 
         		"	}\n" + 
         		"}\n", result);
 	}
 }
