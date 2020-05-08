 package com.koujalgi.shell.commands;
 
 import java.util.Date;
 
 import com.koujalgi.shell.core.AbstractCommand;
 
 public class Now extends AbstractCommand {
 
 	public Now(String baseCommand, int params) {
 		super(baseCommand, params);
 	}
 
 	@Override
 	public String getUsage() {
 		return "now";
 	}
 
	@SuppressWarnings("deprecation")
 	@Override
 	public void execute() {
 		System.out.println("Current system time: "
				+ new Date().toLocaleString());
 	}
 
 	@Override
 	public String getDescription() {
 		return "shows the current system time";
 	}
 
 }
