 /*
  * GCF -- Generic Connection Framework for Java SE
  *        GCF-Comm (RXTX)
  *
  * Copyright (c) 2007-2010 Marcel Patzlaff (marcel.patzlaff@gmail.com)
  *
  * This library is free software: you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published
  * by the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this library.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.github.gcf.io.comm;
 
 import gnu.io.SerialPort;
 import gnu.io.UnsupportedCommOperationException;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import javax.microedition.io.CommConnection;
 
 import com.github.gcf.io.AbstractConnection;
 
 
 /**
  * @author Marcel Patzlaff
  */
 final class RXTXCommConnectionImpl extends AbstractConnection implements CommConnection {
     private final SerialPort _port;
     
     protected RXTXCommConnectionImpl(SerialPort port) {
         _port= port;
     }
     
    public int getBaudRate() {
         return _port.getBaudRate();
     }
 
    public int setBaudRate(int baudrate) {
         try {
             _port.setSerialPortParams(
                 baudrate,
                 _port.getDataBits(),
                 _port.getStopBits(),
                 _port.getParity()
             );
         } catch (UnsupportedCommOperationException e) {
             System.err.println("Cannot change baud rate");
         }
         
         return _port.getBaudRate();
     }
 
     public DataInputStream openDataInputStream() throws IOException {
         ensureOpen();
         return new DataInputStream(openInputStream());
     }
 
     public InputStream openInputStream() throws IOException {
         ensureOpen();
         return new RegisteredInputStream(_port.getInputStream());
     }
 
     public DataOutputStream openDataOutputStream() throws IOException {
         ensureOpen();
         return new DataOutputStream(openOutputStream());
     }
 
     public OutputStream openOutputStream() throws IOException {
         ensureOpen();
         return new RegisteredOutputStream(_port.getOutputStream());
     }
 
     protected void closeMainResource() throws IOException {
         _port.close();
     }
 }
