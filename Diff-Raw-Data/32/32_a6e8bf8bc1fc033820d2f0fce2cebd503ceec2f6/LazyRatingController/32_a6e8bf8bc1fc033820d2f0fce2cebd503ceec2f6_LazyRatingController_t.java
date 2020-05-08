 package com.hackaton.duma.controller;
 
 import com.hackaton.duma.dao.DeputyDAO;
import com.hackaton.duma.model.Deputy;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
 
 import javax.annotation.Resource;
import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: ernest
  * Date: 9/15/13
  * Time: 4:48 AM
  */
 @Controller
 @RequestMapping(value = "/laziest")
 public class LazyRatingController {
     @Resource(name = "deputyDAO")
     private DeputyDAO deputyDAO;
 
     @RequestMapping(method = RequestMethod.GET)
     public String laziestGET(Model model) {
         model.addAttribute("rating", deputyDAO.getLaziest(30, 0));
         return "ratings/laziest";
     }

    @RequestMapping(value = "/ajax.json", method = RequestMethod.GET)
    @ResponseBody
    public List<Deputy> laziestAJAX(@RequestParam(value = "offset", defaultValue = "0") Integer offset,
                                    @RequestParam(value = "limit", defaultValue = "20") Integer limit) {
        return deputyDAO.getLaziest(limit, offset);
    }
 }
