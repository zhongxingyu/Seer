 package com.rubensworks.minerva.sdk.data;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.rubensworks.minerva.sdk.DataHolder;
 
 public class Tools {
 	private static final Map<String, Boolean> ACTIVE=new HashMap<String, Boolean>();
 	private String[] tools;
 	private boolean[] enabled;
 	private int amountEnabled=0;
 	private String[] enabledTools;
 	
 	public Tools(DataHolder data) {	
 		ACTIVE.put("announcement", true);
 		//ACTIVE.put("document", true);
 		
		DataHolder[] toolsData=data.getData()[0].getData();
 		enabled=new boolean[toolsData.length];
 		tools=new String[toolsData.length];
 		for(int i=0;i<toolsData.length;i++) {
 			tools[i]=toolsData[i].getData("name").getValue();
 			enabled[i]="1".equals(toolsData[i].getData("enabled").getValue());
 			if(enabled[i]) amountEnabled++;
 		}
 		makeEnabledTools();
 	}
 	
 	private void makeEnabledTools() {
 		if(amountEnabled>0) {
 			enabledTools=new String[Math.min(amountEnabled, ACTIVE.size())];
 			int i=0;
 			int j=0;
 			while(i<tools.length) {
 				if(enabled[i] && ACTIVE.get(tools[i])!=null) {
 					enabledTools[j]=tools[i];
 					j++;
 				}
 				i++;
 			}
 		}
 	}
 	
 	public boolean isEnabled(String name) {
 		for(int i=0;i<tools.length;i++) {
 			if(tools[i].equals(name))
 				return enabled[i];
 		}
 		return false;
 	}
 	
 	@Override
 	public String toString() {
 		String s="";
 		for(int i=0;i<tools.length;i++) {
 			s+=tools[i]+":"+enabled[i]+";";
 		}
 		return s;
 	}
 	
 	public String[] getNames() {
 		return this.enabledTools;
 	}
 	
 	public String[] getAllNames() {
 		return this.tools;
 	}
 	
 	public int getSize() {
 		return Math.min(amountEnabled, ACTIVE.size());
 	}
 }
