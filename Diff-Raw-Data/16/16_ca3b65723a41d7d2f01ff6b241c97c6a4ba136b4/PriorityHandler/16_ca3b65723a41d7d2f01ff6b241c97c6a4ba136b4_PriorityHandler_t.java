 package com.fieldexpert.fbapi4j;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import com.fieldexpert.fbapi4j.common.Util;
 import com.fieldexpert.fbapi4j.dispatch.Dispatch;
 import com.fieldexpert.fbapi4j.dispatch.Request;
 import com.fieldexpert.fbapi4j.dispatch.Response;
 
 class PriorityHandler implements Handler<Priority> {
 	private Util util;
 	private Dispatch dispatch;
 	private String token;
 
 	public PriorityHandler(Dispatch dispatch, Util util, String token) {
 		this.util = util;
 		this.dispatch = dispatch;
 		this.token = token;
 	}
 
 	public void create(Priority t) {
 		throw new Fbapi4jException("Cannot create new priorities.");
 	}
 
 	public List<Priority> findAll() {
 		Response resp = dispatch.invoke(new Request(Fbapi4j.LIST_PRIORITIES, util.map(Fbapi4j.TOKEN, token)));
 		List<Map<String, String>> list = util.data(resp.getDocument(), "priority");
 		List<Priority> priorities = new ArrayList<Priority>();
 		for (Map<String, String> map : list) {
 			priorities.add(new Priority(Integer.parseInt(map.get(Fbapi4j.IX_PRIORITY)), map.get(Fbapi4j.S_PRIORITY)));
 		}
 		return priorities;
 	}
 
 	public Priority findById(Integer id) {
		for (Priority priority : findAll()) {
			if (priority.getId() == id) {
				return priority;
			}
		}
		throw new IllegalArgumentException("Priority with id " + id + " does not exist");
 	}
 }
