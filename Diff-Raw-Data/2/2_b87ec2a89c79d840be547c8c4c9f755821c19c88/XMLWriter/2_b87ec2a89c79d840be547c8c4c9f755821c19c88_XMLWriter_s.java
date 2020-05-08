 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package wvulaunchpad3;
 
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 
 /**
  *
  * @author dom
  */
 public class XMLWriter {
     private Set set;
     private String setViewXML;
 
     public XMLWriter(Set set) {
         setViewXML = set.toXML();
         this.set = set;
     }
 
     public XMLWriter() {
         
     }
 
     public void write() throws FileNotFoundException, IOException {
         FileWriter fw = new FileWriter("/home/calvr/NetBeansProjects/WVULaunchPad3/src/wvulaunchpad3/middle.xml");
         fw.write(this.setViewXML);
         fw.close();
         String[] params = new String[6];
         params[0] = "python";
         params[1] = "/home/calvr/NetBeansProjects/WVULaunchPad3/src/wvulaunchpad3/xmlWriter.py";
         params[2] = "/home/calvr/NetBeansProjects/WVULaunchPad3/src/wvulaunchpad3/beginning.xml";
         params[3] = "/home/calvr/NetBeansProjects/WVULaunchPad3/src/wvulaunchpad3/middle.xml";
         params[4] = "/home/calvr/NetBeansProjects/WVULaunchPad3/src/wvulaunchpad3/end.xml";
         params[5] = "/home/calvr/setconfig/runtimeConfig.xml";
         Runtime.getRuntime().exec(params);
     }
 
     public void write(String specifiedFilePath) throws FileNotFoundException, IOException {
         String description = set.getDescription() + "\n";
         description += "Cells:\n";
         ArrayList<Cell> cells = set.getCells();
         for (Cell cell : cells){
             description += cell + "\n";
         }
         FileWriter fw = new FileWriter("/home/calvr/NetBeansProjects/WVULaunchPad3/src/wvulaunchpad3/description.xml");
         fw.write("<desc>\n");
         fw.write(description);
         fw.write("</desc>");
         fw.close();
         fw = new FileWriter("/home/calvr/NetBeansProjects/WVULaunchPad3/src/wvulaunchpad3/middle.xml");
         fw.write(this.setViewXML);
         fw.close();
        String[] params = new String[6];
         params[0] = "python";
         params[1] = "/home/calvr/NetBeansProjects/WVULaunchPad3/src/wvulaunchpad3/xmlWriter.py";
         params[2] = "/home/calvr/NetBeansProjects/WVULaunchPad3/src/wvulaunchpad3/description.xml";
         params[3] = "/home/calvr/NetBeansProjects/WVULaunchPad3/src/wvulaunchpad3/beginning.xml";
         params[4] = "/home/calvr/NetBeansProjects/WVULaunchPad3/src/wvulaunchpad3/middle.xml";
         params[5] = "/home/calvr/NetBeansProjects/WVULaunchPad3/src/wvulaunchpad3/end.xml";
         params[6] = specifiedFilePath;
         Runtime.getRuntime().exec(params);
     }
     
     public void copyOver(String specifiedFilePath) throws FileNotFoundException, IOException {
       
         String[] params = new String[6];
         params[0] = "python";
         params[1] = "/home/calvr/NetBeansProjects/WVULaunchPad3/src/wvulaunchpad3/xmlWriter.py";
         params[2] = "/home/calvr/NetBeansProjects/WVULaunchPad3/src/wvulaunchpad3/empty.xml";
         params[3] = "/home/calvr/NetBeansProjects/WVULaunchPad3/src/wvulaunchpad3/empty.xml";
         params[4] = specifiedFilePath;
         params[5] = "/home/calvr/setconfig/runtimeConfig.xml";
         Runtime.getRuntime().exec(params);
     }
 }
