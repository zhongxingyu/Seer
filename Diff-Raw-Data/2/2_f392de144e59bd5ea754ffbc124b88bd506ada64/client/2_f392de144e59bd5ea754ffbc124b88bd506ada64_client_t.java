 import java.io.*;
 import java.net.*;
 import javax.net.ssl.*;
 
 public class client{
 
     private int port;
     private String host; 
 
     public static void main(String[] args) {
           
         client c = new client("localhost", 40302);
          c.getTime();
      
     }
     
     
     public client( String host, int port ){
      
         this.host = host;
         this.port = port;
 
     }
  
             
     public void getTime(){
 
         try{
           
            SSLSocketFactory ssf = (SSLSocketFactory)SSLSocketFactory.getDefault();
            SSLSocket s = (SSLSocket)ssf.createSocket(this.host,  this.port );
               
            BufferedReader in = new BufferedReader(new InputStreamReader( s.getInputStream() ) );
            PrintWriter out = new PrintWriter( s.getOutputStream(), true);
            out.println( "What is the time?" );
 
            String line = null;
            if ( ( line = in.readLine() ) != null ){
                   
                System.out.println( line );
 
                out.close();
                in.close();
                s.close();
 
            }
 
 
         }catch(Exception e){
 
             System.out.println( e.getMessage() );
             System.exit(-1);
         }
 
 
     }
 
 
 }
