 package com.xaf.xml;
 
 /**
  * Title:        XML document container (automatically manages "include" files)
  * Description:
  * Copyright:    Copyright (c) 2001
  * Company:      Netspective Communications Corporation
  * @author Shahid N. Shah
  * @version 1.0
  */
 
 import java.io.*;
 import java.util.*;
 import org.w3c.dom.*;
 import javax.xml.parsers.*;
 
 public class XmlSource
 {
 	public class SourceInfo
 	{
 		protected File source;
 		protected long lastModified;
 		protected SourceInfo parent;
 
 		SourceInfo(SourceInfo includedFrom, File file)
 		{
 			source = file;
 			lastModified = source.lastModified();
 			parent = includedFrom;
 		}
 
 		public File getFile() { return source; }
 		public SourceInfo getParent() { return parent; }
 
 		public boolean sourceChanged()
 		{
 			return source.lastModified() > this.lastModified;
 		}
 	}
 
 	protected boolean allowReload = true;
 	protected ArrayList errors = new ArrayList();
 	protected SourceInfo docSource;
 	protected Hashtable sourceFiles = new Hashtable();
 	protected Document xmlDoc;
 
 	public Document getDocument()
 	{
 		reload();
 		return xmlDoc;
 	}
 
 	public List getErrors() { return errors; }
	public void addError(String msg) { errors.add(msg); }

 	public SourceInfo getSourceDocument() { return docSource; }
 	public Map getSourceFiles() { return sourceFiles; }
 
 	public boolean sourceChanged()
 	{
 		if(docSource == null)
 			return false;
 
 		if(sourceFiles.size() > 1)
 		{
 			for(Enumeration e = sourceFiles.elements(); e.hasMoreElements(); )
 			{
 				if(((SourceInfo) e.nextElement()).sourceChanged())
 					return true;
 			}
 		}
 		else
 			return docSource.sourceChanged();
 
 		// if we get to here, none of the files is newer than what's in memory
 		return false;
 	}
 
 	public void reload()
 	{
 		if(allowReload && docSource != null && sourceChanged())
 			loadDocument(docSource.getFile());
 	}
 
 	public boolean loadDocument(File file)
 	{
 		docSource = null;
 		xmlDoc = null;
 		loadXML(file);
 		catalogNodes();
 		return errors.size() == 0 ? true : false;
 	}
 
 	public String findElementOrAttrValue(Element elem, String nodeName)
 	{
 		String attrValue = elem.getAttribute(nodeName);
 		if(attrValue.length() > 0)
 			return attrValue;
 
 		NodeList children = elem.getChildNodes();
 		for(int n = 0; n < children.getLength(); n++)
 		{
 			Node node = children.item(n);
 			if(node.getNodeName().equals(nodeName))
 				return node.getFirstChild().getNodeValue();
 		}
 
 		return null;
 	}
 
 	public String ucfirst(String str)
 	{
 		return str.substring(0, 1).toUpperCase() + str.substring(1);
 	}
 
 	public void inheritNodes(Element element, Hashtable sourcePool, String attrName)
 	{
 		String inheritAttr = element.getAttribute(attrName);
 		while(inheritAttr != null && inheritAttr.length() > 0)
 		{
 			Element inheritFromElem = null;
 			StringTokenizer inheritST = new StringTokenizer(inheritAttr, ",");
 			String[] inherits = new String[15];
 			int inheritsCount = 0;
 			while(inheritST.hasMoreTokens())
 			{
 				inherits[inheritsCount] = inheritST.nextToken();
 			    inheritsCount++;
 			}
 
             /** we're going to work backwards because we want to make sure the
              *  elements are added in the appropriate order (same order as the
              *  inheritance list)
              */
 
 			for(int j = (inheritsCount-1); j >= 0; j--)
 			{
 				String inheritType = inherits[j];
 				inheritFromElem = (Element) sourcePool.get(inheritType);
 				if(inheritFromElem == null)
 				{
 					errors.add("can not extend '"+ element.getAttribute("name") +"' from '"+ inheritType +"': source not found");
 					continue;
 				}
 
                 Element extendsElem = xmlDoc.createElement("extends");
                 extendsElem.appendChild(xmlDoc.createTextNode(inheritType));
                 element.appendChild(extendsElem);
 
 				NamedNodeMap inhAttrs = inheritFromElem.getAttributes();
 				for(int i = 0; i < inhAttrs.getLength(); i++)
 				{
 					Node attrNode = inhAttrs.item(i);
 					if(element.getAttribute(attrNode.getNodeName()).equals(""))
 						element.setAttribute(attrNode.getNodeName(), attrNode.getNodeValue());
 				}
 
 				DocumentFragment inheritFragment = xmlDoc.createDocumentFragment();
 				NodeList inhChildren = inheritFromElem.getChildNodes();
 				for(int i = inhChildren.getLength()-1; i >= 0; i--)
 				{
 					Node childNode = inhChildren.item(i);
 
 					// only add if there isn't an attribute overriding this element
 					if(element.getAttribute(childNode.getNodeName()).length() == 0)
 					{
 						Node cloned = childNode.cloneNode(true);
 						if(cloned.getNodeType() == Node.ELEMENT_NODE)
 							((Element) cloned).setAttribute("_inherited-from", inheritType);
 						inheritFragment.insertBefore(cloned, inheritFragment.getFirstChild());
 					}
 				}
 
 				element.insertBefore(inheritFragment, element.getFirstChild());
 			}
 
 			// find the next one if we have more parents
 			if(inheritFromElem != null)
 				inheritAttr = inheritFromElem.getAttribute(attrName);
 			else
 				inheritAttr = null;
 		}
 	}
 
     public void replaceNodeValue(Node node, String findStr, String replStr)
     {
         String srcStr = node.getNodeValue();
 		if(srcStr == null || findStr == null || replStr == null)
 			return;
 
         int findLoc = srcStr.indexOf(findStr);
         if(findLoc >= 0)
         {
             StringBuffer sb = new StringBuffer(srcStr);
             sb.replace(findLoc, findLoc + findStr.length(), replStr);
             node.setNodeValue(sb.toString());
         }
     }
 
     public void replaceNodeMacros(Node inNode, HashSet nodeNames, Hashtable params)
     {
         if(params == null || params.size() == 0)
             return;
 
 		String nodeType = inNode.getParentNode().getNodeName() + "." + inNode.getNodeName();
         Enumeration e = params.keys();
         while(e.hasMoreElements())
         {
 			String paramName = (String) e.nextElement();
             String paramRepl = "$" + paramName + "$";
             String paramValue = (String) params.get(paramName);
 			if(paramValue == null)
 				continue;
 
             NamedNodeMap attrs = inNode.getAttributes();
             if(attrs != null && attrs.getLength() > 0)
             {
                 for(int i = 0; i < attrs.getLength(); i++)
                 {
                     Node attr = attrs.item(i);
                     if(nodeNames.contains(attr.getNodeName()))
                         replaceNodeValue(attr, paramRepl, paramValue);
                 }
             }
             NodeList children = inNode.getChildNodes();
             for(int c = 0; c < children.getLength(); c++)
             {
                 Node node = children.item(c);
                 if(node.getNodeType() == Node.ELEMENT_NODE && nodeNames.contains(node.getNodeName()))
                     replaceNodeValue(node.getFirstChild(), paramRepl, paramValue);
             }
         }
     }
 
 	public void catalogNodes()
 	{
 	}
 
 	public Document loadXML(File file)
 	{
 		if(docSource == null)
 		{
 			errors.clear();
 			sourceFiles.clear();
 		}
 
 		SourceInfo sourceInfo = new SourceInfo(docSource, file);
 		sourceFiles.put(file.getAbsolutePath(), sourceInfo);
 
 		Document doc = null;
 		try
 		{
 			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder parser = factory.newDocumentBuilder();
 			doc = parser.parse(file);
 			doc.normalize();
 		}
 		catch(Exception e)
 		{
 			throw new RuntimeException("XML Parsing error in '" + file.getAbsolutePath() + "': " + e);
 		}
 
 		if(docSource == null)
 		{
 			xmlDoc = doc;
 			docSource = sourceInfo;
 		}
 
 		/*
 		 find all of the <include file="xyz"> elements and "include" all the
 		 elements in that document as children of the main document
 		*/
 
 		Element rootElem = doc.getDocumentElement();
 		NodeList includes = rootElem.getElementsByTagName("include");
 		if(includes != null && includes.getLength() > 0)
 		{
 			for(int n = 0; n < includes.getLength(); n++)
 			{
 				Element include = (Element) includes.item(n);
                 String incFileAttr = include.getAttribute("file");
 				File incFile = new File(file.getParentFile(), incFileAttr);
 				if(! sourceFiles.containsKey(incFile.getAbsolutePath()))
 				{
 					Document includeDoc = loadXML(incFile);
 					if(includeDoc != null)
 					{
 						Element includeRoot = includeDoc.getDocumentElement();
 						NodeList incChildren = includeRoot.getChildNodes();
 						for(int c = 0; c < incChildren.getLength(); c++)
 						{
                             Node incCopy = doc.importNode(incChildren.item(c), true);
                             if(incCopy.getNodeType() == Node.ELEMENT_NODE)
                                 ((Element) incCopy).setAttribute("_included-from", incFileAttr);
 							rootElem.insertBefore(incCopy, include);
 						}
 					}
 				}
 			}
 		}
 
 		return doc;
  	}
 }
