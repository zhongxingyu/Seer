 package icfp2013.generator;
 
 import icfp2013.generator.syntax.Program;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import java.util.Random;
 
 import static com.google.common.collect.Lists.newArrayList;
 import static icfp2013.generator.syntax.Operators.AND;
 import static icfp2013.generator.syntax.Operators.FOLD;
 import static icfp2013.generator.syntax.Operators.IF0;
 import static icfp2013.generator.syntax.Operators.NOT;
 import static icfp2013.generator.syntax.Operators.OR;
 import static icfp2013.generator.syntax.Operators.PLUS;
import static icfp2013.generator.syntax.Operators.SHL1;
 import static icfp2013.generator.syntax.Operators.SHR1;
import static icfp2013.generator.syntax.Operators.SHR16;
 import static icfp2013.generator.syntax.Operators.SHR4;
import static icfp2013.generator.syntax.Operators.XOR;
 import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
 
 /**
  * @author <a href="mailto:jonas.wagner@life.uni-leipzig.de">Jonas Wagner</a>
  */
 public class ProgramGeneratorTest {
     @Test
     public void testSuperSimple() throws Exception {
         int size = 9;
         ProgramGenerator generator = new ProgramGenerator(newArrayList(NOT), size);
 
         Random r = new Random(0);
         Program p = generator.apply(r);
 
         System.out.println(p.toString());
         assertEquals(size, p.size());
     }
 
     @Test
     public void testComplex() throws Exception {
         int size = 100;
         ProgramGenerator generator = new ProgramGenerator(newArrayList(NOT, OR, AND, SHR1, IF0, FOLD, SHR4, PLUS),
                 size);
 
         Program p = generator.apply(new Random(0));
 
         System.out.println(p.toString());
         assertEquals(size, p.size());
     }
 
     @Test
     public void testWithoutUnary() throws Exception {
         int size = 4;
         ProgramGenerator generator = new ProgramGenerator(newArrayList(AND), size);
 
         Program p = generator.apply(new Random(125041));
 
         System.out.println(p.toString());
         assertEquals(size, p.size());
     }
 
     @Test
     public void testOnlyIf() throws Exception {
         int size = 100;
         ProgramGenerator generator = new ProgramGenerator(newArrayList(IF0), size);
 
         Program program = generator.apply(new Random());
 
         assertEquals(size, program.size());
         System.out.println(program);
     }

    @Ignore("is unlikely ever to succeed without a really heavy algorithm")
    @Test
    public void testNoProgramEquals() {
        int size = 30;
        ProgramGenerator generator =
                new ProgramGenerator(newArrayList(NOT, SHL1, SHR1, SHR4, SHR16, AND, OR, XOR, PLUS, IF0, FOLD), size);

        Program program = generator.apply(new Random());
        for (int i = 0; i < 30; i++) {
            assertNotEquals(program, generator.apply(new Random()));
        }
        assertTrue(true);
    }
 }
