 package rsaChat;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.InputStreamReader;
 import java.math.BigInteger;
 import java.net.InetAddress;
 import java.net.Socket;
 
 public class Client {
     // Port to monitor
     final int myPort = 1074;
     BigInteger pubKey;
     BigInteger cKey;
     BigInteger privateKey;
 
    BigInteger ourPubKey = BigInteger.valueOf(1771102189);
    BigInteger ourCKey = BigInteger.valueOf(136924607551l);
    BigInteger outPrivateKey = BigInteger.valueOf(13346149);
 
     /**
      * JavaProgrammingForums.com
      */
     public static void main(String[] args) throws Exception {
 	new Client().run();
 
     }
 
     public void run() throws Exception {
 
 	// Client client = new Client();
 	InetAddress serverIPAddr = InetAddress.getLocalHost();
 
 	Socket csock = new Socket(serverIPAddr, myPort);
 	DataOutputStream out = new DataOutputStream(csock.getOutputStream());
 	BufferedReader in = new BufferedReader(new InputStreamReader(
 		csock.getInputStream()));
 	System.out.println("Please enter message to send to the server: ");
 
 	String keyInfo = in.readLine();
 	System.out.println(keyInfo);
 	int start = keyInfo.indexOf("Public Key:");
 	StringBuilder pKey = new StringBuilder();
 	int pubKeyStart = start + "Public Key:".length();
 	while (keyInfo.charAt(pubKeyStart) != ' ') {
 	    pKey.append(keyInfo.charAt(pubKeyStart));
 	    pubKeyStart++;
 	}
 	pubKey = new BigInteger(pKey.toString());
 
 	// System.out.println("Extracted pub key is:" + pubKey);
 
 	int startCK = keyInfo.indexOf("C_Key is:");
 	StringBuilder cKeyStrBui = new StringBuilder();
 	int cKeyStart = startCK + "C_Key is:".length();
 	while (keyInfo.charAt(cKeyStart) != ' ') {
 	    cKeyStrBui.append(keyInfo.charAt(cKeyStart));
 	    cKeyStart++;
 	}
 	cKey = new BigInteger(cKeyStrBui.toString());
 
 	SendMsg send = new SendMsg(out, pubKey, cKey);
 	send.start();
 	getRequest(in);
     }
 
     void getRequest(BufferedReader in) throws Exception {
 	String incomingMsg;
 	BigInteger cipher = null;
 	while ((incomingMsg = in.readLine()) != null) {
 	    cipher = new BigInteger(incomingMsg);
 	    System.out.println("Received Cipher is :" + cipher);
 	    BigInteger decrpted = RSA.endecrypt(cipher, outPrivateKey, cKey);
 	    System.out.println((char) decrpted.intValue() + " " + decrpted);
 	}
 	return;
     }
 }
