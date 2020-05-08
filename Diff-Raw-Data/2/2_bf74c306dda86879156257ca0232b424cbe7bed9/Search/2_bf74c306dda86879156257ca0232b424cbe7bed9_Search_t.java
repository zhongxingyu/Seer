 package com.silverwzw.api.google;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.CountDownLatch;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.jsoup.Jsoup;
 
 import com.silverwzw.JSON.JSON;
 import com.silverwzw.JSON.JSON.JsonStringFormatException;
 
 public class Search extends com.silverwzw.api.AbstractSearch{
 	private int i = 10;
 	private Map<String,Integer> apiKey;
 	private String seID = "016567116349354999812:g_wwmaarfsa";
 	private boolean sync = false;
 	private boolean useGoogleApi = true;
 	private int ms = 1500;
 	private static Pattern extractQ, extractURL;
 	private String time = null;
 	
 	static {
 		extractQ = Pattern.compile("url\\?q=([^&]*)&");
 		extractURL = Pattern.compile("[\\?&]url=([^&]*)&"); 
 	}
 	
 	public Search(String queryString, String time) {
 		constructor(queryString, time);
 	}
 	
 	public Search(String queryString) {
 		constructor(queryString, null);
 	}
 	
 	private void constructor(String queryString, String time) {
 		this.time = time;
 		setSearchTerm(queryString);
 		apiKey = new HashMap<String,Integer>();
 		apiKey.put("AIzaSyAxdsUVjbxnEV9FAfmK_5M9a2spo-uFL9g", 100);
 	}
 	
 	final public void setSync(boolean isSync) {
 		sync = isSync;
 	}
 	
 	final public void useGoogleApi(boolean d) {
 		useGoogleApi = d;
 	}
 	
 	final public void setXGoogleApiEscapeTime(int timeInMS) {
 		ms = timeInMS;
 	}
 	
 	final public void setSearchTerm(String queryString) {
 		if (queryString == null) {
 			q = null;
 			return;
 		}
 		try {
 			q = java.net.URLEncoder.encode(queryString, "UTF-8").replaceAll(" ", "%20");
		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	final public void addApiKeyQuota(String key, int quota) {
 		if (apiKey.containsKey(key) && apiKey.get(key) != null) {
 			apiKey.put(key, apiKey.get(key) + quota);
 		} else {
 			apiKey.put(key,quota);
 		}
 	}
 	
 	final public void setSearchEngineID(String seID) {
 		this.seID = seID;
 	}
 	
 	final public void setResultPerPage (int resultNumPerPage) {
 		if (resultNumPerPage < 1 || resultNumPerPage > 99) {
 			System.err.println("Number of Results Per Page should greater than 1 and less than 99, use 30 instead");
 			i = 10;
 		} else {
 			i = resultNumPerPage;
 		}
 	}
 	
 	final private String getGoogleApiURL(int startIndex) {
 		String key = null;
 		int oquota = -99999;
 		for (Entry<String, Integer> en : apiKey.entrySet()) {
 			if (key == null || oquota <= en.getValue() ) {
 				oquota = en.getValue();
 				key = en.getKey();
 			}
 		}
 		apiKey.put(key, oquota - 1);
 		if (startIndex <= 1) {
 			return "https://www.googleapis.com/customsearch/v1?key=" + key + "&cx=" + seID + "&q=" + q + "&num=" + i +"&alt=json";
 		} else {
 			return "https://www.googleapis.com/customsearch/v1?key=" + key + "&cx=" + seID + "&q=" + q + "&num=" + i + "&start=" + startIndex + "&alt=json";
 		}
 	}
 	
 	final public List<String> asUrlStringList(int docNum) {
 		if (docNum < 1) {
 			System.err.println("Number of Documents should greater than 1, use 20 instead");
 			docNum = 20;
 		}
 		
 		int pageNum, cpagei;
 		List<String> uList;
 		
 		uList = new ArrayList<String>(docNum);
 		pageNum = (docNum%i == 0) ? (docNum / i) : (docNum / i +1);
 		
 
 		
 		if (useGoogleApi) {
 			JSON[] apiqpage;
 			apiqpage = new JSON[pageNum];
 			CountDownLatch threadSignal;
 			if (!sync) {
 				threadSignal = new CountDownLatch(pageNum);
 				
 				for (cpagei = 0; cpagei < pageNum; cpagei++) {
 					new Thread(new _GetGQPage(threadSignal,getGoogleApiURL(cpagei * i + 1), apiqpage, cpagei)).start();
 				}
 				try {
 					threadSignal.await();
 				} catch (InterruptedException e) {
 					throw new RuntimeException(e);
 				
 				}
 			} else {
 				for (cpagei = 0; cpagei < pageNum; cpagei++) {
 					new _GetGQPage(null, getGoogleApiURL(cpagei * i + 1), apiqpage, cpagei).run();
 				}
 			}
 			for (JSON json : apiqpage) {
 				if (json == null || json.get("error") != null) {
 					System.err.println("Query failed : " + ((json == null) ? "json == null" : "json.error != null"));
 					continue;
 				}
 				try {
 					for (Entry<String,JSON> el : json.get("items")) {
 						if (docNum > 0) {
 							String link = (String)el.getValue().get("link").toObject();
 								uList.add(link);
 								docNum --;
 						}
 						if (docNum <= 0) {
 							break;
 						}
 					}
 				} catch (RuntimeException ex) {
 					System.out.print(json.format());
 					throw ex;
 				}
 				if (docNum <= 0) {
 					break;
 				}
 			}
 		} else {
 			for (cpagei = 0; cpagei < pageNum; cpagei++) {
 				uList.addAll(xGoogleSearch(cpagei * i + 1));
 				try {
 					Thread.sleep(ms);
 				} catch (InterruptedException e) {
 					throw new RuntimeException(e);
 				}
 			}
 		}
 		
 		return uList;
 	}
 
 
 	final private Collection<String> xGoogleSearch(int startIndex) {
 		List<String> uList;
 		URLConnection conn;
 		String url;
 		String tq;
 		uList = new ArrayList<String>(i);
 		
 		tq = (time == null) ? "" : ("&tbs=qdr:" + time);
 		
 		try {
 			conn = new URL("http://www.google.com/search?hl=en&q=" + q + tq + "&num=" + i + "&start=" + startIndex).openConnection();
 			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.0.6) Gecko/2009011913 Firefox/3.0.6");
 			conn.connect();
 			for (org.jsoup.nodes.Element el : Jsoup.parse(conn.getInputStream(), "UTF-8", "http://www.google.com/").select("li.g > h3.r > a")) {
 				Matcher m = extractURL.matcher(el.attr("data-cthref"));
 				url = null;
 				if (m.find()) {
 					url = m.group(1);
 				} else {
 					m = extractQ.matcher(el.attr("href"));
 
 					if(m.find()) {
 						url = m.group(1);
 					}
 				}
 				if (url != null) {
 					uList.add(url);
 				}
 			}
 		} catch (MalformedURLException e) {
 			throw new RuntimeException(e);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 		return uList;
 	}
 	
 	static public void main(String[] args) {
 		URLConnection conn;
 		try {
 			conn = new URL("http://www.google.com/search?hl=en&q=amy&num=25&start=51").openConnection();
 			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.0.6) Gecko/2009011913 Firefox/3.0.6");
 			conn.connect();
 			for (org.jsoup.nodes.Element el : Jsoup.parse(conn.getInputStream(), "UTF-8", "http://www.google.com/").select("li.g > h3.r > a")) {
 				Matcher m = extractURL.matcher(el.attr("data-cthref"));
 				if (m.find()) {
 					System.out.println("Q  :" + m.group(1));
 				} else {
 					m = extractQ.matcher(el.attr("href"));
 
 					if(m.find()) {
 						System.out.println("URL:" + URLDecoder.decode(m.group(1), "UTF-8"));
 					}
 				}
 			}
 		} catch (MalformedURLException e) {
 			throw new RuntimeException(e);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 		
 	}
 }
 
 class _GetGQPage implements Runnable {
 	JSON[] pArr;
 	int index;
 	CountDownLatch countDownSig;
 	String q;
 	
 	public _GetGQPage(CountDownLatch countDownSig, String queryURL, JSON[] pArr, int index) {
 		this.pArr = pArr;
 		this.index = index;
 		this.countDownSig = countDownSig;
 		q = queryURL;
 	}
 	public void run() {
 		try {
 			URLConnection conn = null;
 			pArr[index] = null;
 			try {
 				conn = (new URL(q)).openConnection();
 				pArr[index] = JSON.parse(conn.getInputStream());
 			} catch (MalformedURLException e) {
 				System.err.println("Google Query URL Exception! URL:" + q);
 			} catch (IOException e) {
 				System.err.println("IO Exception while Query Google on url:" + q);
 			} catch (JsonStringFormatException e) {
 				System.err.println("JSON format exception while Query Google on url:" + q);
 			}
 		} finally {
 			if (countDownSig != null) {
 				countDownSig.countDown();
 			}
 		}
 	}
 }
