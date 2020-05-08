 package test;
 
 import java.io.*;
 import java.util.*;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import nu.staldal.lsp.LSPPage;
 import nu.staldal.lsp.servlet.*;
   
 
 public class LSPTestServlet extends HttpServlet
 {
     private LSPManager lspManager;
     
     public void init()
         throws ServletException
     {
         lspManager = LSPManager.getInstance(getServletContext(),
             getClass().getClassLoader());
     }
          
     
     public void doGet(HttpServletRequest req, HttpServletResponse resp)
         throws ServletException, IOException
     {
         String function = req.getPathInfo();
         if (function == null || function.length() == 0)
         {
             resp.sendError(HttpServletResponse.SC_NOT_FOUND, 
                 "No function specified");
             return;
         }
         
         if (function.equals("/FUNC1"))
         {
             req.getSession(true); // create a session
             req.setAttribute("str", "FOO");
             req.setAttribute("num", new Integer(4711));
             lspManager.getRequestDispatcher("Func1").forward(req, resp);
         }
        else if (function.equals("/FloatTest"))
        {
            req.setAttribute("theFloat", new Float(1.11));
            req.setAttribute("theDouble", new Double(1.11));
            lspManager.getRequestDispatcher("FloatTest").forward(req, resp);
        }
         else if (function.equals("/FUNC2"))
         {
             req.setAttribute("hello", "Hi, there!");
             req.setAttribute("alist", new Object[] { "one", "two", "three", "four", "five" });
             req.setAttribute("intlist", new int[] { 0, 11, 22, 33, 44 });
 
 			List food = new ArrayList();
 			Map m;
 			
 			m = new HashMap();
 			m.put("name", "orange");
 			m.put("type", "fruit");
 			m.put("colour", "orange");
 			food.add(m);
 
 			m = new HashMap();
 			m.put("name", "banana");
 			m.put("type", "fruit");
 			m.put("colour", "yellow");
 			food.add(m);
 
 			m = new HashMap();
 			m.put("name", "ice cream");
 			m.put("type", "desert");
 			m.put("colour", "white");
 			food.add(m);
 
 			req.setAttribute("food", food);
 
             lspManager.getRequestDispatcher("Func2").forward(req, resp);
         }
         else if (function.equals("/FUNC3"))
         {
             String flag = req.getParameter("flag");
             req.setAttribute("flag", (flag == null) ? "" : flag);
             lspManager.getRequestDispatcher("Func3").forward(req, resp);
         }
         else if (function.equals("/SETLOCALE"))
         {
             HttpSession sess = req.getSession(true);
             sess.setAttribute(LSPManager.LOCALE_KEY, Locale.ENGLISH);
             lspManager.getRequestDispatcher("Setlocale").forward(req, resp);
         }
         else if (function.equals("/FUNC4"))
         {
             lspManager.getRequestDispatcher("Func4").forward(req, resp);
         }
         else if (function.equals("/DIRLIST1"))
         {
             req.setAttribute("dirlist", 
                     new String[] { "file 1", "file 2", "file 3" });
             lspManager.getRequestDispatcher("dirlist").forward(req, resp);
         }
         else if (function.equals("/DIRLIST2"))
         {
             try {
                 Map lspParams = new HashMap();
                 lspParams.put("dirlist", 
                     new String[] { "first file", "second file", "third file" });
                 LSPPage thePage = lspManager.getPage("dirlist");
                 lspManager.executePage(thePage, lspParams, "dir.xsl", req, resp);
             }
             catch (org.xml.sax.SAXException e)
             {
                 throw new ServletException(e);    
             }
             catch (javax.xml.transform.TransformerConfigurationException e)
             {
                 throw new ServletException(e);    
             }
         }
         else if (function.equals("/XSLTEXT"))
         {
             req.getSession(true);
             try {
                 Map lspParams = new HashMap();
                 LSPPage thePage = lspManager.getPage("xsltext");
                 lspManager.executePage(thePage, lspParams, "extlib.xsl", req, resp);
             }
             catch (org.xml.sax.SAXException e)
             {
                 throw new ServletException(e);    
             }
             catch (javax.xml.transform.TransformerConfigurationException e)
             {
                 throw new ServletException(e);    
             }
         }
         else
         {
             resp.sendError(HttpServletResponse.SC_NOT_FOUND, 
                 "Unknown function");    
         }
     }
     
     
     public void destroy()
     {
         // nothing to do
     }
 }
 
