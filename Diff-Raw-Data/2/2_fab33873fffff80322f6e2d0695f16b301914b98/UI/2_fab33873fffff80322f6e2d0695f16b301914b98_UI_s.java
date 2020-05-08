 import java.net.Socket;
 import java.io.*;
 import java.util.*;
 
 public class UI
 {
 	public static void main(String[] args)
 	{
 		//GroupServer is named "ALPHA" and is on 8766
 		//FileServer is named "FilePile" and is on 4321
 
 		System.out.println("Attempting to connect to GroupServer.");
 		GroupClient gUser = new GroupClient();
		//gUser.connect(null, 8766);
 		FileClient fUser = new FileClient();
 		fUser.connect(null, 4321);
 		boolean connected = true;
 		//UI is connecting to localhost. May change with cmd line options later
 		
 		
 		System.out.println("Please enter a username.");
 		Scanner in = new Scanner(System.in);
 
 		String username = in.nextLine();
 
 		//for fileserver testing purposes:
 		//List<String> groups = new List<String>();
 		//UserToken token = null;
 
 		UserToken token = gUser.getToken(username);
 		
 		//confirmed that this token is correct
 		do{	
 			System.out.println("What would you like to do now?");//Queries the user
 			System.out.print("Type F for File Server operations or G for Group Server operations");
 			System.out.println("or D to disconnect.");
 			String input = in.nextLine();
 			
 			if(input.equals("F") || input.equals("f"))
 			{
 				System.out.print("Would you like to:\n1-List Files\n2-Upload File\n");
 				System.out.print("3-Download File\n4-Delete File\n");
 				System.out.print("Please enter your selection's");
 				System.out.print(" numeric value.\n");
 				int inputI = Integer.parseInt(in.nextLine());
 
 
 				String srcFile = "";
 				String destFile = "";
 				String group = "";
 
 				switch(inputI)
 				{
 					case 1:
 						System.out.println("\nAccessable files:");
 						for(String file : fUser.listFiles(token))
 						{
 							System.out.println(file);
 						}
 					break;
 
 					case 2:
 						System.out.println("\nEnter source file path");
 						srcFile = in.nextLine();					
 
 						System.out.println("\nEnter destination file path");
 						destFile = in.nextLine();		
 
 						System.out.println("\nEnter destination group name");
 						group = in.nextLine();		
 		
 						if(fUser.upload(srcFile, destFile, group, token))
 						{
 							System.out.println("\nSuccessful upload");
 						}
 						else 
 						{
 							System.out.println("\nUpload failed");
 						}
 					break;
 					
 					case 3:
 						System.out.println("\nEnter source file path");
 						srcFile = in.nextLine();					
 
 						System.out.println("\nEnter destination file path");
 						destFile = in.nextLine();			
 							
 		
 						if(fUser.download(srcFile, destFile, token))
 						{
 							System.out.println("\nSuccessful download");
 						}
 						else 
 						{
 							System.out.println("\nDownload failed");
 						}						
 					break;
 					
 					case 4:
 						System.out.println("\nEnter file path for deletion");
 						destFile = in.nextLine();					
 		
 						if(fUser.delete(destFile, token))
 						{
 							System.out.println("\nSuccessful deletion");
 						}
 						else 
 						{
 							System.out.println("\nDelete failed");
 						}							
 					break;
 					
 					default:
 						System.out.println("\ninvalid input\n");
 					break;
 				}
 				System.out.println();
 			}
 			else if(input.equals("G") || input.equals("g"))
 			{
 				System.out.print("Would you like to:\n1-Create a User\n2-Delete a User\n");
 				System.out.print("3-Create a Group\n4-Delete a Group\n5-List a Group's Members");
 				System.out.print("\n6-Add to a Group\n7-Delete from a Group\nPlease enter your selection's");
 				System.out.print(" numeric value.\n");
 				input = in.nextLine();
 				if(input.equals("1"))
 				{
 					System.out.println("What user would you like to create?");
 					input = in.nextLine();
 					gUser.createUser(input, token);
 				}
 				else if(input.equals("2"))
 				{
 					System.out.println("What user would you like to delete?");
 					input = in.nextLine();
 					gUser.deleteUser(input, token);
 				}
 				else if(input.equals("3"))
 				{
 					System.out.println("What group would you like to create?");
 					input = in.nextLine();
 					gUser.createGroup(input, token);
 				}
 				else if(input.equals("4"))
 				{
 					System.out.println("What group would you like to delete?");
 					input = in.nextLine();
 					gUser.deleteGroup(input, token);
 				}
 				else if(input.equals("5"))
 				{
 					System.out.println("What group would you like to know the members of?");
 					input = in.nextLine();
 					ArrayList<String> members = (ArrayList<String>)gUser.listMembers(input, token);
 					for(int i = 0; i<members.size(); i++)
 					{
 						System.out.println(members.get(i));
 					}
 				}
 				else if(input.equals("6"))
 				{
 					System.out.println("What user would you like to add to a group?");
 					input = in.nextLine();
 					System.out.println("To which group?");
 					String input2 = in.nextLine();
 					gUser.addUserToGroup(input, input2, token);
 				}
 				else if(input.equals("7"))
 				{
 					System.out.println("What user would you like to delete from a group?");
 					input = in.nextLine();
 					System.out.println("From which group?");
 					String input2 = in.nextLine();
 					gUser.deleteUserFromGroup(input, input2, token);
 				}
 			}
 			else if(input.equals("D") || input.equals("d"))
 			{
 				gUser.disconnect();
 				fUser.disconnect();
 				connected = false;
 			}
 			else System.out.println("Could not understand your input");
 		} while(connected);//forever
 	}
 }
