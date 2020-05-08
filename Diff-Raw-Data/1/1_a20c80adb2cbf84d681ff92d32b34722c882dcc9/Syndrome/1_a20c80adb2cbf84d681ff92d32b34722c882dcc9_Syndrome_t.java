 package ontology;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 public class Syndrome {
 	public void setArray() {
 		String[] array = new String[10];
 		int arg1 = 10;
 		int arg2 = 2;
		int arg3 = 5;
 		
 		if (arg1 < arg2) {
 			array[10] = "hello world1";
 		} else {
 			array[10] = "hello world2";
 		}
 	}
 	
 	public long divide() {
 		long dividend = 100; 
 		long divisor = 0;
 		
 		long p = dividend; //100
 		long q = p * 100; //10000
 
 		divisor = q * q  - 100 * p * q + p - 100; //100
 		//divisor = 1;
 		
 		long result = dividend / divisor; // 100\0
 				
 		return result;
 	}
 	
 	public void readFileByBytes(String fileName) {
 		File file = new File(fileName);
         try {
             System.out.println("ֽΪλȡļݣһζһֽڣ");
             // һζһֽ
             InputStream in = new FileInputStream(file);
             int tempbyte;
             while ((tempbyte = in.read()) != -1) {
                 System.out.write(tempbyte);
             }
             
             OutputStream out = null;
             //in.close();
             out.close();
         } catch (IOException e) {
             e.printStackTrace();
             return;
         }
 	}
 }
