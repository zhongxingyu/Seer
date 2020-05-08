 package com.roshka.raf.command;
 
 import com.roshka.raf.annotations.RAFMethod;
 
 public class RAFCommands {
 
 	@RAFMethod(value="/raf/version")
 	public String getVersion()
 	{
		return "0.2";
 	}
 	
 	@RAFMethod(value="/raf/status")
 	public String getStatus()
 	{
		
 		return "will return status";
 	}
 	
 }
