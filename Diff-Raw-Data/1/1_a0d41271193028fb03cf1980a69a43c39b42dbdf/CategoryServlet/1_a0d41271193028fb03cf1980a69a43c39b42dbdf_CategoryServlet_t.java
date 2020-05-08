 package com.epam.lab.buyit.controller.web.servlet;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.epam.lab.buyit.controller.service.product.ProductServiceImpl;
 import com.epam.lab.buyit.controller.service.subcategory.SubCategoryServiceImpl;
 import com.epam.lab.buyit.model.SubCategory;
 
 public class CategoryServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private static final int ITEMS_ON_PAGE = 8;
 	private SubCategoryServiceImpl subCategoryServce;
 	private ProductServiceImpl productService;
 
 	public void init() {
 		subCategoryServce = new SubCategoryServiceImpl();
 		productService = new ProductServiceImpl();
 	}
 
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		
 		int subCategory_id = 0;
 		if (request.getParameter("id") != null)
 			subCategory_id = Integer.parseInt(request.getParameter("id"));
 		
 		int page = 1;
 		if(request.getParameter("page") != null) 
 			page = Integer.parseInt(request.getParameter("page"));
 		
 		SubCategory subCategory = subCategoryServce.getWithProductSelection(
 				subCategory_id, (page-1) * ITEMS_ON_PAGE, ITEMS_ON_PAGE);
 		
 		int numberOfRecords = productService.getCountBySubCategoryId(subCategory_id);
 		int numberOfPages = (int) Math.ceil(numberOfRecords * 1.0 / ITEMS_ON_PAGE);
 		
		request.setAttribute("categoryId", request.getParameter("categoryId"));
 		request.setAttribute("subCategory", subCategory);
 		request.setAttribute("noOfPages", numberOfPages);
 		request.setAttribute("page", page);
 		request.getRequestDispatcher("category").forward(request, response);
 	}
 
 
 }
