 package com.hannah.arduinomotiondetector;
 
 import java.io.BufferedWriter;
 import java.io.ByteArrayOutputStream;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.util.Properties;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import android.content.Context;
 import android.os.AsyncTask;
 import android.util.Log;
 
 import com.google.android.gms.maps.model.LatLng;
 
 public class WebSender extends AsyncTask<String, Void, Void> {
 
 	private static final String TAG = "WebSender";
 
 	private static final int PORT = 4400;
 	private String ip;
 
 	public WebSender(String ip) {
 		this.ip = ip;
 	}
 	
 	@Override
 	protected Void doInBackground(String... arg0) {
 		Log.d(TAG, "Message: " + arg0[0]);
 		try {
 			InetAddress serverAddr = InetAddress.getByName(ip);
 			Log.d(TAG, "C: Connecting to " + ip + "...");
 			Socket socket = new Socket(serverAddr, PORT);
 			try {
 				Log.d(TAG, "C: Sending: " + arg0[0]);
 				PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
 				out.println(arg0[0]);
 				out.flush();
 				out.close();
 				Log.d(TAG, "C: Sent.");
 			} catch (Exception e) {
 				Log.e(TAG, "S: Error", e);
 			}
 			socket.close();
 			Log.d(TAG, "C: Closed.");
 		} catch (Exception e) {
 			e.printStackTrace();
 			Log.e(TAG, "C: Error", e);
 		}
 
 		return null;
 	}
 	
 	public static String getSettingsXMLMessage(Context context, String name, String email, String phone) {
 		LatLng ll = NotificationPreferences.getLocation(context);
 
 		try {
 			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
 			Document document = documentBuilder.newDocument();
 
 			Element rootElement = document.createElement("root");
 			document.appendChild(rootElement);
 
 			Element messageTypeElement = document.createElement("MessageType");
 			messageTypeElement.appendChild(document.createTextNode("event"));
 			rootElement.appendChild(messageTypeElement);
 			
 			Element sensorNameElement = document.createElement("sensorName");
 			sensorNameElement.appendChild(document.createTextNode(name));
 			rootElement.appendChild(sensorNameElement);
 			
 			Element emailElement = document.createElement("email");
 			emailElement.appendChild(document.createTextNode(email));
 			rootElement.appendChild(emailElement);
 			
 			Element phoneElement = document.createElement("phone");
 			phoneElement.appendChild(document.createTextNode(phone));
 			rootElement.appendChild(phoneElement);
 			
 			Element latElement = document.createElement("latitude");
 			latElement.appendChild(document.createTextNode(ll.latitude + ""));
 			rootElement.appendChild(latElement);
 			
 			Element longElement = document.createElement("longitude");
 			longElement.appendChild(document.createTextNode(ll.longitude + ""));
 			rootElement.appendChild(longElement);
 			
 			TransformerFactory factory = TransformerFactory.newInstance();
 			Transformer transformer = factory.newTransformer();
 			Properties outFormat = new Properties();
 			outFormat.setProperty(OutputKeys.INDENT, "yes");
 			outFormat.setProperty(OutputKeys.METHOD, "xml");
 			outFormat.setProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
 			outFormat.setProperty(OutputKeys.VERSION, "1.0");
 			outFormat.setProperty(OutputKeys.ENCODING, "UTF-8");
 			transformer.setOutputProperties(outFormat);
 			DOMSource domSource = new DOMSource(document.getDocumentElement());
 			OutputStream output = new ByteArrayOutputStream();
 			StreamResult result = new StreamResult(output);
 			transformer.transform(domSource, result);
 			return output.toString();
 
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		} catch (TransformerConfigurationException e) {
 			e.printStackTrace();
 		} catch (TransformerException e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 	
 	public static String getAlertXMLMessage(Context context) {
		LatLng ll = NotificationPreferences.getLocation(context);
 		String name = NotificationPreferences.getSensorName(context);
 		String email = NotificationPreferences.getEmail(context);
 
 		try {
 			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
 			Document document = documentBuilder.newDocument();
 
 			Element rootElement = document.createElement("root");
 			document.appendChild(rootElement);
 
 			Element messageTypeElement = document.createElement("MessageType");
 			messageTypeElement.appendChild(document.createTextNode("Alert"));
 			rootElement.appendChild(messageTypeElement);
 			
 			Element sensorNameElement = document.createElement("sensorName");
 			sensorNameElement.appendChild(document.createTextNode(name));
 			rootElement.appendChild(sensorNameElement);
 			
 			Element emailElement = document.createElement("email");
 			emailElement.appendChild(document.createTextNode(email));
 			rootElement.appendChild(emailElement);
 			
			Element latElement = document.createElement("latitude");
			latElement.appendChild(document.createTextNode(ll.latitude + ""));
			rootElement.appendChild(latElement);
			
			Element longElement = document.createElement("longitude");
			longElement.appendChild(document.createTextNode(ll.longitude + ""));
			rootElement.appendChild(longElement);
			
 			TransformerFactory factory = TransformerFactory.newInstance();
 			Transformer transformer = factory.newTransformer();
 			Properties outFormat = new Properties();
 			outFormat.setProperty(OutputKeys.INDENT, "yes");
 			outFormat.setProperty(OutputKeys.METHOD, "xml");
 			outFormat.setProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
 			outFormat.setProperty(OutputKeys.VERSION, "1.0");
 			outFormat.setProperty(OutputKeys.ENCODING, "UTF-8");
 			transformer.setOutputProperties(outFormat);
 			DOMSource domSource = new DOMSource(document.getDocumentElement());
 			OutputStream output = new ByteArrayOutputStream();
 			StreamResult result = new StreamResult(output);
 			transformer.transform(domSource, result);
 			return output.toString();
 
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		} catch (TransformerConfigurationException e) {
 			e.printStackTrace();
 		} catch (TransformerException e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 }
