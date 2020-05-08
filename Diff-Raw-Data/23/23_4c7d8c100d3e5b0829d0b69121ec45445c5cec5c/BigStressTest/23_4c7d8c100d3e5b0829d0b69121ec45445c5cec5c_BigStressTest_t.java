 package net.yeputons.cscenter.dbfall2013.engines;
 
 import net.yeputons.cscenter.dbfall2013.engines.hashtrie.HashTrieEngine;
import org.apache.commons.lang3.time.StopWatch;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.*;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotSame;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Egor Suvorov
  * Date: 11.10.13
  * Time: 17:35
  * To change this template use File | Settings | File Templates.
  */
 public class BigStressTest {
     FileStorableDbEngine engine;
    final int ELEMENTS = 800000;
     final int KEY_SIZE = 10;
     final int VALUE_SIZE = 1024;
 
     final int KEY_P = 23917;
     final int VALUE_P = 17239;
     final int MOD = 1000000000 + 7;
 
     void genBuf(ByteBuffer buf, int id, int p) {
         buf.clear();
         int cur = id + 1;
         for (int i = 0; i < buf.limit(); i++) {
             buf.put((byte)(cur & 0xFF));
             cur = (int)(((long)cur * p + id + 1) % MOD);
         }
         buf.position(0);
     }
 
     protected ByteBuffer str2Buf(String s) {
         return ByteBuffer.wrap(s.getBytes());
     }
 
     public BigStressTest() throws IOException {
         engine = new HashTrieEngine(File.createTempFile("hashtrie-stress", ".trie"));
     }
 
     @Test
     public void stressTest() {
         Random rnd = new Random();
         int[] order = new int[ELEMENTS];
 
        long startTime;

        StopWatch timer = new StopWatch();
        timer.start();

         ByteBuffer key = ByteBuffer.allocate(KEY_SIZE);
         ByteBuffer value = ByteBuffer.allocate(VALUE_SIZE);
         for (int i = 0; i < ELEMENTS; i++) {
             genBuf(key, i, KEY_P);
             genBuf(value, i, VALUE_P);
             engine.put(ByteBuffer.wrap(key.array()), ByteBuffer.wrap(value.array()));
            if (i % 50000 == 0) {
                System.err.printf("[ put ] %s: %d/%d\n", timer.toString(), i + 1, ELEMENTS);
            }
         }
 
         // Shuffling order of check
         System.err.println("Shuffling...");
         for (int i = 0; i < order.length; i++) {
             int pr = rnd.nextInt(i + 1);
             if (pr < i) {
                 int tmp = order[pr];
                 order[pr] = order[i];
                 order[i] = tmp;
             }
         }
 
        timer.reset();
        timer.start();
         for (int i = 0; i < ELEMENTS; i++) {
             int x = order[i];
             genBuf(key, x, KEY_P);
             genBuf(value, x, VALUE_P);
             assertEquals(value, engine.get(ByteBuffer.wrap(key.array())));
            if (i % 50000 == 0) {
                System.err.printf("[check] %s: %d/%d\n", timer.toString(), i + 1, ELEMENTS);
            }
         }
     }
 
 }
