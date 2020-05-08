 /*
  * Copyright 2012 Stefan C. Mueller.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.smurn.fitzer;
 
 import java.io.IOException;
 
 /**
  * Header value converter for {@code null} values.
  */
final class NullHeaderValueConverter implements HeaderValueConverter {
 
     @Override
     public boolean compatibleTypeCheck(Object value) {
         return value == null;
     }
 
     @Override
     public boolean compatibleEncodingCheck(byte[] bytes) {
         if (bytes == null) {
             throw new NullPointerException("bytes must not be null.");
         }
         if (bytes.length != 70) {
             throw new IllegalArgumentException("bytes must be of length 70.");
         }
 
         for (int i = 0; i < bytes.length && bytes[i] != '/'; i++) {
             if (bytes[i] != ' ' && bytes[i] != '\t') {
                 return false;
             }
         }
         return true;
     }
 
     @Override
     public ParsingResult parse(byte[] bytes, long offset,
             ErrorHandler errorHandler) throws IOException {
         
         if (bytes == null) {
             throw new NullPointerException("bytes must not be null.");
         }
         if (errorHandler == null) {
             throw new NullPointerException("errorHandler must not be null.");
         }
         if (bytes.length != 70) {
             throw new IllegalArgumentException("bytes must be of length 70.");
         }
         int i;
         for (i = 0;
                 i < bytes.length && bytes[i] != '/';
                 i++) {
 
             if (bytes[i] != ' ' && bytes[i] != '\t') {
                 throw new IllegalArgumentException(
                         "Encoded type is not compatible with this converter.");
             }
             if (bytes[i] == '\t') {
                 FitsFormatException ex = new FitsFormatException(
                         offset + i,
                         "NullHeaderValueNonSpaceChars", bytes[i]);
                 errorHandler.error(ex);
             }
         }
 
         return new ParsingResult(true, i, null);
     }
 
     @Override
     public byte[] encode(Object value, boolean fixedFormat) {
         if (value != null) {
             throw new IllegalArgumentException(
                     "Can only encode the null value.");
         }
         return new byte[0];
     }
 }
