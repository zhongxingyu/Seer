 package no.niths.services;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.List;
 import java.util.Set;
 
 import javax.validation.ConstraintViolation;
 import javax.validation.Validation;
 import javax.validation.Validator;
 
 import no.niths.application.rest.exception.BadRequestException;
 import no.niths.common.ValidationHelper;
 import no.niths.domain.Domain;
 import no.niths.infrastructure.interfaces.GenericRepository;
 import no.niths.services.interfaces.GenericService;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  * Abstract class to use for services. Extending this class will your service
  * basic CRUD actions
  * <p>
  * How to use:
  * <p>
  * Create an interface called YourDomainService
  * <p>
  * Then create an implementation class that overrides getRepository() like this:
  * 
  * <pre>
  * {@code
  * public class YourDomainServiceImpl extends AbstractGenericService<YourDomain> 
  * 							implements YourDomainService{
  * 		@Autowired
  *  	private YourDomainRepository repository;
  * 
  * 		@Override
  * 		public GenericRepository<YourDomain> getRepository() {
  * 		return repository;
  * 		}
  * 
  * }
  * 
  * }
  * 
  * <pre>
  * 
  * @param <T>
  */
 @Transactional
 public abstract class AbstractGenericService<T extends Domain> implements
 		GenericService<T> {
 
 	private Logger logger = LoggerFactory
 			.getLogger(AbstractGenericService.class);
 	private CustomBeanUtilsBean mergeBean = new CustomBeanUtilsBean();
 
 	private Validator validator =
 	        Validation.buildDefaultValidatorFactory().getValidator();
 
 	/**
 	 * Calls on repository to persist the domain
 	 * 
 	 * @param domain
 	 *            the object to persist
 	 * @return id of the object created
 	 * 
 	 */
 	@Override
 	public Long create(T domain) {
	    //validateFields(domain);
 		return getRepository().create(domain);
 	}
 	
 	private void validateFields(T domain) {
 	    Set<ConstraintViolation<T>> violations = validator.validate(domain);
 	    for (ConstraintViolation<T> violation : violations) {
 	        throw new BadRequestException(String.format(
 	                "Invalid values - %s",
 	                violation.getMessage()));
 	    }
 	}
 
 	/**
 	 * Calls on repository to get all objects of a certain type
 	 * 
 	 * @param domain
 	 *            empty object to return all, or with some attributes to search
 	 *            for
 	 * @return list of objects
 	 */
 	@Override
 	public List<T> getAll(T domain) {
 		return getRepository().getAll(domain);
 	}
 
 	@Override
 	public List<T> getAll(T domain, int firstResult, int maxResults) {
 		return getRepository().getAll(domain, firstResult, maxResults);
 	}
 
 	/**
 	 * Calls on repository to get a certain object
 	 * 
 	 * @param id
 	 *            of the object
 	 * @return object with matching ID, or null if no object found
 	 */
 	@Override
 	public T getById(long id) {
 		return getRepository().getById(id);
 	}
 
 	/**
 	 * Calls on repository to update a domain
 	 * <p>
 	 * Will override the current object in DB
 	 * <p>
 	 * 
 	 * @param domain
 	 *            , the domain to update
 	 */
 	@Override
 	public void update(T domain) {
 		getRepository().update(domain);
 	}
 
 	/**
 	 * Updates an object
 	 * <p>
 	 * Difference from update() is that only the attributes with value will be
 	 * updated. All null attributes will not affect the object in DB
 	 * <p>
 	 * 
 	 * @param domain
 	 *            the domain to update
 	 */
 	public void mergeUpdate(T domain) {
 		ValidationHelper.isObjectNull(domain.getId());
 		T domaineToUpdate = getRepository().getById(domain.getId());
 		ValidationHelper.isObjectNull(domaineToUpdate);
 
 		try {
 			mergeBean.copyProperties(domaineToUpdate, domain);
 		} catch (IllegalAccessException | InvocationTargetException e) {
 			logger.error("error", e);
 			e.printStackTrace();
 		}
 		getRepository().update(domaineToUpdate);
 
 	}
 
 	/**
 	 * Deletes the object in DB with the given id
 	 * 
 	 * @param id
 	 *            the id of the object
 	 * @return true if update succeeds, false otherwise
 	 */
 	@Override
 	public boolean delete(long id) {
 		return getRepository().delete(id);
 	}
 
 	/**
 	 * Deletes an object in DB with the given id. All relationships will also be
 	 * deleted
 	 * <p>
 	 * 
 	 * @param id
 	 *            of the object to delete
 	 */
 	@Override
 	public void hibernateDelete(long id) {
 		getRepository().hibernateDelete(id);
 	}
 
 	/**
 	 * Override this method to return an instance of your repository
 	 * <p>
 	 * 
 	 * @return an instance of your repository
 	 */
 	public abstract GenericRepository<T> getRepository();
 
 }
