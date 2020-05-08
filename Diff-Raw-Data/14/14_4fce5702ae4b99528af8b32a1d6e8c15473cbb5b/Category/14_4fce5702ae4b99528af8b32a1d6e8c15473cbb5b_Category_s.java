 package com.osgo.plugin.portfolio.model.objectify;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.googlecode.objectify.Key;
 import com.googlecode.objectify.Ref;
 import com.googlecode.objectify.annotation.Entity;
 import com.googlecode.objectify.annotation.Id;
 
 @Entity
 public class Category {
 
 	@Id
 	private Long id;
 	private String title;
 	private List<Ref<Project>> projects = new ArrayList<Ref<Project>>();
 	
 	public Long getId() {
 		return id;
 	}
 	public void setId(Long id) {
 		this.id = id;
 	}
 	public String getTitle() {
 		return title;
 	}
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public List<Project> getProjects(){
 		List<Project> results = new ArrayList<Project>(projects.size());
		for(Ref<Project> project : projects){
			results.add(project.get()); //TODO: fix this ya fecker!!!!
 		}
 		return results;
 	}
 	
     public void setProjects(List<Project> list){ 
     	for(Project project : list){
     		projects.add(Ref.create(Key.create(Project.class, project.getId()), project));
     	}
     }
     
     public void addProject(Project project){
     	Ref<Project> ref = Ref.create(Key.create(Project.class, project.getId()), project);
     	projects.add(ref);
     }
 }
