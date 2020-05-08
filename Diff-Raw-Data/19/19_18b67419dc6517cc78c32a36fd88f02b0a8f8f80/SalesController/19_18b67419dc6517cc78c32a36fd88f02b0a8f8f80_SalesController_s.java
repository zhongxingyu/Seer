 package com.vaps.home;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 
 import javax.annotation.Resource;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.SessionAttributes;
 
 import com.vaps.action.ItemsListAction;
 import com.vaps.bean.Items;
 import com.vaps.bean.SalesList;
 import com.vaps.dao.ItemsDAO;
 import com.vaps.userclass.CartBiz;
 
 
 /**
  * 일반 사용자 계정
  * 세션은 HomeController.session 으로 쓰세요
  */
 @Controller
 @SessionAttributes("members")
 public class SalesController{
 	@Resource(name = "itemsDao")
 	private ItemsDAO itemsDAO;
 
 	//판매물품 리스트(고객용)
 	@RequestMapping(value = "/AllItems")
 	public String AllItems(HttpServletRequest req, HttpServletResponse res, Model model) {
 		String result = "home";
 
 		try {
 			ItemsListAction item = new ItemsListAction(itemsDAO);
 			if (HomeController.session != null && HomeController.session.getAttribute("id") != "") {
 				model.addAttribute("ilist", item.getItemsList());
 				result = "sales/AllItems";
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return result;
 	}
 	//판매물품 리스트 카테고리(고객용)
 	@RequestMapping(value = "/ViewItems")
 	public String ViewItems(HttpServletRequest req, HttpServletResponse res, Model model) {
 		HomeController.session = req.getSession();
 		ItemsListAction item = new ItemsListAction(itemsDAO);
 
 		try {
 			req.setCharacterEncoding("UTF-8");
 			if (HomeController.session != null && HomeController.session.getAttribute("id") != "") {
 				String i_category = req.getParameter("str");
 				model.addAttribute("ilist", item.getCategoryContents(i_category));
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return "sales/ViewItems";
 	}
 	//판매물품 내용(고객용)
 	@RequestMapping(value = "/ViewItemsContent")
 	public String ViewItemsContent(HttpServletRequest req, HttpServletResponse res, Model model) {
 		HomeController.session = req.getSession();
 		ItemsListAction item = new ItemsListAction(itemsDAO);
 		try {
 			req.setCharacterEncoding("UTF-8");
 			res.setContentType("text/html;charset=UTF-8");
 		String i_name=null;
 		String i_num = req.getParameter("num");
 
 			if (HomeController.session != null && HomeController.session.getAttribute("id") != "") {
 				i_name = req.getParameter("str");
 				model.addAttribute("ilist", item.getContents(i_name)); // 원글 보기
 			}
 
 			//쿠키 설정
 			Cookie cookie = new Cookie("selectItem"+i_num, URLEncoder.encode(i_name, "UTF-8"));
 			cookie.setMaxAge(60*60*24);
 			 cookie.setPath("/"); 
 			res.addCookie(cookie);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return "sales/ViewItemsContent";
 
 	}
 //--------------------------------------------------------------
 // 판매 관리 
 	@RequestMapping(value = "/CartListAdd")
 	public String CartListAdd(HttpServletRequest request) throws UnsupportedEncodingException {
 		//장바구니 추가
 		request.setCharacterEncoding("UTF-8");
 		ItemsListAction item = new ItemsListAction(itemsDAO);
 		String str = request.getParameter("str");
 		Items items = item.getContents(str);
 
 		CartBiz cartBiz = new CartBiz();
 		cartBiz.addCart(request,items);
 
 		//화면 출력
 		ArrayList<Items> cartList = cartBiz.getCartList(request);
 		int totalMoney=0;
 		if(cartList!=null){	
 			for(int i=0;i<cartList.size();i++){
 			int	money = 
 		    cartList.get(i).getI_price()*cartList.get(i).getBuyCount();
 			totalMoney += money;
 			}
 		}
 			request.setAttribute("totalMoney", totalMoney);
 			request.setAttribute("cartList", cartList);
 
 		return "sales/CartList";
 	}
 	@RequestMapping(value = "/CartList")
 	public String CartList(HttpServletRequest request){
 		//화면 출력
 		CartBiz cartBiz = new CartBiz();
 		ArrayList<Items> cartList = cartBiz.getCartList(request);
 		int totalMoney=0;
 		if(cartList!=null){	
 			for(int i=0;i<cartList.size();i++){
 			int	money = 
 		    cartList.get(i).getI_price()*cartList.get(i).getBuyCount();
 			totalMoney += money;
 			}
 		}
 			request.setAttribute("totalMoney", totalMoney);
 			request.setAttribute("cartList", cartList);
 		return "sales/CartList";
 	}
 	@RequestMapping(value = "/itemsCntUp")
 	public void itemsCntUp(HttpServletRequest request,HttpServletResponse response) throws IOException{
 		request.setCharacterEncoding("UTF-8");
 		String str = request.getParameter("str");
 		CartBiz cartBiz = new CartBiz();
 		cartBiz.cntUp(request,str);
 
 		PrintWriter out = response.getWriter();
 		out.println("<script>");
 		out.println("location.href='/CartList'");
 		out.println("</script>");
 	}
 	@RequestMapping(value = "/itemsCntDown")
 	public void itemsCntDown(HttpServletRequest request,HttpServletResponse response) throws IOException{
 		request.setCharacterEncoding("UTF-8");
 		String str = request.getParameter("str");
 		CartBiz cartBiz = new CartBiz();
 		cartBiz.cntDown(request,str);
 
 		PrintWriter out = response.getWriter();
 		out.println("<script>");
 		out.println("location.href='/CartList'");
 		out.println("</script>");
 	}
 	@RequestMapping(value = "/itemsRemove")
 	public void itemsRemove(HttpServletRequest request,HttpServletResponse response) throws IOException{
 		request.setCharacterEncoding("UTF-8");
 		String[] str = request.getParameterValues("delete");
 		CartBiz cartBiz = new CartBiz();
 		cartBiz.removeCartItem(request,str);
 
 		PrintWriter out = response.getWriter();
 		out.println("<script>");
 		out.println("location.href='/CartList'");
 		out.println("</script>");
 	}
 	@RequestMapping(value = "/TodayList")
 	public String TodayList(HttpServletRequest req,Model model) {
 		// 오늘 본 상품 정보 받아오기
 		ItemsListAction item = new ItemsListAction(itemsDAO);
 		Cookie[] cookies = req.getCookies();
 		ArrayList<Items> today = new ArrayList<Items>();
 		String value = null;
 
 		if(cookies != null){
 			for(int i=0;i<cookies.length;i++){
 				if(cookies[i].getName().startsWith("selectItem")){
 					try {
 						value = URLDecoder.decode(cookies[i].getValue(), "UTF-8");
 					} catch (UnsupportedEncodingException e) {
 						e.printStackTrace();
 					}
 				   today.add(item.getContents(value));
 				   model.addAttribute("today",today );
 				}
 			}
 		}
 		return "sales/TodayList";
 	}
 	// 주문관련 로직(sales)
 		@SuppressWarnings("null")
 		@RequestMapping(value = "/itemsPurchase")
 		public void itemsPurchase(HttpServletRequest request,HttpServletResponse response) throws IOException{
 			request.setCharacterEncoding("UTF-8");
 			SalesList saleslist = new SalesList();
 			CartBiz cartBiz = new CartBiz();
 			ItemsListAction item = new ItemsListAction(itemsDAO);
 			ArrayList<Items> cartList = cartBiz.getCartList(request);
 			
 			int getBuyCode = item.getMaxBuyCode()+1;// s_buy_code는 저장된 code에서 +1하여 새로 코드를 저장함
 			String getBuyID = (String) HomeController.session.getAttribute("id"); // s_id는 현재 세션의 id값을 가져온다
 			for (int i = 0; i < cartList.size(); i++){
 					// s_num, 시퀀스
 				saleslist.setS_buy_code(getBuyCode); // s_id
 				saleslist.setS_id(getBuyID); // s_buy_code
 				saleslist.setS_item_name(cartList.get(i).getI_name()); //s_item_name
 				int buyCnt = cartList.get(i).getBuyCount();
 				saleslist.setS_buy_cnt(buyCnt); //s_buy_cnt
 				saleslist.setS_buy_price(cartList.get(i).getI_price() * buyCnt); //s_buy_price
 					// s_buy_date, 디폴트
 			}
 				
 				response.setContentType("text/html;charset=UTF-8"); // 한글처리코드
 				PrintWriter out = response.getWriter();
				if (item.setBuyItems(saleslist) == 1) { // db로 insert 수행 
 					//구매완료 했으면 장바구니를 날린다.
 					HomeController.session.removeAttribute("cartList");
 					
 					out.println("<script>");
					out.println("alert('정상 처리되었습니다!')");
 					out.println("location.href='/'");
 					out.println("</script>");
				} else {
					// 실패시 홈으로 이동
					out.println("<script>");
					out.println("alert('상품구입에 실패하였습니다.')");
					out.println("location.href='/'");
					out.println("</script>");
				}
 		}
 		
 }
