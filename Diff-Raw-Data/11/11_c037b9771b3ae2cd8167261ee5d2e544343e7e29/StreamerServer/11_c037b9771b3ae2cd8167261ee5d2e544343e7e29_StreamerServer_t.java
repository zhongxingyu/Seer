 package org.apache.hadoop.mapred.lib.stream;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.net.UnknownHostException;
 import java.sql.SQLException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class StreamerServer {
 
 	public static final int QUEUE_SIZE = 10 * 100;
 	public static final long WAIT_THRESHOLD = 1000; // in milliseconds
	public static double SPEEDUP_FACTOR = 100.0;
 	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
 	
 	private int port;
 	private int rows;
 	private int SOCKET_TIMEOUT = 30 * 60 * 1000;
 	
 	private ThreadPoolExecutor tp;
 	private ServerSocket ss;
 	
 	private boolean isShutdown;
 	
 	private final BlockingQueue<String> queue;
 	
 	private long initialVirtualTime = -1;
 	private long initialTime = -1;
 	private boolean timeSet;
 	
 	private Socket textOutSocket;
 	
 	public StreamerServer(int port, int offset, int rows){
 		this.port = port;
 		this.rows = rows;
 		
 		isShutdown = false;
 		timeSet = false;
 		
 		queue = new LinkedBlockingQueue<String>(QUEUE_SIZE);
 		
 		try {
 			init();
 			initTextOutSocket();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void initTextOutSocket(){
 		try {
 			textOutSocket = new Socket("localhost", 5999);
 		} catch (UnknownHostException e) {
			//e.printStackTrace();
			System.out.println("StreamerServer: Could not connect to GUI socket");
 		} catch (IOException e) {
			//e.printStackTrace();
			System.out.println("StreamerServer: Could not connect to GUI socket");
 		}
 	}
 	
 	public synchronized void setInitialTimes(long virtual, long real){
 		initialVirtualTime = virtual;
 		initialTime = real;
 	}
 	
 	private void init() throws IOException{
 		/* Create Threadpool */
 		int poolSize = 5;
 		int maxPoolSize = 5;
 		long keepAliveTime = 10;
 		final ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(5);
 
 		tp = new ThreadPoolExecutor(poolSize, maxPoolSize,
 				keepAliveTime, TimeUnit.SECONDS, queue);
 
 		ss = new ServerSocket(port);
 		
 		createDaemonListener();
 		createDaemonProducer();
 		//createDaemonProducer();
 	}
 	
 	private void createDaemonListener(){
 		// thread used to receive commands
 		Runnable listener = new Runnable(){
 			public void run() {
 				BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
 				String input;
 				try {
 					while((input = in.readLine()) != null){
 						if (input.equals("shutdown") || input.equals("exit") || input.equals("quit")){
 							isShutdown = true;
 							ss.close();
 							break;
 						}
 						// can also include ways to change initial_offset and num_rows_per_thread
 					}
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		};
 		Thread t = new Thread(listener);
 		t.setDaemon(true);
 		t.start();
 	}
 	
 	private void createDaemonProducer() throws IOException {
 		Runnable producer = new DataStreamer(queue, rows);
 		
 		Thread t = new Thread(producer);
 		t.setDaemon(true);
 		t.setPriority(Thread.MAX_PRIORITY);
 		t.start();
 	}
 	
 	private long parseTime(String data){
 		long time = -1;
 		if (data != null){
 			Pattern p = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
 			Matcher m = p.matcher(data);
 			if(m.find()){
 				String t = m.group();
 				SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
 				
 				try {
 					Date date = sdf.parse(t);
 					time = date.getTime();
 				} catch (ParseException e) {}
 			}
 		}
 		return time;
 	}
 	
 	@SuppressWarnings("unused")
 	private void createConsumer(Socket s){
 		createConsumer(s, 0);
 	}
 	private void createConsumer(final Socket s, final int id){
 		Runnable consumer = new Runnable(){
 			public void run() {
 				int i = 0;
 				try {
 					PrintWriter out = new PrintWriter(s.getOutputStream(), true);
 					PrintWriter textOut = null;
 					
 					while(true){
 						// NOTE: possible race condition between peek() and take(), although this shouldn't matter too much.
 						String data = queue.peek();
 						long time = parseTime(data);
 						
 						// sleep for "difference" milliseconds
 						if (timeSet && time > 0){
 							long curr = System.currentTimeMillis();
 							long virtualOffset = (long) ((time - initialVirtualTime)/SPEEDUP_FACTOR);
 							long difference = initialTime + virtualOffset - curr;
 							if (difference > WAIT_THRESHOLD){
 								System.err.println(i+": sleeping for "+(difference)+"ms (realtime)");
 								Thread.sleep(difference);
 							}
 						}
 						
 						data = queue.take();
 						
 						if (!timeSet && time > 0){
 							time = parseTime(data);
 							setInitialTimes(time, System.currentTimeMillis());
 							timeSet = true;
 						}
 						
 						// TODO: Get rid of dates -- quick fix
 						data = data.substring(DATE_FORMAT.length()+2);
 						
 						
 						System.out.println(i+": "+data);
 						// for the GUI 
 						if (textOutSocket != null){
 							textOut = new PrintWriter(textOutSocket.getOutputStream(), true);
 							textOut.println(i+": "+data);
 						}
 						
 						out.println(data);
 						i++;
 					}
 				} catch (IOException e) {
 					e.printStackTrace();
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		};
 		tp.execute(consumer);
 	}
 	
 	public void run(){
 		int i = 0;
 		System.out.println("Running Streamer Server...");
 		try {
 			while( !isShutDown() ){
 				ss.setSoTimeout(SOCKET_TIMEOUT);
 				Socket s = ss.accept();
 				
 				// create a new consumer thread as a data stream
 				createConsumer(s, i);
 				
 				i++;
 			}
 		}
 		catch (SocketTimeoutException e){
 			System.out.println("ServerSocket timed out");
 		} catch (SocketException e) {
 			//e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			this.shutdown();
 		}
 	}
 	
 	private void shutdown(){
 		if (ss != null){
 			try {
 				ss.close();
 			} catch (IOException e) {}
 			ss = null;
 		}
 		tp.shutdown();
 		tp.shutdownNow();
 		System.out.println("Exit Streamer Server...");
 	}
 
 	public static void main(String args[]) throws SQLException, IOException {
 		
 		List<String> other_args = new ArrayList<String>();
 		for(int i=0; i < args.length; ++i) {
 			try {
 				if ("-s".equals(args[i])) {
 					double speedup_factor = Double.parseDouble(args[++i]);
 					SPEEDUP_FACTOR = speedup_factor;
 				} else {
 					other_args.add(args[i]);
 				}
 			} catch (NumberFormatException except) {
 				System.out.println("ERROR: Integer expected instead of " + args[i]);
 				printUsageAndQuit();
 			} catch (ArrayIndexOutOfBoundsException except) {
 				System.out.println("ERROR: Required parameter missing from " +
 						args[i-1]);
 				printUsageAndQuit();
 			}
 		}
 
 		
 		if (other_args.size() != 3) { printUsageAndQuit(); }
 		
 		int port = Integer.parseInt(other_args.get(0));
 		int initial_offset = Integer.parseInt(other_args.get(1));
 		int num_rows_per_thread = Integer.parseInt(other_args.get(2));
 		
 		StreamerServer ss = new StreamerServer(port, initial_offset, num_rows_per_thread);
 		ss.run();
 	}
 
 	private boolean isShutDown() {
 		return isShutdown;
 	}
 
 
 	public static void printUsageAndQuit(){
 		System.out.println("Usage: DataStreamer [-s speedup_factor] <port> <initial_offset> <rows_per_thread>");
 		System.exit(0);
 	}
 }
