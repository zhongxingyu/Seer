 /*
  * Copyright (C) 2013 Mohatu.net
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>
  * @author Mohatu
  * @version 2.9
  */
 
 package net.mohatu.bloocoin.miner;
 
 import java.awt.Color;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.Random;
 
 import javax.swing.JOptionPane;
 
 import org.apache.commons.codec.digest.DigestUtils;
 
 public class RegCustom implements Runnable {
 
 	String url = Main.getURL();
 	int port = Main.getPort();
 	String addr = "";
 	String key = "";
 	boolean submitted = false;
 
 	public RegCustom(String addr) {
 		this.addr = addr;
 	}
 
 	@Override
 	public void run() {
 		genData();
 		register();
 	}
 
 	private void genData() {
 		Random r = new Random();
 		key = DigestUtils.sha1Hex(
 				(randomString() + r.nextInt(Integer.MAX_VALUE)).toString())
 				.toString();
 		System.out.println("Addr: " + addr + "\nKey: " + key);
 	}
 
 	private void register() {
 		try {
 			String result = new String();
 			Socket sock = new Socket(this.url, this.port);
 			String command = "{\"cmd\":\"register" + "\",\"addr\":\"" + addr
 					+ "\",\"pwd\":\"" + key + "\"}";
 			DataInputStream is = new DataInputStream(sock.getInputStream());
 			DataOutputStream os = new DataOutputStream(sock.getOutputStream());
 			os.write(command.getBytes());
 			os.flush();
 
 			BufferedReader in = new BufferedReader(new InputStreamReader(is));
 			String inputLine;
 			while ((inputLine = in.readLine()) != null) {
 				result += inputLine;
 			}
 
 			is.close();
 			os.close();
 			sock.close();
 			System.out.println(result);
 			if (result.contains("\"success\": true")) {
 				System.out.println("Registration successful: " + addr);
 				saveBloostamp();
 			} else if (result.contains("\"success\": false")) {
 				System.out.println("Result: Failed");
 				JOptionPane.showMessageDialog(Main.scrollPane,
 						"Registration failed.\nCheck your network connection",
 						"Registration Failed", JOptionPane.ERROR_MESSAGE);
 				System.exit(0);
 			}
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private String randomString() {
 		String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
 		Random r = new Random();
 		int limit = 5;
 		StringBuffer buf = new StringBuffer();
 
 		buf.append(chars.charAt(r.nextInt(26)));
 		for (int i = 0; i < limit; i++) {
 			buf.append(chars.charAt(r.nextInt(chars.length())));
 		}
 		return buf.toString();
 	}
 
 	private void saveBloostamp() {
 		File bloocoinFolder = new File(System.getProperty("user.home")
 				+ "/.bloocoin");
 		if (!bloocoinFolder.exists()) {
 			System.out.println("Creating " + System.getProperty("user.home")
 					+ "/.bloocoin" + " directory");
 			boolean result = bloocoinFolder.mkdir();
 			if (result) {
 				System.out.println("bloocoin folder created");
 			}
 		}
 		try {
 			PrintWriter out = new PrintWriter(new BufferedWriter(
 					new FileWriter(System.getProperty("user.home")
 							+ "/.bloocoin/bloostamp" + addr)));
			out.print(addr + ":" + key);
 			Main.updateStatusText("bloostamp" + addr + " created.", Color.blue);
 			out.close();
 			Main.loadDataPub();
 
 		} catch (IOException e) {
 			System.out.println("Saving failed:");
 			e.printStackTrace();
 		}
 	}
 }
