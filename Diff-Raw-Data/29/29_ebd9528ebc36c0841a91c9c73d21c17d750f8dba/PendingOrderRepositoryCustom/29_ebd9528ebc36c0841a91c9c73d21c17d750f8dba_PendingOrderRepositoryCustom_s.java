 package com.github.hippoom.food2go.infrastructure.persistence;
 
 import com.github.hippoom.food2go.domain.model.order.PendingOrder;
 import com.github.hippoom.food2go.domain.model.order.TrackingId;
 

 public interface PendingOrderRepositoryCustom {

 	void store(PendingOrder pendingOrder);
 
 	TrackingId nextTrackingId();
 
 }
