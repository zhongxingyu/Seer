 /**
  *
  * @author Devasia
  */
 
 import java.io.*;
 import java.net.*;
 
 public class MultiThreadedServer implements Runnable {
    Socket sock;
     MultiThreadedServer(Socket csocket) {
         this.sock = csocket;
     }
 
     @Override
     public void run() {
         try {            
             /* open client input stream and read all the bytes */
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
             
             /* open socket to server and make request 
              * 
              * IMPORTANT: changed host to usatoday.com to prevent redirect loop, 
              * change back to 'parser.returnHost()' after tests*/
            Socket s=new Socket(parser.returnHost(), 80);
            //Socket s = new Socket("usatoday.com", 80);
             BufferedWriter wt = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
             wt.write(parser.toHTTPString());
             wt.flush();
 
             /* read response as a stream of bytes with InputStream */
             InputStream binaryReader = s.getInputStream();
             /* we're using a 1MB byte array to store data...will that be enough? what is we need to store more data? */
             byte[] data=new byte[1048576];
             int dataRead=binaryReader.read(data);
             /* move binary data into smaller byte array */
            byte[] data_small=new byte[dataRead];
            System.arraycopy(data, 0, data_small, 0, dataRead);
             /* mark larger byte array for garbage collection */
             data=null;
             
             /* parse HTTP response and store in an object */
             HTTPResponse response = new HTTPResponse(data_small);
             byte[] totalData = response.toHTTPBytes();
 
             /* write response to client */
             DataOutputStream os = new DataOutputStream(sock.getOutputStream());
             os.write(totalData);
             os.flush();
 
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 }
