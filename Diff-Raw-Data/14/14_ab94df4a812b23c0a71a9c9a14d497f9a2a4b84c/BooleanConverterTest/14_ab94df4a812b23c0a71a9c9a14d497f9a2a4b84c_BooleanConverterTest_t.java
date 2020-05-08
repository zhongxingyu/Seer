 /**
  * Copyright (c) 2008, Damian Carrillo
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without modification, are permitted 
  * provided that the following conditions are met:
  * 
  *   * Redistributions of source code must retain the above copyright notice, this list of 
  *     conditions and the following disclaimer.
  *   * Redistributions in binary form must reproduce the above copyright notice, this list of 
  *     conditions and the following disclaimer in the documentation and/or other materials 
  *     provided with the distribution.
  *   * Neither the name of the copyright holder's organization nor the names of its contributors 
  *     may be used to endorse or promote products derived from this software without specific 
  *     prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
  * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
  * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package agave.conversion;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import agave.exception.ConversionException;
 
 /**
  * @author <a href="mailto:damiancarrillo@gmail.com">Damian Carrillo</a>
  */
 public class BooleanConverterTest {
 
     private BooleanConverter converter;    
 
     @Before
     public void setup() throws Exception {
         converter = new BooleanConverter();
     }
 
     @Test
     public void testConvert() throws Exception {
         Assert.assertEquals(Boolean.TRUE, converter.convert("true"));
         Assert.assertEquals(Boolean.TRUE, converter.convert("TRUE"));
         Assert.assertEquals(Boolean.TRUE, converter.convert("t"));
         Assert.assertEquals(Boolean.TRUE, converter.convert("T"));
         Assert.assertEquals(Boolean.TRUE, converter.convert("1"));
         Assert.assertEquals(Boolean.FALSE, converter.convert("false"));
         Assert.assertEquals(Boolean.FALSE, converter.convert("FALSE"));
         Assert.assertEquals(Boolean.FALSE, converter.convert("f"));
         Assert.assertEquals(Boolean.FALSE, converter.convert("F"));
         Assert.assertEquals(Boolean.FALSE, converter.convert("0"));
        Assert.assertEquals(Boolean.TRUE, converter.convert("on"));
        Assert.assertEquals(Boolean.FALSE, converter.convert("off"));
         Assert.assertEquals(null, converter.convert(null));
         Assert.assertEquals(null, converter.convert(""));
     }
     
     @Test(expected = ConversionException.class)
     public void testConvertWithException() throws Exception {
         converter.convert("some bad input");
     }
     
 }
 

