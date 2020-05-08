 package edu.ibs.core;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
import java.util.Date;
 import org.apache.log4j.PropertyConfigurator;
 
 /**
  * Hello world!
  *
  */
 public class App {
 
 	static {
 		PropertyConfigurator.configure(App.class.getClassLoader().getResource("log4j.properties"));
 	}
 
 	public static void main(String[] args) throws Exception {
		System.err.print(new Date().toString());
 	}
 }
