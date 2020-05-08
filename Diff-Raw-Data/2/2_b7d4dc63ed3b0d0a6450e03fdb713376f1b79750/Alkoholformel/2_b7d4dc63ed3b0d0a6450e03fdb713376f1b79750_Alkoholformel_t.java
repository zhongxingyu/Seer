 package hyttetur;
 
 import java.util.Scanner;
 
 
 
 public class Alkoholformel {
 	
 	public static int alcoholYouShouldBring = 0;
 	
 	public int getAlcoholYouShouldBring()
 	{
 		return alcoholYouShouldBring;
 	}
 	
 	public static int alcoholCalculator(int nrOfDays, int alcoholAmount)
 	{
 		if(nrOfDays == 0)
 		{
 			return 0;
 		}
 		else if (nrOfDays == 1) {
 			return (alcoholAmount*2);
 		}
 		else
			return alcoholCalculator((nrOfDays-1), alcoholAmount)*2;
 		
 
 	}
 	
 	public static void main(String[] args)
 	{
 		final Scanner scanner = new Scanner(System.in);
 		
 		System.out.print("Hvor mange dager skal du på hyttetur? Skriv inn som tall \n");
 		int nrOfDays = scanner.nextInt();
 		
 		System.out.print("Hvor mange alkoholenheter trenger du på en normal kveld? \n");
 		int alcoholAmount = scanner.nextInt();
 	
 		alcoholYouShouldBring = alcoholCalculator(nrOfDays, alcoholAmount);
 		
 		System.out.println("Takk for at du bruker Aleks' Alkoholformel for hyttetur");
 		System.out.println("Du skal på hyttetur i " + nrOfDays + " dager. For det trenger du " + alcoholYouShouldBring + " enheter alkohol totalt for hele oppholdet.");
 		System.out.println("Vi minner om at inntak av alkohol skjer på eget ansvar, og vi fraskriver oss alt ansvar");
 	}
 	
 	
 	
 
 }
