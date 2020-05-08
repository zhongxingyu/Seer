 package net.derkholm.nmica.extra.app.seq.nextgen;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 import java.util.SortedSet;
 import java.util.StringTokenizer;
 import java.util.TreeSet;
 import java.util.regex.Pattern;
 
 import net.derkholm.nmica.build.NMExtraApp;
 import net.derkholm.nmica.build.VirtualMachine;
 import net.derkholm.nmica.extra.app.seq.RetrieveEnsemblSequences;
 import net.derkholm.nmica.extra.app.seq.RetrieveSequenceFeaturesFromEnsembl;
 import net.derkholm.nmica.extra.app.seq.SequenceSplitter;
 
 import org.biojava.bio.Annotation;
 import org.biojava.bio.BioError;
 import org.biojava.bio.seq.DNATools;
 import org.biojava.bio.seq.Feature;
 import org.biojava.bio.seq.FeatureFilter;
 import org.biojava.bio.seq.FeatureHolder;
 import org.biojava.bio.seq.Sequence;
 import org.biojava.bio.seq.db.HashSequenceDB;
 import org.biojava.bio.seq.impl.SimpleSequence;
 import org.biojava.bio.symbol.Location;
 import org.biojava.bio.symbol.LocationTools;
 import org.biojava.bio.symbol.RangeLocation;
 import org.biojava.bio.symbol.Symbol;
 import org.biojava.bio.symbol.SymbolList;
 import org.biojavax.bio.seq.RichSequence;
 import org.biojavax.bio.seq.RichSequenceIterator;
 import org.bjv2.util.cli.App;
 import org.bjv2.util.cli.Option;
 
 
 @App(overview = "Get sequences from Ensembl that using peaks in the FindPeaks format", generateStub = true)
 @NMExtraApp(launchName = "nmensemblpeakseq", vm = VirtualMachine.SERVER)
 public class RetrievePeakSequencesFromEnsembl extends RetrieveEnsemblSequences {
 	public static enum RankOrder {
     	ASC,
     	ASC_FOLD_CHANGE,
     	DESC,
     	DESC_FOLD_CHANGE,
     	NONE
     }
 	
 	public static enum PeakFormat {
 		BED,
 		MACS,
 		SWEMBL,
 		FINDPEAKS,
 		PEAKS
 	}
 	
 	private PeakFormat inputFormat = PeakFormat.BED;
 	
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
 
 	private int nearbyGeneWindowSize = 1000000;
 
 	private int minNonN;
 	
 	@Option(help="Peaks")
 	public void setPeaks(File f) {
 		peaksFile = f;
 	}
 	
 	@Option(help="Input format")
 	public void setInputFormat(PeakFormat f) {
 		this.inputFormat = f;
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
 	
 	@Option(help="Maximum window size to look for nearby genes (default = 1 megabase)", optional=true)
 	public void setNearbyGeneWindowSize(int ws) {
 		this.nearbyGeneWindowSize = ws;
 	}
 	
 	@Option(help="Minimun number of gap symbols (N)", optional=true)
 	public void setMinNonN(int i) {
 		this.minNonN = i;
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
 		public final double pValue;
 		public final double foldChange;
 		public final double fdr;
 		
 		public PeakEntry(
 				String id2, 
 				String seqName,
 				int startCoord, 
 				int endCoord, 
 				int peakCoord, 
 				double value, 
 				double foldChange, 
 				double fdr) {
 			this.id = id2;
 			this.seqName = seqName;
 			this.startCoord = startCoord;
 			this.endCoord = endCoord;
 			this.peakCoord = peakCoord;
 			this.pValue = value;
 			this.foldChange = foldChange;
 			this.fdr = fdr;
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
 	
 	public static class PeakEntryAscComparitor extends PeakEntryComparator implements Comparator<PeakEntry> {
 
 		public int compare(PeakEntry o1, PeakEntry o2) {
 			int pvalCompare = Double.compare(o1.pValue, o2.pValue);
 			if (pvalCompare != 0) return pvalCompare;
 			
 			int fdrCompare = -Double.compare(o1.fdr, o2.fdr);
 			if (fdrCompare != 0) return fdrCompare;
 			
 			return 0;
 		}
 	}
 	
 	public static class PeakEntryDescComparitor extends PeakEntryComparator implements Comparator<PeakEntry> {
 
 		public int compare(PeakEntry o1, PeakEntry o2) {
 			int pvalCompare = -Double.compare(o1.pValue, o2.pValue);
 			if (pvalCompare != 0) return pvalCompare;
 			
 			int fdrCompare = Double.compare(o1.fdr, o2.fdr);
 			if (fdrCompare != 0) return fdrCompare;
 			
 			int foldChangeCompare = Double.compare(o1.foldChange, o2.foldChange);
 			if (foldChangeCompare != 0) return foldChangeCompare;
 			
 			return o1.id.compareTo(o2.id);
 		}
 	}
 	
 	public static class PeakEntryRandomComparitor extends PeakEntryComparator implements Comparator<PeakEntry> {
 		public int compare(PeakEntry o1, PeakEntry o2) {
 			return 0;
 		}
 	}
 	
 	
 	public void main(String[] argv) throws Exception {
 		
 		PrintStream os = null;
 		if (this.outFile == null) {
 			os = System.out;
 		} else {
 			os = new PrintStream(new FileOutputStream(this.outFile, true));
 		}
 		
 		BufferedReader br = new BufferedReader(new FileReader(peaksFile));
 
 		SortedSet<PeakEntry> peaks = RetrievePeakSequencesFromEnsembl.parsePeaks(
 				br,
 				this.inputFormat, 
 				rankOrder, 
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
 			
 			
 			if (this.nearbyGenes > 0) {
 				
 				FeatureHolder genes = 
 					chromoSeq.filter(
 							new FeatureFilter.OverlapsLocation(
 								new RangeLocation(
 									peak.startCoord - nearbyGeneWindowSize, 
 									peak.endCoord + nearbyGeneWindowSize)));
 					
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
 				
 				Sequence seq = 
 					new SimpleSequence(symList, null, 
 						String.format("%s_%d-%d;%f;%d", 
 								peak.seqName,
 								bloc.getMin(),
 								bloc.getMax(),
 								peak.pValue,
 								++frag),
 						Annotation.EMPTY_ANNOTATION);
 				
 				if (chunkLength > 0) {
 					List<Sequence> outSeqs = 
 						SequenceSplitter.splitSequences(
							new RichSequence.IOTools.SingleRichSeqIterator(seq),this.minLength, this.chunkLength);
 					for (Sequence s : outSeqs) {
 						RichSequence.IOTools.writeFasta(System.out, s, null);
 					}
 				} else {
 					RichSequence.IOTools.writeFasta(System.out, seq, null);					
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
 			int aroundPeak,
 			int minLength,
 			int maxLength) throws IOException {
 		PeakEntryComparator comp = null;
 		int peakCount = 0;
 		if (rankOrder == RankOrder.ASC) {
 			comp = new PeakEntryAscComparitor();
 		} else if (rankOrder == RankOrder.DESC) {
 			comp = new PeakEntryDescComparitor();
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
 			double pValue;
 			double foldChange = Double.NaN;
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
 				pValue = Double.parseDouble(tok.nextToken()); //p-value
 				foldChange = Double.parseDouble(tok.nextToken());
 				fdr = Double.parseDouble(tok.nextToken());
 				//fold enrichment
 				//false discovery rate
 				
 			} else if (inputFormat == PeakFormat.SWEMBL) {
 				chromo = tok.nextToken();
 				id = "" + peakId++;
 				startCoord = Integer.parseInt(tok.nextToken());
 				endCoord = Integer.parseInt(tok.nextToken());
 				tok.nextToken();//count
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
 				pValue = Double.parseDouble(tok.nextToken());
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
 						foldChange,
 						fdr));
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
 						foldChange,
 						fdr));	
 			}
 			
 		}
 		System.err.println("peaks:"+peakCount);
 		return peaks;
 	}
 }
