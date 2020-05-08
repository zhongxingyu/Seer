 package au.org.intersect.samifier.parser;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.io.FilenameUtils;
 
 import au.org.intersect.samifier.domain.GeneInfo;
 import au.org.intersect.samifier.domain.GeneSequence;
 import au.org.intersect.samifier.domain.GenomeConstant;
 import au.org.intersect.samifier.domain.NucleotideSequence;
 
 public class FastaParserImpl implements FastaParser {
     private String previousChromosome;
     private String previousCode;
     private HashMap<String, Integer> chromosomeLength;
     private List<String> scannedFilesNames;
     private Map<String, File> chromosomeToFileName;
     private Map<String, ContigInfo> chromosomeToContigInfo;
     private boolean contig;
     public static final Pattern ALLOWED_CHARS_IN_FASTA_SEQUENCE = Pattern.compile("[^ACGT]");
     public FastaParserImpl(File chromosome) throws FastaParserException {
         contig = false;
         chromosomeLength = new HashMap<String, Integer>();
         scannedFilesNames = new ArrayList<String>();
         chromosomeToFileName = new HashMap<String, File>();
         chromosomeToContigInfo = new HashMap<String, ContigInfo>();
         if (chromosome.isDirectory()) {
             //build a list of fa and faa files
             File[] files = chromosome.listFiles(new FilenameFilter() {
                 public boolean accept(File dir, String name) {
                     return name.toLowerCase().endsWith(".fa") || name.toLowerCase().endsWith(".faa");
                 }
             });
             for (File fastaFile : files) {
                 chromosomeToFileName.put(FilenameUtils.removeExtension(fastaFile.getName()), fastaFile);
             }
         } else {
             //check for contig in file
             if (checkForContig(chromosome)) {
                 contig = true;
             } else {
                 chromosomeToFileName.put(FilenameUtils.getBaseName(chromosome.getName()), chromosome);
             }
         }
     }
 
 
     @Override
     public List<NucleotideSequence> extractSequenceParts(GeneInfo gene) throws IOException, FastaParserException {
         List<NucleotideSequence> parts = new ArrayList<NucleotideSequence>();
         List<GeneSequence> locations = gene.getLocations();
         String code = readCode(gene.getChromosome());
         for (GeneSequence location : locations) {
             // GFF (GenomeParserImpl) files use 1-based indices
             int startIndex = location.getStart() - 1;
             int stopIndex = location.getStop();
 
             if (!location.getSequenceType()) {
                 parts.add(new NucleotideSequence(null, GeneSequence.INTRON, location.getStart(), location.getStop()));
                 continue;
             }
             if (code.length() < startIndex || code.length() < stopIndex) {
                 continue;
             }
             StringBuilder sequence = new StringBuilder(code.substring(startIndex, stopIndex));
             String sequenceString;
             if (gene.isForward()) {
                 sequenceString = sequence.toString();
             } else {
                 sequenceString = StringUtils.replaceChars(sequence.reverse().toString(), "ACGT", "TGCA");
             }
             parts.add(new NucleotideSequence(sequenceString, GeneSequence.CODING_SEQUENCE, location.getStart(), location.getStop()));
         }
         if (GenomeConstant.REVERSE_FLAG.equals(gene.getDirectionStr())) {
             Collections.reverse(parts);
         }
 
         return parts;
     }
     @Override
     public int getChromosomeLength(String chromosome) {
         if (!chromosomeLength.containsKey(chromosome)) {
             try {
                 readCode(chromosome);
             } catch (IOException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             } catch (FastaParserException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }
         return chromosomeLength.get(chromosome);
     }
 
     private boolean checkForContig(File fastaFile) throws FastaParserException {
         boolean contig = false;
         RandomAccessFile reader = null;
         try {
             reader = new RandomAccessFile(fastaFile, "r");
             // parse header
             String line = reader.readLine();
             if (!line.startsWith(">")) {
                 throw new FastaParserException("Genome file not in FASTA format");
             }
             String chromosomeName = parseHeader(line);
             if (chromosomeName == null) {
                 return false;
             }
             ContigInfo info = new ContigInfo(fastaFile, reader.getFilePointer());
             while ((line = reader.readLine()) != null) {
                 if (line.startsWith(">")) {
                     info.setEndOffset(reader.getFilePointer() - line.length() - 1);
                     chromosomeToContigInfo.put(chromosomeName, info);
                     contig = true;
                     chromosomeName = parseHeader(line);
                     info = new ContigInfo(fastaFile, reader.getFilePointer());
                 }
             }
            info.setEndOffset(reader.getFilePointer());
            chromosomeToContigInfo.put(chromosomeName, info);
            contig = true;
             reader.close();
             scannedFilesNames.add(fastaFile.getName());
             return contig;
         } catch (IOException e){
             throw new FastaParserException(e.getMessage());
         }
     }
 
     public String readCode(String chromosomeName) throws IOException, FastaParserException {
         String code;
         if (previousChromosome != null
                 && previousChromosome.equals(chromosomeName)) {
             code = previousCode;
         } else {
             code = readCodeFromFile(chromosomeName);
             chromosomeLength.put(chromosomeName , code.length());
             previousChromosome = chromosomeName;
             previousCode = code;
         }
         return code;
     }
 
     public List<String> scanForChromosomes() throws FastaParserException {
         if (contig) {
             if (scannedFilesNames.size() < chromosomeToFileName.values().size()) {
                 List<File> fileNames = new ArrayList<File>(chromosomeToFileName.values());
                 fileNames.removeAll(scannedFilesNames);
                 for (File file : fileNames) {
                     checkForContig(file);
                 }
             }
             return new ArrayList<String>(chromosomeToContigInfo.keySet());
         } else {
             return new ArrayList<String>(chromosomeToFileName.keySet());
         }
     }
 
     private String readCodeFromFile(String chromosomeName) throws FastaParserException, IOException {
         if (chromosomeToFileName.containsKey(chromosomeName)) {
             return readFromSingleFast(chromosomeToFileName.get(chromosomeName));
         } else if (chromosomeToContigInfo.containsKey(chromosomeName)) {
             return readFromContigFile(chromosomeName);
         } else if (scannedFilesNames.size() < chromosomeToFileName.values().size()) {
             List<File> fileNames = new ArrayList<File>(chromosomeToFileName.values());
             fileNames.removeAll(scannedFilesNames);
             for (File file : fileNames) {
                 if (checkForContig(file)) {
                     contig = true;
                 }
                 if (chromosomeToContigInfo.containsKey(chromosomeName)) {
                     return readFromContigFile(chromosomeName);
                 }
             }
             throw new FileNotFoundException("Can't find fasta file for chromosome: " + chromosomeName);
         } else if (!contig) {
             String strippedName = FilenameUtils.removeExtension(chromosomeName);
             if (chromosomeToFileName.containsKey(strippedName)) {
                 return readFromSingleFast(chromosomeToFileName.get(strippedName));
             }
         } else {
             throw new FileNotFoundException("Can't find fasta file for chromosome: " + chromosomeName);
         }
         return null;
     }
 
     private String readFromSingleFast(File chromosomeFile) throws IOException, FastaParserException {
         BufferedReader reader = null;
         try {
             reader = new BufferedReader(new FileReader(chromosomeFile));
             // Skip header of FASTA file
             String line = reader.readLine();
             if (!line.startsWith(">")) {
                 throw new FastaParserException("Genome file not in FASTA format");
             }
             StringBuffer buffer = new StringBuffer();
             while ((line = reader.readLine()) != null) {
                 buffer.append(line);
             }
             reader.close();
             return cleanCode(buffer.toString());
         } finally {
             reader.close();
         }
     }
 
     private String cleanCode(String fastCode) throws FastaParserException {
         String clean = fastCode.replace("\r", "").replace("\n", "");
         /*Matcher matcher = ALLOWED_CHARS_IN_FASTA_SEQUENCE.matcher(clean);
         if (matcher.find()) {
             System.err.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
             System.err.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
             System.err.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
             System.err.println(clean);
             System.err.println(matcher.group(0));
             System.err.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
             System.err.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
             System.err.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
             throw new FastaParserException("Illegal chars in FASTA sequence");
         }*/
         return clean;
     }
     private String readFromContigFile(String chromosome) throws IOException, FastaParserException{
         ContigInfo contigInfo = chromosomeToContigInfo.get(chromosome);
         byte[] buffer = new byte[(int) (contigInfo.endOffset - contigInfo.startOffset)];
         RandomAccessFile reader = null;
         try {
             reader = new RandomAccessFile(contigInfo.getFastaFile(), "r");
             reader.seek(contigInfo.startOffset);
             int read = reader.read(buffer);
             if (read != buffer.length) {
                 throw new FastaParserException("Wrong sequence for chromosome: " + chromosome);
             }
         } finally {
             reader.close();
         }
         return  cleanCode(new String(buffer));
     }
     private String parseHeader(String line) {
         String[] parts = line.split("\\|");
         if (parts.length < 2) return null;
         return parts[3];
     }
     private class ContigInfo {
         private File fastaFile;
         private long startOffset;
         private long endOffset;
         public ContigInfo(File fastaFile , long startOffset) {
             this.fastaFile = fastaFile;
             this.startOffset = startOffset;
         }
         public File getFastaFile() {
             return fastaFile;
         }
         public long getStartOffset() {
             return startOffset;
         }
         public long getEndOffset() {
             return endOffset;
         }
         public void setEndOffset(long endOffset) {
             this.endOffset = endOffset;
         }
     }
 }
