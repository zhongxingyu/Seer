 package com.osgo.plugin.portfolio.model.objectify;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import com.googlecode.objectify.Key;
 import com.googlecode.objectify.Ref;
 import com.googlecode.objectify.annotation.Entity;
 import com.googlecode.objectify.annotation.Id;
 
 import static com.googlecode.objectify.ObjectifyService.ofy;
 
 @Entity
 public class Project {
 
 	@Id
 	private Long id;
 	private String title;
 	private Key<Category> category;	
 	private List<Ref<Picture>> images = new ArrayList<Ref<Picture>>();
 
 	public Project(){}
 	
 	public Project(Map<String, Object> input){
 	}
 	
 	protected void setAttributes(Map<String, Object> input){
 		Key<Category> key = null;
 		
 		for (Field f : getClass().getDeclaredFields()) {
 			String attrName = f.getName();
 			Object obj = (Object) input.get(attrName);
 			String category = (String) input.get("category");
 			if(category!=null){
 				obj = Key.create(Category.class, 1);
 			}
 			if(obj!=null){
 				Object objToSet = null;
 				try {
 					Class<?> type = f.getType();
 					Constructor<?>[] ctors = type.getDeclaredConstructors();
 					Constructor<?> ctor = null;
 					for (int i = 0; i < ctors.length; i++) {
 					    ctor = ctors[i];
 					    if (ctor.getGenericParameterTypes().length == 1){
 					    	Class<?>[] params = ctor.getParameterTypes();
 					    	if(params[0].getSimpleName().equals("String")){
 					    		try {
 					    			objToSet = ctor.newInstance(obj);
 					    		} catch (InvocationTargetException e) {
 									// using 0 instead of "" for empty numbers
 									objToSet = ctor.newInstance(0);
 								}
 					    		break;
 					    	}
 					    }				
 					}
 					f.set(this, objToSet);
 				} catch (IllegalArgumentException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IllegalAccessException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (InstantiationException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}  catch (InvocationTargetException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
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
 
 	public List<Picture> getImages(){
 		List<Picture> results = new ArrayList<Picture>(images.size());
 		for(Ref<Picture> image : images){
			
 			Picture picture = ofy().load().key(image.getKey()).get();
 			results.add(picture);
 		}
 		return results;
 	}
 	
     public void setImages(List<Picture> list){ 
     	for(Picture image : list){
     		images.add(Ref.create(Key.create(Picture.class, image.getId()), image));
     	}
     }
     
     public void addImage(Picture image){
     	
     	Key<Picture> key = Key.create(Picture.class, image.getId());
     	Ref<Picture> ref = Ref.create(key, image);
     	images.add(ref);
     }
 
 	public Key<Category> getCategory() {
 		return category;
 	}
 
 	public void setCategory(Key<Category> category) {
 		this.category = category;
 	}
 	
 	public Picture getImageById(long id){
 		
 		for(Ref<Picture> image : images){
 			
 			if(id==image.getKey().getId()){
 				Picture picture = ofy().load().key(image.getKey()).get();
 				return picture;
 			}
 		}
 		
 		return null;
 	}
 	
 }
