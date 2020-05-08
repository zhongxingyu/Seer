 package com.imeeting.mvc.controller;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 
 import javax.annotation.PostConstruct;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import com.imeeting.framework.ContextLoader;
 import com.richitec.donkey.client.DonkeyClient;
 import com.richitec.donkey.client.DonkeyHttpResponse;
 import com.richitec.util.RandomString;
 
 @Controller
 @RequestMapping(value = "/wx")
 public class WeiXinController {
     
     private static Log log = LogFactory.getLog(WeiXinController.class);
     
     private DocumentBuilderFactory factory;
     private DocumentBuilder db; 
     
     private DonkeyClient donkeyClient;
     
     public static final String ToUserName = "ToUserName";
     public static final String FromUserName = "FromUserName";
     public static final String CreateTime = "CreateTime";
     public static final String MsgType = "MsgType";
     public static final String Content = "Content";
     public static final String FuncFlag = "FuncFlag";
     public static final String ConfId = "ConfId";
     
     @PostConstruct
     public void init() {
         donkeyClient = ContextLoader.getDonkeyClient();
         
         factory = DocumentBuilderFactory.newInstance();
         try {
             db = factory.newDocumentBuilder();
         } catch (ParserConfigurationException e) {
             e.printStackTrace();
         }
     }
 
     @RequestMapping(method = RequestMethod.GET)
     public @ResponseBody String get(
             @RequestParam(value="signature") String signature,
             @RequestParam(value="timestamp") String timestamp,
             @RequestParam(value="nonce") String nonce,
             @RequestParam(value="echostr") String echostr) {
         log.debug("\n signature = " + signature +
                  "\n timestamp = " + timestamp +
                  "\n nonce = " + nonce +
                  "\n echostr = " + echostr);
         return echostr;
     }
     
 
     @RequestMapping(method = RequestMethod.POST)
     public ModelAndView post(
             @RequestParam(value="signature") String signature,
             @RequestParam(value="timestamp") String timestamp,
             @RequestParam(value="nonce") String nonce,
             @RequestBody String requestBody) throws SAXException, IOException {
         log.debug("\n signature = " + signature +
                  "\n timestamp = " + timestamp +
                  "\n nonce = " + nonce +
                  "\n request = " + requestBody);
         
         Document doc = db.parse(new ByteArrayInputStream(requestBody.getBytes("UTF-8")));
         String toUserName = get_value(doc, ToUserName);
         String fromUserName = get_value(doc, FromUserName);
         String createTime = get_value(doc, CreateTime);
         String msgType = get_value(doc, MsgType);
         String content = get_value(doc, Content);
         
         String confId = null;
        if ("text".equalsIgnoreCase(msgType) && "kh".equalsIgnoreCase(content)){
             confId = RandomString.genRandomNum(5);
             DonkeyHttpResponse donkeyResp =
                 donkeyClient.createNoMediaConference(confId, null, null, "weixin");
             if (null == donkeyResp || !donkeyResp.isAccepted()){
                 confId = "0";
                 log.error("Create audio conference error : "
                         + (null == donkeyResp ? "NULL Response" : donkeyResp
                                 .getStatusCode()));
             }
         }
         
         ModelAndView mv = new ModelAndView();
         mv.addObject(ToUserName, fromUserName);
         mv.addObject(FromUserName, toUserName);
         mv.addObject(CreateTime, createTime);
         mv.addObject(ConfId, confId);
         mv.setViewName("weixin/msg");
         return mv;
     }    
     
     private String get_value(Document doc, String nodeName){
         NodeList nl = doc.getElementsByTagName(nodeName);
         for (int i=0; i<nl.getLength(); i++){
             Node n = nl.item(i);
             return n.getTextContent();
         }
         return null;
     }
 }
