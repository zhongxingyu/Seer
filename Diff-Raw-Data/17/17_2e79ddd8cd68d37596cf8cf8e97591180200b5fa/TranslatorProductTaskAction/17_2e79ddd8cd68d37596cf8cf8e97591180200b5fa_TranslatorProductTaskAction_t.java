 package com.omartech.tdg.action.translator;
 
 import java.util.List;
 
 import javax.servlet.http.HttpSession;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.omartech.tdg.model.Brand;
 import com.omartech.tdg.model.Page;
 import com.omartech.tdg.model.Product;
 import com.omartech.tdg.model.TranslationTask;
 import com.omartech.tdg.model.Translator;
 import com.omartech.tdg.service.BrandService;
 import com.omartech.tdg.service.ProductService;
 import com.omartech.tdg.service.TranslationTaskService;
 import com.omartech.tdg.utils.ProductStatus;
 import com.omartech.tdg.utils.TaskStatus;
 import com.omartech.tdg.utils.TaskType;
 
 @RequestMapping("/translator/task")
 @Controller
 public class TranslatorProductTaskAction {
 
 	@Autowired
 	private TranslationTaskService translationTaskService;
 	
 	@Autowired
 	private ProductService productService;
 	
 	@Autowired
 	private BrandService brandService;
 	
 	@RequestMapping("/list")
 	public ModelAndView listTasks(@RequestParam(value="pageNo", defaultValue = "0", required = false) int pageNo,
 			@RequestParam(value="pageSize", defaultValue = "10", required = false) int pageSize, HttpSession session,
 			@RequestParam String taskType){
 		
 		Translator translator = (Translator) session.getAttribute("translator");
 		int translatorId = translator.getId();
 		
 		Page page = new Page(pageNo, pageSize);
 		List<TranslationTask> tasks = translationTaskService.getTasksByTypeAndPage(translatorId, taskType, page);
 		return new ModelAndView("/translator/task/task-list").addObject("tasks",tasks).addObject("pageNo", pageNo);
 	}
 	
 	@RequestMapping("/edit")
 	public ModelAndView editTask(@RequestParam int taskId, @RequestParam String taskType){
 		TranslationTask task = translationTaskService.getTranslationTaskById(taskId);
 		return new ModelAndView("/translator/task/task-edit").addObject("task", task);
 	}
 	
 	@RequestMapping(value = "/update", method = RequestMethod.POST)
 	public String updateTask(@RequestParam String taskType, @RequestParam String nameInChinese, @RequestParam String descriptionInChinese, @RequestParam int id, @RequestParam int taskId){
 		if(taskType.equals(TaskType.PRODUCT)){
 			Product product = productService.getProductById(id);
 			product.setNameInChinese(nameInChinese);
 			product.setDescriptionInChinese(descriptionInChinese);
 			product.setStatus(ProductStatus.TranslationComplete);
 			productService.translateProduct(product);
 		}else if(taskType.equals(TaskType.BRAND)){
 			Brand brand = brandService.getBrandById(id);
 			brand.setNameInChinese(nameInChinese);
 			brand.setDescriptionInChinese(descriptionInChinese);
 			brand.setStatus(ProductStatus.TranslationComplete);
			brandService.simpleUpdateBrand(brand);
 		}
 		translationTaskService.updateTranslationTaskStatus(taskId, TaskStatus.OK);
 		return "redirect:/translator/task/list?taskType="+taskType;
 	}
 
 	public TranslationTaskService getTranslationTaskService() {
 		return translationTaskService;
 	}
 
 	public void setTranslationTaskService(
 			TranslationTaskService translationTaskService) {
 		this.translationTaskService = translationTaskService;
 	}
 
 	public ProductService getProductService() {
 		return productService;
 	}
 
 	public void setProductService(ProductService productService) {
 		this.productService = productService;
 	}
 
 	public BrandService getBrandService() {
 		return brandService;
 	}
 
 	public void setBrandService(BrandService brandService) {
 		this.brandService = brandService;
 	}
 	
 }
