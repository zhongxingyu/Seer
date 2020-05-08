 package com.tiny.controller;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.social.DuplicateStatusException;
 import org.springframework.social.RateLimitExceededException;
 import org.springframework.social.UncategorizedApiException;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.tiny.document.Document;
import com.tiny.service.CommentService;
 import com.tiny.service.DocumentService;
 import com.tiny.service.MemberService;
 import com.tiny.service.PostService;
 import com.tiny.social.SecurityContext;
 
 @Controller
 public class CountController {
 	private static final Logger LOGGER = LoggerFactory.getLogger(CountController.class);
 
 	@Autowired
 	private DocumentService documentService;
 
 	@Autowired
 	private MemberService memberService;
 
 	@Autowired
 	private PostService postService;
 
 	@RequestMapping(value = "/post", method = RequestMethod.POST)
 	public ModelAndView post(@RequestParam Integer documentId, @RequestParam String content) {
 		ModelAndView mav = new ModelAndView();
 		ModelMap model = new ModelMap();
 		try {
 			postService.post(content);
 			Document document = documentService.get(documentId);
 			documentService.increaseCountToShare(documentId);
 			memberService.increaseCountToShare(document.getProviderUserId());
 			memberService.increaseCountToBeShared(document.getProviderUserId());
 			document = documentService.get(documentId);
 			model.addAttribute("isSuccess", true);
 			model.addAttribute("alertMessage", "Posted.");
 			model.addAttribute("document", document);
 		} catch (DuplicateStatusException e) {
 			model.addAttribute("isSuccess", false);
 			model.addAttribute("alertMessage", "Already Posted.");
 		} catch (UncategorizedApiException e) {
 			model.addAttribute("isSuccess", false);
 			model.addAttribute("alertMessage", "Content is empty.");
 		} catch (RateLimitExceededException e) {
 			model.addAttribute("isSuccess", false);
 			model.addAttribute("alertMessage", "The rate limit has been exceeded.");
 		} catch (Exception e) {
 			model.addAttribute("isSuccess", false);
 			model.addAttribute("alertMessage", "Error to post.");
 		}
 		mav.addAllObjects(model);
 		mav.setViewName("postAlert");
 		return mav;
 	}
 
 	@RequestMapping(value = "/like", method = RequestMethod.GET)
	public void like(@RequestParam Integer documentId) {
 		Document document = documentService.get(documentId);
 		documentService.increaseCountToLike(documentId);
 		memberService.increaseCountToLike(SecurityContext.getCurrentUser().getId());
 		memberService.increaseCountToBeLiked(document.getProviderUserId());
 	}
 	
 	@RequestMapping(value = "/dislike", method = RequestMethod.GET)
	public void dislike(@RequestParam Integer documentId) {
 		Document document = documentService.get(documentId);
 		documentService.increaseCountToDislike(documentId);
 		memberService.increaseCountToDislike(SecurityContext.getCurrentUser().getId());
 		memberService.increaseCountToBeDisliked(document.getProviderUserId());
 	}
 }
