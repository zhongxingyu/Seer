 package com.fyrecloud.amsler;
 
 import java.io.ByteArrayInputStream;
 import java.security.DigestInputStream;
 import java.security.MessageDigest;
 
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBuffers;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ExceptionEvent;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.channel.SimpleChannelHandler;
 
 import com.fyrecloud.mysql.EOFPacket;
 import com.fyrecloud.mysql.HandshakeInitPacket;
 import com.fyrecloud.mysql.LogEventPacket;
 import com.fyrecloud.mysql.MySQLPacket;
 import com.fyrecloud.mysql.ResponseOKPacket;
 
 import android.util.Log;
 
 /**
  * This class handles incoming packets from the MySQL Server.  It's the 2nd part of
  * the packet handling pipeline and it gets the packets after they are decoded by
  * MySQLFrameDecoder.
  *
  * This class will receive an incoming packet and create and send a response
  * if necessary.  The packets are not independent of each other.  Communication
  * with the MySQL server needs to take place in the context of a "session."
  * This class manages such a session in order to do its job.
  * 
  * @author Thomas Radloff  fyreman@fyrecloud.com  http://fyrecloud.com/amsler
  *
  */
 public class MySQLClientHandler extends SimpleChannelHandler {
 
 	/*
 	 * When communication with the server is initially started, the session state = START
 	 */
 	private final static int START = 0;
 
 	/*
 	 * After receipt of the handshake/welcome message, but before proper authentication,
 	 * the session state = AUTHENTICATING
 	 */
 	private final static int AUTHENTICATING = 1;
 
 	/*
 	 * After the server acknowledges that we've successfully authenticated, the session state = AUTHENTICATED
 	 */
 	private final static int AUTHENTICATED = 2;
 
 	/*
 	 * Methods in this class may use an old password hashing scheme from MySQL.  The length
 	 * of the resulting answer, in bytes.
 	 */
 	private final static int SCRAMBLE_323_LEN = 8;
 
 	private int presentState = START;
 	private Channel channel;
 
 	/**
 	 * Save the welcome packet because we'll need it later
 	 */
 	private HandshakeInitPacket welcome_packet;
 
 	/**
 	 * This is the defacto MVC controller for the app.  We need this
 	 * in order to communicate with the rest of the app.
 	 */
 	private AmslerApplication theController;
 
 	// TODO: Clean this up
 	public MySQLClientHandler(AmslerApplication a) {
 		super();
 		this.theController = a;
 	}
 
 	/**
 	 * This method is the meat and potatoes of this class.  It is the method that
 	 * will figure out what to do with an incoming packet, based on the packet type
 	 * and the present MySQL server session state.
 	 * 
 	 */
 	@Override
 	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
 		Log.i("MySQL","MySQLClientHandler.messageReceived");
 
 		MySQLPacket packet = (MySQLPacket) e.getMessage();
 		channel = e.getChannel();
 
 		// Check to see if we should terminate this connection
 		if (theController.masterServerChannelIsTerminated()) {
 			channel.disconnect();
 			Log.i("MySQL","MySQLClientHanderl.messageReceived terminating");
 		}
 
 		// Now determine what kind of packet the incoming packet is
 		// and react accordingly
 		if ( presentState == START && packet.getClass() == HandshakeInitPacket.class) {
 			presentState = AUTHENTICATING;
 			welcome_packet = (HandshakeInitPacket)packet;
 			sendLoginRequest();
 			return;
 
 		} else if (presentState == AUTHENTICATING && packet.getClass() == EOFPacket.class){
 			// sb ShortPacket
 			// And server flags = MYSQL SECURE?
 			sendLoginRequest2();
 			return;
 
 		} else if (presentState == AUTHENTICATING && packet.getClass() == ResponseOKPacket.class){
 			presentState = AUTHENTICATED;
 			sendRequestBinlogDump();
 			return;
 
 		} else if (presentState == AUTHENTICATED && packet.getClass() == ResponseOKPacket.class){
 			dealWithResponseOK( (ResponseOKPacket) packet);
 			return;
 		}
 
 		// TODO: Handle this
 		//else {
 			// unknown state transition
 		//}
 	}
 
 	private void dealWithResponseOK(ResponseOKPacket rop) {
 
 		// The first two frames are not real.  Unknown purpose.
 		if (rop.frameNumber == 1 && rop.frameLength == 44) return;
 		//if (rop.frameNumber == 2 && rop.frameLength == 95) return;
 
 		LogEventPacket lep = LogEventPacket.logEventFactory(rop);
 
 		// Process this event
 		// TODO: Clean this up
 		//synchronizer.pprocess(master_position);
 		theController.handleReplicationEvent(lep);
 		Log.i("MySQL","Next position = "+lep.master_position);
 
 		//int actualPacketLen = rop.frameLength - 1; // the frame has an extra zero
 		//int nextStart = log_position + actualPacketLen;
 		//String m = "start="+log_position+", packet_len="+actualPacketLen+", next="+nextStart;
 		//log_position = nextStart;
 		//Log.i("MySQL",m);
 	}
 
 	// cmd 0x12
 	private void sendRequestBinlogDump() {
 		Log.i("MySQL","MySQLClientHandler.sendRequestBinlogDump");
 		ChannelBuffer buffer = ChannelBuffers.dynamicBuffer(ChannelBuffers.LITTLE_ENDIAN, 1000);
 		buffer.writeMedium(27);		// packet len should calculate this
 		buffer.writeByte(0);		// packet number. always starts at zero
 		buffer.writeByte(0x12);		// command
 		//buffer.writeBytes(new byte[]{0x21,0x20,0x05,0x00,0x00,0x00,0x64,0x00,0x00,0x00,'m','y','s','q','l','-','b','i','n','.','0','0','0','0','0','1'});
 		//buffer.writeBytes(new byte[]{0x021,0x20,0x05,0x00,0x00,0x00,0x64,0x00,0x00,0x00,'m','y','s','q','l','-','b','i','n','.','0','0','0','0','0','1'});
 		buffer.writeInt(theController.getLogPosition());  // 4 bytes - start position little endian
 		buffer.writeShort(0);           // 2 bytes - unused
 		buffer.writeInt(theController.getServerID()); // 4 bytes - server id little endian
 
 		buffer.writeBytes(new byte[]{'m','y','s','q','l','-','b','i','n','.','0','0','0','0','0','2'});
 		//buffer.writeBytes(new byte[]{'m','y','s','q','l','-','b','i','n','.','0','0','0','0','0','1'});
 		channel.write(buffer);
 	}
 
 	private void sendLoginRequest() {
 		Log.i("MySQL","MySQLClientHandler.sendLoginRequest");
 		ChannelBuffer buffer = ChannelBuffers.dynamicBuffer(ChannelBuffers.LITTLE_ENDIAN, 1000);
 
 		buffer.writeMedium(60);		// should calculate this length
 		buffer.writeByte(1);		// packet number, calculate this
 
 		// 4                            client_flags
 		buffer.writeInt(0x8000a205);
 
 		// 4                            max_packet_size
 		buffer.writeInt(0x00100000);
 
 		//1                            charset_number
 		//buffer.writeByte(33; 			// utf8 collate utf8_general_ci
 		buffer.writeByte(8); 			// utf8 collate utf8_general_ci
 
 		//23                           (filler) always 0x00...
 		buffer.writeBytes(new byte[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0});
 
 		//n (Null-Terminated String)   user
 		//buffer.writeBytes(new byte[]{'r','o','o','t',0});
 		buffer.writeBytes(new byte[]{'s','l','a','v','e','r',0});
 
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
 		byte[] password = new byte[]{'c','a','t','f','o','o','d','i','s','g','o','o','d'};
 
 		byte[] stage1_hash;
 		byte[] token1 = new byte[]{};
 		try {
 			MessageDigest sha1 = MessageDigest.getInstance("SHA1");
 			stage1_hash = calculateHash(sha1, password);
 
 			byte[] sha1_stage1_hash = calculateHash(sha1, stage1_hash);
 			byte[] completeSalt = concat( welcome_packet.scramble_buff1, welcome_packet.scramble_buff2);
 
 			byte[] salt_plus_stage1 = concat(completeSalt, sha1_stage1_hash);
 
 			byte[] token = calculateHash( sha1, salt_plus_stage1);
 			token1 = xor(token, stage1_hash, token.length);
 
 		} catch(Exception ignore) {
 			ignore.printStackTrace(System.out);
 		}
 
 		buffer.writeBytes(new byte[]{20});
 		buffer.writeBytes(token1);
 
 		channel.write(buffer);
 	}
 
 	/**
 	 * Under some circumstances during the authentication handshake, the server
 	 * will request that a second scrambled password be sent.  This method will
 	 * compute the value of this password based on the the scramble buffers from the
 	 * original HandshakeInitPacket.
 	 * 
 	 * The details of the computation were determined by examining the MySQL command
 	 * line client source code.  A small number of methods from the source code have 
 	 * been ported in order to support this computation.
 	 */
 	private void sendLoginRequest2() {
 
 		Log.i("MySQL","MySQLClientHandler.sendLoginRequest2");
 		ChannelBuffer buffer = ChannelBuffers.dynamicBuffer(ChannelBuffers.LITTLE_ENDIAN, 20);
 
 		byte[] scramble = concat( welcome_packet.scramble_buff1, welcome_packet.scramble_buff2);
 
 		byte[] to = new byte[SCRAMBLE_323_LEN + 1];
 		for(int i = 0; i < to.length; i++) to[i] = 0;
 
 		// example 1 password
 		byte[] password = new byte[] {'c','a','t','f','o','o','d','i','s','g','o','o','d'};
 		scramble_323(to, scramble, password);
 
 		buffer.writeMedium(9);		// should calculate this
 		buffer.writeByte(3);		// packet number, calculate this
 		buffer.writeBytes(to);
 
 		//ChannelFuture lastWriteFuture = channel.write(buffer);
 		channel.write(buffer);
 	}
 
 	@Override
 	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
 		e.getCause().printStackTrace();
 		e.getChannel().close();
 	}
 
 	/**
 	 * Methods in support of the hashing functions...
 	 */
 
 	/**
 	 * This method will concatenate two byte[].  This is needed by the hashing functions.
 	 * @param a
 	 * @param b 
 	 * @return The resulting concatenated byte[]
 	 */
 	private byte[] concat( byte[] a, byte[] b) {
 		byte[] c = new byte[a.length + b.length];
 		System.arraycopy(a, 0, c, 0, a.length);
 		System.arraycopy(b, 0, c, a.length, b.length);
 		return c;
 	}
 
 	/**
 	 * This method will xor the first length bytes of the two input byte arrays
 	 * and return the result.
 	 * @param a
 	 * @param b
 	 * @param length
 	 * @return
 	 */
 	private byte[] xor(byte[] a, byte[] b, int length) {
 		byte[] retval = new byte[length];
 		for (int i = 0; i < length; i++)
 			retval[i] = (byte)(a[i] ^ b[i]);
 		return retval;
 	}
 
 	/**
 	 * This method will hash clearText using a given algorithm.
 	 * 
 	 * @param algorithm
 	 * @param clearText
 	 * @return
 	 * @throws Exception
 	 */
 	private static byte[] calculateHash(MessageDigest algorithm, byte[] clearText) throws Exception{
 		ByteArrayInputStream bais = new ByteArrayInputStream(clearText);
 		DigestInputStream dis = new DigestInputStream(bais, algorithm);
 
 		// read the file and update the hash calculation
 		while (dis.read() != -1);
 		// get the hash value as byte array
 		byte[] hash = algorithm.digest();
 		return hash;
 	}
 
 	/**
 	 * In order to scramble a password using the old MySQL 3.23 methodology we need
 	 * this method.  This method originally comes from libmysql/password.c  
 	 * I present the original in comment.
 	 * 
 	 * @param rand_struct
 	 *
 	 * New (MySQL 3.21+) random generation structure initialization
 	 * SYNOPSIS
 	 *   randominit()
 	 *   rand_st    OUT  Structure to initialize
 	 *   seed1      IN   First initialization parameter
 	 *   seed2      IN   Second initialization parameter
 	 *
 	 * void randominit(struct rand_struct *rand_st, ulong seed1, ulong seed2)
 	 *{                                               / For mysql 3.21.# /
 	 *#ifdef HAVE_purify
 	 *  bzero((char*) rand_st,sizeof(*rand_st));      / Avoid UMC varnings /
 	 *#endif
 	 *  rand_st->max_value= 0x3FFFFFFFL;
 	 *  rand_st->max_value_dbl=(double) rand_st->max_value;
 	 *  rand_st->seed1=seed1%rand_st->max_value ;
 	 *  rand_st->seed2=seed2%rand_st->max_value;
 	 * }
 	 */
 	private void randominit(RandStruct rand_st, int seed1, int seed2) {
 		rand_st.max_value = 0x3FFFFFFF;
 		rand_st.max_value_dbl=(double) rand_st.max_value;
 		rand_st.seed1=seed1%rand_st.max_value ;
 		rand_st.seed2=seed2%rand_st.max_value;
 	}
 
 	/**
 	 * In order to scramble a password using the old MySQL 3.23 methodology we need
 	 * this method.  This method originally comes from libmysql/password.c  
 	 * I present the original in comment.
 	 *
 	 * Scramble string with password.
 	 * Used in pre 4.1 authentication phase.
 	 * SYNOPSIS
 	 * scramble_323()
 	 * to       OUT Store scrambled message here. Buffer must be at least
 	 *             SCRAMBLE_LENGTH_323+1 bytes long
 	 * message  IN  Message to scramble. Message must be at least
 	 *             SRAMBLE_LENGTH_323 bytes long.
 	 * password IN  Password to use while scrambling
 	 *
 	 * void scramble_323(char *to, const char *message, const char *password)
 	 * {
 	 *   struct rand_struct rand_st;
 	 *   ulong hash_pass[2], hash_message[2];
 	 * 
 	 *   if (password && password[0])
 	 *   {
 	 *     char extra, *to_start=to;
 	 *     const char *message_end= message + SCRAMBLE_LENGTH_323;
 	 *     hash_password(hash_pass,password, (uint) strlen(password));
 	 *     hash_password(hash_message, message, SCRAMBLE_LENGTH_323);
 	 *     randominit(&rand_st,hash_pass[0] ^ hash_message[0],
 	 *                hash_pass[1] ^ hash_message[1]);
 	 *     for (; message < message_end; message++)
 	 *       *to++= (char) (floor(my_rnd(&rand_st)*31)+64);
 	 *     extra=(char) (floor(my_rnd(&rand_st)*31));
 	 *     while (to_start != to)
 	 *       *(to_start++)^=extra;
 	 *   }
 	 *   *to= 0;
 	 * }
 	 *
 	 * @param to
 	 * @param message
 	 * @param password
 	 */
 	private void scramble_323(byte[] to, byte[] message, byte[] password) {
 
 		RandStruct rand_st = new RandStruct();
 
 		int[] hash_pass    = new int[2];
 		int[] hash_message = new int[2];
 
 		// If there is a password...
 		//if (password && password[0]) {
 		if (true) {
 			hash_password(hash_pass,password, password.length);
 			hash_password(hash_message, message, SCRAMBLE_323_LEN);
 			randominit(rand_st, hash_pass[0] ^ hash_message[0], hash_pass[1] ^ hash_message[1]);
 
 			int idx1 = 0;
 			for (idx1 = 0; idx1 < SCRAMBLE_323_LEN; idx1++) {
 				double d = my_rnd(rand_st);
 				double d1 = d*31;
 				double d2 = Math.floor(d1)+64;
 				byte b = (byte)d2;
 				to[idx1] = b;
 			}
 
 			byte extra = (byte) Math.floor(my_rnd(rand_st)*31);
 
 			for (int idx2 = 0; idx2 < idx1; idx2++)
 				to[idx2] ^= extra;
 		}
 	}
 
 	/**
 	 * In order to scramble a password using the old MySQL 3.23 methodology we need
 	 * this method.  This method originally comes from libmysql/password.c  
 	 * I present the original in comment.
 	 * 
 	 * Generate binary hash from raw text string 
 	 * Used for Pre-4.1 password handling
 	 * SYNOPSIS
 	 * hash_password()
 	 * result       OUT store hash in this location
 	 * password     IN  plain text password to build hash
 	 * password_len IN  password length (password may be not null-terminated)
 	 *
 	 * void hash_password(ulong *result, const char *password, uint password_len)
 	 * {
 	 *   register ulong nr=1345345333L, add=7, nr2=0x12345671L;
 	 *   ulong tmp;
 	 *   const char *password_end= password + password_len;
 	 *   for (; password < password_end; password++)
 	 *   {
 	 *     if (*password == ' ' || *password == '\t')
 	 *       continue;                                 / skip space in password /
 	 *     tmp= (ulong) (uchar) *password;
 	 *     nr^= (((nr & 63)+add)*tmp)+ (nr << 8);
 	 *     nr2+=(nr2 << 8) ^ nr;
 	 *     add+=tmp;
 	 *   }
 	 *   result[0]=nr & (((ulong) 1L << 31) -1L); / Don't use sign bit (str2int) /;
 	 *   result[1]=nr2 & (((ulong) 1L << 31) -1L);
 	 * }
 	 *
 	 * @params result  int[]
 	 * @params password byte[]
 	 * @params password_len int
 	*/
 	private void hash_password(int[] result, byte[] password, int password_len) {
 
 		int nr = 1345345333;
 		int add=7;
 		int nr2 = 0x12345671;
 
 		for (int idx = 0; idx < password_len; idx++) {
 			//if (*password == ' ' || *password == '\t')
 			//continue;                                 /* skip space in password */
 			int tmp = password[idx];
 			nr^= (((nr & 63)+add)*tmp)+ (nr << 8);
 			nr2+=(nr2 << 8) ^ nr;
 			add+=tmp;
 		}
 
 		result[0]=nr & (( 1 << 31) -1); /* Don't use sign bit (str2int) */;
 		result[1]=nr2 & ((1 << 31) -1);
 	}
 
 	/**
 	 * In order to scramble a password using the old MySQL 3.23 methodology we need
 	 * this method.  This method originally comes from libmysql/password.c  
 	 * I present the original in comment.
 	 * 
 	 *Generate random number.
 	 *SYNOPSIS
 	 *my_rnd()
 	 *rand_st    INOUT  Structure used for number generation
 	 *RETURN VALUE
 	 *generated pseudo random number
 	 *
 	 *double my_rnd(struct rand_struct *rand_st)
 	 *{
 	 * rand_st->seed1=(rand_st->seed1*3+rand_st->seed2) % rand_st->max_value;
 	 * rand_st->seed2=(rand_st->seed1+rand_st->seed2+33) % rand_st->max_value;
 	 * return (((double) rand_st->seed1)/rand_st->max_value_dbl);
 	 *}
 	 * @params RandStruct rand_st
 	 * @returns double the random number to return
 	 */
 	private double my_rnd(RandStruct rand_st) {
 		rand_st.seed1 = (rand_st.seed1*3 + rand_st.seed2) % rand_st.max_value;
 		rand_st.seed2 = (rand_st.seed1 + rand_st.seed2 + 33) % rand_st.max_value;
 		return ((double)rand_st.seed1)/rand_st.max_value_dbl;
 	}
 
 	/**
 	 * The methods that scramble a password using the old MySQL 3.23 methodology
 	 * need this class.  This class originally comes from mysql.h  
 	 * I present the original in comment.
 	 * 
 	 * struct rand_struct {
 	 * unsigned long seed1,seed2,max_value;
 	 * double max_value_dbl;
 	 *};
 	 */
 	private class RandStruct {
 		public long seed1;
 		public long seed2;
 		public long max_value;
 		public double max_value_dbl;
 	}
 
 }
