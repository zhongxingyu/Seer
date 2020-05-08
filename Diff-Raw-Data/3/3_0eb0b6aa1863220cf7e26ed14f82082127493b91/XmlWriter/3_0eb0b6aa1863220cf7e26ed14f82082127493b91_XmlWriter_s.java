 /**
  * Copyright 2011 The Open Source Research Group,
  *                University of Erlangen-Nürnberg
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package de.fau.cs.osr.ptk.common.xml;
 
 import static de.fau.cs.osr.ptk.common.xml.XmlConstants.*;
 
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.namespace.QName;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Result;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.sax.SAXTransformerFactory;
 import javax.xml.transform.sax.TransformerHandler;
 import javax.xml.transform.stream.StreamResult;
 
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.AttributesImpl;
 
 import de.fau.cs.osr.ptk.common.ast.AstNode;
 import de.fau.cs.osr.ptk.common.ast.AstNodePropertyIterator;
 import de.fau.cs.osr.ptk.common.ast.NodeList;
 import de.fau.cs.osr.ptk.common.ast.Text;
 import de.fau.cs.osr.utils.NameAbbrevService;
 import de.fau.cs.osr.utils.ReflectionUtils;
 import de.fau.cs.osr.utils.ReflectionUtils.ArrayInfo;
 
 public class XmlWriter
 {
 	private static final int MAX_ENTRIES = 128;
 	
 	// =========================================================================
 	
 	private final AttributesImpl atts = new AttributesImpl();
 	
 	private final Map<Class<?>, Marshaller> marshallerCache =
 			new LinkedHashMap<Class<?>, Marshaller>()
 			{
 				private static final long serialVersionUID = 1L;
 				
 				protected boolean removeEldestEntry(
 						Map.Entry<Class<?>, Marshaller> eldest)
 				{
 					return size() > MAX_ENTRIES;
 				}
 			};
 	
 	private boolean compact = false;
 	
 	private Writer writer;
 	
 	private NameAbbrevService abbrevService;
 	
 	private TransformerHandler th;
 	
 	// =========================================================================
 	
 	public static String write(AstNode node) throws SerializationException
 	{
 		StringWriter writer = new StringWriter();
 		new XmlWriter().serialize(node, writer);
 		return writer.toString();
 	}
 	
 	public static String write(AstNode node, NameAbbrevService abbrevService) throws SerializationException
 	{
 		StringWriter writer = new StringWriter();
 		new XmlWriter().serialize(node, writer, abbrevService);
 		return writer.toString();
 	}
 	
 	public static Writer write(AstNode node, Writer writer) throws SerializationException
 	{
 		new XmlWriter().serialize(node, writer);
 		return writer;
 	}
 	
 	public static Writer write(
 			AstNode node,
 			Writer writer,
 			NameAbbrevService abbrevService) throws SerializationException
 	{
 		new XmlWriter().serialize(node, writer, abbrevService);
 		return writer;
 	}
 	
 	// =========================================================================
 	
 	/**
 	 * If set to true the output is not indented or otherwise beautified.
 	 * 
 	 * @param compact
 	 *            Whether to compact the output.
 	 */
 	public void setCompact(boolean compact)
 	{
 		this.compact = compact;
 	}
 	
 	public void serialize(AstNode node, Writer writer) throws SerializationException
 	{
 		serialize(node, writer, new NameAbbrevService());
 	}
 	
 	public void serialize(
 			AstNode node,
 			Writer writer,
 			NameAbbrevService abbrevService) throws SerializationException
 	{
 		this.writer = writer;
 		
 		this.abbrevService = abbrevService;
 		
 		try
 		{
 			before();
 			dispatch(node);
 			after();
 		}
 		catch (TransformerConfigurationException e)
 		{
 			throw new SerializationException(e);
 		}
 		catch (SAXException e)
 		{
 			throw new SerializationException(e);
 		}
 		catch (JAXBException e)
 		{
 			throw new SerializationException(e);
 		}
 	}
 	
 	// =========================================================================
 	
 	private void before() throws TransformerConfigurationException, SAXException
 	{
 		SAXTransformerFactory tf =
 				(SAXTransformerFactory) SAXTransformerFactory.newInstance();
 		
 		if (!compact)
 			// Not sure if this always works
 			tf.setAttribute("indent-number", new Integer(2));
 		
 		th = tf.newTransformerHandler();
 		
 		Transformer t = th.getTransformer();
 		t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
 		t.setOutputProperty(OutputKeys.METHOD, "xml");
 		
 		if (!compact)
 		{
 			// Not sure if this always works
 			t.setOutputProperty(OutputKeys.INDENT, "yes");
 			t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
 		}
 		
 		Result streamResult = new StreamResult(writer);
 		th.setResult(streamResult);
 		
 		th.startDocument();
 		
 		addAttribute(new QName("xmlns:ptk"), PTK_NS);
 		startElement(AST_QNAME);
 		atts.clear();
 	}
 	
 	private void after() throws SAXException
 	{
 		endElement(AST_QNAME);
 		th.endDocument();
 	}
 	
 	// =========================================================================
 	
 	private void dispatch(AstNode n) throws SAXException, JAXBException
 	{
 		switch (n.getNodeType())
 		{
 			case AstNode.NT_TEXT:
 				visit((Text) n);
 				break;
 			case AstNode.NT_NODE_LIST:
 				visit((NodeList) n);
 				break;
 			default:
 				visit(n);
 		}
 	}
 	
 	private void iterate(AstNode n) throws SAXException, JAXBException
 	{
 		for (AstNode c : n)
 			dispatch(c);
 	}
 	
 	private void visit(AstNode n) throws SAXException, JAXBException
 	{
 		String typeName = abbrevService.abbrev(n.getClass());
 		
 		String tagName = typeNameToTagName(typeName);
 		
 		startElement(tagName);
 		{
 			for (Entry<String, Object> e : n.getAttributes().entrySet())
 				writeAttribute(e.getKey(), e.getValue());
 			
 			for (AstNodePropertyIterator i = n.propertyIterator(); i.next();)
 				writeProperty(i.getName(), i.getValue());
 			
 			for (int i = 0; i < n.getChildNames().length; ++i)
 			{
 				startElement(n.getChildNames()[i]);
 				{
 					dispatch(n.get(i));
 				}
 				endElement(n.getChildNames()[i]);
 			}
 		}
 		endElement(tagName);
 	}
 	
 	private void visit(Text n) throws SAXException
 	{
 		startElement(TEXT_QNAME);
 		{
 			String s = n.getContent();
 			th.characters(s.toCharArray(), 0, s.length());
 		}
 		endElement(TEXT_QNAME);
 	}
 	
 	private void visit(NodeList n) throws SAXException, JAXBException
 	{
 		startElement(LIST_QNAME);
 		{
 			iterate(n);
 		}
 		endElement(LIST_QNAME);
 	}
 	
 	// =========================================================================
 	
 	private void writeProperty(String name, Object value) throws SAXException, JAXBException
 	{
 		if (value == null)
 		{
 			addAttribute(ATTR_NULL_QNAME, "true");
 			startElement(name);
 			atts.clear();
 			endElement(name);
 		}
 		else
 		{
 			marshal(name, value);
 		}
 	}
 	
 	private void writeAttribute(String name, Object value) throws SAXException, JAXBException
 	{
 		Class<?> type = null;
 		Class<?> clazz = null;
 		
 		addAttribute(ATTR_NAME_QNAME, name);
 		
 		if (value != null)
 		{
 			clazz = value.getClass();
 			
 			ArrayInfo aInfo = ReflectionUtils.arrayDimension(clazz);
 			if (aInfo.dim > 0)
 			{
 				addAttribute(ATTR_ARRAY_QNAME, String.valueOf(aInfo.dim));
 				type = aInfo.elementClass;
 			}
 			else
 			{
 				type = clazz;
 			}
 		}
 		else
 		{
 			addAttribute(ATTR_NULL_QNAME, "true");
 		}
 		
 		startElement(ATTR_QNAME);
 		atts.clear();
 		{
 			if (value != null)
 			{
 				String typeName = abbrevService.abbrev(type);
 				
 				marshal(typeNameToTagName(typeName), value);
 			}
 		}
 		endElement(ATTR_QNAME);
 	}
 	
 	// =========================================================================
 	
 	public void marshal(String name, Object obj) throws SAXException, JAXBException
 	{
 		Class<?> clazz = obj.getClass();
 		if (ReflectionUtils.isExtPrimitive(clazz))
 		{
 			String value = String.valueOf(obj);
 			
 			startElement(name);
 			th.characters(value.toCharArray(), 0, value.length());
 			endElement(name);
 		}
 		else
 		{
 			Marshaller marshaller = marshallerCache.get(clazz);
 			if (marshaller == null)
 			{
 				marshaller = JAXBContext.newInstance(clazz).createMarshaller();
 				marshallerCache.put(clazz, marshaller);
 			}
 			
 			@SuppressWarnings({ "rawtypes", "unchecked" })
 			JAXBElement elem = new JAXBElement(new QName(name), clazz, obj);
 			
 			marshaller.marshal(elem, th);
 		}
 	}
 	
 	// =========================================================================
 	
 	private void startElement(String localName) throws SAXException
 	{
 		th.startElement("", "", localName, atts);
 	}
 	
 	private void startElement(QName name) throws SAXException
 	{
 		th.startElement("", "", qNameToStr(name), atts);
 	}
 	
 	private void addAttribute(QName name, String value)
 	{
 		atts.addAttribute("", "", qNameToStr(name), "CDATA", value);
 	}
 	
 	private void endElement(String localName) throws SAXException
 	{
 		th.endElement("", "", localName);
 	}
 	
 	private void endElement(QName name) throws SAXException
 	{
 		th.endElement("", "", qNameToStr(name));
 	}
 	
 	private String qNameToStr(QName name)
 	{
 		if (name.getNamespaceURI() == null || name.getNamespaceURI().isEmpty())
 			return name.getLocalPart();
 		
 		return "ptk:" + name.getLocalPart();
 	}
 }
