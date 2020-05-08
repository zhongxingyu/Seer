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
 
    @RequestMapping(value = "/lisaaPelaaja",method = RequestMethod.GET)
     public String lisaa(@ModelAttribute Peli peli){
        System.out.println("Pelin nimi: " + peli.getNimi());
         peli = peliService.add(peli);
        return "listaaPelaaja";
     }
 
    @RequestMapping(value = "/listaaPelaaja")
     public String listaa(Model model){
         model.addAttribute("pelit", peliService.listAll());
         return "listaa";
     }
     
     @RequestMapping(value = "/lisaa")
     public String lisaaKuuntelija(){
         return "lisaa";
     }
 }
