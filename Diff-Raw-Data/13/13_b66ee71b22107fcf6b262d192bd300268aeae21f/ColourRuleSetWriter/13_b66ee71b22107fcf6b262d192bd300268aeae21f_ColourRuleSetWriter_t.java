 package com.hexcore.cas.model;
 
import java.text.DecimalFormat;
 import java.util.List;
 
 import com.hexcore.cas.ui.toolkit.Colour;
 
 public class ColourRuleSetWriter
 {
 	public ColourRuleSetWriter()
 	{
 	}
 	
 	public String write(ColourRuleSet colourRuleSet, String name, List<String> properties)
 	{	
 		String str = "colourset " + name + "\n{\n";
 		
 		for (int i = 0; i < colourRuleSet.getNumProperties(); i++)
 			str += writeProperty(colourRuleSet.getColourRule(i), properties.get(i));
 		
 		return str + "}\n";
 	}
 	
 	private String writeProperty(ColourRule colourRule, String name)
 	{
 		String str = "\tproperty " + name + "\n\t{\n";
 		
 		for (ColourRule.Range range : colourRule.ranges)
 			str += writeRange(range);
 		
 		return str + "\t}\n";
 	}
 	
 	private String writeRange(ColourRule.Range range)
 	{
 		String str = "\t\t" + range.from + " - " + range.to + " : ";
 		
 		switch (range.getType())
 		{
 			case SOLID:
 				str += writeColour(range.getColour(0));
 				break;
 			case GRADIENT:
 				str += writeColour(range.getColour(0)) + " ";
 				str += writeColour(range.getColour(1));
 				break;
 		}
 		
 		return str + ";\n";
 	}
 	
 	private String writeColour(Colour colour)
 	{
		DecimalFormat formater = new DecimalFormat("#.####");
		
 		String str = (colour.a < 1.0) ? "rgba(" : "rgb(";
		str += formater.format(colour.r);
		str += ", " + formater.format(colour.g);
		str += ", " + formater.format(colour.b);
		if (colour.a < 1.0) str += ", " + formater.format(colour.a);
 		return str + ")";
 	}
 }
