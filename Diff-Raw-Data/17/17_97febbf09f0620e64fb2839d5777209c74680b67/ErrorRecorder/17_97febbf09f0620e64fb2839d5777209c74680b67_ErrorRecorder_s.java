 package gov.usgs.cida.ncetl.sis;
 
import gov.usgs.cida.ncetl.jpa.ArchiveConfig;

 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
 
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
 		
		TypedQuery<ArchiveConfig> q = em.createQuery("SELECT s FROM ArchiveConfig s WHERE s.rfc = ? ", ArchiveConfig.class);
		q.setParameter(1, rfc);
 		
		ArchiveConfig cfg = q.getSingleResult();
		cfg.addHistory(x.toString());
 		
 		return x.toString();
 	}
 }
