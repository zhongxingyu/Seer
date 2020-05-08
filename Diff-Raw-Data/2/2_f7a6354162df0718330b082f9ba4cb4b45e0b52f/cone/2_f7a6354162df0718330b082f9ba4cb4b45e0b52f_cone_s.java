 import java.text.DecimalFormat;
 import java.util.*;
 public class cone {
 
 	public static void main(String[] args) {
 		Scanner s = new Scanner(System.in);
 		//height of cone value of pi and 1/3
 		final double PI = 3.14;
 		final double divide = .333333333333333333;
 		System.out.println("\nCone head volume");
 		//get height of cone
 		System.out.print("Enter the height: ");
		double height = s.nextDouble();
 		//check if height is in range
 		while(HEIGHT < 0) {
 			System.out.print("\nCan not have negative radius ");
 			System.out.print("\n\nEnter a positive number: ");
 			HEIGHT = s.nextDouble();
 		}
 		// get radius of cone
 		System.out.print("\nEnter the radius:  ");
 		double radius = s.nextDouble();
 		// check if radius is in range
 		while(radius < 0) {
 			System.out.print("\nCan not have negative radius ");
 			System.out.print("\n\nEnter positive number:  ");
 			radius = s.nextDouble();
 			
 			
 		}
         // find volume of cone
 		double volume = ((divide * PI) * Math.pow(radius, 2)) * HEIGHT;
        //decimal format with 3 spaces 
 		DecimalFormat fmt = new DecimalFormat("###########.###");
 		
         System.out.println("\nThe volume of your cone is "+fmt.format(volume));
 	}
 
 }
