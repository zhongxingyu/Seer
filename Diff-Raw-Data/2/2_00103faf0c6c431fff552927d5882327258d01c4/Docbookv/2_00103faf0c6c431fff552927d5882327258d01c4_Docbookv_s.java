 package com.javaserv.docbook.manualvalidator;
 
 /**
  * The class what contains the main method for the console run
  * DocBook versions: http://www.oasis-open.org/docbook/xml/
  * 
  * @author Daniel Bruessler <mail@danielbruessler.de>
  * @since 0.1.0
  */
 public class Docbookv {
 	static public String VERSION = "0.3.0";
	static public String VERSION_DESCRIPTION = "0.3.0 next milestone: Result in JSON-format with parameter --json, dokumentation refactoring and extended for parameters help und --json";
 
 	public static void main(String[] args){
 		Controller userInfo = new Controller(args, false);
 	}
 }
