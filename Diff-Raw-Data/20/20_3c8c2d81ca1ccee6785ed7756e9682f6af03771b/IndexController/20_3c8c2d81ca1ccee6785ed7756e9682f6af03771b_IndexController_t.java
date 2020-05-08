 package at.dinauer.fhbay.web.controller;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import at.dinauer.fhbay.ServiceLocator;
 import at.dinauer.fhbay.domain.Article;
 import at.dinauer.fhbay.domain.Category;
 import at.dinauer.fhbay.interfaces.ArticleAdminRemote;
 import at.dinauer.fhbay.interfaces.AuctionRemote;
 import at.dinauer.fhbay.interfaces.CategoryAdminRemote;
 import at.dinauer.fhbay.presentation.PmodArticle;
 import at.dinauer.fhbay.presentation.PmodBid;
 import at.dinauer.fhbay.presentation.PmodCategory;
 import at.dinauer.fhbay.util.DateUtil;
 
 @Controller
 public class IndexController {
 
 	private ServiceLocator serviceLocator = new ServiceLocator();
 	private ArticleAdminRemote articleAdmin;
 	private AuctionRemote auction;
 	
 	@RequestMapping(value = {
 			"/", 
 			"/login", 
 			"/loginfailed", 
 			"/logout"})
 	public String showArticles(Model model) throws Exception {
 		model.addAttribute("showArticleList", true);
 
 		fetchCategories(model);
 		fetchAllArticles(model);
 		
 		return "index";
 	}
 
 	@RequestMapping(value = {
 			"/article/{articleId}/{name}", 
 			"/article/{articleId}"})
 	public String showArticleDetails(Model model, @PathVariable("articleId") String articleId) throws Exception {
 		System.out.println("showing details for article with id: " + articleId);
 		model.addAttribute("showArticleDetails", true);
 
 		fetchCategories(model);
 		
 		PmodArticle selectedArticle = new PmodArticle();
 		selectedArticle.setId(123456L);
 		selectedArticle.setName("Nikon D40 (SLR) Body");
 		selectedArticle.setDescription("abcdefg");
 		selectedArticle.setInitialPrice(599.00);
 		selectedArticle.setStartDate(DateUtil.addSeconds(DateUtil.now(), -30));
 		selectedArticle.setEndDate(DateUtil.addSeconds(DateUtil.now(), 600));
 		selectedArticle.setSellerName("Joe User");
 		selectedArticle.setCategoryName("Photography > Cameras");
 		
 		model.addAttribute("selectedArticle", selectedArticle);
 		
 		return "index";
 	}
 
 	@RequestMapping(value = {
 			"/category/{categoryId}/{categoryName}/{subCategoryName}", 
 			"/category/{categoryId}/{name}", 
 			"/category/{categoryId}"})
 	public String showArticlesInCategory(Model model, @PathVariable("categoryId") String categoryId) throws Exception {
 		System.out.println("showing articles in category with id: " + categoryId);
 		model.addAttribute("showArticleList", true);
 
 		fetchCategories(model);
 		fetchArticlesByCategory(model, categoryId);
 		
 		model.addAttribute("selectedCategoryName", "Photography > Cameras");
 
 		return "index";
 	}
 
 	@RequestMapping(value = "/bidHistory")
 	public String showBidHistory(Model model, @RequestParam("articleId") String articleId) throws Exception {
 		System.out.println("showing bids for article with id: " + articleId);
 		model.addAttribute("showBids", true);
 
 		fetchCategories(model);
		fetchBids(model);
 		
 		PmodArticle selectedArticle = new PmodArticle();
 		selectedArticle.setName("Nikon D40 (SLR) Body");
 		selectedArticle.setInitialPrice(599.00);
 		selectedArticle.setStartDate(DateUtil.addSeconds(DateUtil.now(), -300));
 		
 		model.addAttribute("selectedArticle", selectedArticle);
 
 		return "index";
 	}
 	
 	@RequestMapping(value = "/bid", method = RequestMethod.POST)
 	public String bidOnArticle(Model model, @RequestParam("articleId") Long articleId, @RequestParam("amount") String amount) throws Exception {
 		System.out.println("received new bid for article " + articleId + ": " + amount);
 		
 		return "redirect:/article/" + articleId;
 	}
 
 	@RequestMapping(value = "/offerArticle", method = RequestMethod.GET)
 	public String showOfferArticleForm(Model model) throws Exception {
 		model.addAttribute("showOfferArticleForm", true);
 
 		fetchCategories(model);
 		
 		return "index";
 	}
 
 	@RequestMapping(value = "/offerArticle", method = RequestMethod.POST)
 	public String offerArticle(Model model, 
 			@RequestParam("name") String name, 
 			@RequestParam("initialPrice") String initialPrice,
 			@RequestParam("startDate") String startDate,
 			@RequestParam("endDate") String endDate,
 			@RequestParam("category") String categoryId,
 			@RequestParam("description") String description) throws Exception {
 		
 		System.out.println("name: " + name);
 		System.out.println("initialPrice: " + initialPrice);
 		System.out.println("startDate: " + startDate);
 		System.out.println("endDate: " + endDate);
 		System.out.println("categoryId: " + categoryId);
 		System.out.println("description: " + description);
 		
 		Long articleId = 666L;
 		
 		return "redirect:/article/" + articleId;
 	}
 
 	@RequestMapping(value = "/search")
 	public String search(Model model, @RequestParam("q") String searchString, @RequestParam("category") String categoryId) throws Exception {
 		System.out.println("searching for articles: " + searchString);
 		model.addAttribute("showArticleList", true);
 		model.addAttribute("searchString", searchString);
 
 		fetchCategories(model);
 		fetchArticles(model, Long.parseLong(categoryId), searchString, true);
 		
 		return "index";
 	}
 
 	private void fetchBids(Model model) {
 		List<PmodBid> bids = new ArrayList<>();
 		
 		PmodBid johnDoe199 = new PmodBid();
 		johnDoe199.setBidderName("John Doe");
 		johnDoe199.setAmount(1.99);
 		johnDoe199.setBidTime(DateUtil.addSeconds(DateUtil.now(), -100));
 		johnDoe199.setPriceAtBidTime(0.99);
 		
 		PmodBid joeUser299 = new PmodBid();
 		joeUser299.setBidderName("Joe User");
 		joeUser299.setAmount(2.99);
 		joeUser299.setBidTime(DateUtil.addSeconds(DateUtil.now(), -50));
 		joeUser299.setPriceAtBidTime(1.99);
 		joeUser299.setWinning(true);
 		
 		bids.add(joeUser299);
 		bids.add(johnDoe199);
 		
 		model.addAttribute("bids", bids);
 	}
 
 	private void fetchCategories(Model model) throws Exception {
 		List<PmodCategory> categories = new ArrayList<>();
 		
 		CategoryAdminRemote categoryAdmin = serviceLocator.locate(CategoryAdminRemote.class);
 		
 		for (Category rootCategory : categoryAdmin.findRootCategories()) {
 			categories.add(new PmodCategory(rootCategory));
 		}
 		
 		model.addAttribute("categories", categories);
 	}
 
 	private void fetchAllArticles(Model model) throws Exception {
 		fetchArticles(model, null, "", true);
 	}
 
 	private void fetchArticlesByCategory(Model model, String categoryId) throws Exception {
 		fetchArticles(model, Long.parseLong(categoryId), "", true);
 	}
 
 	private void fetchArticles(Model model, Long categoryId, String pattern, boolean includeSubCategories) throws Exception {
 		List<PmodArticle> articles = new ArrayList<>();
 
 		articleAdmin = serviceLocator.locate(ArticleAdminRemote.class);
 		auction = serviceLocator.locate(AuctionRemote.class);
 		
		if (categoryId != null && categoryId <= 0) {
 			for (Article article : articleAdmin.findAllMatchingArticles(pattern)) {
 				articles.add(fetchArticleDetails(article));
 			}
 		} else if (categoryId != null) {
			for (Article article : articleAdmin.findAllMatchingArticles(categoryId, pattern, includeSubCategories)) {
 				articles.add(fetchArticleDetails(article));				
 			}
 		} else {
 			for (Article article : articleAdmin.findAllArticles()) {
 				articles.add(fetchArticleDetails(article));
 			}
 		}
 		
 		model.addAttribute("articles", articles);
 	}
 	
 	private PmodArticle fetchArticleDetails(Article article) throws Exception {
 		PmodArticle pmodArticle = new PmodArticle(article);
 
 		pmodArticle.setCurrentPrice(auction.findCurrentPriceForArticle(article.getId()));
 		pmodArticle.setNumberOfBids(auction.findBidsForArticle(article.getId()).size());
 		
 		return pmodArticle;
 	}
 }
