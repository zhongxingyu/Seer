 package com.joshondesign.tuneserver;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.*;
 import java.net.InterfaceAddress;
 import java.net.NetworkInterface;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import javax.jmdns.JmDNS;
 import javax.jmdns.ServiceInfo;
 import javax.swing.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: josh
  * Date: 3/23/12
  * Time: 11:49 AM
  * To change this template use File | Settings | File Templates.
  */
 public class Main {
     private static ITunesDB itunes;
     private static ServiceInfo service;
     private static JmDNS mdns;
     private static CustomNanoHTTPD server;
 
     public static void main(String ... args) throws InterruptedException {
         final ITunesParser parser = new ITunesParser() {
             @Override
             protected void onEnd(ITunesDB db) {
                 itunes = db;
                 u.p("parsing done!");
                 u.p("total tracks = " + db.getTotalTrackCount());
                 u.p("total playlists = " + db.playlists.size());
                 u.p("total artists = " + db.artistMap.size());
                 startServer(itunes);
             }
         };
         parser.start();
         u.p("started the parser");
     }
 
     public static void printSummary(ITunesDB itunes, OutputStream outputStream) {
         PrintWriter pw = new PrintWriter(outputStream);
         JSONPrinter j = new JSONPrinter(pw);
         j.open();
         j.println("\"artists\":[");
         j.indent();
 
         for(Artist artist : itunes.getArtists()) {
             j.open();
             j.indent();
             j.p("name", artist.getName());
             j.p("trackcount",artist.getTrackCount());
             j.p("albumcount",artist.getAlbumCount());
             j.println(",\"albums\":[");
             j.indent();
             boolean first = true;
             for(Album album : artist.getAlbums()) {
                 if(first) {
                     j.println(" \""+album.getName()+"\"");
                 } else {
                     j.println(",\""+album.getName()+"\"");
                 }
                 first = false;
             }
             j.outdent();
             j.println("],");
             j.outdent();
             j.close();
             j.println(",");
         }
 
         j.outdent();
         j.println("],");
         j.close();
         pw.close();
     }
 
     private static void startServer(ITunesDB itunes) {
         try {
             server = new CustomNanoHTTPD(1957, itunes);
            startMDNS(itunes);
             showGUI();
             /*
             try {
                 Thread.currentThread().sleep(300);
             } catch (InterruptedException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             } */
             /*
             final InetAddress address = server.getAddress();
             SwingUtilities.invokeLater(new Runnable() {
                 public void run() {
                     u.p("address = " + address.getCanonicalHostName());
                     u.p("address = " + address.getHostAddress());
                     u.p("address = " + address.getAddress());
                     for(byte b : address.getAddress()) {
                         u.p("byte = " + b);
                     }
                     u.p("address = " + address.getHostName());
                     try {
                         u.p("address local host = " + address.getLocalHost());
                     } catch (UnknownHostException e) {
                         e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                     }
                     JFrame frame = new JFrame("Tunes Now Server");
                     frame.add(new JLabel("we are running on : " + address.getHostAddress()));
                     frame.pack();
                     frame.setVisible(true);
                 }
             });
             */
             u.p("opened server on port 1957");
             u.p("running on one or more of these interfaces:");
             Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
             while(en.hasMoreElements()) {
                 NetworkInterface net = en.nextElement();
 //                u.p("=== network ");
 //                u.p(net.getDisplayName());
 //                u.p("loopback = " + net.isLoopback());
                 if(!net.isLoopback()) {
                     for(InterfaceAddress ad : net.getInterfaceAddresses()) {
                         u.p("address = " + ad.getAddress().getHostAddress());
                     }
                 }
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     private static void showGUI() {
         final JFrame frame = new JFrame("Budu Server");
 
         JPanel panel = new JPanel();
         panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
         panel.add(new JLabel("Server is running  "));
         JButton stopButton = new JButton("stop");
         stopButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent actionEvent) {
                 try {
                     stopMDNS();
                     stopServer();
                     frame.setVisible(false);
                     frame.dispose();
                     System.exit(0);
                 } catch (IOException e) {
                     e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 }
             }
         });
         panel.add(stopButton);
 
 
         frame.add(panel);
         frame.pack();
         frame.setVisible(true);
     }
 
     private static void stopServer() throws IOException {
         server.stop();
         u.p("server stopped");
     }
 
     private static void startMDNS(ITunesDB itunes) throws IOException {
         mdns = JmDNS.create();
         Map<String,String> props = new HashMap<String,String>();
         props.put("artistcount",""+itunes.sortedArtists.size());
         props.put("trackcount",""+itunes.getTotalTrackCount());
         props.put("albumcount",""+itunes.getTotalAlbumCount());
         service = ServiceInfo.create("_http._tcp.local.", "buduserver", 8954, 0,0, props);
         mdns.registerService(service);
     }
     private static void stopMDNS() throws IOException {
         u.p("unregisetering");
         mdns.unregisterService(service);
         mdns.unregisterAllServices();
         mdns.close();
         u.p("done");
     }
 
 
     private static class CustomNanoHTTPD extends NanoHTTPD {
         private ITunesDB itunes;
 
         public CustomNanoHTTPD(int i, ITunesDB itunes) throws IOException {
             super(i);
             this.itunes = itunes;
         }
         @Override
         public NanoHTTPD.Response serve(String uri, String method, Properties header, Properties parms) {
             u.p("url = " + uri);
             if(uri.startsWith("/download/")) {
                 return serveDownloadSong(uri, header);
             }
             if(uri.startsWith("/artists")) {
                 return serveArtists(itunes);
             }
             if(uri.startsWith("/albums")) {
                 return serveAlbums(itunes,parms);
             }
             if(uri.startsWith("/tracks")) {
                 return serveTracks(itunes,parms);
             }
             return super.serve(uri, method, header, parms);
         }
         
         private Response serveTracks(ITunesDB itunes, Properties parms) {
             u.p("id = " + parms.getProperty("albumid"));
             Album album = itunes.getAlbumById(parms.getProperty("albumid"));
 
             u.p("album = " + album.getName());
             StringBuffer xml = new StringBuffer();
             xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
             xml.append("<library>\n");
             xml.append(" <tracks>\n");
             for(Track track  : album.getTracks()) {
                 xml.append("  <track "
                         +" name=\""+track.getName().replace("&","_")+"\""
                         +" id='"+track.getId()+"'"
                         +" />\n");
             }
             xml.append(" </tracks>\n");
             xml.append("</library>\n");
             return new Response(HTTP_OK, MIME_XML, xml.toString());
         }
 
 
         private Response serveAlbums(ITunesDB itunes, Properties parms) {
             u.p("id = " + parms.getProperty("artistid"));
             Artist artist = itunes.getArtistByID(parms.getProperty("artistid"));
             u.p("artist = " + artist);
             StringBuffer xml = new StringBuffer();
             xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
             xml.append("<library>\n");
             xml.append(" <albums>\n");
             for(Album album : artist.getAlbums()) {
                 u.p("album name = " + album.getName());
                 u.p("id = " + album.getId());
                 xml.append("  <album "
                         +" name=\""+album.getName().replace("&","_")+"\""
                         +" id='"+album.getId()+"'"
                         +" />\n");
             }
             xml.append(" </albums>\n");
             xml.append("</library>\n");
             return new Response(HTTP_OK, MIME_XML, xml.toString());
         }
 
         private Response serveArtists(ITunesDB itunes) {
 
             StringBuffer xml = new StringBuffer();
             xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
             xml.append("<library>\n");
             xml.append(" <artists>\n");
             for(Artist artist : itunes.sortedArtists) {
                 xml.append("  <artist "
                         +" name=\""+artist.getName().replace("&","&amp;")+"\""
                         +" id='"+artist.getID()+"'"
                         +" albumcount='"+artist.getAlbumCount()+"'"
                         +" />\n");
             }
             xml.append(" </artists>\n");
             xml.append("</library>\n");
             return new Response(HTTP_OK, MIME_XML, xml.toString());
         }
 
         private NanoHTTPD.Response serveDownloadSong(String uri, Properties header) {
             try {
                 String id = uri;
                 u.p("getting song: " + id);
                 String iid = id.substring(id.lastIndexOf("/")+1);
                 u.p(iid);
                 iid = iid.substring(0,iid.lastIndexOf("."));
                 u.p(iid);
                 Track track = itunes.findTrackById(Integer.parseInt(iid));
                 u.p("track = " + track.getName() + " : " + track.getArtist() + " : " + track.getAlbum());
                 u.p("going to stream file: \n" + track.getLocation());
                 File f = u.streamToFile(new URI(track.getLocation()), File.createTempFile("foo",".mp3"));
                 //File f = new File("/Users/josh/Sites/together.mp3");
 
                 // Get MIME type from file name extension, if possible
                 String mime = null;
                 int dot = f.getCanonicalPath().lastIndexOf('.');
                 if (dot >= 0)
                     mime = (String) NanoHTTPD.theMimeTypes.get(f.getCanonicalPath().substring(dot + 1).toLowerCase());
                 if (mime == null)
                     mime = NanoHTTPD.MIME_DEFAULT_BINARY;
 
                 // Support (simple) skipping:
                 long startFrom = 0;
                 String range = header.getProperty("Range");
                 if (range != null) {
                     if (range.startsWith("bytes=")) {
                         range = range.substring("bytes=".length());
                         int minus = range.indexOf('-');
                         if (minus > 0)
                             range = range.substring(0, minus);
                         try {
                             startFrom = Long.parseLong(range);
                         } catch (NumberFormatException nfe) {
                         }
                     }
                 }
 
                 FileInputStream fis = new FileInputStream(f);
                 fis.skip(startFrom);
                 NanoHTTPD.Response r = new NanoHTTPD.Response(NanoHTTPD.HTTP_OK, mime, fis);
                 r.addHeader("Content-length", "" + (f.length() - startFrom));
                 r.addHeader("Content-range", "" + startFrom + "-" + (f.length() - 1) + "/" + f.length());
                 return r;
             } catch (IOException ex) {
                 ex.printStackTrace();
             } catch (URISyntaxException e) {
                 e.printStackTrace();
             }
             return null;
         }
     }
 }
