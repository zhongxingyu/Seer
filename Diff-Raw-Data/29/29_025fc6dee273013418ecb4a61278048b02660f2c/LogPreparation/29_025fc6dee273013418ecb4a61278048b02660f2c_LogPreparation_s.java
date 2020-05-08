 package org.jinglenodes.log;
 
 import org.apache.log4j.Logger;
 import org.jinglenodes.jingle.transport.Candidate;
 import org.jinglenodes.jingle.transport.RawUdpTransport;
 import org.jinglenodes.prepare.CallPreparation;
 import org.jinglenodes.session.CallSession;
 import org.xmpp.tinder.JingleIQ;
 import org.zoolu.sip.message.Message;
 import org.zoolu.sip.message.SipChannel;
 
 /**
  * Created with IntelliJ IDEA.
  * User: thiago
  * Date: 6/11/12
  * Time: 10:37 AM
  * To change this template use File | Settings | File Templates.
  */
 public class LogPreparation extends CallPreparation {
 
     final static Logger log = Logger.getLogger(LogPreparation.class);
 
     @Override
     public boolean prepareInitiate(JingleIQ iq, CallSession session) {
         return true;
     }
 
     @Override
     public boolean proceedInitiate(JingleIQ iq, CallSession session) {
         final StringBuilder str = new StringBuilder("INIT\t");
         str.append(iq.getJingle().getSid()).append("\t");
         str.append(iq.getJingle().getInitiator()).append("\t");
         str.append(iq.getJingle().getResponder()).append("\t");
         str.append(getIp(iq)).append("\t");
         log.info(str.toString());
         return true;
     }
 
     private String getIp(JingleIQ iq) {
         if (iq.getJingle() != null && iq.getJingle().getContent() != null) {
             final RawUdpTransport transport = iq.getJingle().getContent().getTransport();
             if (transport != null && transport.getCandidates().size() > 0) {
                 final Candidate c = transport.getCandidates().get(0);
                 if ("srflx".equals(c.getType())) {
                     return c.getIp();
                 }
             }
         }
         return "unknown";
     }
 
     @Override
     public boolean proceedTerminate(JingleIQ iq, CallSession session) {
         final StringBuilder str = new StringBuilder("TERMINATE\t");
         str.append(iq.getJingle().getSid()).append("\t");
         str.append(iq.getJingle().getInitiator()).append("\t");
         str.append(iq.getJingle().getResponder()).append("\t");
         str.append(iq.getJingle().getReason().getType()).append("\t");
         log.info(str.toString());
         return true;
     }
 
     @Override
     public boolean proceedAccept(JingleIQ iq, CallSession session) {
        final StringBuilder str = new StringBuilder("INIT\t");
         str.append(iq.getJingle().getSid()).append("\t");
         str.append(iq.getJingle().getInitiator()).append("\t");
         str.append(iq.getJingle().getResponder()).append("\t");
         str.append(getIp(iq)).append("\t");
         log.info(str.toString());
         return true;
     }
 
     @Override
     public boolean prepareInitiate(Message msg, CallSession session, SipChannel channel) {
         return true;
     }
 
     @Override
     public boolean proceedSIPInitiate(JingleIQ iq, CallSession session, SipChannel channel) {
         return true;
     }
 
     @Override
     public JingleIQ proceedSIPEarlyMedia(JingleIQ iq, CallSession session, SipChannel channel) {
         return iq;
     }
 
     @Override
     public boolean proceedSIPTerminate(JingleIQ iq, CallSession session, SipChannel channel) {
         return true;
     }
 
     @Override
     public JingleIQ proceedSIPAccept(JingleIQ iq, CallSession session, SipChannel channel) {
         return iq;
     }
 }
