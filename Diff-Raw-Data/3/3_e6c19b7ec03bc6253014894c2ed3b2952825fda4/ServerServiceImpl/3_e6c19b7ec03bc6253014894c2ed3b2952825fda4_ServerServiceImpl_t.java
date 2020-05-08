 package com.veilingsite.server;
 
 import java.util.ArrayList;
 
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.Query;
 import javax.persistence.RollbackException;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 import com.veilingsite.shared.ServerService;
 import com.veilingsite.shared.domain.Auction;
 import com.veilingsite.shared.domain.Bid;
 import com.veilingsite.shared.domain.Category;
 import com.veilingsite.shared.domain.Image;
 import com.veilingsite.shared.domain.User;
 
 public class ServerServiceImpl extends RemoteServiceServlet implements ServerService {
 	
 	public ServerServiceImpl() {}
 
 	/**
 	 * Get a single user based on a user name.
 	 * @param s String User name of the user to be returned.
 	 * @return User returns the User if found, else null.
 	 */
 	public User getUser(String s) {		
 		EntityManager em = EMF.get().createEntityManager();
 		User u = null;
 		try {
 			Query q = em.createQuery("select us from User us where us.userName = ?1").setParameter(1, s);
 			u = (User) q.getSingleResult();
 		} catch (NoResultException nre){
 			return null;
 		}
 		finally {
 			em.close();
 		}
 		return u;
 	}
 
 	/**
 	 * adds User u to the DataStore.
 	 * @param u User The user to be added.
 	 */
 	public User addUser(User u) {
 		EntityManager em = EMF.get().createEntityManager();
 		em.getTransaction().begin();
 		if(getUser(u.getUserName())!=null)
 			return null;
 			
 		try {
 			em.persist(u);
 		} finally {
 			em.getTransaction().commit();
 			em.close();
 		}
 		return u;
 	}
 	
 	public void removeUser(User u) {
 		EntityManager em = EMF.get().createEntityManager();	
 		em.getTransaction().begin();
 		try {
 			User user = em.find(User.class, u.getUserName());
 			em.remove(user);
 		} finally {
 			em.getTransaction().commit();
 			em.close();
 		}
 	}
 	
 	public void updateUser(User user) {
 		EntityManager em = EMF.get().createEntityManager();
 		em.getTransaction().begin();
 		  try{
 		    User userx = em.find(User.class, user.getUserName());
 		    userx.setUserName(user.getUserName()); 
 		    userx.setEmail(user.getEmail());
 		    userx.setFirstName(user.getFirstName()); 
 		    userx.setSurName(user.getSurName()); 
 		    userx.setMobilePhoneNumber(user.getMobilePhoneNumber()); 
 		    userx.setPassword(user.getPassword());
 		    userx.setPermission(user.getPermission());
 		    em.persist(userx);
 		  } finally {
 			em.getTransaction().commit();
 		    em.close();
 		  }
 	}
 	
 	public boolean doesUserExist(String userName){
 		EntityManager em = EMF.get().createEntityManager();
 		boolean checkResult = true;
 		try{
 			User findResult = em.find(User.class, userName);
 			if(findResult == null){
 				checkResult = false;
 			}else{
 				checkResult = true;
 			}
 		}catch(Exception e){
 		}
 		return checkResult;
 	}
 	
 	public Auction addAuction(Auction a) {
 		EntityManager em = EMF.get().createEntityManager();
 		em.getTransaction().begin();
 		try {
 			em.persist(a);
 		} finally {
 			em.getTransaction().commit();
 			em.close();
 		}
 		return a;
 	}
 
 	/**
 	 * Gets an ArrayList of all users currently in the DataStore.
 	 * @return ArrayList<User> ArrayList of users.
 	 */
 	public ArrayList<User> getUserList() {
 		EntityManager em = EMF.get().createEntityManager();
 		em.getTransaction().begin();
 		ArrayList<User> l = new ArrayList<User>();
 		try {
 			l = new ArrayList<User>(em.createQuery("select from User").getResultList());
 		} finally {
 			em.getTransaction().commit();
 			em.close();
 		}
 		
 		return l;
 	}
 
 	@Override
 	public User loginUser(User u) {
 		
 		if(u==null)
 			return null;
 		User user = getUser(u.getUserName());
 		
 		if(user==null)
 			return null;
 		
 		if(user.getPassword().equals(u.getPassword())) {
 			return user;
 		} else {
 			return null;
 		}
 	}
 
 	@Override
 	public ArrayList<Auction> getAuctionList(User limitUser, Category limitCat) {
 		EntityManager em = EMF.get().createEntityManager();
 		ArrayList<Auction> l = new ArrayList<Auction>();
 		Query qry;
 		if(limitUser == null){
 			qry = em.createQuery("select a from Auction a").setMaxResults(10);
 		}
 		else{
 			qry = em.createQuery("select a from Auction a where a.owner = ?1").setParameter(1, limitUser).setMaxResults(10);
 		}
 		
 		try {
 			l = new ArrayList<Auction>(qry.getResultList());
 		} catch(Exception e) {
 			System.out.println(e.getMessage());
 			return null;
 		}
 		finally {
 			em.close();
 		}
 		return l;
 	}
 	
 	
 	@Override
 	public ArrayList<Auction> findAuction(String sw, Category ct, ArrayList<Category> c, String or, String ad) {
 		EntityManager em = EMF.get().createEntityManager();
 		ArrayList<Auction> l = new ArrayList<Auction>();
 		String query;
 		int q = 0;
 		query = "select a from Auction a where UPPER(a.description) like UPPER('%" + sw + "%') or UPPER(a.title) like UPPER('%" + sw + "%') ";
 		if(c != null && c.isEmpty() != true){
 			for(Category b : c){
 				if(q == 0){
 					query = query + "and a.category = '" + b + "' ";
 					q++;
 				}
 				else{
 					query = query + "or a.category = '" + b + "' ";
 				}
 			}
 		}
 		
 		query = query + "order by a." + or + " " + ad;
 		System.out.println(query);
 		try {
 			l = new ArrayList<Auction>(em.createQuery(query).getResultList());
 		} catch(Exception e) {
 			e.printStackTrace();
 			System.out.println("JAMMER");
 			return null;
 		}
 		finally {
 			em.close();
 		}
 		return l;
 	}
 	
 	public Auction getAuction(Long id) {
 		EntityManager em = EMF.get().createEntityManager();
 		Auction a = null;
 		System.out.println("test1");
 		try {
 			System.out.println("test2");
 			Query q = em.createQuery("select from Auction where auctionId = ?1").setParameter(1, id);
 			a = (Auction) q.getSingleResult();
 		} catch (NoResultException nre){
 			System.out.println("test3");
 			return null;
 		}
 		finally {
 			em.close();
 		}
 		System.out.println("test4");
 		return a;
 	}
 	
 	public void updateAuction(Auction a) {
 		EntityManager em = EMF.get().createEntityManager();
 		em.getTransaction().begin();
 		  try{
 		    Auction a2 = em.find(Auction.class, a.getAuctionId());
 		    a2.setBidList(a.getBidList());
 		    a2.setCategory(em.find(Category.class, a.getCategory().getTitle()));
 		    a2.setCloseDate(a.getCloseDate());
 		    a2.setDescription(a.getDescription());
 		    a2.setImage(em.find(Image.class, a.getImage().getImageId()));
 		    a2.setStartAmount(a.getStartAmount());
 		    a2.setStartDate(a.getStartDate());
 		    a2.setTitle(a.getTitle());
 		    em.persist(a2);
 		  } catch(Exception e) {
 			  System.out.println(e.getMessage());
 		  } finally {
 			em.getTransaction().commit();
 		    em.close();
 		  }
 	}
 	
 	public Bid addBid(Bid b) {
 		EntityManager em = EMF.get().createEntityManager();
 		em.getTransaction().begin();
 		try {
 			em.persist(b);
 		} finally {
 			em.getTransaction().commit();
 			em.close();
 		}
 		return b;
 	}
 	
 	@Override
 	public ArrayList<Category> getCategoryList() {
 		EntityManager em = EMF.get().createEntityManager();
 		ArrayList<Category> l = new ArrayList<Category>();
 		
 		String query = "select c from Category c";
 		
 		try {
 			l = new ArrayList<Category>(em.createQuery(query).getResultList());
 		}	catch (RollbackException re) {
 			System.out.println(re.getMessage());
 		}
 		finally {
 			em.close();
 		}
 		return l;
 	}
 	
 	public Category addCategory(Category c) {
 		EntityManager em = EMF.get().createEntityManager();
 		em.getTransaction().begin();
 		if(getCategory(c.getTitle())!=null)
 			return null;
 		if(getCategory(c.getParent())==null) {
 			c.setParent("");
 		}
 			
 		try {
 			em.persist(c);
 		} finally {
 			em.getTransaction().commit();
 			em.close();
 		}
 		return c;
 	}
 	
 	public Category getCategory(String s) {		
 		EntityManager em = EMF.get().createEntityManager();
 		Category c = null;
 		try {
 			Query q = em.createQuery("select c from Category c where c.title = ?1").setParameter(1, s);
 			c = (Category) q.getSingleResult();
 		} catch (NoResultException nre){
 			return null;
 		}
 		finally {
 			em.close();
 		}
 		return c;
 	}
 	
 	public ArrayList<Category> getChildrenOfCategory(Category c) {
 		EntityManager em = EMF.get().createEntityManager();
 		em.getTransaction().begin();
 		ArrayList<Category> l = new ArrayList<Category>();
 		
 		String query = "select from Category where parentCategory =" + c.getTitle();
 		
 		try {
 			l = new ArrayList<Category>(em.createQuery(query).getResultList());
 		} finally {
 			em.getTransaction().commit();
 			em.close();
 		}
 		return l;
 	}
 	
 	public void deleteCategory(String s) throws Exception {
 		EntityManager em = EMF.get().createEntityManager();
 		em.getTransaction().begin();		
 		Category cat = getCategory(s);
 		
 		if(cat == null) {
 			throw new Exception("Category was not found");
 		}
 		
 		try {
 			cat = em.getReference(Category.class, cat.getTitle());
 			em.remove(cat);
 		} finally {
 			em.getTransaction().commit();
 			em.close();
 		}
 	}
 }
 
