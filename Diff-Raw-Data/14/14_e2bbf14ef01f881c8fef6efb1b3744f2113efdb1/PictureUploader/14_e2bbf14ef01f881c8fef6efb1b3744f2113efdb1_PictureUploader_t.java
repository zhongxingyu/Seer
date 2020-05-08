 package tice.twitterwalk.HttpClient;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 
 import tice.twitterwalk.Util.Base64Encoder;
 
 import android.os.Bundle;
 import android.os.Handler;
 
 class PictureUploader {
 
 	static String CRLF = "\r\n";
 	static String twoHyphens = "--";
 	static String boundary = "*****tice.twitterwalk*****";
 
 	private String status = "";
 	private String postUrl = null;
 	private String name = null;
 	private String password = null;
 	private Handler handler = null;
 	private DataOutputStream dataStream = null;
 
 	public PictureUploader(Handler handler, String uri, String name, String password, String status)
 	{
 		this.postUrl = uri;
 		this.name = name;
 		this.password = password;
 		this.handler = handler;
 		this.status = status;
 	}
 	
 	public boolean uploadPicture(String pictureFileName, InputStream data)
 	{	
 		if (data != null){
 			try
 			{
 //				URL connectURL = new URL(postUrl);
 				URL connectURL = new URL("http://api.t.sina.com.cn/statuses/upload.json");
 				HttpURLConnection conn = (HttpURLConnection)connectURL.openConnection();
 				conn.setDoInput(true);
 				conn.setDoOutput(true);
 				conn.setUseCaches(false);
 				conn.setRequestMethod("POST");
 				conn.setRequestProperty("User-Agent", "myGeoDiary-V1");
 				conn.setRequestProperty("Connection","Keep-Alive");
 				conn.setRequestProperty("Content-Type","multipart/form-data;boundary="+boundary);
 				String authorization = "Basic " +
                 	new String(new Base64Encoder().encode((name + ":" + password).getBytes()));
 				conn.addRequestProperty("Authorization", authorization);
 				
 				conn.connect();
 
 				dataStream = new DataOutputStream(conn.getOutputStream());
 
 				writeFormField("status", status);
 				writeFormField("source", "1390045420");
 				writeFileField("pic", pictureFileName, "image/jpg", data);
 
 				// final closing boundary line
 				dataStream.writeBytes(twoHyphens + boundary + twoHyphens + CRLF);
 
 				dataStream.flush();
 				dataStream.close();
 				dataStream = null;
 
 				String response = getResponse(conn);
 
				if (response.startsWith("{")) {
 	    			Bundle ret = new Bundle();
 	    			ret.putString(TwitterClient.KEY, response);
 					TwitterClient.SendMessage(handler, TwitterClient.HTTP_POSTIMAGE_SUCCESSFUL, ret);
 					return true;
 				}
 				else {
 	    			Bundle ret = new Bundle();
 	    			ret.putString(TwitterClient.KEY, response);
 					TwitterClient.SendMessage(handler, TwitterClient.HTTP_POSTIMAGE_ERROR, ret);
 					return false;
 				}
 			}
 			catch (MalformedURLException e) {
     			Bundle ret = new Bundle();
     			ret.putString(TwitterClient.KEY, e.getLocalizedMessage());
 				TwitterClient.SendMessage(handler, TwitterClient.HTTP_POSTIMAGE_ERROR, ret);
 				return false;
 			}
 			catch (IOException e) {
     			Bundle ret = new Bundle();
     			ret.putString(TwitterClient.KEY, e.getLocalizedMessage());
 				TwitterClient.SendMessage(handler, TwitterClient.HTTP_POSTIMAGE_ERROR, ret);
 				return false;
 			}
 			catch (Exception e) {
     			Bundle ret = new Bundle();
     			ret.putString(TwitterClient.KEY, e.getLocalizedMessage());
 				TwitterClient.SendMessage(handler, TwitterClient.HTTP_POSTIMAGE_ERROR, ret);
 				return false;
 			}
 		}
 		else
 		{
 			return false;
 		}
 	}
 
 	private String getResponse(HttpURLConnection conn)
 	{
 		try {
 			// try doing this in one read
 			DataInputStream dis = new DataInputStream(conn.getInputStream());
 			byte[] data = new byte[1024];
 			int len = dis.read(data, 0, 1024);
 
 			return new String(data, 0, len);
 		}
 		catch(Exception e) {}
 		return "";
 	}
 
 	private void writeFormField(String fieldName, String fieldValue)
 	{
 		try{
 			dataStream.writeBytes(twoHyphens + boundary + CRLF);
 			dataStream.writeBytes("Content-Disposition: form-data;name=\"" + fieldName + "\"" + CRLF);
 			dataStream.writeBytes(CRLF);
 			dataStream.writeBytes(fieldValue);
 			dataStream.writeBytes(CRLF);
 		}
 		catch(Exception e){ }
 	}
 	
 	private void writeFileField(String fieldName, String fieldValue, String type, InputStream data)
 	{
 		try
 		{
 			// opening boundary line
 			dataStream.writeBytes(twoHyphens + boundary + CRLF);
 			dataStream.writeBytes("Content-Disposition: form-data;name=\""
 					+ fieldName
 					+ "\";filename=\""
 					+ fieldValue
 					+ "\""
 					+ CRLF);
 			dataStream.writeBytes("Content-Type: " + type +  CRLF);
 			dataStream.writeBytes(CRLF);
 
 			// create a buffer of maximum size
 			int bytesAvailable = data.available();
 			int maxBufferSize = 1024;
 			int bufferSize = Math.min(bytesAvailable, maxBufferSize);
 			byte[] buffer = new byte[bufferSize];
 			// read file and write it into form...
 			int bytesRead = data.read(buffer, 0, bufferSize);
 			int maxsize = bytesAvailable;
 			int totalread = bytesRead;
 			Bundle ret = new Bundle();
 			while (bytesRead > 0)
 			{
 				dataStream.write(buffer, 0, bufferSize);
 				bytesAvailable = data.available();
 				bufferSize = Math.min(bytesAvailable, maxBufferSize);
 				bytesRead = data.read(buffer, 0, bufferSize);
 				totalread += bytesRead; 
 				ret.putInt("progress_max", maxsize);
 				ret.putInt("progress_cur", totalread);
 				TwitterClient.SendMessage(handler, TwitterClient.HTTP_POSTIMAGE_PROGRESS, ret);
 
 			}
 
 			// closing CRLF
 			dataStream.writeBytes(CRLF);
 			data.close();
 		}
 		catch(Exception e) { }
 	}
 }
