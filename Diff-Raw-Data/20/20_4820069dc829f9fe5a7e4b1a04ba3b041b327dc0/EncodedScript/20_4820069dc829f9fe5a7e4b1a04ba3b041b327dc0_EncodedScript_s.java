 /*
  * Jpkg - Java library and tools for operating system package creation.
  *
  * Copyright (c) 2007 Three Rings Design, Inc.
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  * 3. Neither the name of the copyright owner nor the names of contributors
  *    may be used to endorse or promote products derived from this software
  *    without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package com.threerings.jpkg.ant.dpkg.scripts.runner;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 
 import javax.mail.MessagingException;
 import javax.mail.internet.MimeUtility;
 
 import org.apache.commons.io.IOUtils;
 
 import com.threerings.jpkg.ant.dpkg.Dpkg;
 import com.threerings.jpkg.ant.dpkg.DpkgData;
 
 /**
  * A base64 encoded version of a {@link PackageScript} suitable for input to the uuencode command.
  * This class is public as required by Velocity, and should not be considered part of the public API.
  */
 public class EncodedScript
 {
     /**
      * Construct a new EncodedScript from the supplied {@link PackageScript}.
      * @param source The {@link PackageScript} to encode.
      * @param data The {@link DpkgData} to pass to the script.
      * @throws IOException If the encoding fails.
      */
     public EncodedScript (PackageScript source, DpkgData data)
         throws IOException
     {
         _source = source;
 
         // perform the encoding in memory. also, do this in the constructor as getEncodedSource()
         // will be called inside of a Velocity template so let's get all the exception handling done
         // right away so the caller can handle it.
         final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
         OutputStream output = null;
         try {
             output = MimeUtility.encode(bytes, BASE64);
         } catch (final MessagingException e) {
             throw new IOException("Failed to uuencode script. name=[" + _source.getFriendlyName() + "], reason=[" + e.getMessage() + "].");
         }
         IOUtils.write(HEADER, bytes, Dpkg.CHAR_ENCODING);
         bytes.flush();
         IOUtils.copy(_source.getSource(data), output);
         output.flush();
         IOUtils.write(FOOTER, bytes, Dpkg.CHAR_ENCODING);
         bytes.flush();
 
         output.close();
         bytes.close();
        _encoded = bytes.toString(Dpkg.CHAR_ENCODING);
     }
 
     /**
      * Returns a human readable name for this script.
      * @see PackageScript#getFriendlyName()
      */
     public String getFriendlyName ()
     {
         return _source.getFriendlyName();
     }
 
     /**
      * Returns whether this scripts failure should be reported to the packaging system.
      * @see PackageScript#failOnError()
      */
     public boolean failOnError ()
     {
         return _source.failOnError();
     }
 
     /**
      * Returns the original script source base64 encoded.
      */
     public String getEncodedSource ()
     {
         return _encoded;
     }
 
     /** The base64 constant as defined in javax.mail. */
     private static final String BASE64 = "base64";
 
     /** The header and footer required by the uuedecode command. */
     private static final String HEADER = "begin-base64 644 encoder.buf\n";
     private static final String FOOTER = "\n====";
 
     /** The uuencoded script source. */
     private final String _encoded;
 
     /** The script being encoded. */
     private final PackageScript _source;
 }
