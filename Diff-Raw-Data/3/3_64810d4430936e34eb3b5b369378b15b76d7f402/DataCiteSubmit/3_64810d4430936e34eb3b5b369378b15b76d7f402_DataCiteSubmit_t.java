 package uk.ac.bristol.dundry.tasks;
 
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.StmtIterator;
 import com.hp.hpl.jena.sparql.vocabulary.FOAF;
 import com.hp.hpl.jena.util.FileManager;
 import com.hp.hpl.jena.vocabulary.DCTerms;
 import com.hp.hpl.jena.vocabulary.RDFS;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.net.MalformedURLException;
 import java.nio.charset.StandardCharsets;
 import java.nio.file.Path;
 import java.util.Properties;
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamWriter;
 import org.apache.http.HttpResponse;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.CredentialsProvider;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.AbstractHttpEntity;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.BasicCredentialsProvider;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.quartz.JobDataMap;
 import org.quartz.JobExecutionException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import uk.ac.bristol.dundry.dao.Repository;
 import uk.ac.bristol.dundry.vocabs.Bibo;
 
 /**
  *
  * @author Damian Steer <d.steer@bris.ac.uk>
  */
 public class DataCiteSubmit extends JobBase {
     
     final static Logger log = LoggerFactory.getLogger(DataCiteSubmit.class);
     final static XMLOutputFactory xof = XMLOutputFactory.newInstance();
     
     @Override
     public void execute(Repository repo, Resource item, Resource prov, String id, Path root, JobDataMap jobData) throws JobExecutionException {
         String username = jobData.getString("datacite.username");
         String password = jobData.getString("datacite.password");
         String endpoint = jobData.getString("datacite.endpoint");
         String doiprefix = jobData.getString("datacite.doiprefix");
         boolean testing = jobData.getBoolean("datacite.testing");
         
         submit(username, password, endpoint, doiprefix, testing,
                 id, repo.getPublishedURL(id), repo.getMetadata(id), prov);
     }
 
     // Broken out for testing purposes
     public void submit(String username, String password, String endpoint,
             String doiprefix, boolean testing, String id, String url,
             Resource item, Resource prov) throws JobExecutionException {
         String mdEndpoint = endpoint + "metadata" + "?testMode=" + testing;
         String doiEndpoint = endpoint + "doi" + "?testMode=" + testing;
         
         String doi = doiprefix + id;
         
         try {
             // see https://test.datacite.org/mds/static/apidoc
 
             DefaultHttpClient httpClient = new DefaultHttpClient();
             
             CredentialsProvider cp = new BasicCredentialsProvider();
             
             cp.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
             
             httpClient.setCredentialsProvider(cp);
             
             handleResponse(submitMetadata(httpClient, mdEndpoint, doi, item));
             handleResponse(submitDOI(httpClient, doiEndpoint, doi, url));
             prov.addProperty(Bibo.doi, doi);
             
         } catch (MalformedURLException ex) {
             throw new JobExecutionException("URL is malformed", ex);
         } catch (IOException ex) {
             throw new JobExecutionException("Issue communicating with datacite", ex);
         } catch (XMLStreamException ex) {
             throw new JobExecutionException("Problem writing metadata record xml for datacite", ex);
         }
     }
 
     // Handle the result of an http request
     private void handleResponse(HttpResponse response) throws JobExecutionException, IOException {
         try {
             int sc = response.getStatusLine().getStatusCode();
             if ((sc / 100) != 2) {
                 log.error("Request failed: {} ({})\n{}", new Object[]{
                             response.getStatusLine().getReasonPhrase(), sc,
                             EntityUtils.toString(response.getEntity())});
                 throw new JobExecutionException("Error with datacite: " + response.getStatusLine().getReasonPhrase());
             } else if (log.isDebugEnabled()) { // log the response if debugging
                 log.debug("Request succeeded ({}):\n{}\n", sc, EntityUtils.toString(response.getEntity()));
             }
         } finally {
             // Ensure connection is finished with
             EntityUtils.consume(response.getEntity());
         }
     }
 
     // Submit datacite metadata
     public HttpResponse submitMetadata(DefaultHttpClient client, String endpoint,
             String doi, Resource item) throws XMLStreamException, IOException {
         
         StringWriter out = new StringWriter();
         XMLStreamWriter writer = xof.createXMLStreamWriter(out);
         //new IndentingXMLStreamWriter(xof.createXMLStreamWriter(out));
 
         // Write datacite md record to string
         toDataCite(item, doi, writer);
         
         log.debug("Submitting to datacite <{}>: \n{}\n", endpoint, out.toString());
         
         AbstractHttpEntity content = new StringEntity(out.toString(), StandardCharsets.UTF_8);
         content.setContentType("application/xml");
         
         HttpPost post = new HttpPost(endpoint);
         post.setEntity(content);
         
         return client.execute(post);
     }
 
     // Point DOI at url
     public HttpResponse submitDOI(DefaultHttpClient client, String endpoint,
             String doi, String url) throws IOException {
        // Trailing slash is a work around for a proxy issue
        String message = String.format("doi=%s\nurl=%s\n", doi, url + "/");
         
         log.debug("Submitting to datacite <{}>: \n{}\n", endpoint, message);
         
         AbstractHttpEntity content = new StringEntity(message, StandardCharsets.UTF_8);
         content.setContentType("text/plain");
         
         HttpPost post = new HttpPost(endpoint);
         post.setEntity(content);
         return client.execute(post);
     }
 
     // Write item out in datacite format
     public void toDataCite(Resource item, String doi, XMLStreamWriter writer) throws XMLStreamException {
         writer.writeStartDocument();
 
         // Preamble
         writer.setDefaultNamespace("http://datacite.org/schema/kernel-2.2");
         writer.writeStartElement("resource");
         writer.writeDefaultNamespace("http://datacite.org/schema/kernel-2.2");
         writer.writeAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
         writer.writeAttribute("xsi:schemaLocation", "http://datacite.org/schema/kernel-2.2 http://schema.datacite.org/meta/kernel-2.2/metadata.xsd");
 
         // DOI
         writer.writeStartElement("identifier");
         writer.writeAttribute("identifierType", "DOI");
         writer.writeCharacters(doi);
         writer.writeEndElement();
 
         // Creators
         writer.writeStartElement("creators");
         writeNamed(item, DCTerms.creator, "creator", "creatorName", writer);
         writer.writeEndElement();
 
         // Titles
         writer.writeStartElement("titles");
         write(item, DCTerms.title, "title", writer);
         write(item, DCTerms.alternative, "title", writer, "titleType", "AlternativeTitle");
         writer.writeEndElement();
 
         // Publisher
         write(item, DCTerms.publisher, "publisher", writer);
 
         // Publication year
         write(item, DCTerms.issued, "publicationYear", writer);
         
         // Dates
         if (item.hasProperty(DCTerms.valid) || item.hasProperty(DCTerms.created)) {
             writer.writeStartElement("dates");
             write(item, DCTerms.valid, "date", writer, "dateType", "Valid");
             write(item, DCTerms.created, "date", writer, "dateType", "Created");
             writer.writeEndElement();
         }
         
         // Subjects
         writeContained("subjects", item, DCTerms.subject, "subject", writer);
         
         // Contributors
         writeNamedContained("contributors", item, DCTerms.contributor,
                 "contributor", "contributorName", writer, "contributorType", "Researcher");
         
         // Language
         write(item, DCTerms.language, "language", writer);
         
         // Identifiers
         writeContained("alternateIdentifiers", item, DCTerms.identifier, "alternateIdentifier", writer, "alternateIdentifierType", "");
 
         // Related publications
         if (item.hasProperty(DCTerms.references) || item.hasProperty(DCTerms.isReferencedBy)) {
             writer.writeStartElement("relatedIdentifiers");
             write(item, DCTerms.references, "relatedIdentifier", writer,
                     "relationType", "Cites", "relatedIdentifierType", "URN");
             write(item, DCTerms.isReferencedBy, "relatedIdentifier", writer,
                     "relationType", "IsCitedBy", "relatedIdentifierType", "URN");
             writer.writeEndElement();
         }
 
         // Rights
         write(item, DCTerms.rights, "rights", writer);
 
         // Description
         writeContained("descriptions", item, DCTerms.description, "description", writer, "descriptionType", "Abstract");
         
         // Close root and document
         writer.writeEndElement();
         writer.writeEndDocument();
         
         writer.flush();
     }
 
     // Wrapper for write which will include a containing element if there's anything
     // to write
     private void writeContained(String container,
             Resource item, Property property, String element,
             XMLStreamWriter writer, String... attVals) throws XMLStreamException {
         if (item.hasProperty(property)) {
             writer.writeStartElement(container);
             write(item, property, element, writer, attVals);
             writer.writeEndElement();
         }
     }
 
     // As above, but for writeNamed
     private void writeNamedContained(String container,
             Resource item, Property property, String containerElem, String nameElem,
             XMLStreamWriter writer, String... attVals) throws XMLStreamException {
         if (item.hasProperty(property)) {
             writer.writeStartElement(container);
             writeNamed(item, property, containerElem, nameElem, writer, attVals);
             writer.writeEndElement();
         }
     }
 
     /**
      * Write out rdf values, if present, to XML stream
      *
      * @param item Resource being written
      * @param property Property to write
      * @param element XML element to use
      * @param writer XML stream to write to
      * @param attVals Addition attribute values to include
      */
     private void write(Resource item, Property property, String element,
             XMLStreamWriter writer, String... attVals) throws XMLStreamException {
         StmtIterator si = item.listProperties(property);
         while (si.hasNext()) {
             writer.writeStartElement(element);
             // Write attributes out
             for (int i = 0; i < attVals.length; i += 2) {
                 writer.writeAttribute(attVals[i], attVals[i + 1]);
             }
 
             // Write value
             writer.writeCharacters(toString(si.next().getObject()));
             
             writer.writeEndElement();
         }
     }
 
     /**
      * Write out a composite entity -- name + identifier identifier element is
      * always 'nameIdentifier'
      *
      * For creators and contributors currently
      *
      * @param item
      * @param property
      * @param containerElem
      * @param nameElem
      * @param writer
      * @throws XMLStreamException
      */
     private void writeNamed(Resource item, Property property, String containerElem,
             String nameElem, XMLStreamWriter writer, String... attVals) throws XMLStreamException {
         StmtIterator si = item.listProperties(property);
         while (si.hasNext()) {
             writer.writeStartElement(containerElem);
             
             // Write attributes out
             for (int i = 0; i < attVals.length; i += 2) {
                 writer.writeAttribute(attVals[i], attVals[i + 1]);
             }
             
             Resource namedThing = si.next().getResource();
 
             // Write out possible names
             write(namedThing, FOAF.name, nameElem, writer);
             write(namedThing, DCTerms.title, nameElem, writer);
             write(namedThing, RDFS.label, nameElem, writer);
 
             // Provide identifier if available
             if (namedThing.isURIResource()) {
                 writer.writeStartElement("nameIdentifier");
                 writer.writeAttribute("nameIdentifierScheme", "URN");
                 writer.writeCharacters(namedThing.getURI());
                 writer.writeEndElement();
             }
             
             writer.writeEndElement();
         }
     }
     
     private String toString(RDFNode object) {
         if (object.isURIResource()) {
             return object.asResource().getURI();
         } else if (object.isLiteral()) {
             return object.asLiteral().getLexicalForm();
         } else {
             return "";
         }
     }
     
     public static void main(String[] args) throws JobExecutionException, IOException {
         
         Properties p = new Properties();
         p.load(DataCiteSubmit.class.getResourceAsStream("/sensitive.properties"));
         
         String user = p.getProperty("datacite.username");
         String pass = p.getProperty("datacite.password");
         
         if (null == user || null == pass) {
             System.err.printf("Missing username and / or password (%s,%s)\n",
                     user, pass);
             System.exit(1);
         }
         
         DataCiteSubmit instance = new DataCiteSubmit();
         
         Resource item = FileManager.get().loadModel("/Users/pldms/Development/Projects/2012/dundry/src/test/resources/datacite/data.rdf").
                 getResource("http://example.com/res");
         
         instance.submit(user, pass, "https://test.datacite.org/mds/", "10.5072/bris.", false,
                 "16o6ls8w6l0md1oufmorwt8tbj", "http://data-bris.acrc.bris.ac.uk/datasets/16o6ls8w6l0md1oufmorwt8tbj/", item, item);
         
     }
 }
