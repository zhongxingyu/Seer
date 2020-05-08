 package ua.krem.agent.mvc;
 
 
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.servlet.http.HttpSession;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 
 import ua.krem.agent.model.Brand;
 import ua.krem.agent.model.Code;
 import ua.krem.agent.model.Document;
 import ua.krem.agent.model.Filter;
 import ua.krem.agent.model.Item;
 import ua.krem.agent.model.Product;
 import ua.krem.agent.model.Shop;
 import ua.krem.agent.model.User;
 import ua.krem.agent.service.ProductService;
 import ua.krem.agent.service.ShopService;
 
 @Controller
 public class MainController {
 
 	private ShopService shopService;
 	private ProductService productService;
 	
 	@Inject
 	public MainController(ShopService shopService, ProductService productService){
 		this.shopService = shopService;
 		this.productService = productService;
 	}
 	
 	@RequestMapping(value="/tasks_with_tp", method = RequestMethod.GET)
 	public String taskWithTp(){
 		return "tasks_with_tp";
 	}
 
 	@RequestMapping(value="/choose_by_code", method = RequestMethod.GET)
 	public String chooseByCode(){
 		return "choose_by_code";
 	}
 
 	@RequestMapping(value="/procDoc", method = RequestMethod.POST)
 	public String procDoc(@ModelAttribute("atribute") Document doc, HttpSession session){
 		session.removeAttribute("itemList");
 		Shop shop = (Shop)session.getAttribute("shop");
 		User user = (User)session.getAttribute("user");
 		if(user != null && shop != null){
 			doc.setUserId(user.getId());
 			doc.setShopId(shop.getId());
 		}
 		
 		productService.addDocument(doc);
 		
		return "choose_doc_type";
 	}
 	
 	
 	@RequestMapping(value="/choose_by_code", method = RequestMethod.POST)
 	public ModelAndView chooseTpByCode(@ModelAttribute("atribute") Code code, HttpSession session){
 		Shop shop = shopService.getShopByCode(code.getCode());
 		System.out.println("[shop] " + shop);
 		System.out.println(code.getCode());
 		ModelAndView model;
 		if(shop.getName() != null && !shop.getName().isEmpty()){
 			model = new ModelAndView("choose_doc_type");
 			model.addObject("shop", shop);
 			session.setAttribute("shop", shop);
 		}else{
 			model = new ModelAndView("choose_by_code");
 			model.addObject("errorMsg", "Торговая точка не найдена!");
 			session.removeAttribute("shop");
 		}
 		return model;
 	}
 	
 	@RequestMapping(value="/addItem", method = RequestMethod.GET)
 	public void addItems(@RequestParam String idAmount, HttpSession session){
 		String[] array = idAmount.split(" ");
 		System.out.println("hello");
 		int id = Integer.parseInt(array[0]);
 		try{
 			int amount = Integer.parseInt(array[1]);
 			
 			List<Item> itemList = (List<Item>) session.getAttribute("itemList");
 			if(itemList == null){
 				System.out.println("create new itemList");
 				itemList = new ArrayList<Item>();
 			}
 			Item item = new Item();
 			item.id = id;
 			item.amount = amount;
 			if(amount == 0){
 				for(Item i : itemList){
 					if(i.id == id){
 						itemList.remove(i);
 						break;
 					}
 				}
 			}else{
 				itemList.add(item);
 			}
 			
 			session.setAttribute("itemList", itemList);
 			
 			System.out.println("ItemList contain:");
 			for(Item i : itemList){
 				System.out.println(i.id + ":" + i.amount);
 			}
 			
 			
 		}catch(NumberFormatException e){
 			e.printStackTrace();
 		}catch(IndexOutOfBoundsException e){
 			e.printStackTrace();
 			List<Item> itemList = (List<Item>) session.getAttribute("itemList");
 			if(itemList != null){
 				for(Item i : itemList){
 					if(i.id == id){
 						itemList.remove(i);
 						break;
 					}
 				}
 				session.setAttribute("itemList", itemList);
 			}
 		}
 		
 	}
 	
 	@RequestMapping(value="/calc", method = RequestMethod.GET)
 	 @ResponseBody public String GetShopName(@RequestParam String code) throws UnsupportedEncodingException{
 		System.out.println("CODE IS: "+code);
 		Shop shop = shopService.getShopByCode(code);
 		System.out.println(shop.toString());
 		String rez;
 		if(shop.getName() != null && !shop.getName().isEmpty()){
 			rez = new String(shop.toString().getBytes("ISO-8859-1"),"UTF-8");
 		}else{
 			rez = new String("Торговая точка не найдена!".getBytes("ISO-8859-1"),"UTF-8");
 			}
 		return rez;
 	}
 	
 	@RequestMapping(value="/choose_doc_type", method = RequestMethod.GET)
 	public String chooseDocType(){
 		return "choose_doc_type";
 	}
 
 	@RequestMapping(value="/realization", method = RequestMethod.POST)
 	public ModelAndView filteredRezult(@ModelAttribute("filterAtribute") Filter filter, HttpSession session){
 		ModelAndView model = new ModelAndView("realization");
 		
 		if(filter != null){
 			if(filter.getProdName() != null){
 				System.out.println("filter prod name: " + filter.getProdName());
 			}
 			if(filter.getBrand() != null){
 				System.out.println("filter brand: " + filter.getBrand());
 			}
 		}
 		
 		List<Brand> brandList = productService.getBrands();
 		model.addObject("brandList", brandList);
 		
 		List<Item> itemList = (List<Item>) session.getAttribute("itemList");
 		if(itemList != null){
 			for(Item i : itemList){
 				System.out.println(i.id + " - " + i.amount);
 			}
 		}
 		
 		List<Product> list = productService.getProducts(filter, itemList);
 		model.addObject("productList", list);
 		
 		return model;
 	}
 	
 	
 	
 	@RequestMapping(value="/realization", method = RequestMethod.GET)
 	public ModelAndView realization(HttpSession session){
 		session.setAttribute("docType", "Реализация");
 		session.removeAttribute("itemList");
 		
 		Shop shop = (Shop)session.getAttribute("shop");
 		System.out.println("getted shop: " + shop);
 		
 		List<Product> list = productService.getProducts(null, null);
 		ModelAndView model = new ModelAndView("realization");
 		model.addObject("productList", list);
 		
 		List<Brand> brandList = productService.getBrands();
 		model.addObject("brandList", brandList);
 		
 		return model;
 	}
 
 	@RequestMapping(value="/return_back", method = RequestMethod.GET)
 	public ModelAndView returnBack(HttpSession session){
 		session.setAttribute("docType", "Возврат");
 		Shop shop = (Shop)session.getAttribute("shop");
 		System.out.println("getted shop: " + shop);
 		
 		List<Product> list = productService.getProducts(null, null);
 		ModelAndView model = new ModelAndView("realization");
 		model.addObject("productList", list);
 		
 		List<Brand> brandList = productService.getBrands();
 		model.addObject("brandList", brandList);
 		
 		return model;
 	}
 
 	@RequestMapping(value="/data", method = RequestMethod.GET)
 	public String data(){
 		return "data";
 	}
 
 	@RequestMapping(value="/test", method = RequestMethod.GET)
 	public String test(){
 		return "test";
 	}
 }
