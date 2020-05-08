 package com.zw.ws;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Queue;
 import java.util.Random;
 
 
 import com.zw.markov.MarkovState;
 
 public class ReCompositorImpl extends ActivityFlow implements ReCompositor{
 
 	private Map<AtomService, AtomService> oldNewReplaceServiceMap;
 	private Activity failedActivity;
 	
 	// 这里导致空指针
 	@Override
 	public MarkovState recomposite(MarkovState state) {
 		oldNewReplaceServiceMap = new HashMap<AtomService, AtomService>();
 		failedActivity = state.getFailedActivity();
 		Queue<Integer> queue = new LinkedList<Integer>();
 		queue.offer(failedActivity.getNumber());
 		MarkovState stateNew = state.clone();
 		while (!queue.isEmpty()) {
 			int ano = queue.poll();
 			AtomService failedService = state.getActivity(ano).getBlindService();
 			if (isReplacedRandom()) {
 				AtomService service = state.getFreeServiceFinder().nextFreeService(ano);
				if (service == null) {
					break;
				}
 				AtomService replaceService = service;
 				oldNewReplaceServiceMap.put(failedService, replaceService);
 				stateNew.getActivity(ano).setBlindService(service);
				//System.out.println("blindService:" + stateNew.getActivity(ano).getBlindService());
 			} else {
 				break;
 			}
 			
 			for (Integer i : state.getSuffixActivityNumbers(ano)) {
 				queue.offer(i);
 			}
 		}
		if (state.equals(stateNew)) {
			return null;
		}
 		return stateNew;
 	}
 
 	@Override
 	public Map<AtomService, AtomService> getOldNewReplaceMap() {
 		return oldNewReplaceServiceMap;
 	}
 	
 	private boolean isReplacedRandom() {
 		Random random = new Random();
 		if (random.nextFloat() > 0.2) {
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public double getPosibility() {
 		if (failedActivity != null) {
 			return failedActivity.getBlindService().getQos().getReliability();
 		}
 		return 0;
 	}
 
 	@Override
 	public double getTimeCost() {
 		double timeRes = 0;
 		if (failedActivity != null) {
 			Iterator<Entry<AtomService, AtomService>> iter = oldNewReplaceServiceMap.entrySet().iterator();
 			while (iter.hasNext()) {
 			    Map.Entry<AtomService, AtomService> entry = iter.next();
 			    timeRes += (entry.getValue().getQos().getExecTime() - entry.getKey().getQos().getExecTime()); 
 			} 
 		}
 		return timeRes;
 	}
 
 	@Override
 	public double getPriceCost() {
 		double price = 0;
 		if (failedActivity != null) {
 			Iterator<Entry<AtomService, AtomService>> iter = oldNewReplaceServiceMap.entrySet().iterator();
 			while (iter.hasNext()) {
 			    Map.Entry<AtomService, AtomService> entry = iter.next();
 			    price += (entry.getValue().getQos().getPrice() - entry.getKey().getQos().getPrice()); 
 			} 
 		}
 		return price;
 	}
 }
