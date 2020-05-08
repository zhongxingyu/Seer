 package com.mojang.mojam.network.packet;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 import com.mojang.mojam.network.NetworkCommand;
 
 public class ChatCommand extends NetworkCommand {
 
 	byte[] message;
 	
	public ChatCommand(){
		message="".getBytes();
	}
	
 	public ChatCommand(String msg) {
 		message=msg.getBytes();
 		
 	}
 
 	@Override
 	public void read(DataInputStream dis) throws IOException {
		message = new byte[dis.readInt()];		
		dis.readFully(message);
 
 	}
 
 	@Override
 	public void write(DataOutputStream dos) throws IOException {
		dos.writeInt(message.length);
 		dos.write(message);
 
 	}
 	
 	public String getMessage(){
 		return new String(message);
 	}
 
 }
