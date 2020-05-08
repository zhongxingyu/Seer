 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package jmydays.util;
 
 import javax.swing.*;
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 //import java.io.BufferedWriter;
 //import java.io.File;
 //import java.util.Enumeration;
 
 /**
  *
  * @author Administrator
  */
 public class Util implements jmydays.JMyDaysConstants {
 
     public static byte[] getBufferedResource(String fileName) {
         InputStream is = null;
 
         try {
             is = new BufferedInputStream(Object.class.getClassLoader().getResourceAsStream(fileName));
             byte[] b = new byte[is.available()];
             is.read(b);
             return b;
         } catch (IOException e) {
             System.out.println("IOException when getting file: " + e.getMessage() );
             return null;
         } finally {
             try {
                 is.close();
             } catch (IOException ioe) {
             	System.out.println("IOException closing file: " + ioe.getMessage());
             }
         }
     }
 
     private static final java.util.ResourceBundle recursosTexto;
     //Static inicializer
     static{
         recursosTexto = java.util.ResourceBundle.getBundle("texts", java.util.Locale.getDefault());
     }
 
     public static boolean export2FormatedTxtFile(final String date, final String[] labels, final String[] labelsStat, final String text){
         //Check if skip creating file
         if( labels.length == 0 && (text == null || text.trim().length() == 0) ){
             return true;
         }
 
         final String checked = recursosTexto.getString("txtFileChecked");
         final String missed = recursosTexto.getString("txtFileMissed");
 
         final java.io.BufferedWriter bfw;
         final java.io.FileWriter fw;
 
         try{
         	fw = new java.io.FileWriter(date+".txt");
             bfw = new java.io.BufferedWriter( fw );
 
             bfw.write(date);
             bfw.newLine();
             bfw.newLine();
 
             if( labels.length == labelsStat.length ){
                 for (int i = 0; i < labels.length; i++) {
                 	if( labels[i] != null ){
 	                    bfw.write(labels[i] + ": " + ( Integer.valueOf(labelsStat[i]) > 0 ? checked : missed) );
 	                    bfw.newLine();
                 	}
                 }
             }
 
             bfw.newLine();
 
             if( text != null && text.trim().length() > 0 ){
                 bfw.write(recursosTexto.getString("txtStartMsg"));
                 bfw.newLine();
                 bfw.write(text);
                 bfw.newLine();
                 bfw.write(recursosTexto.getString("txtEndMsg"));
             }else{
                 bfw.write(recursosTexto.getString("txtNoText"));
             }
 
             bfw.close();
         }catch(IOException ioe){
             System.out.println("IOException message when closing file: " + ioe.getMessage() );
             javax.swing.JOptionPane.showMessageDialog(null, ioe.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
             return false;
         }
 
         return true;
     }
 
     //Sample: { "_id" : "2012-09-11", "activities" : [ { "type" : "text", "name" : "My project", "value" : "!!! =)" } ], "notes" : "First notes! Oh yeah!\n =)" }
     public static boolean export2FormatedJsonFile(final String date, final String[] labels, final String[] labelsStat, final String text){
         //Check if skip creating file
         if( labels.length == 0 && (text == null || text.trim().length() == 0) ){
             return true;
         }
 
         final String checked = recursosTexto.getString("txtFileChecked");
         final String missed = recursosTexto.getString("txtFileMissed");
 
         final java.io.BufferedWriter bfw;
         final java.io.FileWriter fw;
         try{
             fw = new java.io.FileWriter(date+".txt");
             bfw = new java.io.BufferedWriter( fw );
 
             String jsonStr = "";
             String id = "";
             String activities = "";
             String notes = "";
 
             id = date;
 
             activities = "[ ";
             if( labels.length == labelsStat.length ){
                 for (int i = 0; i < labels.length; i++) {
                     if( labels[i] != null ){
                         //bfw.write(labels[i] + ": " + ( Integer.valueOf(labelsStat[i]) > 0 ? checked : missed) );
                        activities += "{ \"type\" : \"checkbox\", \"name\" : \"" + labels[i] + "\", \"value\" : \""
                                + ( Integer.valueOf(labelsStat[i]) > 0 ? "true" : "false") + "\" },";
                     }
                 }
             }

            activities = activities.substring( 0, activities.length()-1); //remove las ','

             activities += " ]";
 
 
             //bfw.write(text);
             notes = text;
 
             jsonStr = "{ \"_id\" : \"" + id + "\", \"activities\" : " + activities + ", \"notes\" : \"" + text + "\" }";
 
             System.out.println(jsonStr);
 
             bfw.write(jsonStr);
 
             bfw.close();
         }catch(IOException ioe){
             System.out.println("IOException message when closing file: " + ioe.getMessage() );
             javax.swing.JOptionPane.showMessageDialog(null, ioe.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
             return false;
         }
 
         System.out.println("");
 
         return true;
     }
     
     private static String osName = System.getProperty("os.name");
     
     public static OS getOS(){
     	
     	osName = osName.toLowerCase();
     	
     	if( osName.indexOf( "win" ) >= 0 ){
     		return OS.WINDOWS;
     	}
     	else if( osName.indexOf( "mac" ) >= 0 ){
     		return OS.MAC;
     	}
     	else if( osName.indexOf( "nix") >=0 || osName.indexOf( "nux") >=0 ){
     		return OS.LINUX;
     	}else{
     		return OS.UNKNOWN;
     	}
     
     }

 }
