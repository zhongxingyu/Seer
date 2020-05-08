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
import org.springframework.web.bind.annotation.*;
 
 @Controller
 public class PeliController {
 
     @Autowired
     private PeliService peliService;
     
     @Autowired
     private GenreService genreService;
 
    @RequestMapping(value = "/lisaaPeli",method = RequestMethod.POST)
     public String lisaa(@ModelAttribute Peli peli){
         peli = peliService.add(peli);
        return "redirect:/listaaPeli";
     }
 
    @RequestMapping(value = "/listaaPeli")
     public String listaa(Model model){
         model.addAttribute("pelit", peliService.listAll());
         return "listaa";
     }
     
     @RequestMapping(value = "/lisaa")
     public String lisaaKuuntelija(){
         return "lisaa";
     }
 }
