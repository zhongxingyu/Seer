 /*
  * @author <a href="wehrens@aei.mpg.de">Oliver Wehrens</a>
  * @versiob $Id$
  */
 
 package org.gridlab.gridsphere.portlets.core;
 
 import org.gridlab.gridsphere.event.ActionEvent;
 import org.gridlab.gridsphere.event.FormEvent;
 import org.gridlab.gridsphere.event.impl.FormEventImpl;
 import org.gridlab.gridsphere.portlet.*;
 import org.gridlab.gridsphere.portlet.service.PortletServiceNotFoundException;
 import org.gridlab.gridsphere.portlet.service.PortletServiceUnavailableException;
 import org.gridlab.gridsphere.portletcontainer.GridSphereProperties;
 import org.gridlab.gridsphere.services.user.UserManagerService;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 
 import javax.servlet.UnavailableException;
 import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.TransformerException;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.MalformedURLException;
 import java.util.*;
 
 public class RSSPortlet extends AbstractPortlet {
 
     String _url = "http://www.xml.com/2002/12/18/examples/rss20.xml.txt";  // the serverside should be default rss feed
     long _lastFetched = 0;
     int _fetch_interval = 5*6000;        // in millisec
     Document RSSFeed = new Document(new Element("rss"));
 
     public void init(PortletConfig config) throws UnavailableException {
         super.init(config);
         System.err.println("init() in RSSPortlet");
     }
 
     private Document getRSSFeed(String url) {
         Document doc = new Document(new Element("rss"));
         try {
             SAXBuilder builder = new SAXBuilder(false);
             URL feedurl = new URL(url);
             doc = builder.build(feedurl);
             _lastFetched = System.currentTimeMillis();
             log.info("Fetched rss feed from "+url);
         } catch (MalformedURLException e) {
         } catch (JDOMException e) {
         }
 
         return doc;
 
     }
 
     public void actionPerformed(ActionEvent evt) {
 
 
         DefaultPortletAction _action = evt.getAction();
         PortletRequest req = evt.getPortletRequest();
 
         FormEvent form = new FormEventImpl(evt);
 
         String button = form.getPressedSubmitButton();
 
         if (_action.getName().equals("rss_edit")) {
             if (button.equals("show")) {
                 String temp_url = req.getParameter("rss_url");
                 if (!temp_url.equals(_url)) {
                     _lastFetched = 0;
                     _url=temp_url;
                 }
                 req.setMode(Portlet.Mode.VIEW);
             }
             if (button.equals("cancel")) {
                 req.setMode(Portlet.Mode.VIEW);
             }
             if (button.equals("check")) {
                 //@TODO need to put the RSS feed into a bean!
                 req.setMode(Portlet.Mode.EDIT);
                 Document doc = getRSSFeed(req.getParameter("rss_url"));
                 Element root = doc.getRootElement();
                 String version = root.getAttributeValue("version");
                 if (version.equals("2.0")) { req.setAttribute("rss_support","yes");} else {req.setAttribute("rss_support","no");}
             }
         }
 
         if (_action.getName().equals("rss_configure")) {
             if (button.equals("cancel")) {
                 req.setMode(Portlet.Mode.VIEW);
             }
             if (button.equals("delete")) {
 
             }
             if (button.equals("ok")) {
                 String[] result = form.getSelectedListBoxValues("Labeltest");
                 req.setMode(Portlet.Mode.VIEW);
             }
 
             if (button.equals("add")) {
                 String name = req.getParameter("rss_name");
                 String url = req.getParameter("rss_url");
             }
         }
     }
 
     public void doView(PortletRequest request, PortletResponse response) throws PortletException, IOException {
 
         PrintWriter out = response.getWriter();
         if ((System.currentTimeMillis()-_lastFetched)>_fetch_interval) {
             RSSFeed = getRSSFeed(_url);
 
         } else {
             out.println("Document was cached.<br>");
         }
         Element root = RSSFeed.getRootElement();
         PortletURI loginURI = response.createURI();
         String version = "unknown";
         version = root.getAttributeValue("version");
         out.println("RSS freed from URL: "+_url);
 
         try {
             if (version.equals("2.0") || version.equals("3.14159265359")) {
                 List items = root.getChild("channel").getChildren("item");
                 Iterator it = items.iterator();
                 out.println("<ul>");
                 while (it.hasNext()) {
                     Element item = (Element)it.next();
                     String title = item.getChild("title").getText();
                     String link = item.getChild("link").getText();
                     String desc = item.getChild("description").getText();
                     out.println("<li><a target=\"_new\" href=\""+link+"\">"+title+"</a><br/>"+desc+"</li>");
                 }
                 out.println("</ul>");
             } else {
                 out.println("Unsupported RSS feed version ("+version+")");
             }
         } catch (Exception e) {
             out.println("This is a not a RSS feed.");
             System.out.println("============> "+e);
         }
     }
 
     public void doEdit(PortletRequest request, PortletResponse response) throws PortletException, IOException {
 
         PortletURI returnURI = response.createReturnURI();
 
         DefaultPortletAction defAction = new DefaultPortletAction("rss_edit");
         returnURI.addAction(defAction);
        request.setAttribute("rss_url", returnURI.toString());
         getPortletConfig().getContext().include("/jsp/rss/edit.jsp", request, response);
     }
 
     public void doConfigure(PortletRequest request, PortletResponse response)
             throws PortletException, IOException {
         PortletURI returnURI = response.createReturnURI();
 
 
         DefaultPortletAction defAction = new DefaultPortletAction("rss_configure");
         returnURI.addAction(defAction);
         request.setAttribute("rss_url", returnURI.toString());
 
         getPortletConfig().getContext().include("/jsp/rss/configure.jsp", request, response);
     }
 
     public void doHelp(PortletRequest request, PortletResponse response) throws PortletException, IOException {
         doView(request,response);
     }
 
 
 }
