 package nl.surfnet.bod.util;
 
 import nl.surfnet.bod.domain.PhysicalPort;
 
 import org.apache.lucene.search.SortField;
 import org.junit.Test;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
 
 public class LuceneSortFieldTypeTest {
 
   @Test
   public void testLuceneTypeInt() {
     assertThat(LuceneSortFieldType.getLuceneTypeFor(int.class), is(SortField.INT));
   }
 
   @Test
   public void testLucenTypeInteger() {
     assertThat(LuceneSortFieldType.getLuceneTypeFor(Integer.class), is(SortField.INT));
   }
 
  @Test
   public void testLucenTypeNonExisting() {
    assertThat(LuceneSortFieldType.getLuceneTypeFor(PhysicalPort.class), is(SortField.STRING_VAL));
   }
 
  @Test
   public void testLucenTypeNull() {
    assertThat(LuceneSortFieldType.getLuceneTypeFor(null), is(SortField.STRING_VAL));
   }
 
 }
