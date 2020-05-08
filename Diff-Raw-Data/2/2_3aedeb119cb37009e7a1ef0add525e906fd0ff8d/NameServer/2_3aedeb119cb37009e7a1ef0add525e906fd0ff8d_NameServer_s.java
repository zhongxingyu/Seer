 // Copyright 2007 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.dns.udp;
 
 import static org.joe_e.file.Filesystem.file;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.net.SocketAddress;
 
 import org.joe_e.array.ByteArray;
 import org.joe_e.array.ConstArray;
 import org.joe_e.charset.ASCII;
 import org.ref_send.deserializer;
 import org.ref_send.name;
 import org.ref_send.promise.eventual.Do;
 import org.waterken.dns.Domain;
 import org.waterken.dns.Resource;
 import org.waterken.udp.UDPDaemon;
 import org.waterken.vat.Pool;
 import org.waterken.vat.Root;
 import org.waterken.vat.Transaction;
 import org.waterken.vat.Vat;
 
 /**
  * A DNS name server.
  */
 public final class
 NameServer extends UDPDaemon {
     static private final long serialVersionUID = 1L;
 
     private final File master;
     private final Pool vats;
     
     /**
      * Constructs an instance.
      * @param port      {@link #port}
      * @param master    root persistence folder
      */
     public @deserializer
     NameServer(@name("port") final int port,
                @name("master") final File master,
                @name("vats") final Pool vats) {
         super(port);
         this.master = master;
         this.vats = vats;
     }
     
     // org.waterken.udp.Daemon interface
 
     public void
     accept(final SocketAddress from, final ByteArray msg,
            final Do<ByteArray,?> respond) throws Exception {
         final ByteArray response;
         try {
             response = process(msg);
         } catch (final Exception e) {
             final byte[] header = respond(msg.toByteArray());
             header[3] |= 2;
             respond.fulfill(ByteArray.array(header));
             return;
         }
         respond.fulfill(response);
     }
     
     // org.waterken.dns.udp.NameServer interface
     
     /**
      * address of the first question
      */
     static private final int QP = 0xC000 | 12;
     
     private ByteArray
     process(final ByteArray msg) throws Exception {
         final byte[] in = msg.toByteArray();
         final byte[] header = respond(in);
 
         // do some sanity checking
         if (0 != (in[2] & 0x02)) { throw new Exception(); } // TC bit set
         if (0 != (in[2] & 0x80)) { throw new Exception(); } // response bit set
         if (0 != (in[2] & 0x78)) {
             // unsupported query opcode
             header[3] |= 4; // set the RCODE to Not Implemented
             return ByteArray.array(header);
         }
         // by convention, only a single query is ever sent
         if (!(0 == in[4] && 1 == in[5])) { throw new Exception(); }
 
         // standard query
         
         // parse the question
         final String qname;
         final short qtype, qclass;
         final int qlen; {
             final StringBuilder buffer = new StringBuilder();
             int i = header.length;
             while (true) {
                 final int length = in[i++] & 0xFF;
                 // since we're only accepting a single query, assume
                 // it's unreasonable to use DNS compression
                 if (0x00 != (length & 0xC0)) { throw new Exception(); }
                 if (0 == length) { break; }
                 if (0 != buffer.length()) { buffer.append('.'); }
                 buffer.append(ASCII.decode(in, i, length));
                 i += length;
             }
             qname = buffer.toString();
             qtype = (short)(((in[i++] & 0xFF) << 8) | ( in[i++] & 0xFF));
             qclass = (short)(((in[i++] & 0xFF) << 8) | ( in[i++] & 0xFF));
             qlen = i - header.length;
         }
         
         // see if we've got any answers
         final ConstArray<Resource> answers;
         try {
             answers = vats.connect(file(master, qname.toLowerCase())).enter(
                     Vat.extend, new Transaction<ConstArray<Resource>>() {
                 public ConstArray<Resource>
                 run(final Root local) throws Exception {
                    final Domain face = (Domain)local.fetch(null, Domain.name);
                     return face.getAnswers();
                 }
             }).cast();
         } catch (final Exception e) {
             header[3] |= (e instanceof FileNotFoundException ? 3 : 2);
             return ByteArray.array(header);
         }
 
         // encode the corresponding answers
         final ByteArray.Builder response = ByteArray.builder(512); 
         header[2] |= 0x04;                          // set the AA bit
         header[5] = 1;                              // qdcount = 1
         response.append(header);                    // output a header
         response.append(in, header.length, qlen);   // echo the question
         int ancount = 0;
         boolean truncated = false;
         for (final Resource a : answers) {
             if ((255 == qtype || qtype == a.type) &&
                 (255 == qclass || qclass == a.clazz)) {
                 final byte[] data = a.data.toByteArray();
                 if (response.length() + 12 + data.length > 512) {
                     truncated = true;
                     continue;
                 }
                 
                 response.append((byte)(QP >>> 8));
                 response.append((byte)(QP      ));
                 response.append((byte)(a.type >>> 8));
                 response.append((byte)(a.type      ));
                 response.append((byte)(a.clazz >>> 8));
                 response.append((byte)(a.clazz      ));
                 response.append((byte)(a.ttl >>> 24));
                 response.append((byte)(a.ttl >>> 16));
                 response.append((byte)(a.ttl >>>  8));
                 response.append((byte)(a.ttl       ));
                 response.append((byte)(data.length >>>  8));
                 response.append((byte)(data.length       ));
                 response.append(data);
                 
                 ++ancount;
             }
         }
         final byte[] out = response.snapshot().toByteArray();
         if (truncated) {
             out[2] |= 0x20; // set the TC bit
         }
         out[6] = (byte)(ancount >>> 8);
         out[7] = (byte)(ancount      );
         return ByteArray.array(out);
     }
     
     static private byte[]
     respond(final byte[] in) {
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
         return header;
     }
 }
