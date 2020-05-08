 package org.cweili.wray.interceptor;
 
 import java.util.Enumeration;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.cweili.wray.service.ArticleService;
 import org.cweili.wray.service.CategoryService;
 import org.cweili.wray.service.CommentService;
 import org.cweili.wray.service.ConfigService;
 import org.cweili.wray.service.LinkService;
 import org.cweili.wray.service.TagService;
 import org.cweili.wray.util.Constant;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.web.servlet.HandlerInterceptor;
 
 /**
  * 基础拦截器
  * 
  * @author Cweili
  * @version 2012-8-16 下午5:14:17
  * 
  */
 public abstract class BaseInterceptor implements HandlerInterceptor {
 
 	protected static final Log log = LogFactory.getLog(BaseInterceptor.class);
 
 	@Autowired
 	protected ConfigService blogConfig;
 
 	@Autowired
 	protected ArticleService articleService;
 
 	@Autowired
 	protected CommentService commentService;
 
 	@Autowired
 	protected CategoryService categoryService;
 
 	@Autowired
 	protected TagService tagService;
 
 	@Autowired
 	protected LinkService linkService;
 
 	/**
 	 * 返回是否在管理员面板状态
 	 * 
 	 * @param request
 	 * @return
 	 */
 	protected boolean isAdminPanel(HttpServletRequest request) {
		return StringUtils.startsWithIgnoreCase(requestScript(request), "/admin-");
 	}
 
 	/**
 	 * 取得请求脚本名
 	 * 
 	 * @param request
 	 * @return
 	 */
 	protected String requestScript(HttpServletRequest request) {
 		return StringUtils.removeStartIgnoreCase(request.getRequestURI(), request.getContextPath());
 	}
 
 	protected String findCookie(HttpServletRequest request, String find) {
 		if (null != request.getCookies()) {
 			for (Cookie cookie : request.getCookies()) {
 				if (find.equals(cookie.getName())) {
 					return cookie.getValue();
 				}
 			}
 		}
 		return null;
 	}
 
 	protected void printRequestHeader(HttpServletRequest request) {
 		if (Constant.DEBUG) {
 			StringBuilder sb = new StringBuilder("\nHTTP Headers:\n");
 			Enumeration<String> r = request.getHeaderNames();
 			String s;
 			while (r.hasMoreElements()) {
 				s = r.nextElement();
 				sb.append(s).append(':').append(request.getHeader(s)).append('\n');
 			}
 
 			log.info(sb.toString());
 		}
 	}
 
 }
