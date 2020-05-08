 /*
  * BoundsPatternTest.java
  * Transform
  *
  * Copyright (c) 2009 Flagstone Software Ltd. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation
  *    and/or other materials provided with the distribution.
  *  * Neither the name of Flagstone Software Ltd. nor the names of its
  *    contributors may be used to endorse or promote products derived from this
  *    software without specific prior written permission.
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
 package com.flagstone.transform.datatype;
 
 import static org.junit.Assert.assertArrayEquals;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Map;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.junit.runners.Parameterized.Parameters;
 
 import org.yaml.snakeyaml.Yaml;
 
 import com.flagstone.transform.coder.CoderException;
 import com.flagstone.transform.coder.Context;
 import com.flagstone.transform.coder.SWFDecoder;
 import com.flagstone.transform.coder.SWFEncoder;
 
 @RunWith(Parameterized.class)
 public final class BoundsPatternTest {
     
     private static final String RESOURCE = "com/flagstone/transform/datatype/Bounds.yaml";
 
     private static final String XMIN = "xMin";
     private static final String YMIN = "yMin";
     private static final String XMAX = "xMax";
     private static final String YMAX = "yMax";
     private static final String DATA = "data";
 
     @Parameters
     public static Collection<Object[]>  patterns() {
 
         ClassLoader loader = BoundsPatternTest.class.getClassLoader();
         InputStream other = loader.getResourceAsStream(RESOURCE);
         Yaml yaml = new Yaml();
         
         Collection<Object[]> list = new ArrayList<Object[]>();
          
         for (Object data : yaml.loadAll(other)) {
             list.add(new Object[] { data });
         }
 
         return list;
     }
 
     private Integer xMin;
     private Integer yMin;
     private Integer xMax;
     private Integer yMax;
     private byte[] data;
     
     private Context context;
     
     public BoundsPatternTest(Map<String,Object>values) {
         xMin = (Integer)values.get(XMIN);
         yMin = (Integer)values.get(YMIN);
         xMax = (Integer)values.get(XMAX);
         yMax = (Integer)values.get(YMAX);
         data = (byte[])values.get(DATA);
         
         context = new Context();
     }
 
     @Test
     public void checkSizeMatchesEncodedSize() throws CoderException {     
         final Bounds bounds = new Bounds(xMin, yMin, xMax, yMax);       
         final SWFEncoder encoder = new SWFEncoder(data.length);        
          
         assertEquals(data.length, bounds.prepareToEncode(encoder, context));
     }
 
     @Test
     public void checkBoundsIsEncoded() throws CoderException {
         final Bounds bounds = new Bounds(xMin, yMin, xMax, yMax);       
         final SWFEncoder encoder = new SWFEncoder(data.length);        
         
         bounds.prepareToEncode(encoder, context);
         bounds.encode(encoder, context);
 
         assertTrue(encoder.eof());
         assertArrayEquals(data, encoder.getData());
     }
 
     @Test
    public void checkColorIsDecoded() throws CoderException {
         final SWFDecoder decoder = new SWFDecoder(data);
         final Bounds bounds = new Bounds(decoder);
 
         assertTrue(decoder.eof());
         assertEquals(xMin.intValue(), bounds.getMinX());
         assertEquals(yMin.intValue(), bounds.getMinY());
         assertEquals(xMax.intValue(), bounds.getMaxX());
         assertEquals(yMax.intValue(), bounds.getMaxY());
     }
 }
