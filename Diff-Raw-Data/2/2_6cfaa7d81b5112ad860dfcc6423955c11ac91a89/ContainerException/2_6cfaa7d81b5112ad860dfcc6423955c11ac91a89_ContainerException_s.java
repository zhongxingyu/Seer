 package com.ecnmelog.app;
 
 
/** Exception levée lorsqu'on essaye d'interagir avec un container qui n'existe pas*/
 class ContainerException extends Exception{
 	public ContainerException(String message){
 		super(message);
 	}
 }
