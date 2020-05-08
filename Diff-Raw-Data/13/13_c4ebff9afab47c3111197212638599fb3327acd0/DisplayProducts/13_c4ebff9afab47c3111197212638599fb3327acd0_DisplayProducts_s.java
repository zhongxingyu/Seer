 ﻿package product.controller;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import product.model.ProductDAO;
 import product.model.ProductVO;
 
 /**
  * Servlet implementation class DisplayProducts
  */
 public class DisplayProducts extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public DisplayProducts() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		doPost(request, response);
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		request.setCharacterEncoding("UTF-8");
 		String action = request.getParameter("action");
 		
 		if ("getAll".equals(action)){
 			ProductDAO productDAO = new ProductDAO();
 			List<ProductVO> list = productDAO.getAll();
 			request.setAttribute("list", list);
 			
 			splitPages(list, request);
 			
 			String listAllUrl = "/product/ProductList.jsp";
 			RequestDispatcher rd = request.getRequestDispatcher(listAllUrl);
 			rd.forward(request, response);
 			return;
 		}
 		
 		if ("getOne_For_Display".equals(action)) {
 			Integer p_no = Integer.parseInt(request.getParameter("pId"));
 			ProductDAO productDAO = new ProductDAO();
 			ProductVO productVO = productDAO.findByPrimaryKey(p_no);
 			
 			request.setAttribute("productVO", productVO);
 			String listAllUrl = "/product/ProductOne.jsp";
 			RequestDispatcher rd = request.getRequestDispatcher(listAllUrl);
 			rd.forward(request, response);
 			return;
 		}
 		
 //		Add getSome_For_Display for where xxx=? search. DAO modification is needed. 
 		//JoJo 3/1/2012
 		if ("getSome_For_Display".equals(action)) {
 			String condition = request.getParameter("condition");
 			String conditionValue = request.getParameter("conditionValue");
 			conditionValue=new String(conditionValue.getBytes("ISO-8859-1"),"UTF-8");
 			ProductDAO productDAO = new ProductDAO();
 			System.out.println(conditionValue);
 			List<ProductVO> list = productDAO.findSomeProduct(condition,conditionValue);
 			
 			request.setAttribute("list", list);
 			
 			splitPages(list, request);
 			
 			String listAllUrl = "/product/ProductList.jsp";
 			RequestDispatcher rd = request.getRequestDispatcher(listAllUrl);
 			rd.forward(request, response);
 			return;
 		}
 		
 	}
 	
 	private void splitPages(List<ProductVO> list, HttpServletRequest request) {
 		int rowsPerPage = 5;  //每頁的筆數    
 	    int totalRows=list.size();      //總筆數
 	    int totalPages=0;     //總頁數      
 	    int whichPage=1;      //第幾頁
 	    int pageIndexArray[]=null;
 	    int pageIndex=0; 
 		
 	    if ((totalRows % rowsPerPage) != 0)
 	    	totalPages = totalRows / rowsPerPage +1;
 	    else
 	    	totalPages = totalRows / rowsPerPage;
 	    
 	    pageIndexArray = new int[totalPages];
 	    for (int i=0; i<totalPages; i++)
 	    	 pageIndexArray[i] = rowsPerPage * i;
 	    
 	    try{
 	    	whichPage = Integer.parseInt(request.getParameter("pageNo"));
 	    	pageIndex = pageIndexArray[whichPage - 1];
 	    } catch (NumberFormatException e){
 	    	whichPage = 1;
 	    	pageIndex = 0;
 	    } catch (ArrayIndexOutOfBoundsException e){
 	    	if (totalPages > 0){
 	    		whichPage = totalPages;
 	    		pageIndex = pageIndexArray[whichPage - 1];
 	    	}
 	    }
 	    
 	    request.setAttribute("whichPage", whichPage);
 	    request.setAttribute("pageIndex", pageIndex);
 	    request.setAttribute("totalPages", totalPages);
 	    request.setAttribute("totalRows", totalRows);
 	    request.setAttribute("rowsPerPage", rowsPerPage);
 	}
 
 }
