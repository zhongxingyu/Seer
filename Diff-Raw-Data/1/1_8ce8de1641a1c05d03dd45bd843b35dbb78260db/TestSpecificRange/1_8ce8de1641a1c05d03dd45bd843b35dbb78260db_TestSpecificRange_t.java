 package app.specificReads;
 
 import java.awt.Component;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import reader.AlignmentHandlersIfc;
 import reader.AlignmentReader;
 import reader.ReadDetails;
 import Games.whatsIntheBin.InterestingGene;
 import alignment.Alignment;
 import app.handlers.SingleGeneAlignmentCount;
 import app.handlers.SingleGeneAlignmentStatistics;
 
 import common.filters.PrimaryAlignmentFilter;
 
 
 public class TestSpecificRange {
 	
 	private SpecificRangeInput params;
 	private static final int MERGIN = 1000;
 	private static final int N_BINS = 100;
 	
 	public TestSpecificRange(SpecificRangeInput params) {
 		this.params = params;
 	}
 	
 	public Map<String, Component> generateGraph() throws Exception {
 		TestSpecificRangeReader generator = new TestSpecificRangeReader(params.getGene());
 		generator.handleReads(params.getInputs(), new PrimaryAlignmentFilter());
 		return generator.getResults();
 	}
 
 	private static class TestSpecificRangeReader extends AlignmentReader {
 
 		private InterestingGene gene;
 		
 		public TestSpecificRangeReader(Object context) {
 			super(context);
 			this.gene = (InterestingGene)context;
 		}
 
 		public Map<String, Component> getResults() {
 			Map<String, Component> res = new HashMap<String, Component>();
 			for (Entry<String, List<AlignmentHandlersIfc>> itr : containers.entrySet()) {
 				for (AlignmentHandlersIfc handler : itr.getValue()) {
 					Map<String, Component> handlerRes = handler.collectResults(); 
 					for ( Entry<String, Component> frames : handlerRes.entrySet()) {
 						res.put(frames.getKey(), frames.getValue());
 					}
 				}
 			}
 			return res;
 		}
 		
 		protected Map<String, List<AlignmentHandlersIfc>> createContainers() {
 			InterestingGene gene = (InterestingGene)context;
 			Map<String, List<AlignmentHandlersIfc>> containers = new HashMap<String, List<AlignmentHandlersIfc>>();
 			
 			List<AlignmentHandlersIfc> handlers = new ArrayList<AlignmentHandlersIfc>();
 			handlers.add(new SingleGeneAlignmentStatistics(N_BINS, MERGIN, "align", gene.getRange()));
 			handlers.add(new SingleGeneAlignmentCount("count"));
 			containers.put(getName(gene), handlers);
 			
 			return containers;
 		}
 	
 		protected void handleSingleAlignment(ReadDetails read, Alignment align, double count) {
 			
 			if(gene.isInteresting(align, true)) {
 				for (AlignmentHandlersIfc handler : containers.get(getName(gene))) {
 					handler.handleAlignment(Boolean.TRUE, read.getName(), align, count);
 				}
 			}
 			
 			if(gene.isInteresting(align, false)) {
 				for (AlignmentHandlersIfc handler : containers.get(getName(gene))) {
 					handler.handleAlignment(Boolean.FALSE, read.getName(), align, count);
 				}
 			}
 		}
 		
 		private String getName(InterestingGene gene) {
 			return gene.getChr().name() + "_" + gene.getRange();
 		}
 	}	
 }
