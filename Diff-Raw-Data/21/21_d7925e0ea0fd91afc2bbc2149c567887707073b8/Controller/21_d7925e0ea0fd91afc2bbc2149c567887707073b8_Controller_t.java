 package juva;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import juva.Exceptions.AuthenticateFailedException;
 import juva.database.Model;
 import juva.rbac.PermissionTable;
 import juva.rbac.Resource;
 import juva.rbac.Role;
 import juva.rbac.Roles;
 import juva.rbac.PermissionTable.METHODS;
 
 
 public class Controller extends HttpServlet {
 
 	protected ArrayList _urlPatterns = new ArrayList();
 
 	protected HttpServletRequest request;
 	protected HttpServletResponse response;
 	protected PrintWriter out;
 	protected ServletOutputStream outputStream;
 	protected ServletContext context;
 	protected String rootPath;
 	protected HttpSession session;
 	protected Map<String, Object> variables = new HashMap<String, Object>();
 	protected juva.rbac.User currentUser;
 
 	protected PermissionTable permissionTable; 
 	
 	public Controller() throws Throwable {
 		super();
 		String thisName = this.getClass().getName();
 		permissionTable = new PermissionTable(new Resource(thisName));
 		initPermission();
 	}
 	
 	public Controller(String urlPattern) throws Throwable {
 		this();
 		this.addUrlPattern(urlPattern);
 		variables.clear();
 	}
 	
 	public Controller(String[] urlPatterns) throws Throwable {
 		this();
 		for(int i=0;i<urlPatterns.length; ++i){
 			this.addUrlPattern(urlPatterns[i]);
 		}
 		variables.clear();
 	}
 
 	protected void initPermission() throws Throwable{
 		this.permissionTable.allow(Roles.Everyone, METHODS.GET);
 	}
 	
 	public void before() throws Throwable{}
 
 	public void destroy() {
 		super.destroy();
 	}
 
 	public void setContext(ServletContext context){
 		this.context = context;
 	}
 	
 	public void setPath(String path){
 		this.rootPath = path;
 	}
 	
 	public void setSession(HttpSession session){
 		this.session = session;
 	}
 	
 	public ArrayList<String> getUrlPatterns(){
 		return this._urlPatterns;
 	}
 	
 	public void addUrlPattern(String urlPattern){
 		this._urlPatterns.add(urlPattern);
 	}
 	
 	public void render(String path) throws IOException, SQLException{
 		Template template = new Template(rootPath + "/templates/" + path);
 		Set<String> keys = variables.keySet();
 		Iterator iterator = keys.iterator();
 		while (iterator.hasNext()){
 			String key = (String) iterator.next();
 			Object value = variables.get(key);
 			template.putVariables(key, value);
 		}
 		String html = template.render();
 		out.print(html);
 	}
 
 	public void get() throws Throwable{
 		this.response.sendError(405, "Method not allow");
 	}
 
 	public void post() throws Throwable{
 		this.response.sendError(405, "Method not allow");
 	}
 
 	public void put() throws Throwable{
 		this.response.sendError(405, "Method not allow");
 	}
 	
 	public void delete() throws Throwable{
 		this.response.sendError(405, "Method not allow");
 	}
 
 	public void doGet(HttpServletRequest request,
 			           HttpServletResponse response)
 			throws ServletException, IOException {
 		try {
 			initActinon(request, response);
 			this.before();
			this.authenticate(PermissionTable.METHODS.GET);
 			this.get();
 		}
 		catch (AuthenticateFailedException e) {
 			response.sendError(405, "Method Not Allow");
 		}
 		catch (Throwable e) {
 			// TODO log this message.
 			response.sendError(500);
 			e.printStackTrace();
 		}
 		
 	}
 
 	public void doPost(HttpServletRequest request,
 			            HttpServletResponse response)
 			throws ServletException, IOException {
 		try {
 			initActinon(request, response);
 			this.before();
			this.authenticate(PermissionTable.METHODS.POST);
 			this.post();
 		}
 		catch (AuthenticateFailedException e) {
 			response.sendError(405, "Method Not Allow");
 		} catch (Throwable e) {
 			// TODO log this message.
 			response.sendError(500);
 			e.printStackTrace();
 		}
 	}
 
 	public void doPut(HttpServletRequest request,
 			           HttpServletResponse response)
 			throws ServletException, IOException {
 		try {
 			initActinon(request, response);
 			this.before();
			this.authenticate(PermissionTable.METHODS.PUT);
 			this.put();
 		}
 		catch (AuthenticateFailedException e) {
 			response.sendError(405, "Method Not Allow");
 		} catch (Throwable e) {
 			// TODO log this message.
 			response.sendError(500);
 			e.printStackTrace();
 		}
 	}
 
 	public void doDelete(HttpServletRequest request,
 			              HttpServletResponse response)
 			throws ServletException, IOException {
 		try {
 			initActinon(request, response);
 			this.before();
			this.authenticate(PermissionTable.METHODS.DELETE);
 			this.delete();
 		}
 		catch (AuthenticateFailedException e) {
 			response.sendError(405, "Method Not Allow");
 		} catch (Throwable e) {
 			// TODO log this message.
 			response.sendError(500);
 			e.printStackTrace();
 		}
 	}
 
 	public void initActinon(HttpServletRequest request,
 			                 HttpServletResponse response)
             throws Throwable{
 		this.request = request;
 		this.response = response;
 		this.response.setContentType("text/html;charset=utf-8");
 		this.setContext(context);
 		this.out = this.response.getWriter();
 		Utils.Json.registerPrinter(out);
 	}
 
 	public void authenticate(PermissionTable.METHODS method)
 	        throws IOException, AuthenticateFailedException{
 		Role currentRole;
 		if (this.currentUser == null){
 			currentRole = Roles.Everyone;
 		}else{
 			currentRole = this.currentUser.getRole();
 		}
 		boolean allow = this.permissionTable.accessiable(currentRole, method);
 		if (!allow){
 			throw new AuthenticateFailedException();
 		}
 	}
 	
 	public Object getCookies(String cookiesName){
 		return getCookies(cookiesName, this.request);
 	}
 
 	public static Object getCookies(String cookiesName,
 			                          HttpServletRequest request){
 		Object object = null;
 		Cookie[] cookies = request.getCookies();
 	    if (cookies != null){
 	        for(int i=0;i <cookies.length; ++i){
 	            if (cookies[i].getName().equals(cookiesName)){
 	            	object = cookies[i].getValue();
 	            }
 	        }
 	    }
 	    return object;
 	}
 	
 	protected void putVar(String key, Object value){
		if (value instanceof Integer ||
            value instanceof Double ||
            value instanceof Float){
			value = value + "";
		}
 		variables.put(key, value);
 	}
 	
 	protected void putModel(String key, Model model){
 		variables.put(key, model);
 	}
 	
 	protected void putTrueVar(String key){
 		variables.put(key, "True");
 	}
 	
 	protected void putFalseVar(String key){
 		variables.put(key, "False");
 	}
 }
