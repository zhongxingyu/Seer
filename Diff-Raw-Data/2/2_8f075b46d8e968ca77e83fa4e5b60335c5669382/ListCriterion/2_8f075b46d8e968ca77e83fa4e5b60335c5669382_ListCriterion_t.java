 package br.jus.tre_pa.frameworkdemoiselle.query.filter.criterion;
 
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.Predicate;
 import javax.persistence.criteria.Root;
 
 import br.jus.tre_pa.frameworkdemoiselle.query.filter.internal.AbstractListCriterion;
 import br.jus.tre_pa.frameworkdemoiselle.query.filter.operation.ListOperation;
 
 public class ListCriterion<X> extends AbstractListCriterion<X> {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	public ListCriterion(String field, ListOperation<X> operation) {
 		super(field, operation);
 	}
 
 	@Override
 	public <T> Predicate asPredicate(CriteriaBuilder cb, Root<T> p) {
		return !getField().isEmpty() && getValue() != null && !getValue().isEmpty() ? executeOperation(cb, p) : null;
 	}
 
 	private <T> Predicate executeOperation(CriteriaBuilder cb, Root<T> p) {
 		return getOperation().execute(cb, p, getField(), getValue());
 	}
 }
