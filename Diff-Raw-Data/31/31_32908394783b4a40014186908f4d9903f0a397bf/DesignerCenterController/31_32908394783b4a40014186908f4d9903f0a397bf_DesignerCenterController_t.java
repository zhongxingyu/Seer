 package cn.seu.cose.controller;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.Writer;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.multipart.MultipartFile;
 
 import cn.seu.cose.command.MessageCommand;
 import cn.seu.cose.entity.Blog;
 import cn.seu.cose.entity.Comment;
 import cn.seu.cose.entity.CommentPojo;
 import cn.seu.cose.entity.Designer;
 import cn.seu.cose.entity.Work;
 import cn.seu.cose.entity.WorkPojo;
 import cn.seu.cose.filter.SecurityContextHolder;
 import cn.seu.cose.service.BlogService;
 import cn.seu.cose.service.CommentService;
 import cn.seu.cose.service.DesignerService;
 import cn.seu.cose.service.WorkService;
 import cn.seu.cose.util.Constant.CommentType;
 import cn.seu.cose.view.util.ViewUtil;
 
 @Controller
 public class DesignerCenterController extends AbstractController {
 
 	@Autowired
 	DesignerService designerService;
 	@Autowired
 	WorkService workService;
 	@Autowired
 	CommentService commentService;
 	@Autowired
 	BlogService blogService;
 
 	@RequestMapping("/designer")
 	public String designerCenter(Model model) {
 		basicIssue(model);
 		List<Blog> recentBlogs = blogService.getRecentBlogsByPnAndPageSize(1,
 				20);
 		List<Blog> hotBlogs = blogService.getHotBlogsByPnAndPageSize(1, 20);
 		List<Designer> popularDesigners = designerService.getPopularDesigner();
 		List<Designer> hardWorkingDesigners = designerService
 				.getHardWorkingDesigners(6);
 		List<WorkPojo> recentWorks = workService.getRecentWorks(9);
 		List<WorkPojo> hotWorks = workService.getHotWorksWithCommentCount(9);
 		model.addAttribute("recentBlogs", recentBlogs);
 		model.addAttribute("hotBlogs", hotBlogs);
 		model.addAttribute("popularDesigners", popularDesigners);
 		model.addAttribute("hardWorkingDesigners", hardWorkingDesigners);
 		model.addAttribute("recentWorks", recentWorks);
 		model.addAttribute("hotWorks", hotWorks);
 		model.addAttribute("titleName", "设计师之家");
 		return "designer/index";
 	}
 
 	@RequestMapping("/designer/{designerId}")
 	public String designerCenterIndex(Model model,
 			@PathVariable("designerId") int designerId,
 			@RequestParam(value = "pn", required = false) Integer pn) {
 		basicIssue(model);
 
 		if (pn == null || pn <= 0) {
 			pn = 1;
 		}
 		Designer designer = designerService.getDesignerById(designerId);
 		List<WorkPojo> works = workService.getWorkByUserAndPnAndSize(
 				designerId, 1, 3);
 		List<Blog> blogs = blogService.getBlogsByDesignerIdAndPnAndPageSize(
 				designerId, 1, 5);
 		List<CommentPojo> comments = commentService
 				.getCommentsByRefAndTypeAndPnAndSize(designerId,
 						CommentType.DESIGNER.ordinal(), pn, 10);
 		model.addAttribute("designer", designer);
 		model.addAttribute("works", works);
 		model.addAttribute("blogs", blogs);
 		model.addAttribute("comments", comments);
 
 		int totalCount = commentService.getCommentCountByRefAndType(designerId,
 				CommentType.DESIGNER.ordinal());
 
 		int worksCount = workService.getWorksCountByDesignerId(designerId);
 		model.addAttribute("worksCount", worksCount);
 		model.addAttribute("pageIndex", pn);
 		model.addAttribute("pageCount",
 				(int) Math.ceil((double) totalCount / 10));
 		model.addAttribute("totalCount", totalCount);
 		StringBuilder sb = new StringBuilder();
 		sb.append(ViewUtil.getContextPath()).append("/designer/")
 				.append(designerId);
 		model.addAttribute("uri", sb.toString());
 		model.addAttribute("titleName", designer.getUserName() + " - 设计师");
 
 		return "designer/designerCenterIndex";
 	}
 
 	/**
 	 * works page -> browse works
 	 * 
 	 * @return
 	 */
 	@RequestMapping("/designer/{designerId}/works")
 	public String designerCenterWorks(Model model,
 			@PathVariable("designerId") int designerId,
 			@RequestParam(value = "pn", required = false) Integer pn) {
 		basicIssue(model);
 
 		Designer designer = designerService.getDesignerById(designerId);
 		pn = pn == null || pn <= 0 ? 1 : pn;
 		int pageSize = 9;
 		List<WorkPojo> works = workService.getWorkByUserAndPnAndSize(
 				designerId, pn, pageSize);
 
 		int totalCount = workService.getWorksCountByDesignerId(designerId);
 		model.addAttribute("pageIndex", pn);
 		model.addAttribute("pageCount",
 				(int) Math.ceil((double) totalCount / pageSize));
 		model.addAttribute("totalCount", totalCount);
 		StringBuilder sb = new StringBuilder();
 		sb.append(ViewUtil.getContextPath()).append("/designer/")
 				.append(designerId).append("/works");
 		model.addAttribute("uri", sb.toString());
 
 		model.addAttribute("designer", designer);
 		model.addAttribute("works", works);
 		model.addAttribute("titleName", "设计作品 - " + designer.getUserName());
 
 		return "designer/works";
 	}
 
 	/**
 	 * view work
 	 * 
 	 * @return
 	 */
 	@RequestMapping("/designer/{designerId}/works/{workId}.html")
 	public String viewWork(Model model,
 			@PathVariable("designerId") int designerId,
 			@PathVariable("workId") int workId,
 			@RequestParam(value = "pn", required = false) Integer pn,
 			HttpServletResponse response) {
 		try {
 			basicIssue(model);
 			Designer designer = designerService.getDesignerById(designerId);
 			WorkPojo work = workService.getWorkViaId(workId);
 
 			if (designer == null || work == null
 					|| work.getUserId() != designerId) {
 				response.sendRedirect(ViewUtil.getContextPath() + "/");
 				return null;
 			}
 			pn = pn == null || pn <= 0 ? 1 : pn;
 			List<CommentPojo> comments = commentService
 					.getCommentsByRefAndTypeAndPnAndSize(workId,
 							CommentType.WORK.ordinal(), pn, 10);
 
 			model.addAttribute("designer", designer);
 			model.addAttribute("work", work);
 			model.addAttribute("comments", comments);
 
 			int totalCount = commentService.getCommentCountByRefAndType(workId,
 					CommentType.WORK.ordinal());
 			model.addAttribute("pageIndex", pn);
 			model.addAttribute("pageCount",
 					(int) Math.ceil((double) totalCount / 10));
 			model.addAttribute("totalCount", totalCount);
 			StringBuilder sb = new StringBuilder();
 			sb.append(ViewUtil.getContextPath()).append("/designer/")
 					.append(designerId).append("/works/").append(workId)
 					.append(".html");
 			model.addAttribute("uri", sb.toString());
 			model.addAttribute("titleName", work.getWorkName() + " - "
 					+ designer.getUserName());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return "designer/viewWork";
 	}
 
	@RequestMapping(value = "/designer/{designerId}/works/{workId}/dlt", method = RequestMethod.POST)
	public void deleteWork(Model model,
			@PathVariable("designerId") int designerId,
			@PathVariable("workId") int workId, HttpServletResponse response) {
		try {
			WorkPojo work = workService.getWorkViaId(workId);
			if (designerService.isTheSignInOne(designerId) && work != null
					&& work.getUserId() == designerId) {
				workService.deleteWork(workId);
				response.getWriter().write("1");
			} else {
				response.getWriter().write("0");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

 	@RequestMapping(value = "/sign-in", method = RequestMethod.GET)
 	public String signInPage(Model model, HttpServletResponse response)
 			throws IOException {
 		if (designerService.isSignIn()) {
 			response.sendRedirect(ViewUtil.getContextPath() + "/");
 			return "index";
 		} else {
 			basicIssue(model);
 			model.addAttribute("titleName", "登录");
 			return "signIn";
 		}
 	}
 
 	@RequestMapping(value = "/sign-in", method = RequestMethod.POST)
 	public void signIn(Model model, HttpServletResponse response,
 			@RequestParam("username") String username,
 			@RequestParam("password") String password) throws IOException {
 		Designer designer = designerService.signIn(username, password);
 		if (designer == null) {
 			response.getWriter().write("0");
 		} else {
 			basicIssue(model);
 			response.getWriter().write("1");
 		}
 	}
 
 	/**
 	 * register page
 	 * 
 	 * @throws IOException
 	 */
 	@RequestMapping(value = "/register", method = RequestMethod.GET)
 	public String registerPage(Model model, HttpServletResponse response)
 			throws IOException {
 		if (designerService.isSignIn()) {
 			response.sendRedirect(ViewUtil.getContextPath() + "/");
 			return "index";
 		} else {
 			basicIssue(model);
 			model.addAttribute("titleName", "注册");
 			return "register";
 		}
 	}
 
 	/**
 	 * go to active page
 	 * 
 	 * @throws IOException
 	 */
 	@RequestMapping(value = "/register-finish", method = RequestMethod.GET)
 	public String goToActivePage(Model model, HttpServletResponse response)
 			throws IOException {
 		if (designerService.isSignIn()) {
 			response.sendRedirect(ViewUtil.getContextPath() + "/");
 			return "index";
 		} else {
 			basicIssue(model);
 			model.addAttribute("titleName", "请前往邮箱激活账号");
 			return "goToActive";
 		}
 	}
 
 	/**
 	 * register submit
 	 * 
 	 * @throws IOException
 	 */
 	@RequestMapping(value = "/register", method = RequestMethod.POST)
 	public void registerPage(Model model, HttpServletResponse response,
 			@RequestParam("name") String name,
 			@RequestParam("pswd") String pswd,
 			@RequestParam("email") String email) throws IOException {
 		PrintWriter out = response.getWriter();
 		if (designerService.isSignIn()) {
 			out.write("0");
 		} else if (designerService.getDesignerByName(name) != null) {
 			out.write("2");
 		} else {
 			Designer designer = new Designer();
 			designer.setUserName(name);
 			designer.setPassword(pswd);
 			designer.setvEmail(email);
 			designerService.registerDesigner(designer);
 			designer = designerService.getDesignerByName(name);
 			designerService.signIn(name, pswd);
 			out.write("1");
 		}
 	}
 
 	@RequestMapping(value = "/log-off", method = RequestMethod.GET)
 	public void logOff(Model model, HttpServletResponse response) {
 		designerService.logOff();
 		try {
 			response.sendRedirect(ViewUtil.getContextPath() + "/");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * leave a message to designer
 	 */
 	@RequestMapping(value = "/designer/{designerId}/message", method = RequestMethod.POST)
 	public void leaveMessage(Model model, MessageCommand command,
 			HttpServletResponse response,
 			@PathVariable("designerId") int designerId) {
 		Designer designer = SecurityContextHolder.getSecurityContext()
 				.getDesigner();
 		if (designer != null) {
 			Comment comment = new Comment();
 			comment.setUserId(designer.getId());
 			comment.setContent(command.getMessage());
 			comment.setReferenceId(designerId);
 			comment.setCommentType(CommentType.DESIGNER.ordinal());
 			commentService.insertComment(comment);
 		}
 		try {
 			response.sendRedirect(ViewUtil.getContextPath() + "/designer/"
 					+ designerId);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * comment to work in designer center
 	 */
 	@RequestMapping(value = "/designer/{designerId}/works/{workId}/comment", method = RequestMethod.POST)
 	public void commentWorkInDesignerCenter(Model model,
 			MessageCommand command, HttpServletResponse response,
 			@PathVariable("designerId") int designerId,
 			@PathVariable("workId") int workId) {
 		Designer designer = SecurityContextHolder.getSecurityContext()
 				.getDesigner();
 		if (designer != null) {
 			Comment comment = new Comment();
 			comment.setUserId(designer.getId());
 			comment.setContent(command.getMessage());
 			comment.setReferenceId(workId);
 			comment.setCommentType(CommentType.WORK.ordinal());
 			commentService.insertComment(comment);
 		}
 		try {
 			response.sendRedirect(ViewUtil.getContextPath() + "/designer/"
 					+ designerId + "/works/" + workId + ".html");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * edit designer's profile
 	 * 
 	 * @return
 	 */
 	@RequestMapping(value = "/designer/{designerId}/admin/profile", method = RequestMethod.GET)
 	public String designerCenterEditProfile(Model model,
 			@PathVariable("designerId") int designerId) {
 		basicIssue(model);
 		Designer curUser = SecurityContextHolder.getSecurityContext()
 				.getDesigner();
 		if (curUser == null || curUser.getId() != designerId) {
 			return "index";
 		}
 		model.addAttribute("designer", curUser);
 		model.addAttribute("titleName", "修改个人资料");
 		return "designer/editProfile";
 	}
 
 	/**
 	 * edit designer's profile
 	 * 
 	 * @return
 	 */
 	@RequestMapping(value = "/designer/{designerId}/admin/profile-finish", method = RequestMethod.GET)
 	public String designerCenterProfileFinish(Model model,
 			@PathVariable("designerId") int designerId) {
 		basicIssue(model);
 		Designer curUser = SecurityContextHolder.getSecurityContext()
 				.getDesigner();
 		if (curUser == null || curUser.getId() != designerId) {
 			return "index";
 		}
 		model.addAttribute("designer", curUser);
 		model.addAttribute("titleName", "个人资料修改完成");
 		return "designer/profileFinish";
 	}
 
 	@RequestMapping(value = "/designer/{designerId}/admin/profile", method = RequestMethod.POST)
 	public void editProfile(HttpServletResponse response, Model model,
 			@RequestParam("email") String email,
 			@RequestParam("intro") String intro,
 			@PathVariable("designerId") int designerId) throws IOException {
 		basicIssue(model);
 		Designer curUser = SecurityContextHolder.getSecurityContext()
 				.getDesigner();
 		Writer writer = response.getWriter();
 		if (curUser == null || curUser.getId() != designerId) {
 			writer.write("0");
 		} else {
 			curUser.setIntro(intro);
 			if (!email.equals(curUser.getEmail())) {
 				curUser.setvEmail(email);
 				designerService.changeEmail(curUser);
 				writer.write("1");
 			} else {
 				writer.write("2");
 			}
 			designerService.updateDesigner(curUser);
 			model.addAttribute("curUser", curUser);
 			model.addAttribute("designer", curUser);
 		}
 	}
 
 	/**
 	 * change password
 	 */
 	@RequestMapping(value = "/designer/{designerId}/admin/pswd", method = RequestMethod.GET)
 	public String changePswd(Model model, HttpServletResponse response,
 			@PathVariable("designerId") int designerId) {
 		basicIssue(model);
 		Designer designer = designerService.getDesignerById(designerId);
 		model.addAttribute("designer", designer);
 		if (designerService.isTheSignInOne(designerId)) {
 			model.addAttribute("titleName", "修改密码");
 			return "designer/changePswd";
 		} else {
 			try {
 				response.sendRedirect(ViewUtil.getContextPath()
 						+ "/designer/designerCenterIndex");
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @throws IOException
 	 * 
 	 */
 	@RequestMapping(value = "/designer/{designerId}/admin/pswd", method = RequestMethod.POST)
 	public void changePswdPost(Model model, HttpServletResponse response,
 			@PathVariable("designerId") int designerId,
 			@RequestParam("currentPswd") String currentPswd,
 			@RequestParam("newPswd") String newPswd) throws IOException {
 		Writer writer = response.getWriter();
 		if (designerService.isTheSignInOne(designerId)) {
 			Designer designer = designerService.getCurrentUser();
 			if (designer.getPassword().equals(currentPswd)) {
 				designer.setPassword(newPswd);
 				designerService.updateDesigner(designer);
 				writer.write("1");
 				return;
 			}
 		}
 		writer.write("0");
 		return;
 	}
 
 	/**
 	 * post a new work
 	 * 
 	 * @return
 	 */
 	@RequestMapping(value = "/designer/{designerId}/admin/new-work", method = RequestMethod.GET)
 	public String newWorkPage(Model model, HttpServletResponse response,
 			@PathVariable("designerId") int designerId) {
 		basicIssue(model);
 		Designer designer = designerService.getDesignerById(designerId);
 		model.addAttribute("designer", designer);
 		if (designerService.isTheSignInOne(designerId)) {
 			model.addAttribute("titleName", "上传作品");
 			return "designer/newWork";
 		} else {
 			try {
 				response.sendRedirect(ViewUtil.getContextPath()
 						+ "/designer/designerCenterIndex");
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		return null;
 
 	}
 
 	@RequestMapping(value = "/designer/{designerId}/admin/edit-avatar", method = RequestMethod.GET)
 	public String editAvatar(Model model, HttpServletResponse response,
 			@PathVariable("designerId") int designerId) {
 		basicIssue(model);
 		Designer designer = designerService.getDesignerById(designerId);
 		model.addAttribute("designer", designer);
 		if (designerService.isTheSignInOne(designerId)) {
 			model.addAttribute("titleName", "编辑头像");
 			return "designer/editAvatar";
 		} else {
 			try {
 				response.sendRedirect(ViewUtil.getContextPath()
 						+ "/designer/designerCenterIndex");
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		return null;
 	}
 
 	@RequestMapping(value = "/designer/{designerId}/admin/edit-avatar", method = RequestMethod.POST)
 	public void editAvatar(HttpServletRequest request,
 			HttpServletResponse response, Model model,
 			@RequestParam MultipartFile avatar,
 			@PathVariable("designerId") int designerId) throws IOException {
 		basicIssue(model);
 		Designer designer = designerService.getDesignerById(designerId);
 		model.addAttribute("designer", designer);
 		if (!designerService.isTheSignInOne(designerId)
 				|| !designerService.getCurrentUser().isCertificated()) {
 			response.sendRedirect(ViewUtil.getContextPath() + "/designer/"
 					+ designerId);
 			return;
 		}
 		if (avatar.getSize() == 0) {
 			response.sendRedirect(ViewUtil.getContextPath() + "/designer/"
 					+ designerId);
 			return;
 		}
 		String path = request.getSession().getServletContext()
 				.getRealPath("static/avatar");
 		String fileName = "";
 		if (avatar != null) {
 			DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
 			fileName = format.format(new Date()) + avatar.getOriginalFilename();
 			File targetFile = new File(path, fileName);
 			String contentType = avatar.getContentType();
 			if (contentType.equals("image/jpeg")
 					|| contentType.equals("image/jpg")
 					|| contentType.equals("image/bmp")
 					|| contentType.equals("image/png")
 					|| contentType.equals("image/gif") && !targetFile.exists()) {
 				// 保存
 				try {
 					avatar.transferTo(targetFile);
 					designer.setAvatar(fileName);
 					designerService.updateDesigner(designer);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		response.sendRedirect(ViewUtil.getContextPath() + "/designer/"
 				+ designerId);
 	}
 
 	@RequestMapping(value = "/designer/{designerId}/admin/new-work", method = RequestMethod.POST)
 	public void postWork(HttpServletRequest request,
 			HttpServletResponse response, Model model,
 			@RequestParam MultipartFile[] file,
 			@RequestParam("title") String title,
 			@RequestParam("intro") String intro,
 			@PathVariable("designerId") int designerId) throws IOException {
 		basicIssue(model);
 		Designer designer = designerService.getDesignerById(designerId);
 		model.addAttribute("designer", designer);
 		if (!designerService.isTheSignInOne(designerId)
 				|| !designerService.getCurrentUser().isCertificated()) {
 			response.sendRedirect(ViewUtil.getContextPath() + "/designer/"
 					+ designerId);
 			return;
 		}
 		Work work = new Work();
 		work.setWorkName(title);
 		work.setIntro(intro);
 		work.setUserId(designerId);
 		StringBuilder workPics = new StringBuilder();
 		String path = request.getSession().getServletContext()
 				.getRealPath("static/works");
 		boolean notFirst = false;
 		for (MultipartFile f : file) {
 			DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
 			String fileName = format.format(new Date())
 					+ f.getOriginalFilename();
 			File targetFile = new File(path, fileName);
 			String contentType = f.getContentType();
 			if (contentType.equals("image/jpeg")
 					|| contentType.equals("image/jpg")
 					|| contentType.equals("image/bmp")
 					|| contentType.equals("image/png")
 					|| contentType.equals("image/gif") && !targetFile.exists()) {
 				// 保存
 				try {
 					f.transferTo(targetFile);
 					if (notFirst) {
 						workPics.append(";");
 					}
 					workPics.append(fileName);
 					notFirst = true;
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		work.setWorkPics(workPics.toString());
 		workService.insertWork(work);
 		response.sendRedirect(ViewUtil.getContextPath() + "/designer/"
 				+ designerId);
 	}
 
 	/**
 	 * post a new blog
 	 * 
 	 * @return
 	 */
 	@RequestMapping(value = "/designer/{designerId}/admin/new-blog", method = RequestMethod.GET)
 	public String newBlog(Model model,
 			@PathVariable("designerId") int designerId,
 			HttpServletResponse response) {
 		try {
 			basicIssue(model);
 			if (designerService.isTheSignInOne(designerId)) {
 				Designer designer = designerService.getCurrentUser();
 				model.addAttribute("designer", designer);
 			} else if (designerService.getCurrentUser() == null) {
 				response.sendRedirect(ViewUtil.getContextPath() + "/sign-in");
 				return null;
 			} else {
 				response.sendRedirect(ViewUtil.getContextPath() + "/designer/"
 						+ designerId);
 				return null;
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		model.addAttribute("titleName", "发表博文");
 		return "designer/newBlog";
 	}
 
 	@RequestMapping(value = "/designer/{designerId}/admin/new-blog", method = RequestMethod.POST)
 	public void newBlogPost(Model model,
 			@PathVariable("designerId") int designerId,
 			@RequestParam("title") String title,
 			@RequestParam("original") int original,
 			@RequestParam("category") int category,
 			@RequestParam("content") String content,
 			@RequestParam("pureText") String pureText,
 			HttpServletResponse response) {
 		try {
 			if (designerService.isTheSignInOne(designerId)) {
 				boolean reprinted = original == 1 ? false : true;
 				blogService.newBlog(title, reprinted, category, pureText,
 						content, designerId);
 				response.getWriter().write("1");
 			} else {
 				response.getWriter().write("0");
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	@RequestMapping(value = "/designer/{designerId}/blogs/{blogId}", method = RequestMethod.GET)
 	public String viewBlog(Model model,
 			@PathVariable("designerId") int designerId,
 			@PathVariable("blogId") int blogId, HttpServletResponse response,
 			@RequestParam(value = "pn", required = false) Integer pn) {
 		try {
 			Blog blog = blogService.getBlogById(blogId);
 			if (blog == null || designerId != blog.getDesignerId()) {
 				response.sendRedirect(ViewUtil.getContextPath() + "/designer");
 				return null;
 			} else {
 				if (!designerService.isTheSignInOne(designerId)) {
 					blogService.addClickCount(blogId);
 				}
 				basicIssue(model);
 				model.addAttribute("blog", blog);
 				Designer designer = designerService.getDesignerById(designerId);
 				model.addAttribute("designer", designer);
 
 				pn = pn == null || pn <= 0 ? 1 : pn;
 				int pageSize = 10;
 				List<CommentPojo> comments = commentService
 						.getCommentsByRefAndTypeAndPnAndSize(blogId,
 								CommentType.BLOG.ordinal(), pn, pageSize);
 
 				model.addAttribute("comments", comments);
 				int totalCount = commentService.getCommentCountByRefAndType(
 						blogId, CommentType.BLOG.ordinal());
 				model.addAttribute("pageIndex", pn);
 				model.addAttribute("pageCount",
 						(int) Math.ceil((double) totalCount / pageSize));
 				model.addAttribute("totalCount", totalCount);
 				StringBuilder sb = new StringBuilder();
 				sb.append(ViewUtil.getContextPath()).append("/designer/")
 						.append(designerId).append("/blogs/").append(blogId);
 				model.addAttribute("uri", sb.toString());
 				model.addAttribute("titleName", blog.getTitle() + " - "
 						+ designer.getUserName());
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return "designer/viewBlog";
 	}
 
 	/**
 	 * comment to work in designer center
 	 */
 	@RequestMapping(value = "/designer/{designerId}/blogs/{blogId}/comment", method = RequestMethod.POST)
 	public void commentBlog(Model model, MessageCommand command,
 			HttpServletResponse response,
 			@PathVariable("designerId") int designerId,
 			@PathVariable("blogId") int blogId) {
 		Designer designer = SecurityContextHolder.getSecurityContext()
 				.getDesigner();
 		if (designer != null) {
 			Comment comment = new Comment();
 			comment.setUserId(designer.getId());
 			comment.setContent(command.getMessage());
 			comment.setReferenceId(blogId);
 			comment.setCommentType(CommentType.BLOG.ordinal());
 			commentService.insertComment(comment);
 		}
 		try {
 			response.sendRedirect(ViewUtil.getContextPath() + "/designer/"
 					+ designerId + "/blogs/" + blogId);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * comment to work in designer center
 	 */
 	@RequestMapping(value = "/designer/{designerId}/blogs/{blogId}/edit", method = RequestMethod.GET)
 	public String editBlog(Model model, HttpServletResponse response,
 			@PathVariable("designerId") int designerId,
 			@PathVariable("blogId") int blogId) {
 		try {
 			Designer designer = designerService.getDesignerById(designerId);
 			Blog blog = blogService.getBlogById(blogId);
 			if (!designerService.isTheSignInOne(designerId)) {
 				response.sendRedirect(ViewUtil.getContextPath() + "/designer/"
 						+ designerId);
 				return null;
 			} else if (blog == null || blog.getDesignerId() != designerId) {
 				response.sendRedirect(ViewUtil.getContextPath() + "/designer/"
 						+ designerId);
 				return null;
 			} else {
 				basicIssue(model);
 				model.addAttribute("designer", designer);
 				model.addAttribute("blog", blog);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		model.addAttribute("titleName", "编辑博文");
 		return "designer/editBlog";
 	}
 
 	@RequestMapping(value = "/designer/{designerId}/blogs/{blogId}/edit", method = RequestMethod.POST)
 	public void editBlogPost(Model model,
 			@PathVariable("designerId") int designerId,
 			@PathVariable("blogId") int blogId,
 			@RequestParam("title") String title,
 			@RequestParam("original") int original,
 			@RequestParam("category") int category,
 			@RequestParam("content") String content,
 			@RequestParam("pureText") String pureText,
 			HttpServletResponse response) {
 		try {
 			Blog blog = blogService.getBlogById(blogId);
 			if (designerService.isTheSignInOne(designerId) && blog != null
 					&& blog.getDesignerId() == designerId) {
 				boolean reprinted = original == 1 ? false : true;
 				blogService.updateBlog(title, reprinted, category, pureText,
 						content, blogId);
 				response.getWriter().write("1");
 			} else {
 				response.getWriter().write("0");
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	@RequestMapping(value = "/designer/{designerId}/blogs", method = RequestMethod.GET)
 	public String blogs(Model model,
 			@PathVariable("designerId") int designerId,
 			@RequestParam(value = "pn", required = false) Integer pn,
 			HttpServletResponse response) {
 		try {
 			Designer designer = designerService.getDesignerById(designerId);
 			if (designer == null) {
 				response.sendRedirect(ViewUtil.getContextPath() + "/designer");
 			} else {
 				basicIssue(model);
 				model.addAttribute("designer", designer);
 				pn = pn == null || pn <= 0 ? 1 : pn;
 				int pageSize = 10;
 				List<Blog> blogs = blogService
 						.getBlogsByDesignerIdAndPnAndPageSize(designerId, pn,
 								pageSize);
 				model.addAttribute("blogs", blogs);
 
 				int totalCount = blogService
 						.getBlogCountByDesignerId(designerId);
 				model.addAttribute("pageIndex", pn);
 				model.addAttribute("pageCount",
 						(int) Math.ceil((double) totalCount / pageSize));
 				model.addAttribute("totalCount", totalCount);
 				StringBuilder sb = new StringBuilder();
 				sb.append(ViewUtil.getContextPath()).append("/designer/")
 						.append(designerId).append("/blogs");
 				model.addAttribute("uri", sb.toString());
 				model.addAttribute("titleName",
 						"博文列表 - " + designer.getUserName());
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return "designer/blogs";
 	}
 
 	@RequestMapping(value = "/designer/{designerId}/blogs/{blogId}/dlt", method = RequestMethod.POST)
 	public void deleteBlog(Model model,
 			@PathVariable("designerId") int designerId,
 			@PathVariable("blogId") int blogId, HttpServletResponse response) {
 		try {
 			Blog blog = blogService.getBlogById(blogId);
 			if (designerService.isTheSignInOne(designerId) && blog != null
 					&& blog.getDesignerId() == designerId) {
 				blogService.archiveBlog(blogId);
 				response.getWriter().write("1");
 			} else {
 				response.getWriter().write("0");
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	@RequestMapping(value = "/designer/active", method = RequestMethod.GET)
 	public void activeDesigner(Model model, @RequestParam("key") String key,
 			HttpServletResponse response) {
 		try {
 			Designer designer = designerService.activeDesigner(key);
 			designerService.signIn(designer.getUserName(),
 					designer.getPassword());
 			response.sendRedirect(ViewUtil.CONTEXT_PATH + "/");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@RequestMapping(value = "/forget-pswd", method = RequestMethod.GET)
 	public String forgetPswd(Model model, HttpServletResponse response) {
 		try {
 			if (designerService.getCurrentUser() != null) {
 				response.sendRedirect(ViewUtil.CONTEXT_PATH + "/");
 				return null;
 			}
 			basicIssue(model);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		model.addAttribute("titleName", "找回密码");
 		return "forgetPswd";
 	}
 
 	@RequestMapping(value = "/forget-pswd", method = RequestMethod.POST)
 	public String forgetPswd(Model model, HttpServletResponse response,
 			@RequestParam("username") String username) {
 		try {
 			if (designerService.getCurrentUser() != null) {
 				response.sendRedirect(ViewUtil.CONTEXT_PATH + "/");
 				return null;
 			}
 			basicIssue(model);
 			Designer designer = designerService.getDesignerByName(username);
 			if (designer != null) {
 				designerService.retrievePswd(designer);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return "forgetPswdFinish";
 	}
 
 	@RequestMapping(value = "/retrieve-pswd", method = RequestMethod.GET)
 	public String retrievePswd(Model model, HttpServletResponse response,
 			@RequestParam("key") String pswd,
 			@RequestParam("username") String username) {
 		try {
 			Designer designer = designerService.getDesignerByUsernameAndPswd(
 					username, pswd);
 			if (designer == null) {
 				response.sendRedirect(ViewUtil.CONTEXT_PATH + "/");
 				return null;
 			} else {
 				designerService.signIn(username, pswd);
 				basicIssue(model);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return "retrievePswd";
 	}
 
 	@RequestMapping(value = "/retrieve-pswd", method = RequestMethod.POST)
 	public void retrievePswdPost(Model model, HttpServletResponse response,
 			@RequestParam("pswd") String pswd,
 			@RequestParam("pswd2") String pswd2) {
 		try {
 			Designer designer = designerService.getCurrentUser();
 			if (designer == null) {
 				response.sendRedirect(ViewUtil.CONTEXT_PATH + "/");
 				return;
 			}
 			basicIssue(model);
 			designer.setPassword(pswd);
 			designerService.updateDesigner(designer);
 			response.sendRedirect(ViewUtil.CONTEXT_PATH + "/");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
