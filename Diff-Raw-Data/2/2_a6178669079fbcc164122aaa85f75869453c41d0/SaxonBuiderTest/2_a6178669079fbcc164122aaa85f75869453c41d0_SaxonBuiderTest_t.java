 /*
  * Copyright (C) 2010 Romain Deltour
  *
  * This library is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 2.1 of the License, or (at your option)
  * any later version.
  * 
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this library; if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
  */
 package org.trancecode.xml.saxon;
 
 import net.sf.saxon.s9api.Axis;
 import net.sf.saxon.s9api.Processor;
 import net.sf.saxon.s9api.QName;
 import net.sf.saxon.s9api.XdmNode;
 import net.sf.saxon.s9api.XdmNodeKind;
 import net.sf.saxon.s9api.XdmSequenceIterator;
 import org.custommonkey.xmlunit.XMLAssert;
 import org.testng.Assert;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 import org.trancecode.AbstractTest;
 
 /**
  * @author Romain Deltour
  */
 @Test
 public class SaxonBuiderTest extends AbstractTest
 {
     private final Processor processor = new Processor(false);
     private SaxonBuilder builder;
 
     @BeforeMethod
     public void setup()
     {
         builder = new SaxonBuilder(processor.getUnderlyingConfiguration());
     }
 
     @Test
     public void getNode_nothingBuilt() throws Exception
     {
         Assert.assertNull(builder.getNode(), "unused builder should returned null node");
     }
 
     @Test
     public void document() throws Exception
     {
         builder.startDocument();
         builder.endDocument();
         final XdmNode result = builder.getNode();
         Assert.assertNotNull(result);
         Assert.assertEquals(result.getNodeKind(), XdmNodeKind.DOCUMENT);
         Assert.assertFalse(result.axisIterator(Axis.CHILD).hasNext());
     }
 
     @Test
     public void element()
     {
         final XdmNode expected = newDocument("<root/>");
         builder.startDocument();
         builder.startElement(new QName("root"));
         builder.endElement();
         builder.endDocument();
         assertXmlEquals(expected, builder.getNode(), processor);
     }
 
     @Test
     public void namepsace_default() throws Exception
     {
         final XdmNode expected = newDocument("<root xmlns='ns'/>");
         builder.startDocument();
         builder.startElement(new QName("root"));
         builder.namespace("", "ns");
         builder.endElement();
         builder.endDocument();
         assertXmlEquals(expected, builder.getNode(), processor);
     }
 
     @Test
     public void namepsace_nullPrefix() throws Exception
     {
         final XdmNode expected = newDocument("<root xmlns='ns'/>");
         builder.startDocument();
         builder.startElement(new QName("root"));
         builder.namespace(null, "ns");
         builder.endElement();
         builder.endDocument();
         assertXmlEquals(expected, builder.getNode(), processor);
     }
 
     @Test
     public void namepsace_multiple() throws Exception
     {
         final XdmNode expected = newDocument("<root xmlns='ns' xmlns:n='ns2'/>");
         builder.startDocument();
         builder.startElement(new QName("root"));
         builder.namespace("", "ns");
         builder.namespace("n", "ns2");
         builder.endElement();
         builder.endDocument();
         assertXmlEquals(expected, builder.getNode(), processor);
     }
 
     @Test
     public void attribute()
     {
         final XdmNode expected = newDocument("<root att='value'/>");
         builder.startDocument();
         builder.startElement(new QName("root"));
         builder.attribute(new QName("att"), "value");
         builder.endElement();
         builder.endDocument();
         assertXmlEquals(expected, builder.getNode(), processor);
     }
 
     @Test
     public void attribute_namespaced()
     {
         final XdmNode expected = newDocument("<root a:att='value' xmlns:a='ns'/>");
         builder.startDocument();
         builder.startElement(new QName("root"));
         builder.namespace("a", "ns");
         builder.attribute(new QName("ns", "a:att"), "value");
         builder.endElement();
         builder.endDocument();
         assertXmlEquals(expected, builder.getNode(), processor);
     }
 
     @Test
     public void content_text()
     {
         final XdmNode expected = newDocument("<root>text</root>");
         builder.startDocument();
         builder.startElement(new QName("root"));
         builder.startContent();
         builder.text("text");
         builder.endElement();
         builder.endDocument();
         assertXmlEquals(expected, builder.getNode(), processor);
     }
 
     @Test
     public void content_element()
     {
         final XdmNode expected = newDocument("<root><elem/></root>");
         builder.startDocument();
         builder.startElement(new QName("root"));
         builder.startContent();
         builder.startElement(new QName("elem"));
         builder.endElement();
         builder.endElement();
         builder.endDocument();
         assertXmlEquals(expected, builder.getNode(), processor);
     }
 
     @Test
     public void appendNode_asDocument()
     {
         final XdmNode expected = newDocument("<root><elem att='value'><subelem/></elem></root>");
         final XdmNode content = newDocument("<elem att='value'><subelem/></elem>");
         assert content.getNodeKind() == XdmNodeKind.DOCUMENT;
         builder.startDocument();
         builder.startElement(new QName("root"));
         builder.startContent();
         builder.nodes(content);
         builder.endElement();
         builder.endDocument();
         final XdmNode result = builder.getNode();
         assertXmlEquals(expected, result, processor);
         final XdmSequenceIterator iter = result.axisIterator(Axis.DESCENDANT);
         while (iter.hasNext())
         {
             final XdmNode node = (XdmNode) iter.next();
             Assert.assertEquals(node.getNodeKind(), XdmNodeKind.ELEMENT);
         }
     }
 
     @Test
     public void appendNode_asElement()
     {
         final XdmNode expected = newDocument("<root><elem att='value'><subelem/></elem></root>");
         final XdmNode content = (XdmNode) newDocument("<elem att='value'><subelem/></elem>").axisIterator(Axis.CHILD)
                 .next();
         assert content.getNodeKind() == XdmNodeKind.ELEMENT;
         builder.startDocument();
         builder.startElement(new QName("root"));
         builder.startContent();
         builder.nodes(content);
         builder.endElement();
         builder.endDocument();
         assertXmlEquals(expected, builder.getNode(), processor);
     }
 
     @Test
     public void appendNodes()
     {
         final XdmNode expected = newDocument("<root><elem1/><elem2/></root>");
         final XdmNode content1 = newDocument("<elem1/>");
         final XdmNode content2 = newDocument("<elem2/>");
         builder.startDocument();
         builder.startElement(new QName("root"));
         builder.startContent();
         builder.nodes(content1, content2);
         builder.endElement();
         builder.endDocument();
         assertXmlEquals(expected, builder.getNode(), processor);
     }
 
     @Test
     public void comment()
     {
        final XdmNode expected = newDocument("<root><!--comment--></root>");
         builder.startDocument();
         builder.startElement(new QName("root"));
         builder.comment("comment");
         builder.endElement();
         builder.endDocument();
         assertXmlEquals(expected, builder.getNode(), processor);
     }
 
     private XdmNode newDocument(final String documentString)
     {
         return Saxon.parse(documentString, processor);
     }
 
     private static void assertXmlEquals(final XdmNode expected, final XdmNode actual, final Processor processor)
     {
         assert expected != null;
         assert actual != null;
         final XdmNode docExpected = Saxon.asDocumentNode(expected, processor);
         final XdmNode docActual = Saxon.asDocumentNode(actual, processor);
         final String message = String.format("expected:\n%s\nactual:\n%s", docExpected, docActual);
         try
         {
             XMLAssert.assertXMLEqual(message, docExpected.toString(), docActual.toString());
         }
         catch (final Exception e)
         {
             throw new IllegalStateException(message, e);
         }
     }
 }
