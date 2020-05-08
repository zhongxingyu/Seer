 package controllers.admin;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.junit.experimental.categories.Categories.IncludeCategory;
 import org.nutz.castor.castor.Datetime2String;
 import org.nutz.dao.Chain;
 import org.nutz.dao.Cnd;
 import org.nutz.dao.Dao;
 import org.nutz.dao.Sqls;
 import org.nutz.dao.sql.Sql;
 import org.nutz.ioc.annotation.InjectName;
 import org.nutz.ioc.loader.annotation.IocBean;
 import org.nutz.lang.Lang;
 import org.nutz.lang.Strings;
 import org.nutz.lang.util.Context;
 import org.nutz.log.Log;
 import org.nutz.log.Logs;
 import org.nutz.mvc.annotation.At;
 import org.nutz.mvc.annotation.Ok;
 import org.nutz.mvc.annotation.Param;
 import org.nutz.mvc.view.ServerRedirectView;
 
 import utils.form.PageForm;
 import domains.Category;
 import domains.News;
 import domains.Tag;
 
 @At("/admin/news")
 @InjectName("admin_newsController")
 @IocBean(name="admin_newsController")
 public class NewsController {
 
 	@Ok(">>:/admin/news/list")
 	public void index(){
 	}
 	/**
 	 * params: offset,max
 	 * @return
 	 */
 	@Ok("jsp:views.admin.news.list")
 	public PageForm<News> list(@Param("offset")int offset , @Param("max")int max ) {
		PageForm<News> pf = PageForm.getPaper(dao, News.class,null, offset, max);
 		for(News news : pf.getResults()){
 			dao.fetchLinks(news, "tags");
 			dao.fetchLinks(news, "categorys");
 		}
 		return pf;
 	}
 	/**
 	 * params: offset,max,tag
 	 * @return
 	 */
 	@Ok("jsp:views.admin.news.list")
 	public PageForm<News>  listByTag(@Param("offset")int offset , @Param("max")int max,@Param("tag")int tag) {
 		PageForm<News> pf = PageForm.getPaper(dao, News.class,Cnd.orderBy().desc("id"), offset, max);
 		return pf;
 	}
 	/**
 	 * params: offset,max,category
 	 * @return
 	 */
 	@Ok("jsp:views.admin.news.list")
 	public PageForm<News>  listByCategory(@Param("offset")int offset , @Param("max")int max,@Param("cat")int category) {
 		PageForm<News> pf = PageForm.getPaper(dao, News.class,Cnd.orderBy().desc("id"), offset, max);
 		return pf;
 	}
 	/**
 	 * params: offset,max,keyword
 	 * @return
 	 */
 	@Ok("jsp:views.admin.news.list")
 	public PageForm<News> search(@Param("offset")int offset , @Param("max")int max,@Param("tag")int tag) {
 		PageForm<News> pf = PageForm.getPaper(dao, News.class,Cnd.orderBy().desc("id"), offset, max);
 		return pf;
 	}
 	@Ok("jsp:views.admin.news.create")
 	public News create() {
 		News news = new News();
 		List<Tag> tags = dao.query(Tag.class, null, null);
 		List<Category> cats = dao.query(Category.class,null,null);
 		news.setTags(tags);
 		news.setCategorys(cats);
 		return news;
 	}
 	@Ok(">>:/admin/news/edit?id=${obj}")
 	public Long save(@Param("title")String title,@Param("content")String content,@Param("tags")String tags,@Param("cats")String cats) {
 		if(Strings.isEmpty(title)){
			title = new Datetime2String().cast(new Date(),null, "yyyy年MM月dd日")+"  留念";
 		}
 		if(Strings.isEmpty(content)){
 			content = "";
 		}
 		News news = new News();
 		news.setTitle(title);
 		news.setContent(content);
 		news.setCreateTime(new Date());
 		
 		List<Tag> tagLists  = null;
 		if(Strings.isEmpty(tags)){
 			tagLists = new ArrayList<Tag>();
 		}else{
 			String tagsIn = tags;
 			// getTheExistsTags
 			tagLists = dao.query(Tag.class, Cnd.wrap("id  in ("+tagsIn+") order by id asc"), null);
 		}
 		
 		List<Category> catLists = null;
 		if(Strings.isEmpty(cats)){
 			 catLists = dao.query(Category.class, Cnd.orderBy().asc("id"), dao.createPager(1, 1));
 		}else{
 			String catsIn = cats;
 			catLists= dao.query(Category.class, Cnd.wrap("id  in ("+ catsIn +") order by id asc") , null);
 		}
 		dao.insert(news);
 		
 		for(Tag tt : tagLists){
 			 dao.insert("t_news_tag", Chain.make("news_id", news.getId()).add("tag_id", tt.getId()));
 		}
 		for(Category cc : catLists){
 			dao.insert("t_news_category" , Chain.make("news_id", news.getId()).add("category_id", cc.getId()));
 		}
 		return news.getId();
 	}
 	@Ok("jsp:views.admin.news.edit")
 	public Object edit(@Param("id")long id) {
 		News news = dao.fetch(News.class,id);
 		if(news == null){
 			// 提示出错
 			return new ServerRedirectView("/admin/news/list");
 		}
 		dao.fetchLinks(news, "tags");
 		dao.fetchLinks(news, "categorys");
 		List<Tag> tags = dao.query(Tag.class, null, null);
 		List<Category> cats = dao.query(Category.class,null,null);
 		Context model = Lang.context();
 		model.set("tags", tags);
 		model.set("cats", cats);
 		model.set("news", news);
 		return model;
 	}
 	@Ok(">>:/admin/news/edit?id=${obj}")
 	public Object update(@Param("id")Long id,@Param("title")String title,@Param("content")String content,@Param("tags")String tags,@Param("cats")String cats) {
 		if(Strings.isEmpty(title)){
 			title = new SimpleDateFormat("yyyy年MM月dd日").format(new Date()) +"  留念";
 		}
 		if(Strings.isEmpty(content)){
 			content = "";
 		}
 		News news = dao.fetch(News.class,id);
 		if(news == null){
 			// 提示出错
 				return new ServerRedirectView("/admin/news/list");
 		}
 		news.setTitle(title);
 		news.setContent(content);
 		Sql tagSql = Sqls.create("delete from t_news_tag where news_id="+news.getId());
 		Sql catSql = Sqls.create("delete from t_news_category  where news_id="+news.getId());
 		dao.execute(tagSql,catSql);
 		List<Tag> tagLists  = null;
 		if(Strings.isEmpty(tags)){
 			tagLists = new ArrayList<Tag>();
 		}else{
 			String tagsIn = tags;
 			// getTheExistsTags
 			tagLists = dao.query(Tag.class, Cnd.wrap("id in ("+tagsIn+") order by id asc"), null);
 		}
 		
 		List<Category> catLists = null;
 		if(Strings.isEmpty(cats)){
 			 catLists = dao.query(Category.class, Cnd.orderBy().asc("id"), dao.createPager(1, 1));
 		}else{
 			String catsIn = cats ;
 			catLists= dao.query(Category.class, Cnd.wrap("id  in ("+ catsIn +") order by id asc") , null);
 		}
 		dao.update(news);
 		
 		for(Tag tt : tagLists){
 			 dao.insert("t_news_tag", Chain.make("news_id", news.getId()).add("tag_id", tt.getId()));
 		}
 		for(Category cc : catLists){
 			dao.insert("t_news_category" , Chain.make("news_id", news.getId()).add("category_id", cc.getId()));
 		}
 		return news.getId();
 	}
 	@Ok(">>:/admin/news/list")
 	public void delete(@Param("id")Long id) {
 		News news = dao.fetch(News.class,id);
 		if(news == null) return ;
 		else{
 			Sql tagSql = Sqls.create("delete from t_news_tag where news_id="+news.getId());
 			Sql catSql = Sqls.create("delete from t_news_category where news_id="+news.getId());
 			dao.execute(tagSql,catSql);
 			dao.delete(news);
 		}
 	}	
 	@Ok(">>:/admin/news/list")
 	public void deleteAll(@Param("ids")String ids) {
 		if(!Strings.isEmpty(ids)){
 			Sql tagSql = Sqls.create("delete from t_news_tag where news_id in ("+ids+")");
 			Sql catSql = Sqls.create("delete from t_news_category  where news_id in ("+ids+")");
 			Sql newsSql = Sqls.create("delete from news where id in ("+ids+")");
 			dao.execute(tagSql,catSql,newsSql);
 		}
 	}
 	
 	private static Log log = Logs.get();
 	private Dao dao;
 	public void setDao(Dao dao){
 		this.dao = dao;
 	}
 }
