 /**
  * JBoss, Home of Professional Open Source. Copyright 2011, Red Hat, Inc., and
  * individual
  * contributors as indicated by the @author tags. See the copyright.txt file in
  * the distribution
  * for a full listing of individual contributors.
  * 
  * This is free software; you can redistribute it and/or modify it under the
  * terms of the GNU Lesser
  * General Public License as published by the Free Software Foundation; either
  * version 2.1 of the
  * License, or (at your option) any later version.
  * 
  * This software is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY;
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  * PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this
  * software; if not, write to the Free Software Foundation, Inc., 51 Franklin
  * St, Fifth Floor,
  * Boston, MA 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.jboss.test.client;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.lang.management.ManagementFactory;
 import java.net.Inet4Address;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.concurrent.atomic.AtomicInteger;
 
 /**
  * {@code JioClient}
  * 
  * Created on Nov 11, 2011 at 3:38:26 PM
  * 
  * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
  */
 public class JioClient extends Thread {
 	
 	/**
      *
      */
 	public static final int				READ_BUFFER_SIZE	= 16 * 1024;
 	/**
      *
      */
 	public static final String			CRLF				= "\r\n";
 	/**
      *
      */
 	public static final int				MAX					= 1000;
 	/**
      *
      */
 	public static int					N_THREADS			= 100;
 	/**
 	 * Default wait delay 1000ms
 	 */
 	public static final int				DEFAULT_DELAY		= 1000;
 	public static final int				DEFAULT_NREQ		= 1000000;
 	private String						hostname;
 	private int							port;
 	private int							max;
 	private int							delay;
 	private Socket						channel;
 	private String						sessionId;
 	private DataOutputStream			dos;
 	private InputStream					is;
 	private static final AtomicInteger	COUNTER				= new AtomicInteger(0);
 	private static final Random			RAND				= new Random();
 	
         // used for synchronizing threads
         private static AtomicInteger counter;
         private static Object lock;
         
 	/**
 	 * Create a new instance of {@code JioClient}
 	 * 
 	 * @param hostname
 	 * @param port
 	 * @param d_max
 	 * @param delay
 	 */
 	public JioClient(String hostname, int port, int d_max, int delay) {
 		this.hostname = hostname;
 		this.port = port;
 		this.max = d_max;
 		this.delay = delay;
                 JioClient.counter = new AtomicInteger(0);
                 JioClient.lock = new Object();
 	}
 	
 	/**
 	 * Create a new instance of {@code JioClient}
 	 * 
 	 * @param hostname
 	 * @param port
 	 * @param delay
 	 */
 	public JioClient(String hostname, int port, int delay) {
 		this(hostname, port, 55 * 1000 / delay, delay);
 	}
 	
 	@Override
 	public void run() {
 		try {
 			sleep(RAND.nextInt(2000));
                         // Initialize the communication between client and server
 			init();
                         
 			while (COUNTER.get() < N_THREADS) {
 				sleep(100);
 			}
                         
                         // increment the counter and spin lock
                         JioClient.counter.incrementAndGet();
                         synchronized( JioClient.lock ) {
                             JioClient.lock.wait();
                         }
                         
                         // all threads will start this at pseudo-equal time
 			runit();
 		} catch (Throwable exp) {
 			exp.printStackTrace();
 		} finally {
 			try {
 				this.channel.close();
 			} catch (IOException ioex) {
 				// NOPE
 			}
 		}
 	}
 	
 	/**
 	 * 
 	 * @throws Exception
 	 */
 	protected void connect() throws Exception {
 		// Open connection with server
 		this.channel = new Socket(this.hostname, this.port);
 		this.channel.setSoTimeout(60000);
 		this.dos = new DataOutputStream(this.channel.getOutputStream());
 		this.is = this.channel.getInputStream();
 		COUNTER.incrementAndGet();
 	}
 	
 	/**
 	 * 
 	 * @throws Exception
 	 */
 	protected void init() throws Exception {
 		// Connect to the server
 		sleep(RAND.nextInt(5000));
 		this.connect();
                 
                 // HTTP REQUEST HERE  (every client gets its own session)
 		write("POST /session-" + getId() +"-"+Inet4Address.getLocalHost().toString()+" HTTP/1.1" + CRLF + CRLF);
                
 		BufferedReader in = new BufferedReader(new InputStreamReader(this.channel.getInputStream()));
 		String response = in.readLine();
                 // do this trick for Netty as first line contain HTTP header
                 while( ! response.startsWith("jSessionId:") ) {
                     response = in.readLine();
                 }
                 
 		System.out.println("RECEIVED FROM SERVER : " + response);
 		String tab[] = response.split("\\s+");
                 this.sessionId = tab[1];
 	}
 	
 	/**
 	 * 
 	 * @throws Exception
 	 */
 	public void runit() throws Exception {
 		// Wait a delay to ensure that all threads are ready
 		sleep(DEFAULT_DELAY + (new Random()).nextInt(COUNTER.get()));
 		long time = 0;
 		String response = null;
 		int counter = 0;
 		int min_count = 10 * 1000 / delay;
 		int max_count = 50 * 1000 / delay;
 
 		List<Long> delays = new ArrayList<Long>();
 
                 long timeWrite;
                 long timeRead;
                 long startTime = System.nanoTime();
 		while ((this.max--) > 0) {
                         // long wait = this.delay - time;
                         // if (wait>0)
 			//    sleep(wait);
 			// time = System.currentTimeMillis();
 			//System.out.println("WRITE TO SERVER");
 			sleep(this.delay);
 		        timeWrite =  System.nanoTime();
                         
                         // HTTP REQUEST HERE !!! 
                        /*StringBuffer buf = new StringBuffer();
                         buf.append("GET /data/file.txt?jSessionId=" + this.sessionId + " HTTP/1.1" + CRLF);
                         if( this.max != 0 ) {
                             buf.append("Connection: keep-alive \n");
                         }
                         buf.append(CRLF);
 			write(buf.toString());
                         //System.out.println("Send this HTTP request: ");
                         System.out.println("GET /data/file.txt?jSessionId=" + this.sessionId + " HTTP/1.1" + CRLF + CRLF);
                         */
                         
                         //get the response 
                         response = read();
                         
 			// time = System.currentTimeMillis() - time;
 			timeRead = System.nanoTime();
 			// delays.add(time);
 			System.out.println("WRITE " + (timeWrite - startTime) + " READ " + (timeRead - startTime));
 		        delays.add(timeRead - timeWrite);
 
 			/*
 			 * if (counter >= min_count && counter <= max_count) {
 			 * // update the average response time
 			 * avg_time += time;
 			 * // update the maximum response time
 			 * if (time > max_time) {
 			 * max_time = time;
 			 * }
 			 * // update the minimum response time
 			 * if (time < min_time) {
 			 * min_time = time;
 			 * }
 			 * }
 			 */
 			counter++;
 		}
                Long sum = 0l;
 		for (Long d : delays) {
                     System.out.println(d);
                     sum += d;
 		}

        //calculating an average response
        Long responseAvg = sum/delays.size();
        System.out.println("Average response time:" + responseAvg);
     }
 	
 	/**
 	 * 
 	 * @param data
 	 * @throws Exception
 	 */
 	public void write(String data) throws Exception {
 		this.dos.write(data.getBytes());
 		this.dos.flush();
 	}
 	
 	/**
 	 * 
 	 * @return data received from server
 	 * @throws Exception
 	 */
 	public String read() throws Exception {
 		byte bytes[] = new byte[READ_BUFFER_SIZE];
 		int nBytes = -1;
 		long n = 0;
                 
 		while ((nBytes = this.is.read(bytes)) != -1) {
 			n += nBytes;
                         //System.out.println(new String(bytes, 0, nBytes));
                         // NUL signals the end of the request
                         if (nBytes >= 1 && bytes[nBytes - 1] == '\0' ) { // && bytes[nBytes - 2] == '\r') {
                                 System.out.println("\n**** NUL attempted ****");
 				break;
 			}
 		}
             /*    System.out.println("header: " + header.toString());
                 int content_length=0;
                 // we have the header here, get the "Content-length:" value 
                 String header_lines[] = header.toString().split("\n");
                 for( int i=0; i < header_lines.length; ++i ) { 
                     if( header_lines[i].startsWith("Content-Length:") ) { 
                         content_length = Integer.parseInt(
                                 header_lines[i].substring(header_lines[i].indexOf(":")+1).trim());
                         break;
                     }
                 }
                 //System.out.println("Content length: " + content_length );
                 int read=0;
                 while ((nBytes = this.is.read(bytes)) != -1) {
                     read += nBytes;
                     System.out.println("content: " + new String(bytes, 0, nBytes));
                     System.out.println("read: " + read + " total content length " + content_length);
                     if( read >= content_length ) { 
                         break;
                     }
                 }*/
                 // print here data received from server (the file content)
                 //System.out.println(new String(bytes));
 		return "Hello world!";
 	}
 	
 	/**
 	 * 
 	 * @param args
 	 * @throws Exception
 	 */
 	public static void main(String[] args) throws Exception 
         {
                 /**
                 * TODO: add parameter for choosing individual tests to be run ! 
                 *      - integrate it with JUnit
                 */ 
             
                 //IMPORTANT FOR TESTING TOOL; DO NOT DELETE!
                 String PID = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
                 System.out.println("PID: "+PID);
                 System.out.flush();
             
 		if (args.length < 3) {
 			System.err.println("Usage: java " + JioClient.class.getName()
 					+ "distributedSynch hostname port [clientID] [n] [delay] [nReq] [wDir]");
                         System.err.println("\tdistributedMode: Set 'yes' if synchronization with other remote clients is desired.");
 			System.err.println("\thostname: The server IP/hostname.");
 			System.err.println("\tport: The server port number.");
                         System.err.println("\tclientID: The client ID. Used only when distributed mode is turned on.");
 			System.err.println("\tn: The number of threads. (default is 100)");
 			System.err.println("\tdelay: The delay between requests. (default is 1000ms)");
 			System.err.println("\tnReq: The total number of requests. (default is 1000000)");
                         System.err.println("\twDir: The working directory. (default is ~/)");
 			System.exit(1);
 		}
 		
                 String distributedMode = args[0];
                 if( !distributedMode.equals("yes") && !distributedMode.equals("no") ) {
                     throw new IllegalArgumentException(
                             "Please pass either 'yes' or 'no' for the distributedMode parameter.");
                 }
                     
 		String hostname = args[1];
 		int port = Integer.parseInt(args[2]);
                 String clientID = args[3];
 		int n = 100, delay = DEFAULT_DELAY, nReq = DEFAULT_NREQ;
                 String wDir = "";
 		if (args.length > 4) {
 			try {
 				n = Integer.parseInt(args[4]);
 				if (n < 1) {
 					throw new IllegalArgumentException(
 							"Number of threads may not be less than zero");
 				}
 				
 				if (args.length > 5) {
 					delay = Integer.parseInt(args[5]);
 					if (delay < 1) {
 						throw new IllegalArgumentException("Negative value of delay: " + delay);
 					}
 				}
 				if (args.length > 6) {
 					nReq = Integer.valueOf(args[6]);
 					if (nReq < 1) {
 						throw new IllegalArgumentException(
 								"Negative value for number of requests: " + nReq);
 					}
 					if (nReq < n) {
 						System.err.println("ERROR: you should have nReq >= n");
 						System.err.println("Adjusting nReq to " + n);
 						nReq = n;
 					}
 				}
                                 if (args.length > 7) {
 					wDir = args[7];
 					if (! new File(wDir).exists() || 
                                             (new File(wDir).exists() && !new File(wDir).isDirectory())) {
 						throw new IllegalArgumentException(
 								"Invalid working directory: " + wDir);
 					}
 				}
 			} catch (Exception exp) {
 				System.err.println("Error: " + exp.getMessage());
 				System.exit(1);
 			}
 		}
 		N_THREADS = n;
 		
 		System.out.println("\n Running test with parameters:");
                 System.out.println("\tDistributed mode: " + distributedMode);
 		System.out.println("\tHostname: " + hostname);
 		System.out.println("\tPort: " + port);
                 System.out.println("\tClientID: " + clientID);
 		System.out.println("\tn: " + n);
 		System.out.println("\tdelay: " + delay);
 		System.out.println("\tnReq: " + nReq);
 		
 		JioClient clients[] = new JioClient[n];
 		
 		int nReqClient = nReq / n;
 		
 		for (int i = 0; i < clients.length; i++) {
 			clients[i] = new JioClient(hostname, port, nReqClient, delay);
 		}
 		
 		for (int i = 0; i < clients.length; i++) {
 			clients[i].start();
 		}
                 
                 // wait that all threads incremented the counter, thus saying that they are 
                 // ready to go further
                 while( JioClient.counter.get() != clients.length ) {
                     System.out.println("Wait the threads to reach same state ...");
                     Thread.sleep(500);
                 }
                 if( distributedMode.equals("no") ) {
                     System.out.println("Threads are now at same state, so release them.");
                     // the counter is set, so release the threads 
                     synchronized(JioClient.lock) {
                         JioClient.lock.notifyAll();
                     }
                 }
                 else if( distributedMode.equals("yes") ) {
                     // threads are now waiting for a signal to continue
                     // this signal will be sent after the coordinator of the testing tool 
                     // will say so; a bit similar to 2PC commit 
                     
                     // now, create a local file that would signal to the coordinator that 
                     // this client reached the point where the threads are synched
                     // IMPORTANT: please be sure that the testing tool uses the same name !!!
                     String synch_threads_filename = wDir+"/"+clientID+"-threads-are-synched";
                     new File(synch_threads_filename).createNewFile();
 
                     // now, we wait until the coordinator creates another file
                     String read_to_start_filename = wDir+"/"+clientID+"-start-sending-requests";
                     while( ! new File(read_to_start_filename).exists() ) {
                         Thread.sleep(50);
                     }
                     
                     // now it's the time to signal the threads to send requests
                     // release the locked threads
                     synchronized(JioClient.lock) {
                         JioClient.lock.notifyAll();
                     }
                 }
 		
                 // join the threads
 		for (int i = 0; i < clients.length; i++) {
 			clients[i].join();
 		}
                 
                 // as the threads are finished, signal it again with a new empty file
                 String ready_filename = wDir+"/"+clientID+"-finished";
                 new File(ready_filename).createNewFile();
 	}
 }
