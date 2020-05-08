 package net.enkun.javatter.history;
 
 import com.orekyuu.javatter.plugin.JavatterPlugin;
 
 public class HistoryPlugin extends JavatterPlugin {
 
 	@Override
 	public void init() {
 		
 		HistoryLogic history = new HistoryModel();
 		final HistoryController historyController = new HistoryController();
 		HistoryView historyView = new HistoryView();
 		historyController.setModel(history);
 		history.setView(historyView);
		this.addUserStreamTab("History", historyView);
 		this.addUserStreamListener(historyController);
 	}
 
 	@Override
 	public String getPluginName() {
 		return "HistoryPlugin";
 	}
 
 	@Override
 	public String getVersion() {
 		return "1.0.0";
 	}
 
 }
