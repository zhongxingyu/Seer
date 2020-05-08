 package org.kalisen.classpathdoctor;
 
 import java.util.List;
 
 public class DefaultClassPathFormatter implements ClassPathFormatter {
 	
 	private String pathSeparator = null;
 	
 	public String format(ClassPath cp) {
 		StringBuilder builder = format(cp, new StringBuilder());
 		return builder.toString();
 	}
 	
 	public StringBuilder format(ClassPath cp, StringBuilder builder) {
 		if (builder == null) {
 			builder = new StringBuilder();
 		}
         List<PathEntry> entries = cp.getEntries();
        boolean separate = false;
         for (PathEntry entry : entries) {
        	if (separate) {
         		builder.append(getPathSeparator());
         	}
             builder.append(entry.toString());
    		separate = true;
         }
         return builder;
 	}
 
 	public String getPathSeparator() {
 		if (this.pathSeparator == null) {
 			this.pathSeparator = System.getProperty("path.separator");
 		}
 		return this.pathSeparator;
 	}
 
 	public void setPathSeparator(String pathSeparator) {
 		this.pathSeparator = pathSeparator;
 	}
 }
