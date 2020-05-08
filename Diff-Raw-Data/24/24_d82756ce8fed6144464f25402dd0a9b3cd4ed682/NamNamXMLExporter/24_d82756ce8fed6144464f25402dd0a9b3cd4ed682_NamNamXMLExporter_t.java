 package org.bytewerk.namnam.export.xml;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.nio.charset.Charset;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.validation.Schema;
 import javax.xml.validation.SchemaFactory;
 import javax.xml.validation.Validator;
 
 import org.bytewerk.namnam.export.NamNamExportException;
 import org.bytewerk.namnam.export.NamNamExporter;
 import org.bytewerk.namnam.model.Mensaessen;
 import org.bytewerk.namnam.model.Tagesmenue;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Text;
 import org.xml.sax.SAXException;
 
 /**
  * module to export a mensa object to java serialised xml
  *
  * @author fake
  */
 public class NamNamXMLExporter extends NamNamExporter {
 
     private static Logger logger = Logger.getLogger(NamNamXMLExporter.class.getName());
     private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // ISO
 
     private String xsdFile = null;
 
     public NamNamXMLExporter(String path, String xsdFile) {
         super(path);
         this.xsdFile = xsdFile;
     }
 
     protected void doExport(OutputStream os) throws NamNamExportException {
         try {
 
             SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
             Schema schema;
             if(xsdFile == null) {
                 schema = factory.newSchema(new StreamSource(NamNamXMLExporter.class.getClassLoader().getResourceAsStream("NamNamXML.xsd")));
             } else {
                 schema = factory.newSchema(new StreamSource(xsdFile));
             }
 
             DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
             dbfac.setNamespaceAware(true);
             dbfac.setSchema(schema);
             dbfac.setValidating(true);
             DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
             Document doc = docBuilder.newDocument();
 
             String docNS = "http://namnam.bytewerk.org/files/NamNamXML";
 
             Element root = doc.createElementNS(docNS, "Mensa");
             root.setAttribute("name", mensa.getName());
             root.setAttribute("xmlns", docNS);
             doc.appendChild(root);
 
             root.appendChild(getDateElem(doc, "firstDate", mensa.getFirstDate()));
             root.appendChild(getDateElem(doc, "lastDate", mensa.getLastDate()));
 
             // Element tagesMs = doc.createElement("Tagesmenues");
 
             Iterator<Tagesmenue> dMit = mensa.getDayMenues().iterator();
             while (dMit.hasNext()) {
                 Tagesmenue t = dMit.next();
                 Element txml = doc.createElement("Tagesmenue");
                 txml.appendChild(getDateElem(doc, "tag", t.getTag()));
 
                 // Element essen = doc.createElement("Essen");
                 Iterator<Mensaessen> eIt = t.getMenues().iterator();
                 while (eIt.hasNext()) {
                     Mensaessen m = eIt.next();
                     Element mxml = doc.createElement("Mensaessen");
 
                     Element desc = doc.createElement("beschreibung");
                     Text descT = doc.createTextNode(m.getBeschreibung());
                     desc.appendChild(descT);
                     mxml.appendChild(desc);
 
                    mxml.appendChild(getPriceElem(doc, "studentenPreis", m.getStudentenPreis().getCents()));
                    mxml.appendChild(getPriceElem(doc, "normalerPreis", m.getPreis().getCents()));

                     if (m.getToken() != null) {
                        mxml.appendChild(getTokenElem(doc, "token", m.getToken().getDescription()));
                     }
 
                     txml.appendChild(mxml);
                 }
                 // txml.appendChild(essen);
 
                 root.appendChild(txml);
             }
             // root.appendChild(tagesMs);
 
             Validator validator = schema.newValidator();
             validator.validate(new DOMSource(doc));
 
             // Output the XML
             // set up a transformer
             TransformerFactory transfac = TransformerFactory.newInstance();
             Transformer trans = transfac.newTransformer();
             trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
             trans.setOutputProperty(OutputKeys.INDENT, "yes");
 
             // create string from xml tree - updated 2010-09-25 to use utf8,
             // thanks dschaudel!
             OutputStreamWriter sw = new OutputStreamWriter(os,
                     Charset.forName("UTF-8"));
             StreamResult result = new StreamResult(sw);
             DOMSource source = new DOMSource(doc);
             trans.transform(source, result);
             sw.close();
         } catch (ParserConfigurationException pcex) {
             logger.log(Level.SEVERE,
                     "Parser config Exception while exporting to xml", pcex);
             throw new NamNamExportException(
                     "Parser config Exception while exporting to xml", pcex);
         } catch (TransformerConfigurationException tcex) {
             logger.log(Level.SEVERE,
                     "Transformer config Exception while exporting to xml", tcex);
             throw new NamNamExportException(
                     "Transformer config Exception while exporting to xml", tcex);
         } catch (TransformerException tex) {
             logger.log(Level.SEVERE,
                     "Transfomer Exception while exporting to xml", tex);
             throw new NamNamExportException(
                     "Transfomer Exception while exporting to xml", tex);
         } catch (IOException ioex) {
             logger.log(Level.SEVERE, "IO Exception while exporting to xml",
                     ioex);
             throw new NamNamExportException(
                     "IO Exception while exporting to xml", ioex);
         } catch (SAXException saxex) {
             logger.log(Level.SEVERE, "SAX Exception while exporting to xml",
                     saxex);
             throw new NamNamExportException(
                     "SAX Exception while exporting to xml", saxex);
         }
 
     }
 
     private Element getDateElem(Document doc, String elemName, Date theDate) {
         Element child = doc.createElement(elemName);
         Text date = doc.createTextNode(sdf.format(theDate));
         child.appendChild(date);
         return child;
     }
 
     private Element getPriceElem(Document doc, String elemName, Integer thePrice) {
         Element priceElem = doc.createElement(elemName);
         Text priceText = doc.createTextNode(thePrice.toString());
         priceElem.appendChild(priceText);
         return priceElem;
     }
 
    private Element getTokenElem(Document doc, String elemName, String theToken) {
        Element tokenElem = doc.createElement(elemName);
        Text tokenText = doc.createTextNode(theToken);
        tokenElem.appendChild(tokenText);
        return tokenElem;
    }

     @Override
     public String getFileName() {
         return mensa.getName() + ".xml";
     }
 }
