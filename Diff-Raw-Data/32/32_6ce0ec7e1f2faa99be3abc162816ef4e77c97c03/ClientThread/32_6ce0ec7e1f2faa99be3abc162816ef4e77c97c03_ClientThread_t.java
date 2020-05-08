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
 
 import static org.oobium.utils.coercion.TypeCoercer.coerce;
 import static org.oobium.http.constants.RequestType.POST;
 import static org.oobium.http.constants.RequestType.PUT;
 import static org.oobium.utils.StringUtils.attrEncode;
 import static org.oobium.utils.StringUtils.attrsEncode;
 
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.OutputStreamWriter;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.oobium.http.constants.Header;
 import org.oobium.http.constants.RequestType;
 import org.oobium.utils.StringUtils;
 
 public class ClientThread extends Thread {
 
 	private Client client;
 	
 	private RequestType type;
 	private String path;
 	private Map<String, List<String>> headers;
 	private Map<String, ?> parameters;
 	private String boundary;
 	private ClientCallback callback;
 	
 	private ClientResponse response;
 
 	ClientThread(Client client, RequestType type, String path, Map<String, List<String>> headers, Map<String, ?> parameters) {
		if(path == null) {
			path = "/";
		} else if(!path.startsWith("/")) {
 			path = "/" + path;
 		}
 		this.client = client;
 		this.type = type;
 		this.path = path;
 		this.parameters = parameters;
 		
 		this.headers = (headers != null) ? new LinkedHashMap<String, List<String>>(headers) : new LinkedHashMap<String, List<String>>();
 	}
 	
 	public void addHeader(String key, String value) {
 		List<String> list = headers.get(key);
 		if(list == null) {
 			list = new ArrayList<String>();
 			headers.put(key, list);
 		}
 		list.add(value);
 	}
 	
 	public String getBoundary() {
 		return boundary;
 	}
 	
 	public ClientResponse getResponse() {
 		return response;
 	}
 
 	private URL getURL(Map<String, String> parameters) throws MalformedURLException {
 		String path = this.path;
 		if(parameters != null && !parameters.isEmpty()) {
 			switch(type) {
 			case GET:
 			case HEAD:
 			case DELETE:
 				path = path + ((path.indexOf('?') == -1) ? "?" : "&") + StringUtils.attrsEncode(parameters);
 				break;
 			}
 		}
 		if(client.url != null) {
 			return new URL(client.url.getProtocol(), client.url.getHost(), client.url.getPort(), path);
 		} else {
 			return new URL(client.protocol, client.host, client.port, path);
 		}
 	}
 
 	@Override
 	public void run() {
 		try {
 			Map<String, String> params = null;
 			Map<String, MessagePart> parts = null;
 			if(parameters != null) {
 				params = new LinkedHashMap<String, String>();
 				for(Entry<String, ?> entry : parameters.entrySet()) {
 					String key = entry.getKey();
 					Object val = entry.getValue();
 					if(val instanceof File) {
 						File file = (File) val;
 						MessagePart part = new MessagePart(new FileInputStream(file));
 						part.put("filename", file.getName());
 						val = part;
 					} else if(val instanceof InputStream) {
 						val = new MessagePart((InputStream) val);
 					}
 					if(val instanceof MessagePart) {
 						if(parts == null) {
 							parts = new LinkedHashMap<String, MessagePart>();
 						}
 						parts.put(key, (MessagePart) val);
 					} else {
 						params.put(key, coerce(val, String.class));
 					}
 				}
 			}
 			
 			URL url = getURL(params);
 	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 	        conn.setRequestMethod(type.name());
 	        setHeaders(conn);
 	        if(type == POST || type == PUT) {
 	        	conn.setDoOutput(true);
 	        	if(parts == null) {
 			        OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
 			        out.write(attrsEncode(params));
 			        out.flush();
 			        out.close();
 	        	} else {
 	        		String boundary = this.boundary;
 	        		if(boundary == null) {
 		        		List<String> ctypes = headers.get(Header.CONTENT_TYPE.key());
 		        		if(ctypes != null && !ctypes.isEmpty()) {
 		        			for(String type : ctypes) {
 		        				String[] sa = type.split("\\s*;\\s*");
 		        				for(int i = 0; i < sa.length; i++) {
 		        					if(sa[i].startsWith("boundary=")) {
 		        						if(sa[i].length() > 9) {
 		        							boundary = (sa[i].substring(9));
 		        						}
 		        						break;
 		        					}
 		        				}
 		        				if(boundary != null) {
 		        					break;
 		        				}
 		        			}
 		        		}
 		        		if(boundary == null) {
 		        			boundary = "---------------------------OobiumBoundary";
 		        		}
 	        		}
 	        		conn.setRequestProperty(Header.CONTENT_TYPE.key(), "multipart/form-data; boundary=" + boundary);
 	                DataOutputStream out = new DataOutputStream(conn.getOutputStream());
 	                out.writeBytes("--" + boundary + "\r\n");
 	                for(Entry<String, String> entry : params.entrySet()) {
 	                	writeParam(out, entry.getKey(), entry.getValue(), boundary);
 	                }
 	                for(Entry<String, MessagePart> entry : parts.entrySet()) {
 	                	writePart(out, entry.getKey(), entry.getValue(), boundary);
 	                }
 	                out.flush();
 	                out.close();
 	        	}
 	        }
 	        response = new ClientResponse(conn);
 		} catch(Exception e) {
 	        response = new ClientResponse(e);
 		} finally {
 			if(callback != null) {
 				callback.handleCallback(response);
 			}
 		}
 	}
 	
 	public void setBoundary(String boundary) {
 		this.boundary = boundary;
 	}
 	
 	public void setCallback(ClientCallback callback) {
 		this.callback = callback;
 	}
 	
     private void setHeaders(URLConnection connection) {
 		connection.addRequestProperty(Header.USER_AGENT.key(), "Oobium Client");
 		for(Entry<String, List<String>> entry : headers.entrySet()) {
 			for(String value : entry.getValue()) {
 				connection.addRequestProperty(entry.getKey(), value);
 			}
 		}
 	}
  	    
 	private void writeParam(DataOutputStream out, String name, String value, String boundary) throws Exception {
         out.writeBytes("content-disposition: form-data; name=\"" + name + "\"\r\n\r\n");
         out.writeBytes(value);
         out.writeBytes("\r\n" + "--" + boundary + "\r\n");
     }
 	
 	private void writePart(DataOutputStream out, String name, MessagePart part, String boundary) throws Exception {
 		StringBuilder sb = new StringBuilder();
 		sb.append("content-disposition: form-data; name=\"").append(name).append('"');
 		for(Entry<String, String> entry : part.getParameters().entrySet()) {
 			sb.append(';').append(attrEncode(entry.getKey(), entry.getValue())).append('"');
 		}
 		sb.append("\r\ncontent-type: audio/wav\r\n\r\n");
 		out.writeBytes(sb.toString());
 		InputStream in = part.getStream();
 		byte[] bytes = new byte[4*1024];
 		int read;
 		while((read = in.read(bytes)) > 0) {
 			out.write(bytes, 0, read);
 		}
 		in.close();
 		out.writeBytes("\r\n" + "--" + boundary + "\r\n");
     }
 	
 }
