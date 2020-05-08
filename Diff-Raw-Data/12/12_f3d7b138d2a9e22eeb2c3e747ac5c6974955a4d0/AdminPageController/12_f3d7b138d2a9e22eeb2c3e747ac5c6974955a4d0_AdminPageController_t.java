 package nl.codebasesoftware.produx.controller.admin;
 
 import nl.codebasesoftware.produx.domain.Article;
 import nl.codebasesoftware.produx.domain.ArticlePage;
 import nl.codebasesoftware.produx.domain.UserProfile;
 import nl.codebasesoftware.produx.domain.dto.entity.ArticleEntityDTO;
 import nl.codebasesoftware.produx.exception.ResourceNotFoundException;
 import nl.codebasesoftware.produx.formdata.ArticlePageFormData;
 import nl.codebasesoftware.produx.service.ArticleService;
 import nl.codebasesoftware.produx.service.UserProfileService;
 import nl.codebasesoftware.produx.service.support.CurrentUser;
 import nl.codebasesoftware.produx.validator.ArticlePageFormDataValidator;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.MessageSource;
 import org.springframework.core.convert.ConversionService;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import java.util.Locale;
 
 /**
  * User: rvanloen
  * Date: 11-4-13
  * Time: 21:40
  */
 @Controller
 public class AdminPageController {
 
     private ArticleService articleService;
     private UserProfileService userProfileService;
     private ConversionService conversionService;
     private ArticlePageFormDataValidator validator;
     private MessageSource messageSource;
 
     @Autowired
     public AdminPageController(ArticleService articleService, UserProfileService userProfileService,
                                ConversionService conversionService,
                                ArticlePageFormDataValidator validator,
                                MessageSource messageSource) {
         this.articleService = articleService;
         this.userProfileService = userProfileService;
         this.conversionService = conversionService;
         this.validator = validator;
         this.messageSource = messageSource;
     }
 
     @RequestMapping(value = "/admin/article/{articleId}/pages/add", method = RequestMethod.GET)
     public String showNewPageForm(@PathVariable("articleId") Long articleId, Model model, Locale locale) {
 
         checkArticle(articleId);
         setHeaderText(locale, model);
         ArticlePageFormData formData = new ArticlePageFormData();
         formData.setArticleId(articleId);
         setFormData(formData, model);
 
         return "adminMain";
     }
 
     @RequestMapping(value = "/admin/articles/page/{pageId}", method = RequestMethod.POST)
     public String savePage(@ModelAttribute("articlePageFormData") ArticlePageFormData formData, @PathVariable("pageId") Long pageId, Model model,
                            BindingResult result, Locale locale) {
 
         if (formData.removeClicked()) {
             articleService.removePage(formData.getId());
             return String.format("redirect:/admin/articles/edit/%s", formData.getArticleId());
         }
 
         String valid = "false";
 
         UserProfile author = userProfileService.findAuthorByPage(pageId);
         if (!CurrentUser.get().equals(author)) {
             throw new ResourceNotFoundException();
         }
 
         validator.validate(formData, result);
 
         if (!result.hasErrors()) {
             valid = "true";
             articleService.saveArticlePage(formData, formData.getArticleId());
         }
 
         setHeaderText(locale, model);
         model.addAttribute("valid", valid);
         setFormData(formData, model);
         return "adminMain";
     }
 
     @RequestMapping(value = "/admin/article/{articleId}/pages/add", method = RequestMethod.POST)
     public String savePage(@ModelAttribute("articlePageFormData") ArticlePageFormData formData, BindingResult result,
                            @PathVariable("articleId") Long articleId, Model model, Locale locale) {
 
         String valid = "false";
         validator.validate(formData, result);
 
         if (!result.hasErrors()) {
             valid = "true";
             long newId = articleService.saveArticlePage(formData, formData.getArticleId());
             return String.format("redirect:/admin/articles/page/%d", newId);
         }
 
         setHeaderText(locale, model);
         setFormData(formData, model);
         model.addAttribute("valid", valid);
 
         return "adminMain";
     }
 
     private void setFormData(ArticlePageFormData formData, Model model) {
         model.addAttribute("articlePageFormData", formData);
         model.addAttribute("mainContent", "forms/articlepage");
        model.addAttribute("richtext", true);
     }
 
     @RequestMapping(value = "/admin/articles/page/{pageId}", method = RequestMethod.GET)
     public String editPageForm(@PathVariable("pageId") Long pageId, Model model, Locale locale) {
 
         ArticlePage page = articleService.findPage(pageId);
 
         if (page == null) {
             throw new ResourceNotFoundException();
         }
 
         ArticlePageFormData formData = conversionService.convert(page, ArticlePageFormData.class);
         setHeaderText(locale, model);
         model.addAttribute("articlePage", true);
         model.addAttribute("articlePageFormData", formData);
         model.addAttribute("mainContent", "forms/articlepage");
        model.addAttribute("articleForm", true);
         return "adminMain";
     }
 
 
     private ArticleEntityDTO checkArticle(long articleId) {
         // Check if the article is this author's article
         ArticleEntityDTO article = articleService.findFull(articleId);
 
         if (article == null) {
             throw new ResourceNotFoundException();
         }
 
         UserProfile author = userProfileService.findAuthorByArticle(article.getId());
 
         // Check if this article belongs to the current user
         if (!author.equals(CurrentUser.get())) {
             throw new ResourceNotFoundException();
         }
 
 
         return article;
     }
 
     private void setHeaderText(Locale locale, Model model) {
         model.addAttribute("headerText", messageSource.getMessage("admin.pageform.header", new Object[]{}, locale));
     }
 
 }
