 /**
  * 
  */
 package br.study.ebah.miguel.cdsCatalog.entities.impl.admin;
 
 import com.google.common.base.Optional;
 import com.google.common.base.Preconditions;
 
 import br.study.ebah.miguel.cdsCatalog.entities.Entity;
 
 /**
  * @author miguel
  * 
  */
 public abstract class AbstractEntity implements Entity {
 
 	Optional<Long> id = Optional.absent();
 
 	/**
 	 * Admin access only
 	 * 
 	 * @param id
 	 */
 	public void setId(Long id) {
		Preconditions.checkState(isTransient(),
 				"Cannot change id of non-transient entities.");
 		this.id = Optional.of(id);
 	}
 
 	/*
 	 * 
 	 * @see br.study.ebah.miguel.cdsCatalog.entities.Entity#getId()
 	 */
 	@Override
 	public Long getId() {
 		return id.get();
 	}
 
 	/*
 	 * 
 	 * @see br.study.ebah.miguel.cdsCatalog.entities.Entity#isTransient()
 	 */
 	@Override
 	public boolean isTransient() {
 		return !id.isPresent();
 	}
 
 }
