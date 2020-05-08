 package org.nohope.typetools.collection;
 
 import org.junit.Test;
 
 import java.util.List;
 
 import static org.junit.Assert.assertSame;
 
 /**
  * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
  * @since 9/18/12 5:34 PM
  */
 public class CollectionUtilsTest {
     @Test
     public void fillListTest() {
         final Object marker = new Object();
         final List<Object> list = CollectionUtils.fillList(10, marker);
         for (final Object obj : list) {
             assertSame(marker, obj);
         }
     }
 }
