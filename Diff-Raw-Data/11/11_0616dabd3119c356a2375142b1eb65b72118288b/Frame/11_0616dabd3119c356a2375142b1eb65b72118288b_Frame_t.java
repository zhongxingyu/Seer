 /*
 * Frame.java            $Revision: 1.8 $ $Date: 2001/05/25 15:28:30 $
  *
  * Copyright (c) 2001 Invisible Worlds, Inc.  All rights reserved.
  *
  * The contents of this file are subject to the Blocks Public License (the
  * "License"); You may not use this file except in compliance with the License.
  *
  * You may obtain a copy of the License at http://www.invisible.net/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied.  See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  */
 package org.beepcore.beep.core;
 
 
 import java.io.UnsupportedEncodingException;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.StringTokenizer;
 
 import org.beepcore.beep.util.Log;
 
 /**
  * Frame encapsulates the MSG, RPY, ERR, ANS and NUL BEEP message types.
  * Contains a the <code>Channel</code> this frame belongs to, the BEEP Frame
  * Payload which holds the BEEP Frames's Header, Trailer, and the message
  * payload.
  *
  * @author Eric Dixon
  * @author Huston Franklin
  * @author Jay Kint
  * @author Scott Pead
 * @version $Revision, $Date: 2001/05/25 15:28:30 $
  *
  * @see FrameDataStream
  * @see BufferSegment
  */
 public class Frame {
 
     public static final String TRAILER = "END\r\n";
     public static final int MAX_HEADER_SIZE = (3        // msg type
                                                + 1      // space
                                                + 10     // channel number
                                                + 1      // space
                                                + 10     // msgno
                                                + 1      // space
                                                + 1      // more
                                                + 1      // space
                                                + 10     // seqno
                                                + 1      // space
                                                + 10     // size
                                                + 1      // space
                                                + 10     // ansno
                                                + 2);    // CRLF
     public static final int MAX_ANS_NUMBER = Integer.MAX_VALUE; // 2147483647;
     public static final int MAX_CHANNEL_NUMBER = Integer.MAX_VALUE;
     public static final int MAX_MESSAGE_NUMBER = Integer.MAX_VALUE;
     public static final long MAX_SEQUENCE_NUMBER = 4294967295L;
     public static final int MAX_SIZE = Integer.MAX_VALUE;
 
     private static final String CRLF = "\r\n";
 
     /** BEEP message type of  <code>Frame</code>. */
     private int messageType;
 
     /** <code>Channel</code> to which <code>Frame</code> belongs. */
     private Channel channel;
 
     /** Message number of <code>Frame</code>. */
     private int msgno;
 
     /** Answer number of this BEEP <code>Frame</code>. */
     private int ansno;
 
     /**
      * Sequence number of a BEEP message.  <code>seqno</code> is equal to
      * the 'seqno' of the BEEP message header.
      */
     private long seqno;
 
     /**
      * Size of the payload of a BEEP message.  <code>size</code> is equal to
      * the 'size' of the BEEP message header.
      */
     private int size;
 
     /**
      * Specifies whether this is the final frame of the message (i.e. the
      * continuation indicator 'more' is equal to a '.' and not a '*').
      */
     private boolean last;
 
     /**
      * The payload of a BEEP message.
      */
     private BufferSegment payload;
 
     /**
      * Initializes a new <code>Frame</code> representing a BEEP ANS frame.
      *
      * @param messageType indicates whether a <code>Frame</code> is a MSG,
      *    RPY, ERR, ANS or NUL.
      * @param channel <code>Channel</code> on which the <code>Frame</code> was
      *    sent.
      * @param msgno Message number of the <code>Frame</code>.
      * @param seqno Sequence number of the <code>Frame</code>.
      * @param ansno Answer number of the <code>Frame</code>.
      * @param payload Payload of the <code>Frame</code>.
      * @param last  Indicates if this is the last <code>Frame</code> sent in a
      *    sequence of frames.
      *
      * @see BufferSegment
      */
     Frame(int messageType, Channel channel, int msgno, boolean last,
           long seqno, int ansno, BufferSegment payload)
     {
         this.messageType = messageType;
         this.channel = channel;
         this.msgno = msgno;
         this.seqno = seqno;
         this.ansno = ansno;
         this.payload = payload;
         this.size = payload.length - payload.offset;
         this.last = last;
     }
 
     Frame(int messageType, Channel channel, int msgno, boolean last,
           long seqno, int size, int ansno)
     {
         this.messageType = messageType;
         this.channel = channel;
         this.msgno = msgno;
         this.last = last;
         this.seqno = seqno;
         this.size = size;
         this.ansno = ansno;
         this.payload = null;
     }
 
     /**
      * Adds the <code>BufferSegment</code> to the list representing the
      * payload for this frame.
      */
     public void addPayload(BufferSegment buf)
     {
         this.payload = buf;
     }
 
     /**
      * Returns an <code>iterator</code> to iterate over a collection of
      * <code>BufferSegment</code> objects.
      *
      */
     public Iterator getBytes()
     {
         LinkedList l = new LinkedList();
         l.add(new BufferSegment(buildHeader()));
         if (this.payload != null) {
             l.add(this.payload);
         }
         l.add(new BufferSegment(TRAILER.getBytes()));
         return l.iterator();
     }
 
     /**
      * Returns the <code>payload</code> of a <code>Frame</code>.
      * A <code>BufferSegment</code> contains a BEEP Frames Payload.
      *
      * @see BufferSegment
      */
     public BufferSegment getPayload()
     {
         return this.payload;
     }
 
     /**
      * Returns the message type of this <code>Frame</code>.
      */
     public int getMessageType()
     {
         return this.messageType;
     }
 
     /**
      * Returns the message type of this <code>Frame</code>.
      */
     public String getMessageTypeString()
     {
         return MessageType.getMessageType(this.messageType);
     }
 
     /**
      * Returns the <code>Channel</code> to which this <code>Frame</code>
      * belongs.
      *
      * @see Channel
      */
     public Channel getChannel()
     {
         return this.channel;
     }
 
     /**
      * Returns the message number of this <code>Frame</code>.
      */
     public int getMsgno()
     {
         return this.msgno;
     }
 
     /**
      * Returns the <code>seqno</code> of this <code>Frame</code>.
      */
     public long getSeqno()
     {
         return this.seqno;
     }
 
     /**
      * Returns the <code>size</code> of the payload for this
      * <code>Frame</code>.
      */
     public int getSize()
     {
         return this.size;
     }
 
     /**
      * Returns the answer number of this <code>Frame</code>.
      */
     public int getAnsno()
     {
         return this.ansno;
     }
 
     /**
      * Indicates if this is the last <code>Frame</code> in a sequence of frames
      */
     public boolean isLast()
     {
         return this.last;
     }
 
     /**
      * Builds a BEEP Header from the given <code>Frame</code> and returns it
      * as a byte array.
      *
      * @param f <code>Frame</code> from which we derive the BEEP Header.
      */
     byte[] buildHeader()
     {
         StringBuffer header = new StringBuffer(Frame.MAX_HEADER_SIZE);
 
         // Create header
         header.append(MessageType.getMessageType(this.messageType));
         header.append(' ');
         header.append(this.channel.getNumberAsString());
         header.append(' ');
         header.append(this.msgno);
         header.append(' ');
         header.append((this.last ? '.' : '*'));
         header.append(' ');
         header.append(this.seqno);
         header.append(' ');
         header.append(this.size);
 
         if (this.messageType == Message.MESSAGE_TYPE_ANS) {
             header.append(' ');
             header.append(this.ansno);
         }
 
         header.append(this.CRLF);
 
         Log.logEntry(Log.SEV_DEBUG, header.toString());
         try {
             return header.toString().getBytes("UTF-8");
         } catch (UnsupportedEncodingException e) {
             throw new RuntimeException("UnsupportedEncodingException" +
                                        e.getMessage());
         }
     }
 
     static Frame parseHeader(Session session, byte[] headerBuffer, int length)
         throws BEEPException
     {
         Log.logEntry(Log.SEV_DEBUG_VERBOSE, "Processing normal BEEP frame");
 
         int limit = length;
 
         StringTokenizer st;
         try {
             st = new StringTokenizer(new String(headerBuffer, 0, length,
                                                 "UTF-8"));
         } catch (UnsupportedEncodingException e) {
             throw new RuntimeException("UnsupportedEncodingException" +
                                        e.getMessage());
         }
 
         int count = st.countTokens();
 
         if (!(count == 6 || count == 7)) {
             Log.logEntry(Log.SEV_DEBUG,
                          "Illegal header tokens=" + count + "\n"
                          + new String(headerBuffer));
 
             throw new BEEPException("Malformed BEEP Header");
         }
 
         // Get Message Type
         // Kick out if we've maxed, or if the type gets set.
         int msgType = MessageType.getMessageType(st.nextToken());
 
         // Read the Channel Number
         int channelNum = Integer.parseInt(st.nextToken());
 
         // Read the Message Number
         int msgNum = Integer.parseInt(st.nextToken());
 
         // Read the more flag
         char isLast = st.nextToken().charAt(0);
 
         boolean last;
         if (isLast == '*') {
             last = false;
         } else if (isLast == '.') {
             last = true;
         } else {
 
             Log.logEntry(Log.SEV_DEBUG, "lastFrame=" + isLast);
 
             throw new BEEPException("Malformed BEEP Header");
         }
 
         // Sequence Number
         long seqNum = Long.parseLong(st.nextToken());
 
         // Size
         int size = Integer.parseInt(st.nextToken());
 
         if (size < 0) {
             throw new BEEPException("Malformed BEEP Header");
         }
 
         int ansNum = -1;
        if (count == 7) {
 
             // AnsNo
             ansNum = Integer.parseInt(st.nextToken());
         }
 
         return new Frame(msgType, session.getValidChannel(channelNum), msgNum,
                          last, seqNum, size, ansNum);
     }
 
     /**
      * A <code>BufferSegment</code> represents a BEEP Frame payload and holds
      * the BEEP Frames's Header, Trailer and the message payload.
      *
      * It contains a byte array an offset into the array and the
      * length from the offset.
      *
      * @author Eric Dixon
      * @author Huston Franklin
      * @author Jay Kint
      * @author Scott Pead
     * @version $Revision: 1.8 $, $Date: 2001/05/25 15:28:30 $
      */
     public static class BufferSegment {
 
         public byte[] data;    // the byte array
         public int offset;     // starting offset
         public int length;     // number of bytes from offset
 
         /**
          * Constructor BufferSegment
          *
          * @param data A byte array containing a BEEP Frame payload.
          */
         public BufferSegment(byte[] data)
         {
             this.data = data;
             this.offset = 0;
             this.length = data.length;
         }
 
         /**
          * Constructor BufferSegment
          *
          * @param data A byte array containing a BEEP Frame payload.
          * @param offset Indicates the begining position of the BEEP Frame
          * payload in the byte array <code>data</code>.
          * @param length Number of valid bytes in the byte array starting from
          * <code>offset</code>.
          */
         public BufferSegment(byte[] data, int offset, int length)
         {
             this.data = data;
             this.offset = offset;
             this.length = length;
         }
     }
 
     private static class MessageType {
 
         public static final String MESSAGE_TYPE_UNK = "UNK";
         public static final String MESSAGE_TYPE_MSG = "MSG";
         public static final String MESSAGE_TYPE_RPY = "RPY";
         public static final String MESSAGE_TYPE_ERR = "ERR";
         public static final String MESSAGE_TYPE_ANS = "ANS";
         public static final String MESSAGE_TYPE_NUL = "NUL";
 
         /**
          * BEEP message type for utility only.
          */
         private static final int MESSAGE_TYPE_MAX = 6;
 
         //    public static LinkedList types = new LinkedList();
         public static String[] types = new String[MESSAGE_TYPE_MAX];
 
         private MessageType() {}
 
         static {
             types[Message.MESSAGE_TYPE_UNK] = MessageType.MESSAGE_TYPE_UNK;
             types[Message.MESSAGE_TYPE_ANS] = MessageType.MESSAGE_TYPE_ANS;
             types[Message.MESSAGE_TYPE_MSG] = MessageType.MESSAGE_TYPE_MSG;
             types[Message.MESSAGE_TYPE_ERR] = MessageType.MESSAGE_TYPE_ERR;
             types[Message.MESSAGE_TYPE_RPY] = MessageType.MESSAGE_TYPE_RPY;
             types[Message.MESSAGE_TYPE_NUL] = MessageType.MESSAGE_TYPE_NUL;
         }
 
         static int getMessageType(String type)
                 throws IndexOutOfBoundsException
         {
             int ret = 0;
             int i = 0;
 
             for (i = 0; i < MESSAGE_TYPE_MAX; i++) {
                 if (type.equals(types[i])) {
                     ret = i;
 
                     break;
                 }
             }
 
             Log.logEntry(Log.SEV_DEBUG_VERBOSE,
                          "getMessageType=" + types[ret] + " (" + ret + ")");
 
             return ret;
         }
 
         static String getMessageType(int type)
                 throws IndexOutOfBoundsException
         {
             return types[type];
         }
     }
 }
