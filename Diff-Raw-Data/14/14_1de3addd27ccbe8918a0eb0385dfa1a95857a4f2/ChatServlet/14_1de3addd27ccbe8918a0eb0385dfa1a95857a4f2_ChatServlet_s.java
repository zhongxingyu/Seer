 package ServerCore;
 
 import java.io.IOException;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 @WebServlet("/ChatServlet")
 public class ChatServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     public ChatServlet() {
         super();
         
     }
     
     private void chatRouting(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
     {
     	HttpSession session 	= req.getSession(true);
     	RequestDispatcher rd 	= null;
     	    	
     	//On recupere le userBean de la session    
     	Bean.UserBean userBean	= (Bean.UserBean) session.getAttribute("userBean");		
 		if(userBean == null)
 		{
 			System.out.println("Warning: userBean is null in ChatServlet");
 			userBean	=  new Bean.UserBean();
 			session.setAttribute("userBean", userBean);
 		}
     	
 		//On recupere le searchBean de la session    
     	Bean.UserBean searchUserBean = (Bean.UserBean) session.getAttribute("searchUserBean");
 		
 		//On recupere le chatRouterBean de la session    	
     	Bean.Router cr 		= (Bean.Router) session.getAttribute("chatRouterBean");
     	if(cr == null)
     	{
     		System.out.println("Warning: chatRouterBean is null in ChatServlet");
     		cr = new Bean.Router();
     		session.setAttribute("chatRouterBean", cr);
     	}
     	cr.setAction(req.getParameter("action"));
     	
     	//On recupere le msgManagerBean de la session    	
     	userBean.getMsgManager().setDstUserId(req.getParameter("contactId"));
     	
 		switch(cr.getAction())
 		{
 			case "openChat":
 			{    		
 				cr.setUrl("chatWindow.jsp");
 	    		rd = req.getRequestDispatcher("content/chat/chat.jsp");
 //	    		rd.forward(req, res);
 	    	}break;
 			
 			case "openAddContactWindow":
			{   
				if(searchUserBean == null)
 				{
					System.out.println("Warning: searchUserBean is null in ChatServlet");
 					searchUserBean	=  new Bean.UserBean();
 					session.setAttribute("searchUserBean", searchUserBean);
 				}
 				cr.setUrl("addContact.jsp");
 	    		rd = req.getRequestDispatcher("content/chat/chat.jsp");		
 
 	    	}break;
 	    	
 			case "clearAddContactWindow":
 			{   
 				searchUserBean	=  new Bean.UserBean();
 				session.setAttribute("searchUserBean", searchUserBean);
 				cr.setUrl("addContact.jsp");
 	    		rd = req.getRequestDispatcher("content/chat/chat.jsp");		
 
 	    	}break;
 	    	
 			case "findContact":
 			{    		
 				// Le searchUserBean est sense etre null avant la recherche
 				if (searchUserBean != null)
 				{
 					searchUserBean		=  new Bean.UserBean();				
 				}
 				String searchString 		= req.getParameter("searchString").trim();
 				
 				searchUserBean.setLogin(searchString);
 				
				System.out.println(searchUserBean.getLogin());
				
 				session.setAttribute("searchUserBean", searchUserBean);
 				cr.setUrl("addContact.jsp");
 	    		rd = req.getRequestDispatcher("content/chat/chat.jsp");
 //	    		rd.forward(req, res);
 	    	}break;
 	    	
 			case "addContact":
 			{
 				session.setAttribute("searchUserBean", null);
 								
 				userBean.getUser().addContact(userBean.getMsgManager().getDstUserId());
 				cr.setUrl("contactWindow.jsp");
 	    		rd = req.getRequestDispatcher("content/chat/chat.jsp");
 //	    		rd.forward(req, res);
 	    	}break;
 	    	
 	    	//Action: on est sur la chatWindow et on veut retourner a la contactWindow. Il faut donc vider le dstUserId du msgManager
 			case "backToContactView":
 			{    		
 				cr.setAction("DefaultView");
 				cr.setUrl("contactWindow.jsp");
 				userBean.getMsgManager().setDstUserId(0);
 				System.out.println("test");
 	    		rd = req.getRequestDispatcher("content/chat/chat.jsp");
 //	    		rd.forward(req, res);
 	    	}break;
 	    	
 	    	default:
 	    	{
 	    		cr.setAction("DefaultView");
 	    		cr.setUrl("contactWindow.jsp");
 	    		rd = req.getRequestDispatcher("content/chat/chat.jsp");
 //	    		rd.forward(req, res);
 	    	}break;
 		}
 		
     	rd.forward(req, res);	
     }
 
 	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
 	{
 		System.out.println("User" + ((Bean.UserBean)req.getSession(true).getAttribute("userBean")).getId() + ": Entering ChatServlet.doGet");
 		chatRouting(req, res);
 		
 	}
 
 	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
 	{
 		System.out.println("ChatServlet: Entering doPost");
 		chatRouting(req, res);
 	}
 	
 	
 }
