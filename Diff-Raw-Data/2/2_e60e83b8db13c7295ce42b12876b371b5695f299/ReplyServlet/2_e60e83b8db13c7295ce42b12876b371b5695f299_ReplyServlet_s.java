 package reply.controller;
 
 import java.io.IOException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import member.model.MemberVO;
 
 import reply.model.ReplyHibernateDAO;
 import reply.model.ReplyVO;
 
 public class ReplyServlet extends HttpServlet {
 
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		this.doPost(request, response);
 	}
 
 	protected void doPost(HttpServletRequest req, HttpServletResponse res)
 			throws ServletException, IOException {
 		req.setCharacterEncoding("UTF-8");
 		String action = req.getParameter("action");
 		ReplyHibernateDAO dao = new ReplyHibernateDAO();
 
 		List<String> errorMsgs = new ArrayList<String>();
 		// Store this set in the request scope, in case we need to
 		// send the ErrorPage view.
 		req.setAttribute("errorMsgs", errorMsgs);
 
 		try {
 			if ("insert".equals(action)) {
 				ReplyVO replyVO = new ReplyVO();
 				String url = req.getRequestURI();
 				req.setAttribute("url", url);
 				Integer d_no = Integer.valueOf(req.getParameter("d_no"));
 				String r_context = req.getParameter("r_context");
 				Timestamp time = new java.sql.Timestamp(
 						new java.util.Date().getTime());
 				// 之後修改成從session獲取會員編號
 //				int m_no = 1001;
 				if (req.getSession().getAttribute("m_no") == null) {
 					RequestDispatcher failureView = req
 							.getRequestDispatcher("/login.jsp"); // 導入登入頁面
 					failureView.forward(req, res);
 					return;
 				}
 				Integer m_no = (Integer) req.getSession().getAttribute("m_no");
 				MemberVO memberVO = new MemberVO();
 				memberVO.setM_no(m_no);
 
				if(r_context.trim().length() < 10){				//推文的檢查
 					errorMsgs.add("推文內容請輸入超過10字");
 				}
 				if (!errorMsgs.isEmpty()) {
 					req.setAttribute("ErrorMsgKey", errorMsgs);
 					RequestDispatcher failureView = req
 							.getRequestDispatcher("/error.jsp");//導入錯誤處理頁面
 					failureView.forward(req, res);
 					return;										// 程式中斷
 				}
 				// 防止使用者在內文中，輸入<sricpt>之攻擊
 				r_context = Script2Text(r_context);
 				replyVO.setD_no(d_no);
 				replyVO.setR_context(r_context);
 				replyVO.setR_status("ooo");
 				replyVO.setR_datetime(time);
 				replyVO.setR_final_edit(time);
 				replyVO.setMemberVO(memberVO);
 				dao.insert(replyVO);
 				res.sendRedirect("DiscussionList.do?action=getOne&d_no=" + d_no);
 			}
 		} catch (Exception e) {
 
 		}
 
 	}
 
 	// 內文<script>標籤檢查
 	public static String Script2Text(String inputString) {
 		String ScriptStr = inputString; // Script標籤字串
 		String textStr = "";
 		java.util.regex.Pattern p_script;
 		java.util.regex.Matcher m_script;
 
 		try {
 			String regEx_script = "<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>"; // 定义script的正则表达式{或<script[^>]*?>[\\s\\S]*?<\\/script>
 																										// }
 			p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);
 			m_script = p_script.matcher(ScriptStr);
 			ScriptStr = m_script.replaceAll("<h1>YOU CAN ATTACK ME!!!!!!</h1>"); // 过滤script标签
 
 			textStr = ScriptStr;
 
 		} catch (Exception e) {
 			System.err.println("Html2Text: " + e.getMessage());
 		}
 
 		return textStr;// 返回文本字符串
 	}
 }
