 package methodVersions;
 
 import java.util.Scanner;
 
 public class Method_Void {
 
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
 	        int fNum, sNum;
 	        System.out.print("Please enter your first number: ");
 	        fNum = readStuff.nextInt();
 	        System.out.print("Please enter your second number: ");
 	        sNum = readStuff.nextInt();
 	       
 	        System.out.println("");
 	        	       
 	        add(varName, fNum, sNum); //firstNum + secondNum;
 	        
 	        sub(varName, fNum, sNum); //firstNum - secondNum;
 	        	        
 	        mult(varName, fNum, sNum); //firstNum * secondNum;
 	        
 	        qotremain(varName, fNum, sNum); //firstNum / secondNum & firstNum % secondNum;
 	  	      
 	          
 	        //Doubles
 	        
 	        System.out.println("");
         	System.out.println("Trial " + (loop+1) + " Doubles: ");
 	        Double fNumD, sNumD;
 	        System.out.print("Please enter your first number: ");
 	        fNumD = readStuff.nextDouble();
 	        System.out.print("Please enter your second number: ");
 	        sNumD = readStuff.nextDouble();
 	       
 	        System.out.println("");
  	               
 	        addD(varName, fNumD, sNumD); //firstNum + secondNum;
 	        
 	        subD(varName, fNumD, sNumD); //firstNum - secondNum;
 	        
 	        multD(varName, fNumD, sNumD); //firstNum * secondNum;
 	        
 	        qotremainD(varName, fNumD, sNumD); //firstNum / secondNum & firstNum % secondNum;
 	        
 	           
         }
         
         System.out.println("");
         System.out.println("This program was run by: " + varName);
         
         readStuff.close();
 	        
     }
 	        
 			//Integer Methods
 	
 	        public static void add(String z, int x, int y){
 	        	int sum;
 	        	sum = x + y;
 	        	System.out.println(z + ", the sum of " + x + " and " + y + " is " + sum);
 	        	
 	        }
 	        
 	        public static void sub(String z, int x, int y){
 	        	int diff;
 	        	diff = x - y;
 	        	System.out.println(z + ", the difference of " + x + " and " + y + " is " + diff);
 	        	
 	        }
 	        
 	        public static void mult(String z, int x, int y){
 	        	int prod;
 	        	prod= x * y;
 	        	System.out.println(z + ", the product of " + x + " and " + y + " is " + prod);
 	        	
 	        }
 	        
 	        public static void qotremain(String z, int x, int y){
 	        	int qot;
 	        	int remain;
 	        	
 	        	if(y == 0){ 
 		        	System.out.println(z + ", you cannot divide by 0. Remainder and quotent not available. ");
 		        } else{
 		        	qot = x / y;
 		        	remain = x % y;
 		        	System.out.println(z + ", the quotient of " + x + " and " + y + " is " + qot);
 		        	System.out.println(z + ", the remainder of " + x + " and " + y + " is " + remain);
 		  	          }
 	        	
 	        }
 	       	        
 	       //Double Methods
 	    	
 	        public static void addD(String z, double x, double y){
 	        	double total;
 	        	total = x + y;
 	        	System.out.println(z + ", the sum of " + x + " and " + y + " is " + total);
 	        	
 	        }
 	        
 	        public static void subD(String z, double x, double y){
 	        	double diff;
 	        	diff = x - y;
 	        	System.out.println(z + ", the difference of " + x + " and " + y + " is " + diff);
 	        	
 	        }
 	        
 	        public static void multD(String z, double x, double y){
 	        	double prod;
 	        	prod = x * y;
 	        	System.out.println(z + ", the product of " + x + " and " + y + " is " + prod);
 	        	
 	        }
 	        
 	        public static void qotremainD(String z, double x, double y){
 	        	double qot;
 	        	double remain;
 	        	
 	        	if(y == 0){ 
 		        	System.out.println(z + ", you cannot divide by 0. Remainder and quotent not available. ");
 		        } else{
 		        	qot = x / y;
 		        	remain = x % y;
 		        	System.out.println(z + ", the quotient of " + x + " and " + y + " is " + qot);
 		        	System.out.println(z + ", the remainder of " + x + " and " + y + " is " + remain);
 		  	          }
 	        	
 	        }
 	
 	       
 	              	
 	       
 	}   
	        
