 package at.jku.ce;
 
 public class Main {
 	
 	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
		Set<DomainObject> objectSet = new HashSet<DomainObject>();		objectSet.add(new DomainObject("Helga"));		objectSet.add(new DomainObject("Walter"));		objectSet.add(new DomainObject("Hannah"));		objectSet.add(new DomainObject("Paul"));		objectSet.add(new DomainObject("Claudia"));				for (DomainObject domainObject : objectSet) {		  LOGGER.debug(domainObject.id);		}
 	}
 }
