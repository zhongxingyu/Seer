 /**
  * Copyright 2012 Impetus Infotech.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.impetus.kundera.ycsb.utils;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import org.springframework.mail.SimpleMailMessage;
 import org.springframework.mail.javamail.JavaMailSenderImpl;
 
 /**
  * @author Kuldeep Mishra
  * 
  */
 public class MailUtils
 {
     public static void sendMail(Map<String, Double> delta, String operationType, String dataStore)
     {
         String host = "192.168.150.5";
 //        int port = 465;
 //        Properties props = new Properties();
 ////        props.put("mail.smtp.auth", "true");
 ////        props.put("mail.smtp.starttls.enable", "true");
 //        // props.put("mail.smtp.port", "465");
 ////        props.put("mail.smtp.host", host);
 
         JavaMailSenderImpl emailSender = new JavaMailSenderImpl();
         emailSender.setHost(host);
         // emailSender.setPort(port);
         emailSender.setUsername("noreply-kundea@impetus.co.in");
//        emailSender.setJavaMailProperties(props);
         SimpleMailMessage mail = new SimpleMailMessage();
 //        mail.setTo(new String[] {"vivek.mishra@impetus.co.in", "amresh.singh@impetus.co.in",
 //                "kuldeep.mishra@impetus.co.in", "vivek.shrivastava@impetus.co.in"});
 
         mail.setTo(new String[] {"vivek.mishra@impetus.co.in"});
 
         mail.setFrom("noreply-kundea@impetus.co.in");
 
         if (operationType.equalsIgnoreCase("load"))
         {
             operationType = "write";
         }
         else if (operationType.equalsIgnoreCase("t"))
         {
             operationType = "read";
         }
         mail.setSubject(operationType + " kundera-" + dataStore + "-performance Delta");
 
         String mailBody = null;
         for (String key : delta.keySet())
         {
  if (mailBody == null)
             {
                 mailBody = key + " Performance Delta ==> " + delta.get(key) + " \n";
             }
             else
             {
                 mailBody = mailBody + key + " Performance Delta ==> " + delta.get(key) + " \n";
             }
         }
         mail.setText(mailBody);
         emailSender.send(mail);
     }
 
 
     public static void main(String[] args)
     {
         MailUtils mailUtils = new MailUtils();
         mailUtils.sendMail(new HashMap<String, Double>(), "load", "Cassandra");
     }
 }
