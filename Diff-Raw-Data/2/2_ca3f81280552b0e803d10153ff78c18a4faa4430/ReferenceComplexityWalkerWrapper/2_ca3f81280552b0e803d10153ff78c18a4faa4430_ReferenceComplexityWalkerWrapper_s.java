 package net.malariagen.gatk.coverage;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 
 import net.malariagen.gatk.coverage.CoverageBiasWalker.GroupBy;
 
 import org.broadinstitute.sting.gatk.GenomeAnalysisEngine;
 import org.broadinstitute.sting.gatk.contexts.AlignmentContext;
 import org.broadinstitute.sting.gatk.contexts.ReferenceContext;
 import org.broadinstitute.sting.gatk.refdata.RefMetaDataTracker;
 import org.broadinstitute.sting.utils.GenomeLoc;
 import org.broadinstitute.sting.utils.GenomeLocParser;
 import org.broadinstitute.sting.utils.codecs.vcf.VCFHeader;
 import org.broadinstitute.sting.utils.codecs.vcf.VCFWriter;
 import org.broadinstitute.sting.utils.variantcontext.Genotype;
 import org.broadinstitute.sting.utils.variantcontext.GenotypesContext;
 import org.broadinstitute.sting.utils.variantcontext.VariantContext;
 import org.broadinstitute.sting.utils.variantcontext.VariantContextBuilder;
 
 public class ReferenceComplexityWalkerWrapper {
 
 	private GenomeAnalysisEngine toolkit;
 	
 	private GroupBy groupBy;
 	
 	private File fragmentLengthFile;
 	
 	private Map<String,List<String>> windowSizeToGroup;
 	
 	private Integer windowSize;
 	
 	private final SortedMap<GenomeLoc, LocusComplexity> complexityBuffer = new TreeMap<GenomeLoc, net.malariagen.gatk.coverage.ReferenceComplexityWalkerWrapper.LocusComplexity>();
 
 	private ReferenceComplexityWalker complexityWalker = new ReferenceComplexityWalker();
 
 	private Set<String> groupNames;
 
 	
 	public ReferenceComplexityWalkerWrapper(GenomeAnalysisEngine gae, GroupBy gb, File fls, Integer windowSize, Set<String> groupNames) {
 		this.toolkit = gae;
 		this.groupBy = gb;
 		this.fragmentLengthFile = fls;
 		this.windowSize = windowSize;
 		this.groupNames = groupNames;
 		initialize();
 
 	}
 
 
 
 	class LocusComplexity {
 
 		public final GenomeLoc locus;
 		public VariantContext forward;
 		public GenotypesContext reverse;
 		
 		LocusComplexity(GenomeLoc l, VariantContext fwd) {
 			locus = l;
 			forward = fwd;
 			reverse = GenotypesContext.create();
 		}
 
 		LocusComplexity(GenomeLoc l, Genotype gt) {
 			this(l, (VariantContext) null);
 			reverse.add(gt);
 		}
 
 		void addReverse(Genotype gt) {
 			reverse.add(gt);
 		}
 		
 		private VariantContext translateWsToGroupName(VariantContext vc) {
 			VariantContextBuilder vcb = new VariantContextBuilder(vc);
 			GenotypesContext gc = GenotypesContext.create();
 			for (Genotype gt : forward.getGenotypes()) {
				List<String> names = windowSizeToGroup.get(gc);
 				for (String name : names) {
 					if (groupNames.contains(name)) gc.add(new Genotype(name, gt.getAlleles(), gt.getLog10PError(), gt.getFilters(), gt.getAttributes(), gt.isPhased()));
 				}
 			}
 			vcb.genotypes(gc);
 			return vcb.make();
 		}
 
 		double getGcBias(String name, boolean forward) {
 			if (forward)
 				return getForwardGcBias(name);
 			else
 				return getReverseGcBias(name);
 		}
 
 		private double getReverseGcBias(String name) {
 			Genotype gt = reverse.get(name);
 			if (gt == null)
 				return Double.NaN;
 			return gt.getAttributeAsDouble("GC", Double.NaN);
 		}
 
 		private double getForwardGcBias(String name) {
 			if (forward == null)
 				return Double.NaN;
 			Genotype gt = forward.getGenotype(name);
 			if (gt == null)
 				return Double.NaN;
 			return gt.getAttributeAsDouble("GC", Double.NaN);
 		}
 
 		public boolean isCompleted() {
 			return forward != null;
 		}
 
 		public VariantContext getForwardVariantContext() {
 			return translateWsToGroupName(forward);
 		}
 
 		public VariantContext getReverseVariantContext() {
 			if (forward == null)
 				throw new IllegalStateException("locus complexity not completed");
 			return translateWsToGroupName(new VariantContextBuilder(forward).genotypes(reverse).make());	
 		}
 	}
 	
 	public List<LocusComplexity> removeCompleted() {
 	
 		List<GenomeLoc> toRemove = new LinkedList<GenomeLoc>();
 		List<LocusComplexity> result = new LinkedList<LocusComplexity>();
 		for (Map.Entry<GenomeLoc,LocusComplexity> e : complexityBuffer.entrySet()) {
 			LocusComplexity lc = e.getValue();
 			if (!lc.isCompleted()) continue;
 			toRemove.add(e.getKey());
 			result.add(lc);
 		}
 		for (GenomeLoc loc : toRemove)
 		  complexityBuffer.remove(loc);
 		return result;
 	}
 	
 	public void map(RefMetaDataTracker tracker,
 			ReferenceContext ref, AlignmentContext context) {
 		complexityWalker.map(tracker, ref, context);
 	}
 	
 	public MultiWindowSequenceComplexity reduce(ReferenceContext rc, MultiWindowSequenceComplexity sum) {
 		return complexityWalker.reduce(rc, sum);
 	}
 	
 	private void initialize() {
 		final GenomeLocParser locParser = toolkit.getGenomeLocParser();
 		complexityWalker.setToolkit(toolkit);
 		complexityWalker.groupBy = GroupBy.WS;
 		complexityWalker.rounding = 10;
 		complexityWalker.windowSize = this.windowSize;
 		complexityWalker.fragmentLengthsFile = this.fragmentLengthFile;
 		complexityWalker.writer = new VCFWriter() {
 
 			@Override
 			public void writeHeader(VCFHeader header) {
 				// Ignore it.
 			}
 
 			@Override
 			public void close() {
 				// Ignore it.
 			}
 
 			@Override
 			public void add(VariantContext vc) {
 				
 				GenomeLoc loc = locParser.createGenomeLoc(vc.getChr(),
 						vc.getStart(), vc.getStart());
 				LocusComplexity c = complexityBuffer.get(loc);
 				if (c == null) {
 					complexityBuffer.put(loc, c = new LocusComplexity(loc, vc));
 				}
 				else 
 					c.forward = vc;
 				for (Genotype gt : vc.getGenotypes()) {
 					int end = gt.getAttributeAsInt("ED", -1);				
 					if (end == -1)
 						continue;
 					GenomeLoc rloc = locParser.createGenomeLoc(loc.getContig(),
 							end, end);
 					LocusComplexity rc = complexityBuffer.get(rloc);
 					if (rc == null)
 						complexityBuffer.put(rloc,
 								rc = new LocusComplexity(rloc, gt));
 					else
 						rc.addReverse(gt);
 				}
 			}
 
 		};
 		complexityWalker.initialize();
 		windowSizeToGroup = new HashMap<String,List<String>>();
 		for (Map.Entry<String,Integer> e : this.complexityWalker.groupWindowSize.entrySet()) {
 			String wsStr = "" + e.getValue();
 			List<String> groupList = windowSizeToGroup.get(wsStr);
 			if (groupList == null)
 				windowSizeToGroup.put(wsStr,groupList = new LinkedList<String>());
 			groupList.add(e.getKey());
 		}
 		
 	}
 
 	public MultiWindowSequenceComplexity reduceInit() {
 		return complexityWalker.reduceInit();
 	}
 
 }
