 package com.richitec.donkey.sip.listener;
 
 import javax.servlet.sip.SipApplicationSession;
 import javax.servlet.sip.SipApplicationSessionEvent;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.richitec.donkey.conference.actor.ControlChannelActor;
 import com.richitec.donkey.conference.message.ActorMessage;
 
 import akka.actor.ActorRef;
 
 @javax.servlet.sip.annotation.SipListener
 public class SipApplicationSessionListener 
 		implements javax.servlet.sip.SipApplicationSessionListener {
 
 	private static final Log log = LogFactory.getLog(SipApplicationSessionListener.class);
 	
 	@Override
 	public void sessionCreated(SipApplicationSessionEvent ev) {	    
 		log.debug("\nControl Channel Session <" 
 		        + ev.getApplicationSession().getId() + "> Created");
 	}
 
 	@Override
 	public void sessionDestroyed(SipApplicationSessionEvent ev) {
         log.debug("\nControl Channel Session <" 
                 + ev.getApplicationSession().getId() + "> Destroyed");
 	}
 
 	/**
 	 * SipApplicationSession Expired 以后，不会触发sessionReadyToInvalidate，
 	 * 而是直接进入 sessionDestroyed。该SipApplicationSession的所有SipSession
 	 * 也是直接进入 sessionDestroyed。
 	 */
 	@Override
 	public void sessionExpired(SipApplicationSessionEvent ev) {
 		SipApplicationSession sipAppSession = ev.getApplicationSession();
 		
 		Integer expireMinutes =
 			(Integer) sipAppSession.getAttribute(ControlChannelActor.ExpireMinutes);
 		
 		if (null == expireMinutes){
 			//this SipApplicationSession is not control channel
 			sipAppSession.setExpires(10);
 			return;
 		}
 		
 		//control channel SipApplicationSession
 		if (expireMinutes > 0){
 			expireMinutes -= 1;
 			sipAppSession.setAttribute(ControlChannelActor.ExpireMinutes, expireMinutes);
 			sipAppSession.setExpires(1);
 		}
 		
 		String confId = 
             (String) sipAppSession.getAttribute(ControlChannelActor.ConferenceId);
 	      
 		if (expireMinutes <= 0){
 		    log.warn("\nAPP SESSION <" + sipAppSession.getId() + 
 		            "> of Conference <"+ confId + "> Expired");
 		    destroyConference(sipAppSession);
 		    return;
 		}
 		
 		Integer joinCount = 
 		    (Integer) sipAppSession.getAttribute(ControlChannelActor.JoinCount);
 		if (null == joinCount){
             log.error("Cannot get JoinCount of APP SESSION <" 
                     + sipAppSession.getId() + "> of Conference <" + confId + ">");
 		    return;
 		}
 		
		log.debug("Conference <" + confId + "> JoinCount = " + joinCount);
 		if (joinCount > 0){
		    sipAppSession.setAttribute(ControlChannelActor.EmptyCount, 0);
 		    return;
 		}
 		
 		//joinCount <= 0
 		Integer emptyCount = 
 		    (Integer) sipAppSession.getAttribute(ControlChannelActor.EmptyCount);
 		
 		emptyCount = (null == emptyCount) ? 0 : emptyCount+1;
 		if (emptyCount < 5){
 		    sipAppSession.setAttribute(ControlChannelActor.EmptyCount, emptyCount);
 		    return;
 		}
 		
 		log.warn("Not any phone call at least " + emptyCount + " Minutes, " +
 				"so destroy conference <" + confId + ">");
 		destroyConference(sipAppSession);
 	}
 
 	@Override
 	public void sessionReadyToInvalidate(SipApplicationSessionEvent ev) {
 	    SipApplicationSession sipAppSession = ev.getApplicationSession();
 	    String confId = 
             (String) sipAppSession.getAttribute(ControlChannelActor.ConferenceId);
 		log.debug("\nConference <" + confId + 
 		        "> Control Channel Session <" + ev.getApplicationSession().getId() + "> ReadyToInvalidate");	
 	}
 
 	private void destroyConference(SipApplicationSession sipAppSession){
         ActorRef actor = 
             (ActorRef) sipAppSession.getAttribute(ControlChannelActor.ConferenceActor);
         if (null != actor){
             actor.tell(ActorMessage.controlChannelExpired);
         } else {
             //will be never happened
             log.error("Cannot get ConferenceActor of APP SESSION <" 
                     + sipAppSession.getId() + ">");
         }
 	}
 }
