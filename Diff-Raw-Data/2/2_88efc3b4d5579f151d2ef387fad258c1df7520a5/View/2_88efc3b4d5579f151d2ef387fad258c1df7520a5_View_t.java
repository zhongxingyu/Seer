 package ooo.pasteit;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 @WebServlet(urlPatterns = { "/t/*" })
 public class View extends HttpServlet {
 
     private static final long serialVersionUID = 6712349714840057898L;
 
     private static final String STORAGE = "var"; //$NON-NLS-1$
 
    private static final int MAX_CONTENT_LENGTH = 1024 * 1204 * 1024; // 1M
 
     private File storage;
 
     @Override
     public void init() throws ServletException {
         storage = new File(FileUtils.getUserDir(), STORAGE);
         if (!storage.exists()) {
             storage.mkdirs();
         }
     }
 
     @Override
     protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
         final String q = request.getParameter("q"); //$NON-NLS-1$
         if (q == null) {
             response.sendRedirect("/"); //$NON-NLS-1$
             return;
         }
         if (request.getContentLength() > MAX_CONTENT_LENGTH || request.getContentLength() < 0) {
             response.sendRedirect("/"); //$NON-NLS-1$
             return;
         }
         final String key = new String(Long.toHexString(System.currentTimeMillis()));
         FileUtils.write(new File(storage, key), q);
         response.sendRedirect("/t/" + key); //$NON-NLS-1$
     }
 
     @Override
     protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
         final String uri = request.getRequestURI();
         final File file = new File(storage, uri.substring(uri.lastIndexOf("/") + 1, uri.length())); //$NON-NLS-1$
         if (!file.exists() || !file.isFile()) {
             response.sendRedirect("/"); //$NON-NLS-1$
             return;
         }
         if (!storage.equals(file.getParentFile())) {
             response.sendRedirect("/"); //$NON-NLS-1$
             return;
         }
         request.setAttribute("text", FileUtils.read(file)); //$NON-NLS-1$
         request.setAttribute("location", request.getRequestURL().toString()); //$NON-NLS-1$
         request.setAttribute("fileName", file.getName()); //$NON-NLS-1$
         request.getRequestDispatcher("/WEB-INF/jsp/view.jsp").forward(request, response); //$NON-NLS-1$
     }
 }
