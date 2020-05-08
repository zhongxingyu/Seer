 /*
  * Copyright (C) 2013 mewin<mewin001@hotmail.de>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.mewin.jhttp;
 
 import de.mewin.jhttp.http.HttpHeader;
 import de.mewin.jhttp.http.ProtocolException;
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
import java.io.Reader;
 import java.net.Socket;
 import java.util.HashMap;
 
 /**
  *
  * @author mewin<mewin001@hotmail.de>
  */
 public class HttpConnection
 {
     private Socket socket;
     private Thread listenerThread;
     private HttpServer server;
     
     public HttpConnection(Socket socket, HttpServer server)
     {
         this.socket = socket;
         this.server = server;
         listen();
     }
     
     private void listen()
     {
         listenerThread = new Thread()
         {
             @Override
             public void run()
             {
                 while(!socket.isClosed() && socket.isConnected())
                 {
                     try
                     {
                         readQuery(socket.getInputStream());
                     }
                     catch(Exception ex)
                     {
                         handleThrowable(ex);
                     }
                 }
             }
         };
         
         listenerThread.start();
     }
     
     public void sendQuery(HttpHeader header, String body) throws IOException
     {
         try
         {
             OutputStream out = socket.getOutputStream();
             if (!body.equals(""))
             {
                 header.addHeaderValue("Content-Length", String.valueOf(body.length()));
             }
             String toSend = header.toHTTP() + body;
             
             for (char chr : toSend.toCharArray())
             {
                 out.write(chr);
             }
         }
         catch(IOException ex)
         {
             throw ex;
         }
         catch(Exception ex)
         {
             handleThrowable(ex);
         }
     }
     
     public void sendQuery(HttpHeader header, InputStream body) throws IOException
     {
         try
         {
             BufferedInputStream input = new BufferedInputStream(body);
             OutputStream out = socket.getOutputStream();
             String toSend = header.toHTTP();
             
             for (char chr : toSend.toCharArray())
             {
                 out.write(chr);
             }
             
             int r;
             while((r = input.read()) > -1)
             {
                 out.write(r);
             }
         }
         catch(IOException ex)
         {
             throw ex;
         }
         catch(Exception ex)
         {
             handleThrowable(ex);
         }
     }
     
     private void readQuery(InputStream in)
     {
         try
         {
             HttpHeader header = new HttpHeader(in);
             HashMap<String, String> headers = header.getHeaderValues();
             String body = "";
             
             if (headers.containsKey("Content-Length"))
             {
                 for (int i = 0; i < Integer.parseInt(headers.get("Content-Length").trim()); i++)
                 {
                     int r = in.read();
                    if (r == 0)
                     {
                         throw new ProtocolException("Unexpected end of stream.");
                     }
                     body += (char) r;
                 }
             }
             
             server.handleQuery(this, header, body);
         }
        catch(ProtocolException ex)
         {
            close();
            // TODO: handle protocol errors
        }
        catch(NumberFormatException ex2)
        {
            close();
            // TODO: handle protocol errors
         }
        catch(IOException ex3)
         {
             close();
            // TODO: handle protocol errors
         }
         catch(Exception ex4)
         {
             handleThrowable(ex4);
         }
     }
     
     public void close()
     {
         try
         {
             socket.close();
         }
         catch(Exception ex)
         {
             handleThrowable(ex);
         }
     }
     
     private void handleThrowable(Throwable t)
     {
         server.handleThrowable(t);
     }
 }
