 package edu.rit.asksg.web;
 
 import edu.rit.asksg.domain.Facebook;
 import edu.rit.asksg.domain.Service;
 import edu.rit.asksg.domain.config.SpringSocialConfig;
 import edu.rit.asksg.service.FacebookService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.ResponseEntity;
 import org.springframework.roo.addon.web.mvc.controller.json.RooWebJson;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.RequestBody;
 
 @RooWebJson(jsonObject = Service.class)
 @Controller
 @RequestMapping("/services")
 public class ServiceController {
 
 	@Autowired
 	FacebookService facebookService;
 
 	@RequestMapping(method = RequestMethod.POST, value = "facebookToken")
 	public ResponseEntity<String> facebookToken(@RequestParam("id") final String id,
 	                                            @RequestParam("code") final String code) {
 		//TODO: type safety
 		final Facebook facebook = (Facebook) providerService.findService(Long.parseLong(id));
		((SpringSocialConfig) facebook.getConfig()).setAccessToken(code);
 		facebookService.makeAccessTokenRequest(facebook);
 		providerService.updateService(facebook);
 		return new ResponseEntity<String>(HttpStatus.OK);
 	}
 
 	@RequestMapping(method = RequestMethod.POST, headers = "Accept=application/json")
 	public ResponseEntity<String> createFromJson(@RequestBody String json) {
 		Service service = Service.fromJsonToService(json);
 		providerService.saveService(service);
 		HttpHeaders headers = new HttpHeaders();
 		headers.add("Content-Type", "application/json");
 		return new ResponseEntity<String>(service.toJson(), headers, HttpStatus.CREATED);
 	}
 
 
 }
