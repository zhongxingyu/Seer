 package com.widen.profiler;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.widen.profiler.services.JobIdentifier;
 
 public class ProfilerThread implements Runnable
 {
 	private final Logger log = LoggerFactory.getLogger(ProfilerThread.class);
 
 	public static final String THREAD_NAME = "Profiler";
 
 	private final JobIdentifier jobIdentifier;
 	private final int pollingInterval;
 	private final int maxThreadRuntime;
 	private final String[] ignoredStrings;
 
 	private Map<String, Integer> overallCount;
 	private Map<String, Map<String, Integer>> pageCount;
 	private Map<String, Map<String, Integer>> jobCount;
 
 	private boolean ignoreJobs;
 	private boolean stop = false;
 
 	public ProfilerThread(JobIdentifier jobIdentifier, int pollingInterval, int maxThreadRuntime, String ignoredStrings, boolean ignoreJobs)
 	{
 		this.jobIdentifier = jobIdentifier;
 		this.pollingInterval = pollingInterval;
 		this.maxThreadRuntime = maxThreadRuntime;
 		this.ignoredStrings = StringUtils.split(ignoredStrings, " ");
 		this.ignoreJobs = ignoreJobs;
 	}
 
 	public void run()
 	{
 		overallCount = new HashMap<String, Integer>();
 		pageCount = new HashMap<String, Map<String, Integer>>();
 		jobCount = new HashMap<String, Map<String, Integer>>();
 
 		long startTime = System.currentTimeMillis();
 
 		while (!stop)
 		{
 			Map<Thread,StackTraceElement[]> allTraces = Thread.getAllStackTraces();
 
 			for (Map.Entry<Thread,StackTraceElement[]> entry: allTraces.entrySet())
 			{
 				String pageName = null;
 				String jobName = null;
 
 				Map<String, Integer> classCountForThread = new HashMap<String, Integer>();
 				for (StackTraceElement stackElement : entry.getValue())
 				{
 					Class clazz = getStackElementClass(stackElement);
 					String className = clazz == null ? stackElement.getClassName() : clazz.getName();
 
 					if (!containsBoringString(className))
 					{
 						String key = className + "." + stackElement.getMethodName() + "() line " + stackElement.getLineNumber();
 
 						if (!classCountForThread.containsKey(key))
 						{
 							classCountForThread.put(key, 0);
 						}
 						classCountForThread.put(key, classCountForThread.get(key) + 1);
 					}
 					if (clazz != null && StringUtils.containsIgnoreCase(className, ".pages."))
 					{
 						pageName = clazz.getSimpleName();
 					}
 					if (clazz != null && jobIdentifier.isJob(clazz))
 					{
 						jobName = clazz.getSimpleName();
 					}
 				}
 				if (pageName != null)
 				{
 					if (!pageCount.containsKey(pageName))
 					{
 						pageCount.put(pageName, new HashMap<String, Integer>());
 					}
 					mergeCounts(pageCount.get(pageName), classCountForThread);
 				}
 				if (jobName != null && !ignoreJobs)
 				{
 					if (!jobCount.containsKey(jobName))
 					{
 						jobCount.put(jobName, new HashMap<String, Integer>());
 					}
 					mergeCounts(jobCount.get(jobName), classCountForThread);
 				}
 				if (!ignoreJobs || jobName == null)
 				{
 					mergeCounts(overallCount, classCountForThread);
 				}
 			}
 
 			long curTime = System.currentTimeMillis();
 			if (curTime - startTime >= (maxThreadRuntime * 60 * 1000))
 			{
 				log.info("Profiler thread has been running for the maximum of {} minutes, quitting", maxThreadRuntime);
 				stop = true;
 			}
 
 			try
 			{
 				Thread.sleep(pollingInterval);
 			}
 			catch (InterruptedException e) {}
 		}
 	}
 
 	private boolean containsBoringString(String className)
 	{
 		for (String ignoreString : ignoredStrings)
 		{
 			if (StringUtils.containsIgnoreCase(className, ignoreString))
 			{
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private Class getStackElementClass(StackTraceElement element)
 	{
 		try
 		{
 			return Class.forName(element.getClassName());
 		}
 		catch (ClassNotFoundException e)
 		{
 			return null;
 		}
 	}
 
 	private void mergeCounts(Map<String, Integer> aggregateCount, Map<String, Integer> countToAdd)
 	{
		int previous = 0;

 		for (Map.Entry<String, Integer> entry : countToAdd.entrySet())
 		{
			if (!aggregateCount.containsKey(entry.getKey()))
 			{
				previous = aggregateCount.get(entry.getKey());
 			}

			aggregateCount.put(entry.getKey(), previous + entry.getValue());
 		}
 	}
 
 	public void stop()
 	{
 		stop = true;
 	}
 
 	public Map<String, Integer> getOverallCount()
 	{
 		return overallCount;
 	}
 
 	public Map<String, Map<String, Integer>> getPageCount()
 	{
 		return pageCount;
 	}
 
 	public Map<String, Map<String, Integer>> getJobCount()
 	{
 		return jobCount;
 	}
 }
