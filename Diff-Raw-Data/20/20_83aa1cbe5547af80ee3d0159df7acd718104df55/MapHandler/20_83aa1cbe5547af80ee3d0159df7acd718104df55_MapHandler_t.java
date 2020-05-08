 package fap_java;
 
 import java.util.ArrayList;
 
 import java.util.LinkedList;
 
 import java.util.regex.Pattern;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 public class MapHandler extends DefaultHandler {
     private CMap map = new CMap();
     private Cell c;
     private int i = 0;
     private int j = 0;
 
     // Flags for the position of the parser
     private boolean inFile, inI, inJ, inStartCell;
     //buffer for retreiving datas
     private StringBuffer buffer;
 
     public MapHandler() {
         super();
     }
 
 
     // Detecting tag opening
 
     public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
         String regex1 = "st[0-9]";
         if (qName.equals("root")) {
             inFile = true;
         } else if (qName.equals("ligne")) {
             inI = true;
         } else if (qName.equals("map")) {
         } else if (qName.equals("persos")) {
         } else if (qName.matches(regex1)) { // ie <stX> where X is 0->9
             inStartCell = true;
         } else if (qName.equals("i")) {
         } else if (qName.equals("j")) {
         } else {
             buffer = new StringBuffer();
             if (qName.equals("case")) {
                 inJ = true;
             } else {
                 // Error
                 throw new SAXException("Unknown tag " + qName + ".");
             }
         }
         buffer = new StringBuffer();
     }
     //Detecting tag closing
 
     public void endElement(String uri, String localName, String qName) throws SAXException {
         String regex1 = "st[0-9]";
         
         if (qName.equals("root")) {
             inFile = false;
         } else if (qName.equals("ligne")) {
             i++;
             j = 0;
             inI = false;
         } else if (qName.equals("case")) {
             String content = buffer.toString();
             String param = "";
             int t = 0;
             int did = 0;
 
             String[] tabS = content.split(",", 2);
             did = Integer.parseInt(tabS[0]);
             if (tabS.length > 1) {
                 param = tabS[1];
             }
            //System.out.println(did);
 
             if (did >= 100 && did < 200) {
                 t = 20;
             } else if (did >= 200) {
                 t = 19;
             } else if (param.equals("")) {
                 t = 1;
             } else if (param.indexOf(',') == -1) {
                 if(param.matches("[a-z]")){ // Healthy Healthy and special tiles
                     t=1;
                 }
                 else{
                     t = 2;
                 }
             } else if (did == 10) {
                 t = 10;
             } else {
 
             }
             //int t = Integer.parseInt(buffer.toString());
             inJ = false;
             if (did != 0) {
                 c = new Cell(i, j, t, param, did);
                 map.addElement(c);
             }
             j++;
         } else if (qName.equals("map")) {
         } else if (qName.equals("persos")) {
         } else if (qName.equals("i")) {
             i = Integer.parseInt(buffer.toString());
         } else if (qName.equals("j")) {
             j = Integer.parseInt(buffer.toString());
         } else if (qName.matches(regex1)) {
             //Cell c = new Cell(i,j,0,0);
             Cell c = map.getCell(i, j);
             i = 0;
             j = 0;
             int pid = Integer.parseInt("" + qName.charAt(2));
             map.addStartCell(c, pid - 2);
             inStartCell = false;
         } else {
             //Error
             throw new SAXException("Unknown tag " + qName + ".");
         }
     }
     //Detecting chars
 
     public void characters(char[] ch, int start, int length) throws SAXException {
         String lecture = new String(ch, start, length);
         if (buffer != null)
             buffer.append(lecture);
     }
     //Parsing starting
 
     public void startDocument() throws SAXException {
     }
     //End of parsing
 
     public void endDocument() throws SAXException {
         //System.out.println("End of parsing");
     }
 
     public void setMap(CMap map) {
         this.map = map;
     }
 
     public CMap getMap() {
         return map;
     }
 }
