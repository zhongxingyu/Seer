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
 
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 /**
  * Assemble valid URL's from component parts, encoding as necessary. There are
  * methods to add path components and query strings.
  * 
  * <h3>Anatomy of a URL</h3>
  * <p>
  * A picture is worth a thousand words, especially if it's ASCII Art. This
  * stunning image is reproduced from <a
  * href="http://tools.ietf.org/html/rfc3986">RFC 3986</a>
  * 
  * <code>
  * <pre>
  *        foo://example.com:8042/over/there?name=ferret#nose
  *        \_/   \______________/\_________/ \_________/ \__/
  *         |           |            |            |        |
  *      scheme     authority       path        query   fragment
  * </pre>
  * </code>
  * 
  * The URL class in Java is too stringent to accept our use cases here, namely
 * you can not create a URL class that excludes the scheme and authority
  * portions of a URL. However, Java does include a URI class which is suitable
  * for our needs.
  * 
  * <h3>Usage Overview</h3>
  * <p>
  * The URLBuilder class doesn't help you build the scheme or authority parts of
  * a URL. The URI class in java.net handles that part perfectly well. This class
  * helps you with the path, query, and fragment parts of a URL.
  * <p>
  * Since this class relies on the URI class to handle the scheme and authority
  * parts of a URI, we frequently refer to them together. We've chosen the term
  * <code>base</code>. It's short and descriptive.
  * <p>
  * Here's a few examples of using URLBuilder to construct URL's. Here's an easy
  * one:
  * 
  * <code>
  * <pre>
  * URI base = new URI("http://www.example.com");
  * URLBuilder urlb = new URLBuilder(base);
  * urlb.add("yellow").add("brick");
  * urlb.add("road.html");
  * assertEquals("http://www.example.com/yellow/brick/road.html",urlb.toString());
  * </pre>
  * </code>
  * 
  * Here's one with a query string:
  * 
  * <code>
  * <pre>
  * URI base = new URI("http://www.example.com");
  * URLBuilder urlb = new URLBuilder(base);
  * urlb.add("people.xml").addQuery("title","manager");
  * assertEquals("http://www.example.com/people.xml?title=manager",urlb.toString());
  * </pre>
  * </code>
  * 
  * All of the {@link #add(Object)} and {@link #addQuery(Object, Object)} methods
  * do their thing, but also return <code>this</code>. Which means you can chain
  * the methods together like we have done in both of our examples so far.
  * 
  * We don't have to have a base, we can create just the path component and the
  * query string:.
  * 
  * <code>
  * <pre>
  * URLBuilder urlb = new URLBuilder();
  * urlb.add("people.xml")
  * urlb.addQuery("title","manager");
  * assertEquals("people.xml?title=manager",urlb.toString());
  * </pre>
  * </code>
  * 
  * @version $LastChangedRevision$ <br>
  *          $LastChangedDate$
  * @author $LastChangedBy$
  */
 public class URLBuilder {
 
 	// this separates the segments of the path
 	private static final char PATH_SEGMENT_SEPARATOR = '/';
 	// this separates the entire query string from the rest of the URL
 	private static final char QUERY_DELIMITER = '?';
 	// the separates the various query parameters from each other
 	private static final char QUERY_PARAM_SEPARATOR = '&';
 	// join the key and value into a single query parameter
 	private static final char QUERY_PARAM_JOINER = '=';
 	// this separates the fragment from the rest of the URL
 	private static final char FRAGMENT_DELIMITER = '#';
 
 	private static final String UTF8 = "UTF-8";
 
 	private URI base;
 	private ArrayList<String> path;
 	private ArrayList<QueryParam> query;
 	private String fragment;
 
 	/**
 	 * Create an empty URL builder, no base, no path.
 	 */
 	public URLBuilder() {
 		init();
 	}
 
 	/**
 	 * Create a new URL builder using an existing URL as a base.
 	 * 
 	 * @param base
 	 *            an existing URI fragment
 	 * @throws MalformedURLException
 	 */
 	public URLBuilder(URI base) throws MalformedURLException {
 		init();
 		this.setBase(base);
 	}
 
 	/**
 	 * Create a URL builder with a path. In the example below, the URL's for A
 	 * and B are the same.
 	 * 
 	 * <code>
 	 * <pre>
 	 * URLBuilder a = new URLBuilder();
 	 * a.add("people.xml");
 	 * URLBuilder b = new URLBuilder("people.xml");
 	 * assertEquals(a.toString(), b.toString())
 	 * assertEquals("/people.xml", a.toString());
 	 * </pre>
 	 * </code>
 	 * 
 	 * @param path
 	 *            the path to start the URL with
 	 */
 	public URLBuilder(String path) {
 		init();
 		this.add(path);
 	}
 
 	private void init() {
 		clearPath();
 		clearQuery();
 	}
 
 	/**
 	 * Return the base URI object for the URLBuilder instance.
 	 * 
 	 * @return the base URI
 	 */
 	public URI getBase() {
 		return base;
 	}
 
 	/**
 	 * Set the base URI to be used for this URLBuilder instance. The scheme,
 	 * authority, and path components, if present, will be used as the base for
 	 * this URLBuilder object. Query parameters and fragments in
 	 * <code>base</code> will be silently ignored.
 	 * 
 	 * If you pass an <i>opaque</i> URI, a MalformedURLException will be thrown.
 	 * 
 	 * @param base
 	 * @throws MalformedURLException
 	 */
 	public void setBase(URI base) throws MalformedURLException {
 		StringBuffer newurl = new StringBuffer();
 		if (base.isOpaque()) {
 			throw new MalformedURLException();
 		}
 		if (base.getScheme() != null) {
 			// we assume you have scheme and authority
 			newurl.append(base.getScheme());
 			newurl.append("://");
 			newurl.append(base.getAuthority());
 		}
 		newurl.append(base.getPath());
 		try {
 			this.base = new URI(newurl.toString());
 		} catch (URISyntaxException e) {
 			// Uh-oh
 		}
 	}
 
 	/**
 	 * Append a path component to the URL. Adding slashes is taken care of for
 	 * you. If you want "/people/1/contacts.xml" then you have two choices:
 	 * 
 	 * <code>
 	 * <pre>
 	 * URLBuilder u = new URLBuilder();
 	 * u.add("people").add("1").add("contacts.xml");
 	 * 
 	 * URLBuilder v = new URLBuilder();
 	 * u.add("/people/1/contacts.xml");
 	 * </pre>
 	 * </code>
 	 * 
 	 * 
 	 * If you want "http://localhost:3000/people/1/contacts.xml" then do:
 	 * 
 	 * <code>
 	 * <pre>
 	 * URLBuilder u = new URLBuilder("http://localhost:3000");
 	 * u.add("people").add("1").add("contacts.xml");
 	 * </pre>
 	 * </code>
 	 * 
 	 * This method can not be used to set the scheme or authority segments of
 	 * the url.
 	 * 
 	 * @param pathcomponent
 	 *            any object that can render itself as a string
 	 * 
 	 * @return self
 	 */
 	public URLBuilder add(Object pathcomponent) {
 		StringTokenizer st = new StringTokenizer(pathcomponent.toString(),
 				Character.toString(PATH_SEGMENT_SEPARATOR));
 		while (st.hasMoreTokens()) {
 			path.add(st.nextToken());
 		}
 		return this;
 	}
 
 	/**
 	 * clear all path components
 	 */
 	public void clearPath() {
 		this.path = new ArrayList<String>();
 	}
 
 	/**
 	 * Add a query parameter to the URL. You can do lots of fun stuff with this.
 	 * As long as the key and the value both return a useful .toString(), you
 	 * are good to go.
 	 * <p>
 	 * If you pass a value that implements Iterable, then you will get a query
 	 * parameter added for each value returned by hasNext(). You can also pass a
 	 * value that is an array[] of somethings and get the same behavior.
 	 * 
 	 * @param key
 	 * @param value
 	 * @return self
 	 */
 	public URLBuilder addQuery(Object key, Object value) {
 		query.add(new QueryParam(key, value));
 		return this;
 	}
 
 	/**
 	 * Add a bunch of query parameters from a Map. This method provides
 	 * compatability with the query parameters provided by
 	 * javax.servlet.ServletRequest
 	 * 
 	 * @param params
 	 * @return self
 	 */
 	public URLBuilder addQuery(Map<Object, Object> params) {
 		for (Map.Entry<Object, Object> e : params.entrySet())
 			query.add(new QueryParam(e.getKey(), e.getValue()));
 		return this;
 	}
 
 	/**
 	 * Add just the query part (ignore the path) from another URLBuilder and add
 	 * it to this URLBuilder.
 	 * 
 	 * @param params
 	 * @return self
 	 */
 	public URLBuilder addQuery(URLBuilder params) {
 		query.addAll(params.query);
 		return this;
 	}
 
 	/**
 	 * clear all query parameters
 	 */
 	public void clearQuery() {
 		this.query = new ArrayList<QueryParam>();
 	}
 
 	/**
 	 * Set the fragment for this URL. Don't put a question mark or a slash in
 	 * your fragment or an IllegalArgumentException will be thrown.
 	 * 
 	 * @param fragment
 	 *            the fragment to be attached to the URL
 	 */
 	public void setFragment(String fragment) {
 		if (fragment.contains(String.valueOf(PATH_SEGMENT_SEPARATOR))
 				|| fragment.contains(String.valueOf(QUERY_DELIMITER))) {
 			throw new IllegalArgumentException();
 		}
 		this.fragment = fragment;
 	}
 
 	/**
 	 * get the fragment to be attached to the URL
 	 * 
 	 * @return the fragment to be attached to the URL
 	 */
 	public String getFragment() {
 		return this.fragment;
 	}
 
 	/**
 	 * clear any fragment associated with this URL
 	 */
 	public void clearFragment() {
 		this.fragment = null;
 	}
 
 	/**
 	 * turn this URL builder object into a URLEncoded string
 	 */
 	public String toString() {
 		Iterator<String> pi;
 		Iterator<URLBuilder.QueryParam> qi;
 		StringBuffer out = new StringBuffer();
 
 		// first the base
 		if (base != null)
 			out.append(base.toString());
 
 		// then the path
 		if ((pi = path.iterator()).hasNext()) {
 			if (out.length() == 0) {
 				// nothing there yet but we have a path; tack on a slash
 				out.append(PATH_SEGMENT_SEPARATOR);
 			} else {
 				// we have something already, see if it ends with a slash
 				// if not, add one
 				if (out.charAt(out.length() - 1) != PATH_SEGMENT_SEPARATOR)
 					out.append(PATH_SEGMENT_SEPARATOR);
 			}
 
 			try {
 				out.append(URLEncoder.encode(pi.next(), UTF8));
 				while (pi.hasNext())
 					out.append(PATH_SEGMENT_SEPARATOR).append(
 							URLEncoder.encode(pi.next(), UTF8));
 			} catch (UnsupportedEncodingException e) {
 			}
 		}
 
 		// the query string
 		if ((qi = query.iterator()).hasNext()) {
 			out.append(QUERY_DELIMITER);
 			out.append(qi.next().toString());
 			while (qi.hasNext())
 				out.append(QUERY_PARAM_SEPARATOR).append(qi.next().toString());
 		}
 
 		// and finally the fragment
 		if (this.fragment != null) {
 			try {
 				out.append(FRAGMENT_DELIMITER);
 				out.append(URLEncoder.encode(this.fragment, UTF8));
 			} catch (UnsupportedEncodingException e) {
 			}
 		}
 		return out.toString();
 	}
 
 	/**
 	 * turn this URLBuilder object into a new URI
 	 * 
 	 * @return a new URI object
 	 */
 	public URI toURI() {
 		try {
 			return new URI(toString());
 		} catch (URISyntaxException e) {
 			// if this happens the URLBuilder class is broken
 			throw new RuntimeException(e);
 		}
 	}
 
 	/**
 	 * shamelessly took the LinkTool class in Velocity Tools 1.4 and improved it
 	 * http://velocity.apache.org/tools/releases/1.4/view/LinkTool.html
 	 * 
 	 * @version $LastChangedRevision$ <br>
 	 *          $LastChangedDate$
 	 * @author $LastChangedBy$
 	 */
 	public final class QueryParam {
 
 		private final Object key;
 		private final Object value;
 
 		/**
 		 * Create a new query parameter
 		 * 
 		 * @param key
 		 *            query pair
 		 * @param value
 		 *            query value
 		 */
 		public QueryParam(Object key, Object value) {
 			this.key = key;
 			this.value = value;
 		}
 
 		/**
 		 * Return the URL-encoded query string.
 		 */
 		public String toString() {
 			StringBuffer out = new StringBuffer();
 			try {
 				String encodedKey = URLEncoder.encode(key.toString(), UTF8);
 				if (value == null) {
 					out.append(encodedKey).append(QUERY_PARAM_JOINER);
 
 				} else if (Iterable.class.isInstance(value)) {
 					// if it's something iterable, then spin through it
 					Iterator<?> i;
 					if ((i = ((Iterable<?>) value).iterator()).hasNext()) {
 						out.append(encodedKey);
 						out.append(QUERY_PARAM_JOINER);
 						out.append(i.next().toString());
 
 						while (i.hasNext()) {
 							out.append(QUERY_PARAM_SEPARATOR);
 							out.append(encodedKey);
 							out.append(QUERY_PARAM_JOINER);
 							out.append(i.next().toString());
 						}
 					}
 
 				} else if (value instanceof Object[]) {
 					// we'll take array's too
 					Object[] array = (Object[]) value;
 					for (int i = 0; i < array.length; i++) {
 						out.append(encodedKey);
 						out.append(QUERY_PARAM_JOINER);
 						if (array[i] != null) {
 							out.append(URLEncoder.encode(
 									String.valueOf(array[i]), UTF8));
 						}
 						if (i + 1 < array.length) {
 							out.append(QUERY_PARAM_SEPARATOR);
 						}
 					}
 
 				} else {
 					out.append(encodedKey);
 					out.append(QUERY_PARAM_JOINER);
 					out.append(URLEncoder.encode(String.valueOf(value), UTF8));
 				}
 			} catch (UnsupportedEncodingException e) {
 				// make sure we return an empty string
 				out = new StringBuffer();
 			}
 
 			return out.toString();
 		}
 	}
 }
