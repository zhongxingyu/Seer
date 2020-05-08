 package at.sti2.spark.streamer;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.util.Date;
 
 import at.sti2.spark.streamer.file.NTripleStreamReader;
 
 public class SparkStreamer {
 
 	private String triplesFileName = null;
 	
 	private int port = 0;
 	private Socket sock = null;
 	
 //	private AFUNIXSocket sock = null;
 //	private String unixDomainSocketPath = null;
 	
 	
 	/**
 	 * UNIX Domain Socket constructor
 	 * @param unixDomainSocketPath
 	 * @param triplesFileName
 	 */
 //	public SparkStreamer(String unixDomainSocketPath, String triplesFileName) {
 //		this.unixDomainSocketPath = unixDomainSocketPath;
 //		this.triplesFileName = triplesFileName;
 //		
 //		//Connect to the socket
 //		connect();
 //		
 //		//Stream file
 //		stream();
 //		
 //	}
 	
 	/**
 	 * TCP/IP constructor
 	 * @param unixDomainSocketPath
 	 * @param triplesFileName
 	 */
 	public SparkStreamer(String port, String triplesFileName) {
 		this.port = Integer.parseInt(port);
 		this.triplesFileName = triplesFileName;
 		
 		//Connect to the socket
 		connect();
 		
 		//Stream file
 		stream();
 		
 	}
 	
 	private void connect(){
 		
 		try {
 			sock = new Socket ("localhost", port);
 		} catch (IOException e) {
 			System.out.println("Cannot connect to server.");
             System.out.flush();
             System.exit(1);
 		}
 		System.out.println("Connected.");
 	}
 	
 	private void stream(){
 		
 		long tripleCounter = 0;
 		long timepoint = (new Date()).getTime();
 		
 		try {
 			PrintWriter streamWriter = new PrintWriter(sock.getOutputStream());
 		
 			//Read file, create objects and stream them
 			NTripleStreamReader streamReader = new NTripleStreamReader(triplesFileName);
 			
 			streamReader.openFile();
 			
 			String tripleLine = null;
 			
 			System.out.println("Beginning of streaming.");
 			
 			Date startStreaming = new Date();
 			
 			while ((tripleLine = streamReader.nextTripleLine()) != null){
 				
 				streamWriter.println(tripleLine);
 				tripleCounter++;
 				
 //				if (tripleCounter%1000 == 0){
 //					long currentTimepoint = (new Date()).getTime();
 //					System.out.println("Processing " + (1000/(currentTimepoint - timepoint)) + " triples/sec.");
 //					timepoint = currentTimepoint;
 //					streamWriter.flush();
 //				}
 			}
 			
 			streamWriter.flush();
 			
 			Date endStreaming = new Date();
 			
 			System.out.println("End of streaming.");
 			System.out.println("Streamed " + tripleCounter + " triples.");
 			System.out.println("Total streaming time " + (endStreaming.getTime() - startStreaming.getTime()) + " ms.");
 			
 			
 			streamReader.closeFile();
 			
 			streamWriter.close();
 	        sock.close();
 	        
 	        System.out.println("Disconnected.");
 	        
 		} catch (IOException e) {
             e.printStackTrace();
             System.exit(1);
 		}
 	}
 	
 	public static void main(String args[]){
 		if (args.length != 2){
			System.out.println("SparkStreamer sends a stream of triples to a localhost at the designated port. The streamer expects to receive following arguments:");
			System.out.println(" <port> - the local port at which Sparkweave instance listens for upcoming triples.");
			System.out.println(" <triple_file_name> - name of the file in N-TRIPLES format holding triples to be streamed.");
 			System.exit(0);
 		}
 		
 		new SparkStreamer(args[0], args[1]);
 	}
 	
 }
