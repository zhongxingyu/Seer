 package cz.cvut.fit.mi_mpr_dip.admission.dao.persistence;
 
 public abstract class BaseUniqueConstraint<T> implements UniqueConstraint<T> {
 
 	@Override
 	public Boolean isFound() {
		return !isNotFound();
 	}
 }
