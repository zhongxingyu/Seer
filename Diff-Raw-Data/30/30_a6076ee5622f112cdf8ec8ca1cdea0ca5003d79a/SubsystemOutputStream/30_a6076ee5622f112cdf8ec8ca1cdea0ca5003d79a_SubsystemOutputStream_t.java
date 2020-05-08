 /*
  *  SSHTools - Java SSH2 API
  *
  *  Copyright (C) 2002-2003 Lee David Painter and Contributors.
  *
  *  Contributions made by:
  *
  *  Brett Smith
  *  Richard Pernavas
  *  Erwin Bolwidt
  *
  *  This program is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU General Public License
  *  as published by the Free Software Foundation; either version 2
  *  of the License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 package com.sshtools.j2ssh.subsystem;
 
 import com.sshtools.j2ssh.io.ByteArrayReader;
 import com.sshtools.j2ssh.transport.InvalidMessageException;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 
 /**
  *
  *
  * @author $author$
  * @version $Revision: 1.21 $
  */
 public class SubsystemOutputStream extends OutputStream {
 	
 	/**
 	 * The number of bytes at the start of a message, which contain
 	 * an unsigned 32-bit integer indicating the length of the
 	 * _rest_ of the message.
 	 */
 	private static final int LENGTH_BYTES = 4;
 
 	private static final Log LOG = LogFactory.getLog(SubsystemOutputStream.class);
 	
     // Temporary storage buffer to build up a message
     ByteArrayOutputStream buffer = new ByteArrayOutputStream();
     SubsystemMessageStore messageStore;
     int messageStart = 0;
 
     /**
      * Creates a new SubsystemOutputStream object.
      *
      * @param messageStore
      */
     public SubsystemOutputStream(SubsystemMessageStore messageStore) {
         super();
         this.messageStore = messageStore;
     }
 
     /**
      *
      *
      * @param b
      * @param off
      * @param len
      *
      * @throws IOException
      */
     public void write(byte[] b, int off, int len) throws IOException {
         // Write the data
     	//LOG.debug("Writing");
         super.write(b, off, len);
         processMessage();
     }
    
    @Override
    public void close() throws IOException {
    	try {
    		LOG.debug("Closing subsystem output stream");
    		processMessage();
    	} finally {
    		super.close();
    	}
    }
 
     /**
      *
      *
      * @param b
      *
      * @throws IOException
      */
     public void write(int b) throws IOException {
         buffer.write(b);
     }
 
     private void processMessage() throws IOException {
         // Now try to process a message
         if (buffer.size() > (messageStart + LENGTH_BYTES)) {
             int messageLength = (int) ByteArrayReader.readInt(buffer.toByteArray(),
                     messageStart);
             
             if (messageLength + messageStart <= (buffer.size() - LENGTH_BYTES)) {
            	//LOG.debug("Message: "+messageLength+", messageStart:"+messageStart+", buffer:"+(buffer.size() - LENGTH_BYTES));
                 byte[] msgdata = new byte[messageLength];
 
                 // Process a message
                 System.arraycopy(buffer.toByteArray(), messageStart + LENGTH_BYTES,
                     msgdata, 0, messageLength);
 
                 try {
                     messageStore.addMessage(msgdata);
                 } catch (InvalidMessageException ime) {
                     throw new IOException(
                         "An invalid message was encountered in the outputstream: " +
                         ime.getMessage());
                 }
 
                if (messageLength + messageStart == (buffer.size() - LENGTH_BYTES)) {
                	//LOG.debug("Message reaches end of buffer. resetting it.");
                     buffer.reset();
                     messageStart = 0;
                 } else {
                	//LOG.debug("Extra bytes at the end of the message. Moving messageStart.");
                    messageStart += messageLength + LENGTH_BYTES;
                 }
             } else {
             	//LOG.debug("Not enough bytes in buffer to make the message");
             }
         } else {
        	LOG.debug("Haven't received the message length yet");
         }
     }
 }
