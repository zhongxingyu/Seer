 package plugin.htcondor;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import vcluster.engine.groupexecutor.ProxyExecutor;
 import vcluster.global.Config;
 
 public class HTCondor implements ProxyExecutor{
 
 	
 
 	public boolean check_pool() 
 	{
         Socket socket = null;
         BufferedReader in = null;
         DataOutputStream out = null;
         
         String cmdLine = "condor_status";
 
         try {
         	socket = new Socket(Config.CONDOR_IPADDR, Config.PORTNUM);
         	
             in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             out = new DataOutputStream(socket.getOutputStream());
             out.flush();
             /* make an integer to unsigned int */
             int userInput = 16;
             userInput <<= 8;
             userInput |=  1;
             userInput &= 0x7FFFFFFF;
 
             /*
              * easy and simple way is using writeInt() function, 
              * but writing an integer to socket through writeInt() 
              * causes "connection reset" problem because an additional data being
              * transmitted.
              * 
              * In order to resolve this problem, we use write() function after
              * converting the integer to byte[].
              */
             String s = Integer.toString(userInput);
             byte[] b = s.getBytes();
             
             out.write(b, 0, b.length);
             out.write(cmdLine.getBytes(), 0, cmdLine.getBytes().length);
 
             char[] cbuf = new char[1024];
             
             while (in.read(cbuf, 0, 1024) != -1) {
             	String str = new String(cbuf);
     	        str = str.trim();
     	        
     	        if (str.contains("Total") && !str.contains("Owner")) {
     	        	PoolStatus.extractInfo(str);
     	        	PoolStatus.printQStatus();
     	        }
     	        
     	        for(int i = 0; i< 1024; i++)
     	        	cbuf[i] = '\0';
             }
         } catch (UnknownHostException e) {
     		System.out.print("ERROR: " + e.getMessage());
             closeStream(in, out, socket);    		
     		return false;
         } catch (IOException e) {
     		System.out.print("ERROR: " + e.getMessage());
             closeStream(in, out, socket);
             
             return false;
         }
 
         
         closeStream(in, out, socket);
  		
 		return true;
 	}
 	
 	private static void closeStream(BufferedReader in, DataOutputStream out, Socket socket)
 	{
 		try {
 	        if (in != null) in.close();
 	        if (out != null) out.close();
 	        if (socket != null) socket.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 	public boolean check_q() 
 	{
 		
 		String cmdLine = "condor_q";
 		
         Socket socket = null;
         BufferedReader in = null;
         DataOutputStream out = null;
 
         try {
         	socket = new Socket(Config.CONDOR_IPADDR, Config.PORTNUM);
         	
             in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             out = new DataOutputStream(socket.getOutputStream());
             out.flush();
             /* make an integer to unsigned int */
             int userInput = 16;
             userInput <<= 8;
             userInput |=  1;
             userInput &= 0x7FFFFFFF;
 
             /*
              * easy and simple way is using writeInt() function, 
              * but writing an integer to socket through writeInt() 
              * causes "connection reset" problem because an additional data being
              * transmitted.
              * 
              * In order to resolve this problem, we use write() function after
              * converting the integer to byte[].
              */
             String s = Integer.toString(userInput);
             byte[] b = s.getBytes();
             
             out.write(b, 0, b.length);
             out.write(cmdLine.getBytes(), 0, cmdLine.getBytes().length);
 
             char[] cbuf = new char[1024];
             
             while (in.read(cbuf, 0, 1024) != -1) {
             	String str = new String(cbuf);
     	        str = str.trim();
     	        
     	        if (str.contains("jobs")) {
     	        	QStatus.extractInfo(str);
     	        	QStatus.printQStatus();
     	        }
     	        
     	        for(int i = 0; i< 1024; i++)
     	        	cbuf[i] = '\0';
             	
             }
             closeStream(in, out, socket);
             
         } catch (UnknownHostException e) {
     		System.out.print("ERROR: " + e.getMessage());
             closeStream(in, out, socket);
         } catch (IOException e) {
     		System.out.print("ERROR: " +e.getMessage());
             closeStream(in, out, socket);
         }
 
         closeStream(in, out, socket);
  		return true;
 	}
 
 	public boolean condor(String cmdLine) 
 	{
         Socket socket = null;
         BufferedReader in = null;
         DataOutputStream out = null;
 
         try {
         	socket = new Socket(Config.CONDOR_IPADDR, Config.PORTNUM);
         	
             in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             out = new DataOutputStream(socket.getOutputStream());
             out.flush();
             
             /* make an integer to unsigned int */
             int userInput = 16;
             userInput <<= 8;
             userInput |=  1;
             userInput &= 0x7FFFFFFF;
 
             /*
              * easy and simple way is using writeInt() function, 
              * but writing an integer to socket through writeInt() 
              * causes "connection reset" problem because an additional data being
              * transmitted.
              * 
              * In order to resolve this problem, we use write() function after
              * converting the integer to byte[].
              */
             String s = Integer.toString(userInput);
             byte[] b = s.getBytes();
             
             out.write(b, 0, b.length);
             out.write(cmdLine.getBytes(), 0, cmdLine.getBytes().length);
 
             char[] cbuf = new char[1024];
             
         	String temp = null;
             while (in.read(cbuf, 0, 1024) != -1) {
             	String str = new String(cbuf);
     	        str = str.trim();
     	        
     	        if (!str.equals(temp))
     	        	System.out.println(str);
     	        
     	        for(int i = 0; i< 1024; i++)
     	        	cbuf[i] = '\0';
     	        
             	temp = str;
             }
         } catch (UnknownHostException e) {
     		System.out.print("ERROR: " +e.getMessage());
             closeStream(in, out, socket);
         } catch (IOException e) {
     		System.out.print("ERROR: " +e.getMessage());
             closeStream(in, out, socket);
         }
 
         closeStream(in, out, socket);
 		
 		return true;
 	}
 
 	
 	public boolean onevm(String cmdLine) 
 	{
 		
         Socket socket = null;
         BufferedReader in = null;
         DataOutputStream out = null;
 
         try {
         	socket = new Socket(Config.ONE_IPADDR, Config.PORTNUM);
         	
             in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
             out = new DataOutputStream(socket.getOutputStream());
             out.flush();
             /* make an integer to unsigned int */
             int userInput = 5;
             userInput <<= 8;
             userInput |=  1;
             userInput &= 0x7FFFFFFF;
 
             /*
              * easy and simple way is using writeInt() function, 
              * but writing an integer to socket through writeInt() 
              * causes "connection reset" problem because of an additional data being
              * transmitted.
              * 
              * In order to resolve this problem, we use write() function after
              * converting the integer to byte[].
              */
             String s = Integer.toString(userInput);
             byte[] b = s.getBytes();
             
             out.write(b, 0, b.length);
             out.write(cmdLine.getBytes(), 0, cmdLine.getBytes().length);
             
             char[] cbuf = new char[1024];
         	String temp = null;
 
         	while (in.read(cbuf, 0, 1024) != -1) {
             	String str = new String(cbuf);
     	        str = str.trim();
     	        
     	        if (!str.equals(temp))
     	        	System.out.println(str);
             	cbuf[0] = '\0';
             	temp = str;
             }
             
         } catch (UnknownHostException e) {
     		System.out.print("ERROR: " +e.getMessage());
             closeStream(in, out, socket);
         } catch (IOException e) {
     		System.out.print("ERROR: " +e.getMessage());
             closeStream(in, out, socket);
         }
         
         closeStream(in, out, socket);
 		
 		return true;
 	}
 
 	@Override
 	public int getTotalJob() {
 		// TODO Auto-generated method stub
 		return QStatus.getTotalJob();
 	}
 
 	@Override
 	public int getIdleJob() {
 		// TODO Auto-generated method stub
 		return QStatus.getIdleJob();
 	}
 
 	@Override
 	public int getRunningJob() {
 		// TODO Auto-generated method stub
 		return QStatus.getRunningJob();
 	}
 
 	@Override
 	public int getHeldJob() {
 		// TODO Auto-generated method stub
 		return QStatus.getHeldJob();
 	}
 
 	@Override
 	public double getRatio() {
 		// TODO Auto-generated method stub
 		return QStatus.getRatio();
 	}

	@Override
	public String getInfo() {
		// TODO Auto-generated method stub
		return null;
	}
 	
 
 }
