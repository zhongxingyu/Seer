 package discussion.controller;
 
 import java.io.IOException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.regex.Pattern;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import member.model.MemberVO;
 import discussion.model.DiscussionHibernateDAO;
 import discussion.model.DiscussionVO;
 
 public class DiscussionManagment extends HttpServlet {
 
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		doPost(request, response);
 	}
 
 	protected void doPost(HttpServletRequest req, HttpServletResponse res)
 			throws ServletException, IOException {
 		req.setCharacterEncoding("UTF-8");
 		String action = req.getParameter("action");
 		DiscussionHibernateDAO dao = new DiscussionHibernateDAO();
 
 		List<String> errorMsgs = new ArrayList<String>();
 		// Store this set in the request scope, in case we need to
 		// send the ErrorPage view.
 		req.setAttribute("ErrorMsgKey", errorMsgs);
 
 		try {
 //			 新增主題功能
 			if ("insert".equals(action)) {
 				// 之後修改成從session獲取管理員編號

 				Integer a_no = (Integer) req.getSession().getAttribute("a_no");
 				if(a_no==null){
 					RequestDispatcher failureView = req
 							.getRequestDispatcher("/admin_login.htm"); //導入後台豋入頁面
 					failureView.forward(req, res);
 					return;
 				}
 				String d_title = req.getParameter("d_title");
 				String d_context = req.getParameter("d_context");
 
 				if (d_title.trim().length() < 5) { // 主題字串的檢查
					errorMsgs.add("請輸入文章主題，並超過5字");
 				}
 				if (d_context.trim().length() < 10) { // 內文的檢查
 					errorMsgs.add("文章內容請輸入超過10字");
 				}
 				if (!errorMsgs.isEmpty()) {
 					req.setAttribute("ErrorMsgKey", errorMsgs);
 					RequestDispatcher failureView = req
							.getRequestDispatcher("/errorReason.jsp");// 導入錯誤處理頁面
 					failureView.forward(req, res);
 					return; // 程式中斷
 				}
 				// 防止使用者在內文中，輸入<sricpt>之攻擊
 				d_context = Script2Text(d_context);
 				System.out.println(d_context);
 				// 設定新增之主題物件參數
 				Timestamp time = new java.sql.Timestamp(
 						new java.util.Date().getTime());
 				String d_status = "ooo";
 				DiscussionVO discussionVO = new DiscussionVO();
 				MemberVO memberVO = new MemberVO();
 				memberVO.setM_no(7);
 				discussionVO.setMemberVO(memberVO);
 				discussionVO.setD_title(d_title);
 				discussionVO.setD_context(d_context);
 				discussionVO.setD_datetime(time);
 				discussionVO.setD_final_edit(time);
 				discussionVO.setD_status(d_status);
 				dao.insert(discussionVO);
 
 				res.sendRedirect("DiscussionManagment?action=getAll");
 			}
 			// 觀看所有文章列表
 			if ("getAll".equals(action)) {
 				dao = new DiscussionHibernateDAO();
 				List<DiscussionVO> list = dao.getAllMananger();
 				splitPages(list, req);
 				req.getSession().setAttribute("action", "getAll");
 				RequestDispatcher dis = req
 						.getRequestDispatcher("/wine_admin/ademin_discussion.jsp");
 				dis.forward(req, res);
 
 			}
 
 			// 觀看主題內文功能
 			if ("getOne".equals(action)) {
 				String url = req.getRequestURI();
 				req.setAttribute("url", url);
 				System.out.println(url);
 				String str = req.getParameter("d_no");
 				int d_no = new Integer(str);
 				// System.out.println(d_no);
 				DiscussionVO discussionVO = dao.findByPrimaryKey(d_no);
 
 				if (discussionVO == null) {
 					errorMsgs.add("查無資料");
 				}
 
 				if (!errorMsgs.isEmpty()) {
 					RequestDispatcher failureView = req
 							.getRequestDispatcher(url);
 					failureView.forward(req, res);
 					return;// 程式中斷
 				}
 
 				req.setAttribute("discussionVO", discussionVO); // 資料庫取出的VO物件,存入req
 				RequestDispatcher successView = req
 						.getRequestDispatcher("/wine_admin/admin_listOneDisc.jsp"); // 成功轉交
 				successView.forward(req, res);
 			}
 			
 			//編輯功能
 			if ("edit".equals(action)) {
 				// 檢驗是否為管理者
 				Integer a_no = (Integer) req.getSession().getAttribute("a_no");
 				System.out.println(a_no);
 				DiscussionVO discussionVO = new DiscussionVO();
 				Integer d_no = Integer.valueOf(req.getParameter("d_no"));
 				discussionVO = dao.findByPrimaryKey(d_no);				
 //				if (a_no != null) {
 					req.setAttribute("discussionVO", discussionVO);
 					RequestDispatcher successView = req
 							.getRequestDispatcher("/wine_admin/admin_editDisc.jsp"); // 成功轉交
 					successView.forward(req, res);
 //				} else {
 //					errorMsgs.add("您並非管理者，請重新登入帳號或電洽資訊部詢問");
 //					req.setAttribute("ErrorMsgKey", errorMsgs);
 //					RequestDispatcher failureView = req
 //							.getRequestDispatcher("/errorReason.jsp");// 導入錯誤處理頁面
 //					failureView.forward(req, res);
 //					return; // 程式中斷
 //				}
 			}
 			
 			//修改
 			if ("update".equals(action)) {
 				DiscussionVO discussionVO = new DiscussionVO();
 				Integer d_no = Integer.valueOf(req.getParameter("d_no"));
 				String d_title = req.getParameter("d_title");
 				String d_context = req.getParameter("d_context");
 				String d_status = req.getParameter("d_status");
 				Integer m_no = Integer.valueOf(req.getParameter("m_no"));
 				MemberVO memberVO = new MemberVO();
 				memberVO.setM_no(m_no);
 				System.out.println(m_no);
 				Timestamp d_datetime = Timestamp.valueOf(req
 						.getParameter("d_datetime"));
 				if (d_title.trim().length() < 5) { // 主題字串的檢查
					errorMsgs.add("請輸入文章主題，並超過5字");
 				}
 				if (d_context.trim().length() < 10) { // 內文的檢查
					errorMsgs.add("文章內容請輸入超過10字");
 				}
 				if (!errorMsgs.isEmpty()) {
 					req.setAttribute("ErrorMsgKey", errorMsgs);
 					RequestDispatcher failureView = req
							.getRequestDispatcher("/errorReason.jsp");// 導入錯誤處理頁面
 					failureView.forward(req, res);
 					return; // 程式中斷
 				}
 
 				discussionVO.setD_no(d_no);
 				discussionVO.setMemberVO(memberVO);
 				discussionVO.setD_title(d_title);
 				discussionVO.setD_context(d_context);
 				discussionVO.setD_datetime(d_datetime);
 				discussionVO.setD_final_edit(new java.sql.Timestamp(
 						new java.util.Date().getTime()));
 				discussionVO.setD_status(d_status);
 				dao.update(discussionVO);
 				
 				res.sendRedirect("DiscussionManagment?action=getOne&d_no=" + d_no);
 
 			}
 
 			//隱藏功能
 			if ("invisible".equals(action)) {
 				Integer a_no = (Integer) req.getSession().getAttribute("a_no");
 //				if (a_no != null) {
 //					System.out.println(a_no);
 					DiscussionVO discussionVO = new DiscussionVO();
 					Integer d_no = Integer.valueOf(req.getParameter("d_no"));
 					discussionVO = dao.findByPrimaryKey(d_no);
 					if(discussionVO.getD_status().equals("xxx")){
 						discussionVO.setD_status("ooo");
 					}else{
 						discussionVO.setD_status("xxx");
 					}
 					dao.update(discussionVO);
 					res.sendRedirect("DiscussionManagment?action=getAll");
 //				} else {
 //					errorMsgs.add("您並非管理者，請重新登入帳號或電洽資訊部詢問");
 //					req.setAttribute("ErrorMsgKey", errorMsgs);
 //					RequestDispatcher failureView = req
 //							.getRequestDispatcher("/errorReason.jsp");// 導入錯誤處理頁面
 //					failureView.forward(req, res);
 //					return; // 程式中斷
 //				}
 			}
 			// 搜尋功能
 			if ("search".equals(action)) {
 				try{
 				String srchThing = req.getParameter("srchThing");
 				String txtsrch = req.getParameter("txtsrch");
 				System.out.println(txtsrch);
 				List<DiscussionVO> list = new ArrayList<DiscussionVO>();
 				dao = new DiscussionHibernateDAO();
 				if(srchThing == null){
 					srchThing = (String) req.getSession().getAttribute("srchThing");
 					txtsrch = (String) req.getSession().getAttribute("txtsrch");
 				}
 				Set<DiscussionVO> set = dao.search(srchThing, txtsrch);
 				Iterator<DiscussionVO> iterator = set.iterator();
 				while(iterator.hasNext()) {
 					list.add((DiscussionVO) iterator.next());
 //					System.out.println(((DiscussionVO)iterator.next()).getD_title());
 				}				
 				splitPages(list, req);
 				if(list.size()==0){
 					req.setAttribute("msgbox","沒有符合您所搜尋的文章");
 					RequestDispatcher dis = req
 							.getRequestDispatcher("DiscussionManagment?action=getAll");
 					dis.forward(req, res);
 					return;
 				}
 				req.getSession().setAttribute("srchThing",srchThing);
 				req.getSession().setAttribute("txtsrch",txtsrch);
 				req.getSession().setAttribute("action", "search");
 				RequestDispatcher dis = req
 						.getRequestDispatcher("/wine_admin/ademin_discussion.jsp");
 				dis.forward(req, res);
 				} catch(Exception e){
 					req.setAttribute("msgbox","沒有符合您所搜尋的文章");
 					req.setAttribute("list", "");
 					RequestDispatcher dis = req
 							.getRequestDispatcher("DiscussionManagment?action=getAll");
 					dis.forward(req, res);
 				}
 			}
 
 		} catch (Exception e) {
 			errorMsgs.add("無法取得資料:" + e.getMessage());
 			RequestDispatcher failureView = req
 					.getRequestDispatcher("/errorReason.jsp");
 			failureView.forward(req, res);
 		}
 	}
 
 	// 分頁功能
 	private void splitPages(List<DiscussionVO> list, HttpServletRequest req) {
 		int rowsPerPage = 10; // 每頁的筆數
 		int rowNumber = list.size(); // 總筆數
 		int pageNumber = 0; // 總頁數
 		int whichPage = 1; // 第幾頁
 		int pageIndexArray[] = null;
 		int pageIndex = 0;
 		
 		req.setAttribute("list", list);
 		String url = req.getRequestURI();
 		req.setAttribute("url", url);
 		req.setAttribute("rowsPerPage", rowsPerPage);
 		req.setAttribute("rowNumber", rowNumber);
 
 		if (rowNumber % rowsPerPage != 0) {
 			pageNumber = rowNumber / rowsPerPage + 1;
 		}
 
 		else {
 			pageNumber = rowNumber / rowsPerPage;
 		}
 		pageIndexArray = new int[pageNumber];
 		int pageIndexArray_length = pageIndexArray.length;// 迴圈變數
 		for (int i = 1; i <= pageIndexArray_length; i++) {
 			pageIndexArray[i - 1] = i * rowsPerPage - rowsPerPage;
 			req.setAttribute("pageIndexArray", pageIndexArray);
 		}
 		
 		try {
 			whichPage = Integer.parseInt(req.getParameter("whichPage"));
 			pageIndex = pageIndexArray[whichPage - 1];
 		} catch (NumberFormatException e) {
 			whichPage = 1;
 			pageIndex = 0;
 		} catch (ArrayIndexOutOfBoundsException e) {
 			if (pageNumber > 0) {
 				whichPage = pageNumber;
 				pageIndex = pageIndexArray[whichPage - 1];
 			}
 		}
 		
 		req.setAttribute("whichPage", whichPage);
 		req.setAttribute("pageIndex", pageIndex);
 		req.setAttribute("pageNumber", pageNumber);
 		req.setAttribute("rowNumber", rowNumber);
 		req.setAttribute("rowsPerPage", rowsPerPage);
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
