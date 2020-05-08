 package controllers;
 
 import java.util.Date;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.nutz.dao.Cnd;
 import org.nutz.dao.Dao;
 import org.nutz.lang.Lang;
 import org.nutz.lang.Strings;
 import org.nutz.lang.util.Context;
 import org.nutz.mvc.annotation.At;
 import org.nutz.mvc.annotation.Ok;
 import org.nutz.mvc.annotation.POST;
 import org.nutz.mvc.annotation.Param;
 
 import utils.CV;
 import utils.PluginUtil;
 import utils.form.PageForm;
 import domains.Category;
 import domains.Comment;
 import domains.News;
 import domains.Tag;
 
 public class NewsController {
 
 	@Ok(">>:/page/1")
 	public void index(){
 	}
 	/**
 	 * params: offset,max
 	 * @return
 	 */
 	@At({"/page/*","/page","/"})
 	public Object list(@Param("offset")int offset ,@Param("max") int max ) {
 		PageForm<News> pf = PageForm.getPaper(dao, News.class,Cnd.orderBy().desc("id"),null, offset, max);
 		for(News news : pf.getResults()){
 			dao.fetchLinks(news, null);
 		}
 		Context ctx = Lang.context();
 		ctx.set("obj", pf);
 		PluginUtil.getAllCount(dao,ctx);
 		return ctx;
 	}
 	/**
 	 * params: offset,max,tag
 	 * @return
 	 */
 	@At({"/tag/*","/tag"})
 	public Object listByTag(@Param("id")int id,@Param("offset")int offset , @Param("max")int max) {
 		if(id == 0){
 			return CV.redirect("/news/list","标签不能为空");
 		}
 		PageForm<News> pf = PageForm.getPaper(dao, News.class,Cnd.format("id in (select news_id from t_news_tag where tag_id = %d) order by id desc",id ),Cnd.format("id in (select news_id from t_news_tag where tag_id = %d)",id ), offset, max);
 		for(News news : pf.getResults()){
 			dao.fetchLinks(news, null);
 		}
 		Context ctx = Lang.context();
 		ctx.set("obj", pf);
 		ctx.set("tagId", id);
 		ctx.set("tag", dao.fetch(Tag.class, id));
 		PluginUtil.getAllCount(dao,ctx);
 		return ctx;
 	}
 	/**
 	 * params: offset,max,tag
 	 * @return
 	 */
 	@At({"/month/*","/month"})
 	public Object  listByMonth(@Param("month")String month,@Param("offset")int offset ,@Param("max")int max) {
 		if(Strings.isEmpty(month)){
 			return CV.redirect("/","日期归档不能为空");
 		}
 		PageForm<News> pf = PageForm.getPaper(dao, News.class,Cnd.where("concat(year(create_time),'-',month(create_time))","=", month).desc("id"),Cnd.where("concat(year(create_time),'-',month(create_time))","=", month), offset, max);
 		for(News news : pf.getResults()){
 			dao.fetchLinks(news, null);
 		}
 		Context ctx = Lang.context();
 		ctx.set("obj", pf);
 		ctx.set("month", month);
 		PluginUtil.getAllCount(dao,ctx);
 		return ctx;
 	}
 	/**
 	 * params: offset,max,category
 	 * @return
 	 */
 	@At({"/cat/*","/cat"})
 	public Object  listByCategory(@Param("id")int id,@Param("offset")int offset , @Param("max")int max) {
 		if(id == 0){
 			return CV.redirect("/","分类不能为空");
 		}
 		PageForm<News> pf = PageForm.getPaper(dao, News.class,Cnd.format("id in (select news_id from t_news_category where category_id = %d) order by id desc",id ),Cnd.format("id in (select news_id from t_news_category where category_id = %d)",id ), offset, max);
 		for(News news : pf.getResults()){
 			dao.fetchLinks(news, null);
 		}
 		Context ctx = Lang.context();
 		ctx.set("obj", pf);
 		ctx.set("catId", id);
 		ctx.set("cat", dao.fetch(Category.class, id));
 		PluginUtil.getAllCount(dao,ctx);
 		return ctx;
 	}
 	/**
 	 * params: offset,max,keyword
 	 * @return
 	 */
 	@At({"/search/*","/search"})
 	public Object search(@Param("p")String p,@Param("offset")int offset , @Param("max")int max) {
 		if(Strings.isEmpty(p)){
 			return CV.redirect("/","搜索字段不能为空");
 		}
 		PageForm<News> pf = PageForm.getPaper(dao, News.class,Cnd.where("title","like","%"+p+"%").or("content", "like", "%"+p+"%").desc("id"),Cnd.where("title","like","%"+p+"%").or("content", "like", "%"+p+"%"), offset, max);
 		for(News news : pf.getResults()){
 			dao.fetchLinks(news, null);
 		}
 		Context ctx = Lang.context();
 		ctx.set("obj", pf);
 		ctx.set("p", p);
 		PluginUtil.getAllCount(dao,ctx);
 		return ctx;
 	}
 	@At({"/show/*","/show"})
 	public Object show(@Param("id")long id){
 		News news = dao.fetch(News.class,id);
 		if(news == null){
 			return CV.redirect("/", "此文章不存在");
 		}else{
 			dao.fetchLinks(news, null);
 			Context ctx = Lang.context();
 			ctx.set("obj", news);
 			PluginUtil.getAllCount(dao, ctx);
 			return ctx;
 		}
 	}
 	@Ok("raw")
 	@POST
 	public Object saveComment(HttpSession session,HttpServletRequest req,@Param("username")String username,@Param("code")String code,@Param("content")String content,@Param("newsId")long newsId){
 		if(Strings.isEmpty(username)){
			username = req.getHeader("X-Real-IP");
 		}
 		if(Strings.isEmpty(code)){
 			return  "{\"result\":false,\"msg\":\"正等你说接头暗号呢，\"}";
 		}
 		if(newsId ==0){
 			return  "{\"result\":false,\"msg\":\"和谐社会，繁华天朝\"}";
 		}
 		if(session == null || session.getAttribute("verifyCode") == null || ! ((String)session.getAttribute("verifyCode")).equalsIgnoreCase(code)){
 			return  "{\"result\":false,\"msg\":\"接头暗号好像不对，你还有3次机会\"}";
 		}
 		session.removeAttribute("verifyCode");
 		Comment comment = new Comment();
 		comment.setUsername(username);
 		comment.setCreateTime(new Date());
 		comment.setNewsId(newsId);
 		comment.setContent(content);
 		dao.insert(comment);
 		return  "{\"result\":true,\"msg\":\"评论插入成功\"}";
 	}
 	private Dao dao;
 	public void setDao(Dao dao){
 		this.dao = dao;
 	}
 }
