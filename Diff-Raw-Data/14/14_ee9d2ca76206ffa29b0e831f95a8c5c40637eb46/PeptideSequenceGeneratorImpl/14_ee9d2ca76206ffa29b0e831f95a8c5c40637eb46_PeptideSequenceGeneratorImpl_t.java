 package au.org.intersect.samifier.generator;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import au.org.intersect.samifier.domain.GeneInfo;
 import au.org.intersect.samifier.domain.GeneSequence;
 import au.org.intersect.samifier.domain.Genome;
 import au.org.intersect.samifier.domain.GenomeConstant;
 import au.org.intersect.samifier.domain.NucleotideSequence;
 import au.org.intersect.samifier.domain.PeptideSearchResult;
 import au.org.intersect.samifier.domain.PeptideSequence;
 import au.org.intersect.samifier.domain.ProteinToOLNMap;
 import au.org.intersect.samifier.parser.FastaParserException;
 import au.org.intersect.samifier.parser.FastaParserImpl;
 
 public class PeptideSequenceGeneratorImpl implements PeptideSequenceGenerator {
     private static Logger LOG = Logger
             .getLogger(PeptideSequenceGeneratorImpl.class);
 
     private Genome genome;
     private ProteinToOLNMap proteinOLNMap;
     private File chromosomeDirectory;
     private FastaParserImpl fasta;
 
     public PeptideSequenceGeneratorImpl(Genome genome, ProteinToOLNMap proteinOLNMap, File chromosomeDirectory) {
         this.genome = genome;
         this.proteinOLNMap = proteinOLNMap;
         this.chromosomeDirectory = chromosomeDirectory;
         this.fasta = new FastaParserImpl();
     }
 
     @Override
     public List<PeptideSequence> getPeptideSequences(
             List<PeptideSearchResult> peptideSearchResults)
             throws PeptideSequenceGeneratorException {
         List<PeptideSequence> peptideSequenceList = new ArrayList<PeptideSequence>();
         for (PeptideSearchResult searchResult : peptideSearchResults) {
             PeptideSequence sequence = getPeptideSequence(searchResult);
             if (sequence != null) {
                 peptideSequenceList.add(sequence);
             }
         }
         return peptideSequenceList;
     }
 
     @Override
     public PeptideSequence getPeptideSequence(
             PeptideSearchResult peptideSearchResult)
             throws PeptideSequenceGeneratorException {
         String proteinName = peptideSearchResult.getProteinName();
         String oln = proteinOLNMap.getOLN(proteinName);
 
         GeneInfo gene = genome.getGene(oln);
         if (gene == null) {
             LOG.info("Protein ID found in accession file, but locus not found in genome file");
             LOG.info("ERR_GFF: " + proteinName + " " + oln);
             return null;
         }
 
         File chromosomeFile = getChromosomeFile(gene);
 
         return getPeptideSequenceFromChromosomeFile(peptideSearchResult,
                 chromosomeFile, gene);
     }
 
     public PeptideSequence getPeptideSequenceFromChromosomeFile(PeptideSearchResult peptide, File chromosomeFile, GeneInfo gene)
             throws PeptideSequenceGeneratorException {
         List<NucleotideSequence> sequenceParts;
         try {
             sequenceParts = extractSequenceParts(chromosomeFile, gene);
         } catch (FastaParserException e) {
             throw new PeptideSequenceGeneratorException("Genome file not in FASTA format", e);
         } catch (FileNotFoundException e) {
             throw new PeptideSequenceGeneratorException("Chromosome file not found", e);
         } catch (IOException e) {
             throw new PeptideSequenceGeneratorException("Cannot open chromosome file", e);
         }
 
         if (sequenceParts.size() == 0) {
             LOG.warn(gene.getId() + " in " + chromosomeFile.getName() + " seems empty");
             return null;
         }
 
         StringBuilder nucleotideSequence = new StringBuilder();
         StringBuilder cigar = new StringBuilder();
 
         // Coordinates for the peptide are 1-based, so substract 1 so it
         // can be used with a 0-based string slice.
         int relativeStart = (peptide.getPeptideStart() - 1)
                 * GenomeConstant.BASES_PER_CODON;
         int relativeStop = peptide.getPeptideStop()
                 * GenomeConstant.BASES_PER_CODON - 1;
 
         int absoluteStartIndex = 0;
         int absoluteStopIndex = 0;
         int readCursor = 0;
         String direction = gene.getDirectionStr();
 
         /*
          * The chromosome nucleotide sequence contains everything- exons and
          * introns. The peptide positions we are given exclude the introns.
          * Hence, we walk through each sequence part (describe in the genome
          * file), skipping past introns and just counting through exons.
          */
         for (NucleotideSequence part : sequenceParts) {
             // Skip introns, but mark them in the cigar string
             if (GeneSequence.INTRON.equals(part.getType())) {
                 /*
                  * We don't start cigar strings with introns. A non-empty cigar
                  * string means we've already got part of the peptide's
                  * sequence, and this intron is in the middle.
                  */
                 int size = part.getStopIndex() - part.getStartIndex() + 1;
                 if (cigar.length() > 0) {
                     updateCigar(cigar, size, GeneSequence.INTRON, direction);
                     absoluteStopIndex += size;
                 } else {
                     // This intron is before our absolute start position
                     absoluteStartIndex += size;
                     absoluteStopIndex += size;
                 }
                 continue;
             }
 
             int sequenceSize = part.getSequence().length();
             int substringStart = 0;
             int substringEnd = sequenceSize;
 
             // If the desired start position is not in this coding sequence,
             // move our cursor past it
             if (relativeStart > (readCursor + sequenceSize - 1)) {
                 readCursor += sequenceSize;
                 absoluteStartIndex += sequenceSize;
                 absoluteStopIndex += sequenceSize;
                 continue;
             }
 
             // After skipping through, the next part should have the starting
             // position within it. Update the absoluteStartIndex for the last
             // time.
             if (readCursor < relativeStart) {
                 substringStart = relativeStart - readCursor;
                 absoluteStartIndex += substringStart;
                 absoluteStopIndex += substringStart;
             }
 
             // If this part contains the stop position, then this is the last
             // part to process.
             if ((readCursor + sequenceSize) > relativeStop) {
                 substringEnd = relativeStop - readCursor + 1;
                 nucleotideSequence.append(part.getSequence().substring(
                         substringStart, substringEnd));
                 int partSize = substringEnd - substringStart;
                 absoluteStopIndex += partSize;
                 updateCigar(cigar, partSize, GeneSequence.CODING_SEQUENCE,
                         direction);
                 break;
             }
 
             nucleotideSequence.append(part.getSequence().substring(
                     substringStart, substringEnd));
             int partSize = substringEnd - substringStart;
             absoluteStopIndex += partSize;
             updateCigar(cigar, partSize, GeneSequence.CODING_SEQUENCE,
                     direction);
             // readCursor = substringEnd;
             readCursor += part.getSequence().length();
         }
 
         String peptideSequence = nucleotideSequence.toString(); 
                 //GenomeConstant.REVERSE_FLAG.equals(direction) ? nucleotideSequence.reverse().toString() : nucleotideSequence.toString();
         // When direction is reverse,
         // 5 17
         // |####----#####|
         // ^ S
         // 6 15
         // absoluteStopIndex = 11, from 17 to 6
         // absoluteStartIndex = 2, from 17 to 15
         // startIndex = (17-5 = 12) - 11 + 1 = 2 (because it is 1 based)
         // stopIndex = (17-5 = 12) - 2 + 1 = 11 (because it is 1 based)
         if (peptideSequence.length() == 0) {
             return null;
         }
         int startIndex = GenomeConstant.REVERSE_FLAG.equals(direction) ? (gene.getStop() - gene.getStart() - absoluteStopIndex + 1) : absoluteStartIndex;
         int stopIndex = GenomeConstant.REVERSE_FLAG.equals(direction) ? (gene.getStop() - gene.getStart() - absoluteStartIndex + 1) : absoluteStopIndex;
         int bedStartIndex = gene.getStart() + startIndex - 1; // BED files are
                                                               // zero based (6
                                                               // in the
                                                               // example)
         int bedStopIndex = gene.getStart() + stopIndex - 1; // BED files are
                                                             // zero based (15 in
                                                             // the example)
         return new PeptideSequence(peptideSequence, cigar.toString(), startIndex, bedStartIndex, bedStopIndex, gene);
     }
 
     private void updateCigar(StringBuilder cigar, int size, String type,
             String direction) {
         String marker = GeneSequence.INTRON.equals(type) ? "N" : "M";
         if (GenomeConstant.REVERSE_FLAG.equals(direction)) {
             cigar.insert(0, marker);
             cigar.insert(0, size);
         } else {
             cigar.append(size);
             cigar.append(marker);
         }
     }
 
     protected List<NucleotideSequence> extractSequenceParts(
             File chromosomeFile, GeneInfo gene) throws IOException, FastaParserException {
         return fasta.extractSequenceParts(chromosomeFile, gene);
     }
 
     private File getChromosomeFile(GeneInfo gene) {
         // TODO: find the different chrormosome file extensions
        File faExt = new File(chromosomeDirectory, gene.getChromosome() + ".fa");
        if (faExt.exists()) return faExt;
        return new File(chromosomeDirectory, gene.getChromosome() + ".faa");
     }
 
 }
