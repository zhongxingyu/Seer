 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package test;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Random;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /**
  *
  * @author Matthew Shepherd <s0935850> and Robert Evans <s0949775>
  */
 public class XMLReader {
 
     String fname;
     private Document dom;
 
     /**
      * Creates a new XML reader
      *
      * @param file file to open
      */
     public XMLReader(String file) {
         fname = file;
 
     }
 
     /**
      * Opens the XML file and loads data into manager
      *
      * @param manager The object manager in which to store the new polygons
      * @param scale The scale of the image, if image is too big it is scaled
      * down, this value allows loader to scale polygons as they load
      */
     public void openXML(ObjectManager manager, double scale) {
 
 
 
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 
         try {
 
             //Using factory get an instance of document builder
             DocumentBuilder db = dbf.newDocumentBuilder();
 
             //parse using builder to get DOM representation of the XML file
             dom = db.parse(fname);
 
 
         } catch (ParserConfigurationException pce) {
             //pce.printStackTrace();
             return;
         } catch (SAXException se) {
             //se.printStackTrace();
             return;
         } catch (IOException ioe) {
            manager.clear();
            
             //ioe.printStackTrace();
             return;
         }
 
         //System.out.println("LOADED");
        
 
         Element docEle = dom.getDocumentElement();
 
         //get a nodelist of elements
         NodeList nl = docEle.getElementsByTagName("object");
         PolygonObject po;
         if (nl != null && nl.getLength() > 0) {
             for (int i = 0; i < nl.getLength(); i++) {
 
 
                 //System.out.println(((Element)nl.item(i)).getTextContent());
 
                 //get the employee element
                 Element object = (Element) nl.item(i);
 
                 String name = getTextValue(object, "name");
                 po = new PolygonObject();
                 po.setName(name);
 
                 NodeList polys = object.getElementsByTagName("polygon");
                 Element poly = (Element) polys.item(0);
                 NodeList points = poly.getElementsByTagName("pt");
                 if (points != null) {
                     for (int p = 0; p < points.getLength(); p++) {
                         //System.out.println(points.item(p).getNodeName());
                         Element point = (Element) points.item(p);
 
 
 
                         int x = Integer.parseInt(getTextValue(point, "x"));
                         int y = Integer.parseInt(getTextValue(point, "y"));
                         //System.out.println("POINT: " + x * scale + ", " + y * scale);
                         po.addPoint((int) (x * scale), (int) (y * scale));
                         //po.addPoint(x,y);
 
 
 
 
                     }
                 }
                 Random gen = new Random();
                 po.setColor(gen.nextInt(256), gen.nextInt(256), gen.nextInt(256));
                 po.generatePoly();
                 manager.addObject(po);
 
                 //get the Employee object
                 //Employee e = getEmployee(el);
 
                 //add it to list
                 //myEmpls.add(e);
             }
         }
 
 
 
 
 
     }
 
     private String getTextValue(Element ele, String tagName) {
         String textVal = null;
         NodeList nl = ele.getElementsByTagName(tagName);
         if (nl != null && nl.getLength() > 0) {
             Element el = (Element) nl.item(0);
             textVal = el.getFirstChild().getNodeValue();
         }
 
         return textVal;
     }
 }
