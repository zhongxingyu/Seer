 package ca.ubc.ctlt.copyalerts.systemconfig.indexer;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Date;
 
 import org.quartz.DateBuilder.IntervalUnit;
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
 
 import static org.quartz.JobBuilder.*;
 import static org.quartz.TriggerBuilder.*;
 import static org.quartz.DateBuilder.*;
 import static org.quartz.impl.matchers.GroupMatcher.*;
 
 import blackboard.cms.filesystem.CSContext;
 import blackboard.cms.filesystem.CSEntry;
 import blackboard.cms.filesystem.CSFile;
 import blackboard.db.ConnectionNotAvailableException;
 import blackboard.persist.PersistenceException;
 import blackboard.platform.vxi.service.VirtualSystemException;
 
 import ca.ubc.ctlt.copyalerts.SavedConfiguration;
 import ca.ubc.ctlt.copyalerts.db.FilesTable;
 import ca.ubc.ctlt.copyalerts.db.HostsTable;
 import ca.ubc.ctlt.copyalerts.db.InaccessibleDbException;
 import ca.ubc.ctlt.copyalerts.db.QueueTable;
 
 public class CSIndexJob implements InterruptableJob, TriggerListener
 {
 	// Meant to keep two threads from running indexing at the same time, need to be static as it's shared between all instances
 	public static Boolean executing = false; // an intrinsic lock using Java's synchronized statement
 	
 	// Execute will check this variable periodically. If true, it'll immediately stop execution.
 	public Boolean stop = false;
 	
 	// interrupt() will not return until it's sure execute got the stop message. We indicate that execute has received the stop message with this.
 	public Boolean interruptProcessed = false;
 	
 	public CSIndexJob() throws ConnectionNotAvailableException
 	{
 	}
 
 	@Override
 	public void execute(JobExecutionContext context) throws JobExecutionException
 	{
 		String id = context.getFireTime().toString();
 		System.out.println("ubc.ctlt.copyalerts Start");
 
 		// only one thread should ever be indexing at the same time
 		synchronized(executing)
 		{
 			if (executing)
 			{
 				System.out.println(id + " stopping, already executing.");
 				return;
 			}
 			else
 			{
 				System.out.println(id + " proceeding, no previous execution.");
 				executing = true;
 			}
 		}
 		
 		// only 1 of the servers should be running indexing, check if this is us
 		String hostname = context.getJobDetail().getJobDataMap().getString("hostname");
 		HostsTable ht;
 		try
 		{
 			ht = new HostsTable();
 			if (!ht.getLeader().equals(hostname))
 			{
 				System.out.println("We're not the one supposed to be executing.");
 				synchronized (executing)
 				{
 					executing = false;
 				}
 				return;
 			}
 		} catch (VirtualSystemException e1)
 		{
 			e1.printStackTrace();
 			throw new JobExecutionException(e1);
 		} catch (InaccessibleDbException e)
 		{
 			e.printStackTrace();
 			throw new JobExecutionException(e);
 		}
 
 		// Implement execution time limit (if needed)
 		// Basically, we'll have a trigger that'll fire after the time limit has passed. We use the CSIndexJob object as a trigger listener
 		// and will trigger an interrupt when the time has passed.
 		SavedConfiguration config = new SavedConfiguration();
 		Timestamp started = new Timestamp((new Date()).getTime());
 		try
 		{
 			// Save the fact that we've started running
 			ht.setRunStats(hostname, HostsTable.STATUS_RUNNING, started, new Timestamp(0));
 		} catch (InaccessibleDbException e)
 		{
 			e.printStackTrace();
 			throw new JobExecutionException(e);
 		}
 		Scheduler sched = context.getScheduler();
 		JobDetail noopJob = newJob(NoOpJob.class).withIdentity("CSIndexJobNoOp", "CSIndexJobGroup").build();
 		Trigger trigger;
 		if (config.isLimited())
 		{
 			System.out.println("Limit enabled");
 			int minutes = (config.getHours() * 60) + config.getMinutes();
 			System.out.println("Limit at: " + minutes);
 			trigger = newTrigger()
 					.withIdentity("CSIndexJobStopTrigger", "CSIndexJobGroup")
 					.startAt(futureDate(minutes, IntervalUnit.MINUTE))
 					.build();
 			try
 			{
 				sched.scheduleJob(noopJob, trigger);
 				sched.getListenerManager().addTriggerListener(this, triggerGroupEquals("CSIndexJobGroup"));
 			} catch (SchedulerException e)
 			{
 				System.out.println("Unable to schedule job or add limit listener.");
 				throw new JobExecutionException(e);
 			}
 		}
 		
 		// run indexing
 		boolean limitReached = false;
 		try
 		{
 			boolean ret = indexer(config);
 			if (ret)
 			{
 				Timestamp ended = new Timestamp((new Date()).getTime());
 				System.out.println("Finished by time limit");
 				ht.setRunStats(hostname, HostsTable.STATUS_LIMIT, started, ended);
 				limitReached = true;
 			}
 		} catch (JobExecutionException e)
 		{
 			System.out.println("Indexer failed during execution.");
 			Timestamp ended = new Timestamp((new Date()).getTime());
 			try
 			{
 				ht.setRunStats(hostname, HostsTable.STATUS_ERROR, started, ended);
 			} catch (InaccessibleDbException e1)
 			{
 				System.out.println("Catastrophic error, unable to even save error notification.");
 			}
 			throw e;
 		} catch (InaccessibleDbException e)
 		{
 			System.out.println("Indexer could not access database.");
 			e.printStackTrace();
 			throw new JobExecutionException(e);
 		} catch(Exception e)
 		{
 			System.out.println("Indexer threw unknown exception.");
 			e.printStackTrace();
 		}
 		
 		// Remove execution time limit now that we're done
 		if (config.isLimited())
 		{
 			try
 			{
 				System.out.println("Removing limit trigger");
 				sched.deleteJob(noopJob.getKey());
 				//sched.getListenerManager().removeTriggerListener(getName());
 			} catch (SchedulerException e)
 			{
 				System.out.println("Unable to remove limit trigger listener.");
 				e.printStackTrace();
 			}
 		}
 
 		// Save the fact that we've finished running only if we didn't finish by time limit
 		if (!limitReached)
 		{
 			Timestamp ended = new Timestamp((new Date()).getTime());
 			try
 			{
 				ht.setRunStats(hostname, HostsTable.STATUS_STOPPED, started, ended);
 			} catch (InaccessibleDbException e)
 			{
 				System.out.println("Unable to save run stats.");
 				e.printStackTrace();
 			}
 		}
 		System.out.println("ubc.ctlt.copyalerts Done");
 	
 		// let others execute now
 		synchronized (executing)
 		{
 			executing = false;
 		}
 
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
 		System.out.println("Queue Generation Start");
 		
 		ArrayList<String> paths = new ArrayList<String>();
 		try
 		{
 			queue = new QueueTable();
 
 			paths = queue.load();
 			if (paths.isEmpty())
 			{
 				QueueGenerator generator = new QueueGenerator();
 				// put files into the queue, 500 at a time, making sure to check if we need to stop
 				while (generator.hasNext())
 				{
 					paths = generator.next();
 					queue.add(paths);
 					if (syncStop())
 					{
 						return true;
 					}
 				}
 				// now we can process the paths from the start
 				paths = queue.load();
 			}
 		} catch (InaccessibleDbException e1)
 		{
 			System.out.println("Could not access database, stopping index job.");
 			throw new JobExecutionException(e1);
 		} catch (PersistenceException e)
 		{
 			System.out.println("Persistence Exception.");
 			throw new JobExecutionException(e);
 		}
 		// make sure not to execute next part if we're supposed to halt
 		System.out.println("Queue Generation Done");
 		if (syncStop())
 		{
 			return true;
 		}
 		// part 2, go through the queue and check each file's metadata
 		System.out.println("Check Metadata Start");
 		IndexGenerator indexGen = new IndexGenerator(config.getAttributes());
 		// clear the database
 		try
 		{
 			FilesTable.deleteAll();
 		} catch (InaccessibleDbException e)
 		{
 			System.out.println("Could not reset the database.");
 			throw new JobExecutionException(e);
 		}
 		while (!paths.isEmpty())
 		{
 			System.out.println("Copyright Alerts Indexing: " + paths.get(0));
 			for (String p : paths)
 			{
 				CSContext ctx = CSContext.getContext();
 				// Give ourself permission to do anything in the Content Collections.
 				// Must do this cause we don't have a real request contest that many of the CS API calls
 				// require when you're not a superuser.
 				ctx.isSuperUser(true);
 				// Retrieve file entry
 				CSEntry entry = ctx.findEntry(p);
 				if (entry == null)
 				{
 					System.out.println("Could not find: " + p);
 					continue;
 				}
 				CSFile file = (CSFile) entry;
 				// Retrieve metadata
 				try
 				{
 					indexGen.process(file);
 				} catch (PersistenceException e)
 				{
 					System.out.println("Could not access BB database, stopping index job.");
 					throw new JobExecutionException(e);
 				} catch (InaccessibleDbException e)
 				{
 					System.out.println("Could not access database, stopping index job.");
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
 				System.out.println("Could not access database, stopping index job.");
 				throw new JobExecutionException(e);
 			}
 			if (syncStop())
 			{
 				return true;
 			}
 		}
 		System.out.println("Check Metadata Done");
 		return false;
 	}
 	
 	@Override
 	public void interrupt() throws UnableToInterruptJobException
 	{
 		System.out.println("In interrupt");
 
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
 			System.out.println("Unable to self interrupt.");
 			e.printStackTrace();
 		}
 		
 	}
 	
 	private boolean syncStop()
 	{
 		synchronized (stop)
 		{
 			if (stop)
 			{
 				System.out.println("Execute stopping");
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
