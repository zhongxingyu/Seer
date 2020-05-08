 package edu.asu.krypton.service;
 
import java.io.File;

 import org.junit.Test;
 
 public class MiscTest {
 
 	@Test
 	public void test() {
		System.out.println(
		System.getProperty("user.home")
		);
		File file = new File(System.getProperty("user.home")+File.separator+"keyStore");
		file.mkdirs();
 	}
 
 }
