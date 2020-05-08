 package uk.co.eelpieconsulting.busroutes.controllers;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.List;
 
 import org.apache.solr.client.solrj.SolrServerException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import uk.co.eelpieconsulting.busroutes.model.MultiStopMessage;
 import uk.co.eelpieconsulting.busroutes.parsing.CountdownService;
 import uk.co.eelpieconsulting.busroutes.parsing.RouteImportService;
 import uk.co.eelpieconsulting.busroutes.services.MessageService;
 import uk.co.eelpieconsulting.busroutes.services.StopsService;
 import uk.co.eelpieconsulting.busroutes.services.solr.SolrUpdateService;
 import uk.co.eelpieconsulting.common.views.ViewFactory;
import uk.co.eelpieconsulting.countdown.exceptions.HttpFetchException;
 import uk.co.eelpieconsulting.countdown.exceptions.ParsingException;
 import uk.co.eelpieconsulting.countdown.model.StopBoard;
 
 @Controller
 public class StopsController {
 	
 	private final StopsService stopsService;
 	private final CountdownService countdownService;
 	private final MessageService messageService;
 	private final ViewFactory viewFactory;
 	private final RouteImportService routeImportService;
 	private final SolrUpdateService solrUpdateService;
 	
 	@Autowired
 	public StopsController(StopsService stopsService, CountdownService countdownService, MessageService messageService, ViewFactory viewFactory, RouteImportService routeImportService, SolrUpdateService solrUpdateService) {
 		this.stopsService = stopsService;
 		this.countdownService = countdownService;
 		this.messageService = messageService;
 		this.viewFactory = viewFactory;
 		this.routeImportService = routeImportService;
 		this.solrUpdateService = solrUpdateService;
 	}
 	
 	@RequestMapping("/stop/{id}")
 	public ModelAndView stop(@PathVariable int id) {
 		final ModelAndView mv = new ModelAndView(viewFactory.getJsonView());
 		mv.addObject("data", stopsService.getStopById(id));
 		return mv;
 	}
 	
 	@RequestMapping("/stop/{id}/arrivals")
 	public ModelAndView arrivals(@PathVariable int id) throws HttpFetchException, ParsingException {
 		final ModelAndView mv = new ModelAndView(viewFactory.getJsonView());		
 		final StopBoard stopBoard = countdownService.getStopBoard(id);
 		mv.addObject("data", stopBoard);
 		return mv;
 	}
 	
 	@RequestMapping("/stop/{id}/messages")
 	public ModelAndView messages(@PathVariable int id) throws HttpFetchException, ParsingException {
 		final ModelAndView mv = new ModelAndView(viewFactory.getJsonView());		
 		mv.addObject("data", messageService.getMessages(id));
 		return mv;
 	}
 	
 	@RequestMapping("/messages")
 	public ModelAndView multiMessages(@RequestParam(value="stops", required = false) int[] stops) throws HttpFetchException, ParsingException {
 		final ModelAndView mv = new ModelAndView(viewFactory.getJsonView());
 		final List<MultiStopMessage> messages = messageService.getMessages(stops);		
 		mv.addObject("data", messages);
 		return mv;
 	}                                       
 	
 	@RequestMapping("/stops/near")
 	public ModelAndView stopsNear(@RequestParam(value="latitude", required=true) double latitude, 
 			@RequestParam(value="longitude", required=true) double longitude) {
 		final ModelAndView mv = new ModelAndView(viewFactory.getJsonView());
 		mv.addObject("data", stopsService.findStopsNear(latitude, longitude));
 		return mv;
 	}
 	
 	@RequestMapping("/routes/near")
 	public ModelAndView routesNear(@RequestParam(value="latitude", required=true) double latitude, 
 			@RequestParam(value="longitude", required=true) double longitude) {
 		final ModelAndView mv = new ModelAndView(viewFactory.getJsonView());
 		mv.addObject("data", stopsService.findRoutesNear(latitude, longitude));
 		return mv;
 	}
 	
 	@RequestMapping("/stops/search")
 	public ModelAndView search(@RequestParam(value="q", required=true) String q) throws SolrServerException {
 		final ModelAndView mv = new ModelAndView(viewFactory.getJsonView());	
 		mv.addObject("data",  stopsService.search(q));
 		return mv;
 	}
 	
 	@RequestMapping("/route/{route}/{run}/stops")
 	public ModelAndView route(@PathVariable String route, @PathVariable int run) {
 		final ModelAndView mv = new ModelAndView(viewFactory.getJsonView());				
 		mv.addObject("data", stopsService.findStopsForRoute(route, run));				
 		return mv;
 	}
 	
 	@RequestMapping("/import")
 	public ModelAndView importRoutes() throws FileNotFoundException, InterruptedException {
 		final ModelAndView mv = new ModelAndView(viewFactory.getJsonView());
 		routeImportService.importRoutes();
 		return mv;
 	}
 	
 	@RequestMapping("/solr/update")
 	public ModelAndView updateSolr() throws SolrServerException, IOException {
 		final ModelAndView mv = new ModelAndView(viewFactory.getJsonView());
 		solrUpdateService.updateSolr();
 		return mv;
 	}
 	
 }
