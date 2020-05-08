 /*******************************************************************************
  * Copyright (c) 2008, 2009 Bug Labs, Inc.
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *    - Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  *    - Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    - Neither the name of Bug Labs, Inc. nor the names of its contributors may be
  *      used to endorse or promote products derived from this software without
  *      specific prior written permission.
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
  *******************************************************************************/
 package com.buglabs.util.simplerestclient;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.net.HttpURLConnection;
 import java.net.URLEncoder;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import com.buglabs.util.Base64;
 
 /**
  * class for dealing RESTfully with HTTP Requests
  * 
  * Example Usage: HttpRequest req = new HttpRequest(myConnectionProvider)
  * HttpResponse resp = req.get("http://some.url")
  * System.out.println(resp.getString());
  * 
  * @author Brian
  * 
  *         Revisions 09-03-2008 AK added a Map header parameter to "put" and
  *         "post" to support http header
  * 
  * 
  */
 public class HTTPRequest {
 	////////////////////////////////////////////////  HTTP REQUEST METHODS	
 
 	private static final String HEADER_TYPE = "Content-Type";
 	private static final String HEADER_PARA = "Content-Disposition: form-data";
 	private static final String CONTENT_TYPE = "multipart/form-data";
 	private static final String LINE_ENDING = "\r\n";
 	private static final String BOUNDARY = "boundary=";
 	private static final String PARA_NAME = "name";
 	private static final String FILE_NAME = "filename";
 
 	private IConnectionProvider _connectionProvider;
 
 	/**
 	 * constructor where client provides connectionProvider
 	 * 
 	 */
 	public HTTPRequest(IConnectionProvider connectionProvider) {
 		_connectionProvider = connectionProvider;
 	}
 
 	/**
 	 * constructor that uses default connection provider
 	 */
 	public HTTPRequest() {
 		_connectionProvider = new DefaultConnectionProvider();
 	}
 
 	/**
 	 * Do an authenticated HTTP GET from url
 	 * 
 	 * @param url
 	 *            String URL to connect to
 	 * @return HttpURLConnection ready with response data
 	 */
 	public HTTPResponse get(String url) throws IOException {
 		HttpURLConnection conn = _connectionProvider.getConnection(url);
 		conn.setDoInput(true);
 		conn.setDoOutput(false);
 		return connect(conn);
 	}
 
 	/**
 	 * Do an HTTP POST to url
 	 * 
 	 * @param url
 	 *            String URL to connect to
 	 * @param data
 	 *            String data to post
 	 * @return HttpURLConnection ready with response data
 	 */
 	public HTTPResponse post(String url, String data) throws IOException {
 		return post(url, data, null);
 	}
 
 	/**
 	 * Do an HTTP POST to url w/ extra http headers
 	 * 
 	 * @param url
 	 * @param data
 	 * @param headers
 	 * @return
 	 * @throws IOException
 	 */
 	public HTTPResponse post(String url, String data, Map headers) throws IOException {
 		HttpURLConnection conn = _connectionProvider.getConnection(url);
 		if (headers != null) {
 			Iterator iterator = headers.keySet().iterator();
 			String key;
 			while (iterator.hasNext()) {
 				key = iterator.next().toString();
 				conn.setRequestProperty(key, headers.get(key).toString());
 			}
 		}
 		conn.setDoOutput(true);
 		OutputStreamWriter osr = new OutputStreamWriter(conn.getOutputStream());
 		osr.write(data);
 		osr.flush();
 		osr.close();
 		return connect(conn);
 	}
 
 	/**
 	 * Do an HTTP POST to url
 	 * 
 	 * @param url
 	 *            String URL to connect to
 	 * @param stream
 	 *            InputStream data to post
 	 * @return HttpURLConnection ready with response data
 	 */
 	public HTTPResponse post(String url, InputStream stream) throws IOException {
 		byte[] buff = streamToByteArray(stream);
 		String data = Base64.encodeBytes(buff);
 		return post(url, data);
 	}
 
 	/**
 	 * Posts a Map of key, value pair properties, like a web form
 	 * 
 	 * @param url
 	 * @param properties
 	 * @return
 	 * @throws IOException
 	 */
 	public HTTPResponse post(String url, Map properties) throws IOException {
 		String data = propertyString(properties);
 		HashMap headers = new HashMap();
 		headers.put("Content-Type", "application/x-www-form-urlencoded");
 		return post(url, data, headers);
 	}
	
	/**
	 * Posts a Map of key, value pair properties, like a web form
	 * 
	 * @param url
	 * @param properties
	 * @return
	 * @throws IOException
	 */
	public HTTPResponse post(String url, Map properties, Map headers) throws IOException {
		String data = propertyString(properties);		
		headers.put("Content-Type", "application/x-www-form-urlencoded");
		return post(url, data, headers);
	}
 
 	/**
 	 * Post byte data to a url
 	 * 
 	 * @param url
 	 * @param data
 	 * @return
 	 * @throws IOException
 	 */
 	public HTTPResponse post(String url, byte[] data) throws IOException {
 		HttpURLConnection conn = _connectionProvider.getConnection(url);
 		conn.setRequestProperty("Content-Length", String.valueOf(data.length));
 		conn.setRequestMethod("POST");
 		conn.setDoOutput(true);
 		OutputStream os = conn.getOutputStream();
 		os.write(data);
 		return connect(conn);
 	}
 
 	/**
 	 * Does a multipart post which is different than a regular post mostly use
 	 * this one if you're posting files
 	 * 
 	 * @param url
 	 * @param parameters
 	 *            Key-Value pairs in map. Keys are always string. Values can be
 	 *            string or IFormFile
 	 * @param properties
 	 * @return
 	 */
 	public HTTPResponse postMultipart(String url, Map parameters) throws IOException {
 		HttpURLConnection conn = _connectionProvider.getConnection(url);
 		conn.setRequestMethod("POST");
 		String boundary = createMultipartBoundary();
 		conn.setRequestProperty(HEADER_TYPE, CONTENT_TYPE + "; " + BOUNDARY + boundary);
 		conn.setDoOutput(true);
 
 		// write things out to connection
 		OutputStream os = conn.getOutputStream();
 
 		// add parameters
 		Object[] elems = parameters.keySet().toArray();
 		StringBuffer buf; // lil helper
 		IFormFile file;
 		for (int i = 0; i < elems.length; i++) {
 			String key = (String) elems[i];
 			Object obj = parameters.get(key);
 			//System.out.println("--" + key);
 
 			buf = new StringBuffer();
 			if (obj instanceof IFormFile) {
 				file = (IFormFile) obj;
 				buf.append("--" + boundary + LINE_ENDING);
 				buf.append(HEADER_PARA);
 				buf.append("; " + PARA_NAME + "=\"" + key + "\"");
 				buf.append("; " + FILE_NAME + "=\"" + file.getFilename() + "\"" + LINE_ENDING);
 				buf.append(HEADER_TYPE + ": " + file.getContentType() + ";");
 				buf.append(LINE_ENDING);
 				buf.append(LINE_ENDING);
 				os.write(buf.toString().getBytes());
 				os.write(file.getBytes());
 			} else if (obj != null) {
 				buf.append("--" + boundary + LINE_ENDING);
 				buf.append(HEADER_PARA);
 				buf.append("; " + PARA_NAME + "=\"" + key + "\"");
 				buf.append(LINE_ENDING);
 				buf.append(LINE_ENDING);
 				buf.append(obj.toString());
 				os.write(buf.toString().getBytes());
 			}
 			os.write(LINE_ENDING.getBytes());
 		}
 		os.write(("--" + boundary + "--" + LINE_ENDING).getBytes());
 		return connect(conn);
 	}
 
 	/**
 	 * Do an HTTP PUT to url
 	 * 
 	 * @param url
 	 *            String URL to connect to
 	 * @param data
 	 *            String data to post
 	 * @return HttpURLConnection ready with response data
 	 */
 	public HTTPResponse put(String url, String data) throws IOException {
 		return put(url, data, null);
 	}
 
 	/**
 	 * Do an HTTP PUT to url with extra headers
 	 * 
 	 * @param url
 	 * @param data
 	 * @param headers
 	 * @return
 	 * @throws IOException
 	 */
 	public HTTPResponse put(String url, String data, Map headers) throws IOException {
 		HttpURLConnection connection = _connectionProvider.getConnection(url);
 		if (headers != null) {
 			Iterator iterator = headers.keySet().iterator();
 			String key;
 			while (iterator.hasNext()) {
 				key = iterator.next().toString();
 				connection.setRequestProperty(key, headers.get(key).toString());
 			}
 		}
 		connection.setDoOutput(true);
 		connection.setRequestMethod("PUT");
 		OutputStreamWriter osr = new OutputStreamWriter(connection.getOutputStream());
 		osr.write(data);
 		osr.flush();
 		osr.close();
 		return connect(connection);
 	}
 
 	/**
 	 * Do an HTTP PUT to url
 	 * 
 	 * @param url
 	 *            String URL to connect to
 	 * @param stream
 	 *            InputStream data to put
 	 * @return HttpURLConnection ready with response data
 	 */
 	public HTTPResponse put(String url, InputStream stream) throws IOException {
 		byte[] buff = streamToByteArray(stream);
 		String data = Base64.encodeBytes(buff);
 		return put(url, data);
 	}
 
 	/**
 	 * Do an HTTP DELETE to url
 	 * 
 	 * @param url
 	 * @return
 	 * @throws IOException
 	 */
 	public HTTPResponse delete(String url) throws IOException {
 		HttpURLConnection connection = _connectionProvider.getConnection(url);
 		connection.setDoInput(true);
 		connection.setRequestMethod("DELETE");
 		return connect(connection);
 	}
 
 	/**
 	 * Puts a Map of key, value pair properties, like a web form
 	 * 
 	 * @param url
 	 * @param properties
 	 * @return
 	 * @throws IOException
 	 */
 	public HTTPResponse put(String url, Map properties) throws IOException {
 		String data = propertyString(properties);
 		HashMap headers = new HashMap();
 		headers.put("Content-Type", "application/x-www-form-urlencoded");
 		return put(url, data, headers);
 	}
 
 	/**
 	 * Do an HTTP HEAD to url
 	 * 
 	 * @param url
 	 *            String URL to connect to
 	 * @return HttpURLConnection ready with response data
 	 */
 	public HTTPResponse head(String url) throws IOException {
 		HttpURLConnection connection = _connectionProvider.getConnection(url);
 		connection.setDoOutput(true);
 		connection.setRequestMethod("HEAD");
 		return connect(connection);
 	}
 
 	////////////////////////////////////////////////////////////// THESE HELP
 
 	/**
 	 * Connect to server, check the status, and return the new HTTPResponse
 	 */
 	private HTTPResponse connect(HttpURLConnection connection) throws HTTPException, IOException {
 		HTTPResponse response = new HTTPResponse(connection);
 		response.checkStatus();
 		return response;
 	}
 
 	/**
 	 * A simple helper function
 	 * 
 	 * @param in
 	 *            InputStream to turn into a byte array
 	 * @return byte array (byte[]) w/ contents of input stream
 	 */
 	public static byte[] streamToByteArray(InputStream in) throws IOException {
 		ByteArrayOutputStream os = new ByteArrayOutputStream();
 		int read = 0;
 		byte[] buff = new byte[4096];
 		try {
 			while ((read = in.read(buff)) > 0) {
 				os.write(buff, 0, read);
 			}
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		return os.toByteArray();
 	}
 
 	/**
 	 * turns a map into a key=value property string for sending to bugnet
 	 */
 	private static String propertyString(Map props) throws IOException {
 		String propstr = new String();
 		String key;
 		for (Iterator i = props.keySet().iterator(); i.hasNext();) {
 			key = (String) i.next();
 			propstr = propstr + URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode((String) props.get(key), "UTF-8");
 			if (i.hasNext()) {
 				propstr = propstr + "&";
 			}
 		}
 		return propstr;
 	}
 
 	/**
 	 * helper to create multipart form boundary
 	 * 
 	 * @return
 	 */
 	private static String createMultipartBoundary() {
 		StringBuffer buf = new StringBuffer();
 		buf.append("---------------------------");
 
 		for (int i = 0; i < 15; i++) {
 			double rand = Math.random() * 35;
 			if (rand < 10) {
 				buf.append((int) rand);
 			} else {
 				int ascii = 87 + (int) rand;
 				char symbol = (char) ascii;
 				buf.append(symbol);
 			}
 		}
 		return buf.toString();
 	}
 
 }
