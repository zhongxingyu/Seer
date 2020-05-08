 package mpa.main.loader;
 
 import java.io.IOException;
 
 import mpa.mad.Mad;
 import mpa.mad.MadHome;
 import mpa.pair.Pair;
 import mpa.pair.PairHome;
 import mpa.search.evaluator.PredefinedPairs;
 
 public class ExpectedLoaderStream implements ExpectedLoader{
 
 	private PredefinedPairs predefinedPairs;
 	private ConfigStream configStream;
 	private MadHome madHome;
 	private PairHome pairHome;
 
 	public ExpectedLoaderStream(PredefinedPairs predefinedPairs, ConfigStream configData, MadHome madHome, PairHome pairHome){
 		this.predefinedPairs = predefinedPairs;
 		this.configStream = configData;
 		this.madHome = madHome;
 		this.pairHome = pairHome;
 	}
 	
 	public void load() {
 		try {
 			loadExpectedPairs();
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	private void loadExpectedPairs() throws IOException {
		String streamPairs = configStream.getExpectedPairsStream();
		if ("".equals(streamPairs))
			return;
		
		for (String pair : streamPairs.split("\n")) {
			
 			String operation = pair.substring(0,1);
 			
 			if ("+".equals(operation))
 				predefinedPairs.expectPair(readPair(pair));
 			else if ("-".equals(operation))
 				predefinedPairs.rejectPair(readPair(pair));
 			else
 				throw new RuntimeException("Expected '+' or '-' but found " + operation);
 		}
 	}
 
 	private Pair readPair(String pair) {
 		String[] mads = pair.substring(1).split("/");
 		return pairHome.createPair(getMad(mads[0].trim()), getMad(mads[1].trim()));
 	}
 	
 	private Mad getMad(String name) {
 		return madHome.getByName(name);		
 	}
 	
 }
