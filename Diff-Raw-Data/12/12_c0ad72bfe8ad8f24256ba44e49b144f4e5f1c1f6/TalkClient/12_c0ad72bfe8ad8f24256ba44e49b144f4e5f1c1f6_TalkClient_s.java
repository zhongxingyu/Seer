 package com.zhuri.talk;
 
 import java.nio.*;
 import java.io.IOException;
 
 import com.zhuri.slot.*;
 import com.zhuri.util.DEBUG;
 import com.zhuri.net.Connector;
 import com.zhuri.net.XyConnector;
 import com.zhuri.net.IConnectable;
 import com.zhuri.slot.IWaitableChannel;
 import com.zhuri.net.WaitableSslChannel;
 import com.zhuri.talk.protocol.Body;
 import com.zhuri.talk.protocol.Bind;
 import com.zhuri.talk.protocol.Packet;
 import com.zhuri.talk.protocol.Stream;
 import com.zhuri.talk.protocol.Session;
 import com.zhuri.talk.protocol.IQPacket;
 import com.zhuri.talk.protocol.Starttls;
 import com.zhuri.talk.protocol.PlainSasl;
 import com.zhuri.talk.protocol.Message;
 import com.zhuri.talk.protocol.Presence;
 
 public class TalkClient {
 	final static int WF_RESOLV = 0x00000001;
 	final static int WF_HEADER = 0x00000002;
 	final static int WF_FEATURE = 0x00000004;
 	final static int WF_PROCEED = 0x00000008;
 	final static int WF_SUCCESS  = 0x00000010;
 	final static int WF_STARTTLS = 0x00000040;
 	final static int WF_CONNECTED = 0x00000080;
 	final static int WF_HANDSHAKE = 0x00000100;
 	final static int WF_PLAINSASL = 0x00000200;
 	final static int WF_LASTSTART = 0x00000400;
 
 	final static int WF_DESTROY = 0x00020000;
 	final static int WF_CONFIGURE = 0x00004000;
 	final static int WF_CONNECTING = 0x00008000;
 	final static int WF_DISCONNECT = 0x00010000;
 	final static int WF_SASLFINISH = 0x00020000;
 	final static int WF_LASTFINISH = 0x00040000;
 
 	final static int WF_FORCETLS   = 0x10000000;
 	final static int WF_ENABLETLS  = 0x20000000;
 
 	final private static String LOG_TAG = "TalkClient";
	final private static String XYHOST  = "112.64.221.141:9418";
 
 	private long mLastActive = 0;
 	final private int mInterval = 10000;
 	final private SlotSlot mESlot = new SlotSlot();
	final private Connector mConnector = new Connector();
 	final private OutgoingIQManager mIQManager = new OutgoingIQManager();
 
 	final public Packet get() {
 		Packet packet;
 		boolean msgIsLooping = stateMatch(WF_DISCONNECT, WF_LASTFINISH);
 
 		packet = msgIsLooping? mXmlChannel.get(): null;
 		if (packet != null && packet != Packet.EMPTY_PACKET)
 			mLastActive = System.currentTimeMillis();
 
 		return packet;
 	}
 
 	final public void put(Packet packet) {
 		boolean msgIsLooping = stateMatch(WF_DISCONNECT, WF_LASTFINISH);
 
 		if (msgIsLooping)
 			mXmlChannel.put(packet);
 
 		return;
 	}
 
 	final public boolean isStreamClosed() {
 		return stateMatch(0, WF_DISCONNECT);
 	}
 
 	final public void mark() {
 		mXmlChannel.mark(SampleXmlChannel.XML_NEXT);
 		return;
 	}
 
 	final public void waitI(SlotWait wait) {
 		boolean msgIsLooping = stateMatch(WF_DISCONNECT, WF_LASTFINISH);
 
 		if (msgIsLooping)
 			mXmlChannel.waitI(wait);
 		else
 			mESlot.record(wait);
 		return;
 	}
 
 	final private void updateFeature(Packet packet) {
 		return;
 	}
 
 	final private SlotTimer mKeepalive = new SlotTimer() {
 		final private Packet mPresense = new Presence();
 
 		public void invoke() {
 			boolean isAlive = true;
 			if (!stateMatch(WF_DISCONNECT, WF_LASTFINISH)) {
 				DEBUG.Print("login timeout");
 				disconnect();
 			} else if (mLastActive + mInterval < System.currentTimeMillis()) {
 				mLastActive = System.currentTimeMillis();
 				isAlive = mXmlChannel.put(mPresense);
 			}
 
 			if (isAlive == true) {
 				mKeepalive.reset(mInterval);
 			} else {
 				disconnect();
 			}
 			return;
 		}
 	};
 
 	final public boolean disconnect() {
 		mStateFlags |= WF_DISCONNECT;
 		DEBUG.Print("disconnect");
 		mXmlChannel.close();
 		mKeepalive.clean();
 		mWaitOut.clean();
 		mWaitIn.clean();
 		mESlot.wakeup();
 		try { mConnector.close(); } catch (IOException e) { e.printStackTrace(); }
 		return true;
 	}
 
 	final private SlotWait mWaitOut = new SlotWait() {
 		public void invoke() {
 			DEBUG.Print("output event");
 			mKeepalive.reset(mInterval);
 			routine();
 			return;
 		}
 	};
 
 	final private SlotWait mWaitIn = new SlotWait() {
 		public void invoke() {
 			boolean msgIsLooping = stateMatch(WF_DISCONNECT, WF_LASTFINISH);
 			mKeepalive.reset(mInterval);
 			if (msgIsLooping)
 				mESlot.wakeup();
 			else
 				routine();
 			return;
 		}
 	};
 
 	private int mStateFlags = 0;
 	private SampleXmlChannel mXmlChannel = null;
 	private IWaitableChannel mWaitableChannel = null;
 	private boolean stateMatch(int next, int prev) {
 		int flags = mStateFlags;
 		flags &= (next | prev);
 		return (flags == prev);
 	}
 
 	public void start() {
 		mStateFlags |= WF_CONFIGURE;
 		routine();
 		return;
 	}
 
 	private void routine() {
 		if (stateMatch(WF_RESOLV, WF_CONFIGURE)) {
 			mWaitableChannel = mConnector;
 			mStateFlags |= WF_RESOLV;
 		}
 
 		if (stateMatch(WF_CONNECTING, WF_RESOLV)) {
 			mConnector.connect("xmpp.l.google.com:5222");
 			mKeepalive.reset(mInterval);
 			mConnector.waitO(mWaitOut);
 			mStateFlags |= WF_CONNECTING;
 		}
 
 		if (stateMatch(WF_CONNECTED, WF_CONNECTING)) {
 			if (mWaitOut.completed()) {
 				mXmlChannel = new SampleXmlChannel(mWaitableChannel);
 				mStateFlags |= WF_CONNECTED;
 				mWaitOut.clear();
 			}
 		}
 
 		if (stateMatch(WF_HEADER, WF_CONNECTED)) {
 			mXmlChannel.mark(SampleXmlChannel.XML_NEXT);
 			mXmlChannel.open("gmail.com");
 			mXmlChannel.waitI(mWaitIn);
 			mStateFlags |= WF_HEADER;
 		}
 
 		if (stateMatch(WF_FEATURE, WF_HEADER)) {
 			Packet packet = mXmlChannel.get();
 			if (packet.matchTag("features")) {
 				DEBUG.Print("features");
 				mStateFlags |= WF_FEATURE;
 				updateFeature(packet);
 			}
 		}
 
 		if (stateMatch(WF_STARTTLS, WF_FEATURE)) {
 			mXmlChannel.mark(SampleXmlChannel.XML_NEXT);
 			Packet packet = new Starttls();
 			mStateFlags |= WF_STARTTLS;
 			mXmlChannel.waitI(mWaitIn);
 			mXmlChannel.put(packet);
 		}
 
 		if (stateMatch(WF_PROCEED, WF_STARTTLS)) {
 			Packet packet = mXmlChannel.get();
 			if (packet.matchTag("proceed")) {
 				mStateFlags |= WF_PROCEED;
 				DEBUG.Print("proceed");
 			}
 		}
 
 		if (stateMatch(WF_HANDSHAKE, WF_PROCEED)) {
 			WaitableSslChannel sslChannel = new WaitableSslChannel(mConnector);
 			try { sslChannel.handshake(); } catch (Exception e) {};
 
 			mWaitOut.clear();
 			mWaitableChannel = sslChannel;
 			mWaitableChannel.waitO(mWaitOut);
 
 			mStateFlags &= ~(WF_CONNECTED | WF_FEATURE | WF_HEADER);
 			mStateFlags |= WF_HANDSHAKE;
 		}
 
 		if (stateMatch(WF_PLAINSASL, WF_HANDSHAKE | WF_FEATURE)) {
 			mXmlChannel.mark(SampleXmlChannel.XML_NEXT);
 			Packet packet = new PlainSasl("dupit8", "L8PaPUL1nfQT");
 			mStateFlags |= WF_PLAINSASL;
 			mXmlChannel.waitI(mWaitIn);
 			mXmlChannel.put(packet);
 			DEBUG.Print("WF_PLAINSASL");
 		}
 
 		if (stateMatch(WF_SASLFINISH, WF_PLAINSASL)) {
 			Packet packet = mXmlChannel.get();
 			if (packet.matchTag("success") || packet.matchTag("failure")) {
 				DEBUG.Print(packet.matchTag("success")? "success": "failue");
 				mStateFlags |= (packet.matchTag("success")? WF_SUCCESS: 0x0);
 				mStateFlags |= WF_SASLFINISH;
 			}
 		}
 
 		if (stateMatch(WF_LASTSTART, WF_SUCCESS)) {
 			mStateFlags &= ~(WF_CONNECTED | WF_FEATURE | WF_HEADER);
 			mWaitableChannel.waitO(mWaitOut);
 			mStateFlags |= WF_LASTSTART;
 		}
 
 		if (stateMatch(WF_LASTFINISH, WF_LASTSTART| WF_FEATURE)) {
 			mXmlChannel.mark(SampleXmlChannel.XML_NEXT);
 			mStateFlags |= WF_LASTFINISH;
 			initializeFinalLogin();
 		}
 
 		if (stateMatch(WF_DISCONNECT, WF_LASTFINISH)) {
 			DEBUG.Print("TalkClient", "connections connected");
 			mESlot.wakeup();
 		}
 	}
 
 	private void initializeFinalLogin() {
 		Packet presense;
 		IQPacket packet;
 
 		packet = mIQManager.createPacket(new Bind());
 		packet.setType("set");
 		mXmlChannel.put(packet);
 
 		packet = mIQManager.createPacket(new Session());
 		packet.setType("set");
 		mXmlChannel.put(packet);
 
 		presense = new Presence();
 		mXmlChannel.put(presense);
 		return;
 	}
 
 	public void processIncomingPresence(Packet packet) {
 		return;
 	}
 
 	public void processIncomingMessage(Packet packet) {
 		return;
 	}
 
 	public void processIncomingIQ(Packet packet) {
 		return;
 	}
 }
 
