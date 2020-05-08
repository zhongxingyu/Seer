 package controller;
 
 import helper.BaseHelper;
 import helper.DashboardHelper;
 import hibernate.HibernateUtil;
 
import java.io.BufferedInputStream;
import java.io.BufferedReader;
 import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
 import java.io.PrintWriter;
import java.util.ArrayList;
 import java.util.List;
import java.util.Properties;
 
import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import net.sf.json.JSONObject;
 
import org.apache.catalina.connector.Request;
import org.apache.tomcat.util.net.SecureNioChannel.ApplicationBufferHandler;
 import org.hibernate.Criteria;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.hibernate.criterion.Restrictions;
import org.hibernate.mapping.Property;
 
 import view.BaseView;
 
 import constant.ActionConstant;
 import constant.Application;
 import daemon.BuildRunner;
 import daemon.TailBuffer;
 
 import bean.LoginBean;
 import bean.UserBean;
 
 
 public class CentralController extends HttpServlet{
 	
 	@Override
 	public void init() throws ServletException {
 		// TODO Auto-generated method stub
 		super.init();
 	}
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException,IOException{
 		doPost(req,res);
 	}
 	private boolean loginValidate(HttpServletRequest req){
 		HttpSession session = req.getSession();
 		if(session.getAttribute("userbean")==null)
 			return false;
 		return true;
 	}
 	private boolean actionParamCheck(HttpServletRequest req,int ACTION){
 		if(req.getParameter("actionid")!=null && Integer.parseInt(req.getParameter("actionid")) == ACTION)
 			return true;
 		return false;
 	}
 	public void doPost(HttpServletRequest req,HttpServletResponse res)throws ServletException,IOException{
 		if(loginValidate(req) && !actionParamCheck(req, ActionConstant.LOGIN_A)){
 			System.out.println("login done");
 			Transaction transaction = null;
 			if(actionParamCheck(req,ActionConstant.USER_UPDATE)){//for user detail updates
 				Session session = null;
 				try{
 				session = HibernateUtil.getSessionFactory().openSession();
 				transaction = session.beginTransaction();
 				String hql = "update UserBean set firstname = :fName, lastname = :lName,midname = :mName," +
 							 "mob = :mob where id = :id";
 				Query query = session.createQuery(hql);
 				query.setInteger("id",Integer.parseInt(req.getParameter("userid")));
 				query.setString("fName",req.getParameter("firstname"));
 				query.setString("lName",req.getParameter("lasttname"));
 				query.setString("mName",req.getParameter("midname"));
 				query.setString("mob",req.getParameter("mob"));
 				transaction.begin();
 				query.executeUpdate();
 				transaction.commit();
 				}catch (Exception e){
 					transaction.rollback();
 					e.printStackTrace();
 				}finally{
 					session.flush();
 					session.disconnect();
 					session.close();
 				}
 				res.sendRedirect("index.jsp");
 			//	RequestDispatcher rd = req.getRequestDispatcher("login.html");
 				//rd.forward(req, res);
 			}//END OF USER UPDATE
 			else if(actionParamCheck(req, ActionConstant.USER_READ)){
 				BaseHelper baseHelper = (BaseHelper)classLoader("DashboardHelper");
 				baseHelper.prepareManagePage(req, (BaseView)classLoader("DashboardView"));
 			}else if(actionParamCheck(req, ActionConstant.DASHBOARD)){
 				String branchList = ((JSONObject) DashboardHelper.listBranch(Application._DEPOTPATH)).toString();
 				res.getWriter().write(branchList);
 			}else if(actionParamCheck(req, ActionConstant.INITBUILD)){
 				String branchName = (String) req.getParameter("branchname");
 				BuildRunner buildRunner = new BuildRunner();
 				buildRunner.branchName = branchName;
 				Thread thread = new Thread(buildRunner);
 				thread.start();
 			}else if(actionParamCheck(req, ActionConstant.TAIL)){
 				int frame = (req.getParameter("frame")!=null?Integer.parseInt(req.getParameter("frame")):0);
 				System.out.println("tailing file");
 				    res.setContentType("text/html");
 				    res.setHeader("Cache-Control", "no-cache");
 				    PrintWriter pr = res.getWriter();
 				    String jsonString = TailBuffer.getTailOutput(frame).toString();
 				    pr.write(jsonString);
 				    pr.flush();
 				    pr.close();
 				    
 				    return;
 			}else{
 				res.sendRedirect("index.jsp");
 			}
 		}
 		else if(actionParamCheck(req,ActionConstant.LOGIN_A)){
 			System.out.println("username password Param");
 			if(!req.getSession().isNew()){
 				req.getSession().invalidate();
 			}
 			String username = null,password=null;
 			username = req.getParameter("username");
 			password = req.getParameter("password");
 			System.out.println("user" + username +"----password" +password);
 			LoginBean loginBean=null;
 			Session session = null;
 			Criteria userCriteria = null;
 			try{
 				session = HibernateUtil.getSessionFactory().openSession();
 				Criteria crit = session.createCriteria(LoginBean.class).add(Restrictions.and(Restrictions.eq("username",username),Restrictions.eq("password", password)));
 				List <LoginBean>list = crit.list();
 				System.out.println("list obj"+list);
 				
 				for(LoginBean lb : list){
 					loginBean = lb;
 				}
 				if(loginBean == null){
 					System.out.println("login failed");
 					res.sendRedirect("sign-in.html");	
 					return;
 				}else{
 					 userCriteria= session.createCriteria(UserBean.class).add(Restrictions.eq("loginid", loginBean.getId()));
					List<UserBean> userList = userCriteria.list();	
 					UserBean uBean = (UserBean)userCriteria.list().iterator().next();
 					req.getSession().setAttribute("userbean",uBean);		
 					req.setAttribute("userbean", uBean);
 				}
 			}catch(Exception e){
 					System.out.println("ERROR "+e);
 			}finally{
 				if(session!=null)
 					session.close();
 			}
 							
 				res.sendRedirect("index.jsp");
 		}else{
 			System.out.println("else welcome jsp");
 			res.sendRedirect("sign-in.html");
 				
 		}	
         return;
 	}
 	private Object classLoader(String className){
 		try {
 			@SuppressWarnings("unchecked")
 			Class<Object> loadClass = (Class<Object>) Class.forName(className);
 			return (Object)loadClass.newInstance();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 }
