 /*
  *  RSSamantha is a rss/atom feedaggregator.
  *  Copyright (C) 2011-2013  David Schröer <tengcomplexATgmail.com>
  *
  *
  *  This file is part of RSSamantha.
  *
  *  RSSamantha is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  RSSamantha is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with RSSamantha.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package com.drinschinz.rssamantha;
 
 /**
  * Simple http acceptor.<br>
  * 
  * TODO: handlePOST seems as if we could optimize. We don't need an Item object to remove.
  * TODO: APPNAME seems used often, maybe make it global static acessable.
  * @author teng
  */
 import java.io.*;
 import java.util.*;
 import java.net.*;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.logging.Level;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 import java.util.Scanner;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import org.w3c.dom.Document;
 
 
 public class ItemAcceptor implements Runnable
 {
     private final Control control;
     private final int port;
     private final String host = System.getProperty(Control.PNAME+".itemacceptorhost", "localhost");
     private final SimpleDateFormat htmlhandlerdatetimeformat = new SimpleDateFormat(System.getProperty(Control.PNAME+".htmlfiledatetimeformat", HtmlFileHandler.DEFAULTDATETIMEHTMLPATTERN));
     private String css = null;
     private List<String>acceptorlist;
     static int timeout = 0;
     private final Vector<ClientThread> threads;
     /* max # worker threads */
     private final int maxworkers;
     
     private static int[] channelixs;
     
     public ItemAcceptor(final Control control, final int port, final int maxworkers, final int[] chix)
     {
         this.control = control;
         this.port = port;
         this.acceptorlist = new ArrayList<String>();
         channelixs = chix;
         if(System.getProperties().containsKey(Control.PNAME+".acceptorlist"))
         {
             Collections.addAll(acceptorlist, System.getProperty(Control.PNAME+".acceptorlist").split(","));
         }
         else
         {
             acceptorlist.add("0:0:0:0:0:0:0:1");
         }
         timeout = 5000;
         this.maxworkers = maxworkers;
         threads = new Vector<ClientThread>(maxworkers);
         if(System.getProperties().containsKey(Control.PNAME+".cssfile"))
         {   
             final StringBuilder s = new StringBuilder();
             for(String ln : Control.readFile(System.getProperties().getProperty(Control.PNAME+".cssfile").toString()))
             {
                 s.append(ln).append(Control.LINESEP);
             }
             if(s.length() == 0)
             {
                Control.L.log(Level.WARNING, "cssfile was not available or empty");
             }
             else
             {
                 css = s.toString();
             }
         }
     }
     
     protected int[] getAllChannelIndicies()
     {
         return channelixs;
     }
     
     protected SimpleDateFormat getHtmlDatetimeFormat()
     {
         return this.htmlhandlerdatetimeformat;
     }
     
     protected String getCss()
     {
         return this.css;
     }
 
     protected int getPort()
     {
         return this.port;
     }
 
     protected String getHost()
     {
         return this.host;
     }
 
     protected Control getControl()
     {
         return this.control;
     }
 
     protected List<String> getAcceptorList()
     {
         return this.acceptorlist;
     }
 
     protected void removeClient(final ClientThread c)
     {
         this.threads.remove(c);
     }
 
     public void run()
     {
         ServerSocket httpd = null;
         try
         {
             Control.L.log(Level.INFO, "Listening to port {0}", String.valueOf(port));
             int calls = 0;
             httpd = new ServerSocket(port);
             
             for(;;)
             {
                 final Socket socket = httpd.accept();
 //System.out.println("threads.size():"+threads.size());
                 if(threads.size() < maxworkers)
                 {
                     ClientThread ws = new ClientThread(this, ++calls, socket);
                     threads.add(ws);
                     (new Thread(ws, "worker")).start();
                 }
                 else
                 {
                     Control.L.log(Level.SEVERE, "Too many threads (max:{0})", maxworkers);
                     continue;
                 }
             }
         }
         catch(IOException e)
         {
             System.err.println(e.toString());
             Control.L.log(Level.SEVERE, "Itemacceptor has stopped", e);
         }
         finally
         {
             if(httpd != null )
             {
                 try
                 {
                     httpd.close();
                 }
                 catch(IOException e)
                 {
                     /* Ignore */
                 }
             }
         }
     }
 }
 
 /**
  * 
  */
 class ClientThread implements Runnable
 {
     /** For logging */
     private long pre = System.currentTimeMillis();
     private Socket socket;
     private int id;
     private PrintStream out;
     private InputStream in;
     private String cmd, url, httpversion;
     private String content;
     private final ItemAcceptor itemacceptor;
     private final static SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");//Fri, 31 Dec 1999 23:59:59 GMT
     private static Transformer transformer = null;
     static
     {
         formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
         try
         {
             transformer = TransformerFactory.newInstance().newTransformer();
         }
         catch(TransformerConfigurationException ex)
         {
             Control.L.log(Level.SEVERE, null, ex);
         }
     }
     private final static SimpleDateFormat cuttoffformatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//1999-12-22 23:59:59
     public final static String EOL = "\r\n";
     public final static String BR = "<BR>";
     public final static String ALL = "ALL";
     public final static String TODAY = "TODAY";
 
     /** 2XX: generally "OK" */
     public static final int HTTP_OK = 200;
 
     /** 4XX: client error */
     public static final int HTTP_BAD_REQUEST = 400;
     public static final int HTTP_NOT_FOUND = 404;
 
     /** 5XX: server error */
     public static final int HTTP_INTERNAL_ERROR = 501;
     
     /** JavaScript validation for the generator */
     private final static String checkInput = new Scanner(ItemAcceptor.class.getResourceAsStream("checkInput.js")).useDelimiter("\\A").next();
     
     /**
      * 
      */
     public ClientThread(final ItemAcceptor itemacceptor, final int id, final Socket s)
     {
         this.itemacceptor = itemacceptor;
         this.id = id;
         this.socket = s;
     }
 
     private void handleClient()
     {
         try
         {
             Control.L.log(Level.FINEST,"[{0}]: Incoming call...", String.valueOf(id));
             socket.setSoTimeout(ItemAcceptor.timeout);
             socket.setTcpNoDelay(true);
             in = new BufferedInputStream(socket.getInputStream());
             out = new PrintStream(socket.getOutputStream());
             if(!isAccept())
             {
                 itemacceptor.getControl().getStatistics().count(Control.CountEvent.HTTP_NOACCEPT);
                 return;
             }
             readRequest();
             Control.L.log(Level.INFO, "Accepting {0} url:{1} httpversion:{2} from {3}", new Object[]{cmd, url, httpversion, socket.getInetAddress().getHostAddress()});
 //System.out.println("Accepting "+cmd+" url:"+url+" httpversion:"+httpversion);
             handleResponse();
             Control.L.log(Level.FINEST,"[{0}]: Closed.", id);
         }
         catch(Exception e)
         {
             Control.L.log(Level.WARNING,"[{0}]: Aborted.{1}", new Object[]{String.valueOf(id), e.toString()});
             if(out != null)
             {
                 httpAnswer(HTTP_BAD_REQUEST, "Bad Request", e.getMessage(), Main.APPNAME);
             }
             //e.printStackTrace();
         }
         finally
         {
             try
             {
                 if(out != null)
                 {
                     out.flush();
                 }
                 if(socket != null)
                 {
                     socket.close();
                 }
                 Control.L.log(Level.FINEST,"[{0}]: Finally Closed.", id);
             }
             catch(IOException e)
             {
                 /* Ignore */
             }
         }
     }
 
     /**
      * 
      */
     @Override
     public synchronized void run()
     {
         handleClient();
         socket = null;
         itemacceptor.removeClient(this);
 //System.out.println(" size:"+ItemAcceptor.threads.size());
         if(Control.L.isLoggable(Level.FINE))
         {
             Control.L.log(Level.FINE, "Done, handling {0} took {1} ms",new Object[]{content,String.valueOf(System.currentTimeMillis()-pre)});
         }
     }
 
     private boolean isAccept() throws Exception
     {
         final InetAddress ia = socket.getInetAddress();
 //System.out.println("ia:"+ia.getHostAddress());
         for(String s : itemacceptor.getAcceptorList())
         {
             if(ia.getHostAddress().startsWith(s))
             {
                 return true;
             }
         }
         Control.L.log(Level.WARNING, "Not accepting incoming request from {0}", ia.getHostAddress());
         return false;
     }
 
     /**
      * TODO: This is ugly.
      */
     private void readRequest() throws Exception
     {
         /* read request-rows */
         final List<StringBuilder> request = new ArrayList<StringBuilder>(10);
         StringBuilder sb = new StringBuilder(100);
         int c;
         int contentlength = 0;
         int contentcount = -1;
         for(int ii=0; ii<15000; ii++)
         {
             if(contentcount>=contentlength)
             {
                 request.add(sb);
                 break;
             }
             c = in.read();
 //System.out.print((char)c);
             if(contentcount != -1)
             {
                 contentcount++;
             }
             if (c == '\r')
             {
                 //ignore
             }
             else if (c == '\n')
             { //line terminator
                 if (sb.length() <= 0)
                 {
                     //break;
 //System.out.print("\n");
                     continue;
                 }
                 else
                 {
                     if(sb.toString().startsWith("Content-Length:"))
                     {
                         contentlength = Integer.valueOf(sb.toString().substring(16, sb.toString().length()))+2;
                         contentcount = 0;
                     }
                     request.add(sb);
                     if(request.size() == 1)
                     {
                         if(!((request.get(0)).toString()).startsWith("POST"))
                         {
                             break;
                         }
                     }
                     sb = new StringBuilder(100);
                 }
             }
             else
             {
                 sb.append((char) c);
             }
         }
         /* Log request */
         for(Iterator<StringBuilder> iter = request.iterator(); iter.hasNext();)
         {
             sb = iter.next();
 //System.out.println("< " + sb.toString());
             Control.L.log(Level.FINEST, "< [{0}]{1}", new Object[]{id, sb.toString()});
         }
         /* Extract command, URL und HTTP-Version */
         String s = (request.get(0)).toString();
         cmd = "";
         int pos = s.indexOf(' ');
         if (pos != -1)
         {
             cmd = s.substring(0, pos).toUpperCase();
             s = s.substring(pos + 1);
             //URL
             pos = s.indexOf(' ');
             if (pos != -1)
             {
                 url = s.substring(0, pos);
                 s = s.substring(pos + 1);
                 //HTTP-Version
                 pos = s.indexOf('\r');
                 if (pos != -1)
                 {
                     httpversion = s.substring(0, pos);
                 }
                 else
                 {
                     httpversion = s;
                 }
             }
             else
             {
                 url = s;
             }
         }
         content = (request.get(request.size()-1)).toString();
     }
     
     
     /**
      * TODO: It seems this can be slightly optimized.
      * @param s
      * @return
      * @throws Exception 
      */
     private HashMap<String,String> getArgsFromUrl(final String s) throws Exception
     {
         final String [] el = (s.startsWith("?") ? s.substring(1) : s).split("&");
         final HashMap<String, String> hm = new HashMap<String, String>();
         int[] channelix = new int[2];
         for(int ii=0; ii<el.length; ii++)
         {
             final String t = URLDecoder.decode(el[ii], "UTF-8");
             if(t.indexOf("=") != -1)
             {
                 final String[] tok = t.split("=");
                 if("channel".equals(tok[0]))
                 {
                     tok[0]+=(channelix[0]++);
                 }
                 else if("ix".equals(tok[0]))
                 {
                     tok[0]+=(channelix[1]++);
                 }
                 hm.put(tok[0], tok.length > 1 ? tok[1] : "");
             }
             else if(t.length() > 0)
             {
                 hm.put(t, "");
             }
         }
         return hm;
     }
 
     private void handlePOST(final HashMap<String, String> hm)
     {
         int ix = -1;
         if(hm.containsKey("channel0"))
         {
             ix = itemacceptor.getControl().getChannelIndex(hm.get("channel0"));
         }
         else
         {
             ix = Integer.valueOf(hm.get("ix0"));
         }
         if(!itemacceptor.getControl().isValidChannelIndex(ix))
         {
             httpAnswer(HTTP_INTERNAL_ERROR,"Invalid channelindex","Invalid channelindex"+"<BR><BR>"+getHttpUsage(false),"Error");
             return;
         }
         final String title = hm.get("title");
         if(title == null || title.length() == 0)
         {
             httpAnswer(HTTP_INTERNAL_ERROR,"No title","No title"+"<BR><BR>"+getHttpUsage(false),"Error");
             return;
         }
         long created = -1;
         final Item item = new Item(created, Control.replaceHtmlCharacter(title), hm.containsKey("description") ? hm.get("description") : "n/a", "ItemAcceptor", ItemCreator.ItemCreatorType.HTTPFEED);
         if(hm.containsKey("created"))
         {
             created = Long.parseLong(hm.get("created"));
         }
         else
         {
             created = System.currentTimeMillis();
             item.setFoundrsscreated(false);
         }
         if(hm.containsKey("remove") && "1".equals(hm.get("remove")))
         {
             final boolean b = itemacceptor.getControl().removeItem(title, created, ix);
             final String msg = "Removing item with title:"+title+" created:"+created+" in channel:"+itemacceptor.getControl().getChannelName(ix)+" - "+(b?"removed":"not found");
             Control.L.log(Level.INFO, msg);
             httpAnswer(HTTP_OK, "OK", msg, Main.APPNAME);
             return;
         }
         item.setCreated(created);
         if(hm.containsKey("link"))
         {
             item.putElement("link", hm.get("link"));
         }
         final Control.CountEvent b = itemacceptor.getControl().addItem(item, ix);
         Control.L.log(Level.INFO, "Returnvalue:{0} item:{1}", new Object[]{b.toString(), item.toShortString()});
         httpAnswer(HTTP_OK, "OK", "Item accepted, returnvalue:"+b.toString()+" {"+item.toShortString()+"}", Main.APPNAME);
     }
     
     private void doStatus() throws Exception
     {
         final StringBuilder msg = new StringBuilder("java version:"+System.getProperty("java.runtime.version")+BR
                 +"app version:"+Main.APPNAME+" "+Main.APPVERSION+BR
                 +"futuredump:"+itemacceptor.getControl().isFutureDump()+BR
                 +"compression:"+itemacceptor.getControl().getCompression()+BR
                 +"loglevel:"+Control.L.getLevel()+"</b>"+BR+BR
                 +"Status:"+BR+itemacceptor.getControl().getStatus());
         Control.L.log(Level.INFO, msg.toString());
         httpAnswer(HTTP_OK, "OK", msg.toString(), Main.APPNAME);
     }
     
     /**
      * TODO: we could write this directly to out stream.
      * @throws Exception 
      */
     private void doGenerator() throws Exception
     {
         final String[] channels = itemacceptor.getControl().getAllChannelNames();
         final StringBuilder msg = new StringBuilder();
         msg.append(BR+BR+EOL);
         msg.append(checkInput);
         msg.append("<FORM name=\"generate\" action=\"/\" method=\"get\" target=\"_blank\" onsubmit=\"return checkInput()\">").append(EOL);
         msg.append("<TABLE>").append(EOL);
         msg.append("<TR>").append(EOL);
         msg.append("<TD>Channel:</TD>").append(EOL);
         msg.append("<TD>").append(EOL);
         msg.append("<SELECT name=\"channel\" size=\"5\" multiple>").append(EOL);
         for(int ii=0; ii<channels.length; ii++)
         {
             msg.append("<OPTION").append(ii==0?" selected>":">").append(channels[ii]).append("</OPTION>").append(EOL);
         }
         msg.append("</SELECT>").append(EOL);
         msg.append("</TD>").append(EOL);
         msg.append("</TR>").append(EOL);
         msg.append("<TR>").append(EOL);
         msg.append("<TD>Number of Items:</TD>").append(EOL);
         msg.append("<TD>").append(EOL);
         msg.append("<INPUT name=\"numitems\" type=\"text\" size=\"10\" maxlength=\"12\" value=\"100\">").append(BR).append(EOL);
         msg.append("<FONT size=\"1\">[Integer or ALL]</FONT>").append(EOL);
         msg.append("</TD>").append(EOL);
         msg.append("</TR>").append(EOL);
         msg.append("<TR>").append(EOL);
         msg.append("<TD>Cutoff:</TD>").append(EOL);
         msg.append("<TD>").append(EOL);
         msg.append("<INPUT name=\"cutoff\" type=\"text\" size=\"19\" maxlength=\"19\" value=\"TODAY\">").append(BR).append(EOL);
         msg.append("<FONT size=\"1\">[yyyy-mm-dd hh:mm:ss or milliseconds from epoch or TODAY]</FONT>").append(EOL);
         msg.append("</TD>").append(EOL);
         msg.append("</TR>").append(EOL);
         msg.append("<TR>").append(EOL);
         msg.append("<TD>Refresh:</TD>").append(EOL);
         msg.append("<TD>").append(EOL);
         msg.append("<INPUT name=\"refresh\" type=\"text\" size=\"19\" maxlength=\"19\" value=\"\">"+BR).append(EOL);
         msg.append("<FONT size=\"1\">[In seconds, works for HTML]</FONT>").append(EOL);
         msg.append("</TD>").append(EOL);
         msg.append("</TR>").append(EOL);
         msg.append("<TR>").append(EOL);
         msg.append("<TD>RegExp Title:</TD>").append(EOL);
         msg.append("<TD>").append(EOL);
         msg.append("<INPUT name=\"search_title\" type=\"text\" size=\"30\" maxlength=\"100\">"+BR).append(EOL);
         msg.append("<FONT size=\"1\">[Java compliant regexp]</FONT>").append(EOL);
         msg.append("</TD>").append(EOL);
         msg.append("</TR>").append(EOL);
         msg.append("<TR>").append(EOL);
         msg.append("<TD>Type:</TD>").append(EOL);
         msg.append("<TD>").append(EOL);
         msg.append("<INPUT type=\"radio\" name=\"type\" value=\"xml\" checked>XML").append(EOL);
         msg.append("<INPUT type=\"radio\" name=\"type\" value=\"html\">HTML").append(EOL);
         msg.append("<INPUT type=\"radio\" name=\"type\" value=\"txt\">TXT").append(EOL);
         msg.append("</TD>").append(EOL);
         msg.append("</TR>").append(EOL);
         msg.append("<TR>").append(EOL);
         msg.append("<TD>").append(EOL);
         msg.append("<INPUT type=\"submit\" value=\"Generate\">");
         msg.append("</TD><TD></TD>").append(EOL);
         msg.append("<TR>").append(EOL);
         msg.append("</TABLE>").append(EOL);
         msg.append("</FORM>").append(EOL);
         msg.append(BR+BR+EOL+"Channels:<UL>").append(EOL);
         for(String c : channels)
         {
             msg.append("<LI><A HREF=\"/channel=").append(c).append("\">").append(c).append("</A></LI>").append(EOL);
         }
         msg.append(BR+EOL+"</UL>");
         msg.append(EOL+"All Channels as OPML:<UL>").append(EOL);
         msg.append("<LI><A HREF=/opml>").append(Main.APPNAME.toLowerCase()).append(".opml</A></LI>").append(EOL);
         msg.append(BR+EOL+"</UL>");
         httpAnswer(HTTP_OK, false, "OK", msg.toString(), Main.APPNAME);
     }
     
     private void doOpml() throws Exception
     {
         out.print("HTTP/1.0 "+HTTP_OK+" OK"+EOL);
         out.print("Content-type: text/xml"+EOL+EOL);
         out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+EOL);
         out.println("<opml>"+EOL);
         out.println("<head>"+EOL);
         out.println("<title>"+Main.APPNAME+"</title>"+EOL);
         out.println("</head>"+EOL);
         out.println("<body>"+EOL);
         for(String c : itemacceptor.getControl().getAllChannelNames())
         {
             out.println("<outline text=\""+c+"\" xmlUrl=\"http://"+itemacceptor.getHost()+":"+itemacceptor.getPort()+"/channel="+c+"\" />"+EOL);
         }
         out.println("</body>"+EOL);
         out.println("</opml>"+EOL);
     }
     
     /**
      * 
      * @param hm input GET arguments. channel and ix keys indexed.
      * @return 
      */
     private int[] getChannels(final HashMap<String, String> hm)
     {   
         if(hm.containsKey("allchannels"))
         {
             Control.L.log(Level.FINEST, "returning all {0} channels", new Object[]{itemacceptor.getAllChannelIndicies().length});
             return itemacceptor.getAllChannelIndicies();
         }
         final List<Integer> tmp = new ArrayList<Integer>(itemacceptor.getControl().getChannelCount());
         for(int ii=0; ii<itemacceptor.getControl().getChannelCount(); ii++)
         {
             int addix = itemacceptor.getControl().getChannelIndex(hm.get("channel"+ii)); // returns -1 for an invalid
             if(addix != -1 && !tmp.contains(addix))
             {
                 tmp.add(addix);
             }
             try
             {
                 addix = Integer.valueOf(hm.get("ix"+ii)); 
                 if(addix >= 0 && addix < itemacceptor.getControl().getChannelCount() && !tmp.contains(addix))
                 {
                     tmp.add(addix);
                 }
             }
             catch(NumberFormatException n)
             {
                 // ignore
             }
         }
         if(tmp.isEmpty())
         {
             Control.L.log(Level.FINEST, "returning no channels");
             return new int[0];
         }
         final int[] ret = new int[tmp.size()];
         for(int ii=0; ii<tmp.size(); ii++)
         {
             ret[ii] = tmp.get(ii);
         }
         Control.L.log(Level.FINEST, "returning {0} ix:{1}", new Object[]{ret.length, Arrays.toString(ret)});
         return ret;
     }
 
     private void handleGET(final HashMap<String, String> hm) throws Exception
     {
         if(hm.isEmpty() || hm.containsKey("generator"))
         {
             doGenerator();
             return;
         }
         if(hm.size() == 1 && hm.containsKey("favicon.ico"))
         {
             httpAnswer(HTTP_NOT_FOUND, "File not found", "File not found", Main.APPNAME);
             Control.L.log(Level.FINEST, "No favicon support");
             return;
         }
         if(hm.containsKey("status"))
         {
             final String ha = socket.getInetAddress().getHostAddress();
 //System.out.println("ia:"+ha);
             if("0:0:0:0:0:0:0:1".equals(ha) || "127.0.0.1".equals(ha))
             {
                 doStatus();
             }
             else
             {
                 doGenerator();
             }
             return;
         }
         if(hm.containsKey("opml"))
         {
             doOpml();
             return;
         }
         final int[] cis = getChannels(hm);
         if(cis.length == 0)
         {
             httpAnswer(HTTP_BAD_REQUEST, "Bad Request", "Bad GET Request, invalid channel(s) "+url+BR+BR+getHttpUsage(true), Main.APPNAME);
             return;
         }
         int numitems = -1;
         if(hm.containsKey("numitems"))
         {
             numitems = ALL.equals(hm.get("numitems").toUpperCase()) ? Integer.MAX_VALUE : Integer.valueOf(hm.get("numitems"));
         }
         long cutoff = -1;
         if(hm.containsKey("cutoff") && hm.get("cutoff").length() > 0)
         {
             if(TODAY.equals(hm.get("cutoff").toUpperCase()))
             {
                 final Calendar cal = Calendar.getInstance();
                 cal.set(Calendar.HOUR_OF_DAY, 0);
                 cal.set(Calendar.MINUTE, 0);
                 cal.set(Calendar.SECOND, 0);
                 cal.set(Calendar.MILLISECOND, 0);
                 cutoff = cal.getTimeInMillis();
             }
             else
             {
                 synchronized(cuttoffformatter)
                 {
                     try
                     {
                         cutoff = cuttoffformatter.parse(hm.get("cutoff")).getTime();
                     }
                     catch(ParseException pe)
                     {
                         try
                         {
                             cutoff = Long.valueOf(hm.get("cutoff")); // if we fail here we throw in caller
                         }
                         catch(NumberFormatException nfe)
                         {
                             throw new Exception("Unable to parse cutoff "+hm.get("cutoff"));
                         }
                     }
                 }
             }
         }
         final String type = hm.containsKey("type") ? hm.get("type") : "xml";
         if(!"xml".equals(type) && !"html".equals(type) && !"txt".equals(type))
         {
             httpAnswer(HTTP_BAD_REQUEST, "Bad Request", "Bad GET Request, unknown type:"+hm.toString()+BR+BR+getHttpUsage(true), Main.APPNAME);
             Control.L.log(Level.WARNING, "Unknown type:{0}", type);
             return;
         }
         String refresh = "60"; // TODO make default global and configurable.
         if(hm.containsKey("refresh") && hm.get("refresh").length() > 0)
         {
             refresh = hm.get("refresh");
             try
             {
                 Integer.parseInt(refresh);
             }
             catch(NumberFormatException e)
             {
                 httpAnswer(HTTP_BAD_REQUEST, "Bad Request", "Bad GET Request, invalid refresh:"+hm.toString()+BR+BR+getHttpUsage(true), Main.APPNAME);
                 Control.L.log(Level.WARNING, "Invalid refresh:{0}", refresh);
                 return;
             }
         }
         Pattern pt_title = null;
         if(hm.containsKey("search_title") && hm.get("search_title").length() > 0)
         {
             try
             {
 //System.out.println("compile pattern:"+hm.get("search_title"));                
                 pt_title = Pattern.compile(hm.get("search_title"));
             }
             catch(PatternSyntaxException e)
             {
                 httpAnswer(HTTP_BAD_REQUEST, "Bad Request", "Bad GET Request, invalid search pattern:"+hm.get("search_title")+BR+BR+getHttpUsage(true), Main.APPNAME);
                 Control.L.log(Level.WARNING, "Invalid pattern:{0}", hm.get("search_title"));
                 return;
             }
         }
         List<Item> items = itemacceptor.getControl().getSortedItems(cis, cutoff, numitems, pt_title);
 //System.out.println("numitems:"+items.size());        
         out.print("HTTP/1.0 "+HTTP_OK+" OK"+EOL);
         out.print("Content-type: text/"+type+"; charset=utf-8"+EOL);
         out.print("Server: "+Main.APPNAME+"/"+Main.APPVERSION+EOL);
         final Date dt = new Date();
         out.print("Date: "+dt+EOL);
         synchronized(formatter)
         {
             out.print("Last-Modified: "+formatter.format(dt)+EOL+EOL);
         }
         if("html".equals(type))
         {
             out.print((new HtmlFileHandler(itemacceptor.getControl(), cis, null, 0, itemacceptor.getHtmlDatetimeFormat())).getAsString(items, refresh, itemacceptor.getCss()));
         }
         else if("txt".equals(type))
         {
             out.print((new TxtFileHandler(itemacceptor.getControl(), cis, null, 0, TxtFileHandler.DEFAULTDATETIMETXTPATTERN)).getAsString(items));
         }
         else
         {
             Document doc = (new RssFileHandler(itemacceptor.getControl(), cis, null, 0)).getDocument(items);
             synchronized(transformer)
             {
                 transformer.reset();
                 transformer.setOutputProperty("indent", "yes");
                 transformer.transform(new DOMSource(doc), new StreamResult(out));
             }
             doc = null;
         }
         Control.L.log(Level.INFO, "Served channel:{0} type:{1} number of items:{2}", new Object[]{itemacceptor.getControl().getChannelName(cis), type, items.size()});
         items.clear();
         items = null;
     }
     /** 
      * TODO: Add search_title
      * TODO: Add http POST usage
      * TODO: Fix new channel logic
      * TODO: make append quicker
      Add item:
 wget --post-data='title=testtitle&description=testdescription&ix=0' http://host:port/ -O /dev/null
 wget --post-data='title=testtitle&description=testdescription&channel=tengtest&created=$(($(date +%s%N)/1000000))' http://host:port/ -O /dev/null
 
 Remove item:
 wget --post-data='title=testtitle&description=testdescription&ix=0&created=$CREATED&remove=1' http://host:port/ -O /dev/null
 wget --post-data='title=testtitle&description=testdescription&channel=tengtest&created=$CREATED&remove=1' http://host:port/ -O /dev/null
      */
     private String getHttpUsage(final boolean get)
     {
         final StringBuilder ret = new StringBuilder("Usage:"+EOL);
         if(get)
         {
             ret.append("<OL>"+EOL);
                 ret.append("<LI>http://myhost/status</LI>"+EOL);
                 ret.append("<LI>http://myhost/opml</LI>"+EOL);
                 ret.append("<LI>http://myhost/[channel=$NAME1&channel$NAMEn][ix=$IX1&ix$IXn][&numitems={ALL|number}][&type={xml|html|txt}][&refresh={seconds}][&cutoff={TODAY|yyyy-MM-dd HH:mm:ss|epochtimeinmillis}]"+BR+EOL);
                     ret.append("<SMALL>Available channels:").append(Arrays.toString(itemacceptor.getControl().getAllChannelNames())).append("</SMALL>");
                 ret.append("</LI>"+EOL);
             ret.append("</OL>"+EOL);
         }
         else
         {
             ret.append("<UL>"+EOL);
                 ret.append("<LI>channel(String, channelname, defined in config.name)</LI>"+EOL);
                 ret.append("<LI>ix (Integer, the channel index)</LI>"+EOL);
                 ret.append("<LI>title (String)</LI>"+EOL);
                 ret.append("<LI>description (String)</LI>"+EOL);
                 ret.append("<LI>created (Long, milliseconds, between the current time and midnight, January 1, 1970 UTC)</LI>"+EOL);
                 ret.append("<LI>link (String)</LI>"+EOL);
                 ret.append("<LI>remove (Integer, 1 means true, default 0)</LI>"+EOL);
                 ret.append("<LI></LI>"+EOL);
             ret.append("</UL>"+EOL);
         }
         return ret.toString();
     }
 
     /**
      * 
      */
     private void handleResponse() throws Exception
     {
 //System.out.println("content:"+content);        
         if(cmd.equals("POST"))
         {
             handlePOST(getArgsFromUrl(content));
             itemacceptor.getControl().getStatistics().count(Control.CountEvent.HTTP_POST);
         }
         else
         {
             handleGET(getArgsFromUrl(content.substring(5, content.lastIndexOf(" "))));
             itemacceptor.getControl().getStatistics().count(Control.CountEvent.HTTP_GET);
         }
     }
     
     private void httpAnswer(final int returncode, final boolean hr_returncode, final String returndescription, final String body, final String title)
     {
         out.print("HTTP/1.0 "+returncode+ " " +returndescription+EOL);
         out.print("Content-type: text/html; charset=utf-8"+EOL+EOL);    
         out.println("<HTML>");
         out.println("<HEAD>");
         out.println("<TITLE>"+title+"</TITLE>");
         out.println("</HEAD>");
         out.println("<BODY>");
         out.println(hr_returncode ? "<h3>HTTP/1.0 "+returncode +"</h3>" : "");
         out.println(body);
         out.println("</BODY>");
         out.println("</HTML>");
     }
 
     /**
      * 
      */
     private void httpAnswer(final int returncode, final String returndescription, final String body, final String title)
     {
         httpAnswer(returncode, true, returndescription, body, title);
     }
 }
