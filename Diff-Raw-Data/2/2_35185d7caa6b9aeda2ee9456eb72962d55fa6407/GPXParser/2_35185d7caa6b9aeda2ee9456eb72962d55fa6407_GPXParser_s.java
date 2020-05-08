 /**
  * program to convert .gpx files downloaded from geocaching.com 
  * to .txt files with selected information
  * in addition the program downloads the images enclosed the in description
  * Author: kmaeder
  * Mail: kembinski(-at-)gmx.de
  * Version: 0.1, july 2012
  */
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.DocumentBuilder;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Node;
 import org.w3c.dom.Element;
 import java.io.File;
 import java.io.FileWriter;
 import java.util.StringTokenizer;
 
 import java.awt.image.BufferedImage;
 import java.net.URL;
 import javax.imageio.ImageIO;
 
public class XMLParsergpx {
 
     //path to the working directory (a subdirectory output is needed)
     private static String path = "/home/kevin/geocaching/";
 
     //load images from webpage or not
     private static boolean images = true;
 
     public static void main(String argv[]) {
 
         if (argv.length < 1){
             System.err.println("No .gpx source file given.");
             System.exit(1);
         }
         else if (argv.length >= 2){
             if (argv[1].equals("0")){
                 images = false;
             }
         }
         if(!argv[0].contains(".gpx")){
             System.err.println("Given file is no .gpx file.");
             System.exit(1);
         }
 
         try {
 
             File gpxFile = new File(path+argv[0]);
             DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
             Document doc = docFactory.newDocumentBuilder().parse(gpxFile);
             doc.getDocumentElement().normalize();
 
             //for all caches on the gpx file (marked with tag wpt, read in the data
             NodeList nList = doc.getElementsByTagName("wpt");
             for (int temp = 0; temp < nList.getLength(); temp++) {
 
                 //reading data and saving in variables
                 Node cache = nList.item(temp);
                 String coords = "Lat: "+ ((Element) cache).getAttribute("lat")+", Lon: "+ ((Element) cache).getAttribute("lon");
                 String gccode = getTagValue("name",(Element) cache);
                 String name = getTagValue("urlname",(Element) cache);
                 String type = getTagValue("groundspeak:type",(Element) cache);
                 String size = getTagValue("groundspeak:container",(Element) cache);
                 String diff = getTagValue("groundspeak:difficulty",(Element) cache)+"/"+ getTagValue("groundspeak:terrain",(Element) cache);
                 String desc = getTagValue("groundspeak:long_description",(Element) cache);
                 //take short_description if a long description is not given
                 if (desc == ""){
                     desc = getTagValue("groundspeak:short_description",(Element) cache);
                 }
                 desc = delhtml(desc, gccode);
                 String hint = getTagValue("groundspeak:encoded_hints",(Element) cache);
 
                 //change the name of the cache such that it is useable as a
                 //filename (not using /,\,?,%,*,:,|,",<,>, 
                 StringTokenizer tokenizer = new StringTokenizer(name,"/\\?%*:|\"<> ");
                 String filename = path+"output/"+gccode;
                 while (tokenizer.hasMoreTokens()){
                     filename += "_" +tokenizer.nextToken();
                 }
 
                 //open output file and write to it
                 FileWriter output = new FileWriter(new File(filename+".txt"));
                 output.write("Name: "+name+"\n");
                 output.write("GCCode: "+gccode+"\n");
                 output.write(coords+"\n");
                 output.write(type+", D/T: "+diff+", "+size+"\n");
                 //output.write("Size: "+size+"\n");
                 output.write("Description:\n"+desc+"\n");
                 output.write("-------------\n");
 
                 //logs
                 output.write("Logs: \n");
                 NodeList logs = ((Element) cache).getElementsByTagName("groundspeak:log");
                 for (int i = 0; i < logs.getLength(); i++){
                     Node log = logs.item(i);
                     String log_date = getTagValue("groundspeak:date",(Element) log);
                     String log_typ = getTagValue("groundspeak:type",(Element) log);
                     String log_text = getTagValue("groundspeak:text",(Element) log);
                     output.write(log_date.substring(0,10)+", "+log_typ+": "+log_text+"\n");
                 }
                 output.write("-------------\n");
                 output.write("Hint:\n\n\n\n"+hint);
                 output.flush();
                 output.close();
                 
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     //function to delete all html from the description and the load the
     //images from the internet
     private static String delhtml(String input, String gccode){
         String ret = "";
         int numberimg = 0;
         int pos = 0;
         while (true){
             int newpos = input.indexOf('<',pos);
             if(newpos == -1){
                 break;
             }
             ret += input.substring(pos,newpos);
             pos = input.indexOf('>',newpos)+1;
             String tag = input.substring(newpos,pos);
 
             //loading of images from the webpage
             if (tag.length() > 4 && images) {
                 if (tag.substring(1,4).equals("img")){
                     int start = tag.indexOf('"',tag.indexOf("src"))+1;
                     int start2 = tag.indexOf("'",tag.indexOf("src"))+1;
                     if ((start2 > 0 && start2 < start) || (start <= 0)){
                         start = start2;
                     }
                     int end = tag.indexOf('"',start);
                     int end2 = tag.indexOf("'",start);
                     if ((end2 > 0 && end2 < end) || (end <= 0)){
                         end = end2;
                     }
 
                     String urlname = tag.substring(start,end);
                     //System.out.println("Image-URL: "+urlname);
                     File f = null;
                     try{
                         URL url = new URL(urlname);
                         BufferedImage bi = ImageIO.read(url);
                         f = new File(path+"output/"+gccode+"_"+numberimg+".jpg");
                         ImageIO.write(bi,"jpg", f);
                         ret += "(Image: "+gccode+"_"+numberimg+".jpg)";
                         numberimg++;
                     }
                     catch(Exception e){
                         //e.printStackTrace();
                         if( f != null)
                             f.delete();
                         ret += "(Image: not loaded)";
                          
                     }
                 }
             }
         }
         ret += input.substring(pos);
         return ret;
     }
     //function to get tag value of a specified element
     //(from : http://www.mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/ )
     private static String getTagValue(String sTag, Element eElement) {
         NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
 
         Node nValue = (Node) nlList.item(0);
 
         return nValue.getNodeValue();
     }
 
 }
