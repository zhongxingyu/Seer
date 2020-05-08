 package com.kdcloud.server.rest.resource;
 
 import java.util.ArrayList;
 
 import org.restlet.Application;
 import org.restlet.client.resource.Post;
 import org.restlet.resource.Get;
 
 import com.kdcloud.server.entity.Modality;
 import com.kdcloud.server.rest.api.ModalitiesResource;
 
 public class ModalitiesServerResource extends KDServerResource implements ModalitiesResource {
 	
 	
 
 	public ModalitiesServerResource() {
 		super();
 		// TODO Auto-generated constructor stub
 	}
 
 	public ModalitiesServerResource(Application application) {
 		super(application);
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	@Get
 	public ArrayList<Modality> listModalities() {
 		ArrayList<Modality> list = new ArrayList<Modality>();
 		list.addAll(modalityDao.getAll());
		getLogger().info("fetched " + list.size() + " modalities");
 		return list;
 	}
 
 	@Override
 	@Post
 	public void createModality(Modality modality) {
 		modalityDao.save(modality);
 	}
 
 }
