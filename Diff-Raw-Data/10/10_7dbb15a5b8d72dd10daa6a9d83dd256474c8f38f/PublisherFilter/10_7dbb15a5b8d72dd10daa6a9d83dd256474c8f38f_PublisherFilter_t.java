 package au.org.paperminer.main;
// TODO: this one cause problem
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 
 import au.org.paperminer.common.PaperMinerConstants;
 import au.org.paperminer.common.PaperMinerException;
 import au.org.paperminer.db.PublisherHelper;
 
 public class PublisherFilter implements Filter 
 {
     private Logger m_logger;
     private PublisherHelper m_helper;
 
 	@Override
 	public void destroy()
 	{
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain) 
 		throws IOException, ServletException 
     {
         m_logger.info("PublisherFilter doFilter");
         
         HttpServletRequest httpReq = (HttpServletRequest) req;
 
         String remoteReq = httpReq.getRequestURL().toString();
         int idx = remoteReq.lastIndexOf('/');
         String res = null;
         try {
             String id = req.getParameter("id");
 	        if (remoteReq.substring(idx).startsWith("/info")) {
 	            res = m_helper.getInfo(id);
 	            m_logger.debug(" Pub id=" + id + " " + res);
 		        if (res == null) {
 		            req.setAttribute(PaperMinerConstants.ERROR_PAGE, "e200");
 		        }
 		        else {
 		            resp.setContentType("text/json");
 		            PrintWriter pm = resp.getWriter();
 		            pm.write(res);
		            pm.flush();
 		            pm.close();
 		        }
 	        }
         }
         catch (PaperMinerException ex) {
             req.setAttribute(PaperMinerConstants.ERROR_PAGE, "e201");
         }
        //TODO: amazing error here why why response committed?
         filterChain.doFilter(req, resp);
        //return;
 	}
 
 	@Override
 	public void init(FilterConfig arg0) throws ServletException 
 	{
         m_logger = Logger.getLogger(PaperMinerConstants.LOGGER);
         m_logger.info("PublisherFilter init");
         m_helper = new PublisherHelper();
 	}
 
 }
