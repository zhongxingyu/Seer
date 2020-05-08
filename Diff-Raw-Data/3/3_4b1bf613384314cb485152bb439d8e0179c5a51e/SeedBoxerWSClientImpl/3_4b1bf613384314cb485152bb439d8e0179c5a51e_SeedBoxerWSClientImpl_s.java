 /*******************************************************************************
  * SeedBoxerWSClientImpl.java
  * 
  * Copyright (c) 2012 SeedBoxer Team.
  * 
  * This file is part of Seedroid.
  * 
  * Seedroid is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Seedroid is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Seedroid.  If not, see <http ://www.gnu.org/licenses/>.
  ******************************************************************************/
 package com.seedboxer.seedroid.ws;
 
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.seedboxer.seedroid.tools.XMLfunctions;
 import com.seedboxer.seedroid.types.Item;
 
 public class SeedBoxerWSClientImpl implements SeedBoxerWSClient {
 
	private static final String RESPONSE_OK = "<response>\n<status>OK</status>\n</response>";
 	private static final String WS_PREFIX = "webservices/";
 	private static final String LIST_WS = WS_PREFIX + "downloads/list";
 	private static final String STATUS_WS = WS_PREFIX + "status";
 	private static final String REGISTER_DEVICE_WS = WS_PREFIX + "registerDevice";
 	private static final String PUT_DOWNLOAD_WS = WS_PREFIX + "downloads/put";
 
 	private String username;
 	private String password;
 	private String server;
 
 	public SeedBoxerWSClientImpl(String username, String password, String server) {
 		this.username = username;
 		this.password = password;
 		this.server = server;
 		if (!server.endsWith("/")) {
 			this.server = this.server + "/";
 		}
 	}
 
 	public List<Item> getItemsAvaibleForDownload() throws Exception {
 		String response = executeRESTWS(LIST_WS, Collections.<String, Object> emptyMap());
 
 		if (response == null) {
 			return null;
 		} else {
 			Document doc = XMLfunctions.XMLfromString(response);
 			Node nodes = doc.getElementsByTagName("files").item(0);
 			NodeList list = nodes.getChildNodes();
 			List<Item> items = new ArrayList<Item>();
 			for (int i = 0; i < list.getLength(); i++) {
 				Element e = (Element) list.item(i);
 				Item item = new Item();
 				item.setName(XMLfunctions.getElementValue(e.getChildNodes().item(1))); // name
 				items.add(item);
 			}
 			return items;
 		}
 	}
 
 	public boolean putToDownload(List<Item> toDownload) throws Exception {
 		Map<String, Object> params = new HashMap<String, Object>();
 		List<String> fileNames = new ArrayList<String>();
 		for (Item item : toDownload) {
 			fileNames.add(item.getName());
 		}
 		params.put("fileName", fileNames);
 
 		String response = executeRESTWS(PUT_DOWNLOAD_WS, params);
 		if (RESPONSE_OK.equals(response)) {
 			return true;
 		}
 		return false;
 	}
 
 	public List<Item> getStatusOfDownloads() throws Exception {
 		String response = executeRESTWS(STATUS_WS, Collections.<String, Object> emptyMap());
 
 		if (response == null) {
 			return null;
 		} else {
 			Document doc = XMLfunctions.XMLfromString(response);
 			Node nodes = doc.getElementsByTagName("downloads").item(0);
 			NodeList list = nodes.getChildNodes();
 			List<Item> items = new ArrayList<Item>();
 			for (int i = 0; i < list.getLength(); i++) {
 				Element e = (Element) list.item(i);
 
 				String name = XMLfunctions.getElementValue(e.getChildNodes().item(0));
 				String size = XMLfunctions.getElementValue(e.getChildNodes().item(1));
 				String transferred = XMLfunctions.getElementValue(e.getChildNodes().item(2));
 
 				Item item = new Item(name, Long.parseLong(size), Long.parseLong(transferred));
 				items.add(item);
 			}
 			return items;
 		}
 	}
 
 	private String executeRESTWS(String type, Map<String, Object> params) throws Exception {
 		RestClient client = new RestClient(server + type);
 
 		client.AddParam("username", username);
 		for (Map.Entry<String, Object> entry : params.entrySet()) {
 			addParam(client, entry);
 		}
 		//client.AddHeader("Authorization", "Basic aGFybGV5OnAycHJ1bHo=");
 
 		try {
 			client.Execute(RequestMethod.GET);
 			return client.getResponse();
 		} catch (Exception e) {
 			throw new Exception("Error contacting web service", e);
 		}
 	}
 
 	private void addParam(RestClient client, Map.Entry<String, Object> entry) {
 		Object value = entry.getValue();
 		if (value instanceof Collection<?>) {
 			for (Object item : (Collection<?>)value) {
 				client.AddParam(entry.getKey(), item.toString());
 			}
 		} else {
 			client.AddParam(entry.getKey(), value.toString());
 		}
 	}
 
 	public boolean registerDevice(String deviceId, String registrationId)
 			throws Exception {
 		Map<String, Object> params = new HashMap<String, Object>();
 		params.put("deviceId", deviceId);
 		params.put("registrationId", registrationId);
 		String response = executeRESTWS(REGISTER_DEVICE_WS, params);
 
 		if (RESPONSE_OK.equals(response)) {
 			return true;
 		}
 		return false;
 	}
 
 	public boolean unregisterDevice(String deviceId, String registrationId)
 			throws Exception {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 }
