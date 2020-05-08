 package com.mmtzj.action;
 
 import com.google.common.collect.Maps;
 import com.mmtzj.domain.Item;
 import com.mmtzj.mapper.CategoryMapper;
 import com.mmtzj.mapper.IcollectMapper;
 import com.mmtzj.mapper.IlikeMapper;
 import com.mmtzj.mapper.ItemMapper;
 import com.mmtzj.util.BaseUtil;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;
 
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
 public class QQController {
 
     Logger logger = LoggerFactory.getLogger(QQController.class);
 
     @Autowired
     private CategoryMapper categoryMapper;
 
     @Autowired
     private ItemMapper itemMapper;
 
     @Autowired
     private IlikeMapper ilikeMapper;
 
     @Autowired
     private IcollectMapper icollectMapper;
 
     @RequestMapping("/")
     public String index(Model model){
         model.addAttribute("categories", categoryMapper.getCategories());
         List<Item> items = itemMapper.getAll();
         model.addAttribute("items", items);
         Map<String, Integer> itemTypes = Maps.newHashMap();
         for(Item item : items){
             if(itemTypes.containsKey("itemtype"+item.getCategoryId())){
                 itemTypes.put("itemtype"+item.getCategoryId(), itemTypes.get("itemtype"+item.getCategoryId())+1);
             }else{
                 itemTypes.put("itemtype"+item.getCategoryId(), 1);
             }
         }
         model.addAttribute("itemTypes", itemTypes);
         return "index";
     }
 
     @RequestMapping("/ilike")
     @ResponseBody
     public String ilike(HttpServletRequest request){
         int itemId = BaseUtil.getIntValue(request.getParameter("iid"));
         logger.info("the ip {} like the item {}", BaseUtil.getIP(request), itemId);
         Item itemDB = itemMapper.get(itemId);
         Item item = new Item();
         item.setId(itemId);
         item.setLikeCount(itemDB.getLikeCount() + 1);
         itemMapper.update(item);
         return null;
     }
 
     @RequestMapping("/icollect")
     @ResponseBody
     public String icollect(HttpServletRequest request){
         int itemId = BaseUtil.getIntValue(request.getParameter("iid"));
         logger.info("the ip {} collect the item {}", BaseUtil.getIP(request), itemId);
         Item itemDB = itemMapper.get(itemId);
         Item item = new Item();
         item.setId(itemId);
         item.setCollectCount(itemDB.getCollectCount() + 1);
         itemMapper.update(item);
         return null;
     }
 
     @RequestMapping("/iclick")
     @ResponseBody
     public String iclick(HttpServletRequest request){
        int itemId = BaseUtil.getIntValue(request.getParameter("iid"));
        logger.info("the ip {} click the item {}", BaseUtil.getIP(request), itemId);
        Item itemDB = itemMapper.get(itemId);
        Item item = new Item();
        item.setId(itemId);
        item.setWantToBuy(itemDB.getWantToBuy() + 1);
        itemMapper.update(item);
         return null;
     }
 
 }
