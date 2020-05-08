 package uk.ac.ebi.chemet.tools.annotation.parse;
 
 import org.junit.Test;
 import uk.ac.ebi.mdk.domain.annotation.GibbsEnergy;
 import uk.ac.ebi.mdk.domain.annotation.Note;
 
 import static org.junit.Assert.*;
 
 /** @author John May */
 public class GibbsAnnotationParserTest {
 
     @Test
     public void testParse_Null() throws Exception {
         assertNull(new StringAnnotationParser(new Note()).parse(null));
     }
 
     @Test
     public void testParse_Empty() throws Exception {
         assertNull(new StringAnnotationParser(new Note()).parse(""));
     }
 
     @Test
     public void testParse() throws Exception {
         AnnotationParser<GibbsEnergy> parser = new GibbsAnnotationParser();
         GibbsEnergy energy = parser.parse("2.3 ± 0.06");
         assertEquals(energy.getValue(), 2.3, 0.1);
         assertEquals(energy.getError(), 0.06, 0.1);
     }
 
     @Test
     public void testParse_Prefix() throws Exception {
         AnnotationParser<GibbsEnergy> parser = new GibbsAnnotationParser();
         GibbsEnergy energy = parser.parse("ΔG=2.3 ± 0.01");
         assertEquals(energy.getValue(), 2.3, 0.1);
         assertEquals(energy.getError(), 0.01, 0.1);
     }
 
     @Test
     public void testParse_Suffix() throws Exception {
         AnnotationParser<GibbsEnergy> parser = new GibbsAnnotationParser();
         GibbsEnergy energy = parser.parse("2.6 ± 0.052ΔG");
         assertEquals(energy.getValue(), 2.6, 0.1);
         assertEquals(energy.getError(), 0.05, 0.1);
     }
 
     @Test
     public void testParse_noError() throws Exception {
         AnnotationParser<GibbsEnergy> parser = new GibbsAnnotationParser();
         GibbsEnergy energy = parser.parse("2.6");
         assertEquals(energy.getValue(), 2.6, 0.1);
         assertEquals(energy.getError(), 0.0, 0.1);
     }
 }
