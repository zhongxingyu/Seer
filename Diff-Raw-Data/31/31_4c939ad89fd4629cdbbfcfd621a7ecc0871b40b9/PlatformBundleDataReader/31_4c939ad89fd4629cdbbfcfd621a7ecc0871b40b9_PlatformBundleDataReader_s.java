 package com.twam.boostrunner.reader;
 
 import com.twam.boostrunner.data.PlatformBundleData;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Node;
 import org.w3c.dom.Element;
 import org.xml.sax.SAXException;
 
 import java.io.File;
 import java.io.IOException;
 
 /**
  * Created by masseeh on 9/4/13.
  */
 public class PlatformBundleDataReader {
 
     //============================================================
     // constants
     //============================================================
     private final String fileAdress = "datas/platformBundleDatas.xml";
 
 
     //============================================================
     // fields
     //============================================================
 
     private File file;
 
     //============================================================
     // constructors
     //============================================================
 
     public PlatformBundleDataReader(){
         file = new File(fileAdress);
     }
 
     //============================================================
     // methods
     //============================================================
 
    public void read(PlatformBundleData[] platformBundleData, int size){
         try {
             DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
             DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
             Document doc = dBuilder.parse(file);
 
            NodeList nList = doc.getElementsByTagName("");
            int idx = 0;
 
             for (int temp = 0; temp < nList.getLength(); temp++) {
 
                 Node nNode = nList.item(temp);
 
                 if (nNode.getNodeType() == Node.ELEMENT_NODE) {
 
                    Element eElement = (Element) nNode;

                    platformBundleData[idx].setNumberOfPlatform(Integer.parseInt(eElement.getAttribute("platformCount")));
                    platformBundleData[idx].setNumberOfStaticObstacle(Integer.parseInt(eElement.getAttribute("staticObstacleCount")));
                    System.out.println("Last Name : " + eElement.getElementsByTagName("lastname").item(0).getTextContent());
                    System.out.println("Nick Name : " + eElement.getElementsByTagName("nickname").item(0).getTextContent());
                    System.out.println("Salary : " + eElement.getElementsByTagName("salary").item(0).getTextContent());
 
                 }
             }
 
         } catch (Exception e) {
             e.printStackTrace();
         }
 
     }
 
 }
