 package com.snda.youni.taskweb.controllers;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.snda.iyouni.icommon.spring.BeanLocator;
 import com.snda.youni.taskweb.beans.CategoryObject;
 import com.snda.youni.taskweb.daos.CategoryDAO;
 import com.snda.youni.taskweb.daos.CategorygroupDAO;
 import com.snda.youni.taskweb.util.RequestParameters;
 
 @Controller
 @RequestMapping("/category")
 public class CategoryController {
 
 	@RequestMapping(value="/grouplist")
 	public ModelAndView grouplist(HttpServletRequest request,HttpServletResponse response){
 		
 		CategorygroupDAO dao = BeanLocator.getBean("categorygroupDAO");
 		List<CategoryObject> list = dao.query();
 		
 		ModelAndView mav =  new ModelAndView("categorygrouplist");
 		mav.addObject("categorygrouplist", list);
 		return mav;
 	}
 	
 	@RequestMapping(value="/groupsave")
 	public ModelAndView groupsave(HttpServletRequest request,HttpServletResponse response){
 		
 		int cg_id = RequestParameters.getInt(request, "cg_id", 0);
 		String cg_name = request.getParameter("cg_name");
 		
 		CategoryObject obj = new CategoryObject();
 		obj.setId(cg_id);
 		obj.setName(cg_name);
 		
 		CategorygroupDAO dao = BeanLocator.getBean("categorygroupDAO");
 		dao.save(obj);
 		
 		try {
 			response.sendRedirect("/category/grouplist");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	@RequestMapping(value="/groupdelete/{id}")
 	public ModelAndView groupdelete(@PathVariable int id,HttpServletRequest request,HttpServletResponse response){
 		
 		CategorygroupDAO dao = BeanLocator.getBean("categorygroupDAO");
 		dao.delete(id);
 		
 		try {
 			response.sendRedirect("/category/grouplist");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	@RequestMapping(value="/list")
 	public ModelAndView list(HttpServletRequest request,HttpServletResponse response){
 		
 		CategorygroupDAO cgDAO = BeanLocator.getBean("categorygroupDAO");
 		List<CategoryObject> cglist = cgDAO.query();
 		
 		CategoryDAO dao = BeanLocator.getBean("categoryDAO");
 		List<CategoryObject> list = dao.query();
 		
 		ModelAndView mav =  new ModelAndView("categorylist");
 		mav.addObject("categorylist", list);
 		mav.addObject("categorygrouplist", cglist);
 		return mav;
 	}
 	
 	@RequestMapping(value="/save")
 	public ModelAndView save(HttpServletRequest request,HttpServletResponse response){
 		int category_id = RequestParameters.getInt(request, "category_id", 0);
 		int categorygroup_id = RequestParameters.getInt(request, "categorygroup_id", 0);
 		String category_name = request.getParameter("category_name");
 		
 		CategoryObject obj = new CategoryObject();
 		obj.setId(category_id);
 		obj.setName(category_name);
 		obj.setGroupId(categorygroup_id);
 		
 		CategoryDAO dao = BeanLocator.getBean("categoryDAO");
 		dao.save(obj);
 		
 		try {
 			response.sendRedirect("/category/list");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	@RequestMapping(value="/delete/{id}")
 	public ModelAndView delete(@PathVariable int id,HttpServletRequest request,HttpServletResponse response){
 		
 		CategoryDAO dao = BeanLocator.getBean("categoryDAO");
 		dao.delete(id);
 		
 		try {
 			response.sendRedirect("/category/list");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	@RequestMapping(value="/json")
 	public ModelAndView json(HttpServletRequest request,HttpServletResponse response){
 		
 		int categorygroup_id = RequestParameters.getInt(request, "categorygroup_id", 0);
 		
 		CategoryDAO dao = BeanLocator.getBean("categoryDAO");
 		List<CategoryObject> list = dao.queryByCategorygroupId(categorygroup_id);
 		
 		ObjectMapper mapper = new ObjectMapper();
 		try {
 			response.setCharacterEncoding("utf-8");
 			OutputStream os = response.getOutputStream();
 			mapper.writeValue(os, list);
 			os.flush();
 			
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return null;
 	}
 	
 	
 }
