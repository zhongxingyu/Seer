 package com.cqlybest.admin.controller;
 
 import java.text.ParseException;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.time.DateUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.cqlybest.common.bean.Product;
 import com.cqlybest.common.bean.ProductCalendar;
 import com.cqlybest.common.bean.ProductComment;
 import com.cqlybest.common.bean.ProductTravel;
 import com.cqlybest.common.service.DestinationService;
 import com.cqlybest.common.service.DictService;
 import com.cqlybest.common.service.ImageService;
 import com.cqlybest.common.service.JsonService;
 import com.cqlybest.common.service.ProductService;
 
 @Controller
 public class ProductController {
 
   @Autowired
   private JsonService jsonService;
   @Autowired
   private ProductService productService;
   @Autowired
   private DestinationService destinationService;
   @Autowired
   private DictService dictService;
   @Autowired
   private ImageService imageService;
 
   /**
    * 添加产品
    */
   @RequestMapping(value = "/product/add.do", method = RequestMethod.POST)
   @ResponseBody
   public String add(@RequestParam String name) {
     Product product = new Product();
     product.setName(name);
     productService.add(product);
     return product.getId();
   }
 
   /**
    * 修改产品
    */
   @RequestMapping(value = "/product/update.do", method = RequestMethod.GET)
   public void update(@RequestParam String id, Model model) {
     model.addAttribute("product", productService.get(id));
   }
 
   /**
    * 修改产品
    */
   @RequestMapping(value = "/product/update.do", method = RequestMethod.POST)
   @ResponseBody
   public void update(@RequestParam String pk, @RequestParam String name,
       @RequestParam(required = false) String value,
       @RequestParam(required = false, value = "value[]") List<String> values) throws Exception {
     Object _value = value == null ? StringUtils.join(values, ",") : value;
     if ("days".equals(name)) {
       _value = Integer.parseInt(value);
     }
     if ("price".equals(name) || "childPrice".equals(name) || "specialPrice".equals(name)) {
       _value = (int) (Double.parseDouble(value) * 100);
     }
     if ("effectiveDate".equals(name) || "expiryDate".equals(name) || "departureDate".equals(name)) {
       _value = DateUtils.parseDate(value, new String[] {"yyyy-MM-dd"});
     }
     productService.update(pk, name, _value);
   }
 
   /**
    * 添加产品行程
    */
   @RequestMapping(value = "/product/travel/add.do", method = RequestMethod.POST)
   @ResponseBody
   public Integer addTravel(@RequestParam String productId, @RequestParam String name) {
     ProductTravel travel = new ProductTravel();
     travel.setProductId(productId);
     travel.setName(name);
     productService.add(travel);
     return travel.getId();
   }
 
   /**
    * 修改产品行程
    */
   @RequestMapping(value = "/product/travel/update.do", method = RequestMethod.POST)
   @ResponseBody
   public void update(@RequestParam Integer pk, @RequestParam String name, @RequestParam String value) {
     productService.updateTravel(pk, name, value);
   }
 
   /**
    * 修改产品行程
    */
   @RequestMapping(value = "/product/travel/delete.do", method = RequestMethod.POST)
   @ResponseBody
   public void update(@RequestParam Integer id) {
     productService.deleteTravel(id);
   }
 
   @RequestMapping(value = "/product/list.html", method = RequestMethod.GET)
   public void products(@RequestParam(required = false) Boolean hot,
       @RequestParam(required = false) Boolean red, @RequestParam(required = false) Boolean spe,
       @RequestParam(required = false) Boolean pub, @RequestParam(required = false) String name,
       @RequestParam(defaultValue = "0") int page, Model model) {
     page = Math.max(1, page);
     int pageSize = 10;
     model.addAttribute("page", page);
     model.addAttribute("pageSize", pageSize);
     model.addAttribute("total", productService.queryProductTotal(hot, red, spe, pub, name));
     model.addAttribute("products", productService.queryProduct(hot, red, spe, pub, name, page,
         pageSize));
 
     model.addAttribute("paramHot", hot);
     model.addAttribute("paramRed", red);
     model.addAttribute("paramSpe", spe);
     model.addAttribute("paramPub", pub);
     model.addAttribute("paramName", name);
   }
 
   @RequestMapping("/product/toggle.html")
   @ResponseBody
   public void toggle(@RequestParam String id, @RequestParam boolean published) {
     productService.update(id, "published", published);
   }
 
   @RequestMapping("/product/hot.html")
   @ResponseBody
   public void hot(@RequestParam(value = "ids[]") String[] ids, @RequestParam boolean hot) {
     productService.update(ids, "popular", hot);
   }
 
   @RequestMapping("/product/recommend.html")
   @ResponseBody
   public void recommend(@RequestParam(value = "ids[]") String[] ids, @RequestParam boolean red) {
     productService.update(ids, "recommend", red);
   }
 
   @RequestMapping("/product/special.html")
   @ResponseBody
   public void special(@RequestParam(value = "ids[]") String[] ids, @RequestParam boolean special) {
     productService.update(ids, "specialOffer", special);
   }
 
   @RequestMapping("/product/pub.html")
   @ResponseBody
   public void pub(@RequestParam(value = "ids[]") String[] ids, @RequestParam boolean pub) {
     productService.update(ids, "published", pub);
   }
 
   @RequestMapping("/product/delete.html")
   @ResponseBody
   public void del(@RequestParam(value = "ids[]") String[] ids) {
     productService.delete(ids);
   }
 
   /**
    * 添加产品评论
    */
   @RequestMapping(value = "/product/comment/add.do", method = RequestMethod.POST)
   @ResponseBody
   public ProductComment addComment(@RequestParam String productId, @RequestParam String user,
       @RequestParam String content) {
     ProductComment comment = new ProductComment();
     comment.setProductId(productId);
     comment.setUser(user);
     comment.setContent(content);
     comment.setCommentTime(new Date());
     productService.add(comment);
     return comment;
   }
 
   /**
    * 删除产品评论
    */
   @RequestMapping(value = "/product/comment/delete.do", method = RequestMethod.POST)
   @ResponseBody
   public void deleteComment(@RequestParam Integer id) {
     productService.deleteComment(id);
   }
 
   /**
    * 添加产品日历
    */
   @RequestMapping(value = "/product/calendar/add.do", method = RequestMethod.POST)
   @ResponseBody
   public void addCalendar(@RequestParam String productId, @RequestParam String start,
       @RequestParam String end, @RequestParam(required = false) String price) throws ParseException {
     Integer _price = null;
     if (StringUtils.isNotEmpty(price)) {
      _price = (int) (Double.parseDouble(price) * 100);
     }
     productService.addCalendar(productId, DateUtils.parseDate(start, new String[] {"yyyy-MM-dd"}),
         DateUtils.parseDate(end, new String[] {"yyyy-MM-dd"}), _price);
   }
 
   /**
    * 删除产品日历
    */
   @RequestMapping(value = "/product/calendar/delete.do", method = RequestMethod.POST)
   @ResponseBody
   public void deleteCalendar(@RequestParam String productId, @RequestParam String start,
       @RequestParam String end) throws ParseException {
     productService.deleteCalendar(productId, DateUtils
         .parseDate(start, new String[] {"yyyy-MM-dd"}), DateUtils.parseDate(end,
         new String[] {"yyyy-MM-dd"}));
   }
 
   /**
    * 获取产品日历
    */
   @RequestMapping(value = "/product/calendar.do", method = RequestMethod.GET)
   @ResponseBody
   public List<ProductCalendar> getCalendar(@RequestParam String id) {
     return productService.getCalendar(id);
   }
 
 }
