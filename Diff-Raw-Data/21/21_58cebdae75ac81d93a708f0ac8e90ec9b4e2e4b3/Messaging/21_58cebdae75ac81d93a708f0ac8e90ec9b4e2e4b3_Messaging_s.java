 import java.io.*;
 
 public class Messaging {
 
 	private static Server server;
 
	public static void main(String args[]) throws IOException {
 		int port = 5010;
 		int dataport = -1;
 
 		if (args.length > 1) {
 			port = (new Integer(args[0])).intValue();
 			if (args.length > 2)
 				dataport = (new Integer(args[1])).intValue();
 		}
 
 		System.out.println("Server, listening on port " + port
 				+ ", datagram port " + dataport);
 		server = new Server(port, dataport);
 
 		while (true) {
 			System.out.println("Server, waiting for connection...");
 			server.Connect();
 
 			int result;
 			String command = server.RecvString('^');
 
 			switch (command) {
 			case "get":
 				result = get();
 				if (result == 0) {
 					// get successful
 					server.Close();
 				} else {
 					// Is this necessary?
 					server.Close();
 				}
 				break;
 			case "put":
 				result = put();
 				if (result == 0) {
 					// put successful
 					server.Close();
 				} else {
 					// Is this necessary?
 					server.Close();
 				}
 				break;
 			case "ls":
 				result = ls();
 				if (result == 0) {
 					// ls successful
 					server.Close();
 				} else {
 					// Is this necessary?
 					server.Close();
 				}
 				break;
 			case "mkdir":
 				result = mkdir();
 				if (result == 0) {
 					// mkdir successful
 					server.Close();
 				} else {
 					// Is this necessary?
 					server.Close();
 				}
 				break;
 			case "rmdir":
 				result = rmdir();
 				if (result == 0) {
 					// rmdir successful
 					server.Close();
 				} else {
 					// Is this necessary?
 					server.Close();
 				}
 				break;
 			case "rm":
 				result = rm();
 				if (result == 0) {
 					// rmdir successful
 					server.Close();
 				} else {
 					// Is this necessary?
 					server.Close();
 				}
 				break;
 			}
 		}
 	}
 
 	private static int get() {
 		String localPath = server.RecvString('^');
 
 		// May need to call verify user method here
 
		try {
 			server.SendFile(localPath);
 		} catch (IOException e) {
 			// Assume file not found exception
 			int buff[] = new int[1];
 			buff[0] = -1;
 			server.SendInts(buff, 1);
 		}
 
 		return 0;
 	}
 
 	private static int put() throws IOException {
 		String localPath = server.RecvString('^');
 
 		// May need to call verify user method here.
 
 		int fileSize[] = new int[1];
 		fileSize[0] = 0; // Temporarily allow all puts
 		server.SendInts(fileSize, 1);
 
 		server.RecvInts(fileSize, 1);
 
 		server.RecvFile(localPath, fileSize[0]);
 
 		return 0;
 	}
 
 	private static int ls() throws IOException {
 		String localPath = server.RecvString('^');
 
 		// May need to call verify user method here.
 
 		Runtime runtime = Runtime.getRuntime();
 		Process p = runtime.exec("ls " + localPath); // Do I need ./ here?
 		InputStream in = p.getInputStream();
 		BufferedInputStream bis = new BufferedInputStream(in);
 
 		FileOutputStream fos = new FileOutputStream(localPath + "/ls.txt");
 		BufferedOutputStream bos = new BufferedOutputStream(fos);
 
 		int i;
 		do {
 			i = bis.read();
 			if (i != -1) {
 				bos.write(i);
 			}
 		} while (i != -1);
 
 		bis.close();
 		bos.close();
 		in.close();
 		fos.close();
 
 		server.SendFile(localPath + "/ls.txt");
 
 		return 0;
 	}
 
 	private static int mkdir() throws IOException {
 		String localPath = server.RecvString('^');
 
 		// May need to call verify user method here.
 
 		Runtime runtime = Runtime.getRuntime();
 		Process p = runtime.exec("mkdir " + localPath); // Do I need ./ here?
 
		int response = new int[1];
 		response[0] = 0;
 		server.SendInts(response, 1);
 
 		return 0;
 	}
 
 	private static int rmdir() throws IOException {
 		String localPath = server.RecvString('^');
 
 		// May need to call verify user method here.
 
 		Runtime runtime = Runtime.getRuntime();
 		Process p = runtime.exec("rm -rf " + localPath); // Do I need ./ here?
 
		int response = new int[1];
 		response[0] = 0;
 		server.SendInts(response, 1);
 
 		return 0;
 	}
 	
 	private static int rm() throws IOException {
 		String localPath = server.RecvString('^');
 
 		// May need to call verify user method here.
 
 		Runtime runtime = Runtime.getRuntime();
 		Process p = runtime.exec("rm " + localPath); // Do I need ./ here?
 
		int response = new int[1];
 		response[0] = 0;
 		server.SendInts(response, 1);
 
 		return 0;
 	}
 }
