 package com.mirantis.aminakov.bigdatacourse.webapp;
 
 import java.io.IOException;
 import java.util.Map;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.multipart.MultipartFile;
 
 import com.mirantis.aminakov.bigdatacourse.dao.Book;
 import com.mirantis.aminakov.bigdatacourse.service.Service;
 
 @Controller
 public class AddBookController {
 
 	private Service service;
 	
 	@Autowired
 	public void setService(Service service) {
 		this.service = service;
 	}
 	
 	@RequestMapping(method=RequestMethod.GET)
 	public String addForm() {
 		return "addbook";
 	}
 	
 	@RequestMapping("/")
     public String home() {
         return "redirect:/welcome";
     }
 	
 	@RequestMapping(value = "/search", method=RequestMethod.GET)
	public String getAllBooks(Map<String, Object> map, String search, String by, int pageNum, int pageSize) {
		pageNum = 1;
		pageSize = 10;
		if (search.equalsIgnoreCase("")) {
 			map.put("books", service.getAllBooks(pageNum, pageSize));
		} else {
 			
		}
 		
 		return "search";
 	}
 	
 	/*@RequestMapping(value = "/search", method=RequestMethod.POST)
 	public String findBooks(Map<String, Object> map) {
 		int pageNum = 1;
 		int pageSize = 10;
 		map.put("books", service.getAllBooks(pageNum, pageSize));
 		return "search";
 	}*/
 	
 	@RequestMapping(value = "/addbook", method=RequestMethod.POST)
 	public String processUpload(@ModelAttribute("book") Book book, @RequestParam MultipartFile file, Model model) throws IOException {
 		try {
 			if(!file.isEmpty()) {
 				validateFile(file);
 				book.setText(file.getInputStream());
 			}
 		} catch (ContentTypeError e) {
 			model.addAttribute("message", e.getMessage());
 			return "upload";
 		}
 		int id = 0;
 		id = service.addBook(book);
 		if (id != 0) {
 			model.addAttribute("message", "File '" + file.getOriginalFilename() + "' uploaded successfully");
 		} else {
 			model.addAttribute("message", "File '" + file.getOriginalFilename() + "' upload failed");
 		}
 		model.addAttribute("book", book);
 		model.addAttribute("id", id);
 		model.addAttribute("text", book.getText());
 		return "upload";
 	}	
 	
 	private void validateFile(MultipartFile file) throws ContentTypeError {
 		if (!file.getContentType().equals("text/plain")) {
 			throw new ContentTypeError("ERROR. Only *.txt accepted.");
 		}
 	}
 }
