 package com.example.service;
 
 import org.springframework.stereotype.Service;
 import org.springframework.web.client.RestTemplate;
 
 import com.example.model.Problem;
 import com.example.model.TaskInfo;
 
 @Service
 public class ProblemServiceImpl implements ProblemService {
 
 	private RestTemplate template;
 	
 	public void setTemplate(RestTemplate template) {
 		this.template = template;
 	}
 	
	private RestTemplate getTemplate() {
		if (this.template == null) this.template = new RestTemplate();
		return template;
	}
	
 	public Problem getProblem() {
		TaskInfo task = getTemplate().getForObject("http://gentle-gorge-9660.herokuapp.com/API/task", TaskInfo.class);
 		return new Problem(task);
 	}
 	
 }
