 package org.cnio.appform.servlet;
 
 import java.io.IOException;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.cnio.appform.entity.*;
 import org.cnio.appform.util.AppUserCtrl;
 import org.cnio.appform.util.HibernateUtil;
 import org.cnio.appform.util.IntrvFormCtrl;
 import org.cnio.appform.util.IntrvController;
 import org.cnio.appform.util.Singleton;
 
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.hibernate.HibernateException;
 
 import java.util.List;
 import java.util.Iterator;
 import java.util.Enumeration;
 
 import java.io.PrintWriter;
 import java.net.URLDecoder;
 import org.json.simple.JSONObject;
 /**
  * Servlet implementation class for Servlet: AjaxUtil
  * This is a servlet which is used as server communication for the admintool
  *
  */
  public class AjaxUtilServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
    static final long serialVersionUID = 1L;
 // params to retrieve all of the following items
    static final String PRJS = "prj";
    static final String GRPS = "grp";
    static final String ROLES = "rol";
    
 // this is to check whether a subject code is already on database
    static final String PAT_CHK = "patcode";
    
 // this switch is to serve a request to delete answers
    static final String RMV_ANS = "rmvAns";
    
    
     /* (non-Java-doc)
 	 * @see javax.servlet.http.HttpServlet#HttpServlet()
 	 */
 	public AjaxUtilServlet() {
 		super();
 	}   	
 	
 	
 	
 /* 
  * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
  */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String what = request.getParameter("what");
 		String usrId = request.getParameter("usrid");
 		String jsonResp = "";
 		List<AppGroup> groups = null;
 		List<Project> prjs = null;
 		List<Role> roles = null;
 		boolean nothing = false;
 		
 		Session hibSes = HibernateUtil.getSessionFactory().openSession();
 		AppUser theUsr = null;
 		if (usrId != null)
 			theUsr = (AppUser)hibSes.get(AppUser.class, Integer.parseInt(usrId));
 		
 		AppUserCtrl usrCtrl = new AppUserCtrl (hibSes);
 		what = (what==null)? "": what;
 		if (what.equals(AjaxUtilServlet.GRPS)) {
 			groups = (theUsr == null)? usrCtrl.getAllGroups(): usrCtrl.getGroups(theUsr);
 			jsonResp = "{\"groups\":[";
 			for (AppGroup grp: groups) 
 				jsonResp += "{\"name\":\""+grp.getName()+"\",\"id\":"+grp.getId()+"},";
 		}
 		
 		else if (what.equals(AjaxUtilServlet.PRJS)) {
 			prjs = (theUsr == null)? usrCtrl.getAllProjects(): usrCtrl.getProjects(theUsr);
 			jsonResp = "{\"prjs\":[";
 			for (Project prj: prjs) 
 				jsonResp += "{\"name\":\""+prj.getName()+"\",\"id\":"+prj.getId()+"},";
 		}
 		
 		else if (what.equals(AjaxUtilServlet.ROLES)) {
 			roles = (theUsr == null)? usrCtrl.getAllRoles(): usrCtrl.getRoleFromUser(theUsr);
 			jsonResp = "{\"roles\":[";
 			for (Role role: roles) 
 				jsonResp += "{\"name\":\""+role.getName()+"\",\"id\":"+role.getId()+"},";
 		}
 		
 		else if (what.equalsIgnoreCase("perf")) {
 			doPost (request, response);
 			return;
 		}
 		
 // this is to remove session attributes from the performance side
 // when the user close the window without finishing the interview
 		else if (what.equalsIgnoreCase("end")) {  
 			HttpSession ses = request.getSession();
 // System.out.println("ending session in AjaxUtilServlet: "+ses);
 			if (ses != null) {
 				Enumeration<String> en = ses.getAttributeNames();
 				for (String attr = en.nextElement(); en.hasMoreElements();) {
 					ses.removeAttribute(attr);
 				}
 				jsonResp = "{\"msg\":\"Performance session attributes removed\"}";
 				
 			}
 			else
 				jsonResp = "{\"msg\":\"Sesssion is already null\"}";
 			
 			nothing = true;
 		}
 		
 		else {
 			nothing = true;
 			jsonResp = "{\"msg\":\"Nothing to retrieve\"}";
 //			out.print(getServletInfo());
 		}
 		
 		if (!nothing) {
 			jsonResp = (jsonResp.length()==0)? jsonResp: 
 									jsonResp.substring(0, jsonResp.length()-1);
 			jsonResp += "]}";
 		}
 		
 		response.setHeader("Content-type", "application/json; charset=UTF-8");
 		response.setCharacterEncoding("UTF-8");
 		PrintWriter out = response.getWriter();
 		out.print (jsonResp);
 	}  	
 	
 	
 	
 	
 	
 /* (non-Java-doc)
  * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
  */
 	protected void doPost (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String what = request.getParameter("what"), jsonStr;
 		
 		Session ses = HibernateUtil.getSessionFactory().openSession();
 		IntrvFormCtrl formCtrl = new IntrvFormCtrl(ses);
 
 		if (what.equalsIgnoreCase("rmv")) {
 			String usernames = request.getParameter("users");
 			
 			if (usernames != null) {
 				String[] theUsers = usernames.split(",");
 				for (String user: theUsers)
 					Singleton.getInstance().rmvUser(user);
 			}
 			jsonStr = "{\"res\":1,\"msg\":\"Users "+usernames+
 								" were kicked out the application\"}";
 			PrintWriter pwr = response.getWriter();
 		 	pwr.print (jsonStr);
 		 	
 		 	return;
 		}
 		
 		
 // Checking for removing an item
 		if (what.equalsIgnoreCase("chkRmvElem")) {
 			IntrvController intrvCtrl = new IntrvController (ses);
 			String strItemId = request.getParameter("frmid"), sons;
 			Long itemId = Long.decode(strItemId);
 			
 			HttpSession httpSes = request.getSession();
 			String roles = (String)httpSes.getAttribute("roles");
 			
 // This is only for client purposes: if the item has children, on client the 
 // children has to be deleted from the list
 			AbstractItem item = (AbstractItem)ses.get(AbstractItem.class, itemId);
 			sons = buildSonsJson (item);
 			
 			int result = intrvCtrl.deleteItem(item, roles);
 			if (result == 1) // already deleted
 				jsonStr = "{\"res\":1,\"frmid\":"+strItemId+",\"msg\":\"The element (and children) has been deleted successfully\""+sons+"}";
 				
 			else if (result == 2)
 				jsonStr = "{\"res\":2,\"frmid\":"+strItemId+",\"msg\":\"The element or any of its children " +
 						"contain data from subject interviews. Are you sure of deleting this item?\"}";
 			
 			else if (result == 3)
 				jsonStr = "{\"res\":3,\"msg\":\"The element can be removed ONLY by admins\"}";
 			
 			else // result == -1
 				jsonStr = "{\"res\":0,\"msg\":\"An application error ocurred while trying deleting the element\"}";
 			
 			response.setHeader("Content-type", "application/json; charset=UTF-8");
 			response.setCharacterEncoding("UTF-8");
 			PrintWriter pwr = response.getWriter();
 			pwr.print (jsonStr);
 			
 			return;
 		}
 		
 		
 // Remove an element for sure if 		
 		if (what.equalsIgnoreCase("rmvElem")) {
 			IntrvController intrvCtrl = new IntrvController (ses);
 			String strItemId = request.getParameter("frmid"), sons;
 			Long itemId = Long.decode(strItemId);
 			
 			AbstractItem item = (AbstractItem)ses.get(AbstractItem.class, itemId);
 			sons = buildSonsJson (item);
 			
 			int result = intrvCtrl.removeQuestionsAndAnswers(item);
 			if (result == 1)
 				jsonStr = "{\"res\":1,\"frmid\":"+ strItemId +",\"msg\":\"The element (and children) has been deleted successfully\""+sons+"}";
 			
 			else
 				jsonStr = "{\"res\":0,\"frmid\":"+ strItemId +",\"msg\":\"An application error ocurred while trying deleting the element\"}";
 			
 			response.setHeader("Content-type", "application/json; charset=UTF-8");
 			response.setCharacterEncoding("UTF-8");
 			PrintWriter pwr = response.getWriter();
 			pwr.print (jsonStr);
 			
 			return;
 		}
 		
 		
 		
 // Removing a set of answers when removing a group from a repeatable group
 		if (what.equalsIgnoreCase(AjaxUtilServlet.RMV_ANS)) {
 			boolean res = false;
 			Integer patId = (Integer)request.getSession().getAttribute("patId");
 			String paramIds = request.getParameter("ids");
 			
 			res = formCtrl.removeAnswers(paramIds, patId);
 			
 			if (!res)
 				jsonStr = "{\"res\":0," +
 						"\"msg\":\"Unable to remove group of questions. Contact with admin\"}";
 			else
 				jsonStr ="{\"res\":1}";
 			
 			response.setHeader("Content-type", "application/json; charset=UTF-8");
 			response.setCharacterEncoding("UTF-8");
 			PrintWriter pwr = response.getWriter();
 			pwr.print(jsonStr);
 			
 			return;
 		}
 		
 		
 		
 // Saving a SINGLE answer for a question ////////////////////////////////////	
 		String patId = request.getParameter("patid");
 		Patient pat = (Patient)ses.get(Patient.class, Integer.decode(patId));
 		HttpSession httpSes = request.getSession();		
 		
 // the query string from the items on the interview is:
 // what=perf&q=q-NNNN-OO-GG&val=XXX&patid=patId
 		String quesId = request.getParameter("q"); 
 		String paramVal = request.getParameter("val");
System.out.println("paramVal pete: "+paramVal);
 		paramVal = URLDecoder.decode (paramVal, "UTF-8");
 		paramVal = (paramVal == "")? org.cnio.appform.util.RenderEng.MISSING_ANSWER:
 																paramVal;
 		
 		String ansParams[] = quesId.split("-"); // i have {145,1,2,g2}
 		String qId = ansParams[0].substring(1);
 		Integer ansNumber = Integer.decode(ansParams[1]), 
 						ansOrder = Integer.decode(ansParams[2]), ansGroup = null;
 		boolean res = false;
 		
 		if (ansParams.length > 3) { // we have a group
 			if (ansParams[3] != null) {
 				String aux = ansParams[3].substring(1);
 				ansGroup = Integer.decode(aux);
 			}
 		}
 
 // decoupled? objects... lets see if this works
 		Question q = (Question)ses.get(Question.class, Long.decode(qId));
 		List<AnswerItem> ansTypes = HibernateUtil.getAnswerTypes4Question(ses, q);
 		AnswerItem ansType = ansTypes.get(ansOrder.intValue()-1);
 // out.print("ansType: " + ansType.getName()+"<br>");
 
 	 	Object[] ans = 
 	 		formCtrl.getAnswer4Question(Integer.decode(qId), Integer.decode(patId),
 	 																ansNumber,ansOrder);
 	 	if (ans == null) {
 			res = formCtrl.saveAnswer(q, pat, ansNumber, ansOrder, ansGroup, paramVal, ansType);
 // System.out.println("(tid: "+Thread.currentThread().getId()+") AjaxUtilServlet saving answer w/ result: "+res);	
 			
 // System.out.println ("(tid: "+Thread.currentThread().getId()+") #### AjsxUtilServlet.doPost: about to call getAnswer4Question with qId:"+Integer.decode(qId));
 			ans = formCtrl.getAnswer4Question(Integer.decode(qId), Integer.decode(patId),	ansNumber,ansOrder);
 // System.out.println ("(tid: "+Thread.currentThread().getId()+") #### AjaxUtilServlet: CHECKING for answer after saving question ("+
 //									q.getId()+", n="+ansNumber+", o="+ansOrder+"): "+ans[0]);			
 	 	}	
 	 	else {
 // an update of the answer must be done ONLY if values are different
 // this is to optimize unuseful database accesses
 
 //			if (((String)ans[1]).equalsIgnoreCase(paramVal) == false)
 //	 		if (((String)ans[1]).compareTo(paramVal) != 0)
 	 		if (((String)ans[1]).trim().equals(paramVal.trim()) == false)
 				res = formCtrl.updateAnswer((Integer)ans[0], paramVal);
 			else { // both values are equals!!!
 				res = true;
 			}
 	 	}
 	 	
 	 	ses.flush();
 		ses.close();
 		
 // the returned json string is as:
 // {"res":[0|1],"msg":[""|"could not save the value"]}
 	 	if (res)
 	 		jsonStr = "{\"res\":1,\"msg\":\"\"}";
 	 	
 	 	else
 	 		jsonStr = "{\"res\":0,\"itemname\":\""+
 	 				quesId+"\",\"msg\":\"Value '"+paramVal+"' could not be saved\"}";
 		
 	 	response.setHeader("Content-type", "application/json; charset=UTF-8");
 		response.setCharacterEncoding("UTF-8");
 		PrintWriter out = response.getWriter();
 	 	out.print (jsonStr);
 	}
 	 	
 	
 	
 		
 	 	
 /**
  * Build the json string for the sons of the item. This is to send back to client
  * and make some visual management with it
  * @param item, the item
  * @return a string like 'sons:id1,id2,id3,...,idn' or "" if no sons for item
  */
 	private String buildSonsJson (AbstractItem item) {
 		String sons = "";
 		List<AbstractItem> children = item.getContainees();
 		sons=(children.size() > 0)? ",\"sons\":\"": "";
 		for (int i=children.size(); i>0 ; i--) {
 			AbstractItem son = children.get(i-1);
 			
 			sons += son.getId()+",";
 		}
 		sons = sons.equalsIgnoreCase("")? "": sons.substring(0, sons.length()-1)+"\"";
 		
 		return sons;
 	}
 	
 	
 	
 /* (non-Javadoc)
  * @see javax.servlet.Servlet#getServletInfo()
  */
 	public String getServletInfo() {
 		// TODO Auto-generated method stub
 		return super.getServletInfo();
 	}     
 }
