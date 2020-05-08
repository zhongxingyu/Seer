 /*
  * @(#)RegulationDispatchModuleInitializer.java
  *
  * Copyright 2011 Instituto Superior Tecnico
  * Founding Authors: Anil Kassamali
  * 
  *      https://fenix-ashes.ist.utl.pt/
  * 
  *   This file is part of the Dispatch Registry Module.
  *
  *   The Dispatch Registry Module is free software: you can
  *   redistribute it and/or modify it under the terms of the GNU Lesser General
  *   Public License as published by the Free Software Foundation, either version 
  *   3 of the License, or (at your option) any later version.
  *
  *   The Dispatch Registry Module is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *   GNU Lesser General Public License for more details.
  *
  *   You should have received a copy of the GNU Lesser General Public License
  *   along with the Dispatch Registry Module. If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 package module.regulation.dispatch.domain;
 
 import module.regulation.dispatch.domain.metaType.MetaTypeFactory;
import module.workflow.domain.WorkflowSystem;
 import pt.ist.bennu.core.domain.MyOrg;
import pt.ist.bennu.core.domain.VirtualHost;
 import pt.ist.fenixframework.Atomic;
 
 /**
  * 
  * @author Anil Kassamali
  * 
  */
 public class RegulationDispatchModuleInitializer extends RegulationDispatchModuleInitializer_Base {
 
     private static boolean isInitialized = false;
 
     private static ThreadLocal<RegulationDispatchModuleInitializer> init = null;
 
     public static RegulationDispatchModuleInitializer getInstance() {
         if (init != null) {
             return init.get();
         }
 
         if (!isInitialized) {
             initialize();
         }
         final MyOrg myOrg = MyOrg.getInstance();
         return myOrg.getRegulationDispatchModuleInitializer();
     }
 
     @Atomic
     public synchronized static void initialize() {
         if (!isInitialized) {
             try {
                 final MyOrg myOrg = MyOrg.getInstance();
                 final RegulationDispatchModuleInitializer initializer = myOrg.getRegulationDispatchModuleInitializer();
                 if (initializer == null) {
                     new RegulationDispatchModuleInitializer();
                 }
                 init = new ThreadLocal<RegulationDispatchModuleInitializer>();
                 init.set(myOrg.getRegulationDispatchModuleInitializer());
 
                 isInitialized = true;
             } finally {
                 init = null;
             }
         }
     }
 
     private RegulationDispatchModuleInitializer() {
         super();
         setMyOrg(MyOrg.getInstance());
     }
 
     @Override
     public void init(MyOrg root) {
         RegulationDispatchSystem system = RegulationDispatchSystem.getInstance();
 
         if (system.getMetaType() == null) {
            for (final VirtualHost virtualHost : root.getVirtualHostsSet()) {
        	try {
        	    VirtualHost.setVirtualHostForThread(virtualHost);
        	    if (virtualHost.getWorkflowSystem() == null) {
        		WorkflowSystem.createSystem(virtualHost);
        		MetaTypeFactory.createMetaType();
        	    }
        	} finally {
        	    VirtualHost.releaseVirtualHostFromThread();
        	}
            }
         }
     }
 
 }
