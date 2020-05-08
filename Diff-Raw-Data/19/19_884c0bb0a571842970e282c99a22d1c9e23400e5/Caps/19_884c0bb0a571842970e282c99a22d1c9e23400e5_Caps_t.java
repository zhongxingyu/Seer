 package com.zhuri.talk.protocol;
 
 import org.w3c.dom.*;
 import com.zhuri.talk.protocol.FastXmlVisitor;
 
 public class Caps extends Packet {
 	/* <c xmlns="http://jabber.org/protocol/caps" ext="robot" node="http://pagxir.cublog.cn/client/caps" ver="2.6"/>; */
 	public final static String tag = "c";
 	public final static String uri = "http://jabber.org/protocol/caps";
 	public final static String node_uri = "http://pagxir.cublog.cn/client/caps";
 
 	private Element mCaps = FastXmlVisitor.fastFormat("<c xmlns='http://jabber.org/protocol/caps' />");
 
 	public Caps(String ext) {
 		FastXmlVisitor fxv = new FastXmlVisitor(mCaps);
 		fxv.setAttribute("ext", ext);
 		fxv.setAttribute("ver", "2.6");
 		fxv.setAttribute("node", node_uri);
 	}
 
 	Caps(Element e) {
 		mCaps = e;
 		return;
 	}
 
 	public boolean support(String ext) {
		String exts = getExt();
 
		if (exts != null) {
			for (String s: exts.split(" ")) {
				if (s.equals(ext))
					return true;
			}
 		}

 		return false;
 	}
 
 	public String getExt() {
		FastXmlVisitor visitor = new FastXmlVisitor(mCaps);
 		return visitor.getAttribute("ext");
 	}
 
 	public String getVer() {
		FastXmlVisitor visitor = new FastXmlVisitor(mCaps);
 		return visitor.getAttribute("ver");
 	}
 
 	public static boolean isTypeof(Packet packet) {
 		return (packet.matchTag(tag) && packet.matchURI(uri));
 	}
 
 	public String toString() {
 		return FastXmlVisitor.fastFormat(mCaps);
 	}
 }
 
