 package org.opnfre.mail;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.Inet4Address;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Scanner;
 
 import org.apache.commons.codec.binary.Base64;
 
 public class Send {
 	private List<String> bcc = new ArrayList<String>();
 
 	private String formName;
 
 	private String formMail;
 
 	private Scanner in;
 
 	private OutputStream out;
 
 	private String msg;
 
 	private Socket socket;
 
 	private String subject;
 
 	private List<Map<String, String>> to = new ArrayList<Map<String, String>>();
 
 	public void addBcc(String email) {
 		bcc.add(email);
 	}
 
 	public void addTo(String email) {
		addTo(null, email);
 	}
 
 	public void addTo(String name, String email) {
 		Map<String, String> toInfo = new HashMap<String, String>();
 		toInfo.put(email, emailEnCode(name));
 		to.add(toInfo);
 	}
 
 	public int close() throws IOException {
 		out.write("QUIT\r\n".getBytes());
 		int result = new Integer(in.nextLine().substring(0, 3));
 		socket.close();
 		return result;
 	}
 
 	public int connect() throws IOException {
 		return connect(
 				Inet4Address.getByAddress(new byte[] { (byte) 127, 0, 0, 1 }),
 				25);
 	}
 
 	public int connect(InetAddress address, int port) throws IOException {
 		socket = new Socket(address, port);
 		in = new Scanner(socket.getInputStream());
 		out = socket.getOutputStream();
 		return new Integer(in.nextLine().substring(0, 3));
 	}
 
 	private String emailEnCode(String str) {
 		if (str == null)
 			return null;
 		String base64encode = Base64.encodeBase64String(str.getBytes());
 		return "=?UTF-8?B?" + base64encode + "?=";
 	}
 
 	private String genBccsField() {
 		StringBuilder sb = new StringBuilder();
 		boolean first = true;
 		for (String mail : bcc) {
 			if (first)
 				first = false;
 			else
 				sb.append(", ");
 			sb.append(mail);
 		}
 		return sb.toString();
 	}
 
 	private String genMailHeader() {
 		StringBuilder sb = new StringBuilder();
 		sb.append("Subject: ");
 		sb.append(subject);
 		sb.append("\r\n");
 		sb.append("From: ");
 		sb.append(formName);
 		sb.append(" <");
 		sb.append(formMail);
 		sb.append(">\r\n");
 		sb.append("To: ");
 		sb.append(genTosField());
 		sb.append("\r\n");
 		sb.append("MIME-Version: 1.0\r\n");
 		sb.append("Content-type: text/html; charset=utf-8\r\n");
 		sb.append("Content-Transfer-Encoding: base64\r\n");
 		if (bcc.size() > 0) {
 			sb.append("Bcc: ");
 			sb.append(genBccsField());
 			sb.append("\r\n");
 		}
 		return sb.toString();
 	}
 
 	private String genTosField() {
 		StringBuilder sb = new StringBuilder();
 		boolean first = true;
 		for (Map<String, String> one : to) {
 			if (first)
 				first = false;
 			else
 				sb.append(", ");
 			Entry<String, String> toInfo = one.entrySet().iterator().next();
 			if (toInfo.getValue() == null) {
 				sb.append(toInfo.getKey());
 				continue;
 			}
 			sb.append(toInfo.getValue());
 			sb.append(" <");
 			sb.append(toInfo.getKey());
 			sb.append(">");
 		}
 		return sb.toString();
 	}
 
 	public int go() throws IOException {
 		if (formMail == null)
 			return -1;
 		if (to.size() == 0)
 			return -2;
 		out.write(("MAIL FROM: <" + formMail + ">\r\n").getBytes());
 		if (new Integer(in.nextLine().substring(0, 3)) != 250)
 			return -3;
 		for (Map<String, String> one : to) {
 			Entry<String, String> toInfo = one.entrySet().iterator().next();
 			out.write(("RCPT TO: <" + toInfo.getKey() + ">\r\n").getBytes());
 			if (new Integer(in.nextLine().substring(0, 3)) != 250)
 				return -4;
 		}
 		for (String email : bcc) {
 			out.write(("RCPT TO: <" + email + ">\r\n").getBytes());
 			if (new Integer(in.nextLine().substring(0, 3)) != 250)
				return -5;
 		}
 		out.write("DATA\r\n".getBytes());
 		if (new Integer(in.nextLine().substring(0, 3)) != 354)
			return -6;
 		out.write(genMailHeader().getBytes());
 		out.write("\r\n".getBytes());
 		out.write(msg.getBytes());
 		out.write("\r\n.\r\n".getBytes());
 		return new Integer(in.nextLine().substring(0, 3));
 	}
 
 	public int sayHelo(String helo) throws IOException {
 		String heloLine = "HELO " + helo + "\r\n";
 		out.write(heloLine.getBytes());
 		out.flush();
 		return new Integer(in.nextLine().substring(0, 3));
 	}
 
 	public void setFrom(String name, String email) {
 		formName = emailEnCode(name);
 		formMail = email;
 	}
 
 	public void setMessage(String msg) {
 		this.msg = Base64.encodeBase64String(msg.getBytes());
 	}
 
 	public void setSubject(String subject) {
 		this.subject = emailEnCode(subject);
 	}
 }
