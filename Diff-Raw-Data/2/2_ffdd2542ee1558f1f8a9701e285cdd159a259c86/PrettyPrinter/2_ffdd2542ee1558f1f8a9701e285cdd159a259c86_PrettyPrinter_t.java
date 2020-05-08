 package edu.tum.lua.exceptions;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 
 import util.BlockRegistry;
 import edu.tum.lua.ast.Block;
 import edu.tum.lua.ast.SyntaxNode;
 import edu.tum.lua.stdlib.ToString;
 
 public class PrettyPrinter {
 
 	private File getFile(SyntaxNode node) {
 		SyntaxNode current = node;
 
 		while (current.getParent() != null) {
 			current = current.getParent();
 		}
 
 		if (current instanceof Block) {
 			return BlockRegistry.lookup((Block) current);
 		}
 
 		return null;
 	}
 
 	private String getFileName(SyntaxNode node) {
 		File file = getFile(node);
 
 		if (file == null) {
 			return "?";
 		}
 
 		return file.getName();
 	}
 
 	public void print(LuaRuntimeException e) {
 		SyntaxNode errorNode = e.getSyntaxNode();
 
 		// Print Exception Location
 		File file = getFile(errorNode);
 
 		final int column = e.getLocation().getColumn();
 		final int row = e.getLocation().getRow();
 
 		if (file != null) {
 			try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
 				for (int i = 0; i < row - 1; i++) {
 					reader.readLine();
 				}
 
 				String prefix = file.getPath() + ":" + row + ": ";
 				System.out.println(prefix + reader.readLine());
 
 				// Print Arrow
 				for (int i = 0; i < column + prefix.length() + 1; i++) {
 					System.out.print("~");
 				}
 
 				System.out.println("^");
 			} catch (IOException ex) {
 				System.out.print("Failed to find error location");
 			}
 		}
 
 		// Print Exception Message
		System.out.println("reason: " + e.getMessage());
 		System.out.println();
 
 		if (e.stacktrace.size() != 0) {
 			System.out.println("stack traceback:");
 			for (LuaStackTraceElement stackTraceElement : e.stacktrace) {
 				printStackTraceElement(stackTraceElement);
 			}
 		}
 	}
 
 	private void printStackTraceElement(LuaStackTraceElement stacktrace) {
 		System.out.print("\t");
 
 		if (stacktrace.location != null) {
 			System.out.printf("%s:%d: %s(", getFileName(stacktrace.node), stacktrace.location.getRow(),
 					stacktrace.functionName);
 		} else {
 			System.out.printf("no location given: %s(", stacktrace.functionName);
 		}
 
 		// Print args
 		boolean afterFirst = false;
 		for (Object arg : stacktrace.args) {
 			if (afterFirst) {
 				System.out.print(", ");
 			}
 
 			System.out.print(ToString.toString(arg));
 			afterFirst = true;
 		}
 
 		System.out.println(")");
 	}
 }
