 package beans;
 
 import beans.config.Conf;
 import org.apache.commons.lang3.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import server.EventMonitor;
 
 import javax.inject.Inject;
 
 /**
  * User: guym
  * Date: 2/27/13
  * Time: 9:57 AM
  */
 public class BeansFactory {
     @Inject
     private Conf conf;
 
     private static Logger logger = LoggerFactory.getLogger( BeansFactory.class );
 
     @Inject
     private EventMonitor eventMonitorImpl;
 
     @Inject
     private EventMonitor eventMonitorMock;

     public EventMonitor getEventMonitor(){
         if ( StringUtils.isEmpty(conf.mixpanelApiKey) ){
           logger.info( "using mock eventMonitor" );
            return eventMonitorMock;
         }else{
             logger.info( "using impl eventMonitor" );
            return eventMonitorImpl;
         }
     }
 
 
     public void setConf( Conf conf )
     {
         this.conf = conf;
     }
 
     public void setEventMonitorImpl( EventMonitor eventMonitorImpl )
     {
         this.eventMonitorImpl = eventMonitorImpl;
     }
 
     public void setEventMonitorMock( EventMonitor eventMonitorMock )
     {
         this.eventMonitorMock = eventMonitorMock;
     }
 }
