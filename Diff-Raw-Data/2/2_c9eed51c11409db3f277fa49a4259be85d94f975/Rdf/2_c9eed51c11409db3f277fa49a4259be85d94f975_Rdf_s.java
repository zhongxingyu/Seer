 package eionet.meta.exports.rdf;
 
 import java.io.Writer;
 import java.sql.Connection;
 import java.text.MessageFormat;
 import java.util.HashSet;
 import java.util.Vector;
 
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamWriter;
 
 import com.bea.xml.stream.XMLOutputFactoryBase;
 
 import eionet.meta.DDSearchEngine;
 import eionet.meta.DataElement;
 import eionet.meta.DsTable;
 import eionet.util.Props;
 import eionet.util.PropsIF;
 
 /**
  * 
  * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
  *
  */
 public class Rdf {
 	
 	/** */
 	private static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
 	private static final String RDFS_NS = "http://www.w3.org/2000/01/rdf-schema#";
 	private static final String DC_NS = "http://purl.org/dc/elements/1.1/";
 	private static final String OWL_NS = "http://www.w3.org/2002/07/owl#";
 	private static final String DD_NS = "http://dd.eionet.europa.eu/schema.rdf#";
 
 	/** */
 	private Connection conn;	
 	private DDSearchEngine searchEngine;
 	private String baseUri;
 	private DsTable tbl;
 	private Vector tables;
 
 	/**
 	 * 
 	 * @param tblID
 	 * @param conn
 	 * @throws Exception 
 	 * @throws Exception
 	 */
 	public Rdf(String tblID, Connection conn) throws Exception{
 		
 		this.conn = conn;
 		this.searchEngine = new DDSearchEngine(this.conn);
 		this.baseUri = Props.getRequiredProperty(PropsIF.RDF_BASE_URI);
 		
 		if (tblID!=null){
 			
 			tbl = searchEngine.getDatasetTable(tblID);
 			if (tbl==null){
 				throw new Exception("Table not found, id=" + tblID);
 			}
 			
 			this.baseUri = MessageFormat.format(this.baseUri, tblID);
 		}
 		else{
 			HashSet datasetStatuses = new HashSet();
 			datasetStatuses.add("Released");
 			datasetStatuses.add("Recorded");
 			tables = searchEngine.getDatasetTables(null, null, null, null, null, null, datasetStatuses, false);
 			if (tables==null || tables.isEmpty()){
 				throw new Exception("No tables found!");
 			}
 		}
 	}
 	
 	/**
 	 * 
 	 * @param conn
 	 * @throws Exception
 	 */
 	public Rdf(Connection conn) throws Exception{
 		
 		this(null, conn);
 	}
 	
 	/**
 	 * 
 	 * @param writer
 	 * @throws Exception
 	 */
 	public void write(Writer writer) throws Exception{
 		
 		if (tbl!=null){
 			writeSingleTable(writer);
 		}
 		else{
 			writeManifest(writer);
 		}
 	}
 	
 	/**
 	 * 
 	 * @param writer
 	 * @throws Exception
 	 */
 	private void writeManifest(Writer writer) throws Exception{
 		
 		XMLOutputFactory factory = XMLOutputFactoryBase.newInstance();
 		XMLStreamWriter streamWriter = factory.createXMLStreamWriter(writer);
 		streamWriter.writeStartDocument();
 		
 		streamWriter.setPrefix("rdf", RDF_NS);
 		streamWriter.setPrefix("dd", DD_NS);
 
 		streamWriter.writeStartElement(RDF_NS, "RDF");
 		streamWriter.writeNamespace("rdf", RDF_NS);
 		streamWriter.writeNamespace("dd", DD_NS);
 
 		for (int i=0; tables!=null && i<tables.size(); i++){
 			
 			DsTable table = (DsTable)tables.get(i);
 			String tableId = table.getID();
 			String tableRdfUrl = MessageFormat.format(baseUri, tableId);
 			
 			streamWriter.writeStartElement(DD_NS, "TableSchema");
			streamWriter.writeAttribute(RDF_NS, "resource", tableRdfUrl);		
 			streamWriter.writeEndElement(); // </dd:TableSchema>
 		}
 		
 		streamWriter.writeEndElement(); // </rdf:RDF>
 	}
 
 	/**
 	 * 
 	 * @param writer
 	 * @throws Exception
 	 */
 	private void writeSingleTable(Writer writer) throws Exception{
 		
 		XMLOutputFactory factory = XMLOutputFactoryBase.newInstance();
 		XMLStreamWriter streamWriter = factory.createXMLStreamWriter(writer);
 		streamWriter.writeStartDocument();
 		
 		streamWriter.setPrefix("rdf", RDF_NS);
 		streamWriter.setPrefix("rdfs", RDFS_NS);
 		streamWriter.setPrefix("owl", OWL_NS);
 
 		streamWriter.writeStartElement(RDF_NS, "RDF");
 		streamWriter.writeNamespace("rdf", RDF_NS);
 		streamWriter.writeNamespace("rdfs", RDFS_NS);
 		streamWriter.writeNamespace("owl", OWL_NS);
 
 		streamWriter.writeStartElement(RDFS_NS, "Class");
 		streamWriter.writeAttribute(RDF_NS, "about", this.baseUri + tbl.getIdentifier());		
 		streamWriter.writeStartElement(RDFS_NS, "label");
 		streamWriter.writeCharacters(tbl.getShortName());
 		streamWriter.writeEndElement(); // </rdfs:label>
 		streamWriter.writeEndElement(); // </rdfs:Class>
 		
 		Vector elms = searchEngine.getDataElements(null, null, null, null, tbl.getID());
 		for (int i=0; elms!=null && i<elms.size(); i++){
 			
 			DataElement elm = (DataElement)elms.get(i);
 			
 			streamWriter.writeStartElement(RDF_NS, "Property");
 			streamWriter.writeAttribute(RDF_NS, "ID", elm.getIdentifier());
 			
 			streamWriter.writeStartElement(RDFS_NS, "label");
 			streamWriter.writeCharacters(elm.getShortName());
 			streamWriter.writeEndElement(); // </rdfs:label>
 			
 			streamWriter.writeStartElement(RDFS_NS, "domain");
 			streamWriter.writeAttribute(RDF_NS, "resource", this.baseUri + tbl.getIdentifier());
 			streamWriter.writeEndElement(); // </rdfs:domain>
 
 			streamWriter.writeEndElement(); // </rdf:Property>
 		}
 		
 		streamWriter.writeEndElement(); // </rdf:RDF>
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public String getFileName(){
 		return null; // TODO
 	}
 }
