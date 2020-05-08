 package com.tunnel22;
 
 import java.util.HashMap;
import java.util.Iterator;

 import com.jcraft.jsch.Session;
 
 public class TunnelContainer {
 	private HashMap tunnelsMap=new HashMap();
 	
	public String getKeysString(){
		String retval="";
		Iterator it=tunnelsMap.keySet().iterator();
		while(it.hasNext()){
			retval=retval+it.next();
			if(it.hasNext()){
				retval=retval+"\n";
			}
		}
		return retval;
	}
	
 	public boolean openTunnelL(String uname, String rhost, int lport, int rport){
 		//""+host+"|"+uname+"|"+s_port+"|"+d_port+"\n"
 		if(tunnelsMap.containsKey("L|"+rhost+"|"+uname+"|"+lport+"|"+rport)){
 			return false;
 		}
 		SSHTunnel tunnel=new SSHTunnel();
 		if(tunnel.openL(uname, rhost, lport, rport)){
 			tunnelsMap.put("L|"+rhost+"|"+uname+"|"+lport+"|"+rport, tunnel.getSession());
 			return true;
 		}
 		return false;
 	}
 	
 	public boolean closeTunnel(String uname, String rhost, int lport, int rport){
 		Session session=(Session)tunnelsMap.remove("L|"+rhost+"|"+uname+"|"+lport+"|"+rport);
 		if(session!=null){
 			session.disconnect();
 			return true;
 		}
 		return false;
 	}

	public void closeAll(){
		Iterator it=tunnelsMap.keySet().iterator();
		while(it.hasNext()){
			closeTunnel((String)it.next());
		}
	}
 	
 	public boolean closeTunnel(String tunnelID){
 		Session session=(Session)tunnelsMap.remove(tunnelID);
 		if(session!=null){
 			session.disconnect();
 			return true;
 		}
 		return false;
 	}
 	
 	public boolean lTunnelExists(String uname, String rhost, int lport, int rport){
 		return tunnelsMap.containsKey("L|"+rhost+"|"+uname+"|"+lport+"|"+rport);
 	}
 	
 	public boolean lTunnelIsOpen(String uname, String rhost, int lport, int rport){
 		if(lTunnelExists(uname, rhost, lport, rport)){
 			Session session=(Session)((SSHTunnel)tunnelsMap.get("L|"+rhost+"|"+uname+"|"+lport+"|"+rport)).getSession();
 			return session.isConnected();
 		}
 		return false;
 	}
 }
