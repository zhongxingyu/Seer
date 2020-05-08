 package gov.usgs.cida.ncetl.sis;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
import javax.persistence.Query;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.integration.MessageHandlingException;
 import org.springframework.integration.annotation.Header;
 import org.springframework.integration.annotation.Transformer;
 import org.springframework.transaction.annotation.Transactional;
 
 public class ErrorRecorder {
 
 	private Logger logger = LoggerFactory.getLogger(getClass());
 
 	@PersistenceContext
 	private EntityManager em;
 	
 	@Transformer
 	@Transactional
 	public String recordError(
 			MessageHandlingException x,
 			@Header(value="rfc", required=false) Integer rfc) {
 		
 		if ( null == rfc) {
 			logger.warn("No rfc header");
 			return "?";
 		}
 		
		Query direct = em.createNativeQuery("insert into ETL_HISTORY(ARCHIVE_ID, OUTCOME) " +
		" values ((select ID from ARCHIVE_CONFIG where RFC_CODE = ?), ?)");
		
		direct.setParameter(1, rfc);
		direct.setParameter(2, x.toString());
 		
		direct.executeUpdate();
 		
 		return x.toString();
 	}
 }
