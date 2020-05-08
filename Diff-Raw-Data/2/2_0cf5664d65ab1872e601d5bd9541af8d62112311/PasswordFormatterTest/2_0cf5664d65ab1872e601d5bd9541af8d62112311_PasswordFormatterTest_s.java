 /**
  * Copyright (c) 2011, yMock.com
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met: 1) Redistributions of source code must retain the above
  * copyright notice, this list of conditions and the following
  * disclaimer. 2) Redistributions in binary form must reproduce the above
  * copyright notice, this list of conditions and the following
  * disclaimer in the documentation and/or other materials provided
  * with the distribution. 3) Neither the name of the yMock.com nor
  * the names of its contributors may be used to endorse or promote
  * products derived from this software without specific prior written
  * permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
  * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
  * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
  * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package com.ymock.util.formatter.impl;
 
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.equalTo;
 
 /**
 * @todo #25! Provide implementation  PasswordFormatter, write javadoc
  * @author Marina Kosenko (marina.kosenko@gmail.com)
  * @version $Id$
  */
 public class PasswordFormatterTest {
 
     private PasswordFormatter passwordFormatter;
 
     @Before
     public final void setUp() throws Exception {
         this.passwordFormatter = new PasswordFormatter();
     }
 
     @Test
     public final void testFormatFake() {
         this.passwordFormatter.format(null);
     }
 
     @Test
     @Ignore
     public final void testFormat() {
         final String formatted = this.passwordFormatter.format("abcdefghij");
         assertThat(formatted, equalTo("a*****j"));
     }
 
     @Test
     @Ignore
     public final void testFormatNull() {
         final String formatted = this.passwordFormatter.format(null);
         assertThat(formatted, equalTo("NULL"));
     }
 
     @Test
     @Ignore
     public final void testFormatEmpty() {
         final String formatted = this.passwordFormatter.format("");
         assertThat(formatted, equalTo(""));
     }
 }
