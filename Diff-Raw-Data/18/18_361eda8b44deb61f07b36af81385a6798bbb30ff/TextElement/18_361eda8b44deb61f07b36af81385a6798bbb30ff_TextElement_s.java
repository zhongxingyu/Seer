 package de.berlios.statcvs.xml.output;
 
 import java.util.Date;
 
 import org.jdom.Element;
 
 import de.berlios.statcvs.xml.util.Formatter;
 
 /**
  * ElementContainer
  * 
  * @author Steffen Pingel
  */
 public class TextElement extends Element {
 
 	ReportSettings settings;
 
 	public TextElement(ReportSettings settings, String key) 
 	{
 		super("container");
 		
 		this.settings = settings;
 		
 		setAttribute("key", key);
 	}
 
 	public ListElement addList()
 	{
		return new ListElement();
 	}
 
 	public TextElement addPeriod(String name, Date from, Date to) 
 	{
 		Element element = new Element("period");
 		element.setAttribute("name", name);
 		element.setAttribute("from", Formatter.formatDate(from));
 		if (to != null) {
 			element.setAttribute("to", Formatter.formatDate(to));
 		}
 		addContent(element);
 		return this;
 	}
 
 	public TextElement addPeriod(String name, Date at) 
 	{
 		return addPeriod(name, at, null);
 	}
 
 	public TextElement addValue(String key, long value, String description) 
 	{
 		Element element = new Element("value");
 		element.setAttribute("key", name);
 		element.setAttribute("value", value + "");
 		element.setText(description);
 		addContent(element);
 		return this;
 	}
 
 	public TextElement addValue(String key, double value, double percentValue,
 							   String description) 
 	{
 		Element element = new Element("value");
 		element.setAttribute("key", name);
 		element.setAttribute("value", value + "");
 		element.setAttribute("percentage", value + "");
 		element.setText(description);
 		addContent(element);
 		return this;
 	}
 
 	public class ListElement extends Element
 	{
 		
 		public ListElement()
 		{
 			super("ul");
 		}
 		
 		public ListElement addItem(String filename, String link)
 		{
 			Element element = new Element("link");
 			element.setAttribute("ref", filename);
 			element.setText(link);
 			
 			Element item = new Element("li");
 			item.addContent(element);
 			
 			return this;
 		}
 		
 	}
 
 }
