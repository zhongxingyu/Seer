 package com.phonegap.api.impl;
 
 import com.phonegap.PhoneGap;
 import com.phonegap.api.Command;
 
 public class FileCommand implements Command {
 	private static final String CODE = "PhoneGap=file";
 	private static final int READ_COMMAND = 0;
 	
 	/**
 	 * Determines whether the specified instruction is accepted by the command. 
 	 * @param instruction The string instruction passed from JavaScript via cookie.
 	 * @return true if the Command accepts the instruction, false otherwise.
 	 */
 	public boolean accept(String instruction) {
 		return instruction != null && instruction.startsWith(CODE);
 	}
 	
 	/**
 	 * Invokes internal phone application.
 	 */
 	public String execute(String instruction) {
 		switch (getCommand(instruction)) {
 			case READ_COMMAND: 
 				try {
 					String filePath = instruction.substring(CODE.length() + 6);
					return ";if (navigator.file.read_success != null) { navigator.file.read_success('"+escapeString(filePath)+"'); };";
 				} catch (Exception e) {
 					return ";if (navigator.file.read_error != null) { navigator.file.read_error('Exception: " + e.getMessage().replace('\'', '`') + "'); };";
 				}
 		}
 		return null;
 	}
 	
 	private int getCommand(String instruction) {
 		String command = instruction.substring(CODE.length()+1);
 		if (command.startsWith("read")) return READ_COMMAND;
 		return -1;
 	}
 	
 	private String escapeString(String value) {
 		// Replace the following:
 		//   => \ with \\
 		//   => " with \"
 		//   => ' with \'
 		value = PhoneGap.replace(value, "\\", "\\\\");
 		value = PhoneGap.replace(value, "\"", "\\\"");
 		value = PhoneGap.replace(value, "'", "\\'");
 		
 		return value;
 	}
 }
