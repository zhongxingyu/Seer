 package model;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.*;
 
 import java.util.List;
 import java.util.Map;
 
 import org.junit.Test;
 
 /**
  * @author Mitchell Rosen
  * @version 21-Apr-2013
  */
 public class GeneTest {
    /**
     * Helper function to assert that a GffFeature's fields match the given parameters.
     */
    protected void testGffFeature(GffFeature gffFeature, String chromosome, String source,
          String feature, int start, int stop, String score, boolean reverse, String frame,
          int attrSize, String... attrKeyVals) {
       assertThat(gffFeature.getChromosome(), is(chromosome));
       assertThat(gffFeature.getSource(),     is(source));
       assertThat(gffFeature.getFeature(),    is(feature));
       assertThat(gffFeature.getStart(),      is(start));
       assertThat(gffFeature.getStop(),       is(stop));
       assertThat(gffFeature.getScore(),      is(score));
       assertThat(gffFeature.isReverse(),     is(reverse));
       assertThat(gffFeature.getFrame(),      is(frame));
       
       Map<String, String> attrs = gffFeature.getAttributes();
       assertThat(attrs.size(),         is(attrSize));
      assertThat(attrKeyVals.length, is(attrSize*2)); // Make sure we're testing all of them
       
       for (int i = 0; i < attrKeyVals.length-1; i += 2)
          assertThat(attrs.get(attrKeyVals[i]), is(attrKeyVals[i+1]));
    }
    
    @Test
    public void Gene() {
       throw new RuntimeException("Test not implemented");
    }
 
    @Test
    public void addExonsFromGffFile() throws Exception {
       Gene g;
       GeneIsoform iso;
       
       // Two genes, each with two isoforms, each with two exons
       List<Gene> genes = Gene.fromGffFile("test/files/gff1.txt");
       assertThat(genes.size(), is(2));
       
       // Gene 1 isoform 1
       g = genes.get(0);
       assertThat(g.getIsoforms().size(), is(2));
       iso = g.getIsoforms().get(0);
       testGffFeature(iso, "fosmid10", ".", "mRNA", 733, 4493, ".", true, ".", 2, 
             "gene_id", "\"alphaCop\";", "transcript_id", "\"alphaCop-RA\";");
       
       // Gene 1 isoform 1 exons
       assertThat(iso.getExons().size(), is(2));
       testGffFeature(iso.getExons().get(0), "fosmid10", ".", "CDS", 3548, 4493, ".", true, ".", 2, 
             "gene_id", "\"alphaCop\";", "transcript_id", "\"alphaCop-RA\";");
       testGffFeature(iso.getExons().get(1), "fosmid10", ".", "CDS", 733, 3491, ".", true, ".", 2, 
             "gene_id", "\"alphaCop\";", "transcript_id", "\"alphaCop-RA\";");
       
       // Gene 1 isoform 2
       iso = g.getIsoforms().get(1);
       testGffFeature(iso, "fosmid10", ".", "mRNA", 733, 4493, ".", true, ".", 2, 
             "gene_id", "\"alphaCop\";", "transcript_id", "\"alphaCop-RB\";");
       
       // Gene 1 isoform 2 exons
       assertThat(iso.getExons().size(), is(2));
       testGffFeature(iso.getExons().get(0), "fosmid10", ".", "CDS", 3548, 4493, ".", true, ".", 2, 
             "gene_id", "\"alphaCop\";", "transcript_id", "\"alphaCop-RA\";");
       testGffFeature(iso.getExons().get(1), "fosmid10", ".", "CDS", 733, 3491, ".", true, ".", 2, 
             "gene_id", "\"alphaCop\";", "transcript_id", "\"alphaCop-RA\";");
       
       // Gene 2 isoform 1
       g = genes.get(1);
       assertThat(g.getIsoforms().size(), is(2));
       iso = g.getIsoforms().get(0);
       testGffFeature(iso, "fosmid10", ".", "mRNA", 733, 4493, ".", false, ".", 2, 
             "gene_id", "\"alphaCop2\";", "transcript_id", "\"alphaCop2-RA\";");
       
       // Gene 2 isoform 1 exons
       assertThat(iso.getExons().size(), is(2));
       testGffFeature(iso.getExons().get(0), "fosmid10", ".", "CDS", 3548, 4493, ".", false, ".", 2, 
             "gene_id", "\"alphaCop2\";", "transcript_id", "\"alphaCop2-RA\";");
       testGffFeature(iso.getExons().get(1), "fosmid10", ".", "CDS", 733, 3491, ".", false, ".", 2, 
             "gene_id", "\"alphaCop2\";", "transcript_id", "\"alphaCop2-RA\";");
       
       // Gene 2 isoform 2
       iso = g.getIsoforms().get(1);
       testGffFeature(iso, "fosmid10", ".", "mRNA", 733, 4493, ".", false, ".", 2, 
             "gene_id", "\"alphaCop2\";", "transcript_id", "\"alphaCop-RB2\";");
       
       // Gene 2 isoform 2 exons
       assertThat(iso.getExons().size(), is(2));
       testGffFeature(iso.getExons().get(0), "fosmid10", ".", "CDS", 3548, 4493, ".", false, ".", 2, 
             "gene_id", "\"alphaCop2\";", "transcript_id", "\"alphaCop2-RA\";");
       testGffFeature(iso.getExons().get(1), "fosmid10", ".", "CDS", 733, 3491, ".", false, ".", 2, 
             "gene_id", "\"alphaCop2\";", "transcript_id", "\"alphaCop2-RA\";");
       
       assertThat(2, is(1));
    }
 
    @Test
    public void addIsoform() {
       throw new RuntimeException("Test not implemented");
    }
 
    @Test
    public void addIsoformsFromGffFile() {
       throw new RuntimeException("Test not implemented");
    }
 
    @Test
    public void cdsSize() {
       throw new RuntimeException("Test not implemented");
    }
 
    @Test
    public void exonSize() {
       throw new RuntimeException("Test not implemented");
    }
 
    @Test
    public void fromGffFile() {
       throw new RuntimeException("Test not implemented");
    }
 
    @Test
    public void getId() {
       throw new RuntimeException("Test not implemented");
    }
 
    @Test
    public void getIsoforms() {
       throw new RuntimeException("Test not implemented");
    }
 
    @Test
    public void getSequence() {
       throw new RuntimeException("Test not implemented");
    }
 
    @Test
    public void getStart() {
       throw new RuntimeException("Test not implemented");
    }
 
    @Test
    public void getStop() {
       throw new RuntimeException("Test not implemented");
    }
 
    @Test
    public void intronSize() {
       throw new RuntimeException("Test not implemented");
    }
 
    @Test
    public void numExons() {
       throw new RuntimeException("Test not implemented");
    }
 
    @Test
    public void numIntrons() {
       throw new RuntimeException("Test not implemented");
    }
 
    @Test
    public void numIsoforms() {
       throw new RuntimeException("Test not implemented");
    }
 
    @Test
    public void setSequence() {
       throw new RuntimeException("Test not implemented");
    }
 
    @Test
    public void size() {
       throw new RuntimeException("Test not implemented");
    }
 }
