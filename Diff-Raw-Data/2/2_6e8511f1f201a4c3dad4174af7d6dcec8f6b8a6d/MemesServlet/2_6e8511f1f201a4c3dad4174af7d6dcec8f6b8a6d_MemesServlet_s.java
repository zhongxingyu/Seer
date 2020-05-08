 package com.epam.memegen;
 
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 @SuppressWarnings("serial")
 public class MemesServlet extends HttpServlet {
   @SuppressWarnings("unused")
   private static final Logger logger = Logger.getLogger(MemesServlet.class.getName());
 
   private final MemeDao memeDao = new MemeDao();
 
   public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
     resp.setContentType("application/json");
     resp.setCharacterEncoding("UTF-8");
     resp.setHeader("X-Chrome-Exponential-Throttling", "disable");
 
     String filter = req.getParameter("filter");
    if (filter.isEmpty()) {
       filter = "popular";
     }
 
     String json = memeDao.getAllAsJson(req, filter);
     resp.getWriter().write(json);
   }
 }
