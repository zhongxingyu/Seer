 package org.kalibro;
 
 import java.io.File;
 
 public final class ImportConfiguration {
 
 	public static void main(String[] arguments) {
		if (arguments.length < 1)
			throw new IllegalArgumentException("Expected configuration file path as argument.");
		Configuration.importFrom(new File(arguments[0])).save();
 	}
 
 	private ImportConfiguration() {
 		return;
 	}
 }
