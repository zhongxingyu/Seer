 package fr.univmlv.qroxy.download;
 
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.nio.ByteBuffer;
 import java.nio.channels.Pipe;
 
 import fr.univmlv.qroxy.cache.Cache;
 
 public class Download implements Runnable {
 	
 	private final static int BUFFER_SIZE = 1024;
 	private final Pipe.SinkChannel channel;
 	private final URL url;
 	
 	public Download(Pipe.SinkChannel channel, URL url) {
 		this.channel = channel;
 		this.url = url;
 	}
 	
 	@Override
 	public void run() {
 		try {
 			/* Get informations */
 			URLConnection connection = url.openConnection();
 			connection.connect();
 			System.out.println("Url : " + url.toString());
 			System.out.println("Content-Type : " + connection.getContentType());
 			System.out.println("Content-Length : " + connection.getContentLength());
 			System.out.println("Theoric content length " + connection.getInputStream().available());
 			System.out.println("Cache-Control : " + connection.getHeaderField("Cache-Control"));
 			
 			/* Prepare cache */
 			// TODO can be equal to -1 do not cache during download
 			Cache cache = Cache.getInstance();
 			
			/*if (cache.isInCache(url.toString(), connection.getContentType())) {
 				// Get from cache
 				System.out.println("Content is in cache");
 				return;
			}*/
 			
 			boolean caching = false;
 			String cacheControl = (connection.getHeaderField("Cache-Control") == null) ? "" : connection.getHeaderField("Cache-Control");
 			if (connection.getContentLength() != -1) {
 				if (cacheControl.contains("private") == false) {
 					if (cache.freeSpace(connection.getContentLength())) {
 						System.out.println("Space clear for caching");
 						caching = true;
 					}
 					else {
 						System.out.println("No enough space for caching");
 					}
 				}
 				else {
 					System.out.println("Cache-control is private");
 				}
 			}
 			else {
 				System.out.println("We don't know the real size, downloading before caching");
 				if (cache.freeSpace(connection.getInputStream().available())) {
 					System.out.println("Space clear for caching");
 					caching = true;
 				}
 				else {
 					System.out.println("No enough space for caching");
 				}
 				caching = true;
 			}
 			
 			/* Get content from url and send it to the cache and client */
 			DataInputStream dis = new DataInputStream(connection.getInputStream());
 			byte[] buffer = new byte[BUFFER_SIZE];
 			int readbyte = 0;
 			while((readbyte = dis.read(buffer, 0, BUFFER_SIZE)) != -1 || readbyte == 0) {
 				ByteBuffer bb = ByteBuffer.wrap(buffer);
 				
 				/* Send it to the client */
 				channel.write(bb);
 				
 				/* Send it to the cache */
 				if (caching)
 					cache.addContentToCache(bb, url.toString(), connection.getContentType(), true);
 			}
 			channel.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static void main(String[] args) {
 		try {
 			//http://movies.apple.com/media/us/ipad/2012/80ba527a-1a34-4f70-aae8-14f87ab76eea/apple-ipad-tour-safari-us-20120306_600x671.mp4
 			//http://www.apple.fr/
 			//http://www.ubuntu.com/start-download?distro=desktop&bits=32&release=lts
 			
 			/* Create a pipe to communicate with the thread */
 			Pipe pipe = Pipe.open();
 			
 			/* Start the download */
 			new Thread(new Download(pipe.sink(), new URL("http://www.apple.fr/"))).start();
 			
 			/* On receiving data from the pipe, you can send directly to the client */
 			ByteBuffer bb = ByteBuffer.allocateDirect(BUFFER_SIZE);
 			byte[] buffer = new byte[BUFFER_SIZE];
 			int readbyte = 0;
 			while ((readbyte = pipe.source().read(bb)) != -1 || readbyte == 0) {
 				bb.flip();
 				bb.get(buffer);
				for(int i=0; i<readbyte; i++) {
 					System.out.print((char)buffer[i]);
				}
 				bb.clear();
 			}
 			System.out.println("");
 			System.out.println("End of download");
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
