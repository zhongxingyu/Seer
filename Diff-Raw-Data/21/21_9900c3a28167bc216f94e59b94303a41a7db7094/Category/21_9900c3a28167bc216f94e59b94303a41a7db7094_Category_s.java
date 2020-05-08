 package com.osgo.plugin.portfolio.model.objectify;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import com.googlecode.objectify.Key;
 import com.googlecode.objectify.Ref;
 import com.googlecode.objectify.annotation.Entity;
 import com.googlecode.objectify.annotation.Id;
 import com.googlecode.objectify.annotation.Index;
 
 import static com.googlecode.objectify.ObjectifyService.ofy;
 
 @Entity
 @Index
 public class Category {
 
 	@Id
 	private Long id;
 	private String title;
 	private List<Ref<Project>> projects = new ArrayList<Ref<Project>>();
 	private boolean featured = false;
 	private boolean link = false;
 	private Date date;
 	
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
 		for(Ref<Project> projectRef : projects){
 			Project project = ofy().load().key(projectRef.getKey()).get();
 			results.add(project); //TODO: fix this ya fecker!!!!
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
 	public boolean isFeatured() {
 		return featured;
 	}
 	public void setFeatured(boolean featured) {
 		this.featured = featured;
 	}
 	public boolean isLink() {
 		return link;
 	}
 	public void setLink(boolean link) {
 		this.link = link;
 	}
 	public Date getDate() {
 		return date;
 	}
 	public void setDate(Date date) {
 		this.date = date;
 	}
 }
