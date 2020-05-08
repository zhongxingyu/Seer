 package se.wallenius.web;
 
 import java.util.List;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import se.wallenius.domain.RelatedSearchService;
 import se.wallenius.domain.Suggestion;
 
 @Controller
 @RequestMapping("search")
 public class TermController {
 
     @Autowired
     private RelatedSearchService relatedSearchService;
     
     
    @RequestMapping(value = "/{term}", method = RequestMethod.GET)
     @ResponseBody
     public List<Suggestion> getSuggestions(@PathVariable(value="term") final String searchTerm) {
         
         return this.relatedSearchService.findRelatedPhrases(searchTerm);
     }
 }
