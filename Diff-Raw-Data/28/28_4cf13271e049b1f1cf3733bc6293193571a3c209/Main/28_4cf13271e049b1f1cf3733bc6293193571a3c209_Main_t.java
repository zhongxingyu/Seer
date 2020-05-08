 package com.github.assisstion.MTGCardPortfolio;
 
 import java.awt.EventQueue;
 
 /*
  * Version:
  * 
 * Pre-Alpha 0.5.2
 *   More bug fixes
 * 
  * Pre-Alpha 0.5.1
  *   Minor fixes including bug fixes
  * 
  * Pre-Alpha 0.5.0
  *   Portfolio in usable state
  *   Implemented Individual Card Viewer
  *   Implemented Individual Card Editor
  *   Changed User Interface
  *   Added file writing to Database
  *   Other minor fixes
  * 
  * Pre-Alpha 0.4.2
  *   Implemented quotes
  *   Other minor fixes
  * 
  * Pre-Alpha 0.4.1
  *   Incorporated Database in MTGCardPortfolio
  * 
  * Pre-Alpha 0.4.0
  *   Database usable
  *   Other minor edits
  * 
  * Pre-Alpha 0.3.0 (synchronized with superproject)
  *   Started project
  *   Started main
  *   Started GUI
  *   Started database
  * 
  */
 
 public class Main{
 	
 	private static PortfolioFrame mainFrame;
 	
 	public static void main(String[] args){
 		System.out.println("Test Started");
 		EventQueue.invokeLater(new Runnable(){
 			public void run(){
 				try{
 					mainFrame = new PortfolioFrame();
 					mainFrame.setVisible(true);
 				}
 				catch(Exception e){
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 }
