 package linewars.configfilehandler;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.util.Scanner;
 
 /**
  * This class encapsulates the process of reading data from a config file.
  * @author Knexer
  *
  */
 public class ConfigFileReader {
 	
 	private Scanner file;
 	private int currentLine = 0;
 	private String URI = "";
 	
 	/**
 	 * Instantiates the ConfigFileReader on the given string, which is assumed to be a filepath.
 	 * If the file does not exist, throws an exception.
 	 * 
 	 * @param path
 	 * The filepath to the file to be read.
 	 * @throws FileNotFoundException 
 	 */
 	public ConfigFileReader(String URI) throws FileNotFoundException{
 		file = new Scanner(new File(URI));
 		this.URI = URI;
 	}
 	
 	/**
 	 * Instantiates the ConfigFileReader on the given InputStream.
 	 * @param in
 	 * The stream from which to read data.
 	 */
 	public ConfigFileReader(InputStream in){
 		file = new Scanner(in);
 	}
 	
 	/**
 	 * Reads the data in this Reader's file or input stream into a new ConfigData object.
 	 * 
 	 * @return
 	 * A ConfigData object which contains the configuration data stored in the file.
 	 * @throws InvalidConfigFileException 
 	 */
 	public ConfigData read() throws InvalidConfigFileException
 	{
 		return read(true);
 	}
 	
 	/**
 	 * Reads the data in this Reader's file or input stream into a new ConfigData object.
 	 * 
 	 * @param requiredValid
 	 * true if the config file must be valid, false otherwise
 	 * @return
 	 * A ConfigData object which contains the configuration data stored in the file.
 	 * @throws InvalidConfigFileException 
 	 */
 	public ConfigData read(boolean requiredValid) throws InvalidConfigFileException{
 		ConfigData cd = readRecurse();
 		if(requiredValid)
 			if (!cd.getDefinedKeys().contains(ParserKeys.valid)
 					|| !cd.getString(ParserKeys.valid).equalsIgnoreCase("true"))
 				throw new InvalidConfigFileException("The Config " + URI
 						+ " does not contain the key valid or it is not set to true.");
 		return cd;
 	}
 
 	/**
 	 * @return
 	 * @throws InvalidConfigFileException
 	 */
 	private ConfigData readRecurse() throws InvalidConfigFileException {
 		ConfigData config = new ConfigData(URI, this.currentLine);
 		
 		while(file.hasNextLine())
 		{
 			String l = file.nextLine();
 			this.currentLine++;
 			Scanner line = new Scanner(l);
 			//remove comments
 			if(l.contains("#"))
 				line = new Scanner(l.substring(0, l.indexOf("#")));
 			
 			//if the line is empty
 			if(!line.hasNext())
 				continue;
 			
 			String key = line.next().toLowerCase();
 			
 			if(key.equals("}"))
 				break;
 			
			if(!line.hasNext() || !line.next().equals("=") || !line.hasNext())
 				throw new InvalidConfigFileException(URI + "ERROR LINE " + (this.currentLine) + 
 						": is not a valid config file.");
 			
 			String value = line.nextLine().trim();
 			
 			if(value.equals("{"))
 				config.add(ConfigData.getKey(key), this.readRecurse());
 			else
 				config.add(ConfigData.getKey(key), value);
 		}
 		
 		config.setEndLine(currentLine);
 		return config;
 	}
 	
 	
 	
 	public static class InvalidConfigFileException extends Exception {
 		private static final long serialVersionUID = 8024603342014298051L;
 
 		public InvalidConfigFileException(String s) {
 			super(s);
 		}
 	}
 }
