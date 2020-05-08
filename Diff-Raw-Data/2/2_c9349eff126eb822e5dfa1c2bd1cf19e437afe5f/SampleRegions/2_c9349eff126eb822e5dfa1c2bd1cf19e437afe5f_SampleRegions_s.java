 package net.derkholm.nmica.extra.app.seq;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Map;
 import java.util.Random;
 
 import net.derkholm.nmica.build.NMExtraApp;
 import net.derkholm.nmica.build.VirtualMachine;
 
 import org.biojava.bio.BioError;
 import org.biojava.bio.BioException;
 import org.biojava.bio.program.gff.GFFDocumentHandler;
 import org.biojava.bio.program.gff.GFFParser;
 import org.biojava.bio.program.gff.GFFRecord;
 import org.biojava.bio.program.gff.GFFWriter;
 import org.biojava.bio.program.gff.SimpleGFFRecord;
 import org.biojava.bio.seq.StrandedFeature;
 import org.biojava.bio.seq.StrandedFeature.Strand;
 import org.biojava.utils.ParserException;
 import org.bjv2.util.cli.App;
 import org.bjv2.util.cli.Option;
 
 import biobits.utils.IOTools;
 
 @App(overview = "Sample regions", generateStub = true)
 @NMExtraApp(launchName = "nmsampleregions", vm = VirtualMachine.SERVER)
 public class SampleRegions {
 	
 	private int sampleLength;
 	private ArrayList<GFFRecord> features;
 	public int totalLength;
 	private File featuresFile;
 	private int sampleCount;
 	private static Random random = new Random();
 
 	@Option(help="Sampled sequence length")
 	public void setLength(int len) {
 		this.sampleLength = len;
 	}
 	
 	@Option(help="Input features")
 	public void setFeatures(File f) {
 		this.featuresFile = f;
 	}
 	
 	@Option(help="Count of samples to take")
 	public void setCount(int i) {
 		this.sampleCount = i;
 	}
 	
 	public void main(String[] args) throws IOException, BioException, ParserException, Exception {
 		this.features = new ArrayList<GFFRecord>();
 		
 		GFFParser parser = new GFFParser();
 		RegionHandler rHandler = new RegionHandler();
 		parser.parse(IOTools.fileBufferedReader(featuresFile),rHandler);
 		
 		Collections.sort(this.features, new LengthComparator());
 		double[] featureWeights = new double[features.size()];
 		
 		for (int i = 0; i < features.size(); i++) {
 			GFFRecord r = this.features.get(i);
 			double len = r.getEnd() - r.getStart();
 			featureWeights[i] = len / (double)this.totalLength;
 		}
 		
 		GFFWriter writer = new GFFWriter(new PrintWriter(System.out));
 		
 		int r = 0; 
 		while (r < this.sampleCount) {
 			
 			final GFFRecord rec = features.get(randMultinomial(featureWeights));
 			
 			int min = rec.getStart();
             int max = rec.getEnd();
             if (max < min) {
             	min = rec.getEnd();
             	max = rec.getStart();
             }
             
             final int len = max - min;
			final int randomEnd = min + sampleLength + random.nextInt(len-sampleLength+1);
 			final int randomStart = randomEnd - sampleLength;
 			
 			if (randomStart < min) {
 				System.err.println("Too short sequence (sample precedes beginning)");
 				continue;
 			} else if (randomEnd > max) {
 				throw new BioError("Too short sequence (went past end)");
 			}
 			writer.recordLine(new GFFRecord() {
 				public String getComment() {
 					return null;
 				}
 
 				public int getEnd() {
 					return randomEnd;
 				}
 
 				public String getFeature() {
 					return rec.getFeature();
 				}
 
 				public int getFrame() {
 					return rec.getFrame();
 				}
 
 				public Map<?, ?> getGroupAttributes() {
 					return rec.getGroupAttributes();
 				}
 
 				public double getScore() {
 					return rec.getScore();
 				}
 
 				public String getSeqName() {
 					return rec.getSeqName();
 				}
 
 				public String getSource() {
 					return rec.getSource();
 				}
 
 				public int getStart() {
 					return randomStart;
 				}
 
 				public Strand getStrand() {
 					return rec.getStrand();
 				}
 			});
 			
 			writer.endDocument();
 			
 			r++;
 		}
 	}
 	
 	public static int randMultinomial(double[] probs) {
 		double v = random.nextDouble();
 		double sum = 0;
 		for(int i = 0; i < probs.length; i++) {
 			sum += probs[i];
 			if(v < sum) return i;
 		} throw new RuntimeException(sum + " < " + v);
 	}
 
 	
 	private class LengthComparator implements Comparator<GFFRecord> {
 
 		public int compare(GFFRecord o1, GFFRecord o2) {
 			int o1len = o1.getEnd() - o1.getStart();
 			int o2len = o2.getEnd() - o2.getStart();
 			
 			if (o1len > o2len) {
 				return 1;
 			} else if (o1len < o2len) {
 				return -1;
 			} 
 			
 			return 0;
 		}
 		
 	}
 	
 	private class RegionHandler implements GFFDocumentHandler {
 		private int index;
 
 		public RegionHandler() {
 			this.index = 0;
 		}
 
 		public void commentLine(String arg0) {
 			
 		}
 
 		public void endDocument() {
 			
 		}
 
 		public void recordLine(GFFRecord record) {
 			int min = record.getStart();
             int max = record.getEnd();
             if (max < min) {
             	min = record.getEnd();
             	max = record.getStart();
             }
             
             if ((max - min) >= sampleLength) {
                 features.add(record);
                 int len = max-min;
                 totalLength += len;            	
             }
 		}
 
 		public void startDocument(String arg0) {
 		}
 	}
 }
