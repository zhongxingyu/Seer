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
 package org.oobium.http.impl;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.oobium.http.constants.Header;
 
 public class Headers implements Iterable<String> {
 
 	private List<String> headerList;
 	private Map<String, List<String>> headerMap;
 	
 	public Headers() {
 		headerList = new ArrayList<String>();
 	}
 
 	public void add(Header name, String value) {
 		if(name != null && value != null) {
 			String header = name.key() + ":" + value;
 			if(name == Header.STATUS) {
 				headerList.add(0, header);
 			} else {
 				headerList.add(header);
 			}
 			clearMap();
 		}
 	}
 	
 	public void add(String header) {
 		if(headerList.isEmpty() || header.contains(":")) {
 			headerList.add(header);
 			clearMap();
 		}
 	}
 
 	private void clearMap() {
 		if(headerMap != null) {
 			headerMap.clear();
 			headerMap = null;
 		}
 	}
 	
 	/**
 	 * Get the header corresponding to the given name.  If there are more than one
 	 * headers for the given name, return the first only.  If there are no headers
 	 * for the give name, return null;
 	 * @param name
 	 * @return the first header for the given name, or null if none exist.
 	 */
 	public String get(Header name) {
 		List<String> l = getMap().get(name.lowerKey());
 		if(l != null && !l.isEmpty()) {
 			return l.get(0);
 		}
 		return null;
 	}
 
 	/**
 	 * Get the header corresponding to the given index in the array of headers.  If the index
 	 * is out of bounds, null will be returned.
 	 * @param index
 	 * @return the header for the given index, or null if the index is not valid.
 	 */
 	public String get(int index) {
 		if(index >= 0 && index < headerList.size()) {
 			return headerList.get(index).split(":", 2)[1].trim();
 		}
 		return null;
 	}
 
 	/**
 	 * Get a list of all the headers corresponding to the given name.  If there are none, an
 	 * empty list is returned.
 	 * @param name
 	 * @return a list of headers for the given name; will never be null;
 	 */
 	public List<String> getAll(Header name) {
 		List<String> l = getMap().get(name.lowerKey());
 		if(l != null) {
 			return l;
 		}
 		return new ArrayList<String>(0);
 	}
 
 	private Map<String, List<String>> getMap() {
 		if(headerMap == null) {
 			headerMap = new HashMap<String, List<String>>();
 			for(String header : headerList) {
 				String name;
 				String value;
 				String[] sa = header.split(":", 2);
 				if(sa.length == 1) {
 					name = null;
 					value = sa[0].trim();
 				} else {
 					name = sa[0].trim().toLowerCase();
 					value = sa[1].trim();
 				}
 				if(!headerMap.containsKey(name)) {
 					headerMap.put(name, new ArrayList<String>()); 
 				}
 				headerMap.get(name).add(value);
 			}
 		}
 		return headerMap;
 	}
 	
 	/**
 	 * Get the name of the header corresponding to the given index in the array of headers.  If the index
 	 * is out of bounds, null will be returned.
 	 * @param index
 	 * @return the name of the header for the given index, or null if the index is not valid.
 	 */
 	public String getName(int index) {
 		if(index >= 0 && index < headerList.size()) {
 			return headerList.get(index).split(":", 2)[0].trim();
 		}
 		return null;
 	}
 
 	/**
 	 * Return true if there is a header corresponding to the given name, false otherwise.
 	 * @param name
 	 * @return true if there is a header corresponding to the given name, false otherwise.
 	 */
 	public boolean has(Header name) {
 		return getMap().containsKey(name.lowerKey());
 	}
 
 	public boolean hasContentType() {
 		if(headerMap != null) {
 			return has(Header.CONTENT_TYPE);
 		}
 		String key = Header.CONTENT_TYPE.lowerKey();
 		for(String header : headerList) {
 			int ix = header.indexOf(':');
 			if(ix != -1 && key.equals(header.substring(0, ix).toLowerCase())) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public boolean hasStatusLine() {
 		return !headerList.isEmpty() && !headerList.get(0).contains(":");
 	}
 	
 	public boolean isEmpty() {
 		return headerList.isEmpty();
 	}
 	
 	public Iterator<String> iterator() {
 		return headerList.iterator();
 	}
 	
	public void set(Header name, String value) {
		if(name != null && value != null) {
			String key = name.lowerKey();
			for(int i = 0; i < headerList.size(); i++) {
				String s = headerList.get(i);
				int ix = s.indexOf(':');
				if(ix != -1 && key.equals(s.substring(0, ix).toLowerCase())) {
					headerList.set(i, name.key() + ":" + value);
					clearMap();
					return;
				}
 			}
			// not found, add
			add(name, value);
		}		
 	}
 	
 	public void setAll(List<String> headers) {
 		headerList.clear();
 		headerList = new ArrayList<String>(headers);
 		clearMap();
 	}
 	
 	public int size() {
 		return headerList.size();
 	}
 	
 	public String[] toArray() {
 		return headerList.toArray(new String[headerList.size()]);
 	}
 	
 	@Override
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		for(String header : headerList) {
 			sb.append(header).append("\r\n");
 		}
 		return sb.toString();
 	}
 	
 }
