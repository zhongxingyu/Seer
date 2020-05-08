 package com.skplanet.rakeflurry.collector;
 
 import java.net.InetAddress;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.skplanet.cask.container.config.ConfigReader;
 import com.skplanet.cask.util.StringUtil;
 import com.skplanet.rakeflurry.dashboard.AccessCodeSummary;
 import com.skplanet.rakeflurry.dashboard.ApiKeySummary;
 import com.skplanet.rakeflurry.dashboard.DashBoard;
 import com.skplanet.rakeflurry.util.HttpUtil;
 
 public class Alerter {
     private static String SERVICE_NAME = "Rakeflurry";
     private Logger logger = LoggerFactory.getLogger(Alerter.class);
     private String from;
     private String to;
     private String serverUrl;
     private String subject;
     private String message;
     private String commonErrorMsg = 
             "Time : [%s] \n" + 
             "Service : %s \n" + 
             "Server : %s (%s) \n"; 
     
     public Alerter() {
         this.from = ConfigReader.getInstance().getServerConfig().getPropValue("mailSender");
         this.to = ConfigReader.getInstance().getServerConfig().getPropValue("mailRcpt");
         this.serverUrl = ConfigReader.getInstance().getServerConfig().getPropValue("mailServerUrl");
     }
     private void errorLow(AccessCodeSummary acs, ApiKeySummary aks, String word) throws Exception {
         try {
             setSubject("ERROR", word);
             this.message = String.format(
                     "%s" + 
                     "mbr no : %s\n" +
                     "access code : %s\n" +
                     "api key : %s\n",
                     getCommonMsg(), acs.getMbrNo(), acs.getAccessCode(), aks.getApiKey());
             
             sendHttp(); 
             
         } catch (Exception e) {
             logger.error("send http error : {}", StringUtil.exception2Str(e));
         }
     }
     private void setSubject(String category, String word) {
         this.subject = String.format("%s (%s) : %s", category, SERVICE_NAME, word);
     }
     private String getCommonMsg() throws Exception {
         InetAddress addr = InetAddress.getLocalHost();
         String hostname = addr.getHostName();
         
         DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:dd");
         String common = String.format(commonErrorMsg, dateFormat.format(new Date()), SERVICE_NAME, hostname, addr);
         return common;
     }
     private void sendHttp() throws Exception {
         
         if(from == null || to == null || serverUrl == null) {
             logger.error(
                     "PrimeMailer config not valid. from : {}, to : {}, serverUrl : {}",
                     new Object[]{from, to, serverUrl});
             return;
         }
         
         Map<String, String> mailMap = new LinkedHashMap<String, String>(); 
         mailMap.put("from", from);
         mailMap.put("to", to);
         mailMap.put("subject", subject);
         mailMap.put("msg", message);
         ObjectMapper mapper = new ObjectMapper();
         String content = mapper.writeValueAsString(mailMap);
         String result = HttpUtil.sendHttpPut(serverUrl, content);
         logger.info("send mail msg. serverUrl : {}, content : {}, result : {}", new Object[]{serverUrl, content, result});
     }
     
     public void errorApiKey(AccessCodeSummary acs, ApiKeySummary aks) throws Exception {
         errorLow(acs, aks, "Requesting api failed.");
     }
     public void errorHdfsCopy(AccessCodeSummary acs, ApiKeySummary aks) throws Exception {
         errorLow(acs, aks, "Copying to hdfs failed.");
     }
     public void errorCollectApiService(String msg) throws Exception {
         try {
             setSubject("ERROR", "Collecting Api Service failed.");
             this.message = String.format(
                     "%s", 
                     msg);
             this.message = getCommonMsg() + this.message;
             sendHttp();
         } catch (Exception e) {
             logger.error("send http error : {}", StringUtil.exception2Str(e));
         }
     }
     public void errorOverwriteKeyMapService(String msg) throws Exception {
         try {
             setSubject("ERROR", "Overwriting KeyMap Service failed.");
             this.message = String.format(
                     "%s", 
                     msg);
             this.message = getCommonMsg() + this.message;
             sendHttp();
         } catch (Exception e) {
             logger.error("send http error : {}", StringUtil.exception2Str(e));
         }
     }
     public void finishCollectApiService(DashBoard dashboard) throws Exception {
         try {
             
             DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
             
             Date start = dateFormat.parse(dashboard.getStartTime());
             Date end = dateFormat.parse(dashboard.getFinishTime());
             
             float elapsed = (float)(end.getTime() - start.getTime())/1000;
             
             boolean hasError = dashboard.hasError();
             setSubject("Job Reporting", "Collecting api service finished.");
             this.subject += hasError ? "Fail" : "Success";
             
             this.message = String.format(
                             "%s" + 
                             "dashboard id : %s \n" +
                             "start time : %s \n" +
                             "end time : %s \n" +
                             "elapsed : %f sec \n" +
                             "access code count : %s \n" + 
                            "result : %s \n", 
                             getCommonMsg(), dashboard.getDashboardId(), dashboard.getStartTime(), dashboard.getFinishTime(),
                             elapsed, dashboard.getTotalCount(), hasError ? "Fail" : "Success");
             
             sendHttp();
         } catch (Exception e) {
             logger.error("send http error : {}", StringUtil.exception2Str(e));
         }
     }
 }
