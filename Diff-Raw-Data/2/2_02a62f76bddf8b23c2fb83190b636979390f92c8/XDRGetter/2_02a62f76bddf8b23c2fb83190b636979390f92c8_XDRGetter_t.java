 /*
  *  The OpenDiamond Platform for Interactive Search
  *  Version 5
  *
  *  Copyright (c) 2009 Carnegie Mellon University
  *  All rights reserved.
  *
  *  This software is distributed under the terms of the Eclipse Public
  *  License, Version 1.0 which can be found in the file named LICENSE.
  *  ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS SOFTWARE CONSTITUTES
  *  RECIPIENT'S ACCEPTANCE OF THIS AGREEMENT
  */
 
 package edu.cmu.cs.diamond.opendiamond;
 
 import java.io.DataInputStream;
 import java.io.IOException;
 
 class XDRGetter {
 
     static int roundup(int n) {
         int roundup = n;
         if ((roundup & 0x3) != 0) {
             // round up
             roundup = (roundup + 4) & (~3);
         }
         return roundup;
     }
 
     final private DataInputStream data;
 
     public XDRGetter(DataInputStream data) {
         this.data = data;
     }
 
     public int getInt() throws IOException {
         return data.readInt();
     }
 
     public byte[] getOpaque() throws IOException {
         return getOpaque(Integer.MAX_VALUE);
     }
 
     public byte[] getOpaque(int maxLength) throws IOException {
         int len = data.readInt();
         if (len > maxLength) {
             throw new IllegalStateException("length greater than max (" + len
                     + " > " + maxLength + ")");
         }
         return getOpaqueFixed(len);
     }
 
     public byte[] getOpaqueFixed(int len) throws IOException {
         int roundup = roundup(len);
         int slack = roundup - len;
 
         byte result[] = new byte[len];
        data.readFully(result);
 
         // skip slack
         for (int i = 0; i < slack; i++) {
             data.read();
         }
 
         return result;
     }
 
     public long getLong() throws IOException {
         return data.readLong();
     }
 
     public String getString(int maxLength) throws IOException {
         return new String(getOpaque(maxLength), "UTF-8");
     }
 
     public String getString() throws IOException {
         return getString(Integer.MAX_VALUE);
     }
 
     public double getDouble() throws IOException {
         return data.readDouble();
     }
 }
