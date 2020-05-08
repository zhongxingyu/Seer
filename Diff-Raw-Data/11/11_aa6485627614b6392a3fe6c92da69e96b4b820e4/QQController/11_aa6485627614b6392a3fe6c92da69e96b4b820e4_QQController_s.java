 package com.mmtzj.action;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.google.common.collect.Maps;
 import com.mmtzj.domain.Category;
 import com.mmtzj.domain.Item;
 import com.mmtzj.service.DataService;
 import com.mmtzj.service.QQService;
 import com.mmtzj.util.BaseUtil;
 import com.mmtzj.util.QQConstant;
 import com.qq.open.OpenApiV3;
 import com.qq.open.OpensnsException;
 import org.apache.commons.lang.StringUtils;
 import org.apache.shiro.SecurityUtils;
 import org.apache.shiro.session.Session;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.util.WebUtils;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 import java.util.HashMap;
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
 public class QQController extends BaseController {
 
     Logger logger = LoggerFactory.getLogger(QQController.class);
 
     @Resource
     private QQService qqService;
 
     @Resource
     private DataService dataService;
 
     @RequestMapping("/")
     public String index(HttpServletRequest request, Model model) {
         long l1 = System.currentTimeMillis();
         Map<String, Object> reqMap = WebUtils.getParametersStartingWith(request, "");
         String refresh = (String) reqMap.get("refresh");
         String openid = (String) reqMap.get("openid");
         String openkey = (String) reqMap.get("openkey");
         String pf = (String) reqMap.get("pf");
         Session session = SecurityUtils.getSubject().getSession();
         if(StringUtils.isNotBlank(openid)&&StringUtils.isNotBlank(openkey)){
             session.setAttribute("openid", openid);
             session.setAttribute("openkey", openkey);
             session.setAttribute("refresh", refresh);
             session.setAttribute("pf", pf);
         }
         List<Category> categories = dataService.getCategories();
         model.addAttribute("categories", categories);
         List<Item> items = dataService.getItems();
         model.addAttribute("items", items);
         model.addAttribute("guangzhuQQ", "377309000");
 
         Map<String, Integer> itemTypes = dataService.getItemTypeCountsMap(items);
         model.addAttribute("itemTypes", itemTypes);
         long l2 = System.currentTimeMillis();
         logger.info("cost {} ms", l2 - l1);
         return "/qqapp/index";
     }
 
    @RequestMapping("/user/")
     @ResponseBody
    public Map<String, Object> getUserInfo(HttpServletRequest request) {
         Session session = SecurityUtils.getSubject().getSession();
         String openid = "4D2B3A0F6C211F8A55E598D6C2B97B39";
 //        if(session.getAttribute("openid")!=null){
 //            openid = (String) session.getAttribute("openid");
 //        }else{
 //            return Maps.newHashMap();
 //        }
         String openkey = "0DEA4780EB0F37DA0722177B3472919D";//(String) session.getAttribute("openkey");
         String pf = "qzone";//(String) session.getAttribute("pf");
         logger.info("=========={} {} {}", openid, openkey, pf);
         OpenApiV3 sdk = new OpenApiV3(QQConstant.APP_ID_SM, QQConstant.APP_KEY_SM);
         sdk.setServerName("openapi.tencentyun.com");
         String scriptName = "/v3/user/get_info";
         // 指定HTTP请求协议类型
         String protocol = "http";
         // 填充URL请求参数
         HashMap<String, String> params = new HashMap<String, String>();
         params.put("openid", openid);
         params.put("openkey", openkey);
         params.put("pf", pf);
         try {
             String resp = sdk.api(scriptName, params, protocol);
             ObjectMapper mapper = new ObjectMapper();
             return mapper.readValue(resp, Map.class);
         } catch (Exception e) {
             e.printStackTrace();
         }
         return null;
     }
 
    @RequestMapping("/testMap")
     @ResponseBody
    public Map<String, Object> testMap(){
         Session session = SecurityUtils.getSubject().getSession();
         String openid = "";
         if(session.getAttribute("openid")!=null){
             openid = (String) session.getAttribute("openid");
         }else{
             return Maps.newHashMap();
         }
         String openkey = (String) session.getAttribute("openkey");
         String pf = (String) session.getAttribute("pf");
         logger.info("=========={} {} {}", openid, openkey, pf);
         OpenApiV3 sdk = new OpenApiV3(QQConstant.APP_ID_SM, QQConstant.APP_KEY_SM);
         sdk.setServerName("openapi.tencentyun.com");
         String scriptName = "/v3/user/get_info";
         // 指定HTTP请求协议类型
         String protocol = "http";
         // 填充URL请求参数
         HashMap<String, String> params = new HashMap<String, String>();
         params.put("openid", openid);
         params.put("openkey", openkey);
         params.put("pf", pf);
         try {
             String resp = sdk.api(scriptName, params, protocol);
             ObjectMapper mapper = new ObjectMapper();
             return mapper.readValue(resp, Map.class);
         } catch (Exception e) {
             logger.error(e.getMessage());
             e.printStackTrace();
         }
         return null;
     }
 
     @RequestMapping("/testString")
     @ResponseBody
     public String testString(){
         return null;
     }
 
     @RequestMapping("/ilike")
     @ResponseBody
     public String ilike(HttpServletRequest request) {
         int itemId = BaseUtil.getIntValue(request.getParameter("iid"));
         logger.info("the ip {} like the item {}", BaseUtil.getIP(request), itemId);
         qqService.updateItem(itemId, "likeCount");
         return null;
     }
 
     @RequestMapping("/icollect")
     @ResponseBody
     public String icollect(HttpServletRequest request) {
         int itemId = BaseUtil.getIntValue(request.getParameter("iid"));
         logger.info("the ip {} collect the item {}", BaseUtil.getIP(request), itemId);
         qqService.updateItem(itemId, "collectCount");
         return null;
     }
 
     @RequestMapping("/iclick")
     @ResponseBody
     public String iclick(HttpServletRequest request) {
         int itemId = BaseUtil.getIntValue(request.getParameter("iid"));
         logger.info("the ip {} click the item {}", BaseUtil.getIP(request), itemId);
         qqService.updateItem(itemId, "wantToBuy");
         return null;
     }
 
 }
