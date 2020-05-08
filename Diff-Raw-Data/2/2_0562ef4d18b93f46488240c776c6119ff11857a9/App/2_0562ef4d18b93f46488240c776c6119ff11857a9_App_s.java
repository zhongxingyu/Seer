 package com.morningstar.grocerystore;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class App {
 
 	final static Logger logger = LoggerFactory.getLogger(App.class);
 
 	public static void main(String[] args) {
 
 		if (args == null || args.length != 1) {
 
 			logger.info("Please input data file name.");
 			return;
 		}
 
 		InputStreamReader input = null;
 		try {
 
 			input = new InputStreamReader(new FileInputStream(args[0]));
 			Dispatcher dispatcher = new Dispatcher();
 			int time = dispatcher.run(input);
 			System.out.println(String
					.format("Finished at: t= {} minutes", time));
 
 		} catch (Exception ex) {
 
 			logger.error(ex.getMessage(), ex);
 
 		} finally {
 			try {
 				if (input != null) {
 					input.close();
 				}
 			} catch (IOException e) {
 				logger.error(e.getMessage(), e);
 			}
 		}
 	}
 }
