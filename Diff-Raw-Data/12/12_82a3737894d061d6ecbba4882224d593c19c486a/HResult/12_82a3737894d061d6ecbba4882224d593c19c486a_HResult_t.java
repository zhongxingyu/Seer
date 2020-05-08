 package com.bizosys.hsearch.federate;
 
 /**
  * Federated Source Output
  * @author Abinasha Karana, Bizosys
  */
 public final class HResult {
 	
 	private BitSetOrSet foundIds = null;
 	
 	public final  BitSetOrSet getRowIds() {
 		if ( null == this.foundIds) {
 			FederatedSearchLog.l.warn(Thread.currentThread().getName() + " > HResult get has null values");
 		}
 		return this.foundIds ;
 	}
 
 	public final void setRowIds(final BitSetOrSet foundIds) {
 		this.foundIds = foundIds;
 
 		if ( null == this.foundIds) {
 			FederatedSearchLog.l.warn(Thread.currentThread().getName() + " > HResult set has null values");
 		}
 	}
 	
 	@Override
 	public final String toString() {
		int totalIds = ( null == foundIds) ? 0 : foundIds.size();
		return "HResult=" + totalIds;
 	}
 
 }
