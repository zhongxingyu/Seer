 package org.joushou.FiveProxy;
  
 import java.net.Socket;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.io.File;
 import java.io.FileInputStream;
 import java.util.Collection;
 import java.util.regex.Pattern;
 import java.io.FileNotFoundException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.HttpURLConnection;
 import java.io.BufferedReader;
 import java.io.BufferedOutputStream;
 import java.io.BufferedInputStream;
 import java.io.InputStream;
 import java.io.DataInputStream;
 import java.io.FileOutputStream;
  
 class Worker extends Thread {
   
   private Socket s;
   private InetAddress client;
   public long socketOpenTime;
 	synchronized void setSocket(Socket s){
 		this.s = s;
 		notify();
 	}
 	private static boolean connFinished;
  
 	void handleClient() {
 		client = s.getInetAddress();
  
 		connFinished = false;
 		try {
 			BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
 			BufferedOutputStream output = new BufferedOutputStream(s.getOutputStream(), 2048);
                         while (true) {
                             input.mark(10);
                             if(connFinished) {
                                 output.close();
                                 input.close();
                                 break;
                             } else if (input.read() == -1) {
                               output.close();
                               break;
                             } else {
                                 input.reset();
                                 handleHttp(input, output);
                             }
                         }
 		} catch(IOException e) {}
  
 	}
 	public synchronized void run() {
 		while (true){
 			if (s != null) {
 				handleClient();	
 				s = null;
  
 				if (webServer.threads.size() >= webServer.maxThreads) {
 					System.out.println("Too many threads; exiting this: "+ this.getId());
 					return;
 				} else {
 					webServer.threads.addElement(this);
 				}
 			} else {
 				try {
 					wait();
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 	}
  
 	private synchronized int getBandwidth() {
 	  return(Settings.availableBandwidth/webServer.workingThreads);
 	}
  
 	private synchronized void releaseBandwidth() {
 	  webServer.workingThreads--;
 	}
  
 	private void handleHttp(BufferedReader input, BufferedOutputStream output) {
     int method = 0;
     try {
     	String line1 = input.readLine();
     	line1.toUpperCase();
     	String[] req = line1.split("\\s+");
       String reqHead;
       String[] seg;
       if(!line1.startsWith("GET")) {
         output.write(buildHttpHeader(400, "text/plain", -1).getBytes());
         output.write(("Invalid request line: " + line1).getBytes());
         output.flush();
         return;
       }
       boolean authorized = false;
       while (!(reqHead = input.readLine()).equals("")){
         if((seg = reqHead.split(" ")).length == 3) {
           if(seg[0].equals("Authorization:") && seg[1].equals("Basic") && (seg[2].equals("Zml2ZXVzZXI6dGVuc2hp") || seg[2].equals("Zml2ZXVzZXI6MzBiZGJjMGJhZWQ5NWJkNDFjMDVhYzBmZmQ2NDgyZWNiYzg4Y2NhOQ=="))) {
             authorized = true;
           }
         }
       }
       if (!authorized){
         Main.log("AccessDenied: " + client.getHostAddress() + " ("+line1+")");
         output.write(buildHttpHeader(401,"",0).getBytes());
         output.flush();
         return;
       } else {
         Main.log("AccessGranted: " + client.getHostAddress() + " ("+line1+")");
       }
       if (req.length == 3) {
         if (line1.startsWith("GET")) {
     		  String[] segments = req[1].split("/");
     		  if (segments.length > 0) {
     		    if (segments[1].equals("songs")) {
               String requestSong = MusicDB.getTitleFromId(Integer.parseInt(segments[2]));
               Integer id = 0;
               try {
                 id = Integer.parseInt(segments[2]);
               } catch (java.lang.NumberFormatException e) {
                 output.write(buildHttpHeader(400,"",0).getBytes());
                 output.close();
                 return;
               }
               MusicDB.logPlay(id,client.getHostAddress());
               File file = new File("songs/"+ id.toString());
               long fileSize = MusicDB.getSizeFromId(Integer.parseInt(segments[2]));
               output.write(buildHttpHeader(200, "audio/mpeg", fileSize).getBytes());
               if(file.exists() && !Caching.isCaching(id))
                 if(file.length() != fileSize){
                   file.delete();
                   Main.log("Deleting corrupted cached version of '"+ requestSong + "' id: "+ segments[2]);
                 }
               Main.log("Requested '" + requestSong + "' from " + client.getHostAddress() + " ["+(file.exists() ? (Caching.isCaching(id) ? "PARTIAL" : "LOCAL") : "REMOTE")+"]");                                              
               if(file.exists()) {
                 FileInputStream fis = new FileInputStream(file);
                 BufferedInputStream bis = new BufferedInputStream(fis, 2048);
       			    int b;
                 try {
                   int count = 0;
                   while (((b = bis.read()) != -1)  || count < fileSize) {
                     if (b != -1 || count > fileSize){
                       output.write(b);
                       count++;
                     }
                   }
                   output.flush();
                   float timepassed = (System.currentTimeMillis()-socketOpenTime)/1000;
                   float avgspeed = (fileSize/1024) / timepassed;
                   Main.log("Finished Streaming '"+requestSong+"' @"+Float.toString(avgspeed)+" KB/s");
                 } catch (IOException e){
                   //Closed socket
                 }
               } else {
                 InputStream in = Main.getData("/songs/" + id.toString()).getInputStream();
       			    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file), 2048);
                 webServer.workingThreads++;
                 int b;
                 boolean disconnected = false;
                 int counter = 0;
                 int timingCounter = 0;
                 long timingTest = 0;
                 long timing;
                 int bandwidth = getBandwidth();
                 Caching.cache(id);
                 timingTest = System.currentTimeMillis();
                 while (((b = in.read()) != -1) ){
                   if (!disconnected){
                     try {
       				        output.write(b);
                     } catch(IOException e){
                       disconnected = true;
                     }
                   }
  
                   if (timingCounter == 0) {
                     timingCounter++;
                   } else if (timingCounter == 1024) {
                     timing = (System.currentTimeMillis() - timingTest);
                     timingTest = System.currentTimeMillis();
                     timingCounter = 0;
                     bandwidth = getBandwidth();
                     if (timing == 0)
                       timing = 1;
                     float allowedMsPerKB = (float)1000.0/(float)bandwidth;
                     
                     if(allowedMsPerKB > timing) {
                       long ms = ((long) allowedMsPerKB - timing);
                      sleep (ms);
                     }
                   } else {
                     timingCounter++;
                   }
                   bos.write(b);
                 }
                 this.releaseBandwidth();
                 bos.flush();
       		      float timepassed = (System.currentTimeMillis()-this.socketOpenTime)/1000;
       				  float avgspeed = (fileSize/1024) / timepassed;
                 Main.log("Finished Caching '"+requestSong+"' @"+Float.toString(avgspeed)+" KB/s");
                 Caching.doneCaching(id);
                 bos.close();
                 //Eow
                 Main.mCaching.clean();
                 if (disconnected) {
       			      connFinished = true;
       			      return;
                 }
               }
             } else if (segments[1].equals("feeds")) {
               HttpURLConnection con = Main.getData("/feeds/" + segments[2]);
               InputStream in = con.getInputStream();
   				    output.write(buildHttpHeader(200, "text/html", -1).getBytes());
   				    int b;
   			      while ((b = in.read()) != -1)
                 output.write(b);
               } else if (segments[1].equals("info")) {
                 output.write("HTTP/1.1 200 OK\r\nX-Five-Version: 20091215\r\nContent-Length: 0 \r\nConnection: Keep-Alive\r\n\r\n".getBytes());
               } else if (segments[1].equals("playlist")) {
                 String regex = "^[a-z]+$";
                 if(segments.length == 3) {
                   if(Pattern.matches(regex,segments[2])){
                     output.write(buildHttpHeader(200,"audio/x-scpls",-1).getBytes());
                     output.write(MusicDB.getPlaylistFromArtist(segments[2]).getBytes());
                   } else {
                     output.write(buildHttpHeader(400,"",0).getBytes());
                   }
                 } else {
                   output.write(buildHttpHeader(200,"audio/x-scpls",-1).getBytes());
                   output.write(MusicDB.getPlaylistFromArtist("").getBytes());
                 }
                 connFinished = true;
               } else if(segments[1].equals("imageThumb") || segments[1].equals("image")) {
                 InputStream in = null;
                 HttpURLConnection con = Main.getData(req[1]);
                 try {
                   in = con.getInputStream();                              
                 } catch (FileNotFoundException e) {
                 output.write(buildHttpHeader(404,"",0).getBytes());                                                                                    
                 output.flush();
                 return;
               }
               output.write(buildHttpHeader(200,"",Integer.parseInt(con.getHeaderField(1))).getBytes());
               int b;
               while ((b = in.read()) != -1)
                 output.write(b);                                        
               in.close();
             }
           } else {
             output.write(buildHttpHeader(200, "text/html; charset=utf-8", -1).getBytes());	
       			output.write("<html><body><h1>webServer</h1>\r\n".getBytes());
       			output.write(("Request type: " + req[0] + "<br />\r\n").getBytes());
       			output.write(("Request path: " + req[1] + "<br />\r\n").getBytes());
       			output.write(("Thread ID: " + this.getName() + "</body></html>").getBytes());
       			output.write((Main.table.toString()).getBytes());
             connFinished = true;
   				}                                                                          
   				//output.close();
   			} else {
   				output.write(buildHttpHeader(400,"",0).getBytes());
   				//output.close();
   			}
   		} else {
     	output.write(buildHttpHeader(400, "",0).getBytes());
   		//output.close();
   		}	
   		output.flush();
   	} catch (IOException e){
     	e.printStackTrace();
     	//Disconnected 
   	} catch (Exception e) {e.printStackTrace();}
 	}
  
   private static String buildHttpHeader(int returnCode, String ctype, long clength) {
 	  String s = "HTTP/1.1 ";
  
 		switch (returnCode) {
 		case 200:
 			s = s + "200 OK";
 			break;
 		case 206:
 			s = s + "206 Partial Content";
 			break;
 		case 400:
 			s = s + "400 Bad Request";
       connFinished = true;
 			break;
 	  case 401:
 	    s = s + "401 Unauthorized\r\n";
 			s = s + "WWW-Authenticate: Basic realm=\"five-server\"";
 	    break;
 		case 403:
 			s = s + "403 Forbidden";
 			break;
 		case 404:
 			s = s + "404 Not Found";
       break;
 		case 500:
 			s = s + "500 Internal Server Error";
 			break;
 		case 501:
 			s = s + "501 Not Implemented";
 			break;
 		}
  
 		s = s + "\r\n";
 		if(clength != -1)
 			s = s + "Content-Length: " + clength + "\r\n";
 		if(!(ctype.length() == 0))
 		  s = s + "Content-Type: " + ctype + "\r\n";
     if (connFinished) {
       s = s + "Connection: close\r\n";
 		} else {
 		  s = s + "Connection: Keep-Alive\r\n";
 		}
 		s = s + "\r\n";
  
 		return s;
 	}
 }
