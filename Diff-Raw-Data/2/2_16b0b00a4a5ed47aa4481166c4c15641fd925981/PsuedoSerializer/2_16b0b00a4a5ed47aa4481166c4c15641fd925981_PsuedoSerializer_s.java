 package viz;
 import java.net.URI;
 import java.net.URISyntaxException;
 
 
 public class PsuedoSerializer {
 	String[] psuedoLines;
 	String title;
 	
 	public PsuedoSerializer(String[] psuedocode, String title)
 	{
 		psuedoLines = psuedocode;
 		this.title = title;
 	}
 	
 	public String[] getPsuedocode()
 	{
 		return psuedoLines;
 	}
 	
 	public void setPsuedocode(String[] code)
 	{
 		psuedoLines = code;
 	}
 	
 	public String getTitle()
 	{
 		return title;
 	}
 	
 	public void setTitle(String title)
 	{
 		this.title = title;
 	}
 		
 	public String toPsuedoPage(int lineNum)
 	{
 		final String less_than = "%26lt;";
 		final String less_than_in_uri = "%2625lt;";
 		
 		String content = "<html><head><title>" + title + 
 			"</title></head><body><h1>" + title + "</h1>";
 		
 		for (int i = 0; i < psuedoLines.length; i++)
 		{
 			if (i == lineNum)
 				content += "<div style=\"color:red\">";
 			
			content += "<pre>" + (i+1) + "  " + psuedoLines[i] + "</pre>";
 			
 			if (i == lineNum)
 				content += "</div>";
 		}
 		
 		content += "</body></html>";
 		//System.out.println(content);
 		URI uri = null;
 		
 		try {
 			uri = new URI("str", content, "");
 		} catch (URISyntaxException e) {
 		
 			e.printStackTrace();
 		}
 		
 		return uri.toASCIIString().replaceAll(less_than_in_uri, less_than);
 	}
 }
