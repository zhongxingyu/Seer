 package com.kdcloud.server.rest.resource;
 
 import java.util.ArrayList;
 
 import org.restlet.Application;
 import org.restlet.resource.Delete;
 import org.restlet.resource.Put;
 
 import com.kdcloud.server.entity.DataTable;
 import com.kdcloud.server.rest.api.UserDataResource;
 import com.kdcloud.weka.core.Attribute;
 import com.kdcloud.weka.core.Instances;
 
 public class UserDataServerResource extends KDServerResource implements UserDataResource {
 	
  
 	public UserDataServerResource() {
 		super();
 	}
 
 	UserDataServerResource(Application application) {
 		super(application);
 	}
 
 	@Override
 	@Put
 	public Long createDataset(Instances instances) {
 		DataTable dataset = new DataTable();
 		dataset.setName(instances.relationName());
 		dataset.setInstances(instances);
 //		user.getTables().clear();
 //		user.getTables().add(dataset);
 		user.setTable(dataset);
 		userDao.save(user);
 		return dataset.getId();
 	}
 
//	@Override
//	@Get
//	public Collection<DataTable> listDataset() {
//		ArrayList<DataTable> list = new ArrayList<DataTable>(user.getTables());
//		return list;
//	}
 
 	@Override
 	@Delete
 	public void deleteAllData() {
 		userDao.delete(user);
 	}
 
//	@Override
//	@Get
 	public Long createDataset() {
 		String name = "Dataset of user " + user.getId();
 		Instances data = new Instances(name, new ArrayList<Attribute>(), 0);
 		return createDataset(data);
 	}
 
 }
