 package com.bgg.farmstoryback.controller;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.multipart.MultipartFile;
 
 import com.bgg.farmstoryback.common.ConstantsForDb;
 import com.bgg.farmstoryback.common.ConstantsForParam;
 import com.bgg.farmstoryback.common.ConstantsForResponse;
 import com.bgg.farmstoryback.common.FileUtil;
 import com.bgg.farmstoryback.service.CategoryService;
 import com.bgg.farmstoryback.service.DisplayService;
 
 
 @Controller
 public class DisplayController {
 	
 	private Logger logger = LoggerFactory.getLogger(DisplayController.class);
 	
 	@Autowired
 	private FileUtil fileUtil;
 	
 	@Autowired
 	private DisplayService displayService;
 	
 	@Autowired
 	private CategoryService categoryService;
 	
 	@RequestMapping(value = "display/main/manageView.do")
 	public String manageView(Model model, @RequestParam Map<String,Object> parameter) {
 		
 		Map map = displayService.listInfo();
 		model.addAttribute(ConstantsForResponse.TOP_DISPLAY, map.get(ConstantsForResponse.TOP_DISPLAY));
 		model.addAttribute(ConstantsForResponse.BANNER_DISPLAY, map.get(ConstantsForResponse.BANNER_DISPLAY));
 		
 		return "display/main";
 	}
 	
 	@RequestMapping(value = "display/main/createView.do")
 	public String createView(Model model, @RequestParam Map<String,Object> parameter) {
 		return "display/mainCreate";
 	}
 	
 	@RequestMapping(value = "display/main/create.do")
 	public String create(Model model, @RequestParam Map<String,Object> parameter) {
 		
 		displayService.add(parameter);
 		
 		return "redirect:manageView.do";
 	}
 	
 	@RequestMapping(value = "display/main/updateView.do")
 	public String updateView(Model model, @RequestParam Map<String,Object> parameter) {
 		model.addAttribute("displayInfo", displayService.detail(parameter));
 		return "display/mainUpdate";
 	}
 	
 	@RequestMapping(value = "display/main/update.do")
 	public String update(Model model, @RequestParam Map<String,Object> parameter) {
 		
 		displayService.modify(parameter);
 		
 		return "redirect:manageView.do";
 	}
 	
 	@RequestMapping(value = "display/main/delete.do")
 	public String delete(Model model, @RequestParam Map<String,Object> parameter) {
 		
 		displayService.delete(parameter);
 		
 		return "redirect:manageView.do";
 	}
 	
 	@RequestMapping(value = "display/main/bannerCreateView.do")
 	public String bannerCreateView(Model model, @RequestParam Map<String,Object> parameter) {
 		return "display/bannerCreate";
 	}
 	
 	@RequestMapping(value = "display/main/bannerCreate.do")
 	public String bannerCreate(Model model, @RequestParam Map<String,Object> parameter) {
 		
 		displayService.add(parameter);
 		
 		return "redirect:manageView.do";
 	}
 	
 	@RequestMapping(value = "display/main/bannerUpdateView.do")
 	public String bannerUpdateView(Model model, @RequestParam Map<String,Object> parameter) {
 		
 		model.addAttribute("bannerInfo", displayService.detail(parameter));
 		return "display/bannerUpdate";
 	}
 	
 	@RequestMapping(value = "display/main/bannerUpdate.do")
 	public String bannerUpdate(Model model, @RequestParam Map<String,Object> parameter) {
 		displayService.modify(parameter);
 		return "redirect:manageView.do";
 	}
 	
 	@RequestMapping(value = "display/main/bannerDelete.do")
 	public String bannerDelete(Model model, @RequestParam Map<String,Object> parameter) {
 		displayService.delete(parameter);
 		return "redirect:manageView.do";
 	}
 	
 	@RequestMapping(value = "display/contents/manageView.do")
 	public String contentsManageView(Model model, @RequestParam Map<String,Object> parameter) {
 		
 		model.addAttribute("parameter", parameter);
 		
 		List<Map> categoryList = categoryService.list();
 		model.addAttribute("categories", categoryList);
 
 		Object obj = parameter.get(ConstantsForParam.CATEGORY_ID);
 		
 		if(obj == null){
			if(categoryList.size() > 0){
				Map cate = categoryList.get(0);
				parameter.put(ConstantsForParam.CATEGORY_ID, String.valueOf(cate.get(ConstantsForDb.CATEGORY_ID)));
			}
 		}
 		
		model.addAttribute("contents", displayService.contentsList(parameter));
 		return "display/contents";
 	}
 	@RequestMapping(value = "display/contents/orderingUpdate.do")
 	public String orderingUpdate(Model model, @RequestParam Map<String,Object> parameter) {
 		
 		String cateId = String.valueOf(parameter.get(ConstantsForParam.CATEGORY_ID));
 		
 		String[] contentsIdList = String.valueOf(parameter.get(ConstantsForParam.CONTENTS_ID)).split("&");
 		String[] orderingNoList = String.valueOf(parameter.get(ConstantsForParam.ORDERING_NO)).split("&");
 		
 		int contentsIdCount = contentsIdList.length;
 		for(int idx = 0; idx < contentsIdCount; idx++) {
 			Map<String, Object> paramMap = new HashMap<String, Object>();
 			paramMap.put(ConstantsForParam.CATEGORY_ID, cateId);
 			paramMap.put(ConstantsForParam.CONTENTS_ID, contentsIdList[idx]);
 			paramMap.put(ConstantsForParam.ORDERING_NO, orderingNoList[idx]);
 			displayService.modifyContentsOrderingNo(paramMap);
 		}
 		
 		return String.format("redirect:manageView.do?category_id=%s", cateId);
 	}
 	
 	@RequestMapping(value = "display/popup/manageView.do")
 	public String popupManageView(Model model, @RequestParam Map<String,Object> parameter) {
 		
 		model.addAttribute("popupList", displayService.popupList());
 		return "display/popup";
 	}
 	
 	@RequestMapping(value = "display/popup/createView.do")
 	public String popupCreateView(Model model, @RequestParam Map<String,Object> parameter) {
 		return "display/popupCreate";
 	}
 	
 	@RequestMapping(value = "display/popup/create.do")
 	public String popupCreate(Model model, @RequestParam Map<String,Object> parameter) {
 		
 		displayService.add(parameter);
 		
 		return "redirect:manageView.do";
 	}
 	
 	@RequestMapping(value = "display/popup/updateView.do")
 	public String popupUpdateView(Model model, @RequestParam Map<String,Object> parameter) {
 		model.addAttribute("obj", displayService.popupDetail(parameter));
 		return "display/popupUpdate";
 	}
 	
 	@RequestMapping(value = "display/popup/update.do")
 	public String popupUpdate(Model model, @RequestParam Map<String,Object> parameter) {
 		
 		displayService.modify(parameter);
 		
 		return "redirect:manageView.do";
 	}
 	
 	@RequestMapping(value = "display/popup/delete.do")
 	public String popupDelete(Model model, @RequestParam Map<String,Object> parameter) {
 		
 		displayService.delete(parameter);
 		
 		return "redirect:manageView.do";
 	}
 	
 	
 }
