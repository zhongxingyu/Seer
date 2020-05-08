 package serpis.psp;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.*;
 import java.util.Scanner;
 
 public class ThreadServer implements Runnable  {
 	private static final String newLine = "\r\n";
 	private Socket socket;
 
 	
 	public ThreadServer(Socket socket){
 		this.socket = socket;
 	}
 
 	
 	public void run(){
 
					String fileName;
					try {
						fileName = getFileName(socket.getInputStream());
					
 					writeHeader( socket.getOutputStream(),  fileName);
 					writeFile( socket.getOutputStream(),  fileName);
 					socket.close();	
 					
 					Thread.sleep(2000);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
 					}
 				
 
 		}
 	
 	private static String getFileName(InputStream inputStream){
 
 		Scanner scanner = new Scanner(inputStream);
 		String fileName = "";
 
 		while (true){
 			String line = scanner.nextLine();
 			if(line.startsWith("GET")){ //GET  /otro.html HTTP/1.1
 					int count = 5;
 					while(line.charAt(count) != ' ')//Mientra no haya un espacio que siga a�adiendo caracteres.
 						fileName += line.charAt(count++);
 
 			}
 
 			System.out.println(line);
 			if(line.equals(""))
 				break;
 		}
 		
 		return fileName;
 	}
 	
 	//Escribe la cabecera o no en funcion de si existe o no el archivo.
 	private static void writeHeader(OutputStream outputStream, String fileName) throws IOException{
 		final String response200 = "HTTP/1.0 200 OK";
 		final String response404 = "HTTP/1.0 404 NOT FOUND";
 		File file = new File(fileName);
 		String response = file.exists() ? response200 : response404;
 
 		
 		String header  = response + newLine + newLine;
 		byte[] headerBuffer = header.getBytes(); 
 		
 		outputStream.write(headerBuffer);
 		
 	}
 	
 	
 	private static void writeFile(OutputStream outputStream, String fileName) throws IOException{
 		final String fileNameError404 = "fileError404.html"; 
 		
 		File file= new File(fileName);
 		String responseFileName = file.exists() ? fileName : fileNameError404;		
 		
 		final int bufferSize = 2048;
 		byte[] buffer = new byte[bufferSize];
 		
 		FileInputStream fileInputStream = new FileInputStream(responseFileName);
 
 		int count;
 		while((count = fileInputStream.read(buffer))!=-1)
 			outputStream.write(buffer, 0, count);
 
 		fileInputStream.close();
 		
 	}
 
 
 }
