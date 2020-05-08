 /*******************************************************************************
  *  Copyright (c) 2010 Weltevree Beheer BV, Remain Software & Industrial-TSI
  *                                                                      
  * All rights reserved. This program and the accompanying materials     
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at             
  * http://www.eclipse.org/legal/epl-v10.html                            
  *                                                                      
  * Contributors:                                                        
  *    Wim Jongman - initial API and implementation
  *******************************************************************************/
 package org.eclipse.ecf.salvo.ui.internal.provider;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.eclipse.ecf.protocol.nntp.core.ServerStoreFactory;
 import org.eclipse.ecf.protocol.nntp.model.IServer;
 import org.eclipse.ecf.protocol.nntp.model.NNTPException;
 import org.eclipse.ecf.salvo.ui.internal.resources.ISalvoResource;
 import org.eclipse.ecf.salvo.ui.internal.resources.SalvoResourceFactory;
 
 public class SubscribedServerProvider implements IChildProvider {
 
 	public Collection<ISalvoResource> getChildren() {
 
 		ArrayList<ISalvoResource> result = new ArrayList<ISalvoResource>();
 
 		if (ServerStoreFactory.instance().getServerStoreFacade()
 				.getFirstStore() == null) {
			return new ArrayList<ISalvoResource>();
 		}
 
 		try {
 			for (IServer server : ServerStoreFactory.instance()
 					.getServerStoreFacade().getFirstStore()
 					.getSubscribedServers()) {
 				ISalvoResource s1 = SalvoResourceFactory.getResource(
 						server.getAddress(), server);
 				s1.setChildProvider(new SubscribedNewsGroupProvider(s1));
 				result.add(s1);
 			}
 			return result;
 		} catch (NNTPException e) {
			return new ArrayList<ISalvoResource>();
 		}
 	}
 
 	public ISalvoResource getParent() {
 		return null;
 	}
 
 }
