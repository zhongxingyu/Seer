 /*
  * Copyright (c) 2010-2012 The University of Manchester, UK.
  *
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * * Redistributions of source code must retain the above copyright notice,
  *   this list of conditions and the following disclaimer.
  *
  * * Redistributions in binary form must reproduce the above copyright notice,
  *   this list of conditions and the following disclaimer in the documentation
  *   and/or other materials provided with the distribution.
  *
  * * Neither the names of The University of Manchester nor the names of its
  *   contributors may be used to endorse or promote products derived from this
  *   software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 
 package uk.org.taverna.server.client;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.lang.math.LongRange;
 
 import uk.org.taverna.server.client.connection.Connection;
 import uk.org.taverna.server.client.connection.ConnectionFactory;
 import uk.org.taverna.server.client.connection.UserCredentials;
 import uk.org.taverna.server.client.connection.params.ConnectionParams;
 import uk.org.taverna.server.client.util.URIUtils;
 import uk.org.taverna.server.client.xml.Resources.Label;
 import uk.org.taverna.server.client.xml.XMLReader;
 import uk.org.taverna.server.client.xml.XMLWriter;
import uk.org.taverna.server.client.xml.ServerResources;
 
 /**
  * The Server class represents a connection to a Taverna Server instance
  * somewhere on the Internet. Only one instance of this class is created for
  * each Taverna Server instance.
  * 
  * To make a connection to a server call {@link Server#connect(URI)} with it
  * full URL as the parameter. If there already exists a Server instance that is
  * connected to this Taverna Server then it will be returned, otherwise a new
  * Server instance is created and returned.
  * 
  * @author Robert Haines
  */
 public final class Server {
 
 	/*
 	 * Where to find the REST endpoint in relation to the base URI of a Taverna
 	 * Server.
 	 * 
 	 * Add a slash to the end of this address to work around this bug:
 	 * http://dev.mygrid.org.uk/issues/browse/TAVSERV-113
 	 */
 	private final static String REST_ENDPOINT = "rest/";
 
 	private final Connection connection;
 
 	private final URI uri;
 	private final Map<String, Map<String, Run>> runs;
 
 	private final XMLReader reader;
 	private ServerResources resources;
 
 	/**
 	 * 
 	 * @param uri
 	 * @param params
 	 */
 	public Server(URI uri, ConnectionParams params) {
 		// strip out username and password if present in server URI
 		this.uri = URIUtils.stripUserInfo(uri);
 
 		connection = ConnectionFactory.getConnection(this.uri, params);
 
 		reader = new XMLReader(connection);
 		resources = null;
 
 		// initialise run list
 		runs = new HashMap<String, Map<String, Run>>();
 	}
 
 	/**
 	 * 
 	 * @param uri
 	 */
 	public Server(URI uri) {
 		this(uri, null);
 	}
 
 	/**
 	 * Get the version of the remote Taverna Server instance.
 	 * 
 	 * @return the Taverna Server version.
 	 */
 	public float getVersion() {
 		return getServerResources().getVersion();
 	}
 
 	/**
 	 * Get the Run instance hosted by this Server by its id.
 	 * 
 	 * @param id
 	 *            The id of the Run instance to get.
 	 * @return the Run instance.
 	 */
 	public Run getRun(String id, UserCredentials credentials) {
 		return getRunsFromServer(credentials).get(id);
 	}
 
 	/**
 	 * Get all the Run instances hosted on this server.
 	 * 
 	 * @return all the Run instances hosted on this server.
 	 */
 	public Collection<Run> getRuns(UserCredentials credentials) {
 		return getRunsFromServer(credentials).values();
 	}
 
 	/**
 	 * Delete a Run from the server.
 	 * 
 	 * @param id
 	 *            The id of the run to delete.
 	 */
 	public void deleteRun(String id, UserCredentials credentials) {
 		try {
 			connection.delete(URIUtils.appendToPath(getLink(Label.RUNS), id),
 					credentials);
 		} catch (AccessForbiddenException e) {
 			if (getRunsFromServer(credentials).containsKey(id)) {
 				throw e;
 			} else {
 				throw new RunNotFoundException(id);
 			}
 		}
 	}
 
 	/**
 	 * Delete a Run from the server.
 	 * 
 	 * @param run
 	 *            The Run instance to delete.
 	 */
 	public void deleteRun(Run run, UserCredentials credentials) {
 		deleteRun(run.getIdentifier(), credentials);
 	}
 
 	/**
 	 * Delete all runs on this server instance.
 	 */
 	public void deleteAllRuns(UserCredentials credentials) {
 		for (Run run : getRuns(credentials)) {
 			run.delete();
 		}
 	}
 
 	private Map<String, Run> getRunsFromServer(UserCredentials credentials) {
 		// Get this user's run list.
 		URI uri = getLink(Label.RUNS);
 		Map<String, URI> runList = reader.readRunList(uri, credentials);
 
 		// Get this user's run cache.
 		Map<String, Run> userRuns = runs.get(credentials.getUsername());
 		if (userRuns == null) {
 			userRuns = new HashMap<String, Run>();
 			runs.put(credentials.getUsername(), userRuns);
 		}
 
 		// Add new runs to the user's run cache.
 		for (String id : runList.keySet()) {
 			if (!userRuns.containsKey(id)) {
 				userRuns.put(id, new Run(runList.get(id), this, credentials));
 			}
 		}
 
 		// Any ids in the runs list that aren't in the map we've just got from
 		// the server are dead and can be removed.
 		if (userRuns.size() > runList.size()) {
 			for (String i : userRuns.keySet()) {
 				if (!runList.containsKey(i)) {
 					userRuns.remove(i);
 				}
 			}
 		}
 
 		assert (userRuns.size() == runList.size());
 
 		return userRuns;
 	}
 
 	private ServerResources getServerResources() {
 		if (resources == null) {
 			URI restURI = URIUtils.appendToPath(uri, REST_ENDPOINT);
 			resources = reader.readServerResources(restURI);
 		}
 
 		return resources;
 	}
 
 	/**
 	 * Connect to a Taverna Server.
 	 * 
 	 * @param uri
 	 *            The address of the server to connect to in the form
 	 *            http://server:port/location
 	 * @return a Server instance representing the connection to the specified
 	 *         Taverna Server.
 	 * @throws URISyntaxException
 	 *             if the provided URI is badly formed.
 	 */
 	@Deprecated
 	public static Server connect(String uri) throws URISyntaxException {
 		return new Server(new URI(uri));
 	}
 
 	/**
 	 * Connect to a Taverna Server.
 	 * 
 	 * @param uri
 	 *            The URI of the server to connect to.
 	 * @return a Server instance representing the connection to the specified
 	 *         Taverna Server.
 	 */
 	@Deprecated
 	public static Server connect(URI uri) {
 		return new Server(uri);
 	}
 
 	/**
 	 * Get the URI of this server instance.
 	 * 
 	 * @return the URI of this server instance.
 	 */
 	public URI getURI() {
 		return uri;
 	}
 
 	/**
 	 * Get the maximum number of run that this server can host concurrently.
 	 * 
 	 * @return the maximum number of run that this server can host concurrently.
 	 */
 	public int getRunLimit(UserCredentials credentials) {
		return Integer.parseInt(new String(connection.read(
				getLink(Label.RUNLIMIT),
				credentials)).trim());
 	}
 
 	/**
 	 * Initialize a Run on this server instance.
 	 * 
 	 * @param workflow
 	 *            the workflow to be run.
 	 * @return the id of the new run as returned by the server.
 	 */
 	URI initializeRun(byte[] workflow, UserCredentials credentials) {
 		URI location = connection.create(getLink(Label.RUNS), workflow,
 				"application/vnd.taverna.t2flow+xml", credentials);
 
 		return location;
 	}
 
 	/**
 	 * Create a new Run on this server with the supplied workflow.
 	 * 
 	 * @param workflow
 	 *            the workflow to be run.
 	 * @return a new Run instance.
 	 */
 	public Run createRun(byte[] workflow, UserCredentials credentials) {
 		return Run.create(this, workflow, credentials);
 	}
 
 	/**
 	 * Create a new Run on this server with the supplied workflow file.
 	 * 
 	 * @param workflow
 	 *            the workflow file to be run.
 	 * @return a new Run instance.
 	 */
 	public Run createRun(File workflow, UserCredentials credentials)
 			throws IOException {
 		return Run.create(this, workflow, credentials);
 	}
 
 	byte[] getData(URI uri, String type, LongRange range,
 			UserCredentials credentials) {
 		return connection.read(uri, type, range, credentials);
 	}
 
 	InputStream getDataStream(URI uri, String type, LongRange range,
 			UserCredentials credentials) {
 		return connection.readStream(uri, type, range, credentials);
 	}
 
 	/**
 	 * Read attribute data from a run.
 	 * 
 	 * @param id
 	 *            the id of the run.
 	 * @param uri
 	 *            the full URI of the attribute to get.
 	 * @param type
 	 *            the mime type of the attribute being retrieved.
 	 * @return the data associated with the attribute.
 	 */
 	public byte[] getRunData(String id, URI uri, String type,
 			UserCredentials credentials) {
 		return getRunData(id, uri, type, null, credentials);
 	}
 
 	/**
 	 * Read attribute data from a run.
 	 * 
 	 * @param id
 	 *            the id of the run.
 	 * @param uri
 	 *            the full URI of the attribute to get.
 	 * @param type
 	 *            the mime type of the attribute being retrieved.
 	 * @param range
 	 * @param credentials
 	 * @return the data associated with the attribute.
 	 */
 	public byte[] getRunData(String id, URI uri, String type, LongRange range,
 			UserCredentials credentials) {
 		try {
 			return connection.read(uri, type, range, credentials);
 		} catch (AttributeNotFoundException e) {
 			if (getRunsFromServer(credentials).containsKey(id)) {
 				throw e;
 			} else {
 				throw new RunNotFoundException(id);
 			}
 		}
 	}
 
 	/**
 	 * Read attribute data from a run.
 	 * 
 	 * @param run
 	 *            the Run instance.
 	 * @param uri
 	 *            the full URI of the attribute to get.
 	 * @param type
 	 *            the mime type of the attribute being retrieved.
 	 * @return the data associated with the attribute.
 	 */
 	public byte[] getRunData(Run run, URI uri, String type,
 			UserCredentials credentials) {
 		return getRunData(run.getIdentifier(), uri, type, credentials);
 	}
 
 	/**
 	 * 
 	 * @param run
 	 * @param uri
 	 * @param type
 	 * @param range
 	 * @param credentials
 	 * @return
 	 */
 	public byte[] getRunData(Run run, URI uri, String type, LongRange range,
 			UserCredentials credentials) {
 		return getRunData(run.getIdentifier(), uri, type, range, credentials);
 	}
 
 	/**
 	 * Read an attribute, of a specific type, of a run.
 	 * 
 	 * @param id
 	 *            the id of the run.
 	 * @param uri
 	 *            the full URI of the attribute to get.
 	 * @param type
 	 *            the mime type of the attribute being retrieved.
 	 * @return the attribute as a String.
 	 */
 	public String getRunAttribute(String id, URI uri, String type,
 			UserCredentials credentials) {
 		return new String(getRunData(id, uri, type, credentials));
 	}
 
 	/**
 	 * Read an attribute, of a specific type, of a run.
 	 * 
 	 * @param run
 	 *            the Run instance.
 	 * @param uri
 	 *            the full URI of the attribute to get.
 	 * @param type
 	 *            the mime type of the attribute being retrieved.
 	 * @return the attribute as a String.
 	 */
 	public String getRunAttribute(Run run, URI uri, String type,
 			UserCredentials credentials) {
 		return new String(getRunData(run.getIdentifier(), uri, type, credentials));
 	}
 
 	/**
 	 * Set a run's attribute to a new value.
 	 * 
 	 * @param id
 	 *            the id of the run.
 	 * @param uri
 	 *            the full URI of the attribute to set.
 	 * @param value
 	 *            the new value of the attribute.
 	 * @param type
 	 *            the mime type of the attribute.
 	 * @param credentials
 	 *            the user credentials to use for authorization.
 	 */
 	public void setRunAttribute(String id, URI uri, String value,
 			String type, UserCredentials credentials) {
 		try {
 			connection.update(uri, value.getBytes(), type, credentials);
 		} catch (AttributeNotFoundException e) {
 			if (getRunsFromServer(credentials).containsKey(id)) {
 				throw e;
 			} else {
 				throw new RunNotFoundException(id);
 			}
 		}
 	}
 
 	/**
 	 * Set a run's attribute to a new value.
 	 * 
 	 * @param run
 	 *            the Run instance.
 	 * @param uri
 	 *            the full URI of the attribute to set.
 	 * @param value
 	 *            the new value of the attribute.
 	 * @param type
 	 *            the mime type of the attribute.
 	 * @param credentials
 	 *            the user credentials to use for authorization.
 	 */
 	public void setRunAttribute(Run run, URI uri, String value, String type,
 			UserCredentials credentials) {
 		setRunAttribute(run.getIdentifier(), uri, value, type, credentials);
 	}
 
 	public void setRunAttribute(Run run, URI uri, byte[] value, String type,
 			UserCredentials credentials) {
 		String id = run.getIdentifier();
 		try {
 			connection.update(uri, value, type, credentials);
 		} catch (AttributeNotFoundException e) {
 			if (getRunsFromServer(credentials).containsKey(id)) {
 				throw e;
 			} else {
 				throw new RunNotFoundException(id);
 			}
 		}
 	}
 
 	void uploadData(URI location, byte[] data, String remoteName,
 			UserCredentials credentials) {
 		connection.create(location, XMLWriter.upload(remoteName, data),
 				"application/xml", credentials);
 	}
 
 	void makeRunDir(String id, URI root, String name,
 			UserCredentials credentials) throws IOException {
 		if (name.contains("/")) {
 			throw new IllegalArgumentException(
 					"creation of subdirectories directly (" + name + ")");
 		}
 
 		try {
 			connection.create(root, XMLWriter.mkdir(name),
 					"application/xml", credentials);
 		} catch (AttributeNotFoundException e) {
 			if (getRunsFromServer(credentials).containsKey(id)) {
 				throw e;
 			} else {
 				throw new RunNotFoundException(id);
 			}
 		}
 	}
 
 	void makeRunDir(Run run, URI root, String name,
 			UserCredentials credentials) throws IOException {
 		makeRunDir(run.getIdentifier(), root, name, credentials);
 	}
 
 	XMLReader getXMLReader() {
 		return reader;
 	}
 
 	private URI getLink(Label key) {
 		return getServerResources().get(key);
 	}
 }
