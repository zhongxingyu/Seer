 package com.technosophos.rhizome.document;
 
 import java.io.OutputStream;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import com.technosophos.rhizome.repository.DocumentRepository;
 import com.technosophos.rhizome.RhizomeException;
 
 /**
  * A proxy wrapper around a {@link RhizomeDocument}.
  * <p>Searches typically return only a subset of the fields contained in a document.
  * And often, this subset is all that is needed. This object proxies a RhizomeDocument
  * by first populating the document ID and metadata information (typically based on a 
  * search result), but also making it possible to transparently load the full document when
  * necessary.</p>
  * <p><b>On Setters</b>: Many of the setters for this object don't do anything. Since this is
  * not a full RhizomeDocument, only docID and metadata can be changed through this object.</p>
  * @author mbutcher
  *
  */
 public class ProxyRhizomeDocument extends RhizomeDocument {
 	
 	protected DocumentRepository repo = null;
 	protected RhizomeDocument realDoc = null;
 	
 	public ProxyRhizomeDocument(String docID, List<Metadatum> md, DocumentRepository r) {
 		super(docID, md);
 		this.repo = r;
 	}
 	
 	private RhizomeDocument getRealDoc() {
 		try {
 			if(this.realDoc == null) this.realDoc = this.repo.getDocument(this.getDocID());
 		} catch (RhizomeException e) {
 			e.printStackTrace(System.err);
 			return null;
 		}
 		return this.realDoc;
 	}
 	
 	public List<Metadatum> getProxiedMetadata() {
 		return super.getMetadata();
 	}
 	
 	public RhizomeDocument getRealDocument() {
 		return this.getRealDoc();
 	}
 
 	/**
 	 * This masks the parent's function, and does nothing.
 	 */
 	public void addExtension(Extension ext) {}
 
 	public RhizomeData getData() {
 		return this.getRealDoc() != null ? this.realDoc.getData() : null;
 	}
 
 	public Document getDOM() throws ParserConfigurationException {
 		return this.getRealDoc() != null ? this.realDoc.getDOM() : null;
 	}
 
 	public Document getDOM(Document doc, Element parent_ele) {
 		return this.getRealDoc() != null ? this.realDoc.getDOM(doc, parent_ele) : null;
 	}
 
 	public Document getDOM(Document doc) {
 		return this.getRealDoc() != null ? this.realDoc.getDOM(doc) : null;
 	}
 
 	public Extension getExtensionByName(String name) {
 		return this.getRealDoc() != null ? this.realDoc.getExtensionByName(name) : null;
 	}
 
 	public ArrayList<Extension> getExtensions() {
 		return this.getRealDoc() != null ? this.realDoc.getExtensions() : null;
 	}
 
 	public List<Metadatum> getMetadata() {
 		return this.getRealDoc() != null ? this.realDoc.getMetadata() : null;
 	}
 
 	/**
 	 * This will first try to get metadata from the proxy instance. If no such 
 	 * medatum is found, then this will load the full copy of the object and 
 	 * query that.
 	 */
 	public Metadatum getMetadatum(String name) {
 		Metadatum m = super.getMetadatum(name);
		return (m == null) ? this.realDoc.getMetadatum(name) : m;
 	}
 
 	public ArrayList<Relation> getRelations() {
 		return this.getRealDoc() != null ? this.realDoc.getRelations() : null;
 	}
 
 	public boolean hasExtension(String name) {
 		return this.getRealDoc() != null ? this.realDoc.hasExtension(name) : null;
 	}
 
 	public int metadataSize() {
 		return this.getRealDoc() != null ? this.realDoc.metadataSize() : 0;
 	}
 
 	/** Does nothing. */
 	public void setBody(RhizomeData rd) {
 	}
 	/** Does nothing. */
 	public void setBody(String mimeType, String txt) {
 	}
 	/** Does nothing. */
 	public void setBody(String txt) {
 	}
 	public String toString() {
 		return this.getRealDoc() != null ? this.realDoc.toString() : null;
 	}
 	public String toXML() throws ParserConfigurationException {
 		return this.getRealDoc() != null ? this.realDoc.toXML() : null;
 	}
 
 	public void toXML(OutputStream output) throws ParserConfigurationException {
 		if(this.getRealDoc() != null) this.realDoc.toXML(output);
 	}
 
 	public void toXML(Writer output) throws ParserConfigurationException {
 		if(this.getRealDoc() != null) this.realDoc.toXML(output);
 	}
 
 
 }
