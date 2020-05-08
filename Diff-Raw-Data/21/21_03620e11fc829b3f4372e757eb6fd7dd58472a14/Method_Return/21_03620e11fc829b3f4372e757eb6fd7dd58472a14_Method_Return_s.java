 package methodVersions;
 
 import java.util.Scanner;
 
 public class Method_Return {
 
 	public static void main(String[] args) {
 		 
 		System.out.println("This program was written by Daniel Pytlarz");
 		System.out.println("-----------------------------");
 		String varName;
         System.out.print("Enter your name: ");
         Scanner readStuff = new Scanner(System.in);
         varName = readStuff.nextLine();
                
         for (int loop = 0; loop <5; ++loop){
         
         		//Integers
         	
         	System.out.println("");
         	System.out.println("Trial " + (loop+1) + " Integers: ");
 	        int fNum, sNum, sum, differ, prod, qot, remain;
 	        System.out.print("Please enter your first number: ");
 	        fNum = readStuff.nextInt();
 	        System.out.print("Please enter your second number: ");
 	        sNum = readStuff.nextInt();
 	       
 	        System.out.println("");
 	        	       
 	        sum = add(fNum, sNum); //firstNum + secondNum;
 	        System.out.println(varName + ", the sum of " + fNum + " and " + sNum + " is " + sum);
 	        
 	        differ = sub(fNum, sNum); //firstNum - secondNum;
 	        System.out.println(varName + ", the difference of " + fNum + " and " + sNum + " is " + differ);
 	        
 	        prod = mult(fNum, sNum); //firstNum * secondNum;
 	        System.out.println( varName + ", the product of " + fNum + " and " + sNum + " is " + prod);
 	        
 	        if(sNum == 0){ 
 	        	System.out.println(varName + ", you cannot divide by 0. Remainder and quotent not available. ");
 	        } else{
 	        	qot = div1(fNum, sNum); //firstNum / secondNum;
 	  	        System.out.println(varName + ", the quotient of " + fNum + " and " + sNum + " is " + qot);
 	  	        
 	  	        remain = div2(fNum, sNum); //firstNum % secondNum;
 	  	        System.out.println(varName + ", the remainder of " + fNum + " and " + sNum + " is " + remain);
 	        }
 	      
 	        //Doubles
 	        
 	        System.out.println("");
         	System.out.println("Trial " + (loop+1) + " Doubles: ");
 	        Double fNumD, sNumD, sumD, differD, prodD, qotD, remainD;
 	        System.out.print("Please enter your first number: ");
 	        fNumD = readStuff.nextDouble();
 	        System.out.print("Please enter your second number: ");
 	        sNumD = readStuff.nextDouble();
 	       
 	        System.out.println("");
  	               
 	        sumD = addD(fNumD, sNumD); //firstNum + secondNum;
 	        System.out.println(varName + ", the sum of " + fNumD + " and " + sNumD + " is " + sumD);
 	        
 	        differD = subD(fNumD, sNumD); //firstNum - secondNum;
 	        System.out.println(varName + ", the difference of " + fNumD + " and " + sNumD + " is " + differD);
 	        
 	        prodD = multD(fNumD, sNumD); //firstNum * secondNum;
 	        System.out.println( varName + ", the product of " + fNumD + " and " + sNumD + " is " + prodD);
 	        
 	        if(sNumD == 0){ 
 	        	System.out.println(varName + ", you cannot divide by 0. Remainder and quotent not available. ");
 	        } else{
 	        	qotD = div1D(fNumD, sNumD); //firstNum / secondNum;
 	  	        System.out.println(varName + ", the quotient of " + fNumD + " and " + sNumD + " is " + qotD);
 	  	        
 	  	        remainD = div2D(fNumD, sNumD); //firstNum % secondNum;
 	  	        System.out.println(varName + ", the remainder of " + fNumD + " and " + sNumD + " is " + remainD);
 	        }
 	       
         }
         
         System.out.println("");
         System.out.println("This program was run by: " + varName);
         
         readStuff.close();
 	        
     }
 	        
 			//Integer Methods
 	
 	        public static int add(int x, int y){
 	        	int total;
 	        	total = x + y;
 	        	return total;
 	        }
 	        
 	        public static int sub(int x, int y){
 	        	int total;
 	        	total = x - y;
 	        	return total;
 	        }
 	        
 	        public static int mult(int x, int y){
 	        	int total;
 	        	total = x * y;
 	        	return total;
 	        }
 	        
 	        public static int div1(int x, int y){
 	        	int total;
 	        	total = x / y;
 	        	return total;
 	        }
 	        
 	        public static int div2(int x, int y){
 	        	int total;
 	        	total = x % y;
 	        	return total;
 	        }
 	        
 	      //Double Methods
 	    	
 	        public static double addD(double x, double y){
 	        	double total;
 	        	total = x + y;
 	        	return total;
 	        }
 	        
 	        public static double subD(double x, double y){
 	        	double total;
 	        	total = x - y;
 	        	return total;
 	        }
 	        
 	        public static double multD(double x, double y){
 	        	double total;
 	        	total = x * y;
 	        	return total;
 	        }
 	        
 	        public static double div1D(double x, double y){
 	        	double total;
 	        	total = x / y;
 	        	return total;
 	        }
 	        
 	        public static double div2D(double x, double y){
 	        	double total;
 	        	total = x % y;
 	        	return total;
 	        }
 	
 	        
 	}   
	        

      
