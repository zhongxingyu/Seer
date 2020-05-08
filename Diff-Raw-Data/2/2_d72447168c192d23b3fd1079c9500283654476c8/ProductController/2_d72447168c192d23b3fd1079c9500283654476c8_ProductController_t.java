 package com.cqlybest.admin.controller;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.time.DateUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.cqlybest.common.bean.MaldivesRoom;
 import com.cqlybest.common.bean.Product;
 import com.cqlybest.common.bean.ProductCalendar;
 import com.cqlybest.common.bean.ProductComment;
 import com.cqlybest.common.bean.ProductMaldives;
 import com.cqlybest.common.bean.ProductTraffic;
 import com.cqlybest.common.bean.ProductTravel;
 import com.cqlybest.common.controller.ControllerHelper;
 import com.cqlybest.common.service.DictService;
 import com.cqlybest.common.service.ImageService;
 import com.cqlybest.common.service.JsonService;
 import com.cqlybest.common.service.MaldivesService;
 import com.cqlybest.common.service.OptionService;
 import com.cqlybest.common.service.ProductService;
 
 @Controller
 public class ProductController extends ControllerHelper {
 
   @Autowired
   private JsonService jsonService;
   @Autowired
   private ProductService productService;
   @Autowired
   private DictService dictService;
   @Autowired
   private ImageService imageService;
   @Autowired
   private MaldivesService maldivesService;
   @Autowired
   private OptionService optionService;
 
   /**
    * 添加产品
    */
   @RequestMapping(value = "/product/add.do", method = RequestMethod.POST)
   @ResponseBody
   public String add(@RequestParam String name, @RequestParam int type) {
     Product product = new Product();
     product.setName(name);
     product.setProductType(type);
     productService.add(product);
     return product.getId();
   }
 
   /**
    * 修改产品
    */
   @RequestMapping(value = "/product/update.do", method = RequestMethod.GET)
   public void update(@RequestParam String id, Model model) {
     Product product = productService.get(id);
     model.addAttribute("product", product);
     if (product.getProductType() == Product.MALDIVES) {
       model.addAttribute("maldivesIslands", maldivesService.list(1, null));
     }
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
     if ("days".equals(name) || "nights".equals(name)) {
       _value = Integer.parseInt(value);
     }
     if ("price".equals(name) || "marketPrice".equals(name)) {
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
    * 添加产品交通
    */
   @RequestMapping(value = "/product/traffic/add.do", method = RequestMethod.POST)
   @ResponseBody
   public void addTraffic(@RequestParam String productId, @RequestParam String name,
       @RequestParam(required = false) Integer type) {
     ProductTraffic traffic = new ProductTraffic();
     traffic.setProductId(productId);
     traffic.setName(name);
     traffic.setType(type);
     productService.add(traffic);
   }
 
   /**
    * 添加马尔代夫行程
    */
   @RequestMapping(value = "/product/maldives/add.do", method = RequestMethod.POST)
   @ResponseBody
   public void addMaldives(@RequestParam String productId, @RequestParam String name) {
     ProductMaldives maldives = new ProductMaldives();
     maldives.setProductId(productId);
     maldives.setName(name);
     productService.add(maldives);
   }
 
   /**
    * 修改产品行程
    */
   @RequestMapping(value = "/product/travel/update.do", method = RequestMethod.POST)
   @ResponseBody
   public void updateTravel(@RequestParam Integer pk, @RequestParam String name,
       @RequestParam String value) {
     productService.updateTravel(pk, name, value);
   }
 
   /**
    * 修改产品交通
    * 
    * @throws Exception
    */
   @RequestMapping(value = "/product/traffic/update.do", method = RequestMethod.POST)
   @ResponseBody
   public void updateTraffic(@RequestParam Integer pk, @RequestParam String name,
       @RequestParam String value) throws Exception {
     Object _value = value;
     if (name.equals("type")) {
       _value = value.isEmpty() ? null : Integer.parseInt(value);
     }
     if (name.equals("departureTime") || name.equals("landingTime")) {
       _value = DateUtils.parseDate(value, new String[] {"HH:mm", "dd HH:mm"});
     }
     productService.updateTraffic(pk, name, _value);
   }
 
   /**
    * 修改马儿代夫产品行程
    */
   @RequestMapping(value = "/product/maldives/update.do", method = RequestMethod.POST)
   @ResponseBody
   public void updateMaldives(@RequestParam String pk, @RequestParam String name,
       @RequestParam String value) {
     if (name.startsWith("maldives.")) {
       String _name = name.substring(9);
       Object _value = value;
       if (_name.equals("roomId")) {
         _value = Integer.parseInt(value);
       }
       productService.updateMaldives(Integer.valueOf(pk), _name, _value);
     }
 
     if (name.startsWith("detail.")) {
       String _name = name.substring(7);
       Object _value = value;
       if (_name.equals("room1") || _name.equals("room2") || _name.equals("room3")) {
        _value = value.isEmpty() ? null : Integer.parseInt(value);
       }
       productService.updateMaldivesDetail(pk, _name, _value);
     }
   }
 
   /**
    * 删除产品行程
    */
   @RequestMapping(value = "/product/travel/delete.do", method = RequestMethod.POST)
   @ResponseBody
   public void deleteTravel(@RequestParam Integer id) {
     productService.deleteTravel(id);
   }
 
   /**
    * 删除产品交通
    */
   @RequestMapping(value = "/product/traffic/delete.do", method = RequestMethod.POST)
   @ResponseBody
   public void deleteTraffic(@RequestParam Integer id) {
     productService.deleteTraffic(id);
   }
 
   /**
    * 删除马尔代夫产品行程
    */
   @RequestMapping(value = "/product/maldives/delete.do", method = RequestMethod.POST)
   @ResponseBody
   public void deleteMaldives(@RequestParam Integer id) {
     productService.deleteMaldives(id);
   }
 
   @RequestMapping(value = "/product/list.do", method = RequestMethod.GET)
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
     model.addAttribute("options", optionService.getOptions());
 
     model.addAttribute("paramHot", hot);
     model.addAttribute("paramRed", red);
     model.addAttribute("paramSpe", spe);
     model.addAttribute("paramPub", pub);
     model.addAttribute("paramName", name);
   }
 
   @RequestMapping("/product/toggle.do")
   @ResponseBody
   public void toggle(@RequestParam String id, @RequestParam boolean published) {
     productService.update(id, "published", published);
   }
 
   @RequestMapping("/product/hot.do")
   @ResponseBody
   public void hot(@RequestParam(value = "ids[]") String[] ids, @RequestParam boolean hot) {
     productService.update(ids, "popular", hot);
   }
 
   @RequestMapping("/product/recommend.do")
   @ResponseBody
   public void recommend(@RequestParam(value = "ids[]") String[] ids, @RequestParam boolean red) {
     productService.update(ids, "recommend", red);
   }
 
   @RequestMapping("/product/special.do")
   @ResponseBody
   public void special(@RequestParam(value = "ids[]") String[] ids, @RequestParam boolean special) {
     productService.update(ids, "specialOffer", special);
   }
 
   @RequestMapping("/product/pub.do")
   @ResponseBody
   public void pub(@RequestParam(value = "ids[]") String[] ids, @RequestParam boolean pub) {
     productService.update(ids, "published", pub);
   }
 
   @RequestMapping("/product/delete.do")
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
   public ResponseEntity<Object> addCalendar(@RequestParam String productId,
       @RequestParam String start, @RequestParam String end, @RequestParam String price,
       @RequestParam(required = false) String childPrice, @RequestParam boolean special)
       throws ParseException {
     if (!start.matches("^\\d{4}-\\d{2}-\\d{2}") || !end.matches("^\\d{4}-\\d{2}-\\d{2}")) {
       return error("必须设置有效的开始日期和结束日期");
     }
     if (StringUtils.isEmpty(price)
         || !price.matches("^((0(.[\\d]{1,2})?)|([1-9][0-9]*(.[\\d]{1,2})?))$")) {
       return error("必须设置价格/正数/最多支持两位小数");
     }
     if (StringUtils.isNotEmpty(childPrice)
         && !childPrice.matches("^((0(.[\\d]{1,2})?)|([1-9][0-9]*(.[\\d]{1,2})?))$")) {
       return error("儿童价必须是正数/最多支持两位小数");
     }
     Integer _price = (int) (Double.parseDouble(price) * 100);
     Integer _childPrice = null;
     if (StringUtils.isNotEmpty(childPrice)) {
       _childPrice = (int) (Double.parseDouble(childPrice) * 100);
     }
     productService.addCalendar(productId, DateUtils.parseDate(start, new String[] {"yyyy-MM-dd"}),
         DateUtils.parseDate(end, new String[] {"yyyy-MM-dd"}), _price, _childPrice, special);
     return ok();
   }
 
   /**
    * 删除产品日历
    */
   @RequestMapping(value = "/product/calendar/delete.do", method = RequestMethod.POST)
   public ResponseEntity<Object> deleteCalendar(@RequestParam String productId,
       @RequestParam String start, @RequestParam String end) throws ParseException {
     if (!start.matches("^\\d{4}-\\d{2}-\\d{2}") || !end.matches("^\\d{4}-\\d{2}-\\d{2}")) {
       return error("必须设置有效的开始日期和结束日期");
     }
     productService.deleteCalendar(productId, DateUtils
         .parseDate(start, new String[] {"yyyy-MM-dd"}), DateUtils.parseDate(end,
         new String[] {"yyyy-MM-dd"}));
     return ok();
   }
 
   /**
    * 获取产品日历
    */
   @RequestMapping(value = "/product/calendar.do", method = RequestMethod.GET)
   @ResponseBody
   public List<ProductCalendar> getCalendar(@RequestParam String id) {
     return productService.getCalendar(id);
   }
 
   /**
    * 获取马代海岛房型
    */
   @RequestMapping(value = "/product/maldives/room/list.do", method = RequestMethod.GET)
   @ResponseBody
   public List<Object> getMaldivesIslandRooms(@RequestParam String islandId) {
     List<Object> result = new ArrayList<>();
     List<MaldivesRoom> rooms = maldivesService.getSimpleRooms(islandId);
     for (MaldivesRoom room : rooms) {
       Map<String, Object> obj = new HashMap<>();
       obj.put("value", room.getId());
       obj.put("text", room.getZhName() + room.getEnName());
       result.add(obj);
     }
     return result;
   }
 }
