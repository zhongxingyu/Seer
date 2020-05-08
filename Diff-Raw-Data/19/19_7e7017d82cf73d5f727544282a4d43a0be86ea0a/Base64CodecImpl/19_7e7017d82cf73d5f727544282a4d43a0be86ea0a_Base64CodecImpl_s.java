 package org.aldeon.utils.base64;
 
 import org.aldeon.utils.conversion.ConversionException;
 
 import javax.xml.bind.DatatypeConverter;
 import java.nio.ByteBuffer;
 
 /**
 * ExampleEncDec base64 codec.
  *
  * ACHTUNG: the '=' char is replaced with '-'.
  * This way the identifiers are not escaped when sent in json, so we avoid
  * using \u003d instead of '='.
  *
  */
 public class Base64CodecImpl implements Base64Codec {
 
     @Override
     public String encode(ByteBuffer buffer) {
         byte[] b = new byte[buffer.remaining()];   // TODO remove unnecessary allocation
         buffer.get(b);
         return DatatypeConverter.printBase64Binary(b).replace('=', '-');
     }
 
     @Override
     public ByteBuffer decode(String base64) throws ConversionException {
         try {
             return ByteBuffer.wrap(DatatypeConverter.parseBase64Binary(base64.replace('-', '=')));
         } catch(Exception e) {
             throw new ConversionException("Failed to decode a base64 string into ByteBuffer", e);
         }
     }
 }
