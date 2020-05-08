 package org.imirsel.nema.mock;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.imirsel.nema.flowservice.FlowService;
 import org.imirsel.nema.model.*;
 import org.imirsel.nema.model.JobResult;
 
 /**This server mocks the FlowService that Andrew is working on.
  * 
  * @author Amit Kumar
  * @date November 23rd 2009
  *
  */
 public class MockFlowServiceImpl implements FlowService{
 	
 	
 	private List<Flow> flowList = new ArrayList<Flow>(); 
 	private Set<Flow> flowTemplates = new HashSet<Flow>(); 
 	private HashMap<Long,Job> jobMap = new HashMap<Long,Job>();
 	private HashMap<Long,List<Notification>> notificationMap = new HashMap<Long,List<Notification>>();
 	private AtomicInteger count = new AtomicInteger(0);
 	private AtomicInteger ncount = new AtomicInteger(0);
 	 
 	public MockFlowServiceImpl(){
 			Flow flow = new Flow();
 			flow.setCreatorId(100l);
 			flow.setDateCreated(new Date());
 			flow.setDescription("This flow extracts features from the music collection");
 			flow.setId(1l);
 			flow.setInstanceOf(null);
 			flow.setKeyWords("test, flow, feature extractor");
 			flow.setName("Feature Extractor Flow");
 			flow.setTemplate(true);
 			flow.setUrl("http://test.org/helloworld/");
 			
 			Flow flow1 = new Flow();
 			flow1.setCreatorId(101l);
 			flow1.setDateCreated(new Date());
 			flow1.setDescription("This is a test flow 2");
 			flow1.setId(2l);
 			flow1.setInstanceOf(flow);
 			flow1.setKeyWords("test, flow, feature extractor");
 			flow1.setName("flow 2");
 			flow1.setTemplate(false);
 			flow1.setUrl("http://test.org/helloworld/");
 			
 			
 			Flow flow2 = new Flow();
 			flow2.setCreatorId(101l);
 			flow2.setDateCreated(new Date());
 			flow2.setDescription("This flow applies classification algorithms");
 			flow2.setId(3l);
 			flow2.setInstanceOf(null);
 			flow2.setKeyWords("test, flow, algo");
 			flow2.setName("Feature Classification Flow");
 			flow2.setTemplate(true);
 			flow2.setUrl("http://test.org/datatypetest/");
 			
 			Flow flow3 = new Flow();
 			flow3.setCreatorId(101l);
 			flow3.setDateCreated(new Date());
 			flow3.setDescription("This flow does evaluation of the results");
 			flow3.setId(4l);
 			flow3.setInstanceOf(null);
 			flow3.setKeyWords("test, flow, algo");
 			flow3.setName("Evaluation Flow");
 			flow3.setTemplate(true);
 			flow3.setUrl("http://test.org/helloworld/");
 			
 			
 			
 			flowList.add(flow);
 			flowList.add(flow1);
 			flowList.add(flow2);
 			
 			
 			flowTemplates.add(flow);
 			flowTemplates.add(flow2);
 			flowTemplates.add(flow3);
 			
 			//getJobList();
 	 }
 	    
   
   
   public static void main(String args[]){
 	  MockFlowServiceImpl mockImpl = new MockFlowServiceImpl();
 	   List<Job> jobList=mockImpl.getUserJobs(100);
 	  for(int i=0;i<jobList.size();i++){
 		 Job job= jobList.get(i);
 		 System.out.println("Name is: " + job.getName() + " "+job.getHost());
 	  }
 	  Job job=mockImpl.getJob(1l);
 	  System.out.println("job is "+ job.getId()  + "  " + job.getStatusCode());
 	  mockImpl.abortJob(1l);
 	  
 	  Job j=mockImpl.executeJob("tt", "name1", "desc1",1l, 2l, "bob@email.com");
 	  mockImpl.executeJob("tt", "name1", "desc1",1l, 2l, "bob@email.com");
 	  
 	  System.out.println("NUmber of Jobs by user: "+mockImpl.getUserJobs(2l).size());
 	  Job jo=mockImpl.getJob(j.getId());
 	  System.out.println(jo.getName());
 	
 	  
 	  
 	  
 	  System.out.println("job abort is "+ job.getStatusCode());
   }
 
   
 
 	
 	
 
 	private List<Job> getJobValues() {
 		List<Job> jobList = new ArrayList<Job>();
 		Iterator<Long> it = jobMap.keySet().iterator();
 		while(it.hasNext()){
 			jobList.add(jobMap.get(it.next()));
 		}
 		return jobList;
 	}
 
 
 
 	private Job getJob(int index) {
 		Job job1 =jobMap.get(index+0l);
 		return job1;
 	}
 
 
 
 	private void setStatusCode(int i) {
 		Job job = jobMap.get(i+0l);
 		if(job!=null){
 			job.setStatusCode(i);
 		}
 	}
 
 
 
 	private void getJobList() {
 		int num=5;
 		 for(int i=0; i < num;i++){
 			 		Job job = new Job();
 			 		int name_index = i;
 			 		job.setName("name_"+name_index); 
 			 		job.setDescription("description_"+name_index); 
 			 		job.setEndTimestamp(null); 
 			 		job.setExecPort(1024+name_index); 
 			 		job.setExecutionInstanceId("http://test.org/runme/"+name_index); 
 			 		job.setFlow(getFlow(name_index)); 
 			 		job.setHost(getJobHost()); 
 			 		job.setId(name_index+1l);
 			 		job.setNumTries(0); 
 			 		job.setOwnerEmail("bob"+name_index+"@nobody.com"); 
 			 		job.setOwnerId(100l+name_index); 
 			 		job.setPort(200+name_index); ;
 			 		job.setResults(getMockResults(name_index)); 
 			 		job.setStartTimestamp(getDate(-10*name_index)); 
 			 		job.setToken("token-"+name_index); 
 			 		job.setUpdateTimestamp(getDate(-10*name_index+3)); 
 			 		job.setStatusCode(Job.JobStatus.SCHEDULED.getCode());
 			
 			 jobMap.put(i+0l,job);
 		 }
 		 
 		
 	}
 
 
 
 	private Date getDate(int i) {
 		Date date  = new Date();
 		long time=date.getTime()+i;
 		return new Date(time);
 	}
 
 	
 	private Set<JobResult> getMockResults(int nameIndex) {
 		// TODO Auto-generated method stub
 		JobResult jresult = new JobResult();
 		jresult.setId((long)nameIndex);
 		jresult.setResultType("dir");
 		jresult.setUrl("file:///tmp/"+nameIndex);
 		File f= new File("/tmp/"+nameIndex);
 		f.mkdirs();
 
 		File r1 = new File(f,"result1");
 		try {
 			r1.createNewFile();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		JobResult jr = new JobResult();
 		jresult.setId((long)nameIndex+100);
 		jresult.setResultType("file");
 		jresult.setUrl("file:///tmp/"+nameIndex+"/result1");
 		
 		
 		Set<JobResult> jresults = new HashSet<JobResult>();
 		jresults.add(jresult);
 		jresults.add(jr);
 
 		return jresults;
 	}
 
 
 	private int getJobNumTries(int nameIndex) {
 		Job job = jobMap.get(nameIndex+0l);
 		if(job!=null){
 			return job.getNumTries();
 		}
 		return -1;
 	}
 
 
 	private int getJobStatus(int nameIndex) {
 		Job job = jobMap.get(nameIndex+0l);
 		if(job!=null){
 			return job.getJobStatus().getCode();
 		}
 		return -1;
 	}
 	
 
 
 
 	private int getStatusCode(int nameIndex) {
 		Job job = jobMap.get(nameIndex+0l);
 		if(job!=null){
 			return job.getStatusCode();
 		}
 		return -1;
 	}
 
 
 	private String getJobHost() {
 		Random generator = new Random( System.currentTimeMillis());
 		int index = generator.nextInt(4);
 		return "192.168.0."+index;
 	}
 
 
 	private Flow getFlow(int nameIndex) {
 		Random generator = new Random( System.currentTimeMillis());
 		int index = generator.nextInt(3);
 		return flowList.get(index);
 	}
 
 
 
 	public void deleteJob(long jobId) throws IllegalStateException {
 	
 		if(jobMap.get(jobId)==null){
 			throw new IllegalStateException(); 
 		}else if(jobMap.get(jobId).getStatusCode()== Job.JobStatus.STARTED.getCode()){
 			throw new IllegalStateException(); 
 		}
 		jobMap.remove(jobId);
 		
 	}
 
 
 
 	
 	public Job executeJob(String token, String name, String description,
 			long flowInstanceId, long userId, String userEmail) {
 		Job job = new Job();
 		job.setStartTimestamp(new Date());
 		job.setDescription(description);
 		job.setName(name);
 		job.setId(count.incrementAndGet()+0l);
 		Flow flow = getFlow(flowInstanceId);
 		job.setFlow(flow);
 		job.setToken(token);
 		job.setPort(1024+count.get());
 		job.setExecPort(2024+count.get());
 		job.setHost(getJobHost());
 		job.setJobStatus(Job.JobStatus.STARTED);
 		job.setOwnerEmail(userEmail);
 		job.setOwnerId(userId);
 		job.setResults(getMockResults(count.get()));
 		job.setSubmitTimestamp(new Date());
 		jobMap.put(count.get()+0l, job);
 		return job;
 	}
 
 
 
 	
 	public Set<Flow> getFlowTemplates() {
 		return flowTemplates;
 	}
 
 
 
 	
 	public Job getJob(long jobId) {
 		return jobMap.get(jobId);
 	}
 
 
 
 	
 	public List<Job> getUserJobs(long userId) {
 		List<Job> jobList = new ArrayList<Job>();
 		Iterator<Job> it = jobMap.values().iterator();
 		Job job = null;
 		while(it.hasNext()){
 			job = it.next();
 			if(job.getOwnerId()==userId)
 			jobList.add(copyOf(job));
 		}
 		return jobList;
 	}
 
 
 
 	private Job copyOf(Job next) {
 		Job job  = new Job();
 		job.setDescription(next.getDescription());
 		job.setEndTimestamp(next.getEndTimestamp());
 		job.setStartTimestamp(next.getStartTimestamp());
 		job.setExecPort(next.getExecPort());
 		job.setExecutionInstanceId(next.getExecutionInstanceId());
 		job.setFlow(next.getFlow());
 		job.setHost(next.getHost());
 		job.setId(next.getId());
 		job.setJobStatus(next.getJobStatus());
 		job.setName(next.getName());
 		job.setNumTries(next.getNumTries());
 		job.setOwnerEmail(next.getOwnerEmail());
 		job.setOwnerId(next.getOwnerId());
 		job.setPort(next.getPort());
 		job.setResults(next.getResults());
 		return job;
 	}
 
 
 
 	
 	public List<Notification> getUserNotifications(long userId) {
 		
 		Notification notification = new Notification();
 		notification.setDateCreated(new Date());
 		notification.setId(ncount.incrementAndGet()+0l);
 		notification.setMessage("message from the server arrived at  "+ new Date().toString());
 		notification.setRecipientEmail(userId+"@email.com");
 		notification.setRecipientId(userId);
 		notification.setSent(Boolean.FALSE);
 		
 		List<Notification> notificationSet=notificationMap.get(userId);
 		
 		if(notificationSet==null){
 			List hset = new ArrayList<Notification>();
 			hset.add(notification);
 			notificationMap.put(userId,hset);
 			return hset;
 		}else{
 			if(notificationSet.size()>4){
 				return notificationSet;
 			}else{
 				notificationSet.add(notification);
 				return notificationSet;
 			}
 		
 		}
 	
 		
 	}
 
 
 
 	
 	public Long storeFlowInstance(Flow instance) {
 		throw new RuntimeException("not implemented");
 	}
 	
 
 	 public void abortJob(long l) {
 		 Job job=jobMap.get(l);
 		 if(job!=null){
 			 job.setStatusCode(Job.JobStatus.ABORTED.getCode());
 		 }
 	}
 
 
 
 	public Flow getFlow(long id) {
 		Iterator<Flow> it=flowList.iterator();
 		Flow flow = null;
 		while(it.hasNext()){
 			flow = it.next();
 			if(flow.getId()==id){
 				return flow;
 			}
 		}
 		
 		it=flowTemplates.iterator();
 		while(it.hasNext()){
 			flow = it.next();
 			if(flow.getId()==id){
 				return flow;
 			}
 		}
 		return null;
 	}
 
 
 
 }
