 /*
  * BSAXConstants.java
  * 
  *  Copyright 2005 Gregor N. Purdy. All rights reserved.
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package com.gregorpurdy.xml.bsax;
 
 /**
  * @author Gregor N. Purdy &lt;gregor@focusresearch.com&gt; http://www.gregorpurdy.com/gregor
 * @version $Id; $
  */
 public final class BSAXConstants {
 
   public static final byte[] MAGIC = { 0x42, 0x53, 0x41, 0x58 }; // "BSAX" in ASCII
   
   public static final int VERSION_LATEST = 1;
   public static final int VERSION_UNKNOWN = 0;
   
   public static final int UNLIMITED_STRING_TABLE_SIZE = 0;
   public static final int MINIMUM_STRING_TABLE_SIZE = 7;
   
   public static final int NULL_STRING_ID = 0;
   public static final int EMPTY_STRING_ID = 1;
   
   public static final int STARTING_STRING_TABLE_SIZE = 2;
   
   //
   // Content Handler Operators:
   //
   
   public static final int MIN_OP = 0;
   
   public static final int OP_STRING = 0; // int for id, int for length + utf-8 encoded string
   public static final int OP_START_DOCUMENT = 1; // NO ARGS
   public static final int OP_END_DOCUMENT = 2; // NO ARGS
   public static final int OP_START_ELEMENT = 3; // 4 args: uri, localName, qName, #attrs (5 each)
   public static final int OP_ATTRIBUTE = 4; // 5 args: uri, localName, qName, type, value
   public static final int OP_END_ELEMENT = 5; // 3 args: uri, localName, qName
   public static final int OP_CHARACTERS = 6; // 1 arg: string
   public static final int OP_IGNORABLE_WHITESPACE = 7; // 1 arg: string
   public static final int OP_START_PREFIX_MAPPING = 8; // 2 args: prefix, uri
   public static final int OP_END_PREFIX_MAPPING = 9; // 1 arg: prefix
   public static final int OP_PROCESSING_INSTRUCTION = 10; // 2 args: target, data
   public static final int OP_SKIPPED_ENTITY = 11; // 1 arg: name
   
   public static final int MAX_OP = 11;
   
   private BSAXConstants() { }
   
 }
