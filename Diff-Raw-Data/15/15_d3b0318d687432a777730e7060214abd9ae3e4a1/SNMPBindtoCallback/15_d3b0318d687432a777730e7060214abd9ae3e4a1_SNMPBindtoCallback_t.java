 package plugins.SNMP.snmplib;
 
 import freenet.config.InvalidConfigValueException;
 import freenet.support.api.StringCallback;
 
public class SNMPBindtoCallback extends StringCallback {
 	
 	public SNMPBindtoCallback() {
 	}
 	
 	public String get() {
 		String bindto=SNMPAgent.getSNMPAgent().getSNMPbindto();
 		if(bindto!=null) return bindto;
 		return "127.0.0.1";
 	}
 
 	public void set(String val) throws InvalidConfigValueException {
 		if(!val.equals(get())) {
 			SNMPAgent.setSNMPbindto(val);
 		}
 	}
 }
