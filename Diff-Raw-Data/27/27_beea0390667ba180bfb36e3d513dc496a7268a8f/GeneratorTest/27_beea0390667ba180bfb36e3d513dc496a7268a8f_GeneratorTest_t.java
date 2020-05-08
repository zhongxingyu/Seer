 package unstar;
 
 import java.util.NoSuchElementException;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import static unstar.GeneratorCodeProvider.decode;
 import static unstar.GeneratorCodeProvider.encode;
 
 public class GeneratorTest {
 
     @Test
     public void genTest() {
        CodeProvider gen = new GeneratorCodeProvider("GONY", "GOO2");
        assertTrue(gen.hasNext());
        assertEquals("0000GONY", gen.next());
        assertTrue(gen.hasNext());
        assertEquals("0000GONZ", gen.next());
        assertTrue(gen.hasNext());
        assertEquals("0000GOO0", gen.next());
        assertTrue(gen.hasNext());
        assertEquals("0000GOO1", gen.next());
        assertTrue(gen.hasNext());
        assertEquals("0000GOO2", gen.next());
         assertFalse(gen.hasNext());
         boolean thrown = false;
         try {
             gen.next();
         } catch (NoSuchElementException e) {
             thrown = true;
         }
         assertTrue(thrown);
     }
 
     @Test
     public void encodeTest() {
         assertEquals("1", encode(1, 1));
         assertEquals("A", encode(1, 10));
         assertEquals("00RS", encode(4, 1000));
         assertEquals(1000, decode("00RS"));
     }
 
     @Test
     public void serialTest() {
         /* Public */
         assertEquals(0x1009db6241eL, decode("E2BO659A"));
         assertEquals(0x10120a6bad5L, decode("E3C03B2T")); // personal
         assertEquals(0x101e22fa47cL, decode("E4TP99BW"));
         assertEquals(0x105d81aa6a9L, decode("ECN101WP"));
         assertEquals(0x1071daf128cL, decode("EF5D4KNG"));
         assertEquals(0x10f0e3321c7L, decode("EUTBD0AV"));
         assertEquals(0x2042ff5da94L, decode("SAH96KK4"));
         assertEquals(0x2042ff79a65L, decode("SAH9910L"));
         assertEquals(0x20ea681ff37L, decode("SV4GG27R"));
         assertEquals(0x20f6b14a736L, decode("SWMZYHBQ"));
         assertEquals(0x2006dc96f67L, decode("S32AAS87"));
         assertEquals(0x2042ff5354cl, decode("SAH95NWS"));
 
         /* Discovered */
         assertEquals(0x2042ff197cbL, decode("SAH90L3F"));
         assertEquals(0x2042ff1aabfL, decode("SAH90OU7"));
         assertEquals(0x2042ff22bc1L, decode("SAH91EBL"));
         assertEquals(0x2042ff27201l, decode("SAH91S75"));
     }
 }
