 package com.lateralthoughts.devinlove.controller;
 
 import com.lateralthoughts.devinlove.domain.Mascot;
 import com.lateralthoughts.devinlove.repository.MascotRepository;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.neo4j.support.Neo4jTemplate;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import java.util.Arrays;
 
 @Controller
 public class HomeController {
     @Autowired
     MascotRepository mascotRepository;
 
     @RequestMapping(value = "/index.html")
     public String index(Model model) throws Exception {
         Mascot m = new Mascot();
         m.name = "Django Pony";
         mascotRepository.save(m);
         model.addAttribute("message", "test");
         model.addAttribute("latestMascots", Arrays.asList(new String[]{"a", "b", "c"}));
        return "index";
     }
 }
