 package net.frontlinesms.messaging;
 
 import java.io.*;
import java.util.*;
 
 class Utils {
 	/** Calls {@link Thread#sleep(long)} and ignores {@link InterruptedException}s thrown. */
 	public static final void sleep(long millis) {
 		try {
 			Thread.sleep(millis);
 		} catch(InterruptedException ex) {
 			// ignore
 		}
 	}
 	
 	/**
 	 * Read all bytes available on the input stream, and concatenates them into a string.
 	 * N.B. This casts bytes read directly to characters.
 	 */
 	public static final String readAll(InputStream in) throws IOException {
 		StringBuilder bob = new StringBuilder();
 		int c;
 		while((c = in.read()) != -1) {
 			bob.append((char) c);
 		}
 		return bob.toString();
 	}
 	
 	/** Writes the supplied command to the output stream, followed by a \r character */
 	public static final void writeCommand(OutputStream out, String command) throws IOException {
 		for(char c : command.toCharArray()) out.write(c);
 		out.write('\r');
 	}
 	
 	/** @return <code>true</code> if the response contains "OK" */
 	public static final boolean isResponseOk(String response) {
 		return response.indexOf("OK") != -1;
 	}
 	
 	/** @return the response with the original command, "OK" and all trailing and leading whitespace removed */
 	public static final String trimResponse(String command, String response) {
 		return response.replace("OK", "").replace(command, "").trim();
 	}
 
 	/**
 	 * Writes an AT command to the serial driver and retrieves the response.  The supplied
 	 * command will be prepended with "AT+" and appended with a \r.  If requested, any
 	 * presence of the command in the response will be removed.
 	 * @param command
 	 * @param removeCommand If set true, the command is removed from the response.
 	 * @return the response to the issued command
 	 * @throws IOException If there was an issue contacting the serial port
 	 */
 	public static String executeAtCommand(InputStream in, OutputStream out, String command, boolean removeCommand) throws IOException {
 		// Issue the command
 		writeCommand(out, "AT+" + command);
 		String response = readAll(in);
 
 		// If requested, remove the command we issued from the response string
 		if(removeCommand) {
 			response = response.replaceAll("\\s*(AT)?\\+" + command + "\\s*", "");
 		}
 
 		return response;
 	}
 }
