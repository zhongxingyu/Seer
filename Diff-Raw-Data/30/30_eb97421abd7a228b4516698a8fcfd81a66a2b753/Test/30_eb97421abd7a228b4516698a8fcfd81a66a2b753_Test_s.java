package mybencode;
 
 import java.io.ByteArrayInputStream;
 
 
 public class Test {
 	public static void main(String[] args) throws Exception {
 		String text = "012345678e" ;
 		ByteArrayInputStream is = new ByteArrayInputStream(text.getBytes("US-ASCII"));
 		Scanner scanner = new Scanner();
 		scanner.scan(is);
 	} 
 }
