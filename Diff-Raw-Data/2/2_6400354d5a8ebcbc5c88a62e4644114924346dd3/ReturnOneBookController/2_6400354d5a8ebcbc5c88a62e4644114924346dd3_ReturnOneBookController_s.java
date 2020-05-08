 package de.codecentric.psd.worblehat.web.controller;
 
 import javax.inject.Inject;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import de.codecentric.psd.worblehat.domain.BookService;
 import de.codecentric.psd.worblehat.web.command.ReturnOneBookFormData;
 
 /**
  * Controller class for the
  * 
  * @author psd
  * 
  */
 @Controller
@RequestMapping("/returnOneBook")
 public class ReturnOneBookController {
 
 	ValidateReturnOneBook validateReturnOneBook = new ValidateReturnOneBook();
 
 	@Inject
 	private BookService bookService;
 
 	@RequestMapping(method = RequestMethod.GET)
 	public void prepareView(ModelMap modelMap) {
 		modelMap.put("returnOneBookFormData", new ReturnOneBookFormData());
 	}
 
 	@RequestMapping(method = RequestMethod.POST)
 	public String returnOneBook(
 			ModelMap modelMap,
 			@ModelAttribute("returnOneBookFormData") ReturnOneBookFormData formData,
 			BindingResult result) {
 		validateReturnOneBook.validate(formData, result);
 		if (result.hasErrors()) {
 			return "/returnOneBook";
 		} else {
 			bookService.returnOneBookByBorrower(formData.getISBNNumber(),
 					formData.getEmailAddress());
 			return "/home";
 		}
 	}
 
 	public void setBookService(BookService bookService) {
 		this.bookService = bookService;
 	}
 }
