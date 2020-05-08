 /*
  * Created on 13.09.2004
  */
 package org.caesarj.runtime.aspects;
 
 /**
  * @author Vaidas Gasiunas
  *
  * Aspect deployement strategy, which deploys aspect on entire local process 
  */
 public class AspectLocalDeployer implements AspectDeployerIfc {
 
 	/**
 	 * Deploy the object on given registry 
	 * Deploys the object on the current thread
 	 *  
 	 * @param reg			Registry instance
 	 * @param aspectObj		Aspect object
 	 */
 	public void $deployOn(AspectRegistryIfc reg, Object aspectObj) {
 		
 		AspectContainerIfc cont = reg.$getAspectContainer();
 		AspectList localAspects = null;
 		
 		/* setup appropriate aspect container in the registry */
 		if (cont == null) {		
 			localAspects = new AspectList();
 			reg.$setAspectContainer(localAspects);
 		}
 		else if (cont.$getContainerType() == AspectContainerIfc.LOCAL_CONTAINER) {
 			localAspects = (AspectList)cont;
 		}
 		else if (cont.$getContainerType() == AspectContainerIfc.COMPOSITE_CONTAINER) {
 			CompositeAspectContainer composite = (CompositeAspectContainer)cont;
 			localAspects = (AspectList)composite.findContainer(AspectContainerIfc.LOCAL_CONTAINER);
 			if (localAspects == null) {
 				localAspects = new AspectList();
 				composite.getList().add(localAspects);				
 			}
 		}
 		else {
 			CompositeAspectContainer composite = new CompositeAspectContainer();
 			localAspects = new AspectList();
 			composite.getList().add(cont);
 			composite.getList().add(localAspects);
 			reg.$setAspectContainer(composite);			
 		}
 		
 		/* deploy the object */
 		localAspects.getList().add(aspectObj);		
 	}
 
 	/**
 	 * Undeploy the object from the given registry
	 * Assumes that the object is deployed on the current thread
 	 * 
 	 * @param reg			Registry instance
 	 * @param aspectObj		Aspect object
 	 */
 	public void $undeployFrom(AspectRegistryIfc reg, Object aspectObj) {
 		
 		AspectContainerIfc cont = reg.$getAspectContainer();
 		AspectList localAspects = null;
 		
 		if (cont == null) {
 			return; // ignore
 		}
 		else if (cont.$getContainerType() == AspectContainerIfc.LOCAL_CONTAINER) {
 			localAspects = (AspectList)cont;
 			localAspects.getList().remove(aspectObj);
 			
 			if (localAspects.getList().isEmpty()) {
 				reg.$setAspectContainer(null);
 			}
 		}
 		else if (cont.$getContainerType() == AspectContainerIfc.COMPOSITE_CONTAINER) {
 			CompositeAspectContainer composite = (CompositeAspectContainer)cont;
 			localAspects = (AspectList)composite.findContainer(AspectContainerIfc.LOCAL_CONTAINER);
 			
 			if (localAspects != null) {
 				localAspects.getList().remove(aspectObj);
 				
 				if (localAspects.getList().isEmpty()) {
 					composite.getList().remove(localAspects);
 					
 					if (composite.getList().size() < 2)	{
 						reg.$setAspectContainer((AspectContainerIfc)composite.getList().get(0));
 					}
 				}
 			}
 		}
 	}
 
 }
