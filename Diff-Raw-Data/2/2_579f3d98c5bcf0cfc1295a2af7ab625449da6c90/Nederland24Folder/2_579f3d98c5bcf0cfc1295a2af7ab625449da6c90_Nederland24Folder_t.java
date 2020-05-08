 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package net.pms.uitzendinggemist;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import net.pms.dlna.DLNAResource;
 import net.pms.dlna.WebStream;
 import net.pms.dlna.virtual.VirtualFolder;
 import net.pms.formats.Format;
 import net.pms.uitzendinggemist.web.AsxFile;
 import net.pms.uitzendinggemist.web.HTTPWrapper;
 
 /**
  *
  * @author Paul Wagener
  */
 public class Nederland24Folder extends VirtualFolder {
     public Nederland24Folder()
     {
         super("Nederland 24", null);
     }
 
     @Override
     public void discoverChildren() {
         super.discoverChildren();
 
         String streamsXml = HTTPWrapper.Request("http://slplayer.nederland24.nl/xml/channel.xml");
         Matcher m = Pattern.compile("(?s)<channel.*?<name><!\\[CDATA\\[(.*?)\\]\\]>.*?<breedband>(.*?)</breedband>.*?<logo>(.*?)</logo>").matcher(streamsXml);
 
         while (m.find()) {
             String name = m.group(1);
            String stream = new AsxFile(m.group(2)).getMediaStream();
             String img = "http://slplayer.nederland24.nl/xml/" + m.group(3);
             //System.out.println(name + " " + stream + " " + img);
             addChild(new WebStream(name, stream, img, Format.VIDEO));
         }
     }
 
     public static void main(String args[])
     {
         new Nederland24Folder().discoverChildren();
     }
 
     
 }
