 package com.chat.servlet;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.chat.jdbc.service.IJDBCService;
 import com.chat.jdbc.to.UserDetailsTO;
 import com.chat.jdbc.to.UserFriendsTO;
 import com.chat.util.Constant;
 import com.chat.util.WSUtil;
 
 @Controller
 public class AjaxServlet {
 
 	@Autowired
 	IJDBCService JDBCService;
 
 	/**
 	 * Get Shortcuts.
 	 * 
 	 * @param req
 	 * @param resp
 	 * @throws IOException
 	 */
 	@RequestMapping(value = "/handle")
 	@ResponseBody
 	public void handle(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		// 处理编码问题
 		// req.setCharacterEncoding("UTF-8");
 		// resp.setContentType("text/html;charset=UTF-8");
 
 		req.getSession();
 
 		String action = req.getParameter("act");
 		String result = "";
 		
 		if (action.equals(Constant.ACTION_TYPE.GET_SHORTCUTS)) {
 
 			result = Constant.AJAX_FORMATS.SC_FORMAT;
 
 		} else if (action.equals(Constant.ACTION_TYPE.GET_USER_FRIENDS_LIST)) {
 			long uid = parseUID(req);
 			List<UserFriendsTO> list = JDBCService.getFriendsListByUID(uid);
 
 			StringBuilder names = new StringBuilder();
 			// String str = "";
 			// String regex ="";
 			// String rplmt = "";
 			System.out.println("list.size(): " + list.size());
//			for (UserFriendsTO friend : list) {
//				String fname = friend.getFriendDetailsTO().getAlias();
//				names.append(WSUtil.formatUtil(Constant.AJAX_FORMATS.FRIENDS_LIST_NAME, Constant.REGX.LIST_NAME_PATERN,
//						"'" + fname + "'"));
//				names.append(",");
//			}
//			names.deleteCharAt(names.length() - 1);
//			result = WSUtil.formatUtil(Constant.AJAX_FORMATS.FRIENDS_LIST, Constant.REGX.LIST_NAME_PATERN,
//					names.toString());
			
			ObjectMapper mapper = new ObjectMapper();
			result = mapper.writeValueAsString(list);
 		}else if(action.equals(Constant.ACTION_TYPE.GET_USER_DETAILS)){
 			long uid = parseUID(req);
 			UserDetailsTO list = JDBCService.getUserDetails(uid);
 			//TODO
 			ObjectMapper mapper = new ObjectMapper();
 			result = mapper.writeValueAsString(list);
 		}
 
 		System.out.println("action: " + action);
 		System.out.println("result: " + result.replaceAll("'", "\""));
 
 		resp.getWriter().write(result.replaceAll("'", "\""));
 	}
 
 	public static void main(String[] args) {
 		// boolean b =
 		// Pattern.compile("%[a-zA-z]{0,}%").matcher("asdf#aadd#ass").group();
 		boolean b = "asdf#aadd#ass".matches("#[a-zA-z]{0,}#");
 		boolean a = "da@%aaDd%sd".matches(".*%[a-zA-z]{0,}%*.");
 		"da@%aaDd%sd".split("%[a-zA-z]{0,}%");
 
 		System.out.println(b + " " + a);
 	}
 	
 	private long parseUID(HttpServletRequest req){
 		long uid = 1;
 		try {
 			 uid = Long.parseLong(req.getParameter("uid"));
 		} catch (NumberFormatException e) {
 			System.err.println("[error]: UID number formate error. "+e);
 		} 
 		return uid;
 	}
 }
