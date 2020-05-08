 package def;
 
 import net.sourceforge.jpcap.net.IPPacket;
 
 
public interface AbstractProtocolRuleChecker<T extends IPPacket, S extends AbstractRule> {
 
	public void add(T packet, S rule);
 	
 }
