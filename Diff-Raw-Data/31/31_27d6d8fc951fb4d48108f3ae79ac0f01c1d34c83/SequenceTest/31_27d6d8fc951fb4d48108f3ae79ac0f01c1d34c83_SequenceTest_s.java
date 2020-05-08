 package model;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class SequenceTest {
 
    private static Sequence smallSequence;
 
    // This was the filename, I have no idea what it actually means.
    private static Sequence dereDotContig2;
 
   // private static Sequence dereLarge;
    private static Sequence listSequence;
    private static Sequence emptySequence;
    @BeforeClass
    public static void beforeClass() throws IOException {
       smallSequence = new Sequence("smallfile.txt");
       dereDotContig2 = new Sequence("test_Dere_dot_contig2.txt");
      
       List<Nucleotide> nucleotides = new ArrayList<Nucleotide>();
       nucleotides.add(Nucleotide.ADENINE);
       nucleotides.add(Nucleotide.CYTOSINE);
       nucleotides.add(Nucleotide.GUANINE);
       nucleotides.add(Nucleotide.THYMINE);
       listSequence = new Sequence(nucleotides);
       emptySequence = new Sequence(Collections.<Nucleotide> emptyList());
 
       /*
        * This file has some weird stuff we should ask bio students about. As far
        * as I can tell, it is invalid.
        */
      // dereLarge = new Sequence("test_Dere_large.txt");
    }
 
    @Test
    public void testSlice() {
       Sequence sliced = listSequence.slice(1, 2);
       Assert.assertEquals(1, sliced.size());
       Assert.assertEquals(1, sliced.gcContentMax(), .001);
 
       sliced = dereDotContig2.slice(0, 4);
       Assert.assertEquals(4, sliced.size());
       Assert.assertEquals(.25, sliced.gcContentMax(), .001);
       
    }
 
    @Test(expected=IndexOutOfBoundsException.class)
    public void testBadSlice() {
       emptySequence.slice(1, 2);
    }
 
    @Test
    public void testGcContentHistogram() {
       // TODO: Test histogram and sliding window stuff.
    }
 
    @Test
    public void testGcContentMin() {
       Assert.assertEquals(.4, smallSequence.gcContentMin(), .001);
 
       // TODO: Fix/investigate this test.
       Assert.assertEquals(.174, dereDotContig2.gcContentMin(), .003); // Our tests say this should be .176
      // Assert.assertEquals(.346, dereLarge.gcContentMin(), .001);
 
    }
 
    @Test
    public void testGcContentMax() {
       Assert.assertEquals(.6, smallSequence.gcContentMax(), .001);

    }
 
 }
