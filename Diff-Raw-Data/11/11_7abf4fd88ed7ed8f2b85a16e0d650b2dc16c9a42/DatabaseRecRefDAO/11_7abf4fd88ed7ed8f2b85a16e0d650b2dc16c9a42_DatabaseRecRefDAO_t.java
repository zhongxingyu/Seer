 package uk.ac.ebi.fg.biosd.model.persistence.hibernate.xref;
 
 import static uk.ac.ebi.utils.sql.SqlUtils.parameterizedWithNullSql;
 
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 
 import org.apache.commons.lang.Validate;
 
 import uk.ac.ebi.fg.biosd.model.xref.DatabaseRecordRef;
 import uk.ac.ebi.fg.core_model.persistence.dao.hibernate.toplevel.IdentifiableDAO;
 
 /**
  * The DAO to manage {@link DatabaseRecordRef} entities.
  * 
  * <dl><dt>date</dt><dd>24 Jan 2014</dd></dl>
  * @author Marco Brandizi
  *
  */
 public class DatabaseRecRefDAO extends IdentifiableDAO<DatabaseRecordRef>
 {
 	public DatabaseRecRefDAO ( EntityManager entityManager ) {
 		super ( DatabaseRecordRef.class, entityManager );
 	}
 
 	public boolean contains ( String dbName, String accession, String version )
 	{
 		Validate.notEmpty ( accession, "accession must not be empty" );
 		
 		String hql = "SELECT d.id FROM " + DatabaseRecordRef.class.getCanonicalName() + 
 			" d WHERE d.dbName = :dbName AND d.acc = :acc AND " + parameterizedWithNullSql ( "d.version", "ver" );
 				
 		Query query = getEntityManager ().createQuery( hql )
 			.setParameter ( "dbName", dbName )
 			.setParameter ( "acc", accession )
 			.setParameter ( "ver", version );
 		
 		@SuppressWarnings ( "unchecked" )
 		List<Long> list = query.getResultList();
 		return !list.isEmpty ();
 	}
 	
 	/**
 	 * Works like {@link #contains(String, String, String)}, but don't check the version, i.e., returns true if there is any version
 	 * of the dbrec with the given name/accession. Allows to pick a specific class. 
 	 * 
 	 */
 	public boolean contains ( String dbName, String accession ) 
 	{
 		String hql = "SELECT d.id FROM " + DatabaseRecordRef.class.getCanonicalName() 
 			+ " d WHERE d.dbName = :dbName AND d.acc = :acc";
 				
 		Query query = getEntityManager ().createQuery( hql )
 			.setParameter ( "dbName", dbName )
 			.setParameter ( "acc", accession );
 		
 		@SuppressWarnings ( "unchecked" )
 		List<Long> list = query.getResultList();
 		return !list.isEmpty ();
 	}
 
 	
 	/**
 	 * @return if the dbrec's accession and version don't exist yet, returns the same entity, after having attached 
 	 * it to the persistence context. If the entity already exists in the DB, returns the copy given by the persistence 
 	 * context (i.e., attached to it). Note that the semantics used to search by accession and version is the one explained
 	 * in {@link #find(String, String, String)}.
 	 *  
 	 */
 	public DatabaseRecordRef getOrCreate ( DatabaseRecordRef dbrec ) 
 	{
 	  Validate.notNull ( dbrec, "Database access error: cannot fetch a null database record reference" );
 	  Validate.notEmpty ( dbrec.getDbName (), "Database access error: cannot fetch a database record reference with empty name" );
 	  Validate.notEmpty ( dbrec.getAcc(), "Database access error: cannot fetch a database record reference with empty accession" );
 	
 	  DatabaseRecordRef dbrecDB = find ( dbrec.getDbName (), dbrec.getAcc(), dbrec.getVersion () );
 	  if ( dbrecDB == null ) 
 	  {
 	    create ( dbrec );
 	    dbrecDB = dbrec;
 	  }
 	  return dbrecDB;
 	}
 
 	/**
 	 * Finds a dbrec by name, accession and version. version = null is matched against a record having a null version.
 	 */
 	public DatabaseRecordRef find ( String dbName, String accession, String version ) 
 	{
 	  Validate.notEmpty ( dbName, "Database access error: cannot fetch a database record reference with empty name" );
 	  Validate.notEmpty ( accession, "Database access error: cannot fetch a database record reference with empty accession" );
 	  
 	  String hql = "SELECT d FROM " + DatabaseRecordRef.class.getCanonicalName() + 
 	  	" d WHERE d.dbName = :dbName AND d.acc = :acc AND " + parameterizedWithNullSql ( "d.version", "ver" );
 	
 	  Query query = getEntityManager ().createQuery ( hql )
		  .setParameter ( "dbName", dbName )
 	  	.setParameter ( "acc", accession )
 	  	.setParameter ( "ver", version );
 		
 		@SuppressWarnings("unchecked")
 		List<DatabaseRecordRef> result = query.getResultList();
 		return result.isEmpty () ? null : result.get ( 0 );
 	}
 	
 
 
 
 	/**
 	 * Wrapper with version = null
 	 */
   public List<DatabaseRecordRef> find ( String dbName, String accession ) {
 	  return find ( dbName, accession );
   }
 	
 }
