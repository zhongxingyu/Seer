 package org.hampelratte.net.mms;
 
 import java.nio.ByteOrder;
 
 import org.apache.mina.core.buffer.IoBuffer;
 import org.apache.mina.core.session.IoSession;
 import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
 import org.apache.mina.filter.codec.ProtocolDecoderOutput;
 import org.hampelratte.net.mms.data.decoders.MMSPacketDecoder;
 import org.hampelratte.net.mms.messages.TcpMessageHeader;
 import org.hampelratte.net.mms.messages.server.decoders.MMSResponseDecoder;
 import org.hampelratte.net.mms.messages.server.decoders.MMSResponseDecoderFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Decoder for all objects received from a server (messages data packets)
  *
  * @author <a href="mailto:hampelratte@users.berlios.de">hampelratte@users.berlios.de</a>
  */
 public class MMSObjectDecoder extends CumulativeProtocolDecoder {
 
     private static transient Logger logger = LoggerFactory.getLogger(MMSObjectDecoder.class);
     
     @Override
     protected boolean doDecode(IoSession session, IoBuffer b, ProtocolDecoderOutput out) throws Exception {
         while (b.remaining() > 0) {
 
             int position = b.position();
 
            // read the first 8 bytes
             if (b.remaining() < 8) {
                 // not enough data yet
                 return false;
             } else {
                 b.order(ByteOrder.LITTLE_ENDIAN);
                if (b.getInt(position + 4) == 0xb00bface) { // this is a command
                     logger.trace("Decoding MMS message");
                     b.position(position); // reset buffer position to beginning
                     if (!headerComplete(b)) {
                         // header not yet complete
                         logger.trace("Wating for more header data");
                         return false;
                     }
                     
                     // parse the header
                     TcpMessageHeader header = decodeHeader(b);
                     
                     // check, if we have received the complete message
                     if (!messageComplete(b, header)) {
                         // message not yet complete
                         logger.trace("Wating for more message body data");
                         return false;
                     }
                     
                     int posBeforeBodyDecoding = b.position(); 
                     
                     // skip message chunkLen
                     b.getInt();
                     
                     // decode message id (MID)
                     int mid = b.getInt();
                     
                     b.position(posBeforeBodyDecoding);
                     
                     MMSResponseDecoder decoder = MMSResponseDecoderFactory.createDecoder(mid);
                     decoder.setHeader(header);
                     decoder.decode(session, b, out);
                 } else { // this is a data packet
                    b.skip(4); // locationId
                    b.skip(1); // playIncarnation
                    b.skip(1); // AFFLags
                    int length = b.getUnsignedShort(); // PacketSize
 
                     b.position(position);
                     // packet not yet complete
                     if (b.remaining() < length) {
                         return false;
                     }
 
                     MMSPacketDecoder decoder = new MMSPacketDecoder();
                     decoder.decode(session, b, out);
                 }
             }
         }
         
         return true;
     }
 
     private boolean messageComplete(IoBuffer b, TcpMessageHeader header) {
         return b.remaining() >= header.getMessageLength() - 16; // -16 the header is already read
     }
 
     private boolean headerComplete(IoBuffer b) {
         return b.remaining() >= TcpMessageHeader.SIZE;
     }
     
     private TcpMessageHeader decodeHeader(IoBuffer b) {
         TcpMessageHeader header = new TcpMessageHeader();
         b.getInt();                                     // 01 00 00 00
         b.getInt();                                     // ce fa 0b b0
         header.setMessageLength(b.getUnsignedInt());    // cmd length
         b.getInt();                                     // "MMS "
         b.getUnsignedInt();                             // length until end / 8
         header.setSeq(b.getUnsignedShort());            // sequence
         header.setMbz(b.getShort());                    // mbz
         header.setTimeSent(b.getLong());                // timestamp
         return header;
     }
 }
