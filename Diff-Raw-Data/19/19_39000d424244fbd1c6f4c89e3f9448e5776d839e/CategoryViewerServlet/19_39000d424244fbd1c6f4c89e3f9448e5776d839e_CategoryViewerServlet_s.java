 package com.epam.lab.buyit.controller.web.servlet;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.epam.lab.buyit.controller.service.category.CategoryServiceImpl;
import com.epam.lab.buyit.controller.service.product.ProductServiceImpl;
import com.epam.lab.buyit.controller.service.subcategory.SubCategoryServiceImpl;
 import com.epam.lab.buyit.model.Category;
import com.epam.lab.buyit.model.SubCategory;
 
 public class CategoryViewerServlet extends HttpServlet {
	private static final int ITEMS_ON_PAGE = 8;
	private SubCategoryServiceImpl subCategoryServce;
	private ProductServiceImpl productService;
 	private CategoryServiceImpl categoryService;
 
 	public void init() {
		subCategoryServce = new SubCategoryServiceImpl();
		productService = new ProductServiceImpl();
 		categoryService = new CategoryServiceImpl();
 	}
 
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		int category_id =Integer.parseInt(request.getParameter("id")); 
		int page = 1;
		
 		
 		Category category = categoryService.getNotClosedById(category_id, 4);
 		
 		request.setAttribute("category", category);
 		request.getRequestDispatcher("categoryViewerPage").forward(request, response);
 	}
 
 
 
 }
