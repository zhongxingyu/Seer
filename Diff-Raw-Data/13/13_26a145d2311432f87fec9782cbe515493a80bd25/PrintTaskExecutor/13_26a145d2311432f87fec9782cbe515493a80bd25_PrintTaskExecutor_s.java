 package org.shirdrn.easyrun.common;
 
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map.Entry;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.shirdrn.easyrun.config.Configuration;
 
 public class PrintTaskExecutor extends AbstractParallelTaskExecutor<Entry<Integer, List<String>>> {
 	
 	private static final Log LOG = LogFactory.getLog(PrintTaskExecutor.class);
 	
 	@Override
 	public Iterator<Entry<Integer,List<String>>> iterator() {
 		MyKeyValuePairs pairs = new MyKeyValuePairs();
 		for(int i=0; i<100; i+=4) {
 			pairs.put(i, Arrays.asList("Adam", "Allen"));
 			pairs.put(i+1, Arrays.asList("Bosh", "Bob", "Bill"));
 			pairs.put(i+2, Arrays.asList("Charlie", "Clan"));
 			// cause NullPointerException
 			pairs.put(i+3, null);
 		}
 		return pairs.entrySet().iterator();
 	}
 	
 	@Override
 	public void configure(Configuration config) {
 		super.configure(config);
 	}
 
 	@Override
 	protected void process(Entry<Integer, List<String>> element) throws Exception {
		LOG.info("Print: " + element.getKey() + " => " + element.getValue().toString());			
 	}
 }
