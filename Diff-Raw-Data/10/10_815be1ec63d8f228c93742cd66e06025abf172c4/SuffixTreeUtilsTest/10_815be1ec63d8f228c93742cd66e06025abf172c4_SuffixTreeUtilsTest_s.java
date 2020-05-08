 package suffixtree;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import junit.framework.Assert;
 
 import model.Gene;
 import model.GeneIsoform;
 import model.Nucleotide;
 import model.Sequence;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 /**
  * Tests for the {@link SuffixTreeUtils} class.
  * @author Cameron Stearns
  */
 public class SuffixTreeUtilsTest {
 
    private static SuffixTreeUtils utils;
    @BeforeClass
    public static void setup() {
       GeneIsoform positive1 = new GeneIsoform("", 2, 2, false, "", "");
       GeneIsoform positive2 = new GeneIsoform("", 3, 3, false, "", "");
       GeneIsoform negative1 = new GeneIsoform("", 2, 2, true, "", "");
       GeneIsoform negative2 = new GeneIsoform("", 3, 3, true, "", "");
       Gene gene = Gene.create(positive1);
       gene.addIsoform(positive2);
       gene.addIsoform(negative1);
       gene.addIsoform(negative2);
       List<Nucleotide> nucleotides = new ArrayList<Nucleotide>();
       nucleotides.add(Nucleotide.ADENINE);
       nucleotides.add(Nucleotide.ADENINE);
       nucleotides.add(Nucleotide.THYMINE);
       nucleotides.add(Nucleotide.CYTOSINE);
 
       Sequence sequence = new Sequence(nucleotides);
       utils = new SuffixTreeUtils(sequence,
             Collections.singletonList(gene));
    }
 
    @Test
    public void testReverseComplementString() {
       Assert.assertEquals("CGATT", SuffixTreeUtils.reverseComplement("AATCG"));
       Assert.assertEquals("CGATN", SuffixTreeUtils.reverseComplement("NATCG"));
       Assert.assertEquals("NNTNN", SuffixTreeUtils.reverseComplement("NNANN"));
    }
 
    @Test
    public void testFindExpectedFoldExpression() {
       Assert.assertEquals(0,utils.findExpectedFoldExpression("AATG"), .000001);
       Assert.assertEquals(.0625,utils.findExpectedFoldExpression("AATC"), .000001);
       // This value seems weird.
       Assert.assertEquals(.125,utils.findExpectedFoldExpression("AAAAA"), .000001);
    }
 
    @Test
    public void testAverageDistanceToNextPositiveMRNA() {
       List<Integer> starts = new ArrayList<Integer>();
       starts.add(1); // 1
       starts.add(2); // 1
       starts.add(3); // 1 
       starts.add(4); // 0
       Assert.assertEquals(.75, utils.averageDistanceToNextPositiveMRNA(starts),
             .0001);
    }
 
    @Test
    public void testAverageDistanceToNextNegativeMRNA() {
       List<Integer> starts = new ArrayList<Integer>();
       starts.add(1); // 1
       starts.add(2); // 2
       starts.add(3); // 1 
       starts.add(4); // 1
       Assert.assertEquals(1.25, utils.averageDistanceToNextNegativeMRNA(starts),
             .0001);
    }
 
    @Test
    public void testStripStartsOutsideRange() {
       GeneIsoform positive1 = new GeneIsoform("", 10, 10, false, "", "");
       GeneIsoform positive2 = new GeneIsoform("", 20, 20, false, "", "");
       GeneIsoform negative1 = new GeneIsoform("", 30, 30, true, "", "");
       GeneIsoform negative2 = new GeneIsoform("", 40, 40, true, "", "");
 
       Gene gene = Gene.create(positive1);
       gene.addIsoform(positive2);
       gene.addIsoform(negative1);
       gene.addIsoform(negative2);
 
       int maxDistance = 5;
       List<Integer> starts = new ArrayList<Integer>();
      starts.add(0); // Discard
       starts.add(5); // Keep
       starts.add(11); // Discard
       starts.add(30); // Keep
       starts.add(32); // Keep
       starts.add(34); // Discard
       starts.add(39); // Discard
       starts.add(500); // Discard.
 
       List<Integer> expectedStarts = new ArrayList<Integer>();
      expectedStarts.add(5); // Keep
      expectedStarts.add(30); // Keep
      expectedStarts.add(32); // Keep
       int stringLength = 3;
 
       Sequence sequence = new Sequence();
       utils = new SuffixTreeUtils(sequence, Collections.singletonList(gene));
 
       utils.stripStartsOutsideRange(maxDistance, starts, stringLength);
       Assert.assertEquals(expectedStarts, starts);
    }
 }
