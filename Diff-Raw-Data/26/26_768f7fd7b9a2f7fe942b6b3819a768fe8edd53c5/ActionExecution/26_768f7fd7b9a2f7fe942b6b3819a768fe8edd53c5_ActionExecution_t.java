 package org.eweb4j.mvc.action;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.net.URLEncoder;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Properties;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.HttpMethod;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.QueryParam;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileUploadBase.InvalidContentTypeException;
 import org.apache.commons.fileupload.ProgressListener;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.VelocityEngine;
 import org.eweb4j.cache.ActionConfigBeanCache;
 import org.eweb4j.cache.SingleBeanCache;
 import org.eweb4j.config.ConfigConstant;
 import org.eweb4j.config.Log;
 import org.eweb4j.config.LogFactory;
 import org.eweb4j.config.bean.ConfigBean;
 import org.eweb4j.config.bean.UploadConfigBean;
 import org.eweb4j.ioc.IOC;
 import org.eweb4j.mvc.ActionMethod;
 import org.eweb4j.mvc.Context;
 import org.eweb4j.mvc.MIMEType;
 import org.eweb4j.mvc.ParamUtil;
 import org.eweb4j.mvc.action.annotation.Ioc;
 import org.eweb4j.mvc.action.annotation.Singleton;
 import org.eweb4j.mvc.action.annotation.Transactional;
 import org.eweb4j.mvc.config.ActionClassCache;
 import org.eweb4j.mvc.config.MVCConfigConstant;
 import org.eweb4j.mvc.config.bean.ActionConfigBean;
 import org.eweb4j.mvc.config.bean.ResultConfigBean;
 import org.eweb4j.mvc.interceptor.After;
 import org.eweb4j.mvc.interceptor.Before;
 import org.eweb4j.mvc.interceptor.InterExecution;
 import org.eweb4j.mvc.upload.UploadFile;
 import org.eweb4j.mvc.validator.ValidateExecution;
 import org.eweb4j.mvc.validator.annotation.DateFormat;
 import org.eweb4j.mvc.validator.annotation.Upload;
 import org.eweb4j.orm.dao.DAO;
 import org.eweb4j.orm.dao.DAOFactory;
 import org.eweb4j.orm.dao.DAOImpl;
 import org.eweb4j.orm.dao.cascade.CascadeDAO;
 import org.eweb4j.orm.dao.delete.DeleteDAO;
 import org.eweb4j.orm.dao.insert.InsertDAO;
 import org.eweb4j.orm.dao.select.DivPageDAO;
 import org.eweb4j.orm.dao.select.SearchDAO;
 import org.eweb4j.orm.dao.select.SelectDAO;
 import org.eweb4j.orm.dao.update.UpdateDAO;
 import org.eweb4j.orm.jdbc.transaction.Trans;
 import org.eweb4j.orm.jdbc.transaction.Transaction;
 import org.eweb4j.util.ClassUtil;
 import org.eweb4j.util.CommonUtil;
 import org.eweb4j.util.FileUtil;
 import org.eweb4j.util.JsonConverter;
 import org.eweb4j.util.ReflectUtil;
 import org.eweb4j.util.xml.BeanXMLUtil;
 import org.eweb4j.util.xml.XMLWriter;
 
 import freemarker.template.Configuration;
 import freemarker.template.DefaultObjectWrapper;
 import freemarker.template.Template;
 
 /**
  * Action 执行器
  * 
  * @author weiwei
  * @since 1.b.8
  * 
  */
 public class ActionExecution {
 
 	private static Log log = LogFactory.getMVCLogger(ActionExecution.class);
 
 	// 要执行的Action方法
 	private Method method;
 	private Field[] fields;
 	
 	private Context context;
 
 	private Object actionObject;
 	private Object retn;
 
 	private ReflectUtil ru;
 
 	// 验证器
 	private void handleValidator() throws Exception {
 
 		this.context.setValidation(ValidateExecution.checkValidate(context.getActionConfigBean().getValidator(), context.getQueryParamMap(), context.getRequest()));
 	}
 
 	public ActionExecution(String uri, String httpMethod, Context context) {
 		this.context = context;
 		this.context.setUri(uri);
 		this.context.setHttpMethod(httpMethod);
 		this.context.setQueryParamMap(new HashMap<String, String[]>());
 		this.context.setPathParamMap(new HashMap<String, String[]>());
 	}
 
 	public boolean findAction() throws Exception {
 		// URL参数
 		Map<String, List<?>> pathParams = null;
 		if (ActionConfigBeanCache.containsKey(this.context.getUri())|| (pathParams = ActionConfigBeanCache.getByMatches(this.context.getUri(), this.context.getHttpMethod())) != null) {
 
 			// 处理形如" /xxx/{id}/{name} "的URI
 			if (pathParams != null && pathParams.containsKey("mvcBean")) {
 				// 根据Url配置的UrlParam获取参数值
 				this.context.setActionConfigBean((ActionConfigBean) pathParams.get("mvcBean").get(0));
 
 				this.context.getPathParamMap().putAll(ParamUtil.getPathParamMap(pathParams));
 				this.context.getQueryParamMap().putAll(this.context.getPathParamMap());
 			} else
 				this.context.setActionConfigBean(ActionConfigBeanCache.get(this.context.getUri()));
 
 			// 将request的请求参数转到另外一个map中去
 			this.context.getQueryParamMap().putAll(ParamUtil.copyReqParams(this.context.getRequest()));
 
 			if (this.context.getActionConfigBean() != null)
 				return true;
 		}
 
 		return false;
 	}
 
 	private Object initPojo() throws Exception {
 		Class<?> clazz = ActionClassCache.get(this.context.getActionConfigBean().getClazz());
 		Annotation singletonAnn = clazz.getAnnotation(Singleton.class);
 		if (singletonAnn != null) {
 			this.actionObject = SingleBeanCache.get(clazz.getName());
 			if (this.actionObject == null) {
 				this.actionObject = clazz.newInstance();
 				SingleBeanCache.add(clazz.getName(), this.actionObject);
 			}
 		} else
 			this.actionObject = clazz.newInstance();
 
 		ru = new ReflectUtil(this.actionObject);
 
 		return this.actionObject;
 	}
 
 	// IOC，注入对象到pojo
 	private void injectIocBean() throws Exception {
 		fields = ru.getFields();
 		if (fields == null)
 			return;
 
 		for (Field f : fields) {
 			Class<?> type = f.getType();
 
 			Ioc ioc = f.getAnnotation(Ioc.class);
 			if (ioc == null)
 				continue;
 			String beanId = "";
 			if (ioc.value().trim().length() == 0)
 				beanId = type.getSimpleName();
 			else
 				beanId = CommonUtil.parsePropValue(ioc.value());
 
 			Method setter = ru.getSetter(f.getName());
 			if (setter == null)
 				continue;
 
 			setter.invoke(this.actionObject, IOC.getBean(beanId));
 		}
 	}
 
 	private void exeActionLog() {
 		StringBuilder sb = new StringBuilder();
 		sb.append("execute ").append(this.context.getUri()).append(ActionMethod.CON)
 			.append(this.context.getHttpMethod()).append("[Action]; ");
 
 		log.debug(sb.toString());
 	}
 
 	private Object[] assemParams(Class<?>[] paramTypes, Annotation[][] paramAnns)throws Exception {
 		Object[] params = new Object[paramTypes.length];
 		int pathParamIndex = 0;
 		for (int i = 0; i < paramTypes.length; ++i) {
 			Annotation[] anns = paramAnns[i];
 			Class<?> paramClass = paramTypes[i];
 			String[] paramValue = null;
 			// ------------------------------------------------------
 			// 通过给定class 获取对应的ActionContextObj
 			if (Context.class.isAssignableFrom(paramClass)) {
 				params[i] = this.context;
 				continue;
 			}
 
 			if (HttpServletRequest.class.isAssignableFrom(paramClass)) {
 				params[i] = this.context.getRequest();
 				continue;
 			}
 
 			if (HttpServletResponse.class.isAssignableFrom(paramClass)) {
 				params[i] = this.context.getResponse();
 				continue;
 			}
 
 			if (PrintWriter.class.isAssignableFrom(paramClass)) {
 				params[i] = this.context.getWriter();
 				continue;
 			}
 
 			if (ServletOutputStream.class.isAssignableFrom(paramClass)) {
 				params[i] = this.context.getOut();
 				continue;
 			}
 
 			if (HttpSession.class.isAssignableFrom(paramClass)) {
 				params[i] = this.context.getSession();
 				continue;
 			}
 
 			if (ActionProp.class.isAssignableFrom(paramClass)) {
 				if (this.context.getActionProp() == null)
 					this.context.setActionProp(new ActionProp(this.actionObject.getClass().getName()));
 
 				params[i] = this.context.getActionProp();
 				continue;
 			}
 
 			if (Validation.class.isAssignableFrom(paramClass)) {
 				params[i] = this.context.getValidation();
 				continue;
 			}
 
 			if (QueryParams.class.isAssignableFrom(paramClass)) {
 				params[i] = this.context.getQueryParams();
 				continue;
 			}
 
 			if (DAO.class.isAssignableFrom(paramClass)) {
 				params[i] = new DAOImpl("");
 				continue;
 			}
 
 			if (InsertDAO.class.isAssignableFrom(paramClass)) {
 				params[i] = DAOFactory.getInsertDAO();
 				continue;
 			}
 
 			if (DeleteDAO.class.isAssignableFrom(paramClass)) {
 				params[i] = DAOFactory.getDeleteDAO();
 				continue;
 			}
 
 			if (UpdateDAO.class.isAssignableFrom(paramClass)) {
 				params[i] = DAOFactory.getUpdateDAO();
 				continue;
 			}
 
 			if (SelectDAO.class.isAssignableFrom(paramClass)) {
 				params[i] = DAOFactory.getSelectDAO();
 				continue;
 			}
 
 			if (DivPageDAO.class.isAssignableFrom(paramClass)) {
 				params[i] = DAOFactory.getDivPageDAO();
 				continue;
 			}
 
 			if (SearchDAO.class.isAssignableFrom(paramClass)) {
 				params[i] = DAOFactory.getSearchDAO();
 				continue;
 			}
 
 			if (CascadeDAO.class.isAssignableFrom(paramClass)) {
 				params[i] = DAOFactory.getCascadeDAO();
 				continue;
 			}
 
 			PathParam pathParamAnn = this.getPathParamAnn(anns);
             if (pathParamAnn != null) {
                 paramValue = this.getPathParamValue(pathParamAnn.value());
                 params[i] = ClassUtil.getParamVal(paramClass, paramValue[0]);
                 continue;
             }
 
 			QueryParam queryParamAnn = this.getQueryParamAnn(anns);
 
             // 视图模型
             if (queryParamAnn == null && Map.class.isAssignableFrom(paramClass)) {
                     params[i] = this.context.getModel();
                     continue;
             }
 
             if (queryParamAnn != null) {
             	final String fieldName = queryParamAnn.value();
 				if (File.class.isAssignableFrom(paramClass)) {
 					if (!this.context.getUploadMap().containsKey(fieldName))
 						continue;
 					List<UploadFile> list = this.context.getUploadMap().get(fieldName);
 					if (list == null || list.isEmpty())
 						continue;
 					
 					UploadFile uploadFile = list.get(0);
 					File file = uploadFile.getTmpFile();
 					params[i] = file;
 					
 					continue;
 				}
 				
 				if (File[].class.isAssignableFrom(paramClass)){
 					if (!this.context.getUploadMap().containsKey(fieldName))
 						continue;
 					List<UploadFile> list = this.context.getUploadMap().get(fieldName);
 					if (list == null || list.isEmpty())
 						continue;
 					File[] files = new File[list.size()];
 					for (int j = 0; j < files.length; j++)
 						files[j] = list.get(j).getTmpFile();
 					
 					params[i] = files;
 				}
 				
 				if (UploadFile.class.isAssignableFrom(paramClass)) {
 					if (!this.context.getUploadMap().containsKey(fieldName))
 						continue;
 					List<UploadFile> list = this.context.getUploadMap().get(fieldName);
 					if (list == null || list.isEmpty())
 						continue;
 					
 					UploadFile uploadFile = list.get(0);
 					params[i] = uploadFile;
 					
 					continue;
 				}
 				
 				if (UploadFile[].class.isAssignableFrom(paramClass)){
 					if (!this.context.getUploadMap().containsKey(fieldName))
 						continue;
 					List<UploadFile> list = this.context.getUploadMap().get(fieldName);
 					if (list == null || list.isEmpty())
 						continue;
 					
 					params[i] = list.toArray(new UploadFile[]{});
 				}
 				
 				String defaultValue = null;
 				DefaultValue defaultValueAnn = this.getDefaultValueAnn(anns);
 				if (defaultValueAnn != null)defaultValue = defaultValueAnn.value();
 	
 				paramValue = this.getQueryParamValue(fieldName, defaultValue);
 	
 				if (java.util.Date.class.isAssignableFrom(paramClass)) {
 					params[i] = this.getDateParam(anns, paramValue[0]);
 					continue;
 				}
 	
 				String startName = fieldName;
 				if (ClassUtil.isPojo(paramClass)) {
 					params[i] = this.injectParam2Pojo(paramClass, startName);
 					continue;
 				}
 	
 				if (Map.class.isAssignableFrom(paramClass)) {
 					params[i] = this.injectParam2Map(startName);
 					continue;
 				}
 	
 				if (paramClass.isArray())
 					params[i] = ClassUtil.getParamVals(paramClass, paramValue);
 				else
 					params[i] = ClassUtil.getParamVal(paramClass, paramValue[0]);
             } else {
 				// 如果是基本数据类型，则按照排序进行注入
             	String[] pathParams = this.context.getActionConfigBean().getPathParams();
             	if (pathParams == null){
             		log.warn("QueryParam not found and PathParam not found too");
             		continue;
             	}
             	
 				paramValue = this.getPathParamValue(pathParams[pathParamIndex]);
 				params[i] = ClassUtil.getParamVal(paramClass, paramValue[0]);
 				pathParamIndex++;
 				continue;
 			}
 		}
 
 		return params;
 	}
 
 	private Method getFirstMethd(Method[] methods) {
 		Method m = methods[0];
 		if (methods.length == 1)
 			return m;
 
 		// 如果含有两个或以上同名的方法,优先拿到被@Path注解的第一个方法
 		for (Method mm : methods) {
 			Path p = mm.getAnnotation(Path.class);
 			if (p == null)
 				continue;
 
 			m = mm;
 			break;
 		}
 
 		return m;
 	}
 
 	private String[] getQueryParamValue(String paramName, String defaultValue) {
 		String[] paramValue = this.context.getQueryParamMap().get(paramName);
 
 		return getParamValue(paramValue, defaultValue);
 	}
 
 	private String[] getPathParamValue(String pathParamName) {
 		String[] paramValue = this.context.getPathParamMap().get(pathParamName);
 
 		return getParamValue(paramValue, null);
 	}
 
 	private String[] getParamValue(String[] paramValue, String defaultValue) {
 		if (paramValue == null || paramValue.length == 0 || paramValue[0] == null) {
 			if (defaultValue != null)
 				paramValue = new String[] { defaultValue };
 			else
 				paramValue = new String[] { "" };
 		}
 
 		return paramValue;
 	}
 
 	private Date getDateParam(Annotation[] anns, String paramValue)
 			throws Exception {
 		DateFormat dateAnn = null;
 		for (Annotation a : anns) {
 			if (a == null)
 				continue;
 
 			if (!a.annotationType().isAssignableFrom(DateFormat.class))
 				continue;
 
 			dateAnn = (DateFormat) a;
 			break;
 		}
 
 		String pattern = "yyyy-MM-dd HH:mm:ss";
 		if (dateAnn != null)
 			pattern = dateAnn.value();
 
 		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
 		return sdf.parse(paramValue);
 	}
 
 	private Object injectParam2Pojo(Class<?> paramClass, String startName)
 			throws Exception {
 		Object paramObj = paramClass.newInstance();
 		ReflectUtil ru = new ReflectUtil(paramObj);
 		this.injectActionCxt2Pojo(ru);
 		// 注入mvc action 请求参数
 		ParamUtil.injectParam(this.context, ru, startName);
 
 		return paramObj;
 	}
 
 	private Object injectParam2Map(String startName) throws Exception {
 		Map<String, Object> map = new HashMap<String, Object>();
 		ReflectUtil ru = new ReflectUtil(map);
 		this.injectActionCxt2Pojo(ru);
 		// 注入mvc action 请求参数
 		ParamUtil.injectParam(this.context, ru, startName);
 
 		return map;
 	}
 
 	private PathParam getPathParamAnn(Annotation[] anns) {
 		for (Annotation a : anns) {
 			if (a == null)
 				continue;
 
 			if (!a.annotationType().isAssignableFrom(PathParam.class))
 				continue;
 
 			return (PathParam) a;
 		}
 
 		return null;
 	}
 
 	private QueryParam getQueryParamAnn(Annotation[] anns) {
 		for (Annotation a : anns) {
 			if (a == null)
 				continue;
 
 			if (!a.annotationType().isAssignableFrom(QueryParam.class))
 				continue;
 
 			return (QueryParam) a;
 		}
 
 		return null;
 	}
 	
 	private DefaultValue getDefaultValueAnn(Annotation[] anns) {
 		for (Annotation a : anns) {
 			if (a == null)
 				continue;
 
 			if (!a.annotationType().isAssignableFrom(DefaultValue.class))
 				continue;
 
 			return (DefaultValue) a;
 		}
 
 		return null;
 	}
 
 	private void handleDownload(File file) throws Exception{
 		if (file.exists()){
 			HttpServletResponse response = this.context.getResponse();
 			String filename = URLEncoder.encode(file.getName(), "utf-8");
             response.reset();
             response.setContentType("application/x-msdownload");
             response.addHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
             int fileLength = (int) file.length();
             response.setContentLength(fileLength);
             /*如果文件长度大于0*/
             if (fileLength != 0) {
                 /*创建输入流*/
                 InputStream inStream = new FileInputStream(file);
                 byte[] buf = new byte[4096];
                 /*创建输出流*/
                 ServletOutputStream servletOS = response.getOutputStream();
                 int readLength;
                 while (((readLength = inStream.read(buf)) != -1)) {
                     servletOS.write(buf, 0, readLength);
                 }
                 inStream.close();
                 servletOS.flush();
                 servletOS.close();
             }
 		}
 	}
 	
 	private void handleResult() throws Exception {
 
 		this.exeActionLog();
 
 		if (retn == null)
 			return;
 		String baseUrl = (String) this.context.getServletContext().getAttribute(MVCConfigConstant.BASE_URL_KEY);
 		if (File.class.isAssignableFrom(retn.getClass())){
 			File file = (File)retn;
 			this.handleDownload(file);
 			return ;
 		}else if(File[].class.isAssignableFrom(retn.getClass())){
 			File[] files = (File[])retn;
 			
 			String fileName = CommonUtil.getNowTime("yyyyMMddHHmmss")+ "_" + "download.zip";
 			
 			HttpServletResponse resp = this.context.getResponse();
 			resp.reset();
 			resp.setContentType("application/zip");  
             resp.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
 	          
 	        ServletOutputStream outputStream = resp.getOutputStream();  
 	        ZipOutputStream zip = new ZipOutputStream(outputStream);  
 	       
 	        for (File file :files){
 	        	byte[] b = new byte[1024];  
 	 	        int len;  
 	        	zip.putNextEntry(new ZipEntry(file.getName()));  
 	        	FileInputStream fis = new FileInputStream(file);  
 	 	        while ((len = fis.read(b)) != -1) {  
 	 	            zip.write(b, 0, len);  
 	 	        }  
 	 	        
 	 	        fis.close();
 	        }
 	          
 	        zip.flush();  
 	        zip.close();  
 	        outputStream.flush();  
 			
 			return;
 		}
 		
 		if (!String.class.isAssignableFrom(retn.getClass())) {
 			String mimeType = this.context.getRequest().getParameter(MVCConfigConstant.HTTP_HEADER_ACCEPT_PARAM);
 			if (mimeType == null){
 				String contentType = this.context.getRequest().getContentType();
 				if (contentType != null){
 					this.context.getResponse().setContentType(contentType);
 					mimeType = contentType.split(";")[0];
 				}
 			}
 
 			if (mimeType == null)
 				mimeType = MIMEType.JSON;
 
 			if (this.context.getWriter() == null)
 				this.context.setWriter(this.context.getResponse().getWriter());
 			
 			if (MIMEType.JSON.equals(mimeType) || "json".equalsIgnoreCase(mimeType)) {
 				this.context.getResponse().setContentType(MIMEType.JSON);
 				this.context.getWriter().print(JsonConverter.convert(retn));
 			} else if (MIMEType.XML.equals(mimeType) || "xml".equalsIgnoreCase(mimeType)) {
 				Class<?> cls = retn.getClass();
 				if (Collection.class.isAssignableFrom(cls)) {
 					Class<?> _cls = ClassUtil.getPojoClass(this.method);
 					if (_cls != null)
 						cls = _cls;
 				}
 
 				XMLWriter writer = BeanXMLUtil.getBeanXMLWriter(retn);
 				writer.setCheckStatck(true);
 				writer.setSubNameAuto(true);
 				writer.setClass(cls);
 				writer.setRootElementName(null);
 				this.context.getResponse().setContentType(MIMEType.XML);
 				this.context.getWriter().print(writer.toXml());
 			} else {
 				this.context.getWriter().print("暂时不支持JSON 、XML以外的表述形式");
 			}
 			
 			this.context.getWriter().flush();
 
 			return;
 		}
 
 		List<String> produces = this.context.getActionConfigBean().getProduces();
 		if (produces != null && produces.size() > 0)
 			for (String produce : produces) {
                 this.context.getResponse().setContentType(produce);
 				break;
 			}
 
 		String re = String.valueOf(retn);
 
 		for (Field f : fields) {
 			Method getter = ru.getGetter(f.getName());
 			if (getter == null)
 				continue;
 
 			String name = f.getName();
 			if (this.context.getModel().containsKey(name))
 				continue;
 			
 			this.context.getModel().put(name, getter.invoke(actionObject));
 		}
 
 		this.context.getModel().put(MVCConfigConstant.BASE_URL_KEY, baseUrl);
 
 		// 客户端重定向
 		if (re.startsWith(RenderType.REDIRECT + ":")) {
 			String url = re.substring((RenderType.REDIRECT + ":").length());
 			String location = url;
 
 			this.context.getResponse().sendRedirect(CommonUtil.replaceChinese2Utf8(location));
 
 			return;
 		} else if (re.startsWith(RenderType.ACTION + ":")) {
 			String path = re.substring((RenderType.ACTION + ":").length());
 			// ACTION 重定向
 			handleActionRedirect(context, path, baseUrl);
 
 			return;
 		} else if (re.startsWith(RenderType.OUT + ":")) {
 			String location = re.substring((RenderType.OUT + ":").length());
 			this.context.getWriter().print(location);
 			this.context.getWriter().flush();
 
 			return;
 		} else if (re.startsWith(RenderType.FORWARD + ":")) {
 			String location = re.substring((RenderType.FORWARD + ":").length());
 			HttpServletRequest request = this.context.getRequest();
 			request.setAttribute(MVCConfigConstant.REQ_PARAM_MAP_NAME, this.context.getQueryParamMap());
 
 			for (Iterator<Entry<String, Object>> it = this.context.getModel().entrySet().iterator(); it.hasNext(); ) {
 				Entry<String, Object> entry = it.next();
 				request.setAttribute(entry.getKey(), entry.getValue());
 			}
 
 			// 服务端跳转
 			request.getRequestDispatcher(MVCConfigConstant.FORWARD_BASE_PATH + "/"+ location).forward(request, this.context.getResponse());
 
 			return;
 		} else if (re.startsWith(RenderType.FREEMARKER + ":")) {
 			String location = re.substring((RenderType.FREEMARKER + ":").length());
 			// FreeMarker 渲染
 			Configuration cfg = new Configuration();
 			// 指定模板从何处加载的数据源，这里设置成一个文件目录。
 			cfg.setDirectoryForTemplateLoading(new File(ConfigConstant.ROOT_PATH + MVCConfigConstant.FORWARD_BASE_PATH));
 			// 指定模板如何检索数据模型
 			cfg.setObjectWrapper(new DefaultObjectWrapper());
 			cfg.setDefaultEncoding("utf-8");
 
 			Template template = cfg.getTemplate(location);
 			template.setEncoding("utf-8");
 
 			template.process(this.context.getModel(), this.context.getWriter());
 
 			return;
 		}else if (re.startsWith(RenderType.VELOCITY + ":")) {
 			String location = re.substring((RenderType.VELOCITY + ":").length());
 			File viewsDir = new File(ConfigConstant.ROOT_PATH + MVCConfigConstant.FORWARD_BASE_PATH);
 			 // 初始化Velocity模板引擎
 	        Properties p = new Properties();
 	        p.setProperty("resource.loader", "file");
 	        p.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
 	        p.setProperty("file.resource.loader.path", viewsDir.getAbsolutePath());
 	        p.setProperty("file.resource.loader.cache", "true");
 	        p.setProperty("file.resource.loader.modificationCheckInterval", "2");
 	        p.setProperty("input.encoding", "utf-8");
 	        p.setProperty("output.encoding", "utf-8");
 	        VelocityEngine ve = new VelocityEngine(p);
 	        // Velocity获取模板文件，得到模板引用
 	        org.apache.velocity.Template t = ve.getTemplate(location);
 			VelocityContext velocityCtx = new VelocityContext();
 			for (Iterator<Entry<String, Object>> it = this.context.getModel().entrySet().iterator(); it.hasNext(); ){
 				Entry<String, Object> e = it.next();
 				velocityCtx.put(e.getKey(), e.getValue());
 			}
 			
 			// 将环境变量和输出部分结合
 	        t.merge(velocityCtx, this.context.getWriter());
 	        this.context.getWriter().flush();
 			return;
 		} else {
 			List<ResultConfigBean> results = this.context.getActionConfigBean().getResult();
 
 			if (results == null || results.size() == 0) {
 				this.context.getWriter().print(retn);
 				this.context.getWriter().flush();
 				return;
 			}
 
 			boolean isOut = true;
 			for (ResultConfigBean r : results) {
 				if (!"_props_".equals(r.getName()) && !r.getName().equals(re)
 						&& !"".equals(re)){
 					continue;
 				}
 				
 				isOut = false;
 				String type = r.getType();
 				String location = r.getLocation();
 				if (RenderType.REDIRECT.equalsIgnoreCase(type)) {
 					this.context.getResponse().sendRedirect(CommonUtil.replaceChinese2Utf8(location));
 					
 					return ;
 				} else if (RenderType.FORWARD.equalsIgnoreCase(type)) {
 					HttpServletRequest request = this.context.getRequest();
 					request.setAttribute(MVCConfigConstant.REQ_PARAM_MAP_NAME, this.context.getQueryParamMap());
 
 					fields = ru.getFields();
 					if (fields == null)
 						return;
 
 					for (Iterator<Entry<String, Object>> it = this.context.getModel().entrySet()
 							.iterator(); it.hasNext();) {
 						Entry<String, Object> entry = it.next();
 						request.setAttribute(entry.getKey(), entry.getValue());
 					}
 					// 服务端跳转
 					request.getRequestDispatcher(MVCConfigConstant.FORWARD_BASE_PATH + location).forward(request, this.context.getResponse());
 					
 					return ;
 				} else if (RenderType.FREEMARKER.equalsIgnoreCase(type)) {
 					// FreeMarker 渲染
 					Configuration cfg = new Configuration();
 					// 指定模板从何处加载的数据源，这里设置成一个文件目录。
 					cfg.setDirectoryForTemplateLoading(new File(ConfigConstant.ROOT_PATH + MVCConfigConstant.FORWARD_BASE_PATH));
 					// 指定模板如何检索数据模型
 					cfg.setObjectWrapper(new DefaultObjectWrapper());
 					cfg.setDefaultEncoding("utf-8");
 
 					Template template = cfg.getTemplate(location);
 					template.setEncoding("utf-8");
 
 					template.process(this.context.getModel(), this.context.getWriter());
 					
 					return ;
 				} else if (RenderType.ACTION.equalsIgnoreCase(type)) {
 					// ACTION 重定向
 					handleActionRedirect(context, location, baseUrl);
 
 					return ;
 				} else if (RenderType.OUT.equalsIgnoreCase(type)
 						|| location.trim().length() == 0) {
 					this.context.getWriter().print(location);
 					this.context.getWriter().flush();
 					
 					return ;
 				} 
 			}
 			
 			if (isOut){
 				this.context.getWriter().print(retn);
 				this.context.getWriter().flush();
 			}
 		}
 
 	}
 
 	public static void handleActionRedirect(Context context, String path, String baseUrl)
 			throws IOException {
 		String method;
 		String location;
 		if (!path.contains("@")) {
 			method = HttpMethod.GET;
 			location = path;
 		} else {
 			int lastIndex = path.indexOf("@") + 1;
 			method = path.substring(lastIndex);
 			location = path.substring(0, lastIndex - 1);
 		}
 		StringBuilder sb = new StringBuilder("");
 		String param = null;
 		if (path.contains("?")) {
 			int lastIndex2 = path.indexOf("?") + 1;
 			param = path.substring(lastIndex2);
 			String[] params = param.split("&");
 			for (String para : params) {
 				String[] p = para.split("=");
 				sb.append(String.format("<input name='%s' value='%s' />", p[0], p[1]));
 			}
 
 			method = method.substring(0, method.indexOf("?"));
 		}
 
 		if (HttpMethod.GET.equalsIgnoreCase(method)) {
 			String pa = param == null ? "" : "?" + CommonUtil.replaceChinese2Utf8(param);
 			context.getResponse().sendRedirect(baseUrl + location + pa);
 			return;
 		}
 
 		String _method = "";
 		if (HttpMethod.PUT.equalsIgnoreCase(method) || HttpMethod.DELETE.equalsIgnoreCase(method)) {
 			_method = new String(method);
 			method = HttpMethod.POST;
 		}
 
 		String action = baseUrl + location;
 		// 构造一个Form表单模拟客户端发送新的Action请求
 		String format = "<form id='ACTION_REDIRECT_FORM' action='%s' method='%s' ><input name='_method' value='%s' />%s<input type='submit' /></form>";
 		String form = String.format(format, action, method, _method, sb.toString());
 		String js = "<script>document.getElementById('ACTION_REDIRECT_FORM').submit();</script>";
 		context.getWriter().print(form + js);
 	}
 
 	private void handleUpload() throws Exception{
 		ConfigBean cb = (ConfigBean) SingleBeanCache.get(ConfigBean.class.getName());
 		
 		Upload upload = method.getAnnotation(Upload.class);
 		UploadConfigBean ucb = cb.getMvc().getUpload();
 		String tmpDir = ucb.getTmp();
 		int memoryMax = CommonUtil.strToInt(CommonUtil.parseFileSize(ucb.getMaxMemorySize())+"");
 		long sizeMax = CommonUtil.parseFileSize(ucb.getMaxRequestSize());
 		String[] suffixArray = null;
 		if (upload != null){
 			if (upload.tmpDir().trim().length() > 0)
 				tmpDir = upload.tmpDir();
 			
 			if (upload.maxMemorySize().trim().length() > 0)
 				memoryMax =  CommonUtil.strToInt(CommonUtil.parseFileSize(upload.maxMemorySize())+"");
 			
 			if (upload.maxRequestSize().trim().length() > 0)
 				sizeMax = CommonUtil.parseFileSize(upload.maxRequestSize());
 			
 			if (upload.suffix().length > 0)
 				suffixArray = upload.suffix();
 		}
 		
 		if (tmpDir.trim().length() == 0)
 			tmpDir = "${RootPath}"+File.separator+"WEB-INF"+File.separator+"tmp";
 		
 		tmpDir = tmpDir.replace("${RootPath}", ConfigConstant.ROOT_PATH);
 		
 		DiskFileItemFactory factory = new DiskFileItemFactory();
 		factory.setSizeThreshold(memoryMax);
 		factory.setRepository(new File(tmpDir));
 		
 		ServletFileUpload _upload = new ServletFileUpload(factory);
 		if (!_upload.isMultipartContent(this.context.getRequest()))
 			return ;
 		
 		_upload.setSizeMax(sizeMax);
 		
 		if (upload != null){
 			Class<?> clazz = upload.listener();
			if (!clazz.getName().equals(ProgressListener.class.getName()) && ProgressListener.class.isAssignableFrom(clazz))
 				_upload.setProgressListener((ProgressListener) clazz.newInstance());
 		}
 		
 		try{
 			List<FileItem> items = _upload.parseRequest(this.context.getRequest());
 			
 			Iterator<FileItem> it = items.iterator();
 			while (it.hasNext()){
 				FileItem item = it.next();
 				String fieldName = item.getFieldName();
 				if (item.isFormField()){
 					String value = item.getString();
 					this.context.getQueryParamMap().put(fieldName, new String[]{value});
 				} else {
 					String fileName = item.getName();
 					if (fileName == null || fileName.trim().length() == 0)
 						continue;
 					boolean isOk = false;
 					for (String suffix : suffixArray){
 						if (fileName.endsWith("."+suffix)){
 							isOk = true;
 							break;
 						}
 					}
 					
 					if (!isOk){
 						String err = "your upload file "+fileName+" type invalid ! only allow " + Arrays.asList(suffixArray);
 						Map<String,String> errMap = new HashMap<String,String>();
 						errMap.put(fieldName, err);
 						this.context.getValidation().getErrors().put("upload", errMap);
 						return ;
 					}
 					
 					item.getSize();
 					item.getContentType();
 					String stamp = CommonUtil.getNowTime("yyyyMMddHHmmss");
 					File tmpFile = new File(tmpDir + File.separator + stamp + "_" + fileName);
 					item.write(tmpFile);
 					
 					UploadFile uploadFile = new UploadFile(tmpFile, fileName, fieldName, item.getSize(), item.getContentType());
 					
 					if (this.context.getUploadMap().containsKey(fieldName)){
 						this.context.getUploadMap().get(fieldName).add(uploadFile);
 					}else{
 						List<UploadFile> uploads = new ArrayList<UploadFile>();
 						uploads.add(uploadFile);
 						this.context.getUploadMap().put(fieldName, uploads);
 					}
 				}
 			}
 		}catch(InvalidContentTypeException e){
 			throw new Exception("upload file error", e);
 		} 
 	}
 	
 	/**
 	 * 执行Action
 	 * 
 	 * @param methodName
 	 * @throws Exception
 	 */
 	private void excuteMethod(String methodName) throws Exception{
 
 		/* 对于外部配置的前置拦截器，方法体内的前置拦截器较后执行  */
 		Before before = method.getAnnotation(Before.class);
 		if (before != null){
 			InterExecution before_interExe = new InterExecution("before", context);
 			before_interExe.execute(before.value());
 			if (before_interExe.getError() != null){
 				before_interExe.showErr();
 				return ;
 			}
 		}
 		
 		boolean isTrans = false;
 		if (method.getAnnotation(Transactional.class) != null)
 			isTrans = true;
 		else if (actionObject.getClass().getAnnotation(Transactional.class) != null)
 			isTrans = true;
 
 		Class<?>[] paramTypes = method.getParameterTypes();
 
 		// 无参数运行方法
 		if (paramTypes == null || paramTypes.length == 0) {
 			// 无参数运行方法
 			if (isTrans)
 				Transaction.execute(new Trans() {
 					@Override
 					public void run(Object... args) throws Exception {
 						retn = method.invoke(actionObject);
 					}
 				}, "");
 			else
 				retn = method.invoke(actionObject);
 		} else {
 			
 			Annotation[][] paramAnns = method.getParameterAnnotations();
 			// 拿到方法所需要的参数
 			final Object[] params = assemParams(paramTypes, paramAnns);
 
 			// 带参数运行方法
 			if (isTrans)
 				Transaction.execute(new Trans() {
 					@Override
 					public void run(Object... args) throws Exception {
 						retn = method.invoke(actionObject, params);
 					}
 				}, "");
 			else
 				retn = method.invoke(actionObject, params);
 		}
 	}
 
 	private void injectActionCxt2Pojo(ReflectUtil ru) throws Exception {
 		HttpServletRequest req = this.context.getRequest();
 		HttpServletResponse res = this.context.getResponse();
 		PrintWriter out = this.context.getWriter();
 		HttpSession session = this.context.getSession();
 		ActionProp actionProp = this.context.getActionProp();
 		QueryParams queryParams = this.context.getQueryParams();
 		for (String n : ru.getFieldsName()) {
 			Method m = ru.getSetter(n);
 			if (m == null)
 				continue;
 
 			Class<?> clazz = m.getParameterTypes()[0];
 			if (Context.class.isAssignableFrom(clazz)) {
 				m.invoke(ru.getObject(), this.context);
 			} else if (HttpServletRequest.class.isAssignableFrom(clazz)) {
 				m.invoke(ru.getObject(), req);
 			} else if (HttpServletResponse.class.isAssignableFrom(clazz)) {
 				m.invoke(ru.getObject(), res);
 			} else if (PrintWriter.class.isAssignableFrom(clazz)) {
 				m.invoke(ru.getObject(), out);
 			} else if (ServletOutputStream.class.isAssignableFrom(clazz)) {
 				m.invoke(ru.getObject(), this.context.getOut());
 			} else if (HttpSession.class.isAssignableFrom(clazz)) {
 				m.invoke(ru.getObject(), session);
 			} else if (ActionProp.class.isAssignableFrom(clazz)) {
 				if (actionProp == null)
 					actionProp = new ActionProp(clazz.getName());
 
 				this.context.setActionProp(actionProp);
 				m.invoke(ru.getObject(), actionProp);
 			} else if (QueryParams.class.isAssignableFrom(clazz)) {
 				m.invoke(ru.getObject(), queryParams);
 			} else if (Validation.class.isAssignableFrom(clazz)) {
 				m.invoke(ru.getObject(), this.context.getValidation());
 			} else {
 				/* 如果找不到注入的类型，则尝试从IOC容器获取 */
 				Object obj = IOC.getBean(n);
 				if (obj != null)
 					m.invoke(ru.getObject(), obj);
 			}
 		}
 	}
 
 	/**
 	 * 执行Action
 	 * 
 	 * @throws Exception
 	 */
 	public void execute() throws Exception {
 		// 实例化pojo
 		initPojo();
 
 		// IOC注入对象到pojo中
 		injectIocBean();
 
 		// 注入框架mvc action 上下文环境
 		this.injectActionCxt2Pojo(this.ru);
 		
 		if (IAction.class.isAssignableFrom(this.actionObject.getClass())) {
 			// struts2风格
 			IAction action = (IAction) actionObject;
 			action.init(this.context);
 			retn = action.execute();
 			// 对Action执行返回结果的处理
 			this.handleResult();
 			
 			return ;
 		}
 		
 		String methodName = this.context.getActionConfigBean().getMethod();
 		Method[] methods = ru.getMethods(methodName);
 		if (methods == null || methods.length == 0)
 			return;
 
 		method = this.getFirstMethd(methods);
 		if (method == null)
 			return;
 		
 		// 执行验证器
 		this.handleValidator();
 		try{
 			// upload files
 			this.handleUpload();
 			
 			// 注入mvc action 请求参数
 			ParamUtil.injectParam(this.context, this.ru, null);
 	
 			// execute the action method
 			excuteMethod(methodName);
 			
 			/* 方法体内的后置拦截器先执行  */
 			After after = method.getAnnotation(After.class);
 			if (after != null){
 				// 后置拦截器
 				InterExecution after_interExe = new InterExecution("after", context);
 				after_interExe.execute(after.value());
 				if (after_interExe.getError() != null){
 					after_interExe.showErr();
 					return ;
 				}
 			}
 			
 			/* 外部配置的后置拦截器后执行 */
 			InterExecution after_interExe = new InterExecution("after", this.context);// 7.后置拦截器
 			if (after_interExe.findAndExecuteInter()) {
 				after_interExe.showErr();
 				return;
 			}
 			
 			// 对Action执行返回结果的处理
 			this.handleResult();
 		}catch(Exception e){
 			throw e;
 		}finally{
 			// Action方法执行之后，清空临时文件
 			if (!this.context.getUploadMap().isEmpty())
 				for (Iterator<Entry<String, List<UploadFile>>> it = this.context.getUploadMap().entrySet().iterator(); it.hasNext(); ){
 					Entry<String, List<UploadFile>> en = it.next();
 					if (en.getValue() == null)
 						continue;
 					
 					for (UploadFile f : en.getValue()){
 						FileUtil.deleteFile(f.getTmpFile());
 				}
 			}
 		}
 		
 	}
 }
