 
 package com.richitec.donkey.sip.servlet;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.InetSocketAddress;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.sip.SipApplicationSession;
 import javax.servlet.sip.SipFactory;
 import javax.servlet.sip.SipServlet;
 import javax.servlet.sip.SipServletRequest;
 import javax.servlet.sip.SipServletResponse;
 import javax.servlet.sip.SipSession;
 import javax.servlet.sip.URI;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.stream.FactoryConfigurationError;
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamWriter;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import akka.actor.ActorRef;
 
 import com.richitec.donkey.ContextLoader;
 import com.richitec.donkey.conference.ConferenceManager;
 import com.richitec.donkey.conference.GlobalConfig;
import com.richitec.donkey.conference.actor.AttendeeActor;
 import com.richitec.donkey.conference.message.ActorMessage;
 import com.richitec.donkey.msml.BooleanType;
 import com.richitec.donkey.msml.DialogLanguageDatatype;
 import com.richitec.donkey.msml.Dtmf;
 import com.richitec.donkey.msml.ExitType;
 import com.richitec.donkey.msml.Msml;
 import com.richitec.donkey.msml.ObjectFactory;
 import com.richitec.donkey.msml.Play;
 
 
 /**
  * SipServlet implementation class CallInSipServlet
  */
 @javax.servlet.sip.annotation.SipServlet
 public class CallInSipServlet extends SipServlet {
 
     private static final long serialVersionUID = 3978425801979081269L;
     private static final Log log = LogFactory.getLog(CallInSipServlet.class);
     
     private static final String INVITE = "INVITE";
     private static final String BYE = "BYE";
     private static final String INFO = "INFO";
     
     public static final String MSML_CONTENT_TYPE = "application/msml+xml";
     
     private static final String USER_INVITE_REQUEST = "user_invite_request";
     private static final String LINKED_SESSION = "linked_session";
     private static final String MEDIA_SERVER_CONN = "media_server_conn";
     private static final String USER_SIPURI = "user_sip_uri";
     private static final String CONFERENCE_ID = "conference_id";
     private static final String SESSION_STATE = "session_state";
     private static final String COLLECT_DTMF_TIMES = "collect_dtmf_times";
     
     private enum State {NoConference, HasOneConference, HasMultiConference};
     
     private static final String No_Conference_Voice_Play_Done = "app.noConferenceVoicePlayDone";
     private static final String Join_Conference_Voice_Play_Done = "app.joinConferenceVoicePlayDone";
     private static final String Input_Conference_Voice_Play_Done = "app.inputConferenceVoicePlayDone";
     private static final String Collect_DTMF_Completed = "app.collectDtmfCompleted";
     private static final String Msml_Dialog_Exit = "msml.dialog.exit";
 
     //Reference to context - The ctx Map is used as a central storage for this app
     ServletContext ctx = null;
     private GlobalConfig config;
     private SipFactory sipFactory;
     private ConferenceManager conferenceManager;
     
 	private JAXBContext jc;
 	private Unmarshaller ju;
 	private Marshaller jm;
 	
 	private ObjectFactory msmlObjFactory;
 
     /*
      * Demonstrates extension with a new "REPUBLISH" method
      */
     @Override
     protected void doRequest(SipServletRequest req) throws ServletException, IOException {
         if( req.getMethod().equals("REPUBLISH") ) {
             doRepublish(req);
         } else {
             super.doRequest(req);
         }
     }
     
     /**
      * INVITE from user.
      */
     @Override
     protected void doInvite(SipServletRequest req)  throws ServletException, IOException {
     	//INVITE media server with user SDP.
 		SipApplicationSession sipAppSession = req.getApplicationSession();
 		String from = config.getSipUri();
 		String to = config.getMediaServerSipUri();
 		SipServletRequest invite = sipFactory.createRequest(sipAppSession, INVITE, from, to);
 		invite.setContent(req.getContent(), req.getContentType());
 		
 		SipSession userSession = req.getSession(false);
 		SipSession mediaServerSession = invite.getSession(false);
 		
 		userSession.setAttribute(LINKED_SESSION, mediaServerSession);
 		mediaServerSession.setAttribute(USER_INVITE_REQUEST, req);
 		mediaServerSession.setAttribute(LINKED_SESSION, userSession);
 		
 		mediaServerSession.setHandler(CallInSipServlet.class.getSimpleName());
 		
 		String outboundIPAddr = config.getOutboundIP();
 		Integer port = config.getOutboundPort();
 		InetSocketAddress address = new InetSocketAddress(outboundIPAddr, port);
 		mediaServerSession.setOutboundInterface(address);
 		
 		invite.send();
 	}
     
     private boolean hasConference(String confId){
     	ActorRef actor = conferenceManager.getConferenceActor(confId);
     	return null != actor;
     }
     
     private boolean joinConference(SipApplicationSession sipAppSession, SipSession userSession,
     		SipSession mediaServerSession, String sipUri, String conn, String confId){
     	ActorRef actor = conferenceManager.getConferenceActor(confId);
     	if (null == actor){
     		log.error("Cannot get actor for conference " + confId);
     		return false;
     	}
     	
     	conferenceManager.addAttendeeToConference(sipUri, confId);
     	actor.tell(new ActorMessage.EvtAttendeeCallInConference(sipAppSession, 
     			userSession, mediaServerSession, sipUri, conn));
     	
     	return true;
     }
     
     private Play createPlay(String audioUri, String eventName){
     	Play play = new Play();
     	play.setBarge(BooleanType.TRUE);
     	play.setCleardb(BooleanType.TRUE);
     	
     	Play.Audio audio = new Play.Audio();
     	audio.setUri(audioUri);
     	play.getAudioOrMedia().add(msmlObjFactory.createPlayAudio(audio));
     	
     	if (null!=eventName && eventName.length()>0){
     		Play.Playexit playExit = new Play.Playexit();
     		
 //    		Send send = new Send();
 //    		send.setTarget("source");
 //    		send.setEvent(eventName);
 //    		playExit.getContent().add(send);
     		
 	    	ExitType exit = new ExitType();
 	    	exit.setNamelist("play.end play.amt");
 	    	playExit.getContent().add(msmlObjFactory.createPlayPlayexitExit(exit));
     		
     		play.setPlayexit(playExit);
     	}
     	
     	return play;
     }
     
     private Msml.Dialogstart createDialogStart(String conn){
     	Msml.Dialogstart dialogstart = new Msml.Dialogstart();
     	dialogstart.setTarget("conn:" + conn);
     	dialogstart.setType(DialogLanguageDatatype.APPLICATION_MOML_XML);
     	return dialogstart;
     }
     
     private Dtmf createDtmf(){
     	Dtmf dtmf = new Dtmf();
     	
     	dtmf.setFdt("10s");
     	dtmf.setIdt("5s");
     	dtmf.setEdt("2s");
     	
     	Dtmf.Pattern pattern = new Dtmf.Pattern();
     	pattern.setDigits("min=1;max=9;rtk=#");
     	pattern.setFormat("moml+digits");
     	
     	dtmf.getPattern().add(pattern);
     	
     	Dtmf.Dtmfexit dtmfexit = new Dtmf.Dtmfexit();
     	
     	ExitType exit = new ExitType();
     	exit.setNamelist("dtmf.digits dtmf.end");
     	dtmfexit.getContent().add(msmlObjFactory.createDtmfDtmfexitExit(exit));
     	
 //		Send send = new Send();
 //		send.setTarget("source");
 //		send.setEvent(Collect_DTMF_Completed);
 //		send.setNamelist("dtmf.digits dtmf.end");
 //		dtmfexit.getContent().add(send);
     	
     	dtmf.setDtmfexit(dtmfexit);
     	return dtmf;
     }
     
     private String createMsml(Object msmlRequest){
     	Msml msml = new Msml();
     	msml.getMsmlRequest().add(msmlRequest);
     	
 		OutputStream os = new ByteArrayOutputStream();
 		try {
 			XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(os);
 			writer.setPrefix("cvd", "http://convedia.com/ext");
 			jm.marshal(msml, writer);
 		} catch (JAXBException e) {
 			e.printStackTrace();
 		} catch (XMLStreamException e) {
 			e.printStackTrace();
 		} catch (FactoryConfigurationError e) {
 			e.printStackTrace();
 		}
 		
 		return os.toString();
     }
     
     private void sendInfo(SipSession mediaServerSession, String content) throws IOException{
     	SipServletRequest info = mediaServerSession.createRequest(INFO);
     	info.setContent(content, MSML_CONTENT_TYPE);
     	info.send();
     }
     
     private void playAudio(SipSession mediaServerSession, String audioUri, String evName) throws IOException{
     	String conn = (String) mediaServerSession.getAttribute(MEDIA_SERVER_CONN);
     	Msml.Dialogstart dialogstart = createDialogStart(conn);
     	
     	Play play = createPlay(audioUri, evName);
     	Msml.Dialogstart.Group group = new Msml.Dialogstart.Group ();
     	group.setTopology();
     	group.setPlay(play);
     	dialogstart.setGroup(group);
     	
 //    	Play play2 = createPlay(config.getDelayVoice(), null);
 //    	dialogstart.setPrimitive(msmlObjFactory.createPlay(play2));
     	
     	String msmlContent = createMsml(dialogstart);
     	sendInfo(mediaServerSession, msmlContent);
     }
     
     private void collectDtmf(SipSession mediaServerSession, String audio) throws IOException{
     	
     	Integer t = (Integer)mediaServerSession.getAttribute(COLLECT_DTMF_TIMES);
     	if (null == t){
     		mediaServerSession.setAttribute(COLLECT_DTMF_TIMES, 1);
     	} else {
     		if (t<3){
     			mediaServerSession.setAttribute(COLLECT_DTMF_TIMES, t+1);
     		} else {
     			String phone = (String) mediaServerSession.getAttribute(USER_SIPURI);
     			log.warn("<" + phone + "> tried 3 times to input conference ID.");
     			SipSession userSession = (SipSession) mediaServerSession.getAttribute(LINKED_SESSION);
     			bye(userSession);
     			bye(mediaServerSession);
     			return;
     		}
     	}
     	
        	Play play = createPlay(audio, null);
     	Dtmf dtmf = createDtmf();
     	
     	Msml.Dialogstart.Group group = new Msml.Dialogstart.Group ();
     	group.setTopology();
     	group.setPlay(play);
     	group.setDtmf(dtmf);
     	
     	String conn = (String) mediaServerSession.getAttribute(MEDIA_SERVER_CONN);
     	Msml.Dialogstart dialogstart = createDialogStart(conn);
     	dialogstart.setGroup(group);
     	
 //    	Play play2 = createPlay(config.getDelayVoice(), null);
 //    	dialogstart.setPrimitive(msmlObjFactory.createPlay(play2));
     	
     	String msmlContent = createMsml(dialogstart);
     	sendInfo(mediaServerSession, msmlContent);
     }
     
     private void collectDtmf(SipSession mediaServerSession) throws IOException{
     	collectDtmf(mediaServerSession, config.getInputConferenceVoice());
     }
     
     private String getPhoneNumber(URI uri){
     	String strURI = uri.toString();
     	String [] list = strURI.split(":");
     	String str = list[1];
     	list = str.split("@");
     	return list[0];
     }
     
     /**
      * ACK from user.
      */
     @Override
     protected void doAck(SipServletRequest req) throws ServletException, IOException {
     	SipSession userSession = req.getSession(false);
     	SipSession mediaServerSession = (SipSession) userSession.getAttribute(LINKED_SESSION);
     	
     	//Get conference ID of this user.
     	String uri = getPhoneNumber(req.getFrom().getURI());
     	mediaServerSession.setAttribute(USER_SIPURI, uri);
     	log.debug("From URI: " + uri);
     	Set<String> confSet = conferenceManager.getConferenceByAttendee(uri);
     	if (null == confSet || confSet.size() <= 1){
     		log.warn("Find no conference for " + uri);
     		mediaServerSession.setAttribute(SESSION_STATE, State.NoConference);
     		collectDtmf(mediaServerSession);
 //    		playAudio(mediaServerSession, config.getNoConferenceVoice(), No_Conference_Voice_Play_Done);
     	} else if (confSet.size() == 1) {
     		// Join to conference
     		String confId = confSet.iterator().next();
     		mediaServerSession.setAttribute(CONFERENCE_ID, confId);
     		mediaServerSession.setAttribute(SESSION_STATE, State.HasOneConference);
     		playAudio(mediaServerSession, config.getJoinConferenceVoice(), Join_Conference_Voice_Play_Done);
     	} else { //confSet.size() > 1
     		log.warn("Find " + confSet.size() + " conferences for " + uri);
     		mediaServerSession.setAttribute(SESSION_STATE, State.HasMultiConference);
     		collectDtmf(mediaServerSession);
     	}
     }
     
     /**
      * INFO from media server.
      */
     @Override
     protected void doInfo(SipServletRequest req) throws ServletException, IOException {
     	log.debug("CSeq : " + req.getHeader("cseq"));
     	SipServletResponse resp = req.createResponse(SipServletResponse.SC_OK);
     	resp.send();
     	
 		InputStream iStream = new ByteArrayInputStream(req.getRawContent());
 		try {
 			Msml msml = (Msml) this.ju.unmarshal(iStream);
 			Msml.Event event = msml.getEvent();
 			SipSession mediaServerSession = req.getSession(false);
 			SipSession userSession = (SipSession) mediaServerSession.getAttribute(LINKED_SESSION);
 			onMediaServerEvent(event, userSession, mediaServerSession);
 		} catch (JAXBException e) {
 			log.error(e.getMessage());
 			e.printStackTrace();
 		}
     }
     
     private void bye(SipSession session) throws IOException{
     	SipServletRequest bye = session.createRequest(BYE);
     	bye.send();
     }
     
     private Map<String, String> getKeyValuePairs(List<JAXBElement<String>> pairs){
     	Map<String, String> map = new HashMap<String, String>();
     	for (int i=0; i<pairs.size(); i+=2){
     		JAXBElement<String> k = pairs.get(i);
     		JAXBElement<String> v = pairs.get(i+1);
     		map.put(k.getValue(), v.getValue());
     	}
     	return map;
     }
     
     private void onMediaServerEvent(Msml.Event event, 
     		SipSession userSession, SipSession mediaServerSession) throws IOException{
     	String name = event.getName();
     	String id = event.getId();
     	String phone = (String) mediaServerSession.getAttribute(USER_SIPURI);
     	log.debug("media server event : name=" + name + ", id=" + id);
     	if (Msml_Dialog_Exit.equals(name)){
     		Map<String, String> kv = getKeyValuePairs(event.getNameAndValue());
     		if (kv.containsKey("dtmf.end")){
     			String userInputConfId = kv.get("dtmf.digits"); 
     			if (!userInputConfId.endsWith("#")){
     				log.warn("\nInvalid conference id <" + userInputConfId + "> from " + phone);
     				collectDtmf(mediaServerSession);
     			} else {
     				String confId = userInputConfId.substring(0, userInputConfId.length()-1);
     				boolean result = hasConference(confId);
     				if (result){
     					mediaServerSession.setAttribute(CONFERENCE_ID, confId);
     					playAudio(mediaServerSession, config.getJoinConferenceVoice(), 
     							Join_Conference_Voice_Play_Done);
     				} else {
     					collectDtmf(mediaServerSession, config.getNoConferenceVoice());
     				}
     			}
     		} else if (kv.containsKey("play.end")){
 	    		String sipUri = (String) mediaServerSession.getAttribute(USER_SIPURI);
 	    		String conn = (String) mediaServerSession.getAttribute(MEDIA_SERVER_CONN);
 	    		String confId = (String) mediaServerSession.getAttribute(CONFERENCE_ID);
 	    		joinConference(userSession.getApplicationSession(), userSession,
 	    				mediaServerSession, sipUri, conn, confId);
     		}
     	} else
     	if (No_Conference_Voice_Play_Done.equals(name)){
     		bye(userSession);
     		bye(mediaServerSession);
     	} else 
     	if (Collect_DTMF_Completed.equals(name)){
     		List<JAXBElement<String>> pairs = event.getNameAndValue();
     		for (int i=0; i<pairs.size(); i+=2){
         		JAXBElement<String> k = pairs.get(i);
         		JAXBElement<String> v = pairs.get(i+1);
         		if (k.getValue().equals("dtmf.digits")){
         			String userInputConfId = v.getValue();
         			if (!userInputConfId.endsWith("#")){
         				log.warn("Invalid conference id : " + userInputConfId);
         				collectDtmf(mediaServerSession);
         			} else {
         				String confId = userInputConfId.substring(0, userInputConfId.length()-1);
         				String sipUri = (String) mediaServerSession.getAttribute(USER_SIPURI);
         				String conn = (String) mediaServerSession.getAttribute(MEDIA_SERVER_CONN);
         				boolean result = joinConference(userSession.getApplicationSession(), userSession,
         						mediaServerSession, sipUri, conn, confId);
         				if (!result){
         					log.warn("Cannot get conference actor for : " + confId);
         					collectDtmf(mediaServerSession);
         				}
         			}
         		}
     		}
     	} else if (Join_Conference_Voice_Play_Done.equals(name)) {
     		String sipUri = (String) mediaServerSession.getAttribute(USER_SIPURI);
     		String conn = (String) mediaServerSession.getAttribute(MEDIA_SERVER_CONN);
     		String confId = (String) mediaServerSession.getAttribute(CONFERENCE_ID);
     		joinConference(userSession.getApplicationSession(), userSession,
     				mediaServerSession, sipUri, conn, confId);
     	} else {
     		log.info("unhandled event : " + name);
     	}
     }
     
     /**
      * BYE from user or media server.
      */
     @Override
     protected void doBye(SipServletRequest req) throws ServletException, IOException {
     	SipSession session = req.getSession(false);
	    ActorRef actor = (ActorRef) session.getAttribute(AttendeeActor.Actor);
	    if (null != actor){
	        actor.tell(new ActorMessage.SipByeRequest(session));
	    } else {
	        log.error("AttendeeActor == null for SIP session " + session.getId());
	        SipSession linkedSession = (SipSession) session.getAttribute(LINKED_SESSION);
	        if (null != linkedSession){ 
	            SipServletRequest bye = linkedSession.createRequest(BYE);
	            bye.send();
	        } else {
	            log.error("Cannot find linked session!");
	        }	        
	    }
 		
     	SipServletResponse resp = req.createResponse(SipServletResponse.SC_OK);
     	resp.send();
 	}
     
     @Override
     protected void doResponse(SipServletResponse resp) throws ServletException, IOException {
 		String method = resp.getMethod();
 		if (INVITE.equals(method)){
 			super.doResponse(resp);
 		}
 	}
     
     /**
      * 200 OK from media server.
      */
     @Override
     protected void doSuccessResponse(SipServletResponse resp) throws ServletException, IOException {
     	String conn = resp.getTo().getParameter("tag");
     	
     	log.debug("conn : " + conn);
     
     	SipServletRequest ack = resp.createAck();
     	ack.send();
     	
     	SipSession session = resp.getSession(false);
     	session.setAttribute(MEDIA_SERVER_CONN, conn);
 		SipServletRequest userInvite = (SipServletRequest) session.getAttribute(USER_INVITE_REQUEST);
 		if (null != userInvite){
 			SipServletResponse ok = userInvite.createResponse(SipServletResponse.SC_OK);
 			ok.setContent(resp.getContent(), resp.getContentType());
 			ok.send();
 		}
 	}
     
     /*
      * Implement the REPUBLISH extension here
      */    
     protected void doRepublish(SipServletRequest req) throws ServletException, IOException {
 		// TODO Auto-generated method stub
     }
     
     @Override
     public void init(ServletConfig cfg) throws ServletException {
         super.init(cfg);
         ctx = cfg.getServletContext();
         config = ContextLoader.getGlobalConfig();
         sipFactory = ContextLoader.getSipFactory();
         conferenceManager = ContextLoader.getConfereneManager();
         
         msmlObjFactory = new ObjectFactory(); 
         
 		try {
 			this.jc = JAXBContext.newInstance(Msml.class);
 			this.ju = jc.createUnmarshaller();
 			this.jm = jc.createMarshaller();
 		} catch (JAXBException e) {
 			e.printStackTrace();
 		}
     }
     
 }
