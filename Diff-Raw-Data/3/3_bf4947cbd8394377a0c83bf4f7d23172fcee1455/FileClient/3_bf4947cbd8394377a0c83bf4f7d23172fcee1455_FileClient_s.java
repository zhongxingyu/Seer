 package server;
 
 import java.net.*;
 import java.io.*;
 import java.util.Date;
 
 public class FileClient
 {
     public static void main (String [] args ) throws IOException 
     {
 
         // create socket
         ServerSocket server = new ServerSocket(13267);
         Socket clientSocket;
         final byte[] buffer;
         final int BUFFER_SIZE=33989;
         buffer = new byte[BUFFER_SIZE];
         String currentDateTimeString;
 
 
         //loop waiting for connection
         while (true) 
         {
             System.out.println("Waiting..."); 
             clientSocket = server.accept();
             System.out.println("Accepted connection : " + clientSocket);
             BufferedInputStream in = new BufferedInputStream(clientSocket.getInputStream());
 
 
             currentDateTimeString = getCurrentDateString();
 
 
             System.err.println("UploadReciever.receive: Start!");
             int size = 0;
             BufferedOutputStream out = null;
             try {
                 out = new BufferedOutputStream(new FileOutputStream("images/"+currentDateTimeString +".jpg"));
                 System.err.println("UploadReciever.receive: File output stream is open.");
                 int n = -1;
                 System.err.println("UploadReciever.receive: reading buffers of "+BUFFER_SIZE+" bytes from client-socket ...");
                 while ( (n=in.read(buffer)) != -1 ) 
                 {
                     size += n;
                     //System.err.print('r'); //// slow!!!
                     out.write(buffer, 0, n);
                     //System.err.print('w'); // slow!!!
                     //System.err.print(' '); // slow!!!
                 }
                 out.flush();
                 System.err.println("\nUploadReciever.receive: Done!  returning "+size);
 
                 }
             finally
             {
                 if(out!=null)
                     out.close();
                 System.err.println("UploadReciever.receive: File output stream is closed.");
                 in.close();
                 clientSocket.close();
             }
         }
     }
 
     private static String getCurrentDateString()
     {
         Date currentDate;
         int month, day, year, hour, min, sec;
         String output="";
 
         currentDate = new Date();
         month = (currentDate.getMonth()+1)%12;
         day = currentDate.getDate();
         year = currentDate.getYear()+1900;
         hour = (currentDate.getHours()+2)%24;
         min = currentDate.getMinutes();
         sec = currentDate.getSeconds();
 
         output+=Integer.toString(year);
         if(month < 10)
             output += "0";
         output += Integer.toString(month);
             output +=".";
         if(day < 10)
             output += "0";
         output +=Integer.toString(day);
         output +=".";
        output +=".";
         if(hour < 10)
             output += "0";
         output +=Integer.toString(hour);
         output +=".";
         if(min < 10)
             output += "0";
         output +=Integer.toString(min);
         output +=".";
         if(sec < 10)
             output += "0";
         output +=Integer.toString(sec);
 
             return output;
             
     }
 
 
 }
 /*    public static void main (String [] args ) throws IOException 
 { 
         // create socket
         ServerSocket servsock = new ServerSocket(13267);
         int bytesRead;
         int current=0;
         int filesize=329890;
         while (true) 
         {
             System.out.println("Waiting..."); 
             Socket sock = servsock.accept();
             System.out.println("Accepted connection : " + sock);
 
             //receive file
             byte [] mybytearray  = new byte [filesize];
             InputStream is = sock.getInputStream();
             //FileInputStream fos = new FileInputStream("airpic.jpg");
             //BufferedInputStream bos = new BufferedInputStream(fos);
             FileOutputStream fos = new FileOutputStream("airpic.jpg");
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             bytesRead = is.read(mybytearray,0,mybytearray.length);
             current = bytesRead;
 
             do
             {
                 bytesRead = is.read(mybytearray, current, (mybytearray.length-current));
                 if(bytesRead >= 0) current += bytesRead;
             }while(bytesRead > -1);
 
             //bos.read(mybytearray, 0, mybytearray.length);
             bos.write(mybytearray, 0, current);
             bos.flush();
             System.out.println("Received file");
             bos.close();
             sock.close();
         }
     }*/
 
