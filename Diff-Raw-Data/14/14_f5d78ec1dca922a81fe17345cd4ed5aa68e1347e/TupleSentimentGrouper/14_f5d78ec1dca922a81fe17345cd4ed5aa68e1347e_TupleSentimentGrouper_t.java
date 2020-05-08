 package org.seasr.meandre.components.sentiment;
 
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Collections;
 import java.util.StringTokenizer;
 
 
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.components.abstracts.AbstractExecutableComponent;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.meandre.core.ComponentExecutionException;
 import org.seasr.datatypes.BasicDataTypes;
 import org.seasr.datatypes.BasicDataTypesTools;
 import org.seasr.datatypes.BasicDataTypes.Strings;
 import org.seasr.datatypes.BasicDataTypes.StringsArray;
 import org.seasr.meandre.components.tools.Names;
 
 
 import org.seasr.meandre.support.sentiment.SentimentSupport;
 import org.seasr.meandre.support.components.tuples.SimpleTuple;
 import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;
 
 
 
 /**
  *
  * @author Mike Haberman;
  *
  */
 
 
 @Component(
 		name = "Tuple Sentiment Grouper",
 		creator = "Mike Haberman",
 		baseURL = "meandre://seasr.org/components/foundry/",
 		firingPolicy = FiringPolicy.all,
 		mode = Mode.compute,
 		rights = Licenses.UofINCSA,
 		tags = "semantic, tools, text, opennlp, tokenizer, sentences, pos, tagging",
 		description = "This component labels the incoming set of pos tuples " ,
 		dependency = {"trove-2.0.3.jar","protobuf-java-2.2.0.jar"}
 )
 public class TupleSentimentGrouper  extends AbstractExecutableComponent {
 	
 
     //------------------------------ INPUTS ------------------------------------------------------
 	
 	@ComponentInput(
 			name = Names.PORT_TUPLES,
 			description = "set of labelled tuples to be grouped (e.g. startToken, token, concept)"
 	)
 	protected static final String IN_TUPLES = Names.PORT_TUPLES;
 	
 	@ComponentInput(
 			name = Names.PORT_META_TUPLE,
 			description = "meta data for tuples to be labeled"
 	)
 	protected static final String IN_META_TUPLE = Names.PORT_META_TUPLE;
 	
 
 	
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 	
 	@ComponentOutput(
 			name = Names.PORT_TUPLES,
 			description = "set of grouped tuples"
 	)
 	protected static final String OUT_TUPLES = Names.PORT_TUPLES;
 	
 	@ComponentOutput(
 			name = Names.PORT_META_TUPLE,
 			description = "meta data for the tuples (windowId, begin, end, concept, count, frequency)"
 	)
 	protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;
 	
 	
 	
 	
 	//----------------------------- PROPERTIES ---------------------------------------------------
    @ComponentProperty(description = "field name for the key to group on",
 		   name = "key",
 		   defaultValue = "concept")
     protected static final String DATA_PROPERTY_CONCEPTS = "concept";
 	//--------------------------------------------------------------------------------------------
 
    
    // TODO: pull from properties
    private String conceptLabel = "concept";
    private String startLabel   = "tokenStart";
    private String posLabel     = "pos";
    
   // tuple field names
    public static String WINDOW_FIELD  = "windowId";
    public static String POS_FIELD     = "pos";
    public static String START_FIELD   = "start";
    public static String COUNT_FIELD   = "count";
    public static String CONCEPT_FIELD = "concept";
    public static String FREQ_FIELD    = "frequency";
    
 	
 	public void initializeCallBack(ComponentContextProperties ccp) throws Exception 
 	{
 	}
 
 	public void executeCallBack(ComponentContext cc) throws Exception 
 	{
 		/*
 		Strings inputMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
 		String[] meta = DataTypeParser.parseAsString(inputMeta);
 		String fields = meta[0];
 		DynamicTuplePeer inPeer = new DynamicTuplePeer(fields);
 		
 		Strings input = (Strings) cc.getDataComponentFromInput(IN_TUPLES);
 		String[] tuples = DataTypeParser.parseAsString(input);
 		*/
 		
 		Strings inputMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
 		SimpleTuplePeer inPeer  = new SimpleTuplePeer(inputMeta);
 		SimpleTuplePeer outPeer = new SimpleTuplePeer(new String[]{
 				                 WINDOW_FIELD, START_FIELD, POS_FIELD,
 							     CONCEPT_FIELD, COUNT_FIELD, FREQ_FIELD});	
 		
 		StringsArray input = (StringsArray) cc.getDataComponentFromInput(IN_TUPLES);
 		Strings[] in = BasicDataTypesTools.stringsArrayToJavaArray(input);
 		
 		
 		SimpleTuple tuple    = inPeer.createTuple();
 		SimpleTuple outTuple = outPeer.createTuple();
 		
 		int CONCEPT_IDX = inPeer.getIndexForFieldName(conceptLabel);
 		int START_IDX   = inPeer.getIndexForFieldName(startLabel);
 		int POS_IDX     = inPeer.getIndexForFieldName(posLabel);
 		
 		
		long maxWindows = 65;       // how many windows of data do you want
 		long currentPosition = 0;
 		long total = 0;
 		int window = 1;
 		
 		// the last tuple has the last idx
 		tuple.setValues(in[in.length - 1]);
 		long end = Long.parseLong(tuple.getValue(START_IDX));
		int windowSize = (int) (end/maxWindows);
		if (windowSize <= end ) {
			windowSize = (int) end;
		}
		
		console.info("Window size (charcters of text per group) " + windowSize);
 		
 		Map<String,Integer> freqMap = new HashMap<String,Integer>();
 		
 		List<Strings> output = new ArrayList<Strings>();
 		for (int i = 0; i < in.length; i++) {
 			
 			tuple.setValues(in[i]);	
 			String concept = tuple.getValue(CONCEPT_IDX);
 			String pos     = tuple.getValue(POS_IDX);
 			long start     = Long.parseLong(tuple.getValue(START_IDX));
 			
 			if (concept == null) {
 				console.info("warning, null concept");
 				continue;
 			}
 			
 			Integer count = freqMap.get(concept);
 			if (count == null) {
 				count = new Integer(0);
 			}
 			freqMap.put(concept, count + 1);
 			total += 1;
 			
 			//
 			// check to see if we have a window's worth of data
 			//
 			if (start - currentPosition > windowSize || (i + 1 == in.length)) {
 				
 				List<Map.Entry<String, Integer>> sortedEntries;
 				sortedEntries = SentimentSupport.sortHashMap(freqMap);
 				
 				for (Map.Entry<String,Integer> v : sortedEntries) {
 					count      = v.getValue();
 					String key = v.getKey();
 					
 				//Iterator<String> it = freqMap.keySet().iterator();
 				// while(it.hasNext()) {
 					//String key = it.next();
 					//count = freqMap.get(key);
 					
 					double f = ((double)count/(double)total) * 100.0;
 					int rf = (int) f;
 					
 					outTuple.setValue(WINDOW_FIELD,  Integer.toString(window));
 					outTuple.setValue(START_FIELD,   Long.toString(currentPosition));
 					outTuple.setValue(POS_FIELD,     pos);
 					outTuple.setValue(CONCEPT_FIELD, key);
 					outTuple.setValue(COUNT_FIELD,   Integer.toString(count));
 					outTuple.setValue(FREQ_FIELD,    Integer.toString(rf));
 					output.add(outTuple.convert());
 				}
 				
 				freqMap.clear();  
 				total = 0;
 				currentPosition = start;
 				window++;
 				
 			}
 		}
 		
 		
 		//
 		// push the whole collection, protocol safe
 		//
 		Strings[] results = new Strings[output.size()];
 		output.toArray(results);
 		StringsArray outputSafe = BasicDataTypesTools.javaArrayToStringsArray(results);
 		cc.pushDataComponentToOutput(OUT_TUPLES, outputSafe);
 
 	    //
 		// metaData for this tuple producer
 		//
 	    cc.pushDataComponentToOutput(OUT_META_TUPLE, outPeer.convert());
 		
 	}
 
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception 
     {
     	    
     }
     
     
     
 	
 }
 
 
