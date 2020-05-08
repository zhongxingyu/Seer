 package plugins.SNMP.snmplib;
 
 import freenet.config.InvalidConfigValueException;
 import freenet.support.api.IntCallback;
 
public class SNMPPortNumberCallback implements IntCallback {
 	
 	public SNMPPortNumberCallback() {
 	}
 	
	public int get() {
 		return SNMPAgent.getSNMPAgent().getSNMPort();
 	}
 
	public void set(int val) throws InvalidConfigValueException {
 		if(val != get()) {
 			SNMPAgent.setSNMPPort(val);
 		}
 	}
 }
