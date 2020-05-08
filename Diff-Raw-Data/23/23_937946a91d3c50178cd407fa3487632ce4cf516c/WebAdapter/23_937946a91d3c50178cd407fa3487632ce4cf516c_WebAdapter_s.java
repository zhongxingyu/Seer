 package webClient;
 
 import org.apache.log4j.Logger;
 
 import interfaces.Command;
 import interfaces.WebService;
 import data.Service;
 
 public class WebAdapter implements Command {
 	static Logger logger = Logger.getLogger(WebAdapter.class);
 	private Service service;
 	private WebService web;
 	
 	public WebAdapter(Service service, WebService web) {
 		this.service = service;
 		this.web = web;
 	}
 	
 	public void execute() {
		logger.info("service.getName(): " + service.getName());
 		
 		service.executeWeb(web);
 	}
 }
