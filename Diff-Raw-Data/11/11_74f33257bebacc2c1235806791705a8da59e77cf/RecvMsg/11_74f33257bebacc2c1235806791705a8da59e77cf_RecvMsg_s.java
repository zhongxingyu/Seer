 package com.eplian.wechat.core.message.receive;
 
 /**
  * Created with IntelliJ IDEA.
  * User: www
  * Date: 13-9-1
  * Time: 下午11:29
  * To change this template use File | Settings | File Templates.
  */
 import com.eplian.wechat.core.message.reply.RplMsg;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import org.jdom2.Document;
 import org.jdom2.Element;
 import org.jdom2.JDOMException;
 import org.jdom2.input.SAXBuilder;
 import org.jdom2.Attribute;
 
 public class RecvMsg {
     /**
      * content, such as text, image url, link etc
      */
     public Map<String,String> map=new HashMap<String,String>();
 
     public RecvMsg(InputStream is)throws IOException,JDOMException{
         SAXBuilder saxBuilder = new SAXBuilder();
         Document document = saxBuilder.build(is);
         this.parseMsg(document);
     }
 
     private void parseMsg(Document document){
         Element root = document.getRootElement();
        Iterator it_attr = root.getAttributes().iterator();
         while (it_attr.hasNext()) {
            Attribute attribute = (Attribute) it_attr.next();
            String attrname = attribute.getName();
            String attrvalue = root.getAttributeValue(attrname);
            map.put( attrname, attrvalue);
         }
     }
     /**
      * 获取消息的值
      * @param key
      * @return
      */
     public String get(String key){
         return map.get(key);
     }
 }
