 package com.directi.train.tweetapp.controllers;
 
 import com.directi.train.tweetapp.model.UserProfileItem;
 import com.directi.train.tweetapp.services.SearchStore;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

 import javax.servlet.http.HttpSession;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: elricl
  * Date: 27/7/12
  * Time: 2:53 PM
  * To change this template use File | Settings | File Templates.
  */
 @Controller
 public class SearchController {
     private SearchStore searchStore;
 
     @Autowired
     public SearchController(SearchStore searchStore) {
         this.searchStore = searchStore;
     }
 
     @RequestMapping(value = "search/{query}/json", method = RequestMethod.GET)
     @ResponseBody
     public List<UserProfileItem> favoriteTweet(@PathVariable("query") String query) {
         return searchStore.getResults(query);
     }

    @RequestMapping(value = "search",method = RequestMethod.GET)
    public ModelAndView search() {
        return new ModelAndView();
    }

 }
