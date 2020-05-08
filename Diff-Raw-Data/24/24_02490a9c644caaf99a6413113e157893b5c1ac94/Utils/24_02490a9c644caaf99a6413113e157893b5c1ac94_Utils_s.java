 ﻿package com.desklampstudios.edab;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.util.Arrays;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.crypto.Mac;
 import javax.crypto.spec.SecretKeySpec;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import javax.xml.bind.DatatypeConverter;
 
 import com.google.appengine.api.utils.SystemProperty;
 
 public class Utils {
 	private static final Logger log = Logger.getLogger(Utils.class.getName());
 	public static boolean isProduction = SystemProperty.environment.value() == SystemProperty.Environment.Value.Production;
 	
 	// To be appended to JSON strings in responses as a deterrent to CSRF.
 	// Stripped automatically by angularjs
 	public static final String JsonPad = ")]}',\n";
 
 	// System property, set in appengine-web.xml
 	static final int sessionTimeout = Integer.parseInt(System.getProperty("edab.session-timeout"));
 
 	// Uploading this onto Github may not be the best idea, but whateverr
 	// Let's add more than one to make things interesting
 	// Though just SecureRandom-ing and storing a token would probs be better
 	// ** Moved to Secrets.java **
 	// private static final String[] hashingSalts = { ... };
 
 	// SHA256 produces 256 bits of entropy, or 42-43 base64 chars.
 	// On deployment the session ID is 22 base64 chars, or about *132 bits*.
 	// So we'll round up to 18 bytes = *144 bits* = 24 base64 chars.
 	static final int csrfTokenBytes = 18;
 
 	// Short constructor for fetchURL that don't need to send data.
 	public static String fetchURL(String method, String loadUrl) throws IOException {
 		return fetchURL(method, loadUrl, null, null);
 	}
 	// A function to easily get data from URLs.
 	public static String fetchURL(String method, String loadUrl, String data, String contentType) throws IOException {
 		// Open the connection.
 		HttpURLConnection connection = null;
 		try {
 			URL url = new URL(loadUrl);
 			connection = (HttpURLConnection) url.openConnection();
 			connection.setRequestMethod(method);
 			if (contentType != null) {
 				connection.setRequestProperty("Content-Type", contentType);
 			}
 		} catch (IOException e) {
 			throw e;
 		}
 
 		// If we are given data, write it.
 		if (data != null) {
 			connection.setDoOutput(true);
 			writeOutputStream(connection.getOutputStream(), data);
 		}
 
 		// Get the input back.
 		String input = readInputStream(connection.getInputStream());
 
 		// Disconnect - probably isn't needed.
 		connection.disconnect();
 
 		return input;
 	}
 
 	// Helper function that writes a String to an OutputStream.
 	public static void writeOutputStream(OutputStream outputStream, String output) throws IOException {
 		OutputStreamWriter writer = null;
 		try {
 			writer = new OutputStreamWriter(outputStream);
 			writer.write(output);
 		} catch (IOException e) {
 			throw e;
 		} finally {
 			if (writer != null) {
 				writer.close();
 			}
 		}
 	}
 
 	// Helper function that reads a String from an InputStream.
 	public static String readInputStream(InputStream inputStream) throws IOException {
 		BufferedReader reader = null;
 		StringBuilder inputBuilder = new StringBuilder();
 		String line;
 		try {
 			reader = new BufferedReader(new InputStreamReader(inputStream));
 
 			// As long as there are still more lines, keep appending them.
 			while ((line = reader.readLine()) != null) {
 				inputBuilder.append(line);
 				inputBuilder.append("\n");
 			}
 		} catch (IOException e) {
 			throw e;
 		} finally {
 			if (reader != null) {
 				reader.close();
 			}
 		}
 		return inputBuilder.toString();
 	}
 
 	// Helper function that's close to encodeURIComponent in JS.
 	// See http://stackoverflow.com/q/607176/689161
 	public static String encodeURIComponent(String input) {
 		String result = null;
 
 		try {
 			result = URLEncoder.encode(input, "UTF-8").replaceAll("\\+", "%20");
 		} catch (UnsupportedEncodingException e) {
 			// this should never happen.
 			log.log(Level.WARNING, "Something went wrong", e);
 			return null;
 		}
 
 		return result;
 	}
 
 	// http://stackoverflow.com/q/9655181/
 	public static String bytesToHexString(byte[] bytes) {
 		StringBuilder sb = new StringBuilder();
 		for (byte b : bytes) {
 			sb.append(String.format("%02x", b & 0xff));
 		}
 		return sb.toString();
 	}
 
 	// helper function to generate a random nonce. Gets 8 bytes of random bits 
 	// from SecureRandom, and converts it into hexadecimal to put into a StringBuilder.
 	/*
 	public static String generateNonce(int byteCount) {
 		SecureRandom rand = new SecureRandom();
 		byte[] bytes = new byte[byteCount];
 		rand.nextBytes(bytes);
 
 		return bytesToHexString(bytes);
 	}
 	 */
 
 	// Calculates the corresponding CSRF token for a session ID by hashing it.
 	static String getCsrfTokenFromSessionId(String sessionId) {
 		Mac mac = null;
 		try {
 			mac = Mac.getInstance("HmacSha256");
 		} catch (NoSuchAlgorithmException e) {
 			// should never happen irdc
 			throw new RuntimeException(e);
 		}
 
 		// do some weird stuff and select a hashing salt to use
 		int hashIndex0 = sessionId.charAt(0) % sessionId.length();
		int hashIndex1 = sessionId.charAt(hashIndex0) % Secrets.hashingSalts.length;
		String hashSalt = Secrets.hashingSalts[hashIndex1];
 
 		SecretKeySpec secret = new SecretKeySpec(hashSalt.getBytes(), "HmacSha256");
 		try {
 			mac.init(secret);
 		} catch (InvalidKeyException e) {
 			// should never happen irdc
 			throw new RuntimeException(e);
 		}
 
 		byte[] shaDigest = mac.doFinal(sessionId.getBytes());
 
 		// take only the first 128 bits = 16 bytes (half the size of SHA256 output)
 		return DatatypeConverter.printBase64Binary(
 				Arrays.copyOfRange(shaDigest, 0, csrfTokenBytes));
 	}
 
 	static void sendEmail(String from, String to, String subject, String body) throws MessagingException {
 		Properties props = new Properties();
 		Session session = Session.getDefaultInstance(props, null);
 
 		Message msg = new MimeMessage(session);
 		msg.setFrom(new InternetAddress(from));
 		msg.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
 		msg.setSubject(subject);
 		msg.setText(body);
 		Transport.send(msg);
 	}
 }
