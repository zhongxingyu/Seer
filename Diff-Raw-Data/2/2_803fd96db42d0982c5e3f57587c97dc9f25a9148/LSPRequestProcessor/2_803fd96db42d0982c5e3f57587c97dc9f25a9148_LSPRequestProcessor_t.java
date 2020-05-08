 package nu.staldal.lsp.struts;
 
 import java.util.*;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import org.xml.sax.SAXException;
 
 import org.apache.struts.config.*;
 import org.apache.struts.action.*;
 
 import nu.staldal.lsp.LSPPage;
 import nu.staldal.lsp.servlet.LSPManager;
 
 
 public class LSPRequestProcessor extends RequestProcessor
 {
     private LSPManager lspManager;    
     
     
     public void init(ActionServlet servlet, ModuleConfig moduleConfig)
        throws ServletException 
     {
         super.init(servlet, moduleConfig);
 
         lspManager = LSPManager.getInstance(
             servlet.getServletContext(),
             Thread.currentThread().getContextClassLoader());            
     }
     
     
     protected void processForwardConfig(HttpServletRequest request,
                                         HttpServletResponse response,
                                         ForwardConfig forward)
         throws java.io.IOException, ServletException 
 	{
         if (forward == null) 
         {
             return;
         }
         
         String forwardPath = forward.getPath();
         
         if (forwardPath.endsWith(".lsp") && !forward.getRedirect())
         {
             if (forwardPath.charAt(0) == '/')
                 forwardPath = forwardPath.substring(1);
             
             // Remove ".lsp"
             String pageName = forwardPath.substring(0, forwardPath.length()-4);
 
             LSPPage thePage = lspManager.getPage(pageName);                
             if (thePage == null)
             {
                 throw new ServletException("Unable to find LSP page: " + pageName);
             }
             
             response.setContentType(lspManager.getContentType(thePage));		
             response.resetBuffer();		
             
             Map lspParams = new HashMap();
             for (Enumeration e = request.getAttributeNames(); e.hasMoreElements(); )
             {                
                 String name = (String)e.nextElement();
                 Object value = request.getAttribute(name);
                 lspParams.put(name, value);
             }
     
             try {		
                 lspManager.executePage(thePage, 
                                        lspParams, 
                                        response);
             }
             catch (SAXException e)
             {
                 throw new ServletException(e);	
             }
 
             response.flushBuffer();
         }
         else
         {
             super.processForwardConfig(request, response, forward);
         }
     }
 }
 
