 package com.tda.presentation.controller;
 
 import javax.validation.Valid;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.tda.model.item.Category;
 import com.tda.model.item.Item;
 import com.tda.model.item.MeasureUnit;
 import com.tda.persistence.paginator.Paginator;
 import com.tda.service.api.ItemService;
 
 @Controller
 @RequestMapping(value = "/item")
 @SessionAttributes("item")
 public class ItemController {
 
 	private static final String ITEM_FORM_DELETE_ERROR = "item.form.deleteError";
 	private static final String ITEM_FORM_NOT_FOUND = "item.form.notFound";
 	private static final String ITEM_FORM_MESSAGE = "message";
 	private static final String ITEM_FORM_ADD_SUCCESSFUL = "item.form.addSuccessful";
 	private static final String REDIRECT_TO_ITEM_LIST = "redirect:/item";
 	private static final String ITEM_CREATE_FORM = "item/createForm";
 	private static final String ITEM_LIST = "item/list";
 	private ItemService itemService;
 	private Paginator paginator;
 
 	@Autowired
 	public void setItemService(ItemService itemService) {
 		this.itemService = itemService;
 	}
 
 	@Autowired
 	public void setPaginator(Paginator paginator) {
 		this.paginator = paginator;
 	}
 
 	@ModelAttribute("categories")
 	public Category[] populateCategories() {
 		return Category.values();
 	}
 
 	@ModelAttribute("measureUnits")
 	public MeasureUnit[] populateMeasureUnits() {
 		return MeasureUnit.values();
 	}
 
 	@RequestMapping(value = "/add", method = RequestMethod.GET)
 	public String getCreateForm(Model model) {
 		model.addAttribute("item", new Item());
 
 		return ITEM_CREATE_FORM;
 	}
 
 	@RequestMapping(method = RequestMethod.POST)
 	public ModelAndView create(Model model, @Valid @ModelAttribute Item anItem,
 			BindingResult result) {
 		ModelAndView modelAndView = new ModelAndView();
 
 		// TODO if we're editing and not adding a new item the message
 		// seems somewhat... misleading, CHANGE IT :D
 		if (result.hasErrors()) {
 			modelAndView.setViewName(ITEM_CREATE_FORM);
 		} else {
 			modelAndView.setViewName(REDIRECT_TO_ITEM_LIST);
 			modelAndView.addObject(ITEM_FORM_MESSAGE, ITEM_FORM_ADD_SUCCESSFUL);
 			itemService.save(anItem);
 		}
 
 		return modelAndView;
 	}
 
 	@RequestMapping(value = "/edit/{id}", method = RequestMethod.GET)
 	public String getUpdateForm(@PathVariable Long id, Model model) {
 		Item anItem = itemService.findById(id);
 		model.addAttribute("item", anItem);
 
 		return ITEM_CREATE_FORM;
 	}
 
 	@RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
 	public ModelAndView deleteItem(@PathVariable Long id) {
 		ModelAndView modelAndView = new ModelAndView(REDIRECT_TO_ITEM_LIST);
 		Item anItem = itemService.findById(id);
 
 		if (anItem == null) {
 			modelAndView.addObject(ITEM_FORM_MESSAGE, ITEM_FORM_NOT_FOUND);
 		} else {
 
 			try {
 				itemService.delete(anItem);
 			} catch (Exception e) {
 				modelAndView.addObject(ITEM_FORM_MESSAGE,
 						ITEM_FORM_DELETE_ERROR);
 			}
 		}
 		return modelAndView;
 	}
 
 	@RequestMapping(method = RequestMethod.GET)
 	public ModelAndView getList(
 			@RequestParam(value = "page", required = false) Integer pageNumber) {
 		ModelAndView modelAndView = new ModelAndView(ITEM_LIST);
 
 		if (pageNumber != null) {
 			paginator.setPageIndex(pageNumber);
 		}
 
 		modelAndView.addObject("itemList", itemService.findAllPaged(paginator));
 		modelAndView.addObject("paginator", paginator);
 
 		return modelAndView;
 	}
 }
