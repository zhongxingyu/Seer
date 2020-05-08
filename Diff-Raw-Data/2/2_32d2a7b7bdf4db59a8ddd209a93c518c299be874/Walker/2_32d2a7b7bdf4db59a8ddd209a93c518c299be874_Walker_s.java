 package br.ufmg.dcc.vod.spiderpig.jobs.topsy;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.apache.commons.configuration.Configuration;
 import org.apache.log4j.Logger;
 
 import br.ufmg.dcc.vod.spiderpig.common.config.BuildException;
 import br.ufmg.dcc.vod.spiderpig.common.config.ConfigurableBuilder;
 import br.ufmg.dcc.vod.spiderpig.master.walker.ConfigurableWalker;
 import br.ufmg.dcc.vod.spiderpig.master.walker.monitor.ExhaustCondition;
 import br.ufmg.dcc.vod.spiderpig.master.walker.monitor.StopCondition;
 import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;
 
 import com.google.common.collect.Lists;
 
 public class Walker implements ConfigurableWalker {
 
 	private static final ExhaustCondition CONDITION = new ExhaustCondition();
     private static final Logger LOG = Logger.getLogger(Walker.class);
    private int OVERFLOW_NUM = 0;
     
 	@Override
 	public Iterable<CrawlID> walk(CrawlID id, Iterable<CrawlID> links) {
 		ArrayList<CrawlID> linksList = Lists.newArrayList(links);
         LOG.info("Received " + linksList.size() + " tweets for " + id);
         
         if (linksList.size() <= OVERFLOW_NUM) {
             LOG.info("No Overflow! Done " + id.getId());
             return Collections.emptyList();
         }
         
         ArrayList<Long> timeStamps = getTimeStamps(links);
         Long last = timeStamps.get(timeStamps.size() - 1);
         
         String query = id.getId();
         String[] split = query.split("\t");
         String queryText = split[0];
         
         String nextQuery = queryText + "\t" + last;
         LOG.info("New Seed 1/2l = " + nextQuery);
         
         return Lists.newArrayList(
         		CrawlID.newBuilder().setId(nextQuery).build());
 	}
 	private ArrayList<Long> getTimeStamps(Iterable<CrawlID> links) {
 		ArrayList<Long> rv = new ArrayList<>();
 		for (CrawlID id : links) {
 			long tstamp = Long.parseLong(id.getId());
 			rv.add(tstamp);
 		}
 		Collections.sort(rv);
 		return rv;
 	}
 	@Override
 	public void errorReceived(CrawlID idWithError) {
 		LOG.info("ID returned with error " + idWithError);
 	}
     
 	@Override
     public StopCondition getStopCondition() {
         return CONDITION;
     }
     
     @Override
     public Set<String> getRequiredParameters() {
         return new HashSet<>();
     }
 
     @Override
     public Iterable<CrawlID> filterSeeds(Iterable<CrawlID> seeds) {
         return seeds;
     }
 	
 	@Override
 	public void configurate(Configuration configuration,
 			ConfigurableBuilder builder) throws BuildException {
 	}
 }
