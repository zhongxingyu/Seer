 package rssReader;
 
 import org.horrabin.horrorss.RssChannelBean;
 import org.horrabin.horrorss.RssFeed;
 import org.horrabin.horrorss.RssItemBean;
 import org.horrabin.horrorss.RssParser;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smackx.pubsub.*;
 import org.w3c.dom.CharacterData;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.xml.sax.SAXException;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.List;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Created with IntelliJ IDEA.
  * User: ytihoglaz
  * Date: 21.08.13
  * Time: 15:55
  * To change this template use File | Settings | File Templates.
  */
 public class Feed implements Runnable{
 
     private final String configFileName;
 
     private final RssParser rss;
 
     public Feed(
             String configFileName,
             URL link,
             int checkInterval,
             String feedName,
             JabberClient jabber,
             String newsHub,
             Level logLevel, Properties config) throws ParserConfigurationException, NoSuchAlgorithmException, CloneNotSupportedException {
         this.configFileName = configFileName;
         this.link = link;
         this.checkInterval = checkInterval;
         this.feedName = feedName;
         this.jabber = jabber;
         this.newsHub = newsHub;
         this.logLevel = logLevel;
         this.config = config;
         this.isFirstRun = config.getProperty("firstrun").equals("true");
         this.rss = new RssParser();
         LOGGER.setLevel(this.logLevel);
 
         md = MessageDigest.getInstance("MD5");
 
         builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
     }
 
     private final URL link;
     private final int checkInterval;
     private final String feedName;
     private final JabberClient jabber;
     private final String newsHub;
     private final DocumentBuilder builder;
     private final Level logLevel;
     private final Properties config;
     private MessageDigest md;
 
     private final static Logger LOGGER = Logger.getLogger(Feed.class.getName());
 
     public boolean isFirstRun() throws ClassNotFoundException, SAXException, InstantiationException, IllegalAccessException, IOException {
         return isFirstRun;
     }
 
     public void setFirstRun(boolean firstRun) {
         isFirstRun = firstRun;
 
 
         config.setProperty("firstrun", firstRun ? "true" : "false" );
 
         try {
             config.store(new FileOutputStream(configFileName), null);
         } catch (IOException e) {
             LOGGER.warning(String.format("%s - %s", feedName, e.toString()));
             //e.printStackTrace();
         }
     }
 
     private boolean isFirstRun;
 
     public String getLastHash(){
         return config.getProperty("lasttnodehash");
     }
 
     public void setLastNode(RssItemBean node) throws CloneNotSupportedException {
         String nods = node.getTitle();
         String ret = getHash(nods);
         config.setProperty("lasttnodehash",ret);
 
         try {
             config.store(new FileOutputStream(configFileName), null);
         } catch (IOException e) {
             LOGGER.warning(String.format("%s - %s", feedName, e.toString()));
             //e.printStackTrace();
         }
     }
 
     private String getHash(String nods) throws CloneNotSupportedException {
         md.update(nods.getBytes());
 //        MessageDigest tc1 = (MessageDigest) md.clone();
         byte[] digest = md.digest();
         String ret = new String(digest);
         md.reset();
         return ret;
     }
 
     public void activate() {
         new Thread(this, feedName ).start();
         LOGGER.info(String.format("%s - Feed %s activated",feedName,link.toString()));
     }
 
     @Override
     public void run() {
         while (true)
         {
             try {
                 LOGGER.fine(String.format("%s - Start process feed",feedName));
                 processFeed();
                 LOGGER.fine(String.format("%s - Feed %s processed, sleep to %s seconds",feedName,link.toString(),checkInterval));
                 Thread.sleep(checkInterval * 1000);
             }
             catch (InterruptedException e) {
                 LOGGER.warning(String.format("%s - %s",feedName,e.toString()));
             } catch (ParserConfigurationException e) {
                 LOGGER.warning(String.format("%s - %s", feedName, e.toString()));
             } catch (IOException e) {
                 LOGGER.warning(String.format("%s - %s", feedName, e.toString()));
             } catch (SAXException e) {
                 LOGGER.warning(e.toString());
             } catch (XMPPException e) {
                 LOGGER.warning(String.format("%s - XMPP error %s",feedName,e));
                 //e.printStackTrace();
                 reconnect();
             } catch (ClassNotFoundException e) {
                 LOGGER.warning(String.format("%s - %s", feedName, e.toString()));
             } catch (InstantiationException e) {
                 LOGGER.warning(String.format("%s - %s", feedName, e.toString()));
             } catch (IllegalAccessException e) {
                 LOGGER.warning(String.format("%s - %s", feedName, e.toString()));
                 reconnect();
             } catch (Exception e) {
                 LOGGER.warning(String.format("%s - %s", feedName, e.toString()));
 //                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
         }
     }
 
     private void reconnect() {
         LOGGER.severe(String.format("%s - Reconnecting",feedName));
         jabber.disconnect();
         LOGGER.severe(String.format("%s - Disconnected",feedName));
         try {
             LOGGER.severe(String.format("%s - Connecting",feedName));
             jabber.connect();
             LOGGER.severe(String.format("%s - Connected",feedName));
         } catch (XMPPException e1) {
             LOGGER.warning(String.format("%s - %s", e1.toString()));
         }
     }
 
     private void processFeed() throws Exception {
 
         String url = link.toString();
         RssFeed feed = rss.load(url);
 
         RssChannelBean channel = feed.getChannel();
         LOGGER.finest(String.format("%s - Parse feed",feedName));
         LOGGER.finest(String.format("Feed Title: %s",channel.getTitle()));
 
         // Gets and iterate the items of the feed
         LOGGER.finest(String.format("%s - Get items",feedName));
         List<RssItemBean> items = feed.getItems();
 
         int current = -1;
 
         if (!isFirstRun()){
             int i;
             String ln = getLastHash();
             for( i = 0;i<items.size();i++) {
                 RssItemBean item = items.get(i);
                 String itm = item.getTitle();
                 itm = getHash(itm);
                 if (itm.contains(ln)){
                     current = i;
                     break;
                 }
             }
             if (current < 0)
                current = items.size();
         } else {
         if (current < 0)
             current = items.size();
         }
 
         LOGGER.info(String.format("%s - %s new of %s items",feedName,current,items.size()));
 
         if (items.size() > 0)
         {
             setFirstRun(false);
 
             for(int i=(current-1);i>=0;i--){
                 try {
                     //Element element = (Element)nodes.item(i);
                     RssItemBean item = items.get(i);
                     LOGGER.fine(String.format("%s - Post item to %s", feedName, newsHub));
                     printElement(item);
                 }catch (Exception e){
                     LOGGER.warning(String.format("%s -%s",feedName,e.toString()));
                 }
             }
             setLastNode(items.get(0));
         }
 
 //        for (int i=0; i<items.size(); i++){
 //            RssItemBean item = items.get(i);
 //            System.out.println("Title: " + item.getTitle());
 //            System.out.println("Link : " + item.getLink());
 //            System.out.println("Desc.: " + item.getDescription());
 //        }
     }
 
     private void printElement(RssItemBean element) throws XMPPException, MalformedURLException {
 //        ConfigureForm form = new ConfigureForm(FormType.submit);
 //        form.setPersistentItems(false);
 //        form.setDeliverPayloads(true);
 //        form.setAccessModel(AccessModel.open);
 
         LeafNode myNode = null;
         LOGGER.finest(String.format("%s - Parse item",feedName));
         NewsItem ni = new NewsItem(logLevel, element , newsHub);
 
         LOGGER.finest(String.format("%s - Get payload",feedName));
         PayloadItem p = ni.genPayload();
 
         try {
             myNode = jabber.pmanager.getNode(newsHub);
 
             LOGGER.finest(String.format("%s - Post",feedName));
             myNode.send(p);
         } catch (Exception e){
             LOGGER.warning(String.format("%s - %s", feedName, e.toString()));
             if(e.toString().contains("item-not-found(404)")){
                 ConfigureForm form = new ConfigureForm(FormType.submit);
                 form.setAccessModel(AccessModel.open);
                 form.setDeliverPayloads(true);
                 form.setNotifyRetract(true);
                 form.setPersistentItems(false);
                 form.setPublishModel(PublishModel.open);
                 form.setSubscribe(true);
 
                 LeafNode leaf = (LeafNode) jabber.pmanager.createNode(newsHub, form);
                 leaf.subscribe(jabber.getJid());
 
                 LOGGER.finest(String.format("%s - Post",feedName));
                 leaf.send(p);
             }
 
         }
     }
 
 //    private void printElement(Element element) throws XMPPException, MalformedURLException {
 //
 //        ConfigureForm form = new ConfigureForm(FormType.submit);
 //        form.setPersistentItems(false);
 //        form.setDeliverPayloads(true);
 //        form.setAccessModel(AccessModel.open);
 //
 //        LeafNode myNode = jabber.pmanager.getNode(newsHub);
 //
 //        LOGGER.finest(String.format("%s - Parse item",feedName));
 //        NewsItem ni = new NewsItem(logLevel, element,newsHub);
 //
 //        LOGGER.finest(String.format("%s - Get payload",feedName));
 //        PayloadItem p = ni.genPayload();
 //
 //        LOGGER.finest(String.format("%s - Post",feedName));
 //        myNode.send(p);
 //    }
 
     private String getCharacterDataFromElement(Element e) {
         try {
             Node child = e.getFirstChild();
             if(child instanceof CharacterData) {
                 CharacterData cd = (CharacterData) child;
                 return cd.getData();
             }
         }
         catch(Exception ex) {
             LOGGER.warning(String.format("%s - %s", feedName, ex.toString()));
         }
         return "";
     } //private String getCharacterDataFromElement
 
     protected float getFloat(String value) {
         if(value != null && !value.equals("")) {
             return Float.parseFloat(value);
         }
         return 0;
     }
 
     protected String getElementValue(Element parent,String label) {
         return getCharacterDataFromElement((Element)parent.getElementsByTagName(label).item(0));
     }
 
     @Override
     public String toString() {
         return "Feed [feedName=\"" + feedName + "\", link=\"" + link.toString() + "\", checkInterval="
                 + checkInterval + ", newsHub=\"" + newsHub +
                 "\"]";
     }
 }
