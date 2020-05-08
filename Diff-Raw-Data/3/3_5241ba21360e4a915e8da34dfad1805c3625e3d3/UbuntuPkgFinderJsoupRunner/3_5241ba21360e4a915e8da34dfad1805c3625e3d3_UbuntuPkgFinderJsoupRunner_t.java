 package org.manalith.ircbot.plugin.DistroPkgFinder;
 
 import org.jsoup.Connection;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 public class UbuntuPkgFinderJsoupRunner {
 	
 	private String keyword;
 	
 	public UbuntuPkgFinderJsoupRunner ( )
 	{
 		this.setKeyword( "" );
 	}
 	
 	public UbuntuPkgFinderJsoupRunner ( String newKeyword )
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
 	
 	public String run() {
 		// TODO Auto-generated method stub 
 		String result = "";
 		String latestPkgName = this.getLatestPkgName();
 		String url = "http://packages.ubuntu.com/search?keywords=" + this.getKeyword() + "&searchon=names&suite=" + latestPkgName + "&section=all";
 		
 		boolean hasExacthits = false;
 		
 		String pkgname = "";
 		String version = "";
 		String description = "";
 		
 		try
 		{
 			Connection conn = Jsoup.connect(url);
 			conn.timeout(10000);
 			Elements div = conn.get().select("#psearchres");
 			
 			if ( div.size() == 0 )
 			{
 				result = "There is no result";
 				return result;
 			}
 			
 			Elements hits = div.select("h2");
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
 				
 
 			pkgname = div.select("h3").get(0).text().split("\\s")[1];
 			
 			result = pkgname + "-";
 			Elements ExactHits = div.select("ul").get(0).select("li");
 			int elemCnt = ExactHits.size();
 			Element latestElement = ExactHits.get(elemCnt - 1);
 
 			String [] verSplit = latestElement.toString().split("\\<br\\s\\/>")[1].split("\\:");
 			if ( verSplit.length == 2 )
 				version = verSplit[0].split("\\s")[0];
 			else if ( verSplit.length == 3)
 				version = verSplit[1].split("\\s")[0];
 			
 			result += version + " : ";
 			
 			description = latestElement.toString().split("\\<br\\s\\/>")[0].split("\\:")[1].trim();
			int a = description.indexOf("[");
			if ( a != -1) description = description.substring(0,a);
 			
 			result += description;
 
 		}
 		catch ( Exception e )
 		{
 			result = e.getMessage();
 			return result;
 		}
 		
 		return result; 
 	}
 	
 	private String getLatestPkgName ( )
 	{
 		String result = "";
 		String url = "http://packages.ubuntu.com";
 		String tmp;
 		try 
 		{
 			Document doc = Jsoup.connect(url).get();
 			Elements e = doc.select("select#distro>option");
 			
 			int esize = e.size();
 			for ( int i = 0 ; i < esize; i++ )
 			{
 				tmp = e.get(i).attr("selected");
 				if ( tmp.equals("selected") )
 				{
 					result = e.get(i).text();
 					break;
 				}
 			}
 		}
 		catch ( Exception e )
 		{
 			result = e.getMessage();;
 		}
 		
 		return result;
 	}
 	
 }
