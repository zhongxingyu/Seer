 package tests.application;
 
 import static org.junit.Assert.*;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.math.BigInteger;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.utils.URIUtils;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.ContentBody;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;
 import org.msgpack.MessagePack;
 import org.msgpack.unpacker.Unpacker;
 
 import domain.message.Message;
 import domain.message.MessageFactory;
 
 public class HttpMessageTest {
 	
 	static String serverUrl = "/MasterServer/frontController";
 	
 	public void test() throws URISyntaxException, ClientProtocolException, IOException
 	{
 		ArrayList<Message> messages = get();
 		assertEquals(0, messages.size());
 		long mid = put();
 		messages = get();
 		assertEquals(1, messages.size());
 		assertEquals(mid, messages.get(0).getMid());
 		/*upvote(mid);
 		messages = get();
 		assertEquals(1, messages.size());
 		assertEquals(1, messages.get(0).getUserRating());
 		downvote(mid);
 		messages = get();
 		assertEquals(0, messages.get(0).getUserRating());*/
 	}
 	
 	private ArrayList<Message> get() throws URISyntaxException, ClientProtocolException, IOException
 	{
 		int port = 8080;
 		URI upvoteURI = URIUtils.createURI("http", "localhost", port, serverUrl, "command=GetMessages&longitude=0&latitude=0", null);
 		HttpGet httpGet = new HttpGet(upvoteURI);
 		HttpClient httpClient = new DefaultHttpClient();
 		HttpResponse response = httpClient.execute(httpGet);
 		System.out.println(new BufferedReader(new InputStreamReader(response.getEntity().getContent())).readLine());
 		assertEquals("HTTP/1.1 200 OK", response.getStatusLine().toString());
 		Unpacker unpacker = new MessagePack().createUnpacker(response.getEntity().getContent());
 		int messageCount = unpacker.readInt();
 		ArrayList<Message> arrayList = new ArrayList<Message>();
 		for (int i = 0; i < messageCount; ++i) {
 			BigInteger mid = unpacker.readBigInteger();
 			String email = unpacker.readString();
 			byte[] message = unpacker.readByteArray();
 			float speed = unpacker.readFloat();
 			Timestamp createdAt = new Timestamp(unpacker.readLong());
 			double longitude = unpacker.readDouble();
 			double latitude = unpacker.readDouble();
 			int userRating = unpacker.readInt();
 			arrayList.add(MessageFactory.createClean(mid, 0, message, speed, latitude, longitude, createdAt, userRating, 0));
 		}
 		return arrayList;
 	}
 	
 	public long put() throws URISyntaxException, ClientProtocolException, IOException
 	{
 		int port = 8080;
 		URI upvoteURI = URIUtils.createURI("http", "localhost", port, serverUrl, null, null);
 		HttpClient httpClient = new DefaultHttpClient();
 		HttpPost httpPost = new HttpPost(upvoteURI);
 		httpPost.getParams().setDoubleParameter("longitude", 0.0);
 		httpPost.getParams().setDoubleParameter("latitude", 0.0);
 		httpPost.getParams().setParameter("command", "PutMessage");
 		String fileName = "test.amr";
 		File file = new File(fileName);
 		MultipartEntity mpEntity = new MultipartEntity();
 		ContentBody cbFile = new FileBody(file, "audio/AMR");
 		mpEntity.addPart("bin", cbFile);
 		
 		httpPost.setEntity(mpEntity);
 		HttpResponse response = httpClient.execute(httpPost);
 		System.out.println(new BufferedReader(new InputStreamReader(response.getEntity().getContent())).readLine());
 		assertEquals("HTTP/1.1 202 Accepted", response.getStatusLine());
 		Unpacker unpacker = new MessagePack().createUnpacker(response.getEntity().getContent());
 		return unpacker.readLong();
 	}
 	
 	private void upvote(long mid) throws URISyntaxException, ClientProtocolException, IOException
 	{
 		int port = 8080;
 		URI upvoteURI = URIUtils.createURI("http", "localhost", port, serverUrl, 
 			    "command=UpvoteMessage&mid=" + mid, null);
 		HttpClient httpClient = new DefaultHttpClient();
 		HttpPost httpPost = new HttpPost(upvoteURI);
 		HttpResponse response = httpClient.execute(httpPost);
 		assertEquals("HTTP/1.1 202 Accepted", response.getStatusLine());
 	}
 	
 	private void downvote(long mid) throws URISyntaxException, ClientProtocolException, IOException
 	{
 		int port = 8080;
 		URI upvoteURI = URIUtils.createURI("http", "localhost", port, serverUrl, 
 			    "command=DownvoteMessage&mid=" + mid, null);
 		HttpClient httpClient = new DefaultHttpClient();
 		HttpPost httpPost = new HttpPost(upvoteURI);
 		HttpResponse response = httpClient.execute(httpPost);
 		assertEquals("HTTP/1.1 202 Accepted", response.getStatusLine());
 	}
 }
