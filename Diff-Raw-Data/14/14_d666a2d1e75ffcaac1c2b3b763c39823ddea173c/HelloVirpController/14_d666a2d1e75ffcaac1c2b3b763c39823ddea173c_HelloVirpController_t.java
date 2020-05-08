 package com.jshnd.virp.sample.controller;
 
import com.jshnd.virp.VirpSessionSpec;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import com.jshnd.virp.VirpConfig;
 import com.jshnd.virp.VirpSession;
 import com.jshnd.virp.config.SessionAttachmentMode;
 import com.jshnd.virp.sample.VirpSampleWebSession;
 import com.jshnd.virp.sample.model.VirpUser;
 
 @Controller
 public class HelloVirpController {
 
 	@Autowired
 	private VirpConfig virpConfig;
 	
 	@Autowired
 	private VirpSampleWebSession webSession;
 	
 	@RequestMapping(value = "index.html", method = RequestMethod.GET)
 	public String sayHello(ModelMap model) {
 		model.put("form", new VirpUser());
 		return "helloVirpUser";
 	}
 
 	@RequestMapping(value = "index.html", method = RequestMethod.POST)
 	public String lookupUser(@ModelAttribute("form") VirpUser input, ModelMap model) {
 		// since were not going to be doing anything fancy with the user we grab there's no
 		// reason to make it attached to the session and attachment would prevent the session
 		// from being GC'd until the HttpSession holding the webSession went away
		VirpSessionSpec spec = new VirpSessionSpec(virpConfig).withSessionAttachmentMode(SessionAttachmentMode.NONE);
		VirpSession session = virpConfig.newSession(spec);
 		try {
 			VirpUser attached = session.get(VirpUser.class, input.getEmail());
 			if(attached != null) {
 				model.put("user", attached);
 				webSession.setCurrentUser(attached);
 				return "redirect:my_virps.html";
 			} else {
 				model.put("user", input);
 				return "redirect:edit_user.html";
 			}
 		} finally {
 			session.close();
 		}
 	}
 	
 }
