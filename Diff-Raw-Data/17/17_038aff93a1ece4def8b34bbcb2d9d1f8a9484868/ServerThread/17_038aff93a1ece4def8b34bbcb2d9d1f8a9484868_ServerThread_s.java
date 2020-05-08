 /* superclass used to share functions between  file and group servers*/
 
 import java.lang.Thread;
 import java.net.Socket;
 import java.util.*;
 import java.io.*;
 import java.security.*;
 import javax.crypto.*;
 import javax.crypto.spec.IvParameterSpec;
 
 //These threads are spun off by FileServer.java
 public class ServerThread extends Thread
 {
 	protected final Socket socket;
 	protected CryptoEngine cEngine;
 	protected AESKeySet aesKey;
 	protected ObjectOutputStream output;
 	protected ObjectInputStream input;
 	protected Server myServer;
 	protected Key myPrivateKey;
 	protected Key myPublicKey;
	protected int msgNumber = -1;
 
 	public ServerThread(Server server, Socket _socket)
 	{
 		socket = _socket;
 		cEngine = new CryptoEngine();
 		aesKey = null;
 		myServer = server;
 		myPrivateKey = myServer.getAuthKeys().getPrivate();
 		myPublicKey = myServer.getAuthKeys().getPublic();
 		try
 		{
 			input = new ObjectInputStream(socket.getInputStream());
 			output = new ObjectOutputStream(socket.getOutputStream());
 		}
 		catch(Exception exc)
 		{
 			System.out.println(cEngine.formatAsError("failed to bind streams to the socket"));
 		}
 	}
 	
 //----------------------------------------------------------------------------------------------------------------------
 //-- CONNECTION SETUP FUNCIONS
 //----------------------------------------------------------------------------------------------------------------------
 	protected boolean setUpConnection()
 	{
 		Envelope message = null;
 		Envelope response = null;
 
 		try
 		{
 			message = (Envelope)cEngine.readPlainText(input);
 			System.out.println("\n<< Request Received: " + message.getMessage());
 
 //-- PUBLIC KEY DISTIBURTION------------------------------------------------------------------------------------------
 
 			//if they sent a public key request
 			if(message.getMessage().equals("GET_PUBKEY"))
 			{
 				//send your key
 				response = new Envelope("OK");
 				System.out.println(">> Sending Reponse: OK");
 				response.addObject(myPublicKey);
 				cEngine.writePlainText(response, output);
 				System.out.println(cEngine.formatAsSuccess("public key sent"));
 
 				//expect a new message
 				message = (Envelope)cEngine.readPlainText(input);
 				System.out.println("\n<< Request Received: " + message.getMessage());
 			}
 		
 //--RECIEVE AES KEY---------------------------------------------------------------------------------------------------
 
 			if(message.getMessage().equals("SET_AESKEY"))
 			{
 				//decrypt the key and challenge
 				aesKey = byteToAESKey((byte[])message.getObjContents().get(0),new IvParameterSpec((byte[])message.getObjContents().get(1)));
 				Integer challenge = (Integer)cEngine.deserialize(cEngine.RSADecrypt((byte[])message.getObjContents().get(2), myPrivateKey));
 				if(aesKey == null || challenge == null)
 				{
 					cEngine.writePlainText(genAndPrintErrorEnvelope("Could not decrypt message contents"), output);
 					return false;
 				}
 				else System.out.println(cEngine.formatAsSuccess("Challenge decrypted with private key"));
 				
 				System.out.println(cEngine.formatAsSuccess("AES keyset received and stored"));
 				//THE AES KEY IS NOW SET
 
 	//--CHALLENGE---------------------------------------------------------------------------------------------------------
 				challenge = new Integer((challenge.intValue()+1));
 
 				response = new Envelope("OK");
 				System.out.println(">> Sending Reponse: OK");
 				response.addObject(challenge);
 				cEngine.writeAESEncrypted(response, aesKey, output);
 				System.out.println(cEngine.formatAsSuccess("Challenge answered"));
 			}
 			else 
 			{
 				cEngine.writePlainText(genAndPrintErrorEnvelope("Unexpected request"), output);
 				return false;
 			}
 		}
 		catch(Exception exc)
 		{
 			cEngine.writePlainText(genAndPrintErrorEnvelope("Exception thrown during setup"), output);
 			return false;
 		}
 
 		return true;
 	}
 
 //--CONVERT BYTE ARRAY TO KEY---------------------------------------------------------------------------------------------------
 	protected AESKeySet byteToAESKey(byte [] aesKeyBytes, IvParameterSpec IV)
 	{
 		try
 		{
 			return new AESKeySet((Key)cEngine.deserialize(cEngine.RSADecrypt(aesKeyBytes, myPrivateKey)), IV);
 		}
 		catch(Exception exc)
 		{
 			System.out.println("\nERROR: FILECLIENT; AES Key from encrypted byte stream conversion failed");
 			return null;
 		}
 
 	}
 
 //----------------------------------------------------------------------------------------------------------------------
 //-- UTILITY FUNCITONS
 //----------------------------------------------------------------------------------------------------------------------
 
 	protected Envelope genAndPrintErrorEnvelope(String error)
 	{
 		if(cEngine == null)	cEngine = new CryptoEngine();
 
 		System.out.println(cEngine.formatAsError(error));
 		return new Envelope(error);
 	}
 
 	protected void rejectToken(Envelope response, ObjectOutputStream output)
 	{
 		cEngine.writeAESEncrypted(genAndPrintErrorEnvelope("Token signature rejected"), aesKey, output);
 		try
 		{
 			socket.close();
 		}
 		catch(Exception e)
 		{
 			System.out.println("WARNING: GroupThread; socket could not be closed");
 		}
 	}
 	
 }
