 package com.roshka.raf.command;
 
 import com.roshka.raf.annotations.RAFMethod;
 
 public class RAFCommands {
 
 	@RAFMethod(value="/raf/version")
 	public String getVersion()
 	{
		return "test version";
 	}
 	
 	@RAFMethod(value="/raf/status")
 	public String getStatus()
 	{
 		return "will return status";
 	}
 	
 }
