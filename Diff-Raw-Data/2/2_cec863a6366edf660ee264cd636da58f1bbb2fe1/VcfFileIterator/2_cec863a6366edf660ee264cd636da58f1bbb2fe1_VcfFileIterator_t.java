 package ca.mcgill.mcb.pcingola.fileIterator;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 import ca.mcgill.mcb.pcingola.fileIterator.parser.Parser;
 import ca.mcgill.mcb.pcingola.interval.Genome;
 import ca.mcgill.mcb.pcingola.util.Gpr;
 import ca.mcgill.mcb.pcingola.vcf.VcfEntry;
 import ca.mcgill.mcb.pcingola.vcf.VcfInfo;
 import ca.mcgill.mcb.pcingola.vcf.VcfInfoType;
 
 /**
  * Opens a VCF file and iterates over all entries
  * 
  * Format: VCF 4.1
  * 
  * Reference: 	http://www.1000genomes.org/wiki/Analysis/Variant%20Call%20Format/vcf-variant-call-format-version-41
  * 				Old 4.0 format: http://www.1000genomes.org/wiki/doku.php?id=1000_genomes:analysis:vcf4.0
  * 
  * 1. CHROM chromosome: an identifier from the reference genome. All entries for a specific CHROM should form a contiguous block within the VCF file.(Alphanumeric String, Required)
  * 2. POS position: The reference position, with the 1st base having position 1. Positions are sorted numerically, in increasing order, within each reference sequence CHROM. (Integer, Required)
  * 3. ID semi-colon separated list of unique identifiers where available. If this is a dbSNP variant it is encouraged to use the rs number(s). No identifier should be present in more than one data record. If there is no identifier available, then the missing value should be used. (Alphanumeric String)
  * 4. REF reference base(s): Each base must be one of A,C,G,T,N. Bases should be in uppercase. Multiple bases are permitted. The value in the POS field refers to the position of the first base in the String. For InDels, the reference String must include the base before the event (which must be reflected in the POS field). (String, Required).
  * 5. ALT comma separated list of alternate non-reference alleles called on at least one of the samples. Options are base Strings made up of the bases A,C,G,T,N, or an angle-bracketed ID String (”<ID>”). If there are no alternative alleles, then the missing value should be used. Bases should be in uppercase. (Alphanumeric String; no whitespace, commas, or angle-brackets are permitted in the ID String itself)
  * 6. QUAL phred-scaled quality score for the assertion made in ALT. i.e. give -10log_10 prob(call in ALT is wrong). If ALT is ”.” (no variant) then this is -10log_10 p(variant), and if ALT is not ”.” this is -10log_10 p(no variant). High QUAL scores indicate high confidence calls. Although traditionally people use integer phred scores, this field is permitted to be a floating point to enable higher resolution for low confidence calls if desired. (Numeric)
  * 7. FILTER filter: PASS if this position has passed all filters, i.e. a call is made at this position. Otherwise, if the site has not passed all filters, a semicolon-separated list of codes for filters that fail. e.g. “q10;s50” might indicate that at this site the quality is below 10 and the number of samples with data is below 50% of the total number of samples. “0” is reserved and should not be used as a filter String. If filters have not been applied, then this field should be set to the missing value. (Alphanumeric String)
  * 8. INFO additional information: (Alphanumeric String) INFO fields are encoded as a semicolon-separated series of short keys with optional values in the format: <key>=<data>[,data]. Arbitrary keys are permitted, although the following sub-fields are reserved (albeit optional):
  *        - AA ancestral allele
  *        - AC allele count in genotypes, for each ALT allele, in the same order as listed
  *        - AF allele frequency for each ALT allele in the same order as listed: use this when estimated from primary data, not called genotypes
  *        - AN total number of alleles in called genotypes
  *        - BQ RMS base quality at this position
  *        - CIGAR cigar string describing how to align an alternate allele to the reference allele
  *        - DB dbSNP membership
  *        - DP combined depth across samples, e.g. DP=154
  *        - END end position of the variant described in this record (esp. for CNVs)
  *        - H2 membership in hapmap2
  *        - MQ RMS mapping quality, e.g. MQ=52
  *        - MQ0 Number of MAPQ == 0 reads covering this record
  *        - NS Number of samples with data
  *        - SB strand bias at this position
  *        - SOMATIC indicates that the record is a somatic mutation, for cancer genomics
  *        - VALIDATED validated by follow-up experiment
  * 
  * Warning: You can have more than one type of change simultaneously, 
  *          e.g.:
  *          	TTG	->	TTGTG,T					Insertion of 'TG' and deletion of 'TG'
  *          	TA	->	T,TT					Deletion of 'A' and SNP (A replaced by T) 
  *				T	->	TTTTGTG,TTTTG,TTGTG		Insertion of 'TTTGTG', insertion of 'TTTG' and insertion of 'TGTG'
  *
  * @author pcingola
  */
 public class VcfFileIterator extends MarkerFileIterator<VcfEntry> implements Parser<VcfEntry> {
 
 	public static final String MISSING = "."; // Missing value
 	private static final String EMPTY = "";
 
 	boolean parseNow = true;
 	StringBuffer header = new StringBuffer();
 	HashMap<String, VcfInfo> vcfInfoById;
 
 	public VcfFileIterator(BufferedReader reader) {
 		super(reader, 1);
 	}
 
 	public VcfFileIterator(Genome genome) {
 		super((String) null, 1);
 		this.genome = genome;
 	}
 
 	public VcfFileIterator(String fileName) {
 		super(fileName, 1);
 	}
 
 	public VcfFileIterator(String fileName, Genome genome) {
 		super(fileName, 1);
 		this.genome = genome;
 	}
 
 	/**
 	 * Add a 'FORMAT' meta info
 	 * @param vcfGenotypeStr
 	 */
 	public void addFormat(String formatName, String number, String type, String description) {
 		String headerFormatInfo = "##FORMAT=<ID=" + formatName + ",Number=" + number + ",Type=" + type + ",Description=\"" + description + "\">";
 		addHeader(headerFormatInfo);
 	}
 
 	/**
 	 * Get sample names
 	 * @return
 	 */
 	public void addHeader(String newHeaderLine) {
 		// Split header
 		String headerLines[] = header.toString().split("\n");
 		header = new StringBuffer();
 
 		// Find "#CHROM" line in header (should always be the last one)
 		boolean added = false;
 		for (String line : headerLines) {
 			if (line.equals(newHeaderLine)) {
 				newHeaderLine = null; // Line already present? => Don't add
 				added = true;
 			} else if (line.startsWith("#CHROM") && (newHeaderLine != null)) {
 				header.append(newHeaderLine + "\n"); // Add new header right before title line
 				added = true;
 			}
 
 			if (!line.isEmpty()) header.append(line + "\n"); // Add non-empty lines
 		}
 
 		// Not added yet? => Add to the end
 		if (!added) header.append(newHeaderLine + "\n"); // Add new header right before title line
 	}
 
 	/**
	 * Get header information
 	 * @return
 	 */
 	public String getHeader() {
 		// Delete last character, if it's a '\n'
 		while (header.charAt(header.length() - 1) == '\n')
 			header.deleteCharAt(header.length() - 1);
 
 		return header.toString();
 	}
 
 	/**
 	 * Get sample names
 	 * @return
 	 */
 	public List<String> getSampleNames() {
 		// Split header
 		String headerLines[] = header.toString().split("\n");
 
 		// Find "#CHROM" line in header
 		for (String line : headerLines) {
 			if (line.startsWith("#CHROM")) {
 				// This line contains all the sample names (starting on column 9)
 				String titles[] = line.split("\t");
 
 				// Create a list of names
 				ArrayList<String> sampleNames = new ArrayList<String>();
 				for (int i = 9; i < titles.length; i++)
 					sampleNames.add(titles[i]);
 
 				// Done
 				return sampleNames;
 			}
 		}
 
 		// Not found
 		return null;
 	}
 
 	/**
 	 * Get Info type for a given ID
 	 * @param id
 	 * @return
 	 */
 	public VcfInfo getVcfInfo(String id) {
 		parseInfoLines();
 		return vcfInfoById.get(id);
 	}
 
 	@Override
 	public Collection<VcfEntry> parse(String str) {
 		LinkedList<VcfEntry> list = new LinkedList<VcfEntry>();
 		list.add(parseVcfLine(str));
 		return list;
 	}
 
 	/**
 	 * Parse INFO fields from header
 	 */
 	public void parseInfoLines() {
 		if (vcfInfoById == null) {
 			vcfInfoById = new HashMap<String, VcfInfo>();
 
 			// Add standard fields
 			vcfInfoById.put("CHROM", new VcfInfo("CHROM", VcfInfoType.STRING, "1", "Chromosome name"));
 			vcfInfoById.put("POS", new VcfInfo("POS", VcfInfoType.INTEGER, "1", "Position in chromosome"));
 			vcfInfoById.put("ID", new VcfInfo("ID", VcfInfoType.STRING, "1", "Variant ID"));
 			vcfInfoById.put("REF", new VcfInfo("REF", VcfInfoType.STRING, "1", "Reference sequence"));
 			vcfInfoById.put("ALT", new VcfInfo("ALT", VcfInfoType.STRING, "A", "Alternative sequence/s"));
 			vcfInfoById.put("QUAL", new VcfInfo("QUAL", VcfInfoType.FLOAT, "1", "Mapping quality"));
 			vcfInfoById.put("FILTER", new VcfInfo("FILTER", VcfInfoType.STRING, "1", "Filter status"));
 			vcfInfoById.put("FORMAT", new VcfInfo("FORMAT", VcfInfoType.STRING, "1", "Format in genotype fields"));
 
 			// Add well known fields 
 			// Reference: http://www.1000genomes.org/wiki/Analysis/Variant%20Call%20Format/vcf-variant-call-format-version-41
 			vcfInfoById.put("AA", new VcfInfo("AA", VcfInfoType.STRING, "1", "Ancestral allele"));
 			vcfInfoById.put("AC", new VcfInfo("AC", VcfInfoType.INTEGER, "A", "Allele Frequency"));
 			vcfInfoById.put("AF", new VcfInfo("AF", VcfInfoType.FLOAT, "1", "Allele Frequency"));
 			vcfInfoById.put("AN", new VcfInfo("AN", VcfInfoType.INTEGER, "1", "Total number of alleles"));
 			vcfInfoById.put("BQ", new VcfInfo("BQ", VcfInfoType.FLOAT, "1", "RMS base quality"));
 			vcfInfoById.put("CIGAR", new VcfInfo("CIGAR", VcfInfoType.STRING, "1", "Cigar string describing how to align an alternate allele to the reference allele"));
 			vcfInfoById.put("DB", new VcfInfo("DB", VcfInfoType.FLAG, "1", "dbSNP membership"));
 			vcfInfoById.put("DP", new VcfInfo("DP", VcfInfoType.INTEGER, "1", "Combined depth across samples"));
 			vcfInfoById.put("END", new VcfInfo("END", VcfInfoType.STRING, "1", "End position of the variant described in this record"));
 			vcfInfoById.put("H2", new VcfInfo("H2", VcfInfoType.FLAG, "1", "Membership in hapmap 2"));
 			vcfInfoById.put("H3", new VcfInfo("H3", VcfInfoType.FLAG, "1", "Membership in hapmap 3"));
 			vcfInfoById.put("MQ", new VcfInfo("MQ", VcfInfoType.FLOAT, "1", "RMS mapping quality"));
 			vcfInfoById.put("MQ0", new VcfInfo("MQ0", VcfInfoType.INTEGER, "1", "Number of MAPQ == 0 reads covering this record"));
 			vcfInfoById.put("NS", new VcfInfo("NS", VcfInfoType.INTEGER, "1", "Number of samples with data"));
 			vcfInfoById.put("SB", new VcfInfo("SB", VcfInfoType.FLOAT, "1", "Strand bias at this position"));
 			vcfInfoById.put("SOMATIC", new VcfInfo("SOMATIC", VcfInfoType.FLAG, "1", "Indicates that the record is a somatic mutation, for cancer genomics"));
 			vcfInfoById.put("VALIDATED", new VcfInfo("VALIDATED", VcfInfoType.FLAG, "1", "Validated by follow-up experiment"));
 			vcfInfoById.put("1000G", new VcfInfo("1000G", VcfInfoType.FLAG, "1", "Membership in 1000 Genomes"));
 
 			// Structural variants
 			vcfInfoById.put("IMPRECISE", new VcfInfo("IMPRECISE", VcfInfoType.FLAG, "0", "Imprecise structural variation"));
 			vcfInfoById.put("NOVEL", new VcfInfo("NOVEL", VcfInfoType.FLAG, "0", "Indicates a novel structural variation"));
 			vcfInfoById.put("END", new VcfInfo("END", VcfInfoType.INTEGER, "1", "End position of the variant described in this record"));
 			vcfInfoById.put("SVTYPE", new VcfInfo("SVTYPE", VcfInfoType.STRING, "1", "Type of structural variant"));
 			vcfInfoById.put("SVLEN", new VcfInfo("SVLEN", VcfInfoType.INTEGER, ".", "Difference in length between REF and ALT alleles"));
 			vcfInfoById.put("CIPOS", new VcfInfo("CIPOS", VcfInfoType.INTEGER, "2", "Confidence interval around POS for imprecise variants"));
 			vcfInfoById.put("CIEND", new VcfInfo("CIEND", VcfInfoType.INTEGER, "2", "Confidence interval around END for imprecise variants"));
 			vcfInfoById.put("HOMLEN", new VcfInfo("HOMLEN", VcfInfoType.INTEGER, ".", "Length of base pair identical micro-homology at event breakpoints"));
 			vcfInfoById.put("HOMSEQ", new VcfInfo("HOMSEQ", VcfInfoType.STRING, ".", "Sequence of base pair identical micro-homology at event breakpoints"));
 			vcfInfoById.put("BKPTID", new VcfInfo("BKPTID", VcfInfoType.STRING, ".", "ID of the assembled alternate allele in the assembly file"));
 			vcfInfoById.put("MEINFO", new VcfInfo("MEINFO", VcfInfoType.STRING, "4", "Mobile element info of the form NAME,START,END,POLARITY"));
 			vcfInfoById.put("METRANS", new VcfInfo("METRANS", VcfInfoType.STRING, "4", "Mobile element transduction info of the form CHR,START,END,POLARITY"));
 			vcfInfoById.put("DGVID", new VcfInfo("DGVID", VcfInfoType.STRING, "1", "ID of this element in Database of Genomic Variation"));
 			vcfInfoById.put("DBVARID", new VcfInfo("DBVARID", VcfInfoType.STRING, "1", "ID of this element in DBVAR"));
 			vcfInfoById.put("DBRIPID", new VcfInfo("DBRIPID", VcfInfoType.STRING, "1", "ID of this element in DBRIP"));
 			vcfInfoById.put("MATEID", new VcfInfo("MATEID", VcfInfoType.STRING, ".", "ID of mate breakends"));
 			vcfInfoById.put("PARID", new VcfInfo("PARID", VcfInfoType.STRING, "1", "ID of partner breakend"));
 			vcfInfoById.put("EVENT", new VcfInfo("EVENT", VcfInfoType.STRING, "1", "ID of event associated to breakend"));
 			vcfInfoById.put("CILEN", new VcfInfo("CILEN", VcfInfoType.INTEGER, "2", "Confidence interval around the length of the inserted material between breakends"));
 			vcfInfoById.put("DP", new VcfInfo("DP", VcfInfoType.INTEGER, "1", "Read Depth of segment containing breakend"));
 			vcfInfoById.put("DPADJ", new VcfInfo("DPADJ", VcfInfoType.INTEGER, ".", "Read Depth of adjacency"));
 			vcfInfoById.put("CN", new VcfInfo("CN", VcfInfoType.INTEGER, "1", "Copy number of segment containing breakend"));
 			vcfInfoById.put("CNADJ", new VcfInfo("CNADJ", VcfInfoType.INTEGER, ".", "Copy number of adjacency"));
 			vcfInfoById.put("CICN", new VcfInfo("CICN", VcfInfoType.INTEGER, "2", "Confidence interval around copy number for the segment"));
 			vcfInfoById.put("CICNADJ", new VcfInfo("CICNADJ", VcfInfoType.INTEGER, ".", "Confidence interval around copy number for the adjacency"));
 
 			// Add SnpEff fields
 			vcfInfoById.put("EFF.EFFECT", new VcfInfo("EFF.EFFECT", VcfInfoType.STRING, ".", "SnpEff effect"));
 			vcfInfoById.put("EFF.IMPACT", new VcfInfo("EFF.IMPACT", VcfInfoType.STRING, ".", "SnpEff impact (HIGH, MODERATE, LOW, MODIFIER)"));
 			vcfInfoById.put("EFF.FUNCLASS", new VcfInfo("EFF.FUNCLASS", VcfInfoType.STRING, ".", "SnpEff functional class (NONE, SILENT, MISSENSE, NONSENSE)"));
 			vcfInfoById.put("EFF.CODON", new VcfInfo("EFF.CODON", VcfInfoType.STRING, ".", "SnpEff codon change"));
 			vcfInfoById.put("EFF.AA", new VcfInfo("EFF.AA", VcfInfoType.STRING, ".", "SnpEff amino acid change"));
 			vcfInfoById.put("EFF.GENE", new VcfInfo("EFF.GENE", VcfInfoType.STRING, ".", "SnpEff gene name"));
 			vcfInfoById.put("EFF.BIOTYPE", new VcfInfo("EFF.BIOTYPE", VcfInfoType.STRING, ".", "SnpEff gene bio-type"));
 			vcfInfoById.put("EFF.CODING", new VcfInfo("EFF.CODING", VcfInfoType.STRING, ".", "SnpEff gene coding (CODING, NON_CODING)"));
 			vcfInfoById.put("EFF.TRID", new VcfInfo("EFF.TRID", VcfInfoType.STRING, ".", "SnpEff transcript ID"));
 			vcfInfoById.put("EFF.EXID", new VcfInfo("EFF.EXID", VcfInfoType.STRING, ".", "SnpEff exon ID"));
 
 			// Add all INFO fields from header
 			String headerLines[] = header.toString().split("\n");
 			for (String line : headerLines) {
 				if (line.startsWith("##INFO=") || line.startsWith("##FORMAT=")) {
 					VcfInfo vcfInfo = new VcfInfo(line);
 					vcfInfoById.put(vcfInfo.getId(), vcfInfo);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Parse a line from a VCF file
 	 * @param line
 	 * @return
 	 */
 	protected VcfEntry parseVcfLine(String line) {
 		try {
 			if (line.startsWith("#")) header.append(line + "\n"); // Header?
 			else if ((line.length() > 0) && (!line.startsWith("#"))) return new VcfEntry(this, line, lineNum, parseNow); // Vcf entry?
 		} catch (Throwable t) {
 			Gpr.debug("Fatal error reading file '" + fileName + "' (line: " + lineNum + "):\n" + line);
 			throw new RuntimeException(t);
 		}
 		// Could not create a VcfEntry from this line (e.g. header line)
 		return null;
 	}
 
 	/**
 	 * Read a field an return a value
 	 * @param field
 	 * @param fieldNum
 	 * @return
 	 */
 	public String readField(String fields[], int fieldNum) {
 		if (fields.length > fieldNum) {
 			if (fields[fieldNum].equals(MISSING)) return EMPTY;
 			return fields[fieldNum];
 		}
 		return EMPTY;
 	}
 
 	/**
 	 * Read only header info
 	 * @return
 	 */
 	public String readHeader() {
 		// No more header to read?
 		if ((nextLine != null) && !nextLine.startsWith("#")) return getHeader();
 
 		try {
 			while (ready()) {
 				line = readLine();
 				if (line == null) return null; // End of file?
 				if (!line.startsWith("#")) {
 					nextLine = line;
 					return getHeader(); // End of header?
 				}
 
 				header.append((header.length() > 0 ? "\n" : "") + line);
 			}
 		} catch (IOException e) {
 			throw new RuntimeException("Error reading file '" + fileName + "'. Line ignored:\n\tLine (" + lineNum + "):\t'" + line + "'");
 		}
 
 		return getHeader();
 	}
 
 	@Override
 	protected VcfEntry readNext() {
 		// Read another line from the file
 		try {
 			while (ready()) {
 				line = readLine();
 				if (line == null) return null; // End of file?
 
 				VcfEntry vcfEntry = parseVcfLine(line);
 				if (vcfEntry != null) return vcfEntry;
 			}
 		} catch (IOException e) {
 			throw new RuntimeException("Error reading file '" + fileName + "'. Line ignored:\n\tLine (" + lineNum + "):\t'" + line + "'");
 		}
 		return null;
 	}
 
 	@Override
 	public void setCreateChromos(boolean createChromos) {
 		this.createChromos = createChromos;
 	}
 
 	public void setParseNow(boolean parseNow) {
 		this.parseNow = parseNow;
 	}
 
 }
