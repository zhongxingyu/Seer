 /**
  *
  */
 package q.web;
 
 import java.net.URLEncoder;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang.StringUtils;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.NoSuchBeanDefinitionException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.Controller;
 
 import q.log.Logger;
 import q.util.StringKit;
 import q.web.exception.PeopleNotLoginException;
 
 /**
  * RESTFUL resource entry
  * 
  * @author seanlinwang
  * @date Jan 16, 2011
  * 
  */
 public class ResourceRouter implements Controller, ApplicationContextAware {
 
 	public static final char PATH_SPLIT = '/';
 	public static final String HTTP_METHOD_POST = "post";
 	public static final String HTTP_INNER_METHOD = "_method";
 	public static final String HTTP_METHOD_UPDATE = "update";
 	public static final String HTTP_METHOD_DELETE = "delete";
 	public static final String HTTP_METHOD_GET = "get";
 	private final static Logger log = Logger.getLogger();
 
 	private ApplicationContext applicationContext;
 
 	@Override
 	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
 		this.applicationContext = applicationContext;
 	}
 
 	/**
 	 * default resource
 	 */
 	private Resource defaultResource;
 
 	/**
 	 * set default resource to router
 	 * 
 	 * @param defaultResource
 	 */
 	public void setDefaultResource(Resource defaultResource) {
 		this.defaultResource = defaultResource;
 	}
 
 	private ViewResolver defaultViewResolver = new DefaultViewResolver();
 
 	public void setDefaultViewResolver(ViewResolver viewResolver) {
 		this.defaultViewResolver = viewResolver;
 	}
 
 	private ViewResolver jsonViewResolver;
 
 	public void setJsonViewResolver(ViewResolver jsonViewResolver) {
 		this.jsonViewResolver = jsonViewResolver;
 	}
 
 	private String urlPrefix;
 
 	public void setUrlPrefix(String urlPrefix) {
 		this.urlPrefix = urlPrefix;
 	}
 
 	private String contextPath;
 
 	public void setContextPath(String contextPath) {
 		this.contextPath = contextPath;
 	}
 
 	private String staticUrlPrefix;
 
 	public void setStaticUrlPrefix(String staticUrlPrefix) {
 		this.staticUrlPrefix = staticUrlPrefix;
 	}
 
 	private String imageUrl;
 
 	public void setImageUrl(String imageUrl) {
 		this.imageUrl = imageUrl;
 	}
 
 	private Set<String> needLoginResources;
 
 	public void setNeedLoginResources(Set<String> needLoginResources) {
 		this.needLoginResources = needLoginResources;
 	}
 
 	private String loginPath;
 
 	public void setLoginPath(String loginPath) {
 		this.loginPath = loginPath;
 	}
 
 	@Override
 	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
 		log.debug("requestURL: %s and queryString: %s", request.getRequestURL(), request.getQueryString());
 		String method = request.getMethod().toLowerCase(); // lowercase http method
 		String servletPath = request.getRequestURI().substring(request.getContextPath().length()); // path without context and domain
 		String[] segs = StringKit.split(servletPath, PATH_SPLIT); // split path to path segments
 		Resource resource = getResource(request, method, servletPath, segs); // get request resource
 		if (resource == null) { // if resource not exists , return 404
 			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
 			log.debug("resource not found by method %s and path %s", method, servletPath);
 			return null;
 		} else {
 			ResourceContext context = toResourceContext(request, response, servletPath, segs); // construct resource context
 			boolean isJson = context.isApiRequest();
 			if (this.needLoginResources != null && this.needLoginResources.contains(resource.getName())) { // request resource need visitor login first
 				if (context.getCookiePeopleId() <= 0) { // visitor logoff
 					if (isJson) {
 						context.setErrorModel(new PeopleNotLoginException("login:用户未登录"));
 					} else {
 						context.redirectServletPath(loginPath + "?from=" + URLEncoder.encode(servletPath, "utf-8"));
 						return null;
 					}
 				}
 			}
 
 			try {
 				resource.validate(context);
 				resource.execute(context); // execute resource if exists
 			} catch (ErrorCodeException e) {
 				context.setErrorModel(e);
 				log.debug("resource  %s execute ErrorCodeExeption", e, resource);
 			} catch (Exception e) {// resource internal error
 				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 				log.error("resource  %s execute exeption", e, resource);
 			}
 
 			complementModel(context); // complement model
 			ViewResolver viewResolver = null;
 			if (isJson) {
 				viewResolver = this.jsonViewResolver;
 			} else {
				String from = context.getString("from"); // redirect to origin refferr
				if (from != null) {
					if (from.startsWith("http:")) {
						context.redirectContextPath(from);
					} else {
						context.redirectServletPath(from);
 					}
 				}
 				viewResolver = this.defaultViewResolver;
 			}
 
 			ModelAndView view = viewResolver.view(context, resource);// use resource name as view name
 			return view;
 		}
 	}
 
 	private void complementModel(ResourceContext context) {
 		context.setModel("urlPrefix", this.urlPrefix);
 		context.setModel("contextPath", this.contextPath);
 		context.setModel("staticUrlPrefix", this.staticUrlPrefix);
 		context.setModel("imageUrl", this.imageUrl);
 		if (this.imageUrl != null) {
 			context.setModel("avatarUrlPrefix", this.imageUrl + "/a");
 		}
 		context.setModel("loginCookie", context.getLoginCookie());
 	}
 
 	protected ResourceContext toResourceContext(final HttpServletRequest request, final HttpServletResponse response, String path, String[] segs) {
 		DefaultResourceContext context = new DefaultResourceContext(request, response, segs);
 		context.setContextPath(this.contextPath);
 		context.setUrlPrefix(this.urlPrefix);
 		return context;
 	}
 
 	protected Resource getResource(HttpServletRequest request, String method, String path, String[] segs) {
 		Resource resource = null;
 		String resourceName = toResourceName(method, segs);
 		if (StringKit.isEmpty(resourceName)) {
 			resource = this.defaultResource;
 			resource.setName("default");
 		} else {
 			// get resource by resourceName and method
 			if (HTTP_METHOD_POST.equals(method)) {
 				String _method = request.getParameter(HTTP_INNER_METHOD);
 				if (null == _method) {
 					resource = this.getPostResource(resourceName);
 				} else {
 					_method = _method.toLowerCase();
 					if (HTTP_METHOD_UPDATE.equals(_method)) {
 						resource = this.getUpdateResource(resourceName);
 					}
 					if (HTTP_METHOD_DELETE.equals(_method)) {
 						resource = this.getDeleteResource(resourceName);
 					}
 				}
 			} else if (HTTP_METHOD_GET.equals(method)) {
 				resource = this.getGetResource(resourceName);
 			}
 		}
 		log.debug("get resource:%s by method:%s and path:%s, resourceName:%s", resource, method, path, resourceName);
 		return resource;
 	}
 
 	protected String toResourceName(String method, String[] segs) {
 		String resourceName = null;
 		if (segs.length == 0) {
 			return resourceName;
 		}
 		resourceName = segs[0];
 		if (segs.length == 1) {
 			if (HTTP_METHOD_GET.equals(method)) {
 				resourceName += "Index";
 			}
 		} else if (segs.length == 2) {
 			String last = segs[1];
 			if (!StringUtils.isNumeric(last)) {
 				resourceName += StringKit.capitalize(last);
 			}
 		} else if (segs.length == 3) {
 			if (!StringUtils.isNumeric(segs[1])) {
 				resourceName += StringKit.capitalize(segs[1]);
 			}
 			resourceName += StringKit.capitalize(segs[2]);
 		}
 		return resourceName;
 	}
 
 	private Resource getGetResource(String resourceName) {
 		return getResource("get", resourceName);
 	}
 
 	private Resource getUpdateResource(String resourceName) {
 		return getResource("update", resourceName);
 	}
 
 	private Resource getPostResource(String resourceName) {
 		return getResource("add", resourceName);
 	}
 
 	private Resource getDeleteResource(String resourceName) {
 		return getResource("delete", resourceName);
 	}
 
 	private Resource getResource(String prefix, String resourceName) {
 		Resource resource = null;
 		String beanName = prefix + StringKit.capitalize(resourceName);
 		Object bean = null;
 		try {
 			bean = this.applicationContext.getBean(beanName);
 		} catch (NoSuchBeanDefinitionException e) {
 			log.debug("no such bean %s", beanName);
 		} catch (Exception e) {
 			log.error("get bean error", e);
 		}
 		if (bean instanceof Resource) {
 			resource = (Resource) bean;
 			resource.setName(beanName);
 		}
 		return resource;
 	}
 
 }
