 /**
  * 
  */
 package org.cotrix.web.ingest.server.climport;
 
 import static org.cotrix.action.CodelistAction.*;
 import static org.cotrix.domain.dsl.Users.*;
 
 import javax.inject.Inject;
 
 import org.cotrix.domain.codelist.Codelist;
 import org.cotrix.domain.dsl.Roles;
 import org.cotrix.domain.user.User;
 import org.cotrix.lifecycle.LifecycleService;
 import org.cotrix.repository.CodelistRepository;
 import org.cotrix.repository.UserRepository;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class ImporterTarget {
 
 	protected Logger logger = LoggerFactory.getLogger(ImporterTarget.class);
 
 	@Inject
 	private CodelistRepository repository;
 
 	@Inject
 	private LifecycleService lifecycleService;
 
 	@Inject
 	protected UserRepository userRepository;
 
 
 	public void save(Codelist codelist, boolean sealed, String ownerId) {
 		logger.trace("save codelist.id: {}, sealed: {}, ownerId: {}", codelist.id(), sealed, ownerId);
 
 		try {
 			repository.add(codelist);
 
 			User owner = userRepository.lookup(ownerId);
 			logger.trace("owner: {}", owner);
 			User changeset = modifyUser(owner).is(Roles.OWNER.on(codelist.id())).build();
 			userRepository.update(changeset);
 			
			if (sealed)
				lifecycleService.lifecycleOf(codelist.id()).notify(SEAL.on(codelist.id()));
 			
 		} catch(Throwable throwable) {
 			throw new RuntimeException("Failed completing the import", throwable);
 		}
 	}
 
 }
