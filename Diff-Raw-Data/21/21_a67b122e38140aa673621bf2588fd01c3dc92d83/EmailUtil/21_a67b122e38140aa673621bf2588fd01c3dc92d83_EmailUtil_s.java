 package com.pms.service.util;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 
 import org.apache.commons.mail.Email;
 import org.apache.commons.mail.EmailAttachment;
 import org.apache.commons.mail.EmailException;
 import org.apache.commons.mail.HtmlEmail;
 import org.apache.commons.mail.MultiPartEmail;
 import org.apache.commons.mail.SimpleEmail;
 import org.apache.velocity.app.Velocity;
 import org.apache.velocity.app.VelocityEngine;
 import org.springframework.ui.velocity.VelocityEngineUtils;
 
 import com.pms.service.cfg.ConfigurationManager;
 import com.pms.service.mockbean.PurchaseCommonBean;
 import com.pms.service.mockbean.SalesContractBean;
 
 public class EmailUtil {
 	
     public static void sendMail(String subject, List<Object> toList, String content) {
     	sendMail(subject, toList, content, null);
     }
 
     public static void sendMail(String subject, String to, String content, String file) {
     	List<Object> toList = new ArrayList<Object>();
     	toList.add(to);
     	sendMail(subject, toList, content, file);
     }
     
     public static void sendMail(String subject, List<Object> toList, Map<String,Object> model, String template, String file) {
     	String content  = getContent(model, template);
     	sendMail(subject, toList, content, file);
     }
     
     public static void sendEqListEmails(String subject, List<Object> toList, Map<String,Object> model, String template, Object eqList) {
     	String file = ExcleUtil.createEqcostExcel(eqList);
     	sendMail(subject, toList, model, template, file);
     }
     public static void sendEqListEmails(String subject, List<Object> toList,String content, Object eqList) {
     	String file = ExcleUtil.createEqcostExcel(eqList);
     	sendMail(subject, toList,content, file);
     }
     
     /**发送主方法**/
 	public static void sendMail(String subject, List<Object> toList, String content, String file) {
 		HtmlEmail email = new HtmlEmail();
 		
         try {
             // 如果要求身份验证，设置用户名、密码，分别为发件人在邮件服务器上注册的用户名和密码
             email.setAuthentication(ConfigurationManager.getProperty("emailUserName"), ConfigurationManager.getProperty("password"));
             email.setHostName(ConfigurationManager.getProperty("smtp"));// 设置发送主机的服务器地址
             email.setFrom(ConfigurationManager.getProperty("email"), "工程管理系统");// 发件人邮箱
             
             List<InternetAddress> address = new ArrayList<InternetAddress>();
             for (Object mail : toList) {
                 if (!ApiUtil.isEmpty(mail)) {
                     String[] emailList = mail.toString().split(",");
                     for (String em : emailList) {
                         address.add(new InternetAddress(em));
                     }
                 }
             }
             
             if (!address.isEmpty()) {
                 email.setTo(address);// 设置收件人邮箱
                 email.setSubject(subject);// 设置邮件的主题
 
                 email.setHtmlMsg(content);
                 email.setTextMsg("您的邮箱不支持HTML消息格式");
                 email.setCharset("UTF-8");
 
                 if (!ApiUtil.isEmpty(file) && new File(file).exists()) {
                     EmailAttachment attachment = new EmailAttachment();
                     attachment.setPath(file);
                     attachment.setDisposition(EmailAttachment.ATTACHMENT);
                     attachment.setName(new File(file).getName());
                     email.attach(attachment);
                 }
 
                 email.send();
             }
             
         } catch (EmailException e) {
             e.printStackTrace();
         } catch (AddressException e){
             e.printStackTrace();
         }
     }
     
     /**
      * @return 邮件主体
      * @param model 向模版中传递的对象变量
      * @param tempate  模版名
      * */
     public static String getContent(Map<String,Object> model, String template) {
         VelocityEngine ve = new VelocityEngine(); //配置模板引擎
         ve.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH, ConfigurationManager.getEmailTemplatePath());//模板文件所在的路径
         ve.setProperty(Velocity.INPUT_ENCODING,"UTF-8");//处理中文问题
         ve.setProperty(Velocity.OUTPUT_ENCODING,"UTF-8");//处理中文问题
         String result = "";
         try {
             ve.init();//初始化模板
             result = VelocityEngineUtils.mergeTemplateIntoString(ve, template, "UTF-8",model);
         } catch (Exception e){
             e.printStackTrace();
         }
         return result;
     }
     
 }
