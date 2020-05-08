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
 import java.lang.reflect.*;
 import java.util.*;
 
 import javax.servlet.*;
 import javax.xml.parsers.*;
 import javax.xml.transform.*;
 
 import org.w3c.dom.*;
 import org.apache.xpath.objects.*;
 import org.apache.xpath.*;
 
 import com.xaf.Metric;
 
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
 	protected Element metaInfoElem;
 	protected Element metaInfoOptionsElem;
 	protected Set inheritanceHistorySet = new HashSet();
 
 	public boolean getAllowReload() { return allowReload; }
 	public void setAllowReload(boolean value) { allowReload = value; }
 
 	public void initializeForServlet(ServletContext servletContext)
 	{
 		if(com.xaf.config.ConfigurationManagerFactory.isProductionOrTestEnvironment(servletContext))
 			setAllowReload(false);
 	}
 
     /**
      * returns the boolean equivalent of a string, which is considered true
      * if either "on", "true", or "yes" is found, ignoring case.
      */
     public static boolean toBoolean(String s)
 	{
         return (s.equalsIgnoreCase("on") ||
                 s.equalsIgnoreCase("true") ||
                 s.equalsIgnoreCase("yes"));
     }
 
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
 
 	public void forceReload()
 	{
 		loadDocument(docSource.getFile());
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
 
 	public void inheritNodes(Element element, Map sourcePool, String attrName)
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
 
 				/* don't inherit the same objects more than once */
 				String inheritanceId = Integer.toString(element.hashCode()) + '.' + Integer.toString(inheritFromElem.hashCode());
 				if(inheritanceHistorySet.contains(inheritanceId))
 					continue;
 				inheritanceHistorySet.add(inheritanceId);
 
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
 
 	public void addMetaInfoOptions()
 	{
 		if(metaInfoOptionsElem != null)
 		    metaInfoElem.removeChild(metaInfoOptionsElem);
 
 	    metaInfoOptionsElem = xmlDoc.createElement("options");
 		metaInfoOptionsElem.setAttribute("name", "Allow reload");
 		metaInfoOptionsElem.setAttribute("value", (allowReload ? "Yes" : "No"));
 		metaInfoElem.appendChild(metaInfoOptionsElem);
 	}
 
 	public void addMetaInformation()
 	{
 		metaInfoElem = xmlDoc.createElement("meta-info");
 		xmlDoc.getDocumentElement().appendChild(metaInfoElem);
 
 		addMetaInfoOptions();
 
 		Element filesElem = xmlDoc.createElement("source-files");
 		metaInfoElem.appendChild(filesElem);
 
 		for(Iterator sfi = sourceFiles.values().iterator(); sfi.hasNext(); )
 		{
 			SourceInfo si = (SourceInfo) sfi.next();
 			Element fileElem = xmlDoc.createElement("source-file");
 			fileElem.setAttribute("abs-path", si.getFile().getAbsolutePath());
 			if(si.getParent() != null)
 				fileElem.setAttribute("included-from", si.getParent().getFile().getName());
 			filesElem.appendChild(fileElem);
 		}
 
 		if(errors.size() > 0)
 		{
 			Element errorsElem = xmlDoc.createElement("errors");
 	    	metaInfoElem.appendChild(errorsElem);
 
 			for(Iterator ei = errors.iterator(); ei.hasNext(); )
 			{
 				Element errorElem = xmlDoc.createElement("error");
 				Text errorText = xmlDoc.createTextNode((String) ei.next());
 				errorElem.appendChild(errorText);
 				errorsElem.appendChild(errorElem);
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
 			metaInfoElem = null;
 			metaInfoOptionsElem = null;
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
 
 	public void saveXML(String fileName)
 	{
 		/* we use reflection so that org.apache.xml.serialize.* is not a package requirement */
 
 		OutputStream os = null;
 		try
 		{
 			Class serializerCls = Class.forName("org.apache.xml.serialize.XMLSerializer");
 			Class outputFormatCls = Class.forName("org.apache.xml.serialize.OutputFormat");
 
 			Constructor serialCons = serializerCls.getDeclaredConstructor(new Class[] { OutputStream.class, outputFormatCls });
 			Constructor outputCons = outputFormatCls.getDeclaredConstructor(new Class[] { Document.class });
 
 			os = new FileOutputStream(fileName);
 			Object outputFormat = outputCons.newInstance(new Object[] { xmlDoc });
 			Method indenting = outputFormatCls.getMethod("setIndenting", new Class[] { boolean.class });
 			indenting.invoke(outputFormat, new Object[] { new Boolean(true) });
 
 			Object serializer = serialCons.newInstance(new Object[] { os, outputFormat });
 			Method serialize = serializerCls.getMethod("serialize", new Class[] { Document.class });
 			serialize.invoke(serializer, new Object[] { xmlDoc });
 		}
 		catch(Exception e)
 		{
 			throw new RuntimeException("Unable to save '" + fileName + "': " + e);
 		}
 		finally
 		{
 			try
 			{
 				if(os != null)
 	    			os.close();
 			}
 			catch(Exception e)
 			{
 			}
 		}
 	}
 
 	public NodeList selectNodeList(String expr) throws TransformerException
 	{
 		return XPathAPI.selectNodeList(xmlDoc, expr);
 	}
 
 	public long getSelectNodeListCount(String expr) throws TransformerException
 	{
 		NodeList nodes = selectNodeList(expr);
 		return nodes.getLength();
 	}
 
 	public Metric getMetrics(Metric root)
 	{
 		return null;
 	}
 }
