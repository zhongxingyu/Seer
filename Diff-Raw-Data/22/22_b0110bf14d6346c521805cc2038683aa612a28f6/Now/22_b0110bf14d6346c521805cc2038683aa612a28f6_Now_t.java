 package com.koujalgi.shell.commands;
 
import java.text.DateFormat;
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
 
 	@Override
 	public void execute() {
 		System.out.println("Current system time: "
				+ DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
						DateFormat.MEDIUM).format(new Date()));
 	}
 
 	@Override
 	public String getDescription() {
 		return "shows the current system time";
 	}
 
 }
