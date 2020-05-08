 package com.osgo.plugin.portfolio.api;
 
 import static com.googlecode.objectify.ObjectifyService.ofy;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import com.google.appengine.api.blobstore.BlobKey;
 import com.google.appengine.api.images.Image;
 import com.google.appengine.api.images.ImagesService;
 import com.google.appengine.api.images.ImagesServiceFactory;
 import com.google.appengine.api.images.ServingUrlOptions;
 import com.googlecode.objectify.Key;
 import com.googlecode.objectify.NotFoundException;
 import com.googlecode.objectify.Ref;
 import com.googlecode.objectify.Result;
 import com.googlecode.objectify.VoidWork;
 import com.googlecode.objectify.Work;
 import com.osgo.plugin.portfolio.model.objectify.Category;
 import com.osgo.plugin.portfolio.model.objectify.Picture;
 import com.osgo.plugin.portfolio.model.objectify.Project;
 
 public class PortfolioServiceImpl implements PortfolioService {
 
 	@Override
 	public List<Project> getProjectList() {
 		
 		List<Project> results = ofy().load().type(Project.class).list();
 		return results;
 	}
 
 	@Override
 	public Project getProject(long id) {
 		Project result = ofy().load().key(Key.create(Project.class, id)).get();
 		return result;
 	}
 
 	@Override
 	public void deleteProject(final long id) {
 		ofy().transactNew(new VoidWork() {
 		    public void vrun() {
 		    	ofy().delete().key(Key.create(Project.class, id));
 		    }
 		});
 	}
 	
 	public Project addProject(final Project project, Category category) {
 		Project result = ofy().transactNew(new Work<Project>(){
 			@Override
 			public Project run() {
 				Project result = (Project) ofy().save().entity(project);
 				return result;		
 			}		
 		});
 		return result;
 	}
 	
 	@Override
 	public Project updateProject(final Project project) {
 		Project result = ofy().transactNew(new Work<Project>(){
 			@Override
 			public Project run() {
 				Key<Project> key = ofy().save().entity(project).now();
 				Project proj = ofy().load().key(key).get();
 				return proj;
 			}		
 		});
 		return result;
 	}
 
 	@Override
 	public Project addProject(final Map<String, Object> input) {
 		
 		final Category category;
 		String idStr = (String) input.get("category");
 		if(idStr!=null){
 			category = ofy().load().key(Key.create(Category.class, Long.parseLong(idStr))).get();
 		} else {
 			category = null;
 		}
 		
 		Project result = ofy().transactNew(new Work<Project>(){
 			@SuppressWarnings("unchecked")
 			@Override
 			public Project run() {
 				String title = (String) input.get("title");
 				String text = (String) input.get("text");
 				Project project = new Project();
 				if(category!=null){
 					project.setCategory((Key.create(Category.class, category.getId())));
 				}
 				project.setTitle(title);
 				project.setText(text);
 				Key<Project> result = ofy().save().entity(project).now();
 				project = ofy().load().key(result).get();			
 				
 				if(category!=null){
 					category.addProject(project);
 					ofy().save().entities(category).now();
 				}
 				
 				return project;		
 			}		
 		});
 		return result;
 	}
 	
 	@Override
 	public void addImage(final Picture image, final Project project) {
 		
 		ofy().transactNew(new VoidWork(){
 			@Override
 			public void vrun() {
 				// adds image to DB
 				Key<Picture> result = (Key<Picture>) ofy().save().entity(image).now();
 				// returns newly created image
 				Picture picture = (Picture) ofy().load().key(result).get();
 				// returns relevent project
 				Project proj = ofy().load().key(Key.create(Project.class, project.getId())).get();
 				// adds image to project
 				proj.addImage(picture);
 				// updates project in DB
 				ofy().save().entity(proj).now();
 			}		
 		});
 		
 	}
 
 	@Override
 	public List<Category> getCategoryList() {
 		List<Category> results = ofy().load().type(Category.class).list();
 		return results;
 	}
 
 	@Override
 	public Category getCategory(long id) {
 		return ofy().load().key(Key.create(Category.class, id)).get();
 	}
 
 	@Override
 	public void deleteCategory(long id) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public Category addCategory(final Map<String, Object> input) {
 		Category result = ofy().transactNew(new Work<Category>(){
 			@SuppressWarnings("unchecked")
 			@Override
 			public Category run() {
 				String title = (String) input.get("title");
 				String featured = (String)input.get("featured");
 				boolean featuredVal = false;
				if(featured!=null){
 					if(featured.equals("true"));
 						featuredVal = true;
 				}
 				Category category = new Category();
 				category.setTitle(title);
 				category.setFeatured(featuredVal);
 				Key<Category> result = ofy().save().entity(category).now();
 				Category obj = ofy().load().key(result).get();
 				return obj;		
 			}		
 		});
 		return result;
 	}
 
 	@Override
 	public Category updateCategory(Category category) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Object update(final Object entity) {
 		Object result = ofy().transactNew(new Work<Object>(){
 			@Override
 			public Object run() {
 				Key<Object> key = ofy().save().entity(entity).now();
 				Object proj = ofy().load().key(key).get();
 				return proj;
 			}		
 		});
 		return result;
 	}
 	
 	@Override
 	public void delete(final Object entity) {
 		ofy().transactNew(new VoidWork() {
 		    public void vrun() {
 		    	ofy().delete().entity(entity);
 		    }
 		});
 	}
 		
 }
