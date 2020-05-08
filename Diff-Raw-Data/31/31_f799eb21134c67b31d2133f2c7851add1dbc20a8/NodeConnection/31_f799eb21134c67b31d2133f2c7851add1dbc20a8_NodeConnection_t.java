 /*
  * Copyright (C) 2009  Lars Pötter <Lars_Poetter@gmx.de>
  * All Rights Reserved.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License version 2
  * as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, see <http://www.gnu.org/licenses/>
  *
  */
 
 package org.FriendsUnited.UserInterface;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.ConnectException;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import org.FriendsUnited.Util.ByteConverter;
 import org.FriendsUnited.Util.Tool;
 import org.apache.log4j.Logger;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 import org.jdom.output.Format;
 import org.jdom.output.XMLOutputter;
 
 /**
  *
  * @author Lars P&ouml;tter
  * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
  */
 public abstract class NodeConnection
 {
     private final Logger log = Logger.getLogger(this.getClass().getName());
     private Socket sock;
     private String IPAddresse;
     private int Port;
     private InputStream in;
     private OutputStream out;
 
     /**
      *
      * @param IPAddresse
      * @param Port
      */
     public NodeConnection(final String IPAddresse, final int Port)
     {
         log.debug("Starting to create connection");
         this.IPAddresse = IPAddresse;
         this.Port = Port;
        sock = createSocket();
         log.debug("end of constructor !");
     }
 
     protected abstract Socket createSocket();
 
     public final boolean canConnect()
     {
         if(null == sock)
         {
             log.error("Socket is null !");
             return false;
         }
         else
         {
             if(false == sock.isConnected())
             {
                 InetSocketAddress isa;
                 try
                 {
                     log.debug("trying to connect");
                     isa = new InetSocketAddress(InetAddress.getByName(IPAddresse), Port);
                     sock.connect(isa);
                     out = sock.getOutputStream();
                     in = sock.getInputStream();
                     final int firstbyte = in.read();
                     if(-1 == firstbyte)
                     {
                         System.err.println("Could not read from Server");
                         sock.close();
                         return false;
                     }
                     if(0xca != firstbyte)
                     {
                         System.err.println("Server send invalid start sign of " + firstbyte + " expected 0xca !");
                         sock.close();
                         return false;
                     }
                     final int secondbyte = in.read();
                     if(-1 == secondbyte)
                     {
                         System.err.println("Could not read second byte from Server");
                         sock.close();
                         return false;
                     }
                     if(0xfe != secondbyte)
                     {
                         System.err.println("Server send invalid start sign of " + secondbyte + " expected 0xfe !");
                         sock.close();
                         return false;
                     }
                     log.debug("!!!! Recieved start sign !!!!");
                     return true;
                 }
                 catch(final ConnectException e)
                 {
                     // Could not connect
                     log.error(Tool.fromExceptionToString(e));
                 }
                 catch (final UnknownHostException e)
                 {
                     log.error(Tool.fromExceptionToString(e));
                 }
                 catch (final IOException e)
                 {
                     log.error(Tool.fromExceptionToString(e));
                 }
                 return false;
             }
             else
             {
                 // We are already connected
                 return true;
             }
         }
     }
 
     public final Element executeRequest(final Element Request)
     {
         if(false == canConnect())
         {
             return null;
         }
         else
         {
             final Document reqDoc = new Document();
             reqDoc.setRootElement(Request);
             final Format format = Format.getCompactFormat();
             final XMLOutputter xout = new XMLOutputter(format);
             try
             {
                 final ByteArrayOutputStream bout = new ByteArrayOutputStream();
                 xout.output(reqDoc, bout);
                 final int requestlength = bout.size();
                 ByteConverter bc = new ByteConverter();
                 bc.add(requestlength);
                 final byte[] repsz = bc.toByteArray();
                 // Send size of Request
                 log.debug("Sending Request with a size of " + requestlength + " bytes !");
                 out.write(repsz);
                 log.debug(bout.toString());
                 // Send Request
                 bout.writeTo(out);
                 log.debug("Request has been send !");
                 // Now Read Answer
                 final byte[] lengthbuf = new byte[4];
                 if(4 != in.read(lengthbuf))
                 {
                     // Could not read Length
                     log.error("Could not read Length");
                     sock.close();
                     return null;
                 }
                 log.debug("Recieved Length as : "
                           + lengthbuf[0] + ", "
                           + lengthbuf[1] + ", "
                           + lengthbuf[2] + ", "
                           + lengthbuf[3] + " !");
                 bc = new ByteConverter();
                 bc.add(lengthbuf);
                 final int length = bc.getInt();
                 log.debug("Reading Reply of size " + length + " bytes !");
                 final byte[] replyBytes = new byte[length];
                 if(length != in.read(replyBytes))
                 {
                     // Could not read Reply
                     sock.close();
                     return null;
                 }
                 final ByteArrayInputStream bin = new ByteArrayInputStream(replyBytes);
 
                 final SAXBuilder builder = new SAXBuilder();
                 final Document Replydoc = builder.build(bin);
                 final Element root = Replydoc.getRootElement();
                 return root;
             }
             catch (final IOException e)
             {
                 log.error(Tool.fromExceptionToString(e));
                 try
                 {
                     sock.close();
                 }
                 catch (final IOException e1)
                 {
                     // I don't care
                 }
             }
             catch (final JDOMException e)
             {
                 log.error(Tool.fromExceptionToString(e));
                 try
                 {
                     sock.close();
                 }
                 catch (final IOException e1)
                 {
                     // I don't care
                 }
             }
             return null;
         }
     }
 }
