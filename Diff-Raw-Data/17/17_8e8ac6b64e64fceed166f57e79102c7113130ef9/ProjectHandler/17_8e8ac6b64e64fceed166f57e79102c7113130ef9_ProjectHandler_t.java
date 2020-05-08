 package com.fieldexpert.fbapi4j;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import com.fieldexpert.fbapi4j.common.Util;
 import com.fieldexpert.fbapi4j.dispatch.Dispatch;
 import com.fieldexpert.fbapi4j.dispatch.Request;
 import com.fieldexpert.fbapi4j.dispatch.Response;
 
 class ProjectHandler implements Handler<Project> {
 	private Dispatch dispatch;
 	private Util util;
 	private String token;
 
 	ProjectHandler(Dispatch dispatch, Util util, String token) {
 		this.dispatch = dispatch;
 		this.util = util;
 		this.token = token;
 	}
 
 	void setToken(String token) {
 		this.token = token;
 	}
 
 	public void create(Project t) {
		throw new UnsupportedOperationException("Operation is not *currently* supported by fbapi4j.");
 	}
 
 	public List<Project> findAll() {
 		Response resp = dispatch.invoke(new Request(Fbapi4j.LIST_PROJECTS, util.map(Fbapi4j.TOKEN, token)));
 		List<Map<String, String>> list = util.data(resp.getDocument(), "project");
 		List<Project> projects = new ArrayList<Project>();
 		for (Map<String, String> map : list) {
 			projects.add(new Project(Integer.parseInt(map.get(Fbapi4j.IX_PROJECT)), map.get(Fbapi4j.S_PROJECT)));
 		}
 		return projects;
 	}
 
 	public Project findById(Integer id) {
		for (Project project : findAll()) {
			if (project.getId() == id) {
				return project;
			}
		}
		throw new Fbapi4jException("Project with id " + id + " not found");
 	}
 }
