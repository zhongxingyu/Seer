 package de.fuberlin.wiwiss.ng4j.semwebclient;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class HtmlLinkFetcher {
 	
 	public static ArrayList fetchLinks(InputStream stream){
 		BufferedReader bw = new BufferedReader(new InputStreamReader(stream));
		String line;
		String header = null;
 		ArrayList linkList = new ArrayList();
 		try {
 		while(((line = bw.readLine()).indexOf("</head>") == -1)|(line == null)){
 			header = header + line;
 		}
 		}catch(IOException e){
 			//System.out.println(e.getLocalizedMessage());
 		}
		
 	    Pattern linkPattern = Pattern.compile ("<link[^>]*(rel=\"meta\"|rel=\"alternate\")[^>]*href=\"?[^(>| )]*\"?[^>]*(/>|>)", Pattern.CASE_INSENSITIVE);
 	    Pattern prePattern  = Pattern.compile ("href=\"?");
 	    Pattern postPattern = Pattern.compile ("( |\"|>)");
 	    Pattern typePattern = Pattern.compile (".*(type=\"application/rdf\\+xml\"|type=\"application/n3\"|type=\"text/rdf\\+n3\").*");
 	    Matcher linkMatcher = linkPattern.matcher (header);
 	    LinkedList linkLinkedList = new LinkedList ();
 	    while (linkMatcher.find ()) {
 		      linkLinkedList.addLast (linkMatcher.group ());
 		    }
 		    for (int i = 0; i < linkLinkedList.size (); i++) {
 		      String linkElmt = (String) linkLinkedList.get(i);
 		      if ( typePattern.matcher(linkElmt).matches() ) {
 		        CharSequence cs = prePattern.split ((CharSequence)linkLinkedList.get(i))[1];
 		        linkList.add(postPattern.split (cs)[0]);
 		      }
 		    }
 		return linkList;
 	}
 	
 
 
 }
