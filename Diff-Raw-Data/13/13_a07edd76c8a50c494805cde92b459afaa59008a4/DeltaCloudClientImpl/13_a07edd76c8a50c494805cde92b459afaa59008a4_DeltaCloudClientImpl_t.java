 /*******************************************************************************
  * Copyright (c) 2010 Red Hat Inc..
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat Incorporated - initial API and implementation
  *******************************************************************************/
 package org.jboss.tools.deltacloud.core.client;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.StringReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.jboss.tools.deltacloud.core.client.request.CreateInstanceRequest;
 import org.jboss.tools.deltacloud.core.client.request.CreateKeyRequest;
 import org.jboss.tools.deltacloud.core.client.request.DeleteKeyRequest;
 import org.jboss.tools.deltacloud.core.client.request.DeltaCloudRequest;
 import org.jboss.tools.deltacloud.core.client.request.ListHardwareProfileRequest;
 import org.jboss.tools.deltacloud.core.client.request.ListHardwareProfilesRequest;
 import org.jboss.tools.deltacloud.core.client.request.ListImageRequest;
 import org.jboss.tools.deltacloud.core.client.request.ListImagesRequest;
 import org.jboss.tools.deltacloud.core.client.request.ListInstanceRequest;
 import org.jboss.tools.deltacloud.core.client.request.ListInstancesRequest;
 import org.jboss.tools.deltacloud.core.client.request.ListKeyRequest;
 import org.jboss.tools.deltacloud.core.client.request.ListKeysRequest;
 import org.jboss.tools.deltacloud.core.client.request.ListRealmRequest;
 import org.jboss.tools.deltacloud.core.client.request.ListRealmsRequest;
 import org.jboss.tools.deltacloud.core.client.request.PerformInstanceActionRequest;
 import org.jboss.tools.deltacloud.core.client.request.TypeRequest;
 import org.jboss.tools.deltacloud.core.client.unmarshal.HardwareProfileUnmarshaller;
 import org.jboss.tools.deltacloud.core.client.unmarshal.HardwareProfilesUnmarshaller;
 import org.jboss.tools.deltacloud.core.client.unmarshal.ImageUnmarshaller;
 import org.jboss.tools.deltacloud.core.client.unmarshal.ImagesUnmarshaller;
 import org.jboss.tools.deltacloud.core.client.unmarshal.InstanceUnmarshaller;
 import org.jboss.tools.deltacloud.core.client.unmarshal.InstancesUnmarshaller;
 import org.jboss.tools.deltacloud.core.client.unmarshal.KeyUnmarshaller;
 import org.jboss.tools.deltacloud.core.client.unmarshal.KeysUnmarshaller;
 import org.jboss.tools.deltacloud.core.client.unmarshal.RealmUnmarshaller;
 import org.jboss.tools.deltacloud.core.client.unmarshal.RealmsUnmarshaller;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 /**
  * @author Andre Dietisheim (based on prior implementation by Martyn Taylor)
  */
 public class DeltaCloudClientImpl implements InternalDeltaCloudClient {
 
 	private static final String DOCUMENT_ELEMENT_DRIVER = "driver";
 	private static final String DOCUMENT_ELEMENT_API = "api";
 
 	private String baseUrl;
 	private String username;
 	private String password;
 	private DocumentBuilderFactory documentBuilderFactory;
 
 	public static enum DeltaCloudServerType {
 		UNKNOWN, MOCK, EC2
 	}
 
 	public DeltaCloudClientImpl(String url) throws MalformedURLException,
 			DeltaCloudClientException {
 		this(url, null, null);
 	}
 
 	public DeltaCloudClientImpl(String url, String username, String password) throws DeltaCloudClientException {
 		this.baseUrl = url;
 		this.username = username;
 		this.password = password;
 		this.documentBuilderFactory = DocumentBuilderFactory.newInstance();
 	}
 
 	protected InputStream request(DeltaCloudRequest deltaCloudRequest)
 			throws DeltaCloudClientException {
 		DefaultHttpClient httpClient = new DefaultHttpClient();
 		try {
 			URL url = deltaCloudRequest.getUrl();
 			addCredentials(url, httpClient, username, password);
 			HttpUriRequest request = createRequest(deltaCloudRequest);
 			HttpResponse httpResponse = httpClient.execute(request);
 			throwOnHttpErrors(deltaCloudRequest.getUrl(), httpResponse);
 			if (httpResponse.getEntity() == null) {
 				return null;
 			}
 			return httpResponse.getEntity().getContent();
 		} catch (DeltaCloudClientException e) {
 			throw e;
 		} catch (MalformedURLException e) {
 			throw new DeltaCloudClientException(MessageFormat.format(
 					"Could not connect to \"{0}\". The url is invalid.", deltaCloudRequest.toString()), e);
 		} catch (IOException e) {
 			throw new DeltaCloudClientException(e);
 		} catch (Exception e) {
 			throw new DeltaCloudClientException(e);
 		}
 	}
 
 	protected String requestStringResponse(DeltaCloudRequest deltaCloudRequest)
 			throws DeltaCloudClientException {
 		try {
 			InputStream inputStream = request(deltaCloudRequest);
 			if (inputStream == null) {
 				return null;
 			}
 			return getResponse(inputStream);
 		} catch (IOException e) {
 			throw new DeltaCloudClientException(e);
 		}
 	}
 
 	private void throwOnHttpErrors(URL requestUrl, HttpResponse httpResponse)
 			throws DeltaCloudClientException {
 		int statusCode = httpResponse.getStatusLine().getStatusCode();
 		if (HttpStatusCode.OK.isStatus(statusCode)) {
 			return;
		} else if (HttpStatusCode.UNAUTHORIZED.isStatus(statusCode)) {
 			throw new DeltaCloudAuthClientException(
 					MessageFormat
 							.format("The server reported an authorization error \"{0}\" on requesting \"{1}\"",
 									httpResponse.getStatusLine()
 											.getReasonPhrase(), requestUrl));
 		} else if (HttpStatusCode.NOT_FOUND.isStatus(statusCode)) {
 			throw new DeltaCloudNotFoundClientException(MessageFormat.format(
 					"The server could not find the resource \"{0}\"",
 					requestUrl));
 		} else if (HttpStatusRange.CLIENT_ERROR.isInRange(statusCode)
 				|| HttpStatusRange.SERVER_ERROR.isInRange(statusCode)) {
 			throw new DeltaCloudClientException(
 					MessageFormat
 							.format("The server reported an error \"{0}\" on requesting \"{1}\"",
 									httpResponse.getStatusLine()
 											.getReasonPhrase(), requestUrl));
 		}
 	}
 
 	private String getResponse(InputStream inputStream) throws IOException,
 			DeltaCloudClientException {
 		if (inputStream == null) {
 			return null;
 		}
 		String xml = readInputStreamToString(inputStream);
 		return xml;
 	}
 
 	/**
 	 * Returns a request instance for the given request type and url.
 	 * 
 	 * @param httpMethod
 	 *            the request type to use
 	 * @param requestUrl
 	 *            the requested url
 	 * @return the request instance
 	 * @throws MalformedURLException
 	 */
 	protected HttpUriRequest createRequest(DeltaCloudRequest deltaCloudRequest)
 			throws MalformedURLException {
 		HttpUriRequest request = null;
 		String url = deltaCloudRequest.getUrl().toString();
 		HttpMethod httpMethod = deltaCloudRequest.getHttpMethod();
 		switch (httpMethod) {
 		case POST:
 			request = new HttpPost(url);
 			break;
 		case DELETE:
 			request = new HttpDelete(url);
 			break;
 		case GET:
 		default:
 			request = new HttpGet(url);
 		}
 		request.setHeader("Accept", "application/xml;q=1");
 		return request;
 	}
 
 	/**
 	 * Adds the credentials to the given http client.
 	 * 
 	 * @param httpClient
 	 *            the http client
 	 * @return the default http client
 	 * @throws UnknownHostException
 	 */
 	private DefaultHttpClient addCredentials(URL url,
 			DefaultHttpClient httpClient, String username, String password)
 			throws UnknownHostException {
 		if (username != null && password != null) {
 			httpClient.getCredentialsProvider().setCredentials(
 					new AuthScope(url.getHost(), url.getPort()),
 					new UsernamePasswordCredentials(username, password));
 		}
 		return httpClient;
 	}
 
 	private static String readInputStreamToString(InputStream is)
 			throws DeltaCloudClientException {
 		try {
 			try {
 				if (is != null) {
 					StringBuilder sb = new StringBuilder();
 					String line = null;
 
 					BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
 					while ((line = reader.readLine()) != null) {
 						sb.append(line).append("\n");
 					}
 					return sb.toString();
 				}
 			} finally {
 				is.close();
 			}
 		} catch (Exception e) {
 			throw new DeltaCloudClientException(
 					"Error converting Response to String", e);
 		}
 		return "";
 	}
 
 	public DeltaCloudServerType getServerType() {
 		DeltaCloudServerType serverType = DeltaCloudServerType.UNKNOWN;
 		try {
 			String apiResponse = requestStringResponse(new TypeRequest(baseUrl));
 			Document document = getDocument(apiResponse);
 			NodeList elements = document.getElementsByTagName(DOCUMENT_ELEMENT_API);
 			if (elements.getLength() > 0) {
 				Node n = elements.item(0);
 				Node driver = n.getAttributes().getNamedItem(DOCUMENT_ELEMENT_DRIVER);
 				if (driver != null) {
 					String driverValue = driver.getNodeValue();
 					serverType = DeltaCloudServerType.valueOf(driverValue.toUpperCase());
 				}
 			}
 		} catch (Exception e) {
 			serverType = DeltaCloudServerType.UNKNOWN;
 		}
 		return serverType;
 	}
 
 	@Override
 	public Instance createInstance(String imageId) throws DeltaCloudClientException {
 		try {
 			InputStream response = request(new CreateInstanceRequest(baseUrl, imageId));
 			return new InstanceUnmarshaller().unmarshall(response, new Instance());
 		} catch (DeltaCloudClientException e) {
 			throw e;
 		} catch (Exception e) {
 			throw new DeltaCloudClientException(e);
 		}
 
 	}
 
 	public Instance createInstance(String name, String imageId, String profileId, String realmId, String memory,
 			String storage) throws DeltaCloudClientException {
 		return createInstance(name, imageId, profileId, realmId, null, memory, storage);
 	}
 
 	public Instance createInstance(String name, String imageId, String profileId, String realmId, String keyId,
 			String memory, String storage) throws DeltaCloudClientException {
 		try {
 			InputStream response = request(
 					new CreateInstanceRequest(baseUrl, name, imageId, profileId, realmId, keyId, memory, storage));
 			Instance instance = new InstanceUnmarshaller().unmarshall(response, new Instance());
 			// TODO: WORKAROUND for
 			// https://issues.jboss.org/browse/JBIDE-8005
 			if (keyId != null) {
 				instance.setKeyId(keyId);
 			}
 			// TODO: WORKAROUND for
 			// https://issues.jboss.org/browse/JBIDE-8005
 			return instance;
 		} catch (DeltaCloudClientException e) {
 			throw e;
 		} catch (Exception e) {
 			throw new DeltaCloudClientException(e);
 		}
 	}
 
 	@Override
 	public HardwareProfile listProfile(String profileId) throws DeltaCloudClientException {
 		try {
 			InputStream response = request(new ListHardwareProfileRequest(baseUrl, profileId));
 			return new HardwareProfileUnmarshaller().unmarshall(response, new HardwareProfile());
 		} catch (DeltaCloudClientException e) {
 			throw e;
 		} catch (Exception e) {
 			throw new DeltaCloudClientException(e);
 		}
 	}
 
 	@Override
 	public List<HardwareProfile> listProfiles() throws DeltaCloudClientException {
 		try {
 			InputStream response = request(new ListHardwareProfilesRequest(baseUrl));
 			List<HardwareProfile> profiles = new ArrayList<HardwareProfile>();
 			new HardwareProfilesUnmarshaller().unmarshall(response, profiles);
 			return profiles;
 		} catch (Exception e) {
 			throw new DeltaCloudClientException(MessageFormat.format("could not get realms on cloud at \"{0}\"",
 					baseUrl), e);
 		}
 	}
 
 	@Override
 	public List<Image> listImages() throws DeltaCloudClientException {
 		InputStream response = request(new ListImagesRequest(baseUrl));
 		List<Image> images = new ArrayList<Image>();
 		new ImagesUnmarshaller().unmarshall(response, images);
 		return images;
 	}
 
 	@Override
 	public Image listImages(String imageId) throws DeltaCloudClientException {
 		InputStream response = request(new ListImageRequest(baseUrl, imageId));
 		return new ImageUnmarshaller().unmarshall(response, new Image());
 	}
 
 	@Override
 	public List<Instance> listInstances() throws DeltaCloudClientException {
 		InputStream inputStream = request(new ListInstancesRequest(baseUrl));
 		List<Instance> instances = new ArrayList<Instance>();
 		new InstancesUnmarshaller().unmarshall(inputStream, instances);
 		return instances;
 	}
 
 	@Override
 	public Instance listInstances(String instanceId) throws DeltaCloudClientException {
 		try {
 			InputStream response = request(new ListInstanceRequest(baseUrl, instanceId));
 			return new InstanceUnmarshaller().unmarshall(response, new Instance());
 		} catch (DeltaCloudClientException e) {
 			throw e;
 		} catch (Exception e) {
 			throw new DeltaCloudClientException(e);
 		}
 	}
 
 	@Override
 	public List<Realm> listRealms() throws DeltaCloudClientException {
 		try {
 			InputStream inputStream = request(new ListRealmsRequest(baseUrl));
 			List<Realm> realms = new ArrayList<Realm>();
 			new RealmsUnmarshaller().unmarshall(inputStream, realms);
 			return realms;
 		} catch (Exception e) {
 			throw new DeltaCloudClientException(
 					MessageFormat.format("could not get realms on cloud at \"{0}\"", baseUrl), e);
 		}
 	}
 
 	@Override
 	public Realm listRealms(String realmId) throws DeltaCloudClientException {
 		try {
 			InputStream response = request(new ListRealmRequest(baseUrl, realmId));
 			return new RealmUnmarshaller().unmarshall(response, new Realm());
 		} catch (Exception e) {
 			throw new DeltaCloudClientException(
 					MessageFormat.format("could not get realms on cloud at \"{0}\"", baseUrl), e);
 		}
 	}
 
 	public Key createKey(String keyname) throws DeltaCloudClientException {
 		try {
 			CreateKeyRequest keyRequest = new CreateKeyRequest(baseUrl, keyname);
 			InputStream inputStream = request(keyRequest);
 			Key key = new KeyUnmarshaller().unmarshall(inputStream, new Key());
 			return key;
 		} catch (DeltaCloudClientException e) {
 			throw e;
 		} catch (Exception e) {
 			throw new DeltaCloudClientException(e);
 		}
 	}
 
 	public void deleteKey(String keyname) throws DeltaCloudClientException {
 		request(new DeleteKeyRequest(baseUrl, keyname));
 	}
 
 	public List<Key> listKeys() throws DeltaCloudClientException {
 		InputStream inputStream = request(new ListKeysRequest(baseUrl));
 		List<Key> keys = new ArrayList<Key>();
 		new KeysUnmarshaller().unmarshall(inputStream, keys);
 		return keys;
 	}
 
 	public Key listKey(String id) throws DeltaCloudClientException {
 		InputStream inputStream = request(new ListKeyRequest(baseUrl, id));
 		Key key = new KeyUnmarshaller().unmarshall(inputStream, new Key());
 		return key;
 	}
 
 	private Document getDocument(String response) throws ParserConfigurationException, SAXException, IOException {
 		InputSource is = new InputSource(new StringReader(response));
 		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
 		Document document = documentBuilder.parse(is);
 		return document;
 	}
 
 	public boolean performInstanceAction(InstanceAction action) throws DeltaCloudClientException {
 		if (action != null) {
 			try {
 				InputStream inputStream = request(
 						new PerformInstanceActionRequest(action.getUrl(), action.getMethod()));
 				if (!InstanceAction.DESTROY.equals(action.getName())) {
 					new InstanceUnmarshaller().unmarshall(inputStream, action.getOwner());
 				}
 			} catch (DeltaCloudClientException e) {
 				throw e;
 			} catch (Exception e) {
 				throw new DeltaCloudClientException(e);
 			}
 			return true;
 		}
 		return false;
 	}
 }
