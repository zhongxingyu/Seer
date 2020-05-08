 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Tsoha.controller;
 
 import Tsoha.domain.Peli;
 import Tsoha.service.GenreService;
 import Tsoha.service.PeliService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 @Controller
 public class PeliController {
 
     @Autowired
     private PeliService peliService;
     
     @Autowired
     private GenreService genreService;
 
     @RequestMapping(value = "/home")
     public String home(Model model) {
         model.addAttribute("pelit", peliService.listAll());
         model.addAttribute("genret", genreService.listAll());
         return "index";
     }
     
     @RequestMapping(value = "poista/{peliId}")
     public String poista(@PathVariable Integer peliId){
         
         return "index";
     }
     
     @RequestMapping(value = "/test")
     public String test(){
         return "index";
     }
     
     @RequestMapping(value = "/main",method = RequestMethod.GET)
     public String main(Model model){
         model.addAttribute("pelit", peliService.listAll());
         return "main";
     }
     
     @RequestMapping(value = "/lisaa",method = RequestMethod.POST)
     public String lisaa(@ModelAttribute Peli peli){
        peli = peliService.lisaa(peli);
         return "main";
     }
 
 }
