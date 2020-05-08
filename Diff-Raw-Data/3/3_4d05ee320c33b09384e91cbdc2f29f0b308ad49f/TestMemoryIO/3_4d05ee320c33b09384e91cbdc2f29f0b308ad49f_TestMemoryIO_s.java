 package headwater.io;
 
 import headwater.bitmap.BitmapFactory;
 import headwater.bitmap.IBitmap;
 import headwater.bitmap.MemoryBitmap2;
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.internal.ArrayComparisonFailure;
 
 import java.util.Random;
 
 public class TestMemoryIO {
     
     @Test
     public void testFlushing() throws Exception{
         
         Random rand = new Random(634523423562L);
 
         BitmapFactory factory = new BitmapFactory() {
             public IBitmap make() {
                 return MemoryBitmap2.wrap(new byte[1024]);
             }
         };
         MemoryIO src = new MemoryIO().withBitmapFactory(factory);
         MemoryIO dst = new MemoryIO().withBitmapFactory(factory);
 
         Assert.assertEquals(0, src.getRowCountUnsafe());
         Assert.assertEquals(0, dst.getRowCountUnsafe());
         
         byte[] key0 = new byte[10];
         rand.nextBytes(key0);
         
         IBitmap bm1 = factory.make();
         IBitmap bm2 = factory.make();
         randomize(bm1, rand);
         randomize(bm2, rand);
         
         Assert.assertFalse(bm1.isEmpty());
         Assert.assertFalse(bm2.isEmpty());
         try {
             Assert.assertArrayEquals(bm1.toBytes(), bm2.toBytes());
             Assert.fail("Those arrays should not be equal");
         } catch (ArrayComparisonFailure expected) {
             // it's all good.
         }
         
         src.put(key0, 2L, bm1);
         src.put(key0, 1L, bm2);
         
         Assert.assertEquals(1, src.getRowCountUnsafe());
         Assert.assertEquals(0, dst.getRowCountUnsafe());
         
         // flush src into dst
 //        src.flush(dst);
         
         Assert.assertEquals(0, src.getRowCountUnsafe());
         Assert.assertEquals(1, dst.getRowCountUnsafe());
         
         // verify the columns are correct.
 
         Assert.assertArrayEquals(bm1.toBytes(), dst.get(key0, 2L).toBytes());
         Assert.assertArrayEquals(bm2.toBytes(), dst.get(key0, 1L).toBytes());
         
         Assert.assertFalse(bm1 == dst.get(key0, 2L));
         Assert.assertFalse(bm2 == dst.get(key0, 1L));
         Assert.assertFalse(bm1.toBytes() == dst.get(key0, 2L).toBytes());
         Assert.assertFalse(bm2.toBytes() == dst.get(key0, 1L).toBytes());
     }
     
     private void randomize(IBitmap bm, Random rand) {
         byte[] buf = new byte[(int)(bm.getBitLength() / 8)];
         rand.nextBytes(buf);
         bm.setAll(buf);
     }
 }
