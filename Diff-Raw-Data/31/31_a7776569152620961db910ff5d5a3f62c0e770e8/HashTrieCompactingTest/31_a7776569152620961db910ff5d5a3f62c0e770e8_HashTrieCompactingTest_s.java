 package net.yeputons.cscenter.dbfall2013.engines;
 
 import net.yeputons.cscenter.dbfall2013.engines.hashtrie.HashTrieEngine;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Random;
 
 import static org.junit.Assert.assertEquals;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Egor Suvorov
  * Date: 25.10.13
  * Time: 17:33
  * To change this template use File | Settings | File Templates.
  */
 public class HashTrieCompactingTest extends AbstractStressTest {
     final Logger log = LoggerFactory.getLogger(HashTrieCompactingTest.class);
     protected File storage;
 
     public HashTrieCompactingTest() throws IOException {
         storage = File.createTempFile("storage", ".bin");
         storage.delete();
     }
 
     @Test
     public void stressTest() throws IOException, InterruptedException {
         final HashTrieEngine engine = new HashTrieEngine(this.storage);
         Map<ByteBuffer, ByteBuffer> real = new HashMap<ByteBuffer, ByteBuffer>();
         assertEquals(real, engine);
 
         LinkedList<Thread> threads = new LinkedList<Thread>();
 
         Random rnd = new Random();
        for (int step = 0; step < 500; step++) {
             if (rnd.nextInt(100) <= 1) {
                 Thread th = new Thread(new Runnable() {
                     @Override
                     public void run() {
                         try {
                             engine.runCompaction();
                        } catch (IOException e) {
                            e.printStackTrace();
                         } catch (IllegalStateException e) {
                         }
                     }
                 });
                 threads.add(th);
                 th.start();
             }
             synchronized (engine) {
                 performRandomOperation(rnd, engine, real);
 
                assertEquals(engine.size(), engine.keySet().size());
                assertEquals(engine.size(), engine.entrySet().size());
                assertEquals(real.keySet(), engine.keySet());
                assertEquals(real.entrySet(), engine.entrySet());
                assertEquals(real, engine);
             }
         }
         for (Thread th : threads) {
             th.join();
         }
     }
 }
