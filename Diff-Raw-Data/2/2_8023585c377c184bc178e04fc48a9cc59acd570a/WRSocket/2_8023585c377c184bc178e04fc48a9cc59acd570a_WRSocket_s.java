 package io.webrocket.kosmonaut;
 
 import java.io.*;
 import java.net.*;
 import java.util.UUID;
 import java.util.ArrayList;
 
 /**
  *  Internal: Socket is a base class defining tools and helpers used by
  *  Client and Worker implementations.
  */
 public abstract class WRSocket{
     private URI uri;
     
     protected Socket socket = null;
     
     private String identity;
     
     public WRSocket(String uri){
         this.uri = URI.create(uri);
     }
 
     /**
      *  Internal: Connect creates new connection with the backend endpoint.
      *
      *  timeout - A value of the maximum executing time (float).
      *
      */
     public Socket connect(float timeout){
         try{
             int secs = (int) timeout;
             String server = this.uri.getHost();
             this.socket = new Socket(server, this.uri.getPort());
             this.socket.setSoTimeout(secs);
             generateIdentity();
             return this.socket;
         }catch (UnknownHostException e){
             System.err.println("Can't find host: " + this.uri.getHost());
             return null;
         } catch (IOException e) {
             System.err.println("Couldn't get I/O for "
                                + "the connection to: " + this.uri.getHost());
             return null;
         } catch (Exception e_){
             System.err.println("Error: " + e_.getMessage());
             return null;
         }
     }
     
     protected abstract String getSocketType();
     
     /**
      *  Internal: Generates unique identity for the socket connection.
      *  Identity is composed from the following parts:
      *
      * [socket-type]:[vhost]:[vhost-token]:[uuid]
      *
      */
     public void generateIdentity(){
         this.identity = new String(this.getSocketType() + ":" + this.uri.getPath() + ":" + this.uri.getUserInfo() + ":" + UUID.randomUUID());
     }
 
     /**
      *  Internal: Pack converts given payload into single packet in format
      *  defined by WebRocket Backend Protocol.
      *
      * Packet format
      *
      * 0x01 | identity \n | *
      * 0x02 | \n | *
      * 0x03 | command \n |
      * 0x04 | payload... \n | *
      * 0x.. | ... \n | *
      * | \r\n\r\n |
      *
      * * - optional field
      *
      * payload - The data to be packed.
      * with_identity - Whether identity should be prepend to the packet.
      *
      * Returns packed data.
      */
     public String pack(ArrayList<String> payload, boolean withIdentity){
         StringBuilder response = new StringBuilder();
         if (withIdentity){
         	response.append(this.identity + "\n");
             response.append("\n");
         }
         for(String data : payload){
             response.append(data);
             response.append("\n");
         }
         response.append("\n\r\n\r\n");
         return response.toString();
     }
 
     public ArrayList<String> recv(Socket socket){
     	return read(socket);
     }
 
     public Boolean write(String packet){
         try{
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.print(packet);
             return true;
         }catch (Exception e){
             //TODO Log
             System.err.println("ERROR: " + e.getMessage());
             return false;
         }
     }
 
     public ArrayList<String> read(Socket socket){
         try{
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             ArrayList<String> lines = new ArrayList<String>();
             String line;
             while( (line = reader.readLine()) != null){
             	lines.add(line);
             }
             return lines;
         }catch (Exception e){
             //TODO log
             System.err.println("ERROR: " + e.getMessage());
             return null;
         }
     }
 
     public void closeSocket(){
         try{
             this.socket.close();
         }catch (IOException e){
             //TODO log
             System.err.println("ERROR: " + e.getMessage());
         }
     }
 }
