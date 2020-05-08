 package test.group;
 
 import group.Permutation;
 import group.SimpleGroupGen;
 
 import java.util.ArrayList;
 import java.util.List;
 
import junit.framework.Assert;

 import org.junit.Test;
 
 public class SimpleGroupGenTest {
     
     public int makeFromGenerators(int n, String... cycleStrings) {
         List<Permutation> generators = new ArrayList<Permutation>();
         for (String cycleString : cycleStrings) {
             generators.add(Permutation.fromCycleString(cycleString, n));
         }
         List<Permutation> group = SimpleGroupGen.generate(n, generators);
         int index = 0;
         for (Permutation p : group) {
             System.out.println(index + "\t" + p + "\t" + p.toCycleString());
             index++;
         }
         return index;
     }
     
     @Test
     public void makeFrom4CycleGenerators() {
         int n = makeFromGenerators(4, "(1,3)", "(0,1)(2,3)");
         Assert.assertEquals(8, n);
     }
     
     @Test
     public void makeFrom6CycleAlternateColorGenerators() {
         int n = makeFromGenerators(6, "(1,5)(2,4)", "(0,2)(3,5)");
         Assert.assertEquals(6, n);
     }
     
     @Test
     public void makeCubeFromGenerators() {
         int n = makeFromGenerators(8, "(0,1,3,7,6,4)(2,5)", "(0,1,3,2)(4,5,7,6)");
         Assert.assertEquals(48, n);
     }
 
 }
