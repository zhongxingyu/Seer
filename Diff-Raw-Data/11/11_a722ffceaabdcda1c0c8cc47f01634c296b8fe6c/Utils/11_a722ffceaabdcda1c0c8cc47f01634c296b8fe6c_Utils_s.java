 package net.stenuit.xavier.wlanservice;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.StringReader;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserFactory;
 
 import android.content.Context;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.util.Log;
 
 public class Utils {
 	
 	// Parse an xhtml document
 	public static String xhtmlParse(String s)
 	{
 		try
 		{
 			XmlPullParserFactory factory=XmlPullParserFactory.newInstance();
 			factory.setNamespaceAware(true);
 			factory.setValidating(false);
 			
 			XmlPullParser parser=factory.newPullParser();
 			parser.setInput(new StringReader(s));
 			
 			int eventType=parser.getEventType();
 			while(eventType!=XmlPullParser.END_DOCUMENT)
 			{
 				switch(eventType)
 				{
 				case XmlPullParser.START_TAG:
 					Log.d("net.stenuit.xavier.wlanservice.Utils",parser.getName());
 					if("LoginURL".equals(parser.getName()))
 					{
 						String ret=parser.nextText();
 						Log.d("net.stenuit.xavier.wlanservice.Utils",ret);
 						return ret;
 					}
 				}
 				eventType=parser.next();
 			}
 		}
 		catch(Exception e)
 		{
 			Log.e("net.stenuit.xavier.Utils","exception thrown",e);
 		}
 		return null;
 	}
 	public static Map<String,String> readLoginFromFile(File SettingsFile) {
 		BufferedReader reader=null;
 		String ret=null;
 		HashMap<String,String>hm=new HashMap<String, String>();
 		try
 		{
 			reader=new BufferedReader(new FileReader(SettingsFile));
 			ret=reader.readLine();
 			while(ret!=null)
 			{
 				if(ret.contains("login")&&ret.contains(".")&&ret.contains("="))
 				{ // seems to be a login
 					String key=ret.substring(0, ret.indexOf((int)'.'));
 					String login=ret.substring(ret.indexOf('=')+1);
 					hm.put(key,login);
 				}
 				ret=reader.readLine();
 			}
			reader.close();
 		}
 		catch(Exception e)
 		{
 			Log.i(Utils.class.getName(),"Exception thrown",e);
 		}
 		return hm;
 	}
 	
 	public static void clearFile(File settingsFile)
 	{
 		settingsFile.delete();
 		
 	}
 	public static HashMap<String,String> readPasswordFromFile(File SettingsFile) {
 		BufferedReader reader=null;
 		String ret=null;
 		HashMap<String,String>hm=new HashMap<String, String>();
 		try
 		{
 			reader=new BufferedReader(new FileReader(SettingsFile));
 			ret=reader.readLine();
 			while(ret!=null)
 			{
 				if(ret.contains("password")&&ret.contains(".")&&ret.contains("="))
 				{ // seems to be a login
 					String key=ret.substring(0, ret.indexOf((int)'.'));
 					String pwd=ret.substring(ret.indexOf('=')+1);
 					hm.put(key,pwd);
 				}
 				ret=reader.readLine();
 			}
 			reader.close();
 		}
 		catch(Exception e)
 		{
 			Log.i(Utils.class.getName(),"Exception thrown",e);
 		}
 		return hm;
 	}
 	/** Gets SSID of the network
 	 * 
 	 * @return SSID of network connected, or null if not connected
 	 */
 	public static String getSSID(Context ctx)
 	{
 		WifiManager wifiManager=(WifiManager)ctx.getSystemService(Context.WIFI_SERVICE);
 		WifiInfo wifiInfo=wifiManager.getConnectionInfo();
 		return(wifiInfo.getSSID());
 	}
 	/**
 	 * Parse a line resembling to 
 	 * 	  <input type="hidden" name="hs" value="8271">
 	 * @param s the string to parse
 	 * @return String[]{"hs","8271"};
 	 */
 	public static String[] parseInputLine(String s) {
 		s=s.trim();
 		s=s.substring(27);
 		String name=s.substring(0, s.indexOf('"'));
 		s=s.substring(s.indexOf('"')+1);
 		s=s.substring(8);
 		String value=s.substring(0,s.indexOf('"'));
 		
 		return new String[]{name,value};
 	}
 
 }
