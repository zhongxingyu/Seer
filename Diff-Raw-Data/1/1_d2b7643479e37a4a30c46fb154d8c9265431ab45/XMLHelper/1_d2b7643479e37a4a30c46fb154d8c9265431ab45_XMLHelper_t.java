 package directi.androidteam.training.lib.xml;
 
 import android.util.Log;
 import directi.androidteam.training.TagStore.Tag;
 import directi.androidteam.training.chatclient.PacketStore.MessageQueue;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserFactory;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import java.io.ByteArrayOutputStream;
 import java.io.OutputStream;
 import java.io.Reader;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Set;
 
 /**
  * Created with IntelliJ IDEA.
  * User: vinayak
  * Date: 29/8/12
  * Time: 4:49 PM
  * To change this template use File | Settings | File Templates.
  */
 public class XMLHelper {
 
 
     public Element buildElement(Document document,Tag tag){
         try {
             Element rootelem = document.createElement(tag.getTagname());
             HashMap<String,String> attrmap = tag.getAttributes();
             if(attrmap!=null){
                 Set<String> attr =  tag.getAttributes().keySet();
                 for (String key : attr) {
                     rootelem.setAttribute(key,attrmap.get(key));
                 }
             }
             ArrayList<Tag> childT = tag.getChildTags();
             if(childT!=null)
                 for (Tag tag1 : childT) {
                     rootelem.appendChild(buildElement(document,tag1));
                 }
             if(tag.getContent()!=null)
                 rootelem.setTextContent(tag.getContent());
             return rootelem;
         }
         catch (Exception e){e.printStackTrace();return  null;}
     }
     public  String buildPacket(Tag tag){
 
         DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
         try {
             DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
             Document document = documentBuilder.newDocument();
 
             Element rootelem = buildElement(document,tag);
             document.appendChild(rootelem);
 
             TransformerFactory factory = TransformerFactory.newInstance();
             Transformer transformer = factory.newTransformer();
 
             DOMSource domSource = new DOMSource(document.getDocumentElement());
             OutputStream output = new ByteArrayOutputStream();
             StreamResult result = new StreamResult(output);
             transformer.transform(domSource, result);
             String xmlString = output.toString();
             //System.out.println(xmlString);
             //Log.d("msg123",xmlString);
             //Log.d("msg123","heyhey");
             //Log.d("testing buildpacket :",xmlString.split("\\?>")[1]);
             return xmlString.split("\\?>")[1];
 
 
         } catch (Exception e) {
             Log.d("msg123","error happeneing");
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         return null;
 
     }
 
     private boolean streamCondition(int event, String name) {
         if (name.equals("stream:stream")) {
             return false;
         } else {
             return (event!=XmlPullParser.END_TAG);
         }
     }
 
     public Tag tearTag(XmlPullParser xpp){
 
         int event;
         try{
             String name = xpp.getName();
             Log.d("XML : Name",name);
             String content = null;
             HashMap<String,String> map = null;
             if(xpp.getAttributeCount()!=0){
                 map= new HashMap<String, String>();
                 int count = xpp.getAttributeCount();
                 for(int i=0;i<count;i++)
                     map.put(xpp.getAttributeName(i),xpp.getAttributeValue(i));
             }
             ArrayList<Tag> childlist = null;
             do{
                 event=xpp.next();
                 if(event==XmlPullParser.START_TAG)  {
                     if (childlist==null)
                         childlist= new ArrayList<Tag>();
                     childlist.add(tearTag(xpp));
                 }
                 if(event==XmlPullParser.TEXT){
                     content = xpp.getText();
                     event=xpp.next();
                 }
             }while (streamCondition(event, name));
 
             return new Tag(name,map,childlist,content);
         }
         catch (Exception e){e.printStackTrace();return  null;}
     }
     public Tag tearPacket(String xml){
         Tag temptag=null;
         try{
             XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
             XmlPullParser xpp = factory.newPullParser();
             xpp.setInput(new StringReader(xml));
             int event = xpp.getEventType();
             while (event !=XmlPullParser.END_DOCUMENT){
                 if(event==XmlPullParser.START_TAG){
                     temptag =  tearTag(xpp);
                 }
                 event=xpp.next();
             }
         }
         catch (Exception e){e.printStackTrace(); return null;}
         return temptag;
     }
     public Tag tearxmlPacket(Reader reader,String jid){
         Tag temptag=null;
         try{
             Log.d("packetreader","ddfdf");
             XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
             XmlPullParser xpp = factory.newPullParser();
             xpp.setInput(reader);
             int event = xpp.getEventType();
             while (event !=XmlPullParser.END_DOCUMENT){
                 if(event==XmlPullParser.START_TAG){
                     temptag =  tearTag(xpp);
                    temptag.setRecipientAccount(jid);
                     Log.d("packetxml",buildPacket(temptag));
                     MessageQueue.getInstance().pushPacket(temptag);
                     Log.d("packet","packetpushed");
                 }
                 event=xpp.next();
             }
         }
         catch (Exception e){e.printStackTrace(); return null;}
         return temptag;
     }
 }
