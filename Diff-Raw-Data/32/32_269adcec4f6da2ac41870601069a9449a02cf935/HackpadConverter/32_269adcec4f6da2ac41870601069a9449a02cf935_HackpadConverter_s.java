 package org.lee.converter;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

 import org.lee.converter.content.Content;
 import org.lee.hackpad.jackpad.JackpadClient;
 
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.oauth.client.OAuthClientFilter;
import com.sun.jersey.oauth.signature.OAuthParameters;
import com.sun.jersey.oauth.signature.OAuthSecrets;
 import com.tumblr.jumblr.JumblrClient;
 import com.tumblr.jumblr.types.TextPost;
 
 public class HackpadConverter 
 {
 	private static final String API_KEYS_FILE = "api_keys.txt";
 	private static final String PADS_FILE = "pads.txt";
 	
 	private String HACKPAD_CLIENT_ID;
 	private String HACKPAD_SECRET;
 	private String TUMBLR_CONSUMER_KEY;
 	private String TUMBLR_CONSUMER_SECRET;
 	private String TUMBLR_TOKEN_KEY;
 	private String TUMBLR_TOKEN_SECRET;
 	
	private JackpadClient clientJackpad;
	private JumblrClient clientTrumblr;
 	
 	private String[] padsID;
 
 	public HackpadConverter()
 	{
 		setApiKey();
 		setPadID();
 		jackpadBuild();
 		tumblrBuild();
 	}
 	
 	public void jackpadBuild()
 	{
		clientJackpad = new JackpadClient(HACKPAD_CLIENT_ID, HACKPAD_SECRET);
		clientJackpad.build();
 	}
 	
 	public void tumblrBuild()
 	{
		clientTrumblr = new JumblrClient(TUMBLR_CONSUMER_KEY, TUMBLR_CONSUMER_SECRET);
		clientTrumblr.setToken(TUMBLR_TOKEN_KEY, TUMBLR_TOKEN_SECRET);
 	}
 	
 	public void run()
 	{
 		for(int i = 0; i < padsID.length; i++)
 		{
 			String padText = getPadText(padsID[i]);
 			Content content = new Content(padText);
 			content.createIndex();
 			updatePadTextToTumblr(content.getTitle(), content.getContent());
 			System.out.println(padsID[i] + "Succeed");
 		}
 	}
 	
 	public String getPadText(String padID)
 	{
		String padText = clientJackpad.getPadContentHTML("g0v", padID, "latest");
 		System.out.println(padText);	
 		return padText;
 	}
 	
 	public void updatePadTextToTumblr(String tumblrTitle, String tumblrContent)
 	{	
 		try
 		{
			TextPost post = clientTrumblr.newPost("g0vtw.tumblr.com", TextPost.class);
 			post.setFormat("html");
 			post.setBody(tumblrContent);
 			post.setTitle(tumblrTitle);
 			post.setState("draft");
 			post.save();
 		}
 		catch (Exception e)
 		{
 			System.out.println(e.toString());
 		}
 		
 	}
 	
 	public void setApiKey()
 	{
 		try
 		{
 			FileReader fr = new FileReader(API_KEYS_FILE);
 			BufferedReader br = new BufferedReader(fr);
 			String line;
 			for(int i = 0; i < 3; i++)
 			{
 				line = br.readLine();
 				String[] para= line.split(" ");
 				if(i == 0 && line != null)
 				{
 					HACKPAD_CLIENT_ID = para[0];
 					HACKPAD_SECRET = para[1];
 				}
 				else if(i ==1 && line !=null)
 				{
 					TUMBLR_CONSUMER_KEY = para[0];
 					TUMBLR_CONSUMER_SECRET = para[1];
 				}
 				else if(i ==2 && line !=null)
 				{
 					TUMBLR_TOKEN_KEY = para[0];
 					TUMBLR_TOKEN_SECRET = para[1];
 				}
 			}
 			br.close();
 			fr.close();
 		}
 		catch(IOException e)
 		{
 			System.out.println(e.toString());
 		}
 	}
 	
 	public void setPadID()
 	{
 		try
 		{
 			FileReader fr = new FileReader(PADS_FILE);
 			BufferedReader br = new BufferedReader(fr);
 			String line;
 			String tmpCom = "";
 			while((line = br.readLine()) != null)
 			{
 				tmpCom += line + " ";
 			}
 			br.close();
 			fr.close();
 			padsID = tmpCom.split(" ");
 		}
 		catch(IOException e)
 		{
 			System.out.println(e.toString());
 		}
 	}
 	
 	public static void main(String[] args) 
 	{
 		// TODO Auto-generated method stub
 		HackpadConverter hackpadConverter = new HackpadConverter();
 		hackpadConverter.run();
 		
 	}
 
 }
