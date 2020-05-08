 package com.example.service;
 
import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.web.client.RestTemplate;
 
 import com.example.model.Problem;
 import com.example.model.TaskInfo;
 
 @Service
 public class ProblemServiceImpl implements ProblemService {
 
	@Autowired
 	private RestTemplate template;
 	
 	public void setTemplate(RestTemplate template) {
 		this.template = template;
 	}
 	
 	public Problem getProblem() {
		TaskInfo task = template.getForObject("http://gentle-gorge-9660.herokuapp.com/API/task", TaskInfo.class);
 		return new Problem(task);
 	}
 	
 }
