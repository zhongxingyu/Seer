 package com.vbitz.MinecraftScript.commands;
 
 import java.util.ArrayList;
 
 import org.mozilla.javascript.EcmaError;
 import org.mozilla.javascript.EvaluatorException;
 
 import net.minecraft.command.CommandBase;
 import net.minecraft.command.ICommandSender;
 import net.minecraft.command.WrongUsageException;
 import net.minecraft.server.MinecraftServer;
 
 public abstract class ScriptingCommand extends CommandBase {
 	
 	private static String concat(String[] arr, int start, String concatStr) {
 		String ret = "";
 		for (int i = start; i < arr.length; i++) {
 			if (i != arr.length - 1) {
 				ret += arr[i] + concatStr;
 			}
 		}
 		ret += arr[arr.length - 1];
 		return ret;
 	}
 	
 	private static String[] slice(String[] arr, int start, int length) {
 		String[] ret = new String[length - start];
 		int x = 0;
 		for (int i = start; i < length; i++) {
 			ret[x++] = arr[i];
 		}
 		return ret;
 	}
 	
 	protected ArrayList<String> commandNames = new ArrayList<String>();
 	
 	public abstract String getName();
 	public abstract void runString(ICommandSender cmdSender, String code);
 	public abstract void runFile(ICommandSender cmdSender, String code);
 	public abstract void runBook(ICommandSender cmdSender);
 	public abstract void runCommand(ICommandSender cmdSender, String commandName, String[] args);
 	public abstract void createStick(ICommandSender cmdSender, String code);
 
 	@Override
 	public String getCommandName() {
 		return getName();
 	}
 	
 	@Override
     public int getRequiredPermissionLevel() {
         return 2;
     }
 
 	@Override
 	public String getCommandUsage(ICommandSender var1) {
 		return "/" + this.getCommandName() + "";
 	}
 
 	@Override
 	public void processCommand(ICommandSender cmdSender, String[] args) {
 		if (args.length == 0) {
 			throw new WrongUsageException(this.getCommandUsage(cmdSender));
 		}
 		
 		if (args[0].equals("dofile")) {
			if (args.length > 1) {
				runFile(cmdSender, args[1]);
			} else {
				throw new WrongUsageException("/" + this.getCommandName() + " dofile filename");
			}
 		} else if (args[0].equals("dobook")) {
 			runBook(cmdSender);
 		} else if (args[0].equals("stick")) {
 			createStick(cmdSender, concat(args, 1, " "));
 		} else if (commandNames.contains(args[0])) {
 			runCommand(cmdSender, args[0], slice(args, 1, args.length));
 		} else {
 			runString(cmdSender, concat(args, 0, " "));
 		}
 	}
 	
 }
