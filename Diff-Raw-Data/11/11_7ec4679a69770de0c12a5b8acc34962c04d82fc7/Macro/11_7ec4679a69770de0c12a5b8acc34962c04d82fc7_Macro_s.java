 /*
  * Created on Mar 27, 2005
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package com.arcaner.warlock.rcp.ui.macros.internal;
 
 import java.util.Map;
 
 import com.arcaner.warlock.client.IWarlockClientViewer;
 import com.arcaner.warlock.rcp.ui.macros.IMacro;
 import com.arcaner.warlock.rcp.ui.macros.IMacroCommand;
 import com.arcaner.warlock.rcp.ui.macros.IMacroVariable;
 import com.arcaner.warlock.rcp.ui.macros.MacroFactory;
 
 
 public class Macro implements IMacro
 {
 	protected int keycode;
 	protected String command;
 	
 	public Macro (int keycode, String command)
 	{
 		this.keycode = keycode;
 		this.command = command;
 	}
 	
 	public String executeCommand (IWarlockClientViewer context) {
 		Map<String, IMacroVariable> variables = MacroFactory.instance().getMacroVariables();
 		Map<String, IMacroCommand> commands = MacroFactory.instance().getMacroCommands();
 		
 		//context.getCurrentCommand() + 
 		String newCommand = new String(command);
 		
 		for (String var : variables.keySet())
 		{
 			if (newCommand.contains(var))
 			{
 				String newVar = var.replaceAll("\\$", "\\\\\\$");
 				String value = variables.get(var).getValue(context);
 				if (value != null)
 				{
					newCommand = newCommand.replaceAll(newVar, variables.get(var).getValue(context));
 				}
 			}
 		}
 		
 		for (String command : commands.keySet())
 		{
 			if (newCommand.contains(command))
 			{
 				for ( int i = newCommand.indexOf(command); i > -1 && i < newCommand.length(); i = newCommand.indexOf(command, i+1))
 				{
 					String result = commands.get(command).execute(context, newCommand, i);
 					if (result != null)
 					{
 						newCommand = result;
 					}
 				}
 			}
 		}
 		return newCommand;
 	}
 	
 	public int getKeyCode() {
 		return this.keycode;
 	}
 	
 	public String getCommand () {
 		return this.command;
 	}
 	
 	public void setCommand(String command) {
 		this.command = command;
 	}
 	
 	public void setKeyCode(int keycode) {
 		this.keycode = keycode;
 	}
 }
