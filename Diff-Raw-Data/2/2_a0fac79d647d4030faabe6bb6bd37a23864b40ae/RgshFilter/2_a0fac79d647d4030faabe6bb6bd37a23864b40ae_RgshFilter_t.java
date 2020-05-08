 package com.github.safrain.remotegsh.server;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.lang.reflect.UndeclaredThrowableException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.UUID;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.script.ScriptEngine;
 import javax.script.ScriptEngineManager;
 import javax.script.ScriptException;
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Remote Groovy Shell server side <br>
  * <h2>Supported entries</h2> Assume that requests with URI '/admin/rgsh' will
  * be handled with this filter:
  * <ul>
  * <li><b>GET /admin/rgsh</b> Show welcome screen</li>
  * <li><b>GET /admin/rgsh?r=install</b> Download shell client install script</li>
  * <li><b>GET /admin/rgsh?r=shell</b> Request to start a shell session,return a
  * <li><b>GET /admin/rgsh?r=rgsh</b> Download boostrap script</li>
  * <li><b>GET /admin/rgsh?r=jar</b> Download client jar file</li>
  * new sid to the client</li>
  * <li><b>POST /admin/rgsh</b> Run script in request body</li>
  * <li><b>POST /admin/rgsh?shell=[sid]</b> Execute shell command</li>
  * </ul>
  * <br>
  * <p/>
  * <h2>Init parameters:</h2> Init paramters configured in you web.xml
  * <ul>
  * <li><b>charset</b> Request & response charset</li>
  * <li><b>shellSessionTimeout</b> Shell sessions with idle time greater than
  * this will be dropped</li>
  * <li><b>initScriptPath</b> Init script classpath, some initialization work(put
  * some utility function in the script context or something more) will be done
  * in this script</li>
  * <li><b>initScriptCharset</b> Init script charset</li>
  * </ul>
  *
  * @author safrain
  */
 public class RgshFilter implements Filter {
     private static final Logger log = Logger.getLogger(RgshFilter.class.getName());
    private static final String RESOURCE_PATH = "com/github/safrain/remotegsh/server/";
     private static final String DEFAULT_CHARSET = "utf-8";
     private static final long SESSION_PURGE_INTERVAL = 1000 * 60 * 5L;// 5 min
     private static final String PLACEHOLDER_SERVER = "\\{\\{server\\}\\}";
     private static final String PLACEHOLDER_CHARSET = "\\{\\{charset\\}\\}";
     private static final String JAR_NAME = "rgsh.jar";
     /**
      * Request & response charset
      */
     private String charset = DEFAULT_CHARSET;
 
     /**
      * Shell sessions with idle time greater than this will be dropped
      */
     private long shellSessionTimeout = 1000 * 60 * 5L;// 5 min default
 
     /**
      * Init script classpath, some initialization work(put some utility function
      * in the script context or something more) will be done in this script
      */
     private String initScriptPath = RESOURCE_PATH + "init.groovy";
 
     /**
      * Init script charset
      */
     private String initScriptCharset = DEFAULT_CHARSET;
 
     /**
      * Shell sessions
      */
     private Map<String, ShellSession> shellSessions = new HashMap<String, ShellSession>();
 
     // ==========Filter implementation==========
     @Override
     public void init(FilterConfig filterConfig) throws ServletException {
         if (filterConfig.getInitParameter("charset") != null) {
             charset = filterConfig.getInitParameter("charset");
         }
         if (filterConfig.getInitParameter("shellSessionTimeout") != null) {
             shellSessionTimeout = Long.valueOf(filterConfig.getInitParameter("shellSessionTimeout"));
         }
         if (filterConfig.getInitParameter("initScriptPath") != null) {
             initScriptPath = filterConfig.getInitParameter("initScriptPath");
         }
         if (filterConfig.getInitParameter("initScriptCharset") != null) {
             initScriptCharset = filterConfig.getInitParameter("initScriptCharset");
         }
         // Setup a timer to purge timeout shell sessions
         Timer timer = new Timer("Remote Groovy Shell session purge daemon", true);
         timer.scheduleAtFixedRate(new TimerTask() {
             @Override
             public void run() {
                 purgeTimeOutSessions();
             }
         }, 0, SESSION_PURGE_INTERVAL);
     }
 
     @Override
     public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
         HttpServletRequest request = (HttpServletRequest) req;
         HttpServletResponse response = (HttpServletResponse) resp;
         req.setCharacterEncoding(charset);
         resp.setCharacterEncoding(charset);
 
         if ("GET".equals(request.getMethod())) {
             String res = request.getParameter("r");
             if (res != null && !res.isEmpty()) {
                 if ("install".equals(res)) {
                     performInstall(request, response);
                 } else if ("shell".equals(res)) {
                     performStartShell(request, response);
                 } else if ("jar".equals(res)) {
                     performJar(request, response);
                 } else if ("rgsh".equals(res)) {
                     performRgsh(request, response);
                 }
 
             } else {
                 performWelcomeScreen(request, response);
             }
         } else if ("POST".equals(request.getMethod())) {
             String sid = request.getParameter("sid");
             if (sid != null && !sid.isEmpty()) {
                 performShellExecute(request, response);
             } else {
                 performRunScript(request, response);
             }
         }
     }
 
     @Override
     public void destroy() {
     }
 
     // ==========Filter entries==========
     /*
      * Welcome Screen
 	 */
     private void performWelcomeScreen(HttpServletRequest request, HttpServletResponse response) throws IOException {
         response.getWriter().println(
                 getResource(RESOURCE_PATH + "welcome.txt", DEFAULT_CHARSET).replaceAll(PLACEHOLDER_SERVER,
                         request.getRequestURL().toString()));
         response.setStatus(200);
     }
 
     /*
      * Download client jar file
      */
     private void performJar(HttpServletRequest request, HttpServletResponse response) throws IOException {
         ServletOutputStream os = response.getOutputStream();
         os.write(toBytes(RgshFilter.class.getClassLoader().getResourceAsStream(RESOURCE_PATH + JAR_NAME)));
         response.setStatus(200);
     }
 
     /*
      * Download bootstrap script
      */
     private void performRgsh(HttpServletRequest request, HttpServletResponse response) throws IOException {
         response.getWriter().println(
                 getResource(RESOURCE_PATH + "rgsh.txt", DEFAULT_CHARSET).replaceAll(PLACEHOLDER_SERVER, request.getRequestURL().toString())
                         .replaceAll(PLACEHOLDER_CHARSET, charset));
         response.setStatus(200);
     }
 
     /*
      * Download install script
      */
     private void performInstall(HttpServletRequest request, HttpServletResponse response) throws IOException {
         response.getWriter().println(
                 getResource(RESOURCE_PATH + "install.txt", DEFAULT_CHARSET).replaceAll(PLACEHOLDER_SERVER,
                         request.getRequestURL().toString()).replaceAll(PLACEHOLDER_CHARSET, charset));
         response.setStatus(200);
     }
 
     /*
      * Start new shell session
      */
     private synchronized void performStartShell(HttpServletRequest request, HttpServletResponse response) throws IOException {
         PrintWriter writer = response.getWriter();
         ShellSession session = new ShellSession();
         session.setId(UUID.randomUUID().toString().replaceAll("-", ""));
         session.setLastAccessTime(System.currentTimeMillis());
         session.setEngine(createEngine(request, response));
         shellSessions.put(session.getId(), session);
         writer.print(session.getId());
         response.setStatus(200);
     }
 
     /*
      * Shell command execute
      */
     private void performShellExecute(HttpServletRequest request, HttpServletResponse response) throws IOException {
         ShellSession session = getSession(request.getParameter("sid"));
         if (session == null) {
             response.setStatus(410);// Http status GONE
             return;
         }
         ScriptEngine engine = session.getEngine();
         PrintWriter writer = response.getWriter();
         engine.getContext().setWriter(writer);
         engine.getContext().setErrorWriter(writer);
         String script = toString(request.getInputStream(), charset);
         try {
             Object result = engine.eval(script);
             response.setStatus(200);
             response.getWriter().print("@|bold ===>|@ " + String.valueOf(result));
         } catch (ScriptException e) {
             log.log(Level.SEVERE, "Error while running shell command:" + script, e);
             response.setStatus(500);
             e.getCause().printStackTrace(response.getWriter());
         }
     }
 
     /*
      * Run script
      */
     private void performRunScript(HttpServletRequest request, HttpServletResponse response) throws IOException {
         String script = toString(request.getInputStream(), charset);
         try {
             ScriptEngine engine = createEngine(request, response);
             engine.eval(script);
             response.setStatus(200);
             // Post and run won't return evaluate result to client
         } catch (ScriptException e) {
             log.log(Level.SEVERE, "Error while running script:" + script, e);
             response.setStatus(500);
             e.getCause().printStackTrace(response.getWriter());
         }
     }
 
     // ==========Engine creation==========
 
     /**
      * Create a script engine with stdout and stderr replaced by
      * {@link ServletResponse#getWriter()}. A default init script or a custom
      * init script(determine by {@link #initScriptPath} and
      * {@link #initScriptCharset}) is evaluate by this engine to do some
      * intialization work
      */
     private ScriptEngine createEngine(HttpServletRequest request, HttpServletResponse response) throws IOException {
         ScriptEngine engine = new ScriptEngineManager().getEngineByName("Groovy");
         if (engine == null) {
             log.log(Level.SEVERE, "Groovy engine not found.");
             throw new IllegalArgumentException("Groovy engine not found.");
         }
         PrintWriter writer = response.getWriter();
         engine.getContext().setWriter(writer);
         engine.getContext().setErrorWriter(writer);
         engine.put("_request", request);
         engine.put("_response", response);
         engine.put("_charset", charset);
 
         try {
             engine.eval(getResource(initScriptPath, initScriptCharset));
         } catch (Exception e) {
             log.log(Level.SEVERE, "Error while creating engine.", e);
             throw new UndeclaredThrowableException(e, "Error while creating engine.");
         }
         return engine;
     }
 
     // ==========Shell session management==========
 
     /**
      * Get shell session by sid,and purge timeout sessions
      */
     private ShellSession getSession(String sid) {
         purgeTimeOutSessions();
         ShellSession session = shellSessions.get(sid);
         if (session != null) {
             session.setLastAccessTime(System.currentTimeMillis());
         }
         return session;
     }
 
     /**
      * Remove all timeout sessions
      */
     private synchronized void purgeTimeOutSessions() {
         long now = System.currentTimeMillis();
         for (Iterator<Entry<String, ShellSession>> iterator = shellSessions.entrySet().iterator(); iterator.hasNext(); ) {
             Entry<String, ShellSession> entry = iterator.next();
             if (now - entry.getValue().getLastAccessTime() > shellSessionTimeout) {
                 iterator.remove();
             }
         }
     }
 
     // ==========Some utilities==========
 
     /**
      * Get classpath resource as string
      */
     public static String getResource(String path, String charset) throws IOException {
         return toString(RgshFilter.class.getClassLoader().getResourceAsStream(path), charset);
     }
 
     /**
      * Input stream to byte array
      */
     public static byte[] toBytes(InputStream inputStream) throws IOException {
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         byte[] buffer = new byte[4096];
         int readed;
         while ((readed = inputStream.read(buffer)) != -1) {
             bos.write(buffer, 0, readed);
         }
         return bos.toByteArray();
     }
 
     /**
      * Input stream to string
      */
     public static String toString(InputStream inputStream, String charset) throws IOException {
         return new String(toBytes(inputStream), charset);
     }
 
 }
