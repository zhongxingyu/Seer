 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Tsoha.controller;
 
 import Tsoha.domain.Peli;
 import Tsoha.domain.Genre;
 import Tsoha.domain.Kommentti;
 import Tsoha.domain.Arvostelu;
 import Tsoha.service.GenreService;
 import Tsoha.service.KommenttiService;
 import Tsoha.service.PeliService;
 import Tsoha.service.ArvosteluService;
 import java.util.List;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.*;
 
 @Controller
 public class PeliController {
 
     @Autowired
     private PeliService peliService;
     @Autowired
     private GenreService genreService;
     @Autowired
     private KommenttiService kommenttiService;
     @Autowired
     private ArvosteluService arvosteluService;
 
     @RequestMapping(value = "/")
     public String kuuntele() {
         return "redirect:/home";
     }
 
     @RequestMapping(value = "lisaaPeli", method = RequestMethod.POST)
     public String lisaa(@ModelAttribute Peli peli) {
         peli = peliService.add(peli);
         return "redirect:/listaa";
     }
 
     @RequestMapping(value = "lisaaGenre", method = RequestMethod.POST)
     public String lisaaGenre(@ModelAttribute Genre genre) {
         genre = genreService.add(genre);
         return "redirect:/listaa";
     }
 
     
     @RequestMapping(value = "listaa")
     public String listaaPelitJaGenretJaArvostelut(Model model) {
         model.addAttribute("pelit", peliService.listAll());
         model.addAttribute("genret", genreService.listAll());
         model.addAttribute("arvostelut", arvosteluService.listAll());
         return "listaa";
     }
 
     @RequestMapping(value = "kommentoi/{peliId}")
     public String kommentoiPelia(@PathVariable Integer peliId, Model model) {     
         model.addAttribute("peli", peliService.findPeli(peliId));
         return "kommentoi";
     }
       
     @RequestMapping(value = "poista/{peliId}")
     public String poistaPeli(@PathVariable Integer peliId) {
         Peli peli = peliService.findPeli(peliId);
         peliService.remove(peli);
         return "redirect:/listaa";
     }
     @RequestMapping(value="/404.html")
     public String error404() {
         return "error";
     }
     
     @RequestMapping(value = "poistaLaina/{peliId}")
     public String poistaLainasta(@PathVariable Integer peliId) {
         Peli peli = peliService.findPeli(peliId);
         peli.setLainassa(null);
         peliService.add(peli);
         return "redirect:/listaa";
     }
     
     @RequestMapping(value = "lisaaGenreen/{peliId}")
     public String liitaGenreen(@PathVariable Integer peliId, Model model) {
         model.addAttribute("peli", peliService.findPeli(peliId));
         model.addAttribute("genres", genreService.listAll());
         return "liitaGenreen";
     }
     
     @RequestMapping(value = "arvostele/{peliId}")
     public String arvostele(@PathVariable Integer peliId, Model model) {
         model.addAttribute("peli", peliService.findPeli(peliId));
         return "arvostele";
     }
     
     @RequestMapping(value = "lainaa/{peliId}")
     public String lainaa(@PathVariable Integer peliId, Model model){
         model.addAttribute("peli", peliService.findPeli(peliId));
         return "lainaa";
     }
     
     @RequestMapping(value = "lainassaHenkilolla")
     public String lainassa(Model model, @RequestParam String lainassa){
         model.addAttribute("pelit", peliService.findByLainassa(lainassa));
         return "lainassa";
     }
 
     
     @RequestMapping(value = "lisaaPeliGenreen/{peliId}/{genreId}")
     public String liitaPeliGenreen(@PathVariable Integer peliId, @PathVariable Integer genreId){
         Peli peli = peliService.findPeli(peliId);
         Genre genre = genreService.findGenre(genreId);
         peli.setGenre(genre);
        genre.getPelit().add(peli);
         peliService.add(peli);
         genreService.add(genre);
         return "redirect:/listaa";
     }
 
     @RequestMapping(value = "lisaaKommentti/{peliId}")
     public String lisaaKommentti(@ModelAttribute Kommentti kommentti, @PathVariable Integer peliId) {          
         kommenttiService.add(kommentti); 
         Integer id = kommentti.getId();
         Kommentti comment = kommenttiService.findKommentti(id);
         comment.setPeli(peliService.findPeli(peliId));
         comment = kommenttiService.add(comment);
         Peli peli = peliService.findPeli(peliId);
         peli.getKommentit().add(kommentti);
         peliService.add(peli); 
         return "redirect:/listaa";
     }
     
     @RequestMapping(value = "lisaaArvostelu/{peliId}")
     public String lisaaArvostelu(@ModelAttribute Arvostelu arvostelu, @PathVariable Integer peliId) {          
         arvosteluService.add(arvostelu); 
         Integer id = arvostelu.getId();
         Arvostelu rate = arvosteluService.findArvostelu(id);
         rate.setPeli(peliService.findPeli(peliId));
         rate = arvosteluService.add(rate);
         Peli peli = peliService.findPeli(peliId);
         peli.getArvostelut().add(rate);
         peliService.add(peli); 
         return "redirect:/listaa";
     }
     
     @RequestMapping(value = "lainaaPeli/{peliId}")
     public String lainaaPeli(@PathVariable Integer peliId,@RequestParam String lainassa) {
         Peli peli = peliService.findPeli(peliId);
         peli.setLainassa(lainassa);
         peliService.add(peli);
         return "redirect:/listaa";
     }
     
     
 
     @RequestMapping(value = "/lisaa")
     public String lisaaKuuntelija() {
         return "lisaa";
     }
 
     @RequestMapping(value = "/home")
     public String home() {
         return "home";
     }  
     
     @RequestMapping(value = "/error")
     public String error(){
         return "error";
     }
     
 }
