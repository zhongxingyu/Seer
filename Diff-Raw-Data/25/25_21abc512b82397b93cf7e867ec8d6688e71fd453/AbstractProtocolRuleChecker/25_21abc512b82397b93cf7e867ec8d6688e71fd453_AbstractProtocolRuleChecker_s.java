 package def;
 
 import net.sourceforge.jpcap.net.IPPacket;
 
 
public interface AbstractProtocolRuleChecker<T extends IPPacket> {
 
	public void add(T packet);
 	
 }
