 /* ===============================================================================
 *
 * Part of the InfoGlue Content Management Platform (www.infoglue.org)
 *
 * ===============================================================================
 *
 *  Copyright (C)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2, as published by the
 * Free Software Foundation. See the file LICENSE.html for more information.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
 * Place, Suite 330 / Boston, MA 02111-1307 / USA.
 *
 * ===============================================================================
 */
 
 package org.infoglue.cms.controllers.kernel.impl.simple;
 
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.xerces.parsers.DOMParser;
 import org.infoglue.cms.applications.databeans.ComponentPropertyDefinition;
 import org.infoglue.cms.applications.databeans.ComponentPropertyOptionDefinition;
 import org.infoglue.cms.entities.kernel.BaseEntityVO;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 
 /**
  * This class handles aspects of component properties definitions.
  * 
  * @author Mattias Bogeblad
  */
 
 public class ComponentPropertyDefinitionController extends BaseController
 {
 
     /**
 	 * Factory method
 	 */
 
 	public static ComponentPropertyDefinitionController getController()
 	{
 		return new ComponentPropertyDefinitionController();
 	}
 	
 	public List parseComponentPropertyDefinitions(String xml)
 	{
 	    List componentPropertyDefinitions = new ArrayList();
 	    
 	    if(xml == null || xml.equals(""))
 	        return componentPropertyDefinitions;
 	    
 	    try
 	    {
 	        InputSource xmlSource = new InputSource(new StringReader(xml));
 
 			DOMParser parser = new DOMParser();
 			parser.parse(xmlSource);
 			Document document = parser.getDocument();
 
 			NodeList nl = document.getElementsByTagName("properties");
 			for(int i=0; i<nl.getLength(); i++)
 			{
 			    Node propertiesNode = nl.item(i);
 			    Element propertiesElement = (Element)propertiesNode;
 			    
 				NodeList propertyNodeList = propertiesElement.getElementsByTagName("property");
 				for(int j=0; j<propertyNodeList.getLength(); j++)
 				{
 				    Node propertyNode = propertyNodeList.item(j);
 				    Element propertyElement = (Element)propertyNode;
 				    
 				    String name 					= propertyElement.getAttribute("name");
 				    String type 					= propertyElement.getAttribute("type");
 				    String entity 					= propertyElement.getAttribute("entity");
 				    String multiple 				= propertyElement.getAttribute("multiple");
 				    String allowedContentTypeNames 	= propertyElement.getAttribute("allowedContentTypeDefinitionNames");
 				    String description				= propertyElement.getAttribute("description");
 				    				    
 				    ComponentPropertyDefinition cpd = new ComponentPropertyDefinition(name, type, entity, new Boolean(multiple), allowedContentTypeNames, description);
 
 				    
					NodeList optionsNodeList = propertiesElement.getElementsByTagName("option");
 					for(int k=0; k<optionsNodeList.getLength(); k++)
 					{
 					    Node optionNode = optionsNodeList.item(k);
 					    Element optionElement = (Element)optionNode;
 					    
 					    String optionName 	= optionElement.getAttribute("name");
 					    String optionValue 	= optionElement.getAttribute("value");
 					    				    
 					    ComponentPropertyOptionDefinition cpod = new ComponentPropertyOptionDefinition(optionName, optionValue);
 					    
 					    cpd.getOptions().add(cpod);
 					}
 
 					
 				    componentPropertyDefinitions.add(cpd);
 				}
 			}
 		}
 	    catch(Exception e)
 	    {
 	        e.printStackTrace();
 	    }
 	    
 	    return componentPropertyDefinitions;
 	}
 
     public BaseEntityVO getNewVO()
     {
         return null;
     }
 
 	
 	
 }
