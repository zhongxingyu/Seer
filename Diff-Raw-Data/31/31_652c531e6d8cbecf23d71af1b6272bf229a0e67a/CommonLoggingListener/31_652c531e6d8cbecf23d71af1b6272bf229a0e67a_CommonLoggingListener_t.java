 package gov.nih.nci.caxchange.servicemix.jbi.audit;
 
 import gov.nih.nci.logging.api.user.UserInfoHelper;
 
 
 import java.security.Principal;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import javax.jbi.messaging.ExchangeStatus;
 import javax.jbi.messaging.MessageExchange;
 
 import javax.jbi.messaging.NormalizedMessage;
 
 import javax.security.auth.Subject;
 
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.apache.servicemix.jbi.audit.AbstractAuditor;
 import org.apache.servicemix.jbi.container.JBIContainer;
 import org.apache.servicemix.jbi.event.ExchangeEvent;
 import org.apache.servicemix.jbi.event.ExchangeListener;
 /**
  *  Audit exchanges sent from the NMR and the exchanges accepted into the NMR
  *  using Commons Logging API.
  *  
  *  @author Ekagra
  */
 public class CommonLoggingListener implements ExchangeListener {
 
     //Defines the default user to set is subject principal details are not available.
     private String defaultUser;
     private JBIContainer container;
     private boolean logExchangesSent = true;
     private boolean logExchangesAccepted=true;
     private List<String> filters = new ArrayList();
    private List<String> exchangeStatuses = new ArrayList();
     
 
     Logger logger = LogManager.getLogger(CommonLoggingListener.class);
     
     public CommonLoggingListener() {
     }
     
     public void doStart() {
         container.addListener(this);
     }
     
     public void doStop() {
         container.removeListener(this);
     }
     /**
      * 
      * @param exchangeEvent
      * @return
      */
     public String getUserFromExchangeEvent(ExchangeEvent exchangeEvent) {
         if (exchangeEvent == null) {
             return defaultUser;
         }
         MessageExchange messageExchange = exchangeEvent.getExchange();
         if (messageExchange == null) {
             return defaultUser;
         }
         try {
           NormalizedMessage message = messageExchange.getMessage("in");
           if (message == null) {
               message = messageExchange.getMessage("out");
           }
           if (message == null) {
               return defaultUser;
           }
           Subject subject =message.getSecuritySubject();
           if (subject == null) {
               return defaultUser;
           }
           Set<Principal> principals =subject.getPrincipals();
           if ((principals == null)||(principals.isEmpty())) {
               return defaultUser;
           }
           return principals.iterator().next().getName();
         }catch (Throwable e)   {
             return defaultUser;
         }
           
     }
     
     public boolean matchFilter(ExchangeEvent exchangeEvent) {
         if ((filters == null)||(filters.size()==0)) {
             return true;
         }
         boolean retVal = false;
         if (exchangeEvent == null) return retVal;
         MessageExchange messageExchange = exchangeEvent.getExchange();
         if (messageExchange==null) return retVal;
         String messageExchangeString = messageExchange.toString();
         for(String filter:filters) {
             if (messageExchangeString.indexOf(filter)>0) {
                 return true;
             }
         }
         return retVal;
     }
 
     /**
      * Log messages sent out of  the NMR.
      * 
      * @param exchangeEvent
      */
     public void exchangeSent(ExchangeEvent exchangeEvent) {
        if ((logExchangesSent)&&(matchFilter(exchangeEvent))) {
          MessageExchange messageExchange = exchangeEvent.getExchange();    
          boolean error = messageExchange.getStatus().equals(ExchangeStatus.ERROR);
          UserInfoHelper.setUserInfo(getUserFromExchangeEvent(exchangeEvent), messageExchange.getExchangeId());
          try {
        	 String messageExchangeAsString = messageExchange.toString();
        	 //Escape any single quotes :
        	 messageExchangeAsString = messageExchangeAsString.replace("'", "''");        	 
          if (error) {
             logger.error(messageExchangeAsString);
          }else {
             logger.info(messageExchangeAsString);
          }
          }catch(Exception e){
        	 //Log but ignore the exception as this is a logging error .
        	  logger.error("Error logging message exchange.", e);
          }
        }
          
     }
     /**
      * Log messages sent to the NMR
      * @param exchangeEvent
      */
     public void exchangeAccepted(ExchangeEvent exchangeEvent) {
         if ((logExchangesAccepted)&&(matchFilter(exchangeEvent))) {
            MessageExchange messageExchange = exchangeEvent.getExchange();    
            boolean error = messageExchange.getStatus().equals(ExchangeStatus.ERROR);
            UserInfoHelper.setUserInfo(getUserFromExchangeEvent(exchangeEvent), messageExchange.getExchangeId());
            try{
        	 String messageExchangeAsString = messageExchange.toString();
        	 //Escape any single quotes :
        	 messageExchangeAsString = messageExchangeAsString.replace("'", "''");
              if (error) {
                logger.error(messageExchangeAsString);
              }else {
                logger.info(messageExchangeAsString);
              }
            }catch(Exception e) {
        	 //Log but ignore the exception as this is a logging error .
        	   logger.error("Error logging message exchange.", e);
            }
         }
     }
 
     public void setDefaultUser(String defaultUser) {
         this.defaultUser = defaultUser;
     }
 
     public String getDefaultUser() {
         return defaultUser;
     }
 
     public void setContainer(JBIContainer container) {
         this.container = container;
     }
 
     public JBIContainer getContainer() {
         return container;
     }
 
     public void setLogExchangesSent(boolean logExchangesSent) {
         this.logExchangesSent = logExchangesSent;
     }
 
     public boolean isLogExchangesSent() {
         return logExchangesSent;
     }
 
     public void setLogExchangesAccepted(boolean logExchangesAccepted) {
         this.logExchangesAccepted = logExchangesAccepted;
     }
 
     public boolean isLogExchangesAccepted() {
         return logExchangesAccepted;
     }
 
     public void setFilters(List<String> filters) {
         this.filters = filters;
     }
 
     public List<String> getFilters() {
         return filters;
     }
 }
