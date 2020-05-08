 /**
  * 
  */
 package pl.psnc.dl.wf4ever.sms;
 
 import java.io.InputStream;
 import java.net.URI;
 import java.nio.charset.Charset;
 import java.util.Set;
 
 import org.openrdf.rio.RDFFormat;
 
 import pl.psnc.dl.wf4ever.dlibra.ResourceInfo;
 
 import com.google.common.collect.Multimap;
 
 /**
  * @author piotrhol
  * 
  */
 public interface SemanticMetadataService
 {
 
 	public static final RDFFormat SPARQL_XML = new RDFFormat("XML", "application/sparql-results+xml",
 			Charset.forName("UTF-8"), "xml", false, false);
 
 	public static final RDFFormat SPARQL_JSON = new RDFFormat("JSON", "application/sparql-results+json",
 			Charset.forName("UTF-8"), "json", false, false);
 
 
 	/**
 	 * Create a new ro:ResearchObject and ro:Manifest.
 	 * 
 	 * @param researchObjectURI
 	 *            RO URI, absolute
 	 */
 	void createResearchObject(URI researchObjectURI);
 
 
 	/**
 	 * Update the manifest of a research object.
 	 * 
 	 * @param manifestURI
 	 *            manifest URI, absolute
 	 * @param inputStream
 	 *            the manifest
 	 * @param rdfFormat
 	 *            manifest RDF format
 	 */
 	void updateManifest(URI manifestURI, InputStream inputStream, RDFFormat rdfFormat);
 
 
 	/**
 	 * Removes a research object, its manifest, proxies, internal aggregated resources and
 	 * internal named graphs. A resource/named graph is considered internal if it contains
 	 * the research object URI.
 	 * 
 	 * @param researchObjectURI
 	 *            RO URI, absolute
 	 */
 	void removeResearchObject(URI researchObjectURI);
 
 
 	/**
 	 * Returns the manifest of an RO.
 	 * 
 	 * @param manifestURI
 	 *            manifest URI, absolute
 	 * @param rdfFormat
 	 *            returned manifest format
 	 * @return manifest with the research object URI as base URI
 	 */
 	InputStream getManifest(URI manifestURI, RDFFormat rdfFormat);
 
 
 	/**
 	 * Adds a resource to ro:ResearchObject.
 	 * 
 	 * @param researchObjectURI
 	 *            RO URI, absolute
 	 * @param resourceURI
 	 *            resource URI, absolute or relative to RO URI
 	 * @param resourceInfo
 	 *            resource metadata
 	 */
 	void addResource(URI researchObjectURI, URI resourceURI, ResourceInfo resourceInfo);
 
 
 	/**
 	 * Removes a resource from ro:ResearchObject.
 	 * 
 	 * @param researchObjectURI
 	 *            RO URI, absolute
 	 * @param resourceURI
 	 *            resource URI, absolute or relative to RO URI
 	 */
 	void removeResource(URI researchObjectURI, URI resourceURI);
 
 
 	/**
 	 * Returns resource metadata.
 	 * 
 	 * @param researchObjectURI
 	 *            RO URI, absolute
 	 * @param resourceURI
 	 *            resource URI, absolute or relative to RO URI
 	 * @param rdfFormat
 	 *            resource metadata format
	 * @return resource description with URIs relative to RO URI
 	 */
 	InputStream getResource(URI researchObjectURI, URI resourceURI, RDFFormat rdfFormat);
 
 
 	/**
 	 * Return true if the resource exists and belongs to class ro:Folder
 	 * 
 	 * @param resourceURI
 	 *            resource URI
 	 * @return true if the resource exists and belongs to class ro:Folder, false otherwise
 	 */
 	boolean isRoFolder(URI researchObjectURI, URI resourceURI);
 
 
 	/**
 	 * Add a named graph to the quadstore
 	 * 
 	 * @param graphURI
 	 *            named graph URI
 	 * @param inputStream
 	 *            named graph content
 	 * @param rdfFormat
 	 *            graph content format
 	 */
 	void addNamedGraph(URI graphURI, InputStream inputStream, RDFFormat rdfFormat);
 
 
 	/**
 	 * Check if a named graph exists
 	 * 
 	 * @param graphURI
 	 *            named graph URI
 	 * @return true if a named graph with this URI exists, false otherwie
 	 */
 	boolean containsNamedGraph(URI graphURI);
 
 
 	/**
 	 * Return true if a named graph with given URI can be part of RO metadata. Such named
 	 * graphs are manifests and annotation bodies. Note that the graph itself does not
 	 * necessarily exist.
 	 * 
 	 * @param graphURI
 	 *            graph URI
 	 * @return
 	 */
 	boolean isROMetadataNamedGraph(URI researchObjectURI, URI graphURI);
 
 
 	/**
 	 * Get a named graph. If the named graph references other named graphs and the RDF
 	 * format is TriG or TriX, referenced named graphs are returned as well.
 	 * 
 	 * @param graphURI
 	 *            graph URI
 	 * @param rdfFormat
 	 *            response format
 	 * @return
 	 */
 	InputStream getNamedGraph(URI graphURI, RDFFormat rdfFormat);
 
 
 	/**
 	 * Get a portable named graph. The URIs will be relativized against the RO URI. All
 	 * references to other named graphs within the RO will have a file extension appended.
 	 * 
 	 * @param graphURI
 	 * @param rdfFormat
 	 * @param researchObjectURI
 	 * @param fileExtension
 	 * @return
 	 */
 	InputStream getNamedGraphWithRelativeURIs(URI graphURI, URI researchObjectURI, RDFFormat rdfFormat);
 
 
 	/**
 	 * Delete a named graph from the quadstore.
 	 * 
 	 * @param roURI
 	 *            the RO URI, used for distinguishing internal resources from external
 	 * @param graphURI
 	 *            graph URI
 	 */
 	void removeNamedGraph(URI researchObjectURI, URI graphURI);
 
 
 	// TODO limit results depending on the user
 	/**
 	 * List ro:ResearchObject resources that start with the given URI.
 	 * 
 	 * @param partialURI
 	 *            URI with which the RO URI must start. If null, all ROs are returned.
 	 * 
 	 * @return set of RO URIs
 	 */
 	Set<URI> findResearchObjects(URI partialURI);
 
 
 	/**
 	 * Responses are a available in a range of different formats. The specific formats
 	 * available depend on the type of SPARQL query being executed. SPARQL defines four
 	 * different types of query: CONSTRUCT, DESCRIBE, SELECT and ASK.
 	 * 
 	 * CONSTRUCT and DESCRIBE queries both return RDF graphs and so the usual range of RDF
 	 * serializations are available, including RDF/XML, RDF/JSON, Turtle, etc.
 	 * 
 	 * SELECT queries return a tabular result set, while ASK queries return a boolean
 	 * value. Results from both of these query types can be returned in either SPARQL XML
 	 * Results Format or SPARQL JSON Results Format.
 	 * 
 	 * See also http://www.w3.org/TR/rdf-sparql-XMLres/
 	 * 
 	 * @param query
 	 * @param rdfFormat
 	 * @return
 	 */
 	QueryResult executeSparql(String query, RDFFormat rdfFormat);
 
 
 	/**
 	 * Returns an RDF graph describing the given user.
 	 * 
 	 * @param userURI
 	 *            User URI
 	 * @param rdfFormat
 	 *            Requested RDF format, RDF/XML is the default one
 	 * @return A FOAF RDF graph in selected format
 	 */
 	QueryResult getUser(URI userURI, RDFFormat rdfFormat);
 
 
 	/**
 	 * Removes all data about a given user.
 	 * 
 	 * @param userURI
 	 *            User URI
 	 */
 	void removeUser(URI userURI);
 
 
 	/**
 	 * Returns a flat list of all attributes (facts and annotations) having a given
 	 * resource as a subject. This searches all named graphs, in all ROs.
 	 * 
 	 * If the property is dcterms:creator and the object is a foaf:Person, instead of the
 	 * Person resource, its foaf:name is put.
 	 * 
 	 * @param subjectURI
 	 *            URI of the resource
 	 * @return map of property URI with either a resource URI or a literal value (i.e.
 	 *         String or Calendar)
 	 */
 	Multimap<URI, Object> getAllAttributes(URI subjectURI);
 
 
 	/**
 	 * Closes the SemanticMetadataService and frees up resources held. Any subsequent
 	 * calls to methods of the object have undefined results.
 	 */
 	void close();
 
 }
