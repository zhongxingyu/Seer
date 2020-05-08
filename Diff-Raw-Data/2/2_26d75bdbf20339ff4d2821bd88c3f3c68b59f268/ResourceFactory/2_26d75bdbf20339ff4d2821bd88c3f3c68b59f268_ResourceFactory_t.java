 /*
 
 Copyright (c) 2010, Jared Crapo All rights reserved. 
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met: 
 
 - Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer. 
 
 - Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution. 
 
 - Neither the name of jactiveresource.org nor the names of its
   contributors may be used to endorse or promote products derived from
   this software without specific prior written permission. 
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 
  */
 
 package org.jactiveresource;
 
 import java.io.BufferedReader;
 import java.io.EOFException;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.http.HttpException;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.util.EntityUtils;
 import org.jactiveresource.annotation.CollectionName;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
 import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
 import com.thoughtworks.xstream.io.xml.XppDriver;
 
 /**
  * A resource factory provides methods that construct url's, retrieves the
  * contents of those url's, and deserializes the contents into java objects.
  * 
  * <h3>Creating a Factory</h3>
  * 
  * To create a factory, you need a <code>ResourceConnection</code> object that
  * points to the URL, and you need a class that represents the resources
  * provided by the URL.
  * 
  * Say there is a person resource available at
  * <code>http://localhost:3000/people.xml</code>, and a <code>Person</code>
  * class which models the data elements provided by that resource. You can use
  * those two things to create a <code>ResourceFactory</code> for people.
  * 
  * <code>
  * <pre>
  * ResourceConnection c = new ResourceConnection("http://localhost:3000");
  * ResourceFactory f = new ResourceFactory<Person>(c, Person.class);
  * </pre>
  * </code>
  * 
  * XML is the default format, but if you want to be declarative about it you
  * could use:
  * 
  * <code>
  * <pre>
  * ResourceFactory f = new ResourceFactory<Person>(c, Person.class, ResourceFormat.XML);
  * </pre>
  * </code>
  * 
  * <h3>REST operations</h3>
  * 
  * The factory provides basic REST methods for creating, retrieving, updating
  * and deleting resources. The core methods are:
  * <ul>
  * <li>{@link #find(String)} - find a resource on the server with the given
  * identifier</li>
  * <li>{@link #findAll()} - find all resources known to the server</li>
  * <li>{@link #findAll(URL)} - find all resources meeting certain criteria</li>
  * <li>{@link #instantiate()} - create an empty resource object</li>
  * <li>{@link #create(Resource)} - create a new resource on the server</li>
  * <li>{@link #update(Resource)} - update an existing resource on the server</li>
  * <li>{@link #save(Resource)} - convenience method that creates if object is
  * new, updates it otherwise</li>
  * <li>{@link #reload(Resource)} - reload an object from the server</li>
  * <li>{@link #reload(Resource)} - delete a resource from the server</li>
  * </ul>
  * <p>
  * You may discover that the methods supplied are not sufficient for all of the
  * capabilities provided by the server resource you are accessing. You can
  * easily subclass and add additional methods specific to a particular resource.
  * There are several lower level methods available which can do the network IO
  * and serialization for you.
  * <ul>
  * <li>{@link #fetchMany(Object)} - pass this method any url, and it will fetch
  * and deserialize the contents into a list</li>
  * <li>{@link #deserializeMany(BufferedReader)} - if you want to do the network
  * IO yourself, this method will deserialize a list of objects from an input
  * stream</li>
  * </ul>
  * 
  * @version $LastChangedRevision$ <br>
  *          $LastChangedDate$
  * @author $LastChangedBy$
  */
 public class ResourceFactory<T extends Resource> {
 
 	private ResourceConnection connection;
 	private ResourceFormat rf;
 	private Class<T> clazz;
 	private XStream xstream;
 	private Log log = LogFactory.getLog(ResourceFactory.class);
 
 	/**
 	 * Create a new resource factory for a class mapped to a network resource.
 	 * By default, the format is assumed to be XML.
 	 * 
 	 * You have to pass in the class for this resource in addition to using the
 	 * concrete parameterized type because of type erasure. See <a href=
 	 * "http://www.angelikalanger.com/GenericsFAQ/FAQSections/ParameterizedTypes.html#FAQ106"
 	 * >Angelika Langer's Java Generics FAQ</a> for details.
 	 * 
 	 * @param c
 	 * @param clazz
 	 */
 	public ResourceFactory(ResourceConnection c, Class<T> clazz) {
 		this(c, clazz, ResourceFormat.XML);
 	}
 
 	/**
 	 * If your resources uses a ResourceFormat other than the default XML, use
 	 * this constructor to specify which ResourceFormat you would like to use.
 	 * 
 	 * @param c
 	 * @param clazz
 	 * @param rf
 	 */
 	public ResourceFactory(ResourceConnection c, Class<T> clazz,
 			ResourceFormat rf) {
 		this.setConnection(c);
 		this.setResourceClass(clazz);
 		this.rf = rf;
 		makeXStream();
 		registerClass(clazz);
 	}
 
 	/**
 	 * return the connection used by this factory
 	 * 
 	 * @return the {@link ResourceConnection} for this factory
 	 */
 	public ResourceConnection getConnection() {
 		return connection;
 	}
 
 	/**
 	 * set the connection to be used by this factory
 	 * 
 	 * @param connection
 	 */
 	public void setConnection(ResourceConnection connection) {
 		this.connection = connection;
 	}
 
 	/**
 	 * return the resource format used by this factory. This is set when the
 	 * factory is created.
 	 * 
 	 * @return a resource format
 	 */
 	public final ResourceFormat getResourceFormat() {
 		return this.rf;
 	}
 
 	/**
 	 * create the appropriate Hierarchical Stream Driver for XStream based on
 	 * the resource format of this factory
 	 * 
 	 * @return a stream driver for XStream
 	 */
 	protected HierarchicalStreamDriver getStreamDriver() {
 		HierarchicalStreamDriver hsd = null;
 		switch (getResourceFormat()) {
 		case XML:
 			hsd = new XppDriver();
 			break;
 		case JSON:
 			hsd = new JettisonMappedXmlDriver();
 			break;
 		}
 		return hsd;
 	}
 
 	/**
 	 * create an XStream object for use in serialization/deserializations. The
 	 * created object is retained in the factory for future use, as well as
 	 * returned to the caller.
 	 * 
 	 * The default behavior is to create an XStream object using the stream
 	 * driver returned by {@link #getStreamDriver()}. Subclasses may include
 	 * custom xstream creation and/or/configuration of aliases, implicit
 	 * collections etc.
 	 * 
 	 * You can do your XStream configuration outside of the factory by using
 	 * either this method or {@link #getXStream()} and then putting it back with
 	 * {@link #setXStream(XStream)}
 	 * 
 	 * @return a new XStream object
 	 */
 	public XStream makeXStream() {
 		setXStream(new XStream(getStreamDriver()));
 		return getXStream();
 	}
 
 	/**
 	 * return the XStream object used by this factory
 	 * 
 	 * @return the current XStream object
 	 */
 	public XStream getXStream() {
 		return xstream;
 	}
 
 	/**
 	 * set the XStream object used by this factory
 	 * 
 	 * @param xstream
 	 */
 	public void setXStream(XStream xstream) {
 		this.xstream = xstream;
 	}
 
 	/**
 	 * Tell XStream to process the annotations on a class. This is generally the
 	 * preferred method of tweaking the xml construction or parsing.
 	 * 
 	 * @param c
 	 */
 	public void registerClass(Class<?> c) {
 		log.trace("registering class " + c.getName());
 		log.trace("processing XStream annotations");
 		getXStream().processAnnotations(c);
 	}
 
 	/**
 	 * Retrieve the resource identified by <code>id</code>, and return a new
 	 * instance of the appropriate object
 	 * 
 	 * @param id
 	 *            the primary identifier
 	 * @return one new resource
 	 * @throws URISyntaxException
 	 * @throws InterruptedException
 	 * @throws IOException
 	 * @throws HttpException
 	 */
 	public T find(String id) throws HttpException, IOException,
 			InterruptedException, URISyntaxException {
 		log.trace("finding id=" + id);
 		return fetchOne(uriForOne(id));
 	}
 
 	/**
 	 * Fetch all the resources. Say I have a person service at
 	 * <code>http://localhost:3000/</code>. The following would return the list
 	 * of people returned by <code>http://localhost:3000/people.xml</code>.
 	 * 
 	 * <code>
 	 * <pre>
 	 * c = new ResourceConnection("http://localhost:3000");
 	 * rf = new ResourceFactory<Person>(c, Person.class);
 	 * ArrayList<Person> people = rf.findAll();
 	 * </pre>
 	 * </code>
 	 * 
 	 * @return a list of resources
 	 * @throws HttpException
 	 * @throws IOException
 	 * @throws InterruptedException
 	 * @throws URISyntaxException
 	 */
 
 	public ArrayList<T> findAll() throws HttpException, IOException,
 			InterruptedException, URISyntaxException {
 		URI url = uriForCollection();
		log.trace("finding all url=" + url);
 		return fetchMany(url);
 	}
 
 	/**
 	 * Fetch resources from the URL you supply. There is a convenience class to
 	 * help you construct your URL's easily, see {@link URLBuilder}. If the URL
 	 * you pass only returns one resource, you're still going to get an array
 	 * with a single element in it.
 	 * 
 	 * To get resources from
 	 * <code>http://localhost:3000/people/wierdos.xml?haircolor=green</code> do:
 	 * 
 	 * <code>
 	 * <pre>
 	 * ResourceConnection c = new ResourceConnection("http://localhost:3000");
 	 * ResourceFactory<Person> rf = new ResourceFactory<Person>(c, Person.class);
 	 * URLBuilder urlb = new URLBuilder(rf.getCollectionName());
 	 * urlb.add("wierdos" + getResourceFormat().extension());
 	 * urlb.addQuery("haircolor", "green");
 	 * ArrayList<Person> people = rf.findAll(urlb.toURL());
 	 * </pre>
 	 * </code>
 	 * 
 	 * @param url
 	 *            the URL you want to retrieve the objects from
 	 * @return a list of objects
 	 * @throws HttpException
 	 * @throws IOException
 	 * @throws InterruptedException
 	 * @throws URISyntaxException
 	 */
 	public ArrayList<T> findAll(URL url) throws HttpException, IOException,
 			InterruptedException, URISyntaxException {
 		log.trace("finding all url=" + url);
 		return fetchMany(url);
 	}
 
 	/**
 	 * Return true if a resource exists. Say I have a person service at
 	 * <code>http://localhost:3000/</code>.
 	 * 
 	 * <code>
 	 * <pre>
 	 * c = new ResourceConnection("http://localhost:3000");
 	 * rf = new ResourceFactory(c, Person.class);
 	 * boolean fred = rf.exists("5");
 	 * </pre>
 	 * </code>
 	 * 
 	 * If <code>http://localhost:3000/people/5.xml</code> is a valid URL which
 	 * returns data , then <code>fred</code> is true.
 	 * 
 	 * @param id
 	 *            the id you want to check
 	 * @return true if the resource exists, false if it does not
 	 */
 	public boolean exists(String id) {
 		log.trace("exists(String id) id=" + id);
 
 		URI url = uriForOne(id);
 		try {
 			getConnection().get(url);
 			log.trace(url + " exists");
 			return true;
 		} catch (ResourceNotFound e) {
 			log.trace(url + " does not exist");
 			return false;
 		} catch (HttpException e) {
 			log.info(url + " generated an HttpException", e);
 			return false;
 		} catch (IOException e) {
 			log.info(url + " generated an IOException", e);
 			return false;
 		} catch (InterruptedException e) {
 			log.info(url + " generated an InterruptedException", e);
 			return false;
 		} catch (URISyntaxException e) {
 			log.info(url + " generated an URISyntaxException", e);
 			return false;
 		}
 	}
 
 	/**
 	 * Create a new instance of a resource. This method calls the default
 	 * constructor of your resource class, and also attaches the factory to the
 	 * resource class. Say we had a Person class like this:
 	 * 
 	 * <code>
 	 * <pre>
 	 * public class Person extends ActiveResource {
 	 *   private String id;
 	 *   public String getId() {
 	 *     return id;
 	 *   }
 	 *   public void setId(String id) {
 	 *     this.id = id;
 	 *   }
 	 * }
 	 * </pre>
 	 * </code>
 	 * 
 	 * And:
 	 * 
 	 * <code>
 	 * <pre>
 	 * ResourceFactory pf = new ResourceFactory(c, Person.class);
 	 * Person a = new Person();
 	 * a.setFactory(pf);
 	 * Person b = pf.instantiate();
 	 * </pre>
 	 * </code>
 	 * 
 	 * Person a and Person b are now equivalent.
 	 * 
 	 * @return a new instance of a resource
 	 * @throws InstantiationException
 	 * @throws IllegalAccessException
 	 */
 	public T instantiate() throws InstantiationException,
 			IllegalAccessException {
 		T obj = (T) getResourceClass().newInstance();
 		setFactory(obj);
 		log.trace("instantiated resource class=" + clazz.toString());
 		return obj;
 	}
 
 	/**
 	 * create a new resource on the server from a local object
 	 * 
 	 * @param r
 	 * @throws ClientProtocolException
 	 * @throws ClientError
 	 * @throws ServerError
 	 * @throws IOException
 	 */
 	public boolean create(T r) throws ClientProtocolException, ClientError,
 			ServerError, IOException {
 		log.trace("trying to create resource of class="
 				+ r.getClass().toString());
 		URI url = uriForCollection();
 		String xml = serializeOne(r);
 		HttpResponse response = getConnection().post(url, xml,
 				getResourceFormat().contentType());
 		String entity = EntityUtils.toString(response.getEntity());
 		try {
 			getConnection().checkHttpStatus(response);
 			getXStream().fromXML(entity, r);
 			setFactory(r);
 			log.trace("resource created from " + r.toString());
 			return true;
 		} catch (ResourceInvalid e) {
 			return false;
 		}
 	}
 
 	/**
 	 * update the server resource associated with an object
 	 * 
 	 * @param r
 	 * @throws URISyntaxException
 	 * @throws HttpException
 	 * @throws IOException
 	 * @throws InterruptedException
 	 */
 	public boolean update(T r) throws URISyntaxException, HttpException,
 			IOException, InterruptedException {
 		log.trace("update class=" + r.getClass().toString());
 		URI url = uriForOne(r.getId());
 		String xml = getXStream().toXML(r);
 		HttpResponse response = getConnection().put(url, xml,
 				getResourceFormat().contentType());
 		// String entity = EntityUtils.toString(response.getEntity());
 		try {
 			connection.checkHttpStatus(response);
 			return true;
 		} catch (ResourceInvalid e) {
 			return false;
 		}
 	}
 
 	/**
 	 * create the resource if it is new, otherwise update it
 	 * 
 	 * @param r
 	 * @throws ClientProtocolException
 	 * @throws IOException
 	 * @throws URISyntaxException
 	 * @throws HttpException
 	 * @throws InterruptedException
 	 */
 	public boolean save(T r) throws ClientProtocolException, IOException,
 			URISyntaxException, HttpException, InterruptedException {
 		if (r.isNew())
 			return create(r);
 		else
 			return update(r);
 	}
 
 	/**
 	 * repopulate a local object with data from the service
 	 * 
 	 * @param r
 	 * @throws HttpException
 	 * @throws IOException
 	 * @throws InterruptedException
 	 * @throws URISyntaxException
 	 */
 	public void reload(T r) throws HttpException, IOException,
 			InterruptedException, URISyntaxException {
 		log.trace("reloading class=" + r.getClass().toString());
 		URI url = uriForOne(r.getId());
 		fetchOne(url, r);
 	}
 
 	/**
 	 * delete a resource
 	 * 
 	 * @param r
 	 * @throws ClientError
 	 * @throws ServerError
 	 * @throws ClientProtocolException
 	 * @throws IOException
 	 */
 	public void delete(T r) throws ClientError, ServerError,
 			ClientProtocolException, IOException {
 		URI url = uriForOne(r.getId());
 		log.trace("deleting class=" + r.getClass().toString() + " id="
 				+ r.getId());
 		getConnection().delete(url);
 	}
 
 	/**
 	 * Create one object from the response of a given url. If your subclass
 	 * wants to create a bunch of cool find methods that each generate a proper
 	 * URL, they can then use this method to get the data and create the objects
 	 * from the response.
 	 * 
 	 * @param url
 	 * @return a new object representing the data returned by the url
 	 * @throws HttpException
 	 * @throws IOException
 	 * @throws InterruptedException
 	 * @throws URISyntaxException
 	 */
 	public T fetchOne(Object url) throws HttpException, IOException,
 			InterruptedException, URISyntaxException {
 		return deserializeOne(getConnection().get(url));
 	}
 
 	/**
 	 * Inflate (or unmarshall) an object from serialized data.
 	 * 
 	 * If the inflated class inherits from {@link ActiveResource} then also call
 	 * the setFactory() method so that the convenience methods of ActiveResource
 	 * work.
 	 * 
 	 * @param data
 	 *            a string of serialized data
 	 * @return a new object
 	 * @throws IOException
 	 */
 	public T deserializeOne(String data) throws IOException {
 		@SuppressWarnings("unchecked")
 		T obj = (T) getXStream().fromXML(data);
 		log.trace("create new object of class=" + obj.getClass().toString());
 		setFactory(obj);
 		return obj;
 	}
 
 	/**
 	 * serialize a single resource into a String
 	 * 
 	 * @param resource
 	 * @return a string representation of resource
 	 */
 	public String serializeOne(T resource) {
 		return getXStream().toXML(resource);
 	}
 
 	/**
 	 * Update an existing object with data from the given url
 	 * 
 	 * @param url
 	 * @param resource
 	 *            the object to update
 	 * @return the updated resource object you passed in
 	 * @throws HttpException
 	 * @throws IOException
 	 * @throws InterruptedException
 	 * @throws URISyntaxException
 	 */
 	public T fetchOne(Object url, T resource) throws HttpException,
 			IOException, InterruptedException, URISyntaxException {
 		return deserializeAndUpdateOne(getConnection().get(url), resource);
 	}
 
 	/**
 	 * Weasel method to update an existing object with serialized data.
 	 * 
 	 * @param data
 	 *            serialized data
 	 * @param resource
 	 *            the object to update
 	 * @return the updated resource object you passed in
 	 * @throws IOException
 	 */
 	public T deserializeAndUpdateOne(String data, T resource)
 			throws IOException {
 		getXStream().fromXML(data, resource);
 		log.trace("updating object of class=" + resource.getClass().toString()
 				+ " id=" + resource.getId());
 		setFactory(resource);
 		return resource;
 	}
 
 	/**
 	 * Create an array of objects from the response of a given url.
 	 * 
 	 * @param url
 	 * @return an array of objects
 	 * @throws HttpException
 	 * @throws IOException
 	 * @throws InterruptedExceptionInflate
 	 *             (or unmarshall) a list of objects from a stream
 	 * @throws URISyntaxException
 	 */
 	public ArrayList<T> fetchMany(Object url) throws HttpException,
 			IOException, InterruptedException, URISyntaxException {
 		return deserializeMany(getConnection().getStream(url));
 	}
 
 	/**
 	 * Inflate (or unmarshall) a list of objects from a stream using XStream.
 	 * This method exhausts and closes the stream.
 	 * 
 	 * @param stream
 	 *            an open input stream
 	 * @return a list of objects
 	 * @throws IOException
 	 */
 	@SuppressWarnings("unchecked")
 	public ArrayList<T> deserializeMany(BufferedReader stream)
 			throws IOException {
 		ObjectInputStream ostream = getXStream()
 				.createObjectInputStream(stream);
 		ArrayList<T> list = new ArrayList<T>();
 		T obj;
 		while (true) {
 			try {
 				obj = (T) ostream.readObject();
 				setFactory(obj);
 				list.add(obj);
 			} catch (EOFException e) {
 				break;
 			} catch (ClassNotFoundException e) {
 				// do nothing
 			}
 		}
 		ostream.close();
 		log.trace("deserialized " + list.size() + " objects");
 		return list;
 	}
 
 	/**
 	 * serialize a list of resources to a string. This is mostly useful for
 	 * testing because REST doesn't really have the capability to create many or
 	 * update many.
 	 * 
 	 * @param list
 	 *            a list of objects to be serialized
 	 * @return a string serialization of the resources in list
 	 */
 	public String serializeMany(List<T> list) {
 		return getXStream().toXML(list);
 	}
 
 	/**
 	 * return the url that accesses the resource identified by id, ie
 	 * <code>/people/1.xml</code>
 	 * 
 	 * If you pass null, you'll get null. If a malformed URL is created
 	 * 
 	 * @param id
 	 *            the identifier of the resource you want the URL to
 	 * @return a url fragment to be appended to a {@link ResourceConnection}
 	 * @throws MalformedURLException
 	 */
 	protected URI uriForOne(String id) {
 		URLBuilder urlb;
 		if (id == null) {
 			return null;
 		} else {
 			urlb = new URLBuilder(getCollectionName());
 			urlb.add(id + getResourceFormat().extension());
 			return urlb.toURI();
 		}
 	}
 
 	/**
 	 * return the url that accesses the entire collection of resources, ie
 	 * <code>/people.xml</code>
 	 * 
 	 * @return a url fragment to be appended to a {@link ResourceConnection}
 	 * @throws MalformedURLException
 	 */
 	protected URI uriForCollection() {
 		URLBuilder urlb;
 		urlb = new URLBuilder(getCollectionName()
 				+ getResourceFormat().extension());
 		return urlb.toURI();
 	}
 
 	/**
 	 * figure out the name of the collection of resources generated by the main
 	 * resource of this factory.
 	 * 
 	 * This method first looks for a CollectionName annotation on the class it
 	 * knows how to create. If there is no annotation, then the name of the
 	 * class is used.
 	 * 
 	 * @return the name of the collection
 	 */
 	protected String getCollectionName() {
 		String name;
 		CollectionName cn = getResourceClass().getAnnotation(
 				CollectionName.class);
 		if (cn != null) {
 			name = cn.value();
 		} else {
 			name = getResourceClass().getSimpleName();
 		}
 		return name;
 	}
 
 	/**
 	 * If the resource is a subclass of ActiveResource, then attach the factory
 	 * to it
 	 * 
 	 * @param resource
 	 */
 	@SuppressWarnings("unchecked")
 	private void setFactory(T resource) {
 		if (ActiveResource.class.isInstance(resource)) {
 			ActiveResource<T> res = (ActiveResource<T>) resource;
 			res.setFactory(this);
 		}
 	}
 
 	protected Class<T> getResourceClass() {
 		return clazz;
 	}
 
 	protected void setResourceClass(Class<T> clazz) {
 		this.clazz = clazz;
 	}
 
 }
