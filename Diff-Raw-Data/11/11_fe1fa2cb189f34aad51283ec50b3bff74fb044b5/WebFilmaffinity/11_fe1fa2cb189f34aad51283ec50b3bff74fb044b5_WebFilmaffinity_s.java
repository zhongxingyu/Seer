 package movies.web;
 
 import java.net.URLEncoder;
 import java.text.Collator;
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import movies.db.Movie;
 
 import org.htmlparser.Node;
 import org.htmlparser.Parser;
 import org.htmlparser.filters.TagNameFilter;
 import org.htmlparser.nodes.TagNode;
 import org.htmlparser.nodes.TextNode;
 import org.htmlparser.util.NodeIterator;
 import org.htmlparser.util.NodeList;
 import org.htmlparser.util.ParserException;
 
 import web.WebReader;
 
 public class WebFilmaffinity {
 
 	protected static final String WEFAURLSearchMovieInfo="http://www.filmaffinity.com/es/search.php?stype=title&stext=";
 	public String movieId;
 	public String web;
 	
 	
 	public WebFilmaffinity(){
 
 	}
 		
 	public static TagNode parseLinkGroup(String line){
 		Parser parser;
 		Node node;
 		TagNode tag=null;
 		
 		try {
 			parser = new Parser(line);
 		
 			NodeList nl = parser.parse (null); 
 			NodeList nodes = nl.extractAllNodesThatMatch(new TagNameFilter ("a"));
 			if (nodes.size()>0)
 	             for (NodeIterator i = nl.elements (); i.hasMoreNodes (); )
 	                 if ((node=i.nextNode()) instanceof TagNode){
 	                	tag  = (TagNode) node;	
 	                	break;
 	                 }
 			return tag;  //return
        	     // output the modified HTML
 		} catch (ParserException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public static ArrayList<Movie> getMovieList(String title){
 	
 		Movie movieInfo=null;
 		ArrayList<Movie> movies = new ArrayList<Movie>();
 		Collator comparador = Collator.getInstance();
 	    comparador.setStrength(Collator.TERTIARY);
 	    Parser parser;
 	    Node nodea,nodeb,nodec;
 	    String className,year,director;
 		
 		try {
 			String HTMLText = WebReader.getHTMLfromURL(WEFAURLSearchMovieInfo+URLEncoder.encode(title,"UTF-8"),"ISO-8859-1");
 			parser = new Parser(HTMLText);
 			NodeList nl = parser.parse(null);
 			NodeList ndiv = WebReader.getTagNodesOfType(nl,"div",true);
 			for (NodeIterator k = ndiv.elements (); k.hasMoreNodes (); ){				
 	 			if ((nodea=k.nextNode()) instanceof TagNode){
 	 				if ((className = ((TagNode)nodea).getAttribute("class"))!=null){
 	 					if (className.compareTo("mc-info-container")==0) {
 	 						movieInfo=new Movie();
 	 						if (nodea.getChildren()!=null){
 	 							NodeList ndivNested = WebReader.getTagNodesOfType(nodea.getChildren(),"div",false);
 	 							for (NodeIterator j = ndivNested.elements (); j.hasMoreNodes (); ){				
 	 								if ((nodeb=j.nextNode()) instanceof TagNode){
 	 									if ((className = ((TagNode)nodeb).getAttribute("class"))!=null){
 	 										if (className.compareTo("mc-title")==0) {
 	 											if (nodeb.getChildren()!=null){
 	 												for (NodeIterator i = nodeb.getChildren().elements (); i.hasMoreNodes (); ){
 	 													nodec = i.nextNode();
 	 													if (nodec instanceof TextNode){
 	 														year = ((TextNode)nodec).toPlainTextString().trim();
 	 														year = year.substring(1, year.length()-1);
 	 														try{
 				 			 		 					    	Integer.getInteger(year);
 				 			 		 					    	movieInfo.setYear(year);
 	 														}
 	 														catch(Exception ex){  	
 	 														}
 	 													}else if (nodec instanceof TagNode){
 	 														try{
 		 														NodeList nr = ((TagNode)nodec).getChildren();
 		 														title = nr.elementAt(0).getText();
 				 			 		 					    	movieInfo.setTitle(title);
 	 														}catch(Exception ex){  	
 	 														}
 	 													}
 	 												}
 	 											}
 	 										}
 	 										if (className.compareTo("mc-director")==0) {
 	 											if (nodeb.getChildren()!=null){
 	 												NodeList hrefist = WebReader.getTagNodesOfType(nodeb.getChildren(),"a",false);
 	 												for (NodeIterator i = hrefist.elements (); i.hasMoreNodes (); ){				
 	 													if ((nodec=i.nextNode()) instanceof TagNode){
 	 														try{
 		 														NodeList nr = ((TagNode)nodec).getChildren();
 		 														director = nr.elementAt(0).getText();
 				 			 		 					    	movieInfo.setDirector(director);
 	 														}catch(Exception ex){  	
 	 														}
 	 													}
 	 												}
 	 											}
 	 										}
 			 				 			}
 	 								}
 	 							}
 	 						}
 	 					    movies.add(movieInfo);
 	 					}
 	 				}
 	 					
 	 				  }
 				}				
 
         		return movies;		
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 		
 	}
 	
 	public static Movie getMovieInfo(String title){
 		
 		Movie movieInfo=null;
 		Collator comparador = Collator.getInstance();
 	    comparador.setStrength(Collator.TERTIARY);
 	    Parser parser;
 	    Node nodea,nodeb;
 	    String str,year;
 		
 		try {
 			String HTMLText = WebReader.getHTMLfromURL(WEFAURLSearchMovieInfo+URLEncoder.encode(title,"UTF-8"),"ISO-8859-1");
				System.out.println("get movie info done");
 				parser = new Parser(HTMLText);
 				NodeList nl = parser.parse(null);
 				NodeList ndl = WebReader.getTagNodesOfType(nl,"dl",true);
 				movieInfo = new Movie();
 				for (NodeIterator k = ndl.elements (); k.hasMoreNodes (); ){				
 	 				  if ((nodea=k.nextNode()) instanceof TagNode){
 	 					  if (nodea.getChildren()!=null){
 		 					 NodeList dnodes = nodea.getChildren();
 		 	        		 for (NodeIterator j = dnodes.elements (); j.hasMoreNodes (); ){
 		 		 				  if ((nodeb=j.nextNode()) instanceof TagNode){
 		 		 					  if (((TagNode)nodeb).toPlainTextString()!=null){
 		 		 						  str=((TagNode)nodeb).toPlainTextString();
		 		 						  if (str.contains("A&ntilde;o")){
 		 		 							 j.nextNode(); //pass blank text node
 		 		 							 if ((nodeb=j.nextNode()) instanceof TagNode){
 		 		 		 		 				 if (((TagNode)nodeb).toPlainTextString()!=null){
 		 		 		 		 					year=((TagNode)nodeb).toPlainTextString().trim();
 		 		 		 		 					movieInfo.setYear(year);
 		 		 		 		 				 }		 		 		 		 				 
 		 		 		 	        		 }
 		 		 						  }
 		 		 						  else if (str.contains("Director")){
 		 		 							 j.nextNode(); //pass blank text node	
 		 		 							 if ((nodeb=j.nextNode()) instanceof TagNode){
 		 		 		 		 				 if (((TagNode)nodeb).toPlainTextString()!=null){
 		 		 		 		 					str=((TagNode)nodeb).toPlainTextString().trim();
 		 		 		 		 					movieInfo.setDirector(str);
 		 		 		 		 				 }		 		 		 		 				 
 		 		 		 	        		 }
 			 		 					  }
 		 		 					  }	 		 					  
 		 		 				  }
 		 	        		 }
 	 	        		  }
 	 				  }
 				}				
         		return movieInfo;		
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 		
 	}
 	
 	
 }
