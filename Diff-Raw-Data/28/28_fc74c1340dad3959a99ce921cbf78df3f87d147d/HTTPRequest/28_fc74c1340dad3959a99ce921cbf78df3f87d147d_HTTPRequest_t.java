 package lobos.andrew.aztec.http;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.Socket;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Vector;
 
 public class HTTPRequest {
 	String path;
 	String type;
 	String host;
 	String clientIP;
 	HashMap<String, String> headers = new HashMap<String,String>();
 	HashMap<String, String> queryData = new HashMap<String,String>();
 	
 	public HTTPRequest(Vector<String> headerData, String postData, String clientIP)
 	{
 		this.clientIP = clientIP;
 		Iterator<String> it = headerData.iterator();
 		
 		String[] spaceSplit = it.next().split(" ");
 		type = spaceSplit[0];
 		path = spaceSplit[1];
 		while ( it.hasNext() )
 		{
 			String thisHeader[] = it.next().split(": ");
 			headers.put(thisHeader[0], thisHeader[1]);
 		}
 		
 		host = headers.get("Host").split(":")[0];
 		
 		if ( type.equals("GET") )
 		{
 			String[] querysplit = path.split("\\?");
 			path = querysplit[0];
 			if ( querysplit.length == 2 )
 			{
 				String[] args = querysplit[1].split("&");
 				for ( int i = 0; i < args.length; i++ )
 				{
 					String[] thisValue = args[i].split("=");
 					if ( thisValue.length == 2 )
 						this.queryData.put(thisValue[0], thisValue[1]);
 				}
 			}
 		}
 		else if ( type.equals("POST") )
 		{
 			String[] separatePostData = postData.split("&");
 			for ( int i = 0; i < separatePostData.length; i++ )
 			{
 				String[] thisValue = separatePostData[i].split("=");
 				if ( thisValue.length == 2 )
 					this.queryData.put(thisValue[0], thisValue[1]);
 			}
 		}
 	}
 	
 	public static HTTPRequest fromSocket(Socket client) throws IOException
 	{	
 		Vector<String> headerData = new Vector<String>();
 		BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
 		
 		while ( !reader.ready() && client.isConnected() );
 		while ( client.isConnected() )
 		{
 			String line = reader.readLine();
 			if ( line == null )
 				break;
 			if ( line.equals("") )
 				break;
 			headerData.add(line);
 		}
 		String postData = "";
 
 		// Read POST data if it is available
 		while ( reader.ready() )
 		{
 			char[] buf = new char[50];
 			reader.read(buf, 0, buf.length);
 			postData += new String(buf);
 		}
 		
 		return new HTTPRequest(headerData, postData, client.getInetAddress().getHostAddress());
 	}
 	
 	public String getHeader(String name)
 	{
 		if ( !headers.containsKey(name) )
 			return "";
 		return headers.get(name);
 	}
 	
 	public String getPath()
 	{
 		return path;
 	}
 	
 	public String getMethod()
 	{
 		return type;
 	}
 	
 	public String getHost()
 	{
 		return host;
 	}
 	
 	public String getClientIP()
 	{
 		return clientIP;
 	}
 	
 	public String getParam(String key)
 	{
 		String value = queryData.get(key);
 		if ( value == null )
 			return "";
 		return value;
 	}
 	
 	public String getQueryString()
 	{
		try
 		{
			Iterator<String> it = queryData.keySet().iterator();
			String output = "";
			while ( it.hasNext() )
			{
				String thisKey = it.next();
				output += thisKey+"="+queryData.get(thisKey)+"&";
			}
			return output.substring(0,output.length()-1);
		} catch (Exception e) {
			return "";
 		}
 	}
 
 }
