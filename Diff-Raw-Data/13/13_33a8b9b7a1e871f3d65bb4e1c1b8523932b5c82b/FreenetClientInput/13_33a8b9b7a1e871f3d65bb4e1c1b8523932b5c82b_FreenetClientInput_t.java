 package plugin.frirc;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.net.MalformedURLException;
 import java.net.Socket;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerFactoryConfigurationError;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.w3c.dom.Document;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import com.db4o.ObjectContainer;
 import freenet.client.FetchContext;
 import freenet.client.FetchException;
 import freenet.client.FetchResult;
 import freenet.client.HighLevelSimpleClient;
 import freenet.client.async.ClientGetCallback;
 import freenet.client.async.ClientGetter;
 import freenet.keys.FreenetURI;
 
 public class FreenetClientInput extends FreenetClient implements ClientGetCallback {
 
 
 	private FreenetURI requestURI;
 	private long latest_edition = 0;
 	
 	private long latest_message_from = 0; //when did we receive the latest message from this identity?
 	
 	public FreenetClientInput(Socket socket, IRCServer server, HighLevelSimpleClient priority, HighLevelSimpleClient low_priority, String channel, String nick) throws TransformerConfigurationException, ParserConfigurationException, TransformerFactoryConfigurationError {
 		super(socket, priority, low_priority, channel, nick);
 		this.server = server;
 	}
 
 	public void setRequestURI(FreenetURI requestURI)
 	{
 		this.requestURI = requestURI;
 	}
 
 	/**
 	 * Callback called when requesting the latest channel-edition doesn't exist yet
 	 */
 
 	@Override
 	public void onFailure(FetchException arg0, ClientGetter arg1, ObjectContainer arg2) {
 
		if (isOldFailedURI(arg1.getURI())) return;
 		if (stopThread()) return;
 		
 		if (!this.calibrated)
 		{
 			//failed? -> good, we've found our latest index to publish to
 			System.out.println("CALIBRATED FREENET CLIENT LISTENER!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
 			this.calibrated = true;
 			pollForNewMessage(true);
 		}
 	}
 
 	@Override
 	public void onSuccess(FetchResult fetchResult, ClientGetter arg1, ObjectContainer arg2) {
 
 		if (isOldSuccessURI(arg1.getURI())) return;
 		if (stopThread()) return;
 		
 		if (!calibrated)
 		{
 			//index already exists? increase the count with one and try again
 			latest_edition++;
 			pollForNewMessage(false);
 		}
 		
 		//We've retrieved a new message! deal with it
 		else
 		{
 			//increase the latest edition that we want to poll for (since we've received the current edition
 			latest_edition++;
 			pollForNewMessage(true);
 			
 			//parse message in result as an xmldoc
 			Document xml;
 			try {
 				xml = parseXML(new String(fetchResult.asByteArray()));
 				
 				System.out.println(fetchResult.asByteArray());
 				
 				//determine type of message
 				XPath xpath = XPathFactory.newInstance().newXPath();
 				String type = "UNKNOWN";
 				try {
 					type = xpath.evaluate("//Message/@type", xml);
 				} catch (XPathExpressionException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				
 				if (type.equals("privmsg"))
 				{
 					String message = xpath.evaluate("//Message/text()", xml);
 					long timestamp = Long.parseLong(xpath.evaluate("//Message/@timestamp", xml));
 					this.server.messageFromFreenet(this, Frirc.requestURItoID(requestURI), this.channel, message, timestamp);
 				}
 				else if (type.equals("channelping"))
 				{
 					System.out.println("I have found a channelping!");
 				}
 				
 				
 				//no matter the messagetype update the latest_message_received time
 				latest_message_from = Long.parseLong( xpath.evaluate("//Message/@timestamp", xml) );
 				
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (XPathExpressionException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			catch(Exception e)
 			{
 				e.printStackTrace();
 			}
 		}
 	}
 
 	
 	private Document parseXML(String unparsedXML)
 	{
 		StringReader reader = new StringReader(unparsedXML);
 		InputSource inputSource = new InputSource( reader );
 		
 		try {
 			return mDocumentBuilder.parse(inputSource);
 		} catch (SAXException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return null;
 	}
 	
 	
 	@Override
 	public void onMajorProgress(ObjectContainer arg0) {
 		// TODO Auto-generated method stub
 	}
 	
 	/**
 	 * Find the latest key used by this user
 	 */
 
 	private synchronized void pollForNewMessage(boolean ulpr)
 	{
 		FetchContext fc = low_priority_hl.getFetchContext();
 		fc.maxNonSplitfileRetries = 1;
 		fc.followRedirects = true;
 		fc.ignoreStore = true;
 
 		
 		if (ulpr)
 		{
 			fc.maxNonSplitfileRetries = -1;
 		}
 		
 		if (last_startpoint != Frirc.currentIndex())
 		{
 			last_startpoint = Frirc.currentIndex();
 			latest_edition = 0;
 		}
 
 		try {
 			FreenetURI fetchURI = new FreenetURI(requestURI.toString() + "-" + last_startpoint + "-" + latest_edition + "/feed");
 			if (!isOldFailedURI(fetchURI))
 			{
 				System.out.println("Watching: " + fetchURI);
 				hl.fetch(fetchURI, 20000, this, this, fc);
 			}
 		} catch (FetchException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void run()
 	{
 		
 		pollForNewMessage(false);
 		
 		while(true)
 		{
 			if (calibrated)
 			{
 				//we are calibrated? cool! 
 				//start a ULPR for the next index! and manage the ulpr's (possibly cancel them) in this thread
 			
 				if (last_startpoint != Frirc.currentIndex())
 				{
 					pollForNewMessage(true);
 				}
 			}
 			
 			if (stopThread()) return;
 			
 			//check whether the last seen timestamp is too long ago... if yes:
 			// leave channel and stop thread
 			
 			if (latest_message_from != 0 && calibrated && latest_message_from < System.currentTimeMillis() - Frirc.TIMEOUT)
 			{
 				System.out.println("Leaving channel");
 				server.leaveChannel(this);
 				return;
 			}
 			
 			
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	public boolean isFreenetClientInput()
 	{
 		return true;
 	}
 
 
 }
