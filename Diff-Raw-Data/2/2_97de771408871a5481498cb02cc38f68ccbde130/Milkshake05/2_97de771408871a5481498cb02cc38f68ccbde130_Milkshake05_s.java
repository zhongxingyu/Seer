 //Chris Dildy
 //Milkshake app .04
 
 import java.util.Scanner;
 import java.util.Random;
 
public class Milkshake
 {
  public static Scanner kb;
  private static int MAXFLAVORS = 28;
  public static void main(String[] args)
  {
     String[] a = new String[] {"Banana", "Pineapple", "Cherry", "Peach", //4
  		 "Blueberry", "Strawberry", "BerryBerry", "Mocha", "Oreo",  //5
  		 "Mint", "Cobbler", "Chocolate", "Vanilla", "Fudge", "HiCPunch", //6
  		 "Snickers", "Reeses", "Walnut", "Chocolate Malt", "Cappuccino", //5
  		 "Caramel", "Peanut Butter", "Cheesecake", "Brownie",            //4
  		 "Heath Toffee", "Orange Push Up", "Egg Nog", "Watermelon"};  //4
  		 
  	Random generator = new Random();
  	kb = new Scanner(System.in);
  	int choice1 = generator.nextInt(MAXFLAVORS);
  	int choice2 = generator.nextInt(MAXFLAVORS);
  	int choice3 = generator.nextInt(MAXFLAVORS);
 
  	System.out.println("How many flavors would you like? (Maximum 3)");
  	int choices = kb.nextInt();
  	if (choices <=0 || choices > 3)
  	{
  	System.out.println("You have made an invalid selection,please try again");
  	}
  	
  	else if (choices == 3)
  	{
  	System.out.println("You are bold! 59,280 possibilities!");
  	System.out.println(a[choice1] + " " + a[choice2]+ " " + a[choice3]);
  	}
  	
  	else if (choices == 2)
  	{
  	System.out.println("You trying to play it safe with 1,560 possibilities!");
  	System.out.println(a[choice1] + " " + a[choice2]);
  	}else{
  	System.out.println("Only one flavor? Boring!");
  	System.out.println(a[choice1]);
  	}
  }
 }
  
 	
 
