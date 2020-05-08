 package com.cqlybest.weixin.controller;
 
 import java.util.Arrays;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.cqlybest.common.Constant;
 import com.cqlybest.common.service.OptionService;
 import com.cqlybest.common.service.WeixinUserService;
 import com.cqlybest.weixin.ConnectOpenidFakeid;
 import com.cqlybest.weixin.bean.RequestMessage;
 import com.cqlybest.weixin.bean.ResponseMessage;
 import com.cqlybest.weixin.bean.ResponseNewsMessage;
 import com.cqlybest.weixin.bean.ResponseTextMessage;
 import com.cqlybest.weixin.service.SmartResponseService;
 import com.fasterxml.jackson.databind.DeserializationFeature;
 import com.fasterxml.jackson.dataformat.xml.XmlMapper;
 
 @Controller
 public class IndexController extends ControllerHelper {
 
   private static final Logger LOGGER = LoggerFactory.getLogger(IndexController.class);
 
   private static final String TOKEN_CONFIG = "weixin.token";
   private static final XmlMapper XML = new XmlMapper();
 
   @Autowired
   private OptionService optionService;
   @Autowired
   private WeixinUserService weixinUserService;
   @Autowired
   private SmartResponseService smartResponseService;
 
   @RequestMapping(method = RequestMethod.GET, value = "/")
   public Object root(@RequestParam String signature, @RequestParam String timestamp,
       @RequestParam String nonce, @RequestParam String echostr, HttpServletRequest request) {
     if (auth(signature, timestamp, nonce)) {
       return new ResponseEntity<byte[]>(echostr.getBytes(), HttpStatus.OK);
     }
 
     return ok();
   }
 
   @RequestMapping(method = RequestMethod.POST, value = "/")
   public Object root(@RequestParam String signature, @RequestParam String timestamp,
       @RequestParam String nonce, @RequestBody String data, HttpServletRequest request, Model model) {
     if (!auth(signature, timestamp, nonce)) {
       return ok();
     }
 
     try {
       String encoding = request.getCharacterEncoding();
       RequestMessage message =
           XML.readValue(new String(data.getBytes(encoding == null ? "ISO-8859-1" : encoding)),
               RequestMessage.class);
       System.out.println(XML.writeValueAsString(message));
       String type = message.getMsgType();
       if ("text".equals(type)) {// 文本消息
         ConnectOpenidFakeid.connect(weixinUserService, message);
         return text(model, message);
       }
       if ("image".equals(type)) {// 图片消息
         return image(model, message);
       }
       if ("location".equals(type)) {// 地理位置消息
         return location(model, message);
       }
       if ("link".equals(type)) {// 链接消息
         return link(model, message);
       }
       if ("event".equals(type)) {// 事件推送
         return event(model, message);
       }
     } catch (Exception e) {
       e.printStackTrace();
     }
 
     return ok();
   }
 
   private boolean auth(String signature, String timestamp, String nonce) {
     try {
       String token = System.getProperty(TOKEN_CONFIG, System.getenv(TOKEN_CONFIG));
       String[] chars = new String[] {token, timestamp, nonce,};
       Arrays.sort(chars);
       String sha1 = DigestUtils.shaHex(StringUtils.join(chars));
       if (sha1.equals(signature)) {
         return true;
       }
     } catch (Exception e) {
       e.printStackTrace();
     }
 
     return false;
   }
 
   private Object text(Model model, RequestMessage message) throws Exception {
     ResponseMessage response = smartResponseService.smartResponse(message);
     if (response != null) {
       model.addAttribute("data", response);
       if (response instanceof ResponseTextMessage) {
         return "/text";
       }
       if (response instanceof ResponseNewsMessage) {
         return "/news";
       }
     }
     return ok();
   }
 
   private Object image(Model model, RequestMessage message) throws Exception {
     return ok();
   }
 
   private Object location(Model model, RequestMessage message) throws Exception {
     return ok();
   }
 
   private Object link(Model model, RequestMessage message) throws Exception {
     return ok();
   }
 
   private Object event(Model model, RequestMessage message) throws Exception {
     String type = message.getEvent();
     if ("subscribe".equals(type)) {// 订阅
       String welcomeMessage =
           optionService.getOptions().get(Constant.OPTION_WEIXIN_WELCOME_MESSAGE);
       if (StringUtils.isNotBlank(welcomeMessage)) {
         ResponseTextMessage response = new ResponseTextMessage();
         response.setFromUserName(message.getToUserName());
         response.setToUserName(message.getFromUserName());
         response.setContent(welcomeMessage);
         response.setCreateTime(System.currentTimeMillis());
        model.addAttribute("message", response);
         ConnectOpenidFakeid.connect(weixinUserService, message.getFromUserName());// 设置OpenID与FakeID
         return "/text";
       }
       LOGGER.warn("Due no weixin_welcome_message set, system don't send ");
     }
     if ("unsubscribe".equals(type)) {// 取消
     }
     if ("CLICK".equals(type)) {// 自定义菜单点击事件
     }
     return ok();
   }
 
   static {
     XML.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
   }
 }
