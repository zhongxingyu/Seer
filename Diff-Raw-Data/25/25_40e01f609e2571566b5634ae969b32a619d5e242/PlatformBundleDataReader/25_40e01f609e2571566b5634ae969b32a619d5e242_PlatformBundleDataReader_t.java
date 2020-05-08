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
 
    public void read(PlatformBundleData[] platformBundleDatas){
         try {
             DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
             DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
             Document doc = dBuilder.parse(file);
 
             NodeList nList = doc.getElementsByTagName("data");
 
             for (int temp = 0; temp < nList.getLength(); temp++) {
 
                 Node nNode = nList.item(temp);
 
                 if (nNode.getNodeType() == Node.ELEMENT_NODE) {
 
                     Element dataElement = (Element) nNode;
                     platformBundleDatas[temp] = new PlatformBundleData(Integer.parseInt(dataElement.getAttribute("platformCount")), Integer.parseInt(dataElement.getAttribute("staticObstacleCount")));
                     NodeList staticObstacleList = dataElement.getElementsByTagName("staticObstacle");
                     for(int i=0 ; i < staticObstacleList.getLength(); i++){
                         Element staticObstacleElement = (Element)staticObstacleList.item(i);
                         platformBundleDatas[temp].getStaticObstacleX()[i] = Integer.parseInt(staticObstacleElement.getAttribute("x"));
                         platformBundleDatas[temp].getStaticObstacleY()[i] = Integer.parseInt(staticObstacleElement.getAttribute("y"));
                     }
 
                 }
             }
 
         } catch (Exception e) {
             e.printStackTrace();
         }
 
     }
 
 }
