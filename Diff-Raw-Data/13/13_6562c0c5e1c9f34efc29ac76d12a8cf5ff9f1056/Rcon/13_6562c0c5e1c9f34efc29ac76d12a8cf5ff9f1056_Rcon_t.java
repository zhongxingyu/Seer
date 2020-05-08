 /**
  * Copyright (c) 2013, Daniele Pantaleone
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  * 
  * @author      Daniele Pantaleone, Mathias Van Malderen
  * @version     1.0
  * @copyright   Daniele Pantaleone, 28 July, 2013
  * @package     com.orion.misc
  **/
 
 package com.orion.misc;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.SocketTimeoutException;
 import java.net.UnknownHostException;
 
 import com.orion.exception.RconException;
 
 public class Rcon {
     
     private final String      ENCODING             = "UTF-8";
     private final String      COLOR_PATTERN        = "\\^[0-9]{1}";
     
     private       int         PACKET_SIZE          = 1400;
     private       int         PACKET_TIMEOUT       = 2000;
     private       int         PACKET_TIMEOUT_MULTI = 300;
     private       long        SOCKET_DELAY         = 200;
     
     private final InetAddress ip;
     private final int         port;
     private final String      password;
     
     private       long        lastCmdTime;
     
     
     /**
     * Build a new <tt>Rcon</tt> object
      * 
      * @author Daniele Pantaleone
      * @param  ip The ip/domain where RCON packets should be sent
      * @param  port The port on which the server is awaiting RCON packets
      * @param  password The server RCON password
      * @throws IllegalArgumentException If the given port number is not in range [1-65535]
      **/
     public Rcon(InetAddress ip, int port, String password) throws IllegalArgumentException {
         
         if ((port < 1) || (port > 65535))
             throw new IllegalArgumentException("port must be in range [1-65535]");
         
         this.ip = ip;
         this.port = port;
         this.password = password;
         
     }
     
     
     /**
     * Build a new <tt>Rcon</tt> object
      * 
      * @author Daniele Pantaleone
      * @param  ip The ip/domain where RCON packets should be sent
      * @param  port The port on which the server is awaiting RCON packets
      * @param  password The server RCON password
      * @throws IllegalArgumentException If the given port number is not in range [1-65535]
      * @throws UnknownHostException If the given ip/domain is not valid
      **/
     public Rcon(String ip, int port, String password) throws IllegalArgumentException, UnknownHostException {
         this(InetAddress.getByName(ip), port, password);
     }
     
     
     /**
      * Set the packet size
      * 
      * <br><br>
      * DEFAULT: <b>1400</b>
      * 
      * @author Daniele Pantaleone
      * @param  size The number of bytes of each packet
      * @throws IllegalArgumentException If the specified size is not valid
      **/
     public void setPacketSize(int size) throws IllegalArgumentException {
     
         if (size < 1)
             throw new IllegalArgumentException("packet size must be a positive value");
     
         PACKET_SIZE = size;
     
     }
     
     
     /**
      * Set the socket timeout value for the first packet received 
      * from the server upon an RCON command being issued
      * 
      * <br><br>
      * DEFAULT: <b>2000</b>
      * 
      * @author Daniele Pantaleone
      * @param  timeout The first packet timeout in milliseconds
      * @throws IllegalArgumentException If the specified timeout is not valid
      **/
     public void setPacketTimeout(int timeout) throws IllegalArgumentException {
         
         if (timeout < 1)
             throw new IllegalArgumentException("packet timeout must be a positive value");
         
         PACKET_TIMEOUT = timeout;
        
     }
     
     
     /**
      * Set the socket timeout value for multiple packets received from the server
      * after the first one being collected upon an RCON command being issued
      * 
      * <br><br>
      * DEFAULT: <b>300</b>
      * 
      * @author Daniele Pantaleone
      * @param  timeout The first packet timeout in milliseconds
      * @throws IllegalArgumentException If the specified timeout is not valid
      **/
     public void setMultiPacketTimeout(int timeout) throws IllegalArgumentException {
         
         if (timeout < 1)
             throw new IllegalArgumentException("multi packet timeout must be a positive value");
         
         PACKET_TIMEOUT_MULTI = timeout;
        
     }
     
     
     /**
      * Set the delay between two consequential RCON commands
      * 
      * <br><br>
      * DEFAULT: <b>200</b>
      * 
      * @author Daniele Pantaleone
      * @param  delay The amount of milliseconds between two consequential RCON commands
      * @throws IllegalArgumentException If the specified delay is not valid
      **/
     public void setSocketDelay(int delay) {
         
         if (delay < 0)
             throw new IllegalArgumentException("socket delay must be a positive value");
         
         SOCKET_DELAY = delay;
        
     }
    
     
     /**
      * Build and return a <tt>DatagramPacket</tt> according the given RCON command
      * 
      * @author Daniele Pantaleone, Mathias Van Malderen
      * @param  command The RCON command to be sent
      * @throws UnsupportedEncodingException If the specified encoding is not supported by the server
      * @return A <tt>DatagramPacket</tt> built according the given RCON command
      **/
     private DatagramPacket getDatagramPacket(String command) throws UnsupportedEncodingException {
         
         byte[] buff = command.getBytes(ENCODING);
         byte[] send = new byte[buff.length + 5];
         
         send[0] = (byte)0xFF;
         send[1] = (byte)0xFF;
         send[2] = (byte)0xFF;
         send[3] = (byte)0xFF;
         
         // Copying byte to byte the buffer into the
         // byte array we'll send over the UDP socket
         for (int i = 0; i < buff.length; i++) 
             send[i + 4] = buff[i];
         
         send[buff.length + 4] = (byte)0x00;
         
         return new DatagramPacket(send, send.length, this.ip, this.port);
         
     }
     
     
     /**
      * Write a command on the RCON UDP socket
      * 
      * @author Daniele Pantaleone
      * @param  command The command to be sent over the UDP socket
      * @param  socket The UDP socket where to write the command on
      * @throws UnsupportedEncodingException If the specified encoding is not supported by the server
      * @throws IOException If an error occurs while writing on the UDP socket
      **/
     private void write(String command, DatagramSocket socket) throws UnsupportedEncodingException, IOException {
         
         long currentTime = System.currentTimeMillis();
         
         if ((currentTime - this.lastCmdTime) < SOCKET_DELAY) {
             
             try {
                 
                 // Be sure not to overflow the server with RCON packets
                 // otherwise it will may discard them and we would not
                 // notice it due to UDP being connection less
                 Thread.sleep(SOCKET_DELAY - (currentTime - this.lastCmdTime));
             
             } catch (InterruptedException e) { } 
             
         }
         
         assert socket != null;
         
         socket.send(this.getDatagramPacket("rcon " + this.password + " " + command));
         this.lastCmdTime = System.currentTimeMillis(); 
         
     }
     
     
     /**
      * Read a command response from the RCONUDP socket
      * 
      * @author Daniele Pantaleone
      * @param  socket The UDP socket from which to read data
      * @throws IOException If an error occurs while fetching data from the UDP socket
      * @return The RCON command response 
      **/
     private String read(DatagramSocket socket) throws IOException {
         
         StringBuilder builder = new StringBuilder(PACKET_SIZE);
         DatagramPacket packet = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);   
         
         assert socket != null && !socket.isClosed();
         
         socket.setSoTimeout(PACKET_TIMEOUT);
         socket.receive(packet);
         
         builder.append(new String(packet.getData(), 10, packet.getLength() - 10));
         
         try {
             
             // Wait for possible multiple packets
             packet = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);
             socket.setSoTimeout(PACKET_TIMEOUT_MULTI);
             
             while (true) {
                 socket.receive(packet);
                 builder.append(new String(packet.getData(), 10, packet.getLength() - 10));
             }
             
         } catch(SocketTimeoutException e) { }
         
         return builder.toString()
                       .replaceAll(COLOR_PATTERN, "")
                       .trim();
         
     }
     
     
     /**
      * Send an RCON command<br>
      * Will return the command response if specified in the
      * method execution and if there is a valid server response<br>
      * Will return <tt>null</tt> if the command response is not valid
      *
      * @author Daniele Pantaleone
      * @param  command The RCON command to be sent
      * @param  read <tt>true</tt> if the method should wait for a response, <tt>false</tt> otherwise
      * @throws RconException If the RCON command couldn't be sent
      * @return The server command response or <tt>null</tt> if the response it's not valid
      **/
     public synchronized String send(String command, boolean read) throws RconException {
         
         DatagramSocket socket = null;
         String response = null;
         
         try {
             
             socket = new DatagramSocket();
             this.write(command, socket);
             
             if (read)
                 response = read(socket);
         
         } catch (IOException e) {
            
             // Re-throw our custom Exception
             throw new RconException("could not send command: " + command, e);
             
         } finally {
             
             // Closing the socket if necessary
             if ((socket != null) && (!socket.isClosed())) {
                 socket.close();
             }
             
         }
         
         return response;
        
     }
     
     
     /**
      * Send an RCON command<br>
      * The command response is discarded and the UDP socket is 
      * closed right after the command being sent<br>
      *
      * @author Daniele Pantaleone
      * @param  command The RCON command to be sent
      * @throws RconException If the RCON command couldn't be sent
      **/
     public synchronized void send(String command) throws RconException {
         this.send(command, false);
     }
        
 }
