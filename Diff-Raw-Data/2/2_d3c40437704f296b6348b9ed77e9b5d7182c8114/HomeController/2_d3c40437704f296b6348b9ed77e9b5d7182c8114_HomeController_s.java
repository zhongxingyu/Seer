 package com.localjobs.controllers;
 
 import java.security.Principal;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.inject.Provider;
 
 import org.springframework.social.connect.ConnectionRepository;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import com.localjobs.domain.Account;
 import com.localjobs.domain.Job;
 import com.localjobs.googleapis.DistanceResponse;
 import com.localjobs.googleapis.GoogleDistanceClient;
 import com.localjobs.jdbc.repository.AccountRepository;
 import com.localjobs.service.CoordinateFinder;
 import com.localjobs.service.LocalJobsService;
 import com.localjobs.utils.SecurityUtils;
 
 @Controller
 public class HomeController {
 
 	private final Provider<ConnectionRepository> connectionRepositoryProvider;
 
 	private final AccountRepository accountRepository;
 	
 	private LocalJobsService localJobsService;
 	
 	@Inject
 	private GoogleDistanceClient googleDistanceClient;
 
 	@Inject
 	private CoordinateFinder coordinateFinder;
 
 	@Inject
 	public HomeController(
 			Provider<ConnectionRepository> connectionRepositoryProvider,
 			AccountRepository accountRepository,LocalJobsService localJobsService) {
 		this.connectionRepositoryProvider = connectionRepositoryProvider;
 		this.accountRepository = accountRepository;
 		this.localJobsService = localJobsService;
 	}
 
 	@RequestMapping(value = {"/","/home"}, method = RequestMethod.GET)
 	public String home(Principal currentUser, Model model)  throws Exception{
 		model.addAttribute("connectionsToProviders", getConnectionRepository()
 				.findAllConnections());
 		model.addAttribute(accountRepository.findAccountByUsername(currentUser
 				.getName()));
 		
 		Account account = accountRepository.findAccountByUsername(SecurityUtils
 				.getCurrentLoggedInUsername());
 		
 		double[] coordinates = coordinateFinder.find(account.getAddress());
 		double latitude = coordinates[0];
 		double longitude = coordinates[1];
 		
 		List<JobDistanceVo> recommendedJobs = recommendedJobs(latitude,longitude,account.getSkills().split(","));
 		
 		model.addAttribute("recommendedJobs", recommendedJobs);
 		
 		List<JobDistanceVo> appliedJobs = appliedJobs(latitude,longitude,SecurityUtils.getCurrentLoggedInUsername());
 		model.addAttribute("appliedJobs", appliedJobs);
 		return "home";
 	}
 
 	private List<JobDistanceVo> appliedJobs(double latitude, double longitude, String user) {
 		List<Job> jobs = localJobsService.appliedJobs(user);
 		return toJobDistanceVo(latitude, longitude, jobs);
 	}
 
 	private List<JobDistanceVo> recommendedJobs(double latitude,double longitude,String[] skills) throws Exception{
 		
 		List<Job> jobs = localJobsService.recommendJobs(latitude, longitude, skills, SecurityUtils
 				.getCurrentLoggedInUsername());
 		return toJobDistanceVo(latitude, longitude, jobs);
 	}
 
 	private List<JobDistanceVo> toJobDistanceVo(double latitude,
 			double longitude, List<Job> jobs) {
 		List<JobDistanceVo> jobsDistanceVo = new ArrayList<JobDistanceVo>();
 		for (Job job : jobs) {
 			DistanceResponse response = googleDistanceClient.findDirections(
 					job.getLocation(),
					new double[] { latitude, longitude });
 			JobDistanceVo vo = new JobDistanceVo(job,
 					response.rows[0].elements[0].distance,
 					response.rows[0].elements[0].duration);
 			jobsDistanceVo.add(vo);
 		}
 		return jobsDistanceVo;
 	}
 	private ConnectionRepository getConnectionRepository() {
 		return connectionRepositoryProvider.get();
 	}
 	
 }
