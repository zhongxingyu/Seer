 /*
  * Copyright (C) 2010 France Telecom
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
 
 package memoplayer;
 import java.io.*;
 
 
 // Store an array of functions, and shared int and string tables
 class FunctionBank {
     Function [] m_functions;
     int m_maxRegPerFunc = 16; // maximum regiters used during a function
     String [] m_stringTable;
     int[] m_intTable;
     
     FunctionBank (byte[] data) {
 //#ifdef MM.JsByteCodeCompat
         if (data[0] == -1) { // 255 => old bytecode format !
             parseOldByteCode(data);
             return;
         }
 //#endif
         parse(data);
     }
     
     void parse (byte[] data) {
         StringBuffer sb = new StringBuffer();
         int offset = 0, size;
         // Decoder max register per functions
        m_maxRegPerFunc = data[offset++] & 0xFF;;
         // Decode string table
        size = data[offset++] & 0xFF;
        System.out.println("string table size: "+size);
         String[] stringTable = size > 0 ? new String [size] : null;
         for (int i=0; i<size; i++) {
             offset = Decoder.bytesToUtf8 (data, offset, sb);
             stringTable[i] = sb.toString();
         }
         m_stringTable = stringTable;
         // Decode int table
        size = data[offset++] & 0xFF;;
         int[] intTable = size > 0 ? new int [size] : null;
         for (int i=0; i<size; i++) {
             intTable[i] = Decoder.bytesToInt32  (data, offset);
             offset += 4;
         }
         m_intTable = intTable;
         // Decode functions
        size = data[offset++] & 0xFF;;
         int maxIndex = 0;
         Function[] functions = new Function [255];
         for (int i=0; i<size; i++) {
             // Read function index
            int index = (data[offset++] & 0xFF) - 1;
             if (index > maxIndex) maxIndex = index;
             // Get size and offset of the function code
             int codeSize = Decoder.bytesToInt32 (data, offset); // code size
             offset += 4;
             int codeOffset = offset; // the code offset
             offset += codeSize;
             // Add function
             functions[index] = new Function (data, codeOffset, m_stringTable, m_intTable);
         }
         m_functions = new Function [maxIndex+1];
         System.arraycopy (functions, 0, m_functions, 0, maxIndex+1);
     }
     
 //#ifdef MM.JsByteCodeCompat
     void parseOldByteCode (byte[] data) {
         Function [] tmp = new Function [255];
         int maxIdx = -1;
         DataInputStream is = new DataInputStream (new ByteArrayInputStream (data));
         int index = Decoder.readUnsignedByte (is);
         if (index == 255) {
             m_maxRegPerFunc = Decoder.readUnsignedByte (is);
             index = Decoder.readUnsignedByte (is);
         }
         while (index > 0) {
             if (index > maxIdx) {
                 maxIdx = index;
             }
             tmp [index-1] = new Function (is);
             index = Decoder.readUnsignedByte (is);
         }
         m_functions = new Function [maxIdx];
         System.arraycopy(tmp, 0, m_functions, 0, maxIdx);
         tmp = null;
     }
 //#endif
     
     boolean hasFunction (int index) {
         return (index >= 0 && index < m_functions.length && m_functions [index] != null);
     }
 }
 
 public class Machine {
     final static int REGISTERS_BANK = 16;
     FunctionBank m_fb;
     Register [] m_register;
     int m_threshold; // limit where expanding registers is necessary
 
     Machine (FunctionBank fb, int nbFields) {
         m_fb = fb;
         // Initial size of registers is 2 times the maxRegPerFunc
         // Size of the threshold is always registers.size - maxRegPerFunc =>  half size of the registers
         m_threshold = fb.m_maxRegPerFunc;
         m_register = new Register [m_threshold*2];
         for (int i = 0; i < m_threshold*2; i++) {
             m_register[i] = new Register ();
         }
 /* RCZ OLD CODE
         m_register = new Register [m_maxRegPerFunc*2];
         for (int i = 0; i < m_maxRegPerFunc*2; i++) {
             m_register[i] = new Register ();
         }
         m_threshold = m_register.length - m_maxRegPerFunc;
 */
     }
 
     void invoke (Context c, int index, Field f, int ts) {
         if (f != null) {
             f.get (Field.OBJECT_IDX, m_register [0], 0); //.setInt (i);
             m_register [1].setFloat (ts);
         }
         run (c, index, 0);
     }
     
     //MCP: Called by the ScriptCallback class
     void invoke (int index, Context c, Register[] r) {
         int cnt = 0;
         for (int i = 0; i<r.length; i++) {
             if (r[i] != null) {
                 m_register[cnt++].set (r[i]);
             }
         }
         m_register[cnt].setFloat (FixFloat.time2fix(Context.time));
         run (c, index, 0);
     }
 
     static Context s_context = null; 
 
     void run (Context c, int index, int regBase) {
         if (m_fb.hasFunction(index)) {
             if (regBase >= m_threshold) {
                 int ol = m_register.length;
                 int nl = ol + REGISTERS_BANK;
                 //Logger.println ("Machine.run: expanding registers to "+nl);
                 Register [] tmp = new Register [nl];
                 System.arraycopy(m_register, 0, tmp, 0, ol);
                 for (; ol < nl; ol++) {
                     tmp[ol] = new Register ();
                 }
                 m_register = tmp;
                 m_threshold += REGISTERS_BANK; // m_threshold = m_fb.m_register.length - m_maxRegPerFunc
             }
 /* RCZ OLD CODE
             if (regBase >= m_threshold) {
                 int l = m_register.length;
                 int i = 0;
                 //Logger.println ("Machine.run: expanding registers to "+(l+REGISTERS_BANK));
                 Register [] tmp = new Register [l+REGISTERS_BANK];
                 for (; i < l; i++) {
                     tmp[i] = m_register [i];
                 }
                 l += REGISTERS_BANK;
                 for (; i < l; i++) {
                     tmp[i] = new Register ();
                 }
                 m_register = tmp;
                 m_threshold = m_register.length - m_maxRegPerFunc;
             }
 */  
             s_context = c;
             //Logger.println ("Machine.run: context="+s_context);
             m_fb.m_functions [index].run (this, c, regBase);
             //Logger.println ("Machine.run: context set to NULL");
             //s_context = null;
         } else {
             Logger.println ("Warning: trying to call null function #"+index);
         }
     }
 
     boolean hasFunction (int index) {
         return m_fb.hasFunction(index);
     }
 }
