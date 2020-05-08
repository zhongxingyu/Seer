 package local.radioschedulers.alg.parallel;
 
import java.util.HashMap;
 import java.util.Set;
 
 import local.radioschedulers.Job;
 import local.radioschedulers.JobCombination;
 import local.radioschedulers.LSTTime;
 import local.radioschedulers.Schedule;
 import local.radioschedulers.ScheduleSpace;
 import local.radioschedulers.alg.ga.watchmaker.SortedCollection;
 import local.radioschedulers.alg.ga.watchmaker.SortedCollection.MappingFunction;
 
 import org.apache.log4j.Logger;
 
 public class ParallelListingScheduler extends
 		TrivialChoiceFirstListingScheduler {
 	private static Logger log = Logger
 			.getLogger(ParallelListingScheduler.class);
 
 	private JobSortCriterion sortFunction;
 
 	public ParallelListingScheduler(JobSortCriterion sortFunction) {
 		setSortFunction(sortFunction);
 	}
 
 	protected void setSortFunction(JobSortCriterion sortFunction) {
 		this.sortFunction = sortFunction;
 		this.sortFunction.setPossibleSlots(possibleSlots);
 	}
 
	/**
	 * how many hours are left for this job
	 */
	protected HashMap<Job, Double> timeleft = new HashMap<Job, Double>();

 	@Override
 	protected Schedule doSchedule(ScheduleSpace timeline, Schedule s) {
 		log.debug("sorting jobs");
 		SortedCollection<Job> jobsSorted = new SortedCollection<Job>(
 				possibleSlots.keySet(), getSortCriterion());
 
 		log.debug("assigning job order in schedulespace based on pressure");
 
 		for (Job j : jobsSorted) {
 			log.debug("placing " + j);
 			placeJob(timeline, s, j);
 		}
 
 		return s;
 	}
 
 	protected void placeJob(ScheduleSpace timeline, Schedule s, Job j) {
 		Double timeleftj = timeleft.get(j);
 		log.debug("need to find an additional "
 				+ (timeleftj * Schedule.LST_SLOTS_PER_HOUR) + " slots");
 		log.debug("have npossibles = " + possibleSlots.get(j).size());
 		for (LSTTime t : possibleSlots.get(j)) {
 			// check slot e
 			Set<JobCombination> jcs = timeline.get(t);
 			JobCombination jc = s.get(t);
 
 			if (jc != null && jc.jobs.contains(j)) {
 				// already in there, already selected
 				continue;
 			}
 
 			// find something that contains j and everything in jc
 			JobCombination jc2 = findSupersetJobCombination(j, jc, jcs);
 			if (jc2 != null) {
 				timeleftj -= 1. / Schedule.LST_SLOTS_PER_HOUR;
 				s.clear(t);
 				s.add(t, jc2);
 				if (timeleftj <= 0) {
 					log.debug("placing " + j + " was successful");
 					break;
 				}
 			}
 		}
 		if (timeleftj > 0) {
 			log.debug("placing " + j + " is incomplete.");
 		}
 	}
 
 	private JobCombination findSupersetJobCombination(Job j, JobCombination jc,
 			Set<JobCombination> jcs) {
 
 		for (JobCombination jc2 : jcs) {
 			if (jc2.jobs.size() != 1 + (jc == null ? 0 : jc.jobs.size()))
 				continue;
 			if (jc2.jobs.contains(j)) {
 				boolean containsall = true;
 				if (jc != null) {
 					for (Job j2 : jc.jobs) {
 						if (!jc2.jobs.contains(j2)) {
 							containsall = false;
 							break;
 						}
 					}
 				}
 				if (containsall) {
 					return jc2;
 				}
 			}
 		}
 		return null;
 	}
 
 	protected MappingFunction<Job, Double> getSortCriterion() {
 		return sortFunction.getSortFunction();
 	}
 
 	@Override
 	public String toString() {
 		return getClass().getSimpleName() + " with jobSortFunction "
 				+ sortFunction + " instance " + hashCode();
 	}
 
 }
