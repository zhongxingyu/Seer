 package net.canadensys.dataportal.vascan.controller;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import net.canadensys.dataportal.vascan.ChecklistService;
 import net.canadensys.dataportal.vascan.NameService;
 import net.canadensys.dataportal.vascan.SearchService;
 import net.canadensys.dataportal.vascan.TaxonService;
 import net.canadensys.dataportal.vascan.VernacularNameService;
 import net.canadensys.dataportal.vascan.config.VascanConfig;
 import net.canadensys.dataportal.vascan.model.NameConceptModelIF;
 import net.canadensys.dataportal.vascan.model.NameConceptTaxonModel;
 import net.canadensys.dataportal.vascan.model.NameConceptVernacularNameModel;
 import net.canadensys.exception.web.ResourceNotFoundException;
 import net.canadensys.query.LimitedResult;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.lang3.time.DateUtils;
 import org.apache.log4j.Logger;
 import org.json.JSONArray;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.scheduling.annotation.Scheduled;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.view.RedirectView;
 import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
 
 import freemarker.template.TemplateModelException;
 
 /**
  * Vascan controller.
  * 
  * @author canadensys
  *
  */
 @Controller
 public class VascanController {
 	//get log4j handler
 	private static final Logger LOGGER = Logger.getLogger(VascanController.class);
 	
 	public static final String JSON_CONTENT_TYPE = "application/json";
 	
 	@Autowired
 	private VascanConfig vascanConfig;
 	
 	@Autowired
 	private FreeMarkerConfigurer freemarkerConfig;
 	
 	@Autowired
 	private TaxonService taxonService;
 	
 	@Autowired
 	private VernacularNameService vernacularNameService;
 	
 	@Autowired
 	private NameService nameService;
 	
 	@Autowired
 	private ChecklistService checklistService;
 	
 	@Autowired
 	private SearchService searchService;
 	
 	
 	/**
 	 * Redirect the root to the search page
 	 * @param request
 	 * @return
 	 */
 	@RequestMapping(value="/", method=RequestMethod.GET)
 	public ModelAndView handleRoot(HttpServletRequest request){
 		RedirectView rv = new RedirectView("/search",true);
 		rv.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
 		ModelAndView mv = new ModelAndView(rv);
 		return mv;
 	}
 	
 	/**
 	 * Render taxon view for a given taxonId
 	 * @param taxonId
 	 * @return
 	 */
 	@RequestMapping(value={"/taxon/{taxonId}"}, method={RequestMethod.GET})
 	public ModelAndView handleTaxon(@PathVariable Integer taxonId){
 
 	    Map<String,Object> model = new HashMap<String,Object>();
 	    Object data = taxonService.retrieveTaxonData(taxonId);
 	    if(data == null){
 	    	throw new ResourceNotFoundException();
 	    }
 	    model.put("data",data);
 	    
 		return new ModelAndView("taxon", model);
 	}
 	
 	/**
 	 * Render a vernacular name view for a given vernacularNameId
 	 * @param id
 	 * @return
 	 */
 	@RequestMapping(value={"/vernacular/{vernacularNameId}"}, method={RequestMethod.GET})
 	public ModelAndView handleVernacular(@PathVariable Integer vernacularNameId){
 		
 		Map<String,Object> model = new HashMap<String,Object>();
 		Object vernacularNameData = vernacularNameService.loadVernacularNameModel(vernacularNameId);
 		
 	    if(vernacularNameData == null){
 	    	throw new ResourceNotFoundException();
 	    }
 	    model.put("vernacularName",vernacularNameData);
 	    Map<String,Object> extra = new HashMap<String,Object>();
 	    extra.put("isVernacular",true);
 	    model.put("extra",extra);
 	    return new ModelAndView("vernacular",model);
 	}
 	
 	/**
 	 * Render a name view for a given name string.
 	 * We use regex(.+) in the PathVariable to make sure we get the whole string and not only the part before the 'extension'.
 	 * Otherwise, a name like Carex arctata var. faxoni would be truncated to Carex arctata var and ". faxoni" would be interpreted as an extension.
 	 * @param name
 	 * @param redirect
 	 * @return
 	 */
 	@RequestMapping(value={"/name/{name:.+}"}, method={RequestMethod.GET})
 	public ModelAndView handleName(@PathVariable String name, @RequestParam(required=false) String redirect){
 		
 	    Map<String,Object> model = new HashMap<String,Object>();
 	    Map<String,Object> extra = new HashMap<String,Object>();
 
 	    Object data = nameService.retrieveNameData(name, redirect,extra);
 	    if( data == null){
 	    	throw new ResourceNotFoundException();
 	    }
 	    model.put("data",data); 
 	    model.put("extra",extra);
 	    
 		return new ModelAndView("name", model);
 	}
 	
 	/**
 	 * Render a checklist view for the given query parameters
 	 * @param request
 	 * @return
 	 */
 	@RequestMapping(value={"/checklist"}, method={RequestMethod.GET})
 	public ModelAndView handleChecklist(HttpServletRequest request){
 		
 		Map<String,Object> model = new HashMap<String,Object>();
 		
 		model.put("data", checklistService.retrieveChecklistData(request.getParameterMap()));
 		model.put("pageQuery",StringUtils.defaultString(request.getQueryString()));
 		
 		return new ModelAndView("checklist", model);
 	}
 	
 	/**
 	 * Render a search page
 	 * @param q name to search for
 	 * @param page page number starting at 1
 	 * @return
 	 */
 	@RequestMapping(value={"/search"}, method={RequestMethod.GET})
 	public ModelAndView handleSearch(@RequestParam(required=false) String q, @RequestParam(required=false,defaultValue="1") Integer page){
 		Map<String,Object> model = new HashMap<String,Object>();
 		
 	    HashMap<String,Object> search = new HashMap<String,Object>();
 	    search.put("term", "");
 	    search.put("total",0);
 
 	    List<Map<String,String>> searchResult = new ArrayList<Map<String,String>>();
 	    if(StringUtils.isNotBlank(q)){
 	    	search.put("term", q);
 	    	LimitedResult<List<NameConceptModelIF>> nameConceptModelList = null;
 	    	int pageIndex = (page <= 0)?0:(page-1);
 	    	//use page index +1 to avoid returning a bad page number
     		search.put("pageNumber", (pageIndex+1));
     		search.put("pageSize", searchService.getPageSize());
     		//check if we want another page than the first one
 	    	if(pageIndex > 0){
 	    		nameConceptModelList = searchService.searchName(q,pageIndex);
 	    	}
 	    	else{
 	    		nameConceptModelList = searchService.searchName(q);
 	    	}
 
 		    search.put("total",nameConceptModelList.getTotal_rows());
 		    List<Map<String,String>> searchResults = new ArrayList<Map<String,String>>();
 		    Map<String,String> searchRow = null;
 		    //TODO use objects directly instead of map
 		    for(NameConceptModelIF currNameConceptModel : nameConceptModelList.getRows()){
 		    	if(currNameConceptModel.getClass().equals(NameConceptTaxonModel.class)){
 		    		searchRow = new HashMap<String,String>();
 		    		searchRow.put("type","taxon");
 		    		searchRow.put("name", currNameConceptModel.getName());
 		    		searchRow.put("id", currNameConceptModel.getTaxonId().toString());
 		    		searchRow.put("status", currNameConceptModel.getStatus());
 		    		searchRow.put("namehtml",((NameConceptTaxonModel)currNameConceptModel).getNamehtml());
 		    		searchRow.put("namehtmlauthor",((NameConceptTaxonModel)currNameConceptModel).getNamehtmlauthor());
 		    		searchRow.put("rankname",((NameConceptTaxonModel)currNameConceptModel).getRankname());
 		    		searchRow.put("parentid",((NameConceptTaxonModel)currNameConceptModel).getParentid().toString());
 		    		searchRow.put("parentnamehtml",((NameConceptTaxonModel)currNameConceptModel).getParentnamehtml());
 		    		searchResult.add(searchRow);
 		    	}
 		    	else if(currNameConceptModel.getClass().equals(NameConceptVernacularNameModel.class)){
 		    		searchRow = new HashMap<String, String>();
 		    		searchRow.put("type","vernacular");
 		    		searchRow.put("name", currNameConceptModel.getName());
 		    		searchRow.put("id", Integer.toString(((NameConceptVernacularNameModel)currNameConceptModel).getId()));
 		    		searchRow.put("status", currNameConceptModel.getStatus());
 		    		searchRow.put("lang",((NameConceptVernacularNameModel)currNameConceptModel).getLang());
 		    		searchRow.put("taxonid",currNameConceptModel.getTaxonId().toString());
 		    		searchRow.put("taxonnamehtml",((NameConceptVernacularNameModel)currNameConceptModel).getTaxonnamehtml());
 		    		searchResult.add(searchRow);
 		    	}
 		    	else{
 		    		//logger
 		    		searchRow = null;
 		    	}
 		    	searchResults.add(searchRow);
 		    }
 		    model.put("results",searchResults);
 	    }
 	    
 	    model.put("search",search);
 	    return new ModelAndView("search", model);
 	}
 	
 	@RequestMapping(value={"/search.json"}, method={RequestMethod.GET})
 	public void handleSearchJson(@RequestParam String q,@RequestParam String t, HttpServletResponse response){
 		
 		//make sure the response is set as UTF-8
 		response.setCharacterEncoding("UTF-8");
 		response.setContentType(JSON_CONTENT_TYPE);
 		
 		List<NameConceptModelIF> nameConceptModelList = null;
 		if("taxon".equalsIgnoreCase(t)){
 			nameConceptModelList = searchService.searchTaxon(q);
 		}
 		else if("vernacular".equalsIgnoreCase(t)){
 			nameConceptModelList = searchService.searchVernacularName(q);
 		}
 		
 		JSONArray responseJson = new JSONArray();
 		for(NameConceptModelIF currNameConcept : nameConceptModelList){
 			responseJson.put(currNameConcept.getName());
 		}
 		try {
 			response.getWriter().print(responseJson.toString());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Scheduled task to check the last publication date of Vascan.
 	 */
 	@Scheduled(fixedDelay=DateUtils.MILLIS_PER_HOUR)
 	protected void checkLastPublicationDate(){
 		try {
 			String lpd = FileUtils.readFileToString(new File(vascanConfig.getLastPublicationDateFilePath()));
 			try {
 				freemarkerConfig.getConfiguration().setSharedVariable("lastPublicationDate", lpd);
 			} catch (TemplateModelException e) {
 				LOGGER.fatal("Could not set Vascan lastPublicationDate", e);
 			}
 		} catch (IOException e) {
 			LOGGER.fatal("Could not read Vascan lastPublicationDate", e);
 		}
 	}
 
 }
