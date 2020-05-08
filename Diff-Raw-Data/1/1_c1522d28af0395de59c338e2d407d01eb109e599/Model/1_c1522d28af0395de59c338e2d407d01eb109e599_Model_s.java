 package models;
 
 import java.io.Serializable;
 import java.lang.reflect.Array;
 import java.util.HashMap;
 import java.util.List;
 
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.cfg.Configuration;
 
 import server.JAuctionLogger;
 
 public abstract class Model implements ModelRules, Serializable{
     protected Session session;
     protected SessionFactory sessionFactory = null;
 	protected String modelName;
 	
 	public String getTablename(){
 		return getModelName();
 	}
 	
 	public String getModelName(){
 		 return modelName;
 	}
 	
 	public Model(String modelName){
 		this.modelName = modelName;
 
 	}
 	
 	public HashMap<Long, Model> all(){
 		this.createSession();;
 		session = sessionFactory.openSession();
 		session.beginTransaction();
 		List<Model> result = session.createQuery( "from "+this.getTablename() ).list();
 		HashMap<Long, Model> records = new HashMap<Long, Model>();
 		for ( Model res : (List<Model>) result ) {
 			JAuctionLogger.log("Adding "+this.getModelName()+" "+res.toString());
 		    records.put(res.getId(), res);
 		}
 		session.getTransaction().commit();
 		return records;
 	}
 	
 	public  List<Model> getByAttribute(String attr, String value){
 		this.createSession();
 		session.beginTransaction();
 		List<Model> result = session.createQuery( "from "+this.getTablename()+" where "+attr+" = '"+value+"'" ).list();
 		session.getTransaction().commit();
 		return result;
 	}
 
 	public void save(){
 		this.createSession();
 		session.beginTransaction();
 		session.save( this );
 		session.getTransaction().commit();
 	}
 	
 	private void createSession(){
 		createFactory();
 		if (session == null)
 		  session = sessionFactory.openSession();
 	}
 	
 	protected void createFactory(){
 		  if (sessionFactory == null)
			JAuctionLogger.log(this.getTablename());
 			sessionFactory = new Configuration()
 	        .configure() // configures settings from hibernate.cfg.xml
 	        .addResource("models/"+this.getTablename()+".hbm.xml")
 	        .buildSessionFactory();
 	}
 		
 
 }
