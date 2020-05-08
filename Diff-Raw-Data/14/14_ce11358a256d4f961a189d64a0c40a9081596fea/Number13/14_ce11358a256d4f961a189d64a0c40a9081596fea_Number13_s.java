 import java.math.BigInteger;
 import java.io.FileReader;
 import java.io.FileNotFoundException;
 import java.util.Scanner;
 public class Number13
 {
 	public static void main(String[] args) throws FileNotFoundException
 	{
		Scanner input = new Scanner(System.in);
		System.out.print("Enter input file: ");
		String fileInputName = input.nextLine();
 		FileReader reader = new FileReader(fileInputName);
 		Scanner in = new Scanner(reader);
 		BigInteger sum = new BigInteger("0");
 		while (in.hasNextLine())
 		{
 			BigInteger lineONE = new BigInteger(in.nextLine());
			BigInteger lineTWO = new BigInteger(in.nextLine()); 
 			sum = sum.add(lineONE.add(lineTWO));
 		}
		
		System.out.println("Answer: " + sum.toString().substring(0, 10));
 	}
 
 }
