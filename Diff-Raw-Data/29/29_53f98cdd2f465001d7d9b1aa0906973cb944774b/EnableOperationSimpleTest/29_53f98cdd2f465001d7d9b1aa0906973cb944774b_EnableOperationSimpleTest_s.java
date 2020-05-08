 /*
  * The MIT License
  * 
  * Copyright (c) 2012-2013 IKEDA Yasuyuki
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package jp.ikedam.jenkins.plugins.jobcopy_builder;
 
 import hudson.EnvVars;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.io.UnsupportedEncodingException;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
 import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
 
 import junit.framework.TestCase;
 
 /**
  * Tests for EnableOperation not corresponded to Jenkins
  *
  */
 public class EnableOperationSimpleTest extends TestCase
 {
     private Document getXmlDocumentFromString(String xmlString)
             throws ParserConfigurationException,UnsupportedEncodingException,SAXException,IOException
     {
         DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder builder = domFactory.newDocumentBuilder();
         InputStream is = new ByteArrayInputStream(xmlString.getBytes("UTF-8")); 
         
         return builder.parse(is);
     }
     
     private NodeList getNodeList(Document doc, String xpathExpression)
             throws XPathExpressionException
     {
         XPathFactory factory = XPathFactory.newInstance();
         XPath xpath = factory.newXPath();
         XPathExpression expr = xpath.compile(xpathExpression);
         
         return (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
     }
     
     public void testPerform() throws UnsupportedEncodingException, ParserConfigurationException, SAXException, IOException, XPathExpressionException
     {
         EnableOperation target = new EnableOperation();
         EnvVars env = new EnvVars();
         ByteArrayOutputStream stream = new ByteArrayOutputStream();
         PrintStream logger = new PrintStream(stream);
         
         // disabled job
         {
             String xml = "<?xml version=\"1.0\"?>"
                     + "<root1>"
                     + "<disabled>true</disabled>"
                     + "</root1>";
             String result = target.perform(xml, "UTF-8", env, logger);
             assertNotNull("disabled job", result);
             Document doc = getXmlDocumentFromString(result);
             NodeList disabledNode = getNodeList(doc, "/root1/disabled");
             assertEquals("disabled job", 1, disabledNode.getLength());
             assertEquals("disabled job", "false", disabledNode.item(0).getTextContent());
         }
         
         // enabled job
         {
             String xml = "<?xml version=\"1.0\"?>"
                     + "<root2>"
                     + "<disabled>false</disabled>"
                     + "</root2>";
             String result = target.perform(xml, "UTF-8", env, logger);
             assertNotNull("enabled job", result);
             Document doc = getXmlDocumentFromString(result);
             NodeList disabledNode = getNodeList(doc, "/root2/disabled");
             assertEquals("enabled job", 1, disabledNode.getLength());
             assertEquals("enabled job", "false", disabledNode.item(0).getTextContent());
         }
         
         // unknown state job
         {
             String xml = "<?xml version=\"1.0\"?>"
                     + "<root>"
                     + "<disabled>あああ</disabled>"
                     + "</root>";
             String result = target.perform(xml, "UTF-8", env, logger);
             assertNotNull("unknown state job", result);
             Document doc = getXmlDocumentFromString(result);
             NodeList disabledNode = getNodeList(doc, "/root/disabled");
             assertEquals("unknown state job", 1, disabledNode.getLength());
             assertEquals("unknown state job", "false", disabledNode.item(0).getTextContent());
         }
         
         // empty state job
         {
             String xml = "<?xml version=\"1.0\"?>"
                     + "<root>"
                     + "<disabled></disabled>"
                     + "</root>";
             String result = target.perform(xml, "UTF-8", env, logger);
             assertNotNull("empty state job", result);
             Document doc = getXmlDocumentFromString(result);
             NodeList disabledNode = getNodeList(doc, "/root/disabled");
             assertEquals("empty state job", 1, disabledNode.getLength());
             assertEquals("empty state job", "false", disabledNode.item(0).getTextContent());
         }
         // disabled node in sub node.
         {
             String xml = "<?xml version=\"1.0\"?>"
                     + "<root>"
                     + "<sub><disabled>true</disabled></sub>"
                     + "<disabled>true</disabled>"
                     + "</root>";
             String result = target.perform(xml, "UTF-8", env, logger);
             assertNotNull("disabled node in sub node", result);
             Document doc = getXmlDocumentFromString(result);
             NodeList disabledNode = getNodeList(doc, "/root/disabled");
             assertEquals("disabled node in sub node.", 1, disabledNode.getLength());
             assertEquals("disabled node in sub node.", "false", disabledNode.item(0).getTextContent());
             
             NodeList unaffectedNode = getNodeList(doc, "/root/sub/disabled");
             assertEquals("disabled node in sub node.", 1, unaffectedNode.getLength());
             assertEquals("disabled node in sub node.", "true", unaffectedNode.item(0).getTextContent());
         }
         
         // no disabled field.
         {
             String xml = "<?xml version=\"1.0\"?>"
                     + "<root>"
                     + "</root>";
             assertNull("no disabled field.", target.perform(xml, "UTF-8", env, logger));
         }
         
         
         // disabled field only in subnode.
         {
             String xml = "<?xml version=\"1.0\"?>"
                     + "<root>"
                     + "<sub><disabled>true</disabled></sub>"
                     + "</root>";
             assertNull("disabled field only in subnode", target.perform(xml, "UTF-8", env, logger));
         }
         // multiple disabled field.
         {
             String xml = "<?xml version=\"1.0\"?>"
                     + "<root>"
                     + "<disabled>true</disabled>"
                     + "<disabled>true</disabled>"
                     + "</root>";
             assertNull("multiple disabled field", target.perform(xml, "UTF-8", env, logger));
         }
     }
 }
