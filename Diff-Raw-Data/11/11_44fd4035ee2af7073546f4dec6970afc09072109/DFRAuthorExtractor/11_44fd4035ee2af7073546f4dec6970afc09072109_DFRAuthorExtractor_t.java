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
 
 package org.seasr.meandre.components.jstor.xml.extractors;
 
 import java.util.List;
 import java.util.Vector;
 
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathFactory;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.seasr.datatypes.core.DataTypeParser;
 import org.seasr.datatypes.core.Names;
 import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
 import org.seasr.meandre.support.generic.jstor.DFRNamespaceContext;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 
 /**
  * Extracts the set of authors from an XML result returned from a JSTOR DFR query
  *
  * @author Boris Capitanu
  */
 
 @Component(
         creator = "Boris Capitanu",
         description = "This component extracts the set of authors from an XML result returned from a JSTOR DFR query",
         name = "DFR Author Extractor",
         tags = "jstor, dfr, data for research, author",
         rights = Licenses.UofINCSA,
         mode = Mode.compute,
         firingPolicy = FiringPolicy.all,
         baseURL = "meandre://seasr.org/components/foundry/",
         dependency = {"protobuf-java-2.2.0.jar"}
 )
 public class DFRAuthorExtractor extends AbstractExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
     @ComponentInput(
             name = Names.PORT_XML,
             description = "The result from a JSTOR DFR query in XML format" +
                 "<br>TYPE: org.w3c.dom.Document" +
                 "<br>TYPE: java.lang.String" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String IN_DFR_XML = Names.PORT_XML;
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
     @ComponentOutput(
             name = Names.PORT_AUTHOR_LIST,
             description = "A list of vectors containing the names of the authors. There is one vector for each entry." +
                "<br>TYPE: java.util.List&lt;java.util.Vector&lt;java.lang.String&gt;&gt;"
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
 
         XPath xpath = XPathFactory.newInstance().newXPath();
         xpath.setNamespaceContext(new DFRNamespaceContext());
 
         XPathExpression exprRecords = xpath.compile("//ns1:record");
         XPathExpression exprAuthors = xpath.compile("descendant::dc:creator/text()");
 
         NodeList nodesRecords = (NodeList) exprRecords.evaluate(doc, XPathConstants.NODESET);
         console.fine("Found " + nodesRecords.getLength() + " records");
 
         List<Vector<String>> authorGroups = new Vector<Vector<String>>();
 
         for (int i = 0, lenRec = nodesRecords.getLength(); i < lenRec; i++) {
             NodeList nodesAuthors = (NodeList) exprAuthors.evaluate(nodesRecords.item(i), XPathConstants.NODESET);
             int nAuthors = nodesAuthors.getLength();
 
             console.fine("Extracted " + nAuthors + " authors");
 
             Vector<String> authors = new Vector<String>(nAuthors);
 
             for (int j = 0; j < nAuthors; j++) {
                 String authorName = nodesAuthors.item(j).getNodeValue();
                 console.finest("author: " + authorName);
                 authors.add(authorName);
             }
 
             if (authors.size() > 0)
                 authorGroups.add(authors);
         }
 
         cc.pushDataComponentToOutput(OUT_AUTHOR_LIST, authorGroups);
     }
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
 
     }
 }
