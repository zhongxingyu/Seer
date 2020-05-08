 /*
  * Jin Hyung Park (jp2105@columbia.edu)
  *
  * UserAgent Class
  * 
  * Contains information to connect SIP server, to make a call, and to receive a call
  */
 
 package com.columbia.ng911;
 
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.net.DatagramSocket;
 import java.util.Random;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 import org.zoolu.sdp.MediaDescriptor;
 import org.zoolu.sdp.MediaField;
 import org.zoolu.sdp.SessionDescriptor;
 import org.zoolu.sip.address.NameAddress;
 import org.zoolu.sip.address.SipURL;
 import org.zoolu.sip.call.Call;
 import org.zoolu.sip.call.CallListenerAdapter;
 import org.zoolu.sip.call.ExtendedCall;
 import org.zoolu.sip.call.SdpTools;
 import org.zoolu.sip.header.Header;
 import org.zoolu.sip.message.Message;
 import org.zoolu.sip.message.MessageFactory;
 import org.zoolu.sip.provider.SipProvider;
 import org.zoolu.sdp.AttributeField;
 
 import android.util.Log;
 import android.widget.TextView;
 
 public class UserAgent extends CallListenerAdapter {
 		private String serverIpAddress;
         protected SipProvider sip_provider;
         protected Call call;
         protected String from_url;
         protected String contact_url;
         
         private AppController appController;
         
         protected int t140_local_port;
         protected int t140_remote_port;
 
         /** Local sdp */
         protected String local_session = null;
         
         private void initSessionDescriptor() { 
             SessionDescriptor sdp = new SessionDescriptor(
                             this.from_url,
                             this.from_url);
   
             local_session = sdp.toString();
             local_session += "m=text "
             		+ Integer.toString(t140_local_port)
             		+ " RTP/AVP 99 98\r\na=fmtp:99 98/98/98\r\na=rtpmap:99 red/1000\r\na=rtpmap:98 t140/1000\r\n";
             
             Log.e("SIP:LOCAL_SDP", local_session);
         } 
 
         public UserAgent (SipProvider sip_provider, String from_url, String contact_url, T140Writer writer) {
         		Random rand = new Random();
                 this.sip_provider = sip_provider;
                 this.from_url = from_url;
                 this.contact_url = contact_url;
                 this.t140_local_port = rand.nextInt(50000) + 1024;
                 do {
                 	t140_local_port++;
                 } while (this.availablePort(t140_local_port) != true);
                 	
                 this.initSessionDescriptor();
                 
                 appController = new AppController("s", 
                 		new BufferedWriter(new OutputStreamWriter(writer)), 
                 		new BufferedWriter(new OutputStreamWriter(System.out)));
         }
         
         public String getLocalSDP() {
         	return local_session;
         }
 
         public boolean call(String target_url, String port) {
         	Log.e("UA", "Contact Url is = " + contact_url);
         	if (call == null)
         		call = new Call(sip_provider, from_url, contact_url, this);
             if (port != "5060") {
             	call.call(target_url+":"+port,local_session);
             	Log.e("CALL", target_url+":"+port);
             }
             else {
             	call.call(target_url,local_session);
             	Log.e("CALL", target_url);
             }
             this.serverIpAddress = target_url;
 
             return true;
         }
         
         public void hangup() {
         	if (appController != null)
         		appController.stop();
        	if (call != null)
         		call.hangup();
        	/*
        	call.cancel();
        	call.bye();
        	call.listen();
        	*/
         }
         
         public void sendRTT(char in) {
         	Log.e("UA", "RTT Sent [" + (int)in + "]");
         	appController.processInput(in);
         }
 
         private void sessionProduct(SessionDescriptor remote_sdp) {
                 SessionDescriptor local_sdp = new SessionDescriptor(local_session);
                 SessionDescriptor new_sdp = new SessionDescriptor(local_sdp
                                 .getOrigin(), local_sdp.getSessionName(), local_sdp
                                 .getConnection(), local_sdp.getTime());
                 
                 new_sdp.addMediaDescriptors(local_sdp.getMediaDescriptors());
                 new_sdp = SdpTools.sdpMediaProduct(new_sdp, remote_sdp
                                 .getMediaDescriptors());
                 
                 MediaDescriptor remote_m = remote_sdp.getMediaDescriptor("text");
                 MediaField remote_m_field = remote_m.getMedia();
                 
                 t140_remote_port = remote_m_field.getPort();
                 
                 AttributeField a1 = new AttributeField("rtpmap", "99 red/1000");
                 AttributeField a2 = new AttributeField("fmtp", "99 98/98/98");
                 AttributeField a3 = new AttributeField("rtpmap", "98 t140/1000");
                 Vector<AttributeField> v = new Vector<AttributeField>();
                 v.add(a1); v.add(a2); v.add(a3);
                 
                 new_sdp.addMedia(new MediaField("text", t140_local_port, 0, 
                 		remote_m_field.getTransport(), remote_m_field.getFormats()), v);
                 
                 local_session = new_sdp.toString();
                 if (call!=null) call.setLocalSessionDescriptor(local_session);
 
                 Log.e("SIP:UA - new SDP", new_sdp.toString());
                 Log.e("SIP:UA - remote text port = ", Integer.toString(t140_remote_port));
                 
                 //To Testing RTP Sesstion for RTT (Real Time Text)
             	appController.start(this.from_url, t140_local_port, this.serverIpAddress, t140_remote_port, 99, 98, 0);
         }
 
         public boolean listen() {
                 call = new ExtendedCall(sip_provider, from_url,
                                 contact_url, "android-test",
                                 "realm", "password", this);
 
                 call.listen();
 
                 return true;
         }
         
         public void onCallIncoming(Call call, NameAddress callee,
                 NameAddress caller, String sdp, Message invite) {
         	Log.e("SIP:UA", "Call Incoming! -> sdp ?" + sdp + "\n" + "-> invite ?"+invite+"\n");
 
         	SessionDescriptor remote_sdp = new SessionDescriptor(sdp);
 	        try {
 	                sessionProduct(remote_sdp);
 	        } catch (Exception e) { 
 	                // only known exception is no codec
 	                Log.e("SIP:UA", e.toString());
 	                return;
 	        }
         
 	        call.ring();
 	        call.accept(this.local_session);
         }
         
         public void onCallRinging(Call call, Message resp) {
         	Log.e("SIP:UA - onCallRinging - ", resp.toString());
         }
         
         public void onCallAccepted(Call call, String sdp, Message resp) {
         	Log.e("SIP:Outgoing", "Call Accepted");
         	//SessionDescriptor remote_sdp = new SessionDescriptor(sdp);
 	        //MediaDescriptor remote_m = remote_sdp.getMediaDescriptor("text");
 	        //Log.e("SIP:UA - md - ", remote_m.toString());
             //MediaField remote_m_field = remote_m.getMedia();
         	Log.e("RTT", sdp);
         	
             t140_remote_port = findTextPortonSDP(sdp);
         	
             //To Testing RTP Sesstion for RTT (Real Time Text)
             Log.e("SIP:RTT", "Local IP - " + this.from_url);
         	appController.start(this.from_url, t140_local_port, this.serverIpAddress, t140_remote_port, 99, 98, 0);
         }
         
         private int findTextPortonSDP(String sdp) {
         	int port = 0;
         	StringTokenizer strTokenizer = new StringTokenizer(sdp, "\r\n");
         	while (strTokenizer.hasMoreTokens()) {
         		String line = strTokenizer.nextToken();
         		if (line.startsWith("m=text")) {
         			StringTokenizer tmpToken = new StringTokenizer(line, " ");
         			String portStr = tmpToken.nextToken();
         			portStr = tmpToken.nextToken();
         			
         			port = new Integer(portStr);
         			
         			Log.e("SIP:UA - findPort ", "Port found - "+portStr);
         			
         			break;
         		}
         	}
         	return port;
         }
         
         public boolean availablePort(int port) {
             if (port < 1025 || port > 65534) {
                 throw new IllegalArgumentException("Invalid start port: " + port);
             }
 
             DatagramSocket ds = null;
             try {
                 ds = new DatagramSocket(port);
                 ds.setReuseAddress(true);
                 return true;
             } catch (IOException e) {
             } finally {
                 if (ds != null) {
                     ds.close();
                 }
             }
 
             return false;
         }
 }
