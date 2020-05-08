 package beans;
 
 import beans.config.Conf;
 import org.apache.commons.lang3.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
import play.modules.spring.Spring;
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
//
     public EventMonitor getEventMonitor(){
//        logger.info( "bean factory - getEventMonitor is invoked with mock [{}] and impl [{}] ", eventMonitorMock, eventMonitorImpl );

        // GUY _ NOTE _ VERY IMPORTANT - the "@Inject"ed fields for eventMonitorImpl and eventMonitorMock are null.
        // please refer to stackoverflow's question : http://stackoverflow.com/questions/15183145/autowiring-factory-bean
        // to read more about this.
         if ( StringUtils.isEmpty(conf.mixpanelApiKey) ){
           logger.info( "using mock eventMonitor" );
            return ( EventMonitor ) Spring.getBean( "eventMonitorMock" );// eventMonitorMock;
         }else{
             logger.info( "using impl eventMonitor" );
            return ( EventMonitor ) Spring.getBean( "eventMonitorImpl" ); // eventMonitor
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
