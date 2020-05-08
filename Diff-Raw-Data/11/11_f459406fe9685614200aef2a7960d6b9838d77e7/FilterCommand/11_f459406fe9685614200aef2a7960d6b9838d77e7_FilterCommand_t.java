 package seqManipulation.fastamanipulator.commands;
 
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import seqManipulation.filtersequences.FilterSequence;
 import seqManipulation.filtersequences.FilterSequenceBooleanNOT;
 
 import cmdGA.NoOption;
 import cmdGA.Option;
 import fileformats.fastaIO.Pair;
 
 public abstract class FilterCommand<Op extends Option> extends FastaCommand<Op> {
 
 	FilterSequence filter;
 	NoOption invertFilter;
 	
	public FilterCommand(InputStream inputstream, PrintStream output, Op option, NoOption invertFilter) {
 		super(inputstream, output, option);
 		
		this.invertFilter = invertFilter;
 		
 	}
 
 	@Override
 	protected List<String> performAction() {
 		
		this.getFilter();
		
 		if (invertFilter.isPresent()) filter = new FilterSequenceBooleanNOT(filter);
 		
 		List<String> results = new ArrayList<String>(); 
 		
 		List<Pair<String, String>> seqs = this.getSequences();
 		
 		for (Pair<String, String> pair : seqs) {
 			
 			if (filter.filter(pair) ) {
 				
 				results.add(">" + pair.getFirst());
 				
 				results.add(pair.getSecond());
 				
 			}
 			
 		}
 		
 		return results;
 		
 	}
 	
 	protected abstract void getFilter();
 
 }
