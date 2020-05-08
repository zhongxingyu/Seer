 package daos;
 
 import java.util.List;
 
 import org.hibernate.Session;
 
 import db.HibernateUtil;
 
 import model.Company;
 import model.CompanyType;
 
 /**
  * This class is responsible of supplying services related to the Company entity which require database access.
  * Singleton class.
  */
 public class CompanyDao {
 
 	private static CompanyDao instance = null;
 
 	private CompanyDao() {}
 
 	public static CompanyDao getInstance() {
 		if (instance == null) {
 			instance = new CompanyDao();
 		}
 		return instance;
 	}
 
 	/**
 	 * Get a list of all the Companies that are stored in the database
 	 */
	private List<Company> getAllCompanies() {
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		List<Company> result = (List<Company>) session.createQuery("from COMPANIES").list();
 		session.getTransaction().commit();
 		return result;
 	}
 
 	/**
 	 * Get a list of all the Companies of type <Company Type> that are stored in the database
 	 */
 	public List<Company> getCompaniesOfType(CompanyType companyType) {
 		return null;
 	}
 
 	/**
 	 * Add a new Company to the database
 	 */
 	public Company addCompany(Company company) {
 
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 
 		session.save(company);
 
 		session.getTransaction().commit();
 
 		return company;
 	}
 
 	/**
 	 * Update an existing company in the database
 	 */
 	public Company updateCompany(Company company) {
 		return null;
 	}
 
 }
