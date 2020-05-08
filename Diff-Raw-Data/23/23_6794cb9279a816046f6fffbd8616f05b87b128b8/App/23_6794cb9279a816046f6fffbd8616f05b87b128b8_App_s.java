 package edu.ibs.core;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
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
		float f = 8.60f;
		System.out.println(f);
		double d = (double) f;
		System.out.println(d);
		BigDecimal bd = new BigDecimal(d);
		System.out.println(bd.floatValue());
		System.out.println(bd.toPlainString());
 	}
 }
