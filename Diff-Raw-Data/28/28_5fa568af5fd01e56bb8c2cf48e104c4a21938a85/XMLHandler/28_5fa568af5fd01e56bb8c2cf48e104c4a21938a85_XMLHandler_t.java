 import java.util.ListIterator;
 import java.awt.Color; 
 import java.util.ArrayList;
 import java.io.*;
 import javax.xml.parsers.*;
 import javax.xml.transform.*;
 import javax.xml.transform.dom.*;
 import javax.xml.transform.stream.*;
 import org.w3c.dom.*;
 
 /**
  *
  * @author bantony
  */
 public class XMLHandler {
     public SlideHandler slideHandler;
     
     public XMLHandler(SlideHandler slidesH)
     {
         //canvas = c;
         slideHandler = slidesH;
     }
     
     public void writeXMLSlideState(){
         try{
             DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
             DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
             Document doc = docBuilder.newDocument();
             Element root = doc.createElement("presentation");
             doc.appendChild(root);
             
             ArrayList<SlideClass> slideList = slideHandler.getSlideList();
             ListIterator slides = slideList.listIterator();
             
             while(slides.hasNext()){
                 SlideClass curSlide = (SlideClass) slides.next();
                 ArrayList<ShapeClass> objectList =  curSlide.getObjectList();
                 ListIterator objects = objectList.listIterator();
                 while(objects.hasNext()){
                     ShapeClass s = (ShapeClass)objects.next();
                     s.writeXML(doc,root);
                 }
             }
             
             Source src = new DOMSource(doc);
            File file = new File("presentationProject.xml");
            Result dest = new StreamResult(file.toURI().getPath());
             TransformerFactory transformerFactory = TransformerFactory.newInstance();
             Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
             transformer.transform(src,dest);
         }
         catch(Exception e){
             System.err.println(e);
         }
     }
     
     public void readXMLSlideState(String fileName){
         
         File file = new File(fileName);
         Document doc;
         if(file.exists()){
             try{
                 
                 DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                 DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                 doc = docBuilder.parse(file);
                 //iterate through tree
                 Node root = doc.getDocumentElement();
                 NodeList slides = doc.getElementsByTagName("slide");
                 for(int i = 0; i < slides.getLength(); i++){
                     Node objs = slides.item(i).getFirstChild();                            
                     if("text".equals(objs.getNodeValue())){
                         int x = Integer.parseInt(slides.item(i).getNextSibling().getNodeValue());
                         int y = Integer.parseInt(slides.item(i).getNextSibling().getNodeValue());
                         int w = Integer.parseInt(slides.item(i).getNextSibling().getNodeValue());
                         int h = Integer.parseInt(slides.item(i).getNextSibling().getNodeValue());
                         String tString = slides.item(i).getNextSibling().getNodeValue();
                         int fontSize = Integer.parseInt(slides.item(i).getNextSibling().getNodeValue());
                         Color c = new Color(Integer.parseInt(slides.item(i).getNextSibling().getNodeValue()));
                                 
                     }
                     else if("freehand".equals(objs.getNodeValue())){
                         int x = Integer.parseInt(slides.item(i).getNextSibling().getNodeValue());
                         int y = Integer.parseInt(slides.item(i).getNextSibling().getNodeValue());
                         int w = Integer.parseInt(slides.item(i).getNextSibling().getNodeValue());
                         int h = Integer.parseInt(slides.item(i).getNextSibling().getNodeValue());
                         Color c = new Color(Integer.parseInt(slides.item(i).getNextSibling().getNodeValue()));
                         //point list
                     }
                     else{
                         int type = Integer.parseInt(slides.item(i).getNextSibling().getNodeValue());
                         int x = Integer.parseInt(slides.item(i).getNextSibling().getNodeValue());
                         int y = Integer.parseInt(slides.item(i).getNextSibling().getNodeValue());
                         int w = Integer.parseInt(slides.item(i).getNextSibling().getNodeValue());
                         int h = Integer.parseInt(slides.item(i).getNextSibling().getNodeValue());
                         Color lc = new Color(Integer.parseInt(slides.item(i).getNextSibling().getNodeValue()));
                         Color fc = new Color(Integer.parseInt(slides.item(i).getNextSibling().getNodeValue()));
                         //create shape
                     }
                     //create slide
                 }
                 
             }
             catch(Exception e){
                 System.err.print(e);
             }
         }
     }
     
     
 }
