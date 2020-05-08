 package net.derkholm.nmica.extra.app.seq;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedSet;
 import java.util.StringTokenizer;
 import java.util.TreeSet;
 import java.util.Comparator;
 
 import net.derkholm.nmica.build.NMExtraApp;
 import net.derkholm.nmica.build.VirtualMachine;
 
 import org.biojava.bio.Annotation;
 import org.biojava.bio.seq.Sequence;
 import org.biojava.bio.seq.StrandedFeature;
 import org.biojava.bio.seq.impl.SimpleSequence;
 import org.biojava.bio.symbol.Location;
 import org.biojava.bio.symbol.SymbolList;
 import org.biojavax.bio.seq.RichSequence;
 import org.bjv2.util.cli.App;
 import org.bjv2.util.cli.Option;
 
 
 @App(overview = "Get sequences from Ensembl that using peaks in the FindPeaks format", generateStub = true)
 @NMExtraApp(launchName = "nmpeakseq", vm = VirtualMachine.SERVER)
 public class RetrievePeaks extends RetrieveEnsemblSequences {
 	public static enum RankOrder {
     	ASC,
     	DESC,
     	NONE
     }
 	
 	private File peaksFile;
 	private File outFile;
 	private int maxLength = Integer.MAX_VALUE;
 	private int minLength = Integer.MIN_VALUE;
 	private RankOrder rankOrder = RankOrder.DESC;
 	private int maxCount = 0;
 	
 	@Option(help="Peaks")
 	public void setPeaks(File f) {
 		peaksFile = f;
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
 	
 	@Option(help="Maximum peak length",optional=true)
 	public void setMaxLength(int maxLength) {
 		this.maxLength = maxLength;
 	}
 	
 	@Option(help="Minimum peak length",optional=true)
 	public void setMinLength(int minLength) {
 		this.minLength = minLength;
 	}
 	
 	public static class PeakEntry {
 		public final int id;
 		public String seqName;
 		public final int startCoord;
 		public final int endCoord;
 		public final int regEndCoord;
 		public final double value;
 		
 		public PeakEntry(
 				int id, 
 				String seqName,
 				int startCoord, 
 				int endCoord, 
 				int regEndCoord, 
 				double value) {
 			this.id = id;
 			this.seqName = seqName;
 			this.startCoord = startCoord;
 			this.endCoord = endCoord;
 			this.regEndCoord = regEndCoord;
 			this.value = value;
 		}
 
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result + id;
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
 			PeakEntry other = (PeakEntry) obj;
 			if (id != other.id)
 				return false;
 			return true;
 		}
 	}
 	
 	public static abstract class PeakEntryComparator implements Comparator<PeakEntry> {
 		
 	}
 	
 	public static class PeakEntryAscComparitor extends PeakEntryComparator implements Comparator<PeakEntry> {
 
 		public int compare(PeakEntry o1, PeakEntry o2) {
 			return Double.compare(o1.value, o2.value);
 		}
 		
 	}
 	
 	public static class PeakEntryDescComparitor extends PeakEntryComparator implements Comparator<PeakEntry> {
 
 		public int compare(PeakEntry o1, PeakEntry o2) {
 			return -Double.compare(o1.value, o2.value);
 		}
 		
 	}
 	
 	public static class PeakEntryRandomComparitor extends PeakEntryComparator implements Comparator<PeakEntry> {
 		public int compare(PeakEntry o1, PeakEntry o2) {
 			return 0;
 		}
 	}
 	
 	
 	public void main(String[] argv) throws Exception {
 		initializeEnsemblConnection();
 		
 		PrintStream os = null;
 		if (this.outFile == null) {
 			os = System.out;
 		} else {
 			os = new PrintStream(new FileOutputStream(this.outFile, true));
 		}
 		
 		BufferedReader br = new BufferedReader(new FileReader(peaksFile));
 		
 		PeakEntryComparator comp = null;
 		
 		if (rankOrder == RankOrder.ASC) {
 			comp = new PeakEntryAscComparitor();
 		} else if (rankOrder == RankOrder.DESC) {
 			comp = new PeakEntryDescComparitor();
 		} else {
 			comp = new PeakEntryRandomComparitor();
 		}
 		
 		SortedSet<PeakEntry> peaks = new TreeSet<PeakEntry>(comp);
 		String line = null;
 		br.readLine();//ignore the header
 		while ((line = br.readLine()) != null) {
 			StringTokenizer tok = new StringTokenizer(line,"\t");
 			
 			int id = Integer.parseInt(tok.nextToken());
 			String chromo = tok.nextToken();
 			int startCoord = Integer.parseInt(tok.nextToken());
 			int endCoord = Integer.parseInt(tok.nextToken());
 			int minValCoord = Integer.parseInt(tok.nextToken());
 			double value = Double.parseDouble(tok.nextToken());
 			
 			boolean maxLengthCondition = Math.abs(startCoord - endCoord) < this.maxLength;
 			if (!maxLengthCondition) continue;
 			boolean minLengthCondition = Math.abs(startCoord - endCoord) > this.minLength;
 			if (!minLengthCondition) continue;
 
 			peaks.add(new PeakEntry(id, chromo, startCoord, endCoord, minValCoord, value));
 		}
 		
 		Map<String, List<StrandedFeature>> locationMap = new HashMap<String,List<StrandedFeature>>();
 		
 		int i = 0;
 		for (PeakEntry peak : peaks) {
 			if (locationMap.get(peak.seqName) == null) {
 				locationMap.put(peak.seqName, new ArrayList<StrandedFeature>());
 			}
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
 			
 			SymbolList symList = chromoSeq.subList(peak.startCoord, peak.endCoord);
 			Sequence seq = 
 				new SimpleSequence(symList, null, 
 					String.format("%s_%d-%d;%f", 
 							peak.seqName, 
 							peak.startCoord,
 							peak.endCoord,
 							peak.value),
 					Annotation.EMPTY_ANNOTATION);
 			
 			RichSequence.IOTools.writeFasta(System.out, seq, null);
 			
 			i++;
 			
 			if ((maxCount > 0) && (i >= maxCount)) {
 				break;
 			}
 		}
 		
 		
 	}
 }
