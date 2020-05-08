 package nl.giantit.minecraft.GiantBanks.core.Tools.db;
 
 import nl.giantit.minecraft.GiantBanks.GiantBanks;
 import nl.giantit.minecraft.GiantBanks.core.Tools.db.Handler.*;
 
 import java.util.HashMap;
 
 public class dbHandler {
 
 	private GiantBanks plugin;
 	private HashMap<String, IHandler> handlers = new HashMap<String, IHandler>(); 
 	
 	public void dbHandler(GiantBanks plugin) {
 		this.plugin = plugin;
 		handlers.put("Accounts", new Accounts());
 		handlers.put("Types", new Types());
 	}
 	
 	public void init() {
 		for(IHandler handler : handlers.values()) {
 			handler.init();
 		}
 	}
 	
 	public void init(dbType type) {
 		switch(type) {
 			case ACCOUNTS:
 				handlers.get("Accounts").init();
 				break;
 			case TYPES:
 				handlers.get("Types").init();
 				break;
 			case BANKS:
 				//init banks
 				break;
 			case LOGS:
 				//init logs
 				break;
 			default:
 				handlers.get("Accounts").init();
 				handlers.get("Types").init();
 				break;
 		}
 	}
 	
 	public void save(dbType type) {
 		switch(type) {
 			case ACCOUNTS:
 				handlers.get("Accounts").save();
 				break;
 			case TYPES:
 				handlers.get("Types").save();
 				break;
 			case BANKS:
 				//init banks
 				break;
 			case LOGS:
 				//init logs
 				break;
 			default:
 				handlers.get("Accounts").save();
 				handlers.get("Types").save();
 				break;
 		}
 	}
 	
 	public void fullSave(dbType type) {
 		switch(type) {
 			case ACCOUNTS:
 				handlers.get("Accounts").fullSave();
 				break;
 			case TYPES:
 				handlers.get("Types").fullSave();
 				break;
 			case BANKS:
 				//init banks
 				break;
 			case LOGS:
 				//init logs
 				break;
 			default:
 				handlers.get("Accounts").fullSave();
 				handlers.get("Types").fullSave();
 				break;
 		}
 	}
 }
