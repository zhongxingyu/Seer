 package org.ritsuka.youji.handlers;
 
 import akka.actor.ActorRef;
 import com.google.gson.Gson;
 import org.jivesoftware.smack.Chat;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smack.packet.Message;
 import org.jivesoftware.smack.util.StringUtils;
 import org.jivesoftware.smackx.muc.MultiUserChat;
 import org.ritsuka.natsuo.Log;
 import org.ritsuka.youji.muc.IMucMsgHandler;
 import org.ritsuka.youji.pm.IPmHandler;
 import org.ritsuka.youji.util.CommandParser;
 import org.ritsuka.youji.util.HTTPGet;
 import org.ritsuka.youji.util.XMPPUtil;
 import org.slf4j.LoggerFactory;
 
 import java.util.List;
 
 /**
  * Date: 04.10.11
  * Time: 16:30
  */
 public class GoogleIt implements IPmHandler, IMucMsgHandler{
 
     private ActorRef worker;
     private MultiUserChat muc;
 
     private Log log() {
         return new Log(LoggerFactory.getLogger(GoogleIt.class));
     }
 
     private final class SearchResult {
         private String titleNoFormatting;
         private String content;
         private String unescapedUrl;
 
         public String getTitleNoFormatting() {
             return titleNoFormatting;
         }
 
         public void setTitleNoFormatting(String titleNoFormatting) {
             this.titleNoFormatting = titleNoFormatting;
         }
 
         public String getContent() {
             return content;
         }
 
         public String getPlainContent() {
             return content.replaceAll("(<b>)|(</b>)", "");
         }
 
         public void setContent(String content) {
             this.content = content;
         }
 
         public String getUnescapedUrl() {
             return unescapedUrl;
         }
 
         public void setUnescapedUrl(String unescapedUrl) {
             this.unescapedUrl = unescapedUrl;
         }
     }
 
     private final class ResponseData {
         private List<SearchResult> results;
 
         public List<SearchResult> getResults() {
             return results;
         }
 
         public void setResults(List<SearchResult> results) {
             this.results = results;
         }
     }
 
     private final class GoogleSearchResult {
         private ResponseData responseData;
         private Integer responseStatus;
 
         public ResponseData getResponseData() {
             return responseData;
         }
 
         public void setResponseData(ResponseData responseData) {
             this.responseData = responseData;
         }
 
         public Integer getResponseStatus() {
             return responseStatus;
         }
 
         public void setResponseStatus(Integer responseStatus) {
             this.responseStatus = responseStatus;
         }
     }
 
     @Override
     public IMucMsgHandler setContext(ActorRef worker, MultiUserChat chat) {
         this.worker = worker;
         this.muc = chat;
         return this;
     }
 
 
 
     @Override
     public void handleMucMsg(Message message) {
         if (!XMPPUtil.isUsualMessage(message))
             return;
 
         if (XMPPUtil.isYoujiMessage(message, muc))
             return;
         Message reply = googleIt(message.getBody(), message);
 
         if (null == reply)
             return;
 
         try {
             reply.setType(Message.Type.groupchat);
             reply.setTo(muc.getRoom());
             muc.sendMessage(reply);
         } catch (XMPPException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 
     @Override
     public IPmHandler setContext(ActorRef worker) {
         this.worker = worker;
         return this;
     }
 
     @Override
     public void handlePm(Chat chat, Message message) {
         Message reply = googleIt(message.getBody(), message);
         if (null == reply)
             return;
         try {
             chat.sendMessage(reply);
         } catch (XMPPException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 
     private Message googleIt(final String rawQuery, Message sourceMessage) {
         String query = rawQuery;
         CommandParser.ParsedCommand cmd = new CommandParser(rawQuery).parse();
         if (null == cmd)
             return null;
 
         if (!(cmd.is("g") || cmd.is("г")))
             return null;
 
         query = cmd.getRawArg();
         Message reply = new Message();
 
         query = HTTPGet.urlencode(query);
         String urlToLoad = String.format("http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=%s&hl=ru", query);
         try {
             String output = HTTPGet.getHTML(urlToLoad);
             GoogleSearchResult result = (new Gson()).fromJson(output, GoogleSearchResult.class);
             ResponseData response = result.getResponseData();
 
             StringBuilder sb = new StringBuilder();
             sb.append(StringUtils.parseResource(sourceMessage.getFrom()));
             sb.append(", here is response for your request: ");
 
             if (null != response) {
                 List<SearchResult> results = response.getResults();
                 if (null != results && results.size() > 0) {
                     sb.append('\n').append(linksToMessage(reply, results));
                 }
                 else
                     sb.append("Nothing found");
             } else {
                 sb.append("Can't parse google's reply: ").append(output);
             }
             reply.setBody(sb.toString());
 
         } catch (Throwable e) {
             String error = e.toString();
             log().error("Can't process google request: {}", error);
             reply.setBody("Error processing request: "+error);
             e.printStackTrace();
         }
         return reply;
     }
 
     private String linksToMessage(Message reply, List<SearchResult> results) {
         StringBuilder sb = new StringBuilder();
         //StringBuilder sxb = new StringBuilder();
         for (SearchResult link: results) {
             String title = link.getTitleNoFormatting();
             String url = link.getUnescapedUrl();
 
             /*sxb.append("<a href=\"");
             sxb.append(link);
             sxb.append("\">");
             sxb.append(link.getContent());
             sxb.append("</a><br/>");*/
 
             sb.append("{");
             sb.append(title);
             sb.append("}\n");
             sb.append(url);
             sb.append("\n");
             sb.append(link.getPlainContent());
             sb.append("\n\n");
         }
 
         //XHTMLManager.addBody(reply, sxb.toString());
         return sb.toString();
     }
 }
