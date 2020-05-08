 /*
  *  UniCrypt Cryptographic Library
  *  Copyright (c) 2013 Berner Fachhochschule, Biel, Switzerland.
  *  All rights reserved.
  *
  *  Distributable under GPL license.
  *  See terms of license at gnu.org.
  *  
  */
 
 package ch.bfh.instacircle.service;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.ObjectInput;
 import java.io.ObjectInputStream;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.nio.ByteBuffer;
 import java.security.InvalidKeyException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 import javax.crypto.BadPaddingException;
 import javax.crypto.Cipher;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.NoSuchPaddingException;
 import javax.crypto.spec.SecretKeySpec;
 
import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;

 import ch.bfh.instacircle.Message;
 
 /**
  * This class implements a Thread which is waiting for incoming UDP messages and
  * dispatches them to the NetworkService to process them.
  * 
  * @author Juerg Ritter (rittj1@bfh.ch)
  */
 public class UDPBroadcastReceiverThread extends Thread {
 
 	private static final String TAG = UDPBroadcastReceiverThread.class
 			.getSimpleName();
 
 	public DatagramSocket socket;
 
 	NetworkService service;
 
 	private String cipherKey;
 
 	/**
 	 * @param service
 	 *            the service to which the message is being dispatched after
 	 *            receiving it
 	 * @param cipherKey
 	 *            the cipher key which will be used for decrypting the messages
 	 */
 	public UDPBroadcastReceiverThread(NetworkService service, String cipherKey) {
 		this.setName(TAG);
 		this.service = service;
 		this.cipherKey = cipherKey;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Thread#run()
 	 */
 	public void run() {
		
		WifiManager wifi;
		wifi = (WifiManager) service.getSystemService(Context.WIFI_SERVICE);
		MulticastLock ml = wifi.createMulticastLock("just some tag text");
		ml.acquire();
		
 		Message msg;
 		try {
 			socket = new DatagramSocket(12345);
 			socket.setBroadcast(true);
 			while (!Thread.currentThread().isInterrupted()) {
 				try {
 
 					DatagramPacket datagram = new DatagramPacket(
 							new byte[socket.getReceiveBufferSize()],
 							socket.getReceiveBufferSize());
 					socket.receive(datagram);
 					byte[] data = datagram.getData();
 
 					// Reading the first 4 bytes which represent a 32 Bit
 					// integer and indicates the length of the encrypted payload
 					byte[] length = new byte[4];
 					System.arraycopy(data, 0, length, 0, length.length);
 
 					// initializing the array for the payload with the length
 					// which has been extracted before
 					byte[] encryptedData = new byte[ByteBuffer.wrap(length)
 							.getInt()];
 
 					System.arraycopy(data, length.length, encryptedData, 0,
 							encryptedData.length);
 
 					// decrypt the payload
 					byte[] cleardata = decrypt(cipherKey.getBytes(),
 							encryptedData);
 
 					// let's try to deserialize the payload only if the
 					// decryption process has been successful
 					if (cleardata != null) {
 
 						// deserializing the payload into a Message object
 						ByteArrayInputStream bis = new ByteArrayInputStream(
 								cleardata);
 						ObjectInput oin = null;
 						try {
 							oin = new ObjectInputStream(bis);
 							msg = (Message) oin.readObject();
 
 						} finally {
 							bis.close();
 							oin.close();
 						}
 
 						// Dispatch it to the service after adding the sender IP
 						// address to the message
 						if (!Thread.currentThread().isInterrupted()) {
 							msg.setSenderIPAddress((datagram.getAddress())
 									.getHostAddress());
 							service.processBroadcastMessage(msg);
 						}
 					}
 
 				} catch (IOException e) {
 					socket.close();
 					Thread.currentThread().interrupt();
					ml.release();
 				}
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
		} finally {
			ml.release();
 		}
 	}
 
 	/**
 	 * @param rawSeed
 	 *            The symetric key as byte array
 	 * @param encrypted
 	 *            The data to be decrypted
 	 * @return A byte array of the decrypted data if decryption was successful,
 	 *         null otherwise
 	 */
 	private byte[] decrypt(byte[] rawSeed, byte[] encrypted) {
 		Cipher cipher;
 		MessageDigest digest;
 		byte[] decrypted = null;
 		try {
 			// we need a 256 bit key, let's use a SHA-256 hash of the rawSeed
 			// for that
 			digest = MessageDigest.getInstance("SHA-256");
 			digest.reset();
 			SecretKeySpec skeySpec = new SecretKeySpec(digest.digest(rawSeed),
 					"AES");
 			cipher = Cipher.getInstance("AES");
 			cipher.init(Cipher.DECRYPT_MODE, skeySpec);
 			decrypted = cipher.doFinal(encrypted);
 		} catch (NoSuchAlgorithmException e) {
 			return null;
 		} catch (NoSuchPaddingException e) {
 			return null;
 		} catch (InvalidKeyException e) {
 			return null;
 		} catch (IllegalBlockSizeException e) {
 			return null;
 		} catch (BadPaddingException e) {
 			return null;
 		}
 		return decrypted;
 	}
 }
