 package rssscout;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.InputStreamReader;
 import java.net.URL;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.jibble.pircbot.Colors;
 import org.w3c.dom.CharacterData;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class RSSReader {
 
     private String firstItem = "";
     private String lastRead = "";
     private static RSSReader instance = null;
     private String lastReadLoc = "";
 
     public RSSReader() {
     }
 
     public static RSSReader getInstance() {
         if (instance == null) {
             instance = new RSSReader();
         }
         return instance;
     }
 
     public void writeNews(String url, String chan, String StringColor, ArrayList<String> sTerms) {
         try {
 
 
             lastReadLoc = MyBot.getLastReadLoc();
             DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
             URL u = new URL(url); // your feed url
             Document doc = builder.parse(u.openStream());
             NodeList nodes = doc.getElementsByTagName("item");
 
             Element firstElement = (Element) nodes.item(0);
             firstItem = getElementValue(firstElement, "link");
 
             lastRead = getLastRead(url);
 
 
             int numOfResults = nodes.getLength();
             numOfResults = MyBot.getMaxResults();
 
 
 
             for (int i = 0; i < numOfResults; i++) {
 
 
                 Element element = (Element) nodes.item(i);
                 String lowerCaseTitle = getElementValue(element, "title").toLowerCase();
 
 
                 if (getElementValue(element, "link").equalsIgnoreCase(lastRead)) { // end of new links
                     i = nodes.getLength();
                 } else {
 
                     if (onSearchList(sTerms, lowerCaseTitle)) {
 
                         if (StringColor.equalsIgnoreCase("black")) {
                             RssScout.bot.sendMessage(chan, Colors.BLACK + getElementValue(element, "title") + Colors.BROWN + " | " + Colors.NORMAL + getElementValue(element, "link"));
                         } else if (StringColor.equalsIgnoreCase("blue")) {
                             RssScout.bot.sendMessage(chan, Colors.BLUE + getElementValue(element, "title") + Colors.BROWN + " | " + Colors.NORMAL + getElementValue(element, "link"));
                         } else if (StringColor.equalsIgnoreCase("bold")) {
                             RssScout.bot.sendMessage(chan, Colors.BOLD + getElementValue(element, "title") + Colors.BROWN + " | " + Colors.NORMAL + getElementValue(element, "link"));
                         } else if (StringColor.equalsIgnoreCase("brown")) {
                             RssScout.bot.sendMessage(chan, Colors.BROWN + getElementValue(element, "title") + Colors.BROWN + " | " + Colors.NORMAL + getElementValue(element, "link"));
                         } else if (StringColor.equalsIgnoreCase("cyan")) {
                             RssScout.bot.sendMessage(chan, Colors.CYAN + getElementValue(element, "title") + Colors.BROWN + " | " + Colors.NORMAL + getElementValue(element, "link"));
                         } else if (StringColor.equalsIgnoreCase("dark_blue")) {
                             RssScout.bot.sendMessage(chan, Colors.DARK_BLUE + getElementValue(element, "title") + Colors.BROWN + " | " + Colors.NORMAL + getElementValue(element, "link"));
                         } else if (StringColor.equalsIgnoreCase("dark_gray")) {
                             RssScout.bot.sendMessage(chan, Colors.DARK_GRAY + getElementValue(element, "title") + Colors.BROWN + " | " + Colors.NORMAL + getElementValue(element, "link"));
                         } else if (StringColor.equalsIgnoreCase("dark_green")) {
                             RssScout.bot.sendMessage(chan, Colors.DARK_GREEN + getElementValue(element, "title") + Colors.BROWN + " | " + Colors.NORMAL + getElementValue(element, "link"));
                         } else if (StringColor.equalsIgnoreCase("green")) {
                             RssScout.bot.sendMessage(chan, Colors.GREEN + getElementValue(element, "title") + Colors.BROWN + " | " + Colors.NORMAL + getElementValue(element, "link"));
                         } else if (StringColor.equalsIgnoreCase("light_gray")) {
                             RssScout.bot.sendMessage(chan, Colors.LIGHT_GRAY + getElementValue(element, "title") + Colors.BROWN + " | " + Colors.NORMAL + getElementValue(element, "link"));
                         } else if (StringColor.equalsIgnoreCase("magenta")) {
                             RssScout.bot.sendMessage(chan, Colors.MAGENTA + getElementValue(element, "title") + Colors.BROWN + " | " + Colors.NORMAL + getElementValue(element, "link"));
                         } else if (StringColor.equalsIgnoreCase("olive")) {
                             RssScout.bot.sendMessage(chan, Colors.OLIVE + getElementValue(element, "title") + Colors.BROWN + " | " + Colors.NORMAL + getElementValue(element, "link"));
                         } else if (StringColor.equalsIgnoreCase("purple")) {
                             RssScout.bot.sendMessage(chan, Colors.PURPLE + getElementValue(element, "title") + Colors.BROWN + " | " + Colors.NORMAL + getElementValue(element, "link"));
                         } else if (StringColor.equalsIgnoreCase("red")) {
                             RssScout.bot.sendMessage(chan, Colors.RED + getElementValue(element, "title") + Colors.BROWN + " | " + Colors.NORMAL + getElementValue(element, "link"));
                         } else if (StringColor.equalsIgnoreCase("teal")) {
                             RssScout.bot.sendMessage(chan, Colors.TEAL + getElementValue(element, "title") + Colors.BROWN + " | " + Colors.NORMAL + getElementValue(element, "link"));
                         } else if (StringColor.equalsIgnoreCase("white")) {
                             RssScout.bot.sendMessage(chan, Colors.WHITE + getElementValue(element, "title") + Colors.BROWN + " | " + Colors.NORMAL + getElementValue(element, "link"));
                         } else if (StringColor.equalsIgnoreCase("yellow")) {
                             RssScout.bot.sendMessage(chan, Colors.YELLOW + getElementValue(element, "title") + Colors.BROWN + " | " + Colors.NORMAL + getElementValue(element, "link"));
                         } else if (StringColor.equalsIgnoreCase("normal")) {
                             RssScout.bot.sendMessage(chan, Colors.NORMAL + getElementValue(element, "title") + Colors.BROWN + " | " + Colors.NORMAL + getElementValue(element, "link"));
                         }
                         }
                 }
            }writeFirstReadToFile(firstItem, url);
 
 
 
 
 
             }//try
          catch (Exception ex) {
             System.out.println(ex);
             writeFirstReadToFile(firstItem, url);
         }
            
 
 
 
     }
 
     private boolean onSearchList(ArrayList<String> sList, String titleToMatch) {
 
         boolean match = false;
         for (int i = 0; i < sList.size(); i++) {
             String currentVal = sList.get(i);
             if ((sList.get(i).equalsIgnoreCase("all")) || (titleToMatch.contains(currentVal))) {
                 match = true;
             }
         }
 
         return match;
     }
 
     private String getLastRead(String url) {
 
         String link = "";
         String md5 = md5(url);
         try {
             // Open the file that is the first 
             // command line parameter
             FileInputStream fstream = new FileInputStream(lastReadLoc + md5 + ".txt");
 
             // Get the object of DataInputStream
             DataInputStream in = new DataInputStream(fstream);
             BufferedReader br = new BufferedReader(new InputStreamReader(in));
             String strLine;
             //Read File Line By Line
             while ((strLine = br.readLine()) != null) {
                 // Print the content on the console
                 System.out.println(strLine);
                 link = strLine;
             }
             //Close the input stream
             in.close();
         } catch (Exception e) {//Catch exception if any
             System.err.println("Error: " + e.getMessage());
         }
 
         return link;
 
 
     }
 
     private void writeFirstReadToFile(String firstRead, String link) {
         try {
             // Create file 
 
             String md5 = md5(link);
             FileWriter fstream = new FileWriter(lastReadLoc + md5 + ".txt");
             BufferedWriter out = new BufferedWriter(fstream);
             out.write(firstRead);
             out.close();
         } catch (Exception e) {//Catch exception if any
             System.err.println("Error: " + e.getMessage());
         }
 
     }
 
     public static String md5(String input) {
         String res = "";
         try {
             MessageDigest algorithm = MessageDigest.getInstance("MD5");
             algorithm.reset();
             algorithm.update(input.getBytes());
             byte[] md5 = algorithm.digest();
             String tmp = "";
             for (int i = 0; i < md5.length; i++) {
                 tmp = (Integer.toHexString(0xFF & md5[i]));
                 if (tmp.length() == 1) {
                     res += "0" + tmp;
                 } else {
                     res += tmp;
                 }
             }
         } catch (NoSuchAlgorithmException ex) {
         }
         return res;
 
     }
 
     private String getCharacterDataFromElement(Element e) {
         try {
             Node child = e.getFirstChild();
             if (child instanceof CharacterData) {
                 CharacterData cd = (CharacterData) child;
                 return cd.getData();
             }
         } catch (Exception ex) {
         }
         return "";
     } //private String getCharacterDataFromElement
 
     protected float getFloat(String value) {
         if (value != null && !value.equals("")) {
             return Float.parseFloat(value);
         }
         return 0;
     }
 
     protected String getElementValue(Element parent, String label) {
         return getCharacterDataFromElement((Element) parent.getElementsByTagName(label).item(0));
     }
 //    public static void main(String[] args) {
 //        RSSReader reader = RSSReader.getInstance();
 //        reader.writeNews(args[0], args[1], args[2], args[3]);
 //    }
 }
