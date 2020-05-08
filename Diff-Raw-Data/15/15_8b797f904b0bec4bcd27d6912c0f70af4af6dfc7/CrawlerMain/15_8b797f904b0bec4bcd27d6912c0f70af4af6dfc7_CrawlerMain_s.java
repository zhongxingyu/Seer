 package Hibu;
 
 
 import LinkFuture.Core.*;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.sql.SQLException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Administrator
  * Date: 9/30/13
  * Time: 2:00 PM
  * To change this template use File | Settings | File Templates.
  */
 public class CrawlerMain {
     public static void main(String[] args) {
        ResetCSVFile();
//        CrawlingController controller = new CrawlingController();
//        try {
//            controller.start();
//        } catch (Exception e) {
//            Debugger.traceln(e);
//            Debugger.traceln("Script terminated unexpected");
//        }
     }
     public static void ResetCSVFile(){
         String filePath = "D:\\SP_Restaurant_listings_20131010.csv";
         File file = new File(filePath);
         if(file.exists())
         {
             StringBuilder sb = new StringBuilder();
             try(BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))){
                 String lineString;
                 while ((lineString = bufferedReader.readLine())!=null) {
                     if(lineString.indexOf("ݬ")>0)
                     {
                         lineString = lineString.replace("ݬ","");
                     }
                     if(lineString.indexOf("NULL")>0)
                     {
                         lineString = lineString.replace("NULL","\\N");
                     }
                     sb.append(lineString+"\r\n");
                 }
             } catch (IOException e) {
                 e.printStackTrace();
             }
             Utility.saveStringToFile(filePath + "_tt",sb.toString(),true);
         }
     }
 }
