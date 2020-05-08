 package com.bryanmarty.ergoproxy;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.nio.ByteBuffer;
 import java.util.LinkedList;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 public class SocketTask implements Runnable {
 
 	private static final String NEW_LINE = "\r\n";
 	private static final DataCache DATA_CACHE = DataCache.getInstance();
 	private final Socket socket_;
 	private BlockingQueue<Runnable> blockingQueue_;
 	private ThreadPoolExecutor workerPool_;
 	private LinkedList<Future<ByteBuffer>> futureList = new LinkedList<Future<ByteBuffer>>();
 	
 	public SocketTask(Socket clientSocket) {
 		socket_ = clientSocket;
 		blockingQueue_ = new LinkedBlockingQueue<Runnable>();
 		workerPool_ = new ThreadPoolExecutor(1, 3, 10, TimeUnit.SECONDS, blockingQueue_);
 	}
 
 	@Override
 	public void run() {
 		if(socket_ == null) {
 			return;
 		}
 		
 		try {
 			while(socket_.isConnected()) {
 				if(socket_.isClosed()) {
 					break;
 				}
 				socket_.setSoTimeout(4000);
 				BufferedReader clientInput = new BufferedReader(new InputStreamReader(socket_.getInputStream(),"US-ASCII"));
 				OutputStream os = socket_.getOutputStream();
 				
 				while(clientInput.ready()) {
 					String line = null;
 					StringBuffer bufferData = new StringBuffer();
 					try {
 						while( (line = clientInput.readLine()) != null) {
 							if(line.isEmpty()) {
 								bufferData.append(NEW_LINE);
 								break;
 							}
 							bufferData.append(line + NEW_LINE);
 						}
 					} catch (SocketTimeoutException ste) {
 						//ste.printStackTrace();
 						break;
 					}
 				
 					HttpRequest request = HttpParser.parse(bufferData.toString());
 					DownloadTask task = new DownloadTask(request);
 					futureList.add(workerPool_.submit(task));
 				}
 				try {
 					for (Future<ByteBuffer> future : futureList) {
 						if(socket_.isClosed()) {
 							break;
 						}
 						ByteBuffer download = future.get(10, TimeUnit.MILLISECONDS);
 						download.flip();
 						if(!socket_.isOutputShutdown()) {
 							int min = socket_.getReceiveBufferSize();
 							byte[] buffer = new byte[min];
 							int length = 0;
 							while(download.hasRemaining()) {
 								try{
 									length = Math.min(download.remaining(),min);
 									download.get(buffer, 0, length);
									os.write(buffer,0,length);
 									os.flush();
 								} catch(SocketException e) {
 									//e.printStackTrace();
 									socket_.close();
 									break;
 									//System.out.println("Buffer: " + buffer + " AS " + new String(buffer,0,length));
 								}
 							}
 						}
 					}
 				} catch (TimeoutException te) {
 					
 				} catch (ExecutionException ee) {
 					ee.printStackTrace();
 					socket_.close();
 				} catch (InterruptedException ie) {
 					socket_.close();
 					ie.printStackTrace();
 				}
 			}
 			
 		if(socket_.isConnected()) {
 			socket_.close();
 		}
 		workerPool_.shutdownNow();
 			
 		} catch (SocketException se) {
 			if(!socket_.isClosed()) {
 				try {
 					socket_.close();
 				} catch (IOException io) {
 					
 				}
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }
