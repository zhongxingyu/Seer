 /*
  * $Id$
  *
  * Authors:
  *      Jeff Buchbinder <jeff@freemedsoftware.org>
  *
  * FreeMED Electronic Medical Record / Practice Management System
  * Copyright (C) 1999-2010 FreeMED Software Foundation
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  */
 
 package org.freemedsoftware.device;
 
 import gnu.io.CommPortIdentifier;
 import gnu.io.NoSuchPortException;
 import gnu.io.PortInUseException;
 import gnu.io.SerialPort;
 import gnu.io.UnsupportedCommOperationException;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import org.apache.log4j.Logger;
 
 public class SerialInterface {
 
 	static final Logger log = Logger.getLogger(SerialInterface.class);
 
 	public enum PortStatus {
 		OPEN, CLOSED, ERROR
 	};
 
 	protected InputStream in = null;
 	protected OutputStream out = null;
 	protected SerialPort serialPort = null;
 	protected PortStatus portStatus = PortStatus.CLOSED;
 
 	public static char LF = 0x0a;
 	public static char CR = 0x0d;
 
 	public SerialInterface() {
 		log.info("Initializing ...");
 	}
 
 	public void open(String portName, int baud, int timeout)
 			throws NoSuchPortException, PortInUseException,
 			UnsupportedCommOperationException, IOException {
 		log.info("open() : " + portName + "/" + baud);
 		CommPortIdentifier portId = CommPortIdentifier
 				.getPortIdentifier(portName);
 		log.info("Attempting to open " + portId.getName());
 		serialPort = (SerialPort) portId.open("Serial", timeout);
 		serialPort.setSerialPortParams(baud, SerialPort.DATABITS_8,
 				SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
 		serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
 		serialPort.enableReceiveThreshold(1);
 		serialPort.enableReceiveTimeout(timeout);
 		out = serialPort.getOutputStream();
 		in = serialPort.getInputStream();
 		portStatus = PortStatus.OPEN;
 	}
 
 	public void write(String data) throws IOException {
 		byte[] content = data.getBytes();
 		int readCount = 0;
 		for (int iter = 0; iter < content.length; iter++) {
 			// Write out the content
 			out.write(content[iter]);
 			readCount++;
 		}
 
 		// End of line CR
 		out.write(CR);
 		readCount++;
 
 		for (int iter = 0; iter < readCount; iter++) {
 			int ch = in.read();
 			if (ch == -1) {
 				return;
 			} else {
 				log.info("during write, read echo char " + ch + " ("
 						+ (char) ch + ")");
 			}
 		}
 	}
 
 	public String read() throws IOException {
 		String o = new String();
 		int b = -1;
 		b = in.read();
 		while (b != -1) {
 			log.info("read() got character " + b + "(" + (char) b + ")");
 			o += (char) b;
 			b = in.read();
 		}
 		return o;
 	}
 
 	public void close() {
 		if (portStatus == PortStatus.OPEN) {
 			portStatus = PortStatus.CLOSED;
			try {
				in.close();
				out.close();
			} catch (IOException ex) {
			}
 		}
 	}
 }
