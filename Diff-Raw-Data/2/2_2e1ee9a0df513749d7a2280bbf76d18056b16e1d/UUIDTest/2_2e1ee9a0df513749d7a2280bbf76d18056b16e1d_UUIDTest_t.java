 /**
    This file is part of Waarp Project.
 
    Copyright 2009, Frederic Bregier, and individual contributors by the @author
    tags. See the COPYRIGHT.txt in the distribution for a full listing of
    individual contributors.
 
    All Waarp Project is free software: you can redistribute it and/or 
    modify it under the terms of the GNU General Public License as published 
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
 
    Waarp is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with Waarp .  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.waarp.common.utility.test;
 
 import org.junit.Test;
 import org.waarp.common.utility.UUID;
 
 import java.net.NetworkInterface;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 import static org.junit.Assert.*;
 
 public class UUIDTest {
 	private static final char VERSION           = 'c';
	private static int NB = 5000000;
 	
     @Test
     public void testStructure() {
         UUID id = new UUID();
         String str = id.toString();
 
         assertEquals(str.charAt(8) , '-');
         assertEquals(str.charAt(13), '-');
         assertEquals(str.charAt(14), VERSION);
         assertEquals(str.charAt(18), '-');
         assertEquals(str.charAt(23), '-');
         assertEquals(str.length(), 36);
     }
 
     @Test
     public void testParsing() {
         UUID id1 = new UUID();
         UUID id2 = new UUID(id1.toString());
         assertEquals(id1, id2);
         assertEquals(id1.hashCode(), id2.hashCode());
 
         UUID id3 = new UUID(id1.getBytes());
         assertEquals(id1, id3);
     }
 
     @Test
     public void testNonSequentialValue() {
         final int n = NB;
         String[] ids = new String[n];
 
         for (int i = 0; i < n; i++) {
             ids[i] = new UUID().toString();
         }
 
         for (int i = 1; i < n; i++) {
             assertTrue(! ids[i-1].equals(ids[i].charAt(0)));
         }
     }
 
     @Test
     public void testGetBytesImmutability() {
         UUID id = new UUID();
         byte[] bytes = id.getBytes();
         byte[] original = Arrays.copyOf(bytes, bytes.length);
         bytes[0] = 0;
         bytes[1] = 0;
         bytes[2] = 0;
 
         assertTrue(Arrays.equals(id.getBytes(), original));
     }
 
     @Test
     public void testConstructorImmutability() {
         UUID id = new UUID();
         byte[] bytes = id.getBytes();
         byte[] original = Arrays.copyOf(bytes, bytes.length);
 
         UUID id2 = new UUID(bytes);
         bytes[0] = 0;
         bytes[1] = 0;
 
         assertTrue(Arrays.equals(id2.getBytes(), original));
     }
 
     @Test
     public void testVersionField() {
         UUID generated = new UUID();
         assertEquals(VERSION, generated.getVersion());
 
         UUID parsed1 = new UUID("20be0ffc-314a-cd53-7a50-013a65ca76d2");
         assertEquals(VERSION, parsed1.getVersion());
 
         UUID parsed2 = new UUID("20be0ffc-314a-7d53-7a50-013a65ca76d2");
         assertEquals('7', parsed2.getVersion());
         assertEquals(-1, parsed2.getProcessId());
         assertNull(parsed2.getTimestamp());
         assertNull(parsed2.getMacFragment());
     }
 
     @Test
     public void testPIDField() throws Exception {
         UUID id = new UUID();
 
         assertEquals(UUID.getJvmpid(), id.getProcessId());
     }
 
     @Test
     public void testDateField() {
         UUID id = new UUID();
         assertTrue(id.getTimestamp().getTime() > new Date().getTime() - 100);
         assertTrue(id.getTimestamp().getTime() < new Date().getTime() + 100);
     }
 
     @Test
     public void testMacAddressField() throws Exception{
         byte[] mac = NetworkInterface.getNetworkInterfaces().nextElement().getHardwareAddress();
 
         // if the machine is not connected to a network it has no active MAC address
         if (mac == null || mac.length < 6) {
             mac = new byte[] {0, 0, 0, 0, 0, 0};
             UUID.setMAC(mac);
         }
         UUID id = new UUID();
         byte[] field = id.getMacFragment();
         assertEquals(0, field[0]);
         assertEquals(0, field[1]);
         assertEquals(mac[2] & 0xF, field[2]);
         assertEquals(mac[3], field[3]);
         assertEquals(mac[4], field[4]);
         assertEquals(mac[5], field[5]);
     }
 
     @Test
     public void testForDuplicates() {
         int n = NB;
         Set<UUID> uuids = new HashSet<UUID>();
         UUID[] uuidArray = new UUID[n];
 
         long start = System.currentTimeMillis();
         for (int i = 0; i < n; i++)
             uuidArray[i] = new UUID();
         long stop = System.currentTimeMillis();
         System.out.println("Time = "+(stop-start)+" so "+(n*1000/(stop-start))+" Uuids/s");
 
         for (int i = 0; i < n; i++)
             uuids.add(uuidArray[i]);
 
         System.out.println("Create "+n+" and get: "+uuids.size());
         assertEquals(n, uuids.size());
     }
 
     private class Generator extends Thread {
         private UUID[] uuids;
         int id;
         int n;
         int numThreads;
 
         public Generator(int n, UUID[] uuids, int id, int numThreads) {
             this.n = n;
             this.uuids = uuids;
             this.id = id;
             this.numThreads = numThreads;
         }
 
         @Override
         public void run() {
             for (int i = 0; i < n; i++) {
                 uuids[numThreads * i + id] = new UUID();
             }
         }
     }
 
     @Test
     public void concurrentGeneration() throws Exception {
         int numThreads = 10;
         Thread[] threads = new Thread[numThreads];
         int n = NB;
         UUID[] uuids = new UUID[n];
 
         for (int i = 0; i < numThreads; i++) {
             threads[i] = new Generator(n / numThreads, uuids, i, numThreads);
             threads[i].start();
         }
 
         for (int i = 0; i < numThreads; i++)
             threads[i].join();
 
         Set<UUID> uuidSet = new HashSet<UUID>();
 
         int effectiveN = n / numThreads * numThreads;
         for (int i = 0; i < effectiveN; i++)
             uuidSet.add(uuids[i]);
 
         assertEquals(effectiveN, uuidSet.size());
     }
 }
