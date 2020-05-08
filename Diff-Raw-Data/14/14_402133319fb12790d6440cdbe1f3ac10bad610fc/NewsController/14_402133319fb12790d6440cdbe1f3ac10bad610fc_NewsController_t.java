 package controllers.admin;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.nutz.dao.Chain;
 import org.nutz.dao.Cnd;
 import org.nutz.dao.Dao;
 import org.nutz.dao.Sqls;
 import org.nutz.dao.sql.Sql;
 import org.nutz.lang.Lang;
 import org.nutz.lang.Strings;
 import org.nutz.lang.util.Context;
 import org.nutz.mvc.annotation.Ok;
 import org.nutz.mvc.annotation.Param;
 
 import com.petebevin.markdown.MarkdownProcessor;
 
 import utils.CV;
 import utils.form.PageForm;
 import utils.xmlrpc.api.Osc;
 import domains.Category;
 import domains.News;
 import domains.Tag;
 
 public class NewsController {
 
 	public static String CONTENT_HTML = "kindeditor"; // HTML
 	public static String CONTENT_MARKDOWN = "markdown"; // MARKDOWN
 	private String contentType = CONTENT_MARKDOWN;// 默认为 markdown编辑器
 
 	@Ok(">>:/admin/news/list")
 	public void index() {
 	}
 
 	/**
 	 * params: offset,max
 	 * 
 	 * @return
 	 */
 	public PageForm<News> list(@Param("offset") int offset,
 			@Param("max") int max) {
 		PageForm<News> pf = PageForm.getPaper(dao, News.class, Cnd.orderBy()
 				.desc("id"), null, offset, max);
 		for (News news : pf.getResults()) {
 			dao.fetchLinks(news, "tags");
 			dao.fetchLinks(news, "categorys");
 		}
 		return pf;
 	}
 
 	public Object create() {
 		News news = new News();
 		List<Tag> tags = dao.query(Tag.class, null, null);
 		List<Category> cats = dao.query(Category.class, null, null);
 		news.setTags(tags);
 		news.setCategorys(cats);
 		Context con = Lang.context();
 		con.set("obj", news);
 		con.set("type", contentType);
 		return con;
 	}
 
 	@Ok(">>:/admin/news/edit?id=${obj}")
 	public Object save(@Param("title") String title,
 			@Param("content") String content, @Param("tags") String tags,
 			@Param("cats") String cats) {
 		if (Strings.isEmpty(title)) {
 			title = new SimpleDateFormat("yyyy年MM月dd日").format(new Date())
 					+ "  留念";
 		}
 		if (Strings.isEmpty(content)) {
 			content = "她轻轻的动了动鼠标没留下一行文字";
 		}
 		News news = new News();
 		news.setTitle(title);
 
 		if (Strings.equals(contentType, CONTENT_MARKDOWN)) {
 			news.setMkContent(content);
 			// content = content; // TODO 需要将转换为 html, java版markdown;
 			// 这里可以对pre进行一些处理,比如添加代码语言，方便进行高亮
 			content = markdown.markdown(content);
 		}
 		// TODO 需要对content 进行安全校验，可以使用jsoup
 		news.setContent(content);
 		news.setCreateTime(new Date());
 		news.setContentType(contentType);
 		List<Tag> tagLists = null;
 		if (Strings.isEmpty(tags)) {
 			tagLists = new ArrayList<Tag>();
 		} else {
 			String tagsIn = tags;
 			// getTheExistsTags
 			tagLists = dao.query(Tag.class,
 					Cnd.wrap("id  in (" + tagsIn + ") order by id asc"), null);
 		}
 
 		List<Category> catLists = null;
 		if (Strings.isEmpty(cats)) {
 			catLists = dao.query(Category.class, Cnd.orderBy().asc("id"),
 					dao.createPager(1, 1));
 		} else {
 			String catsIn = cats;
 			catLists = dao.query(Category.class,
 					Cnd.wrap("id  in (" + catsIn + ") order by id asc"), null);
 		}
 		dao.insert(news);
 
 		for (Tag tt : tagLists) {
 			dao.insert(
 					"t_news_tag",
 					Chain.make("news_id", news.getId()).add("tag_id",
 							tt.getId()));
 		}
		boolean oschina = false;
 		for (Category cc : catLists) {
 			dao.insert("t_news_category", Chain.make("news_id", news.getId())
 					.add("category_id", cc.getId()));
			if (cc.getName().equals("oschina")) {
				oschina = true;
			}
 		}
		if (oschina) {
 			Osc.publish(news.getTitle(), news.getContent());
 		}
 		return CV.redirect("/admin/news/edit?id=" + news.getId(), "文章发布成功");
 	}
 
 	public Object edit(@Param("id") long id) {
 		News news = dao.fetch(News.class, id);
 		if (news == null) {
 			// 提示出错
 			return CV.redirect("/admin/news/list", "此文章不存在");
 		}
 		dao.fetchLinks(news, "tags");
 		dao.fetchLinks(news, "categorys");
 		List<Tag> tags = dao.query(Tag.class, null, null);
 		List<Category> cats = dao.query(Category.class, null, null);
 		Context model = Lang.context();
 		model.set("tags", tags);
 		model.set("cats", cats);
 		model.set("news", news);
 		return model;
 	}
 
 	@Ok(">>:/admin/news/edit?id=${obj}")
 	public Object update(@Param("id") Long id, @Param("title") String title,
 			@Param("content") String content, @Param("tags") String tags,
 			@Param("cats") String cats) {
 		if (Strings.isEmpty(title)) {
 			title = new SimpleDateFormat("yyyy年MM月dd日").format(new Date())
 					+ "  留念";
 		}
 		if (Strings.isEmpty(content)) {
 			content = "她轻轻的动了动鼠标没留下一行文字";
 		}
 		News news = dao.fetch(News.class, id);
 		if (news == null) {
 			// 提示出错
 			return CV.redirect("/admin/news/list", "此文章不存在");
 		}
 		news.setTitle(title);
 		if (Strings.equals(news.getContentType(), CONTENT_MARKDOWN)) {
 			news.setMkContent(content);
 			content = markdown.markdown(content);
 		}
 		// TODO 需要对content 进行安全校验，可以使用jsoup
 		news.setContent(content);
 		Sql tagSql = Sqls.create("delete from t_news_tag where news_id="
 				+ news.getId());
 		Sql catSql = Sqls.create("delete from t_news_category  where news_id="
 				+ news.getId());
 		dao.execute(tagSql, catSql);
 		List<Tag> tagLists = null;
 		if (Strings.isEmpty(tags)) {
 			tagLists = new ArrayList<Tag>();
 		} else {
 			String tagsIn = tags;
 			// getTheExistsTags
 			tagLists = dao.query(Tag.class,
 					Cnd.wrap("id in (" + tagsIn + ") order by id asc"), null);
 		}
 
 		List<Category> catLists = null;
 		if (Strings.isEmpty(cats)) {
 			catLists = dao.query(Category.class, Cnd.orderBy().asc("id"),
 					dao.createPager(1, 1));
 		} else {
 			String catsIn = cats;
 			catLists = dao.query(Category.class,
 					Cnd.wrap("id  in (" + catsIn + ") order by id asc"), null);
 		}
 		dao.update(news);
 
 		for (Tag tt : tagLists) {
 			dao.insert(
 					"t_news_tag",
 					Chain.make("news_id", news.getId()).add("tag_id",
 							tt.getId()));
 		}
 		for (Category cc : catLists) {
 			dao.insert("t_news_category", Chain.make("news_id", news.getId())
 					.add("category_id", cc.getId()));
 		}
 		return CV.redirect("/admin/news/edit?id=" + news.getId(), "文章更新成功");
 	}
 
 	public Object changemode(HttpServletRequest request) {
 		String refer = request.getHeader("REFERER");
 		contentType = contentType == CONTENT_HTML ? CONTENT_MARKDOWN
 				: CONTENT_HTML;
 		if (Strings.isBlank(refer)) {
 			refer = "/";
 		}
 		return CV.redirect(refer, Lang.context().set("type", contentType));
 	}
 
 	@Ok(">>:/admin/news/list")
 	public Object delete(@Param("id") Long id) {
 		News news = dao.fetch(News.class, id);
 		if (news == null)
 			return CV.redirect("/admin/news/list", "此文章不存在");
 		else {
 			Sql tagSql = Sqls.create("delete from t_news_tag where news_id="
 					+ news.getId());
 			Sql catSql = Sqls
 					.create("delete from t_news_category where news_id="
 							+ news.getId());
 			dao.execute(tagSql, catSql);
 			dao.delete(news);
 		}
 		return CV.redirect("/admin/news/list", "删除成功");
 	}
 
 	@Ok(">>:/admin/news/list")
 	public Object deleteAll(@Param("ids") String ids) {
 		if (!Strings.isEmpty(ids)) {
 			Sql tagSql = Sqls
 					.create("delete from t_news_tag where news_id in (" + ids
 							+ ")");
 			Sql catSql = Sqls
 					.create("delete from t_news_category  where news_id in ("
 							+ ids + ")");
 			Sql newsSql = Sqls.create("delete from news where id in (" + ids
 					+ ")");
 			dao.execute(tagSql, catSql, newsSql);
 			return CV.redirect("/admin/news/list", "必须要选择一项，才能进行删除");
 		}
 		return CV.redirect("/admin/news/list", "删除成功");
 	}
 
 	private Dao dao;
 
 	public void setDao(Dao dao) {
 		this.dao = dao;
 	}
 
 	private MarkdownProcessor markdown;
 
 	public void setMarkdown(MarkdownProcessor markdown) {
 		this.markdown = markdown;
 	}
 }
