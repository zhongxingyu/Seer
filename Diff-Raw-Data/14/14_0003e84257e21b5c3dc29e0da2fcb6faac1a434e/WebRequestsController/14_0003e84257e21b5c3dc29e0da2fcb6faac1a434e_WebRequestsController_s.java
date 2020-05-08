 package org.calculator.controllers;
 
 import java.util.List;
 
 import org.calculator.business.IWebParamManager;
 import org.calculator.business.IWebRequestManager;
 import org.calculator.models.WebParam;
 import org.calculator.models.WebRequest;
 import org.calculator.models.viewmodels.JSONJTableModel;
 import org.calculator.models.viewmodels.JSONJTableResponseModel;
 import org.calculator.models.viewmodels.WebRequestsViewModel;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.http.MediaType;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  * Web requests CRUD controller
  * 
  * @author khalil.amdouni
  *
  */
 @Controller
 public class WebRequestsController {
 	
 	private IWebParamManager webParamManager;
 	private IWebRequestManager webRequestManager;
 	
 	@RequestMapping(value = "/webRequestsManager", method = RequestMethod.GET)
 	public ModelAndView webRequestsManager() {
 		return new ModelAndView("webRequestsManager");
 	}
 	
 	@RequestMapping(value = "/getRequestsList", method = RequestMethod.GET)
 	public ModelAndView getRequestsList() {
 		WebRequestsViewModel webRequestsViewModel = new WebRequestsViewModel();
 		webRequestsViewModel.setWebRequests(webRequestManager.getAllWebRequests());
 		return new ModelAndView("webRequestsList", "webRequestsViewModel", webRequestsViewModel);
 	}
 	
	@RequestMapping(value = "/getWebRequests", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
 	public @ResponseBody
	List<WebRequest> getWebRequests() {
		return webRequestManager.getAllWebRequests();
 	}
 	
 	@RequestMapping(value = "/addRequest", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
 	public ModelAndView addRequest(@RequestBody WebRequest webRequest) {
 
 		webRequestManager.save(webRequest);
 		return getRequestsList();
 	}
 	
 	@RequestMapping(value = "/getRequest/{requestId}", method = RequestMethod.GET)
 	public ModelAndView getRequest(@PathVariable("requestId") long requestId) {
 		return new ModelAndView("webRequestView", "webRequestModel",
 				webRequestManager.get(requestId));
 	}
 	
 	@RequestMapping(value = "/updateRequest", method = RequestMethod.POST)
 	public ModelAndView updateRequest(@RequestBody WebRequest webRequest) {
 		webRequestManager.save(webRequest);
 		return getRequest(webRequest.getId());
 	}
 	
 	@RequestMapping(value = "/deleteRequest/{requestId}", method = RequestMethod.DELETE)
 	public @ResponseBody
 	String deleteRequest(@PathVariable("requestId") long requestId) {
 		webRequestManager.delete(requestId);
 		return "OK";
 	}
 	
 	@RequestMapping(value = "/getWebParams/{requestId}", method = RequestMethod.POST, headers = "Accept=application/json")
 	public @ResponseBody
 	JSONJTableModel<WebParam> getWebParams(@RequestParam int jtStartIndex,
 			@RequestParam int jtPageSize,
 			@PathVariable("requestId") long requestId) {
 		return new JSONJTableModel<WebParam>("OK",
 				this.webParamManager.getWebParamByRequestId(requestId,
 						jtStartIndex, jtPageSize),
 				this.webParamManager.getWebParamsCount(requestId));
 	}
 
 	@RequestMapping(value = "/addWebParam/{requestId}", method = RequestMethod.POST, headers = "Accept=application/json")
 	public @ResponseBody
 	JSONJTableResponseModel<WebParam> addWebParam(
 			@ModelAttribute WebParam webParam,
 			@PathVariable("requestId") long requestId, BindingResult result) {
 		webParam.setRequestId(requestId);
 		return new JSONJTableResponseModel<WebParam>("OK",
 				this.webParamManager.save(webParam));
 	}
 
 	@RequestMapping(value = "/updateWebParam", method = RequestMethod.POST, headers = "Accept=application/json")
 	public @ResponseBody
 	JSONJTableResponseModel<WebParam> updateWebParam(
 			@ModelAttribute WebParam webParam, BindingResult result) {
 		return new JSONJTableResponseModel<WebParam>("OK",
 				this.webParamManager.save(webParam));
 	}
 
 	@RequestMapping(value = "/deleteWebParam", method = RequestMethod.POST, headers = "Accept=application/json")
 	public @ResponseBody
 	JSONJTableResponseModel<WebParam> deleteWebParam(
 			@RequestParam long webParamId) {
 		webParamManager.delete(webParamId);
 		return new JSONJTableResponseModel<WebParam>("OK");
 	}
 
 	public IWebParamManager getWebParamManager() {
 		return webParamManager;
 	}
 
 	@Autowired
 	@Qualifier(value = "webParamManager")
 	public void setWebParamManager(IWebParamManager webParamManager) {
 		this.webParamManager = webParamManager;
 	}
 	
 	public IWebRequestManager getWebRequestManager() {
 		return webRequestManager;
 	}
 
 	@Autowired
 	@Qualifier(value = "webRequestManager")
 	public void setWebRequestManager(IWebRequestManager webRequestManager) {
 		this.webRequestManager = webRequestManager;
 	}
 	
 }
