 import java.util.*;
 import java.io.*;
 
 public class RandomSource {
 	public static final int pool_size = 10000000; 
	public static final long[] randomSource = new int[pool_size];
 	private static boolean __initialized___ = false; 
 	private static Random random = new Random();
	
 	public static class NoMoreEntropyException extends RuntimeException {}
 	
 	public static void generate() {
 		if(!__initialized___) {
 			for(int i = 0; i < randomSource.length; i++) {
				randomSource[i] = random.nextLong();
 			}
 			__initialized___ = true;
 		}
 	}
 	
 	public static void save(String fname) {
 		FileWriter storageFile = new FileWriter(fname);
 		BufferedWriter os = new BufferedWriter(storageFile);
 		for(int i = 0; i < randomSource.length; i++) {
 			os.write("" + randomSource[i]);
 			if(i < randomSource.length - 1) os.write("\n");
 		}
 		os.flush();
 		os.close();
 	}
 	
 	public static void load(String fname) {
		FileInputStream storageFile = new FileInputStream(fname);
		long number = 0L;
		int index = 0;
		int character = 0;
 		try {
 			while((character = storageFile.read()) != -1) {
 				if('0' <= character && character <= '9')
 					number = number * 10 + character - '0';
 				else if(character == '\n' || character == '\r') {
 					randomSource[index] = number;
 					number = 0L;
 					index++;
 				} else {
 					throw new IOException("Invalid input");
 				}
 			}
 		} catch(IOException ex) {
 			System.out.println("Input error");
 			System.exit(0);
 		}
 	}
 	
 	public static void main(String[] args) {
 		generate();
 		save("numbers");
 	}
 }
 // 10 (ben)
// 6  (tom)
