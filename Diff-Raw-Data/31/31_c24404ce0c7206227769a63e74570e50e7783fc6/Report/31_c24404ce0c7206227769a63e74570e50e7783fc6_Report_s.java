 package org.roger.study.ExClient.test;
 
 import org.roger.study.ExClient.configuration.Configs;
 import org.roger.study.ExClient.controller.TestCounter;
 
 import java.math.BigDecimal;
 import java.text.MessageFormat;
 
 /**
  * Created with IntelliJ IDEA.
  * User: next
  * Date: 13-8-27
  * Time: 上午10:22
  * To change this template use File | Settings | File Templates.
  */
 public class Report {
 
     public static void report()  {
         System.out.println("测试结束....");
         System.out.println("----------------------------------------------------------------");
         System.out.println(MessageFormat.format("测试账户：{0}个\t\t      每账户发送邮件：{1}封" ,Configs.getUsers().size(), Configs.getRunTimes()));
         System.out.println(MessageFormat.format("实际发送邮件总数：{0}封\t  成功发送邮件：{1}封", TestCounter.getSent(), TestCounter.getSentOk()));
         System.out.println(MessageFormat.format("邮件发送成功率：{0}%", getTestSuccessRatio()));
         System.out.println("----------------------------------------------------------------");
     }
 
     private static float getTestSuccessRatio() {
         if (TestCounter.getSentOk() == 0)
             return (float)0.0;
 
         float res = (float)TestCounter.getSent()/TestCounter.getSentOk() * 100;
         BigDecimal b = new BigDecimal(res);
 
         return   b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
     }
 
     public static void TestStart() {
         System.out.println("测试开始...");
     }
 
     public static void connectServer(boolean b) {
         System.out.println("  连接服务器[" + Configs.getHost() + ":" + Configs.getPort()  + "]" + (b? "成功":"失败"));
     }
 
     public static void logon(String user) {
         System.out.println("  " + user + "开始登陆");
     }
 
     public static void logon(String user, boolean b) {
         System.out.println("  " + user + "登陆" + (b? "成功":"失败"));
     }
 
     public static void paraPolicyKey(String user, boolean b) {
         System.out.println("  解析" + user + "的PolicyKey" + (b? "成功":"失败"));
     }
 
     public static void ackPolicyKey(String user)  {
         System.out.println("  " + user + "开始客户端确认PolicyKey");
     }
     public static void ackPolicyKey(String user, boolean b) {
         System.out.println("  " + user + "客户端确认PolicyKey" + (b? "成功":"失败"));
     }
 
     public static void sendmail(String user)  {
         System.out.println("  " + user + "开始进行邮件发送测试");
     }
 }
