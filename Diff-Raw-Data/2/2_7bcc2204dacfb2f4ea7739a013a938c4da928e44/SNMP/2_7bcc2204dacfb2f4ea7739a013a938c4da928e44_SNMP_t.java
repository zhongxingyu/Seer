 package plugins.SNMP;
 
 import plugins.SNMP.snmplib.*;
 import freenet.pluginmanager.FredPlugin;
 import freenet.pluginmanager.PluginRespirator;
 import freenet.support.Logger;
 
 
 public class SNMP implements FredPlugin{
	private volatile boolean goon=true;
 	PluginRespirator pr;
 	private int port;
 	private String bindto;
 
 	public void terminate() {
 		Logger.normal(this,"Stopping SNMP");
 		SNMPAgent.stopSNMPAgent();
 		goon=false;
 	}
 	
 	public int getPort(){
 		return port;
 	}
 	
 	public String getBindto(){
 		return bindto;
 	}
 
 	public void runPlugin(PluginRespirator pr) {
 		this.pr = pr;
 		SNMPAgent.stopSNMPAgent();
 		
 		try{
 			//FIXME: sort out
 			/*
 			Config c=pr.getNode().config;
 			SubConfig sc=new SubConfig("plugins.snmp",c);
 			sc.register("port", 4000,2, true, false, "SNMP port number", "SNMP port number", new SNMPPortNumberCallback());
 			sc.register("bindto", "127.0.0.1", 2, true, false, "Ip address to bind to", "Ip address to bind the SNMP server to", new SNMPBindtoCallback());
 			
 			bindto=sc.getString("bindto");
 			port=sc.getInt("port");
 			
 			sc.finishedInitialization();
 			*/
 			
 			port=4000;
 			bindto="127.0.0.1";
 			
 			SNMPAgent.setSNMPPort(port);
 			System.out.println("Starting SNMP server on "+bindto+ ':' +port);
 			SNMPStarter.initialize(pr.getNode());
 			Logger.normal(this,"Starting SNMP server on "+bindto+ ':' +port);
 			while(goon){
 				try {
 					Thread.sleep(10000);
 				} catch (InterruptedException e) {
 					// Ignore
 				}
 			};
 		}catch (IllegalArgumentException e){
 			Logger.error(this, "Error loading SNMP server");
 		}
 	}
 }
 
