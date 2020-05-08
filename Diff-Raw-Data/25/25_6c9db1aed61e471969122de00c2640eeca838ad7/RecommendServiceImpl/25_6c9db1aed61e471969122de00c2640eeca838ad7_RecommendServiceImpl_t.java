 package com.dianping.spotlight.service;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.io.FileUtils;
 
 public class RecommendServiceImpl implements RecommendService {
 
 	private StatisticsService statisticsService;
 	
 	private static final String APP_PREFIX = "active";
 	
 	private static final int MAX_HOTKEY_SIZE = 20;
 	
 	
 	@Override
 	public RecommendResult recommend(File inputSeqFile) {
 		
 		List<String> lines = null;
 		try {
 			lines = FileUtils.readLines(inputSeqFile);
 		} catch (IOException e) {
 			e.printStackTrace();
 			return null;
 		}
 		
 		RecommendResult result = new RecommendResult();
 		String appName = "";
 		List<HotkeyRes> hotkeyRess = new ArrayList<HotkeyRes>();
 		result.setAppName(appName);
 		result.setRecommendKeys(hotkeyRess);
 		int hotkeyUsedTimes = 0;
 		int operateTimes = lines.size();
 
 		for(String line:lines){
 			line = line.trim().toLowerCase();
 			if(line.startsWith(APP_PREFIX)) {
 				int startIndex = line.lastIndexOf(".");
 				if(startIndex == line.length()-1 || line.length() == APP_PREFIX.length()) {
 					appName = "";
 				} else if (startIndex == -1){
 					appName = line.substring(APP_PREFIX.length() + 1).trim();
 				} else {
 					appName = line.substring(startIndex, line.length());
 				}
 				if(!appName.isEmpty()) {
 					break;
 				}
 			}
 		}
 		if(!appName.isEmpty()) {
 			return null;
 		}
 		
 		Set<Hotkey> hotkeys = statisticsService.listHotkeys(appName);
 		if(hotkeys == null || hotkeys.size()==0) {
 			return null;
 		} else if(hotkeys.size() > MAX_HOTKEY_SIZE){ //20ݼ
 			List<Hotkey> hotkeyList = new ArrayList<Hotkey>(hotkeys);
 			Collections.sort(hotkeyList, new HotkeyCompartor());
 			hotkeys = new HashSet<Hotkey>();
			for(int i = 0; i < MAX_HOTKEY_SIZE;i ++) {
				hotkeys.add(hotkeyList.get(i));

			}
 		} 
 		
 		
 		Map<Hotkey,Hotkey> hotkeyToHotKeyMap = new HashMap<Hotkey,Hotkey>();
 //		Set<Hotkey> HotkeyUsed =  new HashSet<Hotkey>();
 //		Map<Set<String>,Integer> tokensToNumMap = new HashMap<Set<String>,Integer>();
 		for(Hotkey hotkey:hotkeys){
 			hotkeyToHotKeyMap.put(hotkey, hotkey);
 		}
 		for(String line:lines){
 			line = line.trim().toLowerCase();
 			Set<String> tokens = new HashSet<String>();
 			for(String token:line.split(" ")){
 				tokens.add(token);
 			}
 			Hotkey hotkeyUsed = new Hotkey();
 			hotkeyUsed.setTokens(tokens);
 			if(hotkeys.contains(hotkeyUsed)){
 				HotkeyRes hotkeyRes = new HotkeyRes();
 				hotkeyRes.setHotkey(hotkeyToHotKeyMap.get(hotkeyUsed));
 				hotkeyRes.setUsed(true);
 				hotkeyRess.add(hotkeyRes);
 				hotkeyToHotKeyMap.remove(tokens);
 			}
 			if(hotkeys.contains(hotkeyUsed)){
 				hotkeyUsedTimes ++;
 			}
 		}
 		Collections.sort(hotkeyRess, new HotkeyResCompartor());
 		if(!hotkeyToHotKeyMap.isEmpty()){
 			for(Hotkey hotkey:hotkeyToHotKeyMap.keySet()){
 				HotkeyRes hotkeyRes = new HotkeyRes();
 				hotkeyRes.setHotkey(hotkeyToHotKeyMap.get(hotkey));
 				hotkeyRes.setUsed(false);
 				hotkeyRess.add(hotkeyRes);
 			}
 		}
 		
 		int score = hotkeyUsedScore(hotkeys, hotkeyToHotKeyMap.values(),50);
 		score = score + hotkeyPercentScore(hotkeyUsedTimes*1.0/operateTimes,50 );
 		result.setScore(score);
 		result.setHigherThan(statisticsService.record(appName, hotkeys, score));
 		return result;
 	}
 
 	
 	private int hotkeyUsedScore(Collection<Hotkey> all,Collection<Hotkey> notused, int totalScore){
 		return totalScore * (all.size()-notused.size())/all.size();
 	}
 	
 	private int hotkeyPercentScore(double percent, int totalScore){
 		double usageHigherThan = statisticsService.usageHigherThan(percent);
 		return (int)(totalScore * (1-usageHigherThan));
 	}
 	
 	class HotkeyResCompartor implements Comparator<HotkeyRes> {
 		@Override
 		public int compare(HotkeyRes o1, HotkeyRes o2) {
 			HotkeyCompartor comparator = new HotkeyCompartor();
 			return  comparator.compare(o1.getHotkey(), o2.getHotkey());
 		}
 		
 	}
 	
 	class HotkeyCompartor implements Comparator<Hotkey> {
 		@Override
 		public int compare(Hotkey o1, Hotkey o2) {
			//usageǰ
			return new Double(o2.getUsage()).compareTo(o1.getUsage());
 		}
 
 	}
 	
 	@Override
 	public void init() {
 		
		statisticsService = new StatisticsServiceImpl();
		Store store = new DefaultStore();
		store.init();
		statisticsService.s
 	}
 
 }
