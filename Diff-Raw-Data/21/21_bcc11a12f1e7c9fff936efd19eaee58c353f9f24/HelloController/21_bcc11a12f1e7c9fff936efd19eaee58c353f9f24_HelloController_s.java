 package de.avendoo.jenkins.controller.web;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 import retrofit.RequestInterceptor;
 import retrofit.RestAdapter;
 import de.avendoo.jenkins.domain.BuildJob;
 import de.avendoo.jenkins.domain.Checkstyle;
 import de.avendoo.jenkins.domain.Findbugs;
 import de.avendoo.jenkins.properties.JenkinsProperties;
 import de.avendoo.jenkins.rest.BuildJobService;
 import de.avendoo.jenkins.rest.CheckstyleService;
 import de.avendoo.jenkins.rest.DuplicatedCodeService;
 import de.avendoo.jenkins.rest.FindbugsService;
 import de.avendoo.jenkins.rest.TestReportService;
 
 @Controller
 public class HelloController {
 	
 	BuildJobService buildJobService;
 	TestReportService restReportService;
 	FindbugsService findbugsService;
 	CheckstyleService checkstyleService;
 	DuplicatedCodeService dryService;
 	
 	@RequestMapping(value="/hello", method = RequestMethod.GET)
 	public ModelAndView helloWorld() {
 		ModelAndView model = new ModelAndView("HelloWorldPage");
 		model.addObject("msg", "Hello Jenkins!");
 		
 		setupBuildJobService();
 		setupFindbugsService();
 		setupCheckstyleService();
 		
 		BuildJob bj = buildJobService.lastBuildJob(JenkinsProperties.getInstance().getMetricsJob());
		//TODO check if current status is building, no findbugs available yet...
		Findbugs fb = findbugsService.findbugsLastBuild(JenkinsProperties.getInstance().getMetricsJob());
		Checkstyle cs = checkstyleService.checkstyleLastBuild(JenkinsProperties.getInstance().getMetricsJob());
 		
 		model.addObject("checkstyle", cs);
 		model.addObject("findbugs", fb);
 		model.addObject("buildjob", bj);
 		return model;
 	}
 	
 	private void setupBuildJobService() {
 		buildJobService = getRestAdapter().create(BuildJobService.class);
 	}
 	
 	private void setupFindbugsService() {
 		findbugsService = getRestAdapter().create(FindbugsService.class);
 	}
 	
 	private void setupCheckstyleService() {
 		checkstyleService = getRestAdapter().create(CheckstyleService.class);
 	}
 	
 	private RestAdapter getRestAdapter() {
 		RestAdapter.Builder builder = new RestAdapter.Builder();
 		builder.setServer(JenkinsProperties.getInstance().getApiRoot());
 		builder.setRequestInterceptor(new RequestInterceptor() {
 			
 			@Override
 			public void intercept(RequestFacade request) {
 				request.addHeader("Authorization", "Basic "+JenkinsProperties.getInstance().getAuthHeader());
 			}
 		});
 		
 		RestAdapter restAdapter = builder.build();
 		return restAdapter;
 	}
 
 }
