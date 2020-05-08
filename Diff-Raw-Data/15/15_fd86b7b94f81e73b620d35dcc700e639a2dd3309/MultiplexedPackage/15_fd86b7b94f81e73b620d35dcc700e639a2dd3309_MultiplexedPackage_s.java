 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Hierarchical Routing Management
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.packets.hierarchical;
 
 import java.io.Serializable;
 
 import de.tuilmenau.ics.fog.facade.Name;
 import de.tuilmenau.ics.fog.facade.Namespace;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.ClusterDummy;
 import de.tuilmenau.ics.fog.routing.hierarchical.clustering.ICluster;
 
 /**
  * 
  * An object that contains payload data that will be sent to more than one cluster
  */
public class MultiplexedPackage implements Serializable, Name
 {
 	private static final long serialVersionUID = 5178731557132270381L;
 	
 	private ClusterDummy mSourceCluster;
 	private ClusterDummy mDestinationCluster;
 	private Serializable mData = null;
 
 	/**
 	 * 
 	 * @param pSourceCluster is the origin of the packet
 	 * @param pDestinationCluster is a list of the targets of this packet
 	 * @param pData contains some kind of serializable data
 	 */
 	public MultiplexedPackage(ClusterDummy pSourceCluster, ClusterDummy pDestinationCluster, Serializable pData)
 	{
 		mSourceCluster = pSourceCluster;
 		mDestinationCluster = pDestinationCluster;
 		mData = pData;
 	}
 	
 	/**
 	 * 
 	 * @return cluster that was origin of this multiplexed package
 	 */
 	public ICluster getSourceCluster()
 	{
 		return mSourceCluster;
 	}
 	
 	/**
 	 * 
 	 * @return List of clusters that are supposed to receive the data of this packet
 	 */
 	public ClusterDummy getDestinationCluster()
 	{
 		return mDestinationCluster;
 	}
 	
 	public String toString()
 	{
 		return getClass().getSimpleName() + mSourceCluster + " to " + mDestinationCluster + ":\n" + mData;
 	}
 	
 	/**
 	 * 
 	 * @return payload of this packet
 	 */
 	public Serializable getData()
 	{
 		return mData;
 	}

	@Override
	public Namespace getNamespace()
	{
		return new Namespace(mData.getClass().getSimpleName());
	}

	@Override
	public int getSerialisedSize()
	{
		return 0;
	}
 }
