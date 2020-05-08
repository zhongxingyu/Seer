 package cn.seu.cose.controller.admin;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import cn.seu.cose.entity.Article;
 import cn.seu.cose.entity.ArticlePojo;
 import cn.seu.cose.entity.CategoryPojo;
 import cn.seu.cose.service.ArticleService;
 import cn.seu.cose.service.CategoryService;
 import cn.seu.cose.service.ReporterService;
 import cn.seu.cose.view.util.ViewUtil;
 
 @Controller
 public class AdminArticleController extends AbstractController{
 	@Autowired
 	private ArticleService articleService; 
 	@Autowired
 	private CategoryService catService;
 	@Autowired
 	private ReporterService reporterService;
 	
 	
 	public static final int PAGE_SIZE = 10;
 	
 	@RequestMapping("/admin/article_list-{topCatId}-{subCatId}-{pageIndex}")
 	public String articleList(@PathVariable(value="topCatId") String topCatIdStr, @PathVariable(value="subCatId") String subCatIdStr, 
 			@PathVariable(value="pageIndex") String pageIndexStr, Model model, HttpServletResponse response) {
 		model.addAttribute("searchInput", "");
 		putAdmin(model, response);
 		
 		int topCatId = Integer.parseInt(topCatIdStr);
 		int subCatId = Integer.parseInt(subCatIdStr);
 		int pageIndex = Integer.parseInt(pageIndexStr);
 		
 		/* get cats */
 		List<CategoryPojo> categories = catService.getRootCategories();
 		model.addAttribute("top_cat_list", categories);
 		categories = catService.getCategoriesByParentId(topCatId);
 		model.addAttribute("init_sub_cat_list" ,categories);
 		
 		int catId = subCatId<=0 ? topCatId : subCatId;
 		int index = pageIndex<=0 ? 1 : pageIndex;
 		List<ArticlePojo> list = articleService.getAdminArticleByCatIdAndPageIndexAndPageSize(catId, index, PAGE_SIZE);
 		model.addAttribute("article_list", list);
 		
 		model.addAttribute("topCatId", topCatId);
 		model.addAttribute("subCatId", subCatId);
 		model.addAttribute("pageIndex", pageIndex);
 		model.addAttribute("nextPageIndex", pageIndex+1);
 		model.addAttribute("prePageIndex", pageIndex-1);
 		model.addAttribute("pageCount", getPageCount(articleService.getArticleCountByCatId(topCatId,subCatId)));
 		
 		return "admin_articles";
 	}
 	
 	@RequestMapping("/admin/article_list_search-{searchInput}")
 	public String searchArticle(@PathVariable("searchInput")String searchInput, Model model, HttpServletRequest request, HttpServletResponse response) {
 		putAdmin(model, response);
 		List<ArticlePojo> list = articleService.searchArticle(searchInput);
 		model.addAttribute("article_list", list);
 		model.addAttribute("searchInput", searchInput);
 		/* get cats */
 		List<CategoryPojo> categories = catService.getRootCategories();
 		model.addAttribute("top_cat_list", categories);
 		categories = catService.getCategoriesByParentId(2);
 		model.addAttribute("init_sub_cat_list" ,categories);
 		
 		model.addAttribute("topCatId", 2);
 		model.addAttribute("subCatId", 0);
 		model.addAttribute("pageIndex", 1);
 		model.addAttribute("nextPageIndex", 2);
 		model.addAttribute("prePageIndex", 0);
 		model.addAttribute("pageCount", getPageCount(list.size()));
 		return "admin_articles";
 	}
 	
 	
 	@RequestMapping("/admin/article_list")
 	public String articleList(Model model, HttpServletResponse response) {
 		model.addAttribute("searchInput", "");
 		putAdmin(model, response);
 		
 		List<ArticlePojo> list = articleService.getArticleByCatIdAndPageIndexAndPageSize(2, 1, PAGE_SIZE); // init
 		model.addAttribute("article_list", list);
 		
 		/* get cats */
 		List<CategoryPojo> categories = catService.getRootCategories();
 		model.addAttribute("top_cat_list", categories);
 		categories = catService.getCategoriesByParentId(2);
 		model.addAttribute("init_sub_cat_list" ,categories);
 		
 		model.addAttribute("topCatId", 2);
 		model.addAttribute("subCatId", 0);
 		model.addAttribute("pageIndex", 1);
 		model.addAttribute("nextPageIndex", 2);
 		model.addAttribute("prePageIndex", 0);
 		model.addAttribute("pageCount", getPageCount(articleService.getArticleCountByCatId(2, 0)));
 		return "admin_articles";
 	}
 	
 	@RequestMapping(value="/admin/add_article", method=RequestMethod.GET)
 	public String getAdd(Model model, HttpServletResponse response) {
 		putAdmin(model, response);
 		
 		/* get cats */
 		List<CategoryPojo> categories = catService.getRootCategories();
 		model.addAttribute("top_cat_list", categories);
 		categories = catService.getCategoriesByParentId(1);
 		model.addAttribute("init_sub_cat_list" ,categories);
 		
 		return "admin_articles_add";
 	}
 	
 	@RequestMapping(value="/admin/add_article", method=RequestMethod.POST)
 	public void postAdd(@RequestParam("title") String title, @RequestParam("subhead") String subhead,
 			@RequestParam("catId") String catIdStr, @RequestParam("rootCatId") String rootCatIdStr,
 			@RequestParam("content") String content, @RequestParam("from") String from, @RequestParam("pure") String pure,
 			HttpServletResponse response) {
 		
 		CategoryPojo cat = catService.getCategoryById(Integer.parseInt(catIdStr));
 		
 		ArticlePojo article = new ArticlePojo();
 		article.setTitle(title);
 		article.setSubhead(subhead);
 		article.setCatId(Integer.parseInt(catIdStr));
 		article.setRootCatId(Integer.parseInt(rootCatIdStr));
 		article.setContent(content);
 		article.setFrom(from);
 		article.setPostTime(new Date());
 		pure = wrapPure(pure);
 		int briefLength = pure.length() > 100 ? 100 : pure.length();
 		article.setPureText(pure.substring(0, briefLength) + "……");
 		
 		if (cat.isExclusiveArticle()) {
 			List<ArticlePojo> list = articleService.getArticlesByCatId(Integer.parseInt(catIdStr));
 			if (list != null && !list.isEmpty()) {
 				ArticlePojo temp = list.get(0);
 				article.setId(temp.getId());
 				articleService.updateArticle(article);
 				return;
 			}
 		}
 		articleService.addArticle(article);
 	}
 	
 	@RequestMapping(value="/admin/del_article", method=RequestMethod.POST)
 	public void postDel(@RequestParam("id") String idStr, HttpServletResponse response) {
 		try {
 			int id = Integer.parseInt(idStr);
 			articleService.deleteArticle(id);
 			response.sendRedirect(ViewUtil.getContextPath() + "/admin/article_list");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@RequestMapping(value="/admin/del_articles", method=RequestMethod.POST)
 	public void postDels(@RequestParam("ids") String idsStr, HttpServletResponse response) {
 		if(idsStr == "") {
 				//
 		} else {
 			for (String idStr : idsStr.split("-")) {
 				articleService.deleteArticle(Integer.parseInt(idStr));
 			}
 		}
 		try {
 			response.sendRedirect(ViewUtil.getContextPath() + "/admin/article_list");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 	@RequestMapping(value="/admin/alt_article-{id}", method=RequestMethod.GET)
 	public String getAlt(@PathVariable("id") String idStr, Model model, HttpServletResponse response) {
 		putAdmin(model, response);
 		
 		Article article = articleService.getArticleById(Integer.parseInt(idStr));
 		model.addAttribute("article", article);
 		/* get cats */
 		List<CategoryPojo> categories = catService.getRootCategories();
 		model.addAttribute("top_cat_list", categories);
 		categories = catService.getCategoriesByParentId(article.getRootCatId());
 		model.addAttribute("init_sub_cat_list" ,categories);
 		
 		return "admin_articles_alt";
 	}
 	
 	@RequestMapping(value="/admin/alt_article", method=RequestMethod.POST)
 	public void postAlt(@RequestParam("id") String idStr, @RequestParam("title") String title, @RequestParam("subhead") String subhead,
 			@RequestParam("catId") String catIdStr, @RequestParam("rootCatId") String rootCatIdStr,
 			@RequestParam("content") String content, @RequestParam("from") String from, @RequestParam("pure") String pure,
 			HttpServletResponse response) {
 		ArticlePojo article = new ArticlePojo();
 		article.setId(Integer.parseInt(idStr));
 		article.setTitle(title);
 		article.setSubhead(subhead);
 		article.setCatId(Integer.parseInt(catIdStr));
 		article.setRootCatId(Integer.parseInt(rootCatIdStr));
 		article.setContent(content);
 		article.setFrom(from);
 		article.setPostTime(new Date());
 		pure = wrapPure(pure);
 		int briefLength = pure.length() > 100 ? 100 : pure.length();
 		article.setPureText(pure.substring(0, briefLength) + "……");
 		articleService.updateArticle(article);
 	}
 	
 	private int getPageCount(int count) {
 		if (count <= PAGE_SIZE) {
 			return 1;
 		} else if (count%PAGE_SIZE == 0) {
 			return count/PAGE_SIZE;
 		} else {
 			return count/PAGE_SIZE +1;
 		}
 	}
 	
 	private static String wrapPure(String pure) {
 		Pattern p_image;
 		String regEx_img = "<img.*?>";
 		p_image = Pattern.compile(regEx_img, Pattern.CASE_INSENSITIVE);
 		
 		Matcher matcher = p_image.matcher(pure);
 		while(matcher.find()) {
 			System.out.println(matcher.group());
 			pure = matcher.replaceAll("");
 		}
 		return pure;
 	}
 	
 //***************************通讯员相关的文章操作*******************************//
 	@RequestMapping(value="/reporter/article_list-{id}")
 	public String getArticleListOfMe(@PathVariable("id") int reporterId, Model model, HttpServletResponse response) {
 		model.addAttribute("searchInput", "");
 		putReporter(model,response);
 		
 		//List<ArticlePojo> list = articleService.getArticleByCatIdAndPageIndexAndPageSize(2, 1, PAGE_SIZE); // init
 		List<ArticlePojo> list = articleService.getContributedArticlesOfReporter(reporterId);
 		model.addAttribute("el_list", list);
 		
 //		model.addAttribute("pageIndex", 1);
 //		model.addAttribute("nextPageIndex", 2);
 //		model.addAttribute("prePageIndex", 0);
 //		model.addAttribute("pageCount", getPageCount(articleService.getArticleCountByCatId(2, 0)));
 		return "reporter/article_list";
 	}
 	@RequestMapping(value="/reporter/article_list_{type}-{id}")
 	public String getAccArticleListOfMe(@PathVariable("type") String type, @PathVariable("id") int reporterId, Model model, HttpServletResponse response) {
 		model.addAttribute("searchInput", "");
 		putReporter(model,response);
 		
 		//List<ArticlePojo> list = articleService.getArticleByCatIdAndPageIndexAndPageSize(2, 1, PAGE_SIZE); // init
 		List<ArticlePojo> list= null;
 		if (type.equals("acc")) {
 			list = articleService.getAcceptArticlesOfReporter(reporterId);
 		} else if (type.equals("waiting")) {
 			list = articleService.getWaitArticlesOfReporter(reporterId);
 		} else if (type.equals("rej")) {
 			list = articleService.getRejectArticlesOfReporter(reporterId);
 		}
 		
 		model.addAttribute("el_list", list);
 		
 //		model.addAttribute("pageIndex", 1);
 //		model.addAttribute("nextPageIndex", 2);
 //		model.addAttribute("prePageIndex", 0);
 //		model.addAttribute("pageCount", getPageCount(articleService.getArticleCountByCatId(2, 0)));
 		return "reporter/article_list";
 	}
 	
 	@RequestMapping(value="/reporter/reporter-{id}articles_search-{searchInput}")
 	public String searchArticleListOfMe(@PathVariable("id") int reporterId, @PathVariable("searchInput") String searchInput,
 			Model model, HttpServletResponse response) {
 		model.addAttribute("searchInput", searchInput);
 		putReporter(model,response);
 		List<ArticlePojo> list = articleService.searchArticleOfReporter(reporterId, searchInput);
 		model.addAttribute("el_list", list);
 		return "reporter/article_list";
 	}
 	
 	
 	@RequestMapping(value="/reporter/contribute", method=RequestMethod.GET)
 	public String getContribute(Model model, HttpServletResponse response) {
 		putReporter(model, response);
 		return "reporter/contribute_article";
 	}
 	@RequestMapping(value="/reporter/contribute", method=RequestMethod.POST)
 	public void postContribute(@RequestParam("title") String title, @RequestParam("subhead") String subhead,
 			@RequestParam("content") String content, @RequestParam("pure") String pure, @RequestParam("contributed_from") int reporterId,
 			HttpServletResponse response) {
 		
 		int catId = 16;
 		int rootCatId = 2;
 		
 		ArticlePojo article = new ArticlePojo();
 		article.setTitle(title);
 		article.setSubhead(subhead);
 		article.setCatId(catId);
 		article.setRootCatId(rootCatId);
 		article.setContent(content);
 		article.setPostTime(new Date());
 		pure = wrapPure(pure);
 		int briefLength = pure.length() > 100 ? 100 : pure.length();
 		article.setPureText(pure.substring(0, briefLength) + "……");
 		article.setContributedFrom(reporterId);	// reporter贡献的文章
 		articleService.contributeArticle(article);
 		
 		// 增加reporter的贡献率
 		reporterService.incrContribute(reporterId);
 	}
 	
 	@RequestMapping(value="/reporter/alt_contribute-{id}", method=RequestMethod.GET)
 	public String getAlt(@PathVariable("id") int id, Model model, HttpServletResponse response) {
 		putReporter(model, response);
 		ArticlePojo article = articleService.getArticleById(id);
 		model.addAttribute("el", article);
 		return "/reporter/alt_article";
 	}
 	@RequestMapping(value="/reporter/alt_contribute", method=RequestMethod.POST)
 	public void postAlt(@RequestParam("id") int id, @RequestParam("title") String title, @RequestParam("subhead") String subhead,
 			@RequestParam("content") String content, @RequestParam("pure") String pure, @RequestParam("contributed_from") int reporterId,
 			HttpServletResponse response) {
 		
 		int catId = 17;
 		int rootCatId = 2;
 		
 		ArticlePojo article = new ArticlePojo();
 		article.setId(id);
 		article.setTitle(title);
 		article.setSubhead(subhead);
 		article.setCatId(catId);
 		article.setRootCatId(rootCatId);
 		article.setContent(content);
 		article.setPostTime(new Date());
 		pure = wrapPure(pure);
 		int briefLength = pure.length() > 100 ? 100 : pure.length();
 		article.setPureText(pure.substring(0, briefLength) + "……");
 		article.setContributedFrom(reporterId);	// reporter贡献的文章
 		articleService.updateContributedArticle(article);
 	}
 	
 	
 //***************************管理员对通讯员的相关的文章操作*******************************//	
 	@RequestMapping(value="/admin/contribute_list_{type}_{start}_{end}")
 	public String getContributeList(@PathVariable("type") String type, @PathVariable("start") String s, @PathVariable("end") String e, Model model, HttpServletResponse response) {
 		List<ArticlePojo> list = null;
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
 		Date start = null;
 		Date end = null;
 		try {
 			start = sdf.parse(s);
 			end = sdf.parse(e);
			Calendar calendar = Calendar.getInstance(Locale.CHINA);
			calendar.setTime(end);
			calendar.add(Calendar.DATE, 1);
			end = calendar.getTime();
			
 		} catch (ParseException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		if (type.equals("waiting")) {
 			list = articleService.getWaitingArticles(start, end);
 		} else if (type.equals("all")) {
 			list = articleService.getContributedArticles(start, end);
 		} else if (type.equals("accept")) {
 			list = articleService.getAcceptArticles(start, end);
 		} else if (type.equals("reject")) {
 			list = articleService.getRejectArticles(start, end);
 		}
 		putAdmin(model, response);
 		model.addAttribute("el_list", list);
 		model.addAttribute("type", type);
 		model.addAttribute("start", s);
 		model.addAttribute("end", e);
 		model.addAttribute("fType", "list");	//罗列类型：search、list
 		return "admin_articles_fromreporter";
 	}
 	
 	@RequestMapping(value="/admin/{id}_contribute_list_{type}")
 	public String getReporterContributeList(@PathVariable("type") String type, @PathVariable("id") int reporterId,
 			Model model, HttpServletResponse response) {
 		List<ArticlePojo> list = null;
 		if (type.equals("waiting")) {
 			list = articleService.getWaitArticlesOfReporter(reporterId);
 		} else if (type.equals("all")) {
 			list = articleService.getContributedArticlesOfReporter(reporterId);
 		} else if (type.equals("accept")) {
 			list = articleService.getAcceptArticlesOfReporter(reporterId);
 		}
 		putAdmin(model, response);
 		model.addAttribute("el_list", list);
 		model.addAttribute("start", ViewUtil.weekBefore());
 		model.addAttribute("end", ViewUtil.today());
 		model.addAttribute("type", type);
 		model.addAttribute("fType", "list");	//罗列类型：search、list
 		return "admin_articles_fromreporter";
 	}
 	
 	
 	@RequestMapping("/admin/contribute_list_search-{type}_{searchInput}")
 	public String searchContribute(@PathVariable("type") String t, @PathVariable("searchInput")String searchInput, Model model, HttpServletRequest request, HttpServletResponse response) {
 		putAdmin(model, response);
 		int type = 0;
 		if (t.equals("accept")) {
 			type = 1;
 		} else if (t.equals("reject")) {
 			type= -1;
 		}
 		List<ArticlePojo> list = articleService.searchContribute(type, searchInput);
 		putAdmin(model, response);
 		model.addAttribute("el_list", list);
 		model.addAttribute("type", t);
 		model.addAttribute("fType", "search");	//罗列类型：search、list
 		model.addAttribute("searchInput", searchInput);
 		return "admin_articles_fromreporter";
 	}
 	
 	
 	@RequestMapping("/admin/contribute_list")
 	public void defaultContributeDirect(Model model, HttpServletResponse response) {
 		Calendar cal = Calendar.getInstance(Locale.CHINA);
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
 		String end = sdf.format(cal.getTime());
 		cal.add(Calendar.DATE, -7);
 		String start = sdf.format(cal.getTime());
 		try {
 			response.sendRedirect(ViewUtil.getContextPath() + "/admin/contribute_list_waiting_" + start + "_" + end);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 	@RequestMapping(value="/admin/preview-{id}", method=RequestMethod.GET)
 	public String previewContributedArticle(@PathVariable("id") int id, Model model, HttpServletResponse response) {
 		putAdmin(model, response);
 		ArticlePojo article = articleService.getArticleById(id);
 		model.addAttribute("el", article);
 		
 		/* get cats */
 		List<CategoryPojo> categories = catService.getRootCategories();
 		model.addAttribute("top_cat_list", categories);
 		categories = catService.getCategoriesByParentId(article.getRootCatId());
 		model.addAttribute("init_sub_cat_list" ,categories);
 		return "admin_articles_preview";
 	}
 	
 	@RequestMapping(value="/admin/accept", method=RequestMethod.POST)
 	public void acceptArticle(@RequestParam("id") String idStr, @RequestParam("title") String title, @RequestParam("subhead") String subhead,
 			@RequestParam("catId") String catIdStr, @RequestParam("rootCatId") String rootCatIdStr,
 			@RequestParam("content") String content, @RequestParam("from") String from, @RequestParam("pure") String pure, @RequestParam("contributedFrom") int contributedFrom,
 			HttpServletResponse response) {
 		ArticlePojo article = new ArticlePojo();
 		article.setId(Integer.parseInt(idStr));
 		article.setTitle(title);
 		article.setSubhead(subhead);
 		article.setCatId(Integer.parseInt(catIdStr));
 		article.setRootCatId(Integer.parseInt(rootCatIdStr));
 		article.setContent(content);
 		article.setFrom(from);
 		article.setPostTime(new Date());
 		article.setContributedFrom(contributedFrom); // no use in SQL
 		pure = wrapPure(pure);
 		int briefLength = pure.length() > 100 ? 100 : pure.length();
 		article.setPureText(pure.substring(0, briefLength) + "……");
 		articleService.acceptArticle(article);
 		reporterService.incrAccept(contributedFrom);
 	}
 	@RequestMapping(value="/admin/reject-{id}", method=RequestMethod.GET)
 	public void rejectArticle(@PathVariable("id") int id, Model model, HttpServletResponse response) {
 		articleService.rejectArticle(id);
 		try {
 			response.sendRedirect(ViewUtil.getContextPath() + "/admin/contribute_list");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 }
