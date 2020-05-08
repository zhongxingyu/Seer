 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.team1277.robot;
 
 import edu.wpi.first.wpilibj.networktables.NetworkTable;
 import edu.wpi.first.wpilibj.tables.TableKeyNotDefinedException;
 
 
 /**
  *
  * @author roboclub
  */
 public class ImageProcessor {
     
     //public static NetworkTable server;
     
     
     
     public static void Process() {
         NetworkTable server = NetworkTable.getTable("SmartDashboard");
         
         try
         {
             System.out.println("\\/***\\/");
             //System.out.println(server.getNumber("test"));
             System.out.println(server.containsKey("IMAGE_COUNT"));
             System.out.println(server.getNumber("IMAGE_COUNT",0.0));
             System.out.println("/\\***/\\");
         }
         catch (TableKeyNotDefinedException ex)
         {
             
             ex.printStackTrace();
         }
     }
 
 }
