 package com.tintin.devcloud.database.manager;
 
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 
 import com.tintin.devcloud.database.Activator;
 import com.tintin.devcloud.database.interfaces.IDatabaseManager;
 
 public class DatabaseManager implements IDatabaseManager {
 
 	private Activator activator = null;
 	
 	private EntityManagerFactory modelEMF = null;
 	
 	public DatabaseManager(Activator activator) {
 		this.activator = activator;
 		
		modelEMF = Persistence.createEntityManagerFactory("model");
 	}
 	
 	@Override
 	public EntityManagerFactory getModelEMF() {
 		return modelEMF;
 	}
 
 }
