 package me.docredstone.gc;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 public class asdjkeInjectGUI {
 	
 	public Map<Integer, String> chatHistory = new HashMap<Integer, String>();
 	
	List<Player> visibleChat = new ArrayList<Player>();
 	 
 	public void InjectGui(String msg, Player sender, String intercept){
 		  
 		  
 		   String one = "";
 		   String two = "";
 		   String three = "";
 		   String four = "";
 		   String five = "";
 		   
 		   
 		   if (!(chatHistory.containsKey(1))) {
 		    chatHistory.put(1, "");
 		   }
 		   if (!(chatHistory.containsKey(2))) {
 		    chatHistory.put(2, "");
 		   }
 		   if (!(chatHistory.containsKey(3))) {
 		    chatHistory.put(3, "");
 		   }
 		   if (!(chatHistory.containsKey(4))) {
 		    chatHistory.put(4, "");
 		   }
 		   if (!(chatHistory.containsKey(5))) {
 		    chatHistory.put(5, "");
 		   }
 		   
 
 		   String onepre = "";
 		   String twopre =  "";
 		   String threepre = "";
 		   String fourpre = "";
 		   
 		   onepre = chatHistory.get(1);
 		   twopre = chatHistory.get(2);
 		   threepre = chatHistory.get(3);
 		   fourpre = chatHistory.get(4);
 		   
 		   if (msg != null) {
 		   chatHistory.put(2, onepre);
 		   chatHistory.put(3, twopre);
 		   chatHistory.put(4, threepre);
 		   chatHistory.put(5, fourpre);
 		   chatHistory.put(1, msg);
 		   if (sender != null){
 		    if (!(chatHistory.get(1).equals(""))) {
 		    chatHistory.put(1, "[" + sender.getDisplayName() + "] " + msg);
 		    }
 		   }
 		   }
 		   
 		   one += chatHistory.get(1);
 		   two += chatHistory.get(2);
 		   three += chatHistory.get(3);
 		   four += chatHistory.get(4);
 		   five += chatHistory.get(5);
 
 		   
 		   
 		   
 		  
 		   for (Player p : Bukkit.getServer().getOnlinePlayers()) {	    		    
 			    p.sendMessage("=====================================================");
 			    p.sendMessage(intercept);		    
 			    
 			    if (!visibleChat.contains(p)) {
 			    	p.sendMessage("=======================Chat==========================");
 			    	p.sendMessage(five);
 			    	p.sendMessage(four);
 			    	p.sendMessage(three);
 			    	p.sendMessage(two);
 			    	p.sendMessage(one);
 			    }
 		   }
 		 }
 	
 }
