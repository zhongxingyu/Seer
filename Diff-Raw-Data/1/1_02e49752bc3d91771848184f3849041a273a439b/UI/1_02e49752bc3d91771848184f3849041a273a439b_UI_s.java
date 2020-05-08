 import java.net.Socket;
 import java.security.*;
 import javax.crypto.*;
 import java.io.*;
 import java.util.*;
 import javax.crypto.spec.IvParameterSpec;
 
 public class UI
 {
 	//group server tools
 	private static GroupClient gUser;
 	private static String gServer;
 	private static int gPort;
 
 	//file server tools
 	private static FileClient fUser;
 	private static String fServer;
 	private static int fPort;
 
 	//shared tools
 	private static String username;
 	private static UserToken token;
 	private static int msgNumber;
 	private static int msgNumberF;
 	private static int msgNumberG;
 
 	//utility tools
 	private static Scanner in;
 	
 	//security tools
 	protected static PrivateKey myPrivate;
 	protected static PublicKey myPublic;
 	protected static CryptoEngine cEngine;
 
 	public static void main(String[] args)
 	{
 
 		gUser = new GroupClient();
 		fUser = new FileClient();
 		in = new Scanner(System.in);
 
 		//GroupServer is named "ALPHA" and is on 5555
 		//FileServer is named "FilePile" and is on 4444		
 		System.out.println("\n\n***********************************************************\n"+
 								"****                    New Session                    ****\n"+
 								"***********************************************************\n");
 
 		if(connectionSetup() == false)
 		{
 			System.out.println("\nSomething went wrong during connect. exiting...");
 			return;
 		}
 		msgNumberF = msgNumber;
 		msgNumberG = msgNumber;
 
 		while(true)//loop until the user exits
 		{
 			System.out.println("\nWhat would you like to do now?");//Queries the user
 			System.out.print("Type F for File Server operations or G for Group Server operations");
 			System.out.println(" or D to disconnect.");
 			String input = in.nextLine();
 			
 			if(input.equals("F") || input.equals("f"))
 			{
 				token.setMsgNumber(++msgNumberF);
 				token.signMsgNumber(myPrivate, cEngine);
 				
 				System.out.print("Would you like to:\n1-List Files\n2-Upload File\n");
 				System.out.print("3-Download File\n4-Delete File\n");
 				System.out.print("Please enter your selection's");
 				System.out.print(" numeric value.\n");
 
 				int inputI = 0;
 
 				try
 				{
 					inputI = Integer.parseInt(in.nextLine());
 				}
 				catch(Exception e){continue;}
 
 				String srcFile = "";
 				String destFile = "";
 				String group = "";
 
 				switch(inputI)
 				{
 					case 1:
 						System.out.println("\nAccessable files:");
 						if(fUser.listFiles(token) != null)
 						{
 							for(ShareFile file : fUser.listFiles(token))
 							{
 								System.out.println(file.getPath());
 							}
 						}
 						else System.out.println("\nNo files found");
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
 				token.setMsgNumber(++msgNumberG);
 				token.signMsgNumber(myPrivate, cEngine);
 				
 				System.out.print("Would you like to:\n1-Create a User\n2-Delete a User\n");
 				System.out.print("3-Create a Group\n4-Delete a Group\n5-List a Group's Members");
 				System.out.print("\n6-Add to a Group\n7-Delete from a Group\n8-See all Users\nPlease enter your selection's");
 				System.out.print(" numeric value.\n");
 				input = in.nextLine();
 				boolean works;
 				if(input.equals("1"))
 				{
 					System.out.println("\nWhat user would you like to create?");
 					input = in.nextLine();
 					System.out.println("What password should they have?");
 					String pwd = in.nextLine();
 					works = gUser.createUser(input, pwd, token);
 					if(!works) System.out.println("Creation failed");
 					else System.out.println("Success.");
 				}
 				else if(input.equals("2"))
 				{
 					System.out.println("\nWhat user would you like to delete?");
 					input = in.nextLine();
 					works = gUser.deleteUser(input, token);
 					if(!works) System.out.println("Deletion failed");
 					else System.out.println("Success.");
 				}
 				else if(input.equals("3"))
 				{
 					System.out.println("\nWhat group would you like to create?");
 					input = in.nextLine();
 					works = gUser.createGroup(input, token);
 					if(!works) System.out.println("Creation failed");
 					else System.out.println("Success.");
 				}
 				else if(input.equals("4"))
 				{
 					System.out.println("\nWhat group would you like to delete?");
 					input = in.nextLine();
 					works = gUser.deleteGroup(input, token);
 					if(!works) System.out.println("Deletion failed");
 					else System.out.println("Success.");
 				}
 				else if(input.equals("5"))
 				{
 					System.out.println("\nWhat group would you like to know the members of?");
 					input = in.nextLine();
 					ArrayList<String> members = (ArrayList<String>)gUser.listMembers(input, token);
 					if(members != null){
 						for(int i = 0; i<members.size(); i++)
 						{
 							System.out.println(members.get(i));
 						}
 					}
 					else System.out.println("Group does not exist");
 				}
 				else if(input.equals("6"))
 				{
 					System.out.println("\nWhat user would you like to add to a group?");
 					input = in.nextLine();
 					System.out.println("To which group?");
 					String input2 = in.nextLine();
 					works = gUser.addUserToGroup(input, input2, token);
 					if(!works) System.out.println("Addition failed");
 					else System.out.println("Success.");
 				}
 				else if(input.equals("7"))
 				{
 					System.out.println("\nWhat user would you like to delete from a group?");
 					input = in.nextLine();
 					System.out.println("From which group?");
 					String input2 = in.nextLine();
 					works = gUser.deleteUserFromGroup(input, input2, token);
 					if(!works) System.out.println("Deletion failed");
 					else System.out.println("Success.");
 				}
 				else if(input.equals("8"))
 				{
 					ArrayList<String> allUsers = gUser.allUsers(token);
 					if(allUsers != null)
 					{
 						for(int i =0; i<allUsers.size(); i++)
 						{
 							System.out.println(allUsers.get(i));
 						}
 					}
 				}
 			}
 			else if(input.equals("D") || input.equals("d"))
 			{
 				gUser.disconnect();	
 				fUser.disconnect();
 				break;
 			}
 			else System.out.println("Could not understand your input");
 		}
 	}
 
 
 //---------------------------------------------------------------------------------------------------------------------
 //-- CONNECTION SETUP
 //---------------------------------------------------------------------------------------------------------------------
 
 	private static boolean connectionSetup()
 	{
 		boolean debug = false;
 
 
 		//skip server options and use defaults
 		System.out.println("\nuse debug defaults? [y,n]");
 		if(in.nextLine().equalsIgnoreCase("y"))
 		{
 			debug = true;
 		} 
 
 //--GROUP SERVER CONNECT-----------------------------------------------------------------------------------------------
 
 		System.out.println("\nPlease enter your username for setup/login");
 		username = in.nextLine();
 
 		//get input
 		if(debug == false)
 		{
 			System.out.println("\nWhat Group Server should we connect to?");
 			gServer = in.nextLine();
 
 			System.out.println("What port should we connect to the Group Server on?");
 			gPort = Integer.parseInt(in.nextLine());	
 		}
 		else
 		{
 			gServer = "ALPHA";
 			gPort = 6666;
 		}
 
 		//attempt to connect
 		try
 		{
 			if(gUser.connect(gServer, gPort, username)!=true) return false;
 		}
 		catch(Exception e)
 		{
 			System.out.println("\nfailed to connect to server");
 			return false;
 		}
 		
 		System.out.println("\n*** Generating Keys for Message Number signatures");
 		KeyPair rsaKeys = cEngine.genRSAKeyPair();
 		myPrivate = rsaKeys.getPrivate();
 		myPublic = rsaKeys.getPublic();
 		System.out.println("*** Keys Generated");
 
 
 //--lOGIN & TOKEN RETRIEVAL--------------------------------------------------------------------------------------------
 
 		boolean proceed = false;
 
 		System.out.println("\nAttempting login and token retrieval");
 		do
 		{
 			System.out.println("Please enter your password.");
 			String pwd = in.nextLine();
 
 			token = gUser.getToken(username, pwd, myPublic);
 			msgNumber = token.getMsgNumber();
 			if (token != null)
 			{
 				proceed = true;
 			}
 			else
 			{
 				System.out.println("Login failed");
 				System.out.println("\nRe-enter your username.");
 				username = in.nextLine();
 			}
 		}while(!proceed);//asks for username again
 		
 //--FILE SERVER CONNECT------------------------------------------------------------------------------------------------
 		
 		//get input
 		if(debug == false)
 		{
 			System.out.println("\nWhat File Server should we connect to?");
 			fServer = in.nextLine();
 			System.out.println("What port should we connect to the File Server on?");
 			fPort = Integer.parseInt(in.nextLine());
 		}
 		else
 		{
 			fServer = "FilePile";
 			fPort = 7777;
 		}
 
 		//attempt to connect
 		try
 		{
 			fUser.connect(fServer, fPort, username, token); 
 		}
 		catch(Exception e)
 		{
 			System.out.println("\nfailed to connect to server");
 			return false;
 		}
 
 		return true;
 	}
 }
