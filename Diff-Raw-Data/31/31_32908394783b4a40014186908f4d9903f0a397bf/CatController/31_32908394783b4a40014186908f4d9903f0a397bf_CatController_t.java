 package cn.seu.cose.controller;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import cn.seu.cose.core.CategoryCache;
 import cn.seu.cose.entity.ArticlePojo;
 import cn.seu.cose.entity.Blog;
 import cn.seu.cose.entity.CategoryPojo;
 import cn.seu.cose.entity.Upload;
 import cn.seu.cose.service.ArticleService;
 import cn.seu.cose.service.BlogService;
 import cn.seu.cose.service.CategoryService;
 import cn.seu.cose.service.UploadService;
 import cn.seu.cose.view.util.ViewUtil;
 
 @Controller
 public class CatController extends AbstractController {
 
 	@Autowired
 	CategoryService categoryService;
 	@Autowired
 	ArticleService articleService;
 	@Autowired
 	UploadService uploadService;
 	@Autowired
 	BlogService blogService;
 
 	// about
 	@RequestMapping("/about")
 	public String viewAboutCatIndex(Model model, HttpServletRequest request) {
 		model.addAttribute("url", request.getServletPath());
 		String pageIndex = (String) request.getParameter("pn");
 		int index = pageIndexResolve(pageIndex);
 		return view(model, 9, index);
 	}
 
 	@RequestMapping("/about/cat-{catId}")
 	public String viewAboutCat(Model model, @PathVariable("catId") int catId,
 			HttpServletRequest request) {
 		model.addAttribute("url", request.getServletPath());
 		String pageIndex = (String) request.getParameter("pn");
 		int index = pageIndexResolve(pageIndex);
 		return view(model, catId, index);
 	}
 
 	// new
 	@RequestMapping("/news")
 	public String viewNewCatIndex(Model model, HttpServletRequest request) {
 		model.addAttribute("url", request.getServletPath());
 		String pageIndex = (String) request.getParameter("pn");
 		int index = pageIndexResolve(pageIndex);
 		return view(model, 2, index);
 	}
 
 	@RequestMapping("/news/cat-{catId}")
 	public String viewNewCat(Model model, @PathVariable("catId") int catId,
 			HttpServletRequest request) {
 		model.addAttribute("url", request.getServletPath());
 		String pageIndex = (String) request.getParameter("pn");
 		int index = pageIndexResolve(pageIndex);
 		return view(model, catId, index);
 	}
 
 	// work
 	@RequestMapping("/workshop")
 	public String viewWorkCatIndex(Model model, HttpServletRequest request) {
 		model.addAttribute("url", request.getServletPath());
 		String pageIndex = (String) request.getParameter("pn");
 		int index = pageIndexResolve(pageIndex);
 		return view(model, 3, index);
 	}
 
 	@RequestMapping("/workshop/cat-{catId}")
 	public String viewWorkCat(Model model, @PathVariable("catId") int catId,
 			HttpServletRequest request) {
 		model.addAttribute("url", request.getServletPath());
 		String pageIndex = (String) request.getParameter("pn");
 		int index = pageIndexResolve(pageIndex);
 		return view(model, catId, index);
 	}
 
 	// members
 	@RequestMapping("/members")
 	public String viewMembersCatIndex(Model model, HttpServletRequest request) {
 		model.addAttribute("url", request.getServletPath());
 		String pageIndex = (String) request.getParameter("pn");
 		int index = pageIndexResolve(pageIndex);
 		return view(model, 4, index);
 	}
 
 	@RequestMapping("/members/cat-57")
 	public String viewMembersCenter(Model model, HttpServletRequest request,
 			HttpServletResponse response) {
 		try {
 			response.sendRedirect(ViewUtil.getContextPath() + "/designer");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	@RequestMapping("/members/cat-{catId}")
 	public String viewMembersCat(Model model, @PathVariable("catId") int catId,
 			HttpServletRequest request) {
 		model.addAttribute("url", request.getServletPath());
 		String pageIndex = (String) request.getParameter("pn");
 		int index = pageIndexResolve(pageIndex);
 		return view(model, catId, index);
 	}
 
 	// events
 	@RequestMapping("/events")
 	public String viewEventsCatIndex(Model model, HttpServletRequest request) {
 		model.addAttribute("url", request.getServletPath());
 		String pageIndex = (String) request.getParameter("pn");
 		int index = pageIndexResolve(pageIndex);
 		return view(model, 5, index);
 	}
 
 	@RequestMapping("/events/cat-{catId}")
 	public String viewEventsCat(Model model, @PathVariable("catId") int catId,
 			HttpServletRequest request) {
 		model.addAttribute("url", request.getServletPath());
 		String pageIndex = (String) request.getParameter("pn");
 		int index = pageIndexResolve(pageIndex);
 		return view(model, catId, index);
 	}
 
 	// policy
 	@RequestMapping("/policy")
 	public String viewPolicyCatIndex(Model model, HttpServletRequest request) {
 		model.addAttribute("url", request.getServletPath());
 		String pageIndex = (String) request.getParameter("pn");
 		int index = pageIndexResolve(pageIndex);
 		return view(model, 6, index);
 	}
 
 	@RequestMapping("/policy/cat-{catId}")
 	public String viewPolicyCat(Model model, @PathVariable("catId") int catId,
 			HttpServletRequest request) {
 		model.addAttribute("url", request.getServletPath());
 		String pageIndex = (String) request.getParameter("pn");
 		int index = pageIndexResolve(pageIndex);
 		return view(model, catId, index);
 	}
 
 	@RequestMapping("/publication-navi/cat-{catId}")
 	public String viewPublicationCat(Model model,
 			@PathVariable("catId") int catId, HttpServletRequest request,
 			HttpServletResponse response) {
 		try {
 			if (catId == 37) {
 				response.sendRedirect(ViewUtil.getContextPath()
 						+ "/publication");
 				return null;
 			} else if (catId == 38) {
 				response.sendRedirect(ViewUtil.getContextPath()
 						+ "/publication-com");
 				return null;
 			} else if (catId == 39) {
 				response.sendRedirect(ViewUtil.getContextPath()
 						+ "/publication-cul");
 				return null;
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		model.addAttribute("url", request.getServletPath());
 		String pageIndex = (String) request.getParameter("pn");
 		int index = pageIndexResolve(pageIndex);
 		return view(model, catId, index);
 	}
 
 	// blogzone
 	@RequestMapping("/blogzone")
 	public String viewBlogzoneCatIndex(Model model, HttpServletRequest request) {
 		model.addAttribute("url", request.getServletPath());
 		String pageIndex = (String) request.getParameter("pn");
 		int index = pageIndexResolve(pageIndex);
 
 		basicIssue(model);
 		CategoryPojo categoryPojo = CategoryCache.get(8);
 		model.addAttribute("curCat", categoryPojo);
 
 		List<CategoryPojo> cats = null;
 		int rootCatId;
 		rootCatId = categoryPojo.getId();
 		model.addAttribute("rootCat", categoryPojo);
 		cats = categoryService.getCategoriesByParentId(categoryPojo.getId());
 		model.addAttribute("childrenCats", cats);
 
 		for (CategoryPojo categoryChild : cats) {
 			int type = categoryChild.getId() <= 45 ? categoryChild.getId() - 40
					: categoryChild.getId() - 57;
 			List<Blog> blogs = blogService.getBlogsByTypeAndPnAndPageSize(type,
 					1, 6);
 			categoryChild.setBlogs(blogs);
 		}
 		model.addAttribute("titleName", categoryPojo.getCatName());
 		return "viewSpecialBlogCat";
 	}
 
 	@RequestMapping("/blogzone/cat-{catId}")
 	public String viewBlogzoneCat(Model model,
 			@PathVariable("catId") int catId, HttpServletRequest request) {
 		model.addAttribute("url", request.getServletPath());
 		String pageIndex = (String) request.getParameter("pn");
 		int index = pageIndexResolve(pageIndex);
 
 		basicIssue(model);
 		CategoryPojo categoryPojo = CategoryCache.get(catId);
 		model.addAttribute("curCat", categoryPojo);
 
 		List<CategoryPojo> cats = null;
 		int rootCatId;
 		rootCatId = CategoryCache.get(categoryPojo.getParentCatId()).getId();
 		model.addAttribute("rootCat",
 				CategoryCache.get(categoryPojo.getParentCatId()));
 		model.addAttribute("curCat", categoryPojo);
 		cats = categoryService.getCategoriesByParentId(categoryPojo
 				.getParentCatId());
 		model.addAttribute("childrenCats", cats);
 
 		// concerns
 		List<ArticlePojo> concerns = articleService.getConcerns();// 15
 		model.addAttribute("concerns", concerns);
 		// workshops
 		List<ArticlePojo> workshopsSider = articleService
 				.getArticleByCatIdAndPnAndPsBrief(3, 1, 5);
 		model.addAttribute("workshopsSider", workshopsSider);
 		// memberStyle
 		List<ArticlePojo> memberStyle = articleService
 				.getArticleByCatIdAndPnAndPsBrief(4, 1, 5);
 		model.addAttribute("memberStyle", memberStyle);
 		// events
 		List<ArticlePojo> events = articleService.getEvents();// 5
 		model.addAttribute("events", events);
 		// trains
 		List<ArticlePojo> trains = articleService.getTrains();// 61
 		model.addAttribute("trains", trains);
 
		int type = catId <= 45 ? catId - 40 : catId - 57;
 		List<Blog> blogs = blogService.getBlogsByTypeAndPnAndPageSize(type,
 				index, 10);
 		int blogCount = blogService.getBlogCountByType(type);
 		model.addAttribute("blogs", blogs);
		model.addAttribute("pageIndex", index);
 		model.addAttribute("pageCount",
 				(int) Math.ceil((double) blogCount / 10));
 		model.addAttribute("blogCount", blogCount);
 		model.addAttribute("titleName", categoryPojo.getCatName());
 		return "viewBlogCatList";
 	}
 
 	// train
 	@RequestMapping("/train")
 	public String viewTrainCatIndex(Model model, HttpServletRequest request) {
 		model.addAttribute("url", request.getServletPath());
 		String pageIndex = (String) request.getParameter("pn");
 		int index = pageIndexResolve(pageIndex);
 		return view(model, 61, index);
 	}
 
 	@RequestMapping("/train/cat-{catId}")
 	public String viewTrainCat(Model model, @PathVariable("catId") int catId,
 			HttpServletRequest request) {
 		model.addAttribute("url", request.getServletPath());
 		String pageIndex = (String) request.getParameter("pn");
 		int index = pageIndexResolve(pageIndex);
 		return view(model, catId, index);
 	}
 
 	@RequestMapping("/download.html")
 	public String downloadCenter(Model model, HttpServletRequest request) {
 		basicIssue(model);
 		int sumCount = uploadService.getUploadCount();
 		String pageIndex = (String) request.getParameter("pn");
 		int index = pageIndexResolve(pageIndex);
 		List<Upload> uploads = uploadService.getUploadsByIndexAndPagesize(
 				index, 20);
 		model.addAttribute("titleName", "下载中心");
 		model.addAttribute("pageCount", (int) Math.ceil((double) sumCount / 20));
 		model.addAttribute("uploads", uploads);
 		model.addAttribute("sumCount", sumCount);
 		model.addAttribute("pageIndex", index);
 		return "download";
 	}
 
 	private String view(Model model, int catId, int pageIndex) {
 		basicIssue(model);
 		CategoryPojo categoryPojo = CategoryCache.get(catId);
 		model.addAttribute("titleName", categoryPojo.getCatName());
 		model.addAttribute("curCat", categoryPojo);
 
 		List<CategoryPojo> cats = null;
 		int rootCatId;
 		if (categoryPojo.getCatLevel() == 1) {
 			rootCatId = categoryPojo.getId();
 			model.addAttribute("rootCat", categoryPojo);
 			cats = categoryService
 					.getCategoriesByParentId(categoryPojo.getId());
 		} else {
 			rootCatId = CategoryCache.get(categoryPojo.getParentCatId())
 					.getId();
 			model.addAttribute("rootCat",
 					CategoryCache.get(categoryPojo.getParentCatId()));
 			model.addAttribute("curCat", categoryPojo);
 			cats = categoryService.getCategoriesByParentId(categoryPojo
 					.getParentCatId());
 		}
 		model.addAttribute("childrenCats", cats);
 
 		// concerns
 		List<ArticlePojo> concerns = articleService.getConcerns();
 		model.addAttribute("concerns", concerns);
 		// workshops
 		List<ArticlePojo> workshopsSider = articleService
 				.getArticleByCatIdAndPnAndPsBrief(3, 1, 5);
 		model.addAttribute("workshopsSider", workshopsSider);
 		// memberStyle
 		List<ArticlePojo> memberStyle = articleService
 				.getArticleByCatIdAndPnAndPsBrief(4, 1, 5);
 		model.addAttribute("memberStyle", memberStyle);
 		// policys
 		List<ArticlePojo> policys = articleService
 				.getArticleByCatIdAndPnAndPsBrief(6, 1, 5);
 		model.addAttribute("policys", policys);
 		// events
 		List<ArticlePojo> events = articleService.getEvents();
 		model.addAttribute("events", events);
 		// trains
 		List<ArticlePojo> trains = articleService.getTrains();
 		model.addAttribute("trains", trains);
 
 		if (categoryPojo.getType() == 1) {
 			if (categoryPojo.isExclusiveArticle()) {
 				model.addAttribute("exclusive", true);
 				model.addAttribute("exclusiveArticle",
 						articleService.getExclusiveArticleByCatId(catId));
 			} else {
 				List<ArticlePojo> articles = articleService
 						.getArticleByCatIdAndPageIndexAndPageSize(catId,
 								pageIndex, 10);
 				int articleCount = articleService.getArticleCountByCatId(
 						rootCatId, catId);
 
 				model.addAttribute("articles", articles);
 				model.addAttribute("pageIndex", pageIndex);
 				model.addAttribute("pageCount",
 						(int) Math.ceil((double) articleCount / 10));
 				model.addAttribute("articleCount", articleCount);
 			}
 		}
 		if (categoryPojo.getCatLevel() == 1 && categoryPojo.getType() == 2) {
 			for (CategoryPojo categoryChild : cats) {
 				if (!categoryChild.isExclusiveArticle()) {
 					List<ArticlePojo> articles = articleService
 							.getArticleByCatIdAndPageIndexAndPageSize(
 									categoryChild.getId(), 1, 7);
 					categoryChild.setArticles(articles);
 				}
 			}
 			return "viewSpecialCat";
 		}
 		return "viewCat";
 	}
 
 	private int pageIndexResolve(String pageIndex) {
 		int index = 1;
 		if (pageIndex != null && !pageIndex.trim().isEmpty()) {
 			index = Integer.valueOf(pageIndex.trim());
 		}
 		return index;
 	}
 }
