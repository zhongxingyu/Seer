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
		
		Findbugs fb = null;
		Checkstyle cs = null;
		if(bj.isBuilding()) {
			//If the current job is building we don't have analysis results for the current build so use the results from before
			//If this is the first build use empty objects
			if(bj.getNumber() > 1) {
				fb = findbugsService.findbugs(JenkinsProperties.getInstance().getMetricsJob(), bj.getNumber() - 1);
				cs = checkstyleService.checkstyle(JenkinsProperties.getInstance().getMetricsJob(), bj.getNumber() - 1);
			} else {
				fb = new Findbugs();
				cs = new Checkstyle();
			}
		} else {
			fb = findbugsService.findbugsLastBuild(JenkinsProperties.getInstance().getMetricsJob());
			cs = checkstyleService.checkstyleLastBuild(JenkinsProperties.getInstance().getMetricsJob());
		}
		
 		
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
