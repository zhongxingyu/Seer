 package io.onelinelister;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Reads input data that is conformed by one value per line, and returns 
  * a list of values.
  * 
  * @author javier
  *
  */
 public class OneLineListReader<T> {
     
 	////////////////////////////
 	// Instance Variables
 	private final LineParser<T> parser;
 	
 
 	/////////////////////////////
 	// Constructor
 	public OneLineListReader(LineParser<T> parser) {
 		
 		this.parser = parser;
 		
 	}
 	
 	///////////////////////////////
 	// Public Interface
 	public List<T> read(File file) {
 		
 		try {
 			
 			BufferedReader br = new BufferedReader(new FileReader(file));
 			
 			this.read(br);
 			
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		return null;
 		
 	}
 
 	public List<T> read(BufferedReader br) throws IOException {
 		
 		List<T> result = new ArrayList<T>();
 		
 		String currentline = null;
 		
 		while((currentline=br.readLine())!=null) {
 			
 			result.add( this.parser.parse(currentline.trim()));
 			
 		}
 		
		return result;
 		
 	}
 
 	////////////////////////////////
 	// Auxiliary Classes
 	public interface LineParser<T> {
 		
 		public T parse(String line);
 		
 	}
 	
 	public static class IntegerLineParser implements LineParser<Integer>{
 
 		@Override
 		public Integer parse(String line) {
 			
 			return Integer.valueOf(line);
 			
 		}
 		
 	}
 	
 	public static class DoubleLineParser implements LineParser<Double>{
 
 		@Override
 		public Double parse(String line) {
 			
 			return Double.valueOf(line);
 			
 		}
 		
 	}
 	
 	public static class StringLineParser implements LineParser<String> {
 
 		@Override
 		public String parse(String line) {
 			return line;
 		}
 		
 		
 	}
 	
 	
 	/////////////////////////////////
 	// Factory Methods
 	public static OneLineListReader<String> createOneLineListReaderForString() {
 		
 		return new OneLineListReader<String>(new StringLineParser());
 		
 	}
 	
 	public static OneLineListReader<Double> createOneLineListReaderForDouble() {
 		
 		return new OneLineListReader<Double>(new DoubleLineParser());
 		
 	}
 	
 	public static OneLineListReader<Integer> createOneLineListReaderForInteger() {
 		
 		return new OneLineListReader<Integer>(new IntegerLineParser());
 		
 	}
 
 }
