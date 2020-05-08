 package botto.xmpp.connectors.smack;
 
 
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.Element;
 import org.dom4j.io.SAXReader;
 import org.jivesoftware.smack.Connection;
 import org.jivesoftware.smack.util.PacketParserUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xml.sax.InputSource;
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserFactory;
 import org.xmpp.packet.IQ;
 import org.xmpp.packet.Message;
 import org.xmpp.packet.Packet;
 import org.xmpp.packet.Presence;
 
 import java.io.ByteArrayInputStream;
 import java.io.UnsupportedEncodingException;
 
 public class PacketTypeConverter {
 
     private final static Logger Log = LoggerFactory.getLogger(PacketTypeConverter.class);
 
     // this method converts from smack (xmpp bot library) packet types
     // to tinder/whack packet types (xmpp component library)
    static Packet converttoTinder(org.jivesoftware.smack.packet.Packet packet) {
         SAXReader saxReader = new SAXReader();
         try {
             Document read = saxReader.read(new InputSource(new ByteArrayInputStream(packet.toXML().getBytes("utf-8"))));
             Element element = read.getRootElement();
             if (element.getName().equalsIgnoreCase("iq")) {
                 return new IQ(element);
             }
             else if (element.getName().equalsIgnoreCase("presence")) {
                 return new Presence(element);
             }
             else if(element.getName().equalsIgnoreCase("message")) {
                 return new Message(element);
             }
             else {
                 throw new RuntimeException("Unknown packet type " + element.getName() + ": " + element.toString());
             }
         } catch (DocumentException e) {
             Log.error("Error while parsing packet {}: {}", packet.toXML(), e);
             throw new RuntimeException("exception while parsing packet " + packet.toXML(), e);
         } catch (UnsupportedEncodingException e) {
             Log.error("Error while parsing packet {}: {}", packet.toXML(), e);
             throw new RuntimeException("exception while parsing packet " + packet.toXML(), e);
         }
     }
 
    static org.jivesoftware.smack.packet.Packet convertFromTinder(Packet packet, Connection connection) {
         try {
             XmlPullParserFactory factory = XmlPullParserFactory.newInstance("org.xmlpull.mxp1.MXParserFactory", PacketTypeConverter.class); //XmlPullParserFactory.newInstance("org.xmlpull.v1.XmlPullParserFactory", PacketTypeConverter.class);
             factory.setNamespaceAware(true);
             XmlPullParser xpp = factory.newPullParser();
 
             xpp.setInput(new ByteArrayInputStream(packet.toXML().getBytes("utf-8")), "utf-8");
 
             xpp.next();
 
             if (packet.getElement().getName().equalsIgnoreCase("iq")) {
                 return PacketParserUtils.parseIQ(xpp, connection);
             }
             else if (packet.getElement().getName().equalsIgnoreCase("presence")) {
                 return PacketParserUtils.parsePresence(xpp);
             }
             else if(packet.getElement().getName().equalsIgnoreCase("message")) {
                 return PacketParserUtils.parseMessage(xpp);
             }
             else {
                 throw new RuntimeException("Unknown packet type " + packet.getElement().getName() + ": " + packet.toXML());
             }
         }
         catch (Exception e) {
             Log.error("Error while parsing packet {}: {}", packet.toXML(), e);
             throw new RuntimeException("exception while parsing packet " + packet.toXML(), e);
         }
     }
 }
