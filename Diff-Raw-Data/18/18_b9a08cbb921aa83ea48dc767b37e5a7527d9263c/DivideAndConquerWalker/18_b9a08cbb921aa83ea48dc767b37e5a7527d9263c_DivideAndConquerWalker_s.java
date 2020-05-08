 package br.ufmg.dcc.vod.spiderpig.jobs.youtube.users;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.configuration.Configuration;
 import org.apache.log4j.Logger;
 
import br.ufmg.dcc.vod.spiderpig.jobs.youtube.rss.RSSMonitorWalker;
 import br.ufmg.dcc.vod.spiderpig.master.walker.AbstractWalker;
 import br.ufmg.dcc.vod.spiderpig.master.walker.monitor.ExhaustCondition;
 import br.ufmg.dcc.vod.spiderpig.master.walker.monitor.StopCondition;
 import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;
 
 import com.google.common.collect.Lists;
 
 public class DivideAndConquerWalker  extends AbstractWalker {
 
 	private static final Logger LOG = 
 			Logger.getLogger(DivideAndConquerWalker.class);
 	
 	private int OVERFLOW_NUM = 900;
 	private static final SimpleDateFormat RFC3339_FMT = 
 			new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
 	private static final long ONE_SECOND_MS = 1000l;
 	
 	@Override
 	public Set<String> getRequiredParameters() {
 		return new HashSet<>();
 	}
 
 	@Override
 	protected List<CrawlID> filterSeeds(List<CrawlID> seeds) {
 		return seeds;
 	}
 
 	@Override
 	protected List<CrawlID> getToWalkImpl(CrawlID id, List<CrawlID> links) {
 		
 		LOG.info("Received " + links.size() + " users");
 		
 		if (links.size() < OVERFLOW_NUM) {
			LOG.info("Overflow! Done");
 			return Collections.emptyList();
 		}
 		
 		String dates = id.getId();
 		
 		String[] split = dates.split("\\s");
 		String afterStr = split[0];
 		String beforeStr = split[1];
 		
 		try {
 			Date after = RFC3339_FMT.parse(afterStr);
 			Date before = RFC3339_FMT.parse(beforeStr);
 			
 			long afterTime = after.getTime();
 			long beforeTime = before.getTime();
 			
 			long halfDelta = ((beforeTime - afterTime) / 2);
 			if (halfDelta < ONE_SECOND_MS) {
 				return Collections.emptyList();
 			}
 			
 			long half = afterTime + halfDelta;
 			String halfStr = RFC3339_FMT.format(half);
 			
 			CrawlID firstHalfId = CrawlID.newBuilder().
 					setId(afterStr + " " + halfStr).build();
 
 			CrawlID secondHalfId = CrawlID.newBuilder().
 					setId(halfStr + " " + beforeStr).build();
 			
 			LOG.info("New Seed 1/2l = " + firstHalfId.getId());
 			LOG.info("New Seed 1/2r = " + secondHalfId.getId());
 			
 			return Lists.newArrayList(firstHalfId, secondHalfId);
 		} catch (ParseException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	protected void errorReceivedImpl(CrawlID crawled) {
 	}
 
 	@Override
 	protected StopCondition createStopCondition() {
 		return new ExhaustCondition();
 	}
 
 	@Override
 	public Void realConfigurate(Configuration configuration) throws Exception {
 		return null;
 	}
 }
