 package com.domotica.jarviseSSH;
 
 import java.io.IOException;
 import java.net.ProtocolException;
 import java.net.Socket;
 import com.jcraft.jsch.JSch;
 import com.jcraft.jsch.JSchException;
 import com.jcraft.jsch.Session;
 import com.jcraft.jsch.UserInfo;
 import android.annotation.SuppressLint;
 import android.annotation.TargetApi;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.StrictMode;
 
 
 
 /**
  * @author raelix
  *
  */
 @SuppressLint("NewApi")
 public class MultiThread{
 	private static String nameFile = "jarvise.txt";
 	Socket sock;
 	DataSocket btsock;
 	int port;
 	String dest;
 	Pacco pkt;
 	String returned;
 	private String host;
 	private String user;
 	private String password;
 	Session session ;
 	Configuration readFile;
 	
 	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
 	@SuppressLint("NewApi")
 	@SuppressWarnings("unchecked")
 	public MultiThread(String dest, int port,Pacco pkt){
 		this.readFile = new Configuration(nameFile);
 		this.host = readFile.getHost();
 		this.user = readFile.getUser();
 		this.password = readFile.getPass();
 		this.session = null;
 		this.dest = dest;
 		this.port = port;
 		this.pkt = pkt;
 		new SSHConnection().execute();
 		new Connection().execute();
 	};
 
 
 	@SuppressWarnings("rawtypes")
 	private class Connection extends AsyncTask {
 
 		@Override
 		protected Object doInBackground(Object... arg0) {
 			// TODO Auto-generated method stub
 			connect();
 			return null;
 		}
 		@SuppressWarnings("unchecked")
 		private void connect(){
 			try {
 				sock = new Socket(dest,port);
 				System.out.println("Connect Thread:  Inizio");
 				btsock = new DataSocket(sock.getInputStream(),sock.getOutputStream());
 
 			} catch (IOException e) {
 				System.out.println("Connect Thread: Errore");
 				e.printStackTrace();
 			}
 			System.out.println("Connect Thread: Scrivo pacco di riconoscimento");
 			btsock.writePkt(new PaccoStart());
 			System.out.println("Connect Thread: Adesso spedisco le informazioni");
 			btsock.writePkt(pkt);
 			new ConnectionServer().execute();
 		};
 	};
 
 
 
 	@SuppressWarnings({ "rawtypes" })
 	private class ConnectionServer extends AsyncTask {
 
 		@Override
 		protected Object doInBackground(Object... arg0) {
 
 			connected();
 			return null;
 		}
 		public void connected(){
 			Pacco p = btsock.readPkt();
 			if (p == null || p.getType() != PROTOCOL_CONSTANTS.PACKET_TYPE_STRING){
 				this.close();
 				return;
 			}
 			else if(p.getType() == PROTOCOL_CONSTANTS.PACKET_TYPE_STRING){
 				try {
 					System.out.println("Ricevuta Stringa di ritorno dal Server: "+new PaccoString(p).getString());
 					MainActivity.say(new PaccoString(p).getString());
 					this.close();
 				} catch (ProtocolException e) {
 					e.printStackTrace();
 				}
 			}
 		};
 
 		private void close(){
 			System.out.println("Ricezione: Finito");
 			btsock.close();
 			try {
 				sock.close();
 				session.disconnect();
 				System.out.println("Chiuso");
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	};
 
 	@SuppressWarnings("rawtypes")
 	private class SSHConnection extends AsyncTask {
 
 		@Override
 		protected Object doInBackground(Object... arg0) {
 			execute();
 			return null;
 		}
 
 
 		@SuppressLint("NewApi")
 		private void execute(){
 			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
 			StrictMode.setThreadPolicy(policy); 
 		
 			String rhost="127.0.0.1";
 			int rport=20;	
 			String lhost="127.0.0.1";
 			int lport=9001;      
 			
 			JSch jsch=new JSch(); 
 			try {
 				session=jsch.getSession(user, host, 22);
 				session.setPassword(password.getBytes());
 				session.setConfig("StrictHostKeyChecking", "no");   // Avoid asking for key confirmation
 				session.connect();
 				System.out.println("connesso? "+session.isConnected());
 				int assinged_port = session.setPortForwardingL(lhost,lport, rhost, rport);
 				System.out.println("localhost:"+assinged_port+" -> "+rhost+":"+rport);
 			} catch (JSchException e) {
 				System.out.println("errore");
 				e.printStackTrace();
 			}
 		};
 
 		@SuppressWarnings("unused")
 		class SSHUserInfo implements UserInfo {  
 			private String password;  
 
 			SSHUserInfo(String password) {  
 				this.password = password;  
 			}  
 
 			public String getPassphrase() {  
 				return null;  
 			}  
 
 			public String getPassword() {  
 				return password;  
 			}  
 
 			public boolean promptPassword(String arg0) {  
 				return true;  
 			}  
 
 			public boolean promptPassphrase(String arg0) {  
 				return true;  
 			}  
 
 			public boolean promptYesNo(String arg0) {  
 				return true;  
 			}  
 
 			public void showMessage(String arg0) {  
 				System.out.println(arg0);  
 			}  
 		}  
 	}
 
 }
 
