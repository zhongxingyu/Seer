 package com.bryanmarty.ergoproxy;
 
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.Socket;
 import java.net.SocketTimeoutException;
 import java.nio.BufferOverflowException;
 import java.nio.ByteBuffer;
 import java.util.concurrent.Callable;
 
 public class DownloadTask implements Callable<ByteBuffer> {
 
 	HttpRequest request_;
 	ByteBuffer data;
 	boolean success = false;
 	
 	public DownloadTask(HttpRequest request) {
 		request_ = request;
 		data = ByteBuffer.allocate(1024*1024*1);
 	}
 	
 	@Override
 	public ByteBuffer call() throws Exception {
 		Socket serverSocket = new Socket(request_.getHost(),request_.getPort());
 		InputStream is = serverSocket.getInputStream();
 		OutputStream os = serverSocket.getOutputStream();
 		serverSocket.setSoTimeout(4000);
 		System.out.println(request_.getRequest()+"---");
 		os.write(request_.getRequest().getBytes("US-ASCII"));
 		os.flush();
 		//serverSocket.shutdownOutput();
 		byte[] buffer = new byte[1*1024*1024];
 		int d;
 		try {
 			while(( d = is.read(buffer,0,buffer.length)) != -1) {
 	
				System.out.println(new String(buffer,0,d));
 				//try {
 					data.put(buffer, 0, d);
 				//} /*catch (BufferOverflowException boe) {
 					//boe.printStackTrace();
 					//System.exit(1);
 				//}*/
 			}
 			if(d == -1) {
 				success = true;
 			}
 		} catch(SocketTimeoutException ste) {
 			if(serverSocket.isConnected()) {
 				serverSocket.close();
 			}
 			//throw ste;
 		} catch(Exception e) {
 			e.printStackTrace();
 			throw e;
 		}
 		return data;
 	}
 
 }
