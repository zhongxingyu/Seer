 package ru.smsl.webkrona.service.xmlrpc.client;
 
 import java.util.List;
 import java.util.concurrent.atomic.AtomicInteger;
 import javax.annotation.PostConstruct;
 import org.springframework.util.StringUtils;
 import ru.smsl.webkrona.service.xmlrpc.KronaService;
 
 public class KronaClientService extends XmlRpcClientService {
 
     private final String handlerPrefix = "KRONA.";
     private String serverUrl;
 
     public void setServerUrl(String serverUrl) {
         this.serverUrl = serverUrl;
     }
     private final AtomicInteger UID = new AtomicInteger(0);
     private static final String SOURCE = "web";
     private static final int TONNPI_MSISDN = 0x91;
     private static final int TONNPI_DA = 0x91;
     private static final String DA = "KRONA";
     private static final int PID = 0x00;
     private static final int DCS = 0x04;
 
     /*
     private KronaService kronaService;
     
     @Override
     @PostConstruct
     protected void init() throws Exception {
     super.init();
     kronaService = getImpl(KronaService.class);
     }
      * 
      */
     @Override
     protected String getServerUrl() {
         return serverUrl;
     }
 
    private String processSMSSubmitDelemited(String msisdn, String...params) {
        return processSMSSubmit(msisdn, StringUtils.arrayToDelimitedString(params, " "));
    }
    
     private String processSMSSubmit(String msisdn, String UD) {
         Object[] params = new Object[]{
             UID.incrementAndGet(),
             SOURCE,
             TONNPI_MSISDN,
             msisdn,
             TONNPI_DA,
             DA,
             PID,
             DCS,
             UD
         };
         String result = (String) execute(handlerPrefix + "processSMSSubmit", params);
         logger.debug("{} exec processSMSSubmit(..., {}): {}", new Object[]{msisdn, UD, result});
         return result;
 //        return kronaService.processSMSSubmit(UID.incrementAndGet(), SOURCE, msisdn, TONNPI, DA, PID, DCS, UD);
     }
 
     public String accept(String msisdn) {
         return processSMSSubmit(msisdn, "#ВСТУПИТЬ");
     }
 
     public String add(String ownerMsisdn, String msisdn) {
        return processSMSSubmitDelemited(ownerMsisdn, "#ДОБАВИТЬ", msisdn);
     }
 
     public String createClosed(final String ownerMsisdn, final String groupName, final Integer hours, final List<String> msisdns) {
        return processSMSSubmitDelemited(ownerMsisdn,
                     "#СОЗДАТЬ",
                     groupName,
                     hours.toString(),
                     StringUtils.collectionToDelimitedString(msisdns, " ")
                );
     }
 
     public String createOpened(final String ownerMsisdn, final String groupName, final Integer hours, final List<String> msisdns) {
        return processSMSSubmitDelemited(ownerMsisdn,
                     "#СОЗДАТЬ_О",
                     groupName,
                     hours.toString(),
                     StringUtils.collectionToDelimitedString(msisdns, " ")
                );
     }
 
     public String delete(String ownerMsisdn) {
         return processSMSSubmit(ownerMsisdn, "#ЗАКРЫТЬ");
     }
 
     public String exit(String msisdn) {
         return processSMSSubmit(msisdn, "#ВЫЙТИ");
     }
 
     public Double getBalance(String msisdn) {
         Double result = (Double) execute(handlerPrefix + "getBalance", msisdn);
         logger.debug("{} exec getBalance: {}", msisdn, result);
         return result;
 //        return kronaService.getBalance(msisdn);
     }
 
     public String remove(String ownerMsisdn, String msisdn) {
         return processSMSSubmit(ownerMsisdn, "#ИСКЛЮЧИТЬ " + msisdn);
     }
 
     public String send(String msisdn, String text) {
         return processSMSSubmit(msisdn, text);
     }
 
     public void setTariff(String msisdn, int tariff) {
         logger.debug("{} exec setTariff: void", msisdn);
         execute(handlerPrefix + "setTariff", msisdn, tariff);
 //        kronaService.setTariff(msisdn, tariff);
     }
 
     public void registerUser(String msisdn, String email, String nick) {
         logger.debug("Trying register new user: {}, {}, {}", new Object[]{msisdn, email, nick});
         execute(handlerPrefix + "registerUser", msisdn, email, nick);
     }
 
     public String processDeliveryStatus(String status, String id) {
         String result = (String) execute(handlerPrefix + "processDeliveryStatus", status, id);
         logger.debug("execute processDeliveryStatus({}, {}): {}", new Object[]{status, id, result});
         return result;
     }
 }
