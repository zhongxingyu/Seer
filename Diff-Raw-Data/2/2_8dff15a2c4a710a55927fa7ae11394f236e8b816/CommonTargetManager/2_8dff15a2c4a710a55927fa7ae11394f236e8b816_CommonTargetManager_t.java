 package com.theminequest.common.quest.targeted;
 
 import java.util.HashMap;
 import java.util.logging.Level;
 
 import com.theminequest.api.Managers;
 import com.theminequest.api.targeted.QuestTarget;
 import com.theminequest.api.targeted.TargetManager;
 import com.theminequest.common.impl.targeted.AreaTarget;
 import com.theminequest.common.impl.targeted.AreaTargetQuester;
 import com.theminequest.common.impl.targeted.PartyTarget;
 
 public class CommonTargetManager implements TargetManager {
 	
 	private HashMap<String, Class<? extends QuestTarget>> classes;
 	
 	public CommonTargetManager() {
		Managers.log("[Target] Starting Manager...");
 		classes = new HashMap<String, Class<? extends QuestTarget>>();
 		
 		register(AreaTarget.class);
 		register(AreaTargetQuester.class);
 		register(PartyTarget.class);
 	}
 	
 	private void register(Class<? extends QuestTarget> target) {
 		register(target.getName(), target);
 	}
 	
 	@Override
 	public void register(String reqname, Class<? extends QuestTarget> target) {
 		if (classes.containsKey(reqname) || classes.containsValue(target))
 			throw new IllegalArgumentException("We already have this class!");
 		try {
 			target.getConstructor();
 		} catch (Exception e) {
 			throw new IllegalArgumentException("Constructor tampered with!");
 		}
 		classes.put(reqname, target);
 	}
 	
 	@Override
 	public QuestTarget construct(String targetName, int ID, String[] properties) {
 		if (!classes.containsKey(targetName))
 			return null;
 		Class<? extends QuestTarget> cl = classes.get(targetName);
 		try {
 			QuestTarget e = cl.getConstructor().newInstance();
 			e.setupProperties(ID, properties);
 			return e;
 		} catch (Exception e) {
 			Managers.log(Level.SEVERE, "[Targets] In creating " + targetName + ":");
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 }
