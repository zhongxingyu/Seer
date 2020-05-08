 /* File worker thread handles the business of uploading, downloading, and removing files for clients with valid tokens */
 
 import java.lang.Thread;
 import java.net.Socket;
 import java.util.*;
 import java.io.*;
 import java.security.*;
 import javax.crypto.*;
 import javax.crypto.spec.IvParameterSpec;
 
 //These threads are spun off by FileServer.java
 public class FileThread extends ServerThread
 {
 	private FileServer my_fs;
 
 	public FileThread(FileServer _fs, Socket _socket)
 	{
 		super((Server)_fs, _socket);
 		my_fs = _fs;
 	}
 
 	public void run()
 	{
		String serverFolder = my_fs.name+"_FileServer_Resources/";
 		String resourceFile = serverFolder+"FileResources.rsc";
 
 		try
 		{
 //--SET UP CONNECTION------------------------------------------------------------------------------------------------
 			System.out.println("\n*** New connection from " + socket.getInetAddress() + ":" + socket.getPort() + " ***");
 			if(setUpConnection() == false)
 			{
 				System.out.println("\n!!! Setup Failed: " + socket.getInetAddress() + ":" + socket.getPort() + " !!!");
 				return;
 			}
 			System.out.println("\n*** Setup Finished: " + socket.getInetAddress() + ":" + socket.getPort() + " ***");
 			
 //----------------------------------------------------------------------------------------------------------------------
 //-- REQUEST HANDLING LOOP
 //----------------------------------------------------------------------------------------------------------------------
 			//handle messages from the input stream(ie. socket)
 			do
 			{
 				System.out.println("\nWaiting for request...");
 				Envelope message = (Envelope)cEngine.readAESEncrypted(aesKey, input);
 				System.out.println("\n<< Request Received: " + message.getMessage());
 				UserToken reqToken = null;
 
 				Envelope response = new Envelope("OK"); // if no error occurs, send OK
 				boolean error = true; //assume an error will occur
 				String errorMsg = "Invalid request";
 				
 //--DISCONNECT----------------------------------------------------------------------------------------------------------
 				
 				//no data is required for disconnect, handle it first
 				if(message.getMessage().equals("DISCONNECT"))
 				{
 					socket.close(); //Close the socket
 					System.out.println(cEngine.formatAsSuccess("Disconnected"));
 					System.out.println("\n*** Disconnected: " + socket.getInetAddress() + ":" + socket.getPort() + " ***");
 					return;
 				}
 
 //--CHECK MESSAGE SIZE---------------------------------------------------------------------------------------------------
 				
 				//make sure the message has contents
 				else if(message.getObjContents().size() < 1)
 				{
 					cEngine.writeAESEncrypted(genAndPrintErrorEnvelope("Server received empty message"), aesKey, output);
 					continue;//go back and wait for a new message
 				}
 
 //--AUTHENTICATE TOKEN AND MSGNUMBER-------------------------------------------------------------------------------------------------
 								
 				//!!!! Everything this beyond point requires a valid token !!!!
 
 				reqToken = (UserToken)message.getObjContents().get(0);
 				if(reqToken != null && !reqToken.verifySignature(my_fs.signVerifyKey, cEngine))
 				{
 					rejectToken(response, output);
 					continue;//go back and wait for a new message
 				}
         		System.out.println(cEngine.formatAsSuccess("Token Authenticated"));
				System.out.println("msgnumber is " + msgNumber);
				System.out.println("should be " + reqToken.getMsgNumber());
				if(!msgNumberSet)
 				{
 					msgNumber = reqToken.getMsgNumber();
					msgNumberSet = true;
 				}
 				else if((++msgNumber != reqToken.getMsgNumber()) || !reqToken.verifyMsgNumberSignature(cEngine))
 				{
 					//Either the msgNumbers did not match, or the signature was invalid
 					//This could be the result of an attack
 					//We want to terminate the connection now
 					rejectToken(response, output);
 				}
 
 //--LIST FILES---------------------------------------------------------------------------------------------------------
 				
 				if(message.getMessage().equals("LFILES"))
 				{
 					errorMsg = "Could not list files; ";
 
 					ArrayList<ShareFile> theFiles = FileServer.fileList.getFiles();
 					if(theFiles.size() > 0)
 					{
 						response.addObject(theFiles);//See FileClient for protocol
 						System.out.println(cEngine.formatAsSuccess("File list added to response"));
 						error = false;
 					}
 					else errorMsg += "No files exist";
 					
 				}
 
 //--UPLOAD FILE--------------------------------------------------------------------------------------------------------
 				
 				if(message.getMessage().equals("UPLOADF"))
 				{
 					errorMsg = "Could not upload file; ";
 
 					if(message.getObjContents().size() > 2)//size check
 					{
 						//retrieve the contents of the message
 						String remotePath = (String)message.getObjContents().get(1);
 						String groupName = (String)message.getObjContents().get(2);
 
 						if(remotePath != null && groupName != null) //integrity check
 						{
 							if (!FileServer.fileList.checkFile(remotePath)) //check for the file
 							{
 								if (reqToken.getGroups().contains(groupName)) //check for priveledges
 								{
 									//create file and handle upload
 									System.out.println(serverFolder+"shared_files/" + remotePath.replace('/', '_'));
 									File file = new File(serverFolder+"shared_files/" + remotePath.replace('/', '_'));
 									file.createNewFile();
 									FileOutputStream fos = new FileOutputStream(file);
 									System.out.println(cEngine.formatAsSuccess("Successfully created file: "+remotePath.replace('/', '_')));
 									System.out.println(cEngine.formatAsSuccess("Requesting contents"));
 
 									//request file contents
 									message = new Envelope("READY"); //Success
 									cEngine.writeAESEncrypted(message, aesKey, output);
 
 									//receive and write the file to the directory
 									message = (Envelope)cEngine.readAESEncrypted(aesKey, input);
 									while (message.getMessage().compareTo("CHUNK") == 0) 
 									{
 										fos.write((byte[])message.getObjContents().get(0), 0, (Integer)message.getObjContents().get(1));
 										message = new Envelope("READY"); //Success
 										cEngine.writeAESEncrypted(message, aesKey, output);
 										message = (Envelope)cEngine.readAESEncrypted(aesKey, input);
 									}
 
 									//end of file identifier expected, inform the user of status
 									if(message.getMessage().compareTo("EOF") == 0) 
 									{
 										System.out.println(cEngine.formatAsSuccess("Transfer successful for file: "+ remotePath.replace('/', '_')));
 										FileServer.fileList.addFile(reqToken.getSubject(), groupName, remotePath);
 										error = false;
 									}
 									else errorMsg += "Failed during read";
 
 									fos.close();
 								}
 								else errorMsg += "No membership to specified group";
 							}
 							else errorMsg += "File already exists";
 						}
 						else errorMsg += "Check input before trying again";
 					}
 					else errorMsg += "Message too short";
 				}
 //--DOWNLOAD FILE------------------------------------------------------------------------------------------------------
 				else if (message.getMessage().compareTo("DOWNLOADF") == 0) 
 				{
 					errorMsg = "Could not download file; ";
 
 					if(message.getObjContents().size() > 1) //size check
 					{
 						//retrieve the contents of the message, and attampt to access the requested file
 						String remotePath = (String)message.getObjContents().get(1);
 						ShareFile shareFile = FileServer.fileList.getFile("/" + remotePath);
 
 						if (shareFile != null) //check for file
 						{
 							if (reqToken.getGroups().contains(shareFile.getGroup()))//check for priviledges
 							{
 								try
 								{
 									System.out.println(serverFolder+"shared_files/_" + remotePath.replace('/', '_'));
 									
 									//try to grab the file
 									File f = new File(serverFolder+"shared_files/_" + remotePath.replace('/', '_'));
 									if (f.exists()) 
 									{
 										FileInputStream fis = new FileInputStream(f);
 
 										//send the file in 4096 byte chunks
 										do 
 										{
 											byte[] buf = new byte[4096];
 
 											if(message.getMessage().compareTo("DOWNLOADF") == 0)
 											{
 												message = new Envelope("CHUNK");
 												int n = fis.read(buf); //can throw an IOException
 												if (n <=0) errorMsg += "Read error";
 
 												//tack the chunk onto the message and write it
 												message.addObject(buf);
 												message.addObject(new Integer(n));
 												cEngine.writeAESEncrypted(message, aesKey, output);
 
 												//get response
 												message = (Envelope)cEngine.readAESEncrypted(aesKey, input);
 											}
 											else errorMsg += "Unexpected chunk respons; ";
 										}
 										while (fis.available() > 0);
 
 										//If client indicates success, return the file
 										if(message.getMessage().compareTo("DOWNLOADF") == 0)
 										{
 											//send the end of file identifier
 											message = new Envelope("EOF");
 											cEngine.writeAESEncrypted(message, aesKey, output);
 
 											//accept response
 											message = (Envelope)cEngine.readAESEncrypted(aesKey, input);
 											if(message.getMessage().compareTo("OK") == 0) 
 											{
 												System.out.println(cEngine.formatAsSuccess("File transfer successful for file: "+ remotePath));
 												error = false;
 											}
 										}
 									}
 									else errorMsg += "file missing from disk:"+remotePath.replace('/', '_');
 								}
 								catch(Exception ex)
 								{
 									errorMsg += "Error during file sending";
 									/*System.err.println("Error: " + ex.getMessage());
 									ex.printStackTrace(System.err);*/
 								}
 							}
 							else errorMsg += "No membership to specified group";
 						}
 						else errorMsg += "No such file: "+remotePath.replace('/', '_');
 					}
 					else errorMsg += "Message too short";
 				}
 //--DELETE FILE--------------------------------------------------------------------------------------------------------
 				else if (message.getMessage().compareTo("DELETEF")==0) 
 				{
 					errorMsg = "Could not delete file; ";
 
 					if(message.getObjContents().size() > 1) //size check
 					{
 						//retrieve the contents of the message, and attampt to access the requested file
 						String remotePath = (String)message.getObjContents().get(1);
 						ShareFile shareFile = FileServer.fileList.getFile("/"+remotePath);
 
 						if (shareFile != null) //check for file
 						{
 							if (reqToken.getGroups().contains(shareFile.getGroup())) //check for priviledges
 							{
 								//attempt to delete the file
 								try
 								{
 									System.out.println(serverFolder+"shared_files/_" + remotePath.replace('/', '_'));
 									File f = new File(serverFolder+"shared_files/_" + remotePath.replace('/', '_'));
 
 									if (f.exists()) 
 									{
 										if (f.delete()) 
 										{
 											System.out.println(cEngine.formatAsSuccess("Successfully deleted file: "+remotePath.replace('/', '_')));
 											FileServer.fileList.removeFile("/"+remotePath);
 											error = false;
 										}
 										else errorMsg += "Failed to delete file: "+remotePath.replace('/', '_');
 									}
 									else errorMsg += "File not on disk: "+remotePath.replace('/', '_');
 
 
 								}
 								catch(Exception e1)
 								{
 									/*System.err.println("Error: " + e1.getMessage());
 									e1.printStackTrace(System.err);
 									message = new Envelope(e1.getMessage());*/
 									message =  genAndPrintErrorEnvelope("Exception thrown. file ("+remotePath.replace('/', '_')+") may not exist");
 								}
 							}
 							else errorMsg += "No membership to specified group";
 						}
 						else errorMsg += "No record of file: "+remotePath.replace('/', '_');
 					}
 					else errorMsg += "Message too short";
 				}
 
 //--SEND FINAL MESSAGE---------------------------------------------------------------------------------------------------
 				
 				if(error)
 				{
 					response = genAndPrintErrorEnvelope(errorMsg);
 					System.out.println("\n>> Sending error message");
 				}
 				else 
 				{
 					System.out.println("\n>> Sending Response: OK");
 				}
 
 				cEngine.writeAESEncrypted(response, aesKey, output);
 
 
 			} while(true);
 		}
 		catch(Exception ex)
 		{
 			System.err.println("Error: " + ex.getMessage());
 			ex.printStackTrace(System.err);
 		}
 	}
 }
