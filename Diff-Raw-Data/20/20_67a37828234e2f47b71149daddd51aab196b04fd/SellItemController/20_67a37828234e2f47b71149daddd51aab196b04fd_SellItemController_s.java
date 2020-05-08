 package com.sd.app.controller;
 
 import java.io.FileInputStream;
 
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 /*import net.sf.jasperreports.engine.JasperCompileManager;
 import net.sf.jasperreports.engine.JasperExportManager;
 import net.sf.jasperreports.engine.JasperFillManager;
 import net.sf.jasperreports.engine.JasperPrint;
 import net.sf.jasperreports.engine.JasperReport;
 import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
 import net.sf.jasperreports.engine.design.JasperDesign;
 import net.sf.jasperreports.engine.xml.JRXmlLoader;
 */
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.sd.app.bean.ItemBean;
 import com.sd.app.constraint.ItemAction;
 import com.sd.app.model.Item;
 import com.sd.app.model.ItemMaster;
 import com.sd.app.service.ItemService;
 
 @Controller
 public class SellItemController {
 
 	@Autowired
 	private ItemService itemService;
 	
 	@RequestMapping(value = { "/sellItem"}, method = RequestMethod.GET)
 	public ModelAndView welcomePage() {
 		System.out.println("Sell page..");	
 		ModelAndView model = new ModelAndView();
 		model.addObject("items",  prepareListofBean(itemService.listItems()));
 		model.setViewName("sellItem");
 		return model;
 
 	}
 	
 	@RequestMapping(value = { "/sellSaveItem"}, method = RequestMethod.POST)
 	public ModelAndView sellSaveItem(HttpServletRequest request) {
 		System.out.println("Sell save page..");	
 		Enumeration<String> attributeNames = request.getParameterNames();
 		ArrayList<Item> items = new ArrayList<Item>();
 		
 		while(attributeNames.hasMoreElements()){
 			String nextElement = attributeNames.nextElement();
 			String[] parameterValues = request.getParameterValues(nextElement);
 			Item item = null;
 			for(int i=0; i<parameterValues.length;i++){
 				if(items.size() != parameterValues.length){
 					item = new Item();
 				}else{
 					item = items.get(i);
 				}
 				if("items".equalsIgnoreCase(nextElement)){
 					item.setId(Integer.parseInt(parameterValues[i]));
 				}else if("height".equalsIgnoreCase(nextElement)){
 					item.setHeight(parameterValues[i]);
 				}else if("width".equalsIgnoreCase(nextElement)){
 					item.setWidth(parameterValues[i]);
 				}else if("thikness".equalsIgnoreCase(nextElement)){
 					item.setThikness(parameterValues[i]);
 				}else if("quantity".equalsIgnoreCase(nextElement)){
 					item.setQuantity(parameterValues[i]);
 				}else if("price".equalsIgnoreCase(nextElement)){
					item.setPrice(Double.parseDouble(parameterValues[i]));
 				}else if("subTotal".equalsIgnoreCase(nextElement)){
					item.setSubTotal(Double.parseDouble(parameterValues[i]));
 				}
 			}
 			items.add(item);
 			
 		}
 		
 		ItemMaster itemMaster = new ItemMaster();
 		itemMaster.setCustomerName(request.getParameter("custName"));
 		itemMaster.setContactNo(request.getParameter("contactNo"));
 		itemMaster.setItemAction(ItemAction.SELL.toString());
		itemMaster.setTotalAmount(Double.parseDouble(request.getParameter("subTotal")));
 		itemMaster.setItems(items);
 		
 		ModelAndView model = new ModelAndView();
 		model.addObject("items",  prepareListofBean(itemService.listItems()));
 		itemService.saveSellItems(itemMaster);
 		List<ItemMaster> listSellItems = itemService.listSellItems();
 		model.addObject("listSellItems",listSellItems);
 		model.setViewName("sellItem");
 		return model;
 
 	}
 	private List<ItemBean> prepareListofBean(List<Item> items){
 		List<ItemBean> beans = null;
 		if(items != null && !items.isEmpty()){
 			beans = new ArrayList<ItemBean>();
 			ItemBean bean = null;
 			for(Item item : items){
 				bean = new ItemBean();
 				
 				bean.setId(item.getId());
 				bean.setName(item.getName());
 				bean.setHeight(item.getHeight());
 				bean.setWidth(item.getWidth());
 				bean.setThikness(item.getThikness());
 				bean.setBuyFrom(item.getBuyFrom());
 				bean.setQuantity(item.getQuantity());
 				bean.setPrice(item.getPrice() != null?item.getPrice()+"":"0.0");
 				bean.setDescription(item.getDescription());
 
 				beans.add(bean);
 			}
 		}
 		return beans;
 	}
 /*
 	public static void main(String[] args) throws Exception {
 		new SellItemController().printPDF();
 	}
 	
 	void printPDF(){
 		
 try{
 		InputStream inputStream = new FileInputStream("C:/Users/mohammad.ahmed/Desktop/Quatation.jrxml");
 		ItemMaster itemMaster = new ItemMaster();
 		itemMaster.setContactNo("9999999999999");
 		itemMaster.setCustomerName("test this name");
 		itemMaster.setItemAction("Sell");
 		itemMaster.setTotalAmount(50000.0);
 		ArrayList<Item> item = new ArrayList<Item>();
 		Item item1 = new Item();
 		item1.setId(1);
 		item1.setPrice(700.0);
 		item1.setQuantity("3");
 		item1.setSubTotal(400.00);
 		item1.setName("metal");
 		
 		Item item2 = new Item();
 		item2.setId(1);
 		item2.setPrice(700.0);
 		item2.setQuantity("3");
 		item2.setSubTotal(400.00);
 		item2.setName("metal");
 
 		
 		Item item3 = new Item();
 		
 		item3.setId(1);
 		item3.setPrice(700.0);
 		item3.setQuantity("3");
 		item3.setSubTotal(400.00);
 		item3.setName("metal");
 		for(int i=0; i<5; i++){
 		item.add(item1);
 		item.add(item2);
 		item.add(item3);
 		}
 		itemMaster.setItems(item);
 		
 		List<Item> items = itemMaster.getItems();
 		ArrayList<ItemMaster> itemMasters = new ArrayList<ItemMaster>();
 		itemMasters.add(itemMaster);
 		JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(itemMasters);
 		Map parameters = new HashMap();
 	
 		JasperDesign jasperDesign = JRXmlLoader.load(inputStream);
 		JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
 		JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, beanColDataSource);
 		JasperExportManager.exportReportToPdfFile(jasperPrint,	"c:/TestApp/test_jasper"+System.currentTimeMillis()+".pdf");
 }catch(Exception e){
 	e.printStackTrace();
 }
 	
 		
 	}
 	
 	*/
 
 }
