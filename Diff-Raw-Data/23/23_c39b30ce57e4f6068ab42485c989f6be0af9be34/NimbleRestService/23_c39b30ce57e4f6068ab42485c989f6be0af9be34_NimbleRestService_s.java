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
 import org.springframework.web.bind.annotation.SessionAttributes;
 import org.springframework.web.servlet.HandlerMapping;
 
 import com.felthat.nimble.graph.Graph;
 import com.felthat.nimble.graph.NimbleMapGraph;
 
 
 
 @Controller
 @SessionAttributes(NimbleRestService.NIMBLE_GRAPH)
 @RequestMapping(NimbleRestService.ROOT_PATH)
 public class NimbleRestService {
 	
 	static final String ROOT_PATH = "/nimble";
 
 	public static final String NIMBLE_GRAPH = "NIMBLE_GRAPH";
 
 	@RequestMapping(value="/**", method=RequestMethod.GET)
 	public @ResponseBody NimbleResponse get(HttpServletRequest request, HttpServletResponse httpServletResponse){
 		Graph graphRootGraph = getGraphFromSession(request.getSession());
 		String path = getPath(request);
 		Graph graph;
 		if("".equals(path)){
 			graph = graphRootGraph;
 		}else{
 			 graph = graphRootGraph.get(path);
 		}
		if(graph == null){
 			httpServletResponse.setStatus(HttpStatus.NOT_FOUND.value());
 			return null;
 		}
 		String baseURL = getBaseURL(request);
 		NimbleResponse response = new NimbleResponse(graph,baseURL);//TODO remove this unfortunate cast somehow
 		httpServletResponse.setStatus(HttpStatus.OK.value());
 		return response;
 	
 	}
 	
 	@ResponseStatus(value = HttpStatus.CREATED)
 	@RequestMapping(value="/**", method=RequestMethod.PUT)
	public void create(
			@RequestBody NimbleRequest requestObject, HttpServletRequest request){
 		String path = getPath(request);
 		Graph subGraph = new NimbleRequestMapper().map(requestObject);
 		Graph graph = getGraphFromSession(request.getSession());
 		graph.put(path, subGraph);
 	}
 	
 	@ResponseStatus(value = HttpStatus.CREATED)
 	@RequestMapping(value="/**", method=RequestMethod.POST)
 	public void update(@RequestBody NimbleRequest requestObject, HttpServletRequest request, HttpServletResponse httpServletResponse){
 		Graph graph = getGraphFromSession(request.getSession());
 		String path = getPath(request);
 		Graph subGraph = new NimbleRequestMapper().map(requestObject);
 		graph.merge(path, subGraph);
 	}
 	
 	@ResponseStatus(value = HttpStatus.NO_CONTENT)
 	@RequestMapping(value="/**", method=RequestMethod.DELETE)
 	public void delete(HttpServletRequest request){
 		Graph graph = getGraphFromSession(request.getSession());
 		String path = getPath(request);
 		graph.delete(path);
 	}
 
 	private Graph getGraphFromSession(HttpSession httpSession) {
 		Graph graph = (Graph) httpSession.getAttribute(NIMBLE_GRAPH);
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
