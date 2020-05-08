 package com.porvak.bracket.controller;
 
 import com.porvak.bracket.domain.Tournament;
 import com.porvak.bracket.repository.TournamentRepository;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.social.connect.ConnectionRepository;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import javax.inject.Inject;
 import java.security.Principal;
 
 import static org.springframework.web.bind.annotation.RequestMethod.*;
 
 @Controller
 public class HomeController {
 
     @Inject
     private TournamentRepository tournamentRepository;
 
     @Inject
     private ConnectionRepository connectionRepository;
     
     @RequestMapping("/")
     public String home(Principal currentUser, Model model) {
         if (currentUser != null) {
             model.addAttribute("twitter_status", connectionRepository.findConnections("twitter").size() > 0 ? "Yes" : "No");
             model.addAttribute(((UsernamePasswordAuthenticationToken)currentUser).getPrincipal());
         }
         return "index";
     }
     
     @ResponseBody
    @RequestMapping(value = "/api/pub/tournament/{id}", method = GET)
     public Tournament getTournamentById(@PathVariable("id") String id){
         return tournamentRepository.findOne(id);
     }
     
 }
