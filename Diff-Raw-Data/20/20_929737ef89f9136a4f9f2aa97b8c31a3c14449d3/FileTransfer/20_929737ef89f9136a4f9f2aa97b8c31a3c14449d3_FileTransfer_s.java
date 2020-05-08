 package Client;
 /**
  * SOEN 490
  * Capstone 2011
  * Team members: 	
  * 			Sotirios Delimanolis
  * 			Filipe Martinho
  * 			Adam Harrison
  * 			Vahe Chahinian
  * 			Ben Crudo
  * 			Anthony Boyer
  * 
  * @author Capstone 490 Team Moving Target
  *
  */
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.msgpack.MessagePack;
 import org.msgpack.unpacker.Unpacker;
 
 public class FileTransfer {	
	private static final String HOST_NAME = "localhost";
	private static final String HOST_PORT = "8080";
 	private static final String TYPE = ".amr";
 
 	public int uploadFile(File file, String latitude, String longitude, String speed, String email) throws IOException {
 		HttpClient httpClient = new DefaultHttpClient();	
 		HttpPost httpPost = new HttpPost("http://" + HOST_NAME + ":" + HOST_PORT + "/MasterServer/controller?command=createmessage&latitude="
 	                                  + latitude + "&longitude=" + longitude + "&speed=" + speed + "&email=" + email);
 		
 		MultipartEntity entity = new MultipartEntity();
 		entity.addPart("bin", new FileBody(file, "audio/amr"));	
 		httpPost.setEntity(entity);
 		HttpResponse response = httpClient.execute(httpPost);	
 		return response.getStatusLine().getStatusCode();
 	}
 	
 	public Map<String, Message> downloadFiles(String latitude, String longitude, String speed, String responseType, String folder, String sort) throws ClientProtocolException, IOException {
 		HttpClient httpgetIdClient = new DefaultHttpClient();
 		//first we must get the message Ids
 		HttpGet getIds = new HttpGet("http://" + HOST_NAME + ":" + HOST_PORT + "/MasterServer/controller?command=getmessageids&latitude="
                                      + latitude + "&longitude=" + longitude + "&speed=" + speed + "&responsetype=" + responseType + "&sort=" + sort);
 
 		HttpResponse getMessageIdResponse = httpgetIdClient.execute(getIds);
 		InputStream getMessageIdInputStream = getMessageIdResponse.getEntity().getContent();
 		Unpacker unpacker = (new MessagePack()).createUnpacker(getMessageIdInputStream);
 		int size = unpacker.readInt();
 		
 		// now that we have the message ids we can retrieve the messages	
 		Map<String, Message> messages = new HashMap<String, Message>();
 		HttpGet readMessage;
 		HttpResponse readMessageResponse;
 		InputStream inputStream;
 		
 		for(int i = 0; i < size; i++) {
 			String id = unpacker.readString();
 			HttpClient httpgetMessageClient = new DefaultHttpClient();
 			readMessage = new HttpGet("http://" + HOST_NAME + ":" + HOST_PORT + "/MasterServer/controller?command=readmessage&messageid="+ id + "&responsetype=" + responseType);		
 			readMessageResponse = httpgetMessageClient.execute(readMessage);
 			inputStream = readMessageResponse.getEntity().getContent();
 			Message message = Message.createMessage(inputStream, TYPE, folder);
 			messages.put(message.getMessage().getName(), message);
 		}
 		
 		return messages;
 	}
 	
 }
