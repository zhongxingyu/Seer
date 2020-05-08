 package net.derkholm.nmica.extra.app.seq.nextgen;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedSet;
 import java.util.StringTokenizer;
 import java.util.TreeSet;
 import java.util.regex.Pattern;
 
 import net.derkholm.nmica.build.NMExtraApp;
 import net.derkholm.nmica.build.VirtualMachine;
 import net.derkholm.nmica.extra.app.seq.RetrieveEnsemblSequences;
 import net.derkholm.nmica.extra.app.seq.RetrieveSequenceFeaturesFromEnsembl;
 import net.derkholm.nmica.extra.app.seq.SequenceSplitter;
 import net.derkholm.nmica.extra.peak.PeakEntryAscComparitor;
 import net.derkholm.nmica.extra.peak.PeakEntryDescComparitor;
 import net.derkholm.nmica.extra.peak.PeakEntryRandomComparitor;
 
 import org.biojava.bio.Annotation;
 import org.biojava.bio.BioError;
 import org.biojava.bio.program.gff.GFFWriter;
 import org.biojava.bio.program.gff.SimpleGFFRecord;
 import org.biojava.bio.seq.DNATools;
 import org.biojava.bio.seq.Feature;
 import org.biojava.bio.seq.FeatureFilter;
 import org.biojava.bio.seq.FeatureHolder;
 import org.biojava.bio.seq.Sequence;
 import org.biojava.bio.seq.StrandedFeature;
 import org.biojava.bio.seq.db.HashSequenceDB;
 import org.biojava.bio.seq.impl.SimpleSequence;
 import org.biojava.bio.symbol.Location;
 import org.biojava.bio.symbol.LocationTools;
 import org.biojava.bio.symbol.RangeLocation;
 import org.biojava.bio.symbol.Symbol;
 import org.biojava.bio.symbol.SymbolList;
 import org.biojavax.SimpleRichAnnotation;
 import org.biojavax.bio.seq.RichSequence;
 import org.biojavax.bio.seq.RichSequenceIterator;
 import org.bjv2.util.cli.App;
 import org.bjv2.util.cli.Option;
 import org.bjv2.util.cli.UserLevel;
 
 
 @App(overview = "Get sequences from Ensembl that using peaks in the FindPeaks format", generateStub = true)
 @NMExtraApp(launchName = "nmensemblpeakseq", vm = VirtualMachine.SERVER)
 public class RetrievePeakSequencesFromEnsembl extends RetrieveEnsemblSequences {
 	public static enum RankOrder {
     	ASC,
     	DESC,
     	NONE
     }
 	
 	public static enum RankedProperty {
 		P_VALUE,
 		FDR,
 		TAG_COUNT,
 	}
 	
 	public static enum PeakFormat {
 		BED,
 		MACS,
 		SWEMBL,
 		FINDPEAKS,
 		PEAKS
 	}
 	
 	public static enum PeakOutputFormat {
 		FASTA,
 		GFF
 	}
 	
 	private PeakFormat inputFormat = PeakFormat.BED;
 	private PeakOutputFormat outputFormat = PeakOutputFormat.FASTA;
 	
 	private File peaksFile;
 	private File outFile;
 	private int maxLength = Integer.MAX_VALUE;
 	private int minLength = 20;
 	private RankOrder rankOrder = RankOrder.DESC;
 	private int maxCount = 0;
 	private int aroundPeak;
 	private int chunkLength;
 
 	private File seqDBFile;
 
 	private int nearbyGenes;
 
 	//private int nearbyGeneWindowSize = 1000000;
 
 	private int minNonN = 10;
 
 	private RankedProperty rankedProperty;
 
 	private boolean excludeUnlabelled = false;
 
 	private int maxDistFromGene;
 	private boolean groupBySeq;
 	
 	@Option(help="Peaks")
 	public void setPeaks(File f) {
 		peaksFile = f;
 	}
 	
 	@Option(help="Input format")
 	public void setInputFormat(PeakFormat f) {
 		this.inputFormat = f;
 	}
 
 	@Option(help="Output format (default=fasta)", optional=true)
 	public void setOutputFormat(PeakOutputFormat outputFormat) {
 		this.outputFormat = outputFormat;
 	}
 	
 	@Option(
 		help="Sequence database " +
 				"(you can read the peaks from local disk instead of Ensembl)",
 				optional=true)
 	public void setSeqDB(File f) {
 		this.seqDBFile = f;
 	}
 	
 	@Option(help="Output file",optional=true)
 	public void setOut(File f) {
 		this.outFile = f;
 	}
 	
 	
 	@Option(help="The maximum count of peaks to output",optional=true)
 	public void setMaxCount(int maxCount) {
 		this.maxCount = maxCount;
 	}
 	
 	@Option(help="Ranking order: asc|desc|none (default = desc)",optional=true)
 	public void setRankOrder(RankOrder order) {
 		this.rankOrder = order;
 	}
 
 	@Option(help="Ranked property: p_value|fdr|tag_count",optional=true)
 	public void setRankedProperty(RankedProperty prop) {
 		this.rankedProperty = prop;
 	}
 	
 	@Option(help="Maximum peak length (not defined by default)",optional=true)
 	public void setMaxLength(int maxLength) {
 		this.maxLength = maxLength;
 	}
 	
 	@Option(help="Minimum peak length (default=20)",optional=true)
 	public void setMinLength(int minLength) {
 		this.minLength = minLength;
 	}
 	
 	@Option(help="Region length around peak maximum",optional=true)
 	public void setAroundPeak(int aroundPeak) {
 		this.aroundPeak = aroundPeak;
 	}
 	
 	@Option(help="Cut sequences to chunks of the specified size", optional=true)
 	public void setChunkLength(int chunkLength) {
 		this.chunkLength = chunkLength;
 	}
 	
 	@Option(help="Retrieve nearby genes (specified number of them)", optional=true)
 	public void setNearbyGenes(int n) {
 		this.nearbyGenes = n;
 	}
 	
 	/*
 	@Option(help="Maximum window size to look for nearby genes (default = 1 megabase)", optional=true)
 	public void setNearbyGeneWindowSize(int ws) {
 		this.nearbyGeneWindowSize = ws;
 	}*/
 	
 	@Option(help="Minimun number of unambiguous symbols in the output sequences (N)", optional=true)
 	public void setMinNonN(int i) {
 		this.minNonN = i;
 	}
 	
 	@Option(help="Label features with the closest gene (given the specified maximum distance)", optional=true)
 	public void setMaxDistanceFromGene(int i) {
 		this.maxDistFromGene = i;
 	}
 	
 	@Option(help="Exclude features that do fall within the specified maximum distance from a gene (done by default, applies only when -maxDistanceFromGene was given)", optional=true, userLevel=UserLevel.EXPERT)
 	public void setExcludeUnlabelled(boolean b) {
 		this.excludeUnlabelled  = b;
 	}
 	
 	@Option(help="Group by sequence name", optional=true)
 	public void setGroupBySeq(boolean b) {
 		this.groupBySeq = b;
 	}
 	
 	
 	private static int gapSymbolCount(SymbolList seq) {
 		int numNs = 0;
 		for (Iterator<?> i = seq.iterator(); i.hasNext(); ) {
             Symbol s = (Symbol) i.next();
             if (s == DNATools.n() || s == seq.getAlphabet().getGapSymbol()) {
                 ++numNs;
             }
         }
 		return numNs;
 	}
 
 	public static class PeakEntry {
 		public final String id;
 		public String seqName;
 		public final int startCoord;
 		public final int endCoord;
 		public final int peakCoord;
 		public final double score;
 		public final double fdr;
 		public final double tagCount;
 		
 		public PeakEntry(
 				String id2, 
 				String seqName,
 				int startCoord, 
 				int endCoord, 
 				int peakCoord, 
 				double value, 
 				double fdr,
 				double tagCount) {
 			this.id = id2;
 			this.seqName = seqName;
 			this.startCoord = startCoord;
 			this.endCoord = endCoord;
 			this.peakCoord = peakCoord;
 			this.score = value;
 			this.fdr = fdr;
 			this.tagCount = tagCount;
 		}
 
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result + ((id == null) ? 0 : id.hashCode());
 			return result;
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (getClass() != obj.getClass())
 				return false;
 			final PeakEntry other = (PeakEntry) obj;
 			if (id == null) {
 				if (other.id != null)
 					return false;
 			} else if (!id.equals(other.id))
 				return false;
 			return true;
 		}
 
 		
 	}
 	
 	public static abstract class PeakEntryComparator implements Comparator<PeakEntry> {
 		
 	}
 	
 	public void main(String[] argv) throws Exception {
 		
 		PrintStream os = null;
 		if (this.outFile == null) {
 			os = System.out;
 		} else {
 			os = new PrintStream(new FileOutputStream(this.outFile, true));
 		}
 		
 		GFFWriter gffWriter = null;
 		if (this.outputFormat.equals(PeakOutputFormat.GFF)) {
 			gffWriter = new GFFWriter(new PrintWriter(os));
 		}
 		
 		BufferedReader br = new BufferedReader(new FileReader(peaksFile));
 
 		SortedSet<PeakEntry> peaks = RetrievePeakSequencesFromEnsembl.parsePeaks(
 				br,
 				this.inputFormat, 
 				rankOrder, 
 				this.groupBySeq,
 				aroundPeak, 
 				this.minLength, 
 				this.maxLength);
 		
 		/*
 		for (PeakEntry e : peaks) {
 			if (e.startCoord > e.endCoord) {
 				System.err.printf("Start %d larger than end %d (id:%s)%n", e.startCoord, e.endCoord,e.id);
 				System.exit(1);
 			}
 		}*/
 		
 		if (seqDBFile == null) {
 			initializeEnsemblConnection();
 		} else {
 			seqDB = new HashSequenceDB();
 			RichSequenceIterator seqIter = RichSequence.IOTools.readFastaDNA(new BufferedReader(new FileReader(seqDBFile)), null);
 			while (seqIter.hasNext()) {
 				seqDB.addSequence(seqIter.nextSequence());
 			}
 		}
 		
 		int maskedSeqLength = 0;
 		int totalLength = 0;
 		int i = 0;
 		for (PeakEntry peak : peaks) {
 			totalLength += peak.endCoord - peak.startCoord;
 			Sequence chromoSeq = seqDB.getSequence(peak.seqName);
 			
 			if (chromoSeq == null) {
 				System.err.println("Could not retrieve seq with name " + peak.seqName);
 				System.exit(1);
 			}
 			
 			if (chromoSeq.length() < peak.startCoord) {
 				System.err.printf(
 					"%s : %d - %d (%d) (start > seq.length)%n",
 					peak.seqName,
 					peak.startCoord,
 					peak.endCoord,
 					chromoSeq.length());
 				continue;
 			}
 			
 			if (chromoSeq.length() < peak.endCoord) {
 				System.err.printf(
 					"%s : %d - %d (%d) (end > seq.length) %n",
 					peak.seqName,
 					peak.startCoord,
 					peak.endCoord,
 					chromoSeq.length());
 				continue;
 			}
 			
 			Location mask = Location.empty;
 			
 			Location loc = new RangeLocation(peak.startCoord,peak.endCoord);
 			
 
 			StrandedFeature nearestTranscript = null;
 			if (maxDistFromGene > 0) {
 									
 				nearestTranscript = RetrieveSequenceFeaturesFromEnsembl
 											.transcriptWithClosestTSS(
 												peak.seqName,
 												peak.startCoord,
 												peak.endCoord,
 												StrandedFeature.UNKNOWN,
 												seqDB,
 												maxDistFromGene,
 												ignoreGenesWithNoCrossReferences);
 				
 				if (nearestTranscript == null && this.excludeUnlabelled) {
 					System.err.println("Excluding unlabelled peak");
 					continue;
 				}
 			}
 			
 			if (this.nearbyGenes > 0) {
 				
 				FeatureHolder genes = 
 					chromoSeq.filter(
 							new FeatureFilter.OverlapsLocation(
 								new RangeLocation(
 									peak.startCoord - maxDistFromGene, 
 									peak.endCoord + maxDistFromGene)));
 					
 				Iterator geneIterator = genes.features();
 				while (geneIterator.hasNext()) {
 					Feature gf = (Feature) geneIterator.next();
 					RangeLocation gl = (RangeLocation) gf.getLocation();
 					
 					for (Object o : gf.getAnnotation().asMap().keySet()) {
 						System.err.printf("%s : %s", o, gf.getAnnotation().getProperty(o));						
 					}
 				}
 				
 				continue;
 			}
 			
 			
 			if (this.repeatMask) {
 				FeatureHolder repeats = chromoSeq.filter(new FeatureFilter.And(
 						new FeatureFilter.Or(
 								new FeatureFilter.ByType("repeat"),
 								new FeatureFilter.ByType("Repeat")),
 						new FeatureFilter.OverlapsLocation(loc)));
 				List<Location> repLocs = new ArrayList<Location>();
 				for (Iterator<?> it = repeats.features(); it.hasNext();) {
 					Feature repFeat = (Feature) it.next();
 					RangeLocation repLoc = (RangeLocation)repFeat.getLocation();
 					int len = repLoc.getMax() - repLoc.getMin();
 					System.err.println("Masking repeat of length " + len);
 					maskedSeqLength += len;
 					repLocs.add(repLoc);
 				}
 				
 				System.err.println("Will mask " + repLocs.size() + " repeats");
 				mask = LocationTools.union(repLocs);
 			}
 			
 			loc = LocationTools.subtract(loc, mask);
 			if (excludeTranslations) {
 				FeatureHolder translations = chromoSeq.filter(new FeatureFilter.And(
 						new FeatureFilter.ByType("translation"),
 						new FeatureFilter.OverlapsLocation(loc)));
 				
 				List<Location> transLocs = new ArrayList<Location>();
 				for (Iterator<?> it = translations.features(); it.hasNext();) {
 					Feature transFeat = (Feature) it.next();
 					Location transLoc = (Location)transFeat.getLocation();
 					for (Iterator<?> bi = transLoc.blockIterator(); bi.hasNext();) {
 						Location tl = (Location)bi.next();
 						int len = tl.getMax() - tl.getMin();
 						System.err.println(
 							"Masking translated sequence of length " + len + " from peak with ID " + peak.id);
 						maskedSeqLength += len;
 						transLocs.add(tl);
 					}
 					
 				}
 				Location transMask = feather(LocationTools.union(transLocs),this.featherTranslationsBy);
 				if (transLocs.size() == 0) {
 					System.err.println("No translated segments near peak with ID " + peak.id);
 				}
 				loc = LocationTools.subtract(loc, transMask);
 			}
 			
 			boolean anyWereOutput = false;
 			
 			int frag = 0;
 			for (Iterator<?> bi = loc.blockIterator(); bi.hasNext();) {
 				Location bloc = (Location) bi.next();
 				
 				if ((bloc.getMax() - bloc.getMin()) < this.minLength) continue; //too short, filter out
 				
 				SymbolList symList = chromoSeq.subList(bloc.getMin(), bloc.getMax());
 				
 				if (minNonN > 0 && 
 						RetrieveSequenceFeaturesFromEnsembl
 							.gapSymbolCount(symList) > minNonN) {
 						continue;
 					}
 				
 				
 				Annotation annotation = new SimpleRichAnnotation();
 				annotation.setProperty("fdr", peak.fdr);
 				annotation.setProperty("score", peak.score);
 				annotation.setProperty("tag_count", peak.tagCount);
 				
				
				
				
 				Sequence seq = null;
 				
				if (maxDistFromGene > 0 && this.outputFormat.equals(PeakOutputFormat.GFF)) {
 					if (nearestTranscript == null) {
 						seq = 
 							new SimpleSequence(symList, null, 
 									String.format("%s_%d-%d;%f;%d;fdr=%f;score=%f;tag_count=%f;closest_gene=%s;", 
 										peak.seqName,
 										bloc.getMin(),
 										bloc.getMax(),
 										peak.score,
 										++frag,
 										peak.fdr,
 										peak.score,
 										peak.tagCount,
 										"none"),
 										annotation);						
 					} else {
 						seq = 
 							new SimpleSequence(symList, null, 
 									String.format("%s_%d-%d;%f;%d;fdr=%f;score=%f;tag_count=%f;closest_gene=%s;display_label=%s;", 
 										peak.seqName,
 										bloc.getMin(),
 										bloc.getMax(),
 										peak.score,
 										++frag,
 										peak.fdr,
 										peak.score,
 										peak.tagCount,
 										nearestTranscript.getAnnotation().getProperty("ensembl.gene_id"),
 										nearestTranscript.getAnnotation().containsProperty("ensembl.gene_display_label") ? 
 												nearestTranscript.getAnnotation().getProperty("ensembl.gene_display_label") :
 													"none"),
 										annotation);
 					}
 					
 				} else {
 					seq = 
 						new SimpleSequence(symList, null, 
 								String.format("%s_%d-%d;%f;%d;fdr=%f;score=%f;tag_count=%f;", 
 									peak.seqName,
 									bloc.getMin(),
 									bloc.getMax(),
 									peak.score,
 									++frag,
 									peak.fdr,
 									peak.score,
 									peak.tagCount),
 									annotation);
 				}
 				
 				if (chunkLength > 0) {
 					List<Sequence> outSeqs = 
 						SequenceSplitter.splitSequences(
 							new RichSequence.IOTools.SingleRichSeqIterator(seq),this.minLength, this.chunkLength);
 					for (Sequence s : outSeqs) {
 						
 						if (this.outputFormat.equals(PeakOutputFormat.FASTA)) {
 							RichSequence.IOTools.writeFasta(System.out, s, null);							
 						} else {
 							System.err.println("GFF output format not supported when -chunkLength is specified");
 							System.exit(1);
 						}
 						
 					}
 				} else {
 					
 					if (this.outputFormat.equals(PeakOutputFormat.FASTA)) {
 						RichSequence.IOTools.writeFasta(System.out, seq, null);
 					} else {
						assert seq == null;
 						
 						SimpleGFFRecord rec = new SimpleGFFRecord();
 						rec.setSource("nmensemblpeakseq");
 						rec.setSeqName(peak.seqName);
 						if (nearestTranscript != null) {
 							rec.setFeature(nearestTranscript.getAnnotation().getProperty("ensembl.gene_id").toString());
 						} else {
 							
 						}
 						rec.setStart(bloc.getMin());
 						rec.setEnd(bloc.getMax());
						rec.setScore(Double.NaN);
 
 						//GFFWriter wants its annotations in a specific sort of map
 						Map<String,List<String>> annotationMap = new HashMap<String,List<String>>();
 						for (Object o : annotation.asMap().keySet()) {
 							List<String> items = new ArrayList<String>();
 							items.add(annotation.getProperty(o).toString());
 							annotationMap.put(o.toString(), new ArrayList<String>(items));
 						}
 						
 						rec.setGroupAttributes(annotationMap);
 
 						gffWriter.recordLine(rec);
 						gffWriter.endDocument(); //force flush
 					}
 				}
 				anyWereOutput = true;
 			}
 			
 			/* incremented for each *peak* that had at least one sequence, 
 			 * not for each output sequence 
 			 * (might be cut because of repeats/translations) */
 			if (anyWereOutput) i++;
 			
 			if ((maxCount > 0) && (i >= maxCount)) {
 				break;
 			}
 		}
 		
 		System.err.printf("Masked %d nucleotides out of %d total (%.4f)%n",
 				maskedSeqLength, 
 				totalLength, 100.0 * (double)maskedSeqLength / (double)totalLength);
 	}
 
 	public static SortedSet<PeakEntry> parsePeaks(
 			BufferedReader br,
 			PeakFormat inputFormat,
 			RankOrder rankOrder,
 			boolean groupBySeq,
 			int aroundPeak,
 			int minLength,
 			int maxLength) throws IOException {
 		
 		PeakEntryComparator comp = null;
 		int peakCount = 0;
 		if (rankOrder == RankOrder.ASC) {
 			comp = new PeakEntryAscComparitor(groupBySeq,inputFormat);
 		} else if (rankOrder == RankOrder.DESC) {
 			comp = new PeakEntryDescComparitor(groupBySeq,inputFormat);
 		} else {
 			comp = new PeakEntryRandomComparitor();
 		}
 		
 		SortedSet<PeakEntry> peaks = new TreeSet<PeakEntry>(comp);
 		String line = null;
 		//br.readLine();//ignore the header
 		
 		int peakId = 1;
 		while ((line = br.readLine()) != null) {
 			//ignore comment lines regardless of exact format
 			if (Pattern.compile("^#").matcher(line).find()) {
 				continue;
 			}
 			
 			//the MACS header
 			if (inputFormat == PeakFormat.MACS && Pattern.compile("chr\\s+start\\s+end").matcher(line).find()) {
 				continue;
 			}
 			
 			//the SWEMBL header
 			if (inputFormat == PeakFormat.SWEMBL && Pattern.compile("Region\\s+Start").matcher(line).find()) {
 				continue;
 			}
 			
 			//the BED header
 			if (inputFormat == PeakFormat.BED && Pattern.compile("track name").matcher(line).find()) {
 				continue;
 			}
 			
 			String id; //not really used for anything (not present in the BED format)
 			String chromo;
 			int startCoord;
 			int endCoord;
 			int peakCoord;
 			double tagCount = Double.NaN;
 			double pValue = Double.NaN;
 			double fdr = Double.NaN;
 			
 			StringTokenizer tok = new StringTokenizer(line,"\t");
 			
 			if (inputFormat == PeakFormat.BED) {
 				chromo = tok.nextToken();
 				startCoord = Integer.parseInt(tok.nextToken());
 				endCoord = Integer.parseInt(tok.nextToken());
 				id = tok.nextToken();
 				peakCoord = -1;
 				pValue = Double.parseDouble(tok.nextToken());
 				
 			} else if (inputFormat == PeakFormat.MACS) {
 				id = "" + peakId++;
 				chromo = tok.nextToken();
 				startCoord = Integer.parseInt(tok.nextToken()) - 1; //1-based coords
 				endCoord = Integer.parseInt(tok.nextToken()) - 1; //1-based coords
 				tok.nextToken();//length
 				peakCoord  = startCoord + Integer.parseInt(tok.nextToken());//summit reported relative to start coord
 				tagCount = Double.parseDouble(tok.nextToken());
 				pValue = Math.pow(10.0, -Double.parseDouble(tok.nextToken()) / 10.0) ; //p-value
 				fdr = Double.parseDouble(tok.nextToken());
 				
 			} else if (inputFormat == PeakFormat.SWEMBL) {
 				chromo = tok.nextToken();
 				id = "" + peakId++;
 				startCoord = Integer.parseInt(tok.nextToken());
 				endCoord = Integer.parseInt(tok.nextToken());
 				tagCount = Double.parseDouble(tok.nextToken());//count
 				tok.nextToken();//length
 				tok.nextToken();//uniquePos
 				pValue = Double.parseDouble(tok.nextToken());//score
 				tok.nextToken();//Ref. count
 				tok.nextToken();//Max. coverage
 				peakCoord = (int)Math.round(Double.parseDouble(tok.nextToken()));
 			
 			}else if (inputFormat == PeakFormat.FINDPEAKS) {
 				id = tok.nextToken();
 				chromo = tok.nextToken();
 				startCoord = Integer.parseInt(tok.nextToken());
 				endCoord = Integer.parseInt(tok.nextToken());
 				peakCoord = (int)Math.round(Double.parseDouble(tok.nextToken()));
 				pValue = Double.parseDouble(tok.nextToken());
 			} else if (inputFormat == PeakFormat.PEAKS) {
 				/* <chromosome> 
 				 * <genomic start> 
 				 * <genomic end>
 				 *  <position of peak maximum relative to genomic start> 
 				 *  <peak height> 
 				 *  <number of reads in the peak>
 				 */
 				
 				id = "" + peakId++;
 				chromo = tok.nextToken();
 				startCoord = Integer.parseInt(tok.nextToken());
 				endCoord = Integer.parseInt(tok.nextToken());
 				peakCoord = startCoord + Integer.parseInt(tok.nextToken());
 				tagCount = Double.parseDouble(tok.nextToken());
 			} else {
 				throw 
 					new BioError(
 							String.format(
 								"Unexpected input format %s", inputFormat));
 			}
 
 			boolean maxLengthCondition = (Math.abs(startCoord - endCoord) < maxLength);
 			if ((maxLength > 0)&!maxLengthCondition) {
 				System.err.println("Maximum length condition not met");
 				continue;
 			}
 			boolean minLengthCondition = (Math.abs(startCoord - endCoord) > minLength) && (minLength > 0);
 			if ((minLength > 0)&!minLengthCondition) {
 				System.err.println("Minimum length condition not met");
 				continue;
 			}
 			if (aroundPeak > 0) {
 				int halfLength = (int) Math.round((double)aroundPeak / 2.0);
 				int peakStartCoord = Math.max(startCoord,peakCoord - halfLength);
 				int peakEndCoord = Math.min(endCoord,peakCoord + halfLength);
 				
 				if (peakStartCoord > peakEndCoord) {
 					System.err.printf("Peak start (%d) larger than end (%d) -- will skip%n", 
 							peakStartCoord, peakEndCoord);	
 					continue;
 				}
 				peakCount++;
 				System.err.printf(".");
 				peaks.add(
 					new PeakEntry(
 						id, 
 						chromo, 
 						peakStartCoord, 
 						peakEndCoord, 
 						peakCoord, 
 						pValue,
 						fdr,
 						tagCount));
 			} else {
 				peakCount++;
 				System.err.printf("-");
 				peaks.add(
 					new PeakEntry(
 						id, 
 						chromo, 
 						startCoord, 
 						endCoord, 
 						peakCoord, 
 						pValue, 
 						fdr,
 						tagCount));	
 			}
 			
 		}
 		System.err.println("peaks:"+peakCount);
 		return peaks;
 	}
 }
