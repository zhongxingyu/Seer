import java.io.*;
 import java.net.*;
 import java.security.*;
import java.security.spec.*;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import javax.crypto.*;
 import javax.crypto.spec.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.*;
 
 
 public class SendUDP {
 
 	/**
 	 * @param args IP address
 	 * @throws Exception 
 	 */
 	public static void main(String[] args) {
 
 		DatagramSocket clientSocket;
 		try {
 
 			clientSocket = new DatagramSocket();
 
 
 
 
 			InetAddress IPAddress = InetAddress.getByName("localhost");
 			if (args.length == 1) {
 				IPAddress = InetAddress.getByName(args[0]);
 			}
 			//			for(int i = 0; i < 10; i++){	
 
 			byte[] cryptoData = new byte[1024];
 			//			String sentence = "27,2," + randomValue + ",Temperature," + getTime() + ",";
 			cryptoData = encryptAES(composeXMLFile());
 			DatagramPacket sendPacket = new DatagramPacket(cryptoData, cryptoData.length, IPAddress, 50000);
			System.out.println("Sending encrypted message: " + cryptoData.toString() + "\nto: " + IPAddress);
 			clientSocket.send(sendPacket);
 			//			}
 			clientSocket.close();
 
 
 		}			 
 		catch (SocketException e) {
 			e.printStackTrace();
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (Exception e) {
 			System.out.println("Not a valid message!");
 			e.printStackTrace();
 		}
 
 	}
 	private static byte[] encryptAES(String message) throws InvalidParameterSpecException  {
 		byte[] encryptedMessage;
 		final byte[] temp = new byte[992];
 		final byte[] encryptedMessageToReturn = new byte[1024];
 		final byte[] messageToBeDigested = new byte[1008];
 
 		try {
 			SecretKeySpec skeySpec = new SecretKeySpec("PK80111q''eto0z<".getBytes(), "AES");
 			Cipher cipher = Cipher.getInstance("AES/CBC/Nopadding");
 			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
 			byte[] iv = new byte[16];
 			random.nextBytes(iv);
 			AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
 
 			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, paramSpec);
 
 			final byte[] plainTextBytes = message.getBytes("UTF-8");
 			// Appends the plain message to a byte string with the size 1008 to be encrypted.
 			for(int i = 0; i < plainTextBytes.length; i++){
 				temp[i] = plainTextBytes[i];
 			}
 			encryptedMessage = cipher.doFinal(temp);
 			// Creates a message digest for the encrypted message.
 			Mac md = Mac.getInstance("HmacMD5");
 			md.init(skeySpec);
 
 			for(int i = 0; i < 1008; i++){
 				if(i < 992){
 					messageToBeDigested[i] = encryptedMessage[i];
 				}else{
 					messageToBeDigested[i] = iv[i - 992];
 				}
 			}
 			md.update(messageToBeDigested);
 			byte[] digest = md.doFinal();
 
 			// Adds the encrypted message and the digest to the byte string to be sent.
 			for(int i = 0; i < 992; i++){
 				encryptedMessageToReturn[i] = encryptedMessage[i];
 			}
 			for(int i = 0; i < 16; i++){
 				encryptedMessageToReturn[992+i] = iv[i];
 			}
 			for(int i = 0; i < 16; i++){
 				encryptedMessageToReturn[1008+i] = digest[i];
 			}
 		} catch (NoSuchAlgorithmException e) {
 			e.printStackTrace();
 		} catch (NoSuchPaddingException e) {
 			e.printStackTrace();
 		} catch (InvalidKeyException e) {
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		} catch (IllegalBlockSizeException e) {
 			e.printStackTrace();
 		} catch (BadPaddingException e) {
 			e.printStackTrace();
 		} catch (InvalidAlgorithmParameterException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return encryptedMessageToReturn;
 	}
 	public static String getTime() {
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		String time = sdf.format(Calendar.getInstance().getTime());
 
 		return time;
 
 	}
 	public static String composeXMLFile(){
 		Random rnd = new Random();
 		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder docBuilder;
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		try {
 			docBuilder = docFactory.newDocumentBuilder();
 
 
 			Document doc = docBuilder.newDocument();
 			Element rootElement = doc.createElement("data");
 			doc.appendChild(rootElement);
 			for(int i = 0; i < 2; i++){
 				float randomValue = ((rnd.nextInt(10000) % 50) -20) + rnd.nextFloat();
 				
 				Element sensor_reading = doc.createElementNS(null, "sensor_reading");
 				sensor_reading.setAttributeNS(null, "gateway_id", "27");
 				sensor_reading.setAttributeNS(null, "sensor_id", "2");
 				sensor_reading.setAttributeNS(null, "value", "" + randomValue);
 				sensor_reading.setAttributeNS(null, "type", "Temperature");
 				sensor_reading.setAttributeNS(null, "time", getTime());
 				rootElement.appendChild(sensor_reading);
 			}
 			Result result = new StreamResult(out);
 			TransformerFactory factory = TransformerFactory.newInstance();
 			Transformer transformer = factory.newTransformer();
 			transformer.transform(new DOMSource(doc), result);
 
 			System.out.println(out.toString());
 			return out.toString();
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		} catch (TransformerConfigurationException e) {
 			e.printStackTrace();
 		} catch (TransformerException e) {
 			e.printStackTrace();
 		}
 
 		return out.toString();
 
 
 
 	}
 
 
 }
