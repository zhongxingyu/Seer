 package org.cloudsicle.main.entrypoints;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 
 import org.cloudsicle.communication.IMessageHandler;
 import org.cloudsicle.communication.SocketListener;
 import org.cloudsicle.communication.SocketSender;
 import org.cloudsicle.main.jobs.IJob;
 import org.cloudsicle.messages.Activity;
 import org.cloudsicle.messages.IMessage;
 import org.cloudsicle.messages.SoftExit;
 import org.cloudsicle.messages.StatusUpdate;
 
 import com.jcraft.jsch.JSchException;
 
 public class Slave implements IMessageHandler{
 
 	private SocketListener listener;
 	
 	/**
 	 * Initialize our Slave
 	 * 
 	 * @throws IOException If we failed to deploy gifsicle on the environment
 	 */
 	public Slave() throws IOException{
 		deployExecutable();
		listener = new SocketListener(this);
 		listener.start();
 	}
 	
 	/**
 	 * Copy our gifsicle executable from the jar to the environment
 	 */
 	private void deployExecutable() throws IOException{
 		InputStream is = Slave.class.getResourceAsStream(File.separator + "gifsicle");
 		File f = new File("gifsicle");
 		f.createNewFile();
 		FileOutputStream fos = new FileOutputStream("gifsicle");
 		while (is.available() > 0)
 			fos.write(is.read());
 		fos.flush();
 		fos.close();
 		is.close();
 	}
 	
 	
 	
 	@Override
 	public void process(IMessage message) {
 		if (message instanceof Activity){
 			try {
 			StatusUpdate status = new StatusUpdate("VM Received Activity");
 			SocketSender sender = new SocketSender(((Activity) message).getIP());		
 			
 			//ArrayList<IJob> jobs = ((Activity) message).getJobs();
 			
 				sender.send(status);
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (JSchException e) {
 				e.printStackTrace();
 			}
 		} else if (message instanceof SoftExit){
 			// TODO
 		}
 	}
 
 	/**
 	 * @param args
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) throws IOException {
 		Slave slave = new Slave();
 	}
 
 }
