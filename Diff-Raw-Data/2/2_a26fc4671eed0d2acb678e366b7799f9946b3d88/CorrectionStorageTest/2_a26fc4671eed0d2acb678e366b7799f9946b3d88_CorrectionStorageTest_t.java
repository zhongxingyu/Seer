 package net.nordist.lloydproof;
 
 import android.test.AndroidTestCase;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import junit.framework.Assert;
 
 /*
  * NB: setUp() deletes all stored corrections; each test is expected
  * to clean up after itself and leave the storage empty again when done
  * (to avoid failures from indeterminate test order).
  */
 public class CorrectionStorageTest extends AndroidTestCase
 {
     private CorrectionStorage store;
  
     @Override
     protected void setUp() {
         store = new CorrectionStorage(getContext());
         store.deleteAll();
     }
 
     public void testSaveAndDelete() {
         // test save():
         int id = store.save("XYZZY");
         Assert.assertTrue(id > 0);
         Assert.assertEquals(1, store.count());
        // test deleteById():
         Assert.assertEquals(1, store.deleteById(id));
         Assert.assertEquals(0, store.count());
     }
 
     public void testDeleteByIdArray() {
         List<Integer> idsToDelete = new ArrayList<Integer>();
         // save some corrections, including one NOT to delete:
         final int nDelete = 3;
         idsToDelete.add(store.save("PLUGH"));
         final int keepId = store.save("PLOVER");
         idsToDelete.add(store.save("Y2"));
         idsToDelete.add(store.save("Frobozz"));
         Iterator<Integer> i = idsToDelete.iterator();
         while (i.hasNext()) {
             Assert.assertTrue(i.next().intValue() > 0);
         }
         Assert.assertEquals(nDelete + 1, store.count());
         // test deleteByIdArray():
         Assert.assertEquals(nDelete, store.deleteByIdList(idsToDelete));
         Assert.assertEquals(1, store.count());
         // clean up:
         Assert.assertEquals(1, store.deleteById(keepId));
         Assert.assertEquals(0, store.count());
     }
 }
