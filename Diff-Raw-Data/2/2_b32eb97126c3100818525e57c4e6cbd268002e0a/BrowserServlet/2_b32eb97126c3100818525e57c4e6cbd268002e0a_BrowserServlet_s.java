 package org.makumba.parade.view;
 
 import java.io.PrintWriter;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 
 import org.hibernate.Hibernate;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.makumba.parade.init.InitServlet;
 import org.makumba.parade.model.Parade;
 import org.makumba.parade.model.Row;
 import org.makumba.parade.tools.ParadeException;
 import org.makumba.parade.view.managers.FileViewManager;
 
 public class BrowserServlet extends HttpServlet {
 
     /**
      * 
      */
     private static final long serialVersionUID = 1L;
 
     @Override
     public void init() {
     }
 
     @Override
     public synchronized void service(ServletRequest req, ServletResponse resp) throws java.io.IOException,
             ServletException {
         resp.setContentType("text/html");
         resp.setCharacterEncoding("UTF-8");
 
         // fetching parameters
         String display = req.getParameter("display");
         if (display == null)
             display = (String) req.getAttribute("display");
 
         String context = req.getParameter("context");
         if (context == null)
             context = (String) req.getAttribute("context");
 
         // we cache the context in the session
         ((HttpServletRequest) req).getSession().setAttribute("currentContext", context);
 
         String view = req.getParameter("view");
         if (view == null)
             view = (String) req.getAttribute("view");
 
         String order = req.getParameter("order");
         if (view == null)
             view = (String) req.getAttribute("order");
 
         String file = req.getParameter("file");
         if (file == null)
             file = (String) req.getAttribute("file");
 
         String refreshBrowser = req.getParameter("refreshBrowser");
         if (refreshBrowser == null)
             refreshBrowser = (String) req.getAttribute("refreshBrowser");
 
         String path = null;
 
         String getPathFromSession = req.getParameter("getPathFromSession");
         if (getPathFromSession != null) {
             path = (String) ((HttpServletRequest) req).getSession().getAttribute("path");
         } else {
             path = req.getParameter("path");
         }
         if (path == null)
             path = (String) req.getAttribute("path");
 
         if (path != null && path != "")
             ((HttpServletRequest) req).getSession().setAttribute("path", path);
 
         String opResult = (String) req.getAttribute("result");
         Boolean successAttr = (Boolean) req.getAttribute("success");
         boolean success = true;
         if (successAttr == null)
             success = false;
         else
             success = successAttr.booleanValue();
 
         PrintWriter out = resp.getWriter();
 
         Session s = null;
         Transaction tx = null;
 
         try {
 
             s = InitServlet.getSessionFactory().openSession();
 
             Parade p = (Parade) s.get(Parade.class, new Long(1));
 
             Row r = p.getRows().get(context);
             if (r == null) {
                 out.println("Unknown context " + context);
             } else {
 
                 // fetching data from the persistent store
                 // this is needed for lazy collections
                 // r.getFiles().size();
                 Hibernate.initialize(r.getFiles());
                 Hibernate.initialize(r.getApplication());
 
                 // initialising the displays
                 FileViewManager fileV = new FileViewManager();
 
                 RequestDispatcher header = null;
                 RequestDispatcher footer = super.getServletContext().getRequestDispatcher("/layout/footer.jsp");
 
                // switiching to the right display
                 String page = "";
                 if (display.equals("header")) {
                     header = super.getServletContext().getRequestDispatcher(
                             "/layout/header.jsp?class=header&baseTarget=command");
                     // FIXME - path in here is null always, but should actually be equal to the currently browsed path
                     page = "jsp:/browserHeader.jsp";
 
                 }
                 if (display.equals("tree")) {
                     header = super.getServletContext().getRequestDispatcher("/layout/header.jsp?class=tree");
                     page = fileV.getTreeView(p, r);
 
                 }
                 if (display.equals("file")) {
                     throw new ParadeException("Display is file!!! Please report to developers!");
 
                 }
                 if (display.equals("command")) {
                     throw new ParadeException("Display is command! Please report to developers!");
                 }
 
                 // checking whether we include a JSP or not
                 if (page.startsWith("jsp:")) {
                     String url = page.substring(page.indexOf(":") + 1);
                     RequestDispatcher dispatcher = super.getServletContext().getRequestDispatcher(url);
                     dispatcher.forward(req, resp);
 
                 } else {
                     header.include(req, resp);
                     out.println(page);
                     footer.include(req, resp);
                 }
 
             }
 
         } finally {
             s.close();
         }
     }
 
 }
