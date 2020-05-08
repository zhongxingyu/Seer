 /**
  *
  * @author Devasia
  */
 
 import java.io.*;
 import java.net.*;
 import java.util.ArrayList;
 
 public class MultiThreadedServer implements Runnable {
    Socket sock;
     MultiThreadedServer(Socket csocket) {
         this.sock = csocket;
     }
 
     @Override
     public void run() {
         try {
             /* open input stream and read all the bytes */
             BufferedReader rd=new BufferedReader(new InputStreamReader(sock.getInputStream()));
             String line=null, mess="";
             while((line=rd.readLine())!=null){
                 if(line.equals("")){
                     break;
                 }
                 else{
                     mess=mess+line+"\n";
                 }
             }
 
             /* parse HTTPRequest */
             HTTPParser parser=new HTTPParser(mess);
             System.out.print(parser.toHTTPString());
             
             /* open socket to server and make request 
              * 
              * IMPORTANT: changed host to usatoday.com to prevent redirect loop, 
              * change back to 'parser.returnHost()' after tests*/
             //Socket s=new Socket(parser.returnHost(), 80);
             Socket s=new Socket("usatoday.com", 80);
             BufferedWriter wt=new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
             wt.write(parser.toHTTPString());
             wt.flush();
             
             /* read reponse headers with BufferedReader and binary content with InputStream */
             BufferedReader serverReader=new BufferedReader(new InputStreamReader(s.getInputStream()));
             String line2=null, mess2="";
             while((line2=serverReader.readLine())!=null){
                 if(line2.equals("")){
                     /* finished reading reponse headers */
                     break;
                 }
                 
                 else{
                    mess2=mess2+line+"\n";
                 }
                 
                 /* parse response headers to find 'Content-Length' */
                 HTTPParser p=new HTTPParser(mess2);
                 String length=p.returnHeaderValue("Content-Length");
                 int contentLength=Integer.parseInt(length);
                 
                 /* read binary content */
                 InputStream binaryReader=s.getInputStream();
                 byte[] b=new byte[contentLength];
                 binaryReader.read(b);
                 
                 HTTPResponse response=new HTTPResponse(mess2, b);
                 byte[] totalData=response.toHTTPBytes();
                 
                 /* write response to client */
                 DataOutputStream os=new DataOutputStream(sock.getOutputStream());
                 os.write(totalData);
                 os.flush();
             }
             
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 }
