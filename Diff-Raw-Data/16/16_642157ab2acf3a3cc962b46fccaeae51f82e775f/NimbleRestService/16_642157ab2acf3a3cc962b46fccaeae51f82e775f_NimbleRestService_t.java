 package com.felthat.nimble.rest;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 import org.springframework.web.servlet.HandlerMapping;
 
 import com.felthat.nimble.graph.NimbleMapGraph;
 
 
 
 @Controller
 @RequestMapping(value=NimbleRestService.ROOT_PATH)
 public class NimbleRestService {
 	
	static final String ROOT_PATH = "/nimble";
 
 	public static final String NIMBLE_GRAPH = "NIMBLE_GRAPH";
 
 	@ResponseStatus(value=HttpStatus.OK)
 	@RequestMapping(value="/**", method=RequestMethod.GET)
 	public @ResponseBody NimbleResponse get(HttpServletRequest request, HttpServletResponse httpServletResponse){
 		NimbleMapGraph graph = getGraphFromSession(request.getSession());
 		String path = getPath(request);
 		graph = (NimbleMapGraph) graph.get(path);
 		String baseURL = getBaseURL(request);
 		NimbleResponse response = new NimbleResponse(graph.getGraphObject(),baseURL);
 		return response;
 	}
 	
 	@ResponseStatus(value = HttpStatus.CREATED)
 	@RequestMapping(value="/**", method=RequestMethod.PUT)
 	public void create(@RequestBody NimbleRequest requestObject, HttpServletRequest request, HttpServletResponse httpServletResponse){
 		NimbleMapGraph graph = getGraphFromSession(request.getSession());
 		String path = getPath(request);
 		NimbleMapGraph subGraph = new NimbleMapGraph(requestObject.getData());
 		graph.put(path, subGraph);
 	}
 	
 	@ResponseStatus(value = HttpStatus.CREATED)
 	@RequestMapping(value="/**", method=RequestMethod.POST)
 	public void update(@RequestBody NimbleRequest requestObject, HttpServletRequest request, HttpServletResponse httpServletResponse){
 		NimbleMapGraph graph = getGraphFromSession(request.getSession());
 		String path = getPath(request);
 		NimbleMapGraph subGraph = new NimbleMapGraph(requestObject.getData());
 		graph.merge(path, subGraph);
 	}
 	
 	@ResponseStatus(value = HttpStatus.NO_CONTENT)
 	@RequestMapping(value="/**", method=RequestMethod.DELETE)
 	public void delete(HttpServletRequest request){
 		NimbleMapGraph graph = getGraphFromSession(request.getSession());
 		String path = getPath(request);
 		graph.delete(path);
 	}
 
 	private NimbleMapGraph getGraphFromSession(HttpSession httpSession) {
 		NimbleMapGraph graph = (NimbleMapGraph) httpSession.getAttribute(NIMBLE_GRAPH);
 		if(graph == null){
 			synchronized (httpSession) {
 				graph = new NimbleMapGraph();
 				httpSession.setAttribute(NIMBLE_GRAPH, graph);
 			}
 		}
 		return graph;
 	}
 	
 	private String getPath(HttpServletRequest request) {
 		String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
 		return path;
 	}
 	
 	private String getBaseURL(HttpServletRequest request) {
 		String requestURL = request.getRequestURL().toString();
 		String contextPath = request.getContextPath();
		String servletPath = request.getServletPath();
		String start = requestURL.substring(0,requestURL.indexOf(contextPath)+contextPath.length());		
		return start + servletPath + ROOT_PATH;
 	}
 
 }
