 package com.xaf.config;
 
 import java.util.*;
 import org.w3c.dom.*;
 import com.xaf.value.*;
 
 public class Configuration extends HashMap
 {
 	static public class ReplacementInfo
 	{
 		public StringBuffer result = new StringBuffer();
 		public int dynamicReplacementsCount;
 
 		public boolean isFinal() { return dynamicReplacementsCount == 0 ? true : false; }
 	}
 
 	public final static String REPLACEMENT_PREFIX = "${";
 	private String name;
 
     public Configuration()
     {
     }
 
     public Configuration(String name)
     {
 		this.name = name;
     }
 
 	public String getName()
 	{
 		return name;
 	}
 
     /** Replace ${NAME} with the property value
 	 *  and keep track of whether there are any "dynamic" values that are within
 	 *  the property. As a property replacement value becomes final (only replacements
 	 *  with other static values) replace the expression with a specific value.
      */
     public ReplacementInfo replaceProperties(ValueContext vc, String value)
     {
 		ReplacementInfo ri = new ReplacementInfo();
 
         StringBuffer sb = ri.result;
         int i = 0;
         int prev = 0;
 
         int pos;
         while((pos=value.indexOf( "$", prev )) >= 0)
 		{
             if(pos>0)
 			{
                 sb.append(value.substring( prev, pos ));
             }
             if( pos == (value.length() - 1))
 			{
                 sb.append('$');
                 prev = pos + 1;
             }
             else if (value.charAt( pos + 1 ) != '{')
 			{
                 sb.append(value.charAt(pos + 1));
                 prev=pos+2;
             }
 			else
 			{
                 int endName=value.indexOf('}', pos);
                 if( endName < 0 )
 				{
                     throw new RuntimeException("Syntax error in prop: " + value);
                 }
                 String expression = value.substring(pos+2, endName);
 				Property property = (Property) get(expression);
 				if(property != null)
 				{
 					ReplacementInfo subRi = replaceProperties(vc, property.getValue(vc));
 					if(subRi.isFinal())
 						property.setFinalValue(subRi.result.toString());
 					else if(property.flagIsSet(Property.PROPFLAG_FINALIZE_ON_FIRST_GET))
 						property.setFinalValue(subRi.result.toString());
 					else
 						ri.dynamicReplacementsCount += subRi.dynamicReplacementsCount;
                     sb.append(subRi.result);
 				}
 				else
 				{
 					SingleValueSource vs = ValueSourceFactory.getSingleValueSource(expression);
 					if(vs != null)
 						sb.append(vs.getValueOrBlank(vc));
 					else
 		                sb.append("${" + expression + "}");
 					ri.dynamicReplacementsCount++;
 				}
 
                 prev=endName+1;
             }
         }
 
         if(prev < value.length()) sb.append(value.substring(prev));
         return ri;
     }
 
 	public String getValue(ValueContext vc, Property property, String defaultValue)
 	{
 		if(property != null)
 		{
 		    String value = property.getValue(vc);
 			if(property.hasReplacements())
 			{
 				ReplacementInfo ri = replaceProperties(vc, value);
 				String result = ri.result.toString();
 				if(ri.isFinal())
 					property.setFinalValue(result);
 				else if(property.flagIsSet(Property.PROPFLAG_FINALIZE_ON_FIRST_GET))
 					property.setFinalValue(result);
 				return result;
 			}
 			else
 				return value;
 		}
 		else
 			return defaultValue;
 	}
 
 	public String getValue(ValueContext vc, String name, String defaultValue)
 	{
 		return getValue(vc, (Property) get(name), defaultValue);
 	}
 
 	public String getValue(ValueContext vc, String name)
 	{
 		return getValue(vc, (Property) get(name), null);
 	}
 
 	public Collection getValues(ValueContext vc, String name)
 	{
 		PropertiesCollection property = (PropertiesCollection) get(name);
		if(property == null)
			return null;
 		return property.getCollection();
 	}
 
 	public void importFromXml(Element elem, ConfigurationManager manager)
 	{
 		if(name == null)
 		{
 			String name = elem.getAttribute("name");
 			if(name.length() == 0)
 				name = null;
 		}
 
 		NodeList children = elem.getChildNodes();
 		for(int c = 0; c < children.getLength(); c++)
 		{
 			Node childNode = children.item(c);
 			if(childNode.getNodeType() != Node.ELEMENT_NODE)
 				continue;
 
             String childName = childNode.getNodeName();
 			if(childName.equals("property"))
 			{
 				Element propertyElem = (Element) childNode;
 				String propType = propertyElem.getAttribute("type");
 				if(propType.length() == 0 || propType.equals("text"))
 				{
 					Property prop = new StringProperty();
 					prop.importFromXml(propertyElem);
 					put(prop.getName(), prop);
 				}
 				else
 				{
 					manager.addError("Unknown property type '"+propType+"'");
 				}
 			}
 			else if(childName.equals("system-property"))
 			{
 				Element propertyElem = (Element) childNode;
 				Property prop = new StringProperty();
 				prop.importFromXml(propertyElem);
 
 				System.setProperty(prop.getName(), prop.getValue(null));
 			}
 			else if(childName.equals("properties"))
 			{
 				Element propertiesElem = (Element) childNode;
 				String propType = propertiesElem.getAttribute("type");
 				if(propType.length() == 0 || propType.equals("list"))
 				{
 					PropertiesCollection propColl = new PropertiesList();
 					propColl.importFromXml(propertiesElem, manager, this);
 					put(propColl.getName(), propColl);
 				}
 				else
 				{
 					manager.addError("Unknown properties type '"+propType+"'");
 				}
 			}
 		}
 	}
 }
