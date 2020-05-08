 package com.redhat.ecs.services.docbookcompiling;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Text;
 
 import com.redhat.ecs.commonutils.DocBookUtilities;
 import com.redhat.ecs.commonutils.ExceptionUtilities;
 
 public class DocbookUtils 
 {
 	public static void setSectionTitle(final String titleValue, final Document doc)
 	{
 		assert doc != null : "The doc parameter can not be null";
 		
 		final Element newTitle = doc.createElement(DocBookUtilities.TOPIC_ROOT_TITLE_NODE_NAME);
 		newTitle.appendChild(doc.createTextNode(titleValue));
 
 		final Element docElement = doc.getDocumentElement();
 		if (docElement != null && docElement.getNodeName().equals(DocBookUtilities.TOPIC_ROOT_NODE_NAME))
 		{
 			/* remove any id attributes from the section */
 			docElement.removeAttribute("id");
 
 			final NodeList titleNodes = docElement.getElementsByTagName(DocBookUtilities.TOPIC_ROOT_TITLE_NODE_NAME);
 			/* see if we have a title node whose parent is the section */
 			if (titleNodes.getLength() != 0 && titleNodes.item(0).getParentNode().equals(docElement))
 			{
 				final Node title = titleNodes.item(0);
 				title.getParentNode().replaceChild(newTitle, title);
 			}
 			else
 			{
 				final Node firstNode = docElement.getFirstChild();
 				if (firstNode != null)
 					docElement.insertBefore(newTitle, firstNode);
 				else
 					docElement.appendChild(newTitle);
 			}
 		}
 		
 	}
 	
 	public static String buildChapter(final String contents, final String title)
 	{
 		return buildChapter(contents, title, null);
 	}
 	
 	public static String buildChapter(final String contents, final String title, final String id)
 	{
 		final String titleContents = title==null||title.length()==0?"":title;
 		final String chapterContents = contents==null||contents.length()==0?"":contents;
 		final String idAttribute = id==null||id.length()==0?"":" id=\"" + id + "\"";
 		return "<chapter" + idAttribute + "><title>" + titleContents + "</title>" + chapterContents + "</chapter>";
 	}
 	
 	public static String buildAppendix(final String contents, final String title)
 	{
 		return buildAppendix(contents, title, null);
 	}
 	
 	public static String buildAppendix(final String contents, final String title, final String id)
 	{
 		final String titleContents = title==null||title.length()==0?"":title;
 		final String chapterContents = contents==null||contents.length()==0?"":contents;
 		final String idAttribute = id==null||id.length()==0?"":" id=\"" + id + "\"";
 		return "<appendix" + idAttribute + "><title>" + titleContents + "</title>" + chapterContents + "</appendix>";
 	}
 	
 	public static String buildCleanSection(final String contents, final String title)
 	{
 		return buildCleanSection(contents, title, null);
 	}
 	
 	public static String buildCleanSection(final String contents, final String title, final String id)
 	{
 		final String titleContents = title==null||title.length()==0?"":title;
 		final String chapterContents = contents==null||contents.length()==0?"":contents;
 		final String idAttribute = id==null||id.length()==0?"":" id=\"" + id + "\"";
 		return "<section" + idAttribute + "><title>" + titleContents + "</title>" + chapterContents + "</section>";
 	}
 	
 	public static String addXMLBoilerplate(final String xml)
 	{
 		return "<?xml version='1.0' encoding='UTF-8' ?>\n" +
 		"<!DOCTYPE chapter PUBLIC \"-//OASIS//DTD DocBook XML V4.5//EN\" \"http://www.oasis-open.org/docbook/xml/4.5/docbookx.dtd\" [\n" +
 		"<!ENTITY % BOOK_ENTITIES SYSTEM \"Book.ent\">\n" +
 		"%BOOK_ENTITIES;\n" +
 		"]>\n\n" +
 		xml;
 	}
 	
 	public static String buildXRefListItem(final String xref, final String role)
 	{
 		final String roleAttribute = role==null||role.length()==0?"":" role=\"" + role + "\"";
 		return "<listitem><para><xref" + roleAttribute + " linkend=\"" + xref + "\"/></para></listitem>";
 	}
 	
 	public static List<Element> buildXRef(final Document xmlDoc, final String xref)
 	{
 		final List<Element> retValue = new ArrayList<Element>();
 		
 		final Element xrefItem = xmlDoc.createElement("xref");
 		xrefItem.setAttribute("linkend", xref);
 		
 		retValue.add(xrefItem);
 		
 		return retValue;
 	}
 	
 	public static List<Element> buildULink(final Document xmlDoc, final String url, final String label)
 	{
 		final List<Element> retValue = new ArrayList<Element>();
 		
 		final Element ulinkItem = xmlDoc.createElement("ulink");
 		ulinkItem.setAttribute("url", url);
 		
 		final Text labelElement = xmlDoc.createTextNode(label);
 		ulinkItem.appendChild(labelElement);
 		
 		retValue.add(ulinkItem);
 		
 		return retValue;
 	}
 	
 	public static String buildULinkListItem(final String url, final String label)
 	{
 		return "<listitem><para><ulink url=\"" + url + "\">" + label + "</ulink></para></listitem>";
 	}
 	
 	public static List<Element> buildEmphasisPrefixedXRef(final Document xmlDoc, final String prefix, final String xref)
 	{
 		final List<Element> retValue = new ArrayList<Element>();
 		
 		final Element emphasis = xmlDoc.createElement("emphasis");
 		emphasis.setTextContent(prefix);
 		retValue.add(emphasis);
 		
 		final Element xrefItem = xmlDoc.createElement("xref");
 		xrefItem.setAttribute("linkend", xref);
 		retValue.add(xrefItem);
 		
 		return retValue;
 	}
 	
 	public static List<Element> buildEmphasisPrefixedULink(final Document xmlDoc, final String prefix, final String url, final String label)
 	{
 		final List<Element> retValue = new ArrayList<Element>();
 		
 		final Element emphasis = xmlDoc.createElement("emphasis");
 		emphasis.setTextContent(prefix);
 		retValue.add(emphasis);
 		
 		final Element xrefItem = xmlDoc.createElement("ulink");
 		xrefItem.setAttribute("url", url);
 		retValue.add(xrefItem);
 		
 		final Text labelElement = xmlDoc.createTextNode(label);
 		xrefItem.appendChild(labelElement);
 		
 		return retValue;
 	}
 	
 	public static Node buildDOMXRefLinkListItem(final String xref, final String title, final Document xmlDoc)
 	{
 		final Element listItem = xmlDoc.createElement("listitem");
 		
 		final Element paraItem = xmlDoc.createElement("para");
 		listItem.appendChild(paraItem);
 		
 		final Element linkItem = xmlDoc.createElement("link");
 		linkItem.setAttribute("linkend", xref);
 		linkItem.setTextContent(title);
 		paraItem.appendChild(linkItem);
 		
 		return listItem;
 	}
 	
 	public static Node buildDOMLinkListItem(final List<Node> children, final Document xmlDoc)
 	{
 		final Element listItem = xmlDoc.createElement("listitem");
 		
 		final Element paraItem = xmlDoc.createElement("para");
 		listItem.appendChild(paraItem);
 		
 		for (final Node node : children)
 			paraItem.appendChild(node);
 		
 		return listItem;
 	}
 	
 	public static Node buildDOMXRef(final String xref, final String title, final Document xmlDoc)
 	{
 		final Element linkItem = xmlDoc.createElement("link");
 		linkItem.setAttribute("linkend", xref);
 		linkItem.setTextContent(title);
 		return linkItem;
 	}
 	
 	public static Node buildDOMText(final String title, final Document xmlDoc)
 	{
 		final Node textNode = xmlDoc.createTextNode(title);
 		return textNode;
 	}
 	
 	public static String buildListItem(final String text)
 	{
 		return "<listitem><para>" + text + "</para></listitem>\n";
 	}
 	
 	public static Element buildDOMListItem(final Document doc, final String text)
 	{
 		final Element listItem = doc.createElement("listitem");
 		final Element para = doc.createElement("para");
 		para.setTextContent(text);
 		listItem.appendChild(para);
 		return listItem;
 	}
 	
 	public static String buildSection(final String contents, final String title, final String build)
 	{
 		return buildSection(contents, title, build, null, null, null);
 	}
 	
 	public static String buildSection(final String contents, final String title, final String build, final String id)
 	{
 		return buildSection(contents, title, build, id, null, null);
 	}
 	
 	public static String buildSection(final String contents, final String title, final String build, final String id, final String titleRole)
 	{
 		return buildSection(contents, title, build, id, titleRole, null);
 	}
 	
 	public static String buildSection(final String contents, final String title, final String build, final String id, final String titleRole, final String xreflabel)
 	{
 		final String idAttribute = id==null||id.length()==0?"":" id=\"" + id + "\"";
 		final String xreflabelAttribute = xreflabel==null||xreflabel.length()==0?"":" xreflabel=\"" + xreflabel + "\"";
 		final String titleRoleAttribute = titleRole==null||titleRole.length()==0?"":" role=\"" + titleRole + "\"";
 		
 		return "<section" + idAttribute + xreflabelAttribute + ">\n" +
 			"<title" + titleRoleAttribute + ">" + title + "</title>\n" +
 			contents + "\n" +
 			
 			"	<simplesect>\n" +
 			"		<title></title>\n" +
 			"		<para>\n" + 
 			"			<remark>Built with Skynet version " + build + "</remark>\n" +
 			"		</para>\n" +
 			"	</simplesect>" +
 			
 			"</section>\n";
 	}
 	
 	public static String wrapInListItem(final String content)
 	{
 		return "<listitem>" + content + "</listitem>";
 	}
 	
 	public static String wrapListItems(final List<String> listItems)
 	{
 		return wrapListItems(listItems, null, null);
 	}
 	
 	public static String wrapListItems(final List<String> listItems, final String title)
 	{
 		return wrapListItems(listItems, title, null);
 	}
 	
 	public static String wrapListItems(final List<String> listItems, final String title, final String id)
 	{
 		final String idAttribute = id != null && id.length() != 0 ? " id=\"" + id + "\" " : "";
 		final String titleElement = title == null || title.length() == 0 ? "" : "<title>" + title + "</title>";
 		
 		String retValue = "<itemizedlist" + idAttribute + ">" + titleElement;
 		for (final String listItem : listItems)
 			retValue += listItem;
 		retValue += "</itemizedlist>";
 		return retValue;
 	}
 	
 	public static String wrapListItemsInPara(final String listItems)
 	{
 		if (listItems.length() != 0)
 		{
 			return 
 				"<para>" +
 				"<itemizedlist>\n" + listItems + "</itemizedlist>" +
 				"</para>";
 		}
 		
 		return "";
 	}
 
 	public static String wrapInPara(final String contents)
 	{
 		return wrapInPara(contents, null, null);
 	}
 	
 	public static String wrapInPara(final String contents, final String role)
 	{
 		return wrapInPara(contents, role, null);
 	}
 	
 	public static String wrapInPara(final String contents, final String role, final String id)
 	{
 		final String idAttribute = id==null||id.length()==0?"":" id=\"" + id + "\"";
 		final String roleAttribute = role==null||role.length()==0?"":" role=\"" + role + "\"";
 		return 
 			"<para" + idAttribute + roleAttribute + ">" +
 			contents +
 			"</para>";
 	}
 	
 	public static List<Element> wrapItemizedListItemsInPara(final Document xmlDoc, final List<List<Element>> items)
 	{
 		final List<Element> retValue = new ArrayList<Element>();
 		
 		final Element para = xmlDoc.createElement("para");
 		
 		final Element itemizedlist = xmlDoc.createElement("itemizedlist");
 		para.appendChild(itemizedlist);
 				
 		for (final List<Element> itemSequence : items)
 		{
 			final Element listitem = xmlDoc.createElement("listitem");
 			itemizedlist.appendChild(listitem);
 			
 			final Element listItemPara = xmlDoc.createElement("para");
 			listitem.appendChild(listItemPara);
 			
 			for (final Element item : itemSequence)
 			{				
 				listItemPara.appendChild(item);
 			}
 		}
 		
 		retValue.add(para);
 		
 		return retValue;
 	}
 	
 	public static List<Element> wrapOrderedListItemsInPara(final Document xmlDoc, final List<List<Element>> items)
 	{
 		final List<Element> retValue = new ArrayList<Element>();
 		
 		final Element para = xmlDoc.createElement("para");
 		
 		final Element orderedlist = xmlDoc.createElement("orderedlist");
 		para.appendChild(orderedlist);
 				
 		for (final List<Element> itemSequence : items)
 		{
 			final Element listitem = xmlDoc.createElement("listitem");
 			orderedlist.appendChild(listitem);
 			
 			final Element listItemPara = xmlDoc.createElement("para");
 			listitem.appendChild(listItemPara);
 			
 			for (final Element item : itemSequence)
 			{
 				listItemPara.appendChild(item);
 			}
 		}
 		
 		retValue.add(para);
 		
 		return retValue;
 	}
 	
 	public static List<Element> wrapItemsInListItems(final Document xmlDoc, final List<List<Element>> items)
 	{
 		final List<Element> retValue = new ArrayList<Element>();
 		
 		for (final List<Element> itemSequence : items)
 		{			
 			final Element listitem = xmlDoc.createElement("listitem");
 			final Element listItemPara = xmlDoc.createElement("para");
 			listitem.appendChild(listItemPara);
 			
 			for (final Element item : itemSequence)
 			{
 				listItemPara.appendChild(item);
 			}
 			
 			retValue.add(listitem);
 		}
 
 		return retValue;
 	}
 
 	public static String wrapInSimpleSect(final String contents)
 	{
 		return wrapInSimpleSect(contents, null, null);
 	}
 	
 	public static String wrapInSimpleSect(final String contents, final String role)
 	{
 		return wrapInSimpleSect(contents, null, null);
 	}
 	
 	public static String wrapInSimpleSect(final String contents, final String role, final String id)
 	{
 		final String roleAttribute = role==null||role.length()==0?"":" role=\"" + role + "\"";
 		final String idAttribute = id==null||id.length()==0?"":" id=\"" + id + "\"";
 		
 		return 
 			"<simplesect" + idAttribute + roleAttribute + ">\n"+
 			"\t<title></title>\n" +
 			contents + "\n" + 
 			"</simplesect>";
 	}
 	
 	public static Element wrapListItems(final Document xmlDoc, final List<Node> listItems)
 	{
 		return wrapListItems(xmlDoc, listItems, null);
 	}
 	
 	public static Element wrapListItems(final Document xmlDoc, final List<Node> listItems, final String title)
 	{
 		final Element paraElement = xmlDoc.createElement("para");
 		
 		final Element itemizedlistElement = xmlDoc.createElement("itemizedlist");
 		paraElement.appendChild(itemizedlistElement);
 		
 		if (title != null)
 		{
 			final Element titleElement = xmlDoc.createElement("title");
 			itemizedlistElement.appendChild(titleElement);
 			titleElement.setTextContent(title);
 		}
 		
 		for (final Node listItem : listItems)
 			itemizedlistElement.appendChild(listItem);
 		
 		return paraElement;
 	}
 	
 	public static void insertNodeAfter(final Node reference, final Node insert)
 	{
 		final Node parent = reference.getParentNode();
 		final Node nextSibling = reference.getNextSibling();
 		
 		if (parent == null)
 			return;
 		
 		if (nextSibling != null)
 			parent.insertBefore(insert, nextSibling);
 		else
 			parent.appendChild(insert);
 	}
 	
 	public static Node createRelatedTopicXRef(final Document xmlDoc, final String xref, final Node parent)
 	{
 		final Element listItem = xmlDoc.createElement("listitem");
 		if (parent != null)
 			parent.appendChild(listItem);
 		
 		final Element paraItem = xmlDoc.createElement("para");
 		listItem.appendChild(paraItem);
 		
 		final Element xrefItem = xmlDoc.createElement("xref");
 		xrefItem.setAttribute("linkend", xref);
 		paraItem.appendChild(xrefItem);
 		
 		return listItem;
 	}
 	
 	public static Node createRelatedTopicXRef(final Document xmlDoc, final String xref)
 	{
 		return createRelatedTopicXRef(xmlDoc, xref, null);
 	}
 	
 	public static Node createRelatedTopicULink(final Document xmlDoc, final String url, final String title, final Node parent)
 	{
 		final Element listItem = xmlDoc.createElement("listitem");
 		if (parent != null)
 			parent.appendChild(listItem);
 		
 		final Element paraItem = xmlDoc.createElement("para");
 		listItem.appendChild(paraItem);
 		
 		final Element xrefItem = xmlDoc.createElement("ulink");
 		xrefItem.setAttribute("url", url);
 		paraItem.appendChild(xrefItem);
 		
 		final Text labelElement = xmlDoc.createTextNode(title);
 		xrefItem.appendChild(labelElement);
 		
 		return listItem;
 	}
 	
 	public static Node createRelatedTopicULink(final Document xmlDoc, final String url, final String title)
 	{
 		return createRelatedTopicULink(xmlDoc, url, title, null);
 	}
 	
 	public static Node createRelatedTopicItemizedList(final Document xmlDoc, final String title)
 	{
 		final Node itemizedlist = xmlDoc.createElement("itemizedlist");
 		final Node itemizedlistTitle = xmlDoc.createElement("title");
 		itemizedlistTitle.setTextContent(title);
 		itemizedlist.appendChild(itemizedlistTitle);
 		
 		return itemizedlist;
 	}
 	
 	public static Document wrapDocumentInSection(final Document doc)
 	{
 		if (!doc.getDocumentElement().getNodeName().equals("section"))
 		{
 			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder dBuilder;
 			Document newDoc = null;
 			try
 			{
 				dBuilder = dbFactory.newDocumentBuilder();	
 				newDoc = dBuilder.newDocument();
 			}
 			catch(ParserConfigurationException ex)
 			{
 				ExceptionUtilities.handleException(ex);
 			}
 			Element section = newDoc.createElement("section");
			section.appendChild(newDoc.importNode(doc.getDocumentElement(), true));
 			newDoc.appendChild(section);
 			return newDoc;
 		}
 		else
 		{
 			return doc;
 		}
 	}
 }
