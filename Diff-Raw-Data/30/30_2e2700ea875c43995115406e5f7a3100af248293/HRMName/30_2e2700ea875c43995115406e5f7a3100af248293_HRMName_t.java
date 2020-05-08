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
 
 import java.awt.Color;
 import java.math.BigInteger;
 
 import de.tuilmenau.ics.fog.facade.Namespace;
 import de.tuilmenau.ics.fog.routing.simulated.RoutingServiceAddress;
 import de.tuilmenau.ics.fog.ui.Decorator;
 
 /**
  * This is the base class for the addresses that are used within the HRM system.
  * It is inherited by L2Address and HRMID objects.
  */
 public class HRMName extends RoutingServiceAddress implements Decorator
 {
 	protected BigInteger mAddress;
		
 	public static final Namespace NAMESPACE_HRM = new Namespace("HRM");
 
 	private static final long serialVersionUID = 6612145890128148511L;
 	
 	/**
 	 * 
 	 * @param pAddress Provide the address 
 	 */
 	public HRMName(BigInteger pAddress)
 	{
 		super(pAddress);
 		mAddress = pAddress;
 	}
 	
 	/**
 	 * 
 	 * @return The address will be returned by this method.
 	 */
 	public BigInteger getComplexAddress()
 	{
 		return mAddress;
 	}
 	
 	@Override
 	public Namespace getNamespace()
 	{
 		return NAMESPACE_HRM;
 	}
 
 	@Override
 	public int getSerialisedSize()
 	{
 		return mAddress.bitLength();
 	}
		
 	public boolean equals(Object pObj)
 	{
 		if(pObj == null){
 			return false;
 		}
 		
 		if(pObj == this){
 			return true;
 		}
 		
 		if(pObj instanceof RoutingServiceAddress) {
 			return ((RoutingServiceAddress) pObj).getAddress() == mAddress.longValue();
 		} else if(pObj instanceof HRMName) {
 			return (((HRMName) pObj).mAddress.equals(mAddress));
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Defines the decoration text for the ARG viewer
 	 * 
 	 * @return text for the control entity or null if no text is available
 	 */
 	@Override
 	public String getText()
 	{
 		return null;
 	}
 
 	/**
 	 * Defines the decoration color for the ARG viewer
 	 * 
 	 * @return color for the HRMID
 	 */
 	@Override
 	public Color getColor()
 	{
 		return Color.WHITE;
 	}
 
 	/**
 	 * Defines the decoration image for the ARG viewer
 	 *  
 	 * @return file name of image for the control entity or null if no specific image is available
 	 */
 	@Override
 	public String getImageName()
 	{
 		return null;
 	}
 }
