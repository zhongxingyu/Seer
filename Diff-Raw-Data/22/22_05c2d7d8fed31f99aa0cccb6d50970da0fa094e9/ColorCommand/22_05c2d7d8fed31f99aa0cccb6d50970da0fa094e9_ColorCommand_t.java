 package client;
 
 import java.awt.Color;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 import model.Picture;
 
 import common.Protocol;
 
 public class ColorCommand implements DrawingCommand {
 
 	private Color c;
 	
 	public ColorCommand() {
 		
 	}
 	
 	public ColorCommand(Color c) {
 		this.c = c;
 	}
 	
 	@Override
 	public void send(DataOutputStream dos) throws IOException {
 		dos.writeByte(Protocol.CMD_SET_COLOR);
 		
 		dos.writeShort(c.getRed());
 		dos.writeShort(c.getGreen());
 		dos.writeShort(c.getBlue());
 		
		dos.writeByte(Protocol.END);	
		System.out.println("sending colorcmd");
 	}
 
 	@Override
 	public void perform(DataInputStream dis, Picture picture) throws IOException {
 		short red = dis.readShort();
 		short green = dis.readShort();
 		short blue = dis.readShort();
 		picture.setColor(new Color(red, green, blue));
 		dis.readByte();
 	}
 
 }
