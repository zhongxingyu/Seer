 package groupone;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.UUID;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Servlet implementation class CouponSelect
  */
 @WebServlet("/SelectCoupon")
 public class SelectCoupon extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public SelectCoupon() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 		String[] couponIds = request.getParameterValues("checkBox");
 		String gift = request.getParameter("gift");
 		
 		if(couponIds != null) {
 			ArrayList<Coupon> coupons = DBOperation.searchCoupon(couponIds);
 			String objectId = UUID.randomUUID().toString();
 			String objectId2 = UUID.randomUUID().toString();
 			
 			request.getSession().setAttribute(objectId, couponIds);
 			request.getSession().setAttribute(objectId2, coupons);
 			
 			request.setAttribute("objectId", objectId);
 			request.setAttribute("objectId2", objectId2);
 			request.setAttribute("coupons", coupons);
 			request.setAttribute("gift", gift);
 			
 			request.getRequestDispatcher("/page_checkOut.jsp").forward(request, response);
 		}
 	}
 
 }
