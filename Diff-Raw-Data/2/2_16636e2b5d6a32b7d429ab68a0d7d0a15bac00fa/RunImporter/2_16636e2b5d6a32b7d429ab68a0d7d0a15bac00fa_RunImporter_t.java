 package de.tp82.laufometer.core;
 
 import com.google.common.base.Optional;
 import com.google.common.base.Stopwatch;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import de.tp82.laufometer.model.run.Run;
 import de.tp82.laufometer.model.run.RunDay;
 import de.tp82.laufometer.model.run.RunTickProvider;
 import de.tp82.laufometer.model.run.SingleRun;
 import org.joda.time.DateTime;
 import org.joda.time.Duration;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Service;
 
 import javax.annotation.PostConstruct;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * @author Thorsten Platz
  */
 @Service
 public class RunImporter {
 	private static final Logger LOG = Logger.getLogger(RunImporter.class.getName());
 
 	@Value(value = "${laufometer.import.run.distancePerTick}")
 	private double distancePerTick;
 
 	@Value(value = "${laufometer.import.run.minSpeed}")
 	private double minSpeed;
 
 	@Value(value = "${laufometer.import.run.maxSpeed}")
 	private double maxSpeed;
 
 	private double minTickInterval;
 	private double maxTickInterval;
 
 	@Autowired
 	RunRepository runRepository;
 
 	@PostConstruct
 	private void init() {
 		minTickInterval = distancePerTick/maxSpeed;
 		maxTickInterval = distancePerTick/minSpeed;
 
 		if(LOG.isLoggable(Level.INFO))
 			LOG.info("Initialized using \n"
 					+ "- distancePerTick=" + distancePerTick + "\n"
 					+ "- minSpeed=" + minSpeed + "\n"
 					+ "- maxSpeed=" + maxSpeed + "\n"
 					+ "- minTickInterval=" + minTickInterval + "\n"
 					+ "- maxTickInterval=" + maxTickInterval);
 	}
 
 	public List<Run> importTicksAsRuns(List<Date> ticks, boolean skipKnownTicks) {
 		Stopwatch importDuration = new Stopwatch();
 		importDuration.start();
 
 		if(LOG.isLoggable(Level.INFO))
 			LOG.info("Importing " + ticks.size() + " ticks...");
 
 		preprocess(ticks);
 
 		// filter out ticks from the past
 		if(skipKnownTicks)
 			skipKnownTicks(ticks);
 
 		List<? extends Run> runs;
 		if(ticks.isEmpty())
 			runs = Collections.emptyList();
 		else
 			runs = RunDay.from(RunTickProvider.SimpleTickProvider.from(ticks));
 
 		List<Run> foundRuns;
 		if(runs.isEmpty())
 			foundRuns = Collections.emptyList();
 		else {
 			foundRuns = Lists.newArrayList();
 			foundRuns.addAll(runs);
 			runRepository.store(Sets.newHashSet(foundRuns));
 		}
 
 		runs = Collections.unmodifiableList(runs);
 
 		importDuration.stop();
 
 		if(LOG.isLoggable(Level.INFO))
 			LOG.info("Imported " + runs.size() + " runs in " + importDuration.elapsed(TimeUnit.SECONDS) + " seconds.");
 
 		return foundRuns;
 
 	}
 
 	private void preprocess(List<Date> ticks) {
 		Collections.sort(ticks);
 	}
 
 	/**
 	 * Remove ticks from the beginning of the list if they are older than the beginning of the
 	 * last known run. Then they are already imported.
 	 * @param ticks ticks to import
 	 */
 	private void skipKnownTicks(List<Date> ticks) {
 		Optional<Run> latestRun = runRepository.findLatestRun();
 		if(latestRun.isPresent()) {
 			int initialTicks = ticks.size();
 
 			Iterator<Date> itr = ticks.listIterator();
 			while(itr.hasNext()) {
 				Date tick = itr.next();
				if(tick.before(latestRun.get().getEnd()))
 					itr.remove();
 				else
 					break;
 			}
 			if(LOG.isLoggable(Level.INFO))
 				LOG.info("Skipped " + (initialTicks - ticks.size()) + " ticks before latest run: " + latestRun.get());
 		} else {
 			if(LOG.isLoggable(Level.INFO))
 				LOG.info("No previous run exists in the repository.");
 		}
 	}
 
 	public List<SingleRun> detectRuns(List<Date> ticks) {
 		preprocess(ticks);
 
 		List<SingleRun> runs = Lists.newArrayList();
 
 		long maxTickDistanceMillis = Math.round(maxTickInterval * 1000);
 		Duration maxTickDistance = new Duration(maxTickDistanceMillis);
 
 		DateTime runBegin = new DateTime(ticks.get(0));
 		DateTime latestNextTick = runBegin.plus(maxTickDistance);
 		List<Date> runTicks = Lists.newArrayList();
 		for(Date tick : ticks) {
 			DateTime tickTime = new DateTime(tick);
 
 			if(tickTime.isBefore(latestNextTick)) {
 				runTicks.add(tick);
 				latestNextTick = tickTime.plus(maxTickDistance);
 			} else {
 				if(!runTicks.isEmpty()) {
 					SingleRun run = createRun(runTicks);
 					runs.add(run);
 				}
 
 				// reset to collect a new run
 				runBegin = tickTime;
 				latestNextTick = runBegin.plus(maxTickDistance);
 				runTicks = Lists.newArrayList();
 			}
 		}
 		if(!runTicks.isEmpty()) {
 			SingleRun run = createRun(runTicks);
 			runs.add(run);
 		}
 
 		return runs;
 	}
 
 	private SingleRun createRun(List<Date> runTicks) {
 		SingleRun run = SingleRun.fromRunTicks(new RunTickContainer(runTicks));
 
 		if(LOG.isLoggable(Level.INFO))
 			LOG.info("Detected run: " + run);
 		return run;
 	}
 
 	private class RunTickContainer implements RunTickProvider {
 		private List<Date> runTicks;
 
 		private RunTickContainer(List<Date> runTicks) {
 			this.runTicks = runTicks;
 		}
 
 		@Override
 		public List<Date> getTicks() {
 			return runTicks;
 		}
 	}
 }
