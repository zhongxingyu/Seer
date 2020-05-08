 package net.derkholm.nmica.extra.app.seq;
 
 import gfftools.GFFUtils;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedSet;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import net.derkholm.nmica.build.NMExtraApp;
 import net.derkholm.nmica.build.VirtualMachine;
 import net.derkholm.nmica.extra.seq.DistanceFromStartOfStrandedFeatureToPointLocationComparator;
 
 import org.biojava.bio.SmallAnnotation;
 import org.biojava.bio.program.gff.GFFRecord;
 import org.biojava.bio.program.gff.GFFWriter;
 import org.biojava.bio.seq.DNATools;
 import org.biojava.bio.seq.FeatureFilter;
 import org.biojava.bio.seq.FeatureHolder;
 import org.biojava.bio.seq.Sequence;
 import org.biojava.bio.seq.SequenceTools;
 import org.biojava.bio.seq.StrandedFeature;
 import org.biojava.bio.symbol.PointLocation;
 import org.biojava.bio.symbol.RangeLocation;
 import org.bjv2.util.cli.App;
 import org.bjv2.util.cli.Option;
 
 @App(overview = "Calculate the distance of features in one feature set to the closest features of another feature set", generateStub = true)
 @NMExtraApp(launchName = "nmfeatdist", vm = VirtualMachine.SERVER)
 public class FeatureToFeatureDistance {
 	
 	private int distanceThreshold = 2000;
 	private File comparisonFeatures;
 	private File features;
 	private File outFile;
 	private GFFWriter gffWriter;
 
 	@Option(help="Feature set to find the closest features for")
 	public void setFeatures(File f) {
 		this.features = f;
 	}
 	
 	@Option(help="Distance threshold",optional=true)
 	public void setDistThreshold(int i) {
 		this.distanceThreshold = i;
 	}
 	
 	@Option(help="Comparison feature set")
 	public void setToFeatures(File f) {
 		this.comparisonFeatures = f;
 	}
 	
 	@Option(help="Output file (output goes to stdout if no file given)", optional=true)
 	public void setOut(File f) {
 		this.outFile = f;
 	}
 	
 	public void main(String[] args) throws Exception {
 
 		final OutputStream os;
 		if (this.outFile == null) {
 			os = System.out;
 		} else {
 			os = new FileOutputStream(this.outFile);
 		}
 
 		this.gffWriter = new GFFWriter(new PrintWriter(os));
 		
 		Map<String, List<GFFRecord>> featureMap = GFFUtils.gffToRecordMap(this.features);
 		Map<String, Sequence> sequenceMap = gffToAnnotationMap(this.comparisonFeatures);
 		
 		for (String str : featureMap.keySet()) {
 			List<GFFRecord> locs = featureMap.get(str);
 			
 			for (GFFRecord r : locs) {
 				RangeLocation l = (RangeLocation) new RangeLocation(r.getStart(), r.getEnd());;
 				int point = l.getMin() + (l.getMax() - l.getMin()) / 2;
 				
 				Comparator<StrandedFeature> comp = 
 					new DistanceFromStartOfStrandedFeatureToPointLocationComparator(new PointLocation(point));
 				
				SortedSet<StrandedFeature> feats = new TreeSet<StrandedFeature>();
 				Sequence seq = sequenceMap.get(str);
 				
 				FeatureFilter locationFilter = 
 					new FeatureFilter.OverlapsLocation(
 						new RangeLocation(point - this.distanceThreshold, point + this.distanceThreshold));
 				FeatureHolder filteredFeatures = seq.filter(locationFilter);
 				
 				Iterator<?> fs = filteredFeatures.features();
 				while (fs.hasNext()) {feats.add((StrandedFeature) fs.next());}
 				
 				if (feats.size() > 0) {
 					StrandedFeature closestFeature = feats.first();
 					
 					int distance = 
 						DistanceFromStartOfStrandedFeatureToPointLocationComparator
 							.distance(closestFeature, point);
 					
 					r.getGroupAttributes().put("distance", distance);
 					gffWriter.recordLine(r);
 					gffWriter.endDocument();// forces buffer flush
 				} else {
 					System.err.printf("WARNING: No feature found within +/- %d from %s:%d%n",this.distanceThreshold, str, point);
 				}
 			}
 		}
 	}
 	
 	public static Map<String, Sequence> gffToAnnotationMap(File f) throws Exception {
 		Map<String, List<GFFRecord>> recs = GFFUtils.gffToRecordMap(f);
 		
 		Map<String, Sequence> map = new TreeMap<String, Sequence>();
 		
 		
 		
 		
 		for (String str : recs.keySet()) {
 			List<GFFRecord> seqRecs = recs.get(str);
 			
 			Sequence s = 
 				SequenceTools.createDummy(
 						DNATools.getDNA(), 
 						Integer.MAX_VALUE, 
 						DNATools.n(), 
 						null, 
 						str);
 			
 			map.put(str, s);
 			
 			for (GFFRecord r : seqRecs) {
 				StrandedFeature.Template templ = new StrandedFeature.Template();
 				templ.source = r.getSource();
 				templ.type = r.getFeature();
 				templ.annotation = new SmallAnnotation();
 				templ.location = new RangeLocation(r.getStart(), r.getEnd());
 				templ.strand = r.getStrand();
 				templ.annotation.setProperty("score", r.getScore());
 				if (r.getComment() != null) {
 					templ.annotation.setProperty("comment", r.getComment());
 				}
 				for (Object o : r.getGroupAttributes().keySet()) 
 					{templ.annotation.setProperty(o, r.getGroupAttributes().get(o));}
 				
 				s.createFeature(templ);
 			}
 		}
 		return map;
 	}
 	
 
 }
