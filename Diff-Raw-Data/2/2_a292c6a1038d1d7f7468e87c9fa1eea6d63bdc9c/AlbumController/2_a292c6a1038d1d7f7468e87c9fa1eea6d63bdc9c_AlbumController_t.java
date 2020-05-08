 package com.herokuapp.webgalleryshowcase.web.gallery;
 
 import com.herokuapp.webgalleryshowcase.dao.AlbumDao;
 import com.herokuapp.webgalleryshowcase.domain.Album;
 import com.herokuapp.webgalleryshowcase.domain.dto.AlbumResponseDto;
 import com.herokuapp.webgalleryshowcase.service.validators.AlbumValidator;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.*;
 
 import javax.validation.Valid;
 import java.security.Principal;
 import java.util.List;
 
 @Controller
 public class AlbumController {
 
     private final Logger log = LoggerFactory.getLogger(AlbumController.class);
 
     @Autowired
     private AlbumDao albumDao;
 
     @InitBinder
     protected void initBinder(WebDataBinder binder) {
         binder.setValidator(new AlbumValidator());
     }
 
     @RequestMapping(value = "/albums", method = RequestMethod.GET, produces = "application/json")
     public
     @ResponseBody
     List<Album> listAlbums(Principal principal) {
         log.debug("Album list in JSON");
         String userEmail = principal.getName();
         return albumDao.retrieveUserAlbums(userEmail);
     }
 
     @RequestMapping(value = "/albums", method = RequestMethod.GET)
     public String showUserAlbums(Model model, Principal principal) {
 
         String userOwner = principal.getName();
         List<Album> albums = albumDao.retrieveUserAlbums(userOwner);
 
         model.addAttribute("albums", albums);
         model.addAttribute("userOwner", userOwner);
         return "showUserAlbumList";
     }
 
     @RequestMapping(value = "/albums/manage", method = RequestMethod.GET)
     public String manageUserAlbums(Model model, Principal principal) {
 
         String userOwner = principal.getName();
         List<Album> albums = albumDao.retrieveUserAlbums(userOwner);
 
         model.addAttribute("albums", albums);
         model.addAttribute("userOwner", userOwner);
 
         return "manageAlbumList";
     }
 
     @RequestMapping(value = "/albums/{id}")
     private String displayAlbum(@PathVariable int id, Model model) {
         model.addAttribute(albumDao.retrieveAlbum(id));
         return "showAlbumPictures";
     }
 
     @RequestMapping(value = "/albums/{id}", method = RequestMethod.GET, produces = "application/json")
     public
     @ResponseBody
     Album getAlbum(@PathVariable int id) {
         return albumDao.retrieveAlbum(id);
     }
 
     @RequestMapping(value = "/albums/{id}", method = RequestMethod.DELETE)
     public ResponseEntity<String> deleteAlbum(@PathVariable int id) {
        log.debug("Delete album. ID: " + id);
         ResponseEntity<String> response;
 
         if (albumDao.deleteAlbum(id)) {
             response = new ResponseEntity<>("Album has been deleted successfully.", HttpStatus.OK);
         } else {
             response = new ResponseEntity<>(HttpStatus.NO_CONTENT);
         }
         return response;
     }
 
     @RequestMapping(value = "/albums/{id}", method = RequestMethod.PUT)
     public ResponseEntity<AlbumResponseDto> editAlbumE(@RequestBody @Valid Album album, @PathVariable int id) {
         log.debug("editAlbum method PUT");
         album.setId(id);
         Album updatedAlbum = albumDao.updateAlbum(album);
 
         String message = "Album has been edited.";
         AlbumResponseDto albumResponse = new AlbumResponseDto(message, updatedAlbum);
 
         return new ResponseEntity<>(albumResponse, HttpStatus.OK);
     }
 
     @RequestMapping(value = "/albums", method = RequestMethod.POST)
     public ResponseEntity<AlbumResponseDto> createAlbum(@RequestBody @Valid Album album, Principal principal) {
         album.setUserOwner(principal.getName());
         Album createdAlbum = albumDao.createAlbum(album);
 
         String message = "Album has been created successfully.";
         AlbumResponseDto albumResponse = new AlbumResponseDto(message, createdAlbum);
 
         return new ResponseEntity<>(albumResponse, HttpStatus.CREATED);
     }
 }
