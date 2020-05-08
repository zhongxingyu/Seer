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
		
		logger.debug("service.getName(): " + service.getName());
		logger.debug("service.getStateMgr(): " + service.getStateMgr());
		logger.debug("service.getStateMgr().getName(): " + service.getStateMgr().getName());
 	}
 	
 	public void execute() {
		logger.debug("Begin");
		logger.debug("service.getName(): " + service.getName());
		logger.debug("service.getStateMgr(): " + service.getStateMgr());
		logger.debug("service.getStateMgr().getName(): " + service.getStateMgr().getName());

 		
 		service.executeWeb(web);
		
		logger.debug("service.getName(): " + service.getName());
		logger.debug("service.getStateMgr(): " + service.getStateMgr());
		logger.debug("service.getStateMgr().getName(): " + service.getStateMgr().getName());
		logger.debug("End");
 	}
 }
