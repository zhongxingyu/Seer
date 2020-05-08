 /*******************************************************************************
  * Copyright (c) 2010 Oobium, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
  ******************************************************************************/
 package org.oobium.client;
 
 import static org.oobium.http.constants.RequestType.DELETE;
 import static org.oobium.http.constants.RequestType.GET;
 import static org.oobium.http.constants.RequestType.POST;
 import static org.oobium.http.constants.RequestType.PUT;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.oobium.http.constants.ContentType;
 import org.oobium.http.constants.Header;
 import org.oobium.http.constants.RequestType;
 
 public class Client {
 
 	public static Client client(int port) {
 		return new Client("localhost", port);
 	}
 
 	public static Client client(String url) throws MalformedURLException {
 		return new Client(url);
 	}
 	
 	public static Client client(String host, int port) {
 		return new Client(host, port);
 	}
 	
 	final URL url;
 	final String protocol;
 	final String host;
 	final int port;
 	private Map<String, List<String>> headers;
 
 	public Client(String url) throws MalformedURLException {
 		if(url != null && url.length() > 0 && url.indexOf("://") == -1) {
 			url = "http://" + url;
 		}
 		this.protocol = null;
 		this.host = null;
 		this.port = -1;
 		this.url = new URL(url);
 
 		this.headers = new LinkedHashMap<String, List<String>>();
 	}
 	
 	public Client(String host, int port) {
 		this.protocol = "http";
 		this.host = host;
 		this.port = port;
 		this.url = null;
 	}
 
 	public void addHeader(Header header, String value) {
 		addHeader(header.key(), value);
 	}
 	
 	public void addHeader(String key, String value) {
 		List<String> list = headers.get(key);
 		if(list == null) {
 			list = new ArrayList<String>();
 			headers.put(key, list);
 		}
 		list.add(value);
 	}
 
 	public void aDelete() {
		asyncRequest(DELETE, url.getPath(), null, null);
 	}
 	
 	public void aDelete(String path) {
 		asyncRequest(DELETE, path, null, null);
 	}
 	
 	public void aDelete(String path, Map<String, ?> parameters) {
 		asyncRequest(DELETE, path, parameters, null);
 	}
 	
 	public void aGet(String path) {
 		asyncRequest(DELETE, path, null, null);
 	}
 	
 	public void aGet(String path, Map<String, ?> parameters) {
 		asyncRequest(DELETE, path, parameters, null);
 	}
 	
 	public void aPost(Map<String, ?> parameters) {
		asyncRequest(POST, url.getPath(), parameters, null);
 	}
 	
 	public void aPost(Map<String, ?> parameters, ClientCallback callback) {
		asyncRequest(POST, url.getPath(), parameters, callback);
 	}
 	
 	public void aPost(String path) {
 		asyncRequest(POST, path, null, null);
 	}
 	
 	public void aPost(String path, Map<String, ?> parameters) {
 		asyncRequest(POST, path, parameters, null);
 	}
 	
 	public void aPut(String path) {
 		asyncRequest(PUT, path, null, null);
 	}
 	
 	public void aPut(String path, Map<String, ?> parameters) {
 		asyncRequest(PUT, path, parameters, null);
 	}
 	
 	public void asyncRequest(RequestType type, String path, ClientCallback callback) {
 		asyncRequest(type, path, null, callback);
 	}
 	
 	public void asyncRequest(RequestType type, String path, Map<String, ?> parameters, ClientCallback callback) {
 		ClientThread c = new ClientThread(this, type, path, headers, parameters);
 		c.setCallback(callback);
 		c.start();
 	}
 
 	public ClientResponse delete() {
		return syncRequest(DELETE, url.getPath());
 	}
 
 	public ClientResponse delete(String path) {
 		return syncRequest(DELETE, path);
 	}
 
 	public void delete(String path, ClientCallback callback) {
 		asyncRequest(DELETE, path, null, callback);
 	}
 	
 	public ClientResponse delete(String path, Map<String, ?> parameters) {
 		return syncRequest(DELETE, path, parameters);
 	}
 	
 	public void delete(String path, Map<String, ?> parameters, ClientCallback callback) {
 		asyncRequest(DELETE, path, parameters, callback);
 	}
 	
 	public ClientResponse get() {
 		return get(new HashMap<String, Object>(0));
 	}
 	
 	public ClientResponse get(Map<String, ?> parameters) {
		return syncRequest(GET, url.getPath(), parameters);
 	}
 	
 	public ClientResponse get(String path) {
 		return syncRequest(GET, path, null);
 	}
 
 	public void get(String path, ClientCallback callback) {
 		asyncRequest(GET, path, null, callback);
 	}
 
 	public ClientResponse get(String path, Map<String, ?> parameters) {
 		return syncRequest(GET, path, parameters);
 	}
 	
 	public void get(String path, Map<String, ?> parameters, ClientCallback callback) {
 		asyncRequest(GET, path, parameters, callback);
 	}
 	
 	public String getHost() {
 		return (url != null) ? url.getHost() : host;
 	}
 	
 	public String getPath() {
 		return (url != null) ? url.getPath() : null;
 	}
 
 	public int getPort() {
 		return (url != null) ? url.getPort() : port;
 	}
 	
 	public String getProtocol() {
 		return (url != null) ? url.getProtocol() : protocol;
 	}
 	
 	public URL getUrl() {
 		try {
 			return (url != null) ? url : new URL(protocol, host, port, null);
 		} catch(MalformedURLException e) {
 			return null;
 		}
 	}
 
 	public ClientResponse post(Map<String, ?> parameters) {
		return syncRequest(POST, url.getPath(), parameters);
 	}
 
 	public ClientResponse post(String path) {
 		return syncRequest(POST, path);
 	}
 	
 	public void post(String path, ClientCallback callback) {
 		asyncRequest(POST, path, null, callback);
 	}
 	
 	public ClientResponse post(String path, Map<String, ?> parameters) {
 		return syncRequest(POST, path, parameters);
 	}
 	
 	public void post(String path, Map<String, ?> parameters, ClientCallback callback) {
 		asyncRequest(POST, path, parameters, callback);
 	}
 	
 	public ClientResponse put(String path) {
 		return syncRequest(PUT, path);
 	}
 	
 	public void put(String path, ClientCallback callback) {
 		asyncRequest(PUT, path, null, callback);
 	}
 	
 	public ClientResponse put(String path, Map<String, ?> parameters) {
 		return syncRequest(PUT, path, parameters);
 	}
 
 	public void put(String path, Map<String, ?> parameters, ClientCallback callback) {
 		asyncRequest(PUT, path, parameters, callback);
 	}
 	
 	public void setAccepts(ContentType...types) {
 		for(ContentType type : types) {
 			addHeader(Header.ACCEPT, type.getRequestProperty());
 		}
 	}
 	
 	public void setHeaders(String...headers) {
 		for(String header : headers) {
 			String[] sa = header.split("\\s*:\\s*", 2);
 			if(sa.length == 2) {
 				addHeader(sa[0], sa[1]);
 			}
 		}
 	}
 	
 	public ClientResponse syncRequest(RequestType type) {
		return syncRequest(type, url.getPath(), null);
 	}
 	
 	public ClientResponse syncRequest(RequestType type, Map<String, ?> parameters) {
		ClientThread c = new ClientThread(this, type, url.getPath(), headers, parameters);
 		c.run();
 		return c.getResponse();
 	}
 	
 	public ClientResponse syncRequest(RequestType type, String path) {
 		return syncRequest(type, path, null);
 	}
 
 	public ClientResponse syncRequest(RequestType type, String path, Map<String, ?> parameters) {
 		ClientThread c = new ClientThread(this, type, path, headers, parameters);
 		c.run();
 		return c.getResponse();
 	}
 
 	@Override
 	public String toString() {
 		if(url != null) return url.toString();
 		return protocol + "://" + host + ":" + port;
 	}
 	
 }
