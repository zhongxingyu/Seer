 package net.derkholm.nmica.extra.app.seq;
 
 import gfftools.GFFUtils;
 
 import java.io.File;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import net.derkholm.nmica.build.NMExtraApp;
 import net.derkholm.nmica.build.VirtualMachine;
 
 import org.biojava.bio.SmallAnnotation;
 import org.biojava.bio.program.gff.GFFRecord;
 import org.biojava.bio.seq.Sequence;
 import org.biojava.bio.seq.SequenceTools;
 import org.biojava.bio.seq.StrandedFeature;
 import org.biojava.bio.symbol.Location;
 import org.biojava.bio.symbol.RangeLocation;
 import org.bjv2.util.cli.App;
 import org.bjv2.util.cli.Option;
 
 @App(overview = "Calculate the distance of features in one feature set to the closest features of another feature set", generateStub = true)
 @NMExtraApp(launchName = "nmfeatdist", vm = VirtualMachine.SERVER)
 public class FeatureToFeatureDistance {
 	
 	private int distanceThreshold = 2000;
 	private File comparisonFeatures;
 	private File features;
 
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
 	
 	public void main(String[] args) throws Exception {
 		Map<String, Location> featureMap = GFFUtils.gffToLocationMap(this.features);
 		Map<String, Sequence> sequenceMap = gffToAnnotationMap(this.comparisonFeatures);
 	}
 	
 	public static Map<String, Sequence> gffToAnnotationMap(File f) throws Exception {
 		Map<String, List<GFFRecord>> recs = GFFUtils.gffToRecordMap(f);
 		
 		Map<String, Sequence> map = new TreeMap<String, Sequence>();
 		
 		for (String str : recs.keySet()) {
 			List<GFFRecord> seqRecs = recs.get(str);
 			
 			Sequence s = SequenceTools.createDummy(null, str);
 			
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
