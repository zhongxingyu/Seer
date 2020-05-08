 package com.mirantis.aminakov.bigdatacourse;
 
 import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
 
 /**
  * Hello world!
  *
  */
 public class DaoApp {
 	public static final Logger LOG = Logger.getLogger(DaoApp.class);
 	
     public static void main( String[] args ) {
    	DOMConfigurator.configure("log4j.xml");
     	System.out.println( "Hello World! From dao." );
     	LOG.debug("Print string <Hello world!>.");
     }
 }
