 /*******************************************************************************
  * Copyright (c) 2012 GamezGalaxy.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  ******************************************************************************/
 package com.gamezgalaxy.GGS.networking;
 
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import com.gamezgalaxy.GGS.networking.packets.minecraft.Connect;
 import com.gamezgalaxy.GGS.networking.packets.minecraft.DespawnPlayer;
 import com.gamezgalaxy.GGS.networking.packets.minecraft.FinishLevelSend;
 import com.gamezgalaxy.GGS.networking.packets.minecraft.GlobalPosUpdate;
 import com.gamezgalaxy.GGS.networking.packets.minecraft.Kick;
 import com.gamezgalaxy.GGS.networking.packets.minecraft.LevelSend;
 import com.gamezgalaxy.GGS.networking.packets.minecraft.LevelStartSend;
 import com.gamezgalaxy.GGS.networking.packets.minecraft.MOTD;
 import com.gamezgalaxy.GGS.networking.packets.minecraft.Message;
 import com.gamezgalaxy.GGS.networking.packets.minecraft.Ping;
 import com.gamezgalaxy.GGS.networking.packets.minecraft.PosUpdate;
 import com.gamezgalaxy.GGS.networking.packets.minecraft.SetBlock;
 import com.gamezgalaxy.GGS.networking.packets.minecraft.SpawnPlayer;
 import com.gamezgalaxy.GGS.networking.packets.minecraft.TP;
 import com.gamezgalaxy.GGS.networking.packets.minecraft.UpdateUser;
 import com.gamezgalaxy.GGS.networking.packets.minecraft.Welcome;
 import com.gamezgalaxy.GGS.server.Player;
 import com.gamezgalaxy.GGS.server.Server;
 
 public class PacketManager {
 	
 	protected Packet[] packets = new Packet[] {
 		new Connect(this),
 		new DespawnPlayer(this),
 		new FinishLevelSend(this),
 		new GlobalPosUpdate(this),
 		new Kick(this),
 		new LevelSend(this),
 		new LevelStartSend(this),
 		new Message(this),
 		new MOTD(this),
 		new Ping(this),
 		new PosUpdate(this),
 		new SetBlock(this),
 		new SpawnPlayer(this),
 		new TP(this),
 		new UpdateUser(this),
 		new Welcome(this)
 	};
 	
 	protected ServerSocket serverSocket;
 	
 	protected Thread reader;
 	
 	public Server server;
 	
 	public PacketManager(Server instance) {
 		this.server = instance;
 		try {
 			serverSocket = new ServerSocket(this.server.Port);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public Packet getPacket(byte opCode) {
 		for (Packet p : packets) {
 			if (p.ID == opCode)
 				return p;
 		}
 		return null;
 	}
 	
 	public Packet getPacket(String name) {
 		for (Packet p : packets) {
 			if (p.name.equalsIgnoreCase(name))
 				return p;
 		}
 		return null;
 	}
 	
 	public void StartReading() {
 		reader = new Read(this);
 		reader.start();
 		server.Log("Listening on port " + server.Port);
 	}
 	
 	public void StopReading() {
 		reader.stop();
 		try {
 			serverSocket.close();
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		try {
 			reader.join();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public static long ConvertToInt32(byte[] array) {
 		long toreturn = 0;
 		for (int i = 0; i < array.length; i++) {
 			toreturn += ((long) array[i] & 0xffL) << (8 * i);
 		}
 		return toreturn;
 	}
 	public static short INT_little_endian_TO_big_endian(short i)
 	{
 		return(short)(((i&0xff)<<24)+((i&0xff00)<<8)+((i&0xff0000)>>8)+((i>>24)&0xff));
 	}
 	/**
      * Encodes an integer into up to 4 bytes in network byte order in the 
      * supplied buffer starting at <code>start</code> offset and writing
      * <code>count</code> bytes.
      * 
      * @param num the int to convert to a byte array
      * @param buf the buffer to write the bytes to
      * @param start the offset from beginning for the write operation
      * @param count the number of reserved bytes for the write operation
      */
     public static void intToNetworkByteOrder(int num, byte[] buf, int start, int count) {
         if (count > 4) {
             throw new IllegalArgumentException(
                     "Cannot handle more than 4 bytes");
         }
 
         for (int i = count - 1; i >= 0; i--) {
             buf[start + i] = (byte) (num & 0xff);
             num >>>= 8;
         }
     }
     
     public void Accept(Socket connection) throws IOException {
     	DataInputStream reader = new DataInputStream(connection.getInputStream());
     	byte firstsend = reader.readByte();
     	switch (firstsend) {
     	case 0: //Minecraft player
     		new Player(connection, this, firstsend);
     	case (byte)'G': //A browser or website is using GET
     		//TODO Add support for browsers
     	}
     }
 	
 	public class Read extends Thread {
 		
 		PacketManager pm;
 		
 		public Read(PacketManager pm) { this.pm = pm; }
 		
 		@Override
 		public void run() {
 			Socket connection = null;
 			while (server.Running) {
 				try {
 					connection = serverSocket.accept();
 					server.Log("Connection made from " + connection.getInetAddress().toString());
 					Accept(connection);
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 }
