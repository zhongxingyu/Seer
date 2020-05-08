 package com.papagiannis.tuberun.fetchers;
 
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 
 import android.os.AsyncTask;
 import android.util.Log;
 
 import com.papagiannis.tuberun.LinePresentation;
 import com.papagiannis.tuberun.LineType;
 
 public class DeparturesDLRFetcher extends DeparturesFetcher {
 	private static final long serialVersionUID = 1L;
 	public static final String none_msg = "";
 	private AtomicBoolean isFirst = new AtomicBoolean(true);
 	private Date last_update = new Date();
 	protected int update_counter = 0;
 	protected String line, station_code, station_nice;
 	protected transient RequestTask task;
 	protected XMLDeserialiserTask deserialiserTask;
 	private String error = "";
 
 	protected ArrayList<HashMap<String, String>> departures = new ArrayList<HashMap<String, String>>();
 
 	public DeparturesDLRFetcher(LineType line, String stationcode,
 			String stationnice) {
 		super(line, stationcode, stationnice);
 		this.line = LinePresentation.getDeparturesRespresentation(line);
 		station_code = stationcode;
 		station_nice = stationnice;
 	}
 
 	@Override
 	public void update() {
 		boolean first = isFirst.compareAndSet(true, false);
 		if (!first)
 			return; // only one at a time
 		departures.clear();
 		error = "";
 		String request_query = "http://www.dlrlondon.co.uk/xml/mobile/"
 				+ station_code + ".xml";
 		task=new RequestTask(new HttpCallback() {
 			public void onReturn(String s) {
 				httpCallback(s);
 			}
 		});
 		task.execute(request_query);
 	}
 
 	private void httpCallback(String response) {
 		if (response == null || response.equals("")) {
 			error = "The DLR TFL server did not respond to your request (4)";
 			notifyClients();
 		}
 		deserialiserTask = new XMLDeserialiserTask();
 		deserialiserTask.execute(response);
 
 	}
 
 	private class XMLDeserialiserTask extends
 			AsyncTask<String, Integer, ArrayList<HashMap<String, String>>> {
 		@Override
 		protected ArrayList<HashMap<String, String>> doInBackground(
 				String... params) {
 			ArrayList<HashMap<String, String>> res;
 			try {
 				res = parseXMLResponse(params[0]);
 				int i = res.size();
 			} catch (Exception e) {
 				String s = e.toString();
 				s = s + s;
 				res = new ArrayList<HashMap<String, String>>();
 			}
 			return res;
 		}
 
 		private ArrayList<HashMap<String, String>> parseXMLResponse(
 				String response) {
 			ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
 			int platform_number = 0;
 			if (response == null || response.equals(""))
 				return result;
 			try {
 				response = response
 						.substring("﻿<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
 								.length() + 2);
 				DocumentBuilderFactory factory = DocumentBuilderFactory
 						.newInstance();
 				DocumentBuilder builder = factory.newDocumentBuilder();
 				Document dom = builder.parse(new InputSource(new StringReader(
 						response)));
 				Element root = dom.getDocumentElement();
 				NodeList ttBoxList = root.getChildNodes();
 				if (ttBoxList != null && ttBoxList.getLength() > 0) {
 					for (int i = 0; i < ttBoxList.getLength(); i++) {
 						Node ttBox = ttBoxList.item(i);
 
 						String nameString = ttBox.getNodeName();
 						if (nameString.equalsIgnoreCase("#text"))
 							continue;
 						NodeList ttBoxPlatformList = ttBox.getChildNodes();
 						if (ttBoxPlatformList.getLength() < 2)
 							continue;
 
 						Node middlePlaform = null;
 						for (int k = 0; k < ttBoxPlatformList.getLength(); k++) {
 							Node child = ttBoxPlatformList.item(k);
 							nameString = child.getNodeName();
 							if (nameString.equalsIgnoreCase("#text"))
 								continue;
 
 							NamedNodeMap attrs = child.getAttributes();
 							if (attrs == null)
 								continue;
 
 							Node id_node = attrs.getNamedItem("id");
 							String id = id_node.getNodeValue();
 							if (id.equalsIgnoreCase("platformmiddle")) {
 								middlePlaform = child;
 								break;
 							}
 						}
 						nameString = middlePlaform.getNodeName();
 						if (nameString.equalsIgnoreCase("#text"))
 							continue;
 
 						NodeList divList = middlePlaform.getChildNodes();
 
 						HashMap<String, String> train = new HashMap<String, String>();
 						for (int j = 0; j < divList.getLength(); j++) {
 							Node div = divList.item(j);
 							nameString = div.getNodeName();
 							if (nameString.equalsIgnoreCase("#text"))
 								continue;
 
 							NamedNodeMap att = div.getAttributes();
 							Node id = att.getNamedItem("id");
 							nameString = id.getNodeName();
 							if (nameString.equalsIgnoreCase("#text"))
 								continue;
 							String id_value = id.getNodeValue();
 
 							if (id_value.equalsIgnoreCase("line1")) {
 								String data = div.getTextContent();
 								train = decodeLine(data);
 								if (train==null) continue;
 								platform_number++;
 								train.put("platform", "Platform "
 										+ platform_number);
 								result.add(train);
 							} else if (id_value.equalsIgnoreCase("line23")) {
 								String data = div.getTextContent();
 								if (data == null)
 									continue;
 								String[] tokens = data.split("\n\n");
 								for (String token : tokens) {
 									train = decodeLine(token);
 									if (train==null) continue;
 									train.put("platform", "Platform "
 											+ platform_number);
 									result.add(train);
 								}
 							}
 
 						}
 
 					}
 				}
 			} catch (Exception e) {
 				// This should never happen
 				String s = e.toString();
 				Log.w("DLR Fetcher", e);
 			}
 			return result;
 		}
 
 		private HashMap<String, String> decodeLine(String line) {
 			line = cleanHTML(line);
 			return getInfo(line);
 		}
 
 		private String cleanHTML(String s) {
 			String res = s.replace("<p>", " ");
 			res = res.replace("</p>", " ");
 			res = res.replace("\n", " ");
 			res = res.replace("\t", " ");
 			res = res.replace("\r", " ");
 			res = res.replace("<br>", " ");
 			res = res.replace("<br/>", " ");
 			res = res.replace("&nbsp;", " ");
 			res = res.trim();
 			String[] tokens = res.split(" ");
 			res = "";
 			for (String ss : tokens) {
 				if (ss != null && !ss.equals(""))
 					res += ss + " ";
 			}
 			return res;
 		}
 
 		private HashMap<String, String> getInfo(String tr1) {
 			HashMap<String, String> train1 = new HashMap<String, String>();
 			String alphaS = "a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|z|y|z";
 			String alphaC = "A|B|C|D|E|F|G|H|I|J|K|L|M|N|O|P|Q|R|S|T|U|V|W|X|Y|Z";
 			String dec = "(1|2|3|4|5|6|7|8|9|0)";
 			String alpha = "(" + alphaC + "|" + alphaS + ")";
 			Pattern pat = Pattern.compile("[1-3]( )((([^0-9])+[0-9]+.*)| *)");
 			Matcher m = pat.matcher(tr1);
 			String dest = "";
 			String time = "";
 			String where = "";
 			if (m.find() && m.start() != -1) {
 				Boolean first_nums = false;
 				Boolean seond_nums = false;
 
 				for (int j = 0; j < tr1.length(); j++) {
 					char c = tr1.charAt(j);
 					if (first_nums == false) {
 						if (!dec.contains("" + c))
 							continue;
 						else
 							first_nums = true;
 					} else if (seond_nums == false) {
 						if (!dec.contains("" + c))
 							dest = dest + c;
 						else {
 							time = time + c;
 							seond_nums = true;
 						}
 					} else {
 						if (!dec.contains("" + c))
 							break;
 						else
 							time = time + c;
 					}
 				}
 				dest = dest.trim();
 
 				if (time != "")
 					time = time + " min";
 				where = none_msg;
 				dest = toCamelCase(dest);
 
 				String destn = "";
 				Boolean white = true;
 				for (int j = 0; j < dest.length(); j++) {
 					char c = tr1.charAt(j);
 					if (white) {
 						white = false;
 						destn += String.valueOf(c).toUpperCase();
 					} else {
 						if (c == ' ')
 							white = true;
 						destn += c;
 					}
 				}
 				if (destn.length() > 3) {
 					train1.put("time", time);
 					train1.put("destination", dest);
 					train1.put("position", where);
 					return train1;
 				} else
 					return null;
 			} else {
 				return null;
 			}
 
 		}
 		
 		private String toCamelCase(String s) {
 			boolean previous_whitespace = true;
 			String result = "";
 			for (int i = 0; i < s.length(); i++) {
 				String next = s.substring(i, i + 1);
 				if (previous_whitespace) {
 					result += next.toUpperCase();
 				} else {
 					result += next.toLowerCase();
 				}
 				previous_whitespace=next.equals(" ");
 			}
 			return result;
 		}
 
 		protected void onPostExecute(ArrayList<HashMap<String, String>> result) {
 			if (isCancelled())
 				return;
 			last_update = new Date();
 			departures = result;
 			notifyClients();
 			isFirst.set(true);
 		}
 	}
 
 	@Override
 	public Date getUpdateTime() {
 		return new Date(last_update.getTime());
 	}
 
 	public HashMap<String, ArrayList<HashMap<String, String>>> getDepartures() {
 		HashMap<String, ArrayList<HashMap<String, String>>> categorised = new HashMap<String, ArrayList<HashMap<String, String>>>();
 		for (HashMap<String, String> train : departures) {
			if (!categorised.containsKey(train.get("platform"))) {
				categorised.put(train.get("platform"),
 						new ArrayList<HashMap<String, String>>());
 			}
 			categorised.get(train.get("platform")).add(train);
 		}
 		return categorised;
 
 	}
 
 	public ArrayList<HashMap<String, String>> getDepartures(String platform) {
 		ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
 		for (HashMap<String, String> d : departures) {
 			if (d.get("platform").equalsIgnoreCase(platform)) {
 				result.add(d);
 			}
 		}
 		return result;
 	}
 	
 	@Override
     public void abort() {
 		isFirst.set(true);
     	if (task!=null) task.cancel(true);
     	if (deserialiserTask!=null) deserialiserTask.cancel(true);
     }
 
 }
