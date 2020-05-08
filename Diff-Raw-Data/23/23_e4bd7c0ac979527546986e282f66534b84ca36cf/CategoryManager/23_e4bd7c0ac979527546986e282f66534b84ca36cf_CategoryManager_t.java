 package com.charitybuzz.web.manager.controller;
 
 import java.util.List;
 
 import javax.annotation.Resource;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.HttpSessionRequiredException;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.charitybuzz.dto.Category;
import com.charitybuzz.dto.SubCategory;
 import com.charitybuzz.service.CategoryService;
import com.charitybuzz.service.SubCategoryService;
 import com.charitybuzz.web.manager.form.CategoryForm;
 
 @Controller
 @RequestMapping(value = "/manager/category")
 @SessionAttributes({ "operator" })
 public class CategoryManager {
 
 	/**
 	 * 第一級目錄
 	 */
 	@Resource
 	private CategoryService categoryService;
	/**
	 * 第二級目錄
	 */
	@Resource
	private SubCategoryService subCategoryService;
 	/**
 	 * 拿到列表
 	 * 
 	 * @param form
 	 * @param sessionObject
 	 * @return
 	 */
 	@RequestMapping(value = "/list")
 	public ModelAndView categoryList(CategoryForm form) {
 		ModelAndView mav = new ModelAndView("manager/category/list");
 		List<Category> categories = categoryService.findAll();
 		mav.addObject("categories", categories);
		

		for (int i = 0; i < categories.size(); i++) {
			Category category = categories.get(i);
			Long categoryId = category.getId();
			List<SubCategory> subCategories = subCategoryService
					.findByCategoryId(categoryId);
			category.setSubCategories(subCategories);
		}
 		return mav;
 	}
 
 	/**
 	 * 拿到add page
 	 * 
 	 * @param sessionObject
 	 * @return
 	 */
 	@RequestMapping(value = "/add", method = RequestMethod.GET)
 	public ModelAndView categoryAddPage() {
 		ModelAndView mav = new ModelAndView("manager/category/add");
 		return mav;
 	}
 
 	/**
 	 * 新增
 	 * 
 	 * @param sessionObject
 	 * @param form
 	 * @param result
 	 * @return
 	 */
 	@RequestMapping(value = "/add", method = RequestMethod.POST)
 	public ModelAndView categoryAdd(CategoryForm form, BindingResult result) {
 
 		if (result.hasErrors()) {
 		}
 
 		categoryService.insert(new Category(form.getName()));
 
 		ModelAndView mav = new ModelAndView(
 				"redirect:/manager/category/list.do");
 		return mav;
 	}
 
 	/**
 	 * 拿到update page
 	 * 
 	 * @param sessionObject
 	 * @param categoryId
 	 * @return
 	 */
 	@RequestMapping(value = "{categoryId}/update", method = RequestMethod.GET)
 	public ModelAndView categoryUpdatePage(@PathVariable Long categoryId) {
 		ModelAndView mav = new ModelAndView("manager/category/update");
 
 		Category category = categoryService.findById(categoryId);
 		mav.addObject("category", category);
 		return mav;
 	}
 
 	/**
 	 * update
 	 * 
 	 * @param sessionObject
 	 * @param categoryId
 	 * @return
 	 */
 	@RequestMapping(value = "{categoryId}/update", method = RequestMethod.POST)
 	public ModelAndView categoryUpdate(@PathVariable Long categoryId,
 			CategoryForm form, BindingResult result) {
 		if (result.hasErrors()) {
 		}
 
 		categoryService.update(new Category(form.getId(), form.getName()));
 
 		ModelAndView mav = new ModelAndView(
 				"redirect:/manager/category/list.do");
 		return mav;
 	}
 
 	@RequestMapping(value = "{categoryId}/delete", method = RequestMethod.GET)
 	public ModelAndView categoryDelete(@PathVariable Long categoryId,
 			CategoryForm form, BindingResult result) {
 		if (result.hasErrors()) {
 		}
 
		//先確認是否有subcategory
		
 		categoryService.delete(categoryId);
 
 		ModelAndView mav = new ModelAndView(
 				"redirect:/manager/category/list.do");
 		return mav;
 	}
 
 	@ExceptionHandler({ HttpSessionRequiredException.class })
 	public ModelAndView noSessionObject(Exception ex) {
 		return new ModelAndView("redirect:/manager/index.do");
 	}
 }
