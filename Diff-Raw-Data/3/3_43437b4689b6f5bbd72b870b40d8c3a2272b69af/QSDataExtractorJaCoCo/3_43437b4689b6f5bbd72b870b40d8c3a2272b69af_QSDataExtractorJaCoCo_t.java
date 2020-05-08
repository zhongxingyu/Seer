 /*
  * Copyright 2012 Roland Gisler, GISLER iNFORMATiK, Switzerland.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package ch.gitik.qsreporter;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 
 import ch.gitik.qsreporter.jacoco.JaCoCoModel;
 import ch.gitik.qsreporter.jacoco.JaCoCoSensor;
 
 /**
  * @author Roland Gisler
  */
 public class QSDataExtractorJaCoCo extends AbstractDataExtractor {
 
    /**
     * Extrahiert Coveragedaten aus JaCoCo XML-Report.
     * @param pFile
     *           XML-Report von JaCoCo.
     * @return Coveragedaten.
     */
    public final JaCoCoModel extract(final File pFile) {
       JaCoCoModel data = null;
       try {
          final Document doc = getDocument(pFile);
          data = this.getData(doc);
       } catch (SAXParseException err) {
          err.printStackTrace();
       } catch (SAXException e) {
          e.printStackTrace();
       } catch (XPathExpressionException e) {
          e.printStackTrace();
       } catch (ParserConfigurationException e) {
          e.printStackTrace();
       } catch (IOException e) {
          e.printStackTrace();
       }
       return data;
    }
 
    /**
     * Extrahiert Daten aus Document.
     * @param xmlDoc
     *           Document.
     * @return Coveragedaten.
     * @throws ParserConfigurationException
     *            Bla.
     * @throws SAXException
     *            Bla.
     * @throws IOException
     *            Bla.
     * @throws XPathExpressionException
     *            Bla.
     */
    public final JaCoCoModel getData(final Document xmlDoc) throws ParserConfigurationException, SAXException,
          IOException, XPathExpressionException {
 
       final XPathFactory factory = XPathFactory.newInstance();
       final XPath xpath = factory.newXPath();
       XPathExpression expression;
       expression = xpath.compile("//report/counter/@type");
       final Object resultType = expression.evaluate(xmlDoc, XPathConstants.NODESET);
       final NodeList nodesType = (NodeList) resultType;
 
       expression = xpath.compile("//report/counter/@covered");
       final Object resultCovered = expression.evaluate(xmlDoc, XPathConstants.NODESET);
       final NodeList nodesCovered = (NodeList) resultCovered;
 
       expression = xpath.compile("//report/counter/@missed");
       final Object resultMissed = expression.evaluate(xmlDoc, XPathConstants.NODESET);
       final NodeList nodesMissed = (NodeList) resultMissed;
 
       Node typeNode;
       JaCoCoSensor classData = null;
       JaCoCoSensor methodeData = null;
       JaCoCoSensor branchData = null;
       JaCoCoSensor lineData = null;
       JaCoCoSensor instructionData = null;
 
       for (int i = 0; i < nodesType.getLength(); i++) {
          typeNode = nodesType.item(i);
         final String type = typeNode.getNodeValue();
          if ("CLASS".equals(type)) {
             classData = new JaCoCoSensor(Integer.valueOf(nodesCovered.item(i).getNodeValue()),
                   Integer.valueOf(nodesMissed.item(i).getNodeValue()));
          } else if ("METHOD".equals(type)) {
             methodeData = new JaCoCoSensor(Integer.valueOf(nodesCovered.item(i).getNodeValue()),
                   Integer.valueOf(nodesMissed.item(i).getNodeValue()));
          } else if ("BRANCH".equals(type)) {
             branchData = new JaCoCoSensor(Integer.valueOf(nodesCovered.item(i).getNodeValue()),
                   Integer.valueOf(nodesMissed.item(i).getNodeValue()));
          } else if ("LINE".equals(type)) {
             lineData = new JaCoCoSensor(Integer.valueOf(nodesCovered.item(i).getNodeValue()),
                   Integer.valueOf(nodesMissed.item(i).getNodeValue()));
          } else if ("INSTRUCTION".equals(type)) {
             instructionData = new JaCoCoSensor(Integer.valueOf(nodesCovered.item(i).getNodeValue()),
                   Integer.valueOf(nodesMissed.item(i).getNodeValue()));
          }
       }
       return new JaCoCoModel(classData, methodeData, branchData, lineData, instructionData);
    }
 }
