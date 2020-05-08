 package sdfs;
 
 import java.util.Scanner;
 
 public class Main {
 
 	public static void main(String[] args) {
 		Scanner reader = new Scanner(System.in);
 		System.out.println("Enter input");
 		//get user input for a
 		String input = reader.next();
 
 		//		String mode = args[0];
 		if(input.equals("server")){
 			server obj_server = new server();
 			obj_server.listen();
 		}
 		else if(input.equals("client")){
 			client obj_client = new client();

 			obj_client.Start_FS_session();			//starting the session with the server
 			
 			while(1!=2){
 //				Scanner reader2 = new Scanner(System.in);	//asking user to enter the mode. put/get
 				System.out.println("Enter mode");
 				//get user input for a
 				String input2 = reader.next();
 				String UID = null;
 				if(input2.equals("put")){
 					System.out.println("Enter FileId");
 					UID = reader.next();
 					//		Scanner putDelegateInfo = new Scanner(System.in);
 					System.out.println("Do you want to send the delegation? (y/n): ");
 					String answer = reader.next();
 					if(answer.equals("y") || answer.equals("Y") || answer.equals("Yes")){
 						obj_client.send_delegation_token();
 					}
 					obj_client.PutFile(UID);
 				}
 				else if(input2.equals("get")){
 					System.out.println("Enter FileId");
 					UID = reader.next();
 					//		Scanner writeDelegateInfo = new Scanner(System.in);
 					System.out.println("Do you want to send the delegation? (y/n): ");
 					String answer = reader.next();
 					if(answer.equals("y") || answer.equals("Y") || answer.equals("Yes")){
 						obj_client.send_delegation_token();
 					}
 					obj_client.GetFile(UID);
 				}
 				else if(input2.equals("del")){
 					//		Scanner readDelegateInfo = new Scanner(System.in);	//asking user to enter the mode. put/get
 					System.out.println("Please enter file the following.");
 					System.out.println("File name");
 					String FileName = reader.next();
 					System.out.println("Recepient");
 					String Recepient = reader.next();
 					System.out.println("Rights");
 					String Rights = reader.next();
 					System.out.println("Duration");
 					String Duration = reader.next();
 					System.out.println("Is it propagatable?");
 					String Propagateable = reader.next();
 
 					DelegationToken token = new DelegationToken(FileName, Recepient, Rights, Integer.parseInt(Duration), Boolean.parseBoolean(Propagateable));
 					obj_client.create_delegation_files(token);
 				}
 				else if(input2.equals("exit")){
 					obj_client.Exit();
 					return;
 				}
 				else
 					System.out.println("Invalid input. Please try again");
 			}
 		}
 		//			
 		else
 			System.out.println("Invalid input. PLease try again");
 	}
 }
