 package com.github.hippoom.food2go.infrastructure.persistence;
 
 import com.github.hippoom.food2go.domain.model.order.PendingOrder;
 import com.github.hippoom.food2go.domain.model.order.TrackingId;
 
 public interface PendingOrderRepositoryCustom {
	/**
	 * <pre>
	 * There is an-out-of box method {@link org.springframework.data.repository.CrudRepository#save(Object)},
	 * but personally I don't like the method signature for it returns a new instance 
	 * which makes it look like a query method.
	 * </pre>
	 */
 	void store(PendingOrder pendingOrder);
 
	/**
	 * <pre>
	 * 
	 * Personally I prefer decouple the id assignment from persistent procedure.
	 * In this way, a {@link PendingOrder} has an identity since it is created and 
	 * make it easier to assert in persistence tests.
	 * 
	 * </pre>
	 */
 	TrackingId nextTrackingId();
 
 }
