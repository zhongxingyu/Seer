 package com.appwish.web;
 import com.appwish.domain.Comment;
 import com.appwish.domain.Idea;
 import com.appwish.domain.Like;
 import com.appwish.domain.UserAccount;
 
 import org.springframework.roo.addon.web.mvc.controller.json.RooWebJson;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 @RooWebJson(jsonObject = Idea.class)
 @Controller
 @RequestMapping("/ideas")
 public class IdeaController {
 	
 	@RequestMapping(value="createmockups", method=RequestMethod.GET)
 	public void createMockups(){
 		UserAccount user1 = new UserAccount();
     	user1.setName("user1");
     	user1.setEmail("user1@test.com");
     	user1.setCurrentDate();
     	
     	Comment comment = new Comment();
     	comment.setBody("long ass first comment");
     	comment.setUserAccount(user1);
     	comment.setCurrentDate();
     	
     	Comment comment1 = new Comment();
     	comment1.setBody("SECOND comment");
     	comment1.setUserAccount(user1);
     	comment1.setCurrentDate();
 
     	Like like1 = new Like();
     	like1.setUserAccount(user1);
     	
 		for(int i = 1; i < 10; i++){
 			Idea idea = new Idea();
 			idea.setTitle("Some awesome idea " + i);
 			idea.setBody("Some awesome description");
 			idea.setUserAccount(user1);
 			idea.addComment(comment);
			idea.setCurrentDate();
 			if (i == 7){
 				idea.addComment(comment1);
 			}
 			idea.addLikes(like1);
 
 			this.ideaRepository.save(idea);
 
 		}
 
 
 
 	}
 
 	@RequestMapping(value="getuserideas", method=RequestMethod.GET)
 	public void getUserIdeas(@ModelAttribute UserAccount userAccount){
 		this.ideaRepository.findByUserAccount(userAccount);
 
 	}
 
 	@RequestMapping(value="getlikedideas", method=RequestMethod.GET)
 	public void getLikedIdeas(UserAccount userAccount){
 		Like like = new Like();
 		like.setUserAccount(userAccount);
 		this.ideaRepository.findByLikes(like);
 	}
 }
