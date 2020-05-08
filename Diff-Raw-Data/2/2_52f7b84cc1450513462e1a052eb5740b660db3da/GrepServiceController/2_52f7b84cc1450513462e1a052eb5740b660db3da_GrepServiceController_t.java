 package org.devopspy.controller;
 
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
 import org.devopspy.model.DosResult;
 import org.devopspy.model.DosSearchData;
 import org.devopspy.service.GrepService;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 @Controller
@RequestMapping(value = "/api/")
 public class GrepServiceController {
 
 	@Inject
 	@Named("grepService")
 	private GrepService grepService;
 
 	@RequestMapping(value = "grep", method = RequestMethod.POST)
 	@ResponseBody
 	@Produces(MediaType.TEXT_PLAIN)
 	public List<DosResult> executeGrep(@RequestBody DosSearchData searchData) {
 		return grepService.runGrep(searchData);
 	}
 
 	@RequestMapping(value = "grep/{searchDataId}", method = RequestMethod.GET)
 	@ResponseBody
 	@Produces(MediaType.TEXT_PLAIN)
 	public List<DosResult> executeGrep(@PathVariable Long searchDataId) {
 		return grepService.runGrep(searchDataId);
 	}
 
 }
