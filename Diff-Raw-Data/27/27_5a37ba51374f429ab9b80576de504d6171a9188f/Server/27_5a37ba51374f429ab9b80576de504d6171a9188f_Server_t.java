 package rsaChat;
 
 import java.io.BufferedReader;
 import java.math.BigInteger;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.Scanner;
 
 public class Server {
    BigInteger ourPubKey = BigInteger.valueOf(1771102189);
    BigInteger ourCKey = BigInteger.valueOf(136924607551l);
    BigInteger ourPrivateKey = BigInteger.valueOf(13346149);
     BigInteger pubKey;
     BigInteger cKey;
 
     /**
      * JavaProgrammingForums.com
      */
     public static void main(String[] args) {
 	try {
 	    new Server().run();
 	} catch (Exception e) {
 	    // TODO Auto-generated catch block
 	    e.printStackTrace();
 	}
 
     }
 
     public void run() throws Exception {
 	Scanner scan = new Scanner(System.in);
 	// Port to monitor
 	final int myPort = 1074;
 	ServerSocket serSock = new ServerSocket(myPort);
 	System.out.println("port " + myPort + " opened");
 
 	Socket sock = serSock.accept();
 	System.out.println("Someone has made socket connection");
 
 	ListenFor listener = new ListenFor(sock);
 
 	System.out
 		.println("Please enter the public key (e, c): first e, then c");
 	BigInteger pubKey = scan.nextBigInteger();
 	BigInteger cKey = scan.nextBigInteger();
 	listener.out.writeBytes("Public Key:" + pubKey + " C_Key is:" + cKey
 		+ " \n");
 	SendMsg send = new SendMsg(listener.out, pubKey, cKey);
 	send.start();
 	getRequest(listener.in);
 	// String s = listener.getRequest();
     }
 
     void getRequest(BufferedReader in) throws Exception {
 	String incomingMsg;
 	BigInteger cipher = null;
 	while ((incomingMsg = in.readLine()) != null) {
 	    cipher = new BigInteger(incomingMsg);
 	    System.out.println("Received Cipher is :" + cipher);
 	    BigInteger decrpted = RSA.endecrypt(cipher, ourPrivateKey, ourCKey);
 	    System.out.println((char) decrpted.intValue() + " " + decrpted);
 	}
 	return;
     }
 }
