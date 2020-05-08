 package com.sematext.hbase.wd;
 
 import java.util.Arrays;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 public class RowKeyDistributorByHashPrefix_OneByteSimpleHashTest extends HBaseWdTestBase {
   public RowKeyDistributorByHashPrefix_OneByteSimpleHashTest() {
     super(new RowKeyDistributorByHashPrefix(new RowKeyDistributorByHashPrefix.OneByteSimpleHash(15)));
   }
 
   @Test
   public void testMaxDistribution() {
     RowKeyDistributorByHashPrefix.OneByteSimpleHash hasher = new RowKeyDistributorByHashPrefix.OneByteSimpleHash(255);
     byte[][] allPrefixes = hasher.getAllPossiblePrefixes();
     for (int i = 0; i < 1000; i++) {
      byte[] originalKey = new byte[] {(byte) (Math.random() * 255),
                                       (byte) (Math.random() * 255),
                                       (byte) (Math.random() * 255)};
       byte[] hash = hasher.getHashPrefix(originalKey);
       boolean found = false;
       for (int k = 0; k < allPrefixes.length; k++) {
         if (Arrays.equals(allPrefixes[k], hash)) {
           found = true;
           break;
         }
       }
       Assert.assertTrue("Hashed prefix wasn't found in all possible prefixes, val: " + Arrays.toString(hash), found);
     }
 
     Assert.assertArrayEquals(
             hasher.getHashPrefix(new byte[] {123, 12, 11}), hasher.getHashPrefix(new byte[] {123, 12, 11}));
   }
 
   @Test
   public void testLimitedDistribution() {
     RowKeyDistributorByHashPrefix.OneByteSimpleHash hasher = new RowKeyDistributorByHashPrefix.OneByteSimpleHash(10);
     byte[][] allPrefixes = hasher.getAllPossiblePrefixes();
     Assert.assertTrue(allPrefixes.length >= 9 && allPrefixes.length <= 10);
     for (int i = 0; i < 1000; i++) {
      byte[] originalKey = new byte[] {(byte) (Math.random() * 255),
                                       (byte) (Math.random() * 255),
                                       (byte) (Math.random() * 255)};
       byte[] hash = hasher.getHashPrefix(originalKey);
       boolean found = false;
       for (int k = 0; k < allPrefixes.length; k++) {
         if (Arrays.equals(allPrefixes[k], hash)) {
           found = true;
           break;
         }
       }
       Assert.assertTrue("Hashed prefix wasn't found in all possible prefixes, val: " + Arrays.toString(hash), found);
     }
 
     Assert.assertArrayEquals(
             hasher.getHashPrefix(new byte[] {123, 12, 11}), hasher.getHashPrefix(new byte[] {123, 12, 11}));
   }
 }
