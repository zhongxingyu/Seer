 package org.fcrepo.client;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.fcrepo.jaxb.responses.access.ObjectDatastreams;
 import org.fcrepo.jaxb.responses.access.ObjectProfile;
 import org.fcrepo.jaxb.responses.management.DatastreamFixity;
 import org.fcrepo.jaxb.responses.management.DatastreamProfile;
 
 public class FedoraClient {
 
 	public static final String PROPERTY_FCREPO_URL = "org.fcrepo.fixity.fcrepo.url"; 
 
 	private static final String PATH_OBJECT_PROFILE = "/objects/";
 	private static final String PATH_DATASTREAMS = "/datastreams/";
 	private static final String PATH_DATASTREAM_CONTENT = "/content";
 	private static final String PATH_DATASTREAM_FIXITY = "/fixity";
 
 	private final HttpClient client = new DefaultHttpClient();
 	private URI fedoraUri;
 
 	private Unmarshaller unmarshaller;
 
 	public FedoraClient() {
 		super();
 	}
 
 	public void setFedoraUri(String fedoraUri) {
 		/* Check for a user set java property first to have it override the injected bean value */
 		String uriProp = System.getProperty(PROPERTY_FCREPO_URL);
 		if (uriProp == null){
 			this.fedoraUri = URI.create(fedoraUri);
 		}else{
 			this.fedoraUri = URI.create(uriProp);
 		}
 	}
 
 	private Unmarshaller getUnmarshaller() throws JAXBException {
 		if (unmarshaller == null) {
 			unmarshaller = JAXBContext.newInstance(ObjectProfile.class, ObjectDatastreams.class, DatastreamProfile.class)
 					.createUnmarshaller();
 		}
 		return unmarshaller;
 	}
 
 	public ObjectProfile getObjectProfile(final String id) throws IOException {
 		final HttpGet get = new HttpGet(fedoraUri.toASCIIString() + PATH_OBJECT_PROFILE + id);
 		final HttpResponse resp = client.execute(get);
 		if (resp.getStatusLine().getStatusCode() != 200) {
 			throw new IOException("Unable to fetch object profile from fedora: " + resp.getStatusLine().getReasonPhrase());
 		}
 		try {
 			ObjectProfile profile = (ObjectProfile) this.getUnmarshaller().unmarshal(resp.getEntity().getContent());
 			return profile;
 		} catch (JAXBException e) {
 			throw new IOException("Unabel to deserialize object profile", e);
 		} finally {
 			IOUtils.closeQuietly(resp.getEntity().getContent());
 		}
 	}
 
 	public ObjectDatastreams getObjectDatastreams(final String objectId) throws IOException {
 		final HttpGet get = new HttpGet(fedoraUri.toASCIIString() + PATH_OBJECT_PROFILE + objectId + PATH_DATASTREAMS);
 		final HttpResponse resp = client.execute(get);
 		if (resp.getStatusLine().getStatusCode() != 200) {
 			throw new IOException("Unable to fetch object profile from fedora: " + resp.getStatusLine().getReasonPhrase());
 		}
 		try {
 			ObjectDatastreams datastreams = (ObjectDatastreams) this.getUnmarshaller().unmarshal(resp.getEntity().getContent());
 			return datastreams;
 		} catch (JAXBException e) {
			throw new IOException("Unable to deserialize object profile", e);
 		} finally {
 			IOUtils.closeQuietly(resp.getEntity().getContent());
 		}
 	}
 
 	public DatastreamProfile getDatastreamProfile(final String objectId, final String dsId) throws IOException {
 		final HttpGet get = new HttpGet(fedoraUri.toASCIIString() + PATH_OBJECT_PROFILE + objectId + PATH_DATASTREAMS + dsId);
 		final HttpResponse resp = client.execute(get);
 		if (resp.getStatusLine().getStatusCode() != 200) {
 			throw new IOException("Unable to fetch object profile from fedora: " + resp.getStatusLine().getReasonPhrase());
 		}
 		try {
 			DatastreamProfile ds = (DatastreamProfile) this.getUnmarshaller().unmarshal(resp.getEntity().getContent());
 			return ds;
 		} catch (JAXBException e) {
			throw new IOException("Unable to deserialize object profile", e);
 		} finally {
 			IOUtils.closeQuietly(resp.getEntity().getContent());
 		}
 
 	}
 
 	public InputStream getDatastreamContent(final String objectId, final String dsId) throws IOException {
 		final HttpGet get = new HttpGet(fedoraUri.toASCIIString() + PATH_OBJECT_PROFILE + objectId + PATH_DATASTREAMS + dsId
 				+ PATH_DATASTREAM_CONTENT);
 		final HttpResponse resp = client.execute(get);
 		if (resp.getStatusLine().getStatusCode() != 200) {
 			resp.getEntity().getContent().close();
 			throw new IOException("Unable to fetch object profile from fedora: " + resp.getStatusLine().getReasonPhrase());
 		}
 		return resp.getEntity().getContent();
 	}
 	
 	public DatastreamFixity getDatastreamFixity(final String objectId, final String dsId) throws IOException {
 		final HttpGet get = new HttpGet(fedoraUri.toASCIIString() + PATH_OBJECT_PROFILE + objectId + PATH_DATASTREAMS + dsId
 				+ PATH_DATASTREAM_FIXITY);
 		final HttpResponse resp = client.execute(get);
 		if (resp.getStatusLine().getStatusCode() != 200) {
			throw new IOException("Unable to fetch datastream fixity from fedora: " + resp.getStatusLine().getReasonPhrase());
 		}
 		try {
 			DatastreamFixity df = (DatastreamFixity) this.getUnmarshaller().unmarshal(resp.getEntity().getContent());
 			return df;
 		} catch (JAXBException e) {
			throw new IOException("Unable to deserialize datastream fixity", e);
 		} finally {
 			IOUtils.closeQuietly(resp.getEntity().getContent());
 		}
 	}
 
 	public List<String> getPids() throws IOException {
 		final HttpGet get = new HttpGet(fedoraUri.toASCIIString() + PATH_OBJECT_PROFILE);
 		final HttpResponse resp = client.execute(get);
 		if (resp.getStatusLine().getStatusCode() != 200) {
 			resp.getEntity().getContent().close();
 			throw new IOException("Unable to fetch object list from fedora: " + resp.getStatusLine().getReasonPhrase());
 		}
 		String data = IOUtils.toString(resp.getEntity().getContent());
 
 		/* fedora4 returns a JSON array of strings in square brackets 
 		/* e.g. [pid1,pid2] */ 
 		/* so strip the square brackets */
 		/* and return the split */
 		if (data.length() == 2){
 			/* the empty "[]" set so we return null */
 			return null;
 		}
 		data = data.substring(1, data.length() - 1);
 		return Arrays.asList(data.split(","));
 	}
 }
