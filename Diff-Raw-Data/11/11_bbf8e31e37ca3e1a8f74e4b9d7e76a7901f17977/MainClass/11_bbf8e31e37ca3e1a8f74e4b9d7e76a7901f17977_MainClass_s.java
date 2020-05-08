 //to be able to read user input
 import java.util.Scanner;
 
 public class MainClass {
 	private Scanner scan;
 	
 	
 	public static void main(String[] args) {
 		//A new animal is created
 		Animal pinky = null;
 		//create a room
 		Room room = new Room(5, 5);
 		//initiate the scanner
 		Scanner scan = new Scanner(System.in);
 		
 		//describes the room
 		room.roomDescription();
 		//user gets promoted what to do one of four chooses 
 		System.out.println("Games started what do u want to do ?");
 		System.out.println("1 - open the door");
 		System.out.println("2 - eat some food");
 		System.out.println("3 - go to cage");
 		System.out.println("4 - go to wardrobe");
 		//create variable that holds to users choose
 		int choose = scan.nextInt();
 		
 		//If statement checks that user has chosen between 1 - 4 
 		if(choose > 0 && choose <= 4){
 			
 			do{	
 				
 				if(choose == 1){
 					System.out.println("you choose "+choose);
 				}else if(choose == 2){
 					System.out.println("you choose "+choose);
 				}else if(choose == 3){
 					System.out.println("you choose "+choose);
 				}else if(choose == 4){
 					System.out.println("you choose "+choose);
 				}
			}while(room.openDoor() != false);
 			
 		}else{
 			System.out.println("your chose is "+ choose +" you have to choose between 1-4");
 		}
 		
 		
 		
 		//close the scanner
 		scan.close();
 		
 
 	}//end main 
 
 }//end class
