 package com.allplayers.android;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.entity.BufferedHttpEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 
 /* This class is used for storing global variables across activities.
  * Also, update timestamps should be kept to avoid making too many unnecessary calls, such as the 
  * getUserGroups() call for several activities.
  * 
  * Replace global data with database.
  */
 public class Globals
 {
 	//public static ArrayList<GroupData> groupList = new ArrayList<GroupData>();
 	public static GroupData currentGroup = new GroupData();
 	public static AlbumData currentAlbum = new AlbumData();
 	public static PhotoData currentPhoto = new PhotoData();
 	public static EventData currentEvent = new EventData();
 	public static MessageData currentMessage = new MessageData();
 	public static MessageThreadData currentMessageThread = new MessageThreadData();
 	
 	public static ArrayList<GroupData> groupList = new ArrayList<GroupData>();
 	
 	public static String search = "";
 	public static int zipcode = 00000;
 	public static int distance = 10;
 	
 	public static Bitmap getRemoteImage(final String urlString)
 	{
 		try
 		{
			URL url = new URL(urlString);
 			final URLConnection conn = url.openConnection();
 			conn.connect();
 			final BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
 			final Bitmap bm = BitmapFactory.decodeStream(bis);
 			bis.close();
			return bm;
 			
 			//Supposedly fixes images not displaying
 			//Did not work
			/*HttpGet httpRequest = null;
 			
 			try
 			{
 				httpRequest = new HttpGet(new URL(urlString).toURI());
 			}
 			catch(URISyntaxException ex)
 			{
 				System.err.println("Globals/getRemoteImage/" + ex);
 			}
 			
 			HttpClient httpclient = new DefaultHttpClient();
 			HttpResponse response = (HttpResponse)httpclient.execute(httpRequest);
 			HttpEntity entity = response.getEntity();
 			BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
 			InputStream instream = bufHttpEntity.getContent();
			return BitmapFactory.decodeStream(instream);*/
 		}
 		catch(IOException ex)
 		{
 			System.err.println("Globals/getRemoteImage/" + ex);
 		}
 		
 		return null;
 	}
 	
 	public static boolean isUnique(DataObject data, ArrayList<? extends DataObject> list)
 	{
 		if(!list.isEmpty())
 		{
 			for(int i = 0; i < list.size(); i++)
 			{
 				DataObject object = list.get(i);
 				
 				if(object.getId().equals(data.getId()))
 				{
 					return false;
 				}
 			}
 		}
 		
 		return true;
 	}
 	
 	public static void reset()
 	{
 		currentGroup = new GroupData();
 		currentAlbum = new AlbumData();
 		currentPhoto = new PhotoData();
 		currentEvent = new EventData();
 		currentMessage = new MessageData();
 		
 		groupList = new ArrayList<GroupData>();
 		
 		search = "";
 		zipcode = 00000;
 		distance = 10;
 	}
 }
