 /*
  * New BSD license: http://opensource.org/licenses/bsd-license.php
  *
  * Copyright (c) 2010
  * Henry Story
  * http://bblfish.net/
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * - Redistributions of source code must retain the above copyright notice,
  *  this list of conditions and the following disclaimer.
  * - Redistributions in binary form must reproduce the above copyright notice,
  *  this list of conditions and the following disclaimer in the documentation
  *  and/or other materials provided with the distribution.
  * - Neither the name of bblfish.net, Inc. nor the names of its contributors
  *  may be used to endorse or promote products derived from this software
  *  without specific prior written permission.
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
 
 package net.java.dev.sommer.foafssl.util;
 
 import static org.junit.Assert.assertTrue;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * @author Henry Story
  * @date Mar 7, 2010 Time: 2:41:20 PM
  */
 public class SafeInputStreamTest {
 
     final String test = "123456789ABCDEF";
     InputStream in;
 
     @Before
     public void setUp() throws UnsupportedEncodingException {
         in = new ByteArrayInputStream(test.getBytes("UTF-8"));
     }
 
     @Test
     public void testRead() throws Exception {
         SafeInputStream safe = new SafeInputStream(in, 6);
         assertTrue(safe.read() == '1');
         assertTrue(safe.read() == '2');
         assertTrue(safe.read() == '3');
         assertTrue(safe.read() == '4');
         assertTrue(safe.read() == '5');
         assertTrue(safe.read() == '6');
         assertTrue(safe.read() == -1);
     }
 
     @Test
     public void testRead2() throws Exception {
         SafeInputStream safe = new SafeInputStream(in, 6);
         byte[] buf = new byte[4];
 
         assertTrue(safe.read(buf) == 4);
         assertTrue(new String(buf, "UTF-8").equals("1234"));
         assertTrue(safe.read(buf) == 2);
         assertTrue(new String(buf, "UTF-8").equals("5634"));
         assertTrue(safe.read(buf) == -1);
         assertTrue(new String(buf, "UTF-8").equals("5634"));
     }
 
     @Test
     public void testAvailable() throws Exception {
     }
 }
