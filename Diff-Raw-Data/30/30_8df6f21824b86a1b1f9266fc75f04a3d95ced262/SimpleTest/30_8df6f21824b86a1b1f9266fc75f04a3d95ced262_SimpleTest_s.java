 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2012, Red Hat, Inc., and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 
 package org.httpparser;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 /**
  * @author Stuart Douglas
  */
 
 public class SimpleTest {
 
     public static final String[] VALUES = {"PUT", "POST", "Accept",
             "Accept-Charset",
             "Accept-Encoding",
             "Accept-Language",
             "Accept-Ranges",
             "Age",
             "Allow",
             "Authorization",
             "Cache-Control",
             "Cookie",
             "Connection",
             "Content-Disposition",
             "Content-Encoding",
             "Content-Language",
             "Content-Length",
             "Content-Location",
             "Content-MD5",
             "Content-Range",
             "Content-Type",
             "Date",
             "ETag",
             "Expect",
             "Expires",
             "From",
             "Host",
             "If-Match",
             "If-Modified-Since",
             "If-None-Match",
             "If-Range",
             "If-Unmodified-Since",
             "Last-Modified",
             "Location",
             "Max-Forwards",
             "Pragma",
             "Proxy-Authenticate",
             "Proxy-Authorization",
             "Range",
             "Referer",
             "Refresh",
             "Retry-After",
             "Server",
             "Set-Cookie",
             "Set-Cookie2",
             "Strict-Transport-Security",
             "TE",
             "Trailer",
             "Transfer-Encoding",
             "Upgrade",
             "User-Agent",
             "Vary",
             "Via",
             "Warning",
             "WWW-Authenticate"};
     @Test
     public void test() {
 
 
 
         final Tokenizer parser = TokenizerGenerator.createTokenizer(VALUES);
         final List<String> tokens = new ArrayList<>();
         final TokenContext context = new TokenContext(new TokenState(0), new TokenHandler() {
             @Override
             public void handleToken(final String token, final TokenContext tokenContext) {
                 tokens.add(token);
             }
         });
        byte[] in = "PUT POST     ggg ".getBytes();
         for(int i = 0; i< in.length; ++i) {
             parser.handle(in[i], context);
         }
        final String[] expected = {"PUT", "POST", "ggg"};
         Assert.assertEquals(Arrays.asList(expected), tokens);
         Assert.assertSame("PUT", tokens.get(0));
         Assert.assertSame("POST", tokens.get(1));
        Assert.assertNotSame("ggg", tokens.get(2));
     }
 
 
 }
