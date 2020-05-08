 package org.tothought.spring.controllers.blog;
 
 import java.util.Date;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.tothought.entities.Comment;
 import org.tothought.repositories.CommentRepository;
 import org.tothought.repositories.PostViewRepository;
 import org.tothought.repositories.TagViewRepository;
 
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
	public String save(@ModelAttribute Comment comment, Model model){
 		String postId = comment.getPost().getPostId().toString();
 		comment.setPostedDt(new Date());
 		repository.save(comment);
 
 		return "redirect:/post/" + postId;
 	}
 }
