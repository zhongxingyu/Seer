 
 
 /**
 *
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2008, NCSA.  All rights reserved.
 *
 * Developed by:
 * The Automated Learning Group
 * University of Illinois at Urbana-Champaign
 * http://www.seasr.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject
 * to the following conditions:
 *
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimers.
 *
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimers in
 * the documentation and/or other materials provided with the distribution.
 *
 * Neither the names of The Automated Learning Group, University of
 * Illinois at Urbana-Champaign, nor the names of its contributors may
 * be used to endorse or promote products derived from this Software
 * without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *
 */
 
 package org.seasr.meandre.apps.twitter;
 
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.meandre.annotations.Component;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.meandre.webui.ConfigurableWebUIFragmentCallback;
 import org.meandre.webui.WebUIException;
 import org.meandre.webui.WebUIFragmentCallback;
 import org.seasr.datatypes.core.BasicDataTypesTools;
 import org.seasr.datatypes.core.Names;
 import org.seasr.datatypes.core.BasicDataTypes.Strings;
 import org.seasr.datatypes.core.BasicDataTypes.StringsArray;
 import org.seasr.meandre.apps.twitter.support.LRUCache;
 import org.seasr.meandre.apps.twitter.support.LRUCacheWithMap;
 import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
 import org.seasr.meandre.components.abstracts.util.EmptyHttpServletRequest;
 import org.seasr.meandre.support.components.tuples.FrequencyMap;
 import org.seasr.meandre.support.components.tuples.SimpleTuple;
 import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;
 import org.seasr.meandre.support.components.tuples.TupleUtilities;
 
 /**
  * This class implements a component that using the WebUI accepts post requests
  * It assumes the input are tagged (named entity) tuples
  * @author mike haberman
  */
 
 @Component(
 		creator = "Mike Haberman",
 		description = "Service head for a service that gets data via posts",
 		name = "Tuple Web Server",
 		tags = "WebUI, post, process request",
 		rights = Licenses.UofINCSA,
 		mode = Mode.webui,
 		firingPolicy = FiringPolicy.all,
 		baseURL = "meandre://seasr.org/components/tools/"
 )
 public class TwitterTupleWebServer extends AbstractExecutableComponent
     implements WebUIFragmentCallback, ConfigurableWebUIFragmentCallback   {
 	
 	
     //------------------------------ INPUTS ------------------------------------------------------
 	
 	@ComponentInput(
 			name = Names.PORT_TUPLES,
 			description = "set of tuples"
 	)
 	protected static final String IN_TUPLES = Names.PORT_TUPLES;
 	
 	@ComponentInput(
 			name = Names.PORT_META_TUPLE,
 			description = "meta data for tuples"
 	)
 	protected static final String IN_META_TUPLE = Names.PORT_META_TUPLE;
 	
 	
 	@ComponentInput(
 			name = "neTuples",
 			description = "set of ne tuples"
 	)
 	protected static final String IN_NE_TUPLES = "neTuples";
 	
 	@ComponentInput(
 			name = "neMeta_tuple",
 			description = "meta data for ne tuples"
 	)
 	protected static final String IN_NE_META_TUPLE = "neMeta_tuple";
 	
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
 	
     //--------------------------------------------------------------------------------------------
 	 public String getContextPath() {
 	    	return "/twitter";
 	 }
 	 
 	 // Map<String,Map<String,Integer>> wordMap = new HashMap<String, Map<String,Integer>>();
 	 Map<String,LRUCache<SimpleTuple>> tweetCacheMap = new HashMap<String,LRUCache<SimpleTuple>>();
 
 	 Map<String,LRUCache<SimpleTuple>> caches = new HashMap<String,LRUCache<SimpleTuple>>();
 	 LRUCache<SimpleTuple> tweetLRUCache = new LRUCache<SimpleTuple>(5000);
 	 
 	 Map<String, Object> stopWords = new HashMap<String,Object>();
 	 
 	 
 	@Override
 	public void initializeCallBack(ComponentContextProperties ccp) 
 	throws Exception 
 	{
 	    console.info("Service location: " + ccp.getWebUIUrl(true) + ccp.getExecutionInstanceID());
 	    
 	    // no need to add anything < 3 characters (a, -, to, ) .. 
 	    // keep : not, 
 	    String[] list = new String[]{
 	    		"for", "and", "the", "you", "when", "can", "this", "from", "your","would", "could", "should",
 	    		"into", "was", "with", "what","that","but", "nor", "has", "are", "get", "were", "then", "i'm",
 	    		"how", "too", "it's", "got"
 	    		};
 	    
 	    for (String s : list) {
 	    	s = normalize(s);
 	    	stopWords.put(s,s);
 	    }
 	    
 	    console.info("stop words " + stopWords.size());
 	}
 	
 	public SimpleTuple find(SimpleTuplePeer peer, Strings[] in, String fieldName, String value) 
 	{
 		SimpleTuple tuple = peer.createTuple();
 
 		for (int i = 0; i < in.length; i++) {
 			tuple.setValues(in[i]);	
 			
 			String v = tuple.getValue(fieldName);
 			if (v.equals(value)) {
 				return tuple;
 			}
 		}
 
 		return null;
 	}
 
 	
 	static String regEx = "[\"\'()]+";	
 	void normalizeTuple(SimpleTuple tuple, int idx) {
 		// don't do anything that would mess up urls,names, etc
 		String value = tuple.getValue(idx);
 		value = value.replaceAll(regEx ,"").trim();
 		tuple.setValue(idx, value.toLowerCase());
 	}
 	
 	void normalizeTuple(SimpleTuple tuple, String fn) {
 		int idx = tuple.getPeer().getIndexForFieldName(fn);
 		normalizeTuple(tuple, idx);
 	}
 	
 	String normalize(String input) {
 		return input;
 		//return input.toLowerCase();
 	}
 	
 	
 	Boolean webServerStarted = false;
 	int MAX_KEEP = 5000;
 	
 	int requestCount = 0;
 	@Override
 	public void executeCallBack(ComponentContext cc) throws Exception {
 		
 		if (! webServerStarted) {
 			cc.startWebUIFragment(this);
 			console.info("Starting service head for " + cc.getFlowID());
 			webServerStarted = true;
 		}
 	    
 		requestCount++;
 		
 		
 		/* the original twitter tuples */
 		Strings inputMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
 		SimpleTuplePeer tuplePeer = new SimpleTuplePeer(inputMeta);
 		
 		
 		StringsArray input = (StringsArray) cc.getDataComponentFromInput(IN_TUPLES);
 		Strings[] tupleValues = BasicDataTypesTools.stringsArrayToJavaArray(input);
 	
 		
 		/* the ne tuples */
 		Strings inputNEMeta = (Strings) cc.getDataComponentFromInput(IN_NE_META_TUPLE);
 		SimpleTuplePeer neTuplePeer = new SimpleTuplePeer(inputNEMeta);
 		
 		
 		StringsArray neInput = (StringsArray) cc.getDataComponentFromInput(IN_NE_TUPLES);
 		Strings[] neValues = BasicDataTypesTools.stringsArrayToJavaArray(neInput);
 		
 		
 		int TYPE_IDX = neTuplePeer.getIndexForFieldName("type");
 		int TEXT_IDX = neTuplePeer.getIndexForFieldName("text");
 		int PID_IDX  = neTuplePeer.getIndexForFieldName("pid");
 		if (TYPE_IDX == -1 || TEXT_IDX == -1 || PID_IDX == -1) {
 			console.info("bad tuple " + neTuplePeer.toString());
 			return;
 		}
 		
 		//
 		// Obama, is a friend; my Obama
 		// ==> we will have two of the same <PERSON> entities, for the same tweet/text unit.
 		// we do NOT fix this, but could
 		//
 		
 		console.finer("WEB Processing " + neValues.length);
 		console.finer("Global cache size " + tweetLRUCache.size());
 		
 		SimpleTuple tweetTuple = null;
 
 		for (int i = 0; i < neValues.length; i++) {
 			
 			SimpleTuple neTweetTuple = neTuplePeer.createTuple();
 			neTweetTuple.setValues(neValues[i]);
 			normalizeTuple(neTweetTuple, TEXT_IDX);
 			
 			String type = neTweetTuple.getValue(TYPE_IDX);  // type:  person, organization, location, etc
 			String text = neTweetTuple.getValue(TEXT_IDX);  // value: obama, microsoft, paris
 			String pid  = neTweetTuple.getValue(PID_IDX);
 			
 			// ignore any small text, most likely junk anyway (&,-,)
 			if (text.length() < 3) {
 				// tuple is not worth keeping around
 				continue;
 			}
 			
 			// get the parent tuple
 			if (tweetTuple == null || ! tweetTuple.getValue("id").equals(pid)) {
 				// we have a new parent
 				// at this time we could clear a unique cache
 				//
 			   tweetTuple = find(tuplePeer, tupleValues, "id", pid);
 			   
 			   if (tweetTuple == null) {
 				   console.warning("unable to find parent tuple");
 				   continue;
 			   }
 			   
 			   normalizeTuple(tweetTuple, "text");
 			   
 			   //console.info(tweetTuple.toString());
 			}
 			
 			
 			
 			
 			tweetLRUCache.add(neTweetTuple);
 			
 			LRUCache<SimpleTuple> typeCache = caches.get(type);
 			if (typeCache == null) {
 				// keep only the past MAX_KEEP tuples for each type
 				typeCache = new LRUCache<SimpleTuple>(MAX_KEEP);
 				caches.put(type, typeCache);
 			}
 			typeCache.add(neTweetTuple);
 			
 			/*
 			// the single cache to hold last 5000 tuples
 			if (! type.equals("userLocation")) {
 				
 			}
 			*/
 			
 			
 		
 			String key = buildTweetKey(type,text);	
 			LRUCache<SimpleTuple> tweetCache  = tweetCacheMap.get(key);
 			if (tweetCache == null) {
 				// map = new HashMap<String,Integer>();
 				tweetCache = new LRUCache<SimpleTuple>(1000);
 				// console.info("new key:" + key + ":");
 				tweetCacheMap.put(key,tweetCache);
 				
 			}
 			tweetCache.add(tweetTuple);
 		}
 		
 		
 		if (tweetCacheMap.size() > 5000) {	
 			console.info("NEED TO CLEAN");
 			this.cleanCaches();
 			console.info("AFTER CLEAN " + tweetCacheMap.size());
 			
 		}
 				
 		
 		// build the rss.xml content
 		// to be served in future requests
 		// save to local storage, wait for request to come in
 
 		/*
 		while (!cc.isFlowAborting())
 			Thread.sleep(1000);
 
 		console.info("Abort request received for " + cc.getFlowID());
 
 		cc.stopWebUIFragment(this);
 		*/
 		
 	}
 	
 	public void cleanCaches()
 	{
 		Map<String,LRUCache<SimpleTuple>> tmpMap = new HashMap<String,LRUCache<SimpleTuple>>();
 		
 		for (String type : caches.keySet()) {
 			
 			LRUCache<SimpleTuple> c = caches.get(type);  // e.g Person, Location, Organization
 			Object[] tuples = c.toArray();               // ne tuples
 			
 			//
 			// these are the top 100 "organizations", "persons", etc
 			// this number (100) MUST be bigger than the number returned 
 			// when requesting topN (e.g. 50)
 			// otherwise, it might be possible to remove something that will be requested
 			//
 			List<Map.Entry<String, Integer>> sorted = TupleUtilities.topNTupleValues(tuples, "text", 100);
 			
 			
 			for (Map.Entry<String, Integer> e : sorted) {
 				
 				String key = buildTweetKey(type, e.getKey());
 				
 				LRUCache<SimpleTuple> map = tweetCacheMap.get(key);
 				if (map != null) {
 					
 					// since caches may be large .. 5000 tuples
 					// when we sort the top 100 (but we still keep a 5000 window size)
 					// it is possible that the previous clean
 					// through out a value that will be in the next round's top 100
 					/*
 					 * a:4,b:4,c:4  --> throw out d:4 (but d:4 is still in caches)
 					 * now next time: (no new tuples have come in, but the window shifted)
 					 * d:4,b:4,c:4  --> request d:4 
 					 */
 					
 					// if you make the clean request size big, this
 					// should not happen
 					
 					
 					//
 					// we want to keep this map .. e.g. organizations:microsoft
 					//
 					tmpMap.put(key, map);	
 				}
 				else {
 					console.info("NO map:" + key + ":");
 				}
 				
 				
 			}
 		}
 		
 		//
 		// lock parenMap. lock the cache
 		// probably should lock caches
 		// but lets just lock the parentMap
 		tweetCacheMap.clear();
 		tweetCacheMap = tmpMap;
 		
 		// it is possible that parentMap will not contain a request
 		// for the top N if I truncate to aggressively
 		
 	}
 	
 	
 	
 	// pass in all the parents for "mexico", a location
 	public List<Map.Entry<String, Integer>> getTopNWords(LRUCache<SimpleTuple> cache, Map<String, Object> stopWords, String key) 
 	{
 		key = normalize(key);
 		Object[] tuples = cache.toArray();
 		
 		Map<String,String> unique = new HashMap<String,String>();
 		FrequencyMap<String> fMap = new FrequencyMap<String>();
 		
 		for (int i = 0; i < tuples.length; i++) {
 			SimpleTuple tuple = (SimpleTuple)tuples[i];
 			String text = tuple.getValue("text");
 			
 			// this is the tweet: console.info(text);
 			
 			StringTokenizer tokens = new StringTokenizer(text);
 			unique.clear();
 			while(tokens.hasMoreTokens()) {
 				
 				String t = tokens.nextToken();
 				t = normalize(t);
 				
 				if (t.length() < 3  && !t.equals("rt")) {  // keep rt (re-tweets)
 					continue;
 				}
 				
 				
 				// key == janet jackson
 				// text --> janet jackson sings well
 				// we want to ignore janet,jackson (part of the key)
 				
 				int idx = key.indexOf(t);
 				if (idx >= 0) {
 					continue;
 				}
 				
 				if (stopWords.containsKey(t)) {
 					continue;
 				}
 				
 				// for each tweet, only add unique tokens for that tweet
 				// so RT RT RT help me mother help me ==> RT help me mother
 				if (unique.get(t) != null) {
 					//console.info(key + " skip " + t);
 					continue;
 				}
 				unique.put(t, t);
 				
 				fMap.add(t);
 				
 			}
 		}
 		
 		List<Map.Entry<String, Integer>> sorted = fMap.sortedEntries();
 		return sorted;
 	
         /*
 		for (Map.Entry<String, Integer> e : sorted) {
 			String key = e.getKey();
 			Integer v  = e.getValue();
 		}
 		*/
 	
 	}
 	
     
 	
     @Override
 	public void disposeCallBack(ComponentContextProperties ccp) throws Exception 
     {
     }
 
 	// -------------------------------------------------------------------------
     public void emptyRequest(HttpServletResponse response) throws WebUIException 
 	{
 		console.entering(getClass().getName(), "emptyRequest", response);
 
 	    HttpServletRequest request = new EmptyHttpServletRequest();
 		handle( request, response);
 	}
     
     String buildTweetKey(String type, String value) {   	
     	String key = type + ":" + value;
 		key = normalize(key);
 		return key;
     }
 
 	LRUCache<SimpleTuple> getTweetCache(String type, String value)
 	{
 		String key = buildTweetKey(type,value);
     	LRUCache<SimpleTuple> map = tweetCacheMap.get(key);
     	return map;
 	}
 	
 	double round(double v) {
 		v = (int) (v * 100.0);
 		return v/100.0;
 	}
 
 	
     LRUCacheWithMap dnsCache = new LRUCacheWithMap(500);
     
 	@SuppressWarnings("unchecked")
 	public void handle(HttpServletRequest request, HttpServletResponse response) throws WebUIException 
 	{
 		console.info(toString(request));
 
 		String format = "html";
 		String type = getParameterAsString(request, "type");
 		if (type == null) {
 			type = "all";
 		}
 		else {
 			int idx = type.lastIndexOf(".");
 			if (idx > 0) {
 				format = type.substring(idx+1);
 				type   = type.substring(0, idx);
 			}
 		}
 		console.info("request for type " + type + " [" + format + "]");
 		
 		Object[] tuples;
 		LRUCache<SimpleTuple> c = caches.get(type);
 		if (c != null) {
 			tuples = c.toArray();
 		}
 		else {
 			type   = "all";
 			tuples = tweetLRUCache.toArray();	
 		}
 
 
 		boolean doResolve = getParameterAsBoolean(request,"doResolve");
 		String topN       = getParameterAsString(request, "topN");
 		
 		List<Map.Entry<String, Integer>> topNValues;
 		
 		int N = 50;
 		if (topN != null) {
 	       N = Integer.parseInt(topN);
 		}
 		topNValues = TupleUtilities.topNTupleValues(tuples, "text", N);
 
 		try {
 			
 			String html;
 			
 			if (format.equals("json"))
 			{
 				html = toJSON(type,N, topNValues).toString();
 			}
 			else if (format.equals("v1")) {
 				
 				int MAX_EXPORT = 15;
 				double sum = 0;
 				int i = 0;
 				for(Map.Entry<String, Integer> entry : topNValues) {
 					Integer count = entry.getValue();
 					sum += count.doubleValue();
 					if (i++ >= MAX_EXPORT) {
 						break;
 					}
 				}
 				// we assume the MAX_EXPORT is the population
 				// print out relative percentages based on that
 				//
 				 
 				StringBuffer sb = new StringBuffer();
 				i = 0;
 				int rank = 0;
 				for(Map.Entry<String, Integer> entry : topNValues) {
 					rank++;
 					String value  = entry.getKey();
 					Integer count = entry.getValue();
 					
 					double pct = round((count.doubleValue()/sum));
 					
 					LRUCache<SimpleTuple> map = getTweetCache(type,value);
 			    	int size = map.size(); // number of tweets available
 			
 			    	// the encoded key
 					sb.append("\"").append(value);
 					sb.append(":").append(rank);    // it's rank            e.g 1
 					sb.append(":").append(count);   // it's # of accurances e.g. 204
 					sb.append(":").append(size);
 					sb.append(":").append(pct);
 					sb.append("\"");
 					
 					sb.append(":{");
 					
 					JSONArray words = appendWords(type, value);
 					int cnt = words.length();
 					for (int j = 0; j < cnt; j++) {
 						
 						JSONObject job = words.getJSONObject(j);
 						String k = job.getString("k");
 						
 						String rnk = job.getString("rank");
 						String p   = job.getString("p");
 						String p5  = job.getString("p5");
 						
 	
 						k = k.replaceAll("[\"']+","");
 						k = k + ":" + rnk + ":" + p ;
 						
 						sb.append("\"").append(k).append("\"");
 						sb.append(":");
 						sb.append(p5);
 
 						if (j + 1 < cnt) {
 							sb.append(",");
 						}
 					}
 					sb.append("},");
 					
 					if (i++ >= MAX_EXPORT) {
 						break;
 					}
 				}
 				html = sb.toString();
 				html = html.substring(0, html.length() - 1); // remove the last ,
 			}
 			else {
 				StringBuffer sb = new StringBuffer();
 				sb.append("Top ").append(topNValues.size()).append("</br>");
 				for(Map.Entry<String, Integer> entry : topNValues) {
 					String value  = entry.getKey();
 					Integer count = entry.getValue();
 					sb.append(value).append(" ").append(count);
 					
 					if (! type.equals("all")) {
 						JSONArray words = appendWords(type, value);
 						sb.append(words.toString());
 					}
 					
 					sb.append("</br>");
 				}
 				sb.append(toHtml(tuples));
 
 				html = sb.toString();
 			}
 			
 			
 			console.fine(html);
 		
 			PrintWriter pw = response.getWriter();
 			pw.println(html);
 			response.getWriter().flush();
 			
 		}
 		catch (Exception e) {
 			throw new WebUIException(e);
 		}
 		
 
 		
 		// response.setContentType("text/xml");
 
 
 		/* FROM Tail
 			PrintWriter pw = response.getWriter();
 			pw.println("WOW " + responseCount);
 			response.getWriter().flush();
 			sem.release();
 		 */
 
 		/*
 			Semaphore sem = new Semaphore(1, true);
 			sem.acquire();
 			componentContext.pushDataComponentToOutput(OUT_REQUEST, BasicDataTypesTools.mapToByteMap(map));
 			componentContext.pushDataComponentToOutput(OUT_RESPONSE, response);
 			componentContext.pushDataComponentToOutput(OUT_SEMAPHORE, sem);
 			sem.acquire(); // blocks waiting for a release
 			sem.release();
 
 		 */
 
 
 	}
 	
 	
 	
 
 	public String toHtml(Object[] tuples)
 	{
 		
 		StringBuffer sb = new StringBuffer();
         //
 		// push these out in reverse .. 0 is the newest
 		// only push out the last 500 or so 
 		//
 		sb.append("ALL tuples ").append(tuples.length).append(" </br>");
 		for (int i = 0; i < tuples.length; i++) {
 			SimpleTuple tuple = (SimpleTuple)tuples[i];
 			sb.append(tuple.toString()).append("</br>");
 
 		}
 
 		return sb.toString();
 
 	}
 	
 	
 	// to be moved to somewhere more helpful
 	public static String toString(HttpServletRequest request) 
 	{
 		String user = (request.getRemoteUser() != null) ? " [" + request.getRemoteUser() + "]" : "";
 		
 		return "Request received from " + request.getRemoteHost()
 		+ " (" + request.getRemoteAddr() + ":" + request.getRemotePort() + ")"
 		+ user;
 	}
 	
 
 	public static boolean hasParameter(HttpServletRequest request, String name)
 	{
 		String [] values = request.getParameterValues(name);
 		return (values != null && values.length == 1);
 	}
 	
 	public static String getParameterAsString(HttpServletRequest request, String name)
 	{
 		String [] values = request.getParameterValues(name);
 		if (values != null && values.length == 1) {
 			return values[0];
 		}
 		return null;
 	}
 	
 	public static boolean getParameterAsBoolean(HttpServletRequest request, String name)
 	{
 		String val = getParameterAsString(request, name);
 		if (name != null) {
 			return Boolean.parseBoolean(val);
 		}
 		return false;		
 	}
 	
 	
     public JSONArray appendWords(String type, String value) 
     throws JSONException
     {
     	LRUCache<SimpleTuple> map = getTweetCache(type, value);
     	JSONArray items = new JSONArray();
        	
    	int MAX_EXPORT = 5;
     
 		if (map != null) {
 			List<Map.Entry<String, Integer>> sorted = getTopNWords(map, stopWords, value);
 			
 			int i = 0;
 			double sum = 0;
 			double total = 0;
 			for (Map.Entry<String, Integer> e : sorted) {
 				String  k  = e.getKey();
 				Integer v  = e.getValue();
 				total += v;
				if (i++ <= MAX_EXPORT) {
 					sum += v.doubleValue();
 				}
 			}
 			
 			double SUM = 100.0;
 			double SUM_5 = 100.0;
 			i = 0;
 			for (Map.Entry<String, Integer> e : sorted) {
 				String  k  = e.getKey();
 				Integer v  = e.getValue();
 				
 				double pct = round(v.doubleValue()/total);
 				double pct5 = round(v.doubleValue()/sum);
 				SUM   -= (pct * 100.0);
 				SUM_5 -= (pct5 * 100.0);
 				
 				JSONObject item = new JSONObject();
 				items.put(item);
 				
 				item.put("k", k);
 				item.put("rank", i + 1);
 				item.put("p", pct);
 				item.put("p5", pct5);
 			
				if (i++ > MAX_EXPORT) {
 					break;
 				}
 			}
 			
             // add the remaining/leftovers
 			JSONObject item = new JSONObject();
 			items.put(item);
 
 			int left = sorted.size() - i;
 			item.put("k", "rem " + left +"/" + round(SUM) + "%");
 			item.put("rank", i + 1);
 			item.put("p", round(SUM/100.0));
 			item.put("p5", round(SUM_5/100.0));
 			
 			
 		}
 		return items;
     }
     
     public JSONObject toJSON(String type, 
     		                 int N, 
     		                 List<Map.Entry<String, Integer>> topNValues )
     throws JSONException
     {
     	JSONArray items = new JSONArray();
 		for(Map.Entry<String, Integer> entry : topNValues) {
 			String key  = entry.getKey();
 			Integer count = entry.getValue();
 			JSONObject item = new JSONObject();
 			item.put("key", key);
 			item.put("count", count);
 			
 			if (! type.equals("all")) {
 				JSONArray words = appendWords(type, key);
 			    item.put("words", words);
 			}
 			items.put(item);
 		}
 		JSONObject rst = new JSONObject();
 		rst.put("type", type);
 		rst.put("num",N);
 		rst.put("values", items);
 		return rst;
     }
 	
 }
 
 /*
 Map<String,byte[]> map = new Hashtable<String,byte[]>();
 Enumeration mapRequest = request.getParameterNames();
 while ( mapRequest.hasMoreElements() ) {
 	String sName = mapRequest.nextElement().toString();
 	String [] sa = request.getParameterValues(sName);
 	String sAcc = "";
 	for ( String s:sa ) sAcc+=s;
 	try {
         map.put(sName, sAcc.getBytes("UTF-8"));
     }
     catch (UnsupportedEncodingException e) {
         throw new WebUIException(e);
     }
 }
  */
 
 /* skip resolving
 if (doURL && tuple.getValue("type").equals("URL")) {
 
 	String url = value;
 	String full = dnsCache.find(url);
 	if (full == null) {
 		full = TwitterServices.getActualHost(url);
 		dnsCache.add(url, full);
 		lookup++;
 
 	}
 	else {
 		cache++;
 	}
 	tuple.setValue("textClean", full);
 	value = full;
 }
 */
