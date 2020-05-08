 package com.hsun324.ftplite;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.net.Socket;
 
 import com.hsun324.ftplite.handlers.FTPHandlerRegistry;
 
 class FTPStreamThread extends Thread {
 	public static final int DEFAULT_BUFFER_SIZE = 400;
 
 	private final FTPClient client;
 	public final Socket socket;
 	public final InputStream inputStream;
 	public final Reader reader;
 	
 	private final char[] buffer;
 	private boolean stopRequested = false;
 	
 	private int previousCode = -1;
 	private StringBuffer responseBuffer = new StringBuffer();
 	
 	public FTPStreamThread(FTPClient client, Socket socket) throws IOException {
 		this(client, socket, DEFAULT_BUFFER_SIZE);
 	}
 	public FTPStreamThread(FTPClient client, Socket socket, int bufferSize) throws IOException {
 		this.client = client;
 		this.socket = socket;
 		this.inputStream = socket.getInputStream();
 		this.reader = new InputStreamReader(this.inputStream);
 		this.buffer = new char[bufferSize];
 	}
 	
 	@Override
 	public void run() {
 		try {
 			while (!stopRequested) {
 				int newline = findNewlineIndex(buffer, reader.read(buffer));
 				if (newline > -1) {
 					String line = getLine(buffer, newline, 2);
 					
 					if (line.length() > 4) {
 						char delimiter = line.charAt(3);
 						String content = line.substring(4);
 						int code = Integer.parseInt(line.substring(0, 3));
 						
 						if (previousCode == -1 || previousCode != code || delimiter == ' ') {
 							FTPResponse response = new FTPResponse(code, content);
 							System.out.println("  " + response);
 							FTPState state = client.state;
 							if (!FTPHandlerRegistry.tryGlobalHandle(state, response) && state.currentFuture != null)
								if (state.currentFuture.command.pushResponse(state, response)) state.currentFuture = null;
 							previousCode = -1;
 						} else if (delimiter == '-') {
 							responseBuffer.append(content).append("\r\n");
 							previousCode = code;
 						}
 					}
 				}
 			}
 			reader.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	public void requestStop() {
 		stopRequested = true;
 	}
 	
 	private String getLine(char[] buffer, int end, int trim) {
 		String result = new String(buffer, 0, end);
 		
 		int shift = end + trim;
 		int threshold = buffer.length - shift;
 		
 		for(int i = 0; i < shift; i++)
 			if (i < threshold) buffer[i] = buffer[i + shift];
 			else buffer[i] = 0;
 		
 		return result;
 	}
 	private int findNewlineIndex(char[] buffer, int len) {
 		for (int i = 0, j = 1; j < len; i++, j++)
 			if (buffer[i] == '\r' && buffer[j] == '\n') return i;
 		return -1;
 	}
 }
