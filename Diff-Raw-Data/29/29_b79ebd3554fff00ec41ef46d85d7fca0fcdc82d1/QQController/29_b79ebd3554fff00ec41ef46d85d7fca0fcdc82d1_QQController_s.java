 package com.mmtzj.action;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Maps;
 import com.mmtzj.domain.Category;
 import com.mmtzj.domain.Eval;
 import com.mmtzj.domain.Item;
 import com.mmtzj.service.DataService;
 import com.mmtzj.service.JedisService;
 import com.mmtzj.service.QQService;
 import com.mmtzj.util.BaseUtil;
 import com.mmtzj.util.Constant;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 import java.util.List;
 import java.util.Map;
 
 
 /**
  * Created with IntelliJ IDEA.
  * User: wangxin
  * Date: 13-1-6
  * Time: 下午10:27
  * To change this template use File | Settings | File Templates.
  */
 @Controller
 @RequestMapping("/qqapp")
 public class QQController extends BaseController{
 
     Logger logger = LoggerFactory.getLogger(QQController.class);
 
     @Resource
     private QQService qqService;
 
     @Resource
     private DataService dataService;
 
    @Resource
    private JedisService jedisService;

     @RequestMapping("/")
     public String index(Model model){
         long l1 = System.currentTimeMillis();
         List<Category> categories = dataService.getCategories();
         model.addAttribute("categories", categories);
         List<Item> items = dataService.getItems();
         model.addAttribute("items", items);
 
         Map<String, Integer> itemTypes = dataService.getItemTypeCountsMap(items);
         model.addAttribute("itemTypes", itemTypes);
         long l2 = System.currentTimeMillis();
         logger.info("cost {} ms", l2 - l1);
         return "/qqapp/index";
     }
 
 
 
     @RequestMapping("/ilike")
     @ResponseBody
     public String ilike(HttpServletRequest request){
         int itemId = BaseUtil.getIntValue(request.getParameter("iid"));
         logger.info("the ip {} like the item {}", BaseUtil.getIP(request), itemId);
         qqService.updateItem(itemId, "likeCount");
         return null;
     }
 
     @RequestMapping("/icollect")
     @ResponseBody
     public String icollect(HttpServletRequest request){
         int itemId = BaseUtil.getIntValue(request.getParameter("iid"));
         logger.info("the ip {} collect the item {}", BaseUtil.getIP(request), itemId);
         qqService.updateItem(itemId, "collectCount");
         return null;
     }
 
     @RequestMapping("/iclick")
     @ResponseBody
     public String iclick(HttpServletRequest request){
         int itemId = BaseUtil.getIntValue(request.getParameter("iid"));
         logger.info("the ip {} click the item {}", BaseUtil.getIP(request), itemId);
         qqService.updateItem(itemId, "wantToBuy");
         return null;
     }
 
 }
