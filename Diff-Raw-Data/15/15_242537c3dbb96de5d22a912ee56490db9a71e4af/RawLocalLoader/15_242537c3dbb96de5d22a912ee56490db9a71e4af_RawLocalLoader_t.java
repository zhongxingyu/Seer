 package com.grademaster.data;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import com.grademaster.Globals;
 
 public class RawLocalLoader implements IRawLoader {
 	
 	/**
 	 * The path the loader uses for IO
 	 * Must be local
 	 */
 	String path;
 	
 	/**
 	 * Contructs a new RawLocalLoader with a given path
 	 * @param path The Path for IO to take Must be local
 	 */
 	public RawLocalLoader(String path) {
 		Globals.getLogger().log("Created new Raw Local Loader.");
 		this.path=path;
 	}
 	
 	@Override
 	/**
 	 * Loads a string from the given local data path
 	 * @return String the String loaded from the data source
 	 */
 	public String loadString() throws IOException {
 		Globals.getLogger().log("Loading local data as string from " + getPath());
 		@SuppressWarnings("resource")
 		BufferedReader reader = new BufferedReader( new FileReader (path));
 	    String         line = null;
 	    StringBuilder  stringBuilder = new StringBuilder();
 	    String         ls = System.getProperty("line.separator");
 
 	    while( ( line = reader.readLine() ) != null ) {
 	        stringBuilder.append( line );
 	        stringBuilder.append( ls );
 	    }
	    //  Extra debug for Matt - Remove soon
	    //Globals.getLogger().log(stringBuilder.toString());
 	    return stringBuilder.toString();
 	}
 
 	@Override
 	/**
 	 * @return String the path the loader is using for IO
 	 */
 	public String getPath() {
 		Globals.getLogger().log("Returning path...");
 		return path;
 	}
 
 	@Override
 	/**
 	 * Writes a string to the data source
 	 * @param String The data to be written
 	 */
 	public void writeString(String s) throws IOException {
 		Globals.getLogger().log("Writing data as string...");
 		BufferedWriter writer = new BufferedWriter(new FileWriter(path));
 		writer.write(s);
 		writer.flush();
 		writer.close();
 	}
 
 }
