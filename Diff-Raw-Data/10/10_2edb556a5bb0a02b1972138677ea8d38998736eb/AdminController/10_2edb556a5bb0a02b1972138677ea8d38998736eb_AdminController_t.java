 package com.ippoippo.joplin.controller;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.servlet.http.HttpServletRequest;
 import javax.validation.Valid;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.ippoippo.joplin.dto.Article;
 import com.ippoippo.joplin.dto.ItemListForm;
 import com.ippoippo.joplin.dto.YoutubeItem;
 import com.ippoippo.joplin.dto.YoutubeSearchForm;
 import com.ippoippo.joplin.exception.IllegalOperationException;
 import com.ippoippo.joplin.exception.IllegalRequestException;
 import com.ippoippo.joplin.mongo.operations.ArticleOperations;
 import com.ippoippo.joplin.mongo.operations.YoutubeItemOperations;
 import com.ippoippo.joplin.service.ArticleService;
 import com.ippoippo.joplin.service.YoutubeSearchService;
 import com.ippoippo.joplin.util.Utils;
 
 /**
  * Handles requests for articles.
  */
 @Controller
 @RequestMapping("/admin")
 public class AdminController {
 	
 	private final Logger logger = LoggerFactory.getLogger(this.getClass());
 
 	@Value("${admin.password.md5}")
 	private String adminPasswordMD5;
 
 	public static final String SESSION_KEY_AUTH = "loginAsAdmin";
 
 	@Inject
 	YoutubeSearchService youtubeSearchService;
 
 	@Inject
 	ArticleService articleService;
 
 	@Inject
 	YoutubeItemOperations youtubeItemOperations;
 
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public ModelAndView top() {
 		
 		ModelAndView modelAndView = new ModelAndView();
 		modelAndView.setViewName("admin/login");
 		return modelAndView;
 	}
 
 	@RequestMapping(value = "/login", method = RequestMethod.POST)
 	public ModelAndView login(@RequestParam("password") String password, HttpServletRequest request) {
 		if (Utils.hashMD5(password).equals(adminPasswordMD5)) {
 			request.getSession().setAttribute(SESSION_KEY_AUTH, true);
 			ModelAndView modelAndView = new ModelAndView();
 			modelAndView.setViewName("redirect:article/list");
 			return modelAndView;
 		}
 		ModelAndView modelAndView = new ModelAndView();
 		modelAndView.addObject("failed", true);
 		modelAndView.setViewName("admin/login");
 		return modelAndView;
 	}
 
 	@RequestMapping(value = "/logout", method = RequestMethod.GET)
 	public ModelAndView logout(HttpServletRequest request) {
 		request.getSession().removeAttribute(SESSION_KEY_AUTH);
 
 		ModelAndView modelAndView = new ModelAndView();
 		modelAndView.setViewName("redirect:/admin/");
 		return modelAndView;
 	}
 
 	@Transactional(rollbackForClassName="java.lang.Exception")
 	@RequestMapping(value = "/article/list", method = {RequestMethod.GET,RequestMethod.POST})
 	public ModelAndView list() {
 
 		ModelAndView modelAndView = new ModelAndView();
 		modelAndView.addObject("articles", articleService.listLatest(0, 10));
 		modelAndView.setViewName("admin/article/list");
 		return modelAndView;
 	}
 
 	@Transactional(rollbackForClassName="java.lang.Exception")
 	@RequestMapping(value = "/article/new", method = RequestMethod.POST)
 	public ModelAndView create() {
 
 		ModelAndView modelAndView = new ModelAndView();
 		modelAndView.addObject("article", new Article());
 		modelAndView.setViewName("admin/article/new");
 		return modelAndView;
 	}
 
 	@Transactional(rollbackForClassName="java.lang.Exception")
 	@RequestMapping(value = "/article/create", method = RequestMethod.POST)
 	public ModelAndView create(@Valid Article article, BindingResult result) {
 
 		if (result.hasErrors()) {
 			ModelAndView modelAndView = new ModelAndView();
 			modelAndView.addObject("article", article);
 			modelAndView.setViewName("admin/article/new");
 			return modelAndView;
 		}
 
 		String newId = articleService.create(article);
 
 		ModelAndView modelAndView = new ModelAndView();
 		modelAndView.addObject("created", true);
 		modelAndView.setViewName("redirect:"+newId+"/edit");
 		return modelAndView;
 	}
 
 	private void validateAccess(String articleId) throws IllegalRequestException {
 		
		Article article = articleService.getByIdForUpdate(articleId);
 		if (article == null) throw new IllegalRequestException();
 	}
 
 	@Transactional(rollbackForClassName="java.lang.Exception")
 	@RequestMapping(value = "/article/{articleId}/edit", method = {RequestMethod.GET,RequestMethod.POST})
 	public ModelAndView edit(@PathVariable String articleId) throws IllegalRequestException {
 
 		validateAccess(articleId);
 
 		Article article = articleService.getByIdForUpdate(articleId);
 
 		ModelAndView modelAndView = new ModelAndView();
 		modelAndView.addObject("article", article);
 		modelAndView.setViewName("admin/article/edit");
 		return modelAndView;
 	}
 
 	@Transactional(rollbackForClassName="java.lang.Exception")
 	@RequestMapping(value = "/article/{articleId}/update", params = {"updateBtn"}, method = RequestMethod.POST)
 	public ModelAndView update(@PathVariable String articleId, @Valid Article article, BindingResult result) throws IllegalRequestException {
 
 		validateAccess(articleId);
 
 		if (result.hasErrors()) {
 			ModelAndView modelAndView = new ModelAndView();
 			modelAndView.addObject("article", article);
 			modelAndView.setViewName("admin/article/edit");
 			return modelAndView;
 		}
 
 		article.setId(articleId);
 		try {
 			articleService.updateSubjectAndActive(article);
 
 		} catch (IllegalOperationException e) {
 			ModelAndView modelAndView = new ModelAndView();
 			modelAndView.addObject("article", article);
 			modelAndView.addObject("errorMessage", e.getMessage());
 			modelAndView.setViewName("admin/article/edit");
 			return modelAndView;
 		}
 
 		ModelAndView modelAndView = new ModelAndView();
 		modelAndView.addObject("updated", true);
 		modelAndView.setViewName("forward:edit");
 		return modelAndView;
 	}
 
 	@Transactional(rollbackForClassName="java.lang.Exception")
 	@RequestMapping(value = "/article/{articleId}/update", params = {"deleteBtn"}, method = RequestMethod.POST)
 	public ModelAndView delete(@PathVariable String articleId) throws IllegalRequestException {
 		
 		validateAccess(articleId);
 
 		articleService.delete(articleId);
 
 		ModelAndView modelAndView = new ModelAndView();
 		modelAndView.addObject("deleted", true);
 		modelAndView.setViewName("redirect:/admin/article/list");
 		return modelAndView;
 	}
 
 	@Transactional(rollbackForClassName="java.lang.Exception")
 	@RequestMapping(value = "/article/{articleId}/listItem", method = {RequestMethod.POST})
 	public ModelAndView listItem(
 			@PathVariable String articleId
 			, @Valid ItemListForm itemListForm) throws IllegalRequestException {
 
 		validateAccess(articleId);
 
 		itemListForm.update();
 		List<YoutubeItem> items
 			= youtubeItemOperations.listByArticleId(
 					articleId
 					, itemListForm.getStartIndex()
 					, itemListForm.getListSize());
 
 		ModelAndView modelAndView = new ModelAndView();
 		modelAndView.addObject("itemListForm", itemListForm);
 		modelAndView.addObject("items", items);
 		modelAndView.setViewName("admin/article/listItem");
 		return modelAndView;
 	}
 
 	@Transactional(rollbackForClassName="java.lang.Exception")
 	@RequestMapping(value = "/article/{articleId}/item", method = RequestMethod.POST)
 	public ModelAndView item(@PathVariable String articleId) throws IllegalRequestException, IOException {
 
 		validateAccess(articleId);
 
 		ModelAndView modelAndView = new ModelAndView();
 		modelAndView.addObject("youtubeSearchForm", new YoutubeSearchForm());
 		modelAndView.setViewName("admin/article/item");
 		return modelAndView;
 	}
 	
 	@Transactional(rollbackForClassName="java.lang.Exception")
 	@RequestMapping(value = "/article/{articleId}/searchItem", method = RequestMethod.POST)
 	public ModelAndView searchItem(
 			@PathVariable String articleId
 			, @Valid YoutubeSearchForm youtubeSearchForm
 			, BindingResult result) throws IllegalRequestException, IOException {
 		
 		validateAccess(articleId);
 
 		if (result.hasErrors()) {
 			ModelAndView modelAndView = new ModelAndView();
 			modelAndView.addObject("youtubeSearchForm", youtubeSearchForm);
 			modelAndView.setViewName("admin/article/item");
 			return modelAndView;
 		}
 
 		youtubeSearchForm.update();
 		List<YoutubeItem> items = youtubeSearchService.search(articleId, youtubeSearchForm);
 
 		ModelAndView modelAndView = new ModelAndView();
 		modelAndView.addObject("youtubeSearchForm", youtubeSearchForm);
 		modelAndView.addObject("items", items);
 		if (items == null || items.size() == 0) modelAndView.addObject("message", "No result");
 		modelAndView.setViewName("admin/article/item");
 		return modelAndView;
 	}
 
 	@Transactional(rollbackForClassName="java.lang.Exception")
 	@RequestMapping(value = "/article/{articleId}/addItem", method = RequestMethod.POST)
 	public ModelAndView addItem(
 			@PathVariable String articleId, @RequestParam("videoId") String videoId)
 					throws IllegalRequestException {
 
 		validateAccess(articleId);
 
 		if (youtubeItemOperations.countByArticleIdAndVideoId(articleId, videoId) > 0) {
 			logger.info("The video for articldId="+articleId+", videoId="+videoId+" already exists.");
 		} else {
 			YoutubeItem item = new YoutubeItem();
 			item.setArticleId(articleId);
 			item.setVideoId(videoId);
 			youtubeItemOperations.create(item);
 		}
 		ModelAndView modelAndView = new ModelAndView();
 		modelAndView.addObject("updated", true);
 		modelAndView.setViewName("forward:edit");
 		return modelAndView;
 	}
 
 	@Transactional(rollbackForClassName="java.lang.Exception")
 	@RequestMapping(value = "/article/{articleId}/deleteItem", method = RequestMethod.POST)
 	public ModelAndView removeItem(
 			@PathVariable String articleId
 			, @RequestParam("itemId") String itemId) throws IllegalRequestException {
 
 		validateAccess(articleId);
 
 		youtubeItemOperations.delete(itemId);
 
 		ModelAndView modelAndView = new ModelAndView();
 		modelAndView.addObject("updated", true);
 		modelAndView.setViewName("forward:edit");
 		return modelAndView;
 	}
 
 }
