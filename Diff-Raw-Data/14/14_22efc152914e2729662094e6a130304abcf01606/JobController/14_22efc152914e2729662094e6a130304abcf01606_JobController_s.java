 package org.imirsel.nema.webapp.controller;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.imirsel.nema.Constants;
 import org.imirsel.nema.flowservice.FlowService;
 import org.imirsel.nema.model.Flow;
 import org.imirsel.nema.model.Job;
 import org.imirsel.nema.model.Notification;
 import org.imirsel.nema.model.Submission;
 import org.imirsel.nema.model.User;
 import org.imirsel.nema.service.SubmissionManager;
 import org.imirsel.nema.service.UserManager;
 import org.imirsel.nema.webapp.request.ExecuteJobRequest;
 import org.imirsel.nema.webapp.request.GetJobRequest;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
 import org.springframework.web.servlet.view.RedirectView;
 
 import com.hp.hpl.jena.graph.query.Rewrite;
 
 public class JobController extends MultiActionController {
 
 	static protected Log logger=LogFactory.getLog(JobController.class);
 	private UserManager userManager = null;
 	private FlowService flowService = null;
     private SubmissionManager submissionManager;
 
     public void setSubmissionManager(SubmissionManager submissionManager) {
 		this.submissionManager = submissionManager;
 	}
 
 	public FlowService getFlowService() {
 		return flowService;
 	}
 
 	public void setFlowService(FlowService flowService) {
 		this.flowService = flowService;
 	}
 
 	public void setUserManager(UserManager userManager) {
 		this.userManager = userManager;
 	}
 	
 	public ModelAndView hello(HttpServletRequest req,
 			HttpServletResponse res) {
 	
 		res.setContentType("text/xml");
 		try {
 			res.getWriter().write("<hello/>");
 			res.flushBuffer();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 		return null;
 	}
 	
 	
 	/**Returns the submissions for the current user
 	 * 
 	 * @param req
 	 * @param res
 	 * @return
 	 */
 	public ModelAndView getSubmissions(HttpServletRequest req,	HttpServletResponse res){
 		User user=this.userManager.getCurrentUser();
 		List<Submission> submissions=this.submissionManager.getSubmissions(user);
 		logger.info("Submissions are: " +submissions);
 		if(submissions!=null){
 			logger.info("submission size is: " + submissions.size());
 		}
 		
 		
 		
 		
 		return new ModelAndView("submission/submissionList", Constants.SUBMISSIONLIST, submissions);
 	}
 	
 	
 	/**Returns the submissions for all the users
 	 * 
 	 * @param req
 	 * @param res
 	 * @return
 	 */
 	public ModelAndView getAllSubmissions(HttpServletRequest req,	HttpServletResponse res){
 		List<Submission> list=this.submissionManager.getAllSubmissions();
 		return new ModelAndView("submission/submissionAllList", Constants.SUBMISSIONLIST, list);
 	}
 	
 	
 	
 
 	/**selects the job for submission
 	 * 
 	 * @param req
 	 * @param res
 	 * @return
 	 */
 	public ModelAndView selectJobForSubmission(HttpServletRequest req,	HttpServletResponse res){
 		String _jobId = req.getParameter("jobId");
 		Long jobId = Long.parseLong(_jobId);
 		Job job=this.flowService.getJob(jobId);
 		Flow instanceOfFlow=job.getFlow().getInstanceOf();
 		logger.info("getting job's flow "+ instanceOfFlow);
 		User user=this.userManager.getCurrentUser();
 		String type =  instanceOfFlow.getType();
 		Submission submission = new Submission();
 		submission.setDateCreated(new Date());
 		submission.setUser(user);
 		submission.setJobId(jobId);
 		submission.setType(type);
 		submission.setName(job.getName());
 		logger.info("Creating a submission with job:" + jobId + " and type: " + type);
 		Submission thisSubmission=this.submissionManager.getSubmission(user,type);
 		logger.info("Found a submission : "+ thisSubmission);
 		if(thisSubmission==null){
 			Submission s=this.submissionManager.saveSubmission(submission);
 			logger.info("submission not found -adding new submission the id is " + s.getId());
 		}else{
 			// remove the existing submission and add the new submission
 			this.submissionManager.removeSubmission(thisSubmission.getId());
 			Submission s=this.submissionManager.saveSubmission(submission);
 			logger.info("submission found: removing it " +thisSubmission.getId()+" and adding new submission id is " + s.getId());
 		}
 		return new ModelAndView(new RedirectView("/get/JobManager.getSubmissions"));
 	
 	}
 
 	
 	public ModelAndView submissionDetail(HttpServletRequest req,
 			HttpServletResponse res) {
 		String _submissionId = req.getParameter("id");
 		long submissionId = Long.parseLong(_submissionId);
 		Submission submission=this.submissionManager.getSubmission(submissionId);
 		
 		if(submission==null){
 			// do something
 		}
 		
 		Job job = flowService.getJob(submission.getJobId());
 		ModelAndView mav= new ModelAndView("submission/submission");
 		
 		mav.addObject(Constants.JOB, job);
 		mav.addObject(Constants.SUBMISSION, submission);
 		mav.addObject(Constants.RESULTSET,job.getResults());
 		
 		
 		
 		
 		
 		return mav;
 	}
 	
 
 	public ModelAndView jobdetail(HttpServletRequest req,
 			HttpServletResponse res) {
 		String _jobId = req.getParameter("id");
 		long jobId = Long.parseLong(_jobId);
 		Job job = flowService.getJob(jobId);
 		System.out.println("Show Job here: " + job.getName());
 		System.out.println("STATUS CODE: " + job.getStatusCode());
 		System.out.println("STATUS VAL: " + job.getJobStatus());
		
 		return new ModelAndView("job/job", Constants.JOB, job);
 	}
 
 	
 	public ModelAndView submissionAction(HttpServletRequest req,
 			HttpServletResponse res) {
 		String _submissionId = req.getParameter("id");
 		long submissionId = Long.parseLong(_submissionId);
 		this.submissionManager.removeSubmission(submissionId);
 		return new ModelAndView(new RedirectView("/get/JobManager.getSubmissions"));
 	}
 
 	
 	
 	public ModelAndView jobaction(HttpServletRequest req,
 			HttpServletResponse res) {
 		String _jobId = req.getParameter("id");
 		long jobId = Long.parseLong(_jobId);
 
 		Job job = flowService.getJob(jobId);
 		String _submitType = req.getParameter("submit");
 
 		if (_submitType.equals("Abort This Job")) {
 			flowService.abortJob(job.getId());
 		} else if (_submitType.equals("Delete This Job")) {
 			flowService.deleteJob(job.getId());
 		}
 		//, Constants.JOB, job
 		return new ModelAndView(new RedirectView("/get/JobManager.getUserJobs"));
 	}
 
 
 
 	public ModelAndView getuserjobs(HttpServletRequest req,
 			HttpServletResponse res) {
 
 		User user = userManager.getCurrentUser();
 		logger.debug("USER IS ====> " + user);
 
 		if (user == null) {
 			user = userManager.getUserByUsername("admin");
 		}
 		long userId = user.getId();
 
 		logger.debug("start to list the jobs of   " + user);
 		List<Job> jobs = flowService.getUserJobs(userId);
 	
 		for(Job job:jobs){
 			logger.debug(job.getId() +" " +job.getName() + " " + job.getJobStatus());
 		}
 		
 		
 		return new ModelAndView("job/jobList", Constants.JOBLIST, jobs);
 	}
 
 	public ModelAndView getUserNotifications(HttpServletRequest req,
 			HttpServletResponse res) {
 		User user = userManager.getCurrentUser();
 		long userId = user.getId();
 		List<Notification> notificationList = flowService
 				.getUserNotifications(userId);
 		return null;
 	}
 
 }
