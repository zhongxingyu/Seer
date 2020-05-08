 package de.doridian.xvmanage.models;
 
 import de.doridian.xvmanage.Configuration;
 import de.doridian.xvmanage.XVMAPI;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 
 import java.io.IOException;
 import java.util.HashSet;
 
 public class VMNode {
 	protected static final HashSet<VMNode> nodes;
 
 	static {
 		nodes = new HashSet<VMNode>();
 
 		for(Object obj : Configuration.getArray("nodes")) {
 			JSONObject jObj = (JSONObject)obj;
			nodes.add(new VMNode((String)jObj.get("name"), (String)jObj.get("ip")));
 		}
 
 		new Thread() {
 			@Override
 			public void run() {
 				while(true) {
 					for(VMNode vmNode : nodes) {
 						try {
 							vmNode.refreshVMs();
 						} catch (Exception e) {
 							e.printStackTrace();
 						}
 					}
 					try {
 						Thread.sleep(3000);
 					} catch (Exception e) { }
 				}
 			}
 		}.start();
 	}
 
 	public static void loadThisClass() { }
 
 	protected final String name;
 	protected final String ip;
 
 	private VMNode(String name, String ip) {
 		this.name = name;
 		this.ip = ip;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public String getIp() {
 		return ip;
 	}
 
 	public synchronized void refreshVMs() throws IOException {
 		JSONObject query = new JSONObject();
 		query.put("Target", "vm");
 		query.put("Action", "list");
 		JSONArray array = (JSONArray)XVMAPI.apiCall(this, query).get("Result");
 
 		for(LibvirtVM vm : LibvirtVM.vmStorage.values()) {
 			vm.canBeRunRemoved = true;
 		}
 
 		for(Object obj : array) {
 			JSONObject jObj = (JSONObject)obj;
 			LibvirtVM vm = LibvirtVM.getByName((String) jObj.get("Name"));
 			if(vm == null)
 				vm = new LibvirtVM(this, jObj);
 			else
 				vm.receivedVMStatus(this, jObj);
 			vm.canBeRunRemoved = false;
 		}
 
 		for(LibvirtVM vm : new HashSet<LibvirtVM>(LibvirtVM.vmStorage.values())) {
 			if(vm.canBeRunRemoved && vm.getNode() == this)
 				LibvirtVM.vmStorage.remove(vm.getName());
 		}
 	}
 }
