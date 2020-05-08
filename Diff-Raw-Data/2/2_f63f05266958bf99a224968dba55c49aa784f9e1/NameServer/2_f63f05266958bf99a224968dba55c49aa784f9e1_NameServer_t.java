 // Copyright 2007 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.dns.udp;
 
 import static org.joe_e.file.Filesystem.file;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.Serializable;
 import java.net.SocketAddress;
 
 import org.joe_e.Struct;
 import org.joe_e.array.ByteArray;
 import org.joe_e.array.PowerlessArray;
 import org.joe_e.charset.ASCII;
 import org.ref_send.promise.eventual.Do;
 import org.waterken.dns.Domain;
 import org.waterken.dns.Resource;
 import org.waterken.jos.JODB;
 import org.waterken.model.Model;
 import org.waterken.model.Root;
 import org.waterken.model.Transaction;
 import org.waterken.udp.Daemon;
 
 /**
  * A DNS name server.
  */
 public final class
 NameServer {
 
     private
     NameServer() {}
     
     /**
      * address of the first question
      */
     static private final int QP = 12;
     
     /**
      * Constructs an instance.
      * @param master    root persistence folder
      */
     static public Daemon
     make(final File master) {
         class DaemonX extends Struct implements Daemon, Serializable {
             static private final long serialVersionUID = 1L;
 
             public void
             accept(final SocketAddress from, final ByteArray msg,
                    final Do<ByteArray,?> respond) throws Exception {
                 final byte[] in = msg.toByteArray();
                 final byte[] header = {
                     in[0], in[1],   // id
                     in[2], in[3],   // flags
                     0x00, 0x00,     // qdcount
                     0x00, 0x00,     // ancount
                     0x00, 0x00,     // nscount
                     0x00, 0x00      // arcount
                 };
                 header[2] |= 0x80;  // set the QR bit
                 header[2] &= 0xFB;  // clear the AA bit
                 header[2] &= 0xFD;  // clear the TC bit
                 header[3] &= 0x7F;  // clear the RA bit
                 header[3] &= 0x8F;  // clear the Z bits
                 header[3] &= 0xF0;  // clear the RCODE bits
 
                 // do some sanity checking
                 if (0 != (in[2] & 0x02)) {
                     // TC bit is set in the query
                     header[3] |= 1; // set the RCODE to Format Error
                     respond.fulfill(ByteArray.array(header));
                     return;
                 } 
                 if (0 != (in[2] & 0x80)) {
                     // response bit is set in the query
                     header[3] |= 1; // set the RCODE to Format Error
                     respond.fulfill(ByteArray.array(header));
                     return;
                 }
                 if (0 != (in[2] & 0x78)) {
                     // unsupported query opcode
                     header[3] |= 4; // set the RCODE to Not Implemented
                     respond.fulfill(ByteArray.array(header));
                     return;
                 }
                 if (!(0 == in[4] && 1 == in[5])) {
                     // by convention, only a single query is ever sent
                     header[3] |= 1; // set the RCODE to Format Error
                     respond.fulfill(ByteArray.array(header));
                     return;
                 }
 
                 // standard query
                 
                 // parse the question
                 final String qname;
                 final short qtype, qclass;
                 final int qlen; {
                     final StringBuilder buffer = new StringBuilder();
                     int i = QP;
                     while (true) {
                         final int length = in[i++] & 0xFF;
                         if (0x00 != (length & 0xC0)) {
                             if (0xC0 != (length & 0xC0)) {
                                 // unspecified marker
                                 header[3] |= 1; // set the RCODE to Format Error
                                 respond.fulfill(ByteArray.array(header));
                                 return;
                             }
                             
                             // Since we're only accepting a single query, assume
                             // it's unreasonable to use DNS compression.
                             header[3] |= 1; // set the RCODE to Format Error
                             respond.fulfill(ByteArray.array(header));
                             return;
                         } else {
                             if (0 == length) { break; }
                             if (0 != buffer.length()) { buffer.append('.'); }
                             buffer.append(ASCII.decode(in, i, length));
                             i += length;
                         }
                     }
                     qname = buffer.toString();
                     qtype = (short)(((in[i++] & 0xFF) << 8) |
                                     ( in[i++] & 0xFF));
                     qclass = (short)(((in[i++] & 0xFF) << 8) |
                                      ( in[i++] & 0xFF));
                    qlen = i - QP;
                 }
                 
                 // see if we've got any answers
                 final PowerlessArray<Resource> answers;
                 try {
                     answers = JODB.connect(file(master, qname.toLowerCase())).
                         enter(Model.extend,
                               new Transaction<PowerlessArray<Resource>>() {
                         public PowerlessArray<Resource>
                         run(final Root local) throws Exception {
                             final Domain face =
                                 (Domain)local.fetch(null, Domain.name);
                             return face.getAnswers();
                         }
                     });
                 } catch (final Exception e) {
                     header[3] |= 3; // set the RCODE to Name Error
                     respond.fulfill(ByteArray.array(header));
                     return;
                 }
 
                 // encode the corresponding answers
                 final ByteArrayOutputStream response =
                     new ByteArrayOutputStream(512);
                 header[2] |= 0x04;              // set the AA bit
                 header[5] = 1;                  // qdcount = 1
                 response.write(header);         // output a header
                 response.write(in, QP, qlen);   // echo the question
                 int ancount = 0;
                 for (final Resource a : answers) {
                     if ((255 == qtype || qtype == a.type) &&
                         (255 == qclass || qclass == a.clazz)) {
                         final byte[] data = a.data.toByteArray();
                         if (data.length > 0xFFFF) { continue; }
                         
                         response.write(QP >>> 8);
                         response.write(QP      );
                         response.write(a.type >>> 8);
                         response.write(a.type      );
                         response.write(a.clazz >>> 8);
                         response.write(a.clazz      );
                         response.write(a.ttl >>> 24);
                         response.write(a.ttl >>> 16);
                         response.write(a.ttl >>>  8);
                         response.write(a.ttl       );
                         response.write(data.length >>>  8);
                         response.write(data.length       );
                         response.write(data);
                         
                         ++ancount;
                     }
                 }
                 final byte[] responseBuffer = response.toByteArray();
                 final byte[] out;
                 if (responseBuffer.length > 512) {
                     out = new byte[512];
                     System.arraycopy(responseBuffer, 0, out, 0, out.length);
                     out[2] |= 0x20;             // set the TC bit
                 } else {
                     out = responseBuffer;
                 }
                 out[6] = (byte)(ancount >>> 8);
                 out[7] = (byte)(ancount      );
                 respond.fulfill(ByteArray.array(out));
             }
         }
         return new DaemonX();
     }
 }
