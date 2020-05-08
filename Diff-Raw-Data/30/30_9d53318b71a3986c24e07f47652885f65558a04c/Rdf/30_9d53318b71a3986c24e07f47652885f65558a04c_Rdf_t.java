 package eionet.meta.exports.rdf;
 
 import java.io.Writer;
 import java.sql.Connection;
 import java.text.MessageFormat;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Vector;
 
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamWriter;
 
 import org.apache.commons.lang.StringUtils;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import com.bea.xml.stream.XMLOutputFactoryBase;
 
 import eionet.meta.DDSearchEngine;
 import eionet.meta.DataElement;
 import eionet.meta.DsTable;
 import eionet.meta.dao.domain.FixedValue;
 import eionet.meta.service.IDataService;
 import eionet.meta.service.ITableService;
 import eionet.util.Props;
 import eionet.util.PropsIF;
 
 /**
  *
  * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
  *
  */
 public class Rdf {
 
     public static final String TABLE_TYPE = "table";
     public static final String CODE_LIST_TYPE = "code_list";
 
     /** */
     private static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
     private static final String RDFS_NS = "http://www.w3.org/2000/01/rdf-schema#";
     private static final String DC_NS = "http://purl.org/dc/elements/1.1/";
     private static final String OWL_NS = "http://www.w3.org/2002/07/owl#";
     private static final String DD_NS = "http://dd.eionet.europa.eu/schema.rdf#";
     private static final String FOAF_NS = "http://xmlns.com/foaf/0.1/";
     private static final String SKOS_NS = "http://www.w3.org/2004/02/skos/core#";
     private static final String XML_NS = "http://www.w3.org/XML/1998/namespace";
 
     /** */
     private Connection conn;
     private DDSearchEngine searchEngine;
     private String baseUri;
     private DsTable tbl;
     private Vector tables;
     private String type;
     private int id;
 
     private ITableService tableService;
     private IDataService dataService;
 
     /**
      *
      * @param tblID
      * @param conn
      * @throws Exception
      * @throws Exception
      */
     public Rdf(String id, String type, Connection conn) throws Exception {
 
         this.conn = conn;
         this.searchEngine = new DDSearchEngine(this.conn);
         this.type = type;
 
         if (StringUtils.isNotEmpty(id)) {
             this.id = Integer.parseInt(id);
         }
 
         ApplicationContext ctx = new ClassPathXmlApplicationContext("spring-context.xml");
 
         if (type.equals(TABLE_TYPE)) {
             tableService = ctx.getBean(ITableService.class);
             dataService = ctx.getBean(IDataService.class);
 
             if (id != null) {
 
                 tbl = searchEngine.getDatasetTable(id);
                 if (tbl == null) {
                     throw new Exception("Table not found, id=" + id);
                 }
                 this.baseUri = Props.getRequiredProperty(PropsIF.RDF_TABLES_BASE_URI);
                 this.baseUri = MessageFormat.format(this.baseUri, this.id);
             } else {
                 HashSet datasetStatuses = new HashSet();
                 datasetStatuses.add("Released");
                 datasetStatuses.add("Recorded");
                 this.baseUri = Props.getRequiredProperty(PropsIF.RDF_TABLES_BASE_URI);
                 tables = searchEngine.getDatasetTables(null, null, null, null, null, null, datasetStatuses, false);
                 if (tables == null || tables.isEmpty()) {
                     throw new Exception("No tables found!");
                 }
             }
         } else if (type.equals(CODE_LIST_TYPE)) {
             this.baseUri = Props.getRequiredProperty(PropsIF.RDF_DATAELEMENTS_BASE_URI);
             this.baseUri = MessageFormat.format(this.baseUri, this.id);
             dataService = ctx.getBean(IDataService.class);
         }
     }
 
     /**
      *
      * @param writer
      * @throws Exception
      */
     public void write(Writer writer) throws Exception {
 
         if (type.equals(TABLE_TYPE)) {
             if (tbl != null) {
                 writeSingleTable(writer);
             } else {
                 writeManifest(writer);
             }
         } else if (type.equals(CODE_LIST_TYPE)) {
             writeCodeList(writer);
         }
     }
 
     /**
      * Writes RDF output of CommonElement fixed values.
      *
     * @param writer - output
      * @throws Exception
      */
     private void writeCodeList(Writer writer) throws Exception {
         List<FixedValue> fixedValues = dataService.getFixedValues(id);
         eionet.meta.dao.domain.DataElement dataElement = dataService.getDataElement(id);
 
         XMLOutputFactory factory = XMLOutputFactoryBase.newInstance();
         XMLStreamWriter streamWriter = factory.createXMLStreamWriter(writer);
         streamWriter.writeStartDocument();
 
         streamWriter.setPrefix("rdf", RDF_NS);
         streamWriter.setPrefix("rdfs", RDFS_NS);
         streamWriter.setPrefix("skos", SKOS_NS);
 
         streamWriter.writeStartElement(RDF_NS, "RDF");
         streamWriter.writeNamespace("rdf", RDF_NS);
         streamWriter.writeNamespace("rdfs", RDFS_NS);
         streamWriter.writeNamespace("skos", SKOS_NS);
        streamWriter.writeNamespace("foaf", FOAF_NS);
        // Normally the URL in xml:base ends with a slash. Not here.
        // Therefore we have to include the id again in the Collection URI and Concepts.
         streamWriter.writeAttribute("xml", XML_NS, "base", StringUtils.substringBeforeLast(this.baseUri, "/rdf"));
 
         streamWriter.writeStartElement(SKOS_NS, "Collection");
         streamWriter.writeAttribute(RDF_NS, "about", Integer.toString(id));
 
         streamWriter.writeStartElement(RDFS_NS, "label");
         streamWriter.writeCharacters(dataElement.getShortName());
         streamWriter.writeEndElement();
 
        streamWriter.writeEmptyElement(RDFS_NS, "isDefinedBy");
        streamWriter.writeAttribute(RDF_NS, "resource", this.baseUri);

        streamWriter.writeEmptyElement(FOAF_NS, "isPrimaryTopicOf");
        streamWriter.writeAttribute(RDF_NS, "resource", Integer.toString(id));

         for (FixedValue fv : fixedValues) {
            streamWriter.writeEmptyElement(SKOS_NS, "member");
            streamWriter.writeAttribute(RDF_NS, "resource", Integer.toString(id) + "/" + fv.getValue());
         }
         streamWriter.writeEndElement(); // </skos:Collection>
 
         for (FixedValue fv : fixedValues) {
             streamWriter.writeStartElement(SKOS_NS, "Concept");
            streamWriter.writeAttribute(RDF_NS, "about", Integer.toString(id) + "/" + fv.getValue());
 
             streamWriter.writeStartElement(SKOS_NS, "prefLabel");
             streamWriter.writeCharacters(fv.getValue());
             streamWriter.writeEndElement();
             if (StringUtils.isNotEmpty(fv.getDefinition())) {
                 streamWriter.writeStartElement(SKOS_NS, "definition");
                 streamWriter.writeCharacters(fv.getDefinition());
                 streamWriter.writeEndElement();
             }
             if (StringUtils.isNotEmpty(fv.getShortDescription())) {
                 streamWriter.writeStartElement(SKOS_NS, "note");
                 streamWriter.writeCharacters(fv.getShortDescription());
                 streamWriter.writeEndElement();
             }
 
             streamWriter.writeEndElement();
         }
 
         streamWriter.writeEndElement(); // </rdf:RDF>
     }
 
     /**
      *
      * @param writer
      * @throws Exception
      */
     private void writeManifest(Writer writer) throws Exception {
 
         XMLOutputFactory factory = XMLOutputFactoryBase.newInstance();
         XMLStreamWriter streamWriter = factory.createXMLStreamWriter(writer);
         streamWriter.writeStartDocument();
 
         streamWriter.setPrefix("rdf", RDF_NS);
         streamWriter.setPrefix("dd", DD_NS);
 
         streamWriter.writeStartElement(RDF_NS, "RDF");
         streamWriter.writeNamespace("rdf", RDF_NS);
         streamWriter.writeNamespace("dd", DD_NS);
 
         for (int i = 0; tables != null && i < tables.size(); i++) {
 
             DsTable table = (DsTable) tables.get(i);
             String tableId = table.getID();
             String tableRdfUrl = MessageFormat.format(baseUri, Integer.parseInt(tableId));
 
             streamWriter.writeStartElement(DD_NS, "TableSchema");
             streamWriter.writeAttribute(RDF_NS, "about", tableRdfUrl);
             streamWriter.writeEndElement(); // </dd:TableSchema>
         }
 
         streamWriter.writeEndElement(); // </rdf:RDF>
     }
 
     /**
      *
      * @param writer
      * @throws Exception
      */
     private void writeSingleTable(Writer writer) throws Exception {
 
         String tableName = null;
         List<String> tableNames = tableService.getNameAttribute(Integer.parseInt(tbl.getID()));
         if (tableNames != null && tableNames.size() > 0) {
             tableName = StringUtils.join(tableNames, ", ");
         }
         if (StringUtils.isEmpty(tableName)) {
             tableName = tbl.getShortName();
         }
 
         XMLOutputFactory factory = XMLOutputFactoryBase.newInstance();
         XMLStreamWriter streamWriter = factory.createXMLStreamWriter(writer);
         streamWriter.writeStartDocument();
 
         streamWriter.setPrefix("rdf", RDF_NS);
         streamWriter.setPrefix("rdfs", RDFS_NS);
         streamWriter.setPrefix("owl", OWL_NS);
         streamWriter.setPrefix("foaf", FOAF_NS);
         streamWriter.setPrefix("dd", DD_NS);
 
         streamWriter.writeStartElement(RDF_NS, "RDF");
         streamWriter.writeNamespace("rdf", RDF_NS);
         streamWriter.writeNamespace("rdfs", RDFS_NS);
         streamWriter.writeNamespace("owl", OWL_NS);
         streamWriter.writeNamespace("foaf", FOAF_NS);
         streamWriter.writeNamespace("dd", DD_NS);
 
         streamWriter.writeStartElement(RDFS_NS, "Class");
         streamWriter.writeAttribute(RDF_NS, "about", this.baseUri + "/" + tbl.getIdentifier());

         streamWriter.writeStartElement(RDFS_NS, "label");
         streamWriter.writeCharacters(tableName);
         streamWriter.writeEndElement(); // </rdfs:label>

         streamWriter.writeEmptyElement(FOAF_NS, "isPrimaryTopicOf");
         streamWriter.writeAttribute(RDF_NS, "resource", StringUtils.substringBeforeLast(this.baseUri, "/rdf"));

         streamWriter.writeEmptyElement(RDFS_NS, "isDefinedBy");
         streamWriter.writeAttribute(RDF_NS, "resource", this.baseUri);

         streamWriter.writeEndElement(); // </rdfs:Class>
 
         Vector elms = searchEngine.getDataElements(null, null, null, null, tbl.getID());
         for (int i = 0; elms != null && i < elms.size(); i++) {
 
             DataElement elm = (DataElement) elms.get(i);
 
             if (elm.getType().equalsIgnoreCase("CH1")) {
                 writeFixedValueProperty(streamWriter, elm);
             } else {
                 writeRegularProperty(streamWriter, elm);
             }
 
         }
 
         streamWriter.writeEndElement(); // </rdf:RDF>
     }
 
     /**
      * Writes regular property RDF output.
      *
      * @param streamWriter
      * @param element
      * @throws XMLStreamException
      */
     private void writeRegularProperty(XMLStreamWriter streamWriter, DataElement element) throws XMLStreamException {
         String type = null;
         try {
             type = dataService.getDataElementDataType(Integer.parseInt(element.getID()));
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         streamWriter.writeStartElement(RDF_NS, "Property");
         streamWriter.writeAttribute(RDF_NS, "ID", element.getIdentifier());
 
         streamWriter.writeStartElement(RDFS_NS, "label");
         streamWriter.writeCharacters(element.getShortName());
         streamWriter.writeEndElement(); // </rdfs:label>
 
         streamWriter.writeStartElement(RDFS_NS, "domain");
         streamWriter.writeAttribute(RDF_NS, "resource", this.baseUri + "/" + tbl.getIdentifier());
         streamWriter.writeEndElement(); // </rdfs:domain>
 
         if (StringUtils.isNotEmpty(type)) {
             streamWriter.writeStartElement(RDFS_NS, "range");
             streamWriter.writeAttribute(RDF_NS, "resource", getXmlType(type));
             streamWriter.writeEndElement(); // </rdfs:domain>
         }
 
         streamWriter.writeEmptyElement(RDFS_NS, "isDefinedBy");
         streamWriter.writeAttribute(RDF_NS, "resource", this.baseUri);
 
         streamWriter.writeEndElement(); // </rdf:Property>
     }
 
     /**
      * Writes fixed value property RDF output.
      *
      * @param streamWriter
      * @param element
      * @throws XMLStreamException
      */
     private void writeFixedValueProperty(XMLStreamWriter streamWriter, DataElement element) throws XMLStreamException {
         streamWriter.writeStartElement(OWL_NS, "DatatypeProperty");
         streamWriter.writeAttribute(RDF_NS, "ID", element.getIdentifier());
 
         streamWriter.writeStartElement(RDFS_NS, "label");
         streamWriter.writeCharacters(element.getShortName());
         streamWriter.writeEndElement(); // </rdfs:label>
 
         streamWriter.writeStartElement(RDFS_NS, "domain");
         streamWriter.writeAttribute(RDF_NS, "resource", this.baseUri + "/" + tbl.getIdentifier());
         streamWriter.writeEndElement(); // </rdfs:domain>
 
         streamWriter.writeStartElement(DD_NS, "usesVocabulary");
         streamWriter.writeAttribute(RDF_NS, "resource", "/dataelements/" + element.getID());
         streamWriter.writeEndElement(); // </dd:usesVocabulary>
 
         streamWriter.writeEndElement(); // </owl:DatatypeProperty>
     }
 
     /**
      * Returns the xml type according the attribute's type in database.
      *
      * @param type
      * @return
      */
     private String getXmlType(String type) {
         String xmlTypePrefix = "http://www.w3.org/2001/XMLSchema#";
         String suffix = null;
 
         if (type.equalsIgnoreCase("string")) {
             suffix = "string";
         }
         if (type.equalsIgnoreCase("boolean")) {
             suffix = "boolean";
         }
         if (type.equalsIgnoreCase("integer")) {
             suffix = "integer";
         }
         if (type.equalsIgnoreCase("date")) {
             suffix = "date";
         }
         if (type.equalsIgnoreCase("decimal")) {
             suffix = "decimal";
         }
         if (type.equalsIgnoreCase("float")) {
             suffix = "float";
         }
         if (type.equalsIgnoreCase("double")) {
             suffix = "double";
         }
 
         if (suffix == null) {
             suffix = "string";
         }
         return xmlTypePrefix + suffix;
     }
 
     /**
      *
      * @return
      */
     public String getFileName() {
         return null; // TODO
     }
 }
