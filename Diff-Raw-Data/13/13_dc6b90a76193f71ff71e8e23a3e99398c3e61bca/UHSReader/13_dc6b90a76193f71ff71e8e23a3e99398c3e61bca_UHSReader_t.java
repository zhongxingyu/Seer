 package no.truben.uhs;
 
 import java.io.File;
 import java.io.IOException;
 
 import no.truben.uhs.format.Parser;
 
 
 public class UHSReader {
 	public static void main(String args[]) {
 		try {
 			Parser.parse(new File("tunguska.uhs"));
			System.out.println("Ferdig");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
