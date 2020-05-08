 package com.portal.controller;
 
 import java.util.Date;
 
 import javax.inject.Inject;
 import javax.validation.Valid;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.multipart.MultipartFile;
 
 import com.portal.domain.core.Blogger;
 import com.portal.domain.core.Post;
 import com.portal.service.BloggerService;
 import com.portal.service.PostService;
 
 @Controller
 @RequestMapping("/bloggers")
 public class PostController {
 	
 	@Inject
 	PostService postService;
 	
 	@Inject
 	BloggerService bloggerService;
 
 	@RequestMapping(method = RequestMethod.POST, value = "/{login}/createNewPost")
 	public String createNewBloggerPost(@Valid Post post,
 			BindingResult bindingResult, @PathVariable String login, Model model) {
 		Blogger blogger = bloggerService.getBloggerByLogin(login);
 		model.addAttribute(blogger);
 		if (bindingResult.hasErrors()) {
 			return "/bloggers/postList";
 		}
 		post.setBlogger(blogger);
 		post.setLastUpdatedDate(new Date());
 		postService.savePost(post);
 		return "redirect:/bloggers/" + login;
 	}
 }
