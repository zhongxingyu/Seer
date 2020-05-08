 package au.edu.qut.inn372.greenhat.dao.gae;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 
 import com.google.appengine.api.datastore.Blob;
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.Query;
 
 import au.edu.qut.inn372.greenhat.bean.Calculator;
 import au.edu.qut.inn372.greenhat.bean.Customer;
 import au.edu.qut.inn372.greenhat.bean.UserProfile;
 import au.edu.qut.inn372.greenhat.dao.CalculatorDAO;
 import au.edu.qut.inn372.greenhat.dao.UserProfileDAO;
 import au.edu.qut.inn372.greenhat.util.Util;
 
 /**
  * Data Access Object using Google Data Store.
  * For each operation the Bean must be converted to Entity.
  * 
  * ATTENTION: we are using some deprecated methods due to 
  * Google documentation is still using the old methods. That
  * can be re-factored as soon as google provide new documentation.
  * 
  * @author Charleston Telles
  *
  */
 @ManagedBean
 @SessionScoped
 public class CalculatorDAOImpl implements Serializable, CalculatorDAO {
 
 	/**
 	 * Class Unique identifies
 	 */
 	private static final long serialVersionUID = 3965427207529608147L;
 	/**
 	 * Google Data store Entity.
 	 * UserProfile bean must be converted to entity before executing
 	 * DS operations
 	 */
 	private Entity entity;
 	/**
 	 * Google Data store must be a local STATIC variable otherwise
 	 * won't work in the GAE server
 	 */
 	private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 	/**
 	 * see interface documentation
 	 */
 	@Override
 	public String save(Calculator calculator) {
 		if (calculator.getKey() != null) {
 			entity = new Entity("Calculator", calculator.getKey());
 		} else {
 			calculator.setKey("Calculator" + (countEntities() + 1));
 			entity = new Entity("Calculator", calculator.getKey());
 		}
 		if (calculator.getName() == null)
 			calculator.setName("Calc_"+calculator.getKey());
 		calculator.setDatetime(Calendar.getInstance().getTime());
 		entity.setProperty("name", calculator.getName());
 		if(calculator.getCustomer()==null)
 			calculator.setCustomer(new Customer());
 		if(calculator.getCustomer().getUserProfile()==null){
 			UserProfile userProfile = new UserProfile();
 			userProfile.setKey("UNKNOW");
 			calculator.getCustomer().setUserProfile(userProfile);
 		}
 		if (calculator.getCustomer().getUserProfile().getKey()==null){
 			UserProfileDAO userProfileDAO = new UserProfileDAOImpl();			
 			UserProfile userProfile = userProfileDAO.getByEmail(calculator.getCustomer().getUserProfile().getEmail());
 			calculator.getCustomer().setUserProfile(userProfile);
 		}
 		entity.setProperty("user", calculator.getCustomer().getUserProfile().getKey());
 		entity.setProperty("status", calculator.getStatus());
 		entity.setProperty("datetime", calculator.getDatetime());
 		entity.setProperty("bean", new Blob(Util.serialize(calculator).getBytes()));
 		
 		datastore.put(entity);
 		return "";
 	}
 	/**
 	 * see interface documentation
 	 */
 	@Override
 	public String remove(Calculator calculator) {
 		@SuppressWarnings("deprecation")
 		Query query = new Query("Calculator").addFilter("name",
 				Query.FilterOperator.EQUAL, calculator.getName());
 		Iterator<Entity> records = datastore.prepare(query).asIterable().iterator();
 	
 		if(records.hasNext()){
 			Key key = records.next().getKey();
 			datastore.delete(key);
 		}
 		return "";
 	}
 	/**
 	 * see interface documentation
 	 */
 	@Override
 	public List<Calculator> getAllByUserProfile(UserProfile userProfile) {
 		@SuppressWarnings("deprecation")
 		Query query = new Query("Calculator").addFilter("user",
 				Query.FilterOperator.EQUAL, userProfile.getKey());
 		Iterator<Entity> records = datastore.prepare(query).asIterable().iterator();
 		Blob bean;
 		Calculator calculator = null;
 		List<Calculator> calculators = new ArrayList<Calculator>();
		if(records.hasNext()){
 			bean = (Blob)records.next().getProperty("bean");
 			calculator = (Calculator)Util.unserialize(new String(bean.getBytes()));
 			calculators.add(calculator);
 		}
 		return calculators;
 	}
 	/**
 	 * see interface documentation
 	 */
 	@Override
 	public Calculator getByName(String name) {
 		@SuppressWarnings("deprecation")
 		Query query = new Query("Calculator").addFilter("name",
 				Query.FilterOperator.EQUAL, name);
 		Iterator<Entity> records = datastore.prepare(query).asIterable().iterator();
 		Blob bean;
 		Calculator calculator = null;
 		if(records.hasNext()){
 			bean = (Blob)records.next().getProperty("bean");
 			calculator = (Calculator)Util.unserialize(new String(bean.getBytes()));
 		}
 		return calculator;
 	}
 	/**
 	 * Gets the number of entities in database.
 	 * This will be used to generate the key number.
 	 * @return number of Entities
 	 */
 	@SuppressWarnings("deprecation")
 	private int countEntities(){
 		Query query = new Query("Calculator");
 		return datastore.prepare(query).countEntities();
 	}
 }
