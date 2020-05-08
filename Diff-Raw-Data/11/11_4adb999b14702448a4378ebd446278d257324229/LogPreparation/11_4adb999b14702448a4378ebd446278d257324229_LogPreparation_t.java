 package org.jinglenodes.log;
 
 import org.apache.log4j.Logger;
 import org.jinglenodes.jingle.Info;
 import org.jinglenodes.jingle.Jingle;
 import org.jinglenodes.jingle.Reason;
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
     final static public String DEFAULT_BLANK = "-";
     final static public String DEFAULT_UNKNOWN = "unknown";
 
     @Override
     public boolean prepareInitiate(JingleIQ iq, CallSession session) {
         return true;
     }
 
     @Override
     public boolean proceedInitiate(JingleIQ iq, CallSession session) {
         log.info(_createLine(iq));
         return true;
     }
 
     private String getReason(final JingleIQ iq) {
         if (iq.getJingle() != null && iq.getJingle().getReason() != null) {
             final Reason.Type t = iq.getJingle().getReason().getType();
             return t == null ? DEFAULT_BLANK : t.toString();
         }else if(iq.getJingle() != null && iq.getJingle().getInfo()!=null){
             final Info.Type info = iq.getJingle().getInfo().getType();
             return info == null ? DEFAULT_BLANK: info.toString();
         }
         return DEFAULT_BLANK;
     }
 
     private String getIp(final JingleIQ iq) {
         if (iq.getJingle() != null && iq.getJingle().getContent() != null) {
             final RawUdpTransport transport = iq.getJingle().getContent().getTransport();
             if (transport != null && transport.getCandidates().size() > 0) {
                 final Candidate c = transport.getCandidates().get(0);
                 if ("srflx".equals(c.getType())) {
                     return c.getIp();
                 }
             }
         }
         return DEFAULT_UNKNOWN;
     }
 
     @Override
     public boolean proceedTerminate(JingleIQ iq, CallSession session) {
         log.info(_createLine(iq));
         return true;
     }
 
     @Override
     public boolean proceedAccept(JingleIQ iq, CallSession session) {
         log.info(_createLine(iq));
         return true;
     }
 
     @Override
     public void proceedInfo(JingleIQ iq, CallSession session) {
         log.info(_createLine(iq));
     }
 
     private String _createLine(final JingleIQ iq) {
         final Jingle j = iq.getJingle();
         return _createLine(j.getAction(), j.getSid(), getReason(iq), j.getInitiator(), j.getResponder(), getIp(iq));
     }
 
     private String _createLine(final String action, final String sid, final String reasonType, final String initiator, final String responder, final String ip) {
         final StringBuilder str = new StringBuilder();
         str.append(action).append("\t");
         str.append(sid).append("\t");
         str.append(initiator).append("\t");
         str.append(responder).append("\t");
         str.append(reasonType).append("\t");
         str.append(ip).append("\t");
         return str.toString();
     }
 
     @Override
     public boolean prepareInitiate(Message msg, CallSession session, SipChannel channel) {
         return true;
     }
 
     @Override
     public JingleIQ proceedSIPInitiate(JingleIQ iq, CallSession session, SipChannel channel) {
         return iq;
     }
 
     @Override
     public void proceedSIPInfo(JingleIQ iq, CallSession session, SipChannel channel) {
         log.info(_createLine(iq));
     }
 
     @Override
     public JingleIQ proceedSIPEarlyMedia(JingleIQ iq, CallSession session, SipChannel channel) {
         return iq;
     }
 
     @Override
     public boolean proceedSIPTerminate(JingleIQ iq, CallSession session, SipChannel channel) {
        log.info(_createLine(iq));
         return true;
     }
 
     @Override
     public JingleIQ proceedSIPAccept(JingleIQ iq, CallSession session, SipChannel channel) {
         log.info(_createLine(iq));
         return iq;
     }
 }
