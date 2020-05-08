 /*
  * Copyright (c) 2012, someone All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * 1.Redistributions of source code must retain the above copyright notice, this
  * list of conditions and the following disclaimer. 2.Redistributions in binary
  * form must reproduce the above copyright notice, this list of conditions and
  * the following disclaimer in the documentation and/or other materials provided
  * with the distribution. 3.Neither the name of the Happyelements Ltd. nor the
  * names of its contributors may be used to endorse or promote products derived
  * from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package com.happyelements.zero.nio.http;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.nio.ByteBuffer;
 import java.nio.channels.SocketChannel;
 import java.util.concurrent.locks.LockSupport;
 
import com.happyelements.zero.async.Asyncs;
 import com.happyelements.zero.nio.NIO;
 import com.happyelements.zero.nio.NIO.ConnectedListener;
 import com.happyelements.zero.nio.http.Parser.HttpResponse;
 
 /**
  * @author <a href="mailto:zhizhong.qiu@happyelements.com">kevin</a>
  *
  */
 public class Http {
 
 	public static void main(String[] args) {
 		try {
			new NIO(new Asyncs()).start().connect(
 					new InetSocketAddress("www.google.com.hk", 80),
 					new ConnectedListener() {
 						Parser parser;
 						int read;
 
 						@Override
 						public void notifyReadable(SocketChannel channel)
 								throws Exception {
 							// see resposne
 							HttpResponse response = parser.parse();
 							if (response == null) {
 								// not yet finished
 								return;
 							}
 
 							// first time
 							if (response.reading_body) {
 								System.out.println("status:" + response.status
 										+ " message:" + response.message
 										+ " header:" + response.headers);
 							}
 
 							int content_lenght = Integer
 									.parseInt(response.headers
 											.get("Content-Length"));
 
 							String body;
 							while ((body = response.readSome()) != null) {
 								if (body.isEmpty()) {
 									// temporary not enough
 									break;
 								}
 								System.out.println(body);
 								read += body.length();
 							}
 
 							if (read == content_lenght) {
 								channel.write(ByteBuffer
 										.wrap("GET / HTTP/1.1\r\nHost:www.google.com.hk\r\nConnection:Keep-Alive\r\n\r\n"
 												.getBytes()));
 								parser = null;
 								read = 0;
 								return;
 							}
 
 							// resposne all done
 							if (body == null) {
 								System.out.println("closed..");
 								parser = null;
 								channel.close();
 							}
 						}
 
 						@Override
 						public void notifyException(SocketChannel channel,
 								Exception exception) {
 							exception.printStackTrace();
 							try {
 								channel.close();
 							} catch (IOException e) {
 								e.printStackTrace();
 							}
 						}
 
 						@Override
 						public void notifyConnected(SocketChannel channel)
 								throws Exception {
 							channel.write(ByteBuffer
 									.wrap("GET / HTTP/1.1\r\nHost:www.google.com.hk\r\nConnection:Keep-Alive\r\n\r\n"
 											.getBytes()));
 							parser = new Parser(channel);
 						}
 					}, 10000);
 
 			LockSupport.park();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
