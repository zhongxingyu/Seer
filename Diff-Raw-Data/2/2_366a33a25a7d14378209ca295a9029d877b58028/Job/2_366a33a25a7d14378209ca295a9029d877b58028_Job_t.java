 package models;
 
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 import play.Logger;
 import play.db.ebean.*;
 
 import javax.persistence.*;
 
 import org.daisy.pipeline.client.Pipeline2WSException;
 import org.daisy.pipeline.client.Pipeline2WSResponse;
 import org.w3c.dom.Document;
 
 import controllers.Application;
 
 import akka.actor.Cancellable;
 import akka.util.Duration;
 import play.data.validation.*;
 import play.libs.Akka;
 import utils.XML;
 
 @Entity
 public class Job extends Model implements Comparable<Job> {
 	private static final long serialVersionUID = 1L;
 	
 	/** Key is the job ID; value is the sequence number of the last message read from the Pipeline 2 Web Service. */ 
 	public static Map<String,Integer> lastMessageSequence = Collections.synchronizedMap(new HashMap<String,Integer>());
 	public static Map<String,org.daisy.pipeline.client.models.Job.Status> lastStatus = Collections.synchronizedMap(new HashMap<String,org.daisy.pipeline.client.models.Job.Status>());
 	
 	
 	@Id
 	@Constraints.Required
 	public String id;
 
 	// General information
 	public String nicename;
 	public Date created;
 	public Date started;
 	public Date finished;
 	@Column(name="user_id") public Long user;
 	public String guestEmail; // Guest users may enter an e-mail address to receive notifications
 	public String localDirName;
 	
 	// Notification flags
 	public boolean notifiedCreated;
 	public boolean notifiedComplete;
 
 	// Not stored in the job table; retrieved dynamically
 	@Transient
 	public String href;
 	@Transient
 	public String status;
 	@Transient
 	public String userNicename;
 
 	@Transient
 	private Cancellable pushNotifier;
 	
 	public Job(String id, User user) {
 		this.id = id;
 		this.user = user.id;
 		this.nicename = id;
 		this.created = new Date();
 		this.notifiedCreated = false;
 		this.notifiedComplete = false;
 		if (user.id < 0)
 			this.userNicename = Setting.get("users.guest.name");
 		else
 			this.userNicename = User.findById(user.id).name;
 	}
 
 	public int compareTo(Job other) {
 		return created.compareTo(other.created);
 	}
 
 	// -- Queries
 
 	public static Model.Finder<String,Job> find = new Model.Finder<String, Job>(Application.datasource, String.class, Job.class);
 
 	/** Retrieve a Job by its id. */
 	public static Job findById(String id) {
 		Job job = find.where().eq("id", id).findUnique();
 		if (job != null) {
 			User user = User.findById(job.user);
 			if (user != null)
 				job.userNicename = user.name;
 			else if (job.user < 0)
 				job.userNicename = "Guest #"+-job.user;
 			else
 				job.userNicename = "User #"+job.user;
 		}
 		return job;
 	}
 
 	public void pushNotifications() {
 		if (pushNotifier != null)
 			return;
 
 		pushNotifier = Akka.system().scheduler().schedule(
 				Duration.create(0, TimeUnit.SECONDS),
 				Duration.create(1000, TimeUnit.MILLISECONDS),
 				new Runnable() {
 					public void run() {
 						Application.lastRequest = new Date();
 						
						Integer fromSequence = Job.lastMessageSequence.containsKey(id) ? Job.lastMessageSequence.get(id) : 0;
 						Logger.debug("checking job #"+id+" for updates from message #"+fromSequence);
 						
 						Pipeline2WSResponse wsJob;
 						org.daisy.pipeline.client.models.Job job;
 						
 						try {
 							wsJob = org.daisy.pipeline.client.Jobs.get(Setting.get("dp2ws.endpoint"), Setting.get("dp2ws.authid"), Setting.get("dp2ws.secret"), id, fromSequence);
 							
 							if (wsJob.status != 200 && wsJob.status != 201) {
 								return;
 							}
 							
 							Document xml = wsJob.asXml();
 							job = new org.daisy.pipeline.client.models.Job(xml);
 							Logger.debug(XML.toString(xml));
 							
 						} catch (Pipeline2WSException e) {
 							Logger.error(e.getMessage(), e);
 							return;
 						}
 						
 						if (job.status != org.daisy.pipeline.client.models.Job.Status.RUNNING && job.status != org.daisy.pipeline.client.models.Job.Status.IDLE) {
 							pushNotifier.cancel();
 							Job webUiJob = Job.findById(job.id);
 							if (webUiJob.finished == null) {
 								// pushNotifier tends to fire multiple times after canceling it, so this if{} is just to fire the "finished" event exactly once
 								webUiJob.finished = new Date();
 								webUiJob.save(Application.datasource);
 								NotificationConnection.push(webUiJob.user, new Notification("job-finished-"+job.id, webUiJob.finished.toString()));
 							}
 						}
 						
 						Job webuiJob = Job.findById(job.id);
 						for (org.daisy.pipeline.client.models.job.Message message : job.messages) {
 							Notification notification = new Notification("job-message-"+job.id, message);
 							NotificationConnection.push(webuiJob.user, notification);
 						}
 						
 						if (!job.status.equals(lastStatus.get(job.id))) {
 							lastStatus.put(job.id, job.status);
 							NotificationConnection.push(webuiJob.user, new Notification("job-status-"+job.id, job.status));
 						}
 						
 						if (job.messages.size() > 0) {
 							Job.lastMessageSequence.put(job.id, job.messages.get(job.messages.size()-1).sequence);
 						}
 					}
 				}
 				);
 	}
 	
 	public List<Upload> getUploads() {
 		return Upload.find.where("job = '"+id+"'").findList();
 	}
 	
 	@Override
 	public void delete(String datasource) {
 		List<Upload> uploads = getUploads();
 		for (Upload upload : uploads)
 			upload.delete(datasource);
 //		org.daisy.pipeline.client.Jobs.delete(Setting.get("dp2ws.endpoint"), Setting.get("dp2ws.authid"), Setting.get("dp2ws.secret"), this.id);
 		super.delete(datasource);
 	}
 
 }
