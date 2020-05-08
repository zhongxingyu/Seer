 package edu.uw.cs.cse461.ConsoleApps;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import edu.uw.cs.cse461.ConsoleApps.DataXferInterface.DataXferTCPMessageHandlerInterface;
 import edu.uw.cs.cse461.Net.Base.NetBase;
 import edu.uw.cs.cse461.Net.Base.NetLoadable.NetLoadableConsoleApp;
 import edu.uw.cs.cse461.Net.TCPMessageHandler.TCPMessageHandler;
 import edu.uw.cs.cse461.util.ConfigManager;
 import edu.uw.cs.cse461.util.Log;
 import edu.uw.cs.cse461.util.SampledStatistic.TransferRate;
 import edu.uw.cs.cse461.util.SampledStatistic.TransferRateInterval;
 
 public class DataXferTCPMessageHandler extends NetLoadableConsoleApp implements DataXferTCPMessageHandlerInterface {
 	private static final String TAG="DataXferTCPMessageHandler";
 
 	// ConsoleApp's must have a constructor taking no arguments
 	public DataXferTCPMessageHandler() throws Exception {
 		super("dataxfertcpmessagehandler", true);
 	}
 
 	/**
 	 * This method is invoked each time the infrastructure is asked to launch this application.
 	 */
 	@Override
 	public void run() {
 		
 		try {
 
 			// Eclipse doesn't support System.console()
 			BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
 
 			ConfigManager config = NetBase.theNetBase().config();
 			String server = config.getProperty("dataxfertcpmessagehandler.server"); 
 			if ( server == null ) {
 				System.out.print("Enter a host ip, or exit to exit: ");
 				server = console.readLine();
 				if ( server == null ) return;
 				if ( server.equals("exit")) return;
 			}
 
 			int port = config.getAsInt("dataxfertcpmessagehandler.port", -1, TAG); 
 			if ( port == -1 ) {
 				System.out.print("Enter port number, or empty line to exit: ");
 				String portStr = console.readLine();
 				if ( portStr == null || portStr.trim().isEmpty() ) return;
 				port = Integer.parseInt(portStr);
 			}
 			
 			int socketTimeout = config.getAsInt("dataxfertcpmessagehandler.sockettimeout", -1, TAG);
 			if ( socketTimeout < 0 ) {
 				System.out.print("Enter socket timeout (in msec.): ");
 				String timeoutStr = console.readLine();
 				socketTimeout = Integer.parseInt(timeoutStr);
 				
 			}
 
 			int nTrials = config.getAsInt("dataxfertcpmessagehandler.ntrials", -1, TAG);
 			if ( nTrials == -1 ) {
 				System.out.print("Enter number of trials: ");
 				String trialStr = console.readLine();
 				nTrials = Integer.parseInt(trialStr);
 			}
 
 			TransferRate.clear();
 			int xferLength = 1000; 
 			System.out.println("\n" + xferLength + " bytes");
 
 			//-----------------------------------------------------
 			// TCP transfer
 			//-----------------------------------------------------
 			
 			TransferRateInterval tcpStats = DataXfer(server, port, socketTimeout, xferLength, nTrials);
 			
 			System.out.println("\nTCP: xfer rate = " + String.format("%9.0f", tcpStats.mean() * 1000.0) + " bytes/sec.");
 			System.out.println("TCP: failure rate = " + String.format("%5.1f", tcpStats.failureRate()) +
 					" [" + tcpStats.nAborted()+ "/" + tcpStats.nTrials() + "]");
 
 						
 		} catch (Exception e) {
 			System.out.println("Unanticipated exception: " + e.getMessage());
 		}
 	}
 	
 	
 	/**
 	 * Performs nTrials trials via TCP of a data xfer to host hostIP on port tcpPort.  Expects to get xferLength
 	 * bytes in total from that host/port.  Is willing to wait up to socketTimeout msec. for new data to arrive.
 	 * @return A TransferRateInterval object that measured the total bytes of data received over all trials and
 	 * the total time taken.  The measured time should include socket creation time.
 	 */
 	@Override
 	public TransferRateInterval DataXfer(String hostIP, int tcpPort, int socketTimeout, int xferLength, int nTrials) {
 		for (int i = 0; i < nTrials; i++) {
 			System.out.println("trial number: " + i);
 			TransferRate.start("tcp");
 			int dataTransferred = 0;
 			Socket socket = null;
 			try {
 				socket = new Socket();
 				socket.setSoTimeout(socketTimeout);
 				//  Attempts to create a TCP socket.  If this fails, aborts the timer and starts it again
 				socket.connect(new InetSocketAddress(hostIP, tcpPort));
 				TCPMessageHandler tcpMessageHandlerSocket = new TCPMessageHandler(socket);
 				JSONObject message = new JSONObject();
 				try {
 					message.append("transferSize", xferLength);
 				} catch (JSONException e1) {
 					Log.i(TAG, "Failed to create the message to send to the TCPMessageHandler");
					e1.printStackTrace(); // TODO get to the finally clause and skip the rest of this.	
 				}
 				tcpMessageHandlerSocket.sendMessage(message);
 				
 				byte[] buf = tcpMessageHandlerSocket.readMessageAsBytes();
 				// No matter what, TCPMessageHandler will be done when it returns, so assume it has handled things correctly.
 				
 				if (buf != null) {
 					dataTransferred += buf.length;
 					if (dataTransferred == xferLength) {
 						// trial successful!
 						TransferRate.stop("tcp", dataTransferred);
 					} else {
 						TransferRate.abort("tcp", dataTransferred);
 						// dataTransferred will be changed here, whereas in the other else branch it will not
 					}
 				} else {
 					TransferRate.abort("tcp", dataTransferred);
 				}
 				tcpMessageHandlerSocket.discard();
 				
 			} catch (IOException e) {
 				e.printStackTrace();
 				TransferRate.abort("tcp", dataTransferred);
 			} finally {
 				try {
 					socket.close();
 					Log.d(TAG, "socket.close succeeded");
 				} catch (IOException e) {
 					e.printStackTrace();
 					Log.i(TAG, "socket.close failed");
 				}
 			}
 			
 		}		
 		
 		return TransferRate.get("tcp");
 	}
 }
