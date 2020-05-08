 package org.manalith.ircbot.plugin.DistroPkgFinder;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 public class DebianPkgFinderJsoupRunner {
 	private String keyword;
 	public DebianPkgFinderJsoupRunner ( )
 	{
 		this.setKeyword( "" );
 	}
 	
 	public DebianPkgFinderJsoupRunner ( String newKeyword )
 	{
 		this.setKeyword(newKeyword);
 	}
 	
 	public void setKeyword ( String newKeyword )
 	{
 		this.keyword = newKeyword;  
 	}
 	public String getKeyword ( )
 	{
 		return this.keyword;
 	}
 	
 	public String run ( )
 	{
 		String result = "";
 		String url = "http://packages.debian.org/search?keywords=" + this.getKeyword() + "&searchon=names&suite=all&section=all";
 		
 		boolean hasExacthits = false;
 		
 		String pkgname = "";
 		String version = "";
 		String description = "";
 		
 		try
 		{
 			Document doc = Jsoup.connect(url).get();
 			
 			if ( doc.select("#psearchres").size() == 0 )
 			{
 				result = "There is no result";
 				return result;
 			}
 			
 			Elements hits = doc.select("#psearchres").select("h2");
 			int hsize = hits.size();
 			
 			if ( hsize == 0 ) 
 				result = "There is no result";
 			for ( int i = 0 ; i < hsize ; i++ )
 			{
 				if ( hits.get(i).text().equals("Exact hits") )
 				{
 					hasExacthits = true;
 					break;
 				}
 					
 			}
 			if ( !hasExacthits )
 			{
 				result = "There is no result";
 				return result;
 			}
 				
 
 			pkgname = doc.select("#psearchres").select("h3").get(0).text().split("\\s")[1];
 			
 			result = pkgname + "-";
 			Elements ExactHits = doc.select("#psearchres").select("ul").get(0).select("li");
 			int elemCnt = ExactHits.size();
 			Element latestElement = ExactHits.get(elemCnt - 1);
 
 			String [] verSplit = latestElement.toString().split("\\<br\\s\\/>")[1].split("\\:");
 			if ( verSplit.length == 2 )
 				version = verSplit[0];
 			else if ( verSplit.length == 3)
 				version = verSplit[1];
 			
 			result += version + " : ";
 			
 			description = latestElement.toString().split("\\<br\\s\\/>")[0].split("\\:")[1].trim();
 			
 			result += description;
 
 		}
 		catch ( Exception e )
 		{
 			result = e.getMessage();
 			return result;
 		}
 		
 		return result;
 	}
 }
