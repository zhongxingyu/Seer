 /** 
  * Author: Martin van Velsen <vvelsen@cs.cmu.edu>
  * 
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Lesser General Public License as 
  *  published by the Free Software Foundation, either version 3 of the 
  *  License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 
 package edu.cmu.cs.in.base;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.io.StringReader;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 
 /**
  * This class is the workhorse for anything XML. You can derive from it to get easy
  * xml parsing and creation, or you can use it as an exernal tool. Be aware that
  * HoopXMLBase inherits from HoopBase and that means you shouldn't create loads of
  * instances of it since that would waste a lot of memory. You might notice that
  * the code has some old commented out w3c methods and usages. We've completely
  * switched to jdom for easy parsing and generation of xml where we also don't
  * need to get the SAX parser separately.
  */
 public class HoopXMLBase extends HoopRoot
 {			
 	private String xmlid ="hoopxml";
 	private Document doc = null;
 	
 	/**
 	*
 	*/	
 	public HoopXMLBase ()
 	{  
     	setClassName ("HoopXMLBase");
    	//debug ("HoopXMLBase ()");
 	}
 	/**
 	 * 
 	 */	
 	public String getXMLID() 
 	{
 		return xmlid;
 	}
 	/**
 	 * 
 	 */	
 	public void setXMLID(String anID) 
 	{
 		this.xmlid = anID;
 	}		
 	/**
 	 * 
 	 */	
 	public Document getDoc() 
 	{
 		return doc;
 	}
 	/**
 	 * 
 	 */	
 	public void setDoc(Document doc) 
 	{
 		this.doc = doc;
 	}	
 	/**
 	*
 	*/	
     public Element loadXMLFromString (String xml)
     {
     	debug ("loadXMLFromString ()");
     	
     	debug (xml);
     	
     	xml=xml.trim();
     	
     	if (xml.indexOf ("<?xml")==-1)
     	{
     		xml="<?xml version=\"1.0\" encoding=\"utf-8\"?>"+xml;
     	}
     	
     	SAXBuilder builder = new SAXBuilder();
     	Reader in = new StringReader(xml);
     	Element root = null;
   
     	try
     	{
     		doc = builder.build(in);
     		root = doc.getRootElement();
 
     	} 
     	catch (JDOMException e)
     	{
     		// do what you want
     		return (null);
     	} 
     	catch (IOException e)
     	{
     		// do what yo want
     		return (null);
     	} 
     	catch (Exception e)
     	{
     		// do what you want
     		return (null);
     	}
     	
     	return (root);
     }		
 	/**
 	*
 	*/	
     public Element fromXMLString (String xml)
     {
     	debug ("fromXMLString ()");
 
     	return (loadXMLFromString (xml));
     }
 	/**
 	*
 	*/	
 	public Boolean fromXML(Element root) 
 	{
 		debug ("fromXML ()");
 
 		if (root.getName().equals(this.getXMLID())==false)
 		{
 			debug ("Error: element name is not " + this.getXMLID());
 			return (false);			
 		}
 		
 		this.setInstanceName (root.getAttributeValue("instance"));
 
 		return (true);
 	}
 	/**
 	*
 	*/	
 	public Element toXMLID() 
 	{
 		debug ("toXMLID ()");
 		
 		Element baseElement=new Element (this.getXMLID());
 		
 		baseElement.setAttribute("class",this.getClassName());
 		baseElement.setAttribute("instance",this.getInstanceName());		
 				
 		return (baseElement);		
 	}
 	/**
 	*
 	*/	
 	public Element toXML() 
 	{
 		debug ("toXML ()");
 		
 		return (toXMLID());
 	}	
 	/**
 	*	
 	*/	    
     protected String getAttributeValue (Element aNode,String anAttribute)
     {    	    	
     	return (aNode.getAttributeValue(anAttribute));
     }
 }
