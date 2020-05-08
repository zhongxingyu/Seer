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
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import javax.swing.JOptionPane;
 
 /**
  * @author Moritz Bellach
  *
  */
 public class ReceiveOp extends TransferOp implements Runnable{
 	
 
 	
 	/**
 	 * 
 	 * @param s
 	 * @throws IOException 
 	 */
 	public ReceiveOp (Socket s) throws IOException {
 		this.s = s;
 		bytesDone = 0L;
 		t = new Thread(this);
 	}
 	
 	
 
 	@Override
 	public void run() {
 		
 		rHostName = s.getInetAddress().getHostAddress();
 		
 		try {
 			
 			in = new BufferedReader(new InputStreamReader(s.getInputStream()));
 			out = new PrintWriter(s.getOutputStream(), true);
 			ins = new DataInputStream(s.getInputStream());
 			
 			// is there a JLanSend talking to me or something else?
 			if(! (in.readLine().equals("JLanSend"))) {
 				closeNet();
 			}
 			//System.out.println("got a JLanSend");
 			// check protocoll version
 			int rprotov = Integer.parseInt(in.readLine());
 			if(rprotov == JLanSend.getJLanSend().getLProtoV()) {
 				out.println("ok");
 				//System.out.println("sent ok");
 			}
 			else if(rprotov > JLanSend.getJLanSend().getLProtoV()) {
 				out.println(Integer.toString(JLanSend.getJLanSend().getLProtoV()));
 			}
 			else {
 				// switch to compatibility mode in later versions and
 				// out.println("ok");
 				// for now just
 				closeNet();
 				notifyObservers(ObsMsg.REMOVEME);
 			}
 			
 			
 			String cmd = in.readLine();
 			if(cmd.equals("detect")) {
 				// autodetection only, just return nickname
 				out.println(JLanSend.getJLanSend().getNick());
 				//System.out.println("detected by " + s.getInetAddress().getHostAddress());
 				closeNet();
 				notifyObservers(ObsMsg.REMOVEME);
 			}
 			else {
 				// getting a file
 				
 				
 				rnick = cmd;
 				fname = in.readLine();
 				fsize = Long.parseLong(in.readLine());
 				
 				
 				
 				//JOptionPane jop = new JOptionPane(rnick + "@" + rHostName + "wants to send you " + fname + " (" + fsize + "bytes). Receive this file?", JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
 				int jop = JOptionPane.showConfirmDialog(null, rnick + "@" + rHostName + " wants to send you " + fname + " (" + JLanSend.getJLanSend().niceBytes(fsize) + "). Receive this file?", "Receive file?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
 				if(jop == JOptionPane.YES_OPTION) {
 					notifyObservers(ObsMsg.RECVSTART);
 					out.println("goahead");
 					// get data
					f = new File(JLanSend.getJLanSend().getDownloaddir() + System.getProperty("file.seperator") + fname);
 					outs = new DataOutputStream(new FileOutputStream(f));
 					byte [] buffer = new byte[1024*1024];
 					
 					int i;
 					while((i = ins.read(buffer, 0, 1024*1024)) != -1) {
 						outs.write(buffer, 0, i);
 						updateProgress(i);
 						notifyObservers(ObsMsg.RECVPROGRESS);
 					}
 					closeNet();
 					notifyObservers(ObsMsg.RECVDONE);
 				}
 				else {
 					out.println("denied");
 					closeNet();
 					notifyObservers(ObsMsg.REMOVEME);
 				}
 			}
 			
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 	}
 
 }
