 package com.dgrid.transport;
 
 import java.security.SecureRandom;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.hibernate.Criteria;
 import org.hibernate.LockMode;
 import org.hibernate.Query;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Projections;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.orm.ObjectRetrievalFailureException;
 
 import com.dgrid.dao.GenericDAO;
 import com.dgrid.dao.ObjectQueryDAO;
 import com.dgrid.dao.model.HostSetting;
 import com.dgrid.dao.model.JobletLogEntry;
 import com.dgrid.dao.model.SystemSetting;
 import com.dgrid.errors.TransportException;
 import com.dgrid.gen.Constants;
 import com.dgrid.gen.Host;
 import com.dgrid.gen.InvalidApiKey;
 import com.dgrid.gen.InvalidHost;
 import com.dgrid.gen.InvalidJobId;
 import com.dgrid.gen.InvalidJobletId;
 import com.dgrid.gen.JOB_CALLBACK_TYPES;
 import com.dgrid.gen.JOB_STATUS;
 import com.dgrid.gen.Job;
 import com.dgrid.gen.Joblet;
 import com.dgrid.gen.JobletResult;
 import com.dgrid.gen.NoHostAvailable;
 import com.dgrid.gen.NoWorkAvailable;
 import com.dgrid.service.DGridSyncJobService;
 import com.dgrid.service.DGridTransport;
 import com.dgrid.util.ApiCallbackTypes;
 import com.dgrid.util.io.HostnameDiscovery;
 import com.facebook.thrift.TException;
 
 public class DGridHibernateTransport implements DGridTransport {
 	private Log log = LogFactory.getLog(getClass());
 
 	private GenericDAO dao;
 
 	private ObjectQueryDAO queryDAO;
 
 	private DGridSyncJobService syncJobService;
 
 	private DGridTransport self;
 
 	private String apiKey;
 
 	private static final String[] jobStatusTypes = new String[] { null,
 			"saved", "received", "queued", "processing", "completed", "failed" };
 
 	private Random random = new SecureRandom();
 
 	private String hostname;
 
 	public void setGenericDAO(GenericDAO dao) {
 		this.dao = dao;
 	}
 
 	public void setObjectQueryDAO(ObjectQueryDAO dao) {
 		this.queryDAO = dao;
 	}
 
 	public void setSyncJobService(DGridSyncJobService service) {
 		this.syncJobService = service;
 	}
 
 	public void setTransport(DGridTransport transport) {
 		this.self = transport;
 	}
 
 	public void setApiKey(String apiKey) {
 		this.apiKey = apiKey;
 	}
 
 	public void setEndpoint(String endpoint) {
 	}
 
 	public void setPort(int port) {
 	}
 
 	public void init() {
 		log.trace("init()");
 		this.hostname = HostnameDiscovery.getHostname();
 	}
 
 	public void completeJoblet(int jobletId, JobletResult result,
 			String logMessage) throws TransportException, InvalidApiKey,
 			InvalidJobletId {
 		log.trace("completeJoblet()");
 		Joblet joblet = readJoblet(jobletId);
 		joblet.setStatus(result.getStatus());
 		dao.update(joblet);
 		result.setJoblet(joblet);
 		result.setTimeCreated(System.currentTimeMillis());
 		dao.create(result);
 		if ((logMessage != null) && (logMessage.length() > 0)) {
 			JobletLogEntry log = new JobletLogEntry(0, joblet, logMessage);
 			dao.create(log);
 		}
 		// count joblets still in received/queued/processing
 		Criteria crit = queryDAO.createCriteria(Joblet.class);
 		crit.add(Restrictions.eq("jobId", joblet.getJobId()));
 		crit.add(Restrictions.or(
 				Restrictions.eq("status", JOB_STATUS.RECEIVED), Restrictions
 						.or(Restrictions.eq("status", JOB_STATUS.QUEUED),
 								Restrictions
 										.eq("status", JOB_STATUS.PROCESSING))));
 		crit.setProjection(Projections.rowCount());
 		int active = ((Integer) crit.list().get(0)).intValue();
 		if (active == 0) {
 			// update any saved joblets to received
 			Query query = queryDAO
 					.createQuery("update Joblet set status = ? where (jobId = ? and status = ?)");
 			query.setInteger(0, JOB_STATUS.RECEIVED);
 			query.setInteger(1, joblet.getJobId());
 			query.setInteger(2, JOB_STATUS.SAVED);
 			active = query.executeUpdate();
 		}
 		if (active == 0) {
 			// provision callback
 			Job job = (Job) dao.read(Job.class, joblet.getJobId());
 			// look for failures
 			Criteria failures = queryDAO.createCriteria(Joblet.class);
 			failures.add(Restrictions.eq("jobId", job.getId()));
 			failures.add(Restrictions.eq("status", JOB_STATUS.FAILED));
 			failures.setProjection(Projections.rowCount());
 			int failureCount = ((Integer) failures.list().get(0)).intValue();
 			int jobStatus = (failureCount == 0) ? JOB_STATUS.COMPLETED
 					: JOB_STATUS.FAILED;
 			// update job
 			job.setStatus(jobStatus);
 			job = (Job) dao.update(job);
 			if ((job.getCallbackType() != 0)
 					&& (job.getCallbackType() != JOB_CALLBACK_TYPES.NONE)) {
 				Map<String, String> params = new HashMap<String, String>();
 				params.put("jobId", Integer.toString(job.getId()));
 				params.put("jobStatus", jobStatusTypes[jobStatus]);
 				params.put("callbackType", ApiCallbackTypes
 						.getStringCallbackType(job.getCallbackType()));
 				params.put("callbackAddress", job.getCallbackAddress());
 				Joblet callback = new Joblet(0, System.currentTimeMillis(), 0,
 						0, job.getSubmitter(), 2, Constants.CALLBACK_JOBLET,
						"Callback for job # ", params, null,
 						JOB_STATUS.RECEIVED);
 				try {
 					self.submitJoblet(callback, 0, JOB_CALLBACK_TYPES.NONE,
 							null, null);
 				} catch (InvalidJobId e) {
 					log.error("InvalidJobId called while submitting callback!",
 							e);
 				}
 			}
 		}
 	}
 
 	public Host getHostByName(String hostname) throws TransportException,
 			InvalidApiKey, InvalidHost {
 		log.trace("getHostByName()");
 		Criteria crit = queryDAO.createCriteria(Host.class);
 		crit.add(Restrictions.eq("hostname", hostname));
 		Host host = (Host) crit.uniqueResult();
 		if (host == null) {
 			throw new InvalidHost();
 		}
 		return host;
 	}
 
 	public String getHostSetting(int hostid, String name, String defaultValue)
 			throws TransportException, InvalidApiKey, InvalidHost {
 		log.trace("getHostSetting()");
 		Criteria crit = queryDAO.createCriteria(HostSetting.class);
 		crit.createCriteria("host").add(Restrictions.eq("id", hostid));
 		crit.add(Restrictions.eq("name", name));
 		HostSetting setting = (HostSetting) crit.uniqueResult();
 		String value = defaultValue;
 		if (setting == null) {
 			Host host = readHost(hostid);
 			setting = new HostSetting(0, System.currentTimeMillis(), name,
 					value, null, host);
 			setting = (HostSetting) dao.create(setting);
 		} else {
 			value = setting.getValue();
 		}
 		return value;
 	}
 
 	public String getSetting(String name, String defaultValue)
 			throws TransportException, InvalidApiKey {
 		log.trace("getSetting()");
 		Criteria crit = queryDAO.createCriteria(SystemSetting.class);
 		crit.add(Restrictions.eq("name", name));
 		int size = crit.list().size();
 		SystemSetting ss = (SystemSetting) crit.uniqueResult();
 		String value = defaultValue;
 		if (ss == null) {
 			ss = new SystemSetting(0, System.currentTimeMillis(), name,
 					defaultValue, null);
 			ss = (SystemSetting) dao.create(ss);
 		} else {
 			value = ss.getValue();
 		}
 		return value;
 	}
 
 	public Job getJob(int jobId) throws TransportException, InvalidApiKey,
 			InvalidJobId {
 		log.trace("getJob()");
 		try {
 			return (Job) dao.read(Job.class, jobId);
 		} catch (ObjectRetrievalFailureException e) {
 			throw new InvalidJobId();
 		}
 	}
 
 	public int getJobletQueueSize() throws TransportException, InvalidApiKey {
 		log.trace("getJobletQueueSize()");
 		Criteria crit = queryDAO.createCriteria(Joblet.class);
 		crit.add(Restrictions.eq("status", JOB_STATUS.RECEIVED));
 		crit.setProjection(Projections.rowCount());
 		return ((Integer) crit.list().get(0)).intValue();
 	}
 
 	public JobletResult getJobletResult(int jobletId)
 			throws TransportException, InvalidApiKey, InvalidJobletId {
 		log.trace("getJobletResult()");
 		Criteria crit = queryDAO.createCriteria(JobletResult.class);
 		crit.createCriteria("joblet").add(Restrictions.eq("id", jobletId));
 		JobletResult jr = (JobletResult) crit.uniqueResult();
 		if (jr == null)
 			throw new InvalidJobletId();
 		return jr;
 	}
 
 	public List<JobletResult> getResults(int jobId) throws TransportException,
 			InvalidApiKey, InvalidJobId {
 		log.trace("getResults()");
 		Criteria crit = queryDAO.createCriteria(JobletResult.class);
 		crit.createCriteria("joblet").add(Restrictions.eq("jobId", jobId));
 		crit.addOrder(Order.asc("timeCreated"));
 		return crit.list();
 	}
 
 	public Joblet getWork() throws TransportException, InvalidApiKey,
 			InvalidHost, NoWorkAvailable {
 		log.trace("getWork()");
 		Host host = getHostByName(this.hostname);
 		Criteria crit = queryDAO.createCriteria(Joblet.class);
 		crit.add(Restrictions.eq("status", JOB_STATUS.RECEIVED));
 		crit.add(Restrictions.or(Restrictions.eq("hostId", host.getId()),
 				Restrictions.or(Restrictions.isNull("hostId"), Restrictions.eq(
 						"hostId", 0))));
 		crit.addOrder(Order.desc("hostId"));
 		crit.addOrder(Order.desc("priority"));
 		crit.addOrder(Order.asc("timeCreated"));
 		crit.setMaxResults(1);
 		crit.setLockMode(LockMode.UPGRADE);
 		Joblet joblet = (Joblet) crit.uniqueResult();
 		if (joblet == null)
 			throw new NoWorkAvailable();
 		joblet.setStatus(JOB_STATUS.QUEUED);
 		joblet.setHostId(host.getId());
 		dao.update(joblet);
 		return joblet;
 	}
 
 	public JobletResult gridExecute(Joblet joblet, int retries)
 			throws InvalidApiKey, TransportException, NoHostAvailable {
 		log.trace("gridExecute()");
 		// want to pick a random host
 		Criteria crit = queryDAO.createCriteria(Host.class);
 		int size = crit.list().size();
 		int hostNumber = random.nextInt(size);
 		Host host = (Host) crit.list().get(hostNumber);
 		try {
 			JobletResult result = syncJobService.gridExecute(
 					host.getHostname(), joblet);
 			return result;
 		} catch (TException e) {
 			log.error("TException calling gridExecute()", e);
 			throw new TransportException(e);
 		}
 	}
 
 	public void log(int jobletId, int jobletStatus, String message)
 			throws TransportException, InvalidApiKey, InvalidJobletId {
 		log.trace("log()");
 		Joblet joblet = readJoblet(jobletId);
 		JobletLogEntry entry = new JobletLogEntry(0, joblet, message);
 		dao.create(entry);
 		if (jobletStatus != joblet.getStatus()) {
 			joblet.setStatus(jobletStatus);
 			dao.update(joblet);
 		}
 	}
 
 	public Host registerHost(String hostname) throws TransportException,
 			InvalidApiKey, InvalidHost {
 		log.trace("registerHost()");
 		Host host = null;
 		try {
 			host = getHostByName(hostname);
 		} catch (InvalidHost ih) {
 			host = new Host(0, hostname, new HashMap<String, String>());
 			host = (Host) dao.create(host);
 		}
 		return host;
 	}
 
 	public void releaseJoblet(int jobletId) throws InvalidApiKey,
 			TransportException, InvalidJobletId {
 		log.trace("releaseJoblet()");
 		Criteria crit = queryDAO.createCriteria(Joblet.class);
 		crit.add(Restrictions.eq("id", jobletId));
 		crit.add(Restrictions.or(Restrictions.eq("status", JOB_STATUS.QUEUED),
 				Restrictions.eq("status", JOB_STATUS.PROCESSING)));
 		crit.setLockMode(LockMode.UPGRADE);
 		Joblet joblet = (Joblet) crit.uniqueResult();
 		if (joblet == null)
 			throw new InvalidJobletId();
 		joblet.setStatus(JOB_STATUS.RECEIVED);
 		dao.update(joblet);
 	}
 
 	public void setHostFacts(int hostid, Map<String, String> facts)
 			throws TransportException, InvalidApiKey, InvalidHost {
 		log.trace("setHostFacts()");
 		Host host = null;
 		try {
 			host = (Host) dao.read(Host.class, hostid);
 		} catch (ObjectRetrievalFailureException e) {
 			throw new InvalidHost();
 		}
 		Map<String, String> oldFacts = host.getFacts();
 		if (oldFacts == null) {
 			oldFacts = new HashMap<String, String>();
 		} else {
 			oldFacts.putAll(facts);
 		}
 		Set<Entry<String, String>> entries = oldFacts.entrySet();
 		for (Entry<String, String> entry : entries) {
 			if (entry.getValue().length() > 500) {
 				oldFacts
 						.put(entry.getKey(), entry.getValue().substring(0, 499));
 			}
 		}
 		dao.update(host);
 	}
 
 	public int submitJob(Job job) throws TransportException, InvalidApiKey {
 		log.trace("submitJob()");
 		List<Joblet> joblets = new LinkedList<Joblet>();
 		for (Joblet joblet : job.getJoblets()) {
 			joblet.setJobId(0);
 			joblet.setTimeCreated(System.currentTimeMillis());
 			Joblet j = (Joblet) dao.create(joblet);
 			joblets.add(j);
 		}
 		job.setJoblets(joblets);
 		job = (Job) dao.create(job);
 		return job.getId();
 	}
 
 	public int submitJoblet(Joblet joblet, int jobId, int callbackType,
 			String callbackAddress, String callbackContent)
 			throws TransportException, InvalidApiKey, InvalidJobId {
 		log.trace("submitJoblet()");
 		Job job = null;
 		if (jobId != 0) {
 			try {
 				job = (Job) dao.read(Job.class, jobId);
 			} catch (ObjectRetrievalFailureException e) {
 				throw new InvalidJobId();
 			}
 		} else {
 			List<Joblet> joblets = new LinkedList<Joblet>();
 			job = new Job(0, System.currentTimeMillis(), joblet.getSubmitter(),
 					null, joblets, callbackType, callbackAddress,
 					callbackContent, JOB_STATUS.RECEIVED);
 			job = (Job) dao.create(job);
 		}
 		job.getJoblets().add(joblet);
 		joblet.setJobId(job.getId());
 		joblet.setTimeCreated(System.currentTimeMillis());
 		joblet = (Joblet) dao.create(joblet);
 		dao.update(job);
 		return joblet.getId();
 	}
 
 	private Joblet readJoblet(int id) throws InvalidJobletId {
 		log.trace("readJoblet()");
 		try {
 			return (Joblet) dao.read(Joblet.class, id);
 		} catch (ObjectRetrievalFailureException e) {
 			throw new InvalidJobletId();
 		}
 	}
 
 	private Host readHost(int id) throws InvalidHost {
 		log.trace("readHost()");
 		try {
 			return (Host) dao.read(Host.class, id);
 		} catch (ObjectRetrievalFailureException e) {
 			throw new InvalidHost();
 		}
 	}
 }
