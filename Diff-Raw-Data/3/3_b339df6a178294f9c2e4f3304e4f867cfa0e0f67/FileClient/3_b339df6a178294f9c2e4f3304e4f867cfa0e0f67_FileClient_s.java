 /* FileClient provides all the client functionality regarding the file server */
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.List;
 import java.security.*;
 import javax.crypto.*;
 import java.io.*;
 import javax.crypto.spec.IvParameterSpec;
 
 public class FileClient extends Client implements FileClientInterface 
 {
 	private CryptoEngine cEngine;
 	private Key serverPublicKey;
 	private KeyList keyList;
 	private Key aesSessionKey;
 	
 	public boolean connect(final String server, final int port, String username)
 	{
 		super.connect(server, port);
 		
 		cEngine = new CryptoEngine();
 		String userFile = "UserKeys" + username + ".bin";
 		ObjectInputStream keyStream;
 		
 		try
 		{
 			FileInputStream fis = new FileInputStream(userFile);
 			keyStream = new ObjectInputStream(fis);
 			keyList = (KeyList)keyStream.readObject();
 			if(keyList.checkServer(server))
 			{
 				//we have connected before
 				serverPublicKey = keyList.getKey(server);
				if(serverPublicKey.getEncoded() == setPublicKey().getEncoded())
 				{
 					System.out.println("FileServer verified.");
 				}
 				else
 				{
 					System.out.println("Public Keys Do Not Match. This is an unauthorized server.");
 					System.exit(-1);
 				}
 			}
 			else
 			{
 				System.out.println("This is a new file server. Requesting Public Key");
 				serverPublicKey = setPublicKey();
 				keyList.addKey(server, serverPublicKey);
 				ObjectOutputStream outStream = new ObjectOutputStream(new FileOutputStream("UserKeys" + username + ".bin"));
 				outStream.writeObject(keyList);
 				outStream.close();
 			}
 		}
 		catch(FileNotFoundException e)
 		{
 			System.out.println("UserKeys file does not exit. Creating new one.");
 			keyList = new KeyList();
 			System.out.println("This is a new file server. Requesting Public Key");
 			serverPublicKey = setPublicKey();
 			keyList.addKey(server, serverPublicKey);
 			try
 			{
 				ObjectOutputStream outStream = new ObjectOutputStream(new FileOutputStream("UserKeys" + username + ".bin"));
 				outStream.writeObject(keyList);
 				outStream.close();
 			}
 			catch(Exception ex)
 			{
 				System.out.println("ERROR: FILECLIENT: COULD NOT WRITE USERKEYS");
 				ex.printStackTrace();
 				System.exit(-1);
 			}
 		}
 		catch(Exception e)
 		{
 			System.out.println("ERROR: FILECLIENT: COULD NOT FINISH CONNECTION");
 			e.printStackTrace();
 			System.exit(-1);
 		}
 		
 		setAesKey();
 		System.out.println("SUCCESS");
 		return true;
 	}
 	
 	//This function also authenticats the fileserver
 	public void setAesKey()
 	{
 		try{
 			Envelope message, response;
 			AESKeySet aesKey = cEngine.genAESKeySet();
 			ByteArrayOutputStream toBytes = new ByteArrayOutputStream();//create ByteArrayOutputStream
 			ObjectOutputStream localOutput = new ObjectOutputStream(toBytes);//Make an object outputstream to that bytestream
 			
 			localOutput.writeObject(aesKey.getKey());//write to the bytearrayoutputstream
 		
 			byte[] aesKeyBytes = toBytes.toByteArray();
 		
 			byte[] aesKeyBytesA = new byte[100];
 			byte[] aesKeyBytesB = new byte[41];
 		
 			System.arraycopy(aesKeyBytes, 0, aesKeyBytesA, 0, aesKeyBytesA.length);
 			System.arraycopy(aesKeyBytes, 100, aesKeyBytesB, 0, aesKeyBytes.length-100);
 		
 			byte[] encryptedKeyA = cEngine.RSAEncrypt(aesKeyBytesA, serverPublicKey);
 			byte[] encryptedKeyB = cEngine.RSAEncrypt(aesKeyBytesB, serverPublicKey);
 	
 			byte[] encryptedKey = new byte[encryptedKeyA.length + encryptedKeyB.length];
 			System.arraycopy(encryptedKeyA, 0, encryptedKey, 0, encryptedKeyA.length);
 			System.arraycopy(encryptedKeyB, 0, encryptedKey, encryptedKeyA.length, encryptedKeyB.length);
 		
 			message = new Envelope("AESKEY");
 			message.addObject(encryptedKey);
 			message.addObject(aesKey.getIV().getIV());
 		
 			output.writeObject(message);
 		}
 		catch(Exception e)
 		{
 			System.out.println("ERROR:FILECLIENT: COULD NOT SEND AESKEY");
 			e.printStackTrace();
 			System.exit(-1);
 		}
 	}
 	
 	public Key setPublicKey()
 	{
 		Envelope message, response;
 		Key answer = null;
 		try
 		{
 			message = new Envelope("PUBKEYREQ");
 			output.writeObject(message);
 			response = (Envelope)input.readObject();
 			if(response.getMessage().equals("OK"))
 			{
 				answer = (Key)response.getObjContents().get(0);
 				return answer;
 			}
 		}
 		catch(Exception e)
 		{
 			System.out.println("ERROR: FILECLIENT: FAILED TO RECEIVE PUBLIC KEY");
 			e.printStackTrace();
 			System.exit(-1);
 		}
 		return answer;
 	}
 	
 	public boolean delete(String filename, UserToken token) 
 	{
 		String remotePath;
 		//remove the  leading '/' if necessary
 		if (filename.charAt(0)=='/') 
 		{
 			remotePath = filename.substring(1);
 		}
 		else 
 		{
 			remotePath = filename;
 		}
 
 		//create and setup a 'delete' envelope
 		Envelope env = new Envelope("DELETEF"); //Success
 	    env.addObject(remotePath);
 	    env.addObject(token);
 
 	    //send the envelope and output the result
 	    try 
 	    {
 			output.writeObject(env);
 		    env = (Envelope)input.readObject();
 		    
 			if (env.getMessage().compareTo("OK")==0) 
 			{
 				System.out.printf("File %s deleted successfully\n", filename);				
 			}
 			else 
 			{
 				System.out.printf("Error deleting file %s (%s)\n", filename, env.getMessage());
 				return false;
 			}			
 		} catch (IOException e1) 
 		{
 			e1.printStackTrace();
 		} catch (ClassNotFoundException e1) 
 		{
 			e1.printStackTrace();
 		}
 	    	
 		return true;
 	}
 
 	public boolean download(String sourceFile, String destFile, UserToken token) 
 	{
 		//remove the  leading '/' if necessary
 		if (sourceFile.charAt(0)=='/') 
 		{
 			sourceFile = sourceFile.substring(1);
 		}
 		
 		//download the file
 		File file = new File(destFile);
 		try 
 		{
 		    if (!file.exists()) 
 		    {
 			   	file.createNewFile();
 			    FileOutputStream fos = new FileOutputStream(file);
 			    
 			    //create and setup a download envelope
 			    Envelope env = new Envelope("DOWNLOADF"); //Success
 			    env.addObject(sourceFile);
 			    env.addObject(token);
 			    output.writeObject(env); 
 						
 				//retreive the incoming evelope
 			    env = (Envelope)input.readObject();
 						    
 				//read the body of the file one envelope at a time
 				while (env.getMessage().compareTo("CHUNK")==0) 
 				{ 
 					fos.write((byte[])env.getObjContents().get(0), 0, (Integer)env.getObjContents().get(1));
 					System.out.printf(".");
 					env = new Envelope("DOWNLOADF"); //Success
 					output.writeObject(env);
 					env = (Envelope)input.readObject();									
 				}										
 				fos.close();
 						
 				//when the end of file is detected, close and display the appropriate message
 				if(env.getMessage().compareTo("EOF")==0) 
 				{
 				    fos.close();
 					System.out.printf("\nTransfer successful file %s\n", sourceFile);
 					env = new Envelope("OK"); //Success
 					output.writeObject(env);
 				}
 				else 
 				{
 					System.out.printf("Error reading file %s (%s)\n", sourceFile, env.getMessage());
 					file.delete();
 					return false;								
 				}
 			}    		 
 			else 
 			{
 			    System.out.printf("Error couldn't create file %s\n", destFile);
 				return false;
 			}	
 		} 
 		catch (IOException e1) 
 		{
 		   	System.out.printf("Error couldn't create file %s\n", destFile);
 		  	return false;
 		}
 		catch (ClassNotFoundException e1) 
 		{
 			e1.printStackTrace();
 		}
 		return true;
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<ShareFile> listFiles(UserToken token) 
 	{
 		 try
 		 {
 			 Envelope message = null, e = null;
 			 //Tell the server to return the member list
 			 message = new Envelope("LFILES");
 			 message.addObject(token); //Add requester's token
 			 output.writeObject(message); 
 			 
 			 e = (Envelope)input.readObject();
 			 
 			 //If server indicates success, return the member list
 			 if(e.getMessage().equals("OK"))
 			 { 
 
 				return (List<ShareFile>)e.getObjContents().get(0); //This cast creates compiler warnings. Sorry.
 			 }
 				
 			 return null;
 			 
 		 }
 		 catch(Exception e)
 			{
 				System.err.println("Error: " + e.getMessage());
 				e.printStackTrace(System.err);
 				return null;
 			}
 	}
 
 	public boolean upload(String sourceFile, String destFile, String group,
 			UserToken token) 
 	{
 		//add a leading '/' if necessary
 		if (destFile.charAt(0)!='/') 
 		{
 			 destFile = "/" + destFile;
 		}
 		
 		try
 		{
 			Envelope message = null, env = null;
 			//Tell the server to return the member list
 			message = new Envelope("UPLOADF");
 			message.addObject(destFile);
 			message.addObject(group);
 			message.addObject(token); //Add requester's token
 			output.writeObject(message);
 			
 			 
 			FileInputStream fis = new FileInputStream(sourceFile);
 			 
 			env = (Envelope)input.readObject();
 			 
 			//If server indicates success, return the member list
 			if(env.getMessage().equals("READY"))
 			{ 
 				System.out.printf("Meta data upload successful\n");
 				
 			}
 			 else 
 			{
 				
 				System.out.printf("Upload failed: %s\n", env.getMessage());
 				return false;
 			}
 			 
 		 	//unless an error occurs, write the file in 4096 byte chunks
 			do 
 			{
 				byte[] buf = new byte[4096];
 				if (env.getMessage().compareTo("READY")!=0) 
 				{
 					System.out.printf("Server error: %s\n", env.getMessage());
 					return false;
 				}
 				message = new Envelope("CHUNK");
 				int n = fis.read(buf); //can throw an IOException
 				if (n > 0) 
 				{
 					System.out.printf(".");
 				} 
 				else if (n < 0) 
 				{
 					System.out.println("Read error");
 					return false;
 				}
 					
 				message.addObject(buf);
 				message.addObject(new Integer(n));
 					
 				output.writeObject(message);
 						
 				env = (Envelope)input.readObject();
 					
 										
 			 }
 			 while (fis.available()>0);		 
 					 
 			 //If server indicates success, return the member list
 			 if(env.getMessage().compareTo("READY")==0)
 			 { 
 				
 				//tell the sever we're done
 				message = new Envelope("EOF");
 				output.writeObject(message);
 				
 				env = (Envelope)input.readObject();
 				if(env.getMessage().compareTo("OK")==0) 
 				{
 					System.out.printf("\nFile data upload successful\n");
 				}
 				else 
 				{
 					System.out.printf("\nUpload failed: %s\n", env.getMessage());
 					return false;
 				}
 				
 			}
 			else {
 				
 				System.out.printf("Upload failed: %s\n", env.getMessage());
 				return false;
 			}	 
 		}
 		catch(Exception e1)
 		{
 			System.err.println("Error: " + e1.getMessage());
 			e1.printStackTrace(System.err);
 			return false;
 		}
 		return true;
 	}
 
 }
 
