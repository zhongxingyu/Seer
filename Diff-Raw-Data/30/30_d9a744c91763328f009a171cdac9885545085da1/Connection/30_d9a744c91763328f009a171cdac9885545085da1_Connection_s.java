 /*
  * The MIT License
  *
  * Copyright 2013 Jason Unger <entityreborn@gmail.com>.
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
  */
 package me.entityreborn.socbot.api;
 
 import java.io.IOException;
 import javax.net.SocketFactory;
 
 /**
  *
  * @author Jason Unger <entityreborn@gmail.com>
  */
 public interface Connection {
 
     /**
      * Connect the bot to a given server. <p> The server will connect to port
      * 6667 by default.
      *
      * @param server server to connect to
      */
     public void connect(String server) throws IOException;
 
     /**
      * Connect the bot to a given server and port.
      *
      * @param server server to connect to
      * @param port port to connect to
      */
     public void connect(String server, int port) throws IOException;
 
     /**
      * Connect the bot to a given server and port, providing a server password.
      *
      * @param server server to connect to
      * @param port port to connect to
      * @param password password to use during registration
      */
     public void connect(String server, int port, String password) throws IOException;
 
     /**
      * Connect the bot to a given server and port, providing a server password
      * and socketfactory.
      *
      * @param server server to connect to
      * @param port port to connect to
      * @param password password to use during registration
      * @param factory socketfactory to use to provide sockets
      * @throws java.io.IOException
      */
     public void connect(String server, int port, String password, SocketFactory factory) throws IOException;
 
     /**
      * Forcefully shut down everything and return the bot into a disconnected
      * state.
      * @param clean Signifies if the disconnect was clean (non-exception) or not.
      */
     public void disconnect(boolean clean);
 
     /**
      * Get the nickname used while registering.
      *
      * @return the nickname
      */
     public String getNickname();
 
     /**
      * Get the realname used while registering.
      *
      * @return the realname
      */
     public String getRealname();
 
     /**
      * Get the username used while registering.
      *
      * @return the username
      */
     public String getUsername();
 
     /**
      * Check if a full connection has been established.
      *
      * @return true if we are fully connected.
      */
     public boolean isConnected();
 
     /**
      * Check if we are attempting to connect, and haven't established a full
      * connection yet.
      *
      * @return true if we are still connecting.
      */
     public boolean isConnecting();
 
     /**
      * Send a line to the server.
      *
      * @param line
      */
     public void sendLine(String line);
 
     /**
      * Send a line now, overriding any output queue or throttling.
      *
      * @param line line to send.
      */
     public void sendLineNow(String line);
 
     /**
      * Send a PING to the server. <p> Uses the current time in millis as the
      * ping message.
      */
     public void sendPing();
 
     /**
      * Set the nickname to use during the registration process. <p> Will fail
      * silently if we have registered already, as it would be pointless
      * otherwise. If we are still in the registration phase (ie if we got an
      * nickname in use error), it will send a NICK line to the server as well.
      *
      * @param nickname the nickname to set
      */
     public void setNickname(String nickname);
 
     /**
      * Set the realname to use during the registration process. <p> Will fail
      * silently if we have registered already, as it would be pointless
      * otherwise.
      *
      * @param realname the realname to set
      */
     public void setRealname(String realname);
 
     /**
      * Set the username to use during the registration process. <p> Will fail
      * silently if we have registered already, as it would be pointless
      * otherwise.
      *
      * @param username the username to set
      */
     public void setUsername(String username);
 }
