 package org.linphone.jlinphone.media.jsr135;
 
 import org.linphone.jlinphone.media.AudioStream;
 import org.linphone.jlinphone.media.AudioStreamParameters;
 import org.linphone.jortp.JOrtpFactory;
 import org.linphone.jortp.RtpException;
 import org.linphone.jortp.RtpSession;
 import org.linphone.jortp.SocketAddress;
 
 public class AudioStreamImpl implements AudioStream {
 	private RecvStream mRecvStream;
 	private SendStream mSendStream;
 	
 	private RtpSession mSession;
 	
 	public void stop() {
		if (mRecvStream!=null)
			mRecvStream.stop();
		if (mSendStream!=null)
			mSendStream.stop();
 		mSession.close();
 	}
 	public void init(SocketAddress local) throws RtpException {
 		mSession=JOrtpFactory.instance().createRtpSession();
 		mSession.setLocalAddr(local);
 	}
 	public void start(AudioStreamParameters params) throws RtpException {
 		
 		mSession.setRemoteAddr(params.getRemoteDest());
 		mSession.setProfile(params.getRtpProfile());
 		mSession.setSendPayloadTypeNumber(params.getActivePayloadTypeNumber());
 		mSession.setRecvPayloadTypeNumber(params.getActivePayloadTypeNumber());
 		
 		mRecvStream=new RecvStream(mSession);
 		mSendStream=new SendStream(mSession);
 		mRecvStream.start();
 		mSendStream.start();
 	}
 	
 }
