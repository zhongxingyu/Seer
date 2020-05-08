 /*******************************************************************************
 * Copyright (c) 2012 MCForge.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
 package net.mcforge.networking.packets;
 
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import net.mcforge.iomodel.Browser;
 import net.mcforge.iomodel.Player;
 import net.mcforge.networking.IOClient;
 import net.mcforge.networking.packets.browser.GET;
 import net.mcforge.networking.packets.minecraft.Connect;
 import net.mcforge.networking.packets.minecraft.DespawnPlayer;
 import net.mcforge.networking.packets.minecraft.FinishLevelSend;
 import net.mcforge.networking.packets.minecraft.GlobalPosUpdate;
 import net.mcforge.networking.packets.minecraft.Kick;
 import net.mcforge.networking.packets.minecraft.LevelSend;
 import net.mcforge.networking.packets.minecraft.LevelStartSend;
 import net.mcforge.networking.packets.minecraft.MOTD;
 import net.mcforge.networking.packets.minecraft.Message;
 import net.mcforge.networking.packets.minecraft.Ping;
 import net.mcforge.networking.packets.minecraft.PosUpdate;
 import net.mcforge.networking.packets.minecraft.SetBlock;
 import net.mcforge.networking.packets.minecraft.SpawnPlayer;
 import net.mcforge.networking.packets.minecraft.TP;
 import net.mcforge.networking.packets.minecraft.UpdateUser;
 import net.mcforge.networking.packets.minecraft.Welcome;
 import net.mcforge.networking.packets.minecraft.extend.ClickDistancePacket;
 import net.mcforge.networking.packets.minecraft.extend.ExtEntryPacket;
 import net.mcforge.networking.packets.minecraft.extend.ExtInfo;
 import net.mcforge.server.Server;
 
 public class PacketManager {
 	
 	protected Packet[] packets;
 	
 	protected ServerSocket serverSocket;
 	
 	protected Thread reader;
 	
 	/**
 	 * The server this PacketManager belongs to
 	 */
 	public Server server;
 	
 	/**
 	 * The constructor for the PacketManager
 	 * @param instance
 	 *                The server that this PacketManager will belong to
 	 */
 	public PacketManager(Server instance) {
 		this.server = instance;
 		initPackets();
 		try {
 			serverSocket = new ServerSocket(this.server.Port);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	protected void initPackets() {
 		packets = new Packet[] {
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
 				new Welcome(this),
 				new GET(this),
 				new ClickDistancePacket(this),
 				new ExtEntryPacket(this),
 				new ExtInfo(this)
 			};
 	}
 	
 	/**
 	 * Get a packet this PacketManager handles.
 	 * @param opCode
 	 *              The OpCode for the packet
 	 * @return
 	 *        The packet found, if no packet is found, then it will
 	 *        return null.
 	 */
 	public Packet getPacket(byte opCode) {
 		for (Packet p : packets) {
 			if (p.ID == opCode)
 				return p;
 		}
 		return null;
 	}
 	
 	/**
 	 * Get a packet this PacketManager handles.
 	 * @param name
 	 *            The name of the packet
 	 * @return
 	 *        The packet found, if no packet is found, then it will
 	 *        return null.
 	 */
 	public Packet getPacket(String name) {
 		for (Packet p : packets) {
 			if (p.name.equalsIgnoreCase(name))
 				return p;
 		}
 		return null;
 	}
 	
 	/**
 	 * Have the PacketManager start listening for clients
 	 * on the port provided by the {@link PacketManager#server}
 	 */
 	public void StartReading() {
 		reader = new Read();
 		reader.start();
 		server.Log("Listening on port " + server.Port);
 	}
 	
 	/**
 	 * Stop listening for clients.
 	 */
 	public void StopReading() {
		//reader.stop();
 		try {
 			serverSocket.close();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		try {
 			reader.join();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void Accept(Socket connection) throws IOException {
 	DataInputStream reader = new DataInputStream(connection.getInputStream());
 	byte firstsend = reader.readByte();
 	switch (firstsend) {
 	case 0: //Minecraft player
 	new Player(connection, this, firstsend, server);
 	break;
 	case (byte)'G': //A browser or website is using GET
 	new Browser(connection, this, firstsend);
 	break;
 	case 2: //SMP Player
 	Packet p = this.getPacket(firstsend);
 	if (p == null)
 	connection.close();
 	IOClient ic = new IOClient(connection, this);
 	//ic.Listen();
 	p.Handle(new byte[0], server, ic);
 	break;
 	}
 	}
 	
 	private class Read extends Thread {
 		
 		@Override
 		public void run() {
 			Socket connection = null;
 			while (server.Running) {
 				try {
 					connection = serverSocket.accept();
 					server.Log("Connection made from " + connection.getInetAddress().toString());
 					Accept(connection);
 				} catch (IOException e) {
					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 }
 
