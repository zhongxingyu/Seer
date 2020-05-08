 package info.berryworks.photoorder.dao;
 // i did not need to rename this package really -  was: 
 //package de.vogella.gae.java.todo.dao.Dao
 
 
 
 import javax.persistence.Query;
 import java.util.List;
 import javax.persistence.EntityManager;
 import info.berryworks.photoorder.model.*;
 
 
 public enum Dao {
 	INSTANCE;
  
	
 	public String xml() {
 		StringBuilder out = new StringBuilder();
 		
 		List<POOrder> orders = listPOOrders();
 		List<POAlbum> albums = listPOAlbums();
 		List<POGroup> groups = this.listPOGroups();
 		List<POPhoto> photos = this.listPOPhotos();
 		List<POUser> users = this.listPOUsers();
 		
 		out.append("<photoorder>");
 		
 		for (POOrder order : orders) {
 			out.append( order.toString() );
 		}
 		
 		for (POAlbum album : albums) {
 			out.append( album.toString() );
 		}
 		
 		for (POGroup group : groups) {
 			out.append( group.toString() );
 		}
 		
 		for (POPhoto photo : photos) {
 			out.append(photo.toString());
 		}
 		
 		for (POUser user : users) {
 			out.append( user.toString() );
 		}
 		out.append( "</photoorder>" );
 		
 		return out.toString();
 			 
 	}
 	
 
 	
 	public List<POOrder> listPOOrders() {
 		EntityManager em = EMFService.get().createEntityManager();
 		// Read the existing entries
		javax.persistence.Query q = em.createQuery("select r from POOrder r order by orderdate desc");
 		List<POOrder> orders =  q.getResultList();
 		return orders;
 	}
 
 	public void addPOOrder(String customeremail, String photoid, String type,
 			String quantity, String albumid, String orderdate ) {
 		synchronized (this) {
 			EntityManager em = EMFService.get().createEntityManager();
 			POOrder order = new POOrder( customeremail,  photoid,  type,
 					 quantity, albumid, orderdate  );
 			em.persist(order);
 			em.close();
 		}
 	}
 
 	public List<POOrder> getPOOrders(String customeremail) {
 		EntityManager em = EMFService.get().createEntityManager();
 		Query q = em
				.createQuery("select t from POOrder t where t.customeremail = :customeremail order by orderdate desc");
 		q.setParameter("customeremail", customeremail);
 		List<POOrder> orders = q.getResultList();
 		return orders;
 	}
 
 	public void removePOOrder(long id) {
 		EntityManager em = EMFService.get().createEntityManager();
 		try {
 			POOrder order = em.find(POOrder.class, id);
 			em.remove(order);
 		} finally {
 			em.close();
 		}
 	}
 	
 	public POUser getPOUser(String email) {
 		EntityManager em = EMFService.get().createEntityManager();
 		Query q = em
 				.createQuery("select t from POUser t where t.email = :email");
 		q.setParameter("email", email);
 		POUser p = (POUser) q.getSingleResult();
 		return p;
 	}
 	
 	public List<POUser> listPOUsers() {
 		EntityManager em = EMFService.get().createEntityManager();
 		// Read the existing entries
 		Query q = em.createQuery("select m from POUser m");
 		List<POUser> pousers = q.getResultList();
 		return pousers;
 	}
 
 	public void addPOUser(String name, String email,String telephone,
 			String mobile,	String address,String notes, String groupid   ) {
 		synchronized (this) {
 			EntityManager em = EMFService.get().createEntityManager();
 			POUser pouser = new POUser( name, email, telephone,
 				mobile,	address, notes, groupid );
 			em.persist(pouser);
 			em.close();
 		}
 	}
 
 	public List<POUser> getGroupPOUsers(String groupid) {
 		EntityManager em = EMFService.get().createEntityManager();
 		Query q = em
 				.createQuery("select t from POUser t where t.groupid = :groupid");
 		q.setParameter("groupid", groupid);
 		List<POUser> groupposers = q.getResultList();
 		
 		System.out.print("getGroupPOUsers: groupid: " + groupid + " size: " + groupposers.size() );
 		
 		return groupposers;
 	}
 
 	public void removePOUser(long id) {
 		EntityManager em = EMFService.get().createEntityManager();
 		try {
 			POUser POUser = em.find(POUser.class, id);
 			em.remove(POUser);
 		} finally {
 			em.close();
 		}
 	}
 	
 	
 	public List<POAlbum> listPOAlbums() {
 		EntityManager em = EMFService.get().createEntityManager();
 		// Read the existing entries
 		Query q = em.createQuery("select m from POAlbum m");
 		List<POAlbum> POAlbums = q.getResultList();
 		return POAlbums;
 	}
 
 	public void addPOAlbum(String name, String notes, String groupid ) {
 		synchronized (this) {
 			EntityManager em = EMFService.get().createEntityManager();
 			POAlbum poalbum = new POAlbum(  name,  notes,  groupid );
 			em.persist(poalbum);
 			em.close();
 		}
 	}
 
 	public List<POAlbum> getGroupPOAlbums(String groupid) {
 		EntityManager em = EMFService.get().createEntityManager();
 		Query q = em
 				.createQuery("select t from POAlbum t where t.groupid = :groupid");
 		q.setParameter("groupid", groupid);
 		List<POAlbum> groupalbums = q.getResultList();
 		return groupalbums;
 	}
 
 	public void removePOAlbum(long id) {
 		EntityManager em = EMFService.get().createEntityManager();
 		try {
 			POAlbum POAlbum = em.find(POAlbum.class, id);
 			em.remove(POAlbum);
 		} finally {
 			em.close();
 		}
 	}
 	
 
 	
 	
 	public List<POGroup> listPOGroups() {
 		EntityManager em = EMFService.get().createEntityManager();
 		// Read the existing entries
 		Query q = em.createQuery("select p from POGroup p");
 		List<POGroup> POGroups = q.getResultList();
 		return POGroups;
 	}
 
 	public void addPOGroup(String name, String notes ) {
 		synchronized (this) {
 			EntityManager em = EMFService.get().createEntityManager();
 			POGroup poGroup = new POGroup(  name,  notes );
 			em.persist(poGroup);
 			em.close();
 		}
 	}
 
 	public void removePOGroup(long id) {
 		EntityManager em = EMFService.get().createEntityManager();
 		try {
 			POGroup pogroup = em.find(POGroup.class, id);
 			em.remove(pogroup);
 		} finally {
 			em.close();
 		}
 	}
 	
 	
 	public List<POPhoto> listPOPhotos() {
 		EntityManager em = EMFService.get().createEntityManager();
 		// Read the existing entries
 		Query q = em.createQuery("select m from POPhoto m");
 		List<POPhoto> POPhotoss = q.getResultList();
 		return POPhotoss;
 	}
 
 	public void addPOPhoto(String photourl, String notes, String albumid ) {
 		synchronized (this) {
 			EntityManager em = EMFService.get().createEntityManager();
 			POPhoto pophoto = new POPhoto(  photourl,  notes,  albumid);
 			em.persist(pophoto);
 			em.close();
 		}
 	}
 
 	public List<POPhoto> getPOPhotos(String albumid) {
 		EntityManager em = EMFService.get().createEntityManager();
 		Query q = em
 				.createQuery("select t from POPhoto t where t.albumid = :albumid");
 		q.setParameter("albumid", albumid);
 		List<POPhoto> POPhotos = q.getResultList();
 		return POPhotos;
 	}
 
 	public void removePOPhoto(long id) {
 		EntityManager em = EMFService.get().createEntityManager();
 		try {
 			POPhoto POPhoto = em.find(POPhoto.class, id);
 			em.remove(POPhoto);
 		} finally {
 			em.close();
 		}
 	}
 	
 }
