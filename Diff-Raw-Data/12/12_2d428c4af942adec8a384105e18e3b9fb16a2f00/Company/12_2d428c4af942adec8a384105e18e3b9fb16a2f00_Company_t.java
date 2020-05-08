 package nextgenforreal.utilities.dataservice;
 
 import org.mortbay.log.Log;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.Transaction;
 import javax.jdo.annotations.IdGeneratorStrategy;
 import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 import javax.jdo.annotations.PrimaryKey;
 
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 
 @PersistenceCapable
 public class Company {
 	@PrimaryKey
 	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
 	private Key key;
 	
 	@Persistent
 	private String fullCompanyName;
 	
 	@Persistent
 	private String tickerSymbol;
 	
 	@Persistent 
 	private int shares;
 	
 	@Persistent
 	private double marketCap;
 	
 	@Persistent
 	private double price;
 	
 	@Persistent
 	private double growthProbability;
 	
 	@Persistent
 	private double growthMagnitude;
 	
 	public void setKey(Key key) {
 		this.key = key;
 	}
 	
 	public Key getKey() {
 		return this.key;
 	}
 	
 	public void setFullCompanyName(String name) {
 		this.fullCompanyName = name;
 	}
 	
 	public void setTickerSymbol(String tickerSymbol) {
 		this.tickerSymbol = tickerSymbol;
 	}
 	
 	public void setShares(int shares) {
 		this.shares = shares;
 	}
 	
 	public void setMarketCap(double marketCap) {
 		this.marketCap = marketCap;
 	}
 	
 	public void setPrice(double price) {
 		this.price = price;
 	}
 	
 	public void setGrowthProbability(double value) {
 		this.growthProbability = value;
 	}
 	
 	public void setGrowthMagnitude(double value) {
 		this.growthMagnitude = value;
 	}
 	
 	public double getGrowthProbability() {
 		return this.growthProbability;
 	}
 	
 	public double getGrowthMagnitude() {
 		return this.growthMagnitude;
 	}
 	
 	
 	//CURRENT VALUE
 	public double getPrice() {
 		return this.price;
 	}
 	
 	public String getCompanyName() {
 		return this.fullCompanyName;
 	}
 	
 	public String getCompanyTicker() {
 		return this.tickerSymbol;
 	}
 	
	public double getMarketCap() {
		return this.marketCap;
	}
	
 	//HAVE TO CALL THIS FOR THE NEXT VALUE
 	public double getNextPriceForCompany(int companyNumber) {
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		Transaction tx = pm.currentTransaction();
 		
 		Key key = KeyFactory.createKey(Company.class.getSimpleName(), companyNumber);
 		Company company = null;
 		double nextPrice = 0;
 		try
 		{
 			tx.begin();
 			company = pm.getObjectById(Company.class, key);
 			
 			nextPrice = DataService.updateCompany(company);
 			
 			tx.commit();
 			
 			logger.info("found company " + companyNumber);
 		} catch(Exception exception) {
 			if(tx.isActive()) 
 				tx.rollback();
 			
 			logger.log(Level.SEVERE, exception.toString());
 		} finally {
 			pm.close();
 		}
 		
 		logger.info("didnt fine company of id" + companyNumber);
 		
 		return nextPrice;
 	}
 	
 	//return NULL if not found
 	public static Company getCompany(int companyNumber) {
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		Transaction tx = pm.currentTransaction();
 		
 		Key key = KeyFactory.createKey(Company.class.getSimpleName(), companyNumber);
 		Company company = null;
 		
 		try
 		{
 			tx.begin();
 			company = pm.getObjectById(Company.class, key);
 			
 			logger.info("found company " + companyNumber);
 		} catch(Exception exception) {
 			if(tx.isActive()) 
 				tx.rollback();
 			
 			logger.log(Level.SEVERE, exception.toString());
 		} finally {
 			pm.close();
 		}
 		
 		logger.info("didnt fine company of id" + companyNumber);
 		
 		return company;
 	}
 	
 	private static Logger logger = Logger.getLogger(Company.class.getName());
 }
