 package it.unibz.util;
 
 import it.unibz.controller.FtpConnectionDAO;
 import it.unibz.model.FtpConnectionBean;
 import it.unibz.model.UserBean;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 public class FtpConnectionsServlet extends HttpServlet
 {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 8868246447873132050L;
 
 	/**
    * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
    * @param request servlet request
    * @param response servlet response
    * @throws ServletException if a servlet-specific error occurs
    * @throws IOException if an I/O error occurs
    */
   @SuppressWarnings("rawtypes")
 	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
   throws ServletException, IOException {
   	HttpSession s = 	request.getSession();
   	UserBean user = null;
   	if(s!=null && s.getAttribute("currentSessionUser")!=null)
   		user= (UserBean)s.getAttribute("currentSessionUser");
   	else
 return;
   	if(request.getParameter("activity")!=null){
   			String activity=request.getParameter("activity");
   			if(activity.equals("getall")){
   				response.setContentType("text/html;charset=UTF-8");
 
   	      FtpConnectionDAO catalog = new FtpConnectionDAO();
   	      ArrayList catalogItems=null;
   	    	if(user!=null)
   	    		catalogItems = catalog.getItems(user.getID());
   	    	else
   	      	catalogItems= new ArrayList();
 
   	      String callback = request.getParameter("callback");
   	      request.setAttribute("ftpConnections", catalogItems);
   	      request.setAttribute("callback", callback);
   	      
   	      RequestDispatcher dispatcher = request.getRequestDispatcher("ftpConnectionsJSon.jsp");
   	      dispatcher.include(request, response);
   			}
   			if((activity.equals("create")||activity.equals("edit"))&&user!=null){
   				FtpConnectionBean cb = new FtpConnectionBean();
   				cb.setHost(request.getParameter("host"));
   				cb.setPassword(request.getParameter("password"));
   				cb.setConnectionname(request.getParameter("connectionname"));
   				cb.setPort(request.getParameter("port").length()>0?Integer.parseInt(request.getParameter("port")):21);
   				cb.setUsername(request.getParameter("username"));
   				cb.setUserID(user.getID());
   				FtpConnectionDAO dao =  new FtpConnectionDAO();
   				response.getOutputStream().println((activity.equals("create")?dao.createConnection(cb):dao.editConnection(cb))?"success":"fail");
 
   			}
   			if(activity.equals("removeConnection")){
   				FtpConnectionDAO dao =  new FtpConnectionDAO();
   				FtpConnectionBean cb = new FtpConnectionBean();
   				cb.setConnectionname(request.getParameter("connectionname"));
   				cb.setUserID(user.getID());
   				response.getOutputStream().println(dao.removeConnection(cb)?"success":"fail");
   				
   			}
   			if(activity.equals("getfolders")){
   				FtpConnectionDAO dao =  new FtpConnectionDAO();
   				String connectionname=request.getParameter("connectionname");
   				HttpSession ss = request.getSession();
   				FTPConnectionManager ftpconmgr=null;
   				if(!connectionname.equals("/")){
   					//ss.setAttribute("connectionname", connectionname);
     				//Create Connectionmanager only once per user
     				FtpConnectionBean cb=dao.getItem(user.getID(),connectionname);
   					ftpconmgr= new FTPConnectionManager();
     				ftpconmgr.doConnection(cb.getUsername(),cb.getPassword(),cb.getHost(),cb.getPort());
     				ss.setAttribute("connectionmanager", ftpconmgr);
   				}
   				else{
   					//connectionname=(String)ss.getAttribute("connectionname");
   					ftpconmgr=(FTPConnectionManager)ss.getAttribute("connectionmanager");
   				}
   				
   				
   	  	      ArrayList catalogItems=null;
   	  	    	if(user!=null){    	    
   	  	    		catalogItems =  ftpconmgr.getFileList(request.getParameter("currentfolder"));
   	  	    	}else
   	  	      	catalogItems= new ArrayList();
   				//ftpconmgr.removeConnection();
 
   				
   				 String callback = request.getParameter("callback");
   		  	      request.setAttribute("foldercontent", catalogItems);
   		  	      request.setAttribute("callback", callback);
   		  	      
   		  	      RequestDispatcher dispatcher = request.getRequestDispatcher("ftpFolderJSon.jsp");
   		  	      dispatcher.include(request, response);
   				
   			}
   			if(activity.equals("removeItem")){
   				FTPConnectionManager ftpconmgr=(FTPConnectionManager)request.getSession().getAttribute("connectionmanager");
   				response.getOutputStream().println(ftpconmgr.deleteItem((String)request.getParameter("itemname"),((String)request.getParameter("itemtype")).equals("1"))?"success":"fail");
   			}
   			if(activity.equals("makedir")){
   				FTPConnectionManager ftpconmgr=(FTPConnectionManager)request.getSession().getAttribute("connectionmanager");
   				response.getOutputStream().println(ftpconmgr.makeDirectory((String)request.getParameter("currentfolder")+"/"+((String)request.getParameter("dirname")))?"success":"fail");
   			}
   			if(activity.equals("renamename")){
   				FTPConnectionManager ftpconmgr=(FTPConnectionManager)request.getSession().getAttribute("connectionmanager");
   				response.getOutputStream().println(ftpconmgr.renameFileOrDir((String)request.getParameter("currentfolder"),
   						(String)request.getParameter("oldname"),(String)request.getParameter("renamename"))?"success":"fail");
   			}
   			if(activity.equals("downloadfile")){
   				FTPConnectionManager ftpconmgr=(FTPConnectionManager)request.getSession().getAttribute("connectionmanager");
   				
   				String filename=(String)request.getParameter("filename");
  			    response.getOutputStream().println(ftpconmgr.downloadFile((String)request.getParameter("currentfolder"),filename,getServletContext().getRealPath("temp")+"\\")?"success":"fail");
   	  			
   				
   			}
   		}
   		
   	
       
   }
 
 
   // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
   /**
    * Handles the HTTP <code>GET</code> method.
    * @param request servlet request
    * @param response servlet response
    * @throws ServletException if a servlet-specific error occurs
    * @throws IOException if an I/O error occurs
    */
   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response)
   throws ServletException, IOException {
       processRequest(request, response);
   }
 
   /**
    * Handles the HTTP <code>POST</code> method.
    * @param request servlet request
    * @param response servlet response
    * @throws ServletException if a servlet-specific error occurs
    * @throws IOException if an I/O error occurs
    */
   @Override
   protected void doPost(HttpServletRequest request, HttpServletResponse response)
   throws ServletException, IOException {
       processRequest(request, response);
   }
 
 
 
 }
