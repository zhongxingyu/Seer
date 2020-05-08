 package idv.web.controller;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 @Controller
 @RequestMapping(value="/mytable")
 public class MytableController {
 
 	private static final Logger log = LoggerFactory.getLogger(MytableController.class);
 	
 	@RequestMapping(value={"", "/"})
 	public String home() {
 		return "mytable/remoteLoad";
 	}
 	
 	@RequestMapping(value="/remoteLoad")
 	public String remoteLoad() {
 		return "mytable/remoteLoad";
 	}
 	
 	@RequestMapping(value="/formEdit")
 	public String formEdit() {
 		return "mytable/formEdit";
 	}
 	
 	@RequestMapping(value="/customColumn")
 	public String customColumn() {
 		return "mytable/customColumn";
 	}
 }
