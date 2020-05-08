 package de.javakaffee.validation;
 
 import javax.persistence.EntityManagerFactory;
 import javax.validation.ConstraintValidator;
 import javax.validation.ConstraintValidatorFactory;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * A {@link ConstraintValidatorFactory} that sets an
  * {@link javax.persistence.EntityManager} (retrieved from the provided
  * {@link EntityManagerFactory}) on {@link ConstraintValidator}s that implement
  * {@link EntityManagerAwareValidator}.
  * 
  * @author Martin Grotzke
  * @since 1.0
  * @version 1.0.0
  */
 public class ConstraintValidatorFactoryEMFImpl implements ConstraintValidatorFactory {
 
 	private static final Logger LOG = LoggerFactory.getLogger(ConstraintValidatorFactoryEMFImpl.class);
 
 	private final EntityManagerFactory entityManagerFactory;
 
 	public ConstraintValidatorFactoryEMFImpl(final EntityManagerFactory entityManagerFactory) {
 		this.entityManagerFactory = entityManagerFactory;
 	}
 
 	@Override
 	public <T extends ConstraintValidator<?, ?>> T getInstance(final Class<T> key) {
 		T instance = null;
 
 		try {
 			instance = key.newInstance();
 		} catch (final Exception e) {
 			LOG.error("Could not instantiate " + key.getName(), e);
 		}
 
 		if (EntityManagerAwareValidator.class.isAssignableFrom(key)) {
 			final EntityManagerAwareValidator validator = (EntityManagerAwareValidator)instance;
 			validator.setEntityManager(this.entityManagerFactory.createEntityManager());
 		}
 
 		return instance;
 	}
 
 }
