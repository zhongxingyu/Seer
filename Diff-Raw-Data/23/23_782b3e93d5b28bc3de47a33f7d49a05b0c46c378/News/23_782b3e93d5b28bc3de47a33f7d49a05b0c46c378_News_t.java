 package controllers;
 
 import play.data.*;
 import play.data.validation.Constraints.*;
 import play.mvc.*;
 
 import views.html.*;
 import views.html.news.*;
 
 import models.*;
 
 public class News extends Controller
 {
   private static final int NEWS_ITEMS_PER_PAGE = 4;
 
   public static Integer pageCount()
   {
     return Static.pageCount(NEWS_ITEMS_PER_PAGE, NewsItem.count());
   }
 
   public static Integer indexForPageStart(int page)
   {
     return Static.indexForPageStart(page, NEWS_ITEMS_PER_PAGE, NewsItem.count());
   }
 
   public static Integer indexForPageEnd(int page)
   {
     return Static.indexForPageEnd(page, NEWS_ITEMS_PER_PAGE, NewsItem.count());
   }
 
   public static class NewNewsItem
   {
     @Required
     public String title;
     @Required
     public String content;
 
     public String validate()
     {
       return null;
     }
   }
 
   public static Result news(Integer page)
   {
     page = Math.max(1, page);
     return ok(news.render(page, form(NewNewsItem.class)));
   }
 
   public static Result newsItem(Long id)
   {
     if (NewsItem.exists(id))
     {
       return ok(news_single.render(NewsItem.get(id)));
     }
     return renderIDNotFound(id);
   }
 
   public static Result deleteNewsItem(Long id)
   {
     if (NewsItem.exists(id))
     {
       NewsItem.delete(id);
       return redirect(routes.News.news(1));
     }
     return renderIDNotFound(id);
   }
 
   public static Result addNewsItem()
   {
     Form<NewNewsItem> filledForm = form(NewNewsItem.class).bindFromRequest();
     if (filledForm.hasErrors())
     {
       return badRequest(news.render(1, filledForm));
     }
 
     NewsItem item = new NewsItem();
     NewNewsItem newItem = filledForm.get();
 
     item.title = newItem.title;
     item.content = newItem.content;
     if (Auth.isSessionAdmin())
     {
       item.author = Auth.getSessionUser();
     }
     else
     {
       flash("error", "You are not authorized to post News Items.");
      return unauthorized(news.render(1, filledForm));
     }
 
     if (NewsItem.exists(item.id))
     {
       flash("error", "News Item with this ID (" + item.id + ") already exists.");
       return badRequest(news.render(1, filledForm));
     }
 
     NewsItem.create(item);
 
     flash("success", "News Item created.");
     return redirect(routes.News.news(1));
   }
 
   public static class EditedNewsItem
   {
     @Required
     public Long id;
     @Required
     public String title;
     @Required
     public String content;
 
     public EditedNewsItem()
     {
       id = null;
       title = null;
       content = null;
     }
 
     public EditedNewsItem(NewsItem item)
     {
       id = item.id;
       title = item.title;
       content = item.content;
     }
 
     public String validate()
     {
       return null;
     }
   }
 
   public static Result editNewsItem(Long id)
   {
     if (NewsItem.exists(id))
     {
       EditedNewsItem item = new EditedNewsItem(NewsItem.get(id));
       return ok(news_edit.render(id, form(EditedNewsItem.class).fill(item)));
     }
     return renderIDNotFound(id);
   }
 
   public static Result updateNewsItem(Long id)
   {
     Form<EditedNewsItem> filledForm = form(EditedNewsItem.class).bindFromRequest();
     if (filledForm.hasErrors())
     {
       return badRequest(news_edit.render(id, filledForm));
     }
 
     if (NewsItem.exists(id))
     {
       EditedNewsItem item = filledForm.get();
       NewsItem i = NewsItem.get(id);
       i.content = item.content;
       i.title = item.title;
       NewsItem.update(i);
     }
     else
     {
       flash("error", "News Item not found to edit. Please create a new one.");
       return badRequest(news_edit.render(id, filledForm));
     }
 
     return redirect(routes.News.newsItem(id));
   }
 
   public static Result renderIDNotFound(Long id)
   {
     //TODO: Make a nice 404 page.
     return notFound(index.render("'news/" + id + "' not found."));
   }
 }
