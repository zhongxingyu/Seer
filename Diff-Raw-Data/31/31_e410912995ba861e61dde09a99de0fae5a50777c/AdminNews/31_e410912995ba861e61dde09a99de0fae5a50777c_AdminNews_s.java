 package controllers;
 
 import java.util.List;
 import java.util.ArrayList;
 
 import play.data.Form;
 import play.data.validation.Constraints.*;
 import play.libs.Scala;
 import play.mvc.*;
 import scala.Tuple2;
 import scala.collection.Seq;
 
 import views.html.index;
 import views.html.admin.*;
 
 import models.*;
 
 public class AdminNews extends SamsaraController
 {
   private static final int NEWS_ITEMS_PER_PAGE = 20;
 
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
 
   public static Result news(Integer page)
   {
     if (!isSessionAdmin())
     {
       return badRequest();
     }
     return ok(news.render(page));
   }
   
   public static String shortContent(NewsItem news)
   {
    if (news.content.length() > 128)
    {
      return news.content.substring(0, 96) + "...";
    }
    return news.content;
   }
   
   public static class EditNews
   {
     @Required
     public String title;
     @Required
     public String content;
     @Required
     public String author;
     
     public EditNews()
     { }
 
     public EditNews(NewsItem news)
     {
       title = news.title;
       content = news.content;
       author = news.author.email;
     }
     
     public String validate()
     {
       if (!User.exists(author))
       {
         return "Specified author (User) does not exist.";
       }
       return null;
     }
   }
   
   public static Seq<Tuple2<String, String>> getAuthorSelectSeq()
   {
     List<Tuple2<String, String>> authors = new ArrayList<Tuple2<String, String>>();
     for (User u : User.getAdmins())
     {
       authors.add(new Tuple2<String, String>(u.email, u.email));
     }
     return Scala.toSeq(authors);
   }
 
   public static Result newsItem(Long id)
   {
     if (!isSessionAdmin())
     {
       return badRequest();
     }
     
     if (NewsItem.exists(id))
     {
       EditNews news = new EditNews(NewsItem.get(id));
       return ok(news_single.render(id, form(EditNews.class).fill(news)));
     }
 
     return renderIdNotFound(id);
   }
 
   public static Result updateNewsItem(Long id)
   {
     if (!isSessionAdmin())
     {
       return badRequest();
     }
     
     Form<EditNews> filledForm = form(EditNews.class).bindFromRequest();
     if (filledForm.hasErrors())
     {
       return badRequest(news_single.render(id, filledForm));
     }
 
     if (NewsItem.exists(id))
     {
       EditNews editNews = filledForm.get();
       NewsItem n = NewsItem.get(id);
       n.title = editNews.title;
       n.content = editNews.content;
       n.author = User.get(editNews.author);
       NewsItem.update(n);
     }
 
     return redirect(routes.AdminNews.news(1));
   }
 
   public static Result renderIdNotFound(Long id)
   {
     if (!isSessionAdmin())
     {
       return badRequest();
     }
     //TODO: Make a nice 404 page.
     return notFound(index.render("NewsItem with id '" + id + "' not found."));
   }
 }
