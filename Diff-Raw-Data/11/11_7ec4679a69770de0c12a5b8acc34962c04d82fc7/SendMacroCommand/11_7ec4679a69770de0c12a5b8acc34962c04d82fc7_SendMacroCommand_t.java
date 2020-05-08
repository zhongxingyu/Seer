 /*
  * Created on Mar 27, 2005
  */
 package com.arcaner.warlock.rcp.ui.macros.internal.commands;
 
 import com.arcaner.warlock.client.IWarlockClientViewer;
 import com.arcaner.warlock.rcp.ui.macros.IMacroCommand;
 
 /**
  * @author Marshall
  */
 public class SendMacroCommand implements IMacroCommand {
 
 	public String getIdentifier() {
 		return "\\r";
 	}
 
 	public String execute(IWarlockClientViewer context, String currentCommand, int position) {
 		context.getWarlockClient().send(currentCommand.substring(0, position));
		context.getWarlockClient().getDefaultStream().echo(currentCommand.substring(0, position));
 		
 		return currentCommand.substring(position + 2);
 	}
 
 }
