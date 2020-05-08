 // Task 17; Iliyan Petrov, F.N:121212123 ,Group 55 ,KST
 import java.util.Scanner;
 
 public class Main {
 	public static int[][] table = new int[101][101];
 	public static Scanner sc = new Scanner(System.in);
 	public static int ships,br=0,iCoord=0,check,x1,x2,y1,y2;
	
 	public static int[] swapIfNecessary(int a, int b){
 		int[] ab= {a,b};
 		if (a>b){
 			iCoord=1;
 			int temp;
 			System.out.println("Invalid coordinate. They will be inverted.");
			temp=a;
			a=b;
			b=temp;
			ab[0]=a;
			ab[1]=b;
 			}
 		 return ab;
 	}
 	
 	public static void buildShip(int x1, int x2, int y1, int y2){
 		outerfor:
 			  for(int f1 = x1; f1 <= x2 ; f1++){
 			   for(int z1=y1; z1 <= y2 ; z1++){
			    if( table[f1][z1] == 1 ) { 
			     System.out.println("There is a ship. Please enter new coordinates.");
 			     System.out.printf("Enter first x: ");
 			     x1 = sc.nextInt();
 			     System.out.printf("Enter second x: ");
 			     x2 = sc.nextInt();
 			     System.out.printf("Enter first y: ");
 			     y1 = sc.nextInt();
 			     System.out.printf("Enter second y: ");
 			     y2 = sc.nextInt();
 			     buildShip(x1, x2, y1, y2);
 			     break outerfor;
 			    }
 			   }
 			  }
 		for(int f1 = x1; f1 <= x2 ; f1++){
 			for(int z1=y1; z1 <= y2 ; z1++){
 			table[f1][z1]=1;
 			}
 		}
 	}
 	public static void main(String[] args) {
 		int arr[];
 		if(iCoord==0){
 		System.out.println("How many ships do you like to build ?");
 		ships = sc.nextInt();
 		}
 		while(br<ships){
 			System.out.printf("Enter first x: ");
 			x1 = sc.nextInt();
 			System.out.printf("Enter second x: ");
 			x2 = sc.nextInt();
 			System.out.printf("Enter first y: ");
 			y1 = sc.nextInt();
 			System.out.printf("Enter second y: ");
 			y2 = sc.nextInt();
 	
 			arr=swapIfNecessary(x1, x2);
 			x1=arr[0];
 			x2=arr[1];
 			arr=swapIfNecessary(y1, y2);
 			y1=arr[0];
 			y2=arr[1];			
 			buildShip(x1, x2, y1, y2);
 			br++;
 			System.out.println("Ship built successful.");
 		}
 	}
 };
