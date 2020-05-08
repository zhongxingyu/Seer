 package framework.action;
 
 import java.io.PrintWriter;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.ResourceBundle;
 import java.util.regex.Pattern;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import framework.config.Config;
 import framework.db.DB;
 
 /** 
  * 비지니스 로직을 처리하는 클래스가 상속받아야 할 추상클래스이다.
  * 뷰페이지(jsp 페이지)가 실행되기 전에 클라이언트에서 서버로 전송된 데이터를 편리하게 업무로직에 반영하기 
  * 위한 전처리(Pre-processing)모듈이다. 하나의 서비스에 대해 여러개의 업무로직을 컴포넌트 형태로 제작하여 등록할 수 있다. 
  * 작성된 Controller는 routes.properties에 등록한다.
  */
 public abstract class Controller {
 	private static final String _FLASH_SCOPE_OBJECT_KEY = "___FLASH_SCOPE_OBJECT___";
 	private Map<String, DB> _dbMap = new HashMap<String, DB>();
 
 	/**
 	 * 서블릿 컨텍스트 객체
 	 */
 	protected ServletContext application = null;
 
 	/**
 	 * HTTP 클라이언트 요청객체
 	 */
 	protected HttpServletRequest request = null;
 
 	/**
 	 * 요청파라미터의 값을 담는 해시맵
 	 */
 	protected Params params = null;
 
 	/**
 	 * 쿠키값을 담는 해시맵
 	 */
 	protected Params cookies = null;
 
 	/**
 	 * 헤더값을 담는 해시맵
 	 */
 	protected Params headers = null;
 
 	/**
 	 * 클라이언트의 세션 객체
 	 */
 	protected HttpSession session = null;
 
 	/**
 	 * 플래시 요청파라미터의 값을 담는 해시맵
 	 */
 	protected Map<String, Object> flash = null;
 
 	/**
 	 * HTTP 클라이언트 응답객체
 	 */
 	protected HttpServletResponse response = null;
 
 	/**
 	 * 응답객체의 PrintWriter 객체
 	 */
 	protected PrintWriter out = null;
 
 	/**
 	 * 액션 메소드
 	 */
 	protected Method action = null;
 
 	/**
 	 * 액션 이름(패키지 + 클래스 + 메소드명)
 	 */
 	protected String actionName = null;
 
 	/**
 	 * Controller의 로거객체
 	 */
 	protected static final Log logger = LogFactory.getLog(framework.action.Controller.class);
 
 	/** 
 	 * 클라이언트에서 서비스를 호출할 때 요청 url에 설정된 값을 참고하여 해당 메소드를 실행한다.
 	 * 정의되지 않은 메소드를 호출할 경우 로그에 오류메시지가 기록되며 메소드 실행을 마친 후 데이터베이스 컨넥을 자동으로 닫아준다.
 	 * @param servlet 서블릿 객체
 	 * @param request 클라이언트에서 요청된 Request객체
 	 * @param response 클라이언트로 응답할 Response객체
 	 * @param method 메소드
 	 * @throws Throwable 
 	 */
 	public void execute(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response, Method method) throws Throwable {
 		try {
 			this.application = servlet.getServletContext();
 			this.request = request;
 			this.params = Params.getParams(request);
 			this.cookies = Params.getParamsFromCookie(request);
 			this.headers = Params.getParamsFromHeader(request);
 			this.session = request.getSession();
 			this.flash = new HashMap<String, Object>();
 			this.response = response;
 			this.out = response.getWriter();
 			this.action = method;
 			this.actionName = getClass().getName() + "." + method.getName();
 			long currTime = 0;
 			_flashRestore();
 			_before();
 			if (logger.isDebugEnabled()) {
 				currTime = System.currentTimeMillis();
 				logger.debug("Start Class : " + getClass().getName() + ", Method : " + method.getName());
 				logger.debug(this.params.toString());
 				logger.debug(this.cookies.toString());
 			}
 			try {
 				method.invoke(this, (Object[]) null);
 			} catch (InvocationTargetException e) {
 				_catch(e.getCause());
 			}
 			if (logger.isDebugEnabled()) {
 				logger.debug("End | duration : " + (System.currentTimeMillis() - currTime) + " msec");
 			}
 			_after();
 		} catch (_ActionStopException e) {
 			logger.error("", e);
 		} finally {
 			try {
 				_finally();
 			} catch (Throwable te) {
 				logger.error("", te);
 			}
 			_flashSave();
 			_destroy();
 		}
 	}
 
 	/**
 	 * 액션진행을 중단한다.
 	 */
 	protected void stop() {
 		throw new _ActionStopException();
 	}
 
 	/**
 	 * 요청을 JSP페이지로 포워드(Forward) 한다.
 	 * 작성된 JSP페이지는 routes.properties에 등록한다.
 	 * <br>
 	 * ex) 키가 search-jsp 인 JSP페이지로 포워딩 할 경우 : render("search-jsp")
 	 * @param key routes.properties 파일에 등록된 JSP 페이지의 키
 	 */
 	protected void render(String key) {
 		try {
 			ResourceBundle bundle = (ResourceBundle) application.getAttribute("routes-mapping");
 			String url = ((String) bundle.getObject(key)).trim();
 			if (logger.isDebugEnabled()) {
 				logger.debug("☆☆☆ " + request.getRemoteAddr() + " 로 부터 \"" + request.getMethod() + " " + request.getRequestURI() + "\" 요청이 \"" + url + "\" 로 forward 되었습니다");
 			}
 			application.getRequestDispatcher(response.encodeURL(url)).forward(request, response);
 		} catch (Throwable e) {
 			logger.error("", e);
 		}
 	}
 
 	/** 
 	 * 요청을 JSP페이지로 재지향(Redirect) 한다.
 	 * <br>
 	 * ex) search.jsp 인 JSP페이지로 재지향 할 경우 : redirect("/serach.jsp")
 	 * @param url 재지향(Redirect)할 url 주소
 	 */
 	protected void redirect(String url) {
 		try {
 			String encodedUrl = null;
 			if (Pattern.matches("^/.+", url)) {
 				encodedUrl = request.getContextPath() + response.encodeRedirectURL(url);
 				encodedUrl = encodedUrl.replaceAll("/+", "/");
 			} else {
 				encodedUrl = response.encodeRedirectURL(url);
 			}
 			if (logger.isDebugEnabled()) {
 				logger.debug("☆☆☆ " + request.getRemoteAddr() + " 로 부터 \"" + request.getMethod() + " " + request.getRequestURI() + "\" 요청이 \"" + encodedUrl + "\" 로 redirect 되었습니다");
 			}
 			response.sendRedirect(encodedUrl);
 		} catch (Throwable e) {
 			logger.error("", e);
 		}
 	}
 
 	/** 
 	 * 데이타베이스 객체를 리턴한다.
 	 * <br>
 	 * config.properties에 datasource가 등록되어 있으면 JNDI에 등록되어있는 데이타소스에서 컨넥션을 생성한다.
 	 * datasource가 등록되어 있지 않는 경우 연결정보를 바탕으로 jdbc 컨넥션을 생성한다.
 	 * 업무명이 default에 해당하는 설정파일 정보를 이용하여 컨넥션을 생성한다.
 	 * 생성된 컨넥션의 autoCommit 속성은 false 로 셋팅된다.
 	 * @return DB 객체
 	 */
 	protected DB getDB() {
 		return getDB("default");
 	}
 
 	/** 
 	 * 데이타베이스 객체를 리턴한다.
 	 * <br>
 	 * config.properties에 datasource가 등록되어 있으면 JNDI에 등록되어있는 데이타소스에서 컨넥션을 생성한다.
 	 * datasource가 등록되어 있지 않는 경우 연결정보를 바탕으로 jdbc 컨넥션을 생성한다.
 	 * 파라미터로 넘겨진 업무명에 해당하는 설정파일 정보를 이용하여 컨넥션을 생성한다.
 	 * 생성된 컨넥션의 autoCommit 속성은 false 로 셋팅된다.
 	 * @param serviceName 서비스명(업무명)
 	 * @return DB 객체
 	 */
 	protected DB getDB(String serviceName) {
 		if (!_dbMap.containsKey(serviceName)) {
 			String dsName = null;
 			String jdbcDriver = null;
 			String jdbcUrl = null;
 			String jdbcUid = null;
 			String jdbcPw = null;
 			try {
 				dsName = getConfig().getString("jdbc." + serviceName + ".datasource");
 			} catch (Throwable e) {
 				// 설정파일에 데이타소스가 정의되어있지 않으면 실행
 				jdbcDriver = getConfig().getString("jdbc." + serviceName + ".driver");
 				jdbcUrl = getConfig().getString("jdbc." + serviceName + ".url");
 				jdbcUid = getConfig().getString("jdbc." + serviceName + ".uid");
 				jdbcPw = getConfig().getString("jdbc." + serviceName + ".pwd");
 			}
 			try {
 				DB db = new DB(dsName, this);
 				if (dsName != null) {
 					db.connect();
 				} else {
 					db.connect(jdbcDriver, jdbcUrl, jdbcUid, jdbcPw);
 				}
 				_dbMap.put(serviceName, db);
 				db.setAutoCommit(false);
 			} catch (Throwable e) {
 				logger.error("", e);
 			}
 		}
 		return _dbMap.get(serviceName);
 	}
 
 	/** 
 	 * 설정정보를 가지고 있는 객체를 생성하여 리턴한다.
 	 * @return config.properties의 설정정보를 가지고 있는 객체
 	 */
 	protected Config getConfig() {
 		return Config.getInstance();
 	}
 
 	/** 
 	 * 세션객체에서 해당 키에 해당하는 오브젝트를 리턴한다.
 	 * <br>
 	 * ex) 세션에서 result라는 키로 오브젝트를 리턴받는 경우 : Object obj = getSessionAttribute("result")
 	 * @param key 세션객체의 조회키
 	 * @return 세션객체에서 얻어온 오브젝트
 	 */
 	protected Object getSessionAttribute(String key) {
 		return session.getAttribute(key);
 	}
 
 	/**
 	 * 응답객체를 클라이언트에게 전송하기 전에 컨텐츠타입을 설정한다. 
 	 * <br>
 	 * ex1) xml파일을 전송 하는 경우 : setContentType("text/xml; charset=utf-8")
 	 * <br>
 	 * ex2) 텍스트 파일을 전송하는 경우 : setContentType("text/plain; charset=euc-kr")
 	 * @param contentType 응답객체에 설정할 컨텐츠 타입
 	 */
 	protected void setContentType(String contentType) {
 		response.setContentType(contentType);
 	}
 
 	/** 
 	 * 요청객체에 키,값 속성을 설정한다.
 	 * Controller에서 처리한 결과를 뷰 로 넘길때 요청객체에 속성을 설정하여 라우팅한다.
 	 * <br>
 	 * ex) rs라는 RecordSet 객체를 result 라는 키로 요청객체에 설정하는 경우 : setAttribute("result", re) 
 	 * @param key 속성의 키 문자열
 	 * @param value 속성의 값 객체
 	 */
 	protected void setAttribute(String key, Object value) {
 		request.setAttribute(key, value);
 	}
 
 	/** 
 	 * 세션객체에 키,값 속성을 설정한다.
 	 * Controller에서 처리한 결과를 세션에 저장한다.
 	 * <br>
 	 * ex) userinfo 라는 사용자정보객체를 userinfo 라는 키로 세션객체에 설정하는 경우 : setSessionAttribute("userinfo", userinfo)
 	 * @param key 속성의 키 문자열
 	 * @param value 속성의 값 객체
 	 */
 	protected void setSessionAttribute(String key, Object value) {
 		session.setAttribute(key, value);
 	}
 
 	/** 
 	 * 플래시객체에 키,값 속성을 설정한다.
 	 * Controller에서 처리한 결과를 다음 요청의 요청객체에 저장한다.
 	 * <br>
 	 * ex) message 라는 객체를 message 라는 키로 플래시객체에 설정하는 경우 : setFlashAttribute("message", message)
 	 * @param key 속성의 키 문자열
 	 * @param value 속성의 값 객체
 	 */
 	protected void setFlashAttribute(String key, Object value) {
 		flash.put(key, value);
 	}
 
 	//////////////////////////////////////////////////////////////////////////////////////////Private 메소드
 
 	/*
 	 * 액션진행을 중단하기 위해 내부적으로 사용하는 예외
 	 */
 	private class _ActionStopException extends RuntimeException {
 		private static final long serialVersionUID = -4449840322691459821L;
 	}
 
 	/*
 	 * 플래시객체를 세션에 저장
 	 */
 	private void _flashSave() {
 		session.setAttribute(_FLASH_SCOPE_OBJECT_KEY, flash);
 	}
 
 	/*
 	 * 세션에서 플래시객체를 복원
 	 */
 	@SuppressWarnings("unchecked")
 	private void _flashRestore() {
 		Map<String, Object> flashMap = (Map<String, Object>) session.getAttribute(_FLASH_SCOPE_OBJECT_KEY);
 		if (flashMap != null) {
 			for (Entry<String, Object> entry : flashMap.entrySet()) {
 				request.setAttribute(entry.getKey(), entry.getValue());
 			}
 			session.removeAttribute(_FLASH_SCOPE_OBJECT_KEY);
 		}
 	}
 
 	/*
 	 * Play framework 참고
 	 */
 	private void _before() throws Throwable {
 		List<Method> beforeMethods = _getAnnotationMethods(Before.class);
 		Collections.sort(beforeMethods, new Comparator<Method>() {
 			public int compare(Method m1, Method m2) {
 				Before before1 = m1.getAnnotation(Before.class);
 				Before before2 = m2.getAnnotation(Before.class);
 				return before1.priority() - before2.priority();
 			}
 		});
 		for (Method beforeMethod : beforeMethods) {
 			String[] only = beforeMethod.getAnnotation(Before.class).only();
 			String[] unless = beforeMethod.getAnnotation(Before.class).unless();
 			boolean skip = false;
 			for (String o : only) {
 				if (!o.contains(".")) {
 					o = getClass().getName() + "." + o;
 				}
 				if (o.equals(this.actionName)) {
 					skip = false;
 					break;
 				} else {
 					skip = true;
 				}
 			}
 			for (String u : unless) {
 				if (!u.contains(".")) {
 					u = getClass().getName() + "." + u;
 				}
 				if (u.equals(this.actionName)) {
 					skip = true;
 					break;
 				}
 			}
 			if (!skip) {
 				if (logger.isDebugEnabled()) {
 					logger.debug("@Before Class : " + beforeMethod.getDeclaringClass().getName() + ", Method : " + beforeMethod.getName());
 				}
 				beforeMethod.setAccessible(true);
 				try {
 					beforeMethod.invoke(this, (Object[]) null);
 				} catch (InvocationTargetException e) {
 					throw e.getCause();
 				}
 			}
 		}
 	}
 
 	/*
 	 * Play framework 참고
 	 */
 	private void _after() throws Throwable {
 		List<Method> afterMethods = _getAnnotationMethods(After.class);
 		Collections.sort(afterMethods, new Comparator<Method>() {
 			public int compare(Method m1, Method m2) {
 				After after1 = m1.getAnnotation(After.class);
 				After after2 = m2.getAnnotation(After.class);
 				return after1.priority() - after2.priority();
 			}
 		});
 		for (Method afterMethod : afterMethods) {
 			String[] only = afterMethod.getAnnotation(After.class).only();
 			String[] unless = afterMethod.getAnnotation(After.class).unless();
 			boolean skip = false;
 			for (String o : only) {
 				if (!o.contains(".")) {
 					o = getClass().getName() + "." + o;
 				}
 				if (o.equals(this.actionName)) {
 					skip = false;
 					break;
 				} else {
 					skip = true;
 				}
 			}
 			for (String u : unless) {
 				if (!u.contains(".")) {
 					u = getClass().getName() + "." + u;
 				}
 				if (u.equals(this.actionName)) {
 					skip = true;
 					break;
 				}
 			}
 			if (!skip) {
 				if (logger.isDebugEnabled()) {
 					logger.debug("@After Class : " + afterMethod.getDeclaringClass().getName() + ", Method : " + afterMethod.getName());
 				}
 				afterMethod.setAccessible(true);
 				try {
 					afterMethod.invoke(this, (Object[]) null);
 				} catch (InvocationTargetException e) {
 					throw e.getCause();
 				}
 			}
 		}
 	}
 
 	/*
 	 * Play framework 참고
 	 */
 	private void _catch(Throwable e) throws Throwable {
 		List<Method> catchMethods = _getAnnotationMethods(Catch.class);
 		Collections.sort(catchMethods, new Comparator<Method>() {
 			public int compare(Method m1, Method m2) {
 				Catch catch1 = m1.getAnnotation(Catch.class);
 				Catch catch2 = m2.getAnnotation(Catch.class);
 				return catch1.priority() - catch2.priority();
 			}
 		});
 		for (Method catchMethod : catchMethods) {
 			Class<?>[] exceptionClasses = catchMethod.getAnnotation(Catch.class).value();
 			if (exceptionClasses.length == 0) {
 				exceptionClasses = new Class<?>[] { Exception.class };
 			}
 			for (Class<?> exceptionClass : exceptionClasses) {
 				if (exceptionClass.isInstance(e)) {
 					if (logger.isDebugEnabled()) {
 						logger.debug("@Catch Class : " + catchMethod.getDeclaringClass().getName() + ", Method : " + catchMethod.getName());
 					}
 					catchMethod.setAccessible(true);
 					try {
 						catchMethod.invoke(this, e);
 					} catch (InvocationTargetException ie) {
 						throw ie.getCause();
 					}
 					break;
 				}
 			}
 		}
 	}
 
 	/*
 	 * Play framework 참고
 	 */
 	private void _finally() throws Throwable {
 		List<Method> finallyMethods = _getAnnotationMethods(Finally.class);
 		Collections.sort(finallyMethods, new Comparator<Method>() {
 			public int compare(Method m1, Method m2) {
 				Finally finally1 = m1.getAnnotation(Finally.class);
 				Finally finally2 = m2.getAnnotation(Finally.class);
 				return finally1.priority() - finally2.priority();
 			}
 		});
 		for (Method finallyMethod : finallyMethods) {
 			String[] only = finallyMethod.getAnnotation(Finally.class).only();
 			String[] unless = finallyMethod.getAnnotation(Finally.class).unless();
 			boolean skip = false;
 			for (String o : only) {
 				if (!o.contains(".")) {
 					o = getClass().getName() + "." + o;
 				}
 				if (o.equals(this.actionName)) {
 					skip = false;
 					break;
 				} else {
 					skip = true;
 				}
 			}
 			for (String u : unless) {
 				if (!u.contains(".")) {
 					u = getClass().getName() + "." + u;
 				}
 				if (u.equals(this.actionName)) {
 					skip = true;
 					break;
 				}
 			}
 			if (!skip) {
 				if (logger.isDebugEnabled()) {
 					logger.debug("@Finally Class : " + finallyMethod.getDeclaringClass().getName() + ", Method : " + finallyMethod.getName());
 				}
 				finallyMethod.setAccessible(true);
 				try {
 					finallyMethod.invoke(this, (Object[]) null);
 				} catch (InvocationTargetException e) {
 					throw e.getCause();
 				}
 			}
 		}
 	}
 
 	private List<Method> _getAnnotationMethods(Class<? extends Annotation> annotation) {
 		List<Method> methods = new ArrayList<Method>();
 		for (Method method : getClass().getMethods()) {
 			if (method.isAnnotationPresent(annotation)) {
 				methods.add(method);
 			}
 		}
 		return methods;
 	}
 
 	/*
 	 * DB 컨넥션 정리
 	 */
 	private void _destroy() {
 		try {
 			DB db = null;
 			for (String key : _dbMap.keySet()) {
 				db = _dbMap.get(key);
 				if (db != null) {
 					db.release();
 					db = null;
 				}
 			}
 			_dbMap.clear();
 			params = null;
 			out = null;
 		} catch (Throwable e) {
 			logger.error("", e);
 		}
 	}
 }
