 /* SeqImage.java - Encode and Decode Image which stored in
  * Seq file,
  * 10 bytes to store the size + image size
  *
  * Copyright (C) 2012 Reason Zhang
  */
 
 package me.sheimi.util;
 
 import java.util.Arrays;
 import java.io.*;
 
 public class SeqImage {
 
   private byte[] image;
   private byte[] encoded;
 	public static final int SIZE_LEN = 10;
 	public static final String SIZE_FORMATER = "%10d";
 
   public SeqImage(byte[] image) {
     this.image = image;
   }
 
   public SeqImage(InputStream is, int size) throws IOException {
     image = new byte[size];
     is.read(image);
   }
 
   public int getSize() {
     return image.length;
   }
 
   public byte[] getImage() {
     return image;
   }
 
   public byte[] encode() {
     if (encoded == null) {
       byte[] size = String.format(SIZE_FORMATER, image.length).getBytes();
       byte[] encoded = new byte[SIZE_LEN + image.length];
       System.arraycopy(size, 0, encoded, 0, size.length);
      System.arraycopy(image, 0, encoded, size.length, image.length);
     }
     return encoded;
   }
 
   public static SeqImage decode(byte[] src) {
     byte[] len = Arrays.copyOfRange(src, 0, SIZE_LEN);
     int size = Integer.parseInt(new String(len)); 
     byte[] image = Arrays.copyOfRange(src, SIZE_LEN, SIZE_LEN + size);
     return new SeqImage(image);
   }
 
 }
