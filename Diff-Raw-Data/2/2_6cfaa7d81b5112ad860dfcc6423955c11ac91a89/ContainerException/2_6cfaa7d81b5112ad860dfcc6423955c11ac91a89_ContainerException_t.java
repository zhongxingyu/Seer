 package com.ecnmelog.app;
 
 
/** Exception levée lorsqu'on essaye de faire des trucs pas nets avec les containers*/
 class ContainerException extends Exception{
 	public ContainerException(String message){
 		super(message);
 	}
 }
