 package org.odds.mvc.admin.news;
 
 /**
  *
  * @author kenkataiwa
  */
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import org.odds.hibernate.dao.NewsDAO;
 import org.odds.hibernate.entities.News;
 import org.odds.mvc.admin.form.NewsValidator;
 import org.odds.mvc.admin.form.OrphanageBean;
 import org.odds.mvc.admin.form.PostNewsBean;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.support.SessionStatus;
 
 /**
  * Handles requests for the application home page.
  */
 @Controller
 @RequestMapping("/admin/news/create")
 public class CreateNewsController {
 
     NewsValidator newsValidator;
 
     @Autowired
     public CreateNewsController(NewsValidator newsValidator) {
         this.newsValidator = newsValidator;
     }
 
     @RequestMapping(method = RequestMethod.GET)
     public String initForm(Model model) {
         PostNewsBean news = new PostNewsBean();
         model.addAttribute("news", news);
 
         return "/admin/news/create";
     }
 
     @RequestMapping(method = RequestMethod.POST)
     public String processSubmit(
             @ModelAttribute("news") PostNewsBean form,
             BindingResult result, SessionStatus status, Model model) {
 
         newsValidator.validate(form, result);
 
         if (result.hasErrors()) {
             //if validator failed
             return "/admin/news/create";
         } else {
             News news = new News();
             news.setTitle(form.getTitle());
             news.setBody(form.getBody());
             news.setTime(new Date());
             NewsDAO.createNews(news);
 
             status.setComplete();
            model.addAttribute("success", true);
             //form success
             return "/admin/news/create";
         }
     }
 }
