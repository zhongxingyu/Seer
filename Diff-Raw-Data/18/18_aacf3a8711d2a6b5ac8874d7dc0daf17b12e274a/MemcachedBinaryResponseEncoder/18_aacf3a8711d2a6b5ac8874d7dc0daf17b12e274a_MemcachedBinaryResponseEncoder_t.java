 package com.thimbleware.jmemcached.protocol.binary;
 
 import com.thimbleware.jmemcached.protocol.Command;
 import com.thimbleware.jmemcached.protocol.ResponseMessage;
 import com.thimbleware.jmemcached.MCElement;
 import com.thimbleware.jmemcached.Cache;
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBuffers;
 import org.jboss.netty.channel.*;
 
 import java.nio.ByteOrder;
 
 /**
  */
@ChannelPipelineCoverage("all")
 public class MemcachedBinaryResponseEncoder extends SimpleChannelUpstreamHandler {
 
     private ChannelBuffer corkedResponse = null;
 
     public static enum ResponseCode {
         /**
          *       <t hangText="0x0000">No error</t>
          <t hangText="0x0001">Key not found</t>
          <t hangText="0x0002">Key exists</t>
          <t hangText="0x0003">Value too large</t>
          <t hangText="0x0004">Invalid arguments</t>
          <t hangText="0x0005">Item not stored</t>
          <t hangText="0x0081">Unknown command</t>
          <t hangText="0x0082">Out of memory</t>
          */
         OK(0x0000),
         KEYNF(0x0001),
         KEYEXISTS(0x0002),
         TOOLARGE(0x0003),
         INVARG(0x0004),
         NOT_STORED(0x0005),
         UNKNOWN(0x0081),
         OOM(0x00082);
 
         public short code;
 
         ResponseCode(int code) {
             this.code = (short)code;
         }
     }
 
     public ResponseCode getStatusCode(ResponseMessage command) {
         Command cmd = command.cmd.cmd;
         if (cmd == Command.GET || cmd == Command.GETS) {
             return ResponseCode.OK;
         } else if (cmd == Command.SET || cmd == Command.CAS || cmd == Command.ADD || cmd == Command.REPLACE || cmd == Command.APPEND  || cmd == Command.PREPEND) {
             switch (command.response) {
 
                 case EXISTS:
                     return ResponseCode.KEYEXISTS;
                 case NOT_FOUND:
                     return ResponseCode.KEYNF;
                 case NOT_STORED:
                     return ResponseCode.NOT_STORED;
                 case STORED:
                     return ResponseCode.OK;
             }
         } else if (cmd == Command.INCR || cmd == Command.DECR) {
             return command.incrDecrResponse == null ? ResponseCode.KEYNF : ResponseCode.OK;
         } else if (cmd == Command.DELETE) {
             switch (command.deleteResponse) {
                 case DELETED:
                     return ResponseCode.OK;
                 case NOT_FOUND:
                     return ResponseCode.KEYNF;
             }
         } else if (cmd == Command.STATS) {
             return ResponseCode.OK;
         } else if (cmd == Command.VERSION) {
             return ResponseCode.OK;
         } else if (cmd == Command.FLUSH_ALL) {
             return ResponseCode.OK;
         }
         return ResponseCode.UNKNOWN;
     }
 
     public ChannelBuffer constructHeader(MemcachedBinaryCommandDecoder.BinaryCommand bcmd, ResponseMessage command, ChannelBuffer extrasBuffer, ChannelBuffer keyBuffer, ChannelBuffer valueBuffer) {
         // take the ResponseMessage and turn it into a binary payload.
         ChannelBuffer header = ChannelBuffers.buffer(ByteOrder.BIG_ENDIAN, 24);
         header.writeByte((byte)0x81);  // magic
         header.writeByte(bcmd.code); // opcode
         short keyLength = (short) (keyBuffer != null ? keyBuffer.capacity() :0);
 
         header.writeShort(keyLength);
         int extrasLength = extrasBuffer != null ? extrasBuffer.capacity() : 0;
         header.writeByte((byte) extrasLength); // extra length = flags + expiry
         header.writeByte((byte)0); // data type unused
         header.writeShort(getStatusCode(command).code); // status code
 
         int dataLength = valueBuffer != null ? valueBuffer.capacity() : 0;
         header.writeInt(dataLength + keyLength + extrasLength); // data length
         header.writeInt(command.cmd.opaque); // opaque
         header.writeLong(command.cmd.cas_key);
 
         return header;
     }
 
     @Override
     public void messageReceived(ChannelHandlerContext channelHandlerContext, MessageEvent messageEvent) throws Exception {
         ResponseMessage command = (ResponseMessage) messageEvent.getMessage();
         MemcachedBinaryCommandDecoder.BinaryCommand bcmd = MemcachedBinaryCommandDecoder.BinaryCommand.forCommandMessage(command.cmd);
 
 
         // write extras == flags & expiry
         ChannelBuffer extrasBuffer = null;
 
         // write key if there is one
         ChannelBuffer keyBuffer = null;
         if (bcmd.addKeyToResponse && command.cmd.keys != null && command.cmd.keys.size() != 0) {
             keyBuffer = ChannelBuffers.buffer(ByteOrder.BIG_ENDIAN, command.cmd.keys.get(0).length());
             keyBuffer.writeBytes(command.cmd.keys.get(0).getBytes());
         }
 
         // write value if there is one
         ChannelBuffer valueBuffer = null;
         if (command.elements != null) {
             extrasBuffer = ChannelBuffers.buffer(ByteOrder.BIG_ENDIAN, 4);
             MCElement element = command.elements[0];
             extrasBuffer.writeShort((short) (element != null ? element.expire : 0));
             extrasBuffer.writeShort((short) (element != null ? element.flags : 0));
 
             if ((command.cmd.cmd == Command.GET || command.cmd.cmd == Command.GETS)) {
                 valueBuffer = ChannelBuffers.buffer(ByteOrder.BIG_ENDIAN, element.dataLength);
                 valueBuffer.writeBytes(element.data);
             } else if (command.cmd.cmd == Command.INCR || command.cmd.cmd == Command.DECR) {
                 valueBuffer = ChannelBuffers.buffer(ByteOrder.BIG_ENDIAN, 8);
                 valueBuffer.writeLong(command.incrDecrResponse);
             }
         } else if (command.cmd.cmd == Command.INCR || command.cmd.cmd == Command.DECR) {
             valueBuffer = ChannelBuffers.buffer(ByteOrder.BIG_ENDIAN, 8);
             valueBuffer.writeLong(command.incrDecrResponse);
         }
 
         ChannelBuffer headerBuffer = constructHeader(bcmd, command, extrasBuffer, keyBuffer, valueBuffer);
 
         // write everything
         // is the command 'quiet?' if so, then we append to our 'corked' buffer until a non-corked command comes along
         if (bcmd.noreply) {
             int totalCapacity = headerBuffer.capacity() + (extrasBuffer != null ? extrasBuffer.capacity() : 0)
                     + (keyBuffer != null ? keyBuffer.capacity() : 0) + (valueBuffer != null ? valueBuffer.capacity() : 0);
             if (corkedResponse != null) {
                 ChannelBuffer oldBuffer = corkedResponse;
                 corkedResponse = ChannelBuffers.buffer(ByteOrder.BIG_ENDIAN, totalCapacity);
                 corkedResponse.writeBytes(oldBuffer);
                 oldBuffer.clear();
             } else {
                 corkedResponse = ChannelBuffers.buffer(ByteOrder.BIG_ENDIAN, totalCapacity);
             }
 
             corkedResponse.writeBytes(headerBuffer);
             if (extrasBuffer != null)
                 corkedResponse.writeBytes(extrasBuffer);
             if (keyBuffer != null)
                 corkedResponse.writeBytes(keyBuffer);
             if (valueBuffer != null)
                 corkedResponse.writeBytes(valueBuffer);
         } else {
             // first write out any corked responses
             if (corkedResponse != null) {
                 messageEvent.getChannel().write(corkedResponse);
                 corkedResponse = null;
             }
 
             messageEvent.getChannel().write(headerBuffer);
             if (extrasBuffer != null)
                 messageEvent.getChannel().write(extrasBuffer);
             if (keyBuffer != null)
                 messageEvent.getChannel().write(keyBuffer);
             if (valueBuffer != null)
                 messageEvent.getChannel().write(valueBuffer);
         }
     }
 }
