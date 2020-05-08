 package name.stokito.controllers;
 
 import name.stokito.units.BookBean;
 import name.stokito.units.TableModel;
 import name.stokito.service.UserService;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.annotation.Resource;
import java.util.Map;
 
 /**
  * @author november
  * @since 5/27/12
  */
 @Controller
 public class IndexController {
 
 	@Resource
 	UserService userService;
 
 	@RequestMapping("/")
 	public String index() {
 		return "index";
 	}
 
 	@RequestMapping("/game")
 	public String game() {
 		return "/games/red_square/game";
 	}
 
 	@RequestMapping("/formreg")
 	public String formReg() {
 		return "/registration/regForm";
 	}
 
 	@RequestMapping("/contacts")
 	public String contacts() {
 		return "/contacts/contact";
 	}
 
 	@RequestMapping("/resume")
 	public String resume() {
 		return "/resume/resume";
 	}
 
 	@RequestMapping("/books")
 	public String books() {
 		return "/books/books";
 	}
 
 	@RequestMapping(value = "/books/getdatabooks", method = RequestMethod.POST)
 	@ResponseBody
	public Map<String, Object> ajaxGetDataBooks() {
 		return userService.getSelect("SELECT * FROM books");
 	}
 
 	@RequestMapping(value = "/books/addbook", method = RequestMethod.POST)
 	@ResponseBody
 	public boolean ajaxAddBook(@RequestParam(value = "iNameBook", required = true) String iNameBook,
 		@RequestParam(value = "iLinkBook", required = true) String iLinkBook,
 		@RequestParam(value = "iAuthorBook", required = true) String iAuthorBook,
 		@RequestParam(value = "iPhotoLink", required = true) String iPhotoLink,
 		@RequestParam(value = "iDescriptionBook", required = true) String iDescriptionBook) {
 		return userService.addBook(new BookBean(iNameBook, iLinkBook, iAuthorBook, iPhotoLink,iDescriptionBook));
 	}
 
	@RequestMapping("/books/bookdescription")
	public String bookdescription() {
		return "/books/bookdescription";
	}

	@RequestMapping(value = "/books/selectbook", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> ajaxSelectBook(@RequestParam(value = "id", required = false) int id) {
		return userService.getSelect("SELECT * FROM books WHERE booksid = " + id);
 	}
 }
