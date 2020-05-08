 package com.lo54project.webservice.dao;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Order;
 
 import com.lo54project.webservice.hibernate.util.HibernateUtil;
 import com.lo54project.webservice.model.Client;
 import com.lo54project.webservice.model.Course;
 import com.lo54project.webservice.model.CourseSession;
 import com.lo54project.webservice.model.Location;
 import com.lo54project.webservice.util.DbPoolConnection;
 
public enum ClientDao implements ClientDaoInterface {
 	instance;
 	
 	private Map<Integer, Client> contentProvider = new HashMap<Integer, Client>();
 	
 	@SuppressWarnings("unchecked")
 	private ClientDao(){
 		
 //		Connection connection = null;
 //		try {			
 //		    connection = DbPoolConnection.getConnection();
 //		 
 //		    Statement statement = connection.createStatement();
 //		    ResultSet resultat = statement.executeQuery( "SELECT * FROM Client;" );
 //		    
 //		    while ( resultat.next() ) {
 //		        int id = resultat.getInt( "id" );
 //		        String lastname = resultat.getString( "lastname" );
 //		        String firstname = resultat.getString( "firstname" );
 //		        String address = resultat.getString( "address" );
 //		        String phone = resultat.getString( "phone" );
 //		        String email = resultat.getString( "email" );
 //		        //int session_id = resultat.getInt( "session_id" );
 //		        
 //		        contentProvider.put(id, new Client(id, lastname, firstname, address, phone, email));
 //		    }
 //		} catch ( SQLException e ) {
 //			e.printStackTrace();
 //		} finally {
 //		    if ( connection != null )
 //		        try {
 //		            /* Close connection */
 //		        	connection.close();
 //		        } catch ( SQLException ignore ) {}
 //		}
 		
 		SessionFactory sf = HibernateUtil.getSessionFactory();
         Session session = sf.openSession();
 
         List<Client> clients = new ArrayList<Client>();
         clients = session.createCriteria(Client.class).list();
         
         for (Client c : clients) {
         	contentProvider.put(c.getId(), c);
 		}
         
         session.close();
 		
 	}
 
 	public void createClientAndSetCourseSession(Client cli) {
 //		Connection connection = null;
 //		try {
 //		    connection = DbPoolConnection.getConnection();
 //		 
 //		    Statement statement = connection.createStatement();
 //
 //		    int id = statement.executeUpdate( "INSERT INTO Client (`lastname` ,`firstname` ,`address` ,`phone` ,`email` ,`session_id`) VALUES ('"+cli.getLastname()+"', '"+cli.getFirstname()+"', '"+cli.getAddress()+"', '"+cli.getPhone()+"', '"+cli.getEmail()+"', '"+cli.getCrss().getId()+"');", Statement.RETURN_GENERATED_KEYS);
 //		    cli.setId(id);
 //		    contentProvider.put(cli.getId(), cli);
 //		} catch ( SQLException e ) {
 //			e.printStackTrace();
 //		} finally {
 //		    if ( connection != null )
 //		        try {
 //		            /* Close connection */
 //		        	connection.close();
 //		        } catch ( SQLException ignore ) {}
 //		}
 		
 		SessionFactory sf = HibernateUtil.getSessionFactory();
         Session session = sf.openSession();
         session.beginTransaction();
         
         session.save(cli);
         contentProvider.put(cli.getId(), cli);
         
         session.getTransaction().commit();
         session.close();
 	}
 	
 	public Map<Integer, Client> getModel(){
 		return contentProvider;
 	}
 }
