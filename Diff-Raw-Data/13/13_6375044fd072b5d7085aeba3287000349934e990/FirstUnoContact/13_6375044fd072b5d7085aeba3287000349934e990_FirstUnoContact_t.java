 package test;

import com.sun.star.lang.XMultiComponentFactory;
 /******************************************************************************
  |FirstUnoContact.java|  -  TODO description
 
  begin                : |15.02.2009|
  copyright            : (C) |2009| by |SchubertT006|
  email                : |development@thomas-michel.info|
  ******************************************************************************/
 /******************************************************************************
  *                                                                            *
  *   This program is free software; you can redistribute it and/or modify     *
  *   it under the terms of the Lesser GNU General Public License as published *
  *   by the Free Software Foundation; either version 2 of the License, or     *
  *   (at your option) any later version.                                      *
  *                                                                            *
  ******************************************************************************/
import com.sun.star.uno.XComponentContext;
 
 
/** 
  * @author SchubertT006
  *
  */
 public class FirstUnoContact {
     
     public static void main(String[] args) {
         execution();
     }
 
     /**
      * 
      */
     private static void execution() {
         try {
             // get the remote office component context
            XComponentContext xContext = com.sun.star.comp.helper.Bootstrap
                     .bootstrap();
  
             System.out.println("Connected to a running office ...");
  
            XMultiComponentFactory xMCF = xContext
                     .getServiceManager();
  
             String available = (xMCF != null ? "available" : "not available");
             System.out.println("remote ServiceManager is " + available);
         } catch (java.lang.Exception e) {
             e.printStackTrace();
         } finally {
             System.exit(0);
         }
     }
 }
 
 
 /**
 * ChangeLog
 * 
 * $Revision:$
 * 
 */
