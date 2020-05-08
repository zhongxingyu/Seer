 package org.xmpp;
 
 import junit.framework.TestCase;
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.DocumentHelper;
 import org.jinglenodes.jingle.Info;
 import org.jinglenodes.jingle.Jingle;
 import org.jinglenodes.jingle.Reason;
 import org.jinglenodes.jingle.content.Content;
 import org.jinglenodes.jingle.description.Description;
 import org.jinglenodes.jingle.description.Payload;
 import org.jinglenodes.jingle.processor.JingleProcessor;
 import org.jinglenodes.jingle.processor.JingleSipException;
 import org.jinglenodes.jingle.transport.Candidate;
 import org.jinglenodes.jingle.transport.RawUdpTransport;
 import org.jinglenodes.sip.processor.SipProcessor;
 import org.xmpp.packet.IQ;
 import org.xmpp.tinder.JingleIQ;
 import org.zoolu.sip.message.Message;
 
 public class TestParser extends TestCase {
 
     final private String source = "<jingle xmlns=\"urn:xmpp:jingle:1\" action=\"session-initiate\" sid=\"abc\" initiator=\"a@a.com\" responder=\"b@b.com\">\n" +
             "  <content creator=\"initiator\" name=\"audio\" senders=\"both\">\n" +
             "    <description xmlns=\"urn:xmpp:jingle:apps:rtp:1\" media=\"audio\"></description>\n" +
             "    <transport xmlns=\"urn:xmpp:jingle:transports:raw-udp:1\">\n" +
             "      <candidate ip=\"10.166.108.22\" port=\"10000\" generation=\"0\" type=\"host\"/>\n" +
             "    </transport>\n" +
             "  </content>\n" +
             "</jingle>";
     final private String altSource = "<jingle xmlns=\"urn:xmpp:jingle:1\" action=\"session-initiate\" sid=\"abc\" initiator=\"a@a.com\" responder=\"b@b.com\">  <content creator=\"initiator\" name=\"audio\" senders=\"both\"><description xmlns=\"urn:xmpp:jingle:apps:rtp:1\" media=\"audio\">      <payload-type id=\"18\" name=\"G729\" clockrate=\"0\" channels=\"1\"/></description><transport xmlns=\"urn:xmpp:jingle:transports:raw-udp:1\">      <candidate ip=\"10.166.108.22\" port=\"10000\" generation=\"0\" type=\"host\"/></transport></content></jingle>";
     final String initiator = "a@a.com";
     final String responder = "b@b.com";
 
     public void testGenParser() {
         final Jingle jingle = new Jingle("abc", initiator, responder, Jingle.SESSION_INITIATE);
         jingle.setContent(new Content("initiator", "audio", "both", new Description("audio"), new RawUdpTransport(new Candidate("10.166.108.22", "10000", "0"))));
         jingle.getContent().getDescription().addPayload(Payload.G729);
         final JingleIQ jingleIQ = new JingleIQ(jingle);
         //assertEquals(jingleIQ.getChildElement().element("jingle").asXML(), source);
         System.out.println(jingleIQ.toXML());
         final JingleIQ jingleIQParsed = JingleIQ.fromXml(jingleIQ);
         System.out.println(jingleIQParsed.getChildElement().element("jingle").asXML());
         //assertEquals(source, jingleIQParsed.getChildElement().element("jingle").asXML());
         //assertEquals(jingleIQParsed.getJingle().getInitiator(), initiator);
         JingleIQ.getStream().fromXML(altSource);
         System.out.println(source);
     }
 
     final private String sourceTerminate = "<jingle xmlns=\"urn:xmpp:jingle:1\" action=\"session-terminate\" sid=\"abc\" initiator=\"a@a.com\" responder=\"b@b.com\">\n" +
            "<reason><success/></reason>\n" +
             "</jingle>";
 
 
     public void testDoubleParse() throws DocumentException {
 
         final String initiator = "romeo@localhost";
         final String responder = "juliet@localhost";
         final String packet = "<jingle xmlns=\"urn:xmpp:jingle:1\" action=\"session-initiate\" initiator=\"" + initiator + "\" responder=\"" + responder + "\" sid=\"37665\"><content xmlns=\"\" creator=\"initiator\" name=\"audio\" senders=\"both\"><description xmlns=\"urn:xmpp:jingle:apps:rtp:1\"><payload-type xmlns=\"\" id=\"0\" name=\"PCMU\"/></description><transport xmlns=\"urn:xmpp:jingle:transports:raw-udp:1\"><candidate xmlns=\"\" ip=\"192.168.20.172\" port=\"22000\" generation=\"0\"/></transport></content></jingle>";
 
         Document doc = DocumentHelper.parseText(packet);
 
         final IQ iq = new IQ(doc.getRootElement());
         final JingleIQ jingleIQ = JingleIQ.fromXml(iq);
         jingleIQ.setFrom(initiator);
         jingleIQ.setTo("sip.localhost");
 
         final JingleIQ newJingle = JingleIQ.fromXml(jingleIQ);
         assertTrue(newJingle.getJingle().getContent().getDescription() != null);
     }
 
    public void testGenParserTerminate() {
         final Jingle jingle = new Jingle("abc", initiator, responder, Jingle.SESSION_TERMINATE);
         jingle.setReason(new Reason(Reason.Type.success));
         final JingleIQ jingleIQ = new JingleIQ(jingle);
        //assertEquals(jingleIQ.getChildElement().element("jingle").asXML(), sourceTerminate);
         System.out.println(jingleIQ.toXML());
         final JingleIQ jingleIQParsed = JingleIQ.fromXml(jingleIQ);
         System.out.println(jingleIQParsed.getChildElement().element("jingle").asXML());
         assertEquals(sourceTerminate, jingleIQParsed.getChildElement().element("jingle").asXML());
         assertEquals(jingleIQParsed.getJingle().getInitiator(), initiator);
     }
 
     public void testRingingPacket() {
 
         final String initiator = "romeo@localhost";
         final String responder = "juliet@localhost";
 
         final Jingle jingle = new Jingle("12121", initiator, responder, Jingle.SESSION_INFO);
         jingle.setInfo(new Info());
         final JingleIQ iq = new JingleIQ(jingle);
         iq.setTo(initiator);
         iq.setFrom(responder);
 
         System.out.println(jingle.toString());
         System.out.println(iq.toXML());
 
     }
 
     public void testSIPParsing() throws JingleSipException {
         final String sipString = "SIP/2.0 200 OK\n" +
                 "Via: SIP/2.0/UDP 194.183.72.28:5060;branch=z9hG4bK7942901908306987;received=178.33.112.237;rport=5062\n" +
                 "From: \"+31651827042@yuilop.tv\" <sip:+31651827042@yuilop.tv>;tag=A(1.6)(6295399E195B795057FD01CF6D65301CB2E41499)\n" +
                 "To: \"0031611537782\" <sip:0031611537782@sip.yuilop.tv>;tag=as3efe9f43\n" +
                 "Call-ID: 7942901908306987\n" +
                 "CSeq: 1 INVITE\n" +
                 "User-Agent: Asterisk PBX 1.6.0.26-FONCORE-r78\n" +
                 "Allow: INVITE, ACK, CANCEL, OPTIONS, BYE, REFER, SUBSCRIBE, NOTIFY, INFO\n" +
                 "Supported: replaces, timer\n" +
                 "Contact: <sip:0031611537782@194.183.72.28>\n" +
                 "Content-Type: application/sdp\n" +
                 "Content-Length: 124\n" +
                 "\n" +
                 "v=0\n" +
                 "o=root 2139395421 2139395422 IN IP4 194.183.72.28\n" +
                 "s=Asterisk PBX 1.6.0.26-FONCORE-r78\n" +
                 "c=IN IP4 194.183.72.28\n" +
                 "t=0 0\n" +
                 "m=audio 0 RTP/AVP 18 112 3 0";
 
         final Message m = new Message(sipString);
 
         final Content c = SipProcessor.getContent("v=0\n" +
                 "o=root 2139395421 2139395422 IN IP4 194.183.72.28\n" +
                 "s=Asterisk PBX 1.6.0.26-FONCORE-r78\n" +
                 "c=IN IP4 194.183.72.28\n" +
                 "t=0 0\n" +
                 "m=audio 0 RTP/AVP 18 112 3 0");
 
 
         System.out.println(m.toString());
     }
 
 }
