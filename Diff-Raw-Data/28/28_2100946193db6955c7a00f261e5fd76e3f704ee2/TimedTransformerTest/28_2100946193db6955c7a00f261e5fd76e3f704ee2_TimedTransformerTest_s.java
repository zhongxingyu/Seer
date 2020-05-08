 package net.sparkmuse.common;
 
 import org.junit.Test;
 
 import java.util.Iterator;
 import java.util.List;
 
 import play.test.UnitTest;
 import com.google.common.base.Function;
 
 /**
  * Created by IntelliJ IDEA.
  *
  * @author neteller
  * @created: Nov 8, 2010
  */
 public class TimedTransformerTest extends UnitTest {
 
  private static class InfiniteIterator implements Iterator {
 
     private static final Object NEW_OBJECT = new Object();
 
     public boolean hasNext() {
       return true;
     }
 
     public Object next() {
       return NEW_OBJECT;
     }
 
     public void remove() {
       //To change body of implemented methods use File | Settings | File Templates.
     }
   }
 
   private static class DummyTransformation implements Function {
 
     private static final Object TRANSFORMED_OBJECT = new Object();
 
     public Object apply(Object o) {
       return TRANSFORMED_OBJECT;
     }
 
   }
 
   @Test
   public void shouldStopIterationAfterTimeLimit() {
     long start = System.currentTimeMillis();
     new TimedTransformer(50, new DummyTransformation()).transform(new InfiniteIterator());
     assertTrue(System.currentTimeMillis() - start < 100);
   }
 
   @Test
   public void shouldProduceTransformedObjects() {
     final List transformedObjects = new TimedTransformer(50, new DummyTransformation()).transform(new InfiniteIterator());
     assertTrue(transformedObjects.size() > 0);
     assertTrue(transformedObjects.get(0) == DummyTransformation.TRANSFORMED_OBJECT);
   }
 
 }
