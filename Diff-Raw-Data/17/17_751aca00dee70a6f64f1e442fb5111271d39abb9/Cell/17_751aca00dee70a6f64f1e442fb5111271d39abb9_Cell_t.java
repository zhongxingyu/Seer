 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package wvulaunchpad3;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 /**
  *
  * @author dom
  */
 public class Cell {
     private ArrayList<Part> parts;
    private String name;
     
     
     public Cell(File cellDirectory) throws IOException{
        name = cellDirectory.toString();
         String dendriteRegex = "dendrite1.obj";
         String inputRegex = "input[0-9]+.obj";
         parts = new ArrayList<Part>();
         File[] partFiles = cellDirectory.listFiles();
         
         for (File partFile : partFiles){
             String[] strippedFileList;
             String regex = "_";
             strippedFileList = partFile.toString().split(regex);
             String justFileEnd = strippedFileList[strippedFileList.length-1];
             if (justFileEnd.equals("nucleus.obj")) {
                 parts.add(new Part(partFile));
             }
             else if (justFileEnd.equals("axon.obj")) {
                 parts.add(new Part(partFile));
             }
             else if (justFileEnd.equals("cellbody.obj")) {
                 parts.add(new Part(partFile));
             }
             else if (justFileEnd.matches(dendriteRegex)){
                 parts.add(new Part(partFile));
             }
             else if (justFileEnd.matches(inputRegex)){
                 parts.add(new Part(partFile));
             }
             
         }
         
     }
     
    public String getName(){
        return name;
    }
    
     public String toXML(){
         String xml = "<NumLayers value=\""+parts.size()+"\"/>\n";
         for (int i = 0; i < parts.size(); i++){
             xml += "<Layer"+i+">\n";
             xml += parts.get(i).toXML();
             xml += "</Layer"+i+">\n";
         }
         return xml;
     }
 }
