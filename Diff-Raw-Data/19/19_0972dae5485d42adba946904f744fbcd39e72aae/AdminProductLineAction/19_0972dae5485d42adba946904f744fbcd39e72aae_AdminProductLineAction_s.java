 package com.omartech.tdg.action.admin;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.omartech.tdg.model.ProductLine;
 import com.omartech.tdg.service.ProductLineService;
 @RequestMapping("/admin/productLine")
 @Controller
 public class AdminProductLineAction {
 	@Autowired
 	private ProductLineService productLineService;
 	@RequestMapping("/list")
 	public ModelAndView listProductLines(
 			@RequestParam(value="pageNo", defaultValue= "0", required = false) int pageNo,
 			@RequestParam(value="pageSize", defaultValue = "10", required = false) int pageSize){
 		List<ProductLine> productLines = productLineService.getProductLinesByParentId(0);
 		return new ModelAndView("/admin/productLine/list")
 			.addObject("productLines", productLines)
 			.addObject("pageNo", pageNo);
 	}
 	@RequestMapping("/new")
 	public ModelAndView newProductLine(@RequestParam(value="parentId", defaultValue= "0", required = false) int parentId){
 		ProductLine parent = productLineService.getProductLineById(parentId);
 		return new ModelAndView("/admin/productLine/new").addObject("parent", parent);
 	}
 	
 	@RequestMapping("/create")
 	public ModelAndView createProductLine(@RequestParam String name, 
 			@RequestParam String english, 
 			@RequestParam int parentId, 
 			@RequestParam int level,
			@RequestParam String discount,
			@RequestParam String commission){
 		ProductLine productLine = new ProductLine();
 		productLine.setName(name);
 		productLine.setEnglish(english);
 		productLine.setLevel(level);
 		productLine.setParentId(parentId);
 		float d = 0;//总有傻逼非要填其他的
 		float c = 0;//总有傻逼非要填其他的
 		try{
 			d = Float.parseFloat(discount);
 			if(d < 0 || d > 1){
 				d = 0;
 			}
 			c = Float.parseFloat(commission);
 			if(c < 0 || c > 1){
 				c = 0;
 			}
 		}catch(Exception e){
 			
 		}
 		productLine.setDiscount(d);
 		productLine.setCommission(c);
 		productLineService.insertProductLine(productLine);
 		return listProductLines(0, 10).addObject("message", "添加类别成功");
 	}
 	
 	@RequestMapping("/edit")
 	public ModelAndView editProductLine(@RequestParam int id){
 		ProductLine productLine = productLineService.getProductLineById(id);
 		return new ModelAndView("/admin/productLine/edit").addObject("productLine", productLine);
 	}
 	
 	@RequestMapping("/update")
 	public String updateProductLine(@RequestParam String name, 
 			@RequestParam int id,
 			@RequestParam String english, 
 			@RequestParam int parentId, 
 			@RequestParam int level,
			@RequestParam String discount,
			@RequestParam String commission){
 		ProductLine productLine = productLineService.getProductLineById(id);
 		productLine.setName(name);
 		productLine.setEnglish(english);
 		productLine.setLevel(level);
 		productLine.setParentId(parentId);
 		float d = 0;//总有傻逼非要填其他的
 		float c = 0;//总有傻逼非要填其他的
 		try{
 			d = Float.parseFloat(discount);
 			if(d < 0 || d > 1){
 				d = 0;
 			}
 			c = Float.parseFloat(commission);
 			if(c < 0 || c > 1){
 				c = 0;
 			}
 		}catch(Exception e){
 			
 		}
 		productLine.setDiscount(d);
 		productLine.setCommission(c);
 		productLineService.updateProductLine(productLine);
 		return "redirect:/admin/productLine/list";
 	}
 	
 	public ProductLineService getProductLineService() {
 		return productLineService;
 	}
 	public void setProductLineService(ProductLineService productLineService) {
 		this.productLineService = productLineService;
 	}
 	
 }
