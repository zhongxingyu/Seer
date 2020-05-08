 package nl.codebasesoftware.produx.controller;
 
 
 import nl.codebasesoftware.produx.exception.ProduxServiceException;
 import nl.codebasesoftware.produx.exception.ResourceNotFoundException;
 import nl.codebasesoftware.produx.search.criteria.SearchCriteria;
 import nl.codebasesoftware.produx.search.criteria.facet.FacetField;
 import nl.codebasesoftware.produx.search.criteria.filter.Filter;
 import nl.codebasesoftware.produx.search.result.ResultListing;
 import nl.codebasesoftware.produx.search.result.SearchResult;
 import nl.codebasesoftware.produx.service.PageBlockService;
 import nl.codebasesoftware.produx.service.SearchService;
 import nl.codebasesoftware.produx.service.business.FilterFromUrlExtractor;
 import nl.codebasesoftware.produx.util.Properties;
 import nl.codebasesoftware.produx.util.StringUtil;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * User: rvanloen
  * Date: 2-2-13
  * Time: 12:46
  */
 @Controller
 public class SearchController {
 
     private SearchService searchService;
     private Properties properties;
     private PageBlockService pageBlockService;
 
 
     @Autowired
     public SearchController(SearchService searchService, Properties properties, PageBlockService pageBlockService) {
         this.searchService = searchService;
         this.properties = properties;
         this.pageBlockService = pageBlockService;
     }
 
     @RequestMapping(value = "/search", method = RequestMethod.GET)
     public String filterPageBla(@RequestParam("for") String terms, Model model) {
         return process(model, terms, "", 0);
     }
 
     @RequestMapping(value = "/search/{filters}", method = RequestMethod.GET)
     public String filterPage(@RequestParam(value = "for", required = false) String terms, @PathVariable("filters") String filters, Model model) {
         if (StringUtil.isNullOrEmpty(terms)) {
             throw new ResourceNotFoundException();
         }
         return process(model, terms, filters, 0);
     }
 
 
     @RequestMapping(value = "/search/p{page:[0-9]+}", method = RequestMethod.GET)
     public String search(@RequestParam(value = "for", required = false) String terms,
                          @PathVariable("page") Integer page, Model model) {
         if (terms == null) {
             throw new ResourceNotFoundException();
         }
         return process(model, terms, null, page);
     }
 
     @RequestMapping(value = "/search/{filters}/p{page:[0-9]+}", method = RequestMethod.GET)
     public String search(@RequestParam(value = "for", required = false) String terms,
                          @PathVariable("filters") String filters,
                          @PathVariable("page") Integer page, Model model) {
         if (terms == null) {
             throw new ResourceNotFoundException();
         }
         return process(model, terms, filters, page);
     }
 
     private String process(Model model, String terms, String filters, int page) {
 
         int resultsPerPage = properties.getSearchResultsPerPage();
 
         List<Filter> filterList = new FilterFromUrlExtractor().stringToFilters(filters);
 
         FacetField regionsFacet = searchService.createRegionsFacet(filterList);
         FacetField priceFacet = searchService.createPriceFacet(filterList);
         FacetField tagsFacet = searchService.createTagsFacet();
 
         SearchCriteria criteria = new SearchCriteria.Builder()
                 .addFilters(filterList)
                 .addFacetField(priceFacet)
                 .addFacetField(regionsFacet)
                 .addFacetField(tagsFacet)
                 .setStart(page * resultsPerPage)
                 .setRows(resultsPerPage)
                 .setQuery(terms)
                 .build();
 
         SearchResult result = null;
         try {
             result = searchService.findCourses(criteria, Arrays.asList("search"));
         } catch (ProduxServiceException e) {
             e.printStackTrace();
         }
 
         ResultListing.Builder listingBuilder = new ResultListing.Builder();
 
         listingBuilder.setCriteria(criteria).setSearchResult(result);
 
        if (!StringUtil.isNullOrEmpty(filters)) {
             listingBuilder.setFilters(filters);
         }
 
         ResultListing listing = listingBuilder.build();
 
         pageBlockService.setEmptyRightColumn(model);
         model.addAttribute("facetedSearch", true);
         model.addAttribute("mainContent", "content/searchResults");
         model.addAttribute("dir", "search");
         model.addAttribute("resultListing", listing);
         model.addAttribute("searchResult", result);
         return "main";
     }
 
 }
