 package com.bizosys.hsearch.federate;
 
 /**
  * Federated Search Term
  * @author Abinasha Karana, Bizosys
  */
 public final class HTerm {
 	
 	private static boolean DEBUG_MODE = FederatedSearchLog.l.isDebugEnabled();
 	//private static boolean INFO_MODE = FederatedSearchLog.l.isInfoEnabled();
 	
 	public boolean isShould = false;
 	public boolean isMust = false;
 	public float boost = 1;
 	public boolean isFuzzy = false;
 
 	public String type = null;
 	public String text = null;
 	public String minRange = null;
 	public String maxRange = null;
 
 	HResult result = null;
 	
 	public final HResult getResult() {
 		if ( DEBUG_MODE ) FederatedSearchLog.l.debug( 
				"Thread -" + Thread.currentThread().getName() + " HTerm > getResult null : " + ( null == result));
 		return result;
 	}
 	
 	public final void setResult(final HResult result) {
 		if ( DEBUG_MODE ) FederatedSearchLog.l.debug( 
 				"Thread -" + Thread.currentThread().getName() + 
 			" HTerm > setResult : null == " + ( null == result) );
 		this.result = result;
 	}
 	
 	public final void reset() {
 		if ( null != result) this.result = null;
 	}
 	
 	@Override
 	public final String toString() {
 		return "HTerm\t" + type + ":" + text;
 	}
 
 }	
