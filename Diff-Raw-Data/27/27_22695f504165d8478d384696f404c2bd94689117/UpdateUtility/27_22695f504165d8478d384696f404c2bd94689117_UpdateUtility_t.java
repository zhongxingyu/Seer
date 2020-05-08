 package org.aon.esolutions.appconfig.util;
 
 import org.aon.esolutions.appconfig.model.Environment;
import org.aon.esolutions.appconfig.model.PrivateKeyHolder;
 import org.aon.esolutions.appconfig.repository.ApplicationRepository;
 import org.aon.esolutions.appconfig.repository.EnvironmentRepository;
import org.aon.esolutions.appconfig.repository.PrivateKeyRepository;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.access.prepost.PreAuthorize;
 import org.springframework.stereotype.Service;
 
 @Service
 public class UpdateUtility {
 
 	@Autowired private ApplicationRepository applicationRepository;	
	@Autowired private EnvironmentRepository environmentRepository;
	@Autowired private PrivateKeyRepository privateKeyRepository;	
 	
 	/**
 	 * Simple passthrough to the repository.  Needs to be in a solid class, since pre-authroize
 	 * doesn't work well with interfaces that are proxied.
 	 * 
 	 * @param env
 	 * @return
 	 */
 	@PreAuthorize("hasPermission(#env, 'WRITE')")
 	public Environment saveEnvironment(Environment env) {
 		return environmentRepository.save(env);
 	}
	
	/**
	 * Simple passthrough to the repository.  Needs to be in a solid class, since pre-authroize
	 * doesn't work well with interfaces that are proxied.
	 * 
	 * @param env
	 * @return
	 */
	@PreAuthorize("hasPermission(#holder, 'WRITE')")
	public PrivateKeyHolder savePrivateKeyHolder(PrivateKeyHolder holder) {
		return privateKeyRepository.save(holder);
	}
 }
