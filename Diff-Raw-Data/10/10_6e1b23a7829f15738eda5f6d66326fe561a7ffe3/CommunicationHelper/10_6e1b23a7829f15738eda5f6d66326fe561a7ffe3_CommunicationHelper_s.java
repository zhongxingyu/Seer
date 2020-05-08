 /**
  * 
  */
 package com.n8lm.MCShopSystemPlugin.server;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 /**
  * @author Alchemist
  *
  */
 public final class CommunicationHelper {
 
 	public static String readString(DataInputStream in) throws IOException
 	{
 		int stringSize = in.readInt();
 		StringBuilder buffer = new StringBuilder();
 		for (int i = 0; i < stringSize; i++)
 		{
 			buffer.append(in.readChar());
 		}
 	
 		return buffer.toString();
 	}
 
 	public static void writeString(DataOutputStream out, String string) throws IOException
 	{
 		out.writeInt(string.length());
 		out.writeChars(string);
 	}
 	
 	public static void writeInt(DataOutputStream out, int i) throws IOException
 	{
 		out.writeInt(i);
 	}
 }
