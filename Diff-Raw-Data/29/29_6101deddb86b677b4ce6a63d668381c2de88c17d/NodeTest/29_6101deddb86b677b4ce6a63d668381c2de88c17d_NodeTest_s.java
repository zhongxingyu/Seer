 package com.duckasteroid.ratxml;
 
 import java.io.File;
 import java.io.InputStream;
 import java.util.Iterator;
 
 import junit.framework.TestCase;
 
 import org.xml.sax.InputSource;
 
 public class NodeTest extends TestCase {
 	private Document ratXml;
 	Node subject;
 	
 	@Override
 	protected void setUp() throws Exception {
 		// convert countries XML to RAT-XML
 		InputStream stream = getClass().getClassLoader().getResourceAsStream("countries.xml");
 		File cdbFile = new File("countries.cdb");
 		Writer writer = new Writer(cdbFile, true);
 		writer.write(new InputSource(stream));
 		
 		// read the rat-xml 
 		ratXml = new Document(cdbFile);
 	}
 	
 	@Override
 	protected void tearDown() throws Exception {
 		ratXml.close();
 		ratXml = null;
 	}
 	
 	public void testGetRoot() {
 		subject = ratXml.getRoot();
 		assertNotNull(subject);
 		assertEquals("world", subject.getName());
 	}
 	
 	public void testGetChildren() {
 		subject = ratXml.getRoot();
 		Iterator<Node> children = subject.getOrderedChildElements().iterator();
 		assertNotNull(children);
 		assertTrue(children.hasNext());
 		subject = children.next();
 		assertNotNull(subject);
 		assertEquals("continent", subject.getName());
 		assertEquals("1", subject.getAttributeValue("id"));
 		assertTrue(children.hasNext());
 		subject = children.next();
 		assertNotNull(subject);
 		assertEquals("continent", subject.getName());
 		assertEquals("3", subject.getAttributeValue("id"));
 		assertTrue(children.hasNext());
 		subject = children.next();
 		assertNotNull(subject);
 		assertEquals("continent", subject.getName());
 		assertEquals("5", subject.getAttributeValue("id"));
 		assertFalse(children.hasNext());
 	}
 }
