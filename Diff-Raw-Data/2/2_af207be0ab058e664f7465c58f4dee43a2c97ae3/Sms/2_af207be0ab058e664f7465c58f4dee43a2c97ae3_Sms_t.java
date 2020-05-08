 package com.belerweb.sms._9nuo;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.NameValuePair;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.params.HttpMethodParams;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /**
  * 九诺短信平台SMS接口
  * 
  * @author jun
  */
 public class Sms {
 
   private static final HttpClient CLIENT;
 
   private static final String API_BALANCE = "http://admin.9nuo.com/Interface_http/GetBalance.aspx";// 查询指定用户短信剩余额度
   private static final String API_SEND = "http://admin.9nuo.com/Interface_http/SendSms.aspx";// 指定用户发送短信
   private static final String API_SENT_HISTORY =
       "http://admin.9nuo.com/Interface_http/GetReportDetail.aspx";// 查询指定用户某日期范围内短信发送的明细清单
   private static final String API_DAILY_SENT =
       "http://admin.9nuo.com/Interface_http/GetReportTotalByDay.aspx";// 查询指定用户某日期范围内每天发送短信的总数
 
   private static final String CONFIG_KEY_USERNAME = "9nuo.username";
   private static final String CONFIG_KEY_PASSWORD = "9nuo.password";
   private static final String CONFIG_KEY_MD5_PASSWORD = "9nuo.password.md5";
 
   private static final SimpleDateFormat DATE_FORMAT =
       new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
   private static final SimpleDateFormat YMD_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
 
   public static final String PARAM_NAME_USERNAME = "userName";
   public static final String PARAM_NAME_PASSWORD = "pwd";
   private static final String PARAM_NAME_START_DATE = "startDate";
   private static final String PARAM_NAME_END_DATE = "endDate";
   private static final String PARAM_NAME_PHONE = "phone";
   private static final String PARAM_NAME_NOTE = "note";
 
   private NameValuePair username;
   private NameValuePair password;
 
   private Sms(Properties properties) {
     this.username =
         new NameValuePair(PARAM_NAME_USERNAME, properties.getProperty(PARAM_NAME_USERNAME));
     this.password =
         new NameValuePair(PARAM_NAME_PASSWORD, properties.getProperty(PARAM_NAME_PASSWORD));
   }
 
   /**
    * 查询余额
    * 
    * @return 剩余短信条数
    * @throws SmsException
    */
   public int getBalance() throws SmsException {
     String result = null;
     try {
       result = execute(API_BALANCE);
       return Integer.parseInt(result);
     } catch (Exception e) {
       throw new SmsException(result == null ? e.getMessage() : result, e);
     }
   }
 
   /**
    * 发送短信
    * 
    * @param to 收信人 （单个）
    * @param content 短信内容
    * @return 发送结果
    */
   public SendResult send(String to, String content) throws SmsException {
     if (!to.matches("\\d+")) {
       return new SendResult(to, false, "此接口只 支持一个号码");
     }
 
     String result = null;
     try {
       result =
           execute(API_SEND, new NameValuePair(PARAM_NAME_PHONE, to), new NameValuePair(
               PARAM_NAME_NOTE, content));
       return parseSendResult(result).get(0);
     } catch (Exception e) {
       throw new SmsException(result == null ? e.getMessage() : result, e);
     }
   }
 
   /**
    * 批量发送短信
    * 
    * @param to 收信人（多个）
    * @param content 短信内容
    * @return 发送结果
    */
   public List<SendResult> send(List<String> to, String content) {
     if (to == null || to.isEmpty()) {
       throw new SmsException("至少提供一个号码");
     }
 
     if (to.size() > 1000) {
       throw new SmsException("一次最多1000个号码");
     }
 
     String result = null;
     try {
       String phones = "";
       for (String phone : to) {
        phones = phones + "," + phone;
       }
       result =
           execute(API_SEND, new NameValuePair(PARAM_NAME_PHONE, phones.substring(1)),
               new NameValuePair(PARAM_NAME_NOTE, content));
       return parseSendResult(result);
     } catch (Exception e) {
       throw new SmsException(result == null ? e.getMessage() : result, e);
     }
   }
 
   /**
    * 查询短信历史记录
    * 
    * @param start 开始日期
    * @param end 结束日期
    * @return 指定日期内的短信记录
    */
   public List<SmsHistory> getSentHistory(Date start, Date end) throws SmsException {
     String result = null;
     try {
       result =
           execute(API_SENT_HISTORY, new NameValuePair(PARAM_NAME_START_DATE, YMD_DATE_FORMAT
               .format(start)), new NameValuePair(PARAM_NAME_END_DATE, YMD_DATE_FORMAT.format(end)));
       return parseHistoryResult(result);
     } catch (Exception e) {
       throw new SmsException(result == null ? e.getMessage() : result, e);
     }
   }
 
   /**
    * 按天汇总短信发送结果
    * 
    * @param start 开始日期
    * @param end 结束日期
    * @return 指定日期内短信按天汇总条数
    */
   public List<DailyReport> getDailyReport(Date start, Date end) throws SmsException {
     String result = null;
     try {
       result =
           execute(API_DAILY_SENT, new NameValuePair(PARAM_NAME_START_DATE, YMD_DATE_FORMAT
               .format(start)), new NameValuePair(PARAM_NAME_END_DATE, YMD_DATE_FORMAT.format(end)));
       return parseDailyReportResult(result);
     } catch (Exception e) {
       throw new SmsException(result == null ? e.getMessage() : result, e);
     }
   }
 
   private String execute(String api, NameValuePair... params) throws HttpException, IOException {
     int paramSize = params.length + 2;
     NameValuePair[] parameters = new NameValuePair[paramSize];
     parameters[0] = username;
     parameters[1] = password;
     for (int i = 2; i < paramSize; i++) {
       parameters[i] = params[i - 2];
     }
     PostMethod post = new PostMethod(api);
     post.setRequestBody(parameters);
     int code = CLIENT.executeMethod(post);
     String result = post.getResponseBodyAsString();
     if (code != HttpStatus.SC_OK) {
       throw new SmsException(code + result);
     }
     return result;
   }
 
   private List<SendResult> parseSendResult(String text) throws Exception {
     List<SendResult> result = new ArrayList<SendResult>();
     NodeList nodes = parse(text, "dt");
     for (int i = 0; i < nodes.getLength(); i++) {
       SendResult smResult = new SendResult();
       NodeList childNodes = ((Element) nodes.item(i)).getChildNodes();
       for (int j = 0; j < childNodes.getLength(); j++) {
         if (childNodes.item(j).getNodeType() == Node.ELEMENT_NODE) {
           String nodeName = childNodes.item(j).getNodeName();
           String value = null;
           Node firstChild = childNodes.item(j).getFirstChild();
           if (firstChild != null) {
             value = firstChild.getNodeValue();
           }
           if ("FPhone".equalsIgnoreCase(nodeName)) {
             smResult.setPhone(value);
           } else if ("FResult".equalsIgnoreCase(nodeName)) {
             smResult.setSuccess("发送成功".equals(value));
           } else if ("FDescription".equalsIgnoreCase(nodeName)) {
             smResult.setDescription(value);
           }
         }
       }
       result.add(smResult);
     }
     return result;
   }
 
   private List<SmsHistory> parseHistoryResult(String text) throws Exception {
     List<SmsHistory> result = new ArrayList<SmsHistory>();
     NodeList nodes = parse(text, "Table");
     for (int i = 0; i < nodes.getLength(); i++) {
       SmsHistory history = new SmsHistory();
       NodeList childNodes = ((Element) nodes.item(i)).getChildNodes();
       for (int j = 0; j < childNodes.getLength(); j++) {
         if (childNodes.item(j).getNodeType() == Node.ELEMENT_NODE) {
           String nodeName = childNodes.item(j).getNodeName();
           String value = null;
           Node firstChild = childNodes.item(j).getFirstChild();
           if (firstChild != null) {
             value = firstChild.getNodeValue();
           }
           if ("Phone".equalsIgnoreCase(nodeName)) {
             history.setPhone(value);
           } else if ("SendDate".equalsIgnoreCase(nodeName)) {
             history.setDate(DATE_FORMAT.parse(value.replaceAll(":(\\d{2})$", "$1")));
           } else if ("Note".equalsIgnoreCase(nodeName)) {
             history.setContent(value);
           } else if ("Result".equalsIgnoreCase(nodeName)) {
             history.setSuccess("成功".equals(value));
           }
         }
       }
       result.add(history);
     }
     return result;
   }
 
   private List<DailyReport> parseDailyReportResult(String text) throws Exception {
     List<DailyReport> result = new ArrayList<DailyReport>();
     NodeList nodes = parse(text, "Table");
     for (int i = 0; i < nodes.getLength(); i++) {
       DailyReport dailyReport = new DailyReport();
       NodeList childNodes = ((Element) nodes.item(i)).getChildNodes();
       for (int j = 0; j < childNodes.getLength(); j++) {
         if (childNodes.item(j).getNodeType() == Node.ELEMENT_NODE) {
           String nodeName = childNodes.item(j).getNodeName();
           String value = null;
           Node firstChild = childNodes.item(j).getFirstChild();
           if (firstChild != null) {
             value = firstChild.getNodeValue();
           }
           if ("SendDate".equalsIgnoreCase(nodeName)) {
             dailyReport.setDate(YMD_DATE_FORMAT.parse(value));
           } else if ("Result".equalsIgnoreCase(nodeName)) {
             dailyReport.setSuccess("成功".equals(value));
           } else if ("TotalCount".equalsIgnoreCase(nodeName)) {
             dailyReport.setCount(Integer.parseInt(value));
           }
         }
       }
       result.add(dailyReport);
     }
     return result;
   }
 
   private NodeList parse(String text, String nodeName) throws SAXException, IOException,
       ParserConfigurationException {
     return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
         new ByteArrayInputStream(text.getBytes())).getDocumentElement().getElementsByTagName(
         nodeName);
   }
 
   public static Sms init() {
     String username = System.getProperty(CONFIG_KEY_USERNAME, System.getenv(CONFIG_KEY_USERNAME));
     assert username != null : configError(CONFIG_KEY_USERNAME);
 
     String password = System.getProperty(CONFIG_KEY_PASSWORD, System.getenv(CONFIG_KEY_PASSWORD));
     String md5Password =
         System.getProperty(CONFIG_KEY_MD5_PASSWORD, System.getenv(CONFIG_KEY_MD5_PASSWORD));
     assert password != null || md5Password != null : configError(CONFIG_KEY_PASSWORD + " or  "
         + CONFIG_KEY_MD5_PASSWORD);
 
     Properties properties = new Properties();
     properties.put(PARAM_NAME_USERNAME, username);
     properties.put(PARAM_NAME_PASSWORD, md5Password != null ? md5Password : DigestUtils
         .md5Hex(password));
     return init(properties);
   }
 
   private static String configError(String key) {
     return "Need " + key + " config in system properties or environment";
   }
 
   public static Sms init(Properties properties) {
     assert properties != null : "properties is need.";
     assert properties.get(PARAM_NAME_USERNAME) != null : "userName is need.";
     assert properties.get(PARAM_NAME_PASSWORD) != null : "pwd is need.";
     return new Sms(properties);
   }
 
   static {
     CLIENT = new HttpClient();
     CLIENT.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
   }
 
 }
