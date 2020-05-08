 package model;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 /**
  * A single Gene, represented by one or more isoforms.
  * 
  * @author Mitchell Rosen
  * @version 20-Apr-2013
  */
 public class Gene {
     private List<GeneIsoform> isoforms;
     private Sequence sequence;
    
    public Gene(GeneIsoform isoform) {
       isoforms = new ArrayList<GeneIsoform>();
       isoforms.add(isoform);
    }
    
    public void addIsoform(GeneIsoform isoform) {
       isoforms.add(isoform);
    }
 
    /**
     * Parses a list of Genes from a GFF file.
     */
    public static List<Gene> fromGffFile(String filename) throws Exception {
       BufferedReader r = new BufferedReader(new FileReader(filename));
       List<Gene> genes = new ArrayList<Gene>();
 
       GffFeature feature = GffFeature.fromLine(r.readLine());
       while (feature != null) {
          if (!(feature instanceof GeneIsoform)) {
             // If we see score, cigar, etc. Just skip the line.
             feature = GffFeature.fromLine(r.readLine());
             continue;
          }
          GeneIsoform isoform = (GeneIsoform) feature;
          Gene gene = new Gene(isoform);
 
          feature = addIsoformsFromGffFile(r, isoform, gene);
 
          genes.add(gene);
       }
       r.close();
 
       return genes;
    }
 
    /**
     * Helper to fromGffFile. Reads features from |r|, adding isoforms to
     * |gene|'s isoforms until a feature is encountered that is not an isoform of
     * the same gene. Returns this feature, or null if EOF is encountered.
     * 
     * @throws IOException
     */
    protected static GffFeature addIsoformsFromGffFile(BufferedReader r, GeneIsoform isoform, Gene gene) 
          throws IOException {
       List<Exon> exons;
       GffFeature feature;
       
       String geneId = isoform.getGeneId();
       
       // Fill out the current Isoform with Exons (gene already initialized with it).
       exons = new ArrayList<Exon>();
       feature = addExonsFromGffFile(r, exons);
       isoform.setExons(exons);
       
       // Fill out the rest of the Isoforms.
       while (feature != null) {
          if (!(feature instanceof GeneIsoform))
             break;
          
          isoform = (GeneIsoform) feature;
          if (!isoform.getGeneId().equals(geneId))
             break;
             
          exons = new ArrayList<Exon>();
          feature = addExonsFromGffFile(r, exons);
          isoform.setExons(exons);
          gene.addIsoform(isoform);
       }
       
       return feature;
    }
 
    /**
     * Helper to addIsoformsFromGffFile. Reads features from |r|, adding Exons to
     * |exons|, until a feature is encountered that is not an Exon. Returns this
     * feature, or null if EOF is encountered.
     */
    protected static GffFeature addExonsFromGffFile(BufferedReader r, List<Exon> exons)
          throws IOException {
       GffFeature feature = GffFeature.fromLine(r.readLine());
       for (; feature != null; feature = GffFeature.fromLine(r.readLine())) {
          if (!(feature instanceof Exon))
             break;
          exons.add((Exon) feature);
       }
 
       return feature;
    }
    
    public String getId() {
       return isoforms.get(0).getGeneId();
    }
    
    public int getStart() {
       return isoforms.get(0).getStart();
    }
    
    public int getStop() {
       return isoforms.get(0).getStop();
    }
 
    public int numIsoforms() {
       return isoforms.size();
    }
 
    public int numExons() {
       int size = 0;
       for (GeneIsoform iso : isoforms)
          size += iso.numExons();
       return size;
    }
 
    public int numIntrons() {
       int size = 0;
       for (GeneIsoform iso : isoforms)
          size += iso.numIntrons();
       return size;
    }
 
    public int size() {
       return isoforms.get(0).size();
    }
 
    /**
     * Gets the CDS size of this Gene by including the CDS regions of each
     * isoform, taking care not to double count overlapping regions.
     */
    public int cdsSize() {
       Set<Integer> set = new HashSet<Integer>();
       for (GeneIsoform isoform : isoforms) {
          for (Exon exon : isoform.exons) {
             for (int i = exon.getStart(); i <= exon.getStop(); ++i)
                set.add(i);
          }
       }
       return set.size();
    }
 
    /**
     * Gets the total Exon size of all Isoforms, overlap included.
     */
    public int exonSize() {
       int size = 0;
       for (GeneIsoform iso : isoforms)
          size += iso.exonSize();
       return size;
    }
 
    /**
     * Gets the total Intron size of all Isoforms, overlap included.
     */
    public int intronSize() {
       int size = 0;
       for (GeneIsoform iso : isoforms)
          size += iso.intronSize();
       return size;
    }
 
    public void setSequence(Sequence sequence) {
       this.sequence = sequence;
    }
 
    public Sequence getSequence() {
       return sequence;
    }
 
    public void setIsoforms(List<GeneIsoform> isoforms) {
       this.isoforms = isoforms;
    }
 
    public List<GeneIsoform> getIsoforms() {
       return isoforms;
    }
 }
