 /*******************************************************************************
  * Copyright (c) 1999-2011 Percussion Software.
  * 
  * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
  * 
  * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  ******************************************************************************/
 package com.percussion.pso.jexl;
 
 import java.io.IOException;
 import java.io.FileInputStream;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.Message;
 
 import com.percussion.extension.PSExtensionException;
 
 import org.apache.commons.codec.DecoderException;
 import org.apache.commons.codec.net.URLCodec;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.mail.javamail.MimeMessageHelper;
 import org.w3c.tidy.ParserImpl.ParseInline;
 
 import com.percussion.extension.IPSJexlExpression;
 import com.percussion.extension.IPSJexlMethod;
 import com.percussion.extension.IPSJexlParam;
 import com.percussion.extension.PSJexlUtilBase;
 import com.percussion.pso.syndication.PSSynFeedProxy;
 import com.percussion.pso.utils.HTTPProxyClientConfig;
 import com.sun.syndication.feed.module.Module;
 import com.sun.syndication.feed.module.content.ContentModule;
 import com.sun.syndication.feed.module.mediarss.MediaEntryModule;
 import com.sun.syndication.feed.module.mediarss.MediaModule;
 import com.sun.syndication.feed.module.mediarss.types.MediaContent;
 import com.sun.syndication.feed.module.mediarss.types.MediaGroup;
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.sun.syndication.io.FeedException;
 import com.sun.syndication.io.SyndFeedInput;
 import com.sun.syndication.io.XmlReader;
 
 import com.percussion.server.PSServer;
 /***
  * Provides tools to work with Feeds such as RSS or Atom
  * @author natechadwick
  *
  */
 public class PSOFeedTools extends PSJexlUtilBase implements IPSJexlExpression {
 
 	  /**
 	    * Logger for this class
 	    */
 	private static final Log log = LogFactory.getLog(PSOFeedTools.class);
 
 
 	
 	public PSOFeedTools(){
 		super();
 	}
 	
 	 @IPSJexlMethod(description="Returns a Map of Feed parameters from a Jquery formatted feed. For example a post of url: 'http://rss.news.yahoo.com/rss/yahoonewsroom', targetFolder: '//Sites/EnterpriseInvestments/News'", 
 	         params={@IPSJexlParam(name="params", description="The feed parameters")})
 	   public Map<String,String> getFeedParameters(String params) throws IllegalArgumentException, FeedException, IOException {
 
 		 log.debug(params);
 		 URLCodec decode = new URLCodec();
 		 HashMap<String,String> map = new HashMap<String,String>();
 		 
 		 
 		 String[] opts = params.split("&");
 		 String[] param;
 		 for(String s : opts){
 			 param = s.split("=");
 			 
 			 try {
 				map.put(decode.decode(param[0].trim()), decode.decode(param[1].trim()));
 			} catch (DecoderException e) {
 				log.debug(params);
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		 }
 		 
 		return map;
 	 }
 
 	 @IPSJexlMethod(description="Returns a ROME SyndFeed instance for the given URL", 
 	         params={@IPSJexlParam(name="url", description="The URL of the feed to download.")})
 	   public PSSynFeedProxy getFeed(String urlString) throws IllegalArgumentException, FeedException, IOException {
 		 return new PSSynFeedProxy(urlString);	
 	 	}
 		 
 	 @IPSJexlMethod(description="Returns a ROME SyndFeed instance for the given URL.  Supports Optional GET based upon cached ETAG and LastModified Date.  Do not use unless cacheing these values.", 
 	         params={@IPSJexlParam(name="url", description="The URL of the feed to download."),
 			 		 @IPSJexlParam(name="eTag", description="A cached HTTP ETAG Headerfor the feed. "),
 			 		 @IPSJexlParam(name="lastModified", description="A cached HTTP LastModified Header for the feed. ")})
 	   public PSSynFeedProxy getFeed(String urlString, String eTag, String lastModified) throws IllegalArgumentException, FeedException, IOException {
 
 		 return new PSSynFeedProxy(urlString,eTag,lastModified);	
 	
 	 }
 	 
 	 @IPSJexlMethod(description="Does a little cleanup of the sys_title.  URL encoding it if possible.", 
 	         params={@IPSJexlParam(name="title", description="The sys_title to cleanup")})
   	  public String cleanupItemTitle(String title){
 		 String ret=title;
 		 URLCodec codec = new URLCodec();
 		 
 		 ret = ret.replace(" ", "_");
 		 try{
 			 ret = codec.encode(ret);
 		 }catch(Exception ex){
 			 log.debug("Url encoding issue in cleanupItemTitle " + ex.getMessage());
 		 }
 		 return ret;
 	 }
  	
 	 @IPSJexlMethod(description="Returns a ROME MediaEntryModule to handle Media RSS content.", 
 	         params={@IPSJexlParam(name="entry", description="A ROME SyndEntry instance.")})
 	  public List<MediaContent> getMediaRSSModuleContent(Object entry){
 		  MediaEntryModule mediaModule = null;
 		  SyndEntry item = (SyndEntry) entry;
 		  List<MediaContent> contents = new ArrayList<MediaContent>();
 		  
 		  mediaModule = (MediaEntryModule) item.getModule(MediaEntryModule.URI);
 		  if (mediaModule!=null && mediaModule instanceof MediaEntryModule ){
 		        MediaEntryModule mentry = (MediaEntryModule ) mediaModule;
 
 		       
 		        for (MediaGroup mg : mentry.getMediaGroups()) {
 		            for (MediaContent mc : mg.getContents()) {
 		            	contents.add(mc);
 		            }
 		        }
 		  }
 		  return contents;
 	  }
 	 
 	 @IPSJexlMethod(description="Returns a boolean if the specified module is available.",
 			 params ={@IPSJexlParam(name="entry", description="A ROME SyndEntry instance.")})
 	  public boolean isMediaRSSContentAvailable(Object entry){
 		  boolean ret=false;
 		  SyndEntry e = (SyndEntry) entry;
 		
 		  if(e!=null){
 			  MediaEntryModule mediaModule = (MediaEntryModule) e.getModule(MediaEntryModule.URI);
 			 
 			  //First check to make sure there is Media RSS content.
 			  if (mediaModule!=null && mediaModule instanceof MediaEntryModule ){
 				  
 				  //Now check at the content level.
 				  if(mediaModule.getMediaContents().length>0){
 					  ret = true;
 				  }
 				  
 				  //Now check at the group level for alternative formats, could be either or.
 				  if(mediaModule.getMediaGroups().length>0){
 					  for(int i = 0;i<mediaModule.getMediaGroups().length;i++){
 						  if(mediaModule.getMediaGroups()[i].getContents().length>0){
 							  ret = true;
 							  break;
 						  }
 					  }
 				  }
 			  }
 		  }
 		  return ret;
 	  }
 	 
 	 @IPSJexlMethod(description="Returns the base filename portion for a given url.",
 			 params ={@IPSJexlParam(name="entry", description="A valid url")}) 
 	 public String getBaseFilename(String url){
 		 String ret = "";
 		 
 		 if(url!=null && url.trim()!=""){
 			url = url.replace("\\","/");
 			if(url.lastIndexOf("/") == url.length()){
 				url = url.substring(0,url.length()-1);
 			}
 			
 			ret = url.substring(url.lastIndexOf("/"));
 		 }
 		 return ret;
 	 }
 	 
 	 @IPSJexlMethod(description="Returns a folder name of the right length.",
 			 params ={@IPSJexlParam(name="folder", description="Folder name")}) 
 	 public String getCleanFoldername(String folder){
 		 if(folder.length()>100)
 			 return folder.substring(0, 100);
 		 else
 			 return folder;
 	 }
 	 
 	 
 	 
 	 
 	 
 	 @IPSJexlMethod(description="Sends an email to the specified Workflow role",
 			 params ={@IPSJexlParam(name="to_line", description="Who gets the email"),
 			 @IPSJexlParam(name="cc_line", description="Who gets the email"),
 			 @IPSJexlParam(name="subject", description="The role that gets the email"),
 			 @IPSJexlParam(name="body", description="The role that gets the email")			 
 	 	}) 
 	 public void sendEmail(String to_line, String cc_line, String subject, String body ){		 
 
 	
 	try
 	{
 		 String m_smtpHost = null;
 		 String test = null;
 		 Properties rxconfigProps = new Properties();
 
 		 Date dateNow = new Date();
      	 SimpleDateFormat presentDate = new SimpleDateFormat("yyyy-MM-dd");
      	 StringBuilder systemDate = new StringBuilder(presentDate.format(dateNow));
      	// Date date = Date.parse(systemDate.toString());
 		 
      	// rxconfigProps.load(new FileInputStream("rxconfig/Workflow/rxworkflow.properties"));
 		 String propFile = PSServer.getRxFile(PSServer.BASE_CONFIG_DIR + "/rxconfig/Workflow/rxworkflow.properties");
 		 String host = rxconfigProps.getProperty("RX_SERVER_HOST_NOTIFICATION");
          String sPort = rxconfigProps.getProperty("RX_SERVER_PORT_NOTIFICATION");
          String smtp_Host = rxconfigProps.getProperty("SMTP_HOST");
          String domain = rxconfigProps.getProperty("MAIL_DOMAIN");
          int port = Integer.parseInt(sPort);
          
         InternetAddress m_From = new InternetAddress("anvitha_ganesh@precussion.com");
          InternetAddress m_To = new InternetAddress(to_line);
          InternetAddress m_Cc = new InternetAddress(cc_line);
          
          //Doubt abt props being null
          Properties props = System.getProperties();
         props.put("mail.smtp.host", smtp_Host);
         Session session = Session.getDefaultInstance(props);
         MimeMessage message = new MimeMessage(session);
         
         message.setHeader("Testing", test);
         message.addRecipient(javax.mail.Message.RecipientType.TO, m_To);
          message.setFrom(m_From);
          message.addRecipient(javax.mail.Message.RecipientType.CC, m_Cc);
          message.setSubject(subject);
          //message.setSentDate(systemDate);
          
         String m_Body = "";
         StringBuffer buffer = new StringBuffer();
          buffer.append("Hi \n");
          buffer.append(body);
          buffer.append("\n\n");
         m_Body = buffer.toString();
          
          message.setText(m_Body);
          Transport.send(message);
 	  }
 	
 	catch(Exception e){
 		log.error(e);
 	}
 	
 	 }
 	}
