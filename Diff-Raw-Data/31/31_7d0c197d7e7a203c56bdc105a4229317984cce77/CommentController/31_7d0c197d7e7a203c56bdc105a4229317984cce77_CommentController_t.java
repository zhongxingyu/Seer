 package org.tothought.spring.controllers.blog;
 
 import java.util.Date;
 
import javax.validation.Valid;

 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.tothought.entities.Comment;
 import org.tothought.repositories.CommentRepository;
 import org.tothought.repositories.PostViewRepository;
 import org.tothought.repositories.TagViewRepository;
import org.tothought.validators.CommentValidator;
 
 @Controller
 @RequestMapping(value="/comment")
 public class CommentController {
 
 	@Autowired
 	CommentRepository repository;
 	
 	@Autowired
 	PostViewRepository postViewRepository;
 
 	@Autowired
 	TagViewRepository tagViewRepository;
 	
 	@RequestMapping(value="/save")
	public String save(@Valid @ModelAttribute Comment comment, BindingResult result, Model model){
		if(result.hasErrors()){
			model.addAttribute("post", postViewRepository.findOne(comment.getPost().getPostId()));
			model.addAttribute("isSingle", true);
			model.addAttribute("tags", tagViewRepository.findAll());

			return "blog/post";
		}
		
 		String postId = comment.getPost().getPostId().toString();
 		comment.setPostedDt(new Date());
 		repository.save(comment);
 
 		return "redirect:/post/" + postId;
 	}
	
	@InitBinder("comment")
	public void initBinderAll(WebDataBinder binder){
		binder.setValidator(new CommentValidator());
	}
 }
