 /*
  * Fast Infoset ver. 0.1 software ("Software")
  *
  * Copyright, 2004-2005 Sun Microsystems, Inc. All Rights Reserved.
  *
  * Software is licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License. You may
  * obtain a copy of the License at:
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations.
  *
  *    Sun supports and benefits from the global community of open source
  * developers, and thanks the community for its important contributions and
  * open standards-based technology, which Sun has adopted into many of its
  * products.
  *
  *    Please note that portions of Software may be provided with notices and
  * open source licenses from such communities and third parties that govern the
  * use of those portions, and any licenses granted hereunder do not alter any
  * rights and obligations you may have under such open source licenses,
  * however, the disclaimer of warranty and limitation of liability provisions
  * in this License will apply to all Software in this distribution.
  *
  *    You acknowledge that the Software is not designed, licensed or intended
  * for use in the design, construction, operation or maintenance of any nuclear
  * facility.
  *
  * Apache License
  * Version 2.0, January 2004
  * http://www.apache.org/licenses/
  *
  */
 
 
 package com.sun.xml.fastinfoset.algorithm;
 
 import java.nio.CharBuffer;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.jvnet.fastinfoset.EncodingAlgorithm;
 import org.jvnet.fastinfoset.EncodingAlgorithmException;
 
 public abstract class BuiltInEncodingAlgorithm implements EncodingAlgorithm {
     protected final static Pattern SPACE_PATTERN = Pattern.compile("\\s");
 
     public abstract int getPrimtiveLengthFromOctetLength(int octetLength) throws EncodingAlgorithmException;
 
     public abstract int getOctetLengthFromPrimitiveLength(int primitiveLength);
 
     public abstract void encodeToBytes(Object array, int astart, int alength, byte[] b, int start);
         
     public interface WordListener {
         public void word(int start, int end);
     }
     
     public void matchWhiteSpaceDelimnatedWords(CharBuffer cb, WordListener wl) {
         Matcher m = SPACE_PATTERN.matcher(cb);
         int i = 0;
        int s = 0;
         while(m.find()) {
            s = m.start();
             if (s != i) {
                 wl.word(i, s);
             }
             i = m.end();
         }
        wl.word(i, cb.length());
     }
     
     public StringBuffer removeWhitespace(char[] ch, int start, int length) {
         StringBuffer buf = new StringBuffer();
         int firstNonWS = 0;
         int idx = 0;
         for (; idx < length; ++idx) {
            if (Character.isWhitespace(ch[idx + start])) {
                 if (firstNonWS < idx) {
                    buf.append(ch, firstNonWS + start, idx - firstNonWS);
                 }
                 firstNonWS = idx + 1;
             }
         }
         if (firstNonWS < idx) {
            buf.append(ch, firstNonWS + start, idx - firstNonWS);
         }
         return buf;
     }
     
 }
