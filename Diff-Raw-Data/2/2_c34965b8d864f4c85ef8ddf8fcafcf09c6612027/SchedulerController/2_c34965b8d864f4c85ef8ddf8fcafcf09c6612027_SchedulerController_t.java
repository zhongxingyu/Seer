 package com.gushuley.utils.scheduler;
 
 import java.util.*;
 
 import javax.management.ObjectInstance;
 
 
 
 import org.apache.log4j.Logger;
 
 import com.gushuley.utils.Tools;
 import com.gushuley.utils.jmx.*;
 import com.gushuley.utils.orm.*;
 import com.gushuley.utils.scheduler.dom.*;
 
 
 public class SchedulerController extends ThreadedService
 implements SchedulerControllerMBean 
 {	
 	String scheduler;
 	String dbScheme;
 	private String baseName;
 
 	private JobMBean startJob(JobDef jobDef, Date date) throws InstantiationException, IllegalAccessException, ClassNotFoundException, JmxException {
 		final JobMBean job =  (JobMBean) Thread.currentThread().getContextClassLoader().loadClass(jobDef.getClassName()).newInstance();
 		
 		job.setProperties(jobDef.getProperties());
 		job.addJobSuccesListener(new JobFinishListener() {
 			public void jobFinished(JobMBean job) {
 				log.debug("Job finished: " + job.getName());
 				SchedulerContext ctx = new SchedulerContext(getDatabaseJdni(), getScheduler(), getDbScheme());
 				try {
 					final Mapper2<JobDone, Integer, SchedulerContext> mapper = ctx.getMapper2(JobDone.class);
 					ctx.add(new JobDone(mapper.createKey(), new Date(), job.getName()));
 					ctx.commit();
 				} catch (Exception ex) { 
					log.debug("Error commiting of finalizing job: " + job.getName(), ex);
 				} finally {
 					ctx.close();
 				}
 				
 			}
 		});
 		final String jobName = baseName + ",jobId=" + jobDef.getKey();
 		log.debug("Starting job: " + jobName);
 
 		job.start(jobName, jobDef.getKey(), date);
 		return job;
 	}
 	
 	public void startJob(String jobId) throws Exception {
 		SchedulerContext ctx = new SchedulerContext(getDatabaseJdni(), getScheduler(), getDbScheme());
 		try {
 			JobDef def = ctx.find(JobDef.class, jobId);
 			if (def == null) {
 				throw new Exception("Job " + jobId + " not fond.");
 			}
 			else {
 				Calendar today = Calendar.getInstance();
 				today.setTime(new Date());
 				today.set(Calendar.HOUR_OF_DAY, 0);
 				today.set(Calendar.MINUTE, 0);
 				today.set(Calendar.SECOND, 0);
 				today.set(Calendar.MILLISECOND, 0);
 				startJob(def, today.getTime());
 			}
 			ctx.commit();
 		}
 		finally {
 			ctx.close();
 		}
 	}
 
 	public void stopJob(String jobId) throws JmxException {
 		JobMBean job = ServiceLocator.getObjectInterface(JobMBean.class, baseName + ",jobId=" + jobId);
 		if (job != null) {
 			job.stopWork();
 		}
 	}
 
 	public Map<String, JobMBean> getJobs() throws JmxException {
 		Collection<ObjectInstance> jobsNames = ServiceLocator.listNames(baseName);
 		Map<String, JobMBean> jobs = new HashMap<String, JobMBean>();
 		for (ObjectInstance i : jobsNames) {
 			jobs.put(i.getObjectName().toString(), ServiceLocator.getObjectInterface(JobMBean.class, i.getObjectName()));
 		}
 		return jobs;
 	}
 	
 	private String databaseJdni;
 
 	public String getDatabaseJdni() {
 		return databaseJdni;
 	}
 
 	public void setDatabaseJdni(String databaseJdni) {
 		this.databaseJdni = databaseJdni;
 	}
 
 	private Logger log = Logger.getLogger(getClass());
 
 	public String getLoggerName() {
 		return log.getName();
 	}
 
 	public void setLoggerName(String loggerName) {
 		log = Logger.getLogger(loggerName);
 	}
 
 	@Override
 	public void onRunStep() {
 		Calendar today = Calendar.getInstance();
 		today.setTime(new Date());
 		int nowTime = today.get(Calendar.HOUR_OF_DAY) * 100 + today.get(Calendar.MINUTE);
 		today.set(Calendar.HOUR_OF_DAY, 0);
 		today.set(Calendar.MINUTE, 0);
 		today.set(Calendar.SECOND, 0);
 		today.set(Calendar.MILLISECOND, 0);
 
 		SchedulerContext ctx = new SchedulerContext(getDatabaseJdni(), getScheduler(), getDbScheme());
 		try {
 			for (JobDef jobDef : ctx.getMapper2(JobDef.class).getAll()) {
 				String jobBeanName = getBaseName() + ",jobId=" + jobDef.getKey(); 
 				int desiredTime = jobDef.getStartHour() * 100 + jobDef.getStartMinute();
 
 				if ( nowTime >= desiredTime && (
 						(jobDef.getType() == JobDef.Type.Weekly && Tools.isBitSetted(jobDef.getDayMask(), today.get(Calendar.DAY_OF_WEEK)))
 						|| (jobDef.getType() == JobDef.Type.Monthly && Tools.isBitSetted(jobDef.getDayMask(), today.get(Calendar.DAY_OF_MONTH)))
 					))
 				{
 					JobMBean job = ServiceLocator.getObjectInterface(JobMBean.class, jobBeanName);			
 					if (job == null) {
 						Collection<JobDone> done = ctx.getMapper(JobDone.class, JobDone.Mapper.class).getJobDoneDayAndAfter(new Date(), jobDef.getKey());
 						if (done.isEmpty()) {
 							try {
 								job = startJob(jobDef, today.getTime());
 							} catch (Throwable e) {
 								log.error("", e);
 							}
 						}
 					}
 				}
 			}
 			ctx.commit();
 		} catch (ORMException e) {
 			log.error("", e);
 		} catch (JmxException e) {
 			log.error("", e);
 		}
 		finally{
 			ctx.close();
 		}
 	}
 
 	@Override
 	public void onStop() {
 		log.debug("Stopping jobs");
 		try {
 			for (JobMBean job : getJobs().values()) {
 				job.stopWork();
 			}
 		} catch (JmxException e) {
 			log.error("Error stopping jobs: ", e);
 		}			
 	}
 
 	@Override
 	public void onStart() {
 		setSleepTime(1000 * 60);
 	}	
 
 	public String getDbScheme() {
 		return dbScheme;
 	}
 	public void setDbScheme(String dbScheme) {
 		this.dbScheme = dbScheme;
 	}
 
 	public String getScheduler() {
 		return scheduler;
 	}
 	public void setScheduler(String scheduler) {
 		this.scheduler = scheduler;
 	}
 	
 	public String getBaseName() {
 		return baseName;
 	}
 	public void setBaseName(String name) {
 		baseName = name;
 	}
 }
