 import java.io.*;
 import java.net.*;
 
 
 class RequestThread extends Thread {
 	Socket requestSocket;
 	DataOutputStream out;
 	DataInputStream in;
 
 	static final int SIZE = 67108864;
 	//static final int SIZE = 268435456;
 	byte[] buffer = new byte[SIZE];
 
	String host;
	int numRequests;
	int threadid;

	RequestThread(String host, int numRequests, int threadid)
	{
		this.host = host;
		this.numRequests = numRequests;
		this.threadid = threadid;
	}

 	void connect(String host)
 	{
 		try {
 			requestSocket = new Socket(host, 8002);
 			//System.out.println("Connected to " + host);
 			out = new DataOutputStream(requestSocket.getOutputStream());
 			in = new DataInputStream(requestSocket.getInputStream());
 			out.flush();
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 	}
 
	public void run()
 	{
 		for(int i=0; i<numRequests; i++) {
 			boolean worked = false;
 			while(!worked) {
 				long start_time = System.nanoTime();
 				int rv = makeRequest(host);
 				if(rv >= 0) {
 					long end_time = System.nanoTime();
 					double request_mbs = rv / Math.pow(2,20);
 					double diff_secs = (double)(end_time - start_time) / (double)1000000000;
 					double rate = request_mbs / diff_secs;
 
					System.out.println("Thread " + threadid + " Rate: " + rate);
 					worked = true;
 				}
 			}
 		}
 
 	}
 
 	int makeRequest(String host)
 	{
 		int bytes_read = 0;
 		try {
 			int available = 0;
 			long start_time = 0;
 			String request = "Hello server!";
 
 
 			connect(host);
 			//3: Communicating with the server
 			sendMessage(request.getBytes());
 			available = in.available();
 			if(available > SIZE) {
 				available = SIZE;
 			}
 
 			while(bytes_read < SIZE) {
 				try {
 					bytes_read += in.read(buffer, bytes_read, SIZE-bytes_read);
 				} catch(EOFException eof) {
 					System.out.println("EOF");
 					System.exit(-1);
 				} catch(SocketException se) {
 					System.out.println("ERROR: short read " + bytes_read + 
 							" of expected " + SIZE + " bytes!");
 					se.printStackTrace();
 					return -1;
 				}
 			}
 		}
 		catch(UnknownHostException unknownHost) {
 			System.err.println("You are trying to connect to an unknown host!");
 		}
 		catch(IOException ioException) {
 			ioException.printStackTrace();
 		}
 		finally {
 			//4: Closing connection
 			try{
 				in.close();
 				out.close();
 				requestSocket.close();
 			}
 			catch(IOException ioException){
 				ioException.printStackTrace();
 			}
 		}
 
 		return bytes_read;
 	}
 	void sendMessage(byte[] msg)
 	{
 		try{
 			out.write(msg, 0, msg.length);
 			out.flush();
 		}
 		catch(IOException ioException){
 			ioException.printStackTrace();
 		}
 	}
 }
 
 public class Requester {
 
 	Requester()
 	{
 	}
 
 	public static void main(String args[])
 	{
 		String host = args[0];
 		int numRequests = Integer.parseInt(args[1]);
 		int numThreads = Integer.parseInt(args[2]);
 
 		int requestsPerThread = numRequests / numThreads;
 
		System.out.println("Number of threads: " + numThreads);

 		RequestThread[] threads = new RequestThread[numThreads];
 		for(int i=0; i<threads.length; i++) {
			threads[i] = new RequestThread(args[0], requestsPerThread, i);
 		}
 		for(int i=0; i<threads.length; i++) {
			threads[i].start();
 		}
 	}
 
 }
