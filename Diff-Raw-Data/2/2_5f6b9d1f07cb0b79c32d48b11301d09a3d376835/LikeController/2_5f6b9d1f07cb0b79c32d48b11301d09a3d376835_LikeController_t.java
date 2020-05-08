 package org.offlike.server;
 
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.google.common.collect.ImmutableMap;
 
 @org.springframework.stereotype.Controller
 public class LikeController {
 
 	
 	@RequestMapping("/like/{posterId}")
 	public ModelAndView like(@RequestParam("campaign_name") String campaignName, @PathVariable("posterId") String posterId) {
		return new ModelAndView("like",  ImmutableMap.<String, Object>of("campaignName", campaignName));
 	}
 	
 }
