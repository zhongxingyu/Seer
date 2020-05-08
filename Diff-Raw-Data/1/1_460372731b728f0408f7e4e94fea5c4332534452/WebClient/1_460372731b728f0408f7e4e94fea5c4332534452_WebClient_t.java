 package org.sc.annotator.adaptive.client;
 
 import java.net.*;
 import java.util.*;
 import java.io.*;
 
 import org.sc.annotator.adaptive.AdaptiveMatcher;
 import org.sc.annotator.adaptive.Context;
 import org.sc.annotator.adaptive.Match;
 import org.sc.annotator.adaptive.exceptions.MatcherCloseException;
 import org.sc.annotator.adaptive.exceptions.MatcherException;
 
 public class WebClient implements AdaptiveMatcher {
 	
 	private String base;
 	
 	public WebClient(String b) { 
 		base = b;
 	}
 
 	public Collection<Match> findMatches(Context c, String blockText) throws MatcherException {
 		try {
 			URL url = new URL(String.format("%s?context=%s&text=%s",
 					base, 
 					URLEncoder.encode(c.toString(), "UTF-8"),
 					URLEncoder.encode(blockText, "UTF-8")));
 			
 			LinkedList<Match> matches = new LinkedList<Match>();
 			
 			HttpURLConnection cxn = (HttpURLConnection)url.openConnection();
 			cxn.setRequestMethod("GET");
 			cxn.connect();
 			
 			int status = cxn.getResponseCode();
 			if(status == 200) { 
 				BufferedReader reader = new BufferedReader(new InputStreamReader(cxn.getInputStream()));
 				String line;
 				while((line = reader.readLine()) != null) { 
 					String value = line;
 					Match m = new Match(c, blockText, value);
 					matches.add(m);
 				}
 				reader.close();
 				
 			} else { 
 				String msg = String.format("%d : %s", status, cxn.getResponseMessage());
 				throw new MatcherException(msg);
 			}
 			
 			return matches;
 			
 		} catch (MalformedURLException e) {
 			throw new IllegalArgumentException(e);
 		
 		} catch (UnsupportedEncodingException e) {
 			throw new IllegalStateException(e);
 			
 		} catch (IOException e) {
 			throw new MatcherException(e);
 		}
 	}
 
 	public Context registerMatch(Match m) throws MatcherException {
 		try {
 			URL url = new URL(base);
 
 			Context matched = null;
 			
 			HttpURLConnection cxn = (HttpURLConnection)url.openConnection();
 			cxn.setRequestMethod("POST");
 			cxn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
 			cxn.setDoOutput(true);
 			
 			OutputStream os = cxn.getOutputStream();
 			PrintStream ps = new PrintStream(os);
 			
 			ps.print(String.format("context=%s", URLEncoder.encode(m.context().toString(), "UTF-8")));
 			ps.print(String.format("&text=%s", URLEncoder.encode(m.match(), "UTF-8")));
 			ps.print(String.format("&value=%s", URLEncoder.encode(m.value(), "UTF-8")));
 			
 			cxn.connect();
 			
 			int status = cxn.getResponseCode();
 			if(status == 200) { 
 				BufferedReader reader = new BufferedReader(new InputStreamReader(cxn.getInputStream()));
 				String line = reader.readLine();
 				matched = new Context(line);
 				
 				reader.close();
 				
 			} else { 
 				String msg = String.format("%d : %s", status, cxn.getResponseMessage());
 				throw new MatcherException(msg);
 			}
 			
 			return matched;
 			
 		} catch (MalformedURLException e) {
 			throw new IllegalArgumentException(e);
 			
 		} catch (UnsupportedEncodingException e) {
 			throw new IllegalStateException(e);
 			
 		} catch (IOException e) {
 			throw new MatcherException(e);
 		}
 
 	}
 
 	public void close() throws MatcherCloseException {
 	}
 }
