 package tools;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import thewebsemantic.UserDefNamespace;
 import thewebsemantic.binding.Jenabean;
 
 import com.hp.hpl.jena.ontology.OntModelSpec;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 
 /**
  * This tool controls the transformation library. User can transform an
  * object-oriented data model into a semantic resource.
  * 
  * @author Jakub Krauz
  */
 public class JenaBeanExtensionTool implements JenaBeanExtension {
 	
 	/**
 	 * RDF/XML language. This is the default.<br>
 	 * This version produces the output efficiently, but it is not
 	 * readable. For readable output use <code>RDF_XML_ABBREV</code>.
 	 * @see http://www.w3.org/TR/rdf-syntax-grammar/
 	 */
 	public static final String RDF_XML = "RDF/XML";
 	
 	/**
 	 * RDF/XML language.<br>
 	 * This version produces a readable output, but not so
 	 * efficiently as <code>RDF_XML</code>.
 	 * @see http://www.w3.org/TR/rdf-syntax-grammar/
 	 */
 	public static final String RDF_XML_ABBREV = "RDF/XML-ABBREV";
 	
 	/** 
 	 * RDF Core's N-Triples language.
 	 * @see http://www.w3.org/TR/rdf-testcases/#ntriples
 	 */
 	public static final String N_TRIPLE = "N-TRIPLE";
 	
 	/**
 	 * Terse RDF Triple language.
 	 * @see http://www.w3.org/TeamSubmission/turtle/
 	 */
 	public static final String TURTLE = "TURTLE";
 	
 	/**
 	 * Tim Berners-Lee's N3 (Notation 3) language.
 	 * @see http://www.w3.org/2000/10/swap/Primer.html
 	 */
 	public static final String N3 = "N3";
 	
 	/**
 	 * Tim Berners-Lee's N3 (Notation 3) language - full version.
 	 * @see http://www.w3.org/2000/10/swap/Primer.html
 	 */
 	public static final String N3_PP = "N3-PP";
 	
 	/**
 	 * Tim Berners-Lee's N3 (Notation 3) language - plain version.<br>
 	 * This version of the N3 language does not nest bNode structures
 	 * but does write record-like groups of all properties for a subject.
 	 * @see http://www.w3.org/2000/10/swap/Primer.html
 	 */
 	public static final String N3_PLAIN = "N3-PLAIN";
 	
 	/**
 	 * Tim Berners-Lee's N3 (Notation 3) language - triple version.<br>
 	 * This version of the N3 language writes one statement per line,
 	 * like N-TRIPLES, but also does qname conversion of URIrefs.
 	 * @see http://www.w3.org/2000/10/swap/Primer.html
 	 */
 	public static final String N3_TRIPLE = "N3-TRIPLE";
 
 	
 	/** logger */
 	private Log log = LogFactory.getLog(getClass());
 
 	/** provides operations over semantic model */
 	private Jenabean jenaBean = Jenabean.instance();
 
 	/** XML document describing semantic model */
 	private ByteArrayOutputStream ontologyDocument;
	
	/** language of the ontology document */
	private String ontDocumentLang;
 
 
 	/**
 	 * Loads data from a list of objects and creates the semantic model.
 	 * 
 	 * @param dataList list of objects - object-orinted model
 	 */
 	public JenaBeanExtensionTool(List<Object> dataList) {
 		createModel(dataList);
 	}
 
 
 	/**
 	 * Loads data from a list of objects and creates the semantic model.
 	 * Sets the default namespace for the whole model.
 	 * 
 	 * @param dataList list of objects - object-oriented model
 	 * @param namespace namespace for the whole model
 	 */
 	public JenaBeanExtensionTool(List<Object> dataList, String namespace) {
 		UserDefNamespace.set(namespace);
 		createModel(dataList);
 	}
 
 
 	@Override
 	public InputStream getOntologyDocument() throws IOException {
		if (ontologyDocument == null || !ontDocumentLang.equals(RDF_XML)) {
 			createInputStream(null);
			ontDocumentLang = RDF_XML;
		}
 		ByteArrayInputStream out = new ByteArrayInputStream(ontologyDocument.toByteArray());
 		out.close();
 		return out;
 	}
 	
 	
 	@Override
 	public InputStream getOntologyDocument(String lang) throws IOException {
		if (ontologyDocument == null || !ontDocumentLang.equals(lang)) {
 			createInputStream(lang);
			ontDocumentLang = lang;
		}
 		ByteArrayInputStream out = new ByteArrayInputStream(ontologyDocument.toByteArray());
 		out.close();
 		return out;
 	}
 
 
 	/**
 	 * Creates the semantic model and fills it with the user data
 	 * 
 	 * @param dataList list of user objects
 	 */
 	private void createModel(List<Object> dataList) {
 		log.debug("Started creating ontology model.");
 
 		/* parameter OntModelSpec.OWL_MEM disables reasoner included in
 		 * ModelFactory.createOntologyModel(); as default,
 		 * which led to a very slow computation */
 		jenaBean.bind(ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM));
 		
 		for (int i = 0; i < dataList.size(); i++) {
 			jenaBean.writer().save(dataList.get(i));
 		}
 		log.debug("Ontology model was created.");
 	}
 	
 	
 	/**
 	 * Creates the ontology document in a specified language.
 	 * 
 	 * @param lang Required language for the ontology document.
 	 * 			   If null, the default language RDF/XML is set.
 	 * @throws IOException if I/O problems occurred
 	 */
 	private void createInputStream(String lang) throws IOException {
 		ontologyDocument = new ByteArrayOutputStream();
 		jenaBean.writeModel(ontologyDocument, lang);
 		ontologyDocument.flush();
 		ontologyDocument.close();
 	}
 
 }
