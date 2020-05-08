 package com.fyrecloud.amsler;
 
 // TODO: Get rid of this class
 
 import java.io.ByteArrayInputStream;
 import java.security.DigestInputStream;
 import java.security.MessageDigest;
 
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBuffers;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelFuture;
 
 import com.fyrecloud.mysql.MySQLPacket;
 
 import android.util.Log;
 
 /**
  * The purpose of this class is to model the state of a connection.
  * 
  * The following states are defined:
  * START
  * WELCOMED
  * AUTHENTICATED
  *
  * @author Thomas Radloff  fyreman@fyrecloud.com  http://fyrecloud.com/amsler
  *
  */
 public class ReplicationSession {
 	public final static int START = 0;
 	public final static int WELCOMED = 1;
 	public final static int AUTHENTICATED = 2;
 
 	private int presentState = START;
 	private MySQLPacket packet;
 	private Channel channel;
 
 	/**
 	 * Given some transition event, move to the next state
 	 */
 	public void hereIsATransitionEvent(MySQLPacket packet, Channel channel) {
 
 		Log.i("MySQL","Session.hereIsATransitionEvent");
 		this.channel = channel;
 		this.packet = packet;
 
 		//if (presentState == START && packet.frameNumber==0) { //start and packet = welcome, then {
 			//presentState = WELCOMED;
 			//sendLoginRequest();
 			//return;
 		//}
 
 		// else if state = welcomed and we receive a login response
 		// Hack for the short packet
 		//else if (presentState == WELCOMED && packet.frameNumber==2) {
 			//presentState = AUTHENTICATED;
 			//sendBinlogRequest();
 			//return;
 		//}
 
 		//else {
 			// unknown state transition
 		//}
 		
 	}
 
 	private void sendBinlogRequest() {
 		Log.i("MySQL","Session.sendBinlogRequest");
 
 		ChannelBuffer buffer = ChannelBuffers.dynamicBuffer(ChannelBuffers.LITTLE_ENDIAN, 1000);
 
 		buffer.writeMedium(10);		// should calculate this
 		buffer.writeByte(3);		// packet number
 
 		// 4                            binlog position to start at
 		buffer.writeInt(0x0003a285);
 
 		// 2                            binlog flags, always 0
 		buffer.writeInt(0x0);
 
 		// 4                            server id of the slave
 		buffer.writeInt(0x2);
 
 		ChannelFuture lastWriteFuture = channel.write(buffer);
 	}
 
 	/**
 	 * Client Authentication Packet
 	 * From client to server during initial handshake.
 	 * 
 	 */
 	private void sendLoginRequest() {
 		Log.i("MySQL","Session.sendLoginRequest");
 
 		ChannelBuffer buffer = ChannelBuffers.dynamicBuffer(ChannelBuffers.LITTLE_ENDIAN, 1000);
 
 		buffer.writeMedium(58);		// should calculate this
 		buffer.writeByte(1);		// packet number
 
 		// 4                            client_flags
 		buffer.writeInt(0x0003a285);
 
 		// 4                            max_packet_size
 		buffer.writeInt(0x40000000);
 
 		//1                            charset_number
 		buffer.writeByte(33); // utf8 collate utf8_general_ci
 
 		//23                           (filler) always 0x00...
 		buffer.writeBytes(new byte[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0});
 
 		//n (Null-Terminated String)   user
 		buffer.writeBytes(new byte[]{'r','o','o','t',0});
 
 		//n (Length Coded Binary)      scramble_buff (1 + x bytes)
 		// Computed thus.
 		//Remember that mysql.user.Password stores SHA1(SHA1(password))
 		//The server sends a random string (scramble) to the client
 		//the client calculates:
 		//stage1_hash = SHA1(password), using the password that the user has entered.
 		//token = SHA1(scramble + SHA1(stage1_hash)) XOR stage1_hash 
 		//the client sends the token to the server
 		//the server calculates
 		//stage1_hash' = token XOR SHA1(scramble + mysql.user.Password) 
 		//the server compares SHA1(stage1_hash') and mysql.user.Password
 		//If they are the same, the password is okay. 
 		//byte[] example = new byte[] {
 			//0x6f, 0x32, 0x4c, 0x64, 0x35, 0x77, 0x7a, 0x21
 			//0x6f, 0x32, 0x4c, 0x64, 0x35, 0x77, 0x7a, 0x21,
 			//0x32, 0x74, 0x74, 0x6b, 0x3c, 0x29, 0x57, 0x5b, 0x44, 0x7e, 0x71, 0x3e
 			//0x32, 0x74, 0x74, 0x6b, 0x3c, 0x29, 0x57, 0x5b, 0x44, 0x7e, 0x71, 0x3e
 		//};
		byte[] password = new byte[]{};
 		byte[] stage1_hash;
 		byte[] token1 = new byte[]{};
 		try {
 			MessageDigest sha1 = MessageDigest.getInstance("SHA1");
 			//byte[] clearText = new byte[] {'r','o','o','t'};
 			stage1_hash = calculateHash(sha1, password);
 			//System.out.println(stage1_hash);
 
 			byte[] sha1_stage1_hash = calculateHash(sha1, stage1_hash);
 
 			//byte[] completeSalt = concat( packet.scramble_buff1, packet.scramble_buff2);
 			byte[] completeSalt = new byte[]{}; // = concat( packet.scramble_buff1, packet.scramble_buff2);
 
 			//byte[] a = packet.scramble_buff1;
 			//byte[] b = sha1_stage1_hash;
 			//byte[] c = new byte[a.length + b.length];
 			//System.arraycopy(a, 0, c, 0, a.length);
 			//System.arraycopy(b, 0, c, a.length, b.length);
 			byte[] salt_plus_stage1 = concat(completeSalt, sha1_stage1_hash);
 
 			byte[] token = calculateHash( sha1, salt_plus_stage1);
 			token1 = xor(token, stage1_hash, token.length);
 			//final answer for the example should be
 			//0xff, 0x8c, 0x0b, 0xc7, 0xa8, 0x33, 0x7a, 0xd1, 0xb1, 0x1d, 0xc2, 0x61, 0x10, 0x27, 0xd5, 0x36, 0x4f, 0x3d, 0xed, 0xb3
 			//0xff, 0x8c, 0x0b, 0xc7, 0xa8, 0x33, 0x7a, 0xd1, 0xb1, 0x1d, 0xc2, 0x61, 0x10, 0x27, 0xd5, 0x36, 0x4f, 0x3d, 0xed, 0xb3
 			System.out.println(token1);
 		} catch(Exception e) {
 			int i = 5;
 		}
 
 		buffer.writeBytes(new byte[]{20});
 		buffer.writeBytes(token1);
 
 		//n (Null-Terminated String)   databasename (optional)
 		//buffer.writeBytes(new byte[]{0});
 		//buffer.writeBytes(new byte[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0});
 
 		// client_flags:            CLIENT_xxx options. The list of possible flag
 		//                          values is in the description of the Handshake
 		//                          Initialisation Packet, for server_capabilities.
 		//                          For some of the bits, the server passed "what
 		//                          it's capable of". The client leaves some of the
 		//                          bits on, adds others, and passes back to the server.
 		//                          One important flag is: whether compression is desired.
 		//                          Another interesting one is: CLIENT_CONNECT_WITH_DB,
 		//                          which shows the presence of the optional databasename.
 		// 
 		// max_packet_size:         the maximum number of bytes in a packet for the client
 		// 
 		//charset_number:          in the same domain as the server_language field that
 		//                          the server passes in the Handshake Initialization packet.
 		// 
 		// user:                    identification
 		// 
 		// scramble_buff:           the password, after encrypting using the scramble_buff
 		//                          contents passed by the server (see "Password functions"
 		//                          section elsewhere in this document)
 		//                          if length is zero, no password was given
 		// 
 		// databasename:            name of schema to use initially
 		// 
 		ChannelFuture lastWriteFuture = channel.write(buffer);
 		int i = 5;
 	}
 
 	public byte[] xor(byte[] arg1, byte[] arg2, int length) {
 		byte[] retval = new byte[length];
 		for (int i = 0; i < length; i++) {
 			retval[i] = (byte)(arg1[i] ^ arg2[i]);
 		}
 		return retval;
 	}
 
 	public static byte[] calculateHash(MessageDigest algorithm, byte[] clearText) throws Exception{
 		//FileInputStream fis = new FileInputStream(fileName);
 		//BufferedInputStream bis = new BufferedInputStream(fis);
 		ByteArrayInputStream bais = new ByteArrayInputStream(clearText);
 		DigestInputStream dis = new DigestInputStream(bais, algorithm);
 		// read the file and update the hash calculation
 		while (dis.read() != -1);
 		// get the hash value as byte array
 		byte[] hash = algorithm.digest();
 		return hash;
 		//return byteArray2Hex(hash);
 	}
 
 	private byte[] concat( byte[] a, byte[] b) {
 		byte[] c = new byte[a.length + b.length];
 		System.arraycopy(a, 0, c, 0, a.length);
 		System.arraycopy(b, 0, c, a.length, b.length);
 		return c;
 	}
 	public void setChannel(Channel c) {
 		this.channel = c;
 	}
 }
