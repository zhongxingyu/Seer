 package org.otherobjects.cms.controllers;
 
 import groovy.lang.Binding;
 import groovy.lang.GroovyShell;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.net.InetAddress;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.UnknownHostException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.annotation.Resource;
 import javax.jcr.Node;
 import javax.jcr.NodeIterator;
 import javax.jcr.Property;
 import javax.jcr.PropertyIterator;
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 import javax.jcr.query.Query;
 import javax.jcr.query.QueryManager;
 import javax.jcr.query.QueryResult;
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.dom4j.Document;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 import org.dom4j.io.OutputFormat;
 import org.dom4j.io.SAXReader;
 import org.dom4j.io.XMLWriter;
 import org.otherobjects.cms.OtherObjectsException;
 import org.otherobjects.cms.Url;
 import org.otherobjects.cms.config.OtherObjectsConfigurator;
 import org.otherobjects.cms.dao.DaoService;
 import org.otherobjects.cms.dao.GenericDao;
 import org.otherobjects.cms.datastore.JackrabbitDataStore;
 import org.otherobjects.cms.io.ObjectXmlDecoder;
 import org.otherobjects.cms.io.ObjectXmlEncoder;
 import org.otherobjects.cms.jcr.UniversalJcrDao;
 import org.otherobjects.cms.model.BaseNode;
 import org.otherobjects.cms.model.Selector;
 import org.otherobjects.cms.tools.SecurityTool;
 import org.otherobjects.cms.types.TypeService;
 import org.otherobjects.cms.util.HtmlLogger;
 import org.otherobjects.cms.util.ResourceScanner;
 import org.otherobjects.cms.util.Version;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.BeansException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.dao.DataAccessException;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.StatementCallback;
 import org.springframework.security.Authentication;
 import org.springframework.security.GrantedAuthority;
 import org.springframework.security.GrantedAuthorityImpl;
 import org.springframework.security.context.SecurityContextHolder;
 import org.springframework.security.providers.anonymous.AnonymousAuthenticationProvider;
 import org.springframework.security.providers.anonymous.AnonymousAuthenticationToken;
 import org.springframework.security.util.AuthorityUtils;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.context.ServletContextAware;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.view.RedirectView;
 import org.springmodules.jcr.JcrCallback;
 import org.springmodules.jcr.JcrTemplate;
 
 /**
  * Debug controller which provides information about the running application. Also provides simple query facility on 
  * the datastores.
  * 
  * TODO Add groovy scripting suport
  * TODO Add query histories
  * 
  * @author rich
  */
 @Controller
 public class DebugController implements ServletContextAware, ApplicationContextAware
 {
     private static final String EXTERNAL_CONNECTIVITY_TEST_URL = "http://www.google.com/";
 
     protected final Logger logger = LoggerFactory.getLogger(getClass());
 
     private ServletContext servletContext;
     private ApplicationContext applicationContext;
 
     @Resource
     private JcrTemplate jcrTemplate;
 
     @Resource
     private JdbcTemplate jdbcTemplate;
 
     @Resource
     private TypeService typeService;
 
     @Resource
     private DaoService daoService;
 
     @Resource
     private JackrabbitDataStore jackrabbitDataStore;
 
     //@Resource
     //private MailService mailService;
 
     @Resource
     private OtherObjectsConfigurator otherObjectsConfigurator;
 
     @Resource
     private ResourceScanner resourceScanner;
 
     @RequestMapping({"/debug", "/debug/"})
     public ModelAndView debug(HttpServletRequest request, HttpServletResponse response) throws Exception
     {
         // Determine imageMagick status
         String imageMagickError = null;
         String imageMagickVersion = null;
         try
         {
             //TODO this need to be properly configured in ImageMagickResizer
             String binPath = otherObjectsConfigurator.getProperty("otherobjects.imagemagick.bin.path");
 
             String command = binPath + "convert -version";
 
             Process exec = Runtime.getRuntime().exec(command);
             exec.waitFor();
             imageMagickVersion = IOUtils.toString(exec.getInputStream());
 
             Pattern p = Pattern.compile(".*((\\d+)\\.(\\d+)\\.(\\d)+).*", Pattern.DOTALL);
             Matcher m = p.matcher(imageMagickVersion);
             if (m.matches())
             {
                 Version current = Version.getVersion(m.group(1));
                 imageMagickVersion = current.toString();
                 Version required = new Version(6, 3, 2);
                 if (current.compareTo(required) < 0)
                     imageMagickError = "Newer version of ImageMagick required. You have " + current + " but " + required + " is required.";
             }
         }
         catch (Exception e)
         {
             imageMagickError = "Could not find ImageMagick binary: " + e.getMessage();
         }
 
         ModelAndView mav = new ModelAndView("/debug/debug");
         mav.addObject("imageMagickError", imageMagickError);
         mav.addObject("imageMagickVersion", imageMagickVersion);
         mav.addObject("session", request.getSession(false));
 
         // System Properties
         Properties properties = System.getProperties();
         mav.addObject("defaultEncoding", java.nio.charset.Charset.defaultCharset().name());
         mav.addObject("systemUserName", properties.getProperty("user.name"));
         mav.addObject("systemUserLanguage", properties.getProperty("user.language"));
         mav.addObject("systemUserCountry", properties.getProperty("user.country"));
         mav.addObject("systemUserTimezone", properties.getProperty("user.timezone"));
         mav.addObject("javaVendor", properties.getProperty("java.vendor"));
         mav.addObject("javaVersion", properties.getProperty("java.version"));
         mav.addObject("systemOsName", properties.getProperty("os.name"));
         mav.addObject("systemOsVersion", properties.getProperty("os.version"));
         mav.addObject("systemOsArch", properties.getProperty("os.arch"));
         mav.addObject("servletApiVersion", servletContext.getMajorVersion() + "." + servletContext.getMinorVersion());
 
         // Types
         mav.addObject("types", typeService.getTypes());
 
         // Connectivity
         mav.addObject("testExternalUrl", httpPing(EXTERNAL_CONNECTIVITY_TEST_URL));
         mav.addObject("testInternalUrl", httpPing(new Url("/").getAbsoluteLink()));
 
         // Server
         mav.addObject("serverName", getServerName());
         mav.addObject("serverIp", getServerIp());
 
         // Memory
         mav.addObject("freeMemory", Runtime.getRuntime().freeMemory());
         mav.addObject("maxMemory", Runtime.getRuntime().maxMemory());
         mav.addObject("totalMemory", Runtime.getRuntime().totalMemory()); // Total used
 
         // Data stores
         mav.addObject("privateDataPath", otherObjectsConfigurator.getProperty("site.private.data.path"));
         mav.addObject("publicDataPath", otherObjectsConfigurator.getProperty("site.public.data.path"));
         mav.addObject("dbUrl", otherObjectsConfigurator.getProperty("jdbc.url"));
         mav.addObject("dbSchemaVersion", otherObjectsConfigurator.getProperty("db.schema.version"));
         mav.addObject("jcrLocation", otherObjectsConfigurator.getProperty("jcr.repository.location"));
         mav.addObject("jcrSchemaVersion", otherObjectsConfigurator.getProperty("jcr.schema.version"));
 
         mav.addObject("security", new SecurityTool());
         return mav;
     }
 
     /**
      * Provides a view onto the data in the database.
      * 
      * @param request
      * @param response
      * @return
      * @throws Exception
      */
     @RequestMapping({"/debug/database", "/debug/database/"})
     public ModelAndView database(HttpServletRequest request, HttpServletResponse response) throws Exception
     {
         final String sql = request.getParameter("sql");
         String rowsHtml = (String) jdbcTemplate.execute(new StatementCallback()
         {
             public Object doInStatement(Statement stmt) throws SQLException, DataAccessException
             {
                 StringBuffer html = new StringBuffer();
 
                 if (sql != null)
                 {
                     html.append("<table>");
                     ResultSet resultSet = stmt.executeQuery(sql);
 
                     // Add in header rows
                     html.append("\n<tr>");
                     for (int col = 1; col <= resultSet.getMetaData().getColumnCount(); col++)
                         html.append("<th>" + resultSet.getMetaData().getColumnName(col) + "</th>");
                     html.append("</tr>");
 
                     while (resultSet.next())
                     {
                         html.append("\n<tr>");
                         for (int col = 1; col <= resultSet.getMetaData().getColumnCount(); col++)
                             html.append("<td>" + resultSet.getString(col) + "</td>");
 
                         html.append("</tr>");
                     }
                     html.append("</table>");
                 }
                 else
                     html.append("<p>No query.</p>");
                 return html.toString();
             }
         });
 
         ModelAndView mav = new ModelAndView("/debug/database");
         mav.addObject("rowsHtml", rowsHtml);
         mav.addObject("sql", sql);
         return mav;
     }
 
     /**
      * Runs Groovy script.
      * 
      * <p>TODO Need to restrict this to superusers only
      * 
      * @param request
      * @param response
      * @return
      * @throws Exception
      */
     @SuppressWarnings("unchecked")
     @RequestMapping({"/debug/script", "/debug/script/"})
     public ModelAndView script(HttpServletRequest request, HttpServletResponse response) throws Exception
     {
         String script = request.getParameter("script");
 
         ModelAndView mav = new ModelAndView("/debug/script");
         Object output = "No script run.";
         if (StringUtils.isNotEmpty(script))
         {
             try
             {
                 Binding binding = new Binding();
                 binding.setVariable("app", applicationContext);
                 GenericDao dao = ((DaoService) applicationContext.getBean("daoService")).getDao("baseNode");
                 binding.setVariable("jcr", dao);
                 StringWriter writer = new StringWriter();
                 binding.setProperty("logger", new HtmlLogger(writer));
                 GroovyShell shell = new GroovyShell(binding);
                 output = shell.evaluate(script, "DebugScript");
                 mav.addObject("output", output);
                 mav.addObject("log", writer.toString());
             }
             catch (Exception e)
             {
                 e = (Exception) sanitize(e);
                 int line = findLineNumber(e, "DebugScript");
                 String message = e.getMessage() != null ? e.getMessage() : e.getClass().getName();
                 OtherObjectsException otherObjectsException = new OtherObjectsException("Error on line " + line + ": " + message);
                 otherObjectsException.setStackTrace(e.getStackTrace());
                 mav.addObject("exception", e);
                 logger.error("Error running script.", e);
             }
         }
 
         mav.addObject("script", script);
         return mav;
     }
 
     /**
      * Runs Resource Scanner.
      * 
      * <p>TODO Need to restrict this to superusers only
      * 
      * @param request
      * @param response
      * @return
      * @throws Exception
      */
     @RequestMapping("/debug/scan")
     public ModelAndView scan(HttpServletRequest request, HttpServletResponse response) throws Exception
     {
         resourceScanner.updateResources();
         response.getWriter().print("<p>Resource scanning... complete.</p>");
         return null;
     }
 
     /**
      * Exports site data in XML format.
      * 
      * <p>TODO Need to restrict this to superusers only
      * 
      * @param request
      * @param response
      * @return
      * @throws Exception
      */
     @RequestMapping("/debug/export")
     public ModelAndView export(HttpServletRequest request, HttpServletResponse response) throws Exception
     {
         String xpath = request.getParameter("xpath");
 
         Document document = DocumentHelper.createDocument();
 
         Element objects = document.addElement("objects");
         UniversalJcrDao dao = (UniversalJcrDao) daoService.getDao(BaseNode.class);
         ObjectXmlEncoder encoder = new ObjectXmlEncoder();
         for (BaseNode item : dao.getAllByJcrExpression(xpath))
         {
             Element element = objects.addElement("object");
             if (item.getTypeDef() != null)
                 encoder.addObject(element, item, item.getTypeDef());
         }
 
        OutputFormat outformat = new OutputFormat();
        outformat.setNewlines(true);
         outformat.setEncoding("UTF-8");
         XMLWriter writer = new XMLWriter(response.getWriter(), outformat);
         writer.write(document);
         writer.flush();
         return null;
     }
 
     /**
      * Imports site data in XML format.
      * 
      * <p>TODO Need to restrict this to superusers only
      * 
      * @param request
      * @param response
      * @return
      * @throws Exception
      */
     @RequestMapping(value = "/debug/import")
     public ModelAndView importXml(HttpServletRequest request, HttpServletResponse response) throws Exception
     {
         ModelAndView mav = new ModelAndView("/debug/import");
         String xml = request.getParameter("xml");
 
         if (StringUtils.isBlank(xml))
         {
             return mav;
         }
 
         SAXReader xmlReader = new SAXReader();
         Document document = null;
         try
         {
             document = xmlReader.read(new StringReader(xml));
             ObjectXmlDecoder decoder = new ObjectXmlDecoder();
             decoder.setDaoService(daoService);
             decoder.setJackrabbitDataStore(jackrabbitDataStore);
             decoder.setTypeService(typeService);
 
             StringWriter writer = new StringWriter();
             HtmlLogger htmlLogger = new HtmlLogger(writer);
             List<Object> objects = decoder.decode(document);
             for (Object o : objects)
             {
                 htmlLogger.info("Importing: " + o);
                 jackrabbitDataStore.save(o);
             }
             mav.addObject("log", writer.toString());
             mav.addObject("xml", xml);
 
         }
         catch (Exception e)
         {
             e = (Exception) sanitize(e);
             mav.addObject("exception", e);
             logger.error("Error running script.", e);
             mav.addObject("xml", xml);
         }
         return mav;
     }
 
     /**
      * Provides a view onto the data in JCR.
      * 
      * @param request
      * @param response
      * @return
      * @throws Exception
      */
     @RequestMapping({"/debug/jcr", "/debug/jcr/"})
     public ModelAndView jcr(HttpServletRequest request, HttpServletResponse response) throws Exception
     {
         String xpath = request.getParameter("xpath");
         String path = request.getParameter("path");
         String type = request.getParameter("type");
         String export = request.getParameter("export");
 
         if (StringUtils.isNotBlank(path) || StringUtils.isNotBlank(type))
         {
             Selector s = new Selector();
             s.setQueryPath(path);
             s.setSubFolders(true);
             s.setQueryTypeName(type);
             xpath = s.getQuery();
         }
 
         if (StringUtils.isNotEmpty(export))
         {
             RedirectView redirectView = new RedirectView("/otherobjects/debug/export", true, false, true);
             ModelAndView modelAndView = new ModelAndView(redirectView);
             modelAndView.addObject("xpath", xpath);
             return modelAndView;
         }
 
         String liveNodesHtml = null;
         String editNodesHtml = null;
         if (AuthorityUtils.userHasAuthority("ROLE_ADMIN"))
         {
             // we will get the default workspace (edit) so lets temporarily 
             editNodesHtml = getJcrContents(xpath);
 
             // store auth
             Authentication adminAuth = SecurityContextHolder.getContext().getAuthentication();
 
             AnonymousAuthenticationProvider anonymousAuthenticationProvider = new AnonymousAuthenticationProvider();
             anonymousAuthenticationProvider.setKey("testkey");
             AnonymousAuthenticationToken anonymousAuthenticationToken = new AnonymousAuthenticationToken("testkey", "anonymous", new GrantedAuthority[]{new GrantedAuthorityImpl("ROLE_ANONYMOUS")});
             SecurityContextHolder.getContext().setAuthentication(anonymousAuthenticationProvider.authenticate(anonymousAuthenticationToken));
 
             liveNodesHtml = getJcrContents(xpath);
 
             // restore
             SecurityContextHolder.getContext().setAuthentication(adminAuth);
         }
         else
         {
             liveNodesHtml = getJcrContents(xpath);
         }
 
         ModelAndView mav = new ModelAndView("/debug/jcr");
 
         mav.addObject("liveNodesHtml", liveNodesHtml);
         mav.addObject("editNodesHtml", editNodesHtml);
         mav.addObject("xpath", xpath);
         mav.addObject("path", path);
         mav.addObject("type", type);
         return mav;
     }
 
     private String getJcrContents(final String xpath)
     {
         return (String) jcrTemplate.execute(new JcrCallback()
         {
             public Object doInJcr(Session session) throws RepositoryException
             {
                 StringBuffer html = new StringBuffer();
 
                 if (xpath != null)
                 {
                     QueryManager queryManager = session.getWorkspace().getQueryManager();
                     Query query = queryManager.createQuery(xpath, javax.jcr.query.Query.XPATH);
                     QueryResult queryResult = query.execute();
                     NodeIterator nodeIterator = queryResult.getNodes();
                     while (nodeIterator.hasNext())
                         renderNodeInfo(html, nodeIterator.nextNode());
                 }
                 else
                     return "";
                 return html.toString();
             }
         });
     }
 
     protected void renderNodeInfo(StringBuffer html, Node node) throws RepositoryException
     {
         if (node.getPath().equals("/jcr:system"))
             return;
 
         html.append("\n<ul>");
         html.append("<li><strong>" + node.getPath()  +" (" +  node.getPrimaryNodeType().getName()  + ")"+ "</strong><span class=\"properties-area\"");
         html.append("<br/>");
 
         PropertyIterator properties = node.getProperties();
         while (properties.hasNext())
         {
             Property prop = properties.nextProperty();
             if (prop.getDefinition().isMultiple())
                 html.append(" " + prop.getName() + "=" + prop.getValues() + "<br/>");
             else
                 html.append(" " + prop.getName() + "=" + prop.getValue().getString() + "<br/>");
         }
         html.append("</span>");
         html.append("</li>");
         html.append("</ul>");
     }
 
     /**
      * Test connection to the requested URL.
      * 
      * @param url the URL to connect to
      * @return a String describing connection status
      */
     protected String httpPing(String url)
     {
         try
         {
             URL u = new URL(url);
             URLConnection connection = u.openConnection();
             connection.connect();
             return "OK Good connection to: " + url;
         }
         catch (MalformedURLException e)
         {
             return "WARN Bad Url: " + url;
         }
         catch (IOException e)
         {
             return "FAIL Can not connect to: " + url;
         }
     }
 
     /**
      * Sends a test email to the specified recipient.
      * 
      * @param recipient the email address to send to
      * @return a String describing send status
      */
     protected String sendTestEmail(String recipient)
     {
         return "WARN Didn't send test email to: " + recipient;
     }
 
     /**
      * TODO From GrailsUtil.
      * 
      * @param t
      * @return
      */
     public static Throwable sanitize(Throwable t)
     {
         StackTraceElement[] trace = t.getStackTrace();
         List<StackTraceElement> newTrace = new ArrayList<StackTraceElement>();
         for (StackTraceElement stackTraceElement : trace)
         {
             if (isApplicationClass(stackTraceElement.getClassName()))
             {
                 newTrace.add(stackTraceElement);
             }
         }
 
         // Only trim the trace if there was some application trace on the stack
         // if not we will just skip sanitizing and leave it as is
         if (newTrace.size() > 0)
         {
             // We don't want to lose anything, so log it
             //STACK_LOG.error("Sanitizing stacktrace:", t);
             StackTraceElement[] clean = new StackTraceElement[newTrace.size()];
             newTrace.toArray(clean);
             t.setStackTrace(clean);
         }
         return t;
     }
 
     public static int findLineNumber(Throwable t, String fileName)
     {
         StackTraceElement[] trace = t.getStackTrace();
         for (StackTraceElement stackTraceElement : trace)
         {
             if (stackTraceElement.getFileName() != null && stackTraceElement.getFileName().equals(fileName))
                 return stackTraceElement.getLineNumber();
         }
         return -1;
     }
 
     private static final String[] OO_PACKAGES = new String[]{"org.codehaus.groovy.", "groovy.", "org.mortbay.", "sun.", "java.lang.reflect.", "org.springframework.", "com.opensymphony.",
             "org.hibernate.", "javax.servlet."};
 
     public static boolean isApplicationClass(String className)
     {
         for (int i = 0; i < OO_PACKAGES.length; i++)
         {
             String grailsPackage = OO_PACKAGES[i];
             if (className.startsWith(grailsPackage))
             {
                 return false;
             }
         }
         return true;
     }
 
     public static String getServerName()
     {
         try
         {
             return InetAddress.getLocalHost().toString();
         }
         catch (UnknownHostException e)
         {
             return "Could not get server name: " + e.getMessage();
         }
     }
 
     public static String getServerIp()
     {
         try
         {
             return InetAddress.getLocalHost().toString();
         }
         catch (UnknownHostException e)
         {
             return "Could not get server IP: " + e.getMessage();
         }
     }
 
     public void setServletContext(ServletContext servletContext)
     {
         this.servletContext = servletContext;
     }
 
     public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
     {
         this.applicationContext = applicationContext;
     }
 }
