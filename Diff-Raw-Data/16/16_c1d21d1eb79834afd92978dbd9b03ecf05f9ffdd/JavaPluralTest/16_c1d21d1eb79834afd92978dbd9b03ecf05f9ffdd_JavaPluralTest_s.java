 package uk.me.chrs.inflect;
 
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 import static uk.me.chrs.inflect.Inflect.plural;
 
 public class JavaPluralTest {
 
     @Test
     public void unconditionalPlurals() {
         assertEquals("aircraft", plural("aircraft"));
         assertEquals("postmen", plural("postman"));
         assertEquals("skies", plural("sky"));
         assertEquals("faxes", plural("fax"));
     }
 
     @Test
     public void conditionalPlurals() {
        assertEquals("vertices", plural("vertex", 0));
        assertEquals("vertex", plural("vertex", 1));
        assertEquals("vertices", plural("vertex", 8));
     }
 
 }
