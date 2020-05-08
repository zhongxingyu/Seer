 import java.math.BigInteger;
 import java.io.FileReader;
 import java.io.FileNotFoundException;
 import java.util.Scanner;
 public class Number13
 {
 	public static void main(String[] args) throws FileNotFoundException
 	{
		Stopwatch s = new Stopwatch();
		String fileInputName = "Number13.txt";
 		FileReader reader = new FileReader(fileInputName);
 		Scanner in = new Scanner(reader);
 		BigInteger sum = new BigInteger("0");
 		while (in.hasNextLine())
 		{
 			BigInteger lineONE = new BigInteger(in.nextLine());
			BigInteger lineTWO = new BigInteger(in.nextLine());
 			sum = sum.add(lineONE.add(lineTWO));
 		}

		System.out.println(sum.toString().substring(0, 10));
		System.out.println(s.elapsedTime());
 	}
 
 }
