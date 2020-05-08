 package com.lateralthoughts.devinlove.controller;
 
import com.google.common.collect.Iterables;
 import com.google.common.collect.Maps;
 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.annotation.QueryType;
 import org.springframework.data.neo4j.conversion.Result;
 import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.data.neo4j.support.query.QueryEngine;
 import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import static com.google.common.collect.Iterables.addAll;
 import static com.google.common.collect.Lists.newArrayList;
 import static org.springframework.data.neo4j.annotation.QueryType.Cypher;
 import static org.springframework.web.bind.annotation.RequestMethod.GET;
 import static org.springframework.web.bind.annotation.RequestMethod.POST;
 
 @Controller
 public class SandboxController {
     @Autowired
     private Neo4jTemplate template;
 
     @RequestMapping(value = "/sandbox.html", method = GET)
     public ModelAndView executeQuery(RedirectAttributes redirectAttributes) {
         ModelAndView modelAndView = new ModelAndView("sandbox");
 
         Map<String,?> flashAttributes = redirectAttributes.getFlashAttributes();
         if(!flashAttributes.isEmpty()) {
             modelAndView.addObject("results", flashAttributes.get("results"));
         }
         return modelAndView;
     }
 
     @RequestMapping(value = "/sandbox.html", method = POST)
     public String executeQuery(@RequestParam(value = "cypherQuery") String query, RedirectAttributes redirectAttributes) {
         List<Object> results = getResultsOrErrorCause(query);
         redirectAttributes.addFlashAttribute("results", results);
         return "redirect:sandbox.html";
     }
 
     private List<Object> getResultsOrErrorCause(String query) {
         List<Object> results = newArrayList();
         try {
             addAll(results, executeCypherQuery(query));
         }
         catch (Exception e) {
             results.add(e.getMessage());
         }
         return results;
     }
 
     private Result<Object> executeCypherQuery(String query) {
         return template.queryEngineFor(Cypher).query(
                 query,
                 Maps.<String, Object>newHashMap()
         );
     }
 
 }
