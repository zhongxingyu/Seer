 package berlin.reiche.virginia;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.Map;
 import java.util.Properties;
 import java.util.TreeMap;
 import java.util.Map.Entry;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import berlin.reiche.virginia.model.User;
 
 import freemarker.template.Configuration;
 import freemarker.template.DefaultObjectWrapper;
 import freemarker.template.Template;
 import freemarker.template.TemplateException;
 
 /**
  * The main servlet of the application which handles all incoming HTTP requests
  * which are not dedicated to a special servlet.
  * 
  * @author Konrad Reiche
  * 
  */
 @SuppressWarnings("serial")
 public class AppServlet extends HttpServlet {
 
 	/**
 	 * Paths to the web resources.
 	 */
 	static final String WEB_PATH = "site/";
 	static final String ERROR_SITE = "ftl/error.ftl";
 	static final String NOT_FOUND_SITE = "ftl/404.ftl";
 
 	private static final String LOGIN_SITE = "ftl/login.ftl";
 	private static final String MAIN_SITE = "ftl/main.ftl";
 	private static final String DEFAULT_VALUES_PATH = "site/resources/default-values.properties";
 
 	/**
 	 * Further constants which appear more than one in the source code.
 	 */
 	static final String LOGIN_ATTRIBUTE = "login.isLoggedIn";
 	static final String DESTINATION_ATTRIBUTE = "login.destination";
 	static final String REQUEST_HEADLINE_VAR = "requestHeadline";
 
 	/**
 	 * Regular expression for matching a MongoDB object ids.
 	 */
 	static final String ID_REGEX = "[a-f0-9]*";
 
 	/**
 	 * Configuration used for the Freemarker template processing.
 	 */
 	static Configuration configuration;
 
 	private final static AppServlet instance = new AppServlet();
 
 	/**
 	 * The configuration for the template engine Freemarker is set up with
 	 * default settings.
 	 */
 	static {
 
 		try {
 			configuration = new Configuration();
 			configuration.setDirectoryForTemplateLoading(new File(WEB_PATH));
 			configuration.setObjectWrapper(new DefaultObjectWrapper());
 		} catch (IOException e) {
 			System.err.println("The path " + WEB_PATH
 					+ " could not be retrieved.");
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * The constructor is private in order to enforce the singleton pattern.
 	 */
 	private AppServlet() {
 
 	}
 
 	/**
 	 * Parses the HTTP request and writes the response by using the template
 	 * engine.
 	 */
 	@Override
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 
 		String path = request.getPathInfo();
 		Map<String, Object> data = getDefaultData();
 		Writer writer = response.getWriter();
 
 		if (path == null) {
 			throw new IOException("The associated path information is null");
 		}
 
 		HttpSession session = request.getSession();
 		User user = (User) session.getAttribute(LOGIN_ATTRIBUTE);
 		if (user == null && path != null && !path.equals("/login")) {
 			response.sendRedirect("/login");
 			return;
 		}
 
 		if (path.equals("/")) {
 			processTemplate(MAIN_SITE, data, writer);
 		} else if (path.equals("/login")) {
 
 			if (user == null) {
 				processTemplate(LOGIN_SITE, data, writer);
 			} else {
 				response.sendRedirect("/");
 			}
 		} else if (path.equals("/logout")) {
 			session.removeAttribute(LOGIN_ATTRIBUTE);
 			response.sendRedirect("/login");
 		} else {
 			processTemplate(NOT_FOUND_SITE, data, writer);
 		}
 
 	}
 
 	/**
 	 * Parses all user HTML form requests and handles them.
 	 */
 	@Override
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 
 		String path = request.getServletPath() + request.getPathInfo();
 
 		if ("/login".equals(path)) {
 			handleLoginRequest(request, response);
 		}
 	}
 
 	/**
 	 * Handles a user submitted login request.
 	 * 
 	 * @param request
 	 *            provides request information for HTTP servlets.
 	 * @param response
 	 *            provides HTTP-specific functionality in sending a response.
 	 * @throws IOException
 	 *             if an input or output exception occurs.
 	 */
 	private void handleLoginRequest(HttpServletRequest request,
 			HttpServletResponse response) throws IOException {
 
 		Map<String, Object> data = getDefaultData();
 		String login = request.getParameter("name");
 		String password = request.getParameter("password");
 
 		User user = MongoDB.get(User.class, login);
 		if (user != null && user.checkPassword(password)) {
 			HttpSession session = request.getSession();
 			String destination = (String) session
 					.getAttribute(DESTINATION_ATTRIBUTE);
 			session.setAttribute(LOGIN_ATTRIBUTE, user);
 			session.removeAttribute(DESTINATION_ATTRIBUTE);
 			response.sendRedirect(destination);
 			return;
 		} else {
 			data.put("hasLoginFailed", "true");
 			AppServlet.processTemplate(LOGIN_SITE, data, response.getWriter());
 		}
 	}
 
 	/**
 	 * Processed the given template file with the provided data. The result is
 	 * written immediately to the given writer.
 	 * 
 	 * @param relativePath
 	 *            the relative path from the base directory pointing to the
 	 *            template file.
 	 * @param data
 	 *            the root node of the data model.
 	 * @param writer
 	 *            the writer to which the processed template is written.
 	 * @throws IOException
 	 *             if an I/O exception occurs due to writing or flushing.
 	 */
 	public static void processTemplate(String relativePath,
 			Map<String, ?> data, Writer writer) throws IOException {
 
 		try {
 			Template template = AppServlet.configuration
 					.getTemplate(relativePath);
 			template.process(data, writer);
 			writer.flush();
 		} catch (TemplateException e) {
 			System.err.println("The template" + relativePath
 					+ "could not be processed properly.");
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Generates a data model out of the default value property file.
 	 * 
 	 * @return the data model.
 	 * @throws IOException
 	 *             if the file with the default value properties could not been
 	 *             found or an error occurred during reading from it.
 	 * 
 	 */
 	static Map<String, Object> getDefaultData() throws IOException {
 
 		Map<String, Object> defaultData = new TreeMap<>();
 		Properties defaultValues = new Properties();
 		FileInputStream input = new FileInputStream(DEFAULT_VALUES_PATH);
 		defaultValues.load(input);
 		for (Entry<Object, Object> entry : defaultValues.entrySet()) {
 			defaultData.put((String) entry.getKey(), (String) entry.getValue());
 		}
 
 		return defaultData;
 	}
 
 	/**
 	 * @return a singleton instance of {@link RoomServlet}.
 	 */
 	public static AppServlet getInstance() {
 		return instance;
 	}
 
 	/**
 	 * @param request
 	 *            provides request information for HTTP servlets.
 	 * 
 	 * @param response
 	 *            provides HTTP-specific functionality in sending a response.
 	 * @param destination
 	 *            the destination to which the user ought to be redirected after
 	 *            login.
 	 * @throws IOException
 	 *             if an input or output exception occurs.
 	 */
 	static void checkAccessRights(HttpServletRequest request,
 			HttpServletResponse response, String destination) throws IOException {
 
 		HttpSession session = request.getSession();
 		User user = (User) session.getAttribute(LOGIN_ATTRIBUTE);
 		if (user == null) {
 			session.setAttribute(DESTINATION_ATTRIBUTE, destination);
 			response.sendRedirect("/login");
 			return;
 		}
 	}
 }
