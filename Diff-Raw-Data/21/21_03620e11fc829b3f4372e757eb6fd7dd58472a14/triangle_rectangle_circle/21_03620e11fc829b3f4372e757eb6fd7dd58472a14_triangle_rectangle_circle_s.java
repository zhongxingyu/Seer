 package areas;
 
 import java.util.Scanner;
 
 public class triangle_rectangle_circle {
 
 	public static void main(String[] args) {
 		
 		System.out.println("This program was written by Daniel"); // Daniel Pytlarz, 6th Hour, 10/4/13
 		System.out.println("-----------------------------");
 				
 		String varName;
         System.out.print("Enter your name: ");
 		Scanner stringScanner = new Scanner(System.in); //string scanner
         varName = stringScanner.nextLine(); 
                		       
         for (int loop = 0; loop <4; ++loop){
         
         String transChar;
         System.out.println();
 		System.out.print("Please select a shape: (R)ectangle, (T)riangle, or (C)ircle: ");
 		transChar = stringScanner.nextLine(); //transChar = transition to char
 		char caseSelector = transChar.charAt(0);
 											
 		Scanner numScanner = new Scanner(System.in); //number scanner
 				
 		switch (caseSelector){
 			
 			//Case: Rectangle
 		
 			case 'r':
 			case 'R':
 				
 				double rHeight;
 				double rWidth;
 				
 				System.out.print("Please enter height: ");
 				rHeight = numScanner.nextDouble();
 				System.out.println("Please enter width: ");
 				rWidth = numScanner.nextDouble();
 				
 				double rArea = (rHeight * rWidth);
 				
 				//rounding
 				rArea = Math.round(rArea * 100);
 				rArea = rArea / 100;
 												
 				System.out.println();
 				System.out.println("-----------------------------");
 				System.out.println("Area of the rectangle is " + rArea + " units");
 				System.out.println("-----------------------------");
 				break;
 							
 			//Case: Triangle
 				
 			case 't':
 			case 'T':
 				
 				double tSideA;
 				double tSideB;
 				double tSideC;
 								
 				System.out.println("Please enter side 'a' of the triangle: ");
 				tSideA = numScanner.nextDouble();
 				System.out.println("Please enter side 'b' of the triangle: ");
 				tSideB = numScanner.nextDouble();
 				System.out.println("Please enter side 'c' of the triangle: ");
 				tSideC = numScanner.nextDouble();
 				
 				//heron's formula
 				double s = (tSideA + tSideB + tSideC) / 2; 
 		        double x = ((s) * (s - tSideA) * (s - tSideB) * (s - tSideC)); 
 		        double tArea = Math.sqrt(x);
 				
 		        //rounding
 		        tArea = Math.round(tArea * 100); 
 				tArea = tArea / 100; 
 				
 				double compAB = tSideA + tSideB; //sum of sides a & b
 				double compAC = tSideA + tSideC; //sum of sides a & c
 				double compBC = tSideB + tSideC; //sum of sides b & c
 				
 				if(tSideC > compAB){ //if side c (tSideC) is greater than sum of sides a & b (compAB)
 					System.out.println();
 					System.out.println("Error: Third side cannot be greater than the sum of the other two sides");
 				} else if(tSideB > compAC){ //if side b (tSideB) is greater than sum of sides a & c (compAC)
 					System.out.println();
 					System.out.println("Error: Second side cannot be greater than the sum of the other two sides");
 				} else if(tSideA > compBC){ //if side a (tSideA) is greater than sum of sides b & c (compBC)
 					System.out.println();
 					System.out.println("Error: First side cannot be greater than the sum of the other two sides");
 				} else{ //valid option
 					System.out.println();
 					System.out.println("-----------------------------");
 					System.out.println("Area of the triangle is " + tArea + " units");
 					System.out.println("-----------------------------");
 				}
 								
 				break;
 							
 			//Case: Circle
 				
 			case 'c':
 			case 'C':
 				
 				double cRadius;
 							
 				System.out.println("Please enter radius: ");
 				cRadius = numScanner.nextDouble();
 				
 				double cArea = (Math.pow (cRadius,2) * Math.PI);
 				
 				//rounding
 				cArea = Math.round(cArea * 100);
 				cArea = cArea / 100;
 				
 				System.out.println();
 				System.out.println("-----------------------------");
 				System.out.println("Area of the circle is " + cArea + " units");
 				System.out.println("-----------------------------");
 				break;
 				
 			//Case: Error
 			
 			default:
 						
 				System.out.println("Error: Invalid option");
 				break;
 							
 			} //switch (caseSelector)
 		
 			numScanner.close();
         } //for
         	
         	System.out.println();
            	System.out.println("This program was run by: " + varName);
            	stringScanner.close();
 	} //main
 	
 
 } //public class triangle_rectangle_circle
	
	
	
	
