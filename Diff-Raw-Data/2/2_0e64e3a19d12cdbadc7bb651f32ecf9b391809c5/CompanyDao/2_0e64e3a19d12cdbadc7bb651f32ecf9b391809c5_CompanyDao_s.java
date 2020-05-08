 package daos;
 
 import java.util.List;
 
 import org.hibernate.Session;
 
 import db.HibernateUtil;
 
 import model.Company;
 import model.CompanyType;
 import model.Conference;
 
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
 	public List<Company> getAllCompanies() {
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
 	public void addCompany(List<Company> companies) {
 
 		 for (Company comp:companies)
 		 {
 			 addCompany(comp);
 		 }
 
 	}
 	
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
 	
 	/**
 	 * Get a company by its database key ID
 	 * @param id
 	 * @return
 	 */
 	public Company getCompanyById(long id){
 		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
 		session.beginTransaction();
 		Company comp = (Company)session.createQuery(
 				"select comp from  COMPANIES comp where comp.companyID = :compId")
                 .setLong("compId", id)
                 .uniqueResult();
 		session.getTransaction().commit();
 		return comp;
 	}
 
 }
