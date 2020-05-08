 package project_hibernate;
 
 import java.util.List;
 
 
 import javax.persistence.*;
 import org.hibernate.*;
 import org.hibernate.Query;
import org.hibernate.criterion.Order;
 
 /**
  * this class represents a district table on the database
  * @author lamlu
  *
  */
 @Entity
 @Table(name = "district")
 
 public class District 
 {
 	private int id;
 	private String name;
 	private Congressman congressman;
 	
 	/**
 	 * default constructor
 	 */
 	public District(){}
 	/**
 	 * constructor for the class
 	 * @param name the district name
 	 */
 	public District (String districtName)
 	{
 		this.name = districtName;
 	}
 	
 	@Id
 	@GeneratedValue
 	@Column(name ="id")
 	public int getID (){ return this.id;}
 	
 	public void setID (int anID){ this.id = anID;};
 	
 	@Column(name = "name", unique = true)
 	public String getName(){ return this.name;}
 	public void setName(String aName){ this.name = aName;}
 	
     @OneToOne(fetch=FetchType.LAZY)
     @PrimaryKeyJoinColumn
     public Congressman getCongressman(){return this.congressman;}
     public void setCongressman(Congressman cg){this.congressman = cg;}
 	
 	/**
 	 * Load the district name into district table
 	 */
 	public static void load()
 	{
 		Session session = HibernateContext.getSession();
 		Transaction tx = session.beginTransaction();
 		{
 			session.save(new District("Miami's 9th District"));
 			session.save(new District("Kansas's 16th District"));
 			session.save(new District("Texas's 19th District"));
 			session.save(new District("Texas's 12th District"));
 			session.save(new District("Texas's 10th District"));
 			session.save(new District("Washington's 20th District"));
 			session.save(new District("Georgia's 10th District"));
 			session.save(new District("Ohio's 11th District"));
 			session.save(new District("North Dakota's At-Large District"));
 			session.save(new District("Maryland's 1st District"));
 			session.save(new District("Massachusetts's 5th District"));
 			session.save(new District("South Dakota's 2nd District"));
 			session.save(new District("New York's 20th District"));
 			session.save(new District("New Jersey's 20th District"));
 			session.save(new District("Texas's 4th District"));
 			session.save(new District("Michigan's 6th District"));
 			session.save(new District("California's 16th District"));
 			session.save(new District("Georgia's 13th District"));
 			session.save(new District("Oklahoma's 3rd District"));
 		}
 		tx.commit();
 		session.close();
 		System.out.print("District is loaded");
 	}
 	
 	/**
 	 * list all the districts
 	 */
 	public static void list()
 	{
 		Session session = HibernateContext.getSession();
		Criteria c = session.createCriteria(District.class);
		c.addOrder(Order.asc("id"));
		System.out.println("All District: ");
		for (District district : (List<District>) c.list())
 		{
			System.out.format("%-3d: %20s\n", district.getID(), district.getName());
 		}
 	}
 	
 	/**
 	 * fetch the district with the id
 	 * @param id the matching id
 	 * @return the district or null
 	 */
 	public static District find (int id)
 	{
 		Session session = HibernateContext.getSession();
 		Query query = session.createQuery("from District where id = :idvar");
 		query.setInteger("idvar" , id);
 		District district = (District) query.uniqueResult();
 		session.close();
 		return district;
 	}
 	
 	/**
 	 * fetch the district with the name of the district
 	 * @param name the matching name
 	 * @return the district or null
 	 */
 	public static District find (String name)
 	{
 		Session session = HibernateContext.getSession();
 		Query query = session.createQuery("from District where name = :namevar");
 		query.setString("namevar" , name);
 		District district = (District) query.uniqueResult();
 		session.close();
 		return district;
 	}
 }
