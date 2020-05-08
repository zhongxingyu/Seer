 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.routing.naming.hierarchical;
 
 import java.math.BigInteger;
 
 import de.tuilmenau.ics.fog.facade.Description;
 import de.tuilmenau.ics.fog.routing.hierarchical.RoutingServiceLinkVector;
 
 public class L2Address extends HRMName
 {
 	private static final long serialVersionUID = 4484202410314555829L;
 
 	/**
 	 * Create an address that is used to identify a node at the MAC layer.
 	 * 
 	 * @param pAddress This can be a simple long value.
 	 */
 	public L2Address(long pAddress)
 	{
 		super(BigInteger.valueOf(pAddress));
 	}
 
 	public void setCaps(Description pDescription)
 	{
 		super.setCaps(pDescription);
 	}
 	
 	public boolean equals(Object pObj)
 	{
		if(pObj == null){
			return false;
		}
		
		//TODO: remove this
		if(pObj == this){
			return true;
		}
 		
		/**
		 * L2Address
		 */
 		if(pObj instanceof L2Address) {
 			return (((L2Address) pObj).mAddress.equals(mAddress));
		} 
		
		/**
		 * RoutingServiceLinkVector
		 */
		if (pObj instanceof RoutingServiceLinkVector) {
 			return ( ((RoutingServiceLinkVector)pObj).getSource() != null && ((RoutingServiceLinkVector)pObj).getSource().equals(this)) || (((RoutingServiceLinkVector)pObj).getDestination() != null && ((RoutingServiceLinkVector)pObj).getDestination().equals(this) ) ;
 		}
 		
 		return false;
 	}
 	
 	public String toString()
 	{
		return getClass().getSimpleName() + "(name=\"" + mOptionalDescr + "\", addr.=" + mAddress + ")";
 	}
 }
