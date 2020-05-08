 /**-----------------------------------------------------------------------------
  | File Name: EntityEndpoint.java
  | Create Date: Jun 6, 2008
  | @author: Yudha Herwono (UHERWYU)                                                             
  |                                                                              
 | COPYRIGHT © 2008 Pearson Inc.
  | -----------------------------------------------------------------------------
  |                                                                              
  | History:                                                                     
  |==============================================================================
  | Flags  Date      AUTHOR    Comments                                          
  | -----------------------------------------------------------------------------
  | YH01                                                                  
  \*-----------------------------------------------------------------------------*/
 package com.pearson.ed.lplc.services.api;
 
 /**
  * EntityEndpoint is the interface that all entity endpoints should implement
  * in the LPLC.
  * 
  * @author RUMBA
  */
 
 public interface EntityEndpoint {
 	
 	/**
 	 * Fetches the transaction ID.
 	 * 
 	 * @return the transaction ID.
 	 */
 	public String getTransactionId();
 }
