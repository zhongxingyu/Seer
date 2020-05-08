 package com.tngtech.leapdrone.drone.helpers;
 
 import java.util.Random;
 import java.util.zip.CRC32;
 import java.util.zip.Checksum;
 
 public class ChecksumHelper
 {
 
   public static final int RANDOM_STRING_LENGTH = 100;
 
   public static final String CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
 
   public static String createRandomCrc32Hex()
   {
     return createCrc32Hex(createRandomString());
   }
 
   public static String createCrc32Hex(String value)
   {
     byte bytes[] = value.getBytes();
     Checksum checksumCreator = new CRC32();
     checksumCreator.update(bytes, 0, bytes.length);
     long checkSumValue = checksumCreator.getValue();
    return Long.toHexString(checkSumValue);
   }
 
   public static String createRandomString()
   {
     Random random = new Random();
     StringBuilder stringBuilder = new StringBuilder(RANDOM_STRING_LENGTH);
     for (int i = 0; i < RANDOM_STRING_LENGTH; i++)
     {
       stringBuilder.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
     }
     return stringBuilder.toString();
   }
 }
