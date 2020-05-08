 package com.github.st3iny.jxml;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.DOMException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.xml.sax.SAXException;
 
 public class XDocument {
 
 	private XElement root;
 	private XDeclaration declaration;
 
 	public XDocument(String name) throws XException {
 		this(name, XDeclaration.Default);
 	}
 
 	public XDocument(String name, XDeclaration declaration) throws XException {
 		this(new XElement(name), declaration);
 	}
 
 	public XDocument(XElement root) {
 		this(root, XDeclaration.Default);
 	}
 
 	public XDocument(XElement root, XDeclaration declaration) {
 		this.root = root;
 		this.declaration = declaration;
 	}
 
 	public static XDocument parse(String path) throws ParserConfigurationException, SAXException, IOException, DOMException, XException {
 		return parse(new File(path));
 	}
 
	private static XDocument parse(File file) throws ParserConfigurationException, SAXException, IOException, DOMException, XException {
 		if (!file.exists())
 			throw new FileNotFoundException("The specified XML document wasn't found!");
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		factory.setIgnoringComments(true);
 		factory.setIgnoringElementContentWhitespace(true);
 		DocumentBuilder builder = factory.newDocumentBuilder();
 		Document document = builder.parse(file);
 		Node root = document.getFirstChild();
 		XElement xroot = XElement.createFromDomNode(root, null);
 		XDocument xdoc = new XDocument(xroot, new XDeclaration(document.getXmlVersion(), document.getXmlEncoding(), document.getXmlStandalone()));
 		return xdoc;
 	}
 
 	public XElement getRootElement() {
 		return root;
 	}
 
 	public void save(String path) throws IOException {
 		save(new File(path));
 	}
 
 	public void save(File file) throws IOException {
 		FileWriter writer = new FileWriter(file);
 		writer.write(toString());
 		writer.flush();
 		writer.close();
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder builder = new StringBuilder();
 		builder.append(declaration.toString());
 		builder.append(XSettings.NewLine);
 		root.save(builder, 0);
 		return builder.toString();
 	}
 
 }
