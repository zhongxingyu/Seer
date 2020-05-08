 /**
 Copyright (C) 2012  Delcyon, Inc.
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.delcyon.capo.controller.elements;
 
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import com.delcyon.capo.CapoApplication;
 import com.delcyon.capo.controller.AbstractControl;
 import com.delcyon.capo.controller.ControlElementProvider;
 import com.delcyon.capo.xml.XPath;
 
 /**
  * @author jeremiah
  *
  */
 
 @ControlElementProvider(name="repeat")
 public class RepeatElement extends AbstractControl 
 {
 
 	
 	private enum Attributes
 	{
 		nodeset,indexVar
 	}
 	
 	private static final String[] supportedNamespaces = {CapoApplication.SERVER_NAMESPACE_URI};
 	
 	@Override
 	public Attributes[] getAttributes()
 	{
 		return Attributes.values();
 	}
 	
 	@Override
 	public Attributes[] getRequiredAttributes()
 	{
 		return new Attributes[]{Attributes.nodeset};
 	}
 
 	
 	@Override
 	public String[] getSupportedNamespaces()
 	{
 		return supportedNamespaces;
 	}
 
 //	private static final String[] functionNames = {"context"};
 //	
 //	@Override
 //	public String[] getXPathFunctionNames()
 //	{
 //		return functionNames;
 //	}
 //	
 //	@Override
 //	public String processFunction(String prefix, String functionName, String... arguments)
 //	{
 //		if (functionName.equals("context"))
 //		{
 //			try
 //			{
 //				return XPath.getXPath(currentContext);
 //			} catch (Exception e)
 //			{
 //				// TODO Auto-generated catch block
 //				e.printStackTrace();
 //				return null;
 //			}
 //		}
 //		else
 //		{
 //			return null;
 //		}
 //	}
 //	
 //	private Node currentContext = null;
 
 	
 	
 	@Override
 	public Object processServerSideElement() throws Exception
 	{
 		
 	
 		String nodeset = getAttributeValue(Attributes.nodeset);		
 		String indexVar = getAttributeValue(Attributes.indexVar);		
 		if (indexVar == null || indexVar.isEmpty())
 		{
 			indexVar = "index";
 		}
 		
 		
 		
 		NodeList nodeList = XPath.selectNodes(getControlElementDeclaration(), nodeset);
 		for(int index = 0; index < nodeList.getLength(); index++)
 		{
		    //make a copy of the original Element
		    Element originalElement = getControlElementDeclaration();
			Element tempGroupElement = (Element) originalElement.cloneNode(true);
			
			//replace the current element with the temp element, this is so out of context xpaths will still work
			originalElement.getParentNode().replaceChild(tempGroupElement, originalElement);
 			
 			GroupElement groupElement = new GroupElement();			
 			groupElement.init(tempGroupElement, groupElement, getParentGroup(), getControllerClientRequestProcessor());	
 			groupElement.getGroup().set(indexVar, index+"");
 			groupElement.getGroup().set("context", XPath.getXPath(nodeList.item(index)));		
 			groupElement.processServerSideElement();
			
			//once we're done, switch them back
			tempGroupElement.getParentNode().replaceChild(originalElement, tempGroupElement);
			
 		}
 		return null;
 	}
 
 }
