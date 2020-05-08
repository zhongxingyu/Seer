// I don't work yet!!
 
 import java.util.Scanner;
 
 public class Main {
 	public static void Main(String[] args){
 		Scanner scan = new Scanner(System.in);
 		encryptHash encrypt = new encryptHash();
 		System.out.println("Please enter a message: ");
 		encrypt.encryptHash("matt", scan.nextLine());
 	}
 }
