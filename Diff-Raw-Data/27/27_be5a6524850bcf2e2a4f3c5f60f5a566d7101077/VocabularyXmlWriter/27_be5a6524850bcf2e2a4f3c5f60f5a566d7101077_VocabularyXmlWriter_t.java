 /*
  * The contents of this file are subject to the Mozilla Public
  * License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of
  * the License at http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS
  * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * rights and limitations under the License.
  *
  * The Original Code is Content Registry 3
  *
  * The Initial Owner of the Original Code is European Environment
  * Agency. Portions created by TripleDev or Zero Technologies are Copyright
  * (C) European Environment Agency.  All Rights Reserved.
  *
  * Contributor(s):
  *        Juhan Voolaid
  */
 
 package eionet.meta.exports.rdf;
 
 import java.io.OutputStream;
 import java.util.List;
 
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamWriter;
 
 import org.apache.commons.lang.StringUtils;
 
 import eionet.meta.dao.domain.VocabularyConcept;
 import eionet.meta.dao.domain.VocabularyFolder;
 import eionet.meta.service.ServiceException;
 
 /**
  * Vocabulary RDF-XML writer.
  *
  * @author Juhan Voolaid
  */
 public class VocabularyXmlWriter {
 
     private static final String ENCODING = "UTF-8";
     private static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
     private static final String RDFS_NS = "http://www.w3.org/2000/01/rdf-schema#";
     private static final String SKOS_NS = "http://www.w3.org/2004/02/skos/core#";
     private static final String XML_NS = "http://www.w3.org/XML/1998/namespace";
 
     private String contextRoot;
 
     /**
      * XMLWriter to write XML to.
      */
     private XMLStreamWriter writer = null;
 
     /** Objects to write to output. */
     private VocabularyFolder vocabularyFolder;
     private List<VocabularyConcept> vocabularyConcepts;
 
     /**
      * Class constructor.
      *
      * @param out
      * @param contextRoot
      * @param vocabularyService
      * @throws XMLStreamException
      */
     public VocabularyXmlWriter(OutputStream out, String contextRoot, VocabularyFolder vocabularyFolder,
             List<VocabularyConcept> vocabularyConcepts) throws XMLStreamException {
         writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out, ENCODING);
         this.contextRoot = contextRoot;
         this.vocabularyFolder = vocabularyFolder;
         this.vocabularyConcepts = vocabularyConcepts;
     }
 
     /**
      * Writes rdf output to stream.
      *
      * @throws XMLStreamException
      * @throws ServiceException
      */
     public void writeManifestXml() throws XMLStreamException, ServiceException {
 
         writer.writeStartDocument(ENCODING, "1.0");
 
         writer.setPrefix("rdf", RDF_NS);
         writer.setPrefix("rdfs", RDFS_NS);
         writer.setPrefix("skos", SKOS_NS);
 
         writer.writeStartElement("rdf", "RDF", RDF_NS);
         writer.writeNamespace("rdf", RDF_NS);
         writer.writeNamespace("rdfs", RDFS_NS);
        writer.writeNamespace("skos", SKOS_NS);
         writer.writeAttribute("xml", XML_NS, "base", contextRoot);
 
        writer.writeCharacters("\n");
         writer.writeStartElement(SKOS_NS, "ConceptScheme");
         writer.writeAttribute("rdf", RDF_NS, "about", "");
 
        writer.writeCharacters("\n");
         writer.writeStartElement(RDFS_NS, "label");
         writer.writeCharacters(vocabularyFolder.getLabel());
         writer.writeEndElement();
 
        writer.writeCharacters("\n");
        writer.writeEndElement(); // End ConceptScheme
 
         for (VocabularyConcept vc : vocabularyConcepts) {
            writer.writeCharacters("\n");
             writer.writeStartElement(SKOS_NS, "Concept");
             writer.writeAttribute("rdf", RDF_NS, "about", vc.getIdentifier());
 
             if (StringUtils.isNotEmpty(vc.getNotation())) {
                writer.writeCharacters("\n");
                 writer.writeStartElement(SKOS_NS, "notation");
                 writer.writeCharacters(vc.getNotation());
                 writer.writeEndElement();
             }
 
            writer.writeCharacters("\n");
             writer.writeStartElement(SKOS_NS, "prefLabel");
             writer.writeCharacters(vc.getLabel());
             writer.writeEndElement();
 
            writer.writeCharacters("\n");
            writer.writeEmptyElement(SKOS_NS, "inScheme");
             writer.writeAttribute("rdf", RDF_NS, "resource", "");
 
            writer.writeCharacters("\n");
             writer.writeEndElement();
         }
 
        writer.writeCharacters("\n");
        writer.writeEndElement(); // End rdf:RDF
        writer.writeCharacters("\n");
     }
 }
