 package main;
 
 import java.security.NoSuchAlgorithmException;
 import java.security.SecureRandom;
 
 public class Program {
 	static SecureRandom ran;
 	
 	public static void main(String[] args) throws NoSuchAlgorithmException {
 		ran = new SecureRandom();
                 
 		//RainbowTable rainbow = generateRainbowTable();
		String path = "small.rainbow";
 		//RainbowTableIO.writeToFile(rainbow, path);
 
 		RainbowTable testTable = RainbowTableIO.readFromFile(path);
 		
 		long plain = 1535;
 		long hash = Utilities.MD5_Hash(plain);
 		long plainGuess = testTable.lookup(hash);
 		
 		System.out.println("Real: " + plain);
 		System.out.println("Guess: " + plainGuess);
 		
 		long guessHashed = Utilities.MD5_Hash(plainGuess);
 		System.out.println("original hash:" + hash + " guessedHashed: " + guessHashed);
 		
 		System.out.println("Main is done");
 	}
 
 	static RainbowTable generateRainbowTable() {
 		RainbowTable rainbow = new RainbowTable();
 
 		long length = (long) Math.pow(2, 10);
 		long rows = (long) Math.pow(2, 18);
 		rainbow.rows = rows;
 		rainbow.chainLength = length;
 		
 		long lastTime = System.currentTimeMillis();
 		for (int i = 0; i < rows; i++) {
 			long startValue = ran.nextInt() % Utilities.bit28;
 			long accumilator = startValue;
 			for (int j = 0; j < length; j++) {
 				long cipher = Utilities.MD5_Hash(accumilator);
 
 				long reducedCipher = Utilities.reductionFunction(cipher, j, Utilities.bit28 + 1);
 				accumilator = reducedCipher;
 			}
 			rainbow.put(accumilator, startValue);
 		}
 
 		long currentTime = System.currentTimeMillis();
 		System.out.println("Generated the table in: "
 				+ (currentTime - lastTime)/1000);
 
 		return rainbow;
 	}
 }
