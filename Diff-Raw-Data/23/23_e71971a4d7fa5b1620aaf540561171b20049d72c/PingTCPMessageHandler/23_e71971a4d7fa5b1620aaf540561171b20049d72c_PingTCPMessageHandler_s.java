 package edu.uw.cs.cse461.consoleapps.solution;
 
 //import edu.uw.cs.cse461.consoleapps.PingInterface.PingRawInterface;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.net.UnknownHostException;
 
 import edu.uw.cs.cse461.net.base.NetBase;
 import edu.uw.cs.cse461.net.base.NetLoadable.NetLoadableConsoleApp;
 import edu.uw.cs.cse461.net.tcpmessagehandler.TCPMessageHandler;
 import edu.uw.cs.cse461.service.EchoServiceBase;
 import edu.uw.cs.cse461.util.ConfigManager;
 import edu.uw.cs.cse461.util.SampledStatistic.ElapsedTime;
 import edu.uw.cs.cse461.util.SampledStatistic.ElapsedTimeInterval;
 import edu.uw.cs.cse461.consoleapps.PingInterface;
 
 
 public class PingTCPMessageHandler extends NetLoadableConsoleApp implements PingInterface{
 	public PingTCPMessageHandler() {
 		super("pingtcpmessagehandler");
		// TODO Auto-generated constructor stub
 	}
 
 	public ElapsedTimeInterval ping(String header, String hostIP, int port, int timeout, int nTrials) throws Exception{
 		Socket tcpSocket;
 		byte[] msg = new byte[0];
 		try {
 			for (int i = 0; i < nTrials; i++){
 				tcpSocket = new Socket(hostIP, port);
 				tcpSocket.setSoTimeout(timeout);
 				TCPMessageHandler tcpHandler = new TCPMessageHandler(tcpSocket);
 				tcpHandler.setNoDelay(true);
 
 				try {
 					ElapsedTime.start("Ping_TCPTotalDelay");
 					// send header
 					tcpHandler.sendMessage(header);
 
 					// send message
 					tcpHandler.sendMessage(msg);
 					tcpSocket.shutdownOutput();
 					
 					// read the header
 					String headerStr = tcpHandler.readMessageAsString();
 					if ( headerStr.length() != EchoServiceBase.RESPONSE_OKAY_STR.length() ){
 						ElapsedTime.abort("Ping_TCPTotalDelay");
 						System.out.println("Bad response header length: got " + headerStr.length() + " but expected " + EchoServiceBase.RESPONSE_OKAY_STR.length());
 					} else if ( !headerStr.equalsIgnoreCase(EchoServiceBase.RESPONSE_OKAY_STR)){
 						ElapsedTime.abort("Ping_TCPTotalDelay");
 						System.out.println("Bad response header: got '" + headerStr + "' but expected '" + EchoServiceBase.RESPONSE_OKAY_STR + "'");
 					} else {
 						// read message
 						byte[] readBuf = tcpHandler.readMessageAsBytes();
 						if(readBuf.length != 0){
 							ElapsedTime.abort("Ping_TCPTotalDelay");
 							System.out.println("Bad response payload: " + new String(readBuf));
 						}
 						ElapsedTime.stop("Ping_TCPTotalDelay");
 					}
 					
 				} catch (SocketTimeoutException e) {
 					// This exception is thrown if we wait on receive() longer
 					// than the timeout
 					ElapsedTime.abort("Ping_TCPTotalDelay");
 				} catch (IOException e) {
					// TODO Auto-generated catch block
 					ElapsedTime.abort("Ping_TCPTotalDelay");
					//e.printStackTrace();
 				} catch (Exception e) {
					// TODO Auto-generated catch block
 					ElapsedTime.abort("Ping_TCPTotalDelay");
					//e.printStackTrace();
 				}
				tcpSocket.close();
				tcpSocket = null;
 			}
 			
 		} catch (SocketException e) {
 			// This exception is thrown if we wait on receive() longer
 			// than the timeout
 			ElapsedTime.abort("Ping_TCPTotalDelay");
 			System.out.println("TCP socket timeout");
 		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
 			e.printStackTrace();
 			ElapsedTime.abort("Ping_TCPTotalDelay");
 		} catch (IOException e) {
			// TODO Auto-generated catch block
 			e.printStackTrace();
 			ElapsedTime.abort("Ping_TCPTotalDelay");
 		} catch (Exception e) {
			// TODO Auto-generated catch block
 			e.printStackTrace();
 			ElapsedTime.abort("Ping_TCPTotalDelay");
 		}
 		
 		return ElapsedTime.get("Ping_TCPTotalDelay");
 	}
 	
 	@Override
 	public void run(){
 		try {
 			// Eclipse doesn't support System.console()
 			BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
 			ConfigManager config = NetBase.theNetBase().config();
 
 			try {
 				String targetIP = config.getProperty("net.server.ip");
 				if ( targetIP == null ) {
 					System.out.println("No net.server.ip entry in config file.");
 					System.out.print("Enter the server's ip, or empty line to exit: ");
 					targetIP = console.readLine();
 					if ( targetIP == null || targetIP.trim().isEmpty() ) return;
 				}
 
 				int targetTCPPort;
 				System.out.print("Enter the server's TCP port, or empty line to skip: ");
 				String targetTCPPortStr = console.readLine();
 				if ( targetTCPPortStr == null || targetTCPPortStr.trim().isEmpty() ) targetTCPPort = 0;
 				else targetTCPPort = Integer.parseInt(targetTCPPortStr);
 
 				System.out.print("Enter number of trials: ");
 				String trialStr = console.readLine();
 				int nTrials = Integer.parseInt(trialStr);
 
 				int socketTimeout = config.getAsInt("net.timeout.socket", 5000);
 				
 				System.out.println("Host: " + targetIP);
 				System.out.println("tcp port: " + targetTCPPort);
 				System.out.println("trials: " + nTrials);
 
 				ElapsedTimeInterval tcpResult = null;
 
 				if ( targetTCPPort != 0 ) {
 					ElapsedTime.clear();
 					tcpResult = ping(EchoServiceBase.HEADER_STR, targetIP, targetTCPPort, socketTimeout, nTrials);
 				}
 				if ( tcpResult != null ) System.out.println("TCP: " + String.format("%.2f msec (%d failures)", tcpResult.mean(), tcpResult.nAborted()));
 
 			} catch (Exception e) {
 				System.out.println("Exception: " + e.getMessage());
 			} 
 		} catch (Exception e) {
 			System.out.println("PingTCPMessageHandler.run() caught exception: " +e.getMessage());
 		}
 	}
 }
