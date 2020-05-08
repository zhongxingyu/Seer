 package ca.ubc.ctlt.copyalerts.indexer;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.quartz.DateBuilder.IntervalUnit;
 import org.quartz.DisallowConcurrentExecution;
 import org.quartz.InterruptableJob;
 import org.quartz.JobDetail;
 import org.quartz.JobExecutionContext;
 import org.quartz.JobExecutionException;
 import org.quartz.Scheduler;
 import org.quartz.SchedulerException;
 import org.quartz.Trigger;
 import org.quartz.Trigger.CompletedExecutionInstruction;
 import org.quartz.TriggerListener;
 import org.quartz.UnableToInterruptJobException;
 import org.quartz.jobs.NoOpJob;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import static org.quartz.JobBuilder.*;
 import static org.quartz.TriggerBuilder.*;
 import static org.quartz.DateBuilder.*;
 import static org.quartz.impl.matchers.GroupMatcher.*;
 
 import blackboard.cms.filesystem.CSContext;
 import blackboard.cms.filesystem.CSEntry;
 import blackboard.cms.filesystem.CSFile;
 import blackboard.db.ConnectionManager;
 import blackboard.db.ConnectionNotAvailableException;
 import blackboard.persist.PersistenceException;
 import blackboard.platform.vxi.service.VirtualSystemException;
 
 import ca.ubc.ctlt.copyalerts.configuration.HostResolver;
 import ca.ubc.ctlt.copyalerts.configuration.SavedConfiguration;
 import ca.ubc.ctlt.copyalerts.db.DbInit;
 import ca.ubc.ctlt.copyalerts.db.FilesTable;
 import ca.ubc.ctlt.copyalerts.db.HostsTable;
 import ca.ubc.ctlt.copyalerts.db.InaccessibleDbException;
 import ca.ubc.ctlt.copyalerts.db.QueueTable;
 
 @DisallowConcurrentExecution
 public class CSIndexJob implements InterruptableJob, TriggerListener
 {
 	private final static Logger logger = LoggerFactory.getLogger(CSIndexJob.class);
 	
 	private final static String JOBGROUP = "CSIndexJobGroup";
 
 	// Execute will check this variable periodically. If true, it'll immediately stop execution.
 	public Boolean stop = false;
 	// this job does nothing, but we use the trigger to tell us if the job's time limit has been reached
 	private JobDetail noopJob = newJob(NoOpJob.class).withIdentity("CSIndexJobNoOp", JOBGROUP).build();
 	
 	// needed to be made class vars for use by cleanUpOnException
 	private HostsTable ht = null;
 	private JobExecutionContext context = null;
 	private Timestamp started = new Timestamp(0);
 	private Timestamp ended = new Timestamp(0);
 	private String hostname = HostResolver.getHostname();
 	
 	public CSIndexJob() throws ConnectionNotAvailableException
 	{
 	}
 
 	@Override
 	public void execute(JobExecutionContext context) throws JobExecutionException
 	{
 		logger.info("Indexing Start");
 		
 		this.context = context;
 
 		// only 1 of the servers should be running indexing, check if this is us
 		try
 		{
 			ht = new HostsTable();
 			if (!ht.getLeader().equals(hostname))
 			{
 				logger.info("We're not selected as the alert generation host, stopping.");
 				return;
 			}
 		} catch (VirtualSystemException e)
 		{
 			throw cleanUpOnException(e);
 		} catch (InaccessibleDbException e)
 		{
 			throw cleanUpOnException(e);
 		}
 
 		// Implement execution time limit (if needed)
 		// Basically, we'll have a trigger that'll fire after the time limit has passed. We use the CSIndexJob object as a trigger listener
 		// and will trigger an interrupt when the time has passed.
 		SavedConfiguration config;
 		started = new Timestamp((new Date()).getTime());
 		try
 		{
 			// Save the fact that we've started running
 			ht.saveRunStats(hostname, HostsTable.STATUS_RUNNING, started, ended);
 			// load configuration
 			config = SavedConfiguration.getInstance();
 		} catch (InaccessibleDbException e)
 		{
 			throw cleanUpOnException(e);
 		}
 		Scheduler sched = context.getScheduler();
 		Trigger trigger;
 		if (config.isLimited())
 		{
 			int minutes = (config.getHours() * 60) + config.getMinutes();
 			logger.info("Limit execution to " + minutes + " minutes.");
 			trigger = newTrigger()
 					.withIdentity("CSIndexJobStopTrigger", JOBGROUP)
 					.startAt(futureDate(minutes, IntervalUnit.MINUTE))
 					.build();
 			try
 			{
 				sched.scheduleJob(noopJob, trigger);
 				sched.getListenerManager().addTriggerListener(this, triggerGroupEquals(JOBGROUP));
 			} catch (SchedulerException e)
 			{
 				throw cleanUpOnException(e);
 			}
 		}
 		
 		// run indexing
 		boolean limitReached = false;
 		try
 		{
 			boolean ret = indexer(config);
 			if (ret)
 			{
 				ended = new Timestamp((new Date()).getTime());
 				logger.debug("Finished by time limit");
 				ht.saveRunStats(hostname, HostsTable.STATUS_LIMIT, started, ended);
 				limitReached = true;
 			}
 		} catch (JobExecutionException e)
 		{
 			logger.error("Indexer failed during execution.");
 			throw cleanUpOnException(e);
 		} catch (InaccessibleDbException e)
 		{
 			logger.error("Indexer could not access database.");
 			throw cleanUpOnException(e);
 		} catch(Exception e)
 		{
 			logger.error("Indexer threw unexpected exception.");
 			throw cleanUpOnException(e);
 		}
 		
 		// Remove execution time limit now that we're done
 		if (config.isLimited())
 		{
 			try
 			{
 				logger.debug("Removing limit trigger");
 				sched.deleteJob(noopJob.getKey());
 			} catch (SchedulerException e)
 			{
 				logger.warn("Unable to remove limit trigger listener.", e);
 			}
 		}
 
 		// Save the fact that we've finished running only if we didn't finish by time limit
 		if (!limitReached)
 		{
 			Timestamp ended = new Timestamp((new Date()).getTime());
 			try
 			{
 				ht.saveRunStats(hostname, HostsTable.STATUS_STOPPED, started, ended);
 			} catch (InaccessibleDbException e)
 			{
 				logger.warn("Unable to save run stats.", e);
 			}
 		}
 		logger.info("ubc.ctlt.copyalerts Done");
 	}
 	
 	/**
 	 * Need to remove the limit trigger if we've set it and need to update status with error
 	 * @param e
 	 * @throws JobExecutionException
 	 */
 	private JobExecutionException cleanUpOnException(Exception e)
 	{
		// Must log first, or additional exceptions during the rest of the cleanup can hide the original exception
		logger.error(e.getMessage(), e);

		// Remove the execution time limit, if any, that were placed on indexing
 		Scheduler sched = context.getScheduler();
 		try
 		{
 			if (sched.checkExists(noopJob.getKey()))
 			{
 				sched.deleteJob(noopJob.getKey());
 			}
 		} catch (SchedulerException e1)
 		{
 			logger.error("Exception clean up failed, unable to remove time limit trigger.");
 		}
 
		// Update the execution status
 		try
 		{
			if (ht != null)
			{
				ht.saveRunStats(hostname, HostsTable.STATUS_ERROR, started, ended);
			}
			else
			{
				logger.error("Exception clean up occured before hosts table was read.");
			}
 		} catch (InaccessibleDbException e1)
 		{
 			logger.error("Exception clean up failed, unable to update execution status.");
 		}
 		return new JobExecutionException(e);
 	}
 	
 	/**
 	 * The actual indexing operation
 	 * @param config
 	 * @return true if stopped by time limit, false otherwise
 	 * @throws JobExecutionException
 	 */
 	private boolean indexer(SavedConfiguration config) throws JobExecutionException
 	{
 		// run actual job
 		// part 1, try generating the queue
 		QueueTable queue;
 		logger.info("Queue Generation Start");
 		
 		ArrayList<String> paths = new ArrayList<String>();
 		// we're either going to continue processing a previously generated queue or have to generate a new queue entirely.
 		// assume that we're continuing processing a previously generated queue for now
 		boolean newQueue = false;
 		try
 		{
 			queue = new QueueTable();
 
 			paths = queue.load();
 			if (paths.isEmpty())
 			{
 				// load data into the queue table
 				if (generateQueue(queue))
 				{
 					return true;
 				}
 				// now we can process the paths from the start
 				paths = queue.load();
 				// we just generated a new queue, so definitely not loading leftovers from the last run
 				newQueue = true;
 				logger.debug("Generated new queue.");
 			}
 			else
 			{
 				logger.debug("Continue with previously generated queue.");
 			}
 		} catch (InaccessibleDbException e1)
 		{
 			logger.error("Could not access database, stopping index job.", e1);
 			throw new JobExecutionException(e1);
 		}
 		// make sure not to execute next part if we're supposed to halt
 		logger.info("Queue Generation Done");
 		if (syncStop())
 		{
 			return true;
 		}
 		// part 2, go through the queue and check each file's metadata
 		logger.info("Check Metadata Start");
 		IndexGenerator indexGen;
 		try
 		{
 			indexGen = new IndexGenerator(config.getAttributes());
 		} catch (PersistenceException e)
 		{
 			logger.error("Could not get metadata template attributes.", e);
 			throw new JobExecutionException(e);
 		}
 		// clear the database
 		try
 		{
 			// only clear the database if it's a new run
 			// and we're not finishing up the last run
 			if (newQueue)
 			{
 				logger.debug("Removing all previous file records.");
 				FilesTable ft = new FilesTable();
 				ft.deleteAll();
 			}
 		} catch (InaccessibleDbException e)
 		{
 			logger.error("Could not reset the database.", e);
 			throw new JobExecutionException(e);
 		}
 		while (!paths.isEmpty())
 		{
 			logger.debug("Copyright Alerts Indexing: " + paths.get(0));
 			for (String p : paths)
 			{
 				CSContext ctx = CSContext.getContext();
 				// Give ourself permission to do anything in the Content Collections.
 				// Must do this cause we don't have a real request context that many of the CS API calls
 				// require when you're not a superuser.
 				ctx.isSuperUser(true);
 				// Retrieve file entry
 				CSEntry entry = ctx.findEntry(p);
 				if (entry == null)
 				{
 					logger.warn("A non-existent file somehow made it onto the queue.");
 					logger.debug("\tRecorded Path: " + p);
 					continue;
 				}
 				if (!(entry instanceof CSFile))
 				{
 					logger.info("Skipping directory.");
 					logger.info("\tRecorded Path: " + p);
 					continue;
 				}
 				CSFile file = (CSFile) entry;
 				// Retrieve metadata
 				try
 				{
 					indexGen.process(file);
 				} catch (PersistenceException e)
 				{
 					logger.error("Could not access BB database, stopping index job.", e);
 					throw new JobExecutionException(e);
 				} catch (InaccessibleDbException e)
 				{
 					logger.error("Could not access database, stopping index job.", e);
 					throw new JobExecutionException(e);
 				}
 			}
 			
 
 			// load next batch of files
 			try
 			{
 				queue.pop();
 				paths = queue.load();
 			} catch (InaccessibleDbException e)
 			{
 				logger.error("Could not access database, stopping index job.", e);
 				throw new JobExecutionException(e);
 			}
 			if (syncStop())
 			{
 				return true;
 			}
 		}
 		logger.info("Check Metadata Done");
 		return false;
 	}
 	
 	/**
 	 * Generate the list of files to check for indexing.
 	 * Using the API to iterate through the content system turns out to be too memory consuming
 	 * so let's try reading the database directly.
 	 * @return
 	 * @throws JobExecutionException 
 	 */
 	private boolean generateQueue(QueueTable queue) throws JobExecutionException
 	{
 		logger.debug("New Queue Generation method");
 		ConnectionManager cm = DbInit.getConnectionManager();
 		Connection conn = null;
 
 		try
 		{
 			conn = cm.getConnection();
 			logger.debug("Enabling cursor usage in resultset");
 			conn.setAutoCommit(false); // need to disable autocommit to enable
 										// fetching only a small number of rows
 										// at once
 										// necessary to keep memory usage low
 			String query = "SELECT full_path FROM bblearn_cms_doc.xyf_urls";
 			PreparedStatement queryCompiled = conn.prepareStatement(query);
 			queryCompiled.setFetchSize(50); // we'll only fetch 50 rows at a
 											// time
 			ResultSet res = queryCompiled.executeQuery();
 			logger.debug("Going through the data");
 			// single regex to check the filepath to make sure that we're only
 			// picking up course files
 			// the general format for course files is: /courses/course name/file
 			Pattern courseFilePattern = Pattern.compile("^/courses/[^/]+?/.+$");
 			Matcher courseFileMatcher = courseFilePattern.matcher("");
 			// stores a batch of file paths to be put into the queue
 			ArrayList<String> paths = new ArrayList<String>();
 			long count = 0;
 			int batchSize = 500;
 			while (res.next())
 			{
 				String path = res.getString(1);
 				// make sure that we only have course files and no xid- files
 				// it seems that xid- files are not listed in the xyf_urls table, 
 				// but better be safe with an explicit check
 				if (courseFileMatcher.reset(path).matches() &&
 					!path.contains("xid-"))
 				{
 					paths.add(path);
 				}
 				// store the current batch into the queue when we've got enough
 				if (paths.size() >= batchSize)
 				{
 					logger.debug("Added to queue: " + path);
 					queue.add(paths);
 					paths.clear(); // empty the current batch now that they're safely stored
 				}
 				// only check for interrupt if we've processed batchSize entries already
 				if (count >= batchSize) 
 				{ // execution interrupt was requested
 					if (syncStop())
 					{
 						break;
 					}
 					count = 0;
 				}
 				count++;
 			}
 			res.close();
 			queryCompiled.close();
 		} catch (SQLException e)
 		{
 			logger.error(e.getMessage(), e);
 			throw new JobExecutionException(e);
 		} catch (ConnectionNotAvailableException e)
 		{
 			logger.error(e.getMessage(), e);
 			throw new JobExecutionException(e);
 		} catch (InaccessibleDbException e)
 		{
 			logger.error("Could not access database, stopping index job.", e);
 			throw new JobExecutionException(e);
 		} finally
 		{
 			if (conn != null)
 				cm.releaseConnection(conn);
 		}
 		if (syncStop()) return true;
 		return false;
 	}
 	
 	@Override
 	public void interrupt() throws UnableToInterruptJobException
 	{
 		logger.debug("Index job interrupt.");
 
 		// inform execute that it should stop now
 		synchronized (stop)
 		{
 			stop = true;
 		}
 	}
 
 	/**
 	 * For implementing execution time limits, interrupt myself when time is up.
 	 */
 	@Override
 	public void triggerFired(Trigger arg0, JobExecutionContext arg1)
 	{
 		try
 		{
 			interrupt();
 		} catch (UnableToInterruptJobException e)
 		{
 			logger.error("Unable to self interrupt.", e);
 		}
 		
 	}
 	
 	private boolean syncStop()
 	{
 		synchronized (stop)
 		{
 			if (stop)
 			{
 				logger.info("Indexing stopping");
 				return true;
 			}
 		}
 		return false;
 	}
 
 	// trigger required methods that I don't care about
 	@Override
 	public String getName()
 	{
 		// TODO Auto-generated method stub
 		return "CSIndexJobListener";
 	}
 	@Override
 	public void triggerComplete(Trigger arg0, JobExecutionContext arg1, CompletedExecutionInstruction arg2)
 	{
 	}
 	@Override
 	public void triggerMisfired(Trigger arg0)
 	{
 	}
 	@Override
 	public boolean vetoJobExecution(Trigger arg0, JobExecutionContext arg1)
 	{
 		return false;
 	}
 
 }
