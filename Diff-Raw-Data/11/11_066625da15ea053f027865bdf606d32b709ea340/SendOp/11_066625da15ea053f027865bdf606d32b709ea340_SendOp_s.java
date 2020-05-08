 /**
  * 
  */
 package jLanSend;
 
 //import java.io.BufferedInputStream;
 //import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.ConnectException;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 /**
  * @author Moritz Bellach
  *
  */
 public class SendOp extends TransferOp implements Runnable{
 	
 
 	private int port;
 	
 	public SendOp (File f, String host, int port){
		this.rHostName = host;
 		this.f = f;
 		fname = f.getName();
 		fsize = f.length();
 		this.port = port;
 		bytesDone = 0L;
 		t = new Thread(this);
 	}
 
 	@Override
 	public void run() {
 		try {
 			ins = new DataInputStream(new FileInputStream(f.getAbsolutePath()));
 			s = new Socket(rHostName, port);
 			in = new BufferedReader(new InputStreamReader(s.getInputStream()));
 			out = new PrintWriter(s.getOutputStream(), true);
 			outs = new DataOutputStream(s.getOutputStream());
 			
 			// say we really are a JLanSend and not something else going around knocking on heavens ports
 			out.println("JLanSend");
 			// Version
 			out.println(Integer.toString(JLanSend.getJLanSend().getLProtoV()));
 			String rproto = in.readLine();
 			// same version
 			if(rproto.equals("ok")) {
 				// send nick
 				out.println(JLanSend.getJLanSend().getNick());
 				// send file name
 				out.println(f.getName());
 				// send file size
 				out.println(Long.toString(f.length()));
 				
 				// rUser has to accept
 				String cmd = in.readLine();
 				if(cmd.equals("goahead")) {
 					notifyObservers(ObsMsg.SENDSTART);
 					byte [] buffer = new byte[1024*1024];
 					int i;
 					while((i = ins.read(buffer, 0, 1024*1024)) != -1) {
 						outs.write(buffer, 0, i);
 						updateProgress(i);
 						notifyObservers(ObsMsg.SENDPROGRESS);
 					}
 					closeNet();
 					notifyObservers(ObsMsg.SENDDONE);
 				}
 				else {
 					closeNet();
 					notifyObservers(ObsMsg.REMOVEME);
 				}
 			}
 			else if (Integer.parseInt(rproto) < JLanSend.getJLanSend().getLProtoV()){
 				// switch to compatibility mode in later versions. for now just
 				closeNet();
 				notifyObservers(ObsMsg.REMOVEME);
 			}
 			else {
 				System.out.println("oO");
 			}
 		} catch (FileNotFoundException e) {
 			notifyObservers(ObsMsg.REMOVEME);
 		} catch (UnknownHostException e) {
 			notifyObservers(ObsMsg.REMOVEME);
 			e.printStackTrace();
 		} catch (ConnectException e) {
 			notifyObservers(ObsMsg.REMOVEME);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			notifyObservers(ObsMsg.REMOVEME);
 		}
 		
 	}
 
 }
