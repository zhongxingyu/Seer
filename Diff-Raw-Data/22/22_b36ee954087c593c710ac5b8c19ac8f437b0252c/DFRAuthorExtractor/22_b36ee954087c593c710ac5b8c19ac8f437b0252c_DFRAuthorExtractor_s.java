 /**
 *
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2008, NCSA.  All rights reserved.
 *
 * Developed by:
 * The Automated Learning Group
 * University of Illinois at Urbana-Champaign
 * http://www.seasr.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject
 * to the following conditions:
 *
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimers.
 *
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimers in
 * the documentation and/or other materials provided with the distribution.
 *
 * Neither the names of The Automated Learning Group, University of
 * Illinois at Urbana-Champaign, nor the names of its contributors may
 * be used to endorse or promote products derived from this Software
 * without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *
 */
 
 package org.jstor.meandre.components.xml.extractors;
 
 import java.io.InputStreamReader;
 import java.nio.charset.Charset;
 import java.util.Iterator;
 
 import javax.xml.namespace.NamespaceContext;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.components.abstracts.AbstractExecutableComponent;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.seasr.meandre.components.tools.Names;
 import org.seasr.meandre.support.io.DOMUtils;
 import org.seasr.meandre.support.parsers.DataTypeParser;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 
 import de.schlichtherle.io.FileInputStream;
 
 /**
  * Extracts the set of authors from an XML result returned from a JSTOR DFR query
  *
  * @author Boris Capitanu
  */
 
 @Component(
         creator = "Boris Capitanu",
         description = "This component extracts the set of authors from an XML result returned from a JSTOR DFR query",
         name = "DFR Author Extractor",
         tags = "jstor, dfr, research, data for research, author",
         rights = Licenses.UofINCSA,
         mode = Mode.compute,
         firingPolicy = FiringPolicy.all,
         baseURL = "meandre://seasr.org/components/jstor/",
         dependency = {"protobuf-java-2.0.3.jar"}
 )
 public class DFRAuthorExtractor extends AbstractExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
     @ComponentInput(
             description = "The result from a JSTOR DFR query in XML format",
             name = Names.PORT_XML
     )
     protected static final String IN_DFR_XML = Names.PORT_XML;
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
     @ComponentOutput(
             description = "A list of vectors containing the names of the authors. There is one vector for each entry.",
             name = Names.PORT_AUTHOR_LIST
     )
     protected static final String OUT_AUTHOR_LIST = Names.PORT_AUTHOR_LIST;
 
     //--------------------------------------------------------------------------------------------
 
 
 
     //--------------------------------------------------------------------------------------------
 
     @Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
 
     }
 
     @Override
     public void executeCallBack(ComponentContext cc) throws Exception {
         Document doc = DataTypeParser.parseAsDomDocument(cc.getDataComponentFromInput(IN_DFR_XML));
 
         NodeList records = processDocument(doc);
 
         console.fine("Found " + records.getLength() + " records");
     }
 
     public static NodeList processDocument(Document doc) throws XPathExpressionException {
 
         XPath xpath = XPathFactory.newInstance().newXPath();
         xpath.setNamespaceContext(new NamespaceContext() {
 
             public Iterator getPrefixes(String namespaceURI) {
                 // TODO Auto-generated method stub
                 return null;
             }
 
             public String getPrefix(String namespaceURI) {
                 if (namespaceURI.equals("http://purl.org/dc/elements/1.1/"))
                     return "dc";
 
                 return null;
             }
 
             public String getNamespaceURI(String prefix) {
                 if (prefix.equals("dc"))
                     return "http://purl.org/dc/elements/1.1/";
 
                 return null;
             }
         });
 
        XPathExpression exprRecords = xpath.compile("/searchRetrieveResponse/records/record");
         NodeList records = (NodeList) exprRecords.evaluate(doc, XPathConstants.NODESET);
 
         return records;
     }
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
 
     }
 
     public static void main(String[] args) throws Exception{
 
         DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
         domFactory.setNamespaceAware(true);
         DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new InputStreamReader(new FileInputStream("/tmp/data.xml"), Charset.forName("UTF-8"))));
 
         System.out.println(DOMUtils.getString(doc, null) + "\n");
 
         NodeList records = processDocument(doc);
 
         System.out.println("Found " + records.getLength() + " records");
 
     }
 
 }
