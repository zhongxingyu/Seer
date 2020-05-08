 package au.org.intersect.samifier.generator;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import au.org.intersect.samifier.domain.CodonTranslationTable;
 import au.org.intersect.samifier.domain.EqualProteinOLNMap;
 import au.org.intersect.samifier.domain.GeneInfo;
 import au.org.intersect.samifier.domain.Genome;
 import au.org.intersect.samifier.domain.GenomeConstant;
 import au.org.intersect.samifier.domain.GenomeNucleotides;
 import au.org.intersect.samifier.domain.PeptideSearchResult;
 import au.org.intersect.samifier.domain.ProteinLocation;
 import au.org.intersect.samifier.domain.ProteinToOLNMap;
 import au.org.intersect.samifier.domain.TranslationTableParsingException;
 import au.org.intersect.samifier.parser.GenomeFileParsingException;
 import au.org.intersect.samifier.parser.GenomeParserImpl;
 import au.org.intersect.samifier.parser.MascotParsingException;
 import au.org.intersect.samifier.parser.PeptideSearchResultsParser;
 import au.org.intersect.samifier.parser.PeptideSearchResultsParserImpl;
 
 public class VirtualProteinMascotLocationGenerator implements LocationGenerator {
     private static Logger LOG = Logger.getLogger(VirtualProteinMascotLocationGenerator.class);
     private static int NOT_FOUND = -1;
 
     private File genomeFile;
     private File translationTableFile;
     private File chromosomeDir;
     private String[] searchResultsPaths;
 
     private Genome genome;
     private ProteinToOLNMap proteinToOLNMap;
     private Map<String, GenomeNucleotides> genomeNucleotidesMap = new HashMap<String, GenomeNucleotides>();
     private CodonTranslationTable translationTable;
 
     public VirtualProteinMascotLocationGenerator(String[] searchResultsPaths,
             File translationTableFile, File genomeFile, File chromosomeDir) {
         this.searchResultsPaths = searchResultsPaths;
         this.genomeFile = genomeFile;
         this.chromosomeDir = chromosomeDir;
         this.translationTableFile = translationTableFile;
     }
 
     @Override
     public List<ProteinLocation> generateLocations()
             throws LocationGeneratorException {
         try {
             List<ProteinLocation> locations = doGenerateLocations();
             locations = removeDuplicates(locations);
             locations = mergeProteins(locations);
             Collections.sort(locations);
             for (int i = 0; i < locations.size(); i++) {
                 locations.get(i).setName("q" + i);
             }
             return locations;
 
         } catch (TranslationTableParsingException e) {
             throw new LocationGeneratorException(
                     "Error parsing translation table file", e);
         } catch (MascotParsingException e) {
             throw new LocationGeneratorException("Error parsing mascot file", e);
         } catch (GenomeFileParsingException e) {
             throw new LocationGeneratorException("Error parsing genome file", e);
         } catch (IOException e) {
             throw new LocationGeneratorException("Error parsing genome file", e);
         }
 
     }
 
     public List<ProteinLocation> doGenerateLocations()
             throws GenomeFileParsingException, MascotParsingException,
             IOException, TranslationTableParsingException {
         List<ProteinLocation> proteinLocations = new ArrayList<ProteinLocation>();
         System.out.println("Generating locations");
         GenomeParserImpl genomeParser = new GenomeParserImpl();
         genome = genomeParser.parseGenomeFile(genomeFile);
 
         proteinToOLNMap = new EqualProteinOLNMap();
 
         PeptideSearchResultsParser peptideSearchResultsParser = new PeptideSearchResultsParserImpl(
                 proteinToOLNMap);
         List<PeptideSearchResult> peptideSearchResults = peptideSearchResultsParser
                 .parseResults(searchResultsPaths);
         translationTable = CodonTranslationTable
                 .parseTableFile(translationTableFile);
 
         for (PeptideSearchResult peptideSearchResult : peptideSearchResults) {
             GeneInfo geneInfo = genome.getGene(peptideSearchResult
                     .getProteinName());
 
             if (geneInfo == null) {
                 System.err.println(peptideSearchResult.getProteinName()
                         + " not found in the genome");
                 continue;
             }
 
             int virtualGeneStart = geneInfo.getStart() - 1;
             int virtualGeneStop = geneInfo.getStop() - 1;
 
             int peptideAbsoluteStart;
             int peptideAbsoluteStop;
 
             int startOffset = (peptideSearchResult.getPeptideStart() - 1) * GenomeConstant.BASES_PER_CODON;
             int stopOffset = (peptideSearchResult.getPeptideStop() - 1) * GenomeConstant.BASES_PER_CODON;
 
             if (geneInfo.isForward()) {
                 peptideAbsoluteStart = virtualGeneStart + startOffset;
                 peptideAbsoluteStop = virtualGeneStart + stopOffset;
            } else {
                peptideAbsoluteStart = virtualGeneStop - startOffset;
                peptideAbsoluteStop = virtualGeneStop - stopOffset;
             }
 
             File geneFile = getChromosomeFile(chromosomeDir, geneInfo.getChromosome());
             GenomeNucleotides genomeNucleotides = getGenomeNucleotides(geneFile);
             int startPosition = searchStart(peptideSearchResult, peptideAbsoluteStart, genomeNucleotides, geneInfo);
             int stopPosition = searchStop(peptideSearchResult, peptideAbsoluteStop, genomeNucleotides, geneInfo, false);
             if (startPosition == NOT_FOUND || stopPosition == NOT_FOUND) {
                 continue;
             }
             ProteinLocation loc = new ProteinLocation("?", startPosition, Math.abs(stopPosition - startPosition),
                     geneInfo.getDirectionStr(), "0", peptideSearchResult.getConfidenceScore(),
                     peptideSearchResult.getProteinName() + "(" + virtualGeneStart + "-" + virtualGeneStop + ")", geneInfo.getChromosome());
            loc.setAbsoluteStartStop(peptideAbsoluteStart + "_" + Math.abs(stopOffset - startOffset));
             proteinLocations.add(loc);
         }
         return proteinLocations;
     }
 
     private List<ProteinLocation> mergeProteins(List<ProteinLocation> locations) {
         Map<Integer, ProteinLocation> proteinMap = new HashMap<Integer, ProteinLocation>();
         for (ProteinLocation location : locations) {
             ProteinLocation loc = proteinMap.get(location.getStop());
             if (loc == null) {
                 proteinMap.put(location.getStop(), location);
             } else {
                loc.update(location);
             }
         }
         ArrayList<ProteinLocation> proteinList = new ArrayList<ProteinLocation>(proteinMap.values());
         return proteinList;
     }
     private List<ProteinLocation> removeDuplicates(List<ProteinLocation> locations) {
         Map<String, ProteinLocation> uniqueLocation = new HashMap<String, ProteinLocation>();
         for (ProteinLocation loc : locations) {
             ProteinLocation propetinOnList = uniqueLocation.get(loc.getAbsoluteStartStop());
             if (propetinOnList == null) {
                 uniqueLocation.put(loc.getAbsoluteStartStop(), loc);
             } else {
                 if (loc.getStartIndex() < propetinOnList.getStartIndex()) {
                     uniqueLocation.put(loc.getAbsoluteStartStop(), loc);
                 }
             }
         }
         return new ArrayList<ProteinLocation>(uniqueLocation.values());
     }
 
     private int searchStop(PeptideSearchResult peptideSearchResult, int peptideAbsoluteStart, GenomeNucleotides genomeNucleotides, GeneInfo geneInfo, boolean reverse) {
         boolean reachedStop = false;
         int endIterator = peptideAbsoluteStart;
 
         int searchDirection = reverse ? geneInfo.getDirection() * (-1) : geneInfo.getDirection();
         boolean isStopCodon = translationTable.isStopCodon(genomeNucleotides
                 .codonAt(endIterator, geneInfo.getDirection()));
 
         while (!reachedStop && !isStopCodon) {
             endIterator += incrementPosition(searchDirection);
             isStopCodon = translationTable.isStopCodon(genomeNucleotides
                     .codonAt(endIterator, geneInfo.getDirection()));
             reachedStop = reachedEnd(endIterator, searchDirection, genomeNucleotides.getSize());
         }
 
         if (reachedStop && !isStopCodon) {
             LOG.warn("Reached end of sequence without finding stop codon for peptide " + peptideSearchResult.getPeptideSequence());
             return endIterator;
         }
 
         return endIterator;
     }
 
     private int searchStart(PeptideSearchResult peptideSearchResult, int peptideAbsoluteStart, GenomeNucleotides genomeNucleotides, GeneInfo geneInfo) {
         //find last stop and search for start from there ..
         int previousStop = searchStop(peptideSearchResult, peptideAbsoluteStart, genomeNucleotides, geneInfo, true);
         boolean reachedStart = false;
         int startIterator = previousStop;
         int direction = geneInfo.getDirection();
 
         boolean isStartCodon = translationTable.isStartCodon(genomeNucleotides.codonAt(startIterator, direction));
 
         while (!reachedStart && !isStartCodon) {
             startIterator += incrementPosition(geneInfo.getDirection());
             isStartCodon = translationTable.isStartCodon(genomeNucleotides
                     .codonAt(startIterator, direction));
             reachedStart = reachedStart(startIterator, geneInfo.getDirection(), peptideAbsoluteStart);
         }
 
         if (reachedStart && !isStartCodon) {
             LOG.error("Reached beginning of sequence without finding start codon for peptide "
                     + peptideSearchResult.getPeptideSequence());
             if (isStopOnEdge(previousStop, genomeNucleotides, geneInfo.isForward())) {
                 return previousStop;
             } else {
                 return previousStop + (geneInfo.getDirection() * GenomeConstant.BASES_PER_CODON);
             }
         }
 
         return startIterator;
     }
 
     private boolean isStopOnEdge(int previousStop, GenomeNucleotides genomeNucleotides, boolean forward) {
         if (forward) {
             return previousStop < GenomeConstant.BASES_PER_CODON;
         }
         return previousStop > (genomeNucleotides.getSize() - GenomeConstant.BASES_PER_CODON);
     }
 
     private boolean reachedEnd(int position, int direction, int size) {
         if (direction == -1) {
             return position <= 0;
         } else {
             return position + GenomeConstant.BASES_PER_CODON >= size;
         }
     }
 
     private boolean reachedStart(int position, int direction, int peptideStart) {
         if (direction == -1) {
             return position + GenomeConstant.BASES_PER_CODON >= peptideStart;
         } else {
             return position >= peptideStart;
         }
     }
 
     private GenomeNucleotides getGenomeNucleotides(File geneFile)
             throws IOException {
         if (!genomeNucleotidesMap.containsKey(geneFile.getName())) {
             genomeNucleotidesMap.put(geneFile.getName(), new GenomeNucleotides(
                     geneFile));
         }
         return genomeNucleotidesMap.get(geneFile.getName());
     }
 
     private int incrementPosition(int direction) {
         return direction * GenomeConstant.BASES_PER_CODON;
     }
 
     private File getChromosomeFile(File chromosomeDir, String chromosome) {
         // TODO: find the different chrormosome file extensions
         File faExt = new File(chromosomeDir, chromosome + ".fa");
         if (faExt.exists()) return faExt;
         return new File(chromosomeDir, chromosome + ".faa");
     }
 }
