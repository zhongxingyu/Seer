 package com.epam.lab.buyit.controller.web.servlet;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.epam.lab.buyit.controller.service.category.CategoryServiceImpl;
 import com.epam.lab.buyit.model.Category;
 
 public class CategoryViewerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
 	private CategoryServiceImpl categoryService;
 
 	public void init() {
 		categoryService = new CategoryServiceImpl();
 	}
 
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		int category_id =Integer.parseInt(request.getParameter("id")); 
 		
 		Category category = categoryService.getNotClosedById(category_id, 4);
 		
 		request.setAttribute("category", category);
		request.setAttribute("categoryId", category.getIdCategory());
 		request.getRequestDispatcher("categoryViewerPage").forward(request, response);
 	}
 
 
 
 }
