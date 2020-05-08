 package com.omartech.tdg.action.admin;
 
 import java.util.List;
 
 import javax.servlet.http.HttpSession;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.omartech.tdg.mapper.ShopSettingMapper;
 import com.omartech.tdg.model.FinanceRecord;
 import com.omartech.tdg.model.Page;
 import com.omartech.tdg.model.Seller;
 import com.omartech.tdg.model.ShopSetting;
 import com.omartech.tdg.service.FinanceRecordService;
 import com.omartech.tdg.service.seller.SellerAuthService;
 import com.omartech.tdg.utils.UserType;
 
 @RequestMapping("/admin/financeRecord")
 @Controller
 public class AdminFinanceRecordAction {
 
 	@Autowired
 	private FinanceRecordService financeRecordService;
 	@Autowired
 	private ShopSettingMapper shopSettingMapper;
 	@Autowired
 	private SellerAuthService sellerAuthService;
 	
 	@RequestMapping("/list")
 	public ModelAndView list(
 			@RequestParam(value="pageNo", defaultValue= "0", required = false) int pageNo, 
 			@RequestParam(value="pageSize", defaultValue = "10", required = false) int pageSize
 			){
 		List<FinanceRecord> financeRecords = financeRecordService.getFinanceRecordsByPage(new Page(pageNo, pageSize));
 		return new ModelAndView("/admin/finance/record-list").addObject("financeRecords", financeRecords).addObject("pageNo", pageNo);
 	}
 	
 	@RequestMapping("/show/{id}")
 	public ModelAndView show(@PathVariable int id){
 		FinanceRecord record = financeRecordService.getFinanceRecordById(id);
 		String receiver = record.getReceiver();
 		String[] tmp = receiver.split("-");
 		ShopSetting shopSetting = null;
 		if(receiver.contains(UserType.SELLER)){
 			Seller seller = sellerAuthService.getSellerById(Integer.parseInt(tmp[1]));
 			shopSetting = shopSettingMapper.getShopSettingBySellerId(seller.getId());
 		}
 		return new ModelAndView("/admin/finance/record-show").addObject("financeRecord", record).addObject("shopSetting", shopSetting);
 	}
 	@RequestMapping(value = "/update")
 	public String update(
 			@RequestParam int id,
 			@RequestParam int status
 			){
 		financeRecordService.updateStatus(id, status);
		return "redirect:/admin/financeRecord/list";
 	}
 	
 }
