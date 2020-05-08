 package nl.tudelft.cloud_computing_project;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.PriorityQueue;
 import java.util.Set;
 import java.util.TreeSet;
 
 import nl.tudelft.cloud_computing_project.model.*;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.sql2o.Connection;
 import org.sql2o.Query;
 import org.sql2o.Sql2o;
 import org.sql2o.data.Row;
 import org.sql2o.data.Table;
 
 public class Scheduler {
 	private static final Logger LOG = LoggerFactory.getLogger(Scheduler.class);
 	
 	// TODO: get only x*n jobs
 	private final String jobqueue_sql = 
 		"SELECT id, filesize" +
 		" FROM Job " +
 		" WHERE jobstatus = " + Job.JobStatus.SUBMITTED.code +
 		" AND id NOT IN (SELECT job_id FROM Assignment)" +
 		" ORDER BY priority, submission_time";
 	
 	private final String worker_load_sql =
 			  "SELECT worker_instanceid AS instance_id, COALESCE(MAX(Assignment.order), 0) AS maxorder, SUM(COALESCE(Job.filesize, 0)) AS worker_load"
 			+ " FROM Assignment"
 			+ " JOIN Job On Job.id = Assignment.job_id"
 			+ " GROUP BY Assignment.worker_instanceid";
 
 	private final String assign_sql = "INSERT INTO Assignment(job_id, worker_instanceid, `order`) " +
 						"VALUES (:job_id, :worker_instanceid, :order)";
 	
 	private Sql2o sql2o;
 	// Jobs in the order they should be assigned to workers
 	private Iterable<Job> jobs;
 	
 	// Worker information
 	public class Worker {
 		public String instance_id;
 		public long load;
 		public int maxorder;
 		
 		public Worker() {}
 		
 		public Worker(final String instance_id) {
 			this.instance_id = instance_id;
 		}
 		
 		public Worker(final String instance_id, long load, int maxorder) {
 			this.instance_id = instance_id;
 			this.load = load;
 			this.maxorder = maxorder;
 		}
 
 		@Override
 		public int hashCode() {
 			return instance_id.hashCode();
 		}
 
 		/* (non-Javadoc)
 		 * @see java.lang.Object#equals(java.lang.Object)
 		 */
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (!(obj instanceof Worker))
 				return false;
 			return instance_id.equals(((Worker) obj).instance_id);
 		}
 	}
 	private Set<Worker> workers;
 	
 	// TODO: Make this configurable
 	/**
 	 * Maximum load of a worker (in byte filesize of assigned jobs)
 	 */
	private boolean workerLoadAcceptable(long currentload) {
		return currentload < 5 * 1024 * 1024;
 	}
 	
 	/**
 	 * Get the current jobs to be scheduled
 	 */
 	private void pullJobQueue(){
 		// TODO: Add some limit or change method so it won't hog up all memory if the number of jobs is large. E.g. iterate ResultSet manually
 		// query database for the jobs order by priority and submission time.
 		jobs = sql2o.createQuery(jobqueue_sql, "jobqueue_sql").executeAndFetch(Job.class);		
 	}
 	
 	/**
 	 * Gets the information for the available workers
 	 */
 	private void pullWorkerInfo() {
 		// We could actually change the query to only fetch info of the workers we're intered in
 		// But this is only small overhead and much easier coding
 		//List<Worker> workersinfo = sql2o.createQuery(worker_load_sql, "workers_sql").addColumnMapping("worker_load", "load").executeAndFetch(Worker.class);
 		Table t = sql2o.createQuery(worker_load_sql, "workers_sql").executeAndFetchTable();
 		Map<String, Worker> workersinfomap = new HashMap<String, Worker>((int)(t.rows().size()*1.5));
 		for(Row r : t.rows()) {
 			workersinfomap.put(
 					r.getString("instance_id"),
 					new Worker(r.getString("instance_id"), r.getLong("worker_load"), r.getInteger("maxorder"))
 				);
 		}
		LOG.debug(String.format("Got info about %d workers that already have assignments", workersinfomap.size()));
 		
 		// Get the available workers
 		Set<String> availableWorkers = Monitor.getInstance().getAvailableInstancesId();
		LOG.debug(String.format("Got info about %d workers from the monitor", availableWorkers.size()));
 		workers = new HashSet<Worker>(availableWorkers.size());
 		for(String instanceid : availableWorkers) {
 			Worker w = workersinfomap.get(instanceid);
 			if(w == null) {
 				w = new Worker(instanceid);
 			}
 			workers.add(w);
 		}
		LOG.debug(String.format("We now have %d workers", workers.size()));
 	}
 	
 	/**
 	 * Put assignments into the database
 	 */
 	private void assignJobs(Iterable<Assignment> assignments) {
 		Connection c = sql2o.beginTransaction().setRollbackOnException(true);
 		try {
 			Query q = c.createQuery(assign_sql, "assign_sql");
 			for(Assignment a : assignments) {
 				q
 			    .addParameter("job_id", a.getJobId())
 			    .addParameter("worker_instanceid", a.getWorker())
 			    .addParameter("order", a.getOrder())
 			    .addToBatch();
 			}
 			q.executeBatch();
 		} catch(RuntimeException e) {
 			c.rollback();
 			LOG.error("Exception occured while putting assignments in database. Rolled back.", e);
 			throw e;
 		}
 		c.commit();		
 	}
 	
 	/**
 	 * Schedule with a greedy load-balancing algorithm
 	 * For example see http://www.cs.princeton.edu/courses/archive/spr05/cos423/lectures/11approx-alg.pdf
 	 * @return The assignments
 	 */
 	protected Collection<Assignment> schedule_greedy_lb() {
 		LOG.debug("Using greedy load-balancing algorithm");
 		
 		// Make a sorted datastructure that sorts the workers on their load
 		PriorityQueue<Worker> workers = new PriorityQueue<Worker>(this.workers.size()+1, new Comparator<Worker>() {
 			public int compare(Worker o1, Worker o2) {
 				return Long.compare(o1.load, o2.load);
 			}
 		});
 		
 		// Add all the workers to the queue
 		for(Worker w : this.workers) {
 			workers.add(w);
 		}
 		// Keep all the assignments in memory so we can commit them into the database in one go
 		Collection<Assignment> assignments = new ArrayList<Assignment>();
 		
 		// Go through all the jobs that can be assigned
 		for(Job j : jobs) {
 			Worker w = null;
 			// Get the preferred worker to assign to
 			while(!workers.isEmpty() && w == null) {
 				// Get preferred worker
 				w = workers.poll();
 				// Check if the current load is acceptable, otherwise keep removed from queue
				if(!workerLoadAcceptable(w.load)) {
 					w = null;
 				}
 			}
 			// Stop if no more workers are available
 			if(w == null) {
 				break;
 			}
 			
 			// Assign job to worker
 			// TODO: Fix that this doesn't work if maxorder is Integer.MAX_VALUE
 			assignments.add(new Assignment(w.instance_id, j.getId(), w.maxorder++));
			
			w.load += j.getFilesize();
			
			if(workerLoadAcceptable(w.load)) {
				workers.add(w);
			}
 		}
 		return assignments;
 	}
 	
 	/**
 	 * Assign jobs to workers
 	 */
 	public void schedule() {
 		LOG.info("Starting scheduler");
 		
 		sql2o = Database.getConnection();	
 		pullJobQueue();
 		pullWorkerInfo();
 		
 		Collection<Assignment> assignments = schedule_greedy_lb();
 		
 		assignJobs(assignments);
 		
 		LOG.info(String.format("Scheduled %d jobs", assignments.size()));
 	}
 	
 	public static void main(String[] args) {
 		Scheduler s = new Scheduler();
 		s.schedule();
 	}
 }
