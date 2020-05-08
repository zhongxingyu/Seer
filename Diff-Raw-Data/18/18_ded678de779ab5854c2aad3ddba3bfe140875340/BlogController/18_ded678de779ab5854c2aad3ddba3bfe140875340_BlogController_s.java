 package example.springmvc.controller;
 
 import java.security.Principal;
 import java.util.Date;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 import example.springmvc.model.blog.BlogEntry;
 import example.springmvc.model.blog.BlogEntryFormData;
 import example.springmvc.model.blog.BlogEntryFormDataValidator;
 import example.springmvc.model.blog.BlogEntryStorage;
 import example.springmvc.model.users.User;
 import example.springmvc.model.users.UserStorage;
 
 @Controller
 @RequestMapping(value="blog")
 public class BlogController {
 
 	@Autowired
 	private BlogEntryStorage blogStorage;
 	
 	@Autowired
 	private UserStorage userStorage;
 	
 	@RequestMapping(value = "", method = RequestMethod.GET)
 	public ModelAndView getIndexPage() {
 		List<BlogEntry> entries = this.blogStorage.findAll();
 		return new ModelAndView("blog/index", "entries", entries);
 	}
 	
 	@RequestMapping(value = "create", method = RequestMethod.GET)
 	public ModelAndView getCreatePage() {
 		return new ModelAndView("blog/create", "blogEntryFormData", new BlogEntryFormData());
 	}
 	
 	@RequestMapping(value = "create", method = RequestMethod.POST)
 	public ModelAndView createBlogEntry(BlogEntryFormData formData, Principal principal, BindingResult bindingResult) {
 		new BlogEntryFormDataValidator().validate(formData, bindingResult);
 		if (bindingResult.hasErrors()) {
 			return new ModelAndView("blog/create");
 		} else {
 			// the id is empty and will be set when we save the blog entry for
 			// the first time into the database
 			User user = this.getUserFromPrincipal(principal);
			BlogEntry entry = new BlogEntry("", formData.getText(), user);
 			entry.addTags(formData.getTags());
 			entry.setCreationDate(new Date());
 			this.blogStorage.saveOrUpdate(entry);
 			return new ModelAndView("redirect:/blog");
 		}
 	}
 	
 	@RequestMapping(value = "update/{entryId}", method = RequestMethod.GET)
 	public ModelAndView getBlogEntry(@PathVariable String entryId) {
 		BlogEntry entry = this.blogStorage.byId(entryId);
 		if (entry == null) throw new ResourceNotFoundException("exception.blogEntry.notFound");
 		BlogEntryFormData formData = new BlogEntryFormData(entry);
 		return new ModelAndView("blog/update", "entry", formData);
 	}
 	
 	@RequestMapping(value = "update/{entryId}", method = RequestMethod.POST)
 	public ModelAndView changeBlogEntry(@PathVariable String entryId, BlogEntryFormData entry) {
 		BlogEntry blogEntry = this.blogStorage.byId(entryId);
 		if (entry == null) throw new ResourceNotFoundException("exception.blogEntry.notFound");
 		blogEntry.addTags(entry.getTags());
 		this.blogStorage.saveOrUpdate(blogEntry);
 		return new ModelAndView("blog/update", "entry", entry);
 	}
 	
 	private User getUserFromPrincipal(Principal principal) {
 		if (principal != null) {
 			return this.userStorage.byId(principal.getName());
 		} else {
 			return null;
 		}
 	}
 	
 	
 	public BlogEntryStorage getBlogStorage() {
 		return blogStorage;
 	}
 
 	public void setBlogStorage(BlogEntryStorage blogStorage) {
 		this.blogStorage = blogStorage;
 	}
 
 	public UserStorage getUserStorage() {
 		return userStorage;
 	}
 
 	public void setUserStorage(UserStorage userStorage) {
 		this.userStorage = userStorage;
 	}
 
 }
