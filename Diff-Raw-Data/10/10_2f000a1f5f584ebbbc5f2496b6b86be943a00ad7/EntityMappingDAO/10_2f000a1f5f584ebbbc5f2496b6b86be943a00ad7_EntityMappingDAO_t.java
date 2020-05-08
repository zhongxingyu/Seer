 package uk.ac.ebi.fg.myequivalents.dao;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.Validate;
import org.hibernate.SQLQuery;
 import org.hibernate.ejb.HibernateEntityManager;
 import org.hibernate.jdbc.Work;
import org.hibernate.type.StringType;
 
 import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
 import uk.ac.ebi.fg.myequivalents.model.EntityMapping;
 import uk.ac.ebi.fg.myequivalents.utils.EntityMappingUtils;
 
 /**
  * 
  * The DAO for {@link EntityMapping}. This manages basic operations and, as any other DAOs, delegates transaction
  * managements to the outside. 
  *
  * <dl><dt>date</dt><dd>May 25, 2012</dd></dl>
  * @author Marco Brandizi
  *
  */
 public class EntityMappingDAO
 {
 	private EntityManager entityManager;
 	private static MessageDigest messageDigest = null;
 	private final Random random = new Random ( System.currentTimeMillis () );
 
 	public EntityMappingDAO ( EntityManager entityManager )
 	{
 		this.entityManager = entityManager;
 		
 		if ( messageDigest == null )
 		{
 			try {
 				messageDigest = MessageDigest.getInstance ( "SHA1" );
 			} 
 			catch ( NoSuchAlgorithmException ex ) {
 				throw new RuntimeException ( "Internal error, cannot get the SHA1 digester from the JVM", ex );
 			}
 		}
 	}	
 	
 	/**
 	 * This calls {@link #storeMapping(String, String, String, String)} after having achieved the entity ID structure
 	 * via {@link EntityMappingUtils#parseEntityId(String)}. 
 	 */
 	public void storeMapping ( String entityId1, String entityId2 )
 	{
 		String[] entityIdChunks1 = EntityMappingUtils.parseEntityId ( entityId1 );
 		String[] entityIdChunks2 = EntityMappingUtils.parseEntityId ( entityId2 );
 		
 		storeMapping ( entityIdChunks1 [ 0 ], entityIdChunks1 [ 1 ], entityIdChunks2 [ 0 ], entityIdChunks2 [ 1 ] );
 	}
 
 
 	/**
 	 * Stores a mapping between two entities, i.e., a link between service1/acc1 and service2/acc2. Works as specified by 
 	 * {@link EntityMappingManager#storeMappings(String...)}.
 	 * 
 	 */
 	public void storeMapping ( String serviceName1, String accession1, String serviceName2, String accession2 )
 	{
 		serviceName1 = StringUtils.trimToNull ( serviceName1 );
 		Validate.notNull ( serviceName1, "Cannot work with a null service name (first entity)" );
 		
 		accession1 = StringUtils.trimToNull ( accession1 );
 		Validate.notNull ( accession1, "Cannot work with a null accession (first entity)" );
 
 		serviceName2 = StringUtils.trimToNull ( serviceName2 );
 		Validate.notNull ( serviceName2, "Cannot work with a null service name (2nd entity)" );
 		
 		accession2 = StringUtils.trimToNull ( accession2 );
 		Validate.notNull ( accession2, "Cannot work with a null accession (2nd entity)" );
 
 		String bundle1 = this.findBundle ( serviceName1, accession1 );
 		String bundle2 = this.findBundle ( serviceName2, accession2 );
 		
 		if ( bundle1 == null )
 		{
 			if ( bundle2 == null )
 				// The mapping doesn't exist at all, create it
 				//
 				bundle1 = bundle2 = this.create ( serviceName2, accession2 ) ;
 			
 			// join the 1 side to bundle2
 			this.join ( serviceName1, accession1, bundle2 );
 			return;
 		}
 		
 		// The same for the symmetric case
 		//
 		if ( bundle2 == null )
 		{
 			// bundle1 is not null at this point, join the side 2 to bundle1
 			this.join ( serviceName2, accession2, bundle1 );
 			return;
 		}
 		
 		// Bundles are both non null
 		//
 		
 		// The mapping already exists
 		if ( bundle1.equals ( bundle2 ) ) return;
 		
 		// They exists, but belongs to different bundles, so we need to merge them
 		this.mergeBundles ( bundle1, bundle2 );
 	}
 
 	/**
 	 * Assumes the array parameter contains pairs of entity IDs (see {@link EntityMappingUtils#parseEntityId(String)} and creates
 	 * a mapping for each pair (i.e., calls {@link #storeMapping(String, String)}). 
 	 * 
 	 * Throws an exception if the input is not a multiple of 2.
 	 *  
 	 */
 	public void storeMappings ( String... entityIds )
 	{
 		if ( entityIds == null || entityIds.length == 0 ) return;
 		Validate.isTrue ( entityIds.length % 2 == 0, "Wrong no. of arguments for storeMappings, I expect a list of " +
 			"(serviceName1/accession1, serviceName2/accession2) quadruples" 
 		);
 		
 		for ( int i = 0; i < entityIds.length; i++ )
 			storeMapping ( entityIds [ i ], entityIds [ ++ i ] );
 	}
 
 	/**
 	 * Works like specified by {@link EntityMappingManager#storeMappingBundle(String...)}. 
 	 * Uses {@link EntityMappingUtils#parseEntityId(String)} to get the entity ID structure.
 	 *  
 	 */
 	public void storeMappingBundle ( String... entityIds )
 	{
 		if ( entityIds == null || entityIds.length == 0 ) return;
 		
 		// Check if there is some entry already in
 		//
 		for ( int i = 0; i < entityIds.length; i++ )
 		{
 			String ichunks[] = EntityMappingUtils.parseEntityId ( entityIds [ i ] );
 			String bundle = this.findBundle ( ichunks [ 0 ], ichunks [ 1 ] );
 			if ( bundle != null )
 			{
 				// There is already a bundle with one of the input entities, so let's attach all of them to this
 				for ( int j = 0; j < entityIds.length; j++ )
 				{
 					if ( i == j ) continue; 
 					String jchunks[] = EntityMappingUtils.parseEntityId ( entityIds [ j ] );
 					String serviceName = jchunks [ 0 ], accession = jchunks [ 1 ];
 					String bundle1 = this.findBundle ( serviceName, accession );
 					if ( bundle.equals ( bundle1 ) ) continue;
 					if ( bundle1 == null ) 
 						this.join ( serviceName, accession, bundle );
 					else
 						this.moveBundle ( bundle1, bundle );
 				}
 				return;
 			}
 		} // for i
 		
 		
 		// It has not found any of the entries, so we need to create a new bundle that contains all of them.
 		//
 		String bundle = null;
 		for ( int i = 0; i < entityIds.length; i++ )
 		{
 			String chunks[] = EntityMappingUtils.parseEntityId ( entityIds [ i ] );
 			if ( bundle == null )
 				bundle = this.create ( chunks [ 0 ], chunks [ 1 ] );
 			else
 				this.join ( chunks [ 0 ], chunks [ 1 ] , bundle );
 		}
 	}
 	
 	
 	/**
 	 * Uses {@link EntityMappingUtils#parseEntityId(String)} to get the entity ID structure contained by the parameter 
 	 * and then invokes {@link #deleteEntity(String, String)}.
 	 */
 	public boolean deleteEntity ( String entityId ) 
 	{
 		String chunks[] = EntityMappingUtils.parseEntityId ( entityId );
 		return deleteEntity ( chunks [ 0 ], chunks [ 1 ] );
 	}
 	
 	
 	/**
 	 * Deletes an entity from the database, i.e., it removes it from any equivalence/mapping relation it is involved in.
 	 *  
 	 * @return true if the entity was in the DB and it was removed, false if that wasn't the case and the database was
 	 * left unchanged.
 	 */
 	public boolean deleteEntity ( String serviceName, String accession )
 	{
 		// Invalid values
 		serviceName = StringUtils.trimToNull ( serviceName ); if ( serviceName == null ) return false;
 		accession = StringUtils.trimToNull ( accession ); if ( accession == null ) return false;
 
 		String bundle = findBundle ( serviceName, accession );
 		if ( bundle == null ) return false;
 		
 		entityManager.createNativeQuery (
 			"DELETE FROM entity_mapping WHERE service_name = '" + serviceName + "' AND accession = '" + accession + "'"
 		).executeUpdate ();
 		if ( ((Number) entityManager.createNativeQuery ( 
 			    "SELECT COUNT( bundle ) AS ct FROM entity_mapping WHERE bundle = '" + bundle + "'" 
 			   ).getSingleResult () ).longValue () == 1 )
 			entityManager.createNativeQuery ( "DELETE FROM entity_mapping WHERE bundle = '" + bundle + "'" ).executeUpdate ();
 		
 		return true;
 	}
 	
 	/**
 	 * Assumes the input is a set of entity IDs and removes all the corresponding entities, 
 	 * via {@link #deleteEntity(String)}.
 	 * 
 	 * @return the number of entities that were deleted, i.e., the number of times {@link #deleteEntity(String, String)}
 	 * returned true. 
 	 * 
 	 */
 	public int deleteEntitites ( String... entityIds )
 	{
 		if ( entityIds == null || entityIds.length == 0 ) return 0;
 
 		int ct = 0;
 		for ( int i = 0; i < entityIds.length; i++ )
 			ct += this.deleteEntity ( entityIds [ i ] ) ? 1 : 0;
 		
 		return ct;
 	}
 	
 	/**
 	 * Uses {@link EntityMappingUtils#parseEntityId(String)} to get the entity ID structure contained in the parameter, then invokes 
 	 * {@link #deleteMappings(String, String)} with it. 
 	 * 
 	 */
 	public int deleteMappings ( String entityId ) 
 	{
 		String chunks[] = EntityMappingUtils.parseEntityId ( entityId );
 		return deleteMappings ( chunks [ 0 ], chunks [ 1 ] );
 	}
 	
 	
 	/**
 	 * Deletes all the mappings that involve the entity, i.e., the equivalence class it belongs to.
 	 *  
 	 * @return the number of entities (including the parameter) that were in the same equivalence relationship and are
 	 * now deleted. Returns 0 if no such mapping exists.
 	 *  
 	 */
 	public int deleteMappings ( String serviceName, String accession )
 	{
 		// Invalid values
 		serviceName = StringUtils.trimToNull ( serviceName ); if ( serviceName == null ) return 0;
 		accession = StringUtils.trimToNull ( accession ); if ( accession == null ) return 0;
 
 		String bundle = this.findBundle ( serviceName, accession );
 		return bundle == null ? 0 : this.deleteBundle ( bundle );
 	}
 
 	/**
 	 * Assumes the input is a list of entity IDs and deletes all the relations such entities are involved
 	 * in, via {@link #deleteMappings(String)}.  
 	 *  
 	 * This call throws an exception if the input is not a multiple of 2.
 	 * 
 	 * @returns the total number of entities related to the parameters (including the latter) that are now deleted.
 	 * 
 	 */
 	public int deleteMappingsForAllEntitites ( String... entityIds )
 	{
 		if ( entityIds == null || entityIds.length == 0 ) return 0;
 
 		int ct = 0;
 		for ( int i = 0; i < entityIds.length; i++ )
 			ct += this.deleteMappings ( entityIds [ i ] );
 		
 		return ct;
 	}
 	
 	/**
 	 * Uses {@link EntityMappingUtils#parseEntityId(String)} to get the entity ID structure contained in the parameter, then invokes 
 	 * {@link #findMappings(String, String)}.
 	 */
 	public List<String> findMappings ( String entityId )
 	{
 		String chunks[] = EntityMappingUtils.parseEntityId ( entityId );
 		return findMappings ( chunks [ 0 ], chunks [ 1 ] );
 	}
 
 	
 	
 	/**
 	 * Finds all the entities that are related to the parameter.
 	 * 
 	 * @return a possibly empty list of (serviceName, accession) pair. It <b>does include the parameter in the result</b>.
 	 * It returns an empty list if either parameter is empty. It never returns null.
 	 * 
 	 */
 	public List<String> findMappings ( String serviceName, String accession )
 	{
 		serviceName = StringUtils.trimToNull ( serviceName );
 		accession = StringUtils.trimToNull ( accession );
 		final List<String> result = new ArrayList<String> ();
 		if ( serviceName == null || accession == null ) return result; 
 		
 		final String sql = 
 			"SELECT em1.service_name AS service_name, em1.accession AS accession FROM entity_mapping em1, entity_mapping em2\n" +
 			"  WHERE em1.bundle = em2.bundle AND em2.service_name = '" + serviceName + "' AND em2.accession = '" + accession + "'";
 		
 		((HibernateEntityManager)entityManager).getSession ().doWork ( new Work() {
 			@Override
 			public void execute ( Connection conn ) throws SQLException {
 				Statement stmt = conn.createStatement ();
 				for ( ResultSet rs = stmt.executeQuery ( sql ); rs.next (); )
 				{
 					String serviceNamei = rs.getString ( "service_name" ), accessioni = rs.getString ( "accession" );
 					result.add ( serviceNamei ); 
 					result.add ( accessioni ); 
 				}
 			}} 
 		);
 		
 		return result;
 	}
 
 	
 	
 	/**
 	 * Uses {@link EntityMappingUtils#parseEntityId(String)} to get the entity ID structure contained in the parameter, then invokes
 	 * {@link #findEntityMappings(String, String)}.
 	 * 
 	 */
 	public List<EntityMapping> findEntityMappings ( String entityId ) {
 		String chunks[] = EntityMappingUtils.parseEntityId ( entityId );
 		return findEntityMappings ( chunks [ 0 ], chunks [ 1 ] );
 	}
 	
 	/**
 	 * The same as {@link #findMappings(String, String)}, but returns a list of {@link EntityMapping}s from which 
 	 * services can be fetched. Whether you need this or the version that returns strings, it depends on the type of
 	 * result needed, e.g., for raw results only the string-result version is faster, for complete results, this version
 	 * is faster and more practical.
 	 * 
 	 */
 	@SuppressWarnings ( "unchecked" )
 	public List<EntityMapping> findEntityMappings ( String serviceName, String accession )
 	{
 		serviceName = StringUtils.trimToNull ( serviceName );
 		accession = StringUtils.trimToNull ( accession );
 		if ( serviceName == null || accession == null ) return new ArrayList<EntityMapping> (); 
 
 		String hql = 
 			"SELECT em FROM EntityMapping em, EntityMapping em1 " +
 			"WHERE em.bundle = em1.bundle " +
 			"AND em1.service.name = '" + serviceName + "' AND em1.accession = '" + accession + "' ";
 		
 		Query q = entityManager.createQuery ( hql );
 		return q.getResultList ();
 	}
 	
 	
 	public List<EntityMapping> findMappingsForTarget ( String targetServiceName, String entityId )
 	{
 		String chunks[] = EntityMappingUtils.parseEntityId ( entityId );
 		return findMappingsForTarget ( targetServiceName, chunks [ 0 ], chunks [ 1 ] );
 	}
 
 	
 	/**
 	 * Returns the entity IDs of those entities that are equivalent to the input entity and belong to the 
 	 * specified target service. It returns a list, cause nothing forbids to map an entity into multiple ones 
 	 * onto the same target service. 
 	 *   
 	 */
 	@SuppressWarnings ( "unchecked" )
 	public List<EntityMapping> findMappingsForTarget ( String targetServiceName, String serviceName, String accession ) 
 	{
 		serviceName = StringUtils.trimToNull ( serviceName );
 		accession = StringUtils.trimToNull ( accession );
 		targetServiceName = StringUtils.trimToNull ( targetServiceName );
 		if ( serviceName == null || accession == null || targetServiceName == null ) return new ArrayList<EntityMapping> (); 
 
 		String hql = 
 			"SELECT DISTINCT em FROM EntityMapping em, EntityMapping em1 " +
 			"WHERE em.bundle = em1.bundle " + 
 			"AND em1.service.name = '" + serviceName + "' AND em1.accession = '" + accession + "' " +
 			"AND em.service.name = '" + targetServiceName + "'";
 		
 		Query q = entityManager.createQuery ( hql );
 		return q.getResultList ();
 	}
 	
 	
 	/**
 	 * Creates a new {@link EntityMapping}, assigning a new bundle. This is a wrapper of {@link #create(String, String, String)}, 
 	 * with bundle = null.
 	 * 
 	 * @return the new bundle ID.
 	 *  
 	 */
 	private String create ( String serviceName, String accession )
 	{
 		return create ( serviceName, accession, null );
 	}
 
 	/**
 	 * Joins an entity to an existing bundle, i.e., creates a new {@link EntityMapping} with the parameters. This is 
 	 * a wrapper to {@link #create(String, String, String)} with an exception in the case that bundle != null. 
 	 *  
 	 */
 	private String join ( String serviceName, String accession, String bundle )
 	{
 		if ( bundle == null ) throw new RuntimeException (
 			"Cannot work with an empty bundle ID"
 		);
 		return create ( serviceName, accession, bundle );
 	}
 	
 	/**
 	 * Creates a new {@link EntityMapping}, by first creating a new bundle (via {@link #createNewBundleId(String, String)}) 
 	 * if the corresponding parameter is null.
 	 * 
 	 * @return the newly created bundle or the bundle parameter if this is not null. 
 	 */
 	private String create ( String serviceName, String accession, String bundle )
 	{
 		if ( bundle == null )
 			bundle = createNewBundleId ( serviceName, accession );
 		
 		Query q = entityManager.createNativeQuery (   
 			"INSERT INTO entity_mapping (service_name, accession, bundle) VALUES ( :serviceName, :acc, :bundle )" 
 		);
 		q.setParameter ( "serviceName", serviceName );
 		q.setParameter ( "acc", accession );
 		q.setParameter ( "bundle", bundle );
 		
 		// TODO: check the return value
 		q.executeUpdate ();
 		return bundle;
 	}
 		
 	/**
 	 * Merges two bundles, i.e., takes all the {@link EntityMapping} having one of the two bundle IDs and replace their
 	 * bundle ID with the value of the other parameter. Due to optimisation needs, the method selects which bundle to
 	 * change randomly (so you cannot know which one is preserved after the operation). 
 	 * 
 	 * @returns the new unique bundle that there is now in the DB (i.e., either bundle1 or bundle2, the other bundle
 	 * doesn't exist anymore and was merged with the one that is returned).
 	 * 
 	 * This low-level operation is used to store possibly new mappings. It is essentially a random swap of the 
 	 * parameters, followed by {@link #moveBundle(String, String)}.
 	 *  
 	 */
 	private String mergeBundles ( String bundle1, String bundle2 )
 	{
 		bundle1 = StringUtils.trimToNull ( bundle1 );
 		bundle2 = StringUtils.trimToNull ( bundle2 );
 
 		// Bundles have roughly the same size, let's choose the one to update randomly, just to make the the no. of times
 		// that the smaller bundle is chosen uniform.
 		//
 		if ( random.nextBoolean () )
 		{
 			String tmp = bundle1;
 			bundle1 = bundle2;
 			bundle2 = tmp;
 		}
 
 		moveBundle ( bundle1, bundle2 );
 		return bundle2;
 	}
 
 	/**
 	 * Moves srcBundle to destBundle, i.e., replaces all the {@link EntityMapping}(s) in srcBundle with destBundle.
 	 * This low-level operation is used to store possibly new mappings.
 	 * 
 	 */
 	private void moveBundle ( String srcBundle, String destBundle )
 	{
 		srcBundle = StringUtils.trimToNull ( srcBundle );
 		destBundle = StringUtils.trimToNull ( destBundle );
 		
 		Query q = entityManager.createNativeQuery ( 
 			"UPDATE entity_mapping SET bundle = '" + destBundle + "' WHERE bundle = '" + srcBundle + "'" 
 		);
 		// TODO: check > 0
 		q.executeUpdate ();
 	}
 	
 	/**
 	 * @return the ID of the bundle that the parameter belongs to. null if there is no such bundle.
 	 *  
 	 */
 	private String findBundle ( String serviceName, String accession )
 	{
 		Query q = entityManager.createNativeQuery ( 
			"SELECT bundle FROM entity_mapping WHERE service_name = '" + serviceName + "' AND accession = '" + accession + "'"
 		);
 		
		// We've seen this problem (http://stackoverflow.com/questions/4873201/hibernate-native-query-char3-column)
		q.unwrap ( SQLQuery.class ).addScalar ( "bundle", StringType.INSTANCE );
		
 		@SuppressWarnings ( "unchecked" )
 		List<String> results = q.getResultList ();
 		
 		return results.isEmpty () ? null : results.iterator ().next ();
 	}
 	
 	/*private boolean bundleExists ( String bundle )
 	{
 		Query q = entityManager.createNativeQuery ( "SELECT bundle FROM entity_mapping WHERE bundle = '" + bundle + "'" );
 		q.setMaxResults ( 1 );
 		return q.getResultList ().size () > 0;
 	}*/
 
 	/**
 	 * Deletes a bundle by ID, i.e., removes all the {@link EntityMapping} that have the parameter as bundle ID.  
 	 * 
 	 */
 	private int deleteBundle ( String bundle )
 	{
 		return entityManager.createNativeQuery ( 
 			"DELETE FROM entity_mapping WHERE bundle = '" + bundle + "'" ).executeUpdate ();
 	}
 	
 	/**
 	 * Creates a new bundle ID to create a new bundle with the parameter. This is supposed to be used when a new bundle 
 	 * is being inserted in the DB, i.e., because the parameter entity is not in any other stored bundle yet. 
 	 * 
 	 * You should assume the method returns an opaque string which of value depends 1-1 from the parameter. 
 	 * 
 	 * At the moment it generates a SHA1 digest from serviceName + accession and then encodes it in BASE64. This generates
 	 * some overhead (20 bytes for SHA1, instead 8 bytes for a traditional auto-incremented long key, actually 26 bytes
 	 * for BASE64 without any padding, instead of the 20  for SHA1), but it's very fast.
 	 *  
 	 */
 	private String createNewBundleId ( String serviceName, String accession )
 	{
 		// With 20 bytes as input, the BASE64 encoding is always a 27 character string, with the last character always equals
 		// a padding '=', so we don't need the latter in this context  
 		return 
 			Base64.encodeBase64String ( messageDigest.digest ( ( serviceName + accession ).getBytes () ) ).substring (0, 26);
 	}
 }
