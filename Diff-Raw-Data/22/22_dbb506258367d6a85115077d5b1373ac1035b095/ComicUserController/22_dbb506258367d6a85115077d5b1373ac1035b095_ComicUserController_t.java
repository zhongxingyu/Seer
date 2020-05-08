 package com.tekton.comicrate;
 
 import java.util.Locale;
 import java.util.Map;
 import java.util.Iterator;
 
 import javax.servlet.http.HttpServletRequest;
 
 import com.tekton.comicrate.forms.*;
 //import com.tekton.comicrate.home.admin.*;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.SessionAttributes;
 
 @Controller
 @SessionAttributes
 public class ComicUserController extends SQLController {
 	/*
 	 * 
 	 * *** IMPORTANT*** this class is being EOLd and phased out
 	 * 
 	 * This is the class for intercepting most of the /user requests.
 	 * Note: some of them may be in "ComicController" as well
 	 * 
 	 * This is now redundant with Comic.java
 	 * 
 	 */
 	
 	public String user;
 	
 	@RequestMapping(value="/user/comic/{id}", method=RequestMethod.GET)
 	public String user_comic(Model model, Locale locale, @PathVariable("id") String id) {
 		//Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 		//this.user = auth.getName(); //get logged in username; we'll be needing that to find the comic...
 		
 		this.createConnection();
 		
 		Comic comic = new Comic(this.conn);
 		comic.setId(id);
 		comic.getComicFromDB();
 		comic.validate_to_zero();
 		
 		model.addAttribute("commandName", "Comic");
 		
 		model.addAttribute("comic", comic);
 		
 		this.closeConnection();
 		
 		if(comic.overall != "") {
 			return "comic_show_one_user";
 		} else if(comic.comic_exists == false) {
 			return "comic_no_exists";
 		} else {
 			return "comic_none";
 		}
 	}
 	
 	@RequestMapping(value="/user/comic/edit/{id}", method=RequestMethod.GET)
 	public String edit_user_comic(Model model, Locale locale, @PathVariable("id") String id) {
 		this.createConnection();
 		
 		Comic comic = new Comic(this.conn);
 		//comic.user = this.user;
 		comic.setId(id);
 		comic.getComicFromDB();
 		comic.validate_to_zero();
 		
 		System.out.println("ComicUserController 2:: "+comic.reportComic());
 		
 		model.addAttribute("comic", comic);
 		
 		this.closeConnection();
 		
 		return "comic_edit";
 		
 		/*
 		if(comic.comic_exists == false) {
 			return "comic_no_exists";
 		} else {
 			return "comic_edit";
 		}
 		*/
 	}
 	
 	@RequestMapping(value="/user/comic/edit/{id}", method=RequestMethod.POST)
 	public String post_edit_user_comic(@ModelAttribute("Comic") Comic comic, BindingResult result, Model model, Locale locale, @PathVariable("id") String id, HttpServletRequest request) {
 		
 		Map<?, ?> x = request.getParameterMap();
 		
 		/*for(Map.Entry<String, String[]> param : x.entrySet()) {
 			
 		}*/
 		
 		
 		Iterator<?> i = x.entrySet().iterator();
 		while(i.hasNext()) {
 			@SuppressWarnings("rawtypes")
 			Map.Entry entry = (Map.Entry)i.next();
 			System.out.print(entry.getKey() + " ");
 			System.out.print(entry.getValue()+ "\n");
 		}
 		
 		comic.setId(id);
 		comic.updateUserComic();
 		
 		System.out.println("ComicUserController 4:: "+comic.reportComic());
 		
 		model.addAttribute("comic", comic);
 		
 		//return "comic_edit";
 		return "redirect:/user/comic/"+id;
 		
 		// TODO: make sure the comic realy exists and something was updated... 
 		/*
 		if(comic.comic_exists == false) {
 			return "comic_no_exists";
 		} else {
 			return "redirect:/user/comic/"+id;
 		}
 		*/
 	}
 	
 	@RequestMapping(value="/user/comic/json/update/{slot}", method=RequestMethod.POST)
 	public String update_slot_json(Model model, HttpServletRequest request, @PathVariable("slot") String slot) {
		
		System.out.println("json slot update called for :: "+slot);
		
 		String id = request.getParameter("id");
 		String val = request.getParameter("val");
 		
 		model.addAttribute("slot", slot);
 		model.addAttribute("id", id);
 		model.addAttribute("val", val);
 		
 			this.createConnection();
 		
 			Comic comic = new Comic(this.conn);
 			comic.setId(id);
 			comic.getComicFromDB();
 			
			if(slot.equals("overall")) {
				System.out.println("json slot update called for overall");
 				comic.setOverall(val);
 				comic.updateUserComic();
			} else if (slot.equals("art")) {
 				comic.setArt(val);
 				comic.updateUserComic();
			} else if (slot.equals("story")) {
 				comic.setStory(val);
 				comic.updateUserComic();
			} else if (slot.equals("next")) {
 				comic.setLikely_to_buy_next(val);
 				comic.updateUserComic();
 			}
 			
 			/*//requires 1.7
 			switch(slot) {
 				case "overall":
 					comic.setOverall(val);
 					comic.updateUserComic();
 					break;
 				case "art":
 					comic.setArt(val);
 					comic.updateUserComic();
 					break;
 				case "story":
 					comic.setStory(val);
 					comic.updateUserComic();
 					break;
 				case "next":
 					comic.setLikely_to_buy_next(val);
 					comic.updateUserComic();
 					break;
 			}*/
 		
 			this.closeConnection();
 			
 		return "json_slot";
 	}
 }
