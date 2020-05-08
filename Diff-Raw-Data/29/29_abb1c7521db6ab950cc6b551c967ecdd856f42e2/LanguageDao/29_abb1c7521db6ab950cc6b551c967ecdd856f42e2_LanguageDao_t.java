 package de.lbe.sandbox.openejb;
 
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 @Stateless
 public class LanguageDao {
 
 	@PersistenceContext(unitName = "Language")
 	private EntityManager dbSession;
 
 	public Language findByPrimaryKey(int id) {
		return dbSession.find(Language.class, id);
 	}
 }
