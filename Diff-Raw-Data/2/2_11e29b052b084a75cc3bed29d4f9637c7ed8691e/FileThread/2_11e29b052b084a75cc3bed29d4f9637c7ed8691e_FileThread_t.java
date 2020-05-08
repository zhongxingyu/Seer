 /* File worker thread handles the business of uploading, downloading, and removing files for clients with valid tokens */
 
 import java.lang.Thread;
 import java.net.Socket;
 import java.util.List;
 import java.util.ArrayList;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 //These threads are spun off by FileServer.java
 public class FileThread extends Thread
 {
 	private final Socket socket;
 
 	public FileThread(Socket _socket)
 	{
 		socket = _socket;
 	}
 
 	public void run()
 	{
 		boolean proceed = true;
 		try
 		{
 			//setup IO streams to bind with the sockets
 			System.out.println("*** New connection from " + socket.getInetAddress() + ":" + socket.getPort() + "***");
 			final ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
 			final ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
 			Envelope response;
 			
 			//handle messages from the input stream(ie. socket)
 			do
 			{
 				Envelope e = (Envelope)input.readObject();
 				System.out.println("Request received: " + e.getMessage());
 
 				// Handler to list files that this user is allowed to see
 //--LIST FILES---------------------------------------------------------------------------------------------------------
 				
 				if(e.getMessage().equals("LFILES"))
 				{
 					ArrayList<ShareFile> theFiles = FileServer.fileList.getFiles();
 					if(theFiles.size() > 0)
 					{
 						response = new Envelope("OK");//success (check FileClient line 140 to see why this is the message
 						response.addObject(theFiles);//See FileClient for protocol
 						
 						output.writeObject(response);
 					}
 					else	//no files exist
 					{
 						response = new Envelope("FAIL -- no files exist. Ask yourself why. ");
 						output.writeObject(response);
 					}
 					
 //--TODO: Test/Finish this handler-------------------------------------------------------------------------------------------
 				}
 //--UPLOAD FILE--------------------------------------------------------------------------------------------------------
 				
 				if(e.getMessage().equals("UPLOADF"))
 				{
 					if(e.getObjContents().size() < 3)
 					{
 						response = new Envelope("FAIL -- bad contents. Ask yourself why. ");
 					}
 					else
 					{
 						if(e.getObjContents().get(0) == null) 
 						{
 							response = new Envelope("FAIL -- bad path. Ask yourself why. ");
 						}
 						if(e.getObjContents().get(1) == null) 
 						{
 							response = new Envelope("FAIL -- bad group. Ask yourself why. ");
 						}
 						if(e.getObjContents().get(2) == null) 
 						{
 							response = new Envelope("FAIL -- bad token. Ask yourself why. ");
 						}
 						else {
 							//retrieve the contents of the envelope
 							String remotePath = (String)e.getObjContents().get(0);
 							String group = (String)e.getObjContents().get(1);
 							UserToken yourToken = (UserToken)e.getObjContents().get(2); //Extract token
 
 							if (FileServer.fileList.checkFile(remotePath)) 
 							{
 								System.out.printf("Error: file already exists at %s\n", remotePath);
 								response = new Envelope("FAIL -- file already exists. "); //Success
 							}
 							else if (!yourToken.getGroups().contains(group)) 
 							{
 								System.out.printf("Error: user missing valid token for group %s\n", group);
 								response = new Envelope("FAIL -- unauthorized user token for group. Ask yourself why. "); //Success
 							}
 							//create file and handle upload
 							else  
 							{
 								File file = new File("shared_files/" + remotePath.replace('/', '_'));
 								file.createNewFile();
 								FileOutputStream fos = new FileOutputStream(file);
 								System.out.printf("Successfully created file %s\n", remotePath.replace('/', '_'));
 
 								//request file contents
 								response = new Envelope("READY"); //Success
 								output.writeObject(response);
 
 								//recieve and write the file to the directory
 								e = (Envelope)input.readObject();
 								while (e.getMessage().compareTo("CHUNK") == 0) 
 								{
 									fos.write((byte[])e.getObjContents().get(0), 0, (Integer)e.getObjContents().get(1));
 									response = new Envelope("READY"); //Success
 									output.writeObject(response);
 									e = (Envelope)input.readObject();
 								}
 
 								//end of file identifier expected, inform the user of status
 								if(e.getMessage().compareTo("EOF") == 0) 
 								{
 									System.out.printf("Transfer successful file %s\n", remotePath);
 									FileServer.fileList.addFile(yourToken.getSubject(), group, remotePath);
 									response = new Envelope("OK"); //Success
 								}
 								else 
									{
 									System.out.printf("Error reading file %s from client\n", remotePath);
 									response = new Envelope("ERROR -- failed attempt at reading file from client. "); //Success
 								}
 								fos.close();
 							}
 						}
 					}
 
 					output.writeObject(response);
 				}
 //--DOWNLOAD FILE------------------------------------------------------------------------------------------------------
 				else if (e.getMessage().compareTo("DOWNLOADF") == 0) 
 				{
 					//retrieve the contents of the envelope, and attampt to access the requested file
 					String remotePath = (String)e.getObjContents().get(0);
 					UserToken t = (UserToken)e.getObjContents().get(1);
 					ShareFile sf = FileServer.fileList.getFile("/" + remotePath);
 
 					if (sf == null) 
 					{
 						System.out.printf("Error: File %s doesn't exist\n", remotePath);
 						e = new Envelope("ERROR -- file missing. Ask yourself why. ");
 						output.writeObject(e);
 					}
 					else if (!t.getGroups().contains(sf.getGroup()))
 					{
 						System.out.printf("Error user %s doesn't have permission\n", t.getSubject());
 						e = new Envelope("ERROR -- insufficient user permissions. Ask yourself why. ");
 						output.writeObject(e);
 					}
 					else 
 					{
 						try
 						{
 							//try to grab the file
 							File f = new File("shared_files/_"+remotePath.replace('/', '_'));
 							if (!f.exists()) 
 							{
 								System.out.printf("Error file %s missing from disk\n", "_"+remotePath.replace('/', '_'));
 								e = new Envelope("ERROR -- file not on disk. Ask yourself why. ");
 								output.writeObject(e);
 							}
 							else 
 							{
 								FileInputStream fis = new FileInputStream(f);
 
 								//send the file in 4096 byte chunks
 								do 
 								{
 									byte[] buf = new byte[4096];
 									if (e.getMessage().compareTo("DOWNLOADF") != 0) 
 									{
 										System.out.printf("Server error: %s\n", e.getMessage());
 										break;
 									}
 									e = new Envelope("CHUNK");
 									int n = fis.read(buf); //can throw an IOException
 									if (n > 0) 
 									{
 										System.out.printf(".");
 									} 
 									else if (n < 0) 
 									{
 										System.out.println("Read error. Ask yourself why. ");
 									}
 
 									//tack the chunk onto the envelope and write it
 									e.addObject(buf);
 									e.addObject(new Integer(n));
 									output.writeObject(e);
 
 									//get response
 									e = (Envelope)input.readObject();
 								}
 								while (fis.available() > 0);
 
 								//If server indicates success, return the member list
 								if(e.getMessage().compareTo("DOWNLOADF") == 0)
 								{
 									//send the end of file identifier
 									e = new Envelope("EOF -- end of file. Ask yourself why. ");
 									output.writeObject(e);
 
 									//accept response
 									e = (Envelope)input.readObject();
 									if(e.getMessage().compareTo("OK") == 0) 
 									{
 										System.out.printf("File data upload successful\n");
 									}
 									else 
 									{
 										System.out.printf("Upload failed: %s\n", e.getMessage());
 									}
 								}
 								else 
 								{
 
 									System.out.printf("Upload failed: %s\n", e.getMessage());
 								}
 							}
 						}
 						catch(Exception e1)
 						{
 							System.err.println("Error: " + e.getMessage());
 							e1.printStackTrace(System.err);
 
 						}
 					}
 				}
 //--DELETE FILE--------------------------------------------------------------------------------------------------------
 				else if (e.getMessage().compareTo("DELETEF")==0) 
 				{
 					//retrieve the contents of the envelope, and attampt to access the requested file
 					String remotePath = (String)e.getObjContents().get(0);
 					UserToken t = (UserToken)e.getObjContents().get(1);
 					ShareFile sf = FileServer.fileList.getFile("/"+remotePath);
 
 					if (sf == null) 
 					{	
 						System.out.printf("Error: File %s doesn't exist\n", remotePath);
 						e = new Envelope("ERROR -- file does not exists. ");
 					}
 					else if (!t.getGroups().contains(sf.getGroup()))
 					{
 						System.out.printf("Error user %s doesn't have permission\n", t.getSubject());
 						e = new Envelope("ERROR -- insufficient user permissions. Ask yourself why. ");
 					}
 					else 
 					{
 						//attempt to delete the file
 						try
 						{
 							File f = new File("shared_files/"+"_"+remotePath.replace('/', '_'));
 
 							if (!f.exists()) 
 							{
 								System.out.printf("Error file %s missing from disk\n", "_"+remotePath.replace('/', '_'));
 								e = new Envelope("ERROR -- insufficient user permissions. Ask yourself why. ");
 							}
 							else if (f.delete()) 
 							{
 								System.out.printf("File %s deleted from disk\n", "_"+remotePath.replace('/', '_'));
 								FileServer.fileList.removeFile("/"+remotePath);
 								e = new Envelope("OK");
 							}
 							else 
 							{
 								System.out.printf("Error deleting file %s from disk\n", "_"+remotePath.replace('/', '_'));
 								e = new Envelope("ERROR -- file unable to be deleted from disk. ");
 							}
 
 
 						}
 						catch(Exception e1)
 						{
 							System.err.println("Error: " + e1.getMessage());
 							e1.printStackTrace(System.err);
 							e = new Envelope(e1.getMessage());
 						}
 					}
 					output.writeObject(e);
 
 				}
 				else if(e.getMessage().equals("DISCONNECT"))
 				{
 					socket.close();
 					proceed = false;
 				}
 			} while(proceed);
 		}
 		catch(Exception e)
 		{
 			System.err.println("Error: " + e.getMessage());
 			e.printStackTrace(System.err);
 		}
 	}
 
 }
