 package com.github.riking.templateworlds.impl.common;
 
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 
import org.apache.commons.lang3.Validate;
 import org.bukkit.util.Vector;
 import org.junit.Test;
 
 import com.github.riking.templateworlds.impl.common.ChunkAreaIterator;
 
 public class ChunkAreaIteratorTest {
     @Test
     public void testReturnOrder() {
         /*
          * MinX = 1, MaxX = 4
          * MinZ = 2, MaxZ = 4
          * should return:
          * (1, 2) (2, 2) (3, 2) (4, 2)
          * (1, 3) (2, 3) (3, 3) (4, 3)
          * (1, 4) (2, 4) (3, 4) (4, 4)
          */
         Iterator<Vector> iter = new ChunkAreaIterator(1, 2, 4, 4);
         Validate.isTrue(new Vector(1, 0, 2).equals(iter.next()));
         Validate.isTrue(new Vector(2, 0, 2).equals(iter.next()));
         Validate.isTrue(new Vector(3, 0, 2).equals(iter.next()));
         Validate.isTrue(new Vector(4, 0, 2).equals(iter.next()));
 
         Validate.isTrue(new Vector(1, 0, 3).equals(iter.next()));
         Validate.isTrue(new Vector(2, 0, 3).equals(iter.next()));
         Validate.isTrue(new Vector(3, 0, 3).equals(iter.next()));
         Validate.isTrue(new Vector(4, 0, 3).equals(iter.next()));
 
         Validate.isTrue(new Vector(1, 0, 4).equals(iter.next()));
         Validate.isTrue(new Vector(2, 0, 4).equals(iter.next()));
         Validate.isTrue(new Vector(3, 0, 4).equals(iter.next()));
         Validate.isTrue(new Vector(4, 0, 4).equals(iter.next()));
     }
 
     @Test
     public void testReturnAmount() {
         Iterator<Vector> iter = new ChunkAreaIterator(1, 2, 4, 4);
         assert (iter.hasNext());
         iter.next();
         iter.next();
         iter.next();
         iter.next();
 
         iter.next();
         iter.next();
         iter.next();
         iter.next();
 
         iter.next();
         iter.next();
         iter.next();
         assert (iter.hasNext());
         iter.next();
         assert (!iter.hasNext());
     }
 
     @Test
     public void testOneReturn() {
         Iterator<Vector> iter = new ChunkAreaIterator(-45, 20, -45, 20);
         assert (iter.hasNext());
         Validate.isTrue(new Vector(-45, 0, 20).equals(iter.next()));
         assert (!iter.hasNext());
     }
 
     @Test(expected = NoSuchElementException.class)
     public void testThrowOne() {
         Iterator<Vector> iter = new ChunkAreaIterator(-45, 20, -45, 20);
         try {
             iter.next();
         } catch (Throwable t) {
         } // swallow early throw
 
         iter.next(); // throw
     }
 
     @Test(expected = NoSuchElementException.class)
     public void testThrowNormal() {
         Iterator<Vector> iter = new ChunkAreaIterator(1, 2, 4, 4);
         try {
             iter.next();
             iter.next();
             iter.next();
             iter.next();
 
             iter.next();
             iter.next();
             iter.next();
             iter.next();
 
             iter.next();
             iter.next();
             iter.next();
             iter.next();
         } catch (Throwable t) {
         } // swallow early throw
 
         iter.next(); // throw
     }
 }
