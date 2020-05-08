 package nl.codebasesoftware.produx.controller;
 
 import nl.codebasesoftware.produx.domain.Category;
 import nl.codebasesoftware.produx.domain.dto.entity.ArticleEntityDTO;
 import nl.codebasesoftware.produx.domain.dto.entity.ArticlePageEntityDTO;
 import nl.codebasesoftware.produx.exception.ResourceNotFoundException;
 import nl.codebasesoftware.produx.service.ArticleService;
 import nl.codebasesoftware.produx.service.PageBlockService;
 import nl.codebasesoftware.produx.properties.Properties;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import javax.servlet.http.HttpServletRequest;
 
 /**
  * User: rvanloen
  * Date: 28-5-13
  * Time: 16:54
  */
 @Controller
 public class ArticleController {
 
     private ArticleService articleService;
     private PageBlockService pageBlockService;
     private Properties properties;
 
     @Autowired
     public ArticleController(ArticleService articleService, PageBlockService pageBlockService,  Properties systemProperties) {
         this.articleService = articleService;
         this.pageBlockService = pageBlockService;
         this.properties = systemProperties;
     }
 
 
     @RequestMapping(value = "/{category}/a{aid:[0-9]+}/{title:[^p0-9:-].+}", method = RequestMethod.GET)
     public String getArticle(@PathVariable("aid") Long articleId,
                              @PathVariable("category") String categoryUrlName,
                              HttpServletRequest request,
                              Model model) {
 
         ArticleEntityDTO article = articleService.findFull(articleId);
         String url = article.getUrl();
         String requestUri = request.getRequestURI();
 
         if (!requestUri.equals(url)) {
             throw new ResourceNotFoundException();
         }
 
         setData(model, article, null, categoryUrlName);
         model.addAttribute("title", article.getTitle() + " - " + properties.getProperty("domain"));
 
         // If there is a next page, set the relNextUrl. There is no prev page
         ArticlePageEntityDTO nextPage = article.getArticlePage(1);
         if (nextPage != null) {
             model.addAttribute("relNextUrl", nextPage.getUrl());
         }
 
         return "main";
     }
 
     @RequestMapping(value = "/{category}/a{aid:[0-9]+}/p{pnr:[0-9]+}-{title:.+}", method = RequestMethod.GET)
     public String getArticlePage(@PathVariable("aid") Long articleId,
                                  @PathVariable("pnr") int pageNumber,
                                  @PathVariable("category") String categoryUrlName,
                                  HttpServletRequest request,
                                  Model model) {
 
         // Fetch article page using the service
         ArticleEntityDTO article = articleService.findFull(articleId);
         ArticlePageEntityDTO articlePage = article.getArticlePage(pageNumber);
         String url = articlePage.getUrl();
         String requestURI = request.getRequestURI();
 
         // We need a match for the exact url of the article page and not just the article id and page number, o
         // or we will risk duplicate content problems
         if (!requestURI.equals(url)) {
             throw new ResourceNotFoundException();
         }
 
         // Add the necessary data to the model
         model.addAttribute("title", String.format("%s - %s", articlePage.getTitle(), properties.getProperty("domain")));
 
         // If there is a next page, set the relNextUrl.
         ArticlePageEntityDTO nextPage = article.getArticlePage(pageNumber + 1);
         if (nextPage != null) {
             model.addAttribute("relNextUrl", nextPage.getUrl());
         }
 
         // Set the prev url
         ArticlePageEntityDTO prevPage = article.getArticlePage(pageNumber - 1);
         if (prevPage != null) {
             model.addAttribute("relPrevUrl", prevPage.getUrl());
         } else {
             model.addAttribute("relPrevUrl", article.getUrl());
         }
 
         setData(model, article, articlePage, categoryUrlName);
 
         return "main";
     }
 
 
     private void setData(Model model, ArticleEntityDTO article, ArticlePageEntityDTO page, String categoryUrlName) {
 
         pageBlockService.setCourseCategoriesInLeftColumn(model);
         pageBlockService.setEmptyRightColumn(model);
 
 
         if (page != null) {
             model.addAttribute("currentArticlePage", page);
             model.addAttribute("firstPage", false);
         } else {
             model.addAttribute("currentArticlePage", article);
             model.addAttribute("firstPage", true);
         }
 
         model.addAttribute("article", article);
         model.addAttribute("mainContent", "content/article");
         model.addAttribute("articleNav", article.getPages().size() > 0);
         model.addAttribute("includeHighlighter", true);
         model.addAttribute("broadView", true);
         model.addAttribute("category", article.getCategory());
     }
 
 
 }
