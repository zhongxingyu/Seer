 package nl.surfnet.bod.util;
 
 import nl.surfnet.bod.domain.PhysicalPort;
 
 import org.apache.lucene.search.SortField;
 import org.junit.Test;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 
 public class LuceneSortFieldTypeTest {
 
   @Test
   public void testLuceneTypeInt() {
     assertThat(LuceneSortFieldType.getLuceneTypeFor(int.class), is(SortField.INT));
   }
 
   @Test
   public void testLucenTypeInteger() {
     assertThat(LuceneSortFieldType.getLuceneTypeFor(Integer.class), is(SortField.INT));
   }
 
  @Test(expected = NullPointerException.class)
   public void testLucenTypeNonExisting() {
    LuceneSortFieldType.getLuceneTypeFor(PhysicalPort.class);
   }
 
  @Test(expected = NullPointerException.class)
   public void testLucenTypeNull() {
    LuceneSortFieldType.getLuceneTypeFor(null);
   }
 
 }
