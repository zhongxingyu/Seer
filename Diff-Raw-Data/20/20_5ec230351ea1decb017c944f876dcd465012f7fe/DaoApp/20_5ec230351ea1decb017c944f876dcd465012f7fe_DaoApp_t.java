 package com.mirantis.aminakov.bigdatacourse;
 
 import org.apache.log4j.Logger;
 
 /**
  * Hello world!
  *
  */
 public class DaoApp {
 	public static final Logger LOG = Logger.getLogger(DaoApp.class);
 	
     public static void main( String[] args ) {
     	System.out.println( "Hello World! From dao." );
     	LOG.debug("Print string <Hello world!>.");
     }
 }
