 package pl.project.blog.controller;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.servlet.http.HttpServletRequest;
 import pl.project.blog.BlogService;
 import pl.project.blog.domain.Post;
 import pl.project.blog.util.JSONView;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.support.ReloadableResourceBundleMessageSource;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.FieldError;
 import org.springframework.validation.Validator;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 import pl.project.blog.domain.AppDocument;
 import pl.project.blog.domain.Comment;
 import pl.project.blog.domain.Tag;
 import pl.project.blog.util.Messages;
 
 /**
  *
  * @author Jarosław Bela
  */
 @Controller
 public class BlogController {
 
     private static Logger log = LoggerFactory.getLogger(BlogController.class);
     private BlogService blogService;
     private Validator validator;
     private ReloadableResourceBundleMessageSource messageSource;
 
     @Autowired
     public void setBlogService(BlogService blogService) {
         this.blogService = blogService;
     }
 
     @Autowired
     public void setValidator(Validator validator) {
         this.validator = validator;
     }
 
     @Autowired
     public void setMessageSource(ReloadableResourceBundleMessageSource messageSource) {
         this.messageSource = messageSource;
     }
 
     /**
      * Strona główna.
      */
     @RequestMapping("/home")
     public String showHome(ModelMap model) {
         return showIndex(model);
     }
 
     /**
      * Strona główna.
      */
     @RequestMapping("/index")
     public String showIndex(ModelMap model) {
         return "index";
     }
 
     /**
      * Przekierowanie na stronę logowania.
      */
     @RequestMapping("/login")
     public String showLoginForm() {
         return "login";
     }
 
     /**
      * Akcje dostępne po zalogowaniu z uprawnieniami ROLE_ADMIN
      */
     /**
      * Dodawanie nowego postu.
      */
     @RequestMapping("/admin/newPost")
     public ModelAndView showNewPostForm() {
         return new ModelAndView("admin/newPost", "postObject", new Post()).addObject("tags", blogService.getAvailableTags(true));
     }
 
     @RequestMapping("/admin/editPost")
     public ModelAndView showEditPostForm(@RequestParam("id") String id) {
         ModelAndView modelAndView = new ModelAndView("admin/editPost", "post", blogService.getPost(id, true)).addObject("tags", blogService.getAvailableTags(true));
 
         return modelAndView;
     }
 
     @RequestMapping("/admin/newTag")
     public ModelAndView showNewTagForm() {
         return new ModelAndView("admin/newTag", "tagObject", new Tag());
     }
 
     @RequestMapping("/admin/newTag/create")
     public ModelAndView createTag(HttpServletRequest request,
             @RequestParam(value = "ajax", required = false) String ajax,
             @ModelAttribute("tagObject") Tag tag,
             BindingResult bindingResult) {
 
         blogService.createTag(tag);
 
         Map response = new HashMap();
         response.put("ok", true);
         response.put("tagId", tag.getId());
         response.put("tagName", tag.getName());
 
         return JSONView.modelAndView(response);
     }
 
     /*
      *  Dodawanie komentarza do postu
      */
     @RequestMapping("/addComment")
     public ModelAndView addCommentForm() {
         return new ModelAndView("addComment", "commentObject", new Comment());
     }
 
     /*
      *  Akcja dodająca komentarz do postu
      */
     @RequestMapping(value = "/addComment/create")
     public ModelAndView createComment(HttpServletRequest request,
             @RequestParam(value = "ajax", required = false) String ajax,
             @ModelAttribute("commentObject") Comment comment,
             BindingResult bindingResult) {
 
         validator.validate(comment, bindingResult);
 
         if (bindingResult.hasErrors()) {
             if (ajax != null) {
                 Map m = new HashMap();
                 m.put("ok", false);
                 m.put("errors", Messages.getMessagesForErrors(bindingResult, messageSource, request.getLocale()));
                 return JSONView.modelAndView(m);
             } else {
                 for (FieldError error : bindingResult.getFieldErrors()) {
                     System.out.println("ERROR:  " + error.getField());
                 }
                 return addCommentForm();
             }
         }
 
         comment.setCreated(new Date());
         blogService.createComment(comment);
 
         if (ajax != null) {
             Map m = new HashMap();
             m.put("ok", true);
             m.put("redirect", "app/index");
             return JSONView.modelAndView(m);
         } else {
             return new ModelAndView("redirect:/app/index");
         }
     }
 
     /**
      * Akcja dodająca nowego posta.
      * 
      * @param request
      * @param ajax
      * @param post
      * @param bindingResult
      * @return
      */
     @RequestMapping(value = "/admin/newPost/create")
     public ModelAndView createPost(
             HttpServletRequest request,
             @RequestParam(value = "ajax", required = false) String ajax,
             @ModelAttribute("postObject") Post post, BindingResult bindingResult) {
 
 
         validator.validate(post, bindingResult);
         if (bindingResult.hasErrors()) {
             if (ajax != null) {
                 Map m = new HashMap();
                 m.put("ok", false);
                 m.put("errors", Messages.getMessagesForErrors(bindingResult, messageSource, request.getLocale()));
                 return JSONView.modelAndView(m);
             } else {
                 for (FieldError error : bindingResult.getFieldErrors()) {
                     System.out.println("ERROR:  " + error.getField());
                 }
                 return showNewPostForm();
             }
         }
 
         post.setCreateDate(new Date());
         blogService.createPost(post);
 
         if (ajax != null) {
             Map m = new HashMap();
             m.put("ok", true);
             return JSONView.modelAndView(m);
         } else {
             return new ModelAndView("redirect:/app/index");
         }
     }
 
     @RequestMapping(value = "/admin/editPost/update")
     public ModelAndView updatePost(HttpServletRequest request, @ModelAttribute("post") Post post, BindingResult bindingResult) {
         Map response = new HashMap();
 
         validator.validate(post, bindingResult);
         if (bindingResult.hasErrors()) {
             response.put("ok", false);
             response.put("errors", Messages.getMessagesForErrors(bindingResult, messageSource, request.getLocale()));
         } else {
             Post postOld = blogService.getPost(post.getId(), true);
             post.setCreateDate(postOld.getCreateDate());
             post.setModifyDate(new Date());
             post.setTags(postOld.getTags());
             blogService.updatePost(post);
 
             response.put("ok", true);
         }
         return JSONView.modelAndView(response);
     }
 
     @RequestMapping("/admin/delPost")
     public ModelAndView showDeletePostForm(@RequestParam("id") String id) {
         ModelAndView modelAndView = new ModelAndView("admin/delPost");
 
         AppDocument post = blogService.getPost(id, false);
         modelAndView.addObject("post", post);
 
         return modelAndView;
     }
 
     /**
      * Usuń post o danym id oraz wszystkie jego komentarze.
      *
      * @param id        _id of the document to delete
      * @return
      */
     @RequestMapping("/admin/delPost/ok")
     public ModelAndView deletePost(
             @RequestParam(value = "ajax", required = false) String ajax,
             @RequestParam(value = "ok", required = false) String ok,
             @RequestParam("id") String id) {
 
         if (ok != null) {
             blogService.deletePost(blogService.getPost(id, true));
         }
 
         if (ajax != null) {
             Map m = new HashMap();
             m.put("ok", true);
             m.put("deleted", id);
             m.put("redirect", "app/index");
             return JSONView.modelAndView(m);
         } else {
             return new ModelAndView("redirect:/app/index");
         }
     }
 
     @RequestMapping("/bloglist")
     public String showBlogList(
             ModelMap model,
             @RequestParam(value = "tagId", required = false) String tagId,
             @RequestParam(value = "date", required = false) String date,
             @RequestParam(value = "search", required = false) String search) {
 
         List<Post> posts = new ArrayList<Post>();
 
         if (tagId != null) {
             for (Post post : blogService.listPosts(true)) {
                 if (post.containTag(tagId)) {
                     posts.add(post);
                 }
             }
         } else if (date != null) {
             for (Post post : blogService.listPosts(true)) {
                 if (post.isFromDate(date)) {
                     posts.add(post);
                 }
             }
        } else if (search != null) {
             for (Post post : blogService.listPosts(true)) {
                if (post.containPhrase(search)) {
                     posts.add(post);
                 }
             }
         } else {
             posts = blogService.listPosts(true);
         }
 
         model.addAttribute("posts", posts);
 
         return "bloglist";
     }
 
     /**
      * Pobiera komentarze do odpowiedniego posta
      * @param model
      * @return
      */
     @RequestMapping("/commentlist")
     public ModelAndView showComments(
             @RequestParam(value = "ajax", required = false) String ajax,
             @RequestParam("id") String id) {
         ModelAndView modelAndView = new ModelAndView("commentlist");
 
         List<Comment> comments = blogService.getCommentsForPost(id);
 
         return modelAndView.addObject("comments", comments).addObject("commentObject", new Comment());
     }
 
     /**
      * Pobiera tagi.
      * Tylko ajax request.
      * 
      * @param ajax
      * @return
      */
     @RequestMapping("/taglist")
     public String getTags(
             ModelMap model,
             @RequestParam(value = "ajax", required = true) String ajax) {
 
         model.addAttribute("tags", blogService.getAvailableTags(true));
 
         return "taglist";
     }

    @RequestMapping("/about")
    public String showAbout(ModelMap model) {
        return "about";
    }
 }
