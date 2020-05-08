 package it.polito.dp2.PJS.sol5;
 
 import it.polito.dp2.PJS.Cluster;
 import it.polito.dp2.PJS.Host;
 import it.polito.dp2.PJS.Job;
 import it.polito.dp2.PJS.JobGroup;
import it.polito.dp2.PJS.sol5.gen.GetHostsRequest;
import it.polito.dp2.PJS.sol5.gen.GetHostsResponse;
import it.polito.dp2.PJS.sol5.gen.PJSInfo;
import it.polito.dp2.PJS.sol5.gen.PJSInfoService;
import it.polito.dp2.PJS.sol5.gen.THost;
import it.polito.dp2.PJS.sol5.gen.UnknownNames_Exception;
 
 import java.security.InvalidParameterException;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 class ClusterImpl implements Cluster {
 
 	private String name;
 	private ClusterStatus status;
 	private it.polito.dp2.PJS.Host masterHost;
 	
 	private Set<it.polito.dp2.PJS.Host> hosts;
 	private Set<it.polito.dp2.PJS.JobGroup> jobGroups;
 	private Set<it.polito.dp2.PJS.Job> jobs;
 	
 	ClusterImpl(PJSInfoService service) throws InvalidParameterException {
 		
 		try {
 			this.hosts = this.parseHosts(service.getPJSInfoServiceSOAPPort());
 			this.jobGroups = this.parseJobGroups(service.getPJSInfoServiceSOAPPort());
 			this.jobs = this.parseJobs(service.getPJSInfoServiceSOAPPort());
 			
 			String name = "ClusterName"; //FIXME
 			if (name != null && !name.equals(""))
 				this.name = name;
 			else
 				throw new InvalidParameterException("Invalid 'name'...");
 
 			this.status = ClusterStatus.UNAVAIL;
 			this.masterHost = null;
 			for (Host host : this.hosts) {
 				if (host.isMaster()) {
 					this.status = ClusterStatus.OK;
 					this.masterHost = host;
 					break;
 				}
 			}
 			
 			if (this.masterHost == null && !this.status.equals(ClusterStatus.UNAVAIL))
 				throw new InvalidParameterException("Invalid 'masterHost'...");
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new InvalidParameterException(e.getMessage());
 		}
 	}
 	
 	private Set<Host> parseHosts(PJSInfo port) {
 		//extract all hosts
 		Set<Host> hosts = new HashSet<Host>();
 		
 		if (port.getHostNames() != null) {
 			List<String> hostNames = port.getHostNames();
 			GetHostsRequest request = new GetHostsRequest();
 			for (String hostName : hostNames) {
 				request.getName().add(hostName);
 			}
 			if (!request.getName().isEmpty()) {
 				try {
 					GetHostsResponse response = port.getHosts(request);
 					if (response.getHost() != null) {
 						for (THost hostElement : response.getHost()) {
 							String name = hostElement.getName();
 							String type = hostElement.getType().toString();
 							String status = hostElement.getStatus().toString();
 							int physicalMemory = hostElement.getMemory().intValue();
 							int load = hostElement.getLoad().intValue();
 							
 							try {
 								Host host = new HostImpl(name, type, status, physicalMemory, load);
 								hosts.add(host);
 							} catch (InvalidParameterException e) {
 								System.out.println("Host is not parsable, skipping...");
 							}
 						}
 					}
 				} catch (UnknownNames_Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		
 		return hosts;
 	}
 	
 	private Set<JobGroup> parseJobGroups(PJSInfo port) {
 		//extract all jobGroups
 		Set<JobGroup> jobGroups = new HashSet<JobGroup>();
 		
 //		if (jaxb.getJobGroups() != null) {
 //			List<JobGroup> jobGroupsList = jaxb.getJobGroups().getJobGroup();
 //			for (JobGroup jobGroupElement : jobGroupsList) {
 //				String name = jobGroupElement.getName();
 //				String description = jobGroupElement.getDescription();
 //				
 //				try {
 //					it.polito.dp2.PJS.JobGroup jobGroup = new JobGroupImpl(name, description);
 //					jobGroups.add(jobGroup);
 //				} catch (InvalidParameterException e) {
 //					System.out.println("JobGroup is not parsable, skipping...");
 //				}
 //			}
 //		}
 		
 		return jobGroups;
 	}
 	
 	private Set<Job> parseJobs(PJSInfo port) {
 		//extract all jobs
 		Set<Job> jobs = new HashSet<Job>();
 		
 //		if (jaxb.getJobs() != null) {
 //			List<Job> jobsList = jaxb.getJobs().getJob();
 //			for (Job jobElement : jobsList) {
 //				String jobID = jobElement.getJobID();
 //				String state = jobElement.getState();
 //				String submissionHost = jobElement.getSubmissionHost();
 //				long submissionTime = jobElement.getSubmissionTime();
 //				String jobGroup = jobElement.getJobGroup();
 //				String executionHost = jobElement.getExecutionHost();
 //				
 //				it.polito.dp2.PJS.Job job = new JobImpl(jobID, state, submissionHost, submissionTime, jobGroup, executionHost);
 //				jobs.add(job);
 //			}
 //		}
 		
 		return jobs;
 	}
 	
 	private Host getHost(String name) {
 		for (Host host : this.hosts)
 			if (host.getName().equals(name))
 				return host;
 		return null;
 	}
 	
 	private JobGroup getJobGroup(String name) {
 		for (JobGroup jobGroup : this.jobGroups)
 			if (jobGroup.getName().equals(name))
 				return jobGroup;
 		return null;
 	}
 	
 	@Override
 	public JobGroup getDefaultJobGroup() {
 		for (JobGroup jobGroup : this.jobGroups) {
 			if (jobGroup.getName().equals("default")) {
 				return jobGroup;
 			}
 		}
 		return new JobGroupImpl("default", null);
 	}
 
 	@Override
 	public Set<Host> getHosts() {
 		return this.hosts;
 	}
 
 	@Override
 	public Set<JobGroup> getJobGroups() {
 		return this.jobGroups;
 	}
 
 	@Override
 	public Set<Job> getJobs(boolean hist) {
 		if (hist) {
 			return this.jobs;
 		} else {
 			Set<Job> jobs = new HashSet<Job>();
 			
 			for (Job job : this.jobs) {
 				switch (job.getState()) {
 				case DONE:
 				case EXIT:
 					continue;
 				case PENDING:
 				case RUNNING:
 				case SUSPENDED:
 				default:
 					jobs.add(job);
 				}
 			}
 			return jobs;
 		}
 	}
 
 	@Override
 	public Host getMasterHost() {
 		return this.masterHost;
 	}
 
 	@Override
 	public String getName() {
 		return this.name;
 	}
 
 	@Override
 	public int getNumberOfHosts() {
 		return hosts.size();
 	}
 
 	@Override
 	public int getNumberOfServers() {
 		return this.getServers().size();
 	}
 
 	@Override
 	public Set<Host> getServers() {
 		Set<Host> servers = new HashSet<Host>();
 		for (Host host : this.hosts)
 			if (host.isServer())
 				servers.add(host);
 		return servers;
 	}
 
 	@Override
 	public ClusterStatus getStatus() {
 		return this.status;
 	}
 
 	class HostImpl implements Host {
 		
 		private String name;
 		private boolean isServer;
 		private boolean isMaster;
 		private HostStatus status;
 		private int physicalMemory;
 		private int load;
 		
 		HostImpl(String name, String type, String status, int physicalMemory, int load) throws InvalidParameterException {
 
 			try {
 				this.name = name;
 				
 				if (type.equals("MASTER")) {
 					this.isServer = true;
 					this.isMaster = true;
 				} else if (type.equals("SERVER")) {
 					this.isServer = true;
 					this.isMaster = false;
 				} else {
 					this.isServer = false;
 					this.isMaster = false;
 				}
 				
 				this.status = HostStatus.valueOf(status);
 				
 				this.physicalMemory = physicalMemory;
 
 				this.load = load;
 			} catch (Exception e) {
 				e.printStackTrace();
 				throw new InvalidParameterException(e.getMessage());
 			}
 		}
 
 		@Override
 		public int getLoad() {
 			return this.load;
 		}
 
 		@Override
 		public String getName() {
 			return this.name;
 		}
 
 		@Override
 		public int getPhysicalMemory() {
 			return this.physicalMemory;
 		}
 
 		@Override
 		public HostStatus getStatus() {
 			return this.status;
 		}
 
 		@Override
 		public boolean isMaster() {
 			return this.isMaster;
 		}
 
 		@Override
 		public boolean isServer() {
 			return this.isServer;
 		}
 
 	}
 	
 	class JobGroupImpl implements JobGroup {
 
 		private String name;
 		private String description;
 		
 		JobGroupImpl(String name, String description) throws InvalidParameterException {
 			if (name != null)
 				this.name = name;
 			else
 				throw new InvalidParameterException("Empty jobGroup title...");
 			this.description = description;
 		}
 		
 		@Override
 		public String getDescription() {
 			return this.description;
 		}
 
 		@Override
 		public Set<Job> getJobs() {
 			Set<Job> jobs = new HashSet<Job>();
 			for (Job job : ClusterImpl.this.jobs) {
 				if (job.getJobGroup().getName().equals(this.name)) {
 					jobs.add(job);
 				}
 			}
 			return jobs;
 		}
 
 		@Override
 		public String getName() {
 			return this.name;
 		}
 
 	}
 	
 	class JobImpl implements Job {
 		
 		private int jobID;
 		private JobState state;
 		private it.polito.dp2.PJS.Host submissionHost;
 		private long submissionTime;
 		private it.polito.dp2.PJS.JobGroup jobGroup;
 		private it.polito.dp2.PJS.Host executionHost;
 		
 		JobImpl(String jobID, String state, String submissionHost, long submissionTime, String jobGroup, String executionHost) throws InvalidParameterException {
 			try {
 				this.jobID = Integer.parseInt(jobID.substring(1));
 				
 				this.state = JobState.valueOf(state);
 				
 				this.submissionHost = ClusterImpl.this.getHost(submissionHost);
 				
 				this.submissionTime = submissionTime;
 				
 				if (jobGroup != null)
 					this.jobGroup = ClusterImpl.this.getJobGroup(jobGroup);
 				else
 					this.jobGroup = ClusterImpl.this.getDefaultJobGroup();
 				
 				if (executionHost != null)
 					this.executionHost = ClusterImpl.this.getHost(executionHost);
 				else
 					this.executionHost = null;
 			} catch (Exception e) {
 				e.printStackTrace();
 				throw new InvalidParameterException(e.getMessage());
 			}
 		}
 
 		@Override
 		public it.polito.dp2.PJS.Host getExecutionHost() {
 			return executionHost;
 		}
 
 		@Override
 		public int getID() {
 			return this.jobID;
 		}
 
 		@Override
 		public JobGroup getJobGroup() {
 			return this.jobGroup;
 		}
 
 		@Override
 		public JobState getState() {
 			return this.state;
 		}
 
 		@Override
 		public Host getSubmissionHost() {
 			return this.submissionHost;
 		}
 
 		@Override
 		public Calendar getSubmissionTime() {
 			Calendar submissionTime = GregorianCalendar.getInstance();
 			submissionTime.setTimeInMillis(this.submissionTime);
 			return submissionTime;
 		}
 		
 	}
 }
