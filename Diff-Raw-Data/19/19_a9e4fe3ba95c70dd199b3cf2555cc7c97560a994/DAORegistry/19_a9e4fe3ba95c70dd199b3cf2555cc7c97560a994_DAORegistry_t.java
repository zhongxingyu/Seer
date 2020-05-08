 package org.computer.knauss.reqtDiscussion.io;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class DAORegistry {
 
 	private static DAORegistry INSTANCE = null;
 	private Map<String, IDAOManager> managers = new HashMap<String, IDAOManager>();
 	private String selectedDAOManager;
 
 	private DAORegistry() {
 
 	}
 
 	public static synchronized DAORegistry getInstance() {
 		if (INSTANCE == null)
 			INSTANCE = new DAORegistry();
 		return INSTANCE;
 	}
 
 	public void register(String name, IDAOManager mgr) {
 		this.managers.put(name, mgr);
 		if (this.selectedDAOManager == null)
 			this.selectedDAOManager = name;
 	}
 
 	public String[] availableDAOManagers() {
 		return this.managers.keySet().toArray(new String[0]);
 	}
 
	public String getNameForDAOManager(IDAOManager mgr) {
		for (String key : availableDAOManagers()) {
			IDAOManager mgr2 = this.managers.get(key);
			if (mgr2.equals(mgr))
				return key;
		}
		return null;
	}

 	public IDAOManager getDAOManager(String name) {
 		return this.managers.get(name);
 	}
 
 	public void selectDAOManager(String name) {
 		this.selectedDAOManager = name;
 	}
 
 	public IDAOManager getSelectedDAOManager() {
 		return this.managers.get(this.selectedDAOManager);
 	}
 
 	public void closeAllConnections() {
 		for (IDAOManager mgr : this.managers.values())
 			mgr.closeAllConnections();
 	}
 }
