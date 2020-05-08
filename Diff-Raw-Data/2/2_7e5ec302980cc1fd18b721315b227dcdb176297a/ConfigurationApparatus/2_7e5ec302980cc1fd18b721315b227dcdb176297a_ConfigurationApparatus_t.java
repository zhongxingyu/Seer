 package se.bthstudent.sis.afk.GLaDOS;
 
 import java.io.*;
 
 public class ConfigurationApparatus {
 
 	private String[] channels;
 	private TestSubject[] admins;
 	private String password;
 	private String server;
 
 	public ConfigurationApparatus() {
 		this.channels = new String[0];
 		this.admins = new TestSubject[0];
 		this.password = "gasegvioaejrgioajwetg4iojhawergjaweiprgjse89gjaejkrgawjg8jawopjhmae0gb";
 		this.server = "";
 	}
 
 	public void readConfig() {
 		String location = ".GLaDOS.conf";
 
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(location));
 
 			String read = in.readLine();
 			while (read != null) {
 				if (!read.equals("")) {
 					String[] parts = read.split(" ");
 
 					if (parts[0].equals("channels:")) {
 						this.channels = new String[parts.length - 1];
 						System.arraycopy(parts, 1, this.channels, 0,
 								this.channels.length);
 					} else if (parts[0].equals("password:")) {
 						this.password = parts[1];
 					} else if (parts[0].equals("admins:")) {
 						this.admins = new TestSubject[parts.length - 1];
 						for (int i = 0; i < this.admins.length; i++) {
 							String[] temp = { parts[i + 1] };
 							this.admins[i] = new TestSubject(temp[0], temp,
 									TestSubject.Mode.NONE, true);
 						}
 					} else if (parts[0].equals("server:")) {
 						this.server = parts[1];
 					}
 				}
 
 				read = in.readLine();
 			}
 
 		} catch (IOException e) {
 			System.out
					.println(".GLaDOS.conf doesn't exist, please create before running GLaDOS");
 			System.exit(0);
 		}
 	}
 
 	public String[] getChannels() {
 		return this.channels;
 	}
 
 	public TestSubject[] getAdmins() {
 		return this.admins;
 	}
 
 	public String getPassword() {
 		return this.password;
 	}
 
 	public String getServer() {
 		return this.server;
 	}
 }
